 package no.jts.android.simple.gameapi.example;
 
import static android.view.ViewGroup.LayoutParams.FILL_PARENT;
 import no.jts.android.simple.gameapi.Setup;
 import no.jts.android.simple.gameapi.example.screens.GameScreen;
 import no.jts.android.simple.gameapi.example.screens.IntroScreen;
 import no.jts.android.simple.gameapi.example.screens.MenuScreen;
 import no.jts.android.simple.gameapi.example.setup.Assets;
 import no.jts.android.simple.gameapi.screenmanagement.AbstractScreenManager;
 import no.jts.android.simple.gameapi.screenmanagement.ScreenType;
 import android.app.Activity;
 import android.util.Log;
import android.widget.RelativeLayout;
 
 public class GameScreenManager extends AbstractScreenManager {
 
 	private static final String TAG = "GameScreenManager";  
 	
 	public GameScreenManager(Activity activity, Setup setup) {
 		super(activity, setup);
 		Log.i(TAG, "Displaying loading screen");
 		screens.put(ScreenType.INTRO, new IntroScreen(this));
 		setScreenInFocus(ScreenType.INTRO);
 	}
 
 	@Override
 	public void doInBackground(Activity activity) {
 		Log.i(TAG, "Loading game assets and screens");
 		Assets.init();
 		screens.put(ScreenType.MENU, new MenuScreen(this));
 		screens.put(ScreenType.GAME, new GameScreen(this));
 	}
 
 	@Override
 	public void onPostExecute() {
 		Log.i(TAG, "Loading game finished, set menu in focus");
 		setScreenInFocus(ScreenType.MENU);
 	}
 }
