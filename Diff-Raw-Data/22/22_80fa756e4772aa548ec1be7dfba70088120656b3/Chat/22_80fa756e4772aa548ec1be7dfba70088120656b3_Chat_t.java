 package com.example.haw_app.socialfeatures.implementation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.jivesoftware.smack.AccountManager;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 //import org.jivesoftware.smack.SASLAuthentication;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.MessageTypeFilter;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 
 import com.example.haw_app.ChatActivity;
 import com.example.haw_app.LoginActivity;
 import com.example.haw_app.database.DatabaseSocialFeatures;
 import com.example.haw_app.socialfeatures.interfaces.IChat;
 
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.AsyncTask;
 import android.os.Handler;
 
 public class Chat implements IChat {
 	private static Chat instance = null;
 	public static final String HOST = "ec2-176-34-76-54.eu-west-1.compute.amazonaws.com";
 	public static final int PORT = 5222;
 	public static final String SERVICE = "ec2-176-34-76-54.eu-west-1.compute.amazonaws.com";
 	ChatActivity chatActivity = null;
 	LoginActivity loginActivity = null;
 	private XMPPConnection connection;
 	private Handler mHandler = new Handler();
 	private ArrayList<String> offlineMessages = new ArrayList<String>();
 	private PacketListener packetListenerOffline;
 
 	// private Handler eHandler = new Handler();
 	// private ArrayList<String> contactList = new ArrayList<String>();
 
 	private Chat() {
 	}
 
 	@Override
 	public void setChatActivity(ChatActivity chatActivity) {
 		this.chatActivity = chatActivity;
 	}
 
 	@Override
 	public void setLoginActivity(LoginActivity loginActivity) {
 		this.loginActivity = loginActivity;
 	}
 
 	@Override
 	public void sendMessage(String to, String text) {
 		if (connection != null) {
 			to = to + "@" + SERVICE; // komplette adresse muss angegeben werden
 			Message msg = new Message(to, Message.Type.chat);
 			msg.setBody(text);
 			// Log.i("HAW_App Chat ", "Text SEND ");
 			connection.sendPacket(msg);
 		}
 	}
 
 	/**
 	 * Errortext wird an der LoginActivity gesendet.
 	 * 
 	 * @param errorNr
 	 */
 	private void sendError(int errorNr) {
 		switch (errorNr) {
 		case 0:
 			loginActivity.setError("Connect fehlgeschlagen");
 			break;
 		case 1:
 			loginActivity.setError("Login fehlgeschlagen");
 			break;
 		case 2:
 			loginActivity.setError("Register fehlgeschlagen");
 			break;
 		case 3:
 			loginActivity.setError("");
 			break;
 		}
 
 		mHandler.post(new Runnable() {
 			public void run() {
 				loginActivity.errorText();
 			}
 		});
 	}
 
 	/**
 	 * Holt erstmal die Offlinenachrichten. Ausgabe kann nicht sofort
 	 * stattfinden, da es sein kann das die ChatActivity noch nicht aktiv ist.
 	 */
 	private void setConnectionOffline() {
 		if (connection != null) {
 			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
 			packetListenerOffline = new PacketListener() {
 				@Override
 				public void processPacket(Packet packet) {
 					Message message = (Message) packet;
 					if (message.getBody() != null) {
 						String fromName[] = message.getFrom().split("@");
 						// Log.i("HAW_App Chat mOffline", "Text Recieved "
 						// + message.getBody() + " from " + fromName[0]);
 						offlineMessages.add("from " + fromName[0]
 								+ " Offline Message:");
 						offlineMessages.add(message.getBody());
 					}
 				}
 			};
 			connection.addPacketListener(packetListenerOffline, filter);
 		}
 	}
 
 	@Override
 	public void showOfflineMessages() {
 		if ((chatActivity != null) && (!offlineMessages.isEmpty())) {
 			for (String message : offlineMessages) {
 				chatActivity.setMessage(message);
 			}
 		}
 		// beendet den packetListenerOffline, da sonst alles doppelt gelesen
 		// wird.
 		connection.removePacketListener(packetListenerOffline);
 		chatActivity.setListAdapter();
 		setConnectionOnline();
 	}
 
 	/**
 	 * Nachrichten werden ans Chatactivity sofort gesendet.
 	 */
 	private void setConnectionOnline() {
 		if (connection != null) {
 			PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
 			connection.addPacketListener(new PacketListener() {
 				@Override
 				public void processPacket(Packet packet) {
 					Message message = (Message) packet;
 					if (message.getBody() != null) {
 						String fromName[] = message.getFrom().split("@");
 						// Log.i("HAW_App Chat mOnline ", "Text Recieved "
 						// + message.getBody() + " from " + fromName[0]);
 						chatActivity.setMessage("from " + fromName[0] + ":");
 						chatActivity.setMessage(message.getBody());
 
 						mHandler.post(new Runnable() {
 							public void run() {
 								chatActivity.setListAdapter();
 							}
 						});
 					}
 				}
 			}, filter);
 		}
 	}
 
 	@Override
 	public void chatDisconnect() {
 		try {
 			if (connection != null) {
 				// Log.i("HAW_App Chat ", "DISCONNECT");
 				connection.disconnect();
 				connection = null;
 			}
 		} catch (Exception e) {
 
 		}
 	}
 
 	@Override
 	public void prepareConnection() {
 		if (connection == null) {
 			ConnectionConfiguration connConfig = new ConnectionConfiguration(
 					HOST, PORT, SERVICE);
 			this.connection = new XMPPConnection(connConfig);
 		}
 
 	}
 
 	@Override
 	public boolean isConnect() {
 		return connection != null;
 
 	}
 
 	/**
 	 * Im Hintergrund wird eine Verbindung und login durchgef�hrt.
 	 * 
 	 */
 	private class startConnectBackground extends AsyncTask<String, Void, Void> {
 		@Override
 		protected Void doInBackground(String... params) {
 			String userName = params[0];
 			String password = params[1];
 			prepareConnection();
 			try {
 				connection.connect();
 				setConnectionOffline();
 			} catch (XMPPException ex) {
 				sendError(0);
 			}
 			try {
 				// SASLAuthentication.supportSASLMechanism("PLAIN", 0);
 				connection.login(userName, password);
				addUserAutomatic();
 				sendError(3);
 				loginActivity.changeActivity();
 
 				// Setzt Status auf Online
 				Presence presence = new Presence(Presence.Type.available);
 				connection.sendPacket(presence);
 
 			} catch (XMPPException ex) {
 				sendError(1);
 				chatDisconnect();
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void startConnect(String userName, String password) {
 		startConnectBackground connectTask = new startConnectBackground();
 		connectTask.execute(new String[] { userName, password });
 	}
 
 	/**
 	 * Im Hintergrund wird eine Verbindung, Registierung und login durchgef�hrt.
 	 * 
 	 */
 	private class registerUserBackground extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 			String userName = params[0];
 			String password = params[1];
 			prepareConnection();
 
 			try {
 				connection.connect();
 			} catch (XMPPException ex) {
 				sendError(0);
 				chatDisconnect();
 			}
 
 			try {
 				AccountManager manager = new AccountManager(connection);
 				manager.createAccount(userName, password);
 				// connection.disconnect();
 				startConnect(userName, password);
 			} catch (XMPPException ex) {
 				sendError(2);
 				chatDisconnect();
 			}
 			return null;
 		}
 	}
 
 	@Override
 	public void registerUser(String userName, String password) {
 		registerUserBackground registerTask = new registerUserBackground();
 		registerTask.execute(new String[] { userName, password });
 	}
 
 	/**
 	 * Kontaktliste erstellen. Zuerst werden alle Kontakte geholt und dann eine
 	 * Funktion aufgerufen die f�r die Aktualisierung zust�ndig ist.
 	 * 
 	 */
 	private class ContactFetcher extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 
 			Roster roster = connection.getRoster();
 			for (RosterEntry entry : roster.getEntries()) {
 				String[] fromUser = entry.getUser().split("@");
 				if (roster.getPresence(entry.getUser()).isAvailable()) {
 					chatActivity.addContact(fromUser[0] + "-(Online)");
 				} else {
 					chatActivity.addContact(fromUser[0] + "-(Offline)");
 				}
 			}
 			chatActivity.setContactAdapter();
 			ContactPush();
 
 			return null;
 		}
 	}
 
 	/**
 	 * Aktualisierung der Kontaktliste, falls ein Nutzer Online/Offline gehen
 	 * sollte.
 	 */
 	private void ContactPush() {
 		Roster roster = connection.getRoster();
 		roster.addRosterListener(new RosterListener() {
 			public void entriesAdded(Collection<String> addresses) {
 			}
 
 			public void entriesDeleted(Collection<String> addresses) {
 			}
 
 			public void entriesUpdated(Collection<String> addresses) {
 			}
 
 			public void presenceChanged(Presence presence) {
 				String[] fromUser = presence.getFrom().split("@");
 				if (presence.isAvailable()) {
 					chatActivity.changeContactOnline(fromUser[0]);
 					chatActivity.setContactAdapter();
 				} else {
 					chatActivity.changeContactOffline(fromUser[0]);
 					chatActivity.setContactAdapter();
 				}
 
 			}
 		});
 
 	}
 
 	@Override
 	public void startContact() {
 		ContactFetcher contactTask = new ContactFetcher();
 		contactTask.execute();
 	}
 
 	@Override
 	public boolean addUser(String matNr, String userName) {
 		try {
 			Roster roster = connection.getRoster();
			matNr = matNr+ "@" + SERVICE;
 			roster.createEntry(matNr, userName, null);
 		} catch (XMPPException e) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void addUserAutomatic() {
 		DatabaseSocialFeatures dbSF = new DatabaseSocialFeatures(
				loginActivity.getApplicationContext());
 		SQLiteDatabase db = dbSF.getReadableDatabase();
 
 		Cursor c = db.rawQuery("SELECT * FROM "
 				+ DatabaseSocialFeatures.DB_TABLE_CHAT, null);
 		if (c != null) {
 			if (c.moveToFirst()) {
 				do {
 					String matNr = c.getString(c.getColumnIndex("matNr"));
 					String userName = c.getString(c.getColumnIndex("userName"));
 					if(addUser(matNr, userName))
 						db.delete(DatabaseSocialFeatures.DB_TABLE_CHAT, "matNr" + " = '" + matNr + "'", null);
 				} while (c.moveToNext());
 			}
 		}
 		db.close();
 
 	}
 
 	public static Chat getInstance() {
 		if (instance == null) {
 			instance = new Chat();
 		}
 		return instance;
 	}
 }
