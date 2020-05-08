 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
  * by the @authors tag. All rights reserved.
  * See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU Lesser General Public License, v. 2.1.
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License,
  * v.2.1 along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 package org.savara.sam.ams.conversations;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jms.Destination;
 import javax.jms.MessageListener;
 
 import org.infinispan.util.concurrent.NotifyingFuture;
 import org.savara.monitor.ConversationId;
 import org.savara.monitor.Message;
 import org.savara.monitor.MonitorResult;
 import org.savara.protocol.ProtocolCriteria;
 import org.savara.protocol.ProtocolCriteria.Direction;
 import org.savara.protocol.ProtocolId;
 import org.savara.protocol.repository.ProtocolRepository;
 import org.savara.sam.activity.ActivityModel.Activity;
 import org.savara.sam.activity.ConversationDetails;
 import org.savara.sam.activity.ServiceModel;
 import org.savara.sam.activity.ServiceModel.ServiceInvocation;
 import org.savara.sam.activity.Situation;
 import org.savara.sam.activity.Situation.Priority;
 import org.savara.sam.activity.Situation.Severity;
 import org.savara.sam.aq.ActiveChangeType;
 import org.savara.sam.aq.ActiveQuery;
 import org.savara.sam.aqs.ActiveQueryServer;
 import org.savara.sam.aqs.JEEActiveQueryManager;
 import org.savara.sam.aqs.JEECacheActiveQuerySpec;
 import org.scribble.common.resource.ResourceContent;
 import org.scribble.protocol.DefaultProtocolContext;
 import org.scribble.protocol.ProtocolContext;
 import org.scribble.protocol.model.ProtocolModel;
 import org.scribble.protocol.model.Role;
 
 public class ConversationManager extends JEEActiveQueryManager<String,ConversationId> implements MessageListener {
 	
 	private static final Logger LOG=Logger.getLogger(ConversationManager.class.getName());
 	
 	private org.savara.monitor.Monitor _monitor=null;
 	private org.infinispan.Cache<String,Activity> _activities=null;
 	private org.infinispan.Cache<ConversationId,ConversationDetails> _conversationDetails=null;
 	private org.infinispan.manager.CacheContainer _container=null;
 	private ActiveQuery<Situation> _situations=null;
 	
 	private XPathConversationResolver _resolver=new XPathConversationResolver();
 	
 	public ConversationManager(String conversationName) {
 		super(new JEECacheActiveQuerySpec<ConversationId,ConversationDetails>(conversationName,
 					ConversationDetails.class, ConversationId.class), null);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void init(String model, org.infinispan.manager.CacheContainer container,
 				Destination source, Destination notification, Destination... destinations) {
 		super.init(container, source, notification, destinations);
 		
 		_container = container;
 		
 		java.net.URL url=Thread.currentThread().getContextClassLoader().getResource("/"+model);
 		
 		if (LOG.isLoggable(Level.FINE)) {
 			LOG.fine("Loading model '"+model+"' from URL: "+url);		
 		}
 		
 		try {
 			initMonitor(url.toURI());
 			
 		} catch(Exception e) {
 			LOG.log(Level.SEVERE, "Failed to initialize monitor", e);
 		}
 		
 		_activities = _container.getCache("activities");
 		_conversationDetails = _container.getCache("conversationDetails");
 		
 		((JEECacheActiveQuerySpec<ConversationId,ConversationDetails>)getActiveQuerySpec()).setCache(_conversationDetails);
 		
 		_situations = ActiveQueryServer.getInstance().getActiveQuery("Situations");
 	}
 	
 	protected XPathConversationResolver getResolver() {
 		return(_resolver);
 	}
 	
 	protected void initMonitor(java.net.URI uri) {
 		ProtocolRepository pr=new InJarProtocolRepository(uri);
 		
 		_monitor = new org.savara.monitor.impl.DefaultMonitor();
 		_monitor.setProtocolRepository(pr);
 		_monitor.setConversationResolver(_resolver);
 		_monitor.setSessionStore(new CachedSessionStore(_container));
 	}
 
 	@Override
 	protected ConversationId processActivity(String sourceAQName, String id, ActiveChangeType changeType,
 								int retriesLeft) throws Exception {
 		ConversationId ret=null;
 		
		Activity act=_activities.get(id);
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Got cached activity with key '"+id+"' ret="+ret);
		}
		/*
 		NotifyingFuture<Activity> future=_activities.getAsync(id);
 		try {
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Get cached activity with key '"+id+"'");
 			}
 			act = future.get(1000, TimeUnit.MILLISECONDS);
 			
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Got cached activity with key '"+id+"' ret="+ret);
 			}
 		} catch(Exception e) {
 			future.cancel(false);
 			
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Failed to get cached activity with key '"+id+"'");
 			}
 		}
		*/
 		
 		if (act == null) {
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Conversation manager failed to retrieve activity '"+id+"' - retrying...");
 			}
 			if (retriesLeft == 0) {
 				LOG.severe("Failed to process activity for conversation '"+getActiveQueryName()+"'");
 			}
 			throw new Exception("Failed to find activity with activity id '"+id+"' - retrying");
 		} else if (LOG.isLoggable(Level.FINEST)) {
 			LOG.finest("Conversation manager retrieved activity '"+id+"'");
 		}
 		
 		Message mesg=new Message();
 		mesg.setDirection(act.getServiceInvocation().getDirection() ==
 					ServiceModel.ServiceInvocation.Direction.INBOUND ?
 							Direction.Inbound : Direction.Outbound);
 		mesg.setOperator(act.getServiceInvocation().getOperation());
 		mesg.setFault(act.getServiceInvocation().getFault());
 		
 		for (org.savara.sam.activity.ServiceModel.Message sm :
 						act.getServiceInvocation().getMessageList()) {
 			mesg.getTypes().add(sm.getMessageType());
 			mesg.getValues().add(sm.getContent());
 		}
 		
 		if (LOG.isLoggable(Level.FINEST)) {
 			LOG.finest("Monitor activity="+act+" message="+mesg);
 		}
 		
 		MonitorResult result=_monitor.process(null, null, mesg);
 		
 		if (LOG.isLoggable(Level.FINEST)) {
 			LOG.finest("Monitored activity="+act+" message="+mesg+" result="+result);
 		}
 		
 		if (result != null) {
 			
 			// TODO: Could have a retry mechanism here, if the result is invalid
 
 			if (result.getConversationId() != null) {
 				
 				// If retries remaining, and result is invalid, then retry
 				if (retriesLeft > 0 && !result.isValid()) {
 					if (LOG.isLoggable(Level.FINEST)) {
 						LOG.finest("Conversation result="+result+
 							" message="+mesg+" failed to validate: "+act+" - so retrying...");
 					}
 					throw new Exception("Conversation '"+result.getConversationId()+
 							"' failed to validate: "+act);
 				} else if (!result.isValid()) {
 					if (LOG.isLoggable(Level.FINEST)) {
 						LOG.finest("Conversation result="+result+
 							"' message="+mesg+" failed");
 					}
 				}
 				
 				// Add activity summary to conversation details
 				ConversationDetails details=_conversationDetails.get(result.getConversationId());
 				
 				if (details == null) {
 					if (LOG.isLoggable(Level.FINEST)) {
 						LOG.finest("Creating Conversation Details for "+result.getConversationId());
 					}
 					details = new ConversationDetails(result.getConversationId());
 					_conversationDetails.put(result.getConversationId(), details);
 				}
 				
 				boolean wasValid=details.isValid();
 				
 				details.addActivity(id, act, result);
 				
 				if (LOG.isLoggable(Level.FINEST)) {
 					LOG.finest("Updating Conversation Details for cid="+
 								result.getConversationId()+" : added activity "+act);
 				}
 				_conversationDetails.replace(result.getConversationId(), details);
 				
 				ret = result.getConversationId();
 				
 				// If failed, then record situation if this activity caused the
 				// conversation instance to be invalid
 				if (!result.isValid() && wasValid) {
 					Situation situation=new Situation(act.getPrincipal(),
 							"Conversation '"+getActiveQueryName()+"' instance '"+
 								result.getConversationId()+
 								"' validation failure",
 								Severity.Major, Priority.High);
 					
 					situation.setProperty(null, "ActivityId", id);
 					situation.setProperty(null, "ConversationId", result.getConversationId().getId());
 					situation.setProperty(null, "InteractionSummary", getInteractionSummary(act));
 					
 					_situations.add(situation);
 				}
 				
 			} else {
 				LOG.severe("Monitor returned valid="+result.isValid()+" result, but with no conversation id");
 			}
 		}
 		
 		return(ret);
 	}
 		
 	@Override
 	protected ActiveChangeType processChangeType(ConversationId targetActivity, ActiveChangeType changeType) {
 		ActiveChangeType ret=changeType;
 		
 		switch (changeType) {
 		case Add:
 			ActiveQuery<ConversationId> aq=getActiveQuery();
 			if (aq != null) {
 				// If activity already in contents, then change to update
 				if (aq.getContents().contains(targetActivity)) {
 					changeType = ActiveChangeType.Update;
 				}
 			}
 			break;
 		case Update:
 			break;
 		case Remove:
 			break;
 		}
 		
 		return(ret);
 	}
 	
 	protected String getInteractionSummary(Activity act) {
 		String ret=(act.getServiceInvocation().getDirection() == ServiceInvocation.Direction.INBOUND ?
 						"Receiving " : "Invoking ");
 				
 		ret += act.getServiceInvocation().getOperation()+"(";
 		
 		for (int i=0; i < act.getServiceInvocation().getMessageList().size(); i++) {
 			ServiceModel.Message m=act.getServiceInvocation().getMessageList().get(i);
 			ret += m.getMessageType();
 			
 			if (i < act.getServiceInvocation().getMessageList().size()-1) {
 				ret += ",";
 			}
 		}
 		
 		ret += ") on "+act.getServiceInvocation().getServiceType();
 		
 		return(ret);
 	}
 
 	public static class InJarProtocolRepository implements ProtocolRepository {
 		
 		private ProtocolModel _model=null;
 		private java.util.Map<ProtocolId,ProtocolModel> _localModels=
 						new java.util.HashMap<ProtocolId,ProtocolModel>();
 		
 		public InJarProtocolRepository(java.net.URI uri, String... roles) {
 			
 			org.scribble.protocol.parser.ProtocolParser parser=
 					new org.savara.pi4soa.cdm.parser.CDMProtocolParser();
 			org.scribble.protocol.projection.ProtocolProjector projector=
 					new org.scribble.protocol.projection.impl.ProtocolProjectorImpl();
 			ProtocolContext context=new DefaultProtocolContext();
 			
 			org.scribble.common.logging.Journal journal=
 					new org.scribble.common.logging.CachedJournal();
 			
 			try {
 				_model = parser.parse(context, new ResourceContent(uri), journal);
 
 				// If roles not explicitly defined, then initialise all roles
 				if (roles == null || roles.length == 0) {
 					java.util.List<Role> rlist=_model.getRoles();
 					roles = new String[rlist.size()];
 					for (int i=0; i < rlist.size(); i++) {
 						roles[i] = rlist.get(i).getName();
 					}
 				}
 				
 				// Project to relevant roles
 				for (String role : roles) {
 					ProtocolId pid=new ProtocolId(_model.getProtocol().getName(), role);
 					
 					ProtocolModel lm=projector.project(context, _model,
 									new Role(role), journal);
 					
 					_localModels.put(pid, lm);
 				}
 				
 			} catch(Exception e) {
 				LOG.log(Level.SEVERE, "Failed to parse protocol model '"+uri+"'", e);
 			}
 		}
 
 		public ProtocolModel getProtocol(ProtocolId pid) {
 			return (_localModels.get(pid));
 		}
 
 		public List<ProtocolId> getProtocols(ProtocolCriteria criteria) {
 			// TODO: Need to filter out the protocols that are related to
 			// the supplied criteria (e.g. message types etc).
 			List<ProtocolId> ret=new java.util.Vector<ProtocolId>(_localModels.keySet());
 			
 			if (LOG.isLoggable(Level.FINEST)) {
 				LOG.finest("Get protocol ids for criteria="+criteria+" ret="+ret);
 			}
 			
 			return (ret);
 		}
 	}
 }
