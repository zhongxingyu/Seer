 package info.hoang8f.jade.agent;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 import jade.content.lang.Codec;
 import jade.content.lang.sl.SLCodec;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.core.behaviours.TickerBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.util.Logger;
 import jade.util.leap.Set;
 import jade.util.leap.SortedSetImpl;
 
 public class SimpleAgent extends Agent implements SimpleAgentInterface {
 
     private static final String TAG = "SimpleAgent";
     private static final long serialVersionUID = 1594371294421614291L;
     private static final String RECEIVER_AGENT_NAME = "da0";
     private Set participants = new SortedSetImpl();
     private Codec codec = new SLCodec();
     private Context context;
    private static String IP_ADDRESS = "133.19.63.184";
    private static String AGENT_NAME = "android-agent";
 
     protected void setup() {
         Object[] args = getArguments();
         if (args != null && args.length > 0) {
             if (args[0] instanceof Context) {
                 context = (Context) args[0];
             }
         }
 
         // Add initial behaviours
         addBehaviour(new SendMessage(this, 3000));
         addBehaviour(new ParticipantsManager(this));
 
         // Activate the GUI
         registerO2AInterface(SimpleAgentInterface.class, this);
 
         Intent broadcast = new Intent();
         broadcast.setAction("jade.demo.agent.SEND_MESSAGE");
         Log.i(TAG, "###Sending broadcast " + broadcast.getAction());
         context.sendBroadcast(broadcast);
     }
 
     protected void takeDown() {
     }
     
     class SendMessage extends TickerBehaviour {
 
         public SendMessage(Agent a, long period) {
             super(a, period);
         }
 
         @Override
         protected void onTick() {
             Log.i(TAG, "###on Tick");
            ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
             message.setLanguage(codec.getName());
             String convId = "C-" + myAgent.getLocalName();
             message.setConversationId(convId);
             message.setContent("hello! I am from android mobile");
             AID dummyAid = new AID();
            dummyAid.setName(AGENT_NAME +"@" + IP_ADDRESS + ":1099/JADE");
            dummyAid.addAddresses("http://" + IP_ADDRESS + ":7778/acc");
             message.addReceiver(dummyAid);
             myAgent.send(message);
             Log.i(TAG, "###Send message:" + message.getContent());
         }
 
     }
 
     /**
      * Inner class ParticipantsManager. This behaviour registers as a chat
      * participant and keeps the list of participants up to date by managing the
      * information received from the ChatManager agent.
      */
     class ParticipantsManager extends CyclicBehaviour {
         private static final long serialVersionUID = -4845730529175649756L;
         private MessageTemplate template;
 
         ParticipantsManager(Agent a) {
             super(a);
         }
 
         public void onStart() {
             // Send message to another Jade agent platform
            ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
             message.setLanguage(codec.getName());
             String convId = "C-" + myAgent.getLocalName();
             message.setConversationId(convId);
             message.setContent("hello! I am from android mobile");
             AID dummyAid = new AID();
            dummyAid.setName(AGENT_NAME +"@" + IP_ADDRESS + ":1099/JADE");
            dummyAid.addAddresses("http://" + IP_ADDRESS + ":7778/acc");
             message.addReceiver(dummyAid);
             myAgent.send(message);
             Log.i(TAG, "###Send message:" + message.getContent());
             // Initialize the template used to receive notifications
             // from the ChatManagerAgent
             template = MessageTemplate.MatchConversationId(convId);
         }
 
         public void action() {
             // Receives information about people joining and leaving
             // the chat from the ChatManager agent
             ACLMessage msg = myAgent.receive(template);
             if (msg != null) {
                 if (msg.getPerformative() == ACLMessage.INFORM) {
                     try {
 
                     } catch (Exception e) {
                         Logger.println(e.toString());
                         e.printStackTrace();
                     }
                 } else {
                     handleUnexpected(msg);
                 }
             } else {
                 block();
             }
         }
     } // END of inner class ParticipantsManager
 
 
     // ///////////////////////////////////////
     // Methods called by the interface
     // ///////////////////////////////////////
     public void handleSpoken(String s) {
         // Add a ChatSpeaker behaviour that INFORMs all participants about
         // the spoken sentence
     }
 
     public String[] getParticipantNames() {
         String[] pp = new String[participants.size()];
         return pp;
     }
 
     // ///////////////////////////////////////
     // Private utility method
     // ///////////////////////////////////////
     private void handleUnexpected(ACLMessage msg) {
         Log.i(TAG, "###Unexpected message received from " + msg.getSender().getName());
         Log.i(TAG, "###Content is: " + msg.getContent());
     }
 
 }
