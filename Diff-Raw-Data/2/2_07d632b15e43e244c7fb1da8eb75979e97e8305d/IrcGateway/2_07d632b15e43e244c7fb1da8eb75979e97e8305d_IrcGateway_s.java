 package swirc;
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 /**
  * Class to extend abstract class PircBot.
  * @author Janne Kallunki, Ville Hämäläinen, Jaakko Ritvanen
  */
 public class IrcGateway extends PircBot implements Runnable {
     private SwircModel model;
     private ArrayList<Channel> channels;
     
     private String serverAddress;
     private String nick;
     private String port;
     private String password;
     
     /**
      * Constructor.
      * @param model SwircModel of this IrcGateway
      * @param serverAddress Server of this IrcGateway
      * @param nick Nickname of the user
      * @param port Port of this IrcGateway
      * @param password Password of server
      * @throws Exception  
      */
     public IrcGateway(SwircModel model, String serverAddress, String nick, String port, String password) throws Exception {
         this.model = model;
         this.serverAddress = serverAddress;
         this.nick = nick;
         this.port = port;
         this.password = password;
         
         this.setName(nick);
         channels = new ArrayList<Channel>();
     }
     
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         this.getChannel(channel).addMsg(sender, message);
     }
     
     /**
      * Returns channel with given name
      * @param name Name of the wanted channel
      * @return Channel with given name
      */
     public Channel getChannel(String name) {
         Iterator<Channel> i = channels.iterator();
         while (i.hasNext()) {
             Channel c = i.next();
             if(name.equals(c.getName())) return c;
         }
         return null;
     }
     
     @Override
     protected void onJoin(String channelName, String joinedNick, String login, String hostname)  {
         Channel c;
         
         // We joined a channel
         if(joinedNick.equals(this.getNick())) {
             System.out.println("it was us!");
             c = new Channel(channelName, this.getServer(), this.model);
             channels.add(c);
             model.joinedChannel(c);
         }
         else {
             c = this.getChannel(channelName);
             if(c != null) {
                 c.userJoins(joinedNick, login, hostname);
             }
         }
     }
     
     @Override
     protected void onUserList(String channel, User[] users) {
         Channel c = this.getChannel(channel);
         for(int i = 0; i < users.length; i++) {
             c.addUser(users[i].toString());
         }
     }
 
     @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         Channel c = this.getChannel(channel);
         if(c != null) {
             // We got kicked
             if(recipientNick.equals(this.getNick())) {
                 System.out.println("it was us!");
             }
             // Someone else got kicked
             else {
                c.removeUser(nick);
                 c.addRow(recipientNick + " was kicked by " + kickerNick);
             }
         }
     }
     
     /**
      * Initializes the connection
      */
     @Override
     public void run() {
         try {
             this.setVerbose(true);
             if(port == null && password == null) {
                 this.connect(this.serverAddress);
             }
             else if(port != null && password == null) {
                 this.connect(this.serverAddress, Integer.parseInt(port));
             }
             else {
                 this.connect(this.serverAddress, Integer.parseInt(port), password);
             }
             model.connectedServer(this.serverAddress);
         }
         catch(NickAlreadyInUseException e) {
             this.setVerbose(true);
             String altNick = model.getConfs().getUserData("secondaryNick");
             this.setName(altNick);
             this.nick = altNick;
             try {
                 if(port == null && password == null) {
                     this.connect(this.serverAddress);
                 }
                 else if(port != null && password == null) {
                     this.connect(this.serverAddress, Integer.parseInt(port));
                 }
                 else {
                     this.connect(this.serverAddress, Integer.parseInt(port), password);
                 }
             }
             catch(Exception ee) {
                 System.out.println("Cant connect!");
                 model.cantConnect();
             }
         }
         catch(Exception e) {
             System.out.println("Cant connect!");
             model.cantConnect();
         }
     }
 }
