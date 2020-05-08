 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
  * contributors by the @authors tag. See the copyright.txt in the 
  * distribution for a full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,  
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sk.mattho.portlets.chatPortlet.Controllers;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Serializable;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.imageio.ImageIO;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.portlet.ActionRequest;
 
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequest;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.ValidatorException;
 
 import org.pircbotx.User;
 import org.richfaces.application.push.MessageException;
 import org.richfaces.application.push.TopicKey;
 import org.richfaces.application.push.TopicsContext;
 import sk.mattho.portlets.chatPortlet.AccountInfo;
 import sk.mattho.portlets.chatPortlet.PreferencesAccounts;
 import sk.mattho.portlets.chatPortlet.chat.ChatManager;
 import sk.mattho.portlets.chatPortlet.chat.ChatConfigurations;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ChatEventsListener;
 import sk.mattho.portlets.chatPortlet.chat.genericChat.ChatInterface;
 
 import sk.mattho.portlets.chatPortlet.chat.genericChat.Contact;
 import sk.mattho.portlets.chatPortlet.chat.irc.IrcChannel;
 import sk.mattho.portlets.chatPortlet.chat.xmpp.XmppContact;
 import sk.mattho.portlets.chatPortlet.model.DBContact;
 
 /**
  * {@link ChatController} is the JSF backing bean for the application, holding
  * the input data to be re-displayed.
  */
 @Named
 @SessionScoped
 public class ChatController implements Serializable, ChatEventsListener {
 	private boolean connected;
 	private boolean pushWorking;
 
 	private Contact currentContact;
 	private List<Contact> onlineContacts;
 	private String username;
 	private String service;
 	private String password;
 	private Integer port;
 	private String server;
 	private PreferencesAccounts storedAccounts;
 	private boolean saveAccount;
 
 	private ChatConfigurations[] protocols;
 
 	private final String userIdentifier = UUID.randomUUID().toString()
 			.replace("-", "");
 
 	private String actualTab;
 
 	private TopicsContext topicsContext;
 	private static final String CDI_PUSH_TOPIC = "imService";
 
 	@Inject
 	private ChatManager manager;
 
 	@Inject
 	private DBController db;
 	private static final long serialVersionUID = -6239437588285327644L;
 
 	@PostConstruct
 	public void postContruct() {
 		this.connected = false;
 		this.username = "";
 		this.selectedProtocol = ChatConfigurations.XMPP;
 		this.port = 0;
 		this.server = "";
 		this.password = "";
 		this.service = "";
 		this.protocols = ChatConfigurations.values();
 
 		// db.saveDbContact(d);
 		// return "login";
 
 	}
 
 	public boolean isConnected() {
 
 		return this.connected;
 	}
 
 	public void initFromPreferences() {
 		PreferencesAccounts pr = this.getFromPreferences();
 		if (pr != null) {
 			this.storedAccounts = pr;
 			for (AccountInfo a : pr.accounts()) {
 				try {
 					if (this.manager.addAccount(a.getUserName(),
 							a.getPassword(), a.getServer(), a.getPort(),
 							a.getDomain(), a.getProtocol(), this))
 						this.connected = true;
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else
 			this.storedAccounts = new PreferencesAccounts();
 
 	}
 
 	private boolean saveToPreferences(PreferencesAccounts pref)
 			throws ReadOnlyException, ValidatorException, IOException {
 		PortletRequest request = (PortletRequest) FacesContext
 				.getCurrentInstance().getExternalContext().getRequest();
 		if (request instanceof ActionRequest) {
 			ActionRequest actionReq = (ActionRequest) request;
 			PortletPreferences p = actionReq.getPreferences();
 			p.reset("accounts");
 			p.setValue("accounts", pref.getAccounts());
 			actionReq.getPreferences().store();
 			System.out.println("preferences stored");
 			return true;
 
 		} else {
 			System.out.print("Isn't instance of ActionRequest");
 		}
 		return false;
 	}
 
 	private void addToPreferences() throws ReadOnlyException,
 			ValidatorException, IOException {
 		PreferencesAccounts pr = this.getFromPreferences();
 		if (pr == null)
 			pr = new PreferencesAccounts();
 		AccountInfo prefAccount = new AccountInfo(username, password,
 				selectedProtocol);
 		prefAccount.setDomain(service);
 		prefAccount.setServer(server);
 		prefAccount.setPort(port);
 		pr.addAccount(prefAccount);
 
 		this.saveToPreferences(pr);
 		this.storedAccounts = pr;
 	}
 
 	private PreferencesAccounts getFromPreferences() {
 		Object request = FacesContext.getCurrentInstance().getExternalContext()
 				.getRequest();
 		if (request instanceof ActionRequest) {
 			ActionRequest actionReq = (ActionRequest) request;
 			PortletPreferences p = actionReq.getPreferences();
 			String temp = "";
 			temp = p.getValue("accounts", temp);
 
 			if (temp != null && temp.compareTo("") != 0) {
 				PreferencesAccounts pr = new PreferencesAccounts(temp);
 				return pr;
 
 			} else {
 				FacesContext.getCurrentInstance().addMessage(null,
 						new FacesMessage("You haven't saved any account"));
 				;
 				this.storedAccounts = new PreferencesAccounts();
 			}
 			return null;
 
 		}
 		return null;
 	}
 
 	public void deleteAccount(AccountInfo acc) throws ReadOnlyException,
 			ValidatorException, IOException {
 		this.disconnectAccount(acc);
 		this.storedAccounts.accounts().remove(acc);
 		this.saveToPreferences(this.storedAccounts);
 	}
 
 	public void disconnectAccount(AccountInfo acc) {
 		this.manager.disconnect(
 				((acc.getProtocol() == ChatConfigurations.XMPP || acc
 						.getProtocol() == ChatConfigurations.IRC) ? acc
 						.getServer() : acc.getProtocol().getServer()), acc
 						.getUserName());
 		if (this.manager.getAccounts().size() < 1)
 			this.connected = false;
 	}
 
 	public void initContacts() {
 		this.onlineContacts=this.manager.getOnlineFriends();
 		Comparator<Contact> c = new Comparator<Contact>() {
 
 			@Override
 			public int compare(Contact o1, Contact o2) {
 
 				if (o1.getState() == null && o2.getState() == null)
 					return 0;
 				else if (o1.getState() == null && o2.getState() != null)
 					return -1;
 				else if (o1.getState() != null && o2.getState() == null)
 					return 1;
 				// else if (o)
 
 				if (o1.getState().compareTo(o2.getState()) == 0)
 					return o1.getIdName().compareTo(o2.getIdName());
 				else
 					return o1.getState().compareTo(o2.getState());
 			}
 		};
 		Collections.sort(this.onlineContacts, c);
 	//	return this.onlineContacts;
 
 		
 	}
 	public List<Contact> getContacts(){
 		return this.onlineContacts;
 	}
 
 	/**
 	 * Resets {@link #name} to the default value {@code "World"} and
 	 * {@link #greeting} with the default value {@code "Hello"}.
 	 * 
 	 * @param ae
 	 *            ignored
 	 * @throws IOException
 	 * @throws ValidatorException
 	 * @throws ReadOnlyException
 	 */
 
 	public void connect() throws ReadOnlyException, ValidatorException,
 			IOException {
 		try {
 			this.connected = this.manager.addAccount(this.username,
 					this.password, this.server, port, this.service,
 					this.selectedProtocol, this);
 			if (this.saveAccount && this.connected)
 				this.addToPreferences();
 		} catch (Exception e) {
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage("Login failed! Check your configuration"));
 			// e.printStackTrace();
 		}
 	}
 
 	public void disconnect() {
 		this.connected = false;
 		for(Contact c:this.manager.getConversations()){
 			this.saveHistory(c);
 		}
 		this.manager.disconnect();
 
 	}
 
 	public void addAccount() throws ReadOnlyException, ValidatorException,
 			IOException {
 		try {
 			boolean result = this.manager.addAccount(this.username,
 					this.password, this.server, port, this.service,
 					this.selectedProtocol, this);
 			if (this.saveAccount && result)
 				this.addToPreferences();
 		} catch (Exception e) {
 			FacesContext.getCurrentInstance().addMessage(null,
 					new FacesMessage("Login failed! Check your configuration"));
 			e.printStackTrace();
 		}
 	}
 
 	public void setCurrentConversation(String idname) {
 		this.setActualTab(idname);
 	}
 
 	public void newConversation(Contact contact) {
 		this.setActualTab(contact.getIdName());
 		this.currentContact = contact;
 		// if contact is succesfully added
 		if (this.manager.newConversation(contact)) {
 			// if is contact for xmpp
 		//	if (contact instanceof XmppContact) {
 		//		db.initContact((XmppContact) contact);
 	//	}
 		}
 
 		messageWindowRefresh();
 	}
 
 	private TopicsContext getTopicsContext() {
 
 		if (topicsContext == null) {
 			topicsContext = TopicsContext.lookup();
 		}
 		return topicsContext;
 	}
 
 	public void exitConversation() {
 		String conv = (FacesContext.getCurrentInstance().getExternalContext()
 				.getRequestParameterMap().get("current"));
 		Contact c= this.manager.exitConversation(conv);
 		if (this.manager.getConversations().size() > 0) {
 			this.currentContact = this.manager.getConversations().get(0);
 			this.actualTab = currentContact.getIdName();
 		} else {
 			this.currentContact = null;
 			this.actualTab = "";
 		}
 		this.saveHistory(c);
 	}
 
 	public Date getDate() {
 		return new Date();
 	}
 
 	public void saveHistory(Contact c){
 		if(c instanceof XmppContact)
 			db.saveHistory((XmppContact) c);
 	}
 	public void setActualTab(String actualTab) {
 		// find in conversations
 		// Contact c=null;
 		this.actualTab = actualTab;
 		for (Contact con : this.manager.getConversations()) {
 			if (con.getIdName().compareTo(actualTab) == 0) {
 				this.currentContact = con;
 				this.currentContact.setHasUnreadedMessage(false);
 				// System.out.println("setting actual tab" + con);
 				break;
 			}
 		}
 		// this.conversations.g
 
 	}
 	public void loadHistory(){
 		if(this.currentContact !=null && this.currentContact instanceof XmppContact)
 		db.loadHistory((XmppContact) this.currentContact);
 		messageWindowRefresh();
 	}
 	
 	public boolean isIrc() {
 		if (this.currentContact instanceof IrcChannel)
 			return true;
 		return false;
 	}
 
 	/**
 	 * Escaping input string and adding smilies
 	 * 
 	 * @param input
 	 *            input string
 	 * @return String with escaped emoticons
 	 */
 	public String escapeString(String input) {
 		String ret = input;
 		ret = ret.replaceAll("&", "&amp;");
 		ret = ret.replaceAll("<", "&lt;");
 		ret = ret.replaceAll(">", "&gt;");
 		// smile
 		ret = ret
 				.replaceAll(":\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/smile.png\" />");
 		ret = ret
 				.replaceAll(":-\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/smile.png\" />");
 		// laugh
 		ret = ret
 				.replaceAll(":D",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/laugh.png\" />");
 		ret = ret
 				.replaceAll(":-D",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/laugh.png\" />");
 		ret = ret
 				.replaceAll(":d",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/laugh.png\" />");
 		ret = ret
 				.replaceAll(":-d",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/laugh.png\" />");
 		// angel
 		ret = ret
 				.replaceAll("O:-\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/angel.png\" />");
 		ret = ret
 				.replaceAll("O:\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/angel.png\" />");
 		ret = ret
 				.replaceAll("o:-\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/angel.png\" />");
 		ret = ret
 				.replaceAll("o:\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/angel.png\" />");
 		// cool
 		ret = ret
 				.replaceAll("8\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/cool.png\" />");
 		ret = ret
 				.replaceAll("8-\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/cool.png\" />");
 		ret = ret
 				.replaceAll("B\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/cool.png\" />");
 		ret = ret
 				.replaceAll("B-\\)",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/cool.png\" />");
 		// sad
 		ret = ret
 				.replaceAll(":\\(",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/sad.png\" />");
 		ret = ret
 				.replaceAll(":-\\(",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/sad.png\" />");
 		// silly
 		ret = ret
 				.replaceAll(":P",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/silly.png\" />");
 		ret = ret
 				.replaceAll(":p",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/silly.png\" />");
 		ret = ret
 				.replaceAll(":-P",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/silly.png\" />");
 		ret = ret
 				.replaceAll(":-p",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/silly.png\" />");
 
 		// kissy
 		ret = ret
 				.replaceAll(":\\*",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/kissy.png\" />");
 		ret = ret
 				.replaceAll(":-\\*",
 						"<img src=\"/instant-messenger-portlet/pages/emoticons/kissy.png\" />");
 		return ret;
 	}
 
 	public String subString(int lenght, String inputString) {
 		if (inputString.length() > lenght) {
 			StringBuilder sb = new StringBuilder();
 			sb.append(inputString.substring(0, lenght));
 			sb.append("...");
 			return sb.toString();
 		} else
 			return inputString;
 	}
 
 	public void paintAvatar(OutputStream out, Object data) {
 		// if (data instanceof MediaData) {
 
 		// MediaData paintData = (MediaData) data;
 		// System.out.println(data.getClass());
 		if (currentContact.getAvatar() != null) {
 
 			BufferedImage img;
 			try {
 				img = ImageIO.read(new ByteArrayInputStream(currentContact
 						.getAvatar()));
 				Graphics2D graphics2D = img.createGraphics();
 				ImageIO.write(img, "png", out);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		// }
 	}
 
 	// ------------------------------------------------------------------
 	// Implementation of ChatEventsListener
 	// --------- ---------------------------------------------------------
 	@Override
 	public void processMessage(Contact c) {
 	
 		if (this.currentContact != null
 				&& this.currentContact.getIdName().compareTo(c.getIdName()) == 0) {
 			messageWindowRefresh();
		} else
			if(this.currentContact!=null)
 				conversationsWindowRefresh();
		{
			if(!(c instanceof IrcChannel)){
 			this.currentContact=c;
 			c.setHasUnreadedMessage(false);
 			fullConversationWindowsRefersh();
 		}
 		}
 	}
 
 	@Override
 	public void processIncomingChat(Contact c) {
 		// System.out.println("Incoming chat from: ");
 
 		if (this.manager.getConversations().size() <= 1) {
 			this.currentContact = c;
 			fullConversationWindowsRefersh();
 		}
 		else conversationsWindowRefresh();
 	}
 
 	@Override
 	public void processContactStateChanged(Contact c) {
 		this.initContacts();
 		//ifsomebody has loged in
 		if (this.isIrc())
 			ircContactWindowRefresh();
 		else {
 			if (this.manager.getConversations().contains(c))
 				conversationsWindowRefresh();
 			System.out.println("Status changed: " + c.getIdName());
 			
 			contactWindowRefresh();
 		}
 	}
 
 	@Override
 	public void connected(ChatInterface chatInterface) {
 		// TODO Auto-generated method stub
 		this.contactWindowRefresh();
 
 	}
 
 	@Override
 	public void disconnected(ChatInterface chatSession) {
 		//
 		this.contactWindowRefresh();
 		this.messageWindowRefresh();
 
 	}
 
 	public void checkPush() {
 		this.pushWorking = false;
 
 		this.pushTest();
 	}
 
 	// ==================================================================
 	// PUSH methods
 	// ===================================================================
 
 	private void pushTest() {
 		// this.pushWorking=true;
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "pushTest"), "message");
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void messageWindowRefresh() {
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "newmessage"), "sprava");
 
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void contactWindowRefresh() {
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "contact"), null);
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void ircContactWindowRefresh() {
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "ircContacts"), null);
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void conversationsWindowRefresh() {
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "conversations"), "sprava");
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void fullConversationWindowsRefersh(){
 		try {
 			getTopicsContext().publish(
 					new TopicKey(CDI_PUSH_TOPIC, this.getUserIdentifier()
 							+ "messageConversation"), "sprava");
 		} catch (MessageException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 	}
 
 	// ============================================================================
 	// GETTERS and SETTERS
 	// ============================================================================
 
 	public boolean isPushWorking() {
 		return pushWorking;
 	}
 
 	public void setPushWorking(boolean pushWorking) {
 
 		this.pushWorking = pushWorking;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getService() {
 		return service;
 	}
 
 	public void setService(String service) {
 		this.service = service;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public Contact getCurrentContact() {
 		return currentContact;
 	}
 
 	public void setCurrentContact(Contact currentContact) {
 		this.currentContact = currentContact;
 	}
 
 	public String getActualTab() {
 		return actualTab;
 	}
 
 	public List<Contact> getConversations() {
 		return this.manager.getConversations();
 	}
 
 	public List<User> getIrcContacts() {
 		if (this.isIrc()) {
 			return ((IrcChannel) currentContact).getconnectedUsers();
 		}
 		return null;
 	}
 
 	public Integer getPort() {
 		return port;
 	}
 
 	public void setPort(Integer port) {
 		this.port = port;
 	}
 
 	public String getServer() {
 		return server;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	public List<AccountInfo> getStoredAccounts() {
 		if (this.storedAccounts != null)
 			return this.storedAccounts.accounts();
 		else
 			return null;
 	}
 
 	public boolean isSaveAccount() {
 		return saveAccount;
 	}
 
 	public void setSaveAccount(boolean saveAccount) {
 		this.saveAccount = saveAccount;
 	}
 
 	private ChatConfigurations selectedProtocol;
 
 	public ChatConfigurations getSelectedProtocol() {
 		return selectedProtocol;
 	}
 
 	public void setSelectedProtocol(ChatConfigurations selectedProtocol) {
 		this.selectedProtocol = selectedProtocol;
 	}
 
 	public ChatConfigurations[] getProtocols() {
 		return protocols;
 	}
 
 	public String getUserIdentifier() {
 		return userIdentifier;
 	}
 
 }
