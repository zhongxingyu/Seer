 package net.homelinux.paubox;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class AnnounceActivity extends BaseMenuActivity {
 
 
 	/************************
 	 **** CLASS VARIABLE **** 
 	 ************************/
 	TextView debug_text;
 	Spinner score_spinner;   
 
 	/**************************
	 **** PRIVATE METHODS *****
 	 **************************/
 	private void updateDebugText() {
 		// Since we're in the same package, we can use this context to get
 		// the default shared preferences
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		final String winning_score = sharedPref.getString("winning_score", "coucou");
 		Deal current_deal = current_game.currentDeal();
 		debug_text.setText("Current: " + current_deal.getBet() + " " + current_deal.getTrump() + " (max = " + winning_score + ")");
 	}
 	private int toTrumpInt(int id) {
 		switch (id) {
 		case (R.id.radio_heart) :
 			return Deal.TRUMP_HEART;
 		case (R.id.radio_diamond) :
 			return Deal.TRUMP_DIAMOND;
 		case (R.id.radio_club) :
 			return Deal.TRUMP_CLUB;
 		case (R.id.radio_spade) :
 			return Deal.TRUMP_SPADE;
 		case (R.id.radio_alltrump) :
 			return Deal.TRUMP_ALL_TRUMPS;
 		case (R.id.radio_notrump) :
 			return Deal.TRUMP_NO_TRUMP;
 		}
 		return -1;
 	}
 
 
 	/****************************
 	 **** PROTECTED METHODDS ****
 	 ****************************/
 	// Call the waiting activity
 	protected void launchWaitingActivity() {
 		Intent waiting_intent = new Intent(this, WaitingActivity.class);
 		waiting_intent.putExtra("net.homelinux.paubox.Game", current_game.currentDeal());
 		startActivityForResult(waiting_intent, REQUEST_CODE_WAITING);
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		// The preferences returned if the request code is what we had given
 		// earlier in startSubActivity
 		if (requestCode == REQUEST_CODE_PREFERENCES) {
 			// Read a sample value they have set
 			updateDebugText();
 		} else if (requestCode == REQUEST_CODE_WAITING) {
 			boolean won = data.getBooleanExtra("net.homelinux.paubox.won", false);
 			current_game.currentDeal().setWon(won);
 			current_game.newDeal();
 			Toast.makeText(getApplicationContext(), "The game was" + (won ? "won !" : "lost :("),
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	protected int BetFromItemId(long adapter_view_id) {
 		if (adapter_view_id == android.widget.AdapterView.INVALID_ROW_ID)
 			return Deal.MIN_BET;
 		else
 			return Deal.MIN_BET + (int)adapter_view_id * 10;
 	}
 	/*************************
 	 **** PUBLIC METHODDS ****
 	 *************************/
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		writeGame(current_game);
 	}
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.announce_layout);
 
 		final OnClickListener radio_listener = new OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on clicks
 				RadioButton rb = (RadioButton) v;
 				current_game.currentDeal().setTrump(toTrumpInt(rb.getId()));
 				updateDebugText();
 			}
 		};  
 
 		current_game = readGame();
 		if (current_game == null)
 			current_game = new Game();
 		final RadioButton radio0 = (RadioButton) findViewById(R.id.radio_heart);
 		final RadioButton radio1 = (RadioButton) findViewById(R.id.radio_diamond);
 		final RadioButton radio2 = (RadioButton) findViewById(R.id.radio_spade);
 		final RadioButton radio3 = (RadioButton) findViewById(R.id.radio_club);
 		final RadioButton radio4 = (RadioButton) findViewById(R.id.radio_alltrump);
 		final RadioButton radio5 = (RadioButton) findViewById(R.id.radio_notrump);
 		final Button button_go = (Button) findViewById(R.id.button_go);
 
 		final RadioButton radio_us = (RadioButton) findViewById(R.id.button_Us);
 		final RadioButton radio_them = (RadioButton) findViewById(R.id.button_Them);
 		radio_us.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				current_game.currentDeal().setTeam_betting(Game.Us);
 			}});
 		radio_them.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				current_game.currentDeal().setTeam_betting(Game.Them);
 			}});
 		debug_text = (TextView) findViewById(R.id.debug_text);
 		radio0.setOnClickListener(radio_listener);
 		radio1.setOnClickListener(radio_listener);
 		radio2.setOnClickListener(radio_listener);
 		radio3.setOnClickListener(radio_listener);
 		radio4.setOnClickListener(radio_listener);
 		radio5.setOnClickListener(radio_listener);
 
 		button_go.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// Perform action on clicks
 				launchWaitingActivity();
 			}
 		});
 		score_spinner = (Spinner) findViewById(R.id.spinner);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 				this, R.array.points, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		score_spinner.setAdapter(adapter);
 
 		score_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> parent, View view, int position, long i) {
 				current_game.currentDeal().setBet(BetFromItemId(parent.getSelectedItemId()));
 				updateDebugText();
 			}
 
 			public void onNothingSelected(AdapterView<?> parent) {
 				// We don't need to worry about nothing being selected, since Spinners don't allow
 				// this.
 			}
 		});
 	}
 
 	static private String FILENAME = "coinchoid_current_game.ser";
 	private void writeGame(Game game) {
 		try {
 			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(game);
 			fos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private Game readGame() {
 		Game game = null;
 		try {
 			FileInputStream fis = openFileInput(FILENAME);
 			ObjectInputStream ois = new ObjectInputStream(fis);
 			game = (Game) ois.readObject();
 			fis.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return game;
 	}
 }
 
