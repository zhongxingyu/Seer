 package com.oakonell.chaotictactoe.ui.game;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActivityCompat;
 import android.support.v4.app.FragmentActivity;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationSet;
 import android.view.animation.AnticipateInterpolator;
 import android.view.animation.Interpolator;
 import android.view.animation.ScaleAnimation;
 import android.view.animation.TranslateAnimation;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.common.images.ImageManager;
 import com.google.android.gms.games.multiplayer.Participant;
 import com.oakonell.chaotictactoe.Achievements;
 import com.oakonell.chaotictactoe.ChaoTicTacToe;
 import com.oakonell.chaotictactoe.Leaderboards;
 import com.oakonell.chaotictactoe.MainActivity;
 import com.oakonell.chaotictactoe.R;
 import com.oakonell.chaotictactoe.RoomListener;
 import com.oakonell.chaotictactoe.Sounds;
 import com.oakonell.chaotictactoe.googleapi.GameHelper;
 import com.oakonell.chaotictactoe.model.Cell;
 import com.oakonell.chaotictactoe.model.Game;
 import com.oakonell.chaotictactoe.model.GameMode;
 import com.oakonell.chaotictactoe.model.InvalidMoveException;
 import com.oakonell.chaotictactoe.model.Marker;
 import com.oakonell.chaotictactoe.model.MarkerChance;
 import com.oakonell.chaotictactoe.model.Player;
 import com.oakonell.chaotictactoe.model.PlayerStrategy;
 import com.oakonell.chaotictactoe.model.ScoreCard;
 import com.oakonell.chaotictactoe.model.State;
 import com.oakonell.chaotictactoe.settings.SettingsActivity;
 import com.oakonell.chaotictactoe.utils.DevelopmentUtil.Info;
 import com.oakonell.utils.StringUtils;
 import com.oakonell.utils.Utils;
 
 public class GameFragment extends SherlockFragment {
 	private static final int NON_HUMAN_OPPONENT_HIGHLIGHT_MOVE_PAUSE_MS = 300;
 	private static final int MARKER_ROLL_VISIBILITY_PAUSE = 150;
 	private static final int OPPONENT_MARKER_VISIBILITY_PAUSE_MS = 450;
 	
 	private ImageManager imgManager;
 
 	private ImageView markerToPlayView;
 	private View xHeaderLayout;
 	private View oHeaderLayout;
 	private TextView xWins;
 	private TextView oWins;
 	private TextView draws;
 
 	private TextView gameNumber;
 	private TextView numMoves;
 
 	private List<ImageButton> buttons = new ArrayList<ImageButton>();
 	private WinOverlayView winOverlayView;
 
 	private Game game;
 	private ScoreCard score;
 
 	private List<ChatMessage> messages = new ArrayList<ChatMessage>();
 	private int numNewMessages;
 
 	private ChatDialogFragment chatDialog;
 	private MenuItem chatMenuItem;
 
 	private View thinking;
 	private TextView thinkingText;
 
 	boolean exitOnResume;
 
 	@Override
 	public void onPause() {
 		exitOnResume = game.getMode() == GameMode.ONLINE;
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (exitOnResume) {
 			(new AlertDialog.Builder(getMainActivity()))
 					.setMessage(R.string.you_left_the_game)
 					.setNeutralButton(android.R.string.ok,
 							new OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									leaveGame();
 									dialog.dismiss();
 								}
 							}).create().show();
 		}
 		final FragmentActivity activity = getActivity();
 		// adjust the width or height to make sure the board is a square
 		activity.findViewById(R.id.grid_container).getViewTreeObserver()
 				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
 					@Override
 					public void onGlobalLayout() {
 						View squareView = activity
 								.findViewById(R.id.grid_container);
 						if (squareView == null) {
 							// We get this when we are leaving the game?
 							return;
 						}
 						LayoutParams layout = squareView.getLayoutParams();
 						int min = Math.min(squareView.getWidth(),
 								squareView.getHeight());
 						layout.height = min;
 						layout.width = min;
 						squareView.setLayoutParams(layout);
 //						squareView.getViewTreeObserver()
 //								.removeGlobalOnLayoutListener(this);
 
 						LayoutParams params = winOverlayView.getLayoutParams();
 						params.height = layout.height;
 						params.width = layout.width;
 						winOverlayView.setLayoutParams(params);
 					}
 				});
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.game, menu);
 		chatMenuItem = menu.findItem(R.id.action_chat);
 		handleMenu();
 	}
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		chatMenuItem = menu.findItem(R.id.action_chat);
 		handleMenu();
 	}
 
 	private void invalidateMenu() {
 		if (!ActivityCompat.invalidateOptionsMenu(getActivity())) {
 			handleMenu();
 		} else {
 			honeyCombInvalidateMenu();
 		}
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void honeyCombInvalidateMenu() {
 		getActivity().invalidateOptionsMenu();
 	}
 
 	private void handleMenu() {
 		if (chatMenuItem == null) return;
		boolean isOnline = getMainActivity().getRoomListener() != null;
		chatMenuItem.setVisible(isOnline);
		if (!isOnline) {
			return;
		}
 		RelativeLayout actionView = (RelativeLayout) chatMenuItem
 				.getActionView();
 		actionView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				openChatDialog();
 			}
 		});
 		TextView chatMenuItemTextView = (TextView) actionView
 				.findViewById(R.id.actionbar_notifcation_textview);
 		ImageView chatMenuItemImageView = (ImageView) actionView
 				.findViewById(R.id.actionbar_notifcation_imageview);
 		View progressView = actionView
 				.findViewById(R.id.actionbar_notifcation_progress);
 
 		chatMenuItemImageView.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				openChatDialog();
 			}
 		});
 		progressView.setVisibility(opponentInChat ? View.VISIBLE
 				: View.INVISIBLE);
 		if (numNewMessages > 0) {
 			chatMenuItemTextView.setText("" + numNewMessages);
 			chatMenuItemImageView
 					.setImageResource(R.drawable.message_available_icon_1332);
 
 			StringUtils.applyFlashEnlargeAnimation(chatMenuItemTextView);
 		} else {
 			chatMenuItemImageView
 					.setImageResource(R.drawable.message_icon_27709);
 			chatMenuItemTextView.setText("");
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_chat:
 			openChatDialog();
 			break;
 
 		case R.id.action_settings:
 			if (game.getMode() == GameMode.ONLINE) {
 
 				// show an abbreviated "settings"- notably the sound fx and
 				// other immediate game play settings
 				OnlineSettingsDialogFragment onlineSettingsFragment = new OnlineSettingsDialogFragment();
 				onlineSettingsFragment.show(getChildFragmentManager(),
 						"settings");
 				return true;
 			}
 			// create special intent
 			Intent prefIntent = new Intent(getActivity(),
 					SettingsActivity.class);
 
 			GameHelper helper = getMainActivity().getGameHelper();
 			Info info = null;
 			ChaoTicTacToe app = (ChaoTicTacToe) getActivity().getApplication();
 			if (helper.isSignedIn()) {
 				info = new Info(helper);
 			}
 			app.setDevelopInfo(info);
 			// ugh.. does going to preferences leave the room!?
 			getActivity().startActivityForResult(prefIntent,
 					MainActivity.RC_UNUSED);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void openChatDialog() {
 		getMainActivity().getRoomListener().sendInChat(true);
 		chatDialog = new ChatDialogFragment();
 		// TODO get the opponent name in nice fashion- ie, 'Anonymous'
 		// instead of google+'s anonymized string
 		chatDialog.initialize(this, messages, getMainActivity()
 				.getRoomListener().getMe(), getMainActivity().getRoomListener()
 				.getOpponentName());
 		chatDialog.show(getChildFragmentManager(), "chat");
 	}
 
 	public void startGame(Game game, ScoreCard score) {
 		this.score = score;
 		this.game = game;
 		configureNonLocalProgresses();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_game, container, false);
 		view.setKeepScreenOn(game.getMode() == GameMode.ONLINE);
 
 		invalidateMenu();
 		setHasOptionsMenu(true);
 		thinkingText = (TextView) view.findViewById(R.id.thinking_text);
 		if (game.getMode() != GameMode.PASS_N_PLAY) {
 			thinkingText.setText(getResources().getString(
 					R.string.opponent_is_thinking,
 					game.getNonLocalPlayer().getName()));
 		}
 		thinking = view.findViewById(R.id.thinking);
 		configureNonLocalProgresses();
 
 		imgManager = ImageManager.create(getMainActivity());
 
 		TextView xName = (TextView) view.findViewById(R.id.xName);
 		xName.setText(game.getXPlayer().getName());
 		TextView oName = (TextView) view.findViewById(R.id.oName);
 		oName.setText(game.getOPlayer().getName());
 
 		ImageView xImage = (ImageView) view.findViewById(R.id.x_back);
 		ImageView oImage = (ImageView) view.findViewById(R.id.o_back);
 
 		
 		game.getXPlayer().updatePlayerImage(imgManager, xImage);
 		game.getOPlayer().updatePlayerImage(imgManager, oImage);
 		
 		xHeaderLayout = view.findViewById(R.id.x_name_layout);
 		oHeaderLayout = view.findViewById(R.id.o_name_layout);
 
 		markerToPlayView = (ImageView) view.findViewById(R.id.marker_to_play);
 		winOverlayView = (WinOverlayView) view.findViewById(R.id.win_overlay);
 
 		ImageButton button = (ImageButton) view.findViewById(R.id.button_r1c1);
 		button.setOnClickListener(new ButtonPressListener(new Cell(0, 0)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r1c2);
 		button.setOnClickListener(new ButtonPressListener(new Cell(0, 1)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r1c3);
 		button.setOnClickListener(new ButtonPressListener(new Cell(0, 2)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r2c1);
 		button.setOnClickListener(new ButtonPressListener(new Cell(1, 0)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r2c2);
 		button.setOnClickListener(new ButtonPressListener(new Cell(1, 1)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r2c3);
 		button.setOnClickListener(new ButtonPressListener(new Cell(1, 2)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r3c1);
 		button.setOnClickListener(new ButtonPressListener(new Cell(2, 0)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r3c2);
 		button.setOnClickListener(new ButtonPressListener(new Cell(2, 1)));
 		buttons.add(button);
 
 		button = (ImageButton) view.findViewById(R.id.button_r3c3);
 		button.setOnClickListener(new ButtonPressListener(new Cell(2, 2)));
 		buttons.add(button);
 
 		xWins = (TextView) view.findViewById(R.id.num_x_wins);
 		oWins = (TextView) view.findViewById(R.id.num_o_wins);
 		draws = (TextView) view.findViewById(R.id.num_draws);
 
 		gameNumber = (TextView) view.findViewById(R.id.game_number);
 		gameNumber.setText("" + score.getTotalGames());
 
 		numMoves = (TextView) view.findViewById(R.id.num_moves);
 		if (game.getMarkerChance().isNormal()
 				|| game.getMarkerChance().isReverse()) {
 			numMoves.setVisibility(View.GONE);
 			view.findViewById(R.id.num_moves_lbl).setVisibility(View.GONE);
 		}
 
 		TextView gameMode = (TextView) view.findViewById(R.id.game_mode);
 		gameMode.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showGameHelp();
 			}
 		});
 		String gameType = game.getMarkerChance().getLabel(getActivity());
 		gameMode.setText(gameType);
 
 		View num_games_container = view.findViewById(R.id.num_games_container);
 		num_games_container.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showGameStats();
 			}
 		});
 
 		if (savedInstanceState != null) {
 			// reanimate the marker roll on restore?
 			updateHeader(true);
 		} else {
 			showGameHelpOnStart();
 		}
 
 		return view;
 	}
 
 	private void showGameHelpOnStart() {
 		// Always show the game help for custom
 		// otherwise, show it the first time for other game types
 		boolean showHelp = true;
 		RoomListener listener = getMainActivity().getRoomListener();
 		boolean doesntKnowChance = listener != null && !listener.isInitiatedTheGame();
 		if (!game.getMarkerChance().isCustom() ) {
 			SharedPreferences sharedPrefs = PreferenceManager
 					.getDefaultSharedPreferences(getSherlockActivity());
 			String key = "help_shown_for_" + game.getMarkerChance().type();
 			showHelp = !sharedPrefs.getBoolean(key, false);
 
 			Editor edit = sharedPrefs.edit();
 			edit.putBoolean(key, true);
 			edit.commit();
 		}
 		if (showHelp || doesntKnowChance) {
 			showGameHelp();
 		} else {
 			updateHeader(true);
 		}
 	}
 
 	private void showGameStats() {
 		GameStatDialogFragment dialog = new GameStatDialogFragment();
 		dialog.initialize(this, game, score);
 		dialog.show(getChildFragmentManager(), "stats");
 	}
 
 	private void showGameHelp() {
 		if (game.getMarkerChance().isNormal()
 				|| game.getMarkerChance().isReverse()) {
 			SimpleGameDescrDialogFragment helpDialog = new SimpleGameDescrDialogFragment();
 			helpDialog.initialize(this, game);
 			helpDialog.show(getChildFragmentManager(), "help");
 		} else {
 			ComplexGameDescrDialogFragment helpDialog = new ComplexGameDescrDialogFragment();
 			helpDialog.initialize(this, game);
 			helpDialog.show(getChildFragmentManager(), "help");
 		}
 	}
 
 
 
 	private final class ButtonPressListener implements View.OnClickListener {
 		private final Cell cell;
 
 		public ButtonPressListener(Cell cell) {
 			this.cell = cell;
 		}
 
 		@Override
 		public void onClick(View view) {
 			if (isRolling) {
 				return;
 			}
 			if (!game.getCurrentPlayer().getStrategy().isHuman()) {
 				// ignore button clicks if the current player is not a human
 				return;
 			}
 			Marker marker = game.getMarkerToPlay();
 
 			boolean wasValid = makeAndDisplayMove(marker, cell);
 			if (!wasValid)
 				return;
 
 			// send move to opponent
 			RoomListener appListener = getMainActivity().getRoomListener();
 			if (appListener != null) {
 				appListener.sendMove(marker, cell);
 			}
 
 		}
 
 	}
 
 	private void updateHeader(boolean animateMarker) {
 		Player player = game.getCurrentPlayer();
 		if (player.getMarker() == Marker.X) {
 			xHeaderLayout.setBackgroundResource(R.drawable.current_player);
 			oHeaderLayout.setBackgroundResource(R.drawable.inactive_player);
 
 			highlightPlayerTurn(xHeaderLayout, oHeaderLayout);
 
 		} else {
 			oHeaderLayout.setBackgroundResource(R.drawable.current_player);
 			xHeaderLayout.setBackgroundResource(R.drawable.inactive_player);
 
 			highlightPlayerTurn(oHeaderLayout, xHeaderLayout);
 		}
 
 		// make the chaotic mode show some "rolling" of the markers.
 		// This requires the online/input handling to not accept input until
 		// the roll is done.
 		// On device inputs, can simply ignore button presses.
 		// online moves need to be queued while animation is proceeding
 		if (animateMarker
 				&& (game.getMarkerChance().isChaotic() || game
 						.getMarkerChance().isCustom())) {
 			displayAnimatedMarkerToPlay();
 		} else {
 			displayMarkerToPlay();
 		}
 
 		updateGameStatDisplay();
 	}
 
 	private void updateGameStatDisplay() {
 		numMoves.setText("" + game.getNumberOfMoves());
 		xWins.setText(getResources().getQuantityString(R.plurals.num_wins_with_label,
 				score.getXWins(), score.getXWins()));
 		oWins.setText(getResources().getQuantityString(R.plurals.num_wins_with_label,
 				score.getOWins(), score.getOWins()));
 		draws.setText("" + score.getDraws());
 		gameNumber.setText("" + score.getTotalGames());
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void highlightPlayerTurn(View highlight, View dimmed) {
 		float notTurnAlpha = 0.25f;
 		if (Utils.hasHoneycomb()) {
 			highlight.setAlpha(1f);
 			dimmed.setAlpha(notTurnAlpha);
 		}
 	}
 
 	private Random rollRandom = new Random();
 	private volatile boolean isRolling = false;
 	private Runnable afterRoll;
 
 	private void displayAnimatedMarkerToPlay() {
 		isRolling = true;
 		configureNonLocalProgresses();
 		MarkerChance chance = game.getMarkerChance();
 		final List<Integer> resourcesList = new ArrayList<Integer>();
 		if (chance.getMyMarker() > 0) {
 			if (game.getCurrentPlayer().getStrategy().getMarker() == Marker.X) {
 				resourcesList.add(R.drawable.system_cross);
 			} else {
 				resourcesList.add(R.drawable.system_dot);
 			}
 		}
 		if (chance.getOpponentMarker() > 0) {
 			if (game.getCurrentPlayer().getStrategy().getMarker() == Marker.X) {
 				resourcesList.add(R.drawable.system_dot);
 			} else {
 				resourcesList.add(R.drawable.system_cross);
 			}
 		}
 		if (chance.getRemoveMarker() > 0) {
 			resourcesList.add(R.drawable.bomb_icon_1068);
 		}
 		final int numPossible = resourcesList.size();
 		// number of rolls cycled through is random between min <-> max
 		// hold the number in a modifiable int array, as a cheat
 		final int[] rolls = new int[] { rollRandom.nextInt(5) + 5 };
 		final Drawable originalBackground = markerToPlayView.getBackground();
 		markerToPlayView
 				.setBackgroundColor(getResources()
 						.getColor(
 								com.actionbarsherlock.R.color.abs__bright_foreground_disabled_holo_light));
 		final Handler handler = new Handler();
 		final MainActivity mainActivity = getMainActivity();
 		// display a way to say "still rolling"
 		diceRollStreamId = mainActivity.playSound(Sounds.DICE_ROLL, true);
 		final Runnable flip = new Runnable() {
 			@Override
 			public void run() {
 				rolls[0] = rolls[0] - 1;
 				if (rolls[0] == 0) {
 					markerToPlayView.setBackgroundDrawable(originalBackground);
 					displayMarkerToPlay();
 					// let the marker to play show for a bit before allowing the
 					// opponent's (AI or online) move
 					handler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							isRolling = false;
 							configureNonLocalProgresses();
 							if (diceRollStreamId > 0) {
 								mainActivity.stopSound(diceRollStreamId);
 								diceRollStreamId = 0;
 							}
 							if (afterRoll != null) {
 								afterRoll.run();
 								afterRoll = null;
 							}
 						}
 					}, OPPONENT_MARKER_VISIBILITY_PAUSE_MS);
 					return;
 				}
 
 				markerToPlayView.setImageResource(resourcesList.get(rolls[0]
 						% numPossible));
 				handler.postDelayed(this, MARKER_ROLL_VISIBILITY_PAUSE);
 			}
 		};
 
 		flip.run();
 	}
 
 	private int diceRollStreamId;
 	private OnlinePlayAgainFragment onlinePlayAgainDialog;
 
 	private void displayMarkerToPlay() {
 		Marker toPlay = game.getMarkerToPlay();
 		if (toPlay == Marker.EMPTY) {
 			markerToPlayView.setImageResource(R.drawable.bomb_icon_1068);
 		} else if (toPlay == Marker.X) {
 			markerToPlayView.setImageResource(R.drawable.system_cross);
 		} else {
 			markerToPlayView.setImageResource(R.drawable.system_dot);
 		}
 	}
 
 	public boolean makeAndDisplayMove(Marker markerToPlay, Cell cell) {
 		Marker marker = game.getMarkerToPlay();
 		if (marker != markerToPlay) {
 			throw new RuntimeException("Invalid marker played!");
 		}
 		State outcome = null;
 		try {
 			outcome = game.placeMarker(cell);
 		} catch (InvalidMoveException e) {
 			getMainActivity().playSound(Sounds.INVALID_MOVE);
 			int messageId;
 			if (markerToPlay == Marker.EMPTY) {
 				messageId = R.string.invalid_move_need_nonempty;
 			} else {
 				messageId = R.string.invalid_move_need_empty;
 			}
 			Toast toast = Toast.makeText(getActivity(), messageId,
 					Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 			return false;
 		}
 		if (markerToPlay == Marker.X) {
 			getMainActivity().playSound(Sounds.PLAY_X);
 		} else {
 			getMainActivity().playSound(Sounds.PLAY_O);
 		}
 		privateMakeMove(cell, marker, outcome);
 		return true;
 	}
 
 	private void privateMakeMove(Cell cell, Marker marker, State outcome) {
 		ImageButton cellButton = findButtonFor(cell);
 		animateMove(marker, cellButton, outcome);
 	}
 
 	private void animateMove(final Marker marker, final ImageButton cellButton,
 			final State outcome) {
 		boolean animate = true;
 		if (!animate) {
 			postMove(marker, cellButton, outcome);
 			return;
 		}
 		// experimenting with animations...
 		// create set of animations
 		AnimationSet replaceAnimation = new AnimationSet(false);
 		// animations should be applied on the finish line
 		replaceAnimation.setFillAfter(false);
 
 		float xScale = ((float) cellButton.getWidth())
 				/ markerToPlayView.getWidth();
 		float yScale = ((float) cellButton.getHeight())
 				/ markerToPlayView.getHeight();
 
 		View grid_container = getActivity().findViewById(R.id.grid_container);
 		int xChange = cellButton.getLeft() - markerToPlayView.getLeft();
 		int yChange = cellButton.getTop()
 				+ ((View) cellButton.getParent()).getTop()
 				+ grid_container.getTop() - markerToPlayView.getTop();
 
 		// create scale animation
 		ScaleAnimation scale = new ScaleAnimation(1.0f, xScale, 1.0f, yScale);
 		scale.setDuration(1000);
 
 		// create translation animation
 		TranslateAnimation trans = new TranslateAnimation(0, xChange, 0,
 				yChange);
 		trans.setDuration(1000);
 
 		// add new animations to the set
 		replaceAnimation.addAnimation(scale);
 		replaceAnimation.addAnimation(trans);
 
 		AlphaAnimation fade = new AlphaAnimation(1, 0);
 		fade.setStartOffset(800);
 		fade.setDuration(200);
 		replaceAnimation.addAnimation(fade);
 
 		Interpolator interpolator = new AnticipateInterpolator();
 		replaceAnimation.setInterpolator(interpolator);
 
 		replaceAnimation.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// empty
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// empty
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				if (marker == Marker.EMPTY) {
 					explodeCell(cellButton, outcome);
 				} else {
 					postMove(marker, cellButton, outcome);
 				}
 			}
 
 		});
 
 		// start our animation
 		markerToPlayView.startAnimation(replaceAnimation);
 	}
 
 	private void explodeCell(final ImageButton cellButton, final State outcome) {
 		boolean showExplosion = true;
 		if (!showExplosion) {
 			postMove(Marker.EMPTY, cellButton, outcome);
 			return;
 		}
 
 		getMainActivity().playSound(Sounds.REMOVE_MARKER);
 		cellButton.setImageResource(R.drawable.explosion_icon_13018);
 		AnimationSet explodeSet = new AnimationSet(true);
 		Animation flash = new AlphaAnimation(1, 0);
 		flash.setDuration(200);
 		flash.setRepeatCount(3);
 		flash.setRepeatMode(Animation.REVERSE);
 
 		Animation expand = new ScaleAnimation(1, 2, 1, 2);
 		expand.setRepeatCount(3);
 		expand.setRepeatMode(Animation.REVERSE);
 
 		int width = cellButton.getWidth();
 		int height = cellButton.getHeight();
 
 		Animation translate = new TranslateAnimation(0, -width / 2, 0,
 				height / 2);
 		translate.setRepeatCount(3);
 		translate.setRepeatMode(Animation.REVERSE);
 
 		explodeSet.addAnimation(flash);
 		explodeSet.addAnimation(expand);
 		explodeSet.addAnimation(translate);
 
 		explodeSet.setInterpolator(new AccelerateDecelerateInterpolator());
 
 		explodeSet.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// nothing
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// nothing
 			}
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				postMove(Marker.EMPTY, cellButton, outcome);
 			}
 		});
 
 		cellButton.startAnimation(explodeSet);
 
 		// final ExplodeView explodeView = (ExplodeView) getActivity()
 		// .findViewById(R.id.explode_view);
 		// explodeView.setImage(R.drawable.bomb_icon_1068);
 		// explodeView.setOnPostExplode(new OnPostExplosion() {
 		// @Override
 		// public void postExplosion(ExplodeView view) {
 		// explodeView.setVisibility(View.GONE);
 		// explodeView.recycle();
 		// postMove(Marker.EMPTY, cellButton, outcome);
 		// }
 		// });
 		//
 		// AbsoluteLayout.LayoutParams layoutParams =
 		// (android.widget.AbsoluteLayout.LayoutParams) explodeView
 		// .getLayoutParams();
 		// layoutParams.x = cellButton.getLeft()
 		// + ((View) cellButton.getParent()).getLeft();
 		// layoutParams.y = cellButton.getTop()
 		// + ((View) cellButton.getParent()).getTop();
 		// layoutParams.width = cellButton.getWidth();
 		// layoutParams.height = cellButton.getHeight();
 		// explodeView.setLayoutParams(layoutParams);
 		// explodeView.setVisibility(View.VISIBLE);
 		// // TODO find middle, or use middle in default no arg method on
 		// // ExplodeView
 		// explodeView.explode(0, 0);
 	}
 
 	private void postMove(Marker marker, ImageButton cellButton, State outcome) {
 		markerToPlayView.setImageDrawable(null);
 		if (marker == Marker.EMPTY) {
 			cellButton.setImageDrawable(null);
 		} else {
 			int resId = marker == Marker.X ? R.drawable.system_cross
 					: R.drawable.system_dot;
 			cellButton.setImageResource(resId);
 		}
 		if (outcome.isOver()) {
 			updateGameStatDisplay();
 			endGame(outcome);
 		} else {
 			evaluateInGameAchievements(outcome);
 			updateHeader(true);
 			acceptMove();
 		}
 	}
 
 	private void endGame(State outcome) {
 		numMoves.setText("" + game.getNumberOfMoves());
 		evaluateGameEndAchievements(outcome);
 		evaluateLeaderboards(outcome);
 		Player winner = outcome.getWinner();
 		if (winner != null) {
 			score.incrementScore(winner);
 			winOverlayView.setWinStyle(outcome.getWinStyle());
 			winOverlayView.invalidate();
 
 			if (game.getMode() == GameMode.PASS_N_PLAY) {
 				getMainActivity().playSound(Sounds.GAME_WON);
 			} else {
 				// the player either won or lost
 				if (winner.equals(game.getLocalPlayer())) {
 					getMainActivity().playSound(Sounds.GAME_WON);
 				} else {
 					getMainActivity().playSound(Sounds.GAME_LOST);
 				}
 			}
 
 			String title = getString(R.string.player_won, winner.getName());
 
 			promptToPlayAgain(title);
 		} else {
 			score.incrementScore(null);
 			getMainActivity().playSound(Sounds.GAME_DRAW);
 			String title = getString(R.string.draw);
 
 			promptToPlayAgain(title);
 		}
 	}
 
 	private void promptToPlayAgain(String title) {
 		if (game.getMode() == GameMode.ONLINE) {
 			onlinePlayAgainDialog = new OnlinePlayAgainFragment();
 			onlinePlayAgainDialog.initialize(this, getMainActivity()
 					.getRoomListener().getOpponentName(), title);
 			onlinePlayAgainDialog.show(getChildFragmentManager(), "playAgain");
 			// TODO wire up the play again / not play again message handling via
 			// the dialog
 			return;
 		}
 
 		OnClickListener cancelListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				leaveGame();
 			}
 
 		};
 		OnClickListener playAgainListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				playAgain();
 			}
 
 		};
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setTitle(title);
 		builder.setMessage(R.string.play_again);
 		builder.setCancelable(false);
 
 		builder.setNegativeButton(R.string.no, cancelListener);
 		builder.setPositiveButton(R.string.yes, playAgainListener);
 
 		AlertDialog dialog = builder.create();
 
 		dialog.show();
 	}
 
 	private void acceptMove() {
 		final PlayerStrategy currentStrategy = game.getCurrentPlayer()
 				.getStrategy();
 		if (currentStrategy.isHuman()) {
 			// let the buttons be pressed for a human interaction
 			return;
 		}
 		// show a thinking/progress icon, suitable for network play and ai
 		// thinking..
 		configureNonLocalProgresses();
 		if (!currentStrategy.isAI()) {
 			return;
 		}
 
 		AsyncTask<Void, Void, Cell> aiMove = new AsyncTask<Void, Void, Cell>() {
 			@Override
 			protected Cell doInBackground(Void... params) {
 				return currentStrategy.move(game.getBoard(),
 						game.getMarkerToPlay());
 			}
 
 			@Override
 			protected void onPostExecute(final Cell move) {
 				if (isRolling) {
 					afterRoll = new Runnable() {
 						@Override
 						public void run() {
 							highlightAndMakeMove(game.getMarkerToPlay(), move);
 						}
 					};
 					return;
 				}
 				highlightAndMakeMove(game.getMarkerToPlay(), move);
 			}
 		};
 		aiMove.execute((Void) null);
 	}
 
 	private void configureNonLocalProgresses() {
 		if (thinking == null || thinkingText == null) {
 			// safety for when start called before activity is created
 			return;
 		}
 		PlayerStrategy strategy = game.getCurrentPlayer().getStrategy();
 		if (strategy.isHuman() && !isRolling) {
 			thinking.setVisibility(View.GONE);
 			return;
 		}
 		thinking.setVisibility(View.VISIBLE);
 		thinkingText.setVisibility(isRolling ? View.GONE : View.VISIBLE);
 
 	}
 
 	public void onlineMakeMove(final Marker marker, final Cell cell) {
 		if (isRolling) {
 			afterRoll = new Runnable() {
 				@Override
 				public void run() {
 					highlightAndMakeMove(marker, cell);
 				}
 			};
 			return;
 		}
 		highlightAndMakeMove(marker, cell);
 	}
 
 	public void highlightAndMakeMove(final Marker marker, final Cell move) {
 		// hide the progress icon
 		thinking.setVisibility(View.GONE);
 		// delay and highlight the move so the human player has a
 		// chance to see it
 		final ImageButton cellButton = findButtonFor(move);
 		final Drawable originalBackGround = cellButton.getBackground();
 		cellButton.setBackgroundColor(getResources().getColor(
 				R.color.holo_blue_light));
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				// TODO not
 				// TODO play opponent sound
 				cellButton.setBackgroundDrawable(originalBackGround);
 				makeAndDisplayMove(marker, move);
 			}
 		}, NON_HUMAN_OPPONENT_HIGHLIGHT_MOVE_PAUSE_MS);
 	}
 
 	private ImageButton findButtonFor(Cell cell) {
 		int id;
 		int x = cell.getX();
 		int y = cell.getY();
 		if (x == 0) {
 			if (y == 0) {
 				id = R.id.button_r1c1;
 			} else if (y == 1) {
 				id = R.id.button_r1c2;
 			} else if (y == 2) {
 				id = R.id.button_r1c3;
 			} else {
 				throw new RuntimeException("Invalid cell");
 			}
 		} else if (x == 1) {
 			if (y == 0) {
 				id = R.id.button_r2c1;
 			} else if (y == 1) {
 				id = R.id.button_r2c2;
 			} else if (y == 2) {
 				id = R.id.button_r2c3;
 			} else {
 				throw new RuntimeException("Invalid cell");
 			}
 		} else if (x == 2) {
 			if (y == 0) {
 				id = R.id.button_r3c1;
 			} else if (y == 1) {
 				id = R.id.button_r3c2;
 			} else if (y == 2) {
 				id = R.id.button_r3c3;
 			} else {
 				throw new RuntimeException("Invalid cell");
 			}
 		} else {
 			throw new RuntimeException("Invalid cell");
 		}
 		return (ImageButton) getActivity().findViewById(id);
 	}
 
 	private void evaluateGameEndAchievements(State outcome) {
 		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
 				.getApplication());
 
 		Achievements achievements = application.getAchievements();
 		achievements.testAndSetForGameEndAchievements(getMainActivity()
 				.getGameHelper(), getActivity(), game, outcome);
 	}
 
 	private void evaluateInGameAchievements(State outcome) {
 		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
 				.getApplication());
 
 		Achievements achievements = application.getAchievements();
 		achievements.testAndSetForInGameAchievements(getMainActivity()
 				.getGameHelper(), getActivity(), game, outcome);
 	}
 
 	private void evaluateLeaderboards(State outcome) {
 		ChaoTicTacToe application = ((ChaoTicTacToe) getActivity()
 				.getApplication());
 
 		Leaderboards leaderboards = application.getLeaderboards();
 		leaderboards.submitGame(getMainActivity().getGameHelper(),
 				getActivity(), game, outcome);
 
 	}
 
 	public MainActivity getMainActivity() {
 		return (MainActivity) super.getActivity();
 	}
 
 	public void messageRecieved(Participant opponentParticipant, String string) {
 		messages.add(new ChatMessage(opponentParticipant, string, false, System
 				.currentTimeMillis()));
 		getMainActivity().playSound(Sounds.CHAT_RECIEVED);
 		if (chatDialog != null) {
 			chatDialog.newMessage();
 		} else {
 			numNewMessages++;
 			invalidateMenu();
 		}
 	}
 
 	public void chatClosed() {
 		getMainActivity().getRoomListener().sendInChat(false);
 		chatDialog = null;
 		numNewMessages = 0;
 		invalidateMenu();
 	}
 
 	public void leaveGame() {
 		if (onlinePlayAgainDialog != null) {
 			// let the play again dialog handle it
 			return;
 		}
 
 		onGameStatsClose = new Runnable() {
 			@Override
 			public void run() {
 				getMainActivity().getSupportFragmentManager().popBackStack();
 				getMainActivity().gameEnded();
 			}
 		};
 		showGameStats();
 	}
 
 	public void playAgain() {
 		Player currentPlayer = game.getCurrentPlayer();
 		game = new Game(3, game.getMode(), currentPlayer,
 				currentPlayer.opponent(), game.getMarkerChance());
 		updateHeader(true);
 		winOverlayView.setWinStyle(null);
 		winOverlayView.invalidate();
 		for (ImageButton each : buttons) {
 			each.setImageDrawable(null);
 		}
 		acceptMove();
 	}
 
 	public void opponentWillPlayAgain() {
 		if (onlinePlayAgainDialog == null) {
 			return;
 		}
 		onlinePlayAgainDialog.opponentWillPlayAgain();
 	}
 
 	public void opponentWillNotPlayAgain() {
 		if (onlinePlayAgainDialog == null) {
 			return;
 		}
 		onlinePlayAgainDialog.opponentWillNotPlayAgain();
 	}
 
 	public void playAgainClosed() {
 		onlinePlayAgainDialog = null;
 		getMainActivity().getRoomListener().restartGame();
 	}
 
 	private boolean opponentLeftIsShowing;
 
 	public void opponentLeft() {
 		getView().setKeepScreenOn(false);
 		if (onlinePlayAgainDialog != null) {
 			// the user is in the play again dialog, let him read the info
 			return;
 
 		}
 		opponentLeftIsShowing = true;
 		final MainActivity activity = getMainActivity();
 		String message = activity.getResources().getString(
 				R.string.peer_left_the_game,
 				getMainActivity().getRoomListener().getOpponentName());
 		(new AlertDialog.Builder(getMainActivity())).setMessage(message)
 				.setNeutralButton(android.R.string.ok, new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						leaveGame();
 					}
 				}).create().show();
 
 	}
 
 	public void onDisconnectedFromRoom() {
 		if (onlinePlayAgainDialog != null || opponentLeftIsShowing) {
 			// the user is in the play again dialog, let him read the info
 			return;
 
 		}
 	}
 
 	private boolean opponentInChat = false;
 
 	public void opponentInChat() {
 		opponentInChat = true;
 		// show "animated" menu icon
 		invalidateMenu();
 
 		// update the display text
 		thinkingText.setText(getResources().getString(
 				R.string.opponent_is_in_chat,
 				getMainActivity().getRoomListener().getOpponentName()));
 	}
 
 	public void opponentClosedChat() {
 		// stop animated menu icon
 		opponentInChat = false;
 		invalidateMenu();
 
 		// update the display text
 		thinkingText.setText(getResources().getString(
 				R.string.opponent_is_thinking,
 				getMainActivity().getRoomListener().getOpponentName()));
 	}
 
 	private boolean helpShown = false;
 
 	public void gameHelpClosed() {
 		updateHeader(!helpShown);
 		helpShown = true;
 	}
 
 	private Runnable onGameStatsClose;
 
 	public void gameStatsClosed() {
 		if (onGameStatsClose != null) {
 			onGameStatsClose.run();
 		}
 	}
 }
