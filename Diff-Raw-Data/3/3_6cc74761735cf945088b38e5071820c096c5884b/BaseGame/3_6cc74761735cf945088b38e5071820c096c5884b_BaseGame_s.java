 package uk.co.thomasc.wordmaster;
 
 import java.util.HashMap;
 
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.os.Bundle;
 
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.User;
 import uk.co.thomasc.wordmaster.util.BaseGameActivity;
 import uk.co.thomasc.wordmaster.view.menu.MenuDetailFragment;
 import uk.co.thomasc.wordmaster.view.menu.MenuListFragment;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class BaseGame extends BaseGameActivity {
 
 	public static Typeface russo;
 
 	public MenuListFragment menuFragment;
 	public MenuDetailFragment menuDetail;
 	public boolean wideLayout = false;
 
 	public HashMap<String, Game> games = new HashMap<String, Game>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.empty_screen);
 		
 		int screenLayoutSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
 		wideLayout = screenLayoutSize > 2;
 		if (!wideLayout) {
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		menuFragment = new MenuListFragment();
 
 		BaseGame.russo = Typeface.createFromAsset(getAssets(), "fonts/Russo_One.ttf");
 
 		getSupportFragmentManager().beginTransaction().add(R.id.empty, menuFragment).commit();
 	}
 
 	@Override
 	public void onSignInFailed() {
 		System.out.println("oh noes!");
 	}
 
 	@Override
 	public void onSignInSucceeded() {
 		User.getUser(mHelper.getPlusClient().getCurrentPerson(), this); // Load local user into cache
 		menuFragment.onSignInSucceeded();
 	}
 
 	public Game gameForGameID(String gameID) {
 		return games.get(gameID);
 	}
 	
 	public void updateGame(String gameID, Game game) {
 		games.put(gameID, game);
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (menuDetail != null) {
 			menuDetail.hideKeyboard();
 			menuDetail = null;
 		}
 		super.onBackPressed();
 	}
 
 }
