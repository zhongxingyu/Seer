 package SeljeIRC;
  
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.*;
 import javax.swing.JOptionPane;
 import javax.swing.JList;
 import javax.swing.text.BadLocationException;
 import jerklib.Channel;
 import jerklib.ConnectionManager;
 import jerklib.Profile;
 import jerklib.Session;
 import jerklib.events.*;
 import jerklib.events.IRCEvent.Type;
 import jerklib.events.modes.ModeAdjustment;
 import jerklib.events.modes.ModeAdjustment.Action;
 import jerklib.events.modes.ModeEvent;
 import jerklib.events.modes.ModeEvent.ModeType;
 import jerklib.listeners.IRCEventListener;
 
 
 /**
  * 
  * @author Jon Arne Westgaard
  * @author Lars Erik Pedersen
  */
 
 
 public class ConnectionHandler implements IRCEventListener {
 	private ConnectionManager manager;
         
         private IRCEvent event = null;
         private boolean hasConnected = false;
         private tabHandler channelTab;
         private Channel channelCommands;
         
         
         /* Regular Expression patterns */
         private Pattern inputCommandFinderPattern = Pattern.compile("^/\\w+(\\s#\\w*)?");
         private Pattern inputCommandTextFinderPattern = Pattern.compile("(^/\\w+(\\s#\\w+)?\\s)([\\w\\s]+)?$");
         private Pattern inputCommandChannelPattern = Pattern.compile("#\\w+");	// Finds a channel name in pattern
         
         
         public ConnectionHandler(tabHandler ct){
  
         	try{
         		channelTab = ct;
 
             channelTab.setConnection(this);
             
             
             //why? hallvard is asking
             //channelTab.createStatusTab();
             
         	}catch(Exception e){
         		System.err.println("System error" + e.getMessage());
         	}
             /*
              * created object
              */
                 
                 
                 
                 
         }
 	
 	public void connectIt(String server, String nicName) {
 	
        
 		channelTab.updateStatusScreen("Trying to establish connection");    
 		manager = new ConnectionManager(new Profile(nicName));
 	
 		Session session = manager.requestConnection(server);
 		session.addIRCEventListener(this);
 	}
 
 	
 	public void receiveEvent(IRCEvent e) {
 		
             event = e;
             //channelTab.updateStatusScreen("Event :"+e.getType().toString());
             
             if (e.getType() == Type.CONNECT_COMPLETE)
 		{   
 			
                         channelTab.updateStatusScreen(I18N.get("connect.success"));
                         hasConnected = true;
                         
 
 
 		}
                 else if(e.getType() == Type.CHANNEL_MESSAGE){
                    // Print channel-messages in channel-tab
                     MessageEvent me = (MessageEvent) e;
                     
                     String ch = me.getChannel().getName();
                     String message = "<"+me.getNick()+">" +" : "+me.getMessage();
                     
                     
             try {
                 channelTab.updateTabScreen(ch, message);
             } catch (BadLocationException ex) {
                 Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
             }
                 }
             
                 else if(e.getType() == Type.PRIVATE_MESSAGE){
                 	MessageEvent me = (MessageEvent) e;
                 	String userNick = me.getNick();
                 	String message = "<" + me.getNick() + ">" + " : " + me.getMessage();
                 	
                 	if(!channelTab.tabExists(userNick))
                 		try {
                              channelTab.createNewTab(userNick, SingleTab.PRIVATE,null);
                         } catch (BadLocationException ex) {
                             Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         try {
                             channelTab.updateTabScreen(userNick, message);
                         } catch (BadLocationException ex) {
                             Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                         }
 
                             }
                 else if(e.getType() == Type.NOTICE){
                         NoticeEvent no = (NoticeEvent) e;
                         
                         String update = no.getNoticeMessage().toString();
                         channelTab.updateStatusScreen(update);                        
                 }
                 else if(e.getType() == Type.ERROR){
                     
                         
                         ErrorEvent no = (ErrorEvent) e;
                         	
                         String update = no.getErrorType().toString();
                         
                         channelTab.updateStatusScreen(update);
                         channelTab.updateStatusScreen("Error data: " + e.getRawEventData()); //TODO REMOVE
                 }
             
                 else if(e.getType() == Type.NICK_CHANGE){
                 	//ErrorEvent no = (ErrorEvent) e;
                 	//String update = no.getErrorType().toString();
                 	//channelTab.updateStatusScreen("NickInUseBuddy");
                     //channelTab.updateStatusScreen(update);
                     NickChangeEvent nce = (NickChangeEvent) e;
                     Iterator<Channel> i = e.getSession().getChannels().iterator();
                     String ch;
                     while (i.hasNext())   {                                // If the user that changed nick is in several channels
                         ch = i.next().getName();
                         channelTab.changedNick(nce.getOldNick(), nce.getNewNick(), ch);
                         try {
                             channelTab.updateTabScreen(ch, "-!- " + nce.getOldNick() + " is known as " + nce.getNewNick()); //TODO I18N
                         } catch (BadLocationException ex) {
                           }
                     }
                 }
                 
                 else if(e.getType() == Type.JOIN_COMPLETE){
                     // Print topic for channel:
                     JoinCompleteEvent jce = (JoinCompleteEvent) e;
                     String ch = jce.getChannel().getName();
                     String message = ("-!- Topic for " +ch +": "+jce.getChannel().getTopic());
                     try {
                         channelTab.createNewTab(ch,SingleTab.CHANNEL, message);
                     } catch (BadLocationException ex) {
                     }
 
                 }
                 else if(e.getType() == Type.NICK_LIST_EVENT){
                     // List users in channel:
                     NickListEvent nle = (NickListEvent) e;
                     String ch = nle.getChannel().getName();
                     List<String> message = nle.getNicks();
                     channelTab.fetchUsers(ch, e.getSession().getChannel(ch));
                     try {
                         channelTab.updateTabScreen(ch, "-!- Users: " + message);
                     } catch (BadLocationException ex) {
                         Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 
                 }
                 
                 else if (e.getType() == Type.JOIN)   {
                     JoinEvent je = (JoinEvent) e;
                     String nick = je.getNick();
                     try {
                         channelTab.updateTabScreen(je.getChannelName(), "-!- " + nick + I18N.get("channel.userjoin"));
                     } catch (BadLocationException ex) {
                         Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                     }
                             channelTab.userJoined(nick,je.getChannelName());
                         }
                 
                 else if (e.getType() == Type.PART)   {
                     PartEvent pe = (PartEvent) e;
                     String nick = pe.getWho();
                     
                     if(!nick.equalsIgnoreCase(event.getSession().getNick())){
                     
                     try {
                         channelTab.updateTabScreen(pe.getChannelName(), "-!- " + nick + I18N.get("channel.userleft"));
                     } catch (BadLocationException ex) {
                         Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                     }
                             channelTab.userLeft(nick, pe.getChannelName());
                         
                     }
                 }
 
                 else if(e.getType() == Type.MODE_EVENT){
                     // Print mode-adjustments
                     ModeEvent me = (ModeEvent) e;
                     ModeAdjustment m;
                     if (me.getChannel() != null)   {
                         String ch = me.getChannel().getName();
                         String message;
                         StringBuilder strModes = new StringBuilder("-!- mode [");
                         StringBuilder strNicks = new StringBuilder();
                         /*try {
                             channelTab.updateTabScreen(ch, "-!- " + e.getRawEventData());
                         } catch (BadLocationException ex) {
                             Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
                         }*/
                         if (me.getModeType() == ModeType.CHANNEL)   {                                   // Voice and Op are channel modes
                             List<ModeAdjustment> modes = me.getModeAdjustments();                       // Get list of adjustments
                             Iterator<ModeAdjustment> i = modes.iterator();
                             while (i.hasNext())   {                                            // Get the first one (there may be more)
                                 m = i.next();
                                 if (m.getMode() == 'o')   {                                                  // Someone got oped / deoped
                                     channelTab.op(m.getArgument(), m.getAction() == Action.PLUS, ch);
                                     strModes.append(m.toString().substring(0, 2));
                                     strNicks.append(m.getArgument()).append(' ');
                                 }
                                 else if (m.getMode() == 'v')   {                                                  // Someone got voiced / devoiced
                                     channelTab.voice(m.getArgument(), m.getAction() == Action.PLUS, ch);
                                     strModes.append(m.toString().substring(0, 2));
                                     strNicks.append(m.getArgument()).append(' ');
                                 }
                                 else
                                     strModes.append(m.toString().substring(0, 2));
                                 
                             }
                             if (me.setBy().length() != 0)                                                 
                                 message = strModes.toString() + " "  + strNicks.toString() + "] by " + me.setBy();
                             else
                                 message = strModes.toString() + "]";
                             try {
                                 channelTab.updateTabScreen(ch, message);
                             } catch (BadLocationException ex) {
                             }
                         }                            
                     }
                 }
                 
                 else if (e.getType() == Type.CTCP_EVENT)   {
                     CtcpEvent ce = (CtcpEvent) e;
                     if(ce.getCtcpString().equals("VERSION"))   {
                         ce.getSession().notice(ce.getNick(), "\001"+"VERSION SeljeIRC v0.1"+"\001");
                     }
                     else if(ce.getCtcpString().contains("PING"))   {
                         System.out.println(ce.getCtcpString());
                         ce.getSession().notice(ce.getNick(), "\001"+ce.getCtcpString()+"\001");
                     }
                     else if(ce.getCtcpString().contains("TIME"))   {
                         DateFormat df = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss Z");
                         Date date = new Date();
                         ce.getSession().notice(ce.getNick(), "\001"+"TIME "+df.format(date)+"\001");
                     }
                     else if (ce.getCtcpString().contains("ACTION"))   {
                         try {
                             channelTab.updateTabScreen(ce.getChannel().getName(), "* "+ce.getNick()+ce.getMessage().substring(7));
                         } catch (BadLocationException ex) {
                         }
                     }
                 }
 
                 
                 else if(e.getType() == Type.WHOIS_EVENT)   {
                     WhoisEvent we = (WhoisEvent) e;
                     channelTab.updateStatusScreen("-!- " + we.getUser() + " [" + we.getHost() + "]");
                     channelTab.updateStatusScreen("-!-  ircname   : " + we.getRealName());
                     channelTab.updateStatusScreen("-!-  channels  : " + we.getChannelNames());
                     channelTab.updateStatusScreen("-!-  server    : " + we.whoisServer() + " [" + we.whoisServerInfo() + "]");
                     channelTab.updateStatusScreen("-!- End of WHOIS");
                     if(we.isIdle()) System.out.println("idle");
                     else System.out.println("not idle");
                 }
             
                 else if(e.getType() == Type.CHANNEL_LIST_EVENT)   {	// Lists all the channels from the servers with topics
                 	ChannelListEvent chEvt = (ChannelListEvent) e;
                 	channelTab.updateStatusScreen("Channel: " + chEvt.getChannelName() + " Users: " + chEvt.getNumberOfUser() + " " + chEvt.getTopic());
                 }
                 else if (e.getType() == Type.TOPIC)   {
                     TopicEvent te = (TopicEvent) e;
                     String chanName = te.getChannel().getName();
                     String setBy = te.getSetBy();
                     String topic = te.getTopic();
                     try {
                         channelTab.updateTabScreen(chanName, "-!- " + setBy + " changed the topic of " + chanName + " to: " + topic );
                     } catch (BadLocationException ex) {
                     }
                 }
                 
                 else if (e.getType() == Type.AWAY_EVENT)   {
                     AwayEvent aw = (AwayEvent) e;
                     String awayUser = aw.getNick();
                     String message = "-!- "+awayUser+" is away: ["+aw.getAwayMessage()+"]";
                     int privTabIdx = channelTab.getIndexOfTab(awayUser);
                     if (aw.getEventType() == AwayEvent.EventType.USER_IS_AWAY)   {
                         channelTab.updateStatusScreen(message);
                         if (privTabIdx > 0)   {
                             try {
                                 channelTab.updateTabScreen(awayUser, message);
                             } catch (BadLocationException ex) {
                             }
                         }
                     }
                     if (aw.isYou() && aw.getEventType() == AwayEvent.EventType.WENT_AWAY)   {
                             channelTab.updateStatusScreen("-!- You have been marked as away");
                     }
                     
                     if(aw.isYou() && aw.getEventType() == AwayEvent.EventType.RETURNED_FROM_AWAY)   {
                             channelTab.updateStatusScreen("-!- You are no longer marked as away");
                     }
                     
                 }
 
                 else    
 		{       // Prints data received from serve
                         if(!e.getRawEventData().matches(".*(311|319|312|320|317|318|321|322|323).*")) //Do not print whois events marked as DEFAULT
                         	channelTab.updateStatusScreen(e.getType() + " " + e.getRawEventData());
 		}
             
         }
         
         
         public void joinChannel (String channel) throws BadLocationException {
            // String tabTitle;
             
             if(connectedToServer()){
                 //create a validate function?
                 if(!channel.startsWith("#")){		// Appends the hash if not provided
                             StringBuilder stBuild = new StringBuilder();
                             stBuild.insert(0, "#");
                             stBuild.append(channel);
                             channel = stBuild.toString();
                     }
                 
                 if(channel  != ""){
                     
                         String ch = reWriteChannel(channel);
                         if(ch == null){
                             event.getSession().join(channel);
                         }
                         else{
                             channelTab.setSelectedIndex(channelTab.indexOfTab(ch));
                         }
                 }else
                   channelTab.updateStatusScreen("Gotta write something");  
             }
             else
                 channelTab.updateStatusScreen("not connected");
            
         }
         
         public void createPrivateChat(String userName){
         	if(connectedToServer()){
         		//event.getSession();
         	}else
         		channelTab.updateStatusScreen("You have to connect to server first");
         }
         
         /**
          * Sets the channel topic for a specified channel
          * 
          * @author Christer Vaskinn
          * @param channelName String, the channel to set the topic at
          * @param channelTopic String, the topic to set on the channel
          */
         
         public void setChannelTopic(String channelName, String channelTopic){
         	if(channelName != null){
         			Channel ircChannel =  event.getSession().getChannel(channelName);
         			ircChannel.setTopic(channelTopic);
         	}else{
         		channelTab.updateStatusScreen("Channel name not provided");
         	}
         }
         
         public boolean connectedToServer(){
             
             if(event != null){
                 if(hasConnected)
                     return true;
                 else
                     return false;
                 
             }
             else
                 return false;
         }
         /*
          * sending string directly to server
          */
                 
 
         public void sayToServer(String inputString){
         	this.sayToServer(inputString, null);
         }
         /**
          * Checks commands typed in input field
          * 
          * The ugliest code I've ever written :/
          * 
          * @author Christer Vaskinn
          * @param inputString The field provided from the input field
          * @param channelName The channelname provided by the inputfield
          */
         public void sayToServer(String inputString, String channelName){
         	
         	if(connectedToServer()){
         		
             	Matcher stringCommandFinder = inputCommandFinderPattern.matcher(inputString);  // Finds command and possible channel name
             	channelTab.updateStatusScreen(inputString);			// Updates statusscreen with the command (no matter what command);
             	
             	if(stringCommandFinder.find()){						// Checks the matcher for commands
             		
             		
             		try{
             			Matcher channelMatcher = inputCommandChannelPattern.matcher(inputString);
             			String commandFromUser = stringCommandFinder.group().toLowerCase();		// Is pretty
             			
             			String textFromCommand = inputString.substring(stringCommandFinder.end());
             			
            			if(channelMatcher.find() || channelName == null)		// Finds channel name in command
             				channelName = channelMatcher.group();
             			
             			if(commandFromUser.startsWith("/topic"))				// Sets the channel topic
             				this.setChannelTopic(channelName, textFromCommand);
             			else if(commandFromUser.startsWith("/j") || commandFromUser.startsWith("/join")){	// Joins channel Require hashtag
             				joinChannel(channelName);
             			}
             			else if(commandFromUser.startsWith("/op")){							// Set op in the channel
             				Channel ch = event.getSession().getChannel(channelName);
             				ch.op(textFromCommand);
             			}
             			else if(commandFromUser.startsWith("/deop")){						// Set deop in the channel
             				Channel ch = event.getSession().getChannel(channelName);
             				ch.deop(textFromCommand);
             			}
             			else if(commandFromUser.startsWith("/voice")){						// Set voice in the channel
             				Channel ch = event.getSession().getChannel(channelName);
             				ch.voice(textFromCommand);
             			}
             			else if(commandFromUser.startsWith("/devoice")){					// Set devoice in the channel
             				Channel ch = event.getSession().getChannel(channelName);
             				ch.deVoice(textFromCommand);
             			}
             			else if(commandFromUser.startsWith("/mode")){						// Set mode in the channel
             				Channel ch = event.getSession().getChannel(channelName);
             				ch.mode(textFromCommand);										
             			}
                                 else if (commandFromUser.startsWith("/away")){
                                     if (!textFromCommand.equals(""))
                                         event.getSession().setAway(textFromCommand);
                                     else
                                         event.getSession().unsetAway();
                                 }
             			else if(commandFromUser.startsWith("/raw")){						// Send Raw in the irc
             				event.getSession().sayRaw(inputString);
             			}
 
             			else if(commandFromUser.startsWith("/quit")){						// Quit from server
             				System.exit(0);
             			}
             			else if(commandFromUser.startsWith("/help")){						// Help commands
             				String allowedCommands = new String("Allowed commands: /help, /raw, /mode, /voice, /deop, /op, /j, /join, topic");
             				if(channelName != null)
             					channelTab.updateTabScreen(channelName, allowedCommands);
             				else
             					channelTab.updateStatusScreen(allowedCommands);
             			}
             			else if(commandFromUser.startsWith("/disconnect")){					// Disconnect from server
             				this.closeConnection();
             			}
             			else
             				channelTab.updateStatusScreen("Y U NO PROVIDE CORRECT COMMAND");	// TODO: Translate?
             			
             		}catch(Exception e){
             			System.err.println("Exception caught: " + e.getMessage());
             		}
             } else {														// If not connected to server
                 channelTab.updateStatusScreen(inputString + "\n" + "Not connected to server tough");
 
             }
         } 
                 
         }
         /*
          * sending string to channel
          */
         public void sayToChannel(String whatToSay,String channel) throws BadLocationException{
             
             try{
             	event.getSession().sayChannel(whatToSay,event.getSession().getChannel(channel));
             /*
              * just so you see what you write
              */
             	channelTab.updateTabScreen(channel, "<"+event.getSession().getNick()+"> " +whatToSay);
             }catch(BadLocationException ex){
             	System.err.println("Error sending private message: " + ex.getMessage());
             }
             
         }
         
         public void sayToPrivate(final String whatToSay, final String nickName){
         	try{
         		event.getSession().sayPrivate(nickName, whatToSay);
         	
         		channelTab.updateTabScreen(nickName, "<"+event.getSession().getNick()+"> " +whatToSay);
         	}catch(Exception ex){
         		System.err.println("Error sending private message: " + ex.getMessage());
         	}
         }
         
         public void closeConnection(){
             manager.quit();
             channelTab.removeAllTabs();
             System.out.printf("Closing manager");
         }
         /**
          * Get's all the channels from the server
          * 
          * @author Christer Vaskinn
          * @note This function is really fucking stupid and using it on the major networks will get your ass K-lined(banned)
          */
         public void getAllTheChannelsFromServer(){
         	if(this.connectedToServer()){
         		channelTab.updateStatusScreen("List of available channer with users:");
         		event.getSession().chanList();
 			}else
         		channelTab.updateStatusScreen("Not connected to a server jackass!");
         }
         
         public void disconnectFromChannel(String channel){
             
             //TODO Final event comes after part and creates exception because its routed to tab and not statusscreen
             event.getSession().getChannel(channel).part(channel);
             
         }
         
        
         public Session getCurrentSession()   {
             return event.getSession();
         }
         
         public void validateChannel(String ch){
             
         }
         
         public String reWriteChannel(String ch){
             
             List<Channel> l = event.getSession().getChannels();
             Iterator<Channel> i = l.iterator();
             String ls;
             while(i.hasNext())   {
                 ls = i.next().getName();
                 if(ls.equalsIgnoreCase(ch))
                     return ls;
             }
             return null;
         }
             
         
         
 	
 } // End of public class ConnectionHandler	
 
