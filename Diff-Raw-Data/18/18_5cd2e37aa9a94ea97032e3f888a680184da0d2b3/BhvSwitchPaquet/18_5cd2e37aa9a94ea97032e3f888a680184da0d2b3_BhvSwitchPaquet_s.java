 package behaviors;
 
 import sun.security.action.GetLongAction;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.WakerBehaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import messages.AIDSerializer;
 import messages.ProtoInfoLink;
 import messages.ProtoPaquet;
 
 import agents.BaseAgent;
 import agents.SwitchAgent;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class BhvSwitchPaquet extends CyclicBehaviour {
 
 	private static final long serialVersionUID = -8645230578588902973L;
 	BaseAgent myAgent;
 	private GsonBuilder gsonb;
 	
 	public BhvSwitchPaquet(Agent a) {
 		super(a);
 		myAgent=((BaseAgent)a);
 		myAgent.log("création du behavior SwitchPaquet");
 		gsonb = new GsonBuilder();
 		gsonb.registerTypeAdapter(AID.class, new AIDSerializer());
 	}
 
 //	private void answer(ACLMessage msg) {
 //		GsonBuilder gsonb = new GsonBuilder();
 //		gsonb.registerTypeAdapter(AID.class, new AIDSerializer());
 //		Gson json = gsonb.create();
 //
 //		ProtoPaquet mess = json.fromJson(msg.getContent(), ProtoPaquet.class);
 //
 //		System.out.println("recu master:");
 //		System.out.println(mess.content);
 //		System.out.println(mess.src);
 //	}
 
 	@Override
 	public void action() {
 		ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
 		BaseAgent ag = (BaseAgent) myAgent;
 		
 		if(msg!=null)
 		{
 			String sender=msg.getSender().getLocalName();
 			
 			//Log
			Gson gson = gsonb.create();
 			ProtoPaquet p = gson.fromJson(msg.getContent(), ProtoPaquet.class);	
 			myAgent.logPaquet(p);
 			
 			for(String dst:((SwitchAgent)myAgent).getLinkTable())
 			{
 				if(dst.equals(sender)) continue;
 				myAgent.log("transmission du paquet à "+dst);
 				
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				ACLMessage jadeMsgInit = new ACLMessage(ACLMessage.REQUEST);
 				jadeMsgInit.addReceiver(ag.getAIDByName(dst));				
 				jadeMsgInit.setContent(msg.getContent());
 				myAgent.send(jadeMsgInit);
 								
 				ag.addBehaviour(new BhvNotifyMaster(ag.getLocalName(), dst));
 			}
 		}
 		
 	}
 }
