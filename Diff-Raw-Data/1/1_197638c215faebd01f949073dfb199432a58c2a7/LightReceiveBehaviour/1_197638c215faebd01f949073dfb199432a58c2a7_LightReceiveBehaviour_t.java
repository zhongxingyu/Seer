 package smarthouse.lightagent;
 
 import java.util.ArrayList;
 import Data.Constants;
 import Data.MessageContent;
 
 import jade.core.AID;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 @SuppressWarnings("serial")
 public class LightReceiveBehaviour extends CyclicBehaviour{
 
 	@Override
 	public void action() {
 	
 		MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);		
 		ACLMessage message = myAgent.receive(template);
 			
 		if (message != null ) {
 			ACLMessage reply = this.parse(message);
 			System.out.println("envoi code reponse");
 			myAgent.send(reply);
 			System.out.println(message.getSender());
 		}else{
 			block();
 		}
 	
 		
 	}
 	
 	private ACLMessage parse(ACLMessage message){
 		ACLMessage answer;
 		// parsing
 		MessageContent content = new MessageContent(message);
 		System.out.println("on est dans le light receiver behaviour");
 
 		// test valeur
 		/*if( ( ((LightAgent)myAgent).getLightState() &&  content.getValue()==1) 
 				|| ( !((LightAgent)myAgent).getLightState() &&  content.getValue()==0)) {
 			// already done
 			answer = new ACLMessage(ACLMessage.FAILURE);
 		}
 		else {
 			//state change
 			 ((LightAgent)myAgent).changeState();
 			 answer = new ACLMessage(ACLMessage.INFORM);
 		}*/
 		answer = new ACLMessage(ACLMessage.INFORM); //uncomment
 		ArrayList<String> c = new ArrayList<String>();
 		c.add("" + ((LightAgent)myAgent).getPosition());
 		MessageContent answerContent = new MessageContent(content.getValue(), 
 							Constants.LIGHT_AGENT, ((LightAgent)myAgent).getPlace(), c
 							);
 		
 		String res = answerContent.toJSON();
 		
 		//retrieve sender
 		AID sender = null;
 		
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription sd = new ServiceDescription();
 		sd.setType(Constants.SIMULATION);
 		template.addServices(sd);
 		
 		DFAgentDescription[] result = null;
 		try {
 			result = DFService.search(myAgent, template);
 			if(result.length > 0) {
 				sender = result[0].getName();
 				answer.addReceiver(sender);
 				answer.setContent(res);
 			}
 		} catch (FIPAException e) {
 			e.printStackTrace();
 		}
 		
 		return answer;
 	}
 	
 	
 
 }
