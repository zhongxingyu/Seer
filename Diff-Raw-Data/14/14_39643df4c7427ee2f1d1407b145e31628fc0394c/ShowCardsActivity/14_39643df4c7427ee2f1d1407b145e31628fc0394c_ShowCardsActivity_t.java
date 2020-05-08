 package cs309.a1.player.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import cs309.a1.player.R;
 
 public class ShowCardsActivity extends Activity{
 
 	private static final int QUIT_GAME = "QUIT_GAME".hashCode();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.player_hand);
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, QuitGameActivity.class);
 		startActivityForResult(intent, QUIT_GAME);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QUIT_GAME) {
 			if (resultCode == RESULT_OK) {
				// Finish this activity - if everything goes right, we
				// should be back at the main menu
 				setResult(RESULT_OK);
 				finish();
 			}
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 }
