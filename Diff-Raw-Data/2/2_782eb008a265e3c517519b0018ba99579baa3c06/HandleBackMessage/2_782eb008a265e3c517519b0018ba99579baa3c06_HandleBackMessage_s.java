 package behaviours;
 
 import jade.core.Agent;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.lang.acl.ACLMessage;
 import messaging.HistEl;
 import messaging.History;
 import logic.Clause;
 import logic.Literal;
 import agent.BottomEl;
 import messaging.Message;
 import messaging.BackMessage;
 import messaging.FinalMessage;
 import messaging.ForthMessage;
 
 public class HandleBackMessage extends OneShotBehaviour {
 		private Agent agent;
 		private ACLMessage message;
 		public HandleBackMessage(Agent a, ACLMessage msg) {
 			super(a);
 			message = msg;
 		}
 		public void action() {
 			Message msg= (Message)message.getContentObject();
 			History hist = msg.getHistory();
 			HistEl prevEl = hist.getPreviousElement(0);
 			HistEl prevprev_el = hist.getPreviousElement(1);
			agent.setBOTTOM(new BottomEl(prevEl.getLiteral(), hist.pop())) = true;
 			boolean all_true = true;
 			for(Literal l: prevprev_el.getClause().asLiterals())
 				if(agent.getBOTTOM(new BottomEl(l, hist.pop())) == false)
 					all_true = false;
 			if(all_true) {
 				if((hist.pop().pop()).isEmpty()) {
 					System.out.println("back");
 					System.out.println("final");
 				}
 				else {
 					ACLMessage r1 = new ACLMessage();
 					r1.addReceiver(message.getSender());
 					r1.setContentObject(new BackMessage(hist.pop(), Clause.trueClause()));
 					myAgent.send(r1);
 					ACLMessage r2 = new ACLMessage();
 					r2.addReceiver(message.getSender());
 					r2.setContentObject(new FinalMessage(hist.pop(), Clause.trueClause()));
 					myAgent.send(r2);
 				}
 			}
 		}
 	}
