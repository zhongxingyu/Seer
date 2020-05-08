 package se.chalmers.dryleafsoftware.androidrally.libgdx.view;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 /**
  * This stage holds all the cards the player has to play with.
  * 
  * @author
  * 
  */
 public class DeckView extends Stage {
 
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private List<CardView> deckCards = new ArrayList<CardView>();
 	private int position;
 	private final CardListener cl;
 	private final Table container;
 	private final Table lowerArea, upperArea, statusBar;
 	private final Table playPanel; // The panel with [Play] [Step] [Skip]
 	private final Table drawPanel; // The panel with [Draw cards]
 	private final Table allPlayerInfo; // The panel with all the players' info
 	private final RegisterView registerView;
 	private final TextButtonStyle buttonStyle;
 	private final TextButton runButton;
 
 	/**
 	 * Specifying that the actions should stop playing.
 	 */
 	public static final String EVENT_PAUSE = "pause";
 	/**
 	 * Specifying that the game should start playing.
 	 */
 	public static final String EVENT_PLAY = "play";
 	/**
 	 * Specifying that the game should play the actions fast.
 	 */
 	public static final String EVENT_FASTFORWARD = "fast";
 	/**
 	 * Specifying that all actions should be skipped.
 	 */
 	public static final String EVENT_STEP_ALL = "stepAll";
 	/**
 	 * Specifying that the register's set of actions should be skipped.
 	 */
 	public static final String EVENT_STEP_CARD = "stepCard";
 	/**
 	 * Specifying that the player should be given new cards.
 	 */
 	public static final String EVENT_DRAW_CARDS = "drawCards";
 	/**
 	 * Specifying that the player has looked at its cards long enough.
 	 */
 	public static final String TIMER_CARDS = "timerCards";
 	/**
 	 * Specifying that the round has ended.
 	 */
 	public static final String TIMER_ROUND = "timerRound";
 	/**
 	 * Specifying that the info button has been pressed.
 	 */
 	public static final String EVENT_INFO = "info";
 	/**
 	 * Specifying that the player wants to send its cards.
 	 */
 	public static final String EVENT_RUN = "run";
 
 	private final Timer timer;
 	private final Label timerLabel;
 
 	private int cardTick = 0;
 	private int roundTick = 0;
 	private final int roundTime;
 	private static final int MAX_PING = 2;
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param robots
 	 *            The robots in the game.
 	 * @param robotID
 	 *            The ID of the player's robot.
 	 * @param roundTime
 	 *            The time until next round.
 	 */
 	public DeckView(List<RobotView> robots, int robotID, int roundTime) {
 		super();
 		this.roundTime = roundTime;
 		Texture deckTexture = new Texture(
 				Gdx.files.internal("textures/woodenDeck.png"));
 		deckTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		Texture compTexture = new Texture(
 				Gdx.files.internal("textures/deckComponents.png"));
 		compTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		cl = new CardListener(this);
 
 		// Default camera
 		OrthographicCamera cardCamera = new OrthographicCamera(480, 800);
 		cardCamera.zoom = 1.0f;
 		cardCamera.position.set(240, 400, 0f);
 		cardCamera.update();
 		setCamera(cardCamera);
 
 		// Set background image
 		Image deck = new Image(new TextureRegion(deckTexture, 0, 0, 480, 320));
 		deck.setPosition(0, 0);
 		deck.setSize(480, 320);
 		addActor(deck);
 
 		container = new Table();
 		container.debug();
 		container.setSize(480, 320);
 		container.setLayoutEnabled(false);
 		addActor(container);
 
 		lowerArea = new Table();
 		lowerArea.debug();
 		lowerArea.setSize(480, 120);
 		lowerArea.setLayoutEnabled(false);
 		container.add(lowerArea);
 
 		upperArea = new Table();
 		upperArea.debug();
 		upperArea.setSize(480, 120);
 		upperArea.setPosition(0, 120);
 		upperArea.setLayoutEnabled(false);
 		container.add(upperArea);
 
 		statusBar = new Table();
 		statusBar.debug();
 		statusBar.setSize(480, 80);
 		statusBar.setPosition(0, 240);
 		statusBar.setLayoutEnabled(false);
 		container.add(statusBar);
 
 		registerView = new RegisterView(compTexture);
 		registerView.setSize(upperArea.getWidth(), upperArea.getHeight());
 		upperArea.add(registerView);
 
 		NinePatchDrawable buttonTexture = new NinePatchDrawable(new NinePatch(
 				new Texture(Gdx.files.internal("textures/button9patch.png")),
 				4, 4, 4, 4));
 		NinePatchDrawable buttonTexturePressed = new NinePatchDrawable(
 				new NinePatch(new Texture(Gdx.files
 						.internal("textures/button9patchpressed.png")), 4, 4,
 						4, 4));
 		buttonStyle = new TextButtonStyle(buttonTexture, buttonTexturePressed,
 				null);
 		buttonStyle.font = new BitmapFont();
 		buttonStyle.pressedOffsetX = 1;
 		buttonStyle.pressedOffsetY = -1;
 
 		TextButtonStyle playStyle = new TextButtonStyle(
 				new TextureRegionDrawable(new TextureRegion(compTexture, 0,
 						192, 64, 64)), new TextureRegionDrawable(
 						new TextureRegion(compTexture, 64, 192, 64, 64)), null);
 		playStyle.disabled = new TextureRegionDrawable(new TextureRegion(
 				compTexture, 128, 192, 64, 64));
 		playStyle.font = new BitmapFont();
 		playStyle.fontColor = Color.WHITE;
 		playStyle.disabledFontColor = Color.GRAY;
 		playStyle.pressedOffsetX = 1;
 		playStyle.pressedOffsetY = -1;
 
 		runButton = new TextButton("\r\nRun", playStyle);
 		runButton.setPosition(410, 20);
 		runButton.setSize(64, 64);
 		runButton.setDisabled(true);
 		registerView.add(runButton);
 		runButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (!runButton.isDisabled()) {
 					runButton.setDisabled(true);
 					pcs.firePropertyChange(EVENT_RUN, 0, 1);
 				}
 			}
 		});
 
 		playPanel = buildPlayerPanel();
 		drawPanel = buildDrawCardPanel();
 
 		timer = new Timer();
 		// Tick every second.
 		timer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				if (cardTick > 0) {
 					cardTick--;
 					if (cardTick == 0) {
 						pcs.firePropertyChange(TIMER_CARDS, 0, 1);
 					}
 				}
 				if (roundTick - MAX_PING > 0) {
 					roundTick--;
 					if (roundTick - MAX_PING == 0) {
 						pcs.firePropertyChange(TIMER_ROUND, 0, 1);
 					}
 				}
 				int timerTick = cardTick > 0 ? cardTick : Math.max(0, roundTick
 						- MAX_PING);
 				timerLabel.setVisible(timerTick > 0);
 				setTimerLabel(timerTick);
 			}
 		}, 1000, 1000);
 
 		Table statusCenter = new Table();
 		statusCenter.setPosition(200, 0);
 		statusCenter.setSize(80, statusBar.getHeight());
 		statusCenter.debug();
 		statusBar.add(statusCenter);
 		LabelStyle lStyle = new LabelStyle();
 		lStyle.font = new BitmapFont();
		timerLabel = new Label("00:00:00", lStyle);
		timerLabel.setVisible(false);
 		statusCenter.add(timerLabel);
 		statusCenter.row();
 
 		final TextButton showPlayers = new TextButton("Info", buttonStyle);
 		statusCenter.add(showPlayers).minWidth(75);
 		showPlayers.addListener(new ClickListener() {
 			private boolean dispOpp = false;
 
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (dispOpp) {
 					displayNormal();
 					showPlayers.setText("Info");
 				} else {
 					displayOpponentInfo();
 					showPlayers.setText("Back");
 				}
 				dispOpp = !dispOpp;
 			}
 		});
 
 		DamageView dv = new DamageView(compTexture, robots.get(robotID));
 		LifeView lv = new LifeView(compTexture, robots.get(robotID));
 		lv.setPosition(0, 0);
 		lv.setSize(120, 80);
 		lv.debug();
 		dv.setPosition(290, 20);
 		dv.setSize(200, 40);
 		dv.debug();
 		statusBar.add(dv);
 		statusBar.add(lv);
 
 		allPlayerInfo = new Table();
 		allPlayerInfo.setPosition(0, 0);
 		allPlayerInfo.setSize(480, 240);
 		Table scrollContainer = new Table();
 		scrollContainer.defaults().width(240);
 		ScrollPane pane = new ScrollPane(scrollContainer);
 		allPlayerInfo.add(pane);
 
 		NinePatchDrawable divider = new NinePatchDrawable(new NinePatch(
 				new Texture(Gdx.files.internal("textures/divider.png")), 63,
 				63, 0, 0));
 		for (int i = 0; i < robots.size(); i++) {
 			if (i != robotID) {
 				scrollContainer.add(new LifeView(compTexture, robots.get(i)));
 				scrollContainer.add(new DamageView(compTexture, robots.get(i)));
 				scrollContainer.row();
 				if (i != robots.size() - 1) {
 					scrollContainer.add(new Image(divider)).colspan(2)
 							.width(420).pad(3);
 					scrollContainer.row();
 				}
 			}
 		}
 		scrollContainer.debug();
 	}
 
 	/**
 	 * Disable the run-button.
 	 * 
 	 * @param disable
 	 */
 	public void disableRun(boolean disable) {
 		runButton.setDisabled(true);
 	}
 
 	/**
 	 * Resets the roundtimer.
 	 */
 	public void resetRoundTimer() {
 		roundTick = DeckView.this.roundTime;
 	}
 
 	/*
 	 * Creates the panel with the [Draw Cards] button.
 	 */
 	private Table buildDrawCardPanel() {
 		int internalPadding = 50, externalPadding = 10;
 		Table drawPanel = new Table();
 		drawPanel.setSize(480, 120);
 		TextButton draw = new TextButton("Draw Cards", buttonStyle);
 		draw.pad(0, internalPadding, 0, internalPadding); // Internal padding
 		drawPanel.add(draw).pad(externalPadding).minHeight(30); // Border
 		draw.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_DRAW_CARDS, 0, 1);
 			}
 		});
 		return drawPanel;
 	}
 
 	/*
 	 * Creates the panel with the [Play] [Step] [Skip] buttons.
 	 */
 	private Table buildPlayerPanel() {
 		Texture buttons = new Texture(
 				Gdx.files.internal("textures/playButtons.png"));
 		buttons.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		Table playPanel = new Table();
 		playPanel.setSize(480, 120);
 
 		// Create pause button
 		Button pause = new Button(new ButtonStyle(new TextureRegionDrawable(
 				new TextureRegion(buttons, 0, 0, 64, 64)),
 				new TextureRegionDrawable(new TextureRegion(buttons, 0, 64, 64,
 						64)), null));
 		playPanel.add(pause).size(50, 50).pad(0);
 		pause.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_PAUSE, 0, 1);
 			}
 		});
 
 		// Create play button
 		Button play = new Button(new ButtonStyle(new TextureRegionDrawable(
 				new TextureRegion(buttons, 64, 0, 64, 64)),
 				new TextureRegionDrawable(new TextureRegion(buttons, 64, 64,
 						64, 64)), null));
 		playPanel.add(play).size(50, 50).pad(0);
 		play.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_PLAY, 0, 1);
 			}
 		});
 
 		// Create fast forward button
 		Button fastForward = new Button(new ButtonStyle(
 				new TextureRegionDrawable(new TextureRegion(buttons, 128, 0,
 						64, 64)), new TextureRegionDrawable(new TextureRegion(
 						buttons, 128, 64, 64, 64)), null));
 		playPanel.add(fastForward).size(50, 50).pad(0);
 		fastForward.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_FASTFORWARD, 0, 1);
 			}
 		});
 
 		// Create button for skip single card
 		Button skipCard = new Button(new ButtonStyle(new TextureRegionDrawable(
 				new TextureRegion(buttons, 192, 0, 64, 64)),
 				new TextureRegionDrawable(new TextureRegion(buttons, 192, 64,
 						64, 64)), null));
 		playPanel.add(skipCard).size(50, 50).pad(0);
 		skipCard.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_STEP_CARD, 0, 1);
 			}
 		});
 
 		// Create button for skip all cards
 		Button skipAll = new Button(new ButtonStyle(new TextureRegionDrawable(
 				new TextureRegion(buttons, 256, 0, 64, 64)),
 				new TextureRegionDrawable(new TextureRegion(buttons, 256, 64,
 						64, 64)), null));
 		playPanel.add(skipAll).size(50, 50).pad(0);
 		skipAll.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				pcs.firePropertyChange(EVENT_STEP_ALL, 0, 1);
 			}
 		});
 
 		return playPanel;
 	}
 
 	/**
 	 * Adds the specified listener.
 	 * 
 	 * @param listener
 	 *            The listener to add.
 	 */
 	public void addListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	/**
 	 * Removes the specified listener.
 	 * 
 	 * @param listener
 	 *            The listener to remove.
 	 */
 	public void removeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Only displays the locked cards.
 	 * 
 	 * @param input
 	 *            The String with card data.
 	 * @param texture
 	 *            The texture to use.
 	 */
 	public void setChosenCards(String input, Texture texture) {
 		registerView.clear();
 		BitmapFont cardFont = new BitmapFont();
 		cardFont.setColor(Color.GREEN);
 		int i = 0;
 		for (String card : input.split(":")) {
 			int prio = Integer.parseInt(card);
 			CardView cv = buildCard(prio, texture, i, cardFont);
 
 			System.out.println("registerView: " + registerView);
 			System.out.println("register[i]: " + registerView.getRegister(i));
 			registerView.getRegister(i).setCard(cv);
 			registerView.getRegister(i).displayOverlay(Register.UNFOCUS);
 			i++;
 		}
 	}
 
 	private CardView buildCard(int prio, Texture texture, int index,
 			BitmapFont cardFont) {
 		int regX = 0;
 		if (prio <= 60) {
 			regX = 0; // UTURN
 		} else if (prio <= 410 && prio % 20 != 0) {
 			regX = 1; // LEFT
 		} else if (prio <= 420 && prio % 20 == 0) {
 			regX = 2; // LEFT
 		} else if (prio <= 480) {
 			regX = 3; // Back 1
 		} else if (prio <= 660) {
 			regX = 4; // Move 1
 		} else if (prio <= 780) {
 			regX = 5; // Move 2
 		} else if (prio <= 840) {
 			regX = 6; // Move 3
 		}
 
 		CardView cv = new CardView(new TextureRegion(texture, regX * 128, 0,
 				128, 180), prio, index, cardFont);
 		cv.setSize(78, 110);
 		return cv;
 	}
 
 	/**
 	 * Displays the panel with all the players.
 	 */
 	public void displayOpponentInfo() {
 		container.removeActor(lowerArea);
 		container.removeActor(upperArea);
 		container.add(allPlayerInfo);
 	}
 
 	/**
 	 * Displays the panel which should be visible while waiting for round
 	 * results.
 	 */
 	public void displayWaiting() {
 		lowerArea.clear();
 		registerView.removeCardListener(cardListener);
 	}
 
 	/**
 	 * Displays the normal panel which is divided into an upper and a lower
 	 * area.
 	 */
 	public void displayNormal() {
 		container.removeActor(allPlayerInfo);
 		container.add(upperArea);
 		container.add(lowerArea);
 	}
 
 	/**
 	 * Displays the panel which should be visible after viewing the round
 	 * results.
 	 */
 	public void displayDrawCard() {
 		registerView.clear();
 		lowerArea.clear();
 		lowerArea.add(drawPanel);
 	}
 
 	/**
 	 * DIsplays the panel which should be visible when viewing the round
 	 * results.
 	 */
 	public void displayPlayOptions() {
 		lowerArea.clear();
 		lowerArea.add(playPanel);
 		registerView.removeCardListener(cardListener);
 	}
 
 	/**
 	 * Rerenders all the player's card at their correct positions.
 	 */
 	public void updateCards() {
 		updateDeckCards();
 	}
 
 	/**
 	 * Renders all the cards not yet added to the register
 	 */
 	public void updateDeckCards() {
 		if (this.getCardDeckWidth() < 480) {
 			this.position = 240 - this.getCardDeckWidth() / 2;
 		} else {
 			if (this.position > 0) {
 				this.position = 0;
 			} else if (this.position + getCardDeckWidth() < 480) {
 				this.position = 480 - getCardDeckWidth();
 			}
 		}
 		Collections.sort(this.deckCards);
 		for (int i = 0; i < this.deckCards.size(); i++) {
 			CardView cv = this.deckCards.get(i);
 			cv.setPosition((cv.getWidth() + 10) * i + this.position, 0);
 		}
 	}
 
 	private final ActorGestureListener cardListener = new ActorGestureListener() {
 		@Override
 		public void tap(InputEvent event, float x, float y, int count,
 				int button) {
 			Actor pressed = getTouchDownTarget();
 			if (pressed instanceof CardView) {
 				CardView card = (CardView) pressed;
 				if (deckCards.contains(card)) {
 					choseCard(card);
 				} else {
 					unChoseCard(card);
 				}
 			}
 			updateCards();
 		}
 	};
 
 	/**
 	 * Returns the cards added to the register
 	 * 
 	 * @return
 	 */
 	public CardView[] getChosenCards() {
 		return registerView.getCards();
 	}
 
 	/**
 	 * Sets the X-coordinate of the leftmost card not yet added to a register.
 	 * 
 	 * @param position
 	 */
 	public void setPositionX(int position) {
 		this.position = position;
 		updateDeckCards();
 	}
 
 	/**
 	 * Gives the X-coordinate of the leftmost card not yet added to a register.
 	 * 
 	 * @return
 	 */
 	public int getPositionX() {
 		return this.position;
 	}
 
 	/**
 	 * Gives the registers.
 	 * 
 	 * @return The registers.
 	 */
 	public RegisterView getRegisters() {
 		return this.registerView;
 	}
 
 	/**
 	 * Checks if the cardtimer is on.
 	 * 
 	 * @return <code>true</code> if the cardtimer is on.
 	 */
 	public boolean isCardTimerOn() {
 		return cardTick > 0;
 	}
 
 	/*
 	 * Sets the label of the timer to display the specified number of seconds as
 	 * [hh:mm:ss].
 	 */
 	private void setTimerLabel(int ticks) {
 		int h = ticks / 3600;
 		int m = (ticks / 60) % 60;
 		int s = ticks % 60;
 		s = Math.max(s, 0);
 		timerLabel.setText(String.format("%02d", h) + ":"
 				+ String.format("%02d", m) + ":" + String.format("%02d", s));
 	}
 
 	/**
 	 * Sets the card timer to the specified number in seconds.
 	 * 
 	 * @param cardTick
 	 *            The card timer's delay in seconds.
 	 */
 	public void setCardTick(int cardTick) {
 		this.cardTick = cardTick;
 		if (cardTick > 0) {
 			setTimerLabel(cardTick);
 		}
 	}
 
 	/**
 	 * Sets the round timer to the specified number in seconds.
 	 * 
 	 * @param roundTick
 	 *            The round timer's delay in seconds.
 	 */
 	public void setRoundTick(int roundTick) {
 		this.roundTick = roundTick;
 		if (roundTick > 0) {
 			setTimerLabel(roundTick);
 		}
 	}
 
 	/**
 	 * Gives the total width it takes to render the cards not yet added to a
 	 * register.
 	 * 
 	 * @return
 	 */
 	public int getCardDeckWidth() {
 		if (this.deckCards.isEmpty()) {
 			return 0;
 		} else {
 			return this.deckCards.size()
 					* ((int) this.deckCards.get(0).getWidth() + 10) - 10;
 		}
 	}
 
 	private void choseCard(CardView card) {
 		if (registerView.addCard(card)) {
 			deckCards.remove(card);
 		}
 	}
 
 	/**
 	 * Sets a chosen card.
 	 * 
 	 * @param card
 	 *            The card to add.
 	 */
 	private void unChoseCard(CardView card) {
 		if (registerView.removeCard(card)) {
 			lowerArea.add(card);
 			deckCards.add(card);
 		}
 	}
 
 	/**
 	 * Displays all cards.
 	 * 
 	 * @param input
 	 *            A String with all the cards' data.
 	 * @param texture
 	 *            The texture to use when creating the cards.
 	 */
 	public void setDeckCards(String input, Texture texture) {
 		List<CardView> cards = new ArrayList<CardView>();
 		// Clear cards
 		registerView.clear();
 		BitmapFont cardFont = new BitmapFont();
 		cardFont.setColor(Color.GREEN);
 
 		String indata = input;
 		int i = 0;
 		for (String card : indata.split(":")) {
 			String[] data = card.split(";");
 
 			int prio = (data.length == 2) ? Integer.parseInt(data[1]) : Integer
 					.parseInt(data[0]);
 			CardView cv = buildCard(prio, texture, i, cardFont);
 
 			if (data.length == 2) {
 				int lockPos = Integer.parseInt(data[0].substring(1));
 				registerView.getRegister(lockPos).setCard(cv);
 				registerView.getRegister(lockPos).displayOverlay(
 						Register.PADLOCK);
 			} else {
 				cards.add(cv);
 			}
 			i++;
 		}
 		Collections.sort(cards);
 		setDeckCards(cards);
 		updateCards();
 	}
 
 	/**
 	 * Sets which cards the deck should display.
 	 * 
 	 * @param list
 	 *            The cards the deck should display.
 	 */
 	private void setDeckCards(List<CardView> list) {
 		Table holder = new Table();
 		holder.setSize(480, 160);
 		holder.setLayoutEnabled(false);
 		lowerArea.clear();
 		lowerArea.add(holder);
 		this.deckCards = list;
 		for (int i = 0; i < list.size(); i++) {
 			CardView cv = list.get(i);
 			cv.setPosition((cv.getWidth() + 10) * i, 0);
 			holder.add(cv);
 		}
 		for (CardView cv : deckCards) {
 			cv.addListener(cl);
 			cv.addListener(cardListener);
 		}
 		runButton.setDisabled(false);
 	}
 }
