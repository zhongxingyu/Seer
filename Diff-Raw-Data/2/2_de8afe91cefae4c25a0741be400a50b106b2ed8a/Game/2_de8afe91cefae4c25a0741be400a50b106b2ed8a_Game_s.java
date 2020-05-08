 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.animation.*;
 import ca.couchware.wezzle2d.music.MusicManager;
 import ca.couchware.wezzle2d.sound.SoundManager;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.ui.*;
 import ca.couchware.wezzle2d.ui.button.*;
 import ca.couchware.wezzle2d.ui.group.*;
 import ca.couchware.wezzle2d.util.*;
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
     //--------------------------------------------------------------------------
     // Static Members
     //--------------------------------------------------------------------------
     
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
      * The level header path.
      */
     final private static String LEVEL_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_Level.png";
     
     /**
      * The score header path.
      */
     final private static String SCORE_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_Score.png";
     
     /**
      * The high score header path.
      */
     final private static String HIGH_SCORE_HEADER_PATH = Game.SPRITES_PATH 
             + "/Header_HighScore.png";       
     
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
     
     //--------------------------------------------------------------------------
     // Public Members
     //--------------------------------------------------------------------------
     
     /**
      * The current build number.
      */
     public String buildNumber;
     
     /**
      * The menu manager.
      */
     public GroupManager groupMan;
     
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
      * The high score manager.
      */    
     public HighScoreManager highScoreMan;
     
     /** 
      * The manager in charge of sound.
      */
     public SoundManager soundMan;
     
     /**
      * The manager in charge of music.
      */
     public MusicManager musicMan;       
     
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
     public RectangularBooleanButton pauseButton;
        
     /**
      * The options button.
      */
     public RectangularBooleanButton optionsButton;
     
     /**
      * The help button.
      */
     public RectangularBooleanButton helpButton;
     
     /**
      * The progress bar.
      */
     public ProgressBar progressBar;
     
     //--------------------------------------------------------------------------
     // Private Members
     //--------------------------------------------------------------------------
     
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
     private Set<Integer> tileRemovalSet;
     
     /**
      * If true, uses jump animation instead of zoom.
      */
     private boolean tileRemovalUseJumpAnimation = false;
     
     /**
      * If true, award no points for this tile removal.
      */
     private boolean tileRemovalNoScore = false;
     
     /**
      * If true, do not activate items on this removal.
      */
     private boolean tileRemovalNoItems = false;
     
     /**
      * If true, a bomb removal will be activated next loop.
      */
     private boolean activateBombRemoval = false;
     
     /**
      * The set of bomb tile indices that will be removed.
      */
     private Set<Integer> bombRemovalSet;       
     
     /**
      * The number of lines cleared this cycle, where a cycle is defined as the
      * period from the moment the piece is clicked, to when the board finally
      * stops finding lines.
      */
     private int cycleLineCount;
     
     /**
      * The total number of lines cleared.
      */
     private int totalLineCount;
     
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
      * The score header graphic.
      */
     private GraphicEntity scoreHeaderLabel;
     
     /** 
      * The score text.
      */
     private Label scoreLabel;
     
     /**
      * The high score header graphic.
      */
     private GraphicEntity highScoreHeaderLabel;
     
     /** 
      * The high score text. 
      */
     private Label highScoreLabel;
     
     /**
      * The level header graphic.
      */
     private GraphicEntity levelHeader;
     
     /**
      * The level text.
      */
     private Label levelLabel;            
     
     /**
      * The version text.
      */
     private Label versionLabel;     
     
     /**
      * The pause group.
      */
     private PauseGroup pauseGroup;
     
     /**
      * The game over group.
      */
     private GameOverGroup gameOverGroup;    
     
     /**
      * The options group.
      */
     private OptionsGroup optionsGroup;
     
      /**
      * The high score group.
      */
     private HighScoreGroup highScoreGroup;
     
     //--------------------------------------------------------------------------
     // Constructor
     //--------------------------------------------------------------------------
     
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
         //----------------------------------------------------------------------
         // Initialize attributes.
         //----------------------------------------------------------------------
         
         // Set the cycle line count to 0.
         setCycleLineCount(0);
         
         // Set the total line count to 0.       
         setTotalLineCount(0);
         
         // Set cascade count to 0.
         setCascadeCount(0);
         
         // Initialize line index set.
         tileRemovalSet = new HashSet<Integer>();
         
         // Initialize bomb index set.
         bombRemovalSet = new HashSet<Integer>();
         
         //----------------------------------------------------------------------
         // Initialize managers.
         //----------------------------------------------------------------------
         
         // Create the layer manager.   
         layerMan = new LayerManager(window, 4);        
         
         // Draw the current background.
 		background = new GraphicEntity(SPRITES_PATH + "/Background2.png", 0, 0);
         layerMan.add(background, LAYER_BACKGROUND);                
         
         // Create the animation manager.
         animationMan = new AnimationManager();
         
 		// Create the board manager.
 		boardMan = new BoardManager(layerMan, 272, 139, 8, 10);        
         
 		// Create the piece manager.
 		pieceMan = new PieceManager(boardMan);
         layerMan.add(pieceMan.getPieceGrid(), LAYER_EFFECT);
 		window.addMouseListener(pieceMan);
 		window.addMouseMotionListener(pieceMan);	
         
         // Create group manager.
         groupMan = new GroupManager(layerMan, pieceMan);
 	
         // Create the property manager. Must be done before Score manager.
         propertyMan = new PropertyManager();
         Util.handleMessage(
                 propertyMan.getStringProperty(PropertyManager.KEY_MUSIC_MIN),
                 Thread.currentThread());
         
         // Create the high score manager.
         highScoreMan = new HighScoreManager(propertyMan);
         
         // Create the score manager.
         scoreMan = new ScoreManager(boardMan, propertyMan, highScoreMan);
 
         // Create the world manager.
         worldMan = new WorldManager(propertyMan);
         
         // Generate the game board.
         boardMan.generateBoard(worldMan.getItemList());
         boardMan.setVisible(false);
         startBoardShowAnimation();
         
         // Create the sound manager.
         soundMan = new SoundManager(propertyMan);
         
         // Create the music manager.
         musicMan = new MusicManager(propertyMan);                  
         
         // Create the move manager.
         moveMan = new MoveManager();
         
         // Create the time manager.
 		timerMan = new TimerManager(worldMan.getInitialTimer());                            
         
         //----------------------------------------------------------------------
         // Initialize buttons.
         //----------------------------------------------------------------------                                      
         
         // Create the help buttton.
         helpButton = new RectangularBooleanButton(window, 668, 387);
         helpButton.setNormalOpacity(70);
         helpButton.setText("Help");
         helpButton.getLabel().setSize(18);
         helpButton.setAlignment(Button.VCENTER | Button.HCENTER);
         layerMan.add(helpButton, LAYER_UI);   
         
         // Create the options button.
         optionsButton = new RectangularBooleanButton(window, 668, 299);
         optionsButton.setNormalOpacity(70);
         optionsButton.setText("Options");
         optionsButton.getLabel().setSize(18);
         optionsButton.setAlignment(Button.VCENTER | Button.HCENTER);
         layerMan.add(optionsButton, Game.LAYER_UI);
         
         // Create pause button.        
         pauseButton = new RectangularBooleanButton(window, 668, 211)
         {
             // Make it so the button text changes to resume when
             // the button is activated.
             // Kevin: Be sure to read the comment in BooleanButton before
             // using this.
             @Override
             public void onActivation()
             {
                 this.setText("Resume");
             }
             
             // Make it so the button text changes to pause when
             // the button is deactivated.
             @Override
             public void onDeactivation()
             {
                 this.setText("Pause");
             }
         };
         pauseButton.setNormalOpacity(70);
         pauseButton.setText("Pause");
         pauseButton.getLabel().setSize(18);
         pauseButton.setAlignment(Button.VCENTER | Button.HCENTER);        
         layerMan.add(pauseButton, Game.LAYER_UI);               
         
         //----------------------------------------------------------------------
         // Initialize labels.
         //----------------------------------------------------------------------                
               
         // Set up the version text.
 		versionLabel = ResourceFactory.get().getLabel(800 - 10, 600 - 10);        
 		versionLabel.setSize(12);
 		versionLabel.setAlignment(Label.BOTTOM | Label.RIGHT);
 		versionLabel.setColor(TEXT_COLOR);
         versionLabel.setText(applicationName + " Build " + buildNumber);
         layerMan.add(versionLabel, LAYER_UI);
         
 		// Set up the timer text.
 		timerLabel = ResourceFactory.get().getLabel(404, 100);        
 		timerLabel.setSize(50);
 		timerLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
 		timerLabel.setColor(TEXT_COLOR);
         layerMan.add(timerLabel, LAYER_UI);
              
         // Set up the level header.
         levelHeader = new GraphicEntity(LEVEL_HEADER_PATH, 126, 153);
         levelHeader.setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);
         layerMan.add(levelHeader, LAYER_UI);
         
         // Set up the level text.
         levelLabel = ResourceFactory.get().getLabel(126, 210);        
         levelLabel.setSize(20);
         levelLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         levelLabel.setColor(TEXT_COLOR);
         layerMan.add(levelLabel, LAYER_UI);        
         
         // Set up the score header.
         highScoreHeaderLabel = 
                 new GraphicEntity(HIGH_SCORE_HEADER_PATH, 127, 278);        
         highScoreHeaderLabel
                 .setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);        
         layerMan.add(highScoreHeaderLabel, LAYER_UI);
                         
         // Set up the high score text.
         highScoreLabel = ResourceFactory.get().getLabel(126, 337);        
         highScoreLabel.setSize(20);
         highScoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         highScoreLabel.setColor(TEXT_COLOR);
         layerMan.add(highScoreLabel, LAYER_UI);
         
         // Set up the score header.
         scoreHeaderLabel = new GraphicEntity(SCORE_HEADER_PATH, 128, 403);
         scoreHeaderLabel
                 .setAlignment(GraphicEntity.VCENTER | GraphicEntity.HCENTER);
         layerMan.add(scoreHeaderLabel, LAYER_UI);
         
         // Set up the score text.
         scoreLabel = ResourceFactory.get().getLabel(126, 460);        
         scoreLabel.setSize(20);
         scoreLabel.setAlignment(Label.BOTTOM | Label.HCENTER);
         scoreLabel.setColor(TEXT_COLOR);     
         scoreMan.setTargetLevelScore(
                 worldMan.generateTargetLevelScore());
         layerMan.add(scoreLabel, LAYER_UI);
              
         //----------------------------------------------------------------------
         // Initialize progress bar.
         //----------------------------------------------------------------------
         
         // Create the progress bar.
         progressBar = new ProgressBar(393, 501, ProgressBar.WIDTH_200, true);
         progressBar.setAlignment(ProgressBar.VCENTER | ProgressBar.HCENTER);
         progressBar.setProgressMax(scoreMan.getTargetLevelScore());
         layerMan.add(progressBar, LAYER_UI);
         
         //----------------------------------------------------------------------
         // Initialize pause group.
         //----------------------------------------------------------------------
         
         pauseGroup = new PauseGroup(window, layerMan, groupMan);
         Group.register(pauseGroup);
         
         //----------------------------------------------------------------------
         // Initialize game over group.
         //----------------------------------------------------------------------
                         
         // Create the game over screen.
         gameOverGroup = new GameOverGroup(window, layerMan, groupMan);    
         Group.register(gameOverGroup);
         
         //----------------------------------------------------------------------
         // Initialize options group.
         //----------------------------------------------------------------------
         
         // Create the options group.
         optionsGroup = new OptionsGroup(window, layerMan, groupMan, 
                 propertyMan);
         Group.register(optionsGroup);
         
         //----------------------------------------------------------------------
         // Initialize hgih score group.
         //----------------------------------------------------------------------
         
         // Create the game over screen.
         highScoreGroup = new HighScoreGroup(window, layerMan, groupMan,
                 highScoreMan); 
         Group.register(highScoreGroup);
         
         //----------------------------------------------------------------------
         // Start
         //----------------------------------------------------------------------
         
         // Start the game.
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
      * This method is used to update the values shown on the pause screen.
      */
     private void updatePauseGroup()
     {
         // Calculate lines per move.
         double lpm;
         if (moveMan.getMoveCount() == 0)
             lpm = 0.0;
         else
         {
             lpm = (double) totalLineCount 
                 / (double) moveMan.getMoveCount();
             lpm = lpm * 100;
             lpm = ((double) (int) lpm) / 100.0;
         }
         
         // Update the pause screen date.
         pauseGroup.setMoves(moveMan.getMoveCount());
         pauseGroup.setLines(totalLineCount);
         pauseGroup.setLinesPerMove(lpm);         
     }
     
     private void showHighScoreScreen()
     {        
         this.pauseGroup.setActivated(true);
         this.pauseGroup.setVisible(false);
 //        hidePauseScreen();
 //        hideGameOverScreen();
         
         layerMan.hide(LAYER_TILE);
         layerMan.hide(LAYER_EFFECT);  
         highScoreGroup.setActivated(true);
         highScoreGroup.setVisible(true);          
     }
     
     private void hideHighScoreScreen()
     {        
         if (gameOverGroup.isActivated() == true)
         {
             gameOverGroup.setVisible(true);
         } 
         
         layerMan.show(LAYER_TILE);
         layerMan.show(LAYER_EFFECT);
         highScoreGroup.clearChanged();
         highScoreGroup.setActivated(false);
         highScoreGroup.setVisible(false);
         
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
                || gameOverGroup.isActivated() == true
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
         
 //        int score = scoreMan.getTotalScore();
 //        if (score > highScoreMan.getLowestScore())
 //            highScoreMan.addScore("Tester", score);
 //        
 //        showHighScoreScreen();
         
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
             if (pauseButton.isActivated() == true)            
             {
                 updatePauseGroup();                
                 groupMan.showGroup(pauseButton, pauseGroup, 
                         GroupManager.CLASS_PAUSE,
                         GroupManager.LAYER_MIDDLE);            
             }
             else
                 groupMan.hideGroup(GroupManager.CLASS_PAUSE,
                         GroupManager.LAYER_MIDDLE);            
         }
         
         // If the options button was just clicked.
         if (optionsButton.clicked() == true)
         {                           
             if (optionsButton.isActivated() == true)  
             {                
                 groupMan.showGroup(optionsButton, optionsGroup,
                         GroupManager.CLASS_OPTIONS,
                         GroupManager.LAYER_MIDDLE);            
             }
             else            
                 groupMan.hideGroup(GroupManager.CLASS_OPTIONS,
                         GroupManager.LAYER_MIDDLE);
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
             if (gameOverGroup.isActivated() == true)
             {
                 // Draw game over screen.
                 gameOverGroup.setScore(scoreMan.getTotalScore());
                 groupMan.showGroup(null, gameOverGroup, 
                         GroupManager.CLASS_GAME_OVER,
                         GroupManager.LAYER_BOTTOM);                  
             }
         }
         else if (boardAnimation != null && boardAnimation.isDone() == false)
         {
             // Board is still dirty due to animation.
             boardMan.setDirty(true);
         }
         
         // Update all the group logic.
         Group.updateLogicAll(this);
         
         // Update the music manager logic.
         musicMan.updateLogic(this);
 
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
         
         // If the pause button is not on, then we proceed with the
         // normal game loop.
         if (groupMan.isActivated() == false)
         {   
             // See if it's time to level-up.
             if (pieceMan.isTileDropInProgress() == false
                     && isBusy() == false)
             {
                 // Handle Level up.
                 if (scoreMan.getLevelScore() 
                         >= scoreMan.getTargetLevelScore())
                 {    
                     // Hide piece.                    
                     pieceMan.getPieceGrid().setVisible(false);
                     pieceMan.stopAnimation();
                     
                     Util.handleMessage("Level up!!!", Thread.currentThread());
                     worldMan.levelUp(this);
                     
                     this.activateLineRemoval = true;
                     this.tileRemovalUseJumpAnimation = true;
                     this.tileRemovalNoScore = true;
                     this.tileRemovalNoItems = true;
                     tileRemovalSet.clear();
 
                     int j = boardMan.getRows() - 1;
                     for (int i = 0; i < boardMan.getColumns(); i++)
                     {                         
                         int index = i + (j * boardMan.getColumns());
                         if (boardMan.getTile(index) != null)
                             tileRemovalSet.add(
                                     new Integer(index));
                     }                                        
                     
                     soundMan.playSoundEffect(SoundManager.KEY_LEVEL_UP);
                     
                     int x = pieceMan.getPieceGrid().getX() 
                             + boardMan.getCellWidth() / 2;
                     
                     int y = pieceMan.getPieceGrid().getY() 
                             + boardMan.getCellHeight() / 2;
                     
                     Label label = ResourceFactory.get().getLabel(x, y);                                                
                     label.setText("Level Up!");
                     label.setAlignment(Label.LEFT | Label.VCENTER);
                     label.setColor(Game.TEXT_COLOR);
                     label.setSize(26);
                     
                     animationMan.add(new FloatFadeOutAnimation(                            
                             1, 0, layerMan, label));      
                     
                     label = null;
                 }
             } // end if
             
             // See if it's game ovaries.
             if (activateGameOver == true)
             {
                 // Clear flag.
                 clearGameOver();                                
                 
                 // Set in progress flag.
                 gameOverGroup.setActivated(true);
                 
                 // Hide the board.
                 startBoardHideAnimation();                
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
                     
                     cycleLineCount += boardMan.findXMatch(tileRemovalSet);
                     cycleLineCount += boardMan.findYMatch(tileRemovalSet);
 
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
                 
                 // Calculate score, unless no-score flag is set.
                 if (tileRemovalNoScore == false)
                 {
                     final int deltaScore = scoreMan.calculateLineScore(
                             tileRemovalSet, 
                             ScoreManager.TYPE_LINE,
                             cascadeCount);                               
                 
                     // Show the SCT.
                     XYPosition p = boardMan.determineCenterPoint(tileRemovalSet);
                     Label label = ResourceFactory.get().getLabel(p.x, p.y);                
                     label.setText(String.valueOf(deltaScore));
                     label.setAlignment( Label.HCENTER | Label.VCENTER);
                     label.setColor(SCORE_LINE_COLOR);
                     label.setSize(scoreMan.determineFontSize(deltaScore));
 
                     animationMan.add(new FloatFadeOutAnimation(                                             
                             0, -1, layerMan, label));
 
                     // Release references.
                     p = null;
                     label = null;                                         
                 }                  
                 else
                 {
                     // Turn off the flag now that it has been used.
                     tileRemovalNoScore = false;
                 }
                 
                 // Play the sound.
                 soundMan.playSoundEffect(SoundManager.KEY_LINE);
 
                 // Make sure bombs aren't removed (they get removed
                 // in a different step).  However, if the no-items
                 // flag is set, then ignore bombs.
                 if (tileRemovalNoItems == false)
                 {
                     boardMan.scanBombs(tileRemovalSet, bombRemovalSet);
                     tileRemovalSet.removeAll(bombRemovalSet);                
                 }
                 else
                 {
                     // Turn off the flag now that it has been used.
                     tileRemovalNoItems = false;
                 }
                 
                 // Start the line removal animations if there are any
                 // non-bomb tiles.
                 if (tileRemovalSet.size() > 0)
                 {
                     int i = 0;
                     for (Iterator it = tileRemovalSet.iterator(); it.hasNext(); )
                     {
                         TileEntity t = boardMan.getTile((Integer) it.next());
                                             
                         if (tileRemovalUseJumpAnimation == true)
                         {
                             i++;
                             int angle = i % 2 == 0 ? 70 : 180 - 70;                        
                             t.setAnimation(new JumpFadeOutAnimation(
                                 0.3, angle, 0.001, 800, layerMan, t));
                         }
                         else                        
                             t.setAnimation(new ZoomOutAnimation(t));                        
                     }
                     
                     // Clear the animation flag.
                     tileRemovalUseJumpAnimation = false;
 
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
                 XYPosition p = boardMan.determineCenterPoint(tileRemovalSet);
                 Label label = ResourceFactory.get().getLabel(p.x, p.y);
                 label.setText(String.valueOf(deltaScore));
                 label.setAlignment( Label.HCENTER | Label.VCENTER);
                 label.setColor(SCORE_BOMB_COLOR);
                 label.setSize(scoreMan.determineFontSize(deltaScore));
                 
                 animationMan.add(new FloatFadeOutAnimation(                         
                         0, -1, layerMan, label));
                 
                 // Release references.
                 p = null;
                 label = null;                
                                 
                 // Play the sound.
                 soundMan.playSoundEffect(SoundManager.KEY_BOMB);
 
                 // Extract all the new bombs.
                 Set newBombRemovalSet = new HashSet<Integer>();
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
                         t.setAnimation(new JiggleFadeOutAnimation(t));
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
             
             // Update the progress bar.
             progressBar.setProgress(scoreMan.getLevelScore());
             
             // Reset the line count.
             totalLineCount += cycleLineCount;
             cycleLineCount = 0;
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
         
         // If up or down have been pressed
         if (window.isKeyPressed(KeyEvent.VK_UP))
 		{
 			musicMan.increaseVolume();
 		}
         
         if (window.isKeyPressed(KeyEvent.VK_DOWN))
 		{
 			musicMan.decreaseVolume();
 		}
         
          // If right or left have been pressed
         if (window.isKeyPressed(KeyEvent.VK_RIGHT))
 		{
 			soundMan.increaseVolume();
 		}
         
         if (window.isKeyPressed(KeyEvent.VK_LEFT))
 		{
 			soundMan.decreaseVolume();
 		}
         
         return updated;
 	}
     
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------
     
     public int getCascadeCount()
     {
         return cascadeCount;
     }
 
     public void setCascadeCount(int cascadeCount)
     {
         this.cascadeCount = cascadeCount;
     }
             
     public int getCycleLineCount()
     {
         return cycleLineCount;
     }
 
     public void setCycleLineCount(int cycleLineCount)
     {
         this.cycleLineCount = cycleLineCount;
     }
     
     public int getTotalLineCount()
     {
         return totalLineCount;
     }        
     
     public void setTotalLineCount(int totalLineCount)
     {
         this.totalLineCount = totalLineCount;
     }
 
     //--------------------------------------------------------------------------
     // Window Methods
     //--------------------------------------------------------------------------
     
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
         // Don't pause game if we're showing the game over screen.
         if (groupMan.isActivated() == false)
         {
             updatePauseGroup();
             groupMan.showGroup(pauseButton, pauseGroup, 
                     GroupManager.CLASS_PAUSE,
                    GroupManager.LAYER_BOTTOM);
         }
         
         this.background.setDirty(true);
     }
     
      /**
      * Notification that the game window has been reactivated in some way.
      */
     public void windowActivated()
     {
         //Force a background redraw.
         if (this.background != null)
             this.background.setDirty(true);        
     }        
     
     //--------------------------------------------------------------------------
     // Main method
     //--------------------------------------------------------------------------
     
 	/**
 	 * The entry point into the game. We'll simply create an instance of class
 	 * which will start the display and game loop.
 	 * 
 	 * @param argv
 	 *            The arguments that are passed into our game
 	 */
 	public static void main(String argv[])
 	{		        
 //        System.setProperty("sun.java2d.translaccel", "true");
 //        System.setProperty("sun.java2d.ddforcevram", "true");   
         
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
