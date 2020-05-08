 package com.foreveross.chameleon.push.client;
 
 import static com.foreveross.chameleon.store.core.StaticReference.userMf;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.foreveross.chameleon.CubeConstants;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterGroup;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.AndFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.Registration;
 import org.jivesoftware.smack.provider.PrivacyProvider;
 import org.jivesoftware.smack.provider.ProviderManager;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.GroupChatInvitation;
 import org.jivesoftware.smackx.PrivateDataManager;
 import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.jivesoftware.smackx.packet.ChatStateExtension;
 import org.jivesoftware.smackx.packet.LastActivity;
 import org.jivesoftware.smackx.packet.OfflineMessageInfo;
 import org.jivesoftware.smackx.packet.OfflineMessageRequest;
 import org.jivesoftware.smackx.packet.SharedGroupsInfo;
 import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
 import org.jivesoftware.smackx.provider.DataFormProvider;
 import org.jivesoftware.smackx.provider.DelayInformationProvider;
 import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
 import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
 import org.jivesoftware.smackx.provider.MUCAdminProvider;
 import org.jivesoftware.smackx.provider.MUCOwnerProvider;
 import org.jivesoftware.smackx.provider.MUCUserProvider;
 import org.jivesoftware.smackx.provider.MessageEventProvider;
 import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
 import org.jivesoftware.smackx.provider.RosterExchangeProvider;
 import org.jivesoftware.smackx.provider.StreamInitiationProvider;
 import org.jivesoftware.smackx.provider.VCardProvider;
 import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
 import org.jivesoftware.smackx.search.UserSearch;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Handler;
 import android.util.Log;
 
 import com.foreveross.chameleon.Application;
 import com.foreveross.chameleon.TmpConstants;
 import com.foreveross.chameleon.URL;
 import com.foreveross.chameleon.event.ConnectStatusChangeEvent;
 import com.foreveross.chameleon.event.EventBus;
 import com.foreveross.chameleon.event.ModelChangeEvent;
 import com.foreveross.chameleon.phone.muc.MucInvitationListener;
 import com.foreveross.chameleon.phone.muc.MucManager;
 import com.foreveross.chameleon.push.mina.library.util.NetworkUtil;
 import com.foreveross.chameleon.push.mina.library.util.PropertiesUtil;
 import com.foreveross.chameleon.store.core.StaticReference;
 import com.foreveross.chameleon.store.model.ChatGroupModel;
 import com.foreveross.chameleon.store.model.ConversationMessage;
 import com.foreveross.chameleon.store.model.IMModelManager;
 import com.foreveross.chameleon.store.model.UserModel;
 import com.foreveross.chameleon.util.HttpUtil;
 import com.foreveross.chameleon.util.Pool;
 import com.foreveross.chameleon.util.Preferences;
 import com.foreveross.chameleon.util.PushUtil;
 import com.squareup.otto.ThreadEnforcer;
 
 /**
  * [xmpp连接管理]<BR>
  * [功能详细描述]
  * 
  * @author 冯伟立
  * @version [CubeAndroid, 2013-7-15]
  */
 /**
  * [一句话功能简述]<BR>
  * [功能详细描述]
  * 
  * @author 冯伟立
  * @version [CubeAndroid, 2013-7-18]
  */
 public class XmppManager {
 
 	private final static Logger log = LoggerFactory
 			.getLogger(XmppManager.class);
 
 	/**
 	 * [客户端资源名称]
 	 */
 	private static final String XMPP_RESOURCE_NAME = "Cube_Client";
 
 	/**
 	 * [配置储存]
 	 */
 	private SharedPreferences sharedPrefs;
 
 	/**
 	 * [唯一连接]
 	 */
 	public XMPPConnection connection;
 
 	/**
 	 * [主机地址]
 	 */
 	private String xmppHost;
 
 	/**
 	 * [主机端口]
 	 */
 	private int xmppPort;
 
 	/**
 	 * [用户名]
 	 */
 	private String usernameStore;
 
 	/**
 	 * [密码]
 	 */
 	private String passwordStore;
 
 	/**
 	 * [连接状态监听]
 	 */
 	private ConnectionListener connectionListener;
 
 	/**
 	 * [通知监听]
 	 */
 	private PacketListener notificationPacketListener;
 
 	
 	/**
 	 * [xmppManager类型，推送/聊天]
 	 */
 	private Type type;
 	
 	/**
 	 * [Presence状态监听]
 	 */
 
 	/**
 	 * [监听Roster CRUD]
 	 */
 	private RosterListener rosterListener;
 
 	/**
 	 * [重连线程]
 	 */
 	private ReconnectionThread reconnection;
 
 	private static String meJid;
 
 	/**
 	 * 通知服务
 	 */
 	private NotificationService notificationService;
 
 	public NotificationService getNotificationService() {
 		return notificationService;
 	}
 
 	public void setNotificationService(NotificationService notificationService) {
 		this.notificationService = notificationService;
 	}
 
 	/**
 	 * 线程池
 	 */
 	private ExecutorService pool;
 
 	public XmppManager(NotificationService notificationService, int resourceId,Type type) {
 		this.notificationService = notificationService;
 		this.sharedPrefs = notificationService.getSharedPreferences(
 				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
 		PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(notificationService, resourceId);
 		this.setType(type);
 		
 		if(type==Type.CHAT){
 			this.usernameStore = sharedPrefs.getString(Constants.XMPP_USERNAME,null);
 			this.passwordStore = sharedPrefs.getString(Constants.XMPP_PASSWORD,null);
 			notificationPacketListener = new ChatMessageListener(this);
 			this.xmppHost = propertiesUtil.getString("chatHost", "127.0.0.1");
 			this.xmppPort = propertiesUtil.getInteger("chatPort", 5222);
 			rosterListener = new MyRosterListener(notificationService);
 		}else if(type==Type.PUSH){
 			String token = PushUtil.createMD5Token(Application.class.cast(this.getNotificationService()
                     .getApplicationContext()));
 			this.usernameStore =  token;
 			this.passwordStore =  token;
 			notificationPacketListener = new PushMessageListener(this.getNotificationService().getApplicationContext(),this);
 			this.xmppHost = propertiesUtil.getString("pushHost", "127.0.0.1");
 			this.xmppPort = propertiesUtil.getInteger("pushPort", 5222);
 		}
 		
 		connectionListener = new PersistentConnectionListener(this);
 		reconnection = new ReconnectionThread(this);
 		pool = Executors.newCachedThreadPool();
 		addProviders();
 	}
 
 	/**
 	 * [添加providers注册]<BR>
 	 * [功能详细描述] 2013-7-18 下午8:12:26
 	 */
 	public void addProviders() {
 		ProviderManager pm = ProviderManager.getInstance();
 		// Private Data Storage
 		pm.addIQProvider("query", "jabber:iq:private",
 				new PrivateDataManager.PrivateDataIQProvider());
 		// Time
 		try {
 			pm.addIQProvider("query", "jabber:iq:time",
 					Class.forName("org.jivesoftware.smackx.packet.Time"));
 		} catch (ClassNotFoundException e) {
 			Log.w("TestClient",
 					"Can't load class for org.jivesoftware.smackx.packet.Time");
 		}
 		// Roster Exchange
 		pm.addExtensionProvider("x", "jabber:x:roster",
 				new RosterExchangeProvider());
 		// Message Events
 		pm.addExtensionProvider("x", "jabber:x:event",
 				new MessageEventProvider());
 		// Chat State
 		pm.addExtensionProvider("active",
 				"http://jabber.org/protocol/chatstates",
 				new ChatStateExtension.Provider());
 		pm.addExtensionProvider("composing",
 				"http://jabber.org/protocol/chatstates",
 				new ChatStateExtension.Provider());
 		pm.addExtensionProvider("paused",
 				"http://jabber.org/protocol/chatstates",
 				new ChatStateExtension.Provider());
 		pm.addExtensionProvider("inactive",
 				"http://jabber.org/protocol/chatstates",
 				new ChatStateExtension.Provider());
 		pm.addExtensionProvider("gone",
 				"http://jabber.org/protocol/chatstates",
 				new ChatStateExtension.Provider());
 		// XHTML
 		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
 				new XHTMLExtensionProvider());
 		// Group Chat Invitations
 		pm.addExtensionProvider("x", "jabber:x:conference",
 				new GroupChatInvitation.Provider());
 		// Service Discovery # Items
 		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
 				new DiscoverItemsProvider());
 		// Service Discovery # Info
 		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
 				new DiscoverInfoProvider());
 		// Data Forms
 		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
 		// MUC User
 		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
 				new MUCUserProvider());
 		// MUC Admin
 		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
 				new MUCAdminProvider());
 		// MUC Owner
 		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
 				new MUCOwnerProvider());
 		// Delayed Delivery
 		pm.addExtensionProvider("x", "jabber:x:delay",
 				new DelayInformationProvider());
 		// Version
 		try {
 			pm.addIQProvider("query", "jabber:iq:version",
 					Class.forName("org.jivesoftware.smackx.packet.Version"));
 		} catch (ClassNotFoundException e) {
 			// Not sure what's happening here.
 		}
 		// VCard
 		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
 		// Offline Message Requests
 		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
 				new OfflineMessageRequest.Provider());
 		// Offline Message Indicator
 		pm.addExtensionProvider("offline",
 				"http://jabber.org/protocol/offline",
 				new OfflineMessageInfo.Provider());
 		// Last Activity
 		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
 		// User Search
 		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
 		// SharedGroupsInfo
 		pm.addIQProvider("sharedgroup",
 				"http://www.jivesoftware.org/protocol/sharedgroup",
 				new SharedGroupsInfo.Provider());
 		// JEP-33: Extended Stanza Addressing
 		pm.addExtensionProvider("addresses",
 				"http://jabber.org/protocol/address",
 				new MultipleAddressesProvider());
 		// FileTransfer
 		pm.addIQProvider("si", "http://jabber.org/protocol/si",
 				new StreamInitiationProvider());
 		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
 				new BytestreamsProvider());
 		// Privacy
 		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());
 		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider());
 		pm.addExtensionProvider("malformed-action",
 				"http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider.MalformedActionError());
 		pm.addExtensionProvider("bad-locale",
 				"http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider.BadLocaleError());
 		pm.addExtensionProvider("bad-payload",
 				"http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider.BadPayloadError());
 		pm.addExtensionProvider("bad-sessionid",
 				"http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider.BadSessionIDError());
 		pm.addExtensionProvider("session-expired",
 				"http://jabber.org/protocol/commands",
 				new AdHocCommandDataProvider.SessionExpiredError());
 
 	}
 
 	/**
 	 * [连接并登陆]<BR>
 	 * 1.首次登陆 2.非首次登陆 　　2.1.同一用户登陆 2.2.非同一用户登陆
 	 * 
 	 * @param useranme
 	 * @param password
 	 * @return 2013-7-18 上午10:59:04
 	 */
 
 	class Entry {
 
 		public Entry(String name, String password) {
 			super();
 			this.name = name;
 			this.password = password;
 		}
 
 		private String name;
 		private String password;
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getPassword() {
 			return password;
 		}
 
 		public void setPassword(String password) {
 			this.password = password;
 		}
 	}
 
 	/**
 	 * [请求队列]
 	 */
 	private BlockingQueue<Entry> reqConnectQueue = new ArrayBlockingQueue<Entry>(
 			10);
 
 	/**
 	 * [准备连接]<BR>
 	 * 1.每个连接请求排队，应对并发<BR>
 	 * 2013-8-6 下午4:18:28
 	 */
 	public void prepairConnect() {
 		log.debug("xmpp manager prepairConnect...");
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (true) {
 					try {
 						Entry entry = reqConnectQueue.take();
 						log.debug(
 								"take a login entry req for username:{} password:{} and connecting...",
 								entry.getName(), entry.getPassword());
 						if (notificationService != null
 								&& !NetworkUtil
 										.isNetworkConnected(notificationService)) {
 							EventBus.getEventBus(TmpConstants.COMMNAD_INTENT)
 							.post(ConnectStatusChangeEvent.SHOW_TOAST);
 							continue;
 						}
 						connect(entry.getName(), entry.getPassword());
 					} catch (InterruptedException e) {
 						log.error("connect xmpp server error!", e);
 					} catch (Exception e) {
 						log.error("connect xmpp server error!", e);
 					}
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * [请求连接]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param username
 	 * @param password
 	 *            2013-8-6 下午4:20:07
 	 */
 	public void submitConnectReq(String username, String password) {
 		log.debug("submitConnectReq for username:{},password:{}", username,
 				password);
        try {
            reqConnectQueue.add(new Entry(username, password));
        }
        catch (Exception e)
        {
            reqConnectQueue.clear();
            reqConnectQueue.add(new Entry(username, password));
        }
 	}
 
 	/**
 	 * [请求重连接]<BR>
 	 * [功能详细描述] 2013-8-6 下午4:20:30
 	 */
 	public void submitReconnectReq() {
 		log.debug("submitReconnectReq for username:{},password:{}",
 				usernameStore, passwordStore);
        try {
            reqConnectQueue.add(new Entry(usernameStore, passwordStore));
        }catch (Exception e)
        {
            reqConnectQueue.clear();
            reqConnectQueue.add(new Entry(usernameStore, passwordStore));
        }

 	}
 
 	/**
 	 * [连接并登陆xmpp服务器]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param username
 	 * @param password
 	 * @return 2013-8-6 下午4:20:55
 	 */
 	private synchronized boolean connect(String username, String password) {
 		boolean result = false;
 		if (connection != null) {
 			boolean isAuth = connection.isAuthenticated();
 			boolean isUserStore = usernameStore.equals(username);
 			if (isUserStore && isAuth) {
 				log.debug("the same user {} has logined,ignored conect...",
 						username);
 				reqConnectQueue.clear();
 				return true;
 			} else {
 				log.debug("xmpp connection has connect,but not authenticated ,close it!");
 				connection.disconnect();
 				connection = null;
 			}
 		}
 		if (usernameStore == null) {
 			usernameStore = username;
 			passwordStore = password;
 		}
 		
 		try {
 			log.debug(
 					"create a xmpp connection... for username:{} password:{}",
 					username, password);
 			XMPPConnection xmppConnection = pool.submit(new ConnectTask(null))
 					.get();
 			log.debug("authenticated the xmpp connection created by previous!");
 			this.connection = xmppConnection;
 			result = connect(xmppConnection, username, password);
 		} catch (InterruptedException e) {
 			log.error("connect() InterruptedException", e);
 			result = false;
 		} catch (ExecutionException e) {
 			log.error("connect() ExecutionException", e);
 			result = false;
 		} catch (Exception e) {
 			log.error("connect() ExecutionException", e);
 			result = false;
 		}
 		
 		if (result) {
 			
 			if(getType()==Type.CHAT){
 				// 设置当前的usermodel
 				meJid = username + "@" + this.getConnection().getServiceName();
 				sendBroadcastWithStatus(ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 						ConnectStatusChangeEvent.CONN_STATUS_ONLINE);
 			}else{
 				sendBroadcastWithStatus(ConnectStatusChangeEvent.CONN_CHANNEL_OPENFIRE,
 						ConnectStatusChangeEvent.CONN_STATUS_ONLINE);
 			}
 		} else {
 			
 			if(getType()==Type.CHAT){
 				sendBroadcastWithStatus(ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 						ConnectStatusChangeEvent.CONN_STATUS_OFFLINE);
 			}else{
 				sendBroadcastWithStatus(ConnectStatusChangeEvent.CONN_CHANNEL_OPENFIRE,
 						ConnectStatusChangeEvent.CONN_STATUS_OFFLINE);
 			}
 			
 		}
 		return result;
 	}
 
 	/**
 	 * [登陆连接]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param xmppConnection
 	 * @param username
 	 * @param password
 	 * @return 2013-8-6 下午4:22:28
 	 */
 	private synchronized boolean connect(XMPPConnection xmppConnection, String username,
 			String password) {
 		log.debug(" {} is authenticating...", username);
 		try {
 			String errorMsg = pool.submit(
 					new LoginTask(xmppConnection, this, username, password))
 					.get();
 			log.debug(" {}'s athentication result is {}", username, errorMsg);
 			String INVALID_CREDENTIALS_ERROR_CODE = "401";
 			if (errorMsg != null && "".equals(errorMsg)) {
 				// EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 				// new XmppConnectEvent(true));
 				log.debug("authentication is success for username:{},retrun..",
 						username);
 				reqConnectQueue.clear();
 				return true;
 			} else if (errorMsg != null
 					&& errorMsg.contains(INVALID_CREDENTIALS_ERROR_CODE)) {
 				log.info(
 						"uesrname {} has not been registered,begin register ....",
 						username);
 				boolean registerSuccess = pool.submit(
 						new RegisterTask(xmppConnection, username, password))
 						.get();
 
 				if (registerSuccess) {
 					Log.i("username:{} register successfully,retrying connect...",
 							username);
 					connect(username, password);
 				} else {
 					log.error("username:{} register faill....", username);
 				}
 			}
 		} catch (Exception e) {
 			log.error("connect xmpp server execption....", e);
 
 		}
 		if (xmppConnection != null && xmppConnection.isConnected()) {
 			log.debug("disconnect connection...");
 			xmppConnection.disconnect();
 		}
 
 		return false;
 	}
 
 	/**
 	 * [请求重连接]<BR>
 	 * [功能详细描述] 2013-8-6 下午4:23:29
 	 */
 	public void reconnect() {
 		if (usernameStore == null) {
 			log.info("reconnect no username....,return");
 		}
 		log.info("submit a reconnect req!");
 		submitConnectReq(usernameStore, passwordStore);
 	}
 
 	public boolean disconnect() {
 		log.debug("disconnect()...");
 		try {
 			pool.submit(new LogoutTask()).get();
 			log.debug("disconnect success!");
 			return true;
 		} catch (Exception e) {
 			log.error("disconnect error!", e);
 			return false;
 		}
 	}
 
 	public void setConnection(XMPPConnection connection) {
 		this.connection = connection;
 	}
 
 	public XMPPConnection getConnection() {
 		return connection;
 	}
 
 	public void registerLogin(String username, String password) {
 		Editor editor = sharedPrefs.edit();
 		editor.putString(Constants.XMPP_USERNAME, username);
 		editor.putString(Constants.XMPP_PASSWORD, password);
 		editor.commit();
 	}
 
 	public void unregisterLogin() {
 		Editor editor = sharedPrefs.edit();
 		editor.remove(Constants.XMPP_USERNAME);
 		editor.remove(Constants.XMPP_PASSWORD);
 		editor.commit();
 	}
 
 	public void sendPacket(Packet packet) {
 		if (connection != null) {
 			connection.sendPacket(packet);
 		}
 	}
 
 	public RosterListener getRosterListener() {
 		return rosterListener;
 	}
 
 	public void setRosterListener(RosterListener rosterListener) {
 		this.rosterListener = rosterListener;
 	}
 
 	public String getUsernameStore() {
 		return usernameStore;
 	}
 
 	public void setUsernameStore(String username) {
 		this.usernameStore = username;
 	}
 
 	public String getPasswordStore() {
 		return passwordStore;
 	}
 
 	public void setPasswordStore(String password) {
 		this.passwordStore = password;
 	}
 
 	public ConnectionListener getConnectionListener() {
 		return connectionListener;
 	}
 
 	public PacketListener getNotificationPacketListener() {
 		return notificationPacketListener;
 	}
 	
 	public void stopReconnectionThread(){
 		log.info("xmpp manager stopReconnectionThread... ");
 		synchronized (reconnection) {
 			
 			if(reconnection!=null){
 				reconnection.stopReconnect();
 			}
 		}
 	}
 	
 	public void startReconnectionThread() {
 		log.info("xmpp manager startReconnectionThread... ");
 		synchronized (reconnection) {
 //			if (!reconnection.isAlive()) {
 //				reconnection.setName("Xmpp Reconnection Thread");
 //				reconnection = new ReconnectionThread(this);
 //				reconnection.start();
 //			}
 			if (!reconnection.isThreadAlive()) {
 				reconnection.setName("Xmpp Reconnection Thread");
 				reconnection = new ReconnectionThread(this);
 				reconnection.start();
 			}
 		}
 	}
 
 	/******************************************************************************************************
 	 * 
 	 * lognin.....
 	 * 
 	 *****************************************************************************************************/
 
 	/**
 	 * [是否已经连接]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-7-15 下午7:28:10
 	 */
 	public boolean isConnected() {
 		return connection != null && connection.isConnected();
 	}
 
 	/**
 	 * [是否已经验证]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-7-15 下午7:28:16
 	 */
 	public boolean isAuthenticated() {
 		return connection != null && connection.isConnected()
 				&& connection.isAuthenticated();
 	}
 
 	/**
 	 * 
 	 * [是否已经登录过]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @return 2013-7-15 下午7:28:51
 	 */
 	public boolean isRegistered() {
 		return sharedPrefs.contains(Constants.XMPP_USERNAME)
 				&& sharedPrefs.contains(Constants.XMPP_PASSWORD);
 	}
 
 	/**
 	 * [注册任务]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @author 冯伟立
 	 * @version [CubeAndroid, 2013-7-18]
 	 */
 	private class RegisterTask implements Callable<Boolean> {
 
 		private String username;
 		private String password;
 
 		private RegisterTask(XMPPConnection xmppConnection, String username,
 				String password) {
 			this.username = username;
 			this.password = password;
 		}
 
 		public Boolean call() {
 			log.info(username + " RegisterTask.run()...");
 			Registration registration = new Registration();
 			registration.setType(IQ.Type.SET);
 			registration.addAttribute("username", username);
 			registration.addAttribute("password", password);
 
 			PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
 					registration.getPacketID()), new PacketTypeFilter(IQ.class));
 			PacketCollector packetCollector = connection
 					.createPacketCollector(packetFilter);
 			try {
 				connection.sendPacket(registration);
 	
 				Packet packet = packetCollector.nextResult();
 				Log.d("RegisterTask.PacketListener", "processPacket().....");
 				IQ response = (IQ) packet;
 				if (response.getType() == IQ.Type.ERROR) {
 					if (response.getError().toString().contains("409")) {
 						log.error("Unknown error while registering XMPP account! "
 								+ response.getError().getCondition());
 						log.info("register failed");
 					}
 					return false;
 				} else if (response.getType() == IQ.Type.RESULT) {
 					log.info("register success");
 					return true;
 				}
 			} catch (Exception e) {
 				log.info("register failed");
 				e.printStackTrace();
 				return false;
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * [连接任务]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @author 冯伟立
 	 * @version [CubeAndroid, 2013-7-15]
 	 */
 	private class ConnectTask implements Callable<XMPPConnection> {
 
 		private XMPPConnection xmppConnection;
 
 		private ConnectTask(XMPPConnection xmppConnection) {
 			this.xmppConnection = xmppConnection;
 		}
 
 		public XMPPConnection call() {
 			if (xmppConnection == null) {
 				ConnectionConfiguration connConfig = new ConnectionConfiguration(
 						xmppHost, xmppPort);
 				connConfig.setSASLAuthenticationEnabled(false);
 				connConfig.setSecurityMode(SecurityMode.disabled);
 				connConfig.setRosterLoadedAtLogin(true);
 				xmppConnection = new XMPPConnection(connConfig);
 			}
 			log.info(" ConnectTask.run()...");
 
 			if (!xmppConnection.isConnected()) {
 				try {
 
 					xmppConnection.connect();
 					log.info(" XMPP connected successfully");
 					return xmppConnection;
 				} catch (XMPPException e) {
 					log.error(" XMPP connection failed", e);
 				}
 			} else {
 				log.info(" xmppConnection already connected");
 			}
 
 			return xmppConnection;
 		}
 	}
 
 	/**
 	 * [登录任务]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @author 冯伟立
 	 * @version [CubeAndroid, 2013-7-15]
 	 */
 	private class LoginTask implements Callable<String> {
 
 		final XmppManager xmppManager;
 		final XMPPConnection xmppConnection;
 		private String username;
 		private String password;
 
 		public LoginTask(XMPPConnection xmppConnection,
 				XmppManager xmppManager, String username, String password) {
 			this.xmppConnection = xmppConnection;
 			this.xmppManager = xmppManager;
 			this.username = username;
 			this.password = password;
 		}
 
 		public String call() {
 			log.info(username + " LoginTask.run()...");
 			if (!xmppConnection.isAuthenticated()) {
 				try {
 					xmppConnection.login(username, password, XMPP_RESOURCE_NAME);
 					xmppManager.setConnection(xmppConnection);
 					// 记住账号密码
 					xmppManager.setUsernameStore(username);
 					xmppManager.setPasswordStore(password);
 					if(type==Type.CHAT){
 						MucManager mucManager = MucManager
 								.getInstanse(notificationService
 										.getApplicationContext());
 						mucManager.init(xmppConnection);
                         String servicenName = xmppConnection.getServiceName();
                         if(null == servicenName || "".equals(servicenName))
                         {
                             PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(notificationService
                                     .getApplicationContext(),
                                 CubeConstants.CUBE_CONFIG);
                             servicenName = propertiesUtil.getString("ChatServiceName","istation.csair.com");
                         }
 
 						String hostName =  servicenName;
 						Monitor monitor = new Monitor();
 						mucManager.obtainRooms(username + "@" + hostName, monitor);
 						sync(xmppConnection, monitor);
 						log.debug(username + " Loggedn in successfully");
 						// 记录成功登陆的连接
 						collectedFriend();
 						registerLogin(username, password);
 						MucInvitationListener mucInvitationListener = new MucInvitationListener(
 								notificationService.getApplicationContext(),
 								xmppManager);
 						MultiUserChat.addInvitationListener(xmppConnection,
 								mucInvitationListener);
 						sendBroadcastWithStatus(
 								ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 								ConnectStatusChangeEvent.CONN_STATUS_ONLINE);
 					}else if(type == Type.PUSH){
 						Application.class.cast(notificationService.getApplicationContext())
 							.getUIHandler().sendEmptyMessage(0);
 						sendBroadcastWithStatus(
 								ConnectStatusChangeEvent.CONN_CHANNEL_OPENFIRE,
 								ConnectStatusChangeEvent.CONN_STATUS_ONLINE);
 					}
 					// 注册监听器
 					registerListeners();
 					// 发消息通知登录成功
 					
 
 					return "";
 
 				} catch (XMPPException e) {
 					log.error(username
 							+ " Failed to login to xmpp server. Caused by: "
 							+ e.getMessage());
 
 					return e.getMessage();
 
 				} catch (Exception e) {
 					log.error(username
 							+ " Failed to login to xmpp server. Caused by: "
 							+ e.getMessage());
 					return e.getMessage();
 				}
 
 			} else {
 				log.info(username + " Already athenticated...");
 				return "";
 			}
 
 		}
 
 		public void registerListeners() {
 
 			if(type==Type.CHAT){
 				RosterListener rosterListener = xmppManager.getRosterListener();
 				xmppConnection.getRoster().addRosterListener(rosterListener);
 			}
 			
 			PacketFilter messageFilter = new PacketTypeFilter(Message.class);
 			PacketListener packetListener = xmppManager.getNotificationPacketListener();
 			xmppConnection.addPacketListener(packetListener, messageFilter);
 			xmppConnection.addConnectionListener(xmppManager.getConnectionListener());
 		}
 
 		public void syncMe(XMPPConnection xmppConnection) {
 			UserModel me = IMModelManager.instance().getMe();
 			if (me == null) {
 				IMModelManager.instance().setMe(me = new UserModel());
 			}
 			me.setJid(StringUtils.parseBareAddress(xmppConnection.getUser()));
 			String name = null;
 			try {
 				name = xmppConnection.getAccountManager().getAccountAttribute(
 						"name");
 				me.setName(name == null ? StringUtils.parseName(xmppConnection
 						.getUser()) : name);
 			} catch (Exception e) {
 				e.printStackTrace();
 				name = Preferences.getUserName(Application.sharePref);
 				me.setName(name);
 			}
 
 			List<ChatGroupModel> chatGroupList = IMModelManager.instance()
 					.getChatGroupModelsByJid(me.getJid());
 			if (chatGroupList == null || chatGroupList.size() == 0) {
 				return;
 			}
 			for (ChatGroupModel chatGroupModel : chatGroupList) {
 				ChatGroupModel chatGroupModelx = chatGroupModel;
 				if (!IMModelManager.instance().containGroup(
 						chatGroupModel.getGroupCode())) {
 					IMModelManager.instance().addUserGroupModel(chatGroupModel);
 				} else {
 					chatGroupModelx = (ChatGroupModel) IMModelManager
 							.instance().getUserGroupModel(
 									chatGroupModelx.getGroupCode());
 				}
 				me.addGroup(chatGroupModelx);
 			}
 
 			if (!IMModelManager.instance().containUserModel(me.getJid())) {
 				me.transGroups();
 				StaticReference.userMf.createOrUpdate(me);
 				IMModelManager.instance().addUserModel(me);
 			} else {
 
 			}
 
 		}
 
 		public void sync(final XMPPConnection xmppConnection,
 				final Monitor monitor) {
 
 			Pool.getPool().execute(new Runnable() {
 				@Override
 				public void run() {
 					synchronized (monitor) {
 						try {
 							while (!monitor.isNotified()) {
 								monitor.wait();
 							}
 							monitor.setNotified(false);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					syncMe(xmppConnection);
 /*					if (StaticReference.userMf == null) {
 						StaticReference.userMC = ModelCreator.build(xmppManager
 								.getNotificationService().getApplication(),
 								username);
 						StaticReference.userMf = ModelFinder.build(xmppManager
 								.getNotificationService().getApplication(),
 								username);
 					}*/
 					Collection<RosterEntry> c = xmppConnection.getRoster()
 							.getEntries();
 					if (c.isEmpty()) {
 						return;
 					}
 
 					for (RosterEntry rosterEntry : c) {
 						String jid = StringUtils.parseBareAddress(rosterEntry
 								.getUser());
 						if (!IMModelManager.instance().containUserModel(jid)) {
 							UserModel userModel = new UserModel();
 							userModel.sync(new RosterEntryWrapper(rosterEntry));
 							IMModelManager.instance().addUserModel(userModel);
 							StaticReference.userMf.createOrUpdate(userModel);
 						} else {
 							UserModel userModel = IMModelManager.instance()
 									.getUserModel(jid);
 							if (userModel != null){
 								userModel.sync(new RosterEntryWrapper(rosterEntry));
 								// 恢复所有组
 								userModel.transGroups();
 								StaticReference.userMf.createOrUpdate(userModel);
 							}
 						}
 					}
 					// TODO[FENGWIELI] 同步其它类型用户组
 					EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).post(
 							new ModelChangeEvent());
 
 				}
 			});
 		}
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述] //TODO[fengweili]不确定此在线方式判断是否正确
 	 * 
 	 * @return 2013-7-19 上午11:37:26
 	 */
 	public boolean isOnline() {
 		return connection != null && connection.isAuthenticated();
 	}
 
 	public boolean disconnectNow() {
 		boolean result = false;
 		if (isConnected()) {
 			reqConnectQueue.clear();
 			log.debug(usernameStore + " LogoutTask.run()");
 			/**
 			 * 移除
 			 */
 			connection.removePacketListener(notificationPacketListener);
 			connection.getRoster().removeRosterListener(rosterListener);
 			try {
 				connection.disconnect();
 				connection = null;
 				result = true;
 			} catch (Exception e) {
 				log.error("close xmpp connection error!", e);
 				result = false;
 			}
 		} else {
 			log.info(usernameStore + " Already logout...");
 			result = true;
 		}
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * [退出任务]<BR>
 	 * [功能详细描述] 2013-7-15 下午7:31:58
 	 */
 	private class LogoutTask implements Callable<Boolean> {
 
 		final XmppManager xmppManager = XmppManager.this;
 
 		public Boolean call() {
 			boolean result = false;
 			if (xmppManager.isConnected()) {
 				reqConnectQueue.clear();
 				log.debug(usernameStore + " LogoutTask.run()");
 				/**
 				 * 移除
 				 */
 
 				connection.removePacketListener(notificationPacketListener);
 
 				connection.getRoster().removeRosterListener(rosterListener);
 				
 				try {
 					connection.disconnect();
 					connection = null;
 					result = true;
 					if(getType()==Type.CHAT){
 						
 						sendBroadcastWithStatus(
 								ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 								ConnectStatusChangeEvent.CONN_STATUS_OFFLINE);
 					}else{
 						sendBroadcastWithStatus(
 								ConnectStatusChangeEvent.CONN_CHANNEL_OPENFIRE,
 								ConnectStatusChangeEvent.CONN_STATUS_OFFLINE);
 					}
 					
 				} catch (Exception e) {
 					log.error("close xmpp connection error!", e);
 					result = false;
 				}
 			} else {
 				log.info(usernameStore + " Already logout...");
 				result = true;
 			}
 
 			return result;
 		}
 	}
 
 	private class OfflineTask implements Runnable {
 
 		/**
 		 * [一句话功能简述]<BR>
 		 * [功能详细描述] 2013-7-15 下午7:41:11
 		 */
 		@Override
 		public void run() {
 			if (connection != null && connection.isAuthenticated()
 					&& connection.isConnected()) {
 				Roster roster = connection.getRoster();
 				Collection<RosterEntry> entries = roster.getEntries();
 				Presence presence = null;
 				for (RosterEntry entry : entries) {
 					presence = new Presence(Presence.Type.unavailable);
 					presence.setFrom(usernameStore + "@"
 							+ connection.getServiceName());
 
 					presence.setTo(entry.getUser());
 					connection.sendPacket(presence);
 				}
 				connection.sendPacket(presence);
 				sendBroadcastWithStatus(
 						ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 						ConnectStatusChangeEvent.CONN_STATUS_OFFLINE);
 			}
 		}
 	}
 
 	private class OnlineTask implements Runnable {
 		public void run() {
 			if (connection != null && connection.isAuthenticated()
 					&& connection.isConnected()) {
 				Presence presence = new Presence(Presence.Type.available, "在线",
 						1, null);
 				connection.sendPacket(presence);
 				sendBroadcastWithStatus(
 						ConnectStatusChangeEvent.CONN_CHANNEL_CHAT,
 						ConnectStatusChangeEvent.CONN_STATUS_ONLINE);
 			}
 		}
 	}
 
 	public void offline() {
 		pool.submit(new OfflineTask());
 	}
 
 	public void online() {
 		pool.submit(new OnlineTask());
 	}
 
 	private void sendBroadcastWithStatus(String channel, String status) {
 		EventBus.getEventBus(TmpConstants.EVENTBUS_PUSH, ThreadEnforcer.MAIN)
 				.post(new ConnectStatusChangeEvent(channel, status));
 	}
 
 	public String getXmppServiceName() {
 
 		return connection.getServiceName();
 	}
 
 	/**
 	 * [花名册操作类]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @author 冯伟立
 	 * @version [CubeAndroid, 2013-7-18]
 	 */
 	public class RosterManager {
 
 		public RosterEntry getRosterEntry(String jid) {
 			return connection.getRoster().getEntry(jid);
 		}
 
 		private RosterManager rosterManager;
 
 		public RosterManager getRosterManager() {
 			return rosterManager;
 		}
 
 		public void setRosterManager(RosterManager rosterManager) {
 			this.rosterManager = rosterManager;
 		}
 
 		private Handler handler;
 
 		public RosterManager(Handler handler) {
 			this.handler = handler;
 		}
 
 		private Map<String, List<UserModel>> userListMap = new HashMap<String, List<UserModel>>();
 		private Map<String, String> sexJidMap = new HashMap<String, String>();
 
 		/**
 		 * 1.先从本地DB取出缓存对象,直接返回 2.同步服务器，把最新内容保存在本地
 		 * 2.1同时用最新的数据改变list里面的数据,这里包括状态改变，增加、删除对象等 在这里要注意对象的唯一性 3.发送广播
 		 * 4.接收广播后，notifyDataSetChanged();
 		 * 
 		 * @return
 		 * @throws XMPPException
 		 */
 		// class SortByName implements Comparator<Object> {
 		// public int compare(Object o1, Object o2) {
 		// Collator cmp = Collator.getInstance(Locale.CHINA);
 		// AbstractGroupModel g1 = (AbstractGroupModel) o1;
 		// AbstractGroupModel g2 = (AbstractGroupModel) o2;
 		// // g1.getGroupName().compareTo(g2.getGroupName());
 		// return cmp.compare(g1.getGroupName(), g2.getGroupName());
 		// }
 		// }
 		//
 		// @SuppressWarnings("unchecked")
 		// public List<AbstractGroupModel> queryAllGroup() {
 		// Properties airportProps = new Properties();
 		// try {
 		// airportProps.load(notificationService.getResources()
 		// .openRawResource(R.raw.airports));
 		// } catch (NotFoundException e1) {
 		// e1.printStackTrace();
 		// } catch (IOException e1) {
 		// e1.printStackTrace();
 		// }
 		// List<AbstractGroupModel> groups = new
 		// ArrayList<AbstractGroupModel>();
 		// List<String> shareGroupNamelist = new ArrayList<String>();
 		// try {
 		// shareGroupNamelist = SharedGroupManager
 		// .getSharedGroups(getConnection());
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// // 第一次查询不成功，查询第二次
 		// try {
 		// Thread.sleep(100);
 		// shareGroupNamelist = SharedGroupManager
 		// .getSharedGroups(getConnection());
 		// } catch (Exception e2) {
 		// e2.printStackTrace();
 		// // 第二次查询也不成功
 		// return null;
 		// }
 		// }
 		// Roster roster = getConnection().getRoster();
 		// Collection<RosterGroup> entriesGroup = roster.getGroups();
 		// for (RosterGroup group : entriesGroup) {
 		// try {
 		// synchronized (groups) {
 		// if (shareGroupNamelist.contains(group.getName())) {
 		// groups.add(getGroup(group, SharedGroupModel.class,
 		// airportProps));
 		// } else {
 		// groups.add(getGroup(group, CommonGroupModel.class,
 		// airportProps));
 		// }
 		// }
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// }
 		// new AsyncTask<List<AbstractGroupModel>, Integer, String>() {
 		// @Override
 		// protected String doInBackground(
 		// List<AbstractGroupModel>... arg0) {
 		// List<AbstractGroupModel> groups = arg0[0];
 		// for (AbstractGroupModel group : groups) {
 		// for (int i = 0; i < group.getList().size(); i++) {
 		// UserModel user = (UserModel) group.getList().get(i);
 		// VCard vcard = new VCard();
 		// try {
 		// vcard.load(getConnection(), user.getJid());
 		// String sex = (vcard.getMiddleName() != null) ? vcard
 		// .getMiddleName() : "unknow";
 		// getSexJidMap().put(user.getJid(), sex);
 		// } catch (XMPPException e) {
 		// getSexJidMap().put(user.getJid(), "unknow");
 		// }
 		// }
 		// }
 		// return null;
 		// }
 		//
 		// }.execute(groups);
 		//
 		// return groups;
 		// }
 		//
 		// private AbstractGroupModel getGroup(RosterGroup group,
 		// Class<? extends AbstractGroupModel> groupClazz,
 		// Properties airportProps) throws Exception {
 		// AbstractGroupModel groupModel = StaticReference.userMC
 		// .createModel(groupClazz);
 		// String groupCode = group.getName();
 		// // String airport = airportProps.getProperty("airport." +
 		// // group.getName(), group.getName());
 		// // String groupName = airport;
 		// groupModel.setGroupCode(groupCode);
 		// groupModel.setGroupName(groupCode);
 		// // groupModel.setGroupName(groupName);
 		// Collection<RosterEntry> entries = group.getEntries();
 		// List<UserModel> userList = new ArrayList<UserModel>();
 		// String currentAccount = Preferences
 		// .getUserName(Application.sharePref)
 		// + "@"
 		// + getConnection().getServiceName();
 		// for (RosterEntry entry : entries) {
 		// Log.i("--*******", "name: " + entry.getName());
 		// String jid = entry.getUser();
 		// String name = entry.getName();
 		// if (jid.equals(currentAccount))
 		// continue;
 		// RosterPacket.ItemType type = entry.getType();
 		// UserModel userModel = StaticReference.userMC
 		// .createModel(UserModel.class);
 		// userModel.setJid(jid);
 		// userModel.setName(name);
 		// if (group.getName() == null || group.getName().equals("")) {
 		// userModel.setGroupName("未分组");
 		// userModel.setAirportCode("未分组");
 		// } else {
 		// userModel.setGroupName(group.getName());
 		// userModel.setAirportCode(group.getName());
 		// }
 		// userModel.setType(type);
 		// userModel.setIsRead(0);
 		// if (PresencePacketListener.statusMap.containsKey(jid)) {
 		// userModel.setStatus(PresencePacketListener.statusMap
 		// .get(jid));
 		// }
 		// userModel.setSex("unknow");
 		// ConversationMessage conversation = getLastConversation(userModel
 		// .getJid());
 		// if (null != conversation) {
 		// userModel.setLastConversation(conversation);
 		// }
 		// userList.add(userModel);
 		// groupModel.addUserModel(userModel);
 		// }
 		// userListMap.put(group.getName(), userList);
 		// return groupModel;
 		// }
 		//
 		// public List<String> getFriendNames() {
 		// List<String> friendNames = new ArrayList<String>();
 		// Roster roster = getConnection().getRoster();
 		// Collection<RosterGroup> entriesGroup = roster.getGroups();
 		// for (RosterGroup group : entriesGroup) {
 		// Collection<RosterEntry> entries = group.getEntries();
 		// // Log.i("---", group.getName());
 		// for (RosterEntry entry : entries) {
 		// // Log.i("---", "name: "+entry.getUser());
 		// friendNames.add(entry.getUser());
 		// }
 		// }
 		// return friendNames;
 		//
 		// }
 
 		/**
 		 * @param username
 		 *            :根据用户名搜索
 		 * @throws ExecutionException
 		 * @throws InterruptedException
 		 **/
 		private List<UserModel> searchFriendList = new ArrayList<UserModel>();
 
 		public List<UserModel> getSearchFriendList() {
 			return searchFriendList;
 		}
 
 		public void setSearchFriendList(List<UserModel> searchFriendList) {
 			this.searchFriendList = searchFriendList;
 		}
 
 		// public void searchFriends(final String username)
 		// throws InterruptedException, ExecutionException {
 		// final ProgressDialog progressDialog = new ProgressDialog(
 		// notificationService);
 		// progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		// progressDialog.setTitle("提示");
 		// progressDialog.setCanceledOnTouchOutside(false);
 		// progressDialog.setCancelable(true);
 		// progressDialog.setMessage("正在加载数据，请稍候...");
 		// AsyncTask<String, Integer, List<UserModel>> task = new
 		// AsyncTask<String, Integer, List<UserModel>>() {
 		// @Override
 		// protected void onPreExecute() {
 		// super.onPreExecute();
 		// progressDialog.show();
 		// }
 		//
 		// @Override
 		// protected void onPostExecute(List<UserModel> result) {
 		// super.onPostExecute(result);
 		// if (progressDialog.isShowing()) {
 		// progressDialog.cancel();
 		// }
 		// if (null == result) {
 		// Toast.makeText(notificationService, "搜索好友失败",
 		// Toast.LENGTH_SHORT).show();
 		// } else {
 		// notificationService.sendBroadcast(new Intent(
 		// "push.search.change"));
 		// }
 		// }
 		//
 		// @Override
 		// protected List<UserModel> doInBackground(String... param) {
 		// searchFriendList.clear();
 		// // List<UserModel> searchFriendList = new
 		// // ArrayList<UserModel>();
 		// try {
 		// UserSearchManager usm = new UserSearchManager(
 		// getConnection());
 		// Form searchForm = usm.getSearchForm("search."
 		// + getConnection().getServiceName());
 		// Form answerForm = searchForm.createAnswerForm();
 		// answerForm.setAnswer("Username", true);
 		// answerForm.setAnswer("search", username);
 		// ReportedData data = usm.getSearchResults(answerForm,
 		// "search." + getConnection().getServiceName());
 		// Iterator<Row> i = data.getRows();
 		// Row row = null;
 		// // 获取所有好友名字
 		// List<String> friendNames = getFriendNames();
 		// UserModel user = null;
 		// while (i.hasNext()) {
 		// // user =
 		// //
 		// ModelCreator.instance().createModel(CommonUserModel.class,Preferences.getUserName(sharePref));
 		// user = new UserModel();
 		// row = (Row) i.next();
 		//
 		// if (friendNames.contains(row.getValues("jid")
 		// .next().toString())) {
 		// continue;
 		// }
 		// user.setName(row.getValues("Username").next()
 		// .toString());
 		// user.setJid(row.getValues("jid").next().toString());
 		// if (user.getName().equals(
 		// DeviceInfoUtil
 		// .getDeviceId(notificationService))) {
 		// continue;
 		// }
 		// searchFriendList.add(user);
 		// }
 		// return searchFriendList;
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// Log.e("chat_searchFriends", "查找好友失败");
 		// return null;
 		// }
 		// }
 		// };
 		// task.execute();
 		//
 		// }
 
 		/**
 		 * @param userName
 		 * 
 		 **/
 		public boolean addUser(String jid, String name) {
 			try {
 				Roster roster = getConnection().getRoster();
 				roster.createEntry(jid, name, null);
 				Log.v("addFriend", "search Friend success ");
 			} catch (Exception e) {
 				e.printStackTrace();
 				Log.w("addFriend", "search Friend failed " + e.getMessage());
 				return false;
 			}
 			return true;
 		}
 
 		/**
 		 * @param userName
 		 * 
 		 **/
 		public boolean addUser2Group(String jid, String name, String groupName) {
 			try {
 				Roster roster = getConnection().getRoster();
 				RosterGroup rg = roster.getGroup(groupName);
 				if (rg == null) {
 					roster.createGroup(groupName);
 				}
 				roster.createEntry(jid, name, new String[] { groupName });
 				Log.v("addFriend", "search Friend success ");
 			} catch (Exception e) {
 				e.printStackTrace();
 				Log.w("addFriend", "search Friend failed " + e.getMessage());
 				return false;
 			}
 			return true;
 		}
 
 		public ConversationMessage getLastConversation(String chatjid) {
 			// 查询id最大的聊天记录
 			String sql = "select max(id) from Conversation where chater = '"
 					+ chatjid + "'";
 			long conId = userMf.queryRawValue(sql);
 			ConversationMessage conversation = userMf.queryForId(conId,
 					ConversationMessage.class);
 			return conversation;
 		}
 
 		public Presence getPresence(String userId) {
 			return connection.getRoster().getPresence(userId);
 		}
 
 		public Map<String, String> getSexJidMap() {
 			return sexJidMap;
 		}
 
 		public void setSexJidMap(Map<String, String> sexJidMap) {
 			this.sexJidMap = sexJidMap;
 		}
 	}
 
 	public static String getMeJid() {
 		return meJid;
 	}
 
 	public void collectedFriend() {
 		Pool.getPool().execute(new Runnable() {
 			@Override
 			public void run() {
 				String result = null;
                 String servicenName="";
                 if(null == connection)
                 {
                     if(null == servicenName || "".equals(servicenName))
                     {
                         PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(notificationService
                                 .getApplicationContext(),
                                 CubeConstants.CUBE_CONFIG);
                         servicenName = propertiesUtil.getString("ChatServiceName","istation.csair.com");
                     }
                 }
                 else
                 {
                     servicenName = connection.getServiceName();
                 }
                 if(null == servicenName || !"".equals(servicenName))
                 {
                     PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(notificationService
                             .getApplicationContext(),
                             CubeConstants.CUBE_CONFIG);
                     servicenName = propertiesUtil.getString("ChatServiceName","istation.csair.com");
                 }
 				String url = URL.CHATSHOW + "/" + usernameStore + "@"
 						+ servicenName
 						+ URL.getSessionKeyappKey();
 				try {
 					result = HttpUtil.doWrapedHttp(
 							notificationService.getApplicationContext(),
 							new String[] { url, "", HttpUtil.UTF8_ENCODING,
 									HttpUtil.HTTP_GET });
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				if (result != null) {
 					try {
 						IMModelManager.instance().cleanCollectedData();
 						JSONArray jsonary = new JSONArray(result);
 						int length = jsonary.length();
 						ArrayList<String> collecteds = new ArrayList<String>();
 
 						if (length != 0) {
 							for (int i = 0; i < jsonary.length(); i++) {
 								JSONObject json = jsonary.getJSONObject(i);
 								collecteds.add(json.getString("jid"));
 							}
 							IMModelManager.instance().addCollectUserList(
 									collecteds);
 							IMModelManager.instance()
 									.changUserFavor(collecteds);
 							IMModelManager.instance().getFriendContainer()
 									.notifyContentChange();
 							IMModelManager.instance().getFavorContainer()
 									.notifyContentChange();
 						}
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 	}
 	
 	public Type getType() {
 		return type;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 	public enum Type{
 		PUSH, CHAT
 	}
 }
