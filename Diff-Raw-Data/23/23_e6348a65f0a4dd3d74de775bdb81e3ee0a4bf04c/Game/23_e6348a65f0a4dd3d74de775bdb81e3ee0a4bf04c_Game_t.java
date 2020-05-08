 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control;
 
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 
 import javax.swing.JOptionPane;
 
 import es.eucm.eadventure.comm.AdventureApplet;
 import es.eucm.eadventure.comm.manager.commManager.CommManagerApi;
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.auxiliar.SpecialAssetPaths;
 import es.eucm.eadventure.common.data.adaptation.AdaptedState;
 import es.eucm.eadventure.common.data.adventure.ChapterSummary;
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.data.assessment.AssessmentProfile;
import es.eucm.eadventure.common.data.assessment.AssessmentProperty;
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.common.data.chapter.Exit;
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.Timer;
 import es.eucm.eadventure.common.data.chapter.book.Book;
 import es.eucm.eadventure.common.data.chapter.conversation.Conversation;
 import es.eucm.eadventure.common.data.chapter.elements.NPC;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.data.chapter.scenes.Cutscene;
 import es.eucm.eadventure.common.data.chapter.scenes.Scene;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.common.loader.incidences.Incidence;
 import es.eucm.eadventure.engine.adaptation.AdaptationEngine;
 import es.eucm.eadventure.engine.assessment.AssessmentEngine;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalItem;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalNPC;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalScene;
 import es.eucm.eadventure.engine.core.control.functionaldata.TalkingElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffect;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.control.gamestate.GameState;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateBook;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateConversation;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateLoading;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateNextScene;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateOptions;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStatePlaying;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateRunEffects;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateSlidescene;
 import es.eucm.eadventure.engine.core.control.gamestate.GameStateVideoscene;
 import es.eucm.eadventure.engine.core.data.GameText;
 import es.eucm.eadventure.engine.core.data.SaveGame;
 import es.eucm.eadventure.engine.core.data.SaveGameException;
 import es.eucm.eadventure.engine.core.data.SaveTimer;
 import es.eucm.eadventure.engine.core.gui.DebugLogPanel;
 import es.eucm.eadventure.engine.core.gui.DebugValuesPanel;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * This class contains all the elements and data necessary to run an e-Adventure
  * game
  */
 /*
  * Updated by Javier Torrente. 
  * New functionalities: Load effects wherever in a conversation
  */
 public class Game implements KeyListener, MouseListener, MouseMotionListener, Runnable, TimerEventListener, SpecialAssetPaths {
 
     /**
      * Constant for loading state
      */
     public static final int STATE_LOADING = 0;
 
     /**
      * Constant for playing state
      */
     public static final int STATE_PLAYING = 1;
 
     /**
      * Constant for slidescene state
      */
     public static final int STATE_SLIDE_SCENE = 2;
 
     /**
      * Constant for next scene state
      */
     public static final int STATE_NEXT_SCENE = 3;
 
     /**
      * Constant for videoscene state
      */
     public static final int STATE_VIDEO_SCENE = 4;
 
     /**
      * Constant for running effects state
      */
     public static final int STATE_RUN_EFFECTS = 5;
 
     /**
      * Constant for running effects state from a conversation
      */
     public static final int STATE_RUN_EFFECTS_FROM_CONVERSATION = 9;
 
     /**
      * Constant for book state
      */
     public static final int STATE_BOOK = 6;
 
     /**
      * Constant for conversation state
      */
     public static final int STATE_CONVERSATION = 7;
 
     /**
      * Constant for options state
      */
     public static final int STATE_OPTIONS = 8;
 
     /**
      * Path of the file containing the adventure
      */
     private String adventurePath;
 
     /**
      * Name of the file containing the adventure
      */
     private String adventureName;
 
     /**
      * Descriptor info of the adventure
      */
     private DescriptorData gameDescriptor;
 
     /**
      * Game data of the adventure
      */
     private Chapter gameData;
 
     /**
      * Flag summary
      */
     private FlagSummary flags;
 
     /**
      * Var summary
      */
     private VarSummary vars;
 
     /**
      * Assessment engine
      */
     private AssessmentEngine assessmentEngine;
 
     /**
      * Adaptation engine
      */
     private AdaptationEngine adaptationEngine;
 
     /**
      * The adapted state to be executed. It holds a null value if no adapted
      * state must be executed.
      */
     private AdaptedState adaptedStateToExecute;
 
     /**
      * Item summary
      */
     private ItemSummary itemSummary;
 
     /**
      * Atrezzo item summary
      */
     private AtrezzoSummary atrezzoSummary;
 
     /**
      * Inventory
      */
     private Inventory inventory;
 
     /**
      * Options of the game
      */
     private Options options;
 
     /**
      * The next scene that will be loaded
      */
     private Exit nextScene;
 
     /**
      * The last scene that was loaded
      */
     private Exit lastNextScene;
 
     /**
      * Functional scene being played
      */
     private FunctionalScene functionalScene;
 
     /**
      * Functional player of the game
      */
     private FunctionalPlayer functionalPlayer;
 
     /**
      * State of the game
      */
     private GameState currentState;
 
     /**
      * Store if each arraylist of effects in effectsQueue comes from a
      * conversation, to manage stackOfStates properly
      */
     private Stack<Boolean> isConvEffectsBlock;
 
     /**
      * Store the number of blocks of effects created in conversations are in
      * effectsQueue
      */
     private int numberConv;
 
     /**
      * LIFO of Queues of effects to be performed
      */
     private Stack<List<FunctionalEffect>> effectsQueue;
 
     /**
      * Stores the character currently talking
      */
     private TalkingElement characterCurrentlyTalking;
 
     /**
      * Stores if the game is over or not
      */
     private boolean gameOver = false;
 
     /**
      * Stores whether the current chapter has finished
      */
     private boolean nextChapter = false;
 
     /**
      * The number of the current chapter
      */
     private int currentChapter;
 
     /**
      * Time elapsed of game
      */
     private long totalTime = 0;
 
     /**
      * Book to be displayed
      */
     private Book book;
 
     /**
      * Conversation to be played
      */
     private Conversation conversation;
 
     /**
      * Current non player character selected by default in a conversation
      */
     private FunctionalNPC currentNPC;
 
     /**
      * Communication api
      */
     private AdventureApplet comm;
 
     /**
      * Instance of Game (Singleton)
      */
     private static Game instance = new Game( );
 
     /**
      * Last mouse event of the game
      */
     private MouseEvent lastMouseEvent;
 
     /**
      * Controls the actions in the game
      */
     private ActionManager actionManager;
 
     /**
      * Controls the timers in the game (normal timers and assessment timers)
      */
     private TimerManager timerManager;
 
     /**
      * Structure for game timers. The key is the id returned by TimerManager
      */
     private HashMap<Integer, Timer> gameTimers;
 
     /**
      * Stack to store each conversation nested
      */
     private Stack<GameState> stackOfState;
 
     /**
      * Temp Variable that is used to store the last MousePressed Event. It is
      * needed to avoid "fake drags".
      */
     private MouseEvent lastPressedEvent;
 
     /**
      * Temp Variable that is used to store the state of the FSM that controls
      * the removal of "fake drags".
      */
     private String state = "";
 
     private boolean debug = false;
 
     private boolean fromEditor = false;
 
     private DebugValuesPanel debugChangesPanel;
 
     private DebugLogPanel debugLogPanel;
 
     /**
      * Returns the instance of Game
      * 
      * @return Instance of Game
      */
     public static Game getInstance( ) {
 
         return instance;
     }
 
     public static void create( ) {
 
         instance = new Game( );
     }
 
     public static void create( boolean fromEditor, boolean debug ) {
 
         instance = new Game( );
         instance.fromEditor = fromEditor;
         instance.debug = debug;
     }
 
     public static void delete( ) {
 
         staticStop( );
         if( instance.debugChangesPanel != null )
             instance.debugChangesPanel.close( );
         if( instance.debugLogPanel != null )
             instance.debugLogPanel.close( );
         instance = null;
     }
 
     /**
      * Sets the adventure file path
      * 
      * @param adventurePath
      *            The path of the adventure file
      */
     public void setAdventurePath( String adventurePath ) {
 
         this.adventurePath = adventurePath;
     }
 
     /**
      * Sets the adventure name
      * 
      * @param adventureName
      *            The name of the adventure
      */
     public void setAdventureName( String adventureName ) {
 
         this.adventureName = adventureName;
        
     }
 
     /**
      * Gets the adventure name
      * 
      * @return the name of the adventure
      */
     public String getAdventureName( ) {
 
         return adventureName;
     }
 
     /**
      * Returns true if the current player mode is Transparent. False otherwise
      * 
      * @return
      */
     public boolean isTransparent( ) {
 
         return getGameDescriptor( ).getPlayerMode( ) == DescriptorData.MODE_PLAYER_1STPERSON;
     }
 
     /**
      * Init the game parameters
      */
     private void loadCurrentChapter( Graphics2D g ) {
 
         DebugLog.general( "Loading chapter" );
 
         // Reset the image cache
         MultimediaManager.getInstance( ).flushImagePool( MultimediaManager.IMAGE_SCENE );
         MultimediaManager.getInstance( ).flushImagePool( MultimediaManager.IMAGE_PLAYER );
         MultimediaManager.getInstance( ).flushAnimationPool( );
         System.gc( );
 
         // REset game strings
         GameText.reloadStrings( );
         // REset menu images
         MultimediaManager.getInstance( ).flushImagePool( MultimediaManager.IMAGE_MENU );
 
         // Extract the chapter
         ChapterSummary chapter = gameDescriptor.getChapterSummaries( ).get( currentChapter );
 
         // Load the script data
         gameData = Loader.loadChapterData( ResourceHandler.getInstance( ), chapter.getChapterPath( ), new ArrayList<Incidence>( ), true );
 
         
         GUI.getInstance( ).loading( 60 );
         
         preLoadAnimations( );
 
         // Create the flags & vars summaries and the assessment engine
         flags = new FlagSummary( gameData.getFlags( ), debug );
         vars = new VarSummary( gameData.getVars( ), debug );
 
         // Init the time manager
         timerManager = TimerManager.getInstance( );
         timerManager.reset( );
         
         if (gameData.getAdaptationName()!="")
         chapter.setAdaptationName(gameData.getAdaptationName());
         if (gameData.getAssessmentName()!="")
         chapter.setAssessmentName(gameData.getAssessmentName());
         
         AdaptedState initialState=null;
         // Load the assessment rules and adaptation data 
         if (gameData.hasAdaptationProfile())
             initialState = adaptationEngine.init( gameData.getSelectedAdaptationProfile() );
        
         if (gameData.hasAssessmentProfile())
             assessmentEngine.loadAssessmentRules( gameData.getSelectedAssessmentProfile() );
         
         
         // Load the assessment rules and adaptation data (from chapter xml file)
         if (!gameData.hasAdaptationProfile()&&chapter.hasAdaptationProfile())
             initialState  = adaptationEngine.init( chapter.getSelectedAdaptationProfile() );
         
         if (!gameData.hasAssessmentProfile()&&chapter.hasAssessmentProfile())
             assessmentEngine.loadAssessmentRules( chapter.getSelectedAssessmentProfile() );
      
         // Initialize the required elements of the game
         actionManager = new ActionManager( );
         itemSummary = new ItemSummary( gameData.getItems( ) );
         atrezzoSummary = new AtrezzoSummary( gameData.getAtrezzo( ) );
         inventory = new Inventory( );
 
         // Initialize the stack of queue of effects
         effectsQueue = new Stack<List<FunctionalEffect>>( );
         effectsQueue.push( new ArrayList<FunctionalEffect>( ) );
 
         //Initialize the stack that store if each list of effects keep in effectsQueue is made in a conversation or not
         isConvEffectsBlock = new Stack<Boolean>( );
 
         numberConv = 0;
 
         // Initialize the stack of states (used to keep the conversations and can throw its effects)
         stackOfState = new Stack<GameState>( );
 
         GUI.getInstance( ).loading(70);
 
         // Load images to cache
         new GameStateOptions( );
 
         // By default, set the initial scene taking it from the XML script
         //GeneralScene initialScene = gameData.getInitialGeneralScene( );
         Exit firstScene = new Exit( true, 0, 0, 40, 40 );
         firstScene.setNextSceneId( gameData.getInitialGeneralScene( ).getId( ) );
 
         // process the initial adapted state
         processAdaptedState( firstScene, initialState );
         // process the adaptedStateOfExecute (this var will has value if any adaptation rule has been achieve)
         processAdaptedState( firstScene, adaptedStateToExecute );
         // Set the next scene
         setNextScene( firstScene );
 
         // Create the functional player
         functionalPlayer = new FunctionalPlayer( gameData.getPlayer( ) );
         functionalPlayer.setTransparent( gameDescriptor.getPlayerMode( ) == DescriptorData.MODE_PLAYER_1STPERSON );
 
         // Add timers to the TimerManager
         this.gameTimers = new HashMap<Integer, Timer>( );
         for( Timer timer : gameData.getTimers( ) ) {
             int id = timerManager.addTimer( timer, this, timer.getTime( ) );
             gameTimers.put( new Integer( id ), timer );
         }
 
 //        g.clearRect( 0, 0, 800, 600 );
 //        GUI.drawString( g, GameText.TEXT_LOADING_FINISHED, 400, 300 );
 //        GUI.getInstance( ).endDraw( );
         GUI.getInstance( ).loading(90);
 
         currentState = new GameStateNextScene( );
 
         nextChapter = false;
 
         DebugLog.general( "Chapter loaded" );
     }
 
     /**
      * Processes the adapted state
      * 
      * @param firstScene
      *            to add it a new scene
      * @param adaptedState
      */
     private void processAdaptedState( Exit firstScene, AdaptedState adaptedState ) {
 
         // If there is an adapted state to be executed
         if( adaptedState != null ) {
 
             // If it has an initial scene, set it
             if( adaptedState.getTargetId( ) != null ){
                 boolean found = false;
                 // check the scene is in chapter
                 for( Scene scene : gameData.getScenes( ) ) {
                     if( scene.getId( ).equals( adaptedState.getTargetId( ) ) ){
                         firstScene.setNextSceneId( adaptedState.getTargetId( ) );
                         found = true;
                     }
                     
                 }
                 if (!found){
                 // check the scene is a cutscene
                 for( Cutscene cutscene : gameData.getCutscenes( ) ) {
                     if( cutscene.getId( ).equals( adaptedState.getTargetId( ) ) )
                         firstScene.setNextSceneId( adaptedState.getTargetId( ) );
                 
                       }
                  }
             }
             // Set the flags
             for( String flag : adaptedState.getActivatedFlags( ) )
                 if( flags.existFlag( flag ) )
                     flags.activateFlag( flag );
             for( String flag : adaptedState.getDeactivatedFlags( ) )
                 if( flags.existFlag( flag ) )
                     flags.deactivateFlag( flag );
             // Set the vars
             List<String> adaptedVars = new ArrayList<String>( );
             List<String> adaptedValues = new ArrayList<String>( );
             adaptedState.getVarsValues( adaptedVars, adaptedValues );
             for( int i = 0; i < adaptedVars.size( ); i++ ) {
                 String varName = adaptedVars.get( i );
                 String varValue = adaptedValues.get( i );
                 // check if it is a "set value" operation
                 if( AdaptedState.isSetValueOp( varValue ) ) {
                     String val = AdaptedState.getSetValueData( varValue );
                     if( val != null )
                         vars.setVarValue( varName, Integer.parseInt( val ) );
                 }
                 // it is "increment" or "decrement" operation, for both of them is necessary to 
                 // get the current value of referenced variable
                 else {
                     if( vars.existVar( varName ) ) {
                         int currentValue = vars.getValue( varName );
                         int operationValue = Integer.parseInt( varValue.substring( varValue.indexOf( " " ) + 1 ) );
                         if( AdaptedState.isIncrementOp( varValue.substring( 0, varValue.indexOf( " " ) ) ) ) {
                             vars.setVarValue( varName, currentValue + operationValue );
                         }
                         else if( AdaptedState.isDecrementOp( varValue.substring( 0, varValue.indexOf( " " ) ) ) ) {
                             if( currentValue - operationValue >= 0 )
                                 vars.setVarValue( varName, currentValue - operationValue );
                             else
                                 vars.setVarValue( varName, 0 );
                         }
                     }
                 }
             }
 
         }
     }
 
     public void repaintDebug( ) {
 
         if( debug ) {
             debugChangesPanel.updateUI( );
             debugChangesPanel.validate( );
             debugChangesPanel.repaint( );
             debugLogPanel.updateUI( );
             debugLogPanel.validate( );
             debugLogPanel.repaint( );
         }
     }
 
     public void setPlayerLayer( int layer ) {
 
         functionalPlayer.setLayer( layer );
 
     }
 
     private void preLoadAnimations( ) {
 
         MultimediaManager multimedia = MultimediaManager.getInstance( );
         for( Resources r : gameData.getPlayer( ).getResources( ) ) {
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
             if( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_UP ), false, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
 
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
             if( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_UP ), false, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
 
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
             if( r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
 
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), false, MultimediaManager.IMAGE_PLAYER );
             if( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), false, MultimediaManager.IMAGE_PLAYER );
             else
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), true, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_UP ), false, MultimediaManager.IMAGE_PLAYER );
             multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_DOWN ), false, MultimediaManager.IMAGE_PLAYER );
         }
         for( NPC npc : gameData.getCharacters( ) ) {
             for( Resources r : npc.getResources( ) ) {
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), false, MultimediaManager.IMAGE_SCENE );
                 if( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_LEFT ), false, MultimediaManager.IMAGE_SCENE );
                 else
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_RIGHT ), true, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_UP ), false, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_STAND_DOWN ), false, MultimediaManager.IMAGE_SCENE );
 
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_RIGHT ), false, MultimediaManager.IMAGE_SCENE );
                 if( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_LEFT ), false, MultimediaManager.IMAGE_SCENE );
                 else
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_RIGHT ), true, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_UP ), false, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_SPEAK_DOWN ), false, MultimediaManager.IMAGE_SCENE );
 
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_RIGHT ), false, MultimediaManager.IMAGE_SCENE );
                 if( r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_LEFT ), false, MultimediaManager.IMAGE_SCENE );
                 else
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_USE_RIGHT ), true, MultimediaManager.IMAGE_SCENE );
 
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), false, MultimediaManager.IMAGE_SCENE );
                 if( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ) != null && !r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ).equals( ASSET_EMPTY_ANIMATION ) )
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_LEFT ), false, MultimediaManager.IMAGE_SCENE );
                 else
                     multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_RIGHT ), true, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_UP ), false, MultimediaManager.IMAGE_SCENE );
                 multimedia.loadAnimation( r.getAssetPath( NPC.RESOURCE_TYPE_WALK_DOWN ), false, MultimediaManager.IMAGE_SCENE );
             }
         }
 
     }
 
     public static boolean FINISH = false;
 
     /*
      *  (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     public void run( ) {
 
         FINISH = false;
 
         if( debug ) {
             debugLogPanel = new DebugLogPanel( );
         }
         DebugLog.general( "Log started..." );
         
         /*// Timer to show how the memory varies
         java.util.Timer t = new  java.util.Timer();
         t.schedule( new TimerTask() {
             @Override
             public void run( ) {
                 System.out.println("Memory: " + Runtime.getRuntime( ).freeMemory( ) + " " + Runtime.getRuntime( ).totalMemory( ));
             }
         }, 0, 100 );
          */
         
         try {
             this.timerManager = TimerManager.getInstance( );
             totalTime = 0;
             long elapsedTime = 0;
             long oldTime = System.currentTimeMillis( );
             long lastFps = 0;
             long time;
             int fps = 0;
             int oldFps = 0;
 
             // Load the game descriptor (it holds the info of the GUI and the player)
             gameDescriptor = Loader.loadDescriptorData( ResourceHandler.getInstance( ) );
 
             if( gameDescriptor == null ) {
                 // TODO possibly add dialog to tell player the game couldn't get loaded
                 return;
             }
             gameDescriptor.setProjectName( adventureName );
 
             GUI.setGraphicConfig( gameDescriptor.getGraphicConfig( ) );
 
             GUI.create( );
 
             currentState = new GameStateLoading( );
             
             // Just in case this is an Applet, hide the loading Message that appears on the HTML file
             if (comm!=null){
                 String command = "javascript:hideText();";
                 comm.sendJavaScript( command );
             }
 
             GUI.getInstance( ).initGUI( gameDescriptor.getGUIType( ), gameDescriptor.isGUICustomized( ) );
 
             if( GUI.getInstance( ).getFrame( ) != null ) {
                 GUI.getInstance( ).getFrame( ).addKeyListener( this );
                 GUI.getInstance( ).getFrame( ).addMouseListener( this );
                 GUI.getInstance( ).getFrame( ).addMouseMotionListener( this );
             }
 
             Graphics2D g = GUI.getInstance( ).getGraphics( );
 
             GUI.getInstance( ).loading(0);
 
             // Load the options
             options = new Options( );
             options.loadOptions( adventurePath, adventureName );
 
             // Init the assessment and adaptation engines
             adaptationEngine = new AdaptationEngine( );
             assessmentEngine = new AssessmentEngine( );
             
             currentChapter = 0;
 
             boolean needsName = false;
 
             for( ChapterSummary chapter : gameDescriptor.getChapterSummaries( ) ) {
                 AssessmentProfile ap = AssessmentEngine.loadAssessmentProfile( chapter.getAssessmentName( ) );
                 if( !needsName && ap != null && ap.isSendByEmail( ) )
                     needsName = true;
             }
 
             if( needsName ) {
                 DebugLog.general( "Asks for player name" );
                 String name = JOptionPane.showInputDialog( null, TC.get( "Reports.InputReportName" ), TC.get( "Reports.NameInput" ), JOptionPane.QUESTION_MESSAGE );
                 gameDescriptor.setPlayerName( name );
                 assessmentEngine.setPlayerName( name );
             }
 
             GUI.getInstance( ).loading( 10 );
             
             while( !gameOver ) {
                int timeBarrier = 60;
                 loadCurrentChapter( g );
                 
                 GUI.getInstance( ).loading( 100 );
 
                 if( debug ) {
                     if( debugChangesPanel != null )
                         GUI.getInstance( ).getJFrame( ).remove( debugChangesPanel );
                     debugChangesPanel = new DebugValuesPanel( flags, vars, this.getCurrentChapterData( ).getGlobalStates( ) );
                     GUI.getInstance( ).getJFrame( ).add( debugChangesPanel );
                     GUI.getInstance( ).getJFrame( ).add( debugLogPanel );
                     GUI.getInstance( ).getJFrame( ).setAlwaysOnTop( false );
                     GUI.getInstance( ).getJFrame( ).validate( );
                 }
 
                 while( !nextChapter && !gameOver ) {
                     if( debug )
                         debugChangesPanel.updateValues( );
                     time = System.currentTimeMillis( );
                     elapsedTime = time - oldTime;
                     oldTime = time;
                     totalTime += elapsedTime;
                     if( time - lastFps < 1000 ) {
                         fps++;
                     }
                     else {
                         lastFps = time;
                         oldFps = fps;
                         fps = 1;
                         //if( !GUI.getInstance( ).getFrame( ).isFocusOwner( ) ) {
                         //    GUI.getInstance( ).getFrame( ).requestFocusInWindow( );
                         //}
                     }
 
                     currentState.mainLoop( elapsedTime, oldFps );
                     
                     MultimediaManager.getInstance( ).update( );
                    // sent time to LAMS each 1 minute
                    if( comm!=null && comm.getCommType( ) == CommManagerApi.LAMS_TYPE && (totalTime/1000 > timeBarrier)) {
                        timeBarrier += 60;
                      //Sent the time
                        ArrayList l = new ArrayList();
                        l.add( new AssessmentProperty("total-time",  String.valueOf(getTime( ))) );
                        comm.notifyRelevantState( l );
                    }
                     
                     try {
                         Thread.sleep( Math.max((10 - (System.currentTimeMillis( ) - time)), 0) );
                     }
                     catch( InterruptedException e ) {
                     }
                 }
 
                 //If there is an assessment profile, show the "Save Report" dialog
                 while( !assessmentEngine.isEndOfChapterFeedbackDone( ) ) {
                     Thread.sleep( 100 );
                 }
 
                
                 if( currentChapter == gameDescriptor.getChapterSummaries( ).size( ) )
                     gameOver = true;
             }
 
         }
         catch( Exception e ) {
             ReportDialog.GenerateErrorReport( e, Game.getInstance( ).isFromEditor( ), "FATAL ERROR. This should not happen." );
         }
 
         try {
             stop( );
         }
         catch( Exception e ) {
 
         }
     }
 
     /**
      * Stops all sounds and music, the adaptation engine, the gui, etc
      */
     private void stop( ) {
 
         //Stop the music (if it is playing) and the adaptation clock
         if( functionalScene != null )
             functionalScene.stopBackgroundMusic( );
         if( adaptationEngine != null )
             adaptationEngine.stopAdaptationClock( );
 
         // Stop the communication 
         if( comm.getCommType( ) == CommManagerApi.LAMS_TYPE ){
             comm.sendHTMLReport( assessmentEngine.getHTMLReportStringLAMS( ) ); 
             comm.disconnect( null );
         } else if (comm.getCommType( ) == CommManagerApi.SCORMV12_TYPE || comm.getCommType( ) == CommManagerApi.SCORMV2004_TYPE) {
             comm.sendHTMLReport( assessmentEngine.getHTMLReportString( ) ); 
             comm.disconnect( null );
             
         }
         staticStop( );
     }
 
     /**
      * Stops all sounds and music, the gui, etc
      */
     private static void staticStop( ) {
 
         //Delete all sounds
         if( MultimediaManager.getInstance( ) != null )
             MultimediaManager.getInstance( ).deleteSounds( );
 
         //Hide the GUI
         if( GUI.getInstance( ) != null && GUI.getInstance( ).getFrame( ) != null ) {
             // Hide the GUI
             GUI.getInstance( ).getFrame( ).setEnabled( false );
             GUI.getInstance( ).getFrame( ).setVisible( false );
             GUI.getInstance( ).getFrame( ).setFocusable( false );
 
             // Delete the GUI
             GUI.delete( );
         }
     }
 
     /**
      * Returns the game descriptor
      * 
      * @return Game descriptor
      */
     public DescriptorData getGameDescriptor( ) {
 
         return gameDescriptor;
     }
 
     /**
      * Returns the game data
      * 
      * @return Game data
      */
     public Chapter getCurrentChapterData( ) {
 
         return gameData;
     }
 
     /**
      * Returns the flag summary
      * 
      * @return Flag summary
      */
     public FlagSummary getFlags( ) {
 
         return flags;
     }
 
     /**
      * Returns the var summary
      * 
      * @return Var summary
      */
     public VarSummary getVars( ) {
 
         return vars;
     }
 
     /**
      * Returns the assessment engine
      * 
      * @return Assessment engine
      */
     public AssessmentEngine getAssessmentEngine( ) {
 
         return assessmentEngine;
     }
 
     /**
      * Returns the item summary
      * 
      * @return Item summary
      */
     public ItemSummary getItemSummary( ) {
 
         return itemSummary;
     }
 
     /**
      * Returns the atrezzo item summary
      * 
      * @return Atrezzo item summary
      */
     public AtrezzoSummary getAtrezzoItemSummary( ) {
 
         return atrezzoSummary;
     }
 
     /**
      * Returns the options of the game
      * 
      * @return Options of the game
      */
     public Options getOptions( ) {
 
         return options;
     }
 
     public int getTime( ) {
 
         return (int) totalTime / 1000;
     }
 
     /**
      * Saves the options to a file
      */
     public void saveOptions( ) {
 
         options.saveOptions( adventurePath, adventureName );
     }
 
     /**
      * Removes an item from the scene (if placed there) and moves it to the
      * inventory
      * 
      * @param itemId
      *            Id of the item
      */
     public void grabItem( String itemId ) {
 
         // Remove the FunctionalItem from the scene and store it into the inventory
         FunctionalItem grabbedItem = null;
 
         for( FunctionalItem currentItem : functionalScene.getItems( ) )
             // If we found the item we wanted
             if( currentItem.getItem( ).getId( ).equals( itemId ) )
                 grabbedItem = currentItem;
 
         // Delete the item from the scene
         functionalScene.getItems( ).remove( grabbedItem );
 
         // Insert the item in the inventory
         inventory.storeItem( grabbedItem );
 
         // Count the item as grabbed
         itemSummary.grabItem( itemId );
     }
 
     /**
      * Removes an item from the inventory, and counts it as a consumed item
      * 
      * @param itemId
      *            Id of the item
      */
     public void consumeItem( String itemId ) {
 
         // Remove the FunctionalItem from the inventory
         if( itemSummary.isItemGrabbed( itemId ) ) {
             itemSummary.consumeItem( itemId );
             inventory.consumeItem( itemId );
         }
     }
 
     /**
      * Places an item in the inventory (if its previous state was normal)
      * 
      * @param itemId
      *            Id of the item
      */
     public void generateItem( String itemId ) {
 
         if( itemSummary.isItemNormal( itemId ) ) {
           // 23/11/2010 the object has to disappear from the scene when it's generated (aba)
            // inventory.storeItem( new FunctionalItem( gameData.getItem( itemId ), (InfluenceArea) null ) );
           // itemSummary.grabItem( itemId );
            grabItem(itemId);
         }
         else if( itemSummary.isItemConsumed( itemId ) ) {
            // 23/11/2010 in this case it is not necesary because the item is not being showhed in the secene
             itemSummary.regenerateItem( itemId );
             inventory.storeItem( new FunctionalItem( gameData.getItem( itemId ), (InfluenceArea) null ) );
             itemSummary.grabItem( itemId );
         }
     }
 
     /**
      * Returns the action manager
      * 
      * @return Action manager
      */
     public ActionManager getActionManager( ) {
 
         return actionManager;
     }
 
 
     /**
      * Push in the state stack the GameState gs
      * 
      * @param gs
      *            GameState to store
      */
     // this method is only used with GameStateConversation
     public void pushCurrentState( GameState gs ) {
 
         stackOfState.push( gs );
 
         // store the name of the conversation for future conversation restoring. It will be needed to
         // restore the effects in nodes of this conversation.
         ( (GameStateConversation) currentState ).setConvID( conversation.getId( ) );
 
     }
 
     /**
      * Take out the last state introduced in the stack.
      * 
      * @return null if is empty stack, the top state in other case
      */
     /*  public GameState popCurrentState(){
          
       	GameState toReturn=null;
           if (!(stackOfState.size() == 0))
           	toReturn = stackOfState.pop();
           	
           return toReturn;
       }*/
 
     /**
      * Sets the new state for the game
      * 
      * @param state
      *            New game state
      */
     public void setState( int state ) {
 
         GUI.getInstance( ).setDefaultCursor( );
         switch( state ) {
             case STATE_LOADING:
                 currentState = new GameStateLoading( );
                 break;
             case STATE_PLAYING:
                 currentState = new GameStatePlaying( );
                 if( lastMouseEvent != null )
                     currentState.mouseMoved( lastMouseEvent );
                 break;
             case STATE_SLIDE_SCENE:
                 currentState = new GameStateSlidescene( );
                 break;
             case STATE_NEXT_SCENE:
                 currentState = new GameStateNextScene( );
                 break;
             case STATE_VIDEO_SCENE:
                 currentState = new GameStateVideoscene( );
                 break;
             case STATE_RUN_EFFECTS:
                 currentState = new GameStateRunEffects( this.isConvEffectsBlock.peek( ) );
                 break;
             case STATE_RUN_EFFECTS_FROM_CONVERSATION:
                 currentState = new GameStateRunEffects( true );
                 break;
             case STATE_BOOK:
                 currentState = new GameStateBook( );
                 break;
             case STATE_CONVERSATION:
                 currentState = new GameStateConversation( );
                 break;
             case STATE_OPTIONS:
                 currentState = new GameStateOptions( );
                 break;
         }//}
     }
 
     public void evaluateState( ) {
 
         if( numberConv < stackOfState.size( ) ) {
             currentState = stackOfState.pop( );
             // set the game attribute conversation to stored conversation
             setConversation( ( (GameStateConversation) currentState ).getConvID( ) );
 
         }
         else if( !isEmptyFIFOinStack( ) )
             setState( STATE_RUN_EFFECTS );
         else
             setState( STATE_PLAYING );
     }
 
     /**
      * Sets game over to true
      */
     public void setGameOver( ) {
 
         gameOver = true;
     }
 
     public void goToNextChapter( ) {
 
         currentChapter++;
         nextChapter = true;
     }
 
     public void goToChapter( int chapter ) {
 
         currentChapter = chapter;
         nextChapter = true;
     }
 
     /**
      * Sets the functional scene
      * 
      * @param scene
      *            New functional scene
      */
     public void setFunctionalScene( FunctionalScene scene ) {
         if (this.functionalScene != null) {
             functionalScene.freeMemory();
             functionalScene = null;
             Runtime.getRuntime( ).gc( );
         }
         this.functionalScene = scene;
     }
 
     /**
      * Returns the functional scene being played
      * 
      * @return Functional scene
      */
     public FunctionalScene getFunctionalScene( ) {
 
         return functionalScene;
     }
 
     /**
      * Returns the inventory
      * 
      * @return Inventory
      */
     public Inventory getInventory( ) {
 
         return inventory;
     }
 
     /**
      * Returns the functional player
      * 
      * @return Functional player
      */
     public FunctionalPlayer getFunctionalPlayer( ) {
 
         return functionalPlayer;
     }
 
     /**
      * Sets the next scene to be loaded
      * 
      * @param nextScene
      *            New next scene structure
      */
     public void setNextScene( Exit nextScene ) {
 
         this.lastNextScene = this.nextScene;
         this.nextScene = nextScene;
     }
 
     /**
      * Returns the current next scene
      * 
      * @return Next scene
      */
     public Exit getNextScene( ) {
 
         return nextScene;
     }
 
     /**
      * Returns the last next scene
      * 
      * @return Next scene
      */
     public Exit getLastScene( ) {
 
         return lastNextScene;
     }
 
     /**
      * Returns the current character currently talking
      * 
      * @return Character currently talking
      */
     public TalkingElement getCharacterCurrentlyTalking( ) {
 
         return characterCurrentlyTalking;
     }
 
     /**
      * Sets the character currently talking
      * 
      * @param characterCurrentlyTalking
      *            New character currently talking
      */
     public void setCharacterCurrentlyTalking( TalkingElement characterCurrentlyTalking ) {
 
         this.characterCurrentlyTalking = characterCurrentlyTalking;
     }
 
     /**
      * Returns the effects queue
      * 
      * @return FunctionalEffects queue stored in the game
      */
     /*  public Stack<ArrayList<FunctionalEffect>> getEffectsQueue( ) {
           return effectsQueue;
       }*/
 
     /**
      * Clears all the effects from the effects queue
      */
     public void flushEffectsQueue( ) {
 
         effectsQueue.clear( );
     }
 
     /**
      * Stores a series of effects in the queue, and changes the state of the
      * game
      * 
      * @param effects
      *            List of effects to be stored
      * @param fromConversation
      *            Distinguish when the State run effects are called from a
      *            conversation, to manage the stack of states which only stores
      *            conversation states.
      */
     public void storeEffectsInQueue( List<FunctionalEffect> effects, boolean fromConversation ) {
 
         isConvEffectsBlock.push( fromConversation );
         effectsQueue.push( new ArrayList<FunctionalEffect>( ) );
         for( int i = 0; i < effects.size( ); i++ )
             effectsQueue.peek( ).add( i, effects.get( i ) );
 
         if( fromConversation )
             numberConv++;
 
         if( !( currentState instanceof GameStateRunEffects ) ) {
             setState( STATE_RUN_EFFECTS );
         }
     }
 
     /**
      * Gets the first element of the top of the stack
      */
     public FunctionalEffect getFirstElementOfTop( ) {
 
         FunctionalEffect toReturn = null;
         if( effectsQueue.size( ) > 1 && effectsQueue.peek( ).isEmpty( ) ) {
             effectsQueue.pop( );
             if( isConvEffectsBlock.pop( ) )
                 numberConv--;
         }
         else {
             if( effectsQueue.peek( ).size( ) != 0 ) {
                 toReturn = effectsQueue.peek( ).remove( 0 );
                 if( effectsQueue.size( ) > 1 && effectsQueue.peek( ).isEmpty( ) ) {
                     effectsQueue.pop( );
                     if( isConvEffectsBlock.pop( ) )
                         numberConv--;
                 }
             }
         }
         return toReturn;
 
         //Con esto que esta comentado no solo avanzo en la cola, sino en la pila tb
         /*if (effectsQueue.peek().size()==1 && effectsQueue.size()!=1){
         	FunctionalEffect fe = effectsQueue.peek().remove(0);
         	popEffectsStack();
         	return fe;
         }else
         	return effectsQueue.peek().remove(0);*/
         //Solo avanzamos en la cola
 
     }
 
     /**
      * Check if the Stack only have one empty FIFO
      */
     public boolean isEmptyFIFOinStack( ) {
 
         return effectsQueue.peek( ).isEmpty( );
     }
 
     public void endConversation( ) {
 
         if( !isEmptyFIFOinStack( ) )
             setState( STATE_RUN_EFFECTS );
         else if( !stackOfState.isEmpty( ) )
             evaluateState( );
         else
             setState( STATE_PLAYING );
 
     }
 
     /**
      * Adds a element to effect Stack
      */
     /* public void addToTheStack( List<FunctionalEffect> el ){
       	effectsQueue.push(el);
       }*/
 
     /**
      * Places an effect in the end of the queue, and changes the state of the
      * game
      * 
      * @param effect
      *            FunctionalEffect to be enqueued
      */
     /* public void enqueueEffect( FunctionalEffect effect ) {
      	effectsQueue.peek().add(effect);
          setState( STATE_RUN_EFFECTS );
      }*/
 
     /**
      * Sets the asynchronous communication api
      * 
      * @param comm
      *            New asynchronous communication api
      */
     public void setComm( AdventureApplet comm ) {
 
         this.comm = comm;
         if( this.comm != null )
             System.out.println( "comm se instala bien" );
     }
 
     /**
      * Returns whether the game is executing like an applet or as a stand-alone
      * application
      * 
      * @return true if we are executing as an applet
      */
     public boolean isAppletMode( ) {
 
         return ResourceHandler.getInstance( ).isRestrictedMode( ) && ResourceHandler.getInstance( ).isExtraRestriction( );
     }
 
     /**
      * Returns whether there is an active communication link with a Learning
      * Management System
      * 
      * @return true If the communication is active
      */
     public boolean isConnected( ) {
 
         if( comm != null ) {
             return comm.isConnected( );
         }
         return false;
     }
 
     /**
      * Returns the book
      * 
      * @return Book
      */
     public Book getBook( ) {
 
         return book;
     }
 
     /**
      * Sets the book to be displayed
      * 
      * @param bookId
      *            Book id
      */
     public void setBook( String bookId ) {
 
         this.lastNextScene = this.getNextScene( );
         book = gameData.getBook( bookId );
     }
 
     /**
      * Returns the adapted state that must be executed.
      * 
      * @return The adapted state to be executed, null if there is none
      */
     public AdaptedState getAdaptedStateToExecute( ) {
 
         return adaptedStateToExecute;
     }
 
     /**
      * Sets the new adapted state to be executed. This method must be called
      * only by the adaptation engine, when a rule has been processed.
      * 
      * @param adaptedStateToExecute
      *            Adapted state to be executed
      */
     public synchronized void setAdaptedStateToExecute( AdaptedState adaptedStateToExecute ) {
 
         this.adaptedStateToExecute = adaptedStateToExecute;
     }
 
     /**
      * Returns the current npc stored to perform conversations by default
      * 
      * @return Functional character
      */
     public FunctionalNPC getCurrentNPC( ) {
 
         return currentNPC;
     }
 
     /**
      * Sets the current npc stored to perform conversations by default
      * 
      * @param currentNPC
      *            New functional character
      */
     public void setCurrentNPC( FunctionalNPC currentNPC ) {
 
         this.currentNPC = currentNPC;
     }
 
     /**
      * Returns the next conversation to be played
      * 
      * @return Conversation
      */
     public Conversation getConversation( ) {
 
         return conversation;
     }
 
     /**
      * Sets the next conversation to be played
      * 
      * @param conversationId
      *            New conversation
      */
     public void setConversation( String conversationId ) {
 
         conversation = gameData.getConversation( conversationId );
     }
 
     /**
      * Update the data pending from the flags. This include the resources of the
      * game, and the rules processed.
      */
     public synchronized void updateDataPendingFromState( boolean notifyTimerCycles ) {
         timerManager.update( notifyTimerCycles );
         functionalScene.updateScene( );
         if( gameData.hasAssessmentProfile( ) )
             assessmentEngine.processRules( );
 
     }
 
     public void save( String saveFile ) {
 
         SaveGame saveGame = new SaveGame( );
         saveGame.setVersionNumber( Integer.parseInt( gameDescriptor.getVersionNumber( ) ) );
         saveGame.setProjectName( gameDescriptor.getProjectName( ) );
         saveGame.setTitle( gameDescriptor.getTitle( ) );
         saveGame.setChapter( currentChapter );
         Calendar calendar = new GregorianCalendar( );
         saveGame.setSaveTime( calendar.get( Calendar.DAY_OF_MONTH ) + "/" + ( calendar.get( Calendar.MONTH ) + 1 ) + "/" + calendar.get( Calendar.YEAR ) + " " + calendar.get( Calendar.HOUR_OF_DAY ) + ":" + calendar.get( Calendar.MINUTE ) );
         saveGame.setTotalTime( totalTime );
         saveGame.setFlags( flags );
         saveGame.setVars( vars );
         saveGame.setIdScene( functionalScene.getScene( ).getId( ) );
         saveGame.setItemSummary( itemSummary );
         saveGame.setPlayerX( functionalPlayer.getX( ) );
         saveGame.setPlayerY( functionalPlayer.getY( ) );
         saveGame.setTimers( timerManager );
         if( !saveGame.saveTxt( saveFile ) )
             System.out.println( "* Error: There has been an error, savegame ''savedgame.egame'' not saved." );
     }
 
     public void load( String saveFile ) {
 
         SaveGame saveGame = new SaveGame( );
         try {
             if( saveGame.loadTxt( saveFile ) ) {
                 setState( STATE_LOADING );
 
                 if( gameDescriptor.getTitle( ).equals( saveGame.getTitle( ) ) ) {
 
                     currentChapter = saveGame.getChapter( );
 
                     if( gameDescriptor.getChapterSummaries( ).get( currentChapter ) != null ) {
                         ChapterSummary chapter = gameDescriptor.getChapterSummaries( ).get( currentChapter );
                         gameData = Loader.loadChapterData( ResourceHandler.getInstance( ), chapter.getChapterPath( ), new ArrayList<Incidence>( ), true );
                     }
                     totalTime = saveGame.getTotalTime( );
                     if( saveGame.getFlags( ) != null )
                         flags = saveGame.getFlags( );
                     if( saveGame.getVars( ) != null )
                         vars = saveGame.getVars( );
 
                     itemSummary = saveGame.getItemSummary( );
 
                     //functionalPlayer.setDestiny( 0, 0 );
 
                     // TODO check that the following line isn't necessary
                     //functionalPlayer.setState( FunctionalPlayer.IDLE );
                     functionalPlayer.cancelActions( );
                     functionalPlayer.cancelAnimations( );
 
                     if( gameData.getGeneralScene( saveGame.getIdScene( ) ) != null )
                         functionalScene = new FunctionalScene( (Scene) gameData.getGeneralScene( saveGame.getIdScene( ) ), functionalPlayer );
 
                     functionalPlayer.setX( saveGame.getPlayerX( ) );
                     functionalPlayer.setY( saveGame.getPlayerY( ) );
 
                     inventory = new Inventory( );
                     ArrayList<String> grabbedItems = itemSummary.getGrabbedItems( );
 
                     for( String item : grabbedItems )
                         inventory.storeItem( new FunctionalItem( gameData.getItem( item ), (InfluenceArea) null ) );
 
                     SaveTimer st = new SaveTimer( );
                     String[] timers = saveGame.getLoadTimers( );
                     if( timers != null ) {
                         for( int i = 0; i < timers.length; i++ ) {
 
                             // take the correct values for each timer
                             String timer = timers[i];
                             String[] aux = timer.split( "-" );
                             boolean isAssessment = Integer.valueOf( aux[3] ).intValue( ) == 0;
                             st.setState( Integer.valueOf( aux[0] ).intValue( ) );
                             if( timerManager.isRunningState( Integer.valueOf( aux[0] ).intValue( ) ) ) {
                                 st.setLastUpdate( System.currentTimeMillis( ) / 1000 );
                                 if( !isAssessment )
                                     st.setTimeUpdate( Integer.valueOf( aux[1] ).longValue( ) - Integer.valueOf( aux[2] ).longValue( ) );
                             }
                             else {
                                 st.setLastUpdate( 0 );
                                 if( !isAssessment )
                                     st.setTimeUpdate( Integer.valueOf( aux[1] ).longValue( ) );
                             }
                             // change this values in the current TimerManager
                             int check = timerManager.changeValueOfTimer( i, st );
                             if( check >= 0 ) {
                                 // Put this changes in gameTimers
                                 //gameTimers.get(new Integer(i)).
                             }
                             else {
                                 System.out.println( "* Error: There has been an error, savegame ''savedgame.egame'' not propperly loaded." );
                             }
                             // If it is assessment timer, set the correct values in assessmentEngine
                             if( isAssessment ) {
                                 // current time - the time in second that has been
                                 if( assessmentEngine.getTimedAssessmentRule( new Integer( i ) ) != null )
                                     assessmentEngine.getTimedAssessmentRule( new Integer( i ) ).setStartTime( System.currentTimeMillis( ) / 1000 - Integer.valueOf( aux[2] ).longValue( ) );
                             }
 
                         }
 
                         //TODO no estoy seguro 
                         lastMouseEvent = null;
                     }
 
                     setState( STATE_PLAYING );
                 }
                 else {
                     System.out.println( "* Error: There has been an error, savegame ''savedgame.egame'' not loaded." );
                 }
             }
         }
         catch( SaveGameException e ) {
             JOptionPane.showMessageDialog( null, "There was an error while loading the selected adventure.\nPlease check that no configuration file is missing or incorrect", "Error loading adventure", JOptionPane.ERROR_MESSAGE );
             JOptionPane.showMessageDialog( null, "The first chapter will be reloaded", "Error loading adventure", JOptionPane.ERROR_MESSAGE );
             Graphics2D g = GUI.getInstance( ).getGraphics( );
             currentChapter = 0;
             loadCurrentChapter( g );
         }
 
     }
 
     /* Keyboard and mouse inputs */
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
      */
     public void keyTyped( KeyEvent arg0 ) {
 
     }
 
     /**
      * During the last months it had been detected some anomalies dealing with
      * MouseEvents. If the mouse is not completely stopped before hitting a
      * button then a MouseDragged Event is generated in spite of a MouseClicked
      * event. As a result the user thinks that the system is not capturing the
      * mouse interaction events.
      * 
      * This method solve this problem. After studying such situations we
      * discovered that when a mouse clicked is lost the sequence of mouse events
      * that Java produces is : MousePressed MouseDragged+ MouseReleased Then we
      * use a Finite State Machine to detect that situations. When the pattern is
      * matched an artificial MouseClickedEvent is generated, using the
      * coordinates of the first MousePressed Event
      * 
      * Therefore the FSM works as follows:
      * 
      * Nothing MousePressed Detected -------------> Detected ---> State m
      * pressed/- State | ("") ("P") | | | | | | m dragged /- | | | | |
      * MouseDragged | | Detected | -------------- State <------- m released/
      * ("PD") generate ^ | click | | m dragged, | | m moved/- ----
      * 
      * MouseMoved events are ignored and MouseClicked real events always reset
      * the FSM as they are considered to have the highest priority
      * 
      * This method is invoked by all the methods that deal with mouse input when
      * a new Mouse Event is triggered.
      * 
      * @param e
      *            The current MouseEvent. Used for deciding the next state
      *            transition in the FSM
      */
     private void removeFakeDrags( MouseEvent e ) {
         if( e.getID( ) == MouseEvent.MOUSE_PRESSED ) {
             state = "P";
             lastPressedEvent = e;
         }
         else if( e.getID( ) == MouseEvent.MOUSE_DRAGGED && state.equals( "P" ) ) {
             state = "PD";
         }
         else if( e.getID( ) == MouseEvent.MOUSE_RELEASED && state.equals( "PD" ) ) {
             // Pressed Dragged* Released sequence detected. Add artificial MouseClicked event
             MouseEvent newClickEvent = new MouseEvent( lastPressedEvent.getComponent( ), MouseEvent.MOUSE_CLICKED, lastPressedEvent.getWhen( ), lastPressedEvent.getModifiers( ), lastPressedEvent.getX( ), lastPressedEvent.getY( ), lastPressedEvent.getXOnScreen( ), lastPressedEvent.getYOnScreen( ), 1, lastPressedEvent.isPopupTrigger( ), lastPressedEvent.getButton( ) );
             currentState.mouseClicked( newClickEvent );
 
             state = "";
             lastPressedEvent = null;
         }
         else if( e.getID( ) == MouseEvent.MOUSE_CLICKED ) {
             // MouseClicked events have more priority than any other, so then reset the FSM.
             state = "";
             lastPressedEvent = null;
         }
         else if( e.getID( ) == MouseEvent.MOUSE_MOVED ) {
             // Do nothing
         }
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
      */
     public void keyPressed( KeyEvent e ) {
 
         currentState.keyPressed( e );
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
      */
     public void keyReleased( KeyEvent arg0 ) {
 
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
      */
     public void mouseClicked( MouseEvent e ) {
 
         currentState.mouseClicked( e );
         removeFakeDrags( e );
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
      */
     public void mouseMoved( MouseEvent e ) {
 
         currentState.mouseMoved( e );
         lastMouseEvent = e;
         removeFakeDrags( e );
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
      */
     public void mousePressed( MouseEvent e ) {
 
         currentState.mousePressed( e );
         removeFakeDrags( e );
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
      */
     public void mouseReleased( MouseEvent e ) {
 
         currentState.mouseReleased( e );
         removeFakeDrags( e );
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
      */
     public void mouseEntered( MouseEvent e ) {
 
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
      */
     public void mouseExited( MouseEvent e ) {
 
     }
 
     /*
      *  (non-Javadoc)
      * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
      */
     public void mouseDragged( MouseEvent e ) {
         currentState.mouseDragged( e );
         lastMouseEvent = e;
         removeFakeDrags( e );
     }
 
     /**
      * Get the last MouseEvent
      * 
      * @return MouseEvent the last MouseEvent
      */
     public MouseEvent getLastMouseEvent( ) {
 
         return lastMouseEvent;
     }
 
     public AdventureApplet getComm( ) {
 
         return comm;
     }
 
     public void cycleCompleted( int timerId, long elapsedTime ) {
 
         //System.out.println("Timer " + timerId + " expired, executing effects.");
         Timer timer = gameTimers.get( new Integer( timerId ) );
         FunctionalEffects.storeAllEffects( timer.getEffects( ) );
     }
 
     public void timerStarted( int timerId, long currentTime ) {
 
         //System.out.println("Timer " + timerId + " starting");
         // Do nothing
     }
 
     public void timerStopped( int timerId, long currentTime ) {
 
         //System.out.println("Timer " + timerId + " was stopped, executing effects");
         Timer timer = gameTimers.get( new Integer( timerId ) );
         FunctionalEffects.storeAllEffects( timer.getPostEffects( ) );
         //timerManager.deleteTimer( timerId );
     }
 
     public boolean isDebug( ) {
 
         return debug;
     }
 
     public boolean isFromEditor( ) {
 
         return fromEditor;
     }
 
     public String processText( String text ) {
 
         return flags.processText( vars.processText( text ) );
     }
 
     public boolean showTopInventory( ) {
 
         return gameDescriptor.getInventoryPosition( ) == DescriptorData.INVENTORY_TOP || gameDescriptor.getInventoryPosition( ) == DescriptorData.INVENTORY_TOP_BOTTOM;
     }
 
     public boolean showBottomInventory( ) {
 
         return gameDescriptor.getInventoryPosition( ) == DescriptorData.INVENTORY_BOTTOM || gameDescriptor.getInventoryPosition( ) == DescriptorData.INVENTORY_TOP_BOTTOM;
     }
 
     public boolean isShowActions( ) {
         return gameDescriptor.getDefaultClickAction( ) == DescriptorData.DefaultClickAction.showActions;
     }
 
 }
