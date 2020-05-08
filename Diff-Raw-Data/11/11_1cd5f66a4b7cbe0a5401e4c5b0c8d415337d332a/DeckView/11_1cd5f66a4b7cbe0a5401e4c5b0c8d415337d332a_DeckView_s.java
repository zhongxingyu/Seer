 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.badlogic.gdx.utils.Timer;
 
 /**
  * This stage holds all the cards the player has to play with.
  * 
  * @author
  * 
  */
 public class DeckView extends Stage {
 
 	private final Texture buttonTexture;
 	
 	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private List<CardView> deckCards = new ArrayList<CardView>();
 	private List<CardView> chosenCards = new ArrayList<CardView>();
 	private int position;
 	private CardListener cl;
 	private Table container;
 	private final Table lowerArea, upperArea, statusBar;
 	private final Table playPanel; // The panel with [Play] [Step] [Skip]
 	private final Table drawPanel; // The panel with [Draw cards]
 
 	public static final String EVENT_PLAY = "play";
 	public static final String EVENT_STEP = "step";
 	public static final String EVENT_SKIP = "skip";
 	public static final String EVENT_DRAW_CARDS = "drawCards";
 	
 	private int timerTick;
 	private final Timer timer;
 	
 	private final Label timerLabel;
 
 	/**
 	 * Creates a new default instance.
 	 */
 	public DeckView() {
 		super();
 		Texture deckTexture = new Texture(Gdx.files.internal("textures/woodenDeck.png"));
 		deckTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		buttonTexture = new Texture(Gdx.files.internal("textures/button.png"));
 		buttonTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 
 		cl = new CardListener(this);
 		
 		// Default camera
 		OrthographicCamera cardCamera = new OrthographicCamera(480, 800);
 		cardCamera.zoom = 1.0f;
 		cardCamera.position.set(240, 400, 0f);
 		cardCamera.update();
 		setCamera(cardCamera);
 		
 		// Set background image
 		Image deck = new Image(new TextureRegion(deckTexture, 0f, 0f, 1f, 1f));
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
 		container.add(upperArea);
 		
 		statusBar = new Table();
 		statusBar.debug();
 		statusBar.setSize(480, 80);
 		statusBar.setPosition(0, 240);
 		container.add(statusBar);
 		
 		playPanel = buildPlayerPanel();		
     	drawPanel = buildDrawCardPanel();	
     	
     	timer = new Timer();
    	timer.stop();
     	
     	LabelStyle lStyle = new LabelStyle();
     	lStyle.font = new BitmapFont();
     	timerLabel = new Label("", lStyle);
     	statusBar.add(timerLabel);
 	}
 	
 	/*
 	 * Creates the panel with the [Draw Cards] button.
 	 */
 	private Table buildDrawCardPanel() {
 		TextButtonStyle style = new TextButtonStyle();	  
         style.up = new TextureRegionDrawable(
         		new TextureRegion(buttonTexture, 0, 0, 32, 32));
         style.down = new TextureRegionDrawable(
         		new TextureRegion(buttonTexture, 0, 32, 32, 32));
         style.font = new BitmapFont();
 		
 		int internalPadding = 50, externalPadding = 10;
 		Table drawPanel = new Table();
     	drawPanel.setSize(480, 120);
     	TextButton draw = new TextButton("Draw Cards", style);
     	draw.pad(0, internalPadding, 0, internalPadding); // Internal padding
     	drawPanel.add(draw).pad(externalPadding); // Border
     	
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
 		TextButtonStyle style = new TextButtonStyle();	  
         style.up = new TextureRegionDrawable(
         		new TextureRegion(buttonTexture, 0, 0, 32, 32));
         style.down = new TextureRegionDrawable(
         		new TextureRegion(buttonTexture, 0, 32, 32, 32));
         style.font = new BitmapFont();
 		
 		int internalPadding = 50, externalPadding = 10;
 		Table playPanel = new Table();
 		playPanel.setSize(480, 120);
 		TextButton play = new TextButton("Play", style);
 		play.pad(0, internalPadding, 0, internalPadding); // Internal padding
     	playPanel.add(play).pad(externalPadding); // Border
     	TextButton step = new TextButton("Step", style);
     	step.pad(0, internalPadding, 0, internalPadding); // Internal padding
     	playPanel.add(step).pad(externalPadding); // Border
     	TextButton skip = new TextButton("Skip", style);
     	skip.pad(0, internalPadding, 0, internalPadding); // Internal padding
     	playPanel.add(skip).pad(externalPadding); // Border
     	
     	play.addListener(new ClickListener() {
     		@Override
     		public void clicked(InputEvent event, float x, float y) {
     			pcs.firePropertyChange(EVENT_PLAY, 0, 1);
     		}
     	});
     	step.addListener(new ClickListener() {
     		@Override
     		public void clicked(InputEvent event, float x, float y) {
     			pcs.firePropertyChange(EVENT_STEP, 0, 1);
     		}
     	});
     	skip.addListener(new ClickListener() {
     		@Override
     		public void clicked(InputEvent event, float x, float y) {
     			pcs.firePropertyChange(EVENT_SKIP, 0, 1);
     		}
     	});
     	return playPanel;
 	}
 		
 	/**
 	 * Sets the timer to the specified values.
 	 * The timer will then start.
 	 * @param h The number of hours.
 	 * @param m The number of minutes.
 	 * @param s The number of seconds.
 	 */
 	public void setTimerValue(int h, int m , int s) {
 		this.setTimer(s + m*60 + h * 3600 + 1);
 		// +1 To start the timer at the right time, and to stop it when reaching 0.
 	}
 	
 	/**
 	 * Sets the timer to count down the specified amount of seconds.
 	 * @param ticks The second to count down from.
 	 */
 	public void setTimer(int ticks) {
 		this.timerTick = ticks;
 		timer.clear();
 		// Tick every second.
 		timer.scheduleTask(new Timer.Task() {
 			@Override
 			public void run() {
 				timerTick--;
 				int h = timerTick / 3600;
 				int m = (timerTick / 60) % 60;
 				int s = timerTick % 60;
 				s = Math.max(s, 0);
 				timerLabel.setText(String.format("%02d", h) + 
 						":" + String.format("%02d", m) + 
 						":" + String.format("%02d", s));
 			}
 		}, 0, 1f, timerTick - 1);
		// Stopping itself.
		timer.scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				timer.stop();
			}
		}, timerTick);
		timer.start();
 	}
 	
 	public void addListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 
 	/**
 	 * Sets which cards the deck should display.
 	 * 
 	 * @param list
 	 *            The cards the deck should display.
 	 */
 	public void setDeckCards(List<CardView> list) {
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
 		}
 	}
 	
 	public void displayWaiting() {
 		lowerArea.clear();
 	}
 	
 	public void displayDrawCard() {
 		lowerArea.clear();
 		lowerArea.add(drawPanel);
 	}
 	
 	public void displayPlayOptions() {
 		lowerArea.clear();
 		lowerArea.add(playPanel);
 	}
 	
 	/**
 	 * Returns the cards added to the register
 	 * 
 	 * @return
 	 */
 	public List<CardView> getChosenCards() {
 		return this.chosenCards;
 	}
 
 	/**
 	 * Rerenders all the player's card at their correct positions.
 	 */
 	public void updateCards() {
 		updateChosenCards();
 		updateDeckCards();
 	}
 
 	/**
 	 * Rerenders all the cards added to the register.
 	 */
 	public void updateChosenCards() {
 		for (int i = 0; i < this.chosenCards.size(); i++) {
 			CardView cv = this.chosenCards.get(i);
 			cv.setPosition(
 					240 - this.getChosenCardDeckWidth() / 2
 							+ (cv.getWidth() + 10) * i, cv.getHeight() + 10);
 		}
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
 	 * Gives the total width it takes to render the cards not yet added to a
 	 * register.
 	 * 
 	 * @return
 	 */
 	public int getCardDeckWidth() {
 		return this.deckCards.size()
 				* ((int) this.deckCards.get(0).getWidth() + 10) - 10;
 	}
 
 	/**
 	 * Returns the total width it takes to render the cards added to the
 	 * register
 	 * 
 	 * @return
 	 */
 	public int getChosenCardDeckWidth() {
 		if (this.chosenCards.size() != 0) {
 			return this.chosenCards.size()
 					* ((int) this.chosenCards.get(0).getWidth() + 10) - 10;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Moves a card to either the chosen cards or deck cards when it is tapped
 	 * 
 	 * @param card
 	 *            The card that was tapped
 	 */
 	public void moveCard(CardView card) {
 		if (chosenCards.remove(card)) {
 			deckCards.add(card);
 		} else if (chosenCards.size() < 5) {
 			if (deckCards.remove(card)) {
 				chosenCards.add(card);
 			}
 		}
 		updateCards();
 	}
 
 	/**
 	 * Places a card at the correct place when dragged in place
 	 * 
 	 * @param card
 	 *            The card to be placed
 	 * @param x
 	 *            The x coordinate of the card
 	 * @param y
 	 *            The y coordinate of the card
 	 * @return
 	 */
 	public void placeCardAtPosition(CardView card) {
 		System.out.println(card.getX() + " " + card.getY());
 		if (card.getY() > 90 && card.getY() < 240) {
 			if (chosenCards.size() == 0) {
 				moveCard(card);
 				return;
 			} else if (card.getX() > chosenCards.get(chosenCards.size()-1).getX()) {
 				if (chosenCards.size() < 5 && deckCards.remove(card)) {
 					chosenCards.add(chosenCards.size(), card);
 					updateCards();
 					return;
 				} else if (chosenCards.remove(card)) {
 					chosenCards.add(chosenCards.size(), card);
 					updateCards();
 					return;
 				}
 			} else if (chosenCards.size() <= 5) {
 				for (int i = 0; i < chosenCards.size(); i++) {
 					if (card.getX() < chosenCards.get(i).getX()) {
 						if (chosenCards.size() < 5 && deckCards.remove(card)) {
 							chosenCards.add(i, card);
 							updateCards();
 							return;
 						} else if (chosenCards.contains(card) && chosenCards.indexOf(card) < i && chosenCards.remove(card)) {
 							chosenCards.add(i-1, card);
 							updateCards();
 							return;
 						} else if (chosenCards.remove(card)) {
 							chosenCards.add(i, card);
 							updateCards();
 							return;
 						}
 					}
 				}
 			}
 		} else if (card.getY() < 90) {
 			if (chosenCards.remove(card)) {
 				deckCards.add(card);
 			}
 		}
 		updateCards();
 	}
 
 }
