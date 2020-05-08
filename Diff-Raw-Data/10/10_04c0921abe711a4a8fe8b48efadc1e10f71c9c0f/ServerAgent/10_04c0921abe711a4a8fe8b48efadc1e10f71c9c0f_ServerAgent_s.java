 package server;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Set;
 
 import lib.PeerAwareAgent;
 import lib.dao.RequestDAO;
 import lib.dao.UserDAO;
 import lib.entities.Request;
 import lib.entities.SearchRequest;
 import lib.entities.SearchResponse;
 import lib.entities.User;
 import lib.utils.Utility;
 
 public class ServerAgent extends PeerAwareAgent {
 
 	private static final long serialVersionUID = 2L;
 	private RequestForwarder requestForwarder;
 	private ResponseReceiver responseReceiver;
 	private RequestReceiver requestReceiver;
 	
 	private UserDAO userDAO;
 	private RequestDAO requestDAO;
 
 	
 	@Override
 	protected void setup() {
 		System.out.println("Hallo I'm the ServerAgent! My name is " + getAID().getName());
 		
 		//SearchRequestDAO dao = new SearchRequestDAO();
 		//dao.setPersistenceUnit("swazam_server");
 		//SearchRequest searchRequest = new SearchRequest();
 		//searchRequest.setAcessToken("test");
 		//dao.persist(searchRequest);
 
 		userDAO = new UserDAO();
 		userDAO.setPersistenceUnit("swazam_server");
 		requestDAO = new RequestDAO();
 		requestDAO.setPersistenceUnit("swazam_server");
 		Object[] args = getArguments();
 		if (args != null && args.length != 0) {
 			if (args.length > 0) {
 
 			}
 
 		} else {
 
 			// doDelete();
 			// return;
 		}
 
 		DFAgentDescription agentDescription = new DFAgentDescription();
 		agentDescription.setName(getAID());
 		ServiceDescription serviceDescription = new ServiceDescription();
 		serviceDescription.setType("SWAzamServer");
 		serviceDescription.setName("SWAzam Server");
 		agentDescription.addServices(serviceDescription);
 		try {
 			DFService.register(this, agentDescription);
 		} catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 
 		// 1) receive requests from clients
 		requestReceiver = new RequestReceiver();
 		addBehaviour(requestReceiver);
 
 		// 2) get responses, do transaction stuff (coins) and forward the
 		// response back to the client
 		responseReceiver = new ResponseReceiver();
 		addBehaviour(responseReceiver);
 		super.setup();
 	}
 
 	@Override
 	protected void takeDown() {
 		try {
 			DFService.deregister(this);
 		} catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 		log("ServerAgent " + getAID().getName() + " sais good bye");
 	}
 
 	private class RequestReceiver extends CyclicBehaviour {
 
 		private static final long serialVersionUID = 24L;
 
 		@Override
 		public void action() {
 
 			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
 			ACLMessage message = myAgent.receive(mt);
 
 			if (message != null) {
 				SearchRequest request;
 				User requestingUser;
 				Request requestDB;
 				
 				try {
 					request = (SearchRequest) Utility.fromString(message.getContent());
 					log("Request received from client \"" + request.getInitiator().getLocalName() + "\" accessToken: "+request.getAccessToken());	
 					
 					/*
 					//TestUser
 					User testUser = new User();
 					testUser.setUsername("Mr. Tester");
 					testUser.setPassword("password");
 					testUser.setCoins(1);
 					testUser.setToken("1234");
 					userDAO.persist(testUser);
 					*/
 					
 					
 					//Storing the request
 					requestingUser = (User) userDAO.findByToken(request.getAccessToken()).get(0);
 					requestDB = new Request(requestingUser, new Date());
 					requestDAO.persist(requestDB);
 					
 					//Mapping the request to requesting user
 					Set<Request> requestingSet = requestingUser.getRequestsForSenderId();
 					requestingSet.add(requestDB);
 					requestingUser.setRequestsForSenderId(requestingSet);
 					userDAO.merge(requestingUser);
 					
 					//Storing the automated id onto the SearchRequest for later identification
 					request.setId(requestDB.getId());
 					
 					
 					addRequestForForwarding(request);
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				} catch (IndexOutOfBoundsException e) {
 					e.printStackTrace();
 				}
 
 			} else {
 				block();
 			}
 
 		}
 	}
 
 	private class ResponseReceiver extends CyclicBehaviour {
 
 		private static final long serialVersionUID = 23L;
 		
 		@Override
 		public void action() {
 			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
 					MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
 			ACLMessage responseMessage = myAgent.receive(mt);
 			if (responseMessage != null) {
 				if (responseMessage.getPerformative() == ACLMessage.CONFIRM) {
 					AID aid = responseMessage.getSender();
 					if (peers.contains(aid)) {
 						availablePeers.add(aid); // Peer has done its job and is
 													// now
 													// queued again for the next
 													// one
 					}
 
 					SearchResponse searchResponse = null;
 					try {
 						searchResponse = (SearchResponse) Utility.fromString(responseMessage.getContent());
 					} catch (IOException e) {
 						e.printStackTrace();
 						return;
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 						return;
 					}
 
 					if (searchResponse.wasFound()) {
 						log("Response received from user with AccessToken : " + searchResponse.getRespondentToken());
 						
 						/*
 						//testUser
 						User testUser2 = new User();
 						testUser2.setUsername("Mrs. Testerina");
 						testUser2.setPassword("password");
 						testUser2.setCoins(2);
 						testUser2.setToken(searchResponse.getRespondentToken());
 						userDAO.persist(testUser2); 
 						*/
 						
 						try {	
 							User requestingUser = userDAO.findByToken(searchResponse.getSearchRequest().getAccessToken()).get(0);
 							User respondingUser = userDAO.findByToken(searchResponse.getRespondentToken()).get(0);
 							log("Initiating coin transfer between requester: " + requestingUser.getToken() + " and respondent: " + respondingUser.getToken());
 							
 							log("Pre-transaction | Requesting user coins: " + requestingUser.getCoins() + " | Responding user coins: " + respondingUser.getCoins());
 							requestingUser.decrementCoins();
 							respondingUser.incrementCoins();
 							log("Post-transaction| Requesting user coins: " + requestingUser.getCoins() + " | Responding user coins: " + respondingUser.getCoins());
 							
 							Request requestDB = requestDAO.findByID(Request.class, searchResponse.getSearchRequest().getId());
 							requestDB.setSolved(new Date());
 							requestDB.setUserBySolverId(respondingUser);
 							requestDB.setSolution(searchResponse.toString());
 							
 							//Store updated users/request
							userDAO.merge(requestingUser);
							userDAO.merge(respondingUser);
							requestDAO.merge(requestDB);
 							
 							//log to make sure request was stored properly
 							log("Request solved: " + requestDAO.findByID(Request.class, searchResponse.getSearchRequest().getId()).getSolved());
 
 						} catch (IndexOutOfBoundsException e) {
 							e.printStackTrace();
 						}
 						
 						
 						ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
 						message.addReceiver(searchResponse.getSearchRequest().getInitiator());
 						message.setContent(searchResponse.serialize());
 						message.setConversationId("found-fingerPrint");
 						message.setReplyWith("message_" + myAgent.getName() + "_" + System.currentTimeMillis());
 						myAgent.send(message);
 						log("forwarding search response from peer to client: " + searchResponse.toString());
 
 						// server.newResponse(searchResponse);
 
 					} else {
 						// TODO: notify client
 						log("no response found (timed out)");
 					}
 
 				} else if (responseMessage.getPerformative() == ACLMessage.REFUSE) {
 					log("Peer rejected because: " + responseMessage.getContent());
 				}
 
 			} else {
 				block();
 			}
 
 		}
 
 	}
 
 }
