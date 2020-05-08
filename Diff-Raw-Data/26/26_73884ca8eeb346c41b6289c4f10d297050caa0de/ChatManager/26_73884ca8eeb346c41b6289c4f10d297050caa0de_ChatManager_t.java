 package sk.mattho.portlets.chatPortlet.chat;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.inject.Inject;
 
 import sk.mattho.portlets.chatPortlet.Controllers.DBController;
 import sk.mattho.portlets.chatPortlet.chat.ChatConfigurations;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ChatEventsListener;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ChatInterface;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.Contact;
 import sk.mattho.portlets.chatPortlet.chat.irc.IrcChannel;
 import sk.mattho.portlets.chatPortlet.chat.irc.IrcChat;
 import sk.mattho.portlets.chatPortlet.chat.xmpp.XmppChat;
 import sk.mattho.portlets.chatPortlet.model.DBContact;
 
 public class ChatManager implements ChatEventsListener, Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private List<ChatInterface> accounts;
 	private List<Contact> conversations;
 	private List<Contact> friends;
 
 	
 
 	@PostConstruct
 	public void init() {
 	//	System.out.println("iniing");
 		this.accounts = new ArrayList<ChatInterface>();
 		this.conversations = new ArrayList<Contact>();
 		this.friends = new ArrayList<Contact>();
 		
 	}
 
 	public void addChatListener(ChatEventsListener listener) {
 		for (ChatInterface ch : accounts)
 			ch.addListener(listener);
 	}
 
 	public void removeListener(ChatEventsListener listener) {
 		for (ChatInterface ch : accounts)
 			ch.removeListener(listener);
 	}
 
 	/**
 	 * This method adding contacts in conversation list.
 	 * 
 	 * @param contact
 	 *            - contact to conversation start with
 	 */
 	public boolean newConversation(Contact contact) {
 		if (this.conversations.contains(contact))
 			//System.out.println(contact + "already exist!");
 			return false;
 		else {
 			this.conversations.add(contact);
 			contact.beginConversation();
 			return true;
 		}
 	}
 
 	public Contact exitConversation(String convName) {
 
 		Contact c = this.findContactByIdName(convName);
 		if (c != null) {
 			c.endConversation();
 			this.conversations.remove(c);
 			System.out.print("Removed from conversations: " + convName);
 		}
 		return c;
 	}
 
 	private Contact findContactByIdName(String convName) {
 		Contact c = null;
 		for (ChatInterface i : this.accounts) {
 			c = i.findContactByIdName(convName);
 			if (c != null)
 				return c;
 		}
 		return c;
 	}
 
 	
 
 	public boolean addAccount(String userName, String password, String server,
 			int port, String serviceName,ChatConfigurations con, ChatEventsListener listener) throws Exception {
 		ChatInterface chat;
 
 		
 		switch(con){
 		case IRC:{
 			chat= new IrcChat();
 			((IrcChat)chat).setIrcChannelName(serviceName);
 			//chat.set
 			chat.setPort(port);
 			chat.setServer(server);
 		}break;
 		case XMPP:{
 			 chat = new XmppChat();
 			 chat.setPort(port);
 			 chat.setServer(server);
 			 ((XmppChat)chat).setServiceName(serviceName);
 		}break;
 		case GTALK:
 		case FACEBOOK_CHAT:{
 			System.out.println("seting gtalk or fb chat server:" +con.getServer()+"port"+con.getPort()+"");
 			chat= new XmppChat();
 			chat.setServer(con.getServer());
 			chat.setPort(con.getPort());
 			((XmppChat)chat).setServiceName(con.getDomain());
 		}break;
 		default: chat= new XmppChat();
 		}
 		chat.setUsername(userName);
 		chat.addListener(this);
 		chat.addListener(listener);
 		System.out.println("connecting "+chat.getServer() );
 		if (chat.connect(userName, password)) {
 			this.accounts.add(chat);
 			if(con==ChatConfigurations.IRC)
 			{
 				//TODO adding ircchannel direct into conversations
 			//	this.conversations.addAll(chat.getContacts());
 			}
 		
 			this.friends.addAll(chat.getContacts());
 			return true;
 		}
 		throw new Exception();
 	}
 
 	public void disconnect() {
 	//	System
 		for (ChatInterface c : this.accounts) {
 			System.out.println("Disconnecting");
 			c.disconnect();
 		}
 		this.accounts.clear();
 		this.friends.clear();
 		this.conversations.clear();
 	}
 
 	public void disconnect(String server, String username) {
 			ChatInterface ch=this.searchAccount(server, username);
 			if(ch!=null)
 				{ch.disconnect();
 					this.accounts.remove(ch);
 				}
 			
 	}
 
 	public ChatInterface searchAccount(String server, String username) {
 		for (ChatInterface ch : this.accounts)
 			if (ch.getServer().compareTo(server) == 0
 					&& ch.getUsername().compareTo(username) == 0)
 				return ch;
 		return null;
 	}
 
 	// -----------------------------------------------------------
 	// IMPLEMENTS OF chateventsListener
 	// -----------------------------------------------------------
 	@Override
 	public void processMessage(Contact c) {
 		//if (this.conversations.size() < 1) {
 		//	this.conversations.add(c);
 
 	//	} else 
			if (!this.conversations.contains(c)&& !(c instanceof IrcChannel))
 			this.conversations.add(c);
 	}
 
 	@Override
 	public void processIncomingChat(Contact c) {
 		if (!this.conversations.contains(c))
 			this.conversations.add(c);
 	}
 
 	@Override
 	public void connected(ChatInterface chatInterface) {
 		// TODO Auto-generatedt method stub
 		
 		System.out.println("Connected to "+ chatInterface.getServer());
 	}
 
 	@Override
 	public void disconnected(ChatInterface chatInterface) {
 		// TODO Auto-generated method stub
 		// for(int i;i<this.friends.size();i++){
 		// if(c.)
 		List<Contact> contacts= new ArrayList<Contact>();
 		
 		for (Contact c : this.friends) {
 			if (c.isFrom(chatInterface))
 				contacts.add(c);
 		}
 		
 		for (Contact c :contacts) {
 			if(this.friends.contains(c))
 				this.friends.remove(c);
 			if(this.conversations.contains(c))
 				this.conversations.remove(c);
 		}
 		
 		
 		contacts.clear();
 	//	contacts=null;
 	//	this.accounts.remove(chatInterface);
 
 	}
 
 	@Override
 	public void processContactStateChanged(Contact c) {
 		// do nothing
 	}
 
 	// -----------------------------------------------------------
 	// GETTERS AND SETTERS
 	// -----------------------------------------------------------
 	public List<Contact> getConversations() {
 		return conversations;
 	}
 
 	public List<Contact> getFriends() {
 		return friends;
 	}
 
 	public void setFriends(List<Contact> friends) {
 		// this.friends = friends;
 	}
 
 	public List<ChatInterface> getAccounts() {
 		return accounts;
 	}
 	public List<Contact> getOnlineFriends(){
 		List<Contact> res= new ArrayList<Contact>();
 		for(Contact c:this.friends){
 			if(c instanceof IrcChannel)
 				res.add(c);
 			else if(c.isOnline())
 				res.add(c);
 				
 		}
 		return res;
 	}
 
 }
