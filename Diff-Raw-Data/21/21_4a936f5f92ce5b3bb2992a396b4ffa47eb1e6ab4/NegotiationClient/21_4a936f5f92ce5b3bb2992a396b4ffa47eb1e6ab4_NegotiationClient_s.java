 package geniuswebsocket;
 /*
  * socket.io-java-client Test.java
  *
  * Copyright (c) 2012, Enno Boland
  * socket.io-java-client is a implementation of the socket.io protocol in Java.
  * 
  * See LICENSE file for more information
  */
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Level;
 
 import io.socket.IOAcknowledge;
 import io.socket.IOCallback;
 import io.socket.SocketIO;
 import io.socket.SocketIOException;
 
 import negotiator.ActionListener;
 import negotiator.Agent;
 import negotiator.AgentID;
 import negotiator.Domain;
 import negotiator.WorldInformation;
 import negotiator.actions.*;
 import negotiator.exceptions.NegotiatorException;
 import negotiator.tournament.VariablesAndValues.AgentParamValue;
 import negotiator.tournament.VariablesAndValues.AgentParameterVariable;
 import negotiator.utility.UtilitySpace;
 
 import org.json.*;
 
 import agents.biu.KBAgent;
 
 /**
  * A socket.io client that negotiates with humans. 
  *
  * @author Erel Segal-Halevi
  * @since 2013-02
  */
 public class NegotiationClient implements IOCallback, Cloneable {
 	protected String roleOfThisAgent, roleOfOtherPlayer;
 	protected AgentID agentIdOfThisAgent, agentIdOfOtherPlayer;
 	
 	/**
 	 * socket for connecting to the Node.js negotiation server:
 	 */
 	protected SocketIO negotiationSocket;
 	
 	/**
 	 * agent for strategic negotiation
 	 */
 	protected Agent agent;
 	
 	/**
 	 * Genius negotiation domain 
 	 */
 	protected Domain domain;
 	
 	/**
 	 * full URL (http://host:port) of the socket.io server that handles the negotiation. 
 	 */
 	protected String serverUrl;
 	
 	/**
 	 * name of the game-class to join - from the games available on the game-server
 	 */
 	protected String gameType;
 
 
 	/**
 	 * @param domainFile full path to the Genius XML file with the domain data. 
 	 * @param serverUrl full URL (http://host:port) of the socket.io game-server that handles the negotiation.
 	 * @param gameType name of the game-class to join - from the games available on the game-server.
 	 * @throws Exception
 	 */
 	public NegotiationClient(Domain domain, String serverUrl, String gameType) {
 		this.domain = domain;
 		this.serverUrl = serverUrl;
 		this.gameType = gameType;
 		
 		roleOfThisAgent = "Candidate";
 		roleOfOtherPlayer = "Employer";
 	}
 	
 	@Override public NegotiationClient clone() {
 		return new NegotiationClient(domain, serverUrl, gameType);
 	}
 	
 	public void start() throws JSONException, IOException, NegotiatorException {
 		initializeAgent(roleOfThisAgent, roleOfOtherPlayer);
 
 		negotiationSocket = new SocketIO();
 		negotiationSocket.connect(serverUrl, this);
 		
 		String userid = "Java "+new Date().toString()+" "+(int)(Math.random()*1000);
 		System.out.println(gameType+" new client "+userid+" waiting");
 
 		negotiationSocket.emit("start_session", new JSONObject()
 			.put("userid", userid)
 			.put("gametype", gameType)
 			.put("role", roleOfThisAgent)
 			);
 	}
 	
 	BidAction partnerLatestBidAction=null, ourLatestBidAction=null;
 	
 	/**
 	 *  @throws IOException 
 	 * @throws NegotiatorException 
 	 *  @see  negotiator.protocol.asyncoffers.AsyncOffersProtocol#runNegotiationSession
 	 *  @see  negotiator.protocol.asyncoffers.AsyncOffersBilateralAtomicNegoSession#run
 	 *  @see  negotiator.protocol.Protocol#loadWorldInformation
 	 */
 	protected void initializeAgent(String role, String partnerRole) throws IOException, NegotiatorException {
 		agentIdOfThisAgent = new AgentID(role);
 		agentIdOfOtherPlayer = new AgentID(partnerRole);
 		
 		agent = new KBAgent();
 		agent.setName(role);
 		agent.setAgentID(agentIdOfThisAgent);
 		
 		/* handle actions from our agent to the partner */
 		agent.setActionListener(new ActionListener() {
 			@Override public void actionSent(Action action) {
 				if (action instanceof BidAction)
 					ourLatestBidAction = (BidAction)action;
 				try {
 					JSONObject jsonActions = JsonToGeniusBridge.geniusActionToJsonObject(action, domain);
 					System.out.println(gameType+" agent says: "+jsonActions);
 					negotiationSocket.emit("negoactions", jsonActions);
 				} catch (JSONException e) {
 					sayToPartner("My strategic agent said something I didn't understand: "+agentIdOfThisAgent);
 				}
 			}
 		});
 
 		
 		int sessionNumber = 1;
 		int sessionTotalNumber = 1;
 		Date startTime = new Date();
 		Integer totalTimeSeconds = 30*60;
 		Integer turnLengthSeconds = 2*60;
 		HashMap<AgentParameterVariable, AgentParamValue> agentParams = new HashMap<AgentParameterVariable, AgentParamValue>();
 		
 		UtilitySpace agentUtilitySpace = domain.getUtilitySpace(role.toLowerCase(), "short-term");
 		WorldInformation agentWorldInformation = domain.getAllUtilitySpaces(partnerRole.toLowerCase());
 		
 		agent.internalInit(
 			sessionNumber, sessionTotalNumber, startTime, totalTimeSeconds, turnLengthSeconds,
 			agentUtilitySpace, agentParams, agentWorldInformation);
 		agent.init();
 	}
 
 	@Override public void onConnect() {
 		System.out.println("NegotiationClient Connection established.");
 	}
 
 	@Override public void onMessage(JSONObject arg0, IOAcknowledge ack) {
 		try {
 			System.out.println("NegotiationClient receives an object: " + arg0.toString(2));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override public void onMessage(String data, IOAcknowledge ack) {
 		System.out.println("NegotiationClient receives a message: " + data);
 	}
 
 	@Override public void onError(SocketIOException socketIOException) {
 		System.out.println("NegotiationClient receives an error:");
 		socketIOException.printStackTrace();
 	}
 
 	@Override public void onDisconnect() {
 		System.out.println("NegotiationClient Connection terminated.");
 	}
 	
 	
 	/* Handle actions from the partner or from the server to our agent */
 	@Override public void on(String event, IOAcknowledge ack, Object... args) {
 		System.out.println(gameType+" NegotiationClient receives event '" + event + "' arg0="+args[0]);
 		try {
 			if (event.equals("status")) {  // status sent by the server:
 				JSONObject arg0 = (JSONObject)args[0];
 				if (arg0.get("key").equals("phase") && arg0.get("value").equals("")) { 
 					System.out.println("  "+gameType+" game starts - launching a new client!");
 					final NegotiationClient newClient = clone();
 					new Thread() {
 						public void run() {
 							try {
 								newClient.start();
 							} catch (JSONException e) {
 								e.printStackTrace();
 							} catch (IOException e) {
 								e.printStackTrace();
 							} catch (NegotiatorException e) {
 								e.printStackTrace();
 							}
 						}
 					}.start();
 				}
 			} else if (event.equals("EndTurn")) {
 				int turnsFromStart = (Integer)args[0];
 				Action theAction = new EndTurn(turnsFromStart);
 				agent.ReceiveMessage(theAction);
 			} else if (event.equals("negoactions")) {  // offer created by the partner (usually with menus GUI):
 				onPartnerNegoActions(args[0]);
 			} else if (event.equals("announcement")) {
 					JSONObject arg0 = (JSONObject)args[0];
 					String speaker = (String)arg0.get("id");
 					String action = (String)arg0.get("action");
 					Object msg = arg0.get("msg"); // can be either a String or a JSON object
 					boolean you = (Boolean)arg0.get("you");
 					System.out.println("  "+gameType+" "+speaker + (you? " (you)": "")+": "+action+" "+msg);
 					if (!you) {
 						if (action.equals("Connect"))
 							onPartnerConnect();
 						else if (action.equals("Disconnect"))
 							onPartnerDisconnect();
 					}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	/*
 	 * Negotiation-specific event handlers:
 	 */
 
 	public void onPartnerConnect()  {
 		sayToPartner("Hello! I am the "+agentIdOfThisAgent);
 	}
 	
 	NaturalLanguageCollection replyToAccept = new NaturalLanguageCollection("Great, so we can sign the agreement", "I am happy that you agree", "It is a pleasure doing business with you");
 	NaturalLanguageCollection replyToReject = new NaturalLanguageCollection("Too bad you disagree", "Why did you reject my offer?", "This is a good offer, think about it again");
 	NaturalLanguageCollection replyToGreet  = new NaturalLanguageCollection("Hello", "Hi", "Nice to meet you");
 	
 	/**
 	 * The (human) partner sent some actions - forward them to the agent. 
 	 * @param json a JSON object that represents the actions.
 	 */
 	public void onPartnerNegoActions(Object actions) {
 		System.out.println("    "+gameType+" partner actions:   "+actions);
 		try {
 			if (!(actions instanceof JSONObject)) {
 				sayToPartner("I didn't understand what you meant to say - I got a wrong-format action list: "+actions);
 				return;
 			}
 			JSONObject actionsJson = (JSONObject)actions;
 			if (!(actionsJson.keys().hasNext())) {
 				sayToPartner("I didn't understand what you meant to say - I got an empty action list: "+actionsJson);
 				return;
 			}
 			List<Action> actionsGenius = JsonToGeniusBridge.jsonObjectToGeniusAction(
 				actionsJson, domain, agentIdOfOtherPlayer,
 				/*bidTime=*/null,
 				ourLatestBidAction,
 				partnerLatestBidAction
 				);
 			System.out.println("    "+gameType+" partner actions in Genius: "+actionsGenius);
 			boolean actionIdentified = false;
 			for (Action action: actionsGenius) {
				agent.ReceiveMessage(action);
 				if (action instanceof BidAction)
 					partnerLatestBidAction = (BidAction)action;
 				if (action instanceof Accept)
 					sayToPartner(replyToAccept.randomString());
 				if (action instanceof Reject)
 					sayToPartner(replyToReject.randomString());
 				actionIdentified = true;
 			}
 			if (actionsJson.has("Greet")) {
 				sayToPartner(replyToGreet.randomString());
 				actionIdentified = true;
 			}
 				
 			if (!actionIdentified) {
 				sayToPartner("I didn't understand what you meant to say - I couldn't identify any actions in: "+actionsJson);
 				return;
 			}
 		} catch (NegotiatorException ex) {
 			sayToPartner(ex.getMessage()+"!");
 		} catch (Exception ex) {
			ex.printStackTrace();
 			sayToPartner("I could not understand you because: "+ex+"!");
 		}
	}
 
 	public void onPartnerAccept() {
 		agent.ReceiveMessage(new Accept());
 	}
 
 	public void onPartnerReject() {
 		agent.ReceiveMessage(new Reject());
 	}
 
 	public void onPartnerQuit()  {
 		agent.ReceiveMessage(new EndNegotiation());
 	}
 
 	public void onPartnerDisconnect()  {
 		sayToPartner("Bye!");
 	}
 	
 	public void sayToPartner(String message) {
 		negotiationSocket.emit("message",message);
 	}
 
 	/*
 	 * Main program:
 	 */
 	
 	private static String thisClassName = Thread.currentThread().getStackTrace()[1].getClassName();
 	
 	public static void main(String[] args) throws Exception {
 		if (args.length<3) {
 			System.err.println("SYNTAX: "+thisClassName+" <path-to-domain-file> <url-of-negotiation-server> <game-types>");
 			System.exit(1);
 		}
 		java.util.logging.Logger.getLogger("io.socket").setLevel(Level.WARNING);
 
 		for (String gameType: args[2].split(",")) 
 			new NegotiationClient(new Domain(args[0]), args[1], gameType).start();  // Start the first client. It will launch new clients as the need arises.
 	}
 }
