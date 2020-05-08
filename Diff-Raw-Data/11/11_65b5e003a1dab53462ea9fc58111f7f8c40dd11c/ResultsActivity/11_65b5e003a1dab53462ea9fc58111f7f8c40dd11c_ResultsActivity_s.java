 package fa.darts;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import fa.darts.R;
 
 import fa.darts.data.DartsData;
 import fa.darts.data.Player;
 
 public class ResultsActivity extends Activity {
 
	private int results = 0;
 	private Player currentPlayer;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_results);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_results, menu);
 		return true;
 	}
 
 	public void computeResults(View view) {
 		Intent intent = getIntent();
 		int current = intent.getIntExtra(ScoresActivity.CURRENT_PLAYER, 0);
 
 		currentPlayer = DartsData.getPlayer(current - 1);
 
 		EditText arrow1Score = (EditText) findViewById(R.id.arrow1);
 		EditText arrow2Score = (EditText) findViewById(R.id.arrow2);
 		EditText arrow3Score = (EditText) findViewById(R.id.arrow3);
 		try {
 			results += Integer.valueOf(arrow1Score.getText().toString());
 		} catch (NumberFormatException nfe) {
 		}
 		try {
 			results += Integer.valueOf(arrow2Score.getText().toString());
 		} catch (NumberFormatException nfe) {
 		}
 		try {
 			results += Integer.valueOf(arrow3Score.getText().toString());
 		} catch (NumberFormatException nfe) {
 		}
 
 		TextView resultsView = (TextView) findViewById(R.id.results);
 		resultsView.setText(Integer.valueOf(results).toString());
 	}
 
 	public void validateResults(View view) {
 		if (currentPlayer.isUpdatePossible(results)) {
 			currentPlayer.updateScore(results);
 
 			if (currentPlayer.getScore() == 0) {
 				final Intent intentForDialog = new Intent(this, ScoresActivity.class);
 
 				new AlertDialog.Builder(this).setMessage("Gagné !!!")
 						.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int which) {
 								startActivity(intentForDialog);
 							}
 						}).show();
 			}
 
 			Intent intent = new Intent(this, ScoresActivity.class);
 			startActivity(intent);
 		} else {
 			final Intent intentForDialog = new Intent(this, ScoresActivity.class);
 
 			new AlertDialog.Builder(this).setMessage("Tour invalide !")
 					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							startActivity(intentForDialog);
 						}
 					}).show();
 
 		}
 	}
 
 	public void showScores(View view) {
 		Intent intent = new Intent(this, ScoresActivity.class);
 		startActivity(intent);
 	}
 
 }
