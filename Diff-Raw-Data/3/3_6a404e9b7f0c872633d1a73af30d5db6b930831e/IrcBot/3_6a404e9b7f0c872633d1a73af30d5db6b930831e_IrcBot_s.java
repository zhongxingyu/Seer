 package org.touchirc.irc;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.Event;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.events.ConnectEvent;
 import org.pircbotx.hooks.events.JoinEvent;
 import org.pircbotx.hooks.events.KickEvent;
 import org.pircbotx.hooks.events.MessageEvent;
 import org.pircbotx.hooks.events.ModeEvent;
 import org.pircbotx.hooks.events.NickChangeEvent;
 import org.pircbotx.hooks.events.PartEvent;
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
     		this.server.getConversation(event.getChannel().getName()).addMessage(new Message(event.getMessage(), event.getUser().getNick()));
     		Intent intent = new Intent();
     		intent.setAction("org.touchirc.irc.newMessage");
     		service.sendBroadcast(intent);
     		return;
     	}
 		if(rawevent instanceof JoinEvent){
 			JoinEvent event = (JoinEvent) rawevent;
 			if(event.getUser().equals(event.getBot().getUserBot()) && this.server.getConversation(event.getChannel().getName()) == null){
 				this.server.addConversation(new Conversation(event.getChannel().getName()));
 				System.out.println("JoinEvent "+event.getChannel().getName());
 				return;
 			}
 			Intent intent = new Intent();
 			intent.setAction("org.touchirc.irc.userlistUpdated");
 			service.sendBroadcast(intent);
 			return;
 		}
 		if(rawevent instanceof PartEvent){
 			PartEvent event = (PartEvent) rawevent;
 			if(event.getUser().equals(event.getBot().getUserBot()) && this.server.getConversation(event.getChannel().getName()) != null){
 				this.server.removeConversation(event.getChannel().getName());
 				return;
 			}
 			Intent intent = new Intent();
 			intent.setAction("org.touchirc.irc.userlistUpdated");
 			service.sendBroadcast(intent);
 			return;
 		}
 		if(rawevent instanceof UserListEvent || rawevent instanceof ModeEvent || rawevent instanceof KickEvent || rawevent instanceof NickChangeEvent || rawevent instanceof UserModeEvent){
 			Intent intent = new Intent();
 			intent.setAction("org.touchirc.irc.userlistUpdated");
 			service.sendBroadcast(intent);
 		}
 		if(rawevent instanceof ConnectEvent){
 			ConnectEvent event = (ConnectEvent) rawevent;
 			Log.i("[IrcBot - " + event.getBot().getName() + "]", "Connected");
 			return;
 		}
		
 	}
     
     
 
 }
