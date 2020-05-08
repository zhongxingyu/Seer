 package no.whg.whirc.models;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import jerklib.Channel;
 import jerklib.events.AwayEvent;
 import jerklib.events.CtcpEvent;
 import jerklib.events.JoinEvent;
 import jerklib.events.KickEvent;
 import jerklib.events.MessageEvent;
 import jerklib.events.MotdEvent;
 import jerklib.events.PartEvent;
 import jerklib.events.TopicEvent;
 import jerklib.events.modes.ModeEvent;
 import no.whg.whirc.activities.MainActivity;
 import no.whg.whirc.adapters.MessageAdapter;
 import android.widget.ListView;
 
 
 /**
  * @author Snorre
  *
  */
 public class Conversation {
     private ArrayList<Message> messages;
     private Channel channel;
     private ListView messageView;
     private MainActivity activity;
     private MessageAdapter messageAdapter;
     private String channelTitle;
 
     public Conversation(Channel channel){
         this.channel = channel;
         this.channelTitle = channel.getName();
         messages = new ArrayList<Message>();
         Message topic = new Message("none", "no topic", "never", 0);
         messages.add(topic);
 //      if (!channel.getTopic().equals("")){
 //          Message topic = new Message(channel.getTopicSetter(), channel.getTopic(), channel.getTopicSetTime().toString());
 //          messages.add(topic);
 //      }
     }
 
     public Conversation(Channel channel, String priv){
         this.channel = channel;
         this.channelTitle = priv;
         messages = new ArrayList<Message>();
     }
 
     public Conversation(String servername){
         this.channelTitle = servername;
 
         messages = new ArrayList<Message>();
     }
 
     public ArrayList<Message> getMessages(){
         return messages;
     }
 
     public void addMessage(Message m){
         messages.add(m);
     }
 
     public void addMessage(MessageEvent me){
         addMessage(new Message(me.getNick(), me.getMessage(), getTime(), me.hashCode()));
     }
 
     public void addMessage(MotdEvent me){
         addMessage(new Message(me.getHostName(), me.getMotdLine(), getTime(), me.hashCode()));
     }
 
     public void addMessage(TopicEvent te){
         Message topic = new Message(te.getSetBy(), te.getTopic(), te.getSetWhen().toString(), 0);
         messages.set(0, topic);
     }
 
     public void addMessage(JoinEvent je){
         addMessage(new Message(getChannelTitle(), "User " + je.getNick() + " joined the channel (" + je.getUserName() + " " + je.getHostName() + ").", getTime(), je.hashCode()));
     }
 
     public void addMessage(PartEvent pe){
         addMessage(new Message(getChannelTitle(), "User " + pe.getWho() + " left the channel (" + pe.getUserName() + " " + pe.getHostName() + "):\n" + pe.getPartMessage(), getTime(), pe.hashCode()));
     }
 
     public void addMessage(KickEvent ke){
         addMessage(new Message(getChannelTitle(), "User " + ke.getWho() + " was kicked from channel (" + ke.getUserName() + " " + ke.getHostName() + "):\n" + ke.getMessage(), getTime(), ke.hashCode()));
     }
 
     public void addMessage(AwayEvent ae){
         if (ae.isYou()){
             if (ae.isAway()){
                 addMessage(new Message(ae.getSession().getConnectedHostName(), "You are now listed as AWAY.", getTime(), ae.hashCode()));
             } else {
                 addMessage(new Message(ae.getSession().getConnectedHostName(), "You are no longer listed as AWAY.", getTime(), ae.hashCode()));
             }
         } else {
             if (ae.isAway()){
                 addMessage(new Message(ae.getSession().getConnectedHostName(), ae.getNick() + " is now listed as AWAY (" + ae.getAwayMessage() + ").", getTime(), ae.hashCode()));
             } else {
                 addMessage(new Message(ae.getSession().getConnectedHostName(), ae.getNick() + " is no longer listed as AWAY.", getTime(), ae.hashCode()));
             }
         }
     }
 
     public void addMessage(ModeEvent me){
         addMessage(new Message(me.setBy(), "Mode set to " + me.getModeAdjustments().toString(), getTime(), me.hashCode()));
 	}
 	
 	public void addMessage(CtcpEvent ce){
 		String cm = ce.getCtcpString();
 		String[] ctcp = cm.split(" ", 2);
		addMessage(new Message("", ce.getNick() + " " + ctcp[1], (new Date().toString()), ce.hashCode()));
     }
 
     public String getChannelTitle(){
         return channelTitle;
     }
 
     public boolean hasMessage(int hash){
         for (Message m : messages){
             if (m.getHashcode() == hash){
                 return true;
             }
         }
 
         return false;
     }
 
     public String getTime() {
         Calendar cal = Calendar.getInstance();
 
         return cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
     }
 }
