 package com.hdm_i.android.kicker2go.activities;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.TextView;
 
 import com.hdm_i.android.kicker2go.AppContext;
 import com.hdm_i.android.kicker2go.R;
 import com.hdm_i.android.kicker2go.Utilities.Logs;
 import com.hdm_i.android.kicker2go.adapter.ResultsListAdapter;
 import com.hdm_i.android.kicker2go.model.Game;
 
 public class GameResultsActivity extends GenericExpendableListActivity {
 
 	private ResultsListAdapter adapter;
 	private ExpandableListView listview;
 	private Game game;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.game_results_layout);
 		setRequestedOrientation(AppContext.APP_ORIENTATION);
 		initilizeView();
 
 	}
 
 	@Override
 	public void initilizeView() {
 		registerBaseActivityReceiver();
 		TextView title = (TextView) findViewById(R.id.viewTitle);
 		title.setText(R.string.game_results_title);
 
 		listview = (ExpandableListView) findViewById(android.R.id.list);
 		game = AppContext.currentGame;
 		
 		adapter = new ResultsListAdapter(this, getGroup(),getChild());
 		listview.setAdapter(adapter);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unRegisterBaseActivityReceiver();
 	}
 
 	@Override
 	public void onBackPressed() {
 		finish();
 		overridePendingTransition(R.anim.slide_right, R.anim.slide_right);
 	}
 
 	public void onUpdateGameClicked(View view) {
 
 	}
 
 	public String[] getGroup() {
 
 		String[] rounds = new String[game.rounds.size()];
 		int teamAScore = 0, teamBScore = 0;
 
 		for (int i = 0; i < game.rounds.size(); i++) {
 			teamAScore = 0;
 			teamBScore = 0;
 
 			for (int j = 0; j < game.rounds.get(i).player_scored.size(); j++) {
 				if (game.rounds.get(i).player_scored.get(j) == game.rounds
 						.get(i).team_a.striker.player_id
 						|| game.rounds.get(i).player_scored.get(j) == game.rounds
 								.get(i).team_a.defender.player_id) {
 					teamAScore++;
 				} else {
 					teamBScore++;
 				}
 
 				rounds[i] = "Team A  " + teamAScore + " : " + teamBScore
 						+ "  Team B";
 			}
 		}
 
 		return rounds;
 	}
 
 	public String[][] getChild() {
 		String[][] children = new String[game.rounds.size()][game.rounds.get(0).player_scored.size()];
 		
 		for (int i = 0; i < game.rounds.size(); i++) {
 			
 			for (int j = 0; j < game.rounds.get(i).player_scored.size(); j++) {
 				
 				if (game.rounds.get(i).player_scored.get(j) == game.rounds
 						.get(i).team_a.striker.player_id) {
					children[i][j] = j + ". " + game.rounds.get(i).team_a.striker
 							.toString();
 				} else if (game.rounds.get(i).player_scored.get(j) == game.rounds
 						.get(i).team_a.defender.player_id) {
					children[i][j] = j + ". " + game.rounds.get(i).team_a.defender
 							.toString();
 				} else if (game.rounds.get(i).player_scored.get(j) == game.rounds
 						.get(i).team_b.striker.player_id) {
					children[i][j] = j + ". " + game.rounds.get(i).team_b.striker
 							.toString();
 				} else {
					children[i][j] = j + ". " + game.rounds.get(i).team_b.defender
 							.toString();
 				}
 
 			}
 
 		}
 
 		return children;
 	}
 
 }
