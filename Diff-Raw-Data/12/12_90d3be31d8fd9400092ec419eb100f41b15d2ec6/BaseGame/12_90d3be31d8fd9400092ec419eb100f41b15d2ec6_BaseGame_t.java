 package uk.co.thomasc.wordmaster;
 
 import java.util.HashMap;
 
 import uk.co.thomasc.wordmaster.api.GetMatchesRequestListener;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.util.BaseGameActivity;
 import uk.co.thomasc.wordmaster.view.menu.MenuAdapter;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.common.SignInButton;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class BaseGame extends BaseGameActivity implements OnClickListener, GetMatchesRequestListener {
 	
 	public static Typeface russo;
 	public MenuAdapter adapter;
 	
 	public static HashMap<String, Game> games = new HashMap<String, Game>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		adapter = new MenuAdapter(this);
 		
 		russo = Typeface.createFromAsset(getAssets(), "fonts/Russo_One.ttf");
 
 		setContentView(R.layout.menu_screen);
 		
 		SignInButton button = (SignInButton) findViewById(R.id.button_sign_in);
 		button.setSize(SignInButton.SIZE_WIDE); // I commend anyone who can do this in XML
		
		int services = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (services == ConnectionResult.SUCCESS) {
			button.setOnClickListener(this);
		} else {
			GooglePlayServicesUtil.getErrorDialog(services, this, 1);
		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.button_sign_in) {
 			beginUserInitiatedSignIn();
 		} else if (v.getId() == R.id.refresh) {
 			findViewById(R.id.refresh).setVisibility(View.GONE);
 			findViewById(R.id.refresh_progress).setVisibility(View.VISIBLE);
 			loadGames();
 		}
 	}
 	
 	@Override
 	public void onSignInFailed() {
 		System.out.println("oh noes!");
 	}
 	
 	@Override
 	public void onSignInSucceeded() {
 		findViewById(R.id.button_sign_in).setVisibility(View.GONE);
 		findViewById(R.id.whysignin).setVisibility(View.GONE);
 		findViewById(R.id.main_feed).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1F));
 		
 		// Enable UI
 		findViewById(R.id.refresh).setOnClickListener(this);
 		
 		loadGames();
 		
 		//signOut();
 	}
 	
 	private void loadGames() {
 		games.clear();
 		ServerAPI.getMatches(getUserId(), this, this);
 	}
 	
 	public static Game gameForGameID(String gameID) {
 		return games.get(gameID);
 	}
 
 	@Override
 	public void onRequestComplete(final Game[] games) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				adapter.clear();
 				for (Game game : games) {
 					BaseGame.games.put(game.getID(), game);
 					adapter.add(game);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onRequestFailed() {
 		// TODO Tell the user their parents have been murdered		
 	}
 	
 }
