 package sk.estesadohodneme.sturik;
 
 import sk.estesadohodneme.sturik.game.GameEngine;
 import sk.estesadohodneme.sturik.game.OnGameBoardChangedListener;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.RelativeLayout;
 
 /**
  * Creates fragment for game.
  */
 public class GameFragment extends Fragment implements
 		OnGameBoardChangedListener {
 
 	public static final String GAME_ENGINE = "game engine";
 
 	private GameEngine mGameEngine = null;
 	private Handler mHandler = new Handler();
 	private Runnable mUpdateGameBoard;
 	private View mView;
 
 	@Override
 	public void setArguments(Bundle args) {
 		mGameEngine = (GameEngine) args.getSerializable(GAME_ENGINE);
 	}
 
 	/**
 	 * Returns view for {@link GameFragment}.
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mView = inflater.inflate(R.layout.game, container, false);
 
 		int[][] actions = { { R.id.button_left, GameEngine.ACTION_LEFT },
 				{ R.id.button_right, GameEngine.ACTION_RIGHT },
 				{ R.id.button_down, GameEngine.ACTION_DOWN },
 				{ R.id.button_up, GameEngine.ACTION_UP }, };
 		for (int[] action : actions) {
 			int buttonId = action[0];
 			final int userAction = action[1];
 			ImageButton button = (ImageButton) mView.findViewById(buttonId);
 			button.setOnClickListener(new OnClickListener() {
 
				// @Override
 				public void onClick(View v) {
 					mGameEngine.setUserAction(userAction);
 				}
 			});
 		}
 
 		mUpdateGameBoard = new Runnable() {
 
 			@SuppressWarnings("deprecation")
			// @Override
 			public void run() {
 				RelativeLayout gameBoard = (RelativeLayout) mView
 						.findViewById(R.id.game_board);
 				Bitmap bitmap = mGameEngine.getGameBoard();
 				Drawable background = new BitmapDrawable(getResources(), bitmap);
 				gameBoard.setBackgroundDrawable(background);
 			}
 		};
 
 		return mView;
 	}
 
 	/**
 	 * Starts game on resume.
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		mGameEngine.setOnGameBoardChangeListener(this);
 		mGameEngine.start();
 		Log.d("STURIK", "GameFragment start");
 	}
 
 	/**
 	 * Stops game on pause.
 	 */
 	@Override
 	public void onPause() {
 		Log.d("STURIK", "GameFragment stop");
 		mHandler.removeCallbacks(mUpdateGameBoard);
 		mGameEngine.stop();
 		super.onPause();
 	}
 
 	/**
 	 * Updates game board.
 	 */
	// @Override
 	public void onGameBoardChanged() {
 		mHandler.post(mUpdateGameBoard);
 	}
 }
