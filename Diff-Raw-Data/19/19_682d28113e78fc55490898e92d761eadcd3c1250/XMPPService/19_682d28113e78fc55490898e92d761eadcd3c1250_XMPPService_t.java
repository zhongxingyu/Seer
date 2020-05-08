 package nl.sison.xmpp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import nl.sison.xmpp.dao.BuddyEntity;
 import nl.sison.xmpp.dao.BuddyEntityDao;
 import nl.sison.xmpp.dao.BuddyEntityDao.Properties;
 import nl.sison.xmpp.dao.ConnectionConfigurationEntity;
 import nl.sison.xmpp.dao.DaoSession;
 import nl.sison.xmpp.dao.MessageEntity;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.PacketInterceptor;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.Roster.SubscriptionMode;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.util.StringUtils;
 
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.StrictMode;
 import android.util.Log;
 import android.widget.Toast;
 import de.greenrobot.dao.QueryBuilder;
 
 /**
  * 
  * @author Jasm Sison
  * 
  */
 public class XMPPService extends Service {
 	/**
 	 * ISSUES
 	 */
 	/**
 	 * - roster subscription request, packet listener, filter presence type
 	 * Presence.Type.subscribe -> popup dialog (yes, later, never) here:
 	 * http://www.igniterealtime.org/builds/smack/docs/
 	 * latest/documentation/roster.html
 	 */
 	/**
 	 * TODO - intercept packet to enable connection to providers
 	 */
 
 	/**
 	 * ROADMAP
 	 */
 	/**
 	 * TODO - location aware advertising - voice messages?
 	 */
 	/**
 	 * TODO - qr code triggered offers (in a shop?) buy shit together etc.
 	 */
 	/**
 	 * TODO - usage tracker (gps?)
 	 */
 
 	private static final String TAG = "XMPPService";
 
 	private ConcurrentHashMap<Long, XMPPConnection> connection_hashmap;
 
 	private BroadcastReceiver receiver;
 
 	class ServiceReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			// TODO - status = away unavailable etc.
 			// TODO + start chat
 			if (intent.getAction().equals(
 					CRUDConnectionActivity.ACTION_REQUEST_POPULATE_BUDDYLIST)) {
 				long cc_id = intent.getExtras().getLong(
 						CRUDConnectionActivity.KEY_CONNECTION_INDEX);
 				DaoSession daoSession = DatabaseUtils
 						.getReadOnlyDatabaseSession(context);
 				ConnectionConfigurationEntity cc = daoSession.load(
 						ConnectionConfigurationEntity.class, cc_id);
 				connectAndPopulateBuddyList(cc);
 			}
 
 			if (intent.getAction()
 					.equals(BuddyListActivity.ACTION_REQUEST_CHAT)) {
 				long buddy_id = intent.getExtras().getLong(
 						BuddyListActivity.KEY_BUDDY_INDEX);
 				BuddyEntity buddy = getBuddyEntityFromId(context, buddy_id);
 				XMPPConnection connection = connection_hashmap.get(buddy
 						.getConnectionId());
 				Chat chat = connection.getChatManager().createChat(
 						buddy.getPartial_jid(), null);
 				Intent response_intent = new Intent(ACTION_REQUEST_CHAT_GRANTED);
 				response_intent.putExtra(THREAD, chat.getThreadID());
 				response_intent.putExtra(KEY_BUDDY_INDEX, buddy_id);
 				response_intent.putExtra(JID,
 						StringUtils.parseBareAddress(connection.getUser()));
 				context.sendBroadcast(response_intent);
 			}
 
 			if (intent.getAction().equals(
 					ChatActivity.ACTION_REQUEST_DELIVER_MESSAGE)) {
 				Bundle bundle = intent.getExtras();
 				String thread = bundle.getString(ChatActivity.THREAD);
 				String message = bundle.getString(ChatActivity.MESSAGE);
 				long buddy_id = bundle.getLong(ChatActivity.KEY_BUDDY_INDEX);
 
 				BuddyEntity buddy = getBuddyEntityFromId(context, buddy_id);
 
 				XMPPConnection connection = connection_hashmap.get(buddy
 						.getConnectionId());

				// fuck the thread prefetch...
				Chat chat = connection.getChatManager().createChat(
						buddy.getPartial_jid(), null);
 				try {
					chat.sendMessage(message);
 
 					Intent ack_intent = new Intent(ACTION_MESSAGE_SENT);
 					ack_intent.putExtra(
 							KEY_MESSAGE_INDEX,
 							storeMessageEntityReturnId(context, message,
 									connection, chat, buddy_id));
 					DatabaseUtils.close();
 					context.sendBroadcast(ack_intent);
 				} catch (XMPPException e) {
 					context.sendBroadcast(new Intent(ACTION_MESSAGE_ERROR));
 					e.printStackTrace();
 				}
 
 			}
 
 		}
 
 		private long storeMessageEntityReturnId(Context context,
 				String message, XMPPConnection connection, Chat chat,
 				long buddy_id) {
 			DaoSession daoSession = DatabaseUtils
 					.getWriteableDatabaseSession(context);
 			MessageEntity me = new MessageEntity();
 			me.setContent(message);
 			me.setDelivered(true);
 			me.setReceived_date(new Date());
 			me.setReceiver_jid(chat.getParticipant());
 			me.setSender_jid(connection.getUser());
 			me.setThread(chat.getThreadID());
 			me.setBuddyId(buddy_id);
 			return daoSession.getMessageEntityDao().insert(me);
 		}
 
 		private BuddyEntity getBuddyEntityFromId(Context context, final long id) {
 			DaoSession daoSession = DatabaseUtils
 					.getReadOnlyDatabaseSession(context);
 			BuddyEntity buddy = daoSession.load(BuddyEntity.class, id);
 			DatabaseUtils.close();
 			return buddy;
 		}
 	};
 
 	/**
 	 * Intent actions (for Broadcasting)
 	 */
 	public static final String ACTION_BUDDY_PRESENCE_UPDATE = "nl.sison.xmpp.ACTION_BUDDY_PRESENCE_UPDATE";
 	public static final String ACTION_MESSAGE_INCOMING = "nl.sison.xmpp.ACTION_BUDDY_NEW_MESSAGE";
 	public static final String ACTION_CONNECTION_LOST = "nl.sison.xmpp.ACTION_BUDDY_CONNECTION_LOST";
 	public static final String ACTION_CONNECTION_RESUMED = "nl.sison.xmpp.ACTION_BUDDY_CONNECTION_LOST";
 	public static final String ACTION_REQUEST_CHAT_GRANTED = "nl.sison.xmpp.ACTION_REQUEST_CHAT_GRANTED";
 	public static final String ACTION_REQUEST_CHAT_ERROR = "nl.sison.xmpp.ACTION_REQUEST_CHAT_ERROR";
 	public static final String ACTION_MESSAGE_SENT = "nl.sison.xmpp.ACTION_MESSAGE_SENT";
 	public static final String ACTION_MESSAGE_ERROR = "nl.sison.xmpp.ACTION_MESSAGE_ERROR";
 
 	/**
 	 * Intent extras
 	 */
 	// connection
 	public static final String KEY_CONNECTION_INDEX = "USTHUAS34027334H"; // long
 
 	// presence
 	public static final String KEY_BUDDY_INDEX = "UST32UAS340273#@H"; // long
 
 	// message
 	public static final String KEY_MESSAGE_INDEX = "UST32323HU027334H"; // long
 
 	// everybody can use these two
 	public static final String JID = "239eunheun34808"; // String
 	public static final String MANY_JID = "239443342eunheun34808"; // Arraylist<String>
 
 	// incoming message
 	public static final String MESSAGE = "239e#$%unheun34808"; // String
 	public static final String FROM_JID = "23heun348$%$#&08"; // String
 	public static final String THREAD = "@$@$4P789"; // String
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		makeConnectionsFromDatabase();
 		receiver = new ServiceReceiver();
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BuddyListActivity.ACTION_REQUEST_CHAT);
 		filter.addAction(ChatActivity.ACTION_REQUEST_DELIVER_MESSAGE);
 		filter.addAction(CRUDConnectionActivity.ACTION_REQUEST_POPULATE_BUDDYLIST);
 		registerReceiver(receiver, filter);
 		startService(new Intent(XMPPService.this, XMPPNotificationService.class));
 	}
 
 	private void makeConnectionsFromDatabase() {
 		List<ConnectionConfigurationEntity> all_conns = getAllConnectionConfigurations();
 		if (connection_hashmap == null) {
 			connection_hashmap = new ConcurrentHashMap<Long, XMPPConnection>();
 		}
 		weakenNetworkOnMainThreadPolicy(); // TODO - remove and implement this
 											// as asynctask or runnable when
 											// debugging is
 											// done, makeToast must run on main
 											// thread or is invisible
 		for (final ConnectionConfigurationEntity cc : all_conns) {
 			connectAndPopulateBuddyList(cc);
 		}
 	}
 
 	private void connectAndPopulateBuddyList(
 			final ConnectionConfigurationEntity cc) {
 		XMPPConnection connection;
 		try {
 			connection = connectToServer(cc);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			connection = null;
 		}
 		if (connection != null) {
 			long cc_id = cc.getId();
 			connection_hashmap.put(cc_id, connection);
 			setListeners(connection, cc_id);
 			// auto-accept subscribe request
 			connection.getRoster().setSubscriptionMode(
 					SubscriptionMode.accept_all);
 			populateBuddyLists(connection, cc_id);
 		}
 	}
 
 	private void populateBuddyLists(XMPPConnection connection, final long cc_id) {
 		DaoSession daoSession = DatabaseUtils.getWriteableDatabaseSession(this);
 		QueryBuilder<BuddyEntity> qb = daoSession.getBuddyEntityDao()
 				.queryBuilder();
 		Roster roster = connection.getRoster();
 		for (RosterEntry re : roster.getEntries()) {
 			List<BuddyEntity> query_result = qb.where(
 					BuddyEntityDao.Properties.Partial_jid.eq(StringUtils
 							.parseBareAddress(re.getUser()))).list();
 			BuddyEntity b;
 			if (query_result.isEmpty()) {
 				b = new BuddyEntity();
 			} else {
 				b = query_result.get(0);
 			}
 
 			String partial_jid = StringUtils.parseBareAddress(re.getUser());
 			Presence p = roster.getPresence(partial_jid); // TODO - experiment
 															// with
 															// partial_jid
 			// TODO - determine whether roster.getPresence(... accepts partial
 			// jids or full jids (i.e. with our without resource)
 
 			setBuddyBasic(b, partial_jid, cc_id);
 			if (p != null) {
 				setBuddyPresence(b, p);
 			}
 
 			daoSession.insertOrReplace(b);
 		}
 		DatabaseUtils.close();
 	}
 
 	private void setBuddyPresence(BuddyEntity b, Presence p) {
 		b.setIsAvailable(p.isAvailable());
 		Log.i(TAG, "p.isAvailable(): " + p.isAvailable());
 
 		// these makeToast might have broken the update mechanism // ->
 		// Nope: apparently it didn't
 
 		b.setIsAway(p.isAway());
 		Log.i(TAG, "p.isAway(): " + p.isAway());
 
 		b.setPresence_status(p.getStatus());
 		b.setPresence_type(p.getType().toString());
 	}
 
 	private void setBuddyBasic(BuddyEntity b, final String partial_jid,
 			final long cc_id) {
 		b.setPartial_jid(partial_jid);
 		b.setConnectionId(cc_id);
 		if (b.getNickname() == null || b.getNickname().isEmpty()) {
 			b.setNickname(partial_jid);
 		}
 	}
 
 	private void setListeners(XMPPConnection connection, final long cc_id) {
 		setConnectionListeners(connection, cc_id);
 		setRosterListeners(connection, cc_id);
 		setIncomingMessageListener(connection);
 		setOutgoingMessageListener(connection);
 	}
 
 	@Deprecated
 	private void setOutgoingMessageListener(XMPPConnection connection) {
 		connection.addPacketInterceptor(new PacketInterceptor() {
 			public void interceptPacket(Packet p) {
 				// storeMessage((Message) p);
 				// NOTE: this must be done in the broadcastreceiver, otherwise
 				// the chatactivity's adapter can't be updated, via the intent
 			}
 		}, new PacketFilter() {
 			public boolean accept(Packet p) {
 				return p instanceof Message;
 			}
 		});
 	}
 
 	private void setIncomingMessageListener(XMPPConnection connection) {
 		connection.addPacketListener(new PacketListener() {
 			public void processPacket(Packet p) {
 				Message m = (Message) p;
 				broadcastMessage(storeSmackMessageReturnId(m));
 				DatabaseUtils.close();
 			}
 		}, new PacketFilter() {
 			public boolean accept(Packet p) {
 				return p instanceof Message;
 			}
 		});
 	}
 
 	private void broadcastMessage(long id) {
 		Intent intent = new Intent(ACTION_MESSAGE_INCOMING);
 		intent.putExtra(KEY_MESSAGE_INDEX, id);
 		sendBroadcast(intent);
 	}
 
 	private long storeSmackMessageReturnId(Message m) {
 		DaoSession daoSession = DatabaseUtils.getWriteableDatabaseSession(this);
 		MessageEntity message = new MessageEntity();
 		message.setContent(m.getBody());
 		message.setReceived_date(new Date());
 		message.setSender_jid(m.getFrom());
 		message.setReceiver_jid(m.getTo());
 		message.setThread(m.getThread());
 
 		BuddyEntity buddy = daoSession
 				.getBuddyEntityDao()
 				.queryBuilder()
 				.where(Properties.Partial_jid.eq(StringUtils.parseBareAddress(m
 						.getFrom()))).list().get(0);
 
 		message.setBuddyEntity(buddy);
 
 		return daoSession.getMessageEntityDao().insert(message);
 	}
 
 	private void broadcastPresenceUpdate(Presence p, long cc_id) {
 		String from = p.getFrom();
 		Intent intent = new Intent(ACTION_BUDDY_PRESENCE_UPDATE);
 		// intent.putExtra(JID, p.getFrom()); // NOTE: not very useful
 
 		DaoSession daoSession = DatabaseUtils.getWriteableDatabaseSession(this);
 		QueryBuilder<BuddyEntity> qb = daoSession.getBuddyEntityDao()
 				.queryBuilder();
 		List<BuddyEntity> query_result = qb.where(
 				BuddyEntityDao.Properties.Partial_jid.eq(StringUtils
 						.parseBareAddress(p.getFrom()))).list();
 
 		// create entity if it doesn't exist yet
 		// otherwise the broadcast will be pointless
 		BuddyEntity b;
 		if (query_result.isEmpty()) {
 			b = new BuddyEntity();
 			setBuddyBasic(b, StringUtils.parseBareAddress(from), cc_id);
 			setBuddyPresence(b, p);
 		} else {
 			b = query_result.get(0);
 		}
 		b.setLast_seen_resource(StringUtils.parseResource(from));
 
 		if (p.isAvailable()) {
 			b.setLast_seen_online_date(new Date());
 		}
 
 		intent.putExtra(KEY_BUDDY_INDEX, daoSession.insertOrReplace(b));
 		sendBroadcast(intent);
 	}
 
 	private void broadcastRosterUpdate(Collection<String> usernames) {
 		Intent intent = new Intent(ACTION_BUDDY_PRESENCE_UPDATE);
 		ArrayList<String> arrayList = new ArrayList<String>();
 		arrayList.addAll(usernames);
 		intent.putExtra(MANY_JID, arrayList);
 		sendBroadcast(intent);
 	}
 
 	private void setRosterListeners(XMPPConnection connection, final long cc_id) {
 		Roster roster = connection.getRoster();
 		roster.addRosterListener(new RosterListener() {
 
 			public void presenceChanged(Presence p) {
 				// NOTE: makeToast broke the listener and prevented the
 				// broadcast
 				broadcastPresenceUpdate(p, cc_id);
 			}
 
 			public void entriesUpdated(Collection<String> usernames) {
 				broadcastRosterUpdate(usernames);
 			}
 
 			public void entriesDeleted(Collection<String> usernames) {
 				broadcastRosterUpdate(usernames);
 			}
 
 			public void entriesAdded(Collection<String> usernames) {
 				broadcastRosterUpdate(usernames);
 			}
 		});
 	}
 
 	private void broadcastConnectionUpdate(final long cc_id) {
 		Intent intent;
 		if (connection_hashmap.get(cc_id).isConnected()) {
 			intent = new Intent(ACTION_CONNECTION_RESUMED);
 		} else {
 			intent = new Intent(ACTION_CONNECTION_LOST);
 		}
 		intent.putExtra(KEY_CONNECTION_INDEX, cc_id);
 		sendBroadcast(intent);
 	}
 
 	private void setConnectionListeners(XMPPConnection connection,
 			final long cc_id) {
 		connection.addConnectionListener(new ConnectionListener() {
 
 			public void reconnectionSuccessful() {
 				broadcastConnectionUpdate(cc_id);
 			}
 
 			public void reconnectionFailed(Exception ex) {
 				broadcastConnectionUpdate(cc_id);
 			}
 
 			public void reconnectingIn(int countdown) {
 			}
 
 			public void connectionClosedOnError(Exception ex) {
 				broadcastConnectionUpdate(cc_id);
 			}
 
 			public void connectionClosed() {
 				broadcastConnectionUpdate(cc_id);
 			}
 		});
 	}
 
 	private XMPPConnection connectToServer(ConnectionConfigurationEntity cc)
 			throws NumberFormatException {
 		ConnectionConfiguration xmpp_conn_config = new ConnectionConfiguration(
 				cc.getServer(), Integer.valueOf(cc.getPort()), cc.getDomain());
 		xmpp_conn_config.setCompressionEnabled(cc.getCompressed());
 		xmpp_conn_config
 				.setSASLAuthenticationEnabled(cc.getSaslauthenticated());
 		// xmpp_conn_config.setReconnectionAllowed(true); // TODO - turn off if
 		// conflict (e.g.same jid) or hopelessly bad settings
 
 		XMPPConnection connection = new XMPPConnection(xmpp_conn_config);
 		try {
 			connection.connect();
 			makeToast(cc.getLabel() + " is connected:"
 					+ connection.isConnected());
 			connection.login(cc.getUsername(), cc.getPassword(),
 					cc.getResource());
 			makeToast(cc.getLabel() + " is authenticated:"
 					+ connection.isAuthenticated());
 			cc.setConnection_success(cc.getConnection_success() + 1);
 		} catch (XMPPException e) {
 			connection = null;
 			e.printStackTrace();
 			Log.e(TAG, e.toString());
 		}
 		return connection;
 	}
 
 	private void weakenNetworkOnMainThreadPolicy() { // TODO recode with
 														// AsyncTask, maybe even
 														// all listeners
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 				.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 	}
 
 	private List<ConnectionConfigurationEntity> getAllConnectionConfigurations() {
 		DaoSession daoSession = DatabaseUtils.getReadOnlyDatabaseSession(this);
 		List<ConnectionConfigurationEntity> all = daoSession
 				.getConnectionConfigurationEntityDao().loadAll();
 		DatabaseUtils.close();
 		return all;
 	}
 
 	// TODO refactor this away to the the DatabaseUtil, this is the same as
 	// onListItemClick code in ConnectionListActivity
 	private ConnectionConfigurationEntity getConnectionConfiguration(long cc_id) {
 		DaoSession daoSession = DatabaseUtils.getReadOnlyDatabaseSession(this);
 		ConnectionConfigurationEntity cc = daoSession
 				.getConnectionConfigurationEntityDao().load(cc_id);
 		// TODO there's a lag before the database is written to, so the table
 		// appears to be empty right before it's read
 		DatabaseUtils.close();
 		return cc;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		// We want this service to continue running until it is explicitly
 		// stopped, so return sticky.
 		if (intent.hasExtra(CRUDConnectionActivity.RESTART_CONNECTION)) {
 			long cc_id = intent.getExtras().getLong(
 					CRUDConnectionActivity.RESTART_CONNECTION);
 			connectToServer(getConnectionConfiguration(cc_id));
 		}
 		return START_STICKY;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Cancel the persistent notification.
 		for (long key : connection_hashmap.keySet()) {
 			XMPPConnection conn = connection_hashmap.get(key);
 			if (conn != null && conn.isConnected()) {
 				conn.disconnect();
 			}
 		}
 	}
 
 	/**
 	 * Show a notification while this service is running. TODO - use this to
 	 * notify if anyone is chatting with you, last message stuff
 	 */
 	/**
 	 * TODO refactor notification to some other service with a broadcast
 	 * receiver
 	 */
 
 	private void makeToast(String message) {
 		if (!BuildConfig.DEBUG)
 			return;
 		Log.i(TAG, message);
 		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
