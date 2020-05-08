 package sk.mattho.portlets.chatPortlet.chat.xmpp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.annotation.PreDestroy;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.SASLAuthentication;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 
 import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
 import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.pubsub.PresenceState;
 
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ChatInterface;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.Contact;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ContactState;
 
 public class XmppChat extends ChatInterface {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1257959342464436632L;
 	private XMPPConnection connection;
 	private List<Contact> contacts;
 	private String serviceName;
 	private  RosterListener rosterListener;
 	private ChatManagerListener chatManagerListener;
 
 	public XmppChat(){
 		super();
 		this.init();
 	}
 	public String getServiceName() {
 		return serviceName;
 	}
 	
 	public void init(){
 	
 		this.chatManagerListener= new ChatManagerListener() {
 			@Override
 			public void chatCreated(Chat chat, boolean createdLocally) {
 				System.out.println("Incoming chat!!!!"
 						+ chat.getParticipant());
 				XmppContact contact = (XmppContact) findContactByIdName(chat
 						.getParticipant().split("/")[0]);
 				contact.initChat(chat);
 				notifyIncomingChat(contact);
 
 			}
 		};
 		
 	this.rosterListener= new RosterListener() {
 
 		@Override
 		public void presenceChanged(Presence presence) {
 			Contact c = findContactByIdName(presence.getFrom().split(
 					"/")[0]);
 			
 			if(c.getAvatar()==null){
 				try {
 					VCard vcard= new VCard();
 					vcard.load(connection,c.getIdName());
 					c.setAvatar(vcard.getAvatar());
 				} catch (XMPPException e) {
 					// TODO Auto-generated catch block
 					//e.printStackTrace();
 					System.out.print("vcard problem");
 				};
 			}
 			if (presence.getType() == Presence.Type.available) {
 				c.setOnline(true);
 				c.setState(ContactState.AVAILABLE);
 
 			
 			if (presence.getStatus() != "")
 				c.setStatus(presence.getStatus());
 
 			switch (presence.getMode()) {
 
 			case available:
 				c.setState(ContactState.AVAILABLE);
 				break;
 			case away:
 				c.setState(ContactState.AWAY);
 				break;
 			case dnd:
 				c.setState(ContactState.DND);
 
 				break;
 			case chat:
 				c.setState(ContactState.AVAILABLE);
 				break;
 			case xa:
 				c.setState(ContactState.AWAY);
 				break;
 				
 			default:
 				c.setState(ContactState.AVAILABLE);
 			}
 			} else{
 				c.setOnline(false);
 				c.setState(null);
 			}
 
 			notifyContactChange(c);
 		}
 
 		@Override
 		public void entriesUpdated(Collection<String> arg0) {
 			System.out.println("Entry " + arg0 + " was updated");
 		}
 
 		@Override
 		public void entriesDeleted(Collection<String> arg0) {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void entriesAdded(Collection<String> arg0) {
 			// TODO Auto-generated method stub
 		}
 	};
 		
 	
 	}
 	public void setServiceName(String serviceName) {
 		this.serviceName = serviceName;
 	}
 
 	@Override
 	public boolean connect(String name, String password) {
 		// TODO throw exception for bad login etc.
 		if (this.getServer().contains("facebook")) {
 			SASLAuthentication.registerSASLMechanism("DIGEST-MD5",
 					MySASLDigestMD5Mechanism.class);
 			SASLAuthentication.supportSASLMechanism("DIGEST-MD5", 0);
 		}
 		ConnectionConfiguration connConfig = new ConnectionConfiguration(
 				this.getServer(), this.getPort(), this.getServiceName());
 		connConfig.setSASLAuthenticationEnabled(true);
 		// ("talk.google.com", 5222, "gmail.com");
 		
 		this.connection = new XMPPConnection(connConfig);
 		
 	//	this.connection.DEBUG_ENABLED=true;
 		try {
 			connection.connect();
 
 		//	System.out.println("Connected to " + connection.getHost());
 		} catch (XMPPException ex) {
 			 ex.printStackTrace();
 			System.out.println("Failed to connect to " + connection.getHost());
 
 			return false;
 
 		}
 		try {
 			this.connection.login(name, password);
 			System.out.println("Logged in as " + connection.getUser());
 			this.connection.getRoster().addRosterListener(rosterListener);
			Presence p = new Presence(Type.available);
			this.connection.sendPacket(p);
 		//	
 			this.InitContacts();
 			Thread.sleep(2000);
 			this.notifyConnected();
 		} catch (XMPPException ex) {
 			 ex.printStackTrace();
 			System.out.println("Failed to log in as "
 					+ (connection.getUser().split("@"))[0]);
 			System.exit(1);
 			return false;
 		
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return true;
 
 	}
 
 	private void InitContacts() {
 		this.contacts = new ArrayList<Contact>();
 		if (this.connection != null && this.connection.isConnected()) {
 			org.jivesoftware.smack.ChatManager chatManager = connection
 					.getChatManager();
 			chatManager.addChatListener(this.chatManagerListener);
 			
 			Roster roster = connection.getRoster();
 			
 			Collection<RosterEntry> entries = roster.getEntries();
 			for (RosterEntry r : entries) {
 				XmppContact c = new XmppContact(chatManager);
 				c.setIdName(r.getUser());
 				c.setName(r.getName());
 				c.setMySession(this);
 				
 				
 				
 				c.setRosterEntry(r);
 				
 				// r.
 				
 				Presence presence = roster.getPresence(r.getUser());
 				c.setPresenceFrom(presence.getFrom());
 				if (presence.getType() ==Presence.Type.available){
 					
 					c.setOnline(true);
 					 if(presence.getMode()!=null)
 					switch (presence.getMode()) {
 
 					case available:
 						c.setState(ContactState.AVAILABLE);
 						break;
 					case away:
 						c.setState(ContactState.AWAY);
 						break;
 					case dnd:
 						c.setState(ContactState.DND);
 
 						break;
 					case chat:
 						c.setState(ContactState.AVAILABLE);
 						break;
 					case xa:
 						c.setState(ContactState.AWAY);
 						break;
 						
 					default:
 						c.setState(ContactState.AVAILABLE);
 					}
 					
 				
 				}
 				else
 					c.setOnline(false);
 				this.contacts.add(c);
 			}
 		} else
 			System.out.println("You are not connected!");
 
 	}
 
 	@Override
 	public List<Contact> getContacts() {
 
 		return this.contacts;
 
 	}
 
 	@Override
 	public void sendMessage(Contact to, String message) {
 		// TODO Auto-generated method stub
 		System.out.println("sprava pre " + to.getName() + ": " + message);
 		// ((XmppContact)to).sendMessage(message);
 	}
 
 	@Override
 	public void disconnect() {
 		this.contacts.clear();
 		this.connection.getRoster().removeRosterListener(this.rosterListener);
 		this.connection.getChatManager().removeChatListener(this.chatManagerListener);
 		// this.connection.disconnect(Presence.Type.unavailable);
 		//this.connection.
 		this.connection.disconnect();
 		this.contacts.clear();
 		//thos/this.connection.shutdown();
 		this.connection=null;
 		this.notifyDisconnected();
 
 	}
 
 	@PreDestroy
 	public void preDestroy() {
 		if (this.connection.isConnected())
 			this.disconnect();
 		
 		this.connection=null;
 	}
 
 	@Override
 	public boolean deleteContact() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean addContact(Contact toAdd) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public Contact findContact(String idName) {
 		// Contact temp=null;
 		for (Contact c : this.getContacts()) {
 			if (c.getIdName().compareTo(idName) == 0)
 				return c;
 		}
 		return null;
 	}
 
 	@Override
 	public Contact findContactByIdName(String idName) {
 		// Contact temp=null;
 		for (Contact c : this.getContacts()) {
 			// String[] contactName=
 			if (((XmppContact) c).getIdName().compareTo(idName) == 0)
 				return c;
 		}
 		return null;
 	}
 
 }
