 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  * research group.
  * 
  * Copyright 2005-2010 <e-UCM> research group.
  * 
  * You can access a list of all the contributors to <e-Adventure> at:
  * http://e-adventure.e-ucm.es/contributors
  * 
  * <e-UCM> is a research group of the Department of Software Engineering and
  * Artificial Intelligence at the Complutense University of Madrid (School of
  * Computer Science).
  * 
  * C Profesor Jose Garcia Santesmases sn, 28040 Madrid (Madrid), Spain.
  * 
  * For more info please visit: <http://e-adventure.e-ucm.es> or
  * <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  * <e-Adventure> is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with <e-Adventure>. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.control.gamestate;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Transparency;
 import java.awt.color.ColorSpace;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import es.eucm.eadventure.common.auxiliar.CreateImage;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.engine.assessment.ReportDialog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.Options;
 import es.eucm.eadventure.engine.core.control.config.ConfigData;
 import es.eucm.eadventure.engine.core.data.GameText;
 import es.eucm.eadventure.engine.core.data.SaveGame;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 public class GameStateOptions extends GameState {
 
     /**
      * Width of the playable area of the screen
      */
     private final int GAME_AREA_WIDTH;
 
     /**
      * Height of the playable area of the screen
      */
     private final int GAME_AREA_HEIGHT;
 
     /**
      * Size of the options font
      */
     private static final float FONT_SIZE = 23.0f;
 
     /**
      * Size of the font to display the savegames
      */
     private static final float FONT_SIZE_SAVEGAME = 17.0f;
 
     /**
      * X offset of the first options button of a panel
      */
     private static final int FIRST_BUTTON_OFFSET_X = 5;
 
     /**
      * Y offset of the first options button of a panel
      */
     private static final int FIRST_BUTTON_OFFSET_Y = 55;
 
     /**
      * Width of the options button
      */
     private static final int BUTTON_WIDTH = 190;
 
     /**
      * Height of the options button
      */
     private static final int BUTTON_HEIGHT = 48;
 
     /**
      * Number of panels
      */
     private static final int NUMBER_OF_PANELS = 5;
 
     /**
      * Constant for the options panel
      */
     private static final int OPTIONS_PANEL = 0;
 
     /**
      * Constant for the save/load panel
      */
     private static final int SAVELOAD_PANEL = 1;
 
     /**
      * Constant for the save panel
      */
     private static final int SAVE_PANEL = 2;
 
     /**
      * Constant for the load panel
      */
     private static final int LOAD_PANEL = 3;
 
     /**
      * Constant for the configuration panel
      */
     private static final int CONFIGURATION_PANEL = 4;
 
     /**
      * Number of options of each panel (indexed by the panel constants)
      */
     private static final int[] NUMBER_OPTIONS_OF_PANEL = { 5, 3, 5, 5, 4 };
 
     /**
      * Maximum number of save slots
      */
     private static final int MAX_NUM_SAVEGAME_SLOTS = 4;
 
     /**
      * Color for the normal option text
      */
     private static final Color NORMAL_COLOR = new Color( 157, 157, 157 );
 
     /**
      * Color for the highlighted option text
      */
     private static final Color HIGHLIGHTED_COLOR = Color.WHITE;
 
     /**
      * Array of images for the panels (indexed by the panel constants)
      */
     private Image[] imgPanel;
 
     /**
      * Image of the options button
      */
     private Image imgButton;
 
     /**
      * Image of the options pressed button
      */
     private Image imgPressedButton;
 
     /**
      * Font for the options
      */
     private Font optionsFont;
 
     /**
      * Font for displaying the savegames info
      */
     private Font savegameFont;
 
     /**
      * Set of options of the game
      */
     private Options options;
 
     /**
      * Background image of the state
      */
     private BufferedImage gameImage;
 
     /**
      * Position of the panel (to calculate positions)
      */
     private Point panelPosition;
 
     /**
      * Stores the panel which is being showed
      */
     private int currentPanel;
 
     /**
      * Stores the option which is being highlighted
      */
     private int highlightedOption;
 
     /**
      * Stores the option which is being pressed
      */
     private int pressedOption;
 
     /**
      * Stores true if the mouse button is pressed
      */
     private boolean mouseButtonPressed;
 
     /**
      * Array of savegames
      */
     private SaveGame[] saveGames;
 
     /**
      * Array of boolean, storing if each savegame is occupied
      */
     private boolean[] existsSaveGame;
 
     /**
      * If there is a load click
      */
     private boolean loadGame;
 
     /**
      * Which slot to be loaded;
      */
     private int slotGame;
 
     /**
      * If there is a mouseDragged event
      */
     private boolean mouseDragged;
 
     /**
      * Default constructor
      */
     public GameStateOptions( ) {
 
         super( );
         currentPanel = OPTIONS_PANEL;
         highlightedOption = -1;
         gameImage = null;
         options = game.getOptions( );
         pressedOption = -1;
         mouseButtonPressed = false;
         mouseDragged = false;
 
         loadGame = false;
         slotGame = -1;
 
         GAME_AREA_WIDTH = GUI.getInstance( ).getGameAreaWidth( );
         GAME_AREA_HEIGHT = GUI.getInstance( ).getGameAreaHeight( );
 
         try {
             // Load the original font
             InputStream is = ResourceHandler.getInstance( ).getResourceAsStream( "gui/options/optionsFont.ttf" );
             Font originalFont = Font.createFont( Font.TRUETYPE_FONT, is );
 
             // Create the neccessary fonts
             optionsFont = originalFont.deriveFont( Font.BOLD, FONT_SIZE );
             savegameFont = originalFont.deriveFont( Font.BOLD, FONT_SIZE_SAVEGAME );
 
             // Close the input stream
             is.close( );
         }
         catch( FontFormatException e ) {
             es.eucm.eadventure.common.auxiliar.ReportDialog.GenerateErrorReport( e, Game.getInstance( ).isFromEditor( ), "UNKNOWERROR" );
         }
         catch( IOException e ) {
             es.eucm.eadventure.common.auxiliar.ReportDialog.GenerateErrorReport( e, Game.getInstance( ).isFromEditor( ), "UNKNOWERROR" );
         }
 
         imgButton = MultimediaManager.getInstance( ).loadImage( "gui/options/Button.png", MultimediaManager.IMAGE_MENU );
         if( imgButton == null )
             imgButton = createImage( 190, 48, "" );
         imgPressedButton = MultimediaManager.getInstance( ).loadImage( "gui/options/PressedButton.png", MultimediaManager.IMAGE_MENU );
         if( imgPressedButton == null )
             imgPressedButton = createImage( 190, 48, "" );
 
         imgPanel = new Image[ NUMBER_OF_PANELS ];
         imgPanel[OPTIONS_PANEL] = MultimediaManager.getInstance( ).loadImage( LoadButton( "OptionsPanel" ), MultimediaManager.IMAGE_MENU );
         if( imgPanel[OPTIONS_PANEL] == null )
             imgPanel[OPTIONS_PANEL] = createImage( 200, 300, "Options" );
 
         imgPanel[SAVELOAD_PANEL] = MultimediaManager.getInstance( ).loadImage( LoadButton( "SaveLoadPanel" ), MultimediaManager.IMAGE_MENU );
         if( imgPanel[SAVELOAD_PANEL] == null )
             imgPanel[SAVELOAD_PANEL] = createImage( 200, 204, "Save/Load" );
 
         imgPanel[SAVE_PANEL] = MultimediaManager.getInstance( ).loadImage( LoadButton( "SavePanel" ), MultimediaManager.IMAGE_MENU );
         if( imgPanel[SAVE_PANEL] == null )
             imgPanel[SAVE_PANEL] = createImage( 200, 300, "Save game" );
 
         imgPanel[LOAD_PANEL] = MultimediaManager.getInstance( ).loadImage( LoadButton( "LoadPanel" ), MultimediaManager.IMAGE_MENU );
         if( imgPanel[LOAD_PANEL] == null )
             imgPanel[LOAD_PANEL] = createImage( 200, 300, "Load game" );
 
         imgPanel[CONFIGURATION_PANEL] = MultimediaManager.getInstance( ).loadImage( LoadButton( "ConfigurationPanel" ), MultimediaManager.IMAGE_MENU );
         if( imgPanel[CONFIGURATION_PANEL] == null )
             imgPanel[CONFIGURATION_PANEL] = createImage( 200, 252, "Configuration" );
 
         panelPosition = new Point( ( GAME_AREA_WIDTH - imgPanel[currentPanel].getWidth( null ) ) / 2, ( GAME_AREA_HEIGHT - imgPanel[currentPanel].getHeight( null ) ) / 2 );
 
         saveGames = new SaveGame[ MAX_NUM_SAVEGAME_SLOTS ];
         existsSaveGame = new boolean[ MAX_NUM_SAVEGAME_SLOTS ];
         for( int i = 0; i < MAX_NUM_SAVEGAME_SLOTS; i++ ) {
             saveGames[i] = new SaveGame( );
             existsSaveGame[i] = saveGames[i].existSaveFile( game.getAdventureName( ) + "_" + i + ".txt" );
         }
     }
 
     private String LoadButton( String name ) {
 
         //route to the gui elements
         String route = "gui/options/";
         String dirButton = null;
 
         if( Game.getInstance( ).isFromEditor( ) ) {
             dirButton = route + Controller.getInstance( ).getLanguage( ) + "/" + name + ".png";
             File fichero = new File( dirButton );
             if( !fichero.exists( ) ) {
                 // if there isn't file, load the default file 
                 dirButton = route + Controller.getInstance( ).getDefaultLanguage( ) + "/" + name + ".png";
             }
         }
         else {
             try {
                 dirButton = route + ConfigData.getLanguage( ) + "/" + name + ".png";
                 File fichero = new File( dirButton );
                 if( !fichero.exists( ) ) {
                     // if there isn't file, load the default file
                     dirButton = route + ConfigData.getDefaultLanguage( ) + "/" + name + ".png";
                 }
             }
             catch( NullPointerException e ) {
                 dirButton = route + TC.get( "Language.Internationalize" ) + "/" + name + ".png";
             }
         }
         return dirButton;
     }
 
     /**
      * Sets a new active panel
      * 
      * @param currentPanel
      *            Panel to be activated
      */
     private void changePanel( int currentPanel ) {
 
         this.currentPanel = currentPanel;
         panelPosition = new Point( ( GAME_AREA_WIDTH - imgPanel[currentPanel].getWidth( null ) ) / 2, ( GAME_AREA_HEIGHT - imgPanel[currentPanel].getHeight( null ) ) / 2 );
         highlightedOption = -1;
     }
 
     /**
      * Saves the game in the specified slot
      * 
      * @param gameSlot
      *            Savegame slot
      */
     private void saveGame( int gameSlot ) {
 
         game.save( game.getAdventureName( ) + "_" + gameSlot + ".txt" );
 
         //existsSaveGame[gameSlot] = saveGames[gameSlot].loadTxt( game.getAdventureName( ) + "_" + gameSlot + ".txt" );
         existsSaveGame[gameSlot] = saveGames[gameSlot].existSaveFile( game.getAdventureName( ) + "_" + gameSlot + ".txt" );
 
     }
 
     /**
      * Loads the game from the specified slot
      * 
      * @param gameSlot
      *            Savegame slot
      */
     private void loadGame( int gameSlot ) {
 
         if( game.getGameDescriptor( ).getTitle( ).equals( saveGames[gameSlot].getTitle( ) ) )
             game.load( game.getAdventureName( ) + "_" + gameSlot + ".txt" );
     }
 
     /**
      * Switchs the music between on and off
      */
     private void switchMusic( ) {
 
         if( options.isMusicActive( ) )
             options.setMusicActive( false );
         else
             options.setMusicActive( true );
     }
 
     /**
      * Switchs the effects between on and off
      */
     private void switchEffects( ) {
 
         if( options.isEffectsActive( ) )
             options.setEffectsActive( false );
         else
             options.setEffectsActive( true );
     }
 
     /**
      * Changes the text speed, in a cyclic way
      */
     private void changeTextSpeed( ) {
 
         int textSpeed = options.getTextSpeed( );
 
         textSpeed = ( textSpeed + 1 ) % Options.TEXT_NUM_SPEEDS;
 
         options.setTextSpeed( textSpeed );
     }
 
     /**
      * Exit the options panel and returns to the game
      * 
      * @throws InterruptedException
      */
     private void backToGame( ) {
 
         game.saveOptions( );
         if( options.isMusicActive( ) )
             game.getFunctionalScene( ).playBackgroundMusic( );
         else
             game.getFunctionalScene( ).stopBackgroundMusic( );
 
         if( !options.isEffectsActive( ) )
             MultimediaManager.getInstance( ).stopAllSounds( );
 
         game.setState( Game.STATE_PLAYING );
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.gamestate.GameState#mainLoop(long, int)
      */
     @Override
     public void mainLoop( long elapsedTime, int fps ) {
 
         // Set the default cursor
         GUI.getInstance( ).setDefaultCursor( );
 
         // If the background image is not loaded, load it
         if( gameImage == null ) {
             gameImage = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( 800, 600, Transparency.OPAQUE );
             Graphics2D graphicGrey = (Graphics2D) gameImage.getGraphics( );
             game.getFunctionalScene( ).draw( );
             GUI.getInstance( ).drawScene( graphicGrey, elapsedTime );
             GUI.getInstance( ).drawHUD( graphicGrey );
             ColorConvertOp colorConvert = new ColorConvertOp( ColorSpace.getInstance( ColorSpace.CS_GRAY ), null );
             colorConvert.filter( gameImage, gameImage );
             graphicGrey.dispose( );
         }
 
         // Get the graphics
         Graphics2D g = GUI.getInstance( ).getGraphics( );
         g.clearRect( 0, 0, GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT );
 
         // Draw the background image
         g.drawImage( gameImage, 0, 0, null );
 
         // Draw the FPS
         //g.setColor( Color.WHITE );
         //g.drawString( Integer.toString( fps ), 780, 14 );
 
         // Draw the options panel
         g.drawImage( imgPanel[currentPanel], panelPosition.x, panelPosition.y, null );
 
         // The save and the load panel have special painting functions
         if( currentPanel == SAVE_PANEL || currentPanel == LOAD_PANEL ) {
             int i;
             // For every savegame slot
             g.setFont( savegameFont );
             for( i = 0; i < MAX_NUM_SAVEGAME_SLOTS; i++ ) {
                 boolean isValidSlot = isValidSlotGame( i );
                 // Draw the button
                 g.drawImage( ( highlightedOption == i && mouseButtonPressed && isValidSlot ) ? imgPressedButton : imgButton, panelPosition.x + FIRST_BUTTON_OFFSET_X, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i, null );
 
                 // Set the color of the text
                 g.setColor( ( highlightedOption == i && isValidSlot ) ? HIGHLIGHTED_COLOR : NORMAL_COLOR );
 
                 // If the slot exists, draw the data
                 if( existsSaveGame[i] ) {
                     String[] title = { saveGames[i].getTitle( ), saveGames[i].getSaveTime( ) };
                     if( !isValidSlot ) {
                         title[0] = TC.get( "Options.versionError1" );
                         title[1] = TC.get( "Options.versionError2" );
                     }
 
                     GUI.drawString( g, title, GUI.WINDOW_WIDTH / 2, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT / 2 + BUTTON_HEIGHT * i );
                 }
 
                 // If it doesn't exist, show an empty message
                 else
                     GUI.drawString( g, GameText.TEXT_EMPTY, GUI.WINDOW_WIDTH / 2, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT / 2 + BUTTON_HEIGHT * i );
             }
 
             // Draw the "Back" button
             g.setFont( optionsFont );
 
             g.drawImage( ( highlightedOption == i && mouseButtonPressed ) ? imgPressedButton : imgButton, panelPosition.x + FIRST_BUTTON_OFFSET_X, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i, null );
             g.setColor( highlightedOption == i ? HIGHLIGHTED_COLOR : NORMAL_COLOR );
             GUI.drawString( g, GameText.TEXT_BACK, GUI.WINDOW_WIDTH / 2, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT / 2 + BUTTON_HEIGHT * i );
         }
 
         // For the other panels, build and array of options and display it
         else {
             // Create a set of strings for the options
             String[] panelOptionsText;
 
             // Set of general options
             if( currentPanel == OPTIONS_PANEL ) {
                 panelOptionsText = new String[] { GameText.TEXT_SAVE_LOAD, GameText.TEXT_CONFIGURATION, GameText.TEXT_GENERATE_REPORT, GameText.TEXT_EXIT_GAME, GameText.TEXT_BACK_TO_GAME };
             }
 
             // Set of saveload options 
             else if( currentPanel == SAVELOAD_PANEL ) {
                 panelOptionsText = new String[] { GameText.TEXT_SAVE, GameText.TEXT_LOAD, GameText.TEXT_BACK };
             }
 
             // Set of configuration options
             else {
                 panelOptionsText = new String[] { GameText.TEXT_MUSIC + " " + ( options.isMusicActive( ) ? GameText.TEXT_ON : GameText.TEXT_OFF ), GameText.TEXT_EFFECTS + " " + ( options.isEffectsActive( ) ? GameText.TEXT_ON : GameText.TEXT_OFF ), GameText.TEXT_TEXT_SPEED + ": " + Options.TEXT_SPEED_PRINT_VALUES[options.getTextSpeed( )], GameText.TEXT_BACK };
             }
 
             // Paint the options
             g.setFont( optionsFont );
             for( int i = 0; i < NUMBER_OPTIONS_OF_PANEL[currentPanel]; i++ ) {
                 if( Game.getInstance( ).isAppletMode( ) && ( i == 0 || i == 3 ) ) {
                     g.drawImage( imgPressedButton, panelPosition.x + FIRST_BUTTON_OFFSET_X, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i, null );
                     g.setColor( NORMAL_COLOR );
                 }
                 else {
                     g.drawImage( ( highlightedOption == i && mouseButtonPressed ) ? imgPressedButton : imgButton, panelPosition.x + FIRST_BUTTON_OFFSET_X, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i, null );
                     g.setColor( highlightedOption == i ? HIGHLIGHTED_COLOR : NORMAL_COLOR );
                 }
                 if( panelOptionsText.length > i )
                     GUI.drawString( g, panelOptionsText[i], GUI.WINDOW_WIDTH / 2, panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT / 2 + BUTTON_HEIGHT * i );
             }
         }
 
         GUI.getInstance( ).endDraw( );
         g.dispose( );
 
         if( loadGame )
             loadGame( slotGame );
     }
 
     private boolean isValidSlotGame( int i ) {
 
         return saveGames[i].getVersionNumber( ) == Integer.parseInt( Game.getInstance( ).getGameDescriptor( ).getVersionNumber( ) ) && ( saveGames[i].getProjectName( ) == null || saveGames[i].getProjectName( ).equals( Game.getInstance( ).getGameDescriptor( ).getProjectName( ) ) );
 
     }
 
     @Override
     public void mousePressed( MouseEvent e ) {
 
         mouseDragged = false;
         pressedOption = highlightedOption;
         if( highlightedOption != -1 )
             mouseButtonPressed = true;
     }
 
     @Override
     public void mouseClicked( MouseEvent e ) {
 
         mouseDragged = true;
         mouseReleased( e );
     }
 
     @Override
     public void mouseReleased( MouseEvent e ) {
 
         if( mouseDragged ) {
             if( highlightedOption != -1 ) {
                 switch( currentPanel ) {
                     case OPTIONS_PANEL:
                         mouseReleasedMain( e );
                         break;
                     case SAVELOAD_PANEL:
                         mouseReleasedSaveLoad( e );
                         break;
                     case SAVE_PANEL:
                         mouseReleasedSave( e );
                         break;
                     case LOAD_PANEL:
                         mouseReleasedLoad( e );
                         break;
                     case CONFIGURATION_PANEL:
                         mouseReleasedConfiguration( e );
                         break;
 
                 }
                 pressedOption = -1;
             }
             mouseButtonPressed = false;
         }
     }
 
     /**
      * Called when the mouse has been released in the options panel
      * 
      * @param e
      *            The mouse event
      */
     private void mouseReleasedMain( MouseEvent e ) {
 
         switch( highlightedOption ) {
             case 0:
                 changePanel( SAVELOAD_PANEL );
                 break;
             case 1:
                 changePanel( CONFIGURATION_PANEL );
                 break;
             case 2:
                 new ReportDialog( GUI.getInstance( ).getJFrame( ), game.getAssessmentEngine( ), game.getAdventureName( ) );
                 break;
             case 3:
                 game.setGameOver( );
                 break;
             case 4:
                 backToGame( );
                 break;
         }
     }
 
     /**
      * Called when the mouse has been released in the save/load panel
      * 
      * @param e
      *            The mouse event
      */
     private void mouseReleasedSaveLoad( MouseEvent e ) {
 
         switch( highlightedOption ) {
             case 0:
                 changePanel( SAVE_PANEL );
                 break;
             case 1:
                 changePanel( LOAD_PANEL );
                 break;
             case 2:
                 changePanel( OPTIONS_PANEL );
                 break;
         }
     }
 
     /**
      * Called when the mouse has been released in the save panel
      * 
      * @param e
      *            The mouse event
      */
     private void mouseReleasedSave( MouseEvent e ) {
 
         if( highlightedOption == 4 ) {
             changePanel( SAVELOAD_PANEL );
         }
         else {
             for( int i = 0; i < MAX_NUM_SAVEGAME_SLOTS; i++ ) {
                 if( i == highlightedOption )
                     saveGame( i );
             }
         }
     }
 
     /**
      * Called when the mouse has been released in the load panel
      * 
      * @param e
      *            The mouse event
      */
     private void mouseReleasedLoad( MouseEvent e ) {
 
         if( highlightedOption == 4 ) {
             changePanel( SAVELOAD_PANEL );
         }
         else {
             for( int i = 0; i < MAX_NUM_SAVEGAME_SLOTS; i++ ) {
                 if( i == highlightedOption && isValidSlotGame( i ) ) {
                     loadGame = true;
                     slotGame = i;
                 }
             }
         }
     }
 
     /**
      * Called when the mouse has been released in the configuration panel
      * 
      * @param e
      *            The mouse event
      */
     private void mouseReleasedConfiguration( MouseEvent e ) {
 
         switch( highlightedOption ) {
             case 0:
                 switchMusic( );
                 break;
             case 1:
                 switchEffects( );
                 break;
             case 2:
                 changeTextSpeed( );
                 break;
             case 3:
                 changePanel( OPTIONS_PANEL );
                 break;
         }
     }
 
     @Override
     public void mouseMoved( MouseEvent e ) {
 
         highlightedOption = -1;
         if( panelPosition.x + FIRST_BUTTON_OFFSET_X < e.getX( ) && e.getX( ) < panelPosition.x + FIRST_BUTTON_OFFSET_X + BUTTON_WIDTH ) {
             for( int i = 0; i < NUMBER_OPTIONS_OF_PANEL[currentPanel]; i++ ) {
                 if( panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i < e.getY( ) && e.getY( ) < panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * ( i + 1 ) ) {
                     highlightedOption = i;
                     validateOption( );
                 }
             }
         }
     }
 
     @Override
     public void mouseDragged( MouseEvent e ) {
 
         mouseDragged = true;
         highlightedOption = -1;
         if( panelPosition.x + FIRST_BUTTON_OFFSET_X < e.getX( ) && e.getX( ) < panelPosition.x + FIRST_BUTTON_OFFSET_X + BUTTON_WIDTH ) {
             for( int i = 0; i < NUMBER_OPTIONS_OF_PANEL[currentPanel]; i++ ) {
                 if( panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * i < e.getY( ) && e.getY( ) < panelPosition.y + FIRST_BUTTON_OFFSET_Y + BUTTON_HEIGHT * ( i + 1 ) ) {
                     if( pressedOption != -1 && pressedOption == i ) {
                         highlightedOption = i;
                         validateOption( );
                     }
                 }
             }
         }
     }
 
     @Override
     public void keyPressed( KeyEvent e ) {
 
         if( e.getKeyCode( ) == KeyEvent.VK_ESCAPE ) {
             if( currentPanel == OPTIONS_PANEL )
                 backToGame( );
             else if( currentPanel == SAVE_PANEL || currentPanel == LOAD_PANEL )
                 changePanel( SAVELOAD_PANEL );
             else
                 changePanel( OPTIONS_PANEL );
         }
     }
 
     /**
      * This method analyze the current panel and selected option, and check if
      * are good combine
      */
     private void validateOption( ) {
 
         switch( currentPanel ) {
             case OPTIONS_PANEL:
                 if( highlightedOption > NUMBER_OPTIONS_OF_PANEL[OPTIONS_PANEL] ) {
                     highlightedOption = -1;
                 }
                 else if( ( highlightedOption == 0 || highlightedOption == 3 ) && Game.getInstance( ).isAppletMode( ) ) {
                     highlightedOption = -1;
                 }
 
                 break;
             case SAVELOAD_PANEL:
                 if( highlightedOption > NUMBER_OPTIONS_OF_PANEL[SAVELOAD_PANEL] ) {
                     highlightedOption = -1;
                 }
                 break;
             case SAVE_PANEL:
                 if( highlightedOption > NUMBER_OPTIONS_OF_PANEL[SAVE_PANEL] ) {
                     highlightedOption = -1;
                 }
                 break;
             case LOAD_PANEL:
                 if( highlightedOption > NUMBER_OPTIONS_OF_PANEL[LOAD_PANEL] ) {
                     highlightedOption = -1;
                 }
                 break;
             case CONFIGURATION_PANEL:
                 if( highlightedOption > NUMBER_OPTIONS_OF_PANEL[CONFIGURATION_PANEL] ) {
                     highlightedOption = -1;
                 }
                 break;
         }
 
     }
 
     private Image createImage( int width, int height, String text ) {
 
         return CreateImage.createImage( width, height, text, optionsFont );
         /*BufferedImage im = new BufferedImage ( width, height, BufferedImage.TYPE_INT_RGB);
         
         Graphics2D gr = (Graphics2D)im.getGraphics();
         gr.setColor(Color.black);
         gr.fillRect(0, 0, width, height);
         gr.setColor(Color.LIGHT_GRAY);
         for (int i=0; i<5; i++)
         	gr.drawRect(i, i, width-i, height-i);
         gr.setColor(Color.white);
         gr.setFont( optionsFont );
         gr.drawString(text, 5, 25);
         gr.dispose();
         
         return im;*/
     }
 }
