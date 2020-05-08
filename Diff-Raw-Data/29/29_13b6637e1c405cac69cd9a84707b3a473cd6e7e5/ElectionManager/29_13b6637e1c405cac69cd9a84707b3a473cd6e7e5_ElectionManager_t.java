 package org.ssor.protocol.election;
 
 import java.util.Queue;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.ssor.protocol.AbstractProtocolManager;
 import org.ssor.protocol.Command;
 import org.ssor.protocol.Header;
 import org.ssor.protocol.Message;
 import org.ssor.protocol.Token;
 
 public class ElectionManager extends AbstractProtocolManager {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(ElectionManager.class);
 
 	
 	public boolean handleReceive(Message message, Object address, int addressUUID){
 		Header header = message.getHeader();
 		if (header instanceof InterestHeader
 				&& addressUUID != group.getUUID_ADDR()) {
 			
 			
 			// subsequence join node does not need to reply
 			assign(message, address);
 			return true;
 
 		} else if (header instanceof DecisionHeader && group.isConsensus()) {
 
 			record(message, address);
 			return true;
 
 		} else if (header instanceof org.ssor.protocol.election.AgreementHeader) {
 
 			// If this is the subsequence join node, discard this
 			if(group.isConsensus()){
 				//viewSynchronizationManager.releaseOnAgreement(util.getUUIDFromAddress(address));
 				return true;
 			}
 			
 			doAgreement(message, address);
 			return true;
 		}
 		return false;
 	}
 
 	public Object join(Queue<Integer> view) {
 		
 		
 		synchronized(this){
 			try {
 				this.wait((long)2000);
 			} catch (InterruptedException e1) {
 				e1.printStackTrace();
 			}
 		}
 		
 		try {
 			return protocolStack.down(Command.ELECTION_FIRST_BROADCAST, new Token(view));
 		} catch (RuntimeException e) {
			logger.error("The channel is not connected, will try again in 2 sec", e);
 			return join(view);
 		}
 	}
 
 	public Object assign(Message message, Object address) {
 
 		// If this node potential would become new sequencer, then it sync
 		// otherwise it would have any effect since it would not reply
 		// suspendProcess(logger);
 		// Should be array of Decision
 		Token token = protocolStack.up(Command.ELECTION_JOIN, new Token(message));
 		
 		if (Token.ELECTION_JOIN_DISCARD == token.getNextAction())
 			return null;
 		
 		if (Token.ELECTION_JOIN_INTERSTS_ONLY == token.getNextAction()) {
 
 			if (logger.isTraceEnabled()) {
 				logger
 						.trace("Receive join request and reply to new join node, but reply its interests only");
 			}
 
 		} else if (logger.isTraceEnabled()) {
 			logger.trace("Receive join request and reply to new join node");
 		}
 
 		return protocolStack.down(Command.ELECTION_UNICAST, new Token(new ElectionUnit(
 				address, Token.ELECTION_JOIN_INTERSTS_ONLY == token.getNextAction()? null : (Decision[]) token.getDataForNextProtocol())));
 
 	}
 
 	public Object record(Message message, Object address) {
 
 		Token token = protocolStack.up(Command.ELECTION_RECORD, new Token(message));
 
 		if (logger.isTraceEnabled()) {
 			logger
 					.trace("Receive reply from node: " + address + ", and broadcast if all regions have been set, otherwise wait");
 		}
 
 		if (Token.ELECTION_RECORD_FINAL_BROADCAST == token.getNextAction()){
 			
 			return protocolStack.down(Command.ELECTION_SECOND_BROADCAST, new Token(Token.ELECTION_RECORD_FINAL_BROADCAST_FOR_THIS, null));
 		}
 		return null;
 	}
 
 	/**
 	 * The actual phase that add a new node
 	 * 
 	 * @param message
 	 * @param address
 	 * @return
 	 */
 	public Object doAgreement(Message message, Object address) {
 
 		// If receive new join node during consensus, trigger the recording
 		// phase as well
 		// in case of the three concurrent join node problem
 		/*
 		 * if(!group.isConsensus()){
 		 * 
 		 * if(logger.isTraceEnabled()){ logger.trace("Receive join agreement
 		 * during consensus, thus trigger the record phase"); }
 		 * 
 		 * Integer[] regions = (Integer[])message.getBody(); Decision[]
 		 * decisions = new Decision[regions.length]; for(int i =0; i <
 		 * regions.length; i++) decisions[i] = new Decision(regions[i], true);
 		 *  // Recording record(new Message(new DecisionHeader(), decisions,
 		 * false), address); }
 		 */
 
 		if (logger.isTraceEnabled()) {
 			logger
 					.trace("Receive agreement result from election, add new node or change new sequencer");
 		}
 
 		Token token = protocolStack.up(Command.ELECTION_AGREEMENT, new Token(message));
 		// If false means the joined node does not have responsible regions 
 		if (Token.ELECTION_AGREEMENT_NO_NEW_REGION == token.getNextAction()){
 			return false;
 		}
 		if (logger.isTraceEnabled()) {
 			logger.trace("Trigger the FT_UNICAST phase");
 		}
 
 		return protocolStack.down(Command.FT_UNICAST, new Token(message));
 	}
 }
