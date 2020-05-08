 package com.example.wecharades.presenter;
 
 import java.util.ArrayList;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import android.view.KeyEvent;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.example.wecharades.R;
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Invitation;
 import com.example.wecharades.views.SearchPlayerActivity;
 import com.parse.ParseException;
 import com.parse.ParsePush;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class SearchPlayerPresenter extends Presenter {
 
 	private SearchPlayerActivity activity;
 
 	public SearchPlayerPresenter(SearchPlayerActivity activity) {
 		super(activity);
 		this.activity = (SearchPlayerActivity) activity;
 	}
 	
 	/**
 	 * Sets listener on Search box 
 	 * @param searchBox
 	 */
 	public void setListeners(EditText searchBox) {
 		searchBox.setOnEditorActionListener(new OnEditorActionListener() {
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 					activity.onClickSearch(v);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Update screen
 	 * @param s
 	 */
 	public void update(String s){
 		activity.showProgressBar();
 		performSearch(s);
 		activity.hideProgressBar();
 		
 	}
 	
 	/**
 	 * Search for a player
 	 * @param searchString
 	 */
 	private void performSearch(String searchString) {
 		try {
 
 			ListView view = (ListView) activity.findViewById(R.id.result_list);
 			TextView text = (TextView) activity.findViewById(R.id.empty_list_item);
 			text.setText("No results found!");
 			view.setEmptyView(text);
 
 			SortedSet<String> list = dc.getAllOtherPlayerNames().subSet(searchString, searchString + Character.MAX_VALUE);
 			
 			//Create a list of player who the player cannot send an invitation to:
 			TreeSet<String> alreadySent 	= new TreeSet<String>();
 			TreeSet<String> alreadyPlaying 	= new TreeSet<String>();
 			for(Invitation inv : dc.getSentInvitations()){
 				alreadySent.add(inv.getInvitee().getName());
 			}
 			for(Game g : dc.getGames()){
				if(!g.isFinished())
 				alreadyPlaying.add(g.getOpponent(dc.getCurrentPlayer()).getName());
 			}
 			
 			
 			ArrayList<String> resultList = new ArrayList<String>(list);
 			if (!list.isEmpty())
 				view.setAdapter(new SearchPlayerAdapter(activity, resultList, alreadySent, alreadyPlaying));
 		} catch (DatabaseException e) {
 			activity.showNegativeDialog("Error", e.prettyPrint(), "OK");
 		}
 	}
 	
 	/**
 	 * Send invitation to player and update database
 	 * @param invitee
 	 */
 	public void invite(String invitee) {
 		try {
 			dc.sendInvitation(dc.getPlayer(invitee));
 			sendNotificationtoOtherPlayer(invitee);
 		} catch (DatabaseException e){
 			activity.showNegativeDialog("Error", e.prettyPrint(), "OK");
 		}
 
 	}
 	
 	/**
 	 * Send notification about invitation
 	 * @param invitee
 	 */
 	private void sendNotificationtoOtherPlayer(String invitee) {
 		ParsePush push = new ParsePush();
 		System.out.println(invitee);
 		push.setChannel(invitee);
 		push.setMessage("Charade invitation from: " + dc.getCurrentPlayer().getName());
 		try {
 			push.send();
 		} catch (ParseException e) {
 			System.out.println("ParseException");
 			push.sendInBackground();
 		}
 		System.out.println("4");
 	}
 }
