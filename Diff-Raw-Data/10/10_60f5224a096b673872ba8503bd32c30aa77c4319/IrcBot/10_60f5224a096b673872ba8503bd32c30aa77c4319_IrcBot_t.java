 package org.touchirc.irc;
 
 import java.util.regex.Pattern;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.Event;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.events.ActionEvent;
 import org.pircbotx.hooks.events.ConnectEvent;
 import org.pircbotx.hooks.events.JoinEvent;
 import org.pircbotx.hooks.events.KickEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.ModeEvent;
 import org.pircbotx.hooks.events.NickChangeEvent;
 import org.pircbotx.hooks.events.PartEvent;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 import org.pircbotx.hooks.events.UserListEvent;
 import org.pircbotx.hooks.events.UserModeEvent;
 import org.touchirc.model.Conversation;
 import org.touchirc.model.Message;
 import org.touchirc.model.Server;
 
 import android.content.Intent;
 import android.util.Log;
 
 public class IrcBot extends PircBotX implements Listener<IrcBot>{
 	
 	private IrcService service;
 	private Server server;
 	public boolean isConnected;
 	private Pattern highlight;
 
 	public IrcBot(Server server, IrcService service) {
 		this.service = service;
 		this.server = server;
 		this.getListenerManager().addListener(this);
 	}
 
 		
 	/**
 	 * Set the Nick of the user
 	 * 
 	 * @param nickName
 	 */
 	
 	public void setNickName(String nickName){
 		//TODO Add security : the nickname can't have some caracters
 		this.setName(nickName);
 	}
 	
 	/**
      * Set the ident of the user
      *
      * @param ident 
      */
     public void setIdent(String ident)
     {
         this.setLogin(ident);
     }
     
     /**
      * Set the real name of the user
      *
      * @param realName 
      */
     public void setRealName(String realName)
     {
         this.setVersion(realName);
     }
     
     @SuppressWarnings("rawtypes")
 	@Override
 	public void onEvent(Event rawevent) throws Exception {
 		
     	if(rawevent instanceof MessageEvent){
     		MessageEvent event = (MessageEvent) rawevent;
     		if(highlight.matcher(event.getMessage()).find()){
   				this.server.getConversation(event.getChannel().getName()).addMessage(new Message(event.getMessage(), event.getUser().getNick(), Message.TYPE_MENTION));
     		}else{
     			this.server.getConversation(event.getChannel().getName()).addMessage(new Message(event.getMessage(), event.getUser().getNick()));
     		}
     		service.sendBroadcast(new Intent().setAction("org.touchirc.irc.newMessage"));
     		return;
     	}
     	if(rawevent instanceof PrivateMessageEvent){
     		PrivateMessageEvent event = (PrivateMessageEvent) rawevent;
     		String user = event.getUser().getNick();
     		if(!server.hasConversation(user)){
     			server.addConversation(new Conversation(user));
     			service.sendBroadcast(new Intent().setAction("org.touchirc.irc.channellistUpdated"));
     		}
     		this.server.getConversation(user).addMessage(new Message(event.getMessage(), user));
     		service.sendBroadcast(new Intent().setAction("org.touchirc.irc.newMessage"));
     		return;
     	}
     	if(rawevent instanceof ActionEvent){
 			ActionEvent event = (ActionEvent) rawevent;
 			this.server.getConversation(event.getChannel().getName()).addMessage(new Message(event.getMessage(), event.getUser().getNick(), Message.TYPE_ACTION));
     		service.sendBroadcast(new Intent().setAction("org.touchirc.irc.newMessage"));
     		return;
 		}
 		if(rawevent instanceof JoinEvent){
 			JoinEvent event = (JoinEvent) rawevent;
			if(event.getUser().equals(event.getBot().getUserBot()) && this.server.getConversation(event.getChannel().getName()) == null){
 				this.server.addConversation(new Conversation(event.getChannel().getName()));
 				System.out.println("JoinEvent "+event.getChannel().getName());
 				service.sendBroadcast(new Intent().setAction("org.touchirc.irc.channellistUpdated"));
 			}
 			service.sendBroadcast(new Intent().setAction("org.touchirc.irc.userlistUpdated"));
 			return;
 		}
 		if(rawevent instanceof PartEvent){
 			PartEvent event = (PartEvent) rawevent;
 			if(event.getUser().equals(event.getBot().getUserBot()) && this.server.getConversation(event.getChannel().getName()) != null){
 				this.server.removeConversation(event.getChannel().getName());
 				service.sendBroadcast(new Intent().setAction("org.touchirc.irc.channellistUpdated"));
 			}
 			service.sendBroadcast(new Intent().setAction("org.touchirc.irc.userlistUpdated"));
 			return;
 		}
 		if(rawevent instanceof UserListEvent || rawevent instanceof ModeEvent || rawevent instanceof KickEvent || rawevent instanceof NickChangeEvent || rawevent instanceof UserModeEvent){
 			service.sendBroadcast(new Intent().setAction("org.touchirc.irc.userlistUpdated"));
 			return;
 		}
 		if(rawevent instanceof ConnectEvent){
 			ConnectEvent event = (ConnectEvent) rawevent;
 			isConnected = true;
 			// TODO add some highlight preference
 			highlight = Pattern.compile(Pattern.quote(this.getNick()), Pattern.CASE_INSENSITIVE);
 			Log.i("[IrcBot - " + event.getBot().getName() + "]", "Connected");
 			return;
 		}
 	}
     
     
 
 }
