 package uk.co.thomasc.wordmaster.view.menu;
 
 import java.util.ArrayList;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.api.GetTurnsRequestListener;
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.api.TakeTurnSpinnerListener;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.callbacks.ImageLoadedListener;
 import uk.co.thomasc.wordmaster.objects.callbacks.TurnAddedListener;
 import uk.co.thomasc.wordmaster.util.CapsLockLimiter;
 import uk.co.thomasc.wordmaster.util.TurnMaker;
 import uk.co.thomasc.wordmaster.view.game.GameLayout;
 import uk.co.thomasc.wordmaster.view.game.SwipeController;
 import uk.co.thomasc.wordmaster.view.game.SwipeListener;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MenuDetailFragment extends Fragment implements TurnAddedListener, TakeTurnSpinnerListener {
 
 	public static final String ARG_ITEM_ID = "gameid";
 	private Game game;
 	private String gameid;
 	private EditText input;
 	private RefresherThread refresher;
 	private boolean running = true;
 
 	public MenuDetailFragment() {
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (getArguments().containsKey(MenuDetailFragment.ARG_ITEM_ID)) {
 			gameid = getArguments().getString(MenuDetailFragment.ARG_ITEM_ID);
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		game.removeTurnListener(this);
 		running = false;
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input, InputMethodManager.SHOW_FORCED);
 		getActivity().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 	}
 
 	public void hideKeyboard() {
 		((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(), 0);
 		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.game_screen, container, false);
 
 		game = Game.getGame(gameid);
 		
 		game.getPlayer().listenForImage(new ImageLoadedListener() {
 			@Override
 			public void onImageLoaded(final Drawable image) {
 				getActivity().runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						((ImageView) rootView.findViewById(R.id.playerAvatar)).setImageDrawable(image);
 					}
 				});
 			}
 		});
 		game.getOpponent().listenForImage(new ImageLoadedListener() {
 			@Override
 			public void onImageLoaded(final Drawable image) {
 				getActivity().runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						((ImageView) rootView.findViewById(R.id.oppAvatar)).setImageDrawable(image);
 					}
 				});
 			}
 		});
 		
 		((TextView) rootView.findViewById(R.id.turn)).setText(Integer.toString(game.getTurnNumber()));
 		((TextView) rootView.findViewById(R.id.playerscore)).setText(Integer.toString(game.getPlayerScore()));
 		((TextView) rootView.findViewById(R.id.oppscore)).setText(Integer.toString(game.getOpponentScore()));
 		
 		loadTurns();
 		game.addTurnListener(this);
 		
 		Resources res = getActivity().getResources();
 		Drawable guessEnabled = res.getDrawable(R.drawable.guess);
 		Drawable guessDisabled = res.getDrawable(R.drawable.guess_disabled);
 
		input = (EditText) rootView.findViewById(R.id.editText1);
 		input.addTextChangedListener(new CapsLockLimiter(input, rootView, guessEnabled, guessDisabled));
 		
 		((ImageView) rootView.findViewById(R.id.guess_button)).setOnClickListener(new TurnMaker(game, (BaseGame) getActivity(), rootView, this));
 
 		((GameLayout) rootView.findViewById(R.id.screen_game)).setActivity(getActivity());
 
 		SwipeController swipe = new SwipeController(getActivity().getSupportFragmentManager(), gameid);
 		ViewPager mPager = (ViewPager) rootView.findViewById(R.id.pager);
 		mPager.setAdapter(swipe);
 		mPager.setOnPageChangeListener(new SwipeListener((ImageView) rootView.findViewById(R.id.indicator)));
 
 		refresher = new RefresherThread();
 		running = true;
 		refresher.start();
 		
 		return rootView;
 	}
 	
 	
 	
 	private void loadTurns() {
 		ServerAPI.getTurns(gameid, (BaseGame) getActivity(), new GetTurnsRequestListener() {
 			
 			@Override
 			public void onRequestFailed() {
 				// TODO: Tell the user the tooth fairy isn't real
 			}
 			
 			@Override
 			public void onRequestComplete(Turn[] turns) {
 				ArrayList<Turn> gameTurns = game.getTurns();
 				for (Turn turn : turns) {
 					boolean found = false;
 					for (Turn t : gameTurns) {
 						if (t.getID() == turn.getID()) {
 							found = true;
 							break;
 						}
 					}
 					if (!found) {
 						game.addTurn(turn);
 					}
 				}
 			}
 		});
 	}
 	
 	private void updateTurnCount() {
 		BaseGame act = ((BaseGame) getActivity());
 		act.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				if (getView() != null) {
 					((TextView) getView().findViewById(R.id.turn)).setText(Integer.toString(game.getTurnNumber()));
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onTurnAdded(Turn turn) {
 		updateTurnCount();
 	}
 
 	@Override
 	public void startSpinner() {
 		getView().findViewById(R.id.turn_progress).setVisibility(View.VISIBLE);
 		getView().findViewById(R.id.guess_button).setVisibility(View.GONE);
 	}
 
 	@Override
 	public void stopSpinner() {
 		getActivity().runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				getView().findViewById(R.id.guess_button).setVisibility(View.VISIBLE);
 				getView().findViewById(R.id.turn_progress).setVisibility(View.GONE);
 			}
 		});
 	}
 	
 	private class RefresherThread extends Thread {
 		
 		@Override
 		public void run() {
 			while (running) {
 				loadTurns();
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 }
