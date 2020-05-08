 package no.ntnu.tdt4240.activities;
 
 import no.ntnu.tdt4240.R;
 import no.ntnu.tdt4240.models.Player;
 import no.ntnu.tdt4240.views.PlayerView;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class GameActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	    
 		// hide title
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // Hide notification bar
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         setContentView(R.layout.game);
         
        createPlayerViews();
         
         
 	}
 
 	private void createPlayerViews() {		
		Player player1 = new Player("Vegar");
		PlayerView view1 = (PlayerView)findViewById(R.id.playerView1);
		player1.setPlayerView(view1);
		Player player2 = new Player("Jonas");
		PlayerView view2 = (PlayerView)findViewById(R.id.playerView2);
		player2.setPlayerView(view2);
 	}
 	
 }
