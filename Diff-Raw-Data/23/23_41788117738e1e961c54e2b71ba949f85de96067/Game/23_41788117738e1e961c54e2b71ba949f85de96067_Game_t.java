 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.button.*;
 import ca.couchware.wezzle2d.util.*;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.animation.*;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * The main hook of our game. This class with both act as a manager for the
  * display and central mediator for the game logic.
  * 
  * Display management will consist of a loop that cycles round all entities in
  * the game asking them to move and then drawing them in the appropriate place.
  * With the help of an inner class it will also allow the player to control the
  * main ship.
  * 
  * As a mediator it will be informed when entities within our game detect events
  * (e.g. alien killed, played died) and will take appropriate game actions.
  * 
  * @author Kevin Glass
  */
 public class Game extends Canvas implements GameWindowCallback
 {	  
     
     /**
      * The platform specific newline character.
      */
     public static String NL = System.getProperty("line.separator");
                
     /**
      * The width of the screen.
      */
     final public static int SCREEN_WIDTH = 800;
     
     /**
      * The height of the screen .
      */
     final public static int SCREEN_HEIGHT = 600;
     
     /**
      * The path to the resources.
      */
     final public static String RESOURCES_PATH = "resources";
 
     /**
      * The path to the fonts.
      */
     final public static String FONTS_PATH = RESOURCES_PATH + "/fonts";
     
     /**
      * The path to the sprites.
      */
     final public static String SPRITES_PATH = RESOURCES_PATH + "/sprites";
     
     /**
      * The path to the sounds.
      */
     final public static String SOUNDS_PATH = RESOURCES_PATH + "/sounds";
     
     /**
      * The path to the music.
      */
     final public static String MUSIC_PATH = RESOURCES_PATH + "/music";
     
     /**
      * The text color.
      */
     final public static Color TEXT_COLOR = new Color(252, 233, 45);
         
     /**
      * The line score color.
      */
     final public static Color SCORE_LINE_COLOR = new Color(252, 233, 45);
     
     /**
      * The piece score color.
      */
     final public static Color SCORE_PIECE_COLOR = new Color(240, 240, 240);
     
     /**
      * The bomb score color.
      */ 
     final public static Color SCORE_BOMB_COLOR = new Color(255, 127, 0);
   
     /**
      * The background layer.
      */
     final public static int LAYER_BACKGROUND = 0;
     
     /**
      * The board layer.
      */
     final public static int LAYER_TILE = 1;
     
     /**
      * The effects layer.
      */
     final public static int LAYER_EFFECT = 2;
     
     /**
      * The UI layer.
      */
     final public static int LAYER_UI = 3;
     
     /**
      * The build nunber path.
      */
     final public static String BUILD_NUMBER_PATH = 
             RESOURCES_PATH + "/build.number";
     
     /**
      * The current build number.
      */
     public String buildNumber;
     
     /**
      * The animation manager in charge of animations.
      */
     public AnimationManager animationMan;
     
     /**
 	 * The manager in charge of maintaining the board.
 	 */
 	public BoardManager boardMan;
 	
     /**
      * The layer manager.
      */
     public LayerManager layerMan;
     
     /** 
      * The manager in charge of the moves. 
      */
     public MoveManager moveMan;	
     
 	/**
 	 * The manager in charge of moving the piece around with the
 	 * pointer and drawing the piece to the board.
 	 */
 	public PieceManager pieceMan;
 	
     /** 
      * The manager in charge of loading and saving properties.
      */
     public PropertyManager propertyMan;
     
      /** 
       * The manager in charge of score.
       */
     public ScoreManager scoreMan;
 
     /** 
      * The manager in charge of sound.
      */
     public SoundManager soundMan;
     
     /**
      * The manager in charge of music.
      */
     public MusicManager musicMan;
     
     /** 
      * A flag to see if music is playing.
      */
     public boolean musicPlayingInProgress = false;
     
     /**
      * The Manager in charge of the world.
      */
     public WorldManager worldMan;
     
 	/** 
 	 * The manager in charge of keeping track of the time. 
 	 */
 	public TimerManager timerMan;
 	
     /**
      * The pause button.
      */
     public CircularBooleanButton pauseButton;
     
     /**
      * The progress bar.
      */
     public ProgressBar progressBar;
     
     /**
      * If true, refactor will be activated next loop.
      */
 	private boolean activateRefactor = false;
     
     /**
      * If true, the board is currently being refactored downwards.
      */
 	private boolean refactorDownInProgress = false;
     
     /**
      * If true, the board is currently being refactored leftward.
      */
 	private boolean refactorLeftInProgress = false;
     
     /**
      * The speed of the upcoming refactor.
      */
     private int refactorSpeed = 0;
     
     /**
      * If true, a line removal will be activated next loop.
      */
     private boolean activateLineRemoval = false;
     
     /**
      * If true, a line removal is in progress.
      */
     private boolean tileRemovalInProgress = false;
     
     /**
      * The set of tile indices that will be removed.
      */
     private Set tileRemovalSet;
     
     /**
      * If true, a bomb removal will be activated next loop.
      */
     private boolean activateBombRemoval = false;
     
     /**
      * The set of bomb tile indices that will be removed.
      */
     private Set bombRemovalSet;
     
     /**
      * If true, the board show animation will be activated next loop.
      */
     private boolean activateBoardShowAnimation = false;
     
     /**
      * If true, the board hide animation will be activated next loop.
      */
     private boolean activateBoardHideAnimation = false;
     
     /**
      * The animation that will indicate whether the board animation is 
      * complete.
      */
     private Animation boardAnimation = null;
     
     /**
      * If true, the game will end next loop.
      */
     private boolean activateGameOver = false;
             
     /**
      * Is a game over routine in progress?
      */
     private boolean gameOverInProgress = false;
     
     /**
      * The number of cascades thus far.
      */
     private int cascadeCount;
     
 	/**
 	 * The time at which the last rendering looped started from the point of
 	 * view of the game logic.
 	 */
 	private long lastLoopTime;
 	
 	/** 
      * The window that is being used to render the game. 
      */
 	private GameWindow window;
 
 	/** 
      * The time since the last record of FPS. 
      */
 	private long lastFramesPerSecondTime = 0;
 	
 	/** 
      * The recorded FPS. 
      */
 	private int framesPerSecond;
 
     /**
      * The name of the application.
      */
     private String applicationName = "Wezzle";
     
 	/** 
      * The normal title of the window. 
      */
 	private String windowTitle = applicationName;	    
     
     /**
      * The background sprite.
      */
     private Entity background;
     
 	/** 
      * The timer text. 
      */
 	private Label timerLabel;
         
     /** 
      * The score text.
      */
     private Label scoreLabel;
     
     /** 
      * The high score text. 
      */
     private Label highScoreLabel;
     
     /**
      * The level text.
      */
     private Label levelLabel;
     
     /** 
      * The move count text.
      */
     private Label movesLabel;
     
     /**
      * The "Paused" text.
      */
     private Label pausedLabel;
     
     /**
      * The version text.
      */
     private Label versionLabel;        
         
 	/**
 	 * Construct our game and set it running.
 	 * 
 	 * @param renderingType
 	 *            The type of rendering to use (should be one of the contansts
 	 *            from ResourceFactory)
 	 */
 	public Game(int renderingType) 
 	{
         // Get the build number.
         Properties buildProperties = new Properties();            
                
         try
         {
             URL url = this.getClass().getClassLoader()
                 .getResource(BUILD_NUMBER_PATH);  
             
             InputStream in = url.openStream();
             buildProperties.load(in);
             in.close();
             
             buildNumber = 
                     (String) buildProperties.getProperty("build.number");
         }
         catch (Exception e)
         {
             Util.handleException(e);
             Util.handleWarning("Could not find build number at: "
                     + BUILD_NUMBER_PATH + "!",
                     Thread.currentThread());
             buildNumber = "???";
         }
         finally
         {
             if (buildNumber == null)
                 buildNumber = "???";
         }
         
         // Print the build number.
         Util.handleMessage("Wezzle Build " + buildNumber + " @ " + (new Date()),
                 Thread.currentThread());
         
 		// Create a window based on a chosen rendering method.
 		ResourceFactory.get().setRenderingType(renderingType);		        
         
 		final Runnable r = new Runnable()
 		{
 			public void run()
 			{                             
 				window = ResourceFactory.get().getGameWindow();
 				window.setResolution(SCREEN_WIDTH, SCREEN_HEIGHT);
 				window.setGameWindowCallback(Game.this);
 				window.setTitle(windowTitle);
 			}		
 		};
 		
 		try
 		{
 			javax.swing.SwingUtilities.invokeAndWait(r);
 		}
 		catch (InterruptedException e)
 		{
 			Util.handleException(e);
 		}
 		catch (InvocationTargetException e)
 		{
 			Util.handleException(e);
 		}		
 	}
 
 	public void startRendering()
 	{
 		window.startRendering();
 	}
     
 	/**
 	 * Initialize the common elements for the game
 	 */
 	public void initialize()
 	{
         // Set cascade count to 0.
         cascadeCount = 0;
         
         // Initialize line index set.
         tileRemovalSet = new HashSet();
         
         // Initialize bomb index set.
         bombRemovalSet = new HashSet();
         
         // Create the layer manager.   
         layerMan = new LayerManager(window, 4);        
         
         // Create the animation manager.
         animationMan = new AnimationManager();
         
 		// Create the board manager.
 		boardMan = new BoardManager(layerMan, 272, 139, 8, 10);        
         
 		// Create the piece manager.
 		pieceMan = new PieceManager(boardMan);
         layerMan.add(pieceMan.getPieceGrid(), LAYER_EFFECT);
 		window.addMouseListener(pieceMan);
 		window.addMouseMotionListener(pieceMan);	
 	
         // Create the property manager. Must be done before Score manager.
         propertyMan = new PropertyManager();
         
         // Create the score manager.
         scoreMan = new ScoreManager(boardMan, propertyMan);
 
         // Create the world manager.
         worldMan = new WorldManager(propertyMan);
         
         // Generate the game board.
         boardMan.generateBoard(worldMan.getItemList());
         boardMan.setVisible(false);
         startBoardShowAnimation();
         
         // Create the sound manager.
         soundMan = new SoundManager();
         
         // Create the music manager.
         musicMan = new MusicManager(this);
         
         // Create the move manager.
         moveMan = new MoveManager();
         
         // Create the time manager.
 		timerMan = new TimerManager(worldMan.getInitialTimer());
                     
         // Draw the current background.
 		background = new Entity(SPRITES_PATH + "/Background1.png", 0, 0);
         layerMan.add(background, LAYER_BACKGROUND);
         
         // Create a new pause button.
         pauseButton = new CircularBooleanButton(18, 600 - 18);
         pauseButton.setText("Pause");
         pauseButton.setAlignment(Button.BOTTOM | Button.LEFT);
         layerMan.add(pauseButton, LAYER_UI);
         window.addMouseListener(pauseButton);
         window.addMouseMotionListener(pauseButton);
         
         // Create the "Paused" text.
         pausedLabel = ResourceFactory.get().getText();
         pausedLabel.setXYPosition(400, 300);      
 		pausedLabel.setSize(30);
 		pausedLabel.setAlignment(Label.HCENTER | Label.VCENTER);
 		pausedLabel.setColor(TEXT_COLOR);
         pausedLabel.setText("Paused");
         pausedLabel.setVisible(false);
         layerMan.add(pausedLabel, LAYER_UI);
               
         // Set up the version text.
 		versionLabel = ResourceFactory.get().getText();
         versionLabel.setXYPosition(800 - 10, 600 - 10);
 		versionLabel.setSize(12);
 		versionLabel.setAlignment(Label.BOTTOM | Label.RIGHT);
 		versionLabel.setColor(TEXT_COLOR);
         versionLabel.setText(applicationName + " Build " + buildNumber);
         layerMan.add(versionLabel, LAYER_UI);
         
 		// Set up the timer text.
 		timerLabel = ResourceFactory.get().getText();
         timerLabel.setXYPosition(404, 100);
 		timerLabel.setSize(50);
 		timerLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
 		timerLabel.setColor(TEXT_COLOR);
         layerMan.add(timerLabel, LAYER_UI);
                 
         // Set up the score text.
         scoreLabel = ResourceFactory.get().getText();
         scoreLabel.setXYPosition(126, 400); 
         scoreLabel.setSize(20);
         scoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         scoreLabel.setColor(TEXT_COLOR);     
         scoreMan.setTargetLevelScore(
                 worldMan.generateTargetLevelScore());
         layerMan.add(scoreLabel, LAYER_UI);
         
         // Set up the high score text.
         highScoreLabel = ResourceFactory.get().getText();
         highScoreLabel.setXYPosition(126, 270);
         highScoreLabel.setSize(20);
         highScoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         highScoreLabel.setColor(TEXT_COLOR);
         layerMan.add(highScoreLabel, LAYER_UI);
         
         // Set up the level text.
         levelLabel = ResourceFactory.get().getText();
         levelLabel.setXYPosition(669, 270);
         levelLabel.setSize(20);
         levelLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         levelLabel.setColor(TEXT_COLOR);
         layerMan.add(levelLabel, LAYER_UI);
         
         // Set up the move count text.
         movesLabel = ResourceFactory.get().getText();
         movesLabel.setXYPosition(669, 400);
         movesLabel.setSize(20);
         movesLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         movesLabel.setColor(TEXT_COLOR);
         layerMan.add(movesLabel, LAYER_UI);
              
         // Create the progress bar.
         progressBar = new ProgressBar(400, 508, 186, true);
         progressBar.setAlignment(ProgressBar.VCENTER | ProgressBar.HCENTER);
         progressBar.setProgressMax(scoreMan.getTargetLevelScore());
         layerMan.add(progressBar, LAYER_UI);
         
 		// Setup the initial game state.
 		startGame();
 	}
 
 	/**
 	 * Start a fresh game, this should clear out any old data and create a new
 	 * set.
 	 */
 	private void startGame()
 	{		
 		lastLoopTime = SystemTimer.getTime();
 	}
     
      /**
      * A private helper method to handle game pausing.
      */
     private void pauseGame()
     {
         pauseButton.setText("Resume");
         pauseButton.getLabel().setSize(18);
         layerMan.hide(LAYER_TILE);
         layerMan.hide(LAYER_EFFECT);                
         pausedLabel.setVisible(true);
     }
     
     /**
      * A private helper method to handle game resuming.
      */
     private void resumeGame()
     {
         pauseButton.setText("Pause");
         pauseButton.getLabel().setSize(22);
         layerMan.show(LAYER_TILE);
         layerMan.show(LAYER_EFFECT);
         pausedLabel.setVisible(false);
 
         // Clear clicks.
         pieceMan.clearMouseButtons();
     }
     
     /**
      * Start a refactor with the given speed.
      * 
      * @param speed
      */
     public void startRefactor(int speed)
     {
         // Set the refactor flag.
         this.activateRefactor = true;
         this.refactorSpeed = speed;
     }
     
     /**
      * Clear the refactor flag.
      */
     public void clearRefactor()
     {
        // Set the refactor flag.
        this.activateRefactor = false;
     }      
    
     /**
      * A method to check whether the board is busy.
      * 
      * @return True if it is, false otherwise.
      */
     public boolean isBusy()
     {
        return (isRefactoring()               
                || isTileRemoving()
                || this.boardAnimation != null);
     }
     
     /**
      * Checks whether a refactor is, or is about to be, in progress.
      */
     public boolean isRefactoring()
     {
         return this.activateRefactor 
                 || this.refactorDownInProgress 
                 || this.refactorLeftInProgress;
     }
     
     /**
      * Checks whether tiles are, or are about to be, removed.
      */
     public boolean isTileRemoving()
     {
         return this.activateLineRemoval               
                || this.activateBombRemoval 
                || this.tileRemovalInProgress;
     }
     
     public void startBoardShowAnimation()
     {
         // Set the flag.
         activateBoardShowAnimation = true;
     }
     
     public void clearBoardShowAnimation()
     {
         // Clear the flag.
         activateBoardShowAnimation = false;
     }
     
     public void startBoardHideAnimation()
     {
         // Set the flag.
         activateBoardHideAnimation = true;
     }
     
     public void clearBoardHideAnimation()
     {
         // Clear the flag.
         activateBoardHideAnimation = false;
     }
     
     public void startGameOver()
     {
         Util.handleMessage("Game over!", Thread.currentThread());
         activateGameOver = true;
     }
     
     public void clearGameOver()
     {
         activateGameOver = false;
     }
 
 	/**
 	 * Notification that a frame is being rendered. Responsible for running game
 	 * logic and rendering the scene.
 	 */
 	public boolean frameRendering()
 	{
 		SystemTimer.sleep(lastLoopTime + 10 - SystemTimer.getTime());
 
 		// Work out how long its been since the last update, this
 		// will be used to calculate how far the entities should
 		// move this loop.
 		long delta = SystemTimer.getTime() - lastLoopTime;
 		lastLoopTime = SystemTimer.getTime();
 		lastFramesPerSecondTime += delta;
 		framesPerSecond++;
 
 		// Update our FPS counter if a second has passed.
 		if (lastFramesPerSecondTime >= 1000)
 		{
 			window.setTitle(windowTitle + " (FPS: " + framesPerSecond + ")");
 			lastFramesPerSecondTime = 0;
 			framesPerSecond = 0;
 		}
         
         // If the pause button was just clicked.
         if (pauseButton.clicked() == true)
         {
             // If it was clicked on, then hide the board and
             // show the paused text.
             if (pauseButton.isActivated() == true)
             {
                 this.pauseGame();
             }
             else
             {
                 this.resumeGame();
             }
         }
 		
         // If the music stopped playing, play the next song.
         if(this.musicPlayingInProgress == false)
         {
             musicMan.playNext();
         }
         
         
         // If the pause button is not on, then we proceed with the
         // normal game loop.
         if (pauseButton.isActivated() == false)
         {   
             // See if it's time to level-up.
             if (pieceMan.isTileDropInProgress() == false
                     && isBusy() == false)
             {
                 // Handle Level up.
                 if (scoreMan.getLevelScore() 
                         >= scoreMan.getTargetLevelScore())
                 {           
                     Util.handleMessage("Level up!!!", Thread.currentThread());
                     worldMan.levelUp(this);
                     
                     final XYPosition p = levelLabel.getXYPosition();
                     
                     soundMan.play(SoundManager.LEVEL_UP);
                     
                     animationMan.add(new FloatLabelAnimation(
                             pieceMan.getPieceGrid().getX() 
                                 + boardMan.getCellWidth() / 2,
                             pieceMan.getPieceGrid().getY() 
                                 + boardMan.getCellHeight() / 2,
                             1, 0, layerMan,
                             "Level Up!", 
                             Label.LEFT | Label.VCENTER,
                             Game.TEXT_COLOR,
                             26));                            
                 }
             } // end if
             
             // See if it's game ovaries.
             if (activateGameOver == true)
             {
                 // Clear flag.
                 clearGameOver();
                 
                 // Reset a bunch of stuff.
                 timerMan.resetTimer();
                 worldMan.setCurrentLevel(1);
                 scoreMan.setLevelScore(0);
                 scoreMan.setTargetLevelScore(
                         worldMan.generateTargetLevelScore(1));                
                 progressBar.setProgressMax(
                         scoreMan.getTargetLevelScore());
                 scoreMan.setTotalScore(0);                
                 moveMan.setMoveCount(0);
                 
                 // Set in progress flag.
                 gameOverInProgress = true;
                 
                 // Hide the board.
                 startBoardHideAnimation();                
             }
             
             // Check on board animation.
             if (boardAnimation != null && boardAnimation.isDone() == true)
             {
                 // Set animation visible depending on what animation
                 // was just performed.
                 if (boardAnimation instanceof FadeInAnimation)   
                 {
                     boardMan.setVisible(true);
                     pieceMan.getPieceGrid().setVisible(true);
                 }
                 else if (boardAnimation instanceof FadeOutAnimation)
                 {
                     boardMan.setVisible(false);
                     pieceMan.getPieceGrid().setVisible(false);
                 }
                 else
                     throw new RuntimeException(
                             "Unrecognized board animation class.");
                 
                 // Clear the board animation.
                 boardAnimation = null;
                 
                 // Claer mouse button presses.
                 pieceMan.clearMouseButtons();
                 
                 // If game over is in progress, make a new board and start.
                 if (gameOverInProgress == true)
                 {
                     // Create board and make it invisible.
                     boardMan.generateBoard(worldMan.getItemList());
                     boardMan.setVisible(false);
                     
                     // Unpause the game.
                     // Don't worry! The game won't pass updates to the 
                     // timer unless the board is shown.  In hindsight, this
                     // is kind of crappy, but whatever, we'll make it prettier
                     // one day.
                     timerMan.setPaused(false);
                     
                     // Reset the timer. And the score.
                     timerMan.setInitialTime(worldMan.getInitialTimer());
                     timerMan.resetTimer();
                     scoreMan.setLevelScore(0);
                     
                     // Start the board show animation.  This will
                     // make the board visible when it's done.
                     startBoardShowAnimation();
                     
                     // Clear the flag.
                     gameOverInProgress = false;
                 }
             }
             else if (boardAnimation != null && boardAnimation.isDone() == false)
             {
                 // Board is still dirty due to animation.
                 boardMan.setDirty(true);
             }
             
             // Check to see if we should be showing the board.
             if (activateBoardShowAnimation == true)
             {
                 // Start board show animation.
                 boardAnimation = boardMan.animateShow(animationMan);     
                 boardMan.setDirty(true);
                 
                 // Hide the piece.
                 pieceMan.getPieceGrid().setVisible(false);                               
                 
                 // Clear flag.
                 clearBoardShowAnimation();                                
             }
             
             // Check to see if we should be hiding the board.
             if (activateBoardHideAnimation == true)
             {
                 // Start board hide animation.
                 boardAnimation = boardMan.animateHide(animationMan); 
                 boardMan.setDirty(true);
                 
                 // Hide the piece.
                 pieceMan.getPieceGrid().setVisible(false);                               
                 
                 // Clear flag.
                 clearBoardHideAnimation();                                
             }                        
             
             // See if we need to activate the refactor.
             if (activateRefactor == true)
             {            
                 // Hide piece.
                 pieceMan.getPieceGrid().setVisible(false);
 
                 // Start down refactor.
                 boardMan.startShiftDown(refactorSpeed);
                 refactorDownInProgress = true;
 
                 // Clear flag.
                 clearRefactor();
             }
 
             // See if we're down refactoring.
             if (refactorDownInProgress == true)
             {
                 if (boardMan.moveAll(delta) == false)
                 {			
                     // Clear down flag.
                     refactorDownInProgress = false;
 
                     // Synchronize board.
                     boardMan.synchronize();							
 
                     // Start left refactor.
                     boardMan.startShiftLeft(refactorSpeed);
                     refactorLeftInProgress = true;								
                 }
             } // end if
 
             // See if we're left refactoring.
             if (refactorLeftInProgress == true)
             {
                 if (boardMan.moveAll(delta) == false)
                 {
                     // Clear left flag.
                     refactorLeftInProgress = false;
 
                     // Synchronize board.
                     boardMan.synchronize();		
 
                     // Look for matches.
                     tileRemovalSet.clear();
                     
                     boardMan.findXMatch(tileRemovalSet);
                     boardMan.findYMatch(tileRemovalSet);
 
                     // If there are matches, score them, remove 
                     // them and then refactor again.
                     if (tileRemovalSet.size() > 0)
                     {                                       
                         // Activate the line removal.
                         activateLineRemoval = true;                 
                     }
                     else
                     {
                        // Make sure the tiles are not still dropping.
                         if (pieceMan.isTileDropInProgress() == false)
                         {                            
                             pieceMan.loadRandomPiece();   
                             pieceMan.getPieceGrid().setVisible(true);
 
                             // Unpause the timer.
                             timerMan.resetTimer();
                             timerMan.setPaused(false);
 
                             // Reset the mouse.
                             pieceMan.clearMouseButtons();
                         }
                     }
                 } // end if
 
                 // Notify piece manager.
                 pieceMan.notifyRefactored();
             } // end if
 
             // If a line removal was activated.
             if (activateLineRemoval == true)
             {                
                 // Clear flag.
                 activateLineRemoval = false;
 
                 // Increment cascade.
                 cascadeCount++;
                 
                 // Calculate score.
                 final int deltaScore = scoreMan.calculateLineScore(
                         tileRemovalSet, 
                         ScoreManager.TYPE_LINE,
                         cascadeCount);                               
                 
                 // Show the SCT.
                 animationMan.add(new FloatLabelAnimation(
                         boardMan.determineCenterPoint(tileRemovalSet),                         
                         0, -1, layerMan, 
                         String.valueOf(deltaScore),
                         Label.HCENTER | Label.VCENTER,
                         SCORE_LINE_COLOR,
                         scoreMan.determineFontSize(deltaScore)));
                 
                 // Play the sound.
                 soundMan.play(SoundManager.LINE);
 
                 // Make sure bombs aren't removed (they get removed
                 // in a different step).
                 boardMan.scanBombs(tileRemovalSet, bombRemovalSet);
                 tileRemovalSet.removeAll(bombRemovalSet);                
                 
                 // Start the line removal animations if there are any
                 // non-bomb tiles.
                 if (tileRemovalSet.size() > 0)
                 {
                     for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                     {
                         TileEntity t = boardMan.getTile((Integer) it.next());
                         t.setAnimation(new ZoomOutAnimation(t));
                     }
 
                     // Set the flag.
                     tileRemovalInProgress = true;
                 }
                 // Otherwise, start the bomb processing.
                 else
                 {
                     activateBombRemoval = true;
                 }
             }
 
             // If a bomb removal is in progress.
             if (activateBombRemoval == true)
             {
                 // Clear the flag.
                 activateBombRemoval = false;
                 
                 // Increment cascade.
                 cascadeCount++;
 
                 // Used below.
                 int deltaScore = 0;
                 
                 // Get the tiles the bombs would affect.
                 boardMan.processBombs(bombRemovalSet, tileRemovalSet);
                 deltaScore = scoreMan.calculateLineScore(
                         tileRemovalSet, 
                         ScoreManager.TYPE_BOMB, 
                         cascadeCount);
                 
                 // Show the SCT.
                 animationMan.add(new FloatLabelAnimation(
                         boardMan.determineCenterPoint(tileRemovalSet), 
                         0, -1, layerMan, 
                         String.valueOf(deltaScore),
                         Label.HCENTER | Label.VCENTER,
                         SCORE_BOMB_COLOR,
                         scoreMan.determineFontSize(deltaScore)));
                                 
                 // Play the sound.
                 soundMan.play(SoundManager.BOMB);
 
                 // Extract all the new bombs.
                 Set newBombRemovalSet = new HashSet();
                 boardMan.scanBombs(tileRemovalSet, newBombRemovalSet);                                                
                 newBombRemovalSet.removeAll(bombRemovalSet);
 
                 // Remove all tiles that aren't new bombs.
                 tileRemovalSet.removeAll(newBombRemovalSet);
 
                 // Start the line removal animations.
                 for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                 {
                     TileEntity t = boardMan.getTile((Integer) it.next());
 
                     if (t instanceof BombTileEntity)                    
                         t.setAnimation(new ExplosionAnimation(t, layerMan));                                            
                     else
                         t.setAnimation(new JiggleFadeAnimation(t));
                 }
 
                 // If other bombs were hit, they will be dealt with in another
                 // bomb removal cycle.
                 bombRemovalSet = newBombRemovalSet;
 
                 // Set the flag.
                 tileRemovalInProgress = true;
             }
 
             // If a line removal is in progress.
             if (tileRemovalInProgress == true)
             {
                 // Animation completed flag.
                 boolean animationInProgress = false;
 
                 // Check to see if they're all done.
                 for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                 {
                     if (boardMan.getTile((Integer) it.next()).getAnimation()
                             .isDone() == false)
                     {
                         animationInProgress = true;
                     }
                 }
 
                 if (animationInProgress == false)
                 {
                     // Remove the tiles from the board.
                     boardMan.removeTiles(tileRemovalSet);
 
                     // Bomb removal is completed.
                     tileRemovalInProgress = false;
 
                     // See if there are any bombs in the bomb set.
                     // If there are, activate the bomb removal.
                     if (bombRemovalSet.size() > 0)
                         activateBombRemoval = true;
                     // Otherwise, start a new refactor.
                     else                
                         startRefactor(200);
                 }  
             }
             
             // See if we should clear the cascade count.
             if (isRefactoring() == false 
                     && isTileRemoving() == false
                     && pieceMan.isTileDropInProgress() == false)
                 cascadeCount = 0;
 
             // Animate all the pieces.
             boardMan.animate(delta);    
             
             // Animation all animations.
             animationMan.animate(delta);
 
             // Handle the timer.
             if (boardMan.isVisible() == true)
                 timerMan.incrementInternalTime(delta);
 
             // Check to see if we should force a piece commit.
             if (timerMan.getTime() < 0)
             {
                 // Commit the piece.
                 timerMan.setTime(0);
                 pieceMan.initiateCommit(this);            
             }
 
             // Update piece manager logic and then draw it.
             pieceMan.updateLogic(this);
             
             // Draw the timer text.
             timerLabel.setText(String.valueOf(timerMan.getTime()));		
 
             // Draw the score text.
             scoreLabel.setText(String.valueOf(scoreMan.getTotalScore()));
 
             // Draw the high score text.
             highScoreLabel.setText(String.valueOf(scoreMan.getHighScore()));
 
             // Set the level text.
             levelLabel.setText(String.valueOf(worldMan.getLevel()));
 
             // Draw the move count text.
             movesLabel.setText(String.valueOf(moveMan.getMoveCount()));
             
             // Update the progress bar.
             progressBar.setProgress(scoreMan.getLevelScore());
         }                
                 
         // Whether or not the frame was updated.
         boolean updated = false;
         
         // If the background is dirty, then redraw everything.
         if (background.isDirty() == true)
         {            
             layerMan.draw();
             background.setDirty(false);
             updated = true;
         }
         // Otherwise, only draw what needs to be redrawn.
         else
         {           
             updated = layerMan.drawRegion(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);         
         }
 		
 		// if escape has been pressed, stop the game
 		if (window.isKeyPressed(KeyEvent.VK_ESCAPE))
 		{
 			System.exit(0);
 		}
         
         return updated;
 	}
 
 	/**
 	 * Notification that the game window has been closed
 	 */
 	public void windowClosed()
 	{
         if (this.propertyMan != null)
         {            
             try
             {
                 propertyMan.saveProperties();
             }
             catch(Exception e)
             {
                 Util.handleException(e);
             }
         }
         else
         {
             // Do nothin. The property man is not yet initialized.
         }
 		System.exit(0);
 	}
     
     /**
      * Notification that the game window has been deactivated in some way.
      */
     public void windowDeactivated()
     {
         this.pauseButton.setActivated(true);
         this.pauseGame();
         this.background.setDirty(true);
     }
     
      /**
      * Notification that the game window has been reactivated in some way.
      */
    public void windowActivated()
     {
         //Force a background redraw.
        if(this.background != null)
            this.background.setDirty(true);
     }
     
 	/**
 	 * The entry point into the game. We'll simply create an instance of class
 	 * which will start the display and game loop.
 	 * 
 	 * @param argv
 	 *            The arguments that are passed into our game
 	 */
 	public static void main(String argv[])
 	{		
         try
         {
             Game g = new Game(ResourceFactory.JAVA2D);
             g.startRendering();		
         }
         catch (Exception e)
         {
             Util.handleException(e);
         }
 	}
     
   
 }
