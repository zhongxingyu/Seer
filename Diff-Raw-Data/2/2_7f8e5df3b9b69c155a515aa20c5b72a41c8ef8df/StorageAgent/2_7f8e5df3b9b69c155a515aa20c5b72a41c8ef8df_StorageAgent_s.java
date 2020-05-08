 /*****************************************************************
 JADE - Java Agent DEvelopment Framework is a framework to develop 
 multi-agent systems in compliance with the FIPA specifications.
 Copyright (C) 2000 CSELT S.p.A. 
 
 GNU Lesser General Public License
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation, 
 version 2.1 of the License. 
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the
 Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.
  *****************************************************************/
 package customAgents;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 public class StorageAgent extends Agent {
 	public class PathClaimer {
 		public List<Point> claimedPoints = new ArrayList<Point>();
 		public AID ID;
 
 		public PathClaimer(AID a) {
 			ID = a;
 		}
 	}
 
 	public class Product {
 		Point location;
 		String name;
 
 		public Product(String n, Point loc) {
 			name = n;
 			location = loc;
 		}
 	}
 
 	public List<Product> products = new ArrayList<Product>();
 	public List<String> pendingRequests = new ArrayList<String>();
 	public List<PathClaimer> PathClaimers = new ArrayList<PathClaimer>();
 	String requestedItem;
 	Point requestedCoord;
 	Point location;
 	private StorageAgentGUI myGui;// The GUI by which the user can add requested
 									// products
 	private AID[] robotAgents;
 	private AID[] storageAgents;
 
 	/**
 	 * entry point of the agent
 	 */
 	protected void setup() {
 
 		// agent needs a start location, else doDelete
 		Object[] args = getArguments();// starting location (static) of the
 										// storage agent
 		if (args != null && args.length > 0) {
 			location = new Point(Integer.parseInt((String) args[0]),
 					Integer.parseInt((String) args[1]));
 			/*System.out.println(getAID().getName() + " is positioned at "
 					+ location.x + "," + location.y);*/
 		} else {
 			// Make the agent terminate
 			/*System.out.println("No position defined, closing down");*/
 			doDelete();
 		}
 
 		// need 132 products
 		// Create the product list
 		// TODO : think of different products and fill our warehouse, see our
 		// warehouse map file
 		addProduct("Jupiler", new Point(4, 4));
 		addProduct("Heineken", new Point(9, 11));
 		addProduct("Leffe", new Point(12, 7));
 		addProduct("Hoegaarden", new Point(16, 15));
		addProduct("Albani", new Point(17, 17));
 
 		// register this agent at the yellow pages service
 		DFAgentDescription dfd = new DFAgentDescription();
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-StorageAgent");
 		sd.setName("Warehouse-StorageAutomation");
 		dfd.addServices(sd);
 		try {
 			DFService.register(this, dfd);
 
 		} catch (FIPAException e) {
 			/*System.out.println("catch");*/
 			e.printStackTrace();
 		}
 
 		// Create and show the GUI
 		myGui = new StorageAgentGUI(this);
 		myGui.showGui();
 		addBehaviour(new AcceptHopRequest());// respond to incoming movRequests
 												// from RobotAgents
 		addBehaviour(new CheckClaimedMov());// respond to incoming movRequests
 											// from RobotAgents
 		addBehaviour(new CheckArrivalInforms());// respond to incoming arrival
 												// informs from Robotagents
 	}
 
 	/**
 	 * Simply adds a product to the product list
 	 */
 	public void addProduct(String n, Point p) {
 		Product pr = new Product(n, p);
 		products.add(pr);
 	}
 
 	protected void takeDown() {
 		// Close the GUI
 		myGui.dispose();
 		// Deregister from the yellow pages
 		try {
 			DFService.deregister(this);
 		} catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 		// Printout a dismissal message
 		/*System.out.println("StorageAgent " + getAID().getName()
 				+ " terminating.");*/
 	}
 
 	/**
 	 * refresh the list of storage agents
 	 */
 	public void updateStorageAgents() {
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-StorageAgent");
 		template.addServices(sd);
 		try {
 			DFAgentDescription[] result = DFService.search(this, template);
 			storageAgents = new AID[result.length];
 			// System.out.println("Search performed, result amount: " +
 			// result.length);
 			for (int i = 0; i < result.length; ++i) {
 				storageAgents[i] = result[i].getName();
 				/*System.out.println(storageAgents[i].getName());*/
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 			/*System.out.println("in the catch");*/
 		}
 	}
 
 	/**
 	 * refresh the list of robot agents
 	 */
 	public void updateRobotAgents() {
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-RobotAgent");
 		template.addServices(sd);
 		try {
 			DFAgentDescription[] result = DFService.search(this, template);
 			robotAgents = new AID[result.length];
 			// System.out.println("Search performed, result amount: " +
 			// result.length);
 			for (int i = 0; i < result.length; ++i) {
 				robotAgents[i] = result[i].getName();
 				/*System.out.println(robotAgents[i].getName());*/
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 			/*System.out.println("in the catch");*/
 		}
 	}
 
 	/**
 	 * This is invoked by the GUI when the user requests a new item
 	 */
 	public void addRequestedItem(final String pr) {
 		addBehaviour(new OneShotBehaviour() { // via the GUI a product request
 												// can be done
 			public void action() {
 				requestedItem = pr;
 
 				updateRobotAgents();// update the list of living robot agents
 									// before sending a message to them
 
 				boolean productExists = false;
 				for (int i = 0; i < products.size(); i++) {// check if the
 															// product exists in
 															// warehouse
 					if (0 == products.get(i).name.compareTo(requestedItem)) {
 						productExists = true;
 						requestedCoord = products.get(i).location;
 					}
 				}
 				if (true == productExists) {// if it exists start a "complex"
 											// communication behaviour to make a
 											// CFP
 					myAgent.addBehaviour(new RequestPerformer());
 				} else {
 					/*System.out.println(pr
 							+ " does not exist in current warehouse");*/
 				}
 			}
 		});
 	}
 
 	private class RequestPerformer extends Behaviour {// make the CFP
 		private AID cheapestRobot; // The agent who provides the best offer
 		private int bestPrice; // The best offered price
 		private int repliesCnt = 0; // The counter of replies from robot agents
 		private MessageTemplate mt; // The template to receive replies
 		private int step = 0;
 
 		public void action() {
 			switch (step) {
 			case 0:
 				// Send the cfp to all robots
 				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
 				for (int i = 0; i < robotAgents.length; ++i) {
 					cfp.addReceiver(robotAgents[i]);
 				}
 				cfp.setContent(requestedCoord.x + "," + requestedCoord.y);// coords
 																			// of
 																			// the
 																			// requested
 																			// products
 				cfp.setConversationId("item-request");
 				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique
 																		// value
 				myAgent.send(cfp);
 				// Prepare the template to get proposals
 				mt = MessageTemplate.and(
 						MessageTemplate.MatchConversationId("item-request"),
 						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
 				step = 1;
 				break;
 			case 1:
 				// Receive all proposals/refusals from robot agents
 				ACLMessage reply = myAgent.receive(mt);
 				if (reply != null) {
 					// Reply received
 					if (reply.getPerformative() == ACLMessage.PROPOSE) {
 						// This is an offer
 						int price = Integer.parseInt(reply.getContent());
 						if (cheapestRobot == null || price < bestPrice) {
 							// This is the best offer at present
 							bestPrice = price;
 							cheapestRobot = reply.getSender();
 						}
 					}
 					repliesCnt++;
 					if (repliesCnt >= robotAgents.length) {
 						// We received all replies
 						System.out.println("bestprice: " +  bestPrice);
 						System.out.println("cheapest bot: " + cheapestRobot.getName());
 						step = 2;
 						/*System.out.println("Received all replies");*/
 					}
 				} else {
 					block();
 				}
 				break;
 			case 2:
 				// Send the move order to the robot that provided the best offer
 				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
 				order.addReceiver(cheapestRobot);
 				order.setContent(requestedCoord.x + "," + requestedCoord.y
 						+ ":" + location.x + "," + location.y);// the 2nd x,y is
 																// the location
 																// of the
 																// storage agent
 				order.setConversationId("item-request");
 				order.setReplyWith("order" + System.currentTimeMillis());
 				/*System.out.println("Sent accept proposal");*/
 				myAgent.send(order);
 				// Prepare the template to get the purchase order reply
 				mt = MessageTemplate.and(
 						MessageTemplate.MatchConversationId("item-request"),
 						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
 				step = 3;
 				break;
 			case 3:
 				// Receive the confirmation of the robot
 				reply = myAgent.receive(mt);
 				if (reply != null) {
 					// Purchase order reply received
 					if (reply.getPerformative() == ACLMessage.INFORM) {
 						/*System.out.println(getAID().getName()
 								+ ": Conversation succesfully ended");*/
 						pendingRequests.add(reply.getSender() + ","
 								+ requestedItem);
 					} else {
 						/*System.out
 								.println("Conversation failed for unknown reason");*/
 					}
 
 					step = 4;
 				} else {
 					block();
 				}
 				break;
 			}
 		}
 
 		public boolean done() {
 			if (step == 2 && cheapestRobot == null) {
 				/*System.out.println("No single robot replied");*/
 			}
 			return ((step == 2 && cheapestRobot == null) || step == 4);
 		}
 	} // End of inner class RequestPerformer
 
 	private class AcceptHopRequest extends CyclicBehaviour {
 		public void action() {// A robot agent is trying to claim a path of 3
 								// squares
 			MessageTemplate mt = MessageTemplate.and(
 					MessageTemplate.MatchConversationId("hop-request"),
 					MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));
 			ACLMessage movReq = myAgent.receive(mt);
 			if (movReq != null) {// there is a pending movement request from 1
 									// or more robotAgent
 				String requestedLocations = movReq.getContent();// the movement
 																// string looks
 																// like
 																// x,y;x,y;x,y;x,y;
 																// The agent
 																// requests for
 																// these spots
 																// to be
 																// available
 
 				// fill an array with the requested points
 				int pointsInHop = requestedLocations.length() / 4;
 				List<Point> points = new ArrayList<Point>();
 				
 				
 				for (int i = 0; i < pointsInHop - 1; i++) {
 					/*System.out.println("reqeuestedlocs: " + requestedLocations);*/
 					String[] splits = requestedLocations.split(",");
 					Point p = new Point();
 					/*
 					 * p.x=
 					 * Integer.parseInt(String.valueOf(requestedLocations.charAt
 					 * (i*4))); p.y=
 					 * Integer.parseInt(String.valueOf(requestedLocations
 					 * .charAt(i*4+2)));
 					 */
 
 					p.x = Integer.parseInt(splits[i]);
 					p.y = Integer.parseInt(splits[i + 1]);
 
 					points.add(p);
 				}
 
 				// first remove the previous claimed path, if there is one,
 				// although do keep the current position claimed, else just
 				// claim the agent its current pos
 				boolean agentInExistance = false;
 				for (int i = 0; i < PathClaimers.size(); i++) {
 					if (PathClaimers.get(i).ID.toString().equals(
 							movReq.getSender().toString())) {
 						agentInExistance = true;
 						PathClaimers.get(i).claimedPoints.clear();
 						//Add all
 						
 						//PathClaimers.get(i).claimedPoints.addAll(points);
 						PathClaimers.get(i).claimedPoints.add(points.get(points
 								.size() - 1));
 						//System.out.println("claimedPoints size: " + PathClaimers.get(i).claimedPoints.size());
 					}
 				}
 				if (agentInExistance == false) {
 					PathClaimer pc = new PathClaimer(movReq.getSender());
 					//Add all
 					pc.claimedPoints.add(points.get(points.size() - 1));
 					//pc.claimedPoints.addAll(points);
 					PathClaimers.add(pc);
 				}
 
 				// check if the requested hop is available
 				boolean hopIsAvailable = true;
 				for (int i = 0; i < PathClaimers.size(); i++) {
 					/*
 					 * if(pointsInHop==5) { int timeToBreak=1; timeToBreak++; }
 					 */
 					for (int j = 0; j < PathClaimers.get(i).claimedPoints
 							.size() - 1; j++)// size-1 is already
 												// claimed(current position)
 					{
 						for (int p = 0; p < points.size() -1; p++) {
 							if (PathClaimers.get(i).claimedPoints.get(j).x == points
 									.get(p).x
 									&& PathClaimers.get(i).claimedPoints.get(j).y == points
 											.get(p).y) {
 								hopIsAvailable = false;
 							}
 						}
 					}
 				}
 
 				if (hopIsAvailable) {// claim the hop
 										// System.out.println(PathClaimers.size());
 					for (int i = 0; i < PathClaimers.size(); i++) {
 						if (PathClaimers.get(i).ID == movReq.getSender()) {// the
 																			// requesting
 																			// agent
 																			// its
 																			// current
 																			// location
 																			// is
 																			// already
 																			// claimed,
 																			// find
 																			// where
 																			// and
 																			// add
 																			// the
 																			// hop
 																			// to
 																			// it
 							for (int j = 0; j < points.size(); j++) {
 								PathClaimers.get(i).claimedPoints.add(points
 										.get(j));
 							}
 						}
 					}
 				}
 
 				// start building the message
 				ACLMessage acptMov = movReq.createReply();// the robotAgent
 															// sender is added
 															// as recipient
 				updateStorageAgents();
 				for (int i = 0; i < storageAgents.length; ++i) {
 					acptMov.addReceiver(storageAgents[i]);// all storage agents
 															// are added as
 															// recipients, they
 															// need to know that
 															// the locations are
 															// claimed(sharing
 															// their map)
 				}
 
 				acptMov.setPerformative(ACLMessage.INFORM);
 				String sendString = "";
 				sendString += (movReq.getSender().toString() + ",");
 				if (hopIsAvailable) {
 					sendString += ("yes" + ",");
 				} else {
 					sendString += ("no" + ",");
 				}
 
 				for (int i = 0; i < points.size(); i++) {
 					sendString += (points.get(i).x + ",");
 					sendString += (points.get(i).y + ",");
 				}
 				acptMov.setContent(sendString);// looks like :
 												// "sender.getAid.getName(),yes,x,y,x,y,x,y,x,y"
 
 				myAgent.send(acptMov);
 			} else {
 				block();
 			}
 		}
 	} // End of inner class
 
 	private class CheckClaimedMov extends CyclicBehaviour {
 		public void action() {// storage agents should listen to other storage
 								// agents claiming spots for robotAgents
 			MessageTemplate mt = MessageTemplate.and(
 					MessageTemplate.MatchConversationId("hop-request"),
 					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
 			ACLMessage movReq = myAgent.receive(mt);
 			if (movReq != null) {// a storage agent responded to a hop request
 									// of a robot agent
 				String receiveString = movReq.getContent();// looks like:
 															// "sender.getAid.getName(),yes,x,y,x,y,x,y,x,y"
 				String[] splitString = receiveString.split(",");
 				int pointsInHop = (splitString.length - 2) / 2;
 				List<Point> points = new ArrayList<Point>();
 				for (int i = 0; i < pointsInHop; i++) {
 
 					Point p = new Point();
 					p.x = Integer.parseInt((String) splitString[i * 2 + 2]);
 					p.y = Integer.parseInt((String) splitString[i * 2 + 3]);
 					points.add(p);
 				}
 				if (receiveString.contains("yes")) {// the agent is arrived at
 													// his location and got
 													// approval to move on,
 													// clear his previous
 													// claimed hop and claim his
 													// new values
 					boolean agentInExistance = false;
 					for (int i = 0; i < PathClaimers.size(); i++) {
 						if (PathClaimers.get(i).ID == movReq.getSender()) {
 							agentInExistance = true;
 							PathClaimers.get(i).claimedPoints.clear();
 							for (int j = 0; j < points.size(); j++) {
 								PathClaimers.get(i).claimedPoints.add(points
 										.get(j));
 							}
 						}
 					}
 					if (agentInExistance == false) {
 						PathClaimer pc = new PathClaimer(movReq.getSender());
 						for (int j = 0; j < points.size(); j++) {
 							pc.claimedPoints.add(points.get(j));
 						}
 					}
 				} else {// the agent is arrived at his location but didnt get
 						// approval to move on, his previous claimed spots can
 						// be freed now
 					boolean agentInExistance = false;
 					for (int i = 0; i < PathClaimers.size(); i++) {
 						if (PathClaimers.get(i).ID == movReq.getSender()) {
 							agentInExistance = true;
 							PathClaimers.get(i).claimedPoints.clear();
 							PathClaimers.get(i).claimedPoints.add(points
 									.get(points.size() - 1));
 						}
 					}
 					if (agentInExistance == false) {
 						PathClaimer pc = new PathClaimer(movReq.getSender());
 						pc.claimedPoints.add(points.get(points.size() - 1));
 					}
 				}
 			} else {
 				block();
 			}
 		}
 	} // End of inner class
 
 	private class CheckArrivalInforms extends CyclicBehaviour {
 		public void action() {//storage agents should listen to other storage agents claiming spots for robotAgents
 			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("arrival-inform"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
 			ACLMessage arr = myAgent.receive(mt);
 			if (arr != null) 
 			{
 				String receivedString = arr.getContent();
 				if(receivedString.contains((location.x-1)+","+location.y))
 				{//the robot is near this storage agent
 					
 					String[] locs = receivedString.split(";");
 					
 					String[] locItem = locs[0].split(",");
 					Integer itemX = Integer.parseInt(locItem[0]);
 					Integer itemY = Integer.parseInt(locItem[1]);
 					
 					
 					/*Integer itemX = Integer.parseInt(receivedString.substring(receivedString.indexOf(':', 1)+1,
 							receivedString.indexOf(',', receivedString.indexOf(':', 1))));
 					
 					Integer itemY = Integer.parseInt(receivedString.substring(receivedString.indexOf(',', receivedString.indexOf(':', 1)+1),receivedString.length()-1));*/
 					/*for(Product p: products)
 					{
 						if(p.location==new Point(itemX,itemY))
 						{
 							System.out.println("I received the product: "+p.name);
 							
 						}
 					}*/
 					ACLMessage repl = arr.createReply();//the robotAgent sender is added as recipient
 					repl.setContent("go");
 					myAgent.send(repl);
 				}
 				
 				else 
 				{
 					block();
 				}
 			}
 		}
 	} // End of inner class
 }
