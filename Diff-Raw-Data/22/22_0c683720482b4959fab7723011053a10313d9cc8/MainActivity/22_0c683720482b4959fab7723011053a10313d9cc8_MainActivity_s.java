 package sk.estesadohodneme.sturik;
 
 import sk.estesadohodneme.sturik.game.GameEngine;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Toast;
 
 /**
  * Shows {@link WelcomeFragment} for select game, handles button clicks and set
  * properties for desired game.
  */
 public class MainActivity extends FragmentActivity implements OnClickListener {
 
 	private GameEngine mGameEngine = null;
 
 	/**
 	 * Creates fragment for game, creates gameEngine and sets its parameters.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		mGameEngine = new GameEngine();
 
 		Fragment fragment = new WelcomeFragment();
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.replace(R.id.frame, fragment);
 		fragmentTransaction.commit();
 
 	}
 
 	/*
 	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
 	 * menu; this adds items to the action bar if it is present.
 	 * getMenuInflater().inflate(R.menu.activity_main, menu); return true; }
 	 */
 
 	/**
 	 * Handles button clicks for selection game on {@link WelcomeFragment}.
 	 */
 	@SuppressWarnings("deprecation")
	@Override
 	public void onClick(View v) {
 		/*
 		 * Sets parameters of game engine;
 		 */
 		switch (v.getId()) {
 		case R.id.button_snake:
 			mGameEngine.setGame(GameEngine.GAME_SNAKE);
 			mGameEngine.setImageGenerator(GameEngine.IMAGE_GENERATOR_SIMPLE);
 			break;
 		// case R.id.button_snake_mlyny:
 		// mGameEngine.setGame(GameEngine.GAME_SNAKE);
 		// mGameEngine.setImageGenerator(GameEngine.IMAGE_GENERATOR_MLYNY);
 		// break;
 		case R.id.button_tetris:
 			mGameEngine.setGame(GameEngine.GAME_TETRIS);
 			mGameEngine.setImageGenerator(GameEngine.IMAGE_GENERATOR_SIMPLE);
 			break;
 		// case R.id.button_tetris_mlyny:
 		// mGameEngine.setGame(GameEngine.GAME_TETRIS);
 		// mGameEngine.setImageGenerator(GameEngine.IMAGE_GENERATOR_MLYNY);
 		// break;
 		default:
 			Toast.makeText(this, R.string.unimplemented_game,
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		Display display = getWindowManager().getDefaultDisplay();
 		mGameEngine.setImageSize(display.getWidth(), display.getHeight());
 
 		Bundle args = new Bundle();
 		args.putSerializable(GameFragment.GAME_ENGINE, mGameEngine);
 		Fragment fragment = new GameFragment();
 		fragment.setArguments(args);
 
 		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
 				.beginTransaction();
 		fragmentTransaction.replace(R.id.frame, fragment);
 		fragmentTransaction.addToBackStack(null);
 		fragmentTransaction.commit();
 	}
 }
