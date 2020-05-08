 package nl.uu.cs.map.jade.agent;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.UnreadableException;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 
 import nl.uu.cs.map.jade.ItemDB;
 import nl.uu.cs.map.jade.ItemDescriptor;
 
 public class MatchmakerAgent extends Agent {
 
 	private static final long serialVersionUID = 8758414317735123415L;
 
 	private ItemDB itemDB;
 
 	@Override
 	protected void setup() {
 		itemDB = ItemDB.getInstance();
 
 		// register at the DF
 		DFAgentDescription dfd = new DFAgentDescription();
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("matchmaking");
 		sd.setName("Market-Matchmaker");
 		dfd.addServices(sd);
 
 		try {
 			DFService.register(this, dfd);
 		} catch (FIPAException e) {
 			e.printStackTrace();
 		}
 
 		// respond to registration requests from traders
 		addBehaviour(new ContactMatchmakerBehaviour());
 
 		// respond to matchmaking requests from traders
 		addBehaviour(new MatchmakingBehaviour());
 	}
 
 	@Override
 	protected void takeDown() {
 		try {
 			DFService.deregister(this);
 		} catch (FIPAException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Responds to registration requests from agents.
 	 * 
 	 * @author robert
 	 * 
 	 */
 	private class ContactMatchmakerBehaviour extends Behaviour {
 		private static final long serialVersionUID = 8173086777284670349L;
 		private boolean done = false;
 
 		// only receive registration and deregistration messages
 		private final MessageTemplate registrationTemplate = MessageTemplate
				.and(MessageTemplate.and(
 						MessageTemplate.MatchProtocol("registerOffers"),
 						MessageTemplate.MatchProtocol("registerRequests")),
						MessageTemplate.and(MessageTemplate
 								.MatchProtocol("deregisterOffers"),
 								MessageTemplate
 										.MatchProtocol("deregisterRequests")));
 
 		// casting of contentObject
 		@SuppressWarnings("unchecked")
 		@Override
 		public void action() {
 			ACLMessage registration = MatchmakerAgent.this
 					.receive(registrationTemplate);
 			if (registration != null) {
 				AID sender = registration.getSender();
 				String protocol = registration.getProtocol();
 				if ("registerOffers".equals(protocol)) {
 					try {
 						// register this agent along with its offers
 						for (ItemDescriptor offer : (List<ItemDescriptor>) registration
 								.getContentObject())
 							itemDB.addOffer(offer, sender);
 					} catch (UnreadableException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 				} else if ("registerRequests".equals(protocol)) {
 					try {
 						// register this agent along with its requests
 						for (ItemDescriptor request : (List<ItemDescriptor>) registration
 								.getContentObject())
 							itemDB.addRequest(request, sender);
 					} catch (UnreadableException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 				} else if ("deregisterOffers".equals(protocol)) {
 					try {
 						// deregister the offers that this agent previously had
 						for (ItemDescriptor offer : (List<ItemDescriptor>) registration
 								.getContentObject())
 							itemDB.removeOffer(offer, sender);
 					} catch (UnreadableException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 				} else if ("deregisterRequests".equals(protocol)) {
 					try {
 						// deregister the requests that this agent previously
 						// had
 						for (ItemDescriptor request : (List<ItemDescriptor>) registration
 								.getContentObject())
 							itemDB.removeRequest(request, sender);
 					} catch (UnreadableException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 				}
 			} else
 				block();
 		}
 
 		@Override
 		public boolean done() {
 			return done;
 		}
 	}
 
 	/**
 	 * Responds to matchmaking requests from agents.
 	 * 
 	 * @author robert
 	 * 
 	 */
 	private class MatchmakingBehaviour extends Behaviour {
 		private static final long serialVersionUID = 4672330365713782422L;
 		private boolean done = false;
 
 		// only receive matchmaking messages
 		private MessageTemplate matchmakingTemplate = MessageTemplate.or(
 				MessageTemplate.MatchProtocol("getOffers"),
 				MessageTemplate.MatchProtocol("getRequests"));
 
 		@Override
 		public void action() {
 			ACLMessage matchmaking = MatchmakerAgent.this
 					.receive(matchmakingTemplate);
 			if (matchmaking != null) {
 				AID sender = matchmaking.getSender();
 				String protocol = matchmaking.getProtocol();
 				ItemDescriptor item;
 				try {
 					item = (ItemDescriptor) matchmaking.getContentObject();
 				} catch (UnreadableException e) {
 					throw new RuntimeException(e.getMessage(), e);
 				}
 
 				// create reply message with the reply-with content that the
 				// agent has chosen to be able to detect which of his requests
 				// has been answered
 				ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
 				reply.addReceiver(sender);
 				reply.setSender(getAID());
 				reply.setReplyWith(matchmaking.getReplyWith());
 				if ("getOffers".equals(protocol)) {
 					// reply with a list of traders that offer this item
 					reply.setProtocol("setOffers");
 					try {
 						reply.setContentObject((Serializable) itemDB
 								.getSellers(item));
 					} catch (IOException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 					send(reply);
					System.out.println("Matchmaker responded to getOffers request.");
 				} else if ("getRequests".equals(protocol)) {
 					// reply with a list of traders that request this item
 					reply.setProtocol("setRequests");
 					try {
 						reply.setContentObject((Serializable) itemDB
 								.getBuyers(item));
 					} catch (IOException e) {
 						throw new RuntimeException(e.getMessage(), e);
 					}
 					send(reply);
					System.out.println("Matchmaker responded to getRequests request.");
 
 				}
 			} else
 				block();
 		}
 
 		@Override
 		public boolean done() {
 			return done;
 		}
 
 	}
 
 }
