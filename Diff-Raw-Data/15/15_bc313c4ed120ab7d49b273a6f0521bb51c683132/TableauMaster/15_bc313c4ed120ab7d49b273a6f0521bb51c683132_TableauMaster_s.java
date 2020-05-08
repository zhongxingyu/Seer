 package edu.iastate.pdlreasoner.master;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.log4j.Logger;
 import org.jgroups.Address;
 import org.jgroups.Channel;
 import org.jgroups.ChannelClosedException;
 import org.jgroups.ChannelException;
 import org.jgroups.ChannelFactory;
 import org.jgroups.ChannelNotConnectedException;
 import org.jgroups.Message;
 import org.jgroups.ReceiverAdapter;
 import org.jgroups.View;
 
 import edu.iastate.pdlreasoner.exception.NotEnoughSlavesException;
 import edu.iastate.pdlreasoner.kb.OntologyPackage;
 import edu.iastate.pdlreasoner.kb.Query;
 import edu.iastate.pdlreasoner.kb.QueryResult;
 import edu.iastate.pdlreasoner.message.BackwardConceptReport;
 import edu.iastate.pdlreasoner.message.BranchTokenMessage;
 import edu.iastate.pdlreasoner.message.Clash;
 import edu.iastate.pdlreasoner.message.Exit;
 import edu.iastate.pdlreasoner.message.ForwardConceptReport;
 import edu.iastate.pdlreasoner.message.MakeGlobalRoot;
 import edu.iastate.pdlreasoner.message.MessageToMaster;
 import edu.iastate.pdlreasoner.message.MessageToSlave;
 import edu.iastate.pdlreasoner.message.Null;
 import edu.iastate.pdlreasoner.message.ResumeExpansion;
 import edu.iastate.pdlreasoner.message.SyncPing;
 import edu.iastate.pdlreasoner.message.TableauMasterMessageProcessor;
 import edu.iastate.pdlreasoner.model.PackageID;
 import edu.iastate.pdlreasoner.net.ChannelUtil;
 import edu.iastate.pdlreasoner.tableau.branch.BranchPointSet;
 import edu.iastate.pdlreasoner.util.CollectionUtil;
 
 public class TableauMaster {
 
 	private static final Logger LOGGER = Logger.getLogger(TableauMaster.class);
 	
 	private static enum State { ENTRY, EXPAND, CLASH, EXIT }
 	
 	private ChannelFactory m_ChannelFactory;
 	private BlockingQueue<Message> m_MessageQueue;
 	private State m_State;
 	private Channel m_Channel;
 	private Address m_Self;
 	
 	private TableauTopology m_Tableaux;
 	private InterTableauManager m_InterTableauMan;
 	private SyncManager m_SyncMan;
 	private Set<BranchPointSet> m_ClashCauses;
 	private QueryResult m_Result;
 	
 	//Processors
 	private TableauMasterMessageProcessor m_MessageProcessor;
 	
 	public TableauMaster(ChannelFactory channelFactory) {
 		m_ChannelFactory = channelFactory;
 		m_MessageQueue = new LinkedBlockingQueue<Message>();
 		m_State = State.ENTRY;
 	}
 	
 	public QueryResult run(Query query) throws ChannelException, NotEnoughSlavesException {
 		initChannel();
 		connectWithSlaves(query.getOntology().getPackages());
 		initMaster(query);
 		startExpansion(query);
 		
 		while (m_State != State.EXIT) {
 			switch (m_State) {
 			case EXPAND:
 				m_SyncMan.restartSync();
 				while (m_State == State.EXPAND && (!m_MessageQueue.isEmpty() || !m_SyncMan.isSynchronized())) {
 					processOneTableauMessage();
 				}
 				
 				if (m_State != State.EXPAND) break;
 				
 				exitWithResult(true);
 				break;
 				
 			case CLASH:
 				m_SyncMan.restartSync();
 				while (m_State == State.CLASH && (!m_MessageQueue.isEmpty() || !m_SyncMan.isSynchronized())) {
 					processOneTableauMessage();
 				}
 				
 				if (m_State != State.CLASH) break;
 				
 				resume();
 				break;
 			}
 		}
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Disconnecting and closing channel");
 		}
 		
 		m_Channel.disconnect();
 		m_Channel.close();	
 		return m_Result;
 	}
 	
 	public void send(PackageID destID, Serializable msg) {
 		Address dest = m_Tableaux.get(destID);
 		Message channelMsg = new Message(dest, m_Self, msg);
 		try {
 			m_Channel.send(channelMsg);
 		} catch (ChannelNotConnectedException e) {
 			throw new RuntimeException(e);
 		} catch (ChannelClosedException e) {
 			throw new RuntimeException(e);
 		}
 		
 		m_SyncMan.intercept(destID, msg);
 	}
 
 	private Message takeOneMessage() {
 		Message msg = null;
 		do {
 			try {
 				msg = m_MessageQueue.take();
 			} catch (InterruptedException e) {}
 		} while (msg == null);
 		return msg;
 	}
 
 	private void processOneTableauMessage() {
 		Message msg = takeOneMessage();
 		MessageToMaster tabMsg = (MessageToMaster) msg.getObject();
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Received " + tabMsg);
 		}
 		
 		tabMsg.execute(m_MessageProcessor);
 	}
 	
 	private void broadcast(MessageToSlave msg) {
 		for (PackageID packageID : m_Tableaux) {
 			send(packageID, msg);
 		}
 	}
 
 	private void initChannel() throws ChannelException {
 		m_Channel = m_ChannelFactory.createChannel();
 		m_Channel.connect(ChannelUtil.getSessionName());
 		m_Channel.setReceiver(new ReceiverAdapter() {
 				public void receive(Message msg) {
 					while (true) {
 						try {
 							m_MessageQueue.put(msg);
 							return;
 						} catch (InterruptedException e) {}
 					}
 				}
 			});
 		m_Self = m_Channel.getLocalAddress();
 	}
 
 	private void connectWithSlaves(List<OntologyPackage> packages) throws NotEnoughSlavesException {
 		View view = m_Channel.getView();
 		List<Address> m_SlaveAdds = CollectionUtil.makeList(view.getMembers());
 		m_SlaveAdds.remove(m_Self);
 		if (m_SlaveAdds.size() < packages.size()) {
 			m_Channel.disconnect();
 			m_Channel.close();
 			throw new NotEnoughSlavesException("Ontology has "
 					+ packages.size() + " packages but only "
 					+ m_SlaveAdds.size() + " slaves are available.");
 		}
 
 		m_Tableaux = new TableauTopology(packages, m_SlaveAdds);
 		m_SyncMan = new SyncManager(this, m_Tableaux);
 		
 		for (PackageID packageID : m_Tableaux) {
 			send(packageID, packageID);
 		}
 	}
 
 	private void initMaster(Query query) {
 		m_InterTableauMan = new InterTableauManager(this, query.getOntology().getImportGraph());
 		m_ClashCauses = CollectionUtil.makeSet();
 		m_MessageProcessor = new TableauMessageProcessorImpl();
 		m_Result = new QueryResult();
 		m_Result.setQuery(query);
 	}
 
 	private void startExpansion(Query query) throws ChannelNotConnectedException, ChannelClosedException {
 		PackageID witnessID = query.getWitnessID();
 		send(witnessID, new MakeGlobalRoot(query.getSatConcept()));
 		
 		for (PackageID otherID : m_Tableaux) {
 			if (otherID.equals(witnessID)) continue;
 			send(otherID, Null.INSTANCE);
 		}
 		
 		m_State = State.EXPAND;
 	}
 	
 	private void resume() {
 		BranchPointSet clashCause = Collections.min(m_ClashCauses, BranchPointSet.ORDER_BY_LATEST_BRANCH_POINT);
 		m_ClashCauses.clear();
 		if (clashCause.isEmpty()) {
 			exitWithResult(false);
 			return;
 		}
 		
 		m_InterTableauMan.pruneTo(clashCause.getLatestBranchPoint());
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("All prunings completed, resuming expansion.");
 		}
 		
 		broadcast(new ResumeExpansion(clashCause));
 		m_State = State.EXPAND;
 	}
 
 	private void exitWithResult(boolean result) {
 		m_Result.setIsSatisfiable(result);
 		broadcast(Exit.INSTANCE);
 		m_State = State.EXIT;
 	}
 
 	
 	private class TableauMessageProcessorImpl implements TableauMasterMessageProcessor {
 
 		@Override
 		public void process(Clash msg) {
 			BranchPointSet clashCause = msg.getCause();
 			if (m_ClashCauses.add(clashCause)) {
 				broadcast(msg);
 				m_State = State.CLASH;
 			}
 		}
 
 		@Override
 		public void process(ForwardConceptReport msg) {
 			m_InterTableauMan.processConceptReport(msg);
 		}
 
 		@Override
 		public void process(BackwardConceptReport msg) {
 			m_InterTableauMan.processConceptReport(msg);
 		}
 
 		@Override
 		public void process(BranchTokenMessage msg) {
 			if (m_State == State.CLASH) return;
 
 			PackageID nextPackageID = m_Tableaux.getNext(msg.getPackageID());
 			send(nextPackageID, msg);
 		}
 
 		@Override
 		public void process(SyncPing msg) {
 			m_SyncMan.receiveResponse(msg);
 		}
 		
 	}
 	
 }
