 package behaviors;
 
 import agents.BaseAgent;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import messages.AIDSerializer;
 import messages.ProtoPaquet;
 import jade.core.AID;
 import jade.core.behaviours.OneShotBehaviour;
 import jade.lang.acl.ACLMessage;
 
 public class SendMessageBehaviour extends OneShotBehaviour {
 	
 	private String message;
 	private String src;
 	private String dest;
 	private Gson gson;
 	private GsonBuilder gsonb;
 
 	public SendMessageBehaviour(String _source, String _dest, String _message) {
 		this.message = _message;
 		src = _source;
 		dest = _dest;
 		
 		gsonb = new GsonBuilder();
 		gsonb.registerTypeAdapter(AID.class, new AIDSerializer());
 		this.gson = gsonb.create();
 	}
 	
 	
 	public void action() {		
 		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
 
 		ProtoPaquet pq = new ProtoPaquet();
 		pq.content = message;
 		pq.src = this.src;
 		pq.dest = this.dest;
 		
		msg.setContent(gson.toJson(pq));
 		
 		BaseAgent ag = (BaseAgent) myAgent; 
 		for(String linkedSwitch:ag.getLinkTable()) {
 			//if(dst.equals(sender)) continue;
 			msg.addReceiver(ag.getSwitchAID(linkedSwitch));				
 		}
 		
 		System.out.println("msg: " + msg.getContent());
 		
 		System.out.println("have Receiver: " + msg.getAllReceiver().hasNext());
 		
 		myAgent.send(msg);
 	}
 
 }
