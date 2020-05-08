 package lo52.messaging.activities;
 
 import java.util.ArrayList;
 
 import lo52.messaging.R;
 import lo52.messaging.model.Conversation;
 import lo52.messaging.model.broadcast.MessageBroacast;
 import lo52.messaging.services.NetworkService;
 import android.app.TabActivity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.TabHost;
 import android.widget.Toast;
 
 /**
  *	Activité regroupant plusieurs sous-activités au sein de différents onglets 
  */
 public class LobbyActivity extends TabActivity {
 
 	private Intent networkService;
 	private static final String TAG = "LobbyActivity";
 	private SharedPreferences preferences;
 
 	// Utilisé pour indiquer à l'activité ConversationPagerActivity qu'elle doit aller sur un fragment de conversation particulier
 	private ArrayList<Integer> switchToConversation;
 
 	// Tags pour les différents onglets de l'activité
 	public static final String	TAG_TAB_USERLIST 		= "tab1";
 	public static final String	TAG_TAB_CONVERSATIONS 	= "tab2";
 	public static final String	TAG_TAB_MAP			 	= "tab3";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.lobby);
 
 		Log.d(TAG, "Lancement activité lobby");
 
 		// Lancement du service Network
 		networkService = new Intent(LobbyActivity.this, NetworkService.class);
 		startService(networkService);
 
 		// Initialisation préférences
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		preferences.registerOnSharedPreferenceChangeListener(null);
 
 		// Récupération du TabHost
 		TabHost tabHost = getTabHost();
 		TabHost.TabSpec spec; 
 		Intent intent;
 
 		switchToConversation = new ArrayList<Integer>();
 
 		/**
 		 * Création du tab de la liste des users (UserListActivity)
 		 **/
 		intent = new Intent().setClass(this, UserListActivity.class);
 		spec = tabHost.newTabSpec(TAG_TAB_USERLIST).setIndicator(getString(R.string.lobby_tab_users), getResources().getDrawable(R.drawable.icon_group)).setContent(intent);
 		tabHost.addTab(spec);
 
 		/**
 		 * Création du tab de la liste des conversations (ConversationPagerActivity)
 		 **/
 		intent.setClass(this, ConversationPagerActivity.class);
 		spec = tabHost.newTabSpec(TAG_TAB_CONVERSATIONS).setIndicator(getString(R.string.conversations_tab_name), getResources().getDrawable(R.drawable.icon_chat)).setContent(intent);
 		tabHost.addTab(spec);
 
 		/**
 		 * Création du tab de carte de géolocalisation des utilisateurs (LocalizationMapActivity)
 		 **/
 		intent = new Intent().setClass(this, LocalizationMapActivity.class);
 		spec = tabHost.newTabSpec(TAG_TAB_MAP).setIndicator(getString(R.string.lobby_tab_map), getResources().getDrawable(R.drawable.icon_worldmap)).setContent(intent);
 		tabHost.addTab(spec);
 
 	}
 
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 
 		// Arrêt du service Network en fonction des préférences de l'utilisateur
 		if (!preferences.getBoolean("prefs_networkServiceKeepAlive", false)) {
 			Log.d(TAG, "arret service network");
 			// onPause est aussi appellée avant onDestroy ici donc le messageReceiver peut déjà avoir été dé-registered
 			try {
 				// Unregister du broadcastReceiver & arret du service
 				stopService(networkService);
 			} catch (Exception e) {
 				Log.e(TAG, "Service non arrêté");
 			}
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		//Enregistrement de l'intent filter
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(NetworkService.SendMessage);
 		registerReceiver(messageReceiver, filter);
 
 		//Enregistrement de l'intent filter
 		IntentFilter filter2 = new IntentFilter();
 		filter2.addAction(NetworkService.SendConversation);
 		registerReceiver(conversationReceiver, filter2);
 
 		IntentFilter userListUpdatefilter = new IntentFilter();
 		userListUpdatefilter.addAction(NetworkService.UserListUpdated);
 		registerReceiver(UserlistUpdate_BrdcReceiver, userListUpdatefilter);
 	}
 
 	@Override
 	protected void onPause() {
 		unregisterReceiver(conversationReceiver);
 		unregisterReceiver(messageReceiver);
 		unregisterReceiver(UserlistUpdate_BrdcReceiver);
 		super.onPause();
 	}
 
 
 
 
 	/**
 	 * Recoit les nouvelles conversations. Affiche un toast si l'utilisateur n'est pas sur l'onglet des conversations
 	 */
 	private BroadcastReceiver conversationReceiver = new  BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			/*
 			 * Principe : si le tab des conversations est inactif, on affiche un toast
 			 */
 
 			TabHost tabhost = getTabHost();
 			if (tabhost.getCurrentTabTag() != TAG_TAB_CONVERSATIONS) {
 				Bundle bundle = intent.getBundleExtra("conversation");
 				Conversation conversation = bundle.getParcelable("conversation");
 
 				// Phrase au pluriel ou singulier selon le nombre de personnes dans la conversation
 				String phrase = (conversation.getListIdUser().size() > 2) ? getString(R.string.conversation_creation_toast_plur) : getString(R.string.conversation_creation_toast_sing) ;
 
 				// Affichage du toast
 				Toast.makeText(context, conversation.generateConversationName() + " " + phrase, Toast.LENGTH_LONG).show();
 			}
 		}
 	};
 
 
 	/**
 	 * Nouveau message : affiche un toast si l'utilisateur n'est pas sur l'onglet des conversations
 	 */
 	private BroadcastReceiver messageReceiver = new  BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
 			TabHost tabhost = getTabHost();
 			if (tabhost.getCurrentTabTag() != TAG_TAB_CONVERSATIONS) {
 
 				Bundle bundle = intent.getBundleExtra("message");
 				MessageBroacast message = bundle.getParcelable(MessageBroacast.tag_parcelable);
 				String userName = "";
 				if (NetworkService.getListUsers().get(message.getClient_id()) != null) 
 					userName = NetworkService.getListUsers().get(message.getClient_id()).getName();
 
 				// Affichage du toast
 				Toast.makeText(context, userName + " " + getString(R.string.conversation_user_received_message), Toast.LENGTH_LONG).show();
 			}
 		}
 	};
 
 
 	/**
 	 * Nouvel utilisateur
 	 * Broadcast receiver qui reçoit une notification comme quoi la liste des utilisateurs doit être mise à jour
 	 * Affiche un toast si l'on est pas sur l'onglet UserList
 	 */
 	private BroadcastReceiver UserlistUpdate_BrdcReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 
			// FIXME : est effectué non seulement pour les connexions d'utilisateurs mais aussi à chaque fois
			// qu'un ACK est reçu (paquetACK appelle paquetHello)
			/*TabHost tabhost = getTabHost();
 			if (tabhost.getCurrentTabTag() != TAG_TAB_USERLIST) {
 
 				// On récupère le nom de l'user depuis le bundle pour afficher un toast
 				Bundle userInfo = intent.getBundleExtra("new_user");
 				if (userInfo != null && userInfo.getString("new_user") != null) {
 					Toast.makeText(context, userInfo.getString("new_user") + " " + getString(R.string.userlist_new_connection), Toast.LENGTH_SHORT).show();
 				}
			}*/
 		}
 	};
 
 	/**
 	 * @deprecated
 	 * @param tabNumber
 	 */
 	public void setActiveTab(int tabNumber) {
 		Log.d(TAG, "Setting active tab");
 		TabHost tabHost = getTabHost();
 		tabHost.setCurrentTab(tabNumber);
 	}
 
 	/**
 	 * Switche sur le tab indiqué. Pour les tags, utiliser les static définies plus haut (TAG_TAB_xxx)
 	 * @param tabTag
 	 */
 	public void setActiveTabByTag(String tabTag) {
 		TabHost tabHost = getTabHost();
 		tabHost.setCurrentTabByTag(tabTag);
 	}
 
 
 	/**
 	 * Indique à l'activité ConversationPagerActivity qu'elle doit changer la vue vers une conversation précise quand elle onResume()
 	 * @param userIds
 	 */
 	public void setSwitchToConversationFragment(ArrayList<Integer> userIds) {
 		switchToConversation = userIds;
 	}
 
 
 	/**
 	 * Utilisé par l'activité ConversationPagerActivity pour savoir si elle doit aller sur un fragment particulier quand elle reprend
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<Integer> getSwitchToConversationFragmentStatus() {
 
 		ArrayList<Integer> list = (ArrayList<Integer>) switchToConversation.clone();
 		switchToConversation.clear();
 		return list;
 	}
 
 }
