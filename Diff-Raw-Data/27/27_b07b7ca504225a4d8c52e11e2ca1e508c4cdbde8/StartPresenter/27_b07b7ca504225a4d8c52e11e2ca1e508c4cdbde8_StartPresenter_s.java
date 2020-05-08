 package com.example.wecharades.presenter;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 
 import com.example.wecharades.model.DCMessage;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Invitation;
 import com.example.wecharades.model.Player;
 import com.example.wecharades.views.StartActivity;
 import com.parse.ParseAnalytics;
 import com.parse.ParseInstallation;
 import com.parse.PushService;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class StartPresenter extends Presenter implements Observer{
 
 	private StartActivity activity;
 	//private LinkedHashMap<String, ArrayList<Game>> listMap; 
 	private final static String [] headers = {"Your turn", "Opponent's turn", "Finished games"};
 
 	// Adapter for ListView Contents and the actual listview
 	private SeparatedListAdapter adapter;
 	private Map<Game, Map<Player, Integer>> score;
	private boolean isUpdating = false;
 
 	public StartPresenter(StartActivity activity) {
 		super(activity);
 		this.activity = activity;
 		dc.addObserver(this);
 	}
 	public void createNotificationInstallation() {
 		PushService.setDefaultPushCallback(activity.getApplicationContext(), StartActivity.class);
 		ParseInstallation.getCurrentInstallation().saveInBackground();
 		ParseAnalytics.trackAppOpened(activity.getIntent());
 	}
 
 	public void initiate(){
 		String currentPlayer = dc.getCurrentPlayer().getName();
 		activity.setAccountName(currentPlayer);
 		PushService.subscribe(activity.getApplicationContext(), currentPlayer, StartActivity.class);
 		//dc.subscribetoNotification(activity.getApplicationContext());
 	}
 
 	public void update(){
		if(!isUpdating){
			isUpdating = true;
			updateList(dc.getGames());
			dc.getInvitations();
			activity.showProgressBar();
		}
 	}
 
 
 	/**
 	 * Private Method - Called when a new updated game list is received from the database.
 	 */
 	private void updateList(ArrayList<Game> gameList){
 		LinkedHashMap<String, ArrayList<Game>> listMap = new LinkedHashMap<String, ArrayList<Game>>();
 		score = new TreeMap<Game, Map<Player, Integer>>();
 		parseList(listMap, gameList);
 
 		createListView(listMap);
 	}
	
 	/**
 	 * Private method which is called after an updated list of invitations is received.
 	 */
 	private void setInvitationStatus(List<Invitation> invites){
 		LinkedList<Invitation> recInv = new LinkedList<Invitation>();
 		for(Invitation inv : invites){
 			if(inv.getInvitee().equals(dc.getCurrentPlayer())){
 				recInv.add(inv);
 			}
 		}
 		activity.setInvitations(recInv.size());
 	}
 
 	/**
 	 * Check if the there is a user logged in. 
 	 * 	@return if a user is logged in
 	 * 
 	 */
 	public boolean checkLogin() {
 		if(dc.getCurrentPlayer() == null){
 			goToLoginActivity();
 			return false;
 		}
 		else{	
 			createNotificationInstallation();//TODO detta skapas VARJE gng - vill vi verkligen det?
 			return true;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void parseList(LinkedHashMap<String, ArrayList<Game>> listMap, ArrayList<Game> gameList) {
 		for (String s : headers) {
 			listMap.put(s, new ArrayList<Game>());
 		}
 		for (Game g : gameList) {
 			score.put(g, dc.getGameScore(g));
 			if (g.isFinished())
 				listMap.get("Finished games").add(g);
 			else if (g.getCurrentPlayer().equals(dc.getCurrentPlayer()) && !g.isFinished())
 				listMap.get("Your turn").add(g);
 			else
 				listMap.get("Opponent's turn").add(g);
 		}
 	}
 
 	public void createListView(LinkedHashMap<String, ArrayList<Game>> listMap){
 		adapter = new SeparatedListAdapter(activity);
 		for (String s : headers) {
 			if(!listMap.get(s).isEmpty()) {
 				adapter.addSection(s, new GameAdapter(activity, listMap.get(s), dc.getCurrentPlayer(), score));		
 			}
 		}
 		activity.setGameList(adapter);
 	}
 
 	/**
 	 * Log out the current user
 	 */
 	public void logOut() {
 		dc.logOutPlayer(activity);
 		goToLoginActivity();
 	}
	
	public void resetDownloadStatus(){
		isUpdating = false;
	}
 
 	/**
 	 * Called whenever a message is received from the DataController
 	 * 	This method will override the default, but will pass on the message
 	 * 	to super when appropriate
 	 * @param obs - The observer
 	 * @param obj - The object included in the message
 	 */
 	private int recent = 0;
 	public void update(Observable obs, Object obj) {
 		if(obj.getClass().equals(DCMessage.class)
 				&& obj != null){
 			DCMessage dcm = (DCMessage) obj;
 			if (dcm.getMessage() == DCMessage.DATABASE_GAMES){
 				recent++;
 				updateList((ArrayList<Game>) dcm.getData());
 			} else if (dcm.getMessage() == DCMessage.INVITATIONS){
 				recent++;
 				setInvitationStatus((List<Invitation>) dcm.getData());
 			} else{
 				recent++;
 				//Send message to superclass as well
 				super.update(obs, obj);
 			}
 			if(recent == 2){
 				activity.hideProgressBar();
 				recent = 0;
				isUpdating = false;
 			}
 		}
 	}
 
 
 }
