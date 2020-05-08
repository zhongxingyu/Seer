 package lo52.messaging.activities;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Hashtable;
 
 import lo52.messaging.R;
 import lo52.messaging.adapters.UserListArrayAdapter;
 import lo52.messaging.model.Conversation;
 import lo52.messaging.model.User;
 import lo52.messaging.services.NetworkService;
 import lo52.messaging.views.UserListView;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  *	Activité servant à lister les utilisateurs 
  */
 public class UserListActivity extends Activity {
 
 	private static final String TAG = "UserListActivity";
 	// Adapter utilisé pour mapper les données au layout de la liste
 	UserListArrayAdapter adapter;
 
 	UserListView userListView;
 	Button startMultiConversBtn;
 	
 	private final int MENU_ITEM_SELECTION_MODE = 0x1;
 
 	// Liste des users
 	Hashtable<Integer, User> userList;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.useractivity);
 
 		userListView 			= (UserListView) findViewById(R.id.listView1);
 		startMultiConversBtn	= (Button) findViewById(R.id.userlist_start_multi_btn);
 
 		ArrayList<String> values = new ArrayList<String>(Arrays.asList(""));
 
 		// Utilisation de l'adapteur custom
 		adapter = new UserListArrayAdapter(this, values);
 
 
 		/**
 		 * TESTS: décommentez si vous voulez tester les conversations multi user
 		 */
 		/*User u1 = new User("pablo gruer");
 		u1.setId(12345);
 		User u2 = new User("gilles bertrand");
 		u2.setId(12346);
 		User u3 = new User("chuck norris");
 		u3.setId(12347);
 
 		NetworkService.getListUsersNoClone().put(12345, u1);
 		NetworkService.getListUsersNoClone().put(12346, u2);
 		NetworkService.getListUsersNoClone().put(12347, u3);*/
 		/**
 		 * 
 		 */
 
 		userListView.setAdapter(adapter);
 		userListView.setOnItemClickListener(itemClickListenerList);
 
 		// Listener sur le bouton pour commencer une conversation avec plusieurs utilisateurs
 		startMultiConversBtn.setOnClickListener(startMultiConversButtonListener);
 
 		// Enregistre le broadcast receiver permettant de mettre à jour la liste des users en direct
 		IntentFilter userListUpdatefilter = new IntentFilter();
 		userListUpdatefilter.addAction(NetworkService.UserListUpdated);
 		registerReceiver(UserlistUpdate_BrdcReceiver, userListUpdatefilter);
 	}
 
 
 	@Override
 	protected void onResume() {
 		Log.d(TAG, "onResume");
 
 		// on réenregistre le broadcast listener sur la liste des users
 		IntentFilter userListUpdatefilter = new IntentFilter();
 		userListUpdatefilter.addAction(NetworkService.UserListUpdated);
 		registerReceiver(UserlistUpdate_BrdcReceiver, userListUpdatefilter);
 
 		//on récupère la liste des users
 		refreshUserList();
 
 		super.onResume();
 	}
 
 
 	@Override
 	protected void onPause() {
 
 		// Désinscription du broadcast receiver
 		unregisterReceiver(UserlistUpdate_BrdcReceiver);
 
 		super.onPause();
 	}
 
 
 	@Override
 	protected void onDestroy() {
 
 		super.onDestroy();
 	}
 
 
 
 	private OnItemClickListener itemClickListenerList = new OnItemClickListener() {
 
		@Override
 		public void onItemClick(AdapterView<?> listView, View view, int position, long row) {
 
 			Log.d(TAG, "Click item list " + position);
 
 			String item = (String) listView.getAdapter().getItem(position);
 
 			/*User user_selected = null;
 			for(User user : userList.values()){
 				if(user.getName() == item){
 					user_selected = user;
 				}
 			}
 
 			if(user_selected == null){
 				Log.e(TAG,"user inconnu");
 				return;
 			}*/
 			
 			int userId = NetworkService.getUserIdByName(item);
 			
 			if (userId == 0) {	
 				Log.e(TAG,"user inconnu");
 				return;
 			}
 
 			// ArrayList contenant l'ID de l'utilisateur
 			ArrayList<Integer> list = new ArrayList<Integer>();
 			list.add(userId);
 			
 			// Création de la conversation
 			createConversation(list);
 			
 		}
 	};
 
 	private OnClickListener startMultiConversButtonListener = new OnClickListener() {
 
		@Override
 		public void onClick(View v) {
 
 			HashSet<String> usernames = adapter.getCheckUsernames();
 			Log.d(TAG, "Multi users : " + usernames);
 
 			ArrayList<Integer> userIds = new ArrayList<Integer>();
 			
 			for (String s : usernames) {
 				int id = NetworkService.getUserIdByName(s);
 				if (id == 0) {
 					Log.e(TAG, "Erreur, user inconnu");
 					
 				}
 				userIds.add(id);
 			}
 			
 			Log.d(TAG, "Création avec multi " + userIds.toString());
 			
 			// Avant de créer la conversation et mettre en pause cette activité on revient en mode de sélection simple
 			adapter.switchMultiUserChoiceMode();
 			refreshUserList();
 			
 			// Création de la conversation
 			createConversation(userIds);
 		}
 	};
 
 
 	/**
 	 * S'occupe de créer la conversation et fait l'appel à la fonction du service network pour envoyer la demande création de conversation.
 	 * Vérifie également qu'une conversation identique n'existe pas, dans quel cas la vue est redirigée vers le fragment concerné.
 	 * @param userIds	Liste des Ids des membres de la conversation
 	 */
 	private void createConversation(ArrayList<Integer> userIds) {
 		// Activité lobby
 		LobbyActivity parent = (LobbyActivity) getParent();
 		
 		// On vérifie qu'au moins 1 user a été spécifié
 		if (userIds.size() < 1) {
 			Toast.makeText(this, getString(R.string.userlist_error_no_user), Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		// Ajout de user_me à la conversation
 		if (!userIds.contains(NetworkService.getUser_me().getId()))
 			userIds.add(NetworkService.getUser_me().getId());
 
 		
 		// Création de la conversation si elle n'existe pas déjà
 		if (!NetworkService.doesConversationExist(userIds)) {
 
 			Conversation conversation = new Conversation("conversation_name", userIds);	// Le nom n'est plus utilisé
 			conversation.sendToNetworkService(getApplicationContext());
 		} else {
 			// Si la conversation existe déjà on rend le second tab actif et on essaye de switcher sur le fragment correspondant
 			parent.setSwitchToConversationFragment(userIds);
 		}
 		
 		// Rend le tab des conversations actif
 		parent.setActiveTabByTag(LobbyActivity.TAG_TAB_CONVERSATIONS);
 	}
 
 
 	/**
 	 * Gestion du menu option et des actions associées
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.user_list_activity_menu, menu);
 
 		return true;
 	}
 
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		
 		// Exécuté avant l'ouverture du menu
 		// S'il y a moins de 2 utilisateurs on n'affiche pas le bouton de choix multiple
 		if (userList.size() > 1) {
 			
 			// On supprime le bouton d'avant
 			menu.removeGroup(MENU_ITEM_SELECTION_MODE);
 			
 			// On change le texte en fonction du mode de sélection actuel
 			if (adapter.isMultiUserChoiceSelectionMode())
 				menu.add(MENU_ITEM_SELECTION_MODE, MENU_ITEM_SELECTION_MODE, 2, getString(R.string.userlist_multi_select_simple));
 			else
 				menu.add(MENU_ITEM_SELECTION_MODE, MENU_ITEM_SELECTION_MODE, 2, getString(R.string.userlist_multi_select_multi));
 			
 		} else {
 			menu.removeGroup(MENU_ITEM_SELECTION_MODE);
 		}
 		
 		
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		int menuItemId = item.getItemId();
 		if (menuItemId == R.id.item_refresh) {
 			refreshUserList();
 			//Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
 		}
 		else if (menuItemId == MENU_ITEM_SELECTION_MODE) {
 
 			Log.d(TAG, "mode sélection!");
 			adapter.switchMultiUserChoiceMode();
 
 			refreshUserList();
 
 		}
 		else {
 			Log.e(TAG, "Item non pris en charge");
 		}
 		return true;
 	}
 
 	/**
 	 * Met à jour la liste des utilisateurs en faisant appel au service
 	 */
 	public void refreshUserList() {
 		userList = NetworkService.getListUsers();
 		adapter.clearValues();
 		for(User user : userList.values()){
 			adapter.add(user.getName());
 		}
 		Log.d(TAG, "Liste users mise à jour");
 
 		// Affiche ou cache le texte Pas d'utilisateur
 		TextView tv = (TextView) findViewById(R.id.no_user);
 		if (adapter.countValues() > 0) tv.setVisibility(View.GONE);
 		else tv.setVisibility(View.VISIBLE);
 
 		// On affiche ou cache le bouton pour commencer la conversation à plusieurs
 		if (adapter.isMultiUserChoiceSelectionMode())
 			startMultiConversBtn.setVisibility(View.VISIBLE);
 		else
 			startMultiConversBtn.setVisibility(View.GONE);
 	}
 
 
 	/**
 	 * Broadcast receiver qui reçoit une notification comme quoi la liste des utilisateurs doit être mise à jour
 	 * Met simplement à jour la liste des utilisateurs
 	 */
 	private BroadcastReceiver UserlistUpdate_BrdcReceiver = new BroadcastReceiver(){
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			refreshUserList();
 		}
 	};
 
 }
