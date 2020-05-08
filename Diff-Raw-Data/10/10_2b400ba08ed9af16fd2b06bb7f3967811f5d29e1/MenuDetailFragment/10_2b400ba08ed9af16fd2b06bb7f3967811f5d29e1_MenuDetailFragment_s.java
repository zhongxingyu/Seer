 package uk.co.thomasc.wordmaster.view.menu;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.WindowManager.LayoutParams;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
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
 import uk.co.thomasc.wordmaster.view.DialogPanel;
 import uk.co.thomasc.wordmaster.view.Errors;
 import uk.co.thomasc.wordmaster.view.game.GameLayout;
 import uk.co.thomasc.wordmaster.view.game.SwipeController;
 import uk.co.thomasc.wordmaster.view.game.SwipeListener;
 
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
 	public void onDestroyView() {
 		super.onDestroyView();
 		if (game != null) {
 			game.removeTurnListener(this);
 		}
 		refresher.interrupt();
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
 
 	public void showKeyboard() {
 		((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input, 0);
 		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		final View rootView = inflater.inflate(R.layout.game_screen, container, false);
 
 		((BaseGame) getActivity()).menuDetail = this;
 
 		game = Game.getGame(gameid);
 		if (game != null) {
 
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
 
 			if (game.needsWord()) {
 				RelativeLayout footer = (RelativeLayout) rootView.findViewById(R.id.footer);
 				footer.getLayoutParams().height = BaseGame.convertDip2Pixels(getResources(), 70);
 			}
 
 			loadTurns();
 			game.addTurnListener(this);
 
 			Resources res = getActivity().getResources();
 			Drawable guessEnabled = res.getDrawable(R.drawable.guess);
 			Drawable guessDisabled = res.getDrawable(R.drawable.guess_disabled);
 
 			input = (EditText) rootView.findViewById(R.id.guess_input);
 			input.addTextChangedListener(new CapsLockLimiter(input, rootView, guessEnabled, guessDisabled));
 
 			rootView.clearFocus();
 
 			((ImageView) rootView.findViewById(R.id.guess_button)).setOnClickListener(new TurnMaker(game, (BaseGame) getActivity(), rootView, this));
 
 			((GameLayout) rootView.findViewById(R.id.screen_game)).setActivity(getActivity());
 
 			SwipeController swipe = new SwipeController((BaseGame) getActivity(), gameid);
 			ViewPager mPager = (ViewPager) rootView.findViewById(R.id.pager);
 			
 			mPager.setPageMargin(BaseGame.convertDip2Pixels(getResources(), 2));
 			mPager.setPageMarginDrawable(R.color.divider);
 			
 			if (((BaseGame) getActivity()).wideLayout) {
 				
 				mPager.setOnTouchListener(new OnTouchListener() {
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						return true;
 					}
 				});
 				rootView.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);
 			} else {
 				mPager.setOnPageChangeListener(new SwipeListener((ImageView) rootView.findViewById(R.id.indicator)));
 			}
 			
 			mPager.setAdapter(swipe);
 
 			refresher = new RefresherThread();
 			running = true;
 			refresher.start();
 
 			showKeyboard();
 		} else {
 
 			getActivity().getSupportFragmentManager().popBackStack("top", 0);
 
 		}
 
 		return rootView;
 	}
 
 	public void loadTurns() {
 		ServerAPI.getTurns(gameid, game.getPivotLatest(), (BaseGame) getActivity(), new GetTurnsRequestListener() {
 
 			@Override
 			public void onRequestFailed() {
 				Activity act = getActivity();
 				if (act != null) {
 					act.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							DialogPanel errorMessage = (DialogPanel) getActivity().findViewById(R.id.errorMessage);
 							errorMessage.show(Errors.NETWORK);
 						}
 					});
 				}
 			}
 
 			@Override
 			public void onRequestComplete(List<Turn> turns) {
 				ArrayList<Turn> gameTurns = game.getTurns();
 				for (Turn turn : turns) {
 					if (!gameTurns.contains(turn)) {
 						game.addTurn(turn);
 					}
 				}
 				Activity act = getActivity();
 				if (act != null) {
 					act.runOnUiThread(new Runnable() {
 						@Override
 						public void run() {
 							input.clearFocus();
 							input.requestFocus();
 							showKeyboard();
 						}
 					});
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
 	public void onTurnAdded(Turn turn, boolean newerTurn) {
 		updateTurnCount();
 
 		RelativeLayout footer = (RelativeLayout) getView().findViewById(R.id.footer);
 		footer.getLayoutParams().height = BaseGame.convertDip2Pixels(getResources(), game.needsWord() ? 70 : 50);
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
 				try {
 					Thread.sleep(30000);
 					loadTurns();
 				} catch (InterruptedException e) {
 					
 				}
 			}
 		}
 
 	}
 }
