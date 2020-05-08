 package com.hdm_i.android.kicker2go.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.hdm_i.android.kicker2go.AppContext;
 import com.hdm_i.android.kicker2go.R;
 import com.hdm_i.android.kicker2go.Utilities.Toasts;
 import com.hdm_i.android.kicker2go.adapter.PlayerSelectionAdapter;
 import com.hdm_i.android.kicker2go.model.Game;
 import com.hdm_i.android.kicker2go.model.Player;
 import com.hdm_i.android.kicker2go.model.Round;
 import com.hdm_i.android.kicker2go.model.Team;
 
 public class GameSettingActivity extends GenericActivity implements
 		OnItemClickListener {
 
 	private HashMap<Long, Player> players = new HashMap<Long, Player>();
 	private PlayerSelectionAdapter adapter;
 	private ListView playerSelectorList;
 	private Dialog dialog;
 	private Game game;
 	private List<Round> listOfRound;
 	private Team teamA, teamB;
 	private Round round;
 	private int currentTextView;
 	private TextView teamAPlayerOne, teamAPlayerTwo, teamBPlayerOne,
 			teamBPlayerTwo;
 	private Handler handler = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.quick_start_layout);
 		setRequestedOrientation(AppContext.APP_ORIENTATION);
 		initilizeView();
 
 	}
 
 	@Override
 	public void initilizeView() {
 		this.handler = new Handler();
 		registerBaseActivityReceiver();
 		createGameModel();
 		players.putAll(AppContext.players);
 
 		if (players.size() < 4) {
 			Toasts.longToast(this, R.string.not_enough_players);
 			finish();
 			return;
 		}
 
 		dialog = new Dialog(this, R.style.MyDialog);
 		dialog.setContentView(R.layout.players_dialog_list_layout);
 		dialog.setCanceledOnTouchOutside(false);
 		playerSelectorList = (ListView) dialog
 				.findViewById(R.id.player_dialog_list);
 
 		teamAPlayerOne = (TextView) findViewById(R.id.team_a_player_one);
 		teamAPlayerTwo = (TextView) findViewById(R.id.team_a_player_two);
 		teamBPlayerOne = (TextView) findViewById(R.id.team_b_player_one);
 		teamBPlayerTwo = (TextView) findViewById(R.id.team_b_player_two);
 
 		adapter = new PlayerSelectionAdapter(this, players);
 		playerSelectorList.setAdapter(adapter);
 
 		playerSelectorList.setOnItemClickListener(this);
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
 
 	public void onResetButtonClicked(View view) {
 		players.clear();
 		players.putAll(AppContext.players);
 
 		GameSettingActivity.this.adapter = new PlayerSelectionAdapter(
 				GameSettingActivity.this, players);
 
 		playerSelectorList.setAdapter(GameSettingActivity.this.adapter);
 		teamAPlayerOne.setText(this.getString(R.string.default_select));
 		teamAPlayerTwo.setText(this.getString(R.string.default_select));
 		teamBPlayerOne.setText(this.getString(R.string.default_select));
 		teamBPlayerTwo.setText(this.getString(R.string.default_select));
 
 	}
 
 	public void onNextButtonClicked(View view) {
 		AppContext.currentGame = game;
 
 		finish();
 		startActivity(new Intent(
 				GameSettingActivity.this.getApplicationContext(),
 				GameModeSettingActivity.class));
 		overridePendingTransition(R.anim.slide_left, R.anim.slide_left);
 	}
 
 	public void onSelectPlayerClicked(View view) {
		if (players.size() > 0) {
			currentTextView = view.getId();
			dialog.show();
		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View view, int position,
 			long id) {
 
 		Player player = (Player) adapter.getItemAtPosition(position);
 		players.remove(player.player_id);
 		GameSettingActivity.this.adapter = new PlayerSelectionAdapter(
 				GameSettingActivity.this, players);
 		playerSelectorList.setAdapter(GameSettingActivity.this.adapter);
 
 		switch (currentTextView) {
 		case R.id.team_a_player_one: {
 			teamA.striker = player;
 			setTextViewText(teamAPlayerOne, player.toString());
 		}
 			break;
 		case R.id.team_a_player_two: {
 			teamA.defender = player;
 			setTextViewText(teamAPlayerTwo, player.toString());
 		}
 			break;
 		case R.id.team_b_player_one: {
 			teamB.striker = player;
 			setTextViewText(teamBPlayerOne, player.toString());
 		}
 			break;
 		case R.id.team_b_player_two: {
 			teamB.defender = player;
 			setTextViewText(teamBPlayerTwo, player.toString());
 		}
 			break;
 
 		}
 
 		dialog.dismiss();
 
 	}
 
 	private void setTextViewText(final TextView textView, final String text) {
 		handler.post(new Runnable() {
 			public void run() {
				textView.setText("");
 			}
 		});
 	}
 
 	private void createGameModel() {
 		game = new Game();
 		listOfRound = new ArrayList<Round>();
 		List<Long> score = new ArrayList<Long>();
 		round = new Round();
 		teamA = new Team();
 		teamB = new Team();
 		round.team_a = teamA;
 		round.team_b = teamB;
 		round.player_scored = score;
 		listOfRound.add(round);
 		game.rounds = listOfRound;
 	}
 }
