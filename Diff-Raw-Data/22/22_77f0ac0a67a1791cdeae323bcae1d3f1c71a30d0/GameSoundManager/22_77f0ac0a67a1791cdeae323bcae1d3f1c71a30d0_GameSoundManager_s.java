 package com.difane.games.ticktacktoe;
 
 import java.util.Hashtable;
 
 import com.difane.games.ticktacktoe.exceptions.GameBoardLineException;
 import com.livescribe.i18n.SoundResource;
 import com.livescribe.ui.MediaPlayer;
 
 /**
  * Required sound files:
  * 	- "Tick-Tack-Toe" - Game was selected in the menu
  *  - "Please draw first vertical line." - When first vertical line must be drawn
  *  - "Please draw second vertical line." - When second vertical line must be drawn
  *  - "Please draw first horizontal line." - When first horizontal line must be drawn
  *  - "Please draw second horizontal line." - When second horizontal line must be drawn
  *  - "Crosses is Yours. Make Your turn." - When board is drawn and actual game started
  *  - "Congratulations!!! You WIN!!!" - When human wins
  *  - "Sorry, but Pen wins" - When pen wins
  *  - "The Game ends with a Draw."
  *  - "Start Game" - When start game menu item was highlighted
  *  - "Help" - Help menu item
  *  - "About" - About menu item
  *  - "Rules" - Rules menu item
  *  - "How to draw game board" - menu item
  *  - "How to play the game" - menu item
  *  - "Easy" - menu item
  *  - "Hard" - menu item
  *  - "Line is to short"
  *  - "Line is not horizontal"
  *  - "Line is not vertical"
  *  - "Line must be at the right of previous one"
  *  - "Line must be at the bottom of previous one"
  *  - "Line must be near the other line"
  *  - "Line must cross both vertical lines"
  *  - "To start new game start drawing a new board"
  *
  */
 
 public class GameSoundManager {
 	/**
 	 * DI Container
 	 */
 	private Container container;
 	
 	/**
 	 * Resources
 	 */
 	private Hashtable resources;
 	
 	/**
 	 * MediaPlayer
 	 */
 	private MediaPlayer player;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param c
 	 *            DI container
 	 */
 	public GameSoundManager(Container c) {
 		this.container = c;
 		
 		// Initializing MediaPlayer
 		this.player = MediaPlayer.newInstance(this.getContainer().getPenletComponent());
 		
 		// Initializing resources
 		this.resources = new Hashtable(17);
 		this.resources.put("about", getSoundResourceByName("about"));
 		this.resources.put("congratulations-you-win", getSoundResourceByName("congratulations-you-win"));
 		this.resources.put("crosses-is-yours-make-your-turn", getSoundResourceByName("crosses-is-yours-make-your-turn"));
 		this.resources.put("easy", getSoundResourceByName("easy"));
 		this.resources.put("hard", getSoundResourceByName("hard"));
 		this.resources.put("help", getSoundResourceByName("help"));
 		this.resources.put("how-to-draw-game-board", getSoundResourceByName("how-to-draw-game-board"));
 		this.resources.put("how-to-play-the-game", getSoundResourceByName("how-to-play-the-game"));
 		this.resources.put("please-draw-first-horizontal-line", getSoundResourceByName("please-draw-first-horizontal-line"));
 		this.resources.put("please-draw-first-vertical-line", getSoundResourceByName("please-draw-first-vertical-line"));
 		this.resources.put("please-draw-second-horizontal-line", getSoundResourceByName("please-draw-second-horizontal-line"));
 		this.resources.put("please-draw-second-vertical-line", getSoundResourceByName("please-draw-second-vertical-line"));
 		this.resources.put("rules", getSoundResourceByName("rules"));
 		this.resources.put("sorry-but-pen-wins", getSoundResourceByName("sorry-but-pen-wins"));
 		this.resources.put("start-game", getSoundResourceByName("start-game"));
 		this.resources.put("the-game-ends-with-a-draw", getSoundResourceByName("the-game-ends-with-a-draw"));
 		this.resources.put("tick-tack-toe", getSoundResourceByName("tick-tack-toe"));
 		
 		this.resources.put("line-is-to-short", getSoundResourceByName("line-is-to-short"));
 		this.resources.put("line-must-be-at-the-right", getSoundResourceByName("line-must-be-at-the-right"));
 		this.resources.put("line-must-be-near", getSoundResourceByName("line-must-be-near"));
 		this.resources.put("line-must-cross", getSoundResourceByName("line-must-cross"));
 		this.resources.put("line-is-not-vertical", getSoundResourceByName("line-is-not-vertical"));
 		this.resources.put("line-must-be-at-the-bottom", getSoundResourceByName("line-must-be-at-the-bottom"));
 		this.resources.put("to-start-new-game-start-drawing", getSoundResourceByName("to-start-new-game-start-drawing"));
 		this.resources.put("line-is-not-horizontal", getSoundResourceByName("line-is-not-horizontal"));
 		
 		this.resources.put("SL_CA", getSoundResourceByName("SL_CA"));
 		
 		this.getContainer().getLoggerComponent().debug(
 				"[GameSound] Component initialized");
 	}
 	
 	/**
 	 * Returns container
 	 * 
 	 * @return container
 	 */
 	public Container getContainer() {
 		return container;
 	}
 	
 	private SoundResource getSoundResourceByName(String name) {
 		return this.container.getPenletComponent().getContext()
 				.getResourceBundle().getSoundResource(name);
 	}
 	
 	public void playSoundByName(String name, boolean blocked) {
 		this.player.stop();
 		if (this.resources.containsKey(name)) {
 			SoundResource res = (SoundResource) this.resources.get(name);
 			this.player.play(res.getInputStream(), res.getMimeType(), blocked);
 		}
 	}
 	
 	public void playMainMenuStartGame(boolean blocked) {
 		playSoundByName("start-game", blocked);
 	}
 	
 	public void playMainMenuHelp(boolean blocked) {
 		playSoundByName("help", blocked);
 	}
 	
 	public void playMainMenuAbout(boolean blocked) {
 		playSoundByName("about", blocked);
 	}
 	
 	public void playHelpMenuRules(boolean blocked) {
 		playSoundByName("rules", blocked);
 	}
 	
 	public void playHelpMenuHowToDrawGameBoard(boolean blocked) {
 		playSoundByName("how-to-draw-game-board", blocked);
 	}
 	
 	public void playHelpMenuHowToPlayTheGame(boolean blocked) {
 		playSoundByName("how-to-play-the-game", blocked);
 	}
 	
 	public void playDrawFirstVerticalLine(boolean blocked) {
 		playSoundByName("please-draw-first-vertical-line", blocked);
 	}
 
 	public void playDrawSecondVerticalLine(boolean blocked) {
 		playSoundByName("please-draw-second-vertical-line", blocked);
 	}
 
 	public void playDrawFirstHorizontalLine(boolean blocked) {
 		playSoundByName("please-draw-first-horizontal-line", blocked);
 	}
 
 	public void playDrawSecondHorizontalLine(boolean blocked) {
 		playSoundByName("please-draw-second-horizontal-line", blocked);
 	}
 	
 	public void playYourTurn(boolean blocked) {
 		playSoundByName("crosses-is-yours-make-your-turn", blocked);
 	}
 	
 	public void playYouWin(boolean blocked) {
 		playSoundByName("congratulations-you-win", blocked);
 	}
 	
 	public void playPenWins(boolean blocked) {
 		playSoundByName("sorry-but-pen-wins", blocked);
 	}
 	
 	public void playDraw(boolean blocked) {
 		playSoundByName("the-game-ends-with-a-draw", blocked);
 	}
 	
 	public void playEasy(boolean blocked) {
 		playSoundByName("easy", blocked);
 	}
 	
 	public void playHard(boolean blocked) {
 		playSoundByName("hard", blocked);
 	}
 		
 	public void playLineIsToShort(boolean blocked) {
 		playSoundByName("line-is-to-short", blocked);
 	}
 	
 	public void playLineIsNotVertical(boolean blocked) {
 		playSoundByName("line-is-not-vertical", blocked);
 	}
 	
 	public void playLineIsNotHorizontal(boolean blocked) {
 		playSoundByName("line-is-not-horizontal", blocked);
 	}
 	
 	public void playLineMustBeAtTheRight(boolean blocked) {
 		playSoundByName("line-must-be-at-the-right", blocked);
 	}
 	
 	public void playLineMustBeAtTheBottom(boolean blocked) {
 		playSoundByName("line-must-be-at-the-bottom", blocked);
 	}
 	
 	public void playLineMustBeNearOther(boolean blocked) {
 		playSoundByName("line-must-be-near", blocked);
 	}
 	
 	public void playLineMustCross(boolean blocked) {
 		playSoundByName("line-must-cross", blocked);
 	}
 
 	public void playToStartNewGameStartDrawingNewBoard(boolean blocked) {
 		playSoundByName("to-start-new-game-start-drawing", blocked);
 	}
 
 	public void playErrorDrawLine(int reason) {
 		switch (reason) {
 		case GameBoardLineException.REASON_LINE_IS_NOT_HORIZONTAL:
 			playLineIsNotHorizontal(true);
 			break;
 		case GameBoardLineException.REASON_LINE_IS_NOT_VERTICAL:
 			playLineIsNotVertical(true);
 			break;
 		case GameBoardLineException.REASON_LINE_TO_SHORT:
 			playLineIsToShort(true);
 			break;
 		case GameBoardLineException.REASON_MUST_BE_AT_THE_RIGHT:
 			playLineMustBeAtTheRight(true);
 			break;
 		case GameBoardLineException.REASON_MUST_BE_AT_THE_BOTTOM:
 			playLineMustBeAtTheBottom(true);
 			break;
 		case GameBoardLineException.REASON_MUST_BE_NEAR_THE_OTHER_LINES:
 			playLineMustBeNearOther(true);
 			break;
 		case GameBoardLineException.REASON_MUST_CROSS_BOTH_VERTICAL_LINES:
 			playLineMustCross(true);
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	public void playPenTurnReady() {
 		playSoundByName("SL_CA", false);
 	}
 }
