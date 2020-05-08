 package view;
 
 import java.util.ArrayList;
 
 import model.Player;
 import controller.GameController;
 import controller.PlayerController;
 import tdt4240.ordsplid.R;
 import android.app.Dialog;
 import android.content.Context;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ScoreDialog extends Dialog{
 	
 	public ScoreDialog(Context context) {
 		super(context);
 		final GameController gController = GameController.instance();
 		this.setContentView(R.layout.dialog_score_view);
 		ArrayList<Player> players = PlayerController.instance().getPlayersSortedByScore();
 		//ListView listView = (ListView)findViewById(R.id.dialog_score_list);
		//ArrayList<String> playerString = new ArrayList<String>();
 		
		/*
 		for (int i = 0; i < players.size(); i++) {
 			playerString.add(players.get(i).toString());
 		}
		*/
 		
 		TextView txtScore = (TextView)findViewById(R.id.scoreText);
 		String s = "";
 		for (int i = 0; i < players.size(); i++) {
			s += players.get(i).getPlayerInfo() + "\n";
 		}		
 		txtScore.setText(s);
 		
		//Removed flag in gamecontroller, and used this instead.
		setCanceledOnTouchOutside(false);
		
 		Button continueButton = (Button)findViewById(R.id.continue_button);
 		continueButton.setOnClickListener(new View.OnClickListener() {		
 			
 			public void onClick(View arg0) {
 				gController.nextTurn();
 				ScoreDialog.this.dismiss();
 			}
 		});
 	}
 }
