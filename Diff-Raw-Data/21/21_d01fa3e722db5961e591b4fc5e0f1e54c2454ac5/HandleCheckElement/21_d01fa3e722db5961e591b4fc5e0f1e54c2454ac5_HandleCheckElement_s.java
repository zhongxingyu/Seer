 package main.jadrin.bartender;
 
 import java.util.LinkedList;
 
 import main.jadrin.ontology.CheckElement;
 import main.jadrin.ontology.Drink;
 import main.jadrin.ontology.QueryOntology;
 import jade.content.AgentAction;
 import jade.content.ContentElement;
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 public class HandleCheckElement extends OneShotBehaviour {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4871623810810380962L;
 	private ACLMessage msg;
 
 	HandleCheckElement (BartenderAgent agent, ACLMessage question) {
 		super(agent);
 		this.msg = question;
 	}
 
 	@Override
 	public void action() {
 		try {
			System.out.println("HandleCheckElement start");
 			ContentElement ce =  myAgent.getContentManager().extractContent(msg);
 			CheckElement toCheck = (CheckElement)((Action) ce).getAction(); 
 			
 			BartenderAgent bartender = (BartenderAgent) myAgent;
 			CheckElement result = bartender.whatIsThat(toCheck);			
 			ACLMessage reply = msg.createReply();
 			AgentAction act = result;
 			Action actOperator = new Action(msg.getSender(), act);
 			try {
 				myAgent.getContentManager().fillContent(reply,actOperator);
 			} catch (CodecException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (OntologyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			reply.setOntology(QueryOntology.NAME);
 			reply.setPerformative(ACLMessage.INFORM);
 			myAgent.send(reply);
			System.out.println("HandleCheckElement done");
 			
 		} catch (UngroundedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (CodecException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (OntologyException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		}
 
 
 }
