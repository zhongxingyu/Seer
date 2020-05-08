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
 
 import jade.core.behaviours.*;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.gui.GuiAgent;
 import jade.gui.GuiEvent;
 
 public class GUIAgent extends GuiAgent {
 
 	static GUIAgent gui;
 	GUI myGUI;
 	
 	public void readMap()
 	{//TODO : Read the map from an external file and store it in global variable
 
 	}
 
 
 	protected void setup() {//entry point of the agent
 		readMap();//read the external map file
 
 		DFAgentDescription dfd = new DFAgentDescription();//Register this agent in the yellow pages service
 		dfd.setName(getAID());
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType("Warehouse-GuiAgent");
 		sd.setName("Warehouse-StorageAutomation");
 		dfd.addServices(sd);
 		try {
 			DFService.register(this, dfd);
 
 		} catch (FIPAException e) {
 			//System.out.println("catch");//debug purpose
 			e.printStackTrace();
 		}
 
 		addBehaviour(new MapProviderService());//respond to incoming map requests from RobotAgents
 		addBehaviour(new MovementSniffService());//sniff movement messages, needed to update GUI
 		
 		myGUI = new GUI(this);
 		myGUI.setVisible(true);
 		//myGUI.makeMove();
 	}
 
 
 	protected void takeDown() {
 		// Deregister from the yellow pages
 		try {
 			DFService.deregister(this);
 		}
 		catch (FIPAException fe) {
 			fe.printStackTrace();
 		}
 		// Printout a dismissal message
 		System.out.println("GuiAgent "+getAID().getName()+" terminating.");
 	}
 
 	private class MapProviderService extends CyclicBehaviour {//behavior listens to incomming map requests and replies with map
 		public void action() {
 			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("map-request"), MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF));
 			ACLMessage movReq = myAgent.receive(mt);
 			if (movReq != null) {//Only listen to QUERY_IF messages with ID "map-request", these could be sent either by storage or robot agents
 				//System.out.println("I received a map request");//debug purpose
 				ACLMessage acptMap = movReq.createReply();//the sender is added as recipient
 				acptMap.setPerformative(ACLMessage.INFORM);
 				acptMap.setContent(myGUI.getMapString());
 				myAgent.send(acptMap);
 			}
 			else {
 				block();
 			}
 		}
 	}  // End of inner class RequestPerformer
 
 
 	private class MovementSniffService extends CyclicBehaviour {//service sniffs movement from robotAgents
 		public void action() {
 			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("map-update"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
 			ACLMessage movReq = myAgent.receive(mt);
 			if (movReq != null) {//Only listen to INFORM messages with ID "free-request", these are sent by a robot agent right after he made a move
 				String msgString = movReq.getContent();//looks like "x,y" x,y is where the agent has moved to
 				//TODO : visualize the movement of this agent in GUI and globally store this agent's ( .getAID.getName() ) new location
 				String[] content = msgString.split(",");
				
				System.out.println("Here is the content: "+msgString);
				myGUI.makeMove(content[0].charAt(0),
 							   Integer.parseInt(content[1]),
 							   Integer.parseInt(content[2]),
 							   Integer.parseInt(content[3]),
 							   Integer.parseInt(content[4]));
 			}
 			else {
 				block();
 			}
 		}
 	}  // End of inner class 
 
 	@Override
 	protected void onGuiEvent(GuiEvent ev) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
