 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  */
 package es.eucm.eadventure.editor.control;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import es.eucm.eadventure.common.auxiliar.File;
 import es.eucm.eadventure.common.auxiliar.ReleaseFolders;
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.adventure.AdventureData;
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.data.animation.Animation;
 import es.eucm.eadventure.common.data.animation.Frame;
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.common.data.chapter.Trajectory;
 import es.eucm.eadventure.common.data.chapter.elements.Player;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.common.loader.incidences.Incidence;
 import es.eucm.eadventure.editor.auxiliar.filefilters.EADFileFilter;
 import es.eucm.eadventure.editor.auxiliar.filefilters.FolderFileFilter;
 import es.eucm.eadventure.editor.auxiliar.filefilters.JARFileFilter;
 import es.eucm.eadventure.editor.auxiliar.filefilters.XMLFileFilter;
 import es.eucm.eadventure.editor.control.config.ConfigData;
 import es.eucm.eadventure.editor.control.config.ProjectConfigData;
 import es.eucm.eadventure.editor.control.config.SCORMConfigData;
 import es.eucm.eadventure.editor.control.controllers.AdventureDataControl;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.EditorImageLoader;
 import es.eucm.eadventure.editor.control.controllers.VarFlagsController;
 import es.eucm.eadventure.editor.control.controllers.adaptation.AdaptationProfilesDataControl;
 import es.eucm.eadventure.editor.control.controllers.assessment.AssessmentProfilesDataControl;
 import es.eucm.eadventure.editor.control.controllers.atrezzo.AtrezzoDataControl;
 import es.eucm.eadventure.editor.control.controllers.character.NPCDataControl;
 import es.eucm.eadventure.editor.control.controllers.general.AdvancedFeaturesDataControl;
 import es.eucm.eadventure.editor.control.controllers.general.ChapterDataControl;
 import es.eucm.eadventure.editor.control.controllers.general.ChapterListDataControl;
 import es.eucm.eadventure.editor.control.controllers.item.ItemDataControl;
 import es.eucm.eadventure.editor.control.controllers.metadata.lom.LOMDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.SceneDataControl;
 import es.eucm.eadventure.editor.control.tools.Tool;
 import es.eucm.eadventure.editor.control.tools.general.SwapPlayerModeTool;
 import es.eucm.eadventure.editor.control.tools.general.chapters.AddChapterTool;
 import es.eucm.eadventure.editor.control.tools.general.chapters.DeleteChapterTool;
 import es.eucm.eadventure.editor.control.tools.general.chapters.MoveChapterTool;
 import es.eucm.eadventure.editor.control.writer.Writer;
 import es.eucm.eadventure.editor.data.support.IdentifierSummary;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 import es.eucm.eadventure.editor.gui.LoadingScreen;
 import es.eucm.eadventure.editor.gui.MainWindow;
 import es.eucm.eadventure.editor.gui.ProjectFolderChooser;
 import es.eucm.eadventure.editor.gui.displaydialogs.InvalidReportDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.AdventureDataDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.ExportToLOMDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.GraphicConfigDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.SearchDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.VarsFlagsDialog;
 import es.eucm.eadventure.editor.gui.editdialogs.customizeguidialog.CustomizeGUIDialog;
 import es.eucm.eadventure.editor.gui.metadatadialog.ims.IMSDialog;
 import es.eucm.eadventure.editor.gui.metadatadialog.lomdialog.LOMDialog;
 import es.eucm.eadventure.editor.gui.metadatadialog.lomes.LOMESDialog;
 import es.eucm.eadventure.editor.gui.startdialog.StartDialog;
 import es.eucm.eadventure.engine.EAdventureDebug;
 
 /**
  * This class is the main controller of the application. It holds the main
  * operations and data to control the editor.
  * 
  * @author Bruno Torijano Bueno
  */
 /*
  * @updated by Javier Torrente. New functionalities added - Support for .ead files. Therefore <e-Adventure> files are no
  * longer .zip but .ead
  */
 
 public class Controller {
 
     /**
      * Id for the complete chapter data element.
      */
     public static final int CHAPTER = 0;
 
     /**
      * Id for the scenes list element.
      */
     public static final int SCENES_LIST = 1;
 
     /**
      * Id for the scene element.
      */
     public static final int SCENE = 2;
 
     /**
      * Id for the exits list element.
      */
     public static final int EXITS_LIST = 3;
 
     /**
      * Id for the exit element.
      */
     public static final int EXIT = 4;
 
     /**
      * Id for the item references list element.
      */
     public static final int ITEM_REFERENCES_LIST = 5;
 
     /**
      * Id for the item reference element.
      */
     public static final int ITEM_REFERENCE = 6;
 
     /**
      * Id for the NPC references list element.
      */
     public static final int NPC_REFERENCES_LIST = 7;
 
     /**
      * Id for the NPC reference element.
      */
     public static final int NPC_REFERENCE = 8;
 
     /**
      * Id for the cutscenes list element.
      */
     public static final int CUTSCENES_LIST = 9;
 
     /**
      * Id for the slidescene element.
      */
     public static final int CUTSCENE_SLIDES = 10;
 
     public static final int CUTSCENE = 910;
 
     public static final int CUTSCENE_VIDEO = 37;
 
     /**
      * Id for the books list element.
      */
     public static final int BOOKS_LIST = 11;
 
     /**
      * Id for the book element.
      */
     public static final int BOOK = 12;
 
     /**
      * Id for the book paragraphs list element.
      */
     public static final int BOOK_PARAGRAPHS_LIST = 13;
 
     /**
      * Id for the title paragraph element.
      */
     public static final int BOOK_TITLE_PARAGRAPH = 14;
 
     /**
      * Id for the text paragraph element.
      */
     public static final int BOOK_TEXT_PARAGRAPH = 15;
 
     /**
      * Id for the bullet paragraph element.
      */
     public static final int BOOK_BULLET_PARAGRAPH = 16;
 
     /**
      * Id for the image paragraph element.
      */
     public static final int BOOK_IMAGE_PARAGRAPH = 17;
 
     /**
      * Id for the items list element.
      */
     public static final int ITEMS_LIST = 18;
 
     /**
      * Id for the item element.
      */
     public static final int ITEM = 19;
 
     /**
      * Id for the actions list element.
      */
     public static final int ACTIONS_LIST = 20;
 
     /**
      * Id for the "Examine" action element.
      */
     public static final int ACTION_EXAMINE = 21;
 
     /**
      * Id for the "Grab" action element.
      */
     public static final int ACTION_GRAB = 22;
 
     /**
      * Id for the "Use" action element.
      */
     public static final int ACTION_USE = 23;
 
     /**
      * Id for the "Custom" action element.
      */
     public static final int ACTION_CUSTOM = 230;
 
     /**
      * Id for the "Talk to" action element.
      */
     public static final int ACTION_TALK_TO = 231;
 
     /**
      * Id for the "Use with" action element.
      */
     public static final int ACTION_USE_WITH = 24;
 
     /**
      * Id for the "Give to" action element.
      */
     public static final int ACTION_GIVE_TO = 25;
 
     /**
      * Id for the "Drag-to" action element.
      */
     public static final int ACTION_DRAG_TO = 251;
 
     /**
      * Id for the "Custom interact" action element.
      */
     public static final int ACTION_CUSTOM_INTERACT = 250;
 
     /**
      * Id for the player element.
      */
     public static final int PLAYER = 26;
 
     /**
      * Id for the NPCs list element.
      */
     public static final int NPCS_LIST = 27;
 
     /**
      * Id for the NPC element.
      */
     public static final int NPC = 28;
 
     /**
      * Id for the conversation references list element.
      */
     public static final int CONVERSATION_REFERENCES_LIST = 29;
 
     /**
      * Id for the conversation reference element.
      */
     public static final int CONVERSATION_REFERENCE = 30;
 
     /**
      * Id for the conversations list element.
      */
     public static final int CONVERSATIONS_LIST = 31;
 
     /**
      * Id for the tree conversation element.
      */
     public static final int CONVERSATION_TREE = 32;
 
     /**
      * Id for the graph conversation element.
      */
     public static final int CONVERSATION_GRAPH = 33;
 
     /**
      * Id for the graph conversation element.
      */
     public static final int CONVERSATION_DIALOGUE_LINE = 330;
 
     /**
      * Id for the graph conversation element.
      */
     public static final int CONVERSATION_OPTION_LINE = 331;
 
     /**
      * Id for the resources element.
      */
     public static final int RESOURCES = 34;
 
     /**
      * Id for the next scene element.
      */
     public static final int NEXT_SCENE = 35;
 
     /**
      * If for the end scene element.
      */
     public static final int END_SCENE = 36;
 
     /**
      * Id for Assessment Rule
      */
     public static final int ASSESSMENT_RULE = 38;
 
     /**
      * Id for Adaptation Rule
      */
     public static final int ADAPTATION_RULE = 39;
 
     /**
      * Id for Assessment Rules
      */
     public static final int ASSESSMENT_PROFILE = 40;
 
     /**
      * Id for Adaptation Rules
      */
     public static final int ADAPTATION_PROFILE = 41;
 
     /**
      * Id for the styled book element.
      */
     public static final int STYLED_BOOK = 42;
 
     /**
      * Id for the page of a STYLED_BOK.
      */
     public static final int BOOK_PAGE = 43;
 
     /**
      * Id for timers.
      */
     public static final int TIMER = 44;
 
     /**
      * Id for the list of timers.
      */
     public static final int TIMERS_LIST = 45;
 
     /**
      * Id for the advanced features node.
      */
     public static final int ADVANCED_FEATURES = 46;
 
     /**
      * Id for the assessment profiles node.
      */
     public static final int ASSESSSMENT_PROFILES = 47;
 
     /**
      * Id for the adaptation profiles node.
      */
     public static final int ADAPTATION_PROFILES = 48;
 
     /**
      * Id for timed assessment rules
      */
     public static final int TIMED_ASSESSMENT_RULE = 49;
 
     /**
      * Id for active areas list.
      */
     public static final int ACTIVE_AREAS_LIST = 50;
 
     /**
      * Id for active area
      */
     public static final int ACTIVE_AREA = 51;
 
     /**
      * Id for barriers list.
      */
     public static final int BARRIERS_LIST = 52;
 
     /**
      * Id for barrier
      */
     public static final int BARRIER = 53;
 
     /**
      * Id for global state
      */
     public static final int GLOBAL_STATE = 54;
 
     /**
      * Id for global state list
      */
     public static final int GLOBAL_STATE_LIST = 55;
 
     /**
      * Id for macro
      */
     public static final int MACRO = 56;
 
     /**
      * Id for macro list
      */
     public static final int MACRO_LIST = 57;
 
     /**
      * Id for atrezzo item element
      */
     public static final int ATREZZO = 58;
 
     /**
      * Id for atrezzo list element
      */
     public static final int ATREZZO_LIST = 59;
 
     /**
      * Id for atrezzo reference
      */
     public static final int ATREZZO_REFERENCE = 60;
 
     /**
      * Id for atrezzo references list
      */
     public static final int ATREZZO_REFERENCES_LIST = 61;
 
     public static final int NODE = 62;
 
     public static final int SIDE = 63;
 
     public static final int TRAJECTORY = 64;
 
     public static final int ANIMATION = 65;
 
     public static final int EFFECT = 66;
 
     //TYPES OF EAD FILES
     public static final int FILE_ADVENTURE_1STPERSON_PLAYER = 0;
 
     public static final int FILE_ADVENTURE_3RDPERSON_PLAYER = 1;
 
     public static final int FILE_ASSESSMENT = 2;
 
     public static final int FILE_ADAPTATION = 3;
 
     /**
      * Identifiers for differents scorm profiles
      */
     public static final int SCORM12 = 0;
 
     public static final int SCORM2004 = 1;
 
     public static final int AGREGA = 2;
 
     /**
      * Singleton instance.
      */
     private static Controller controllerInstance = null;
 
     /**
      * The main window of the application.
      */
     private MainWindow mainWindow;
 
     /**
      * The complete path to the current open ZIP file.
      */
     private String currentZipFile;
 
     /**
      * The path to the folder that holds the open file.
      */
     private String currentZipPath;
 
     /**
      * The name of the file that is being currently edited. Used only to display
      * info.
      */
     private String currentZipName;
 
     /**
      * The data of the adventure being edited.
      */
     private AdventureDataControl adventureDataControl;
 
     /**
      * Stores if the data has been modified since the last save.
      */
     private boolean dataModified;
 
     /**
      * Stores the file that contains the GUI strings.
      */
     private String languageFile;
 
     private LoadingScreen loadingScreen;
 
     private String lastDialogDirectory;
 
     /*private boolean isTempFile = false;
 
     public boolean isTempFile( ) {
     	return isTempFile;
     }*/
 
     private ChapterListDataControl chaptersController;
 
     private AutoSave autoSave;
 
     private Timer autoSaveTimer;
 
     private boolean isLomEs = false;
 
     /**
      * Store all effects selection. Connects the type of effect with the number
      * of times that has been used
      */
     // private SelectedEffectsController selectedEffects;
     /**
      * Void and private constructor.
      */
     private Controller( ) {
 
         chaptersController = new ChapterListDataControl( );
     }
 
     private String getCurrentExportSaveFolder( ) {
 
         return ReleaseFolders.exportsFolder( ).getAbsolutePath( );
     }
 
     public String getCurrentLoadFolder( ) {
 
         return ReleaseFolders.projectsFolder( ).getAbsolutePath( );
     }
 
     public void setLastDirectory( String directory ) {
 
         this.lastDialogDirectory = directory;
     }
 
     public String getLastDirectory( ) {
 
         if( lastDialogDirectory != null ) {
             return lastDialogDirectory;
         }
         else
             return ReleaseFolders.projectsFolder( ).getAbsolutePath( );
     }
 
     /**
      * Returns the instance of the controller.
      * 
      * @return The instance of the controller
      */
     public static Controller getInstance( ) {
 
         if( controllerInstance == null )
             controllerInstance = new Controller( );
 
         return controllerInstance;
     }
 
     public int playerMode( ) {
 
         return adventureDataControl.getPlayerMode( );
     }
 
     /**
      * Initializing function.
      */
     public void init( String arg ) {
 
         // Load the configuration
         ConfigData.loadFromXML( ReleaseFolders.configFileEditorRelativePath( ) );
         ProjectConfigData.init( );
         SCORMConfigData.init( );
 
         // Create necessary folders if no created befor
         File projectsFolder = ReleaseFolders.projectsFolder( );
         if( !projectsFolder.exists( ) ) {
             projectsFolder.mkdirs( );
         }
         File tempFolder = ReleaseFolders.webTempFolder( );
         if( !tempFolder.exists( ) ) {
             projectsFolder.mkdirs( );
         }
         File exportsFolder = ReleaseFolders.exportsFolder( );
         if( !exportsFolder.exists( ) ) {
             exportsFolder.mkdirs( );
         }
 
         //Create splash screen
         loadingScreen = new LoadingScreen( "PRUEBA", getLoadingImage( ), null );
 
         // Set values for language config
         languageFile = ReleaseFolders.LANGUAGE_UNKNOWN;
         setLanguage( ReleaseFolders.getLanguageFromPath( ConfigData.getLanguangeFile( ) ), false );
 
         // Create a list for the chapters
         chaptersController = new ChapterListDataControl( );
 
         // Inits the controller with empty data
         currentZipFile = null;
         currentZipPath = null;
         currentZipName = null;
         //adventureData = new AdventureDataControl( TextConstants.getText( "DefaultValue.AdventureTitle" ), TextConstants.getText( "DefaultValue.ChapterTitle" ), TextConstants.getText( "DefaultValue.SceneId" ) );
         //selectedChapter = 0;
         //chapterDataControlList.add( new ChapterDataControl( getSelectedChapterData( ) ) );
         //identifierSummary = new IdentifierSummary( getSelectedChapterData( ) );
 
         dataModified = false;
 
         //Create main window and hide it
         mainWindow = new MainWindow( );
         mainWindow.setVisible( false );
 
         // Prompt the user to create a new adventure or to load one
         //while( currentZipFile == null ) {
         // Load the options and show the dialog
         StartDialog start = new StartDialog( );
 
         if( arg != null ) {
             File projectFile = new File( arg );
             if( projectFile.exists( ) ) {
                 if( projectFile.getAbsolutePath( ).toLowerCase( ).endsWith( ".eap" ) ) {
                     String absolutePath = projectFile.getPath( );
                     loadFile( absolutePath.substring( 0, absolutePath.length( ) - 4 ), true );
                 }
                 else if( projectFile.isDirectory( ) && projectFile.exists( ) )
                     loadFile( start.getSelectedFile( ).getAbsolutePath( ), true );
             }
         }
 
         else if( ConfigData.showStartDialog( ) ) {
             int op = start.showOpenDialog( null );
             //start.end();
             if( op == StartDialog.NEW_FILE_OPTION ) {
                 newFile( start.getFileType( ) );
             }
             else if( op == StartDialog.OPEN_FILE_OPTION ) {
                 java.io.File selectedFile = start.getSelectedFile( );
                 if( selectedFile.getAbsolutePath( ).toLowerCase( ).endsWith( ".eap" ) ) {
                     String absolutePath = selectedFile.getPath( );
                     loadFile( absolutePath.substring( 0, absolutePath.length( ) - 4 ), true );
                 }
                 else if( selectedFile.isDirectory( ) && selectedFile.exists( ) )
                     loadFile( start.getSelectedFile( ).getAbsolutePath( ), true );
                 else {
                     this.importGame( selectedFile.getAbsolutePath( ) );
                 }
             }
             else if( op == StartDialog.RECENT_FILE_OPTION ) {
                 loadFile( start.getRecentFile( ).getAbsolutePath( ), true );
             }
             else if( op == StartDialog.CANCEL_OPTION ) {
                 exit( );
             }
             //selectedChapter = 0;
         }
 
         if( currentZipFile == null ) {
             //newFile( FILE_ADVENTURE_3RDPERSON_PLAYER );
             //selectedChapter = -1;
             mainWindow.reloadData( );
         }
         /*
          * int optionSelected = mainWindow.showOptionDialog( TextConstants.getText( "StartDialog.Title" ),
          * TextConstants.getText( "StartDialog.Message" ), options );
          *  // If the user wants to create a new file, show the dialog if( optionSelected == 0 ) newFile( );
          *  // If the user wants to load a existing adventure, show the load dialog else if( optionSelected == 1 )
          * loadFile( );
          *  // If the dialog was closed, exit the aplication else if( optionSelected == JOptionPane.CLOSED_OPTION )
          * exit( );
          */
         //}
         // Show the window
         /*mainWindow.setEnabled( true );
         mainWindow.reloadData( );
         try {
         	Thread.sleep( 1 );
         } catch( InterruptedException e ) {
         	// TODO Auto-generated catch block
         	e.printStackTrace();
         }*/
         // Create the main window and hide it
         //mainWindow.setVisible( false );
         // initialize the selected effects container
         //selectedEffects = new SelectedEffectsController();
 
         mainWindow.setResizable( true );
         mainWindow.setEnabled( true );
         mainWindow.setVisible( true );
         //DEBUGGING
         //tsd = new ToolSystemDebugger( chaptersController );
     }
 
     /*public void addSelectedEffect(String name){
         selectedEffects.addSelectedEffect(name);
     }
     
     public SelectedEffectsController getSelectedEffectsController(){
         return selectedEffects;
     }*/
 
     public void startAutoSave( int minutes ) {
 
         stopAutoSave( );
 
         if( ( ProjectConfigData.existsKey( "autosave" ) && ProjectConfigData.getProperty( "autosave" ).equals( "yes" ) ) || !ProjectConfigData.existsKey( "autosave" ) ) {
             /*			autoSaveTimer = new Timer();
             			autoSave = new AutoSave();
             			autoSaveTimer.schedule(autoSave, 10000, minutes * 60 * 1000);
             */}
         if( !ProjectConfigData.existsKey( "autosave" ) )
             ProjectConfigData.setProperty( "autosave", "yes" );
     }
 
     public void stopAutoSave( ) {
 
         if( autoSaveTimer != null ) {
             autoSaveTimer.cancel( );
             autoSave.stop( );
             autoSaveTimer = null;
         }
         autoSave = null;
     }
 
     //private ToolSystemDebugger tsd;
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // General data functions of the aplication
 
     /**
      * Returns the complete path to the currently open Project.
      * 
      * @return The complete path to the ZIP file, null if none is open
      */
     public String getProjectFolder( ) {
 
         return currentZipFile;
     }
 
     /**
      * Returns the File object representing the currently open Project.
      * 
      * @return The complete path to the ZIP file, null if none is open
      */
     public File getProjectFolderFile( ) {
 
         return new File( currentZipFile );
     }
 
     /**
      * Returns the name of the file currently open.
      * 
      * @return The name of the current file
      */
     public String getFileName( ) {
 
         String filename;
 
         // Show "New" if no current file is currently open
         if( currentZipName != null )
             filename = currentZipName;
         else
             filename = "http://e-adventure.e-ucm.es";
 
         return filename;
     }
 
     /**
      * Returns the parent path of the file being currently edited.
      * 
      * @return Parent path of the current file
      */
     public String getFilePath( ) {
 
         return currentZipPath;
     }
 
     /**
      * Returns an array with the chapter titles.
      * 
      * @return Array with the chapter titles
      */
     public String[] getChapterTitles( ) {
 
         return chaptersController.getChapterTitles( );
     }
 
     /**
      * Returns the index of the chapter currently selected.
      * 
      * @return Index of the selected chapter
      */
     public int getSelectedChapter( ) {
 
         return chaptersController.getSelectedChapter( );
     }
 
     /**
      * Returns the selected chapter data controller.
      * 
      * @return The selected chapter data controller
      */
     public ChapterDataControl getSelectedChapterDataControl( ) {
 
         return chaptersController.getSelectedChapterDataControl( );
     }
 
     /**
      * Returns the identifier summary.
      * 
      * @return The identifier summary
      */
     public IdentifierSummary getIdentifierSummary( ) {
 
         return chaptersController.getIdentifierSummary( );
     }
 
     /**
      * Returns the varFlag summary.
      * 
      * @return The varFlag summary
      */
     public VarFlagSummary getVarFlagSummary( ) {
 
         return chaptersController.getVarFlagSummary( );
     }
 
     public ChapterListDataControl getCharapterList( ) {
 
         return chaptersController;
     }
 
     /**
      * Returns whether the data has been modified since the last save.
      * 
      * @return True if the data has been modified, false otherwise
      */
     public boolean isDataModified( ) {
 
         return dataModified;
     }
 
     /**
      * Called when the data has been modified, it sets the value to true.
      */
     public void dataModified( ) {
 
         // If the data were not modified, change the value and set the new title of the window
         if( !dataModified ) {
             dataModified = true;
             mainWindow.updateTitle( );
         }
     }
 
     public boolean isPlayTransparent( ) {
 
         if( adventureDataControl == null ) {
             return false;
         }
         return adventureDataControl.getPlayerMode( ) == DescriptorData.MODE_PLAYER_1STPERSON;
 
     }
 
     public void swapPlayerMode( boolean showConfirmation ) {
 
         addTool( new SwapPlayerModeTool( showConfirmation, adventureDataControl, chaptersController ) );
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // Functions that perform usual application actions
 
     /**
      * This method creates a new file with it's respective data.
      * 
      * @return True if the new data was created successfully, false otherwise
      */
     public boolean newFile( int fileType ) {
 
         boolean fileCreated = false;
 
         if( fileType == Controller.FILE_ADVENTURE_1STPERSON_PLAYER || fileType == Controller.FILE_ADVENTURE_3RDPERSON_PLAYER )
             fileCreated = newAdventureFile( fileType );
         else if( fileType == Controller.FILE_ASSESSMENT ) {
             //fileCreated = newAssessmentFile();
         }
         else if( fileType == Controller.FILE_ADAPTATION ) {
             //fileCreated = newAdaptationFile();
         }
 
         if( fileCreated )
             AssetsController.resetCache( );
 
         return fileCreated;
     }
 
     public boolean newFile( ) {
 
         boolean createNewFile = true;
 
         if( dataModified ) {
             int option = mainWindow.showConfirmDialog( TC.get( "Operation.NewFileTitle" ), TC.get( "Operation.NewFileMessage" ) );
 
             // If the data must be saved, create the new file only if the save was successful
             if( option == JOptionPane.YES_OPTION )
                 createNewFile = saveFile( false );
 
             // If the data must not be saved, create the new data directly
             else if( option == JOptionPane.NO_OPTION ) {
                 createNewFile = true;
                 dataModified = false;
                 mainWindow.updateTitle( );
             }
 
             // Cancel the action if selected
             else if( option == JOptionPane.CANCEL_OPTION ) {
                 createNewFile = false;
             }
 
         }
 
         if( createNewFile ) {
             stopAutoSave( );
             ConfigData.storeToXML( );
             ProjectConfigData.storeToXML( );
             ConfigData.loadFromXML( ReleaseFolders.configFileEditorRelativePath( ) );
             ProjectConfigData.init( );
 
             // Show dialog
             StartDialog start = new StartDialog( StartDialog.NEW_TAB );
 
             //mainWindow.setEnabled( false );
             mainWindow.setVisible( false );
 
             int op = start.showOpenDialog( null );
             //start.end();
             if( op == StartDialog.NEW_FILE_OPTION ) {
                 newFile( start.getFileType( ) );
             }
             else if( op == StartDialog.OPEN_FILE_OPTION ) {
                 java.io.File selectedFile = start.getSelectedFile( );
                 if( selectedFile.getAbsolutePath( ).toLowerCase( ).endsWith( ".eap" ) ) {
                     String absolutePath = selectedFile.getPath( );
                     loadFile( absolutePath.substring( 0, absolutePath.length( ) - 4 ), true );
                 }
                 else if( selectedFile.isDirectory( ) && selectedFile.exists( ) )
                     loadFile( start.getSelectedFile( ).getAbsolutePath( ), true );
                 else {
                     this.importGame( selectedFile.getAbsolutePath( ) );
                 }
             }
             else if( op == StartDialog.RECENT_FILE_OPTION ) {
                 loadFile( start.getRecentFile( ).getAbsolutePath( ), true );
             }
             else if( op == StartDialog.CANCEL_OPTION ) {
                 exit( );
             }
 
             if( currentZipFile == null ) {
                 mainWindow.reloadData( );
             }
 
             mainWindow.setResizable( true );
             mainWindow.setEnabled( true );
             mainWindow.setVisible( true );
             //DEBUGGING
             //tsd = new ToolSystemDebugger( chaptersController );
         }
 
         return createNewFile;
     }
 
     private boolean newAdventureFile( int fileType ) {
 
         boolean fileCreated = false;
 
         // Decide the directory of the temp file and give a name for it
 
         // If there is a valid temp directory in the system, use it
         //String tempDir = System.getenv( "TEMP" );
         //String completeFilePath = "";
 
         //isTempFile = true;
         //if( tempDir != null ) {
         //	completeFilePath = tempDir + "/" + TEMP_NAME;
         //}
 
         // If the temp directory is not valid, use the home directory
         //else {
 
         //	completeFilePath = FileSystemView.getFileSystemView( ).getHomeDirectory( ) + "/" + TEMP_NAME;
         //}
 
         boolean create = false;
         java.io.File selectedDir = null;
         java.io.File selectedFile = null;
         // Prompt main folder of the project
         ProjectFolderChooser folderSelector = new ProjectFolderChooser( false, false );
         // If some folder is selected, check all characters are correct  
         if( folderSelector.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ) {
             java.io.File selectedFolder = folderSelector.getSelectedFile( );
             selectedFile = selectedFolder;
             if( selectedFile.getAbsolutePath( ).endsWith( ".eap" ) ) {
                 String absolutePath = selectedFolder.getAbsolutePath( );
                 selectedFolder = new File( absolutePath.substring( 0, absolutePath.length( ) - 4 ) );
             }
             else {
                 selectedFile = new File( selectedFile.getAbsolutePath( ) + ".eap" );
             }
             selectedDir = selectedFolder;
 
             // Check the parent folder is not forbidden
             if( isValidTargetProject( selectedFolder ) ) {
 
                 if( FolderFileFilter.checkCharacters( selectedFolder.getName( ) ) ) {
                     // Folder can be created/used
                     // Does the folder exist?
                     if( selectedFolder.exists( ) ) {
                         //Is the folder empty?
                         if( selectedFolder.list( ).length > 0 ) {
                             // Delete content?
                             if( this.showStrictConfirmDialog( TC.get( "Operation.NewProject.FolderNotEmptyTitle" ), TC.get( "Operation.NewProject.FolderNotEmptyMessage" ) ) ) {
                                 File directory = new File( selectedFolder.getAbsolutePath( ) );
                                 if( !directory.deleteAll( ) ) {
                                     this.showStrictConfirmDialog( TC.get( "Error.Title" ), TC.get( "Error.DeletingFolderContents" ) );
                                 }
                             }
                         }
                         create = true;
                     }
                     else {
                         // Create new folder?
                         if( this.showStrictConfirmDialog( TC.get( "Operation.NewProject.FolderNotCreatedTitle" ), TC.get( "Operation.NewProject.FolderNotCreatedMessage" ) ) ) {
                             File directory = new File( selectedFolder.getAbsolutePath( ) );
                             if( directory.mkdirs( ) ) {
                                 create = true;
                             }
                             else {
                                 this.showStrictConfirmDialog( TC.get( "Error.Title" ), TC.get( "Error.CreatingFolder" ) );
                             }
 
                         }
                         else {
                             create = false;
                         }
                     }
                 }
                 else {
                     // Display error message
                     this.showErrorDialog( TC.get( "Error.Title" ), TC.get( "Error.ProjectFolderName", FolderFileFilter.getAllowedChars( ) ) );
                 }
             }
             else {
                 // Show error: The target dir cannot be contained 
                 mainWindow.showErrorDialog( TC.get( "Operation.NewProject.ForbiddenParent.Title" ), TC.get( "Operation.NewProject.ForbiddenParent.Message" ) );
                 create = false;
             }
         }
 
         // Create the new project?
         //LoadingScreen loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.CreateProject" ), getLoadingImage( ), mainWindow);
 
         if( create ) {
             //loadingScreen.setVisible( true );
             loadingScreen.setMessage( TC.get( "Operation.CreateProject" ) );
             loadingScreen.setVisible( true );
 
             // Set the new file, path and create the new adventure
             currentZipFile = selectedDir.getAbsolutePath( );
             currentZipPath = selectedDir.getParent( );
             currentZipName = selectedDir.getName( );
             int playerMode = -1;
             if( fileType == FILE_ADVENTURE_3RDPERSON_PLAYER )
                 playerMode = DescriptorData.MODE_PLAYER_3RDPERSON;
             else if( fileType == FILE_ADVENTURE_1STPERSON_PLAYER )
                 playerMode = DescriptorData.MODE_PLAYER_1STPERSON;
             adventureDataControl = new AdventureDataControl( TC.get( "DefaultValue.AdventureTitle" ), TC.get( "DefaultValue.ChapterTitle" ), TC.get( "DefaultValue.SceneId" ), playerMode );
 
             // Clear the list of data controllers and refill it
             chaptersController = new ChapterListDataControl( adventureDataControl.getChapters( ) );
 
             // Init project properties (empty)
             ProjectConfigData.init( );
             SCORMConfigData.init( );
 
             AssetsController.createFolderStructure( );
             AssetsController.addSpecialAssets( );
 
             // Check the consistency of the chapters
             boolean valid = chaptersController.isValid( null, null );
 
             // Save the data
             if( Writer.writeData( currentZipFile, adventureDataControl, valid ) ) {
                 // Set modified to false and update the window title
                 dataModified = false;
                 try {
                     Thread.sleep( 1 );
                 }
                 catch( InterruptedException e ) {
                     ReportDialog.GenerateErrorReport( e, true, "UNKNOWNERROR" );
                 }
 
                 try {
                     if( selectedFile != null && !selectedFile.exists( ) )
                         selectedFile.createNewFile( );
                 }
                 catch( IOException e ) {
                 }
 
                 mainWindow.reloadData( );
 
                 // The file was saved
                 fileCreated = true;
 
             }
             else
                 fileCreated = false;
         }
         loadingScreen.setVisible( false );
         if( fileCreated ) {
             ConfigData.fileLoaded( currentZipFile );
             // Feedback
             mainWindow.showInformationDialog( TC.get( "Operation.FileLoadedTitle" ), TC.get( "Operation.FileLoadedMessage" ) );
         }
         else {
             // Feedback
             mainWindow.showInformationDialog( TC.get( "Operation.FileNotLoadedTitle" ), TC.get( "Operation.FileNotLoadedMessage" ) );
         }
 
         return fileCreated;
 
     }
 
     public void showLoadingScreen( String message ) {
 
         loadingScreen.setMessage( message );
         loadingScreen.setVisible( true );
     }
 
     public void hideLoadingScreen( ) {
 
         loadingScreen.setVisible( false );
     }
 
     public boolean fixIncidences( List<Incidence> incidences ) {
 
         boolean abort = false;
         List<Chapter> chapters = this.adventureDataControl.getChapters( );
 
         for( int i = 0; i < incidences.size( ); i++ ) {
             Incidence current = incidences.get( i );
             // Critical importance: abort operation, the game could not be loaded
             if( current.getImportance( ) == Incidence.IMPORTANCE_CRITICAL ) {
                 if( current.getException( ) != null )
                     ReportDialog.GenerateErrorReport( current.getException( ), true, TC.get( "GeneralText.LoadError" ) );
                 abort = true;
                 break;
             }
             // High importance: the game is partially unreadable, but it is possible to continue.
             else if( current.getImportance( ) == Incidence.IMPORTANCE_HIGH ) {
                 // An error occurred relating to the load of a chapter which is unreadable.
                 // When this happens the chapter returned in the adventure data structure is corrupted.
                 // Options: 1) Delete chapter. 2) Select chapter from other file. 3) Abort
                 if( current.getAffectedArea( ) == Incidence.CHAPTER_INCIDENCE && current.getType( ) == Incidence.XML_INCIDENCE ) {
                     String dialogTitle = TC.get( "ErrorSolving.Chapter.Title" ) + " - Error " + ( i + 1 ) + "/" + incidences.size( );
                     String dialogMessage = TC.get( "ErrorSolving.Chapter.Message", new String[] { current.getMessage( ), current.getAffectedResource( ) } );
                     String[] options = { TC.get( "GeneralText.Delete" ), TC.get( "GeneralText.Replace" ), TC.get( "GeneralText.Abort" ), TC.get( "GeneralText.ReportError" ) };
 
                     int option = showOptionDialog( dialogTitle, dialogMessage, options );
                     // Delete chapter
                     if( option == 0 ) {
                         String chapterName = current.getAffectedResource( );
                         for( int j = 0; j < chapters.size( ); j++ ) {
                             if( chapters.get( j ).getChapterPath( ).equals( chapterName ) ) {
                                 chapters.remove( j );
                                 //this.chapterDataControlList.remove( j );
                                 // Update selected chapter if necessary
                                 if( chapters.size( ) == 0 ) {
                                     // When there are no more chapters, add a new, blank one
                                     Chapter newChapter = new Chapter( TC.get( "DefaultValue.ChapterTitle" ), TC.get( "DefaultValue.SceneId" ) );
                                     chapters.add( newChapter );
                                     //chapterDataControlList.add( new ChapterDataControl (newChapter) );
                                 }
                                 chaptersController = new ChapterListDataControl( chapters );
                                 dataModified( );
                                 break;
                             }
                         }
                     }
 
                     // Replace chapter
                     else if( option == 1 ) {
                         boolean replaced = false;
                         JFileChooser xmlChooser = new JFileChooser( );
                         xmlChooser.setDialogTitle( TC.get( "GeneralText.Select" ) );
                         xmlChooser.setFileFilter( new XMLFileFilter( ) );
                         xmlChooser.setMultiSelectionEnabled( false );
                         // A file is selected
                         if( xmlChooser.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ) {
                             // Get absolute path
                             String absolutePath = xmlChooser.getSelectedFile( ).getAbsolutePath( );
                             // Try to load chapter with it
                             List<Incidence> newChapterIncidences = new ArrayList<Incidence>( );
                             Chapter chapter = Loader.loadChapterData( AssetsController.getInputStreamCreator( ), absolutePath, incidences, true );
                             // IF no incidences occurred
                             if( chapter != null && newChapterIncidences.size( ) == 0 ) {
                                 // Try comparing names
 
                                 int found = -1;
                                 for( int j = 0; found == -1 && j < chapters.size( ); j++ ) {
                                     if( chapters.get( j ).getChapterPath( ).equals( current.getAffectedResource( ) ) ) {
                                         found = j;
                                     }
                                 }
                                 // Replace it if found
                                 if( found >= 0 ) {
                                     //this.chapterDataControlList.remove( found );
                                     chapters.set( found, chapter );
                                     chaptersController = new ChapterListDataControl( chapters );
                                     //chapterDataControlList.add( found, new ChapterDataControl(chapter) );
 
                                     // Copy original file to project
                                     File destinyFile = new File( this.getProjectFolder( ), chapter.getChapterPath( ) );
                                     if( destinyFile.exists( ) )
                                         destinyFile.delete( );
                                     File sourceFile = new File( absolutePath );
                                     sourceFile.copyTo( destinyFile );
                                     replaced = true;
                                     dataModified( );
                                 }
 
                             }
                         }
                         // The chapter was not replaced: inform
                         if( !replaced ) {
                             mainWindow.showWarningDialog( TC.get( "ErrorSolving.Chapter.NotReplaced.Title" ), TC.get( "ErrorSolving.Chapter.NotReplaced.Message" ) );
                         }
                     }
                     // Report Dialog
                     else if( option == 3 ) {
                         if( current.getException( ) != null )
                             ReportDialog.GenerateErrorReport( current.getException( ), true, TC.get( "GeneralText.LoadError" ) );
                         abort = true;
 
                     }
                     // Other case: abort
                     else {
                         abort = true;
                         break;
                     }
                 }
             }
             // Medium importance: the game might be slightly affected
             else if( current.getImportance( ) == Incidence.IMPORTANCE_MEDIUM ) {
                 // If an asset is missing or damaged. Delete references
                 if( current.getType( ) == Incidence.ASSET_INCIDENCE ) {
                     this.deleteAssetReferences( current.getAffectedResource( ) );
                     mainWindow.showInformationDialog( TC.get( "ErrorSolving.Asset.Deleted.Title" ) + " - Error " + ( i + 1 ) + "/" + incidences.size( ), TC.get( "ErrorSolving.Asset.Deleted.Message", current.getAffectedResource( ) ) );
                     if( current.getException( ) != null )
                         ReportDialog.GenerateErrorReport( current.getException( ), true, TC.get( "GeneralText.LoadError" ) );
 
                 }
                 // If it was an assessment profile (referenced) delete the assessment configuration of the chapter
                 else if( current.getAffectedArea( ) == Incidence.ASSESSMENT_INCIDENCE ) {
                     mainWindow.showInformationDialog( TC.get( "ErrorSolving.AssessmentReferenced.Deleted.Title" ) + " - Error " + ( i + 1 ) + "/" + incidences.size( ), TC.get( "ErrorSolving.AssessmentReferenced.Deleted.Message", current.getAffectedResource( ) ) );
                     for( int j = 0; j < chapters.size( ); j++ ) {
                         if( chapters.get( j ).getAssessmentName( ).equals( current.getAffectedResource( ) ) ) {
                             chapters.get( j ).setAssessmentName( "" );
                             dataModified( );
                         }
                     }
                     if( current.getException( ) != null )
                         ReportDialog.GenerateErrorReport( current.getException( ), true, TC.get( "GeneralText.LoadError" ) );
                     //	adventureData.getAssessmentRulesListDataControl( ).deleteIdentifierReferences( current.getAffectedResource( ) );
                 }
                 // If it was an assessment profile (referenced) delete the assessment configuration of the chapter
                 else if( current.getAffectedArea( ) == Incidence.ADAPTATION_INCIDENCE ) {
                     mainWindow.showInformationDialog( TC.get( "ErrorSolving.AdaptationReferenced.Deleted.Title" ) + " - Error " + ( i + 1 ) + "/" + incidences.size( ), TC.get( "ErrorSolving.AdaptationReferenced.Deleted.Message", current.getAffectedResource( ) ) );
                     for( int j = 0; j < chapters.size( ); j++ ) {
                         if( chapters.get( j ).getAdaptationName( ).equals( current.getAffectedResource( ) ) ) {
                             chapters.get( j ).setAdaptationName( "" );
                             dataModified( );
                         }
                     }
                     if( current.getException( ) != null )
                         ReportDialog.GenerateErrorReport( current.getException( ), true, TC.get( "GeneralText.LoadError" ) );
                     //adventureData.getAdaptationRulesListDataControl( ).deleteIdentifierReferences( current.getAffectedResource( ) );
                 }
 
                 // Abort
                 else {
                     abort = true;
                     break;
                 }
             }
             // Low importance: the game will not be affected
             else if( current.getImportance( ) == Incidence.IMPORTANCE_LOW ) {
                 if( current.getAffectedArea( ) == Incidence.ADAPTATION_INCIDENCE ) {
                     //adventureData.getAdaptationRulesListDataControl( ).deleteIdentifierReferences( current.getAffectedResource( ) );
                     dataModified( );
                 }
                 if( current.getAffectedArea( ) == Incidence.ASSESSMENT_INCIDENCE ) {
                     //adventureData.getAssessmentRulesListDataControl( ).deleteIdentifierReferences( current.getAffectedResource( ) );
                     dataModified( );
                 }
             }
 
         }
         return abort;
     }
 
     /**
      * Called when the user wants to load data from a file.
      * 
      * @return True if a file was loaded successfully, false otherwise
      */
     public boolean loadFile( ) {
 
         return loadFile( null, true );
     }
 
     public boolean replaceSelectedChapter( Chapter newChapter ) {
 
         chaptersController.replaceSelectedChapter( newChapter );
         //mainWindow.updateTree();
         mainWindow.reloadData( );
         return true;
     }
 
     private boolean loadFile( String completeFilePath, boolean loadingImage ) {
 
         boolean fileLoaded = false;
         boolean hasIncedence = false;
         try {
             boolean loadFile = true;
             // If the data was not saved, ask for an action (save, discard changes...)
             if( dataModified ) {
                 int option = mainWindow.showConfirmDialog( TC.get( "Operation.LoadFileTitle" ), TC.get( "Operation.LoadFileMessage" ) );
 
                 // If the data must be saved, load the new file only if the save was succesful
                 if( option == JOptionPane.YES_OPTION )
                     loadFile = saveFile( false );
 
                 // If the data must not be saved, load the new data directly
                 else if( option == JOptionPane.NO_OPTION ) {
                     loadFile = true;
                     dataModified = false;
                     mainWindow.updateTitle( );
                 }
 
                 // Cancel the action if selected
                 else if( option == JOptionPane.CANCEL_OPTION ) {
                     loadFile = false;
                 }
 
             }
 
             if( loadFile && completeFilePath == null ) {
                 this.stopAutoSave( );
                 ConfigData.loadFromXML( ReleaseFolders.configFileEditorRelativePath( ) );
                ProjectConfigData.loadFromXML( );
 
                 // Show dialog
                 StartDialog start = new StartDialog( StartDialog.OPEN_TAB );
 
                 //mainWindow.setEnabled( false );
                 mainWindow.setVisible( false );
 
                 int op = start.showOpenDialog( null );
                 //start.end();
                 if( op == StartDialog.NEW_FILE_OPTION ) {
                     newFile( start.getFileType( ) );
                 }
                 else if( op == StartDialog.OPEN_FILE_OPTION ) {
                     java.io.File selectedFile = start.getSelectedFile( );
                     String absPath = selectedFile.getAbsolutePath( ).toLowerCase( );
                     if( absPath.endsWith( ".eap" ) ) {
                         String absolutePath = selectedFile.getPath( );
                         loadFile( absolutePath.substring( 0, absolutePath.length( ) - 4 ), true );
                     }
                     else if( selectedFile.isDirectory( ) && selectedFile.exists( ) )
                         loadFile( start.getSelectedFile( ).getAbsolutePath( ), true );
                     else 
                         // importGame is the same method for .ead, .jar and .zip (LO) import
                         this.importGame( selectedFile.getAbsolutePath( ) );
                   
                     
                 }
                 else if( op == StartDialog.RECENT_FILE_OPTION ) {
                     loadFile( start.getRecentFile( ).getAbsolutePath( ), true );
                 }
                 else if( op == StartDialog.CANCEL_OPTION ) {
                     exit( );
                 }
 
                 if( currentZipFile == null ) {
                     mainWindow.reloadData( );
                 }
 
                 mainWindow.setResizable( true );
                 mainWindow.setEnabled( true );
                 mainWindow.setVisible( true );
                 //DEBUGGING
                 //tsd = new ToolSystemDebugger( chaptersController );
 
                 return true;
             }
 
             //LoadingScreen loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.LoadProject" ), getLoadingImage( ), mainWindow);
             // If some file was selected
             if( completeFilePath != null ) {
                 if( loadingImage ) {
                     loadingScreen.setMessage( TC.get( "Operation.LoadProject" ) );
                     this.loadingScreen.setVisible( true );
                     loadingImage = true;
                 }
                 // Create a file to extract the name and path
                 File newFile = new File( completeFilePath );
 
                 // Load the data from the file, and update the info
                 List<Incidence> incidences = new ArrayList<Incidence>( );
                 //ls.start( );
                 /*AdventureData loadedAdventureData = Loader.loadAdventureData( AssetsController.getInputStreamCreator(completeFilePath), 
                 		AssetsController.getCategoryFolder(AssetsController.CATEGORY_ASSESSMENT),
                 		AssetsController.getCategoryFolder(AssetsController.CATEGORY_ADAPTATION),incidences );
                  */
                 AdventureData loadedAdventureData = Loader.loadAdventureData( AssetsController.getInputStreamCreator( completeFilePath ), incidences, true );
 
                 //mainWindow.setNormalState( );
 
                 // If the adventure was loaded without problems, update the data
                 if( loadedAdventureData != null ) {
                     // Update the values of the controller
                     currentZipFile = newFile.getAbsolutePath( );
                     currentZipPath = newFile.getParent( );
                     currentZipName = newFile.getName( );
                     loadedAdventureData.setProjectName( currentZipName );
                     adventureDataControl = new AdventureDataControl( loadedAdventureData );
 
                     chaptersController = new ChapterListDataControl( adventureDataControl.getChapters( ) );
 
                     // Check asset files
                     AssetsController.checkAssetFilesConsistency( incidences );
                     Incidence.sortIncidences( incidences );
                     // If there is any incidence
                     if( incidences.size( ) > 0 ) {
                         boolean abort = fixIncidences( incidences );
                         if( abort ) {
                             mainWindow.showInformationDialog( TC.get( "Error.LoadAborted.Title" ), TC.get( "Error.LoadAborted.Message" ) );
                             hasIncedence = true;
                         }
                     }
 
                     ProjectConfigData.loadFromXML( );
                     AssetsController.createFolderStructure( );
                     AssetsController.addSpecialAssets( );
 
                     dataModified = false;
 
                     // The file was loaded
                     fileLoaded = true;
 
                     // Reloads the view of the window
                     mainWindow.reloadData( );
                 }
             }
 
             //if the file was loaded, update the RecentFiles list:
             if( fileLoaded ) {
                 ConfigData.fileLoaded( currentZipFile );
                 AssetsController.resetCache( );
                 // Load project config file
                 ProjectConfigData.loadFromXML( );
 
                 startAutoSave( 15 );
 
                 // Feedback
                 //loadingScreen.close( );
                 if( !hasIncedence )
                     mainWindow.showInformationDialog( TC.get( "Operation.FileLoadedTitle" ), TC.get( "Operation.FileLoadedMessage" ) );
                 else
                     mainWindow.showInformationDialog( TC.get( "Operation.FileLoadedWithErrorTitle" ), TC.get( "Operation.FileLoadedWithErrorMessage" ) );
 
             }
             else {
                 // Feedback
                 //loadingScreen.close( );
                 mainWindow.showInformationDialog( TC.get( "Operation.FileNotLoadedTitle" ), TC.get( "Operation.FileNotLoadedMessage" ) );
             }
 
             if( loadingImage )
                 //ls.close( );
                 loadingScreen.setVisible( false );
         }
         catch( Exception e ) {
             e.printStackTrace( );
             fileLoaded = false;
             if( loadingImage )
                 loadingScreen.setVisible( false );
             mainWindow.showInformationDialog( TC.get( "Operation.FileNotLoadedTitle" ), TC.get( "Operation.FileNotLoadedMessage" ) );
         }
         return fileLoaded;
     }
     
     public boolean importJAR(){
      
         return false;
     }
     
     public boolean importLO(){
         return false;
     }
     
     
     /**
      * Called when the user wants to save data to a file.
      * 
      * @param saveAs
      *            True if the destiny file must be chosen inconditionally
      * @return True if a file was saved successfully, false otherwise
      */
     public boolean saveFile( boolean saveAs ) {
 
         boolean fileSaved = false;
         try {
             boolean saveFile = true;
 
             // Select a new file if it is a "Save as" action
             if( saveAs ) {
                 //loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.SaveProjectAs" ), getLoadingImage( ), mainWindow);
                 //loadingScreen.setVisible( true );
                 String completeFilePath = null;
                 completeFilePath = mainWindow.showSaveDialog( getCurrentLoadFolder( ), new FolderFileFilter( false, false, null ) );
 
                 // If some file was selected set the new file
                 if( completeFilePath != null ) {
                     // Create a file to extract the name and path
                     File newFolder;
                     File newFile;
                     if( completeFilePath.endsWith( ".eap" ) ) {
                         newFile = new File( completeFilePath );
                         newFolder = new File( completeFilePath.substring( 0, completeFilePath.length( ) - 4 ) );
                     }
                     else {
                         newFile = new File( completeFilePath + ".eap" );
                         newFolder = new File( completeFilePath );
                     }
                     // Check the selectedFolder is not inside a forbidden one
                     if( isValidTargetProject( newFile ) ) {
                         if( FolderFileFilter.checkCharacters( newFolder.getName( ) ) ) {
 
                             // If the file doesn't exist, or if the user confirms the writing in the file and the file it is not the current path of the project
                             if( ( this.currentZipFile == null || !newFolder.getAbsolutePath( ).toLowerCase( ).equals( this.currentZipFile.toLowerCase( ) ) ) && ( ( !newFile.exists( ) && !newFolder.exists( ) ) || !newFolder.exists( ) || newFolder.list( ).length == 0 || mainWindow.showStrictConfirmDialog( TC.get( "Operation.SaveFileTitle" ), TC.get( "Operation.NewProject.FolderNotEmptyMessage", newFolder.getName( ) ) ) ) ) {
                                 // If the file exists, delete it so it's clean in the first save
                                 //if( newFile.exists( ) )
                                 //	newFile.delete( );
 
                                 if( !newFile.exists( ) )
                                     newFile.create( );
 
                                 // If this is a "Save as" operation, copy the assets from the old file to the new one
                                 if( saveAs ) {
                                     loadingScreen.setMessage( TC.get( "Operation.SaveProjectAs" ) );
                                     loadingScreen.setVisible( true );
 
                                     AssetsController.copyAssets( currentZipFile, newFolder.getAbsolutePath( ) );
                                 }
 
                                 // Set the new file and path
                                 currentZipFile = newFolder.getAbsolutePath( );
                                 currentZipPath = newFolder.getParent( );
                                 currentZipName = newFolder.getName( );
                             }
 
                             // If the file was not overwritten, don't save the data
                             else
                                 saveFile = false;
 
                             // In case the selected folder is the same that the previous one, report an error
                             if( !saveFile && this.currentZipFile != null && newFolder.getAbsolutePath( ).toLowerCase( ).equals( this.currentZipFile.toLowerCase( ) ) ) {
                                 this.showErrorDialog( TC.get( "Operation.SaveProjectAs.TargetFolderInUse.Title" ), TC.get( "Operation.SaveProjectAs.TargetFolderInUse.Message" ) );
                             }
                         }
                         else {
                             this.showErrorDialog( TC.get( "Error.Title" ), TC.get( "Error.ProjectFolderName", FolderFileFilter.getAllowedChars( ) ) );
                             saveFile = false;
                         }
                     }
                     else {
                         // Show error: The target dir cannot be contained 
                         mainWindow.showErrorDialog( TC.get( "Operation.NewProject.ForbiddenParent.Title" ), TC.get( "Operation.NewProject.ForbiddenParent.Message" ) );
                         saveFile = false;
                     }
                 }
 
                 // If no file was selected, don't save the data
                 else
                     saveFile = false;
             }
             else {
                 //loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.SaveProject" ), getLoadingImage( ), mainWindow);
 
                 //loadingScreen.setVisible( true );
                 loadingScreen.setMessage( TC.get( "Operation.SaveProject" ) );
                 loadingScreen.setVisible( true );
             }
 
             // If the data must be saved
             if( saveFile ) {
                 ConfigData.storeToXML( );
                 ProjectConfigData.storeToXML( );
                 // If the zip was temp file, delete it
                 //if( isTempFile( ) ) {
                 //	File file = new File( oldZipFile );
                 //	file.deleteOnExit( );
                 //	isTempFile = false;
                 //}
 
                 // Check the consistency of the chapters
                 boolean valid = chaptersController.isValid( null, null );
 
                 // If the data is not valid, show an error message
                 if( !valid )
                     mainWindow.showWarningDialog( TC.get( "Operation.AdventureConsistencyTitle" ), TC.get( "Operation.AdventurInconsistentWarning" ) );
 
                 // Control the version number
                 String newValue = increaseVersionNumber( adventureDataControl.getAdventureData( ).getVersionNumber( ) );
                 adventureDataControl.getAdventureData( ).setVersionNumber( newValue );
 
                 // Save the data
                 if( Writer.writeData( currentZipFile, adventureDataControl, valid ) ) {
                     File eapFile = new File( currentZipFile + ".eap" );
                     if( !eapFile.exists( ) )
                         eapFile.create( );
 
                     // Set modified to false and update the window title
                     dataModified = false;
                     mainWindow.updateTitle( );
 
                     // The file was saved
                     fileSaved = true;
                 }
             }
 
             //If the file was saved, update the recent files list:
             if( fileSaved ) {
                 ConfigData.fileLoaded( currentZipFile );
                 ProjectConfigData.storeToXML( );
                 AssetsController.resetCache( );
                 // also, look for adaptation and assessment folder, and delete them
                 File currentAssessFolder = new File( currentZipFile + File.separator + "assessment" );
                 if( currentAssessFolder.exists( ) ) {
                     File[] files = currentAssessFolder.listFiles( );
                     for( int x = 0; x < files.length; x++ )
                         files[x].delete( );
                     currentAssessFolder.delete( );
                 }
                 File currentAdaptFolder = new File( currentZipFile + File.separator + "adaptation" );
                 if( currentAdaptFolder.exists( ) ) {
                     File[] files = currentAdaptFolder.listFiles( );
                     for( int x = 0; x < files.length; x++ )
                         files[x].delete( );
                     currentAdaptFolder.delete( );
                 }
             }
         }
         catch( Exception e ) {
             fileSaved = false;
             mainWindow.showInformationDialog( TC.get( "Operation.FileNotSavedTitle" ), TC.get( "Operation.FileNotSavedMessage" ) );
 
         }
         //loadingScreen.close( );
 
         loadingScreen.setVisible( false );
 
         return fileSaved;
     }
 
     /**
      * Increase the game version number
      * 
      * @param digits
      * @param index
      * @return the version number after increase it
      */
     private String increaseVersionNumber( char[] digits, int index ) {
 
         if( digits[index] != '9' ) {
             // increase in "1" the ASCII code 
             digits[index]++;
             return new String( digits );
         }
         else if( index == 0 ) {
             char[] aux = new char[ digits.length + 1 ];
             aux[0] = '1';
             aux[1] = '0';
             for( int i = 2; i < aux.length; i++ )
                 aux[i] = digits[i - 1];
             return new String( aux );
 
         }
         else {
             digits[index] = '0';
             return increaseVersionNumber( digits, --index );
         }
 
     }
 
     private String increaseVersionNumber( String versionNumber ) {
 
         char[] digits = versionNumber.toCharArray( );
         return increaseVersionNumber( digits, digits.length - 1 );
     }
 
     public void importChapter( ) {
 
     }
 
     public void importGame( ) {
 
         importGame( null );
     }
 
     public void importGame( String eadPath ) {
 
         boolean importGame = true;
         java.io.File selectedFile = null;
         try {
             if( dataModified ) {
                 int option = mainWindow.showConfirmDialog( TC.get( "Operation.SaveChangesTitle" ), TC.get( "Operation.SaveChangesMessage" ) );
                 // If the data must be saved, load the new file only if the save was succesful
                 if( option == JOptionPane.YES_OPTION )
                     importGame = saveFile( false );
 
                 // If the data must not be saved, load the new data directly
                 else if( option == JOptionPane.NO_OPTION ) {
                     importGame = true;
                     dataModified = false;
                     mainWindow.updateTitle( );
                 }
 
                 // Cancel the action if selected
                 else if( option == JOptionPane.CANCEL_OPTION ) {
                     importGame = false;
                 }
 
             }
 
             if( importGame ) {
                 if (eadPath.endsWith( ".zip" ))
                     mainWindow.showInformationDialog( TC.get( "Operation.ImportProject" ), TC.get( "Operation.ImportLO.InfoMessage" ) );
                 else if (eadPath.endsWith( ".jar" ))
                     mainWindow.showInformationDialog( TC.get( "Operation.ImportProject" ), TC.get( "Operation.ImportJAR.InfoMessage" ));
                 
                 // Ask origin file
                 JFileChooser chooser = new JFileChooser( );
                 chooser.setFileFilter( new EADFileFilter( ) );
                 chooser.setMultiSelectionEnabled( false );
                 chooser.setCurrentDirectory( new File( getCurrentExportSaveFolder( ) ) );
                 int option = JFileChooser.APPROVE_OPTION;
                 if( eadPath == null )
                     option = chooser.showOpenDialog( mainWindow );
                 if( option == JFileChooser.APPROVE_OPTION ) {
                     java.io.File originFile = null;
                     if( eadPath == null )
                         originFile = chooser.getSelectedFile( );
                     else
                         originFile = new File( eadPath );
 
                     
                    // if( !originFile.getAbsolutePath( ).endsWith( ".ead" ) )
                      //   originFile = new java.io.File( originFile.getAbsolutePath( ) + ".ead" );
                     
                     // If the file not exists display error
                     if( !originFile.exists( ) )
                         mainWindow.showErrorDialog( TC.get( "Error.Import.FileNotFound.Title" ), TC.get( "Error.Import.FileNotFound.Title", originFile.getName( ) ) );
                     // Otherwise ask folder for the new project
                     else {
                         boolean create = false;
                         java.io.File selectedDir = null;
                         // Prompt main folder of the project
                         ProjectFolderChooser folderSelector = new ProjectFolderChooser( false, false );
                         // If some folder is selected, check all characters are correct  
                         if( folderSelector.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ) {
                             java.io.File selectedFolder = folderSelector.getSelectedFile( );
                             selectedFile = selectedFolder;
                             if( selectedFolder.getAbsolutePath( ).endsWith( ".eap" ) ) {
                                 String absolutePath = selectedFolder.getAbsolutePath( );
                                 selectedFolder = new java.io.File( absolutePath.substring( 0, absolutePath.length( ) - 4 ) );
                             }
                             else {
                                 selectedFile = new java.io.File( selectedFolder.getAbsolutePath( ) + ".eap" );
                             }
 
                             selectedDir = selectedFolder;
 
                             // Check the selectedFolder is not inside a forbidden one
                             if( isValidTargetProject( selectedFolder ) ) {
                                 if( FolderFileFilter.checkCharacters( selectedFolder.getName( ) ) ) {
                                     // Folder can be created/used
                                     // Does the folder exist?
                                     if( selectedFolder.exists( ) ) {
                                         //Is the folder empty?
                                         if( selectedFolder.list( ).length > 0 ) {
                                             // Delete content?
                                             if( this.showStrictConfirmDialog( TC.get( "Operation.NewProject.FolderNotEmptyTitle" ), TC.get( "Operation.NewProject.FolderNotEmptyMessage" ) ) ) {
                                                 File directory = new File( selectedFolder.getAbsolutePath( ) );
                                                 if( directory.deleteAll( ) ) {
                                                     create = true;
                                                 }
                                                 else {
                                                     this.showStrictConfirmDialog( TC.get( "Error.Title" ), TC.get( "Error.DeletingFolderContents" ) );
                                                 }
                                             }
                                         }
                                         else {
                                             create = true;
                                         }
                                     }
                                     else {
                                         // Create new folder?
                                         File directory = new File( selectedFolder.getAbsolutePath( ) );
                                         if( directory.mkdirs( ) ) {
                                             create = true;
                                         }
                                         else {
                                             this.showStrictConfirmDialog( TC.get( "Error.Title" ), TC.get( "Error.CreatingFolder" ) );
                                         }
                                     }
                                 }
                                 else {
                                     // Display error message
                                     this.showErrorDialog( TC.get( "Error.Title" ), TC.get( "Error.ProjectFolderName", FolderFileFilter.getAllowedChars( ) ) );
                                 }
                             }
                             else {
                                 // Show error: The target dir cannot be contained 
                                 mainWindow.showErrorDialog( TC.get( "Operation.NewProject.ForbiddenParent.Title" ), TC.get( "Operation.NewProject.ForbiddenParent.Message" ) );
                                 create = false;
                             }
                         }
 
                         // Create the new project?
                         if( create ) {
                             //LoadingScreen loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.ImportProject" ), getLoadingImage( ), mainWindow);
                             loadingScreen.setMessage( TC.get( "Operation.ImportProject" ) );
                             loadingScreen.setVisible( true );
                             //AssetsController.createFolderStructure();
                             if( !selectedDir.exists( ) )
                                 selectedDir.mkdirs( );
 
                             if( selectedFile != null && !selectedFile.exists( ) )
                                 selectedFile.createNewFile( );
 
                             boolean correctFile = true;
                             // Unzip directory
                             if (eadPath.endsWith( ".ead" ))
                                 File.unzipDir( originFile.getAbsolutePath( ), selectedDir.getAbsolutePath( ) );
                             else if (eadPath.endsWith( ".zip" )){
                                 
                                 // import EadJAR returns false when selected jar is not a eadventure jar
                                 if  (!File.importEadventureLO( originFile.getAbsolutePath( ), selectedDir.getAbsolutePath( )  )){
                                     loadingScreen.setVisible( false );
                                     mainWindow.showErrorDialog( TC.get("Operation.FileNotLoadedTitle"), TC.get( "Operation.ImportLO.FileNotLoadedMessage") );
                                     correctFile = false;
                                 }
                             }else if (eadPath.endsWith( ".jar" )){
                                 // import EadLO returns false when selected zip is not a eadventure LO
                                 
                                 if (!File.importEadventureJar( originFile.getAbsolutePath( ), selectedDir.getAbsolutePath( )  )){
                                     loadingScreen.setVisible( false );
                                     mainWindow.showErrorDialog( TC.get("Operation.FileNotLoadedTitle"), TC.get("Operation.ImportJAR.FileNotLoaded") );
                                     correctFile = false;
                                 }
 
                             }
                             //ProjectConfigData.loadFromXML( );
 
                             // Load new project
                             if (correctFile) {
                             loadFile( selectedDir.getAbsolutePath( ), false );
                             //loadingScreen.close( );
                             loadingScreen.setVisible( false );
                             } else {
                                 //remove .eapFile
                                 selectedFile.delete( );
                                 selectedDir.delete( );
                                 
                             }
                         }
 
                     }
                 }
             }
         }
         catch( Exception e ) {
             loadingScreen.setVisible( false );
             mainWindow.showErrorDialog( TC.get("Operation.FileNotLoadedTitle"), TC.get("Operation.FileNotLoadedMessage" ));
 
         }
     }
 
     public boolean exportGame( ) {
 
         return exportGame( null );
     }
 
     public boolean exportGame( String targetFilePath ) {
 
         boolean exportGame = true;
         boolean exported = false;
         try {
             if( dataModified ) {
 
                 int option = mainWindow.showConfirmDialog( TC.get( "Operation.SaveChangesTitle" ), TC.get( "Operation.SaveChangesMessage" ) );
                 // If the data must be saved, load the new file only if the save was succesful
                 if( option == JOptionPane.YES_OPTION )
                     exportGame = saveFile( false );
 
                 // If the data must not be saved, load the new data directly
                 else if( option == JOptionPane.NO_OPTION )
                     exportGame = true;
 
                 // Cancel the action if selected
                 else if( option == JOptionPane.CANCEL_OPTION )
                     exportGame = false;
             }
 
             if( exportGame ) {
                 // Ask destiny file
                 /*JFileChooser chooser = new JFileChooser();
                 chooser.setFileFilter( new EADFileFilter() );
                 chooser.setMultiSelectionEnabled( false );
                 int option = chooser.showSaveDialog( mainWindow );
                 if (option == JFileChooser.APPROVE_OPTION){
                 	java.io.File destinyFile = chooser.getSelectedFile( );
                 	if (!destinyFile.getAbsolutePath( ).toLowerCase( ).endsWith( ".ead" )){
                 		destinyFile = new java.io.File (destinyFile.getAbsolutePath( )+".ead");*/
 
                 String selectedPath = targetFilePath;
                 if( selectedPath == null )
                     selectedPath = mainWindow.showSaveDialog( getCurrentExportSaveFolder( ), new EADFileFilter( ) );
 
                 if( selectedPath != null ) {
                     if( !selectedPath.toLowerCase( ).endsWith( ".ead" ) )
                         selectedPath = selectedPath + ".ead";
 
                     java.io.File destinyFile = new File( selectedPath );
 
                     // Check the destinyFile is not in the project folder
                     if( targetFilePath != null || isValidTargetFile( destinyFile ) ) {
 
                         // If the file exists, ask to overwrite
                         if( !destinyFile.exists( ) || targetFilePath != null || mainWindow.showStrictConfirmDialog( TC.get( "Operation.SaveFileTitle" ), TC.get( "Operation.OverwriteExistingFile", destinyFile.getName( ) ) ) ) {
                             destinyFile.delete( );
 
                             // Finally, export it
                             //LoadingScreen loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.ExportProject.AsEAD" ), getLoadingImage( ), mainWindow);
                             if( targetFilePath == null ) {
                                 loadingScreen.setMessage( TC.get( "Operation.ExportProject.AsEAD" ) );
                                 loadingScreen.setVisible( true );
                             }
                             if( Writer.export( getProjectFolder( ), destinyFile.getAbsolutePath( ) ) ) {
                                 exported = true;
                                 if( targetFilePath == null )
                                     mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                             }
                             else {
                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.NotSuccess.Title" ), TC.get( "Operation.ExportT.NotSuccess.Message" ) );
                             }
                             //loadingScreen.close( );
                             if( targetFilePath == null )
                                 loadingScreen.setVisible( false );
                         }
                     }
                     else {
                         // Show error: The target dir cannot be contained 
                         mainWindow.showErrorDialog( TC.get( "Operation.ExportT.TargetInProjectDir.Title" ), TC.get( "Operation.ExportT.TargetInProjectDir.Message" ) );
                     }
                 }
             }
         }
         catch( Exception e ) {
             loadingScreen.setVisible( false );
             mainWindow.showErrorDialog( "Operation.FileNotSavedTitle", "Operation.FileNotSavedMessage" );
             exported = false;
         }
         return exported;
     }
 
     public boolean createBackup( String targetFilePath ) {
 
         boolean fileSaved = false;
         if( targetFilePath == null )
             targetFilePath = currentZipFile + ".tmp";
         File category = new File( currentZipFile, "backup" );
         try {
             boolean valid = chaptersController.isValid( null, null );
 
             category.create( );
 
             if( Writer.writeData( currentZipFile + File.separatorChar + "backup", adventureDataControl, valid ) ) {
                 fileSaved = true;
             }
 
             if( fileSaved ) {
                 String selectedPath = targetFilePath;
 
                 if( selectedPath != null ) {
 
                     java.io.File destinyFile = new File( selectedPath );
 
                     if( targetFilePath != null || isValidTargetFile( destinyFile ) ) {
                         if( !destinyFile.exists( ) || targetFilePath != null ) {
                             destinyFile.delete( );
                             if( Writer.export( getProjectFolder( ), destinyFile.getAbsolutePath( ) ) )
                                 fileSaved = true;
                         }
                     }
                     else
                         fileSaved = false;
                 }
             }
         }
         catch( Exception e ) {
             fileSaved = false;
         }
 
         if( category.exists( ) ) {
             category.deleteAll( );
         }
 
         return fileSaved;
     }
 
     public void exportStandaloneGame( ) {
 
         boolean exportGame = true;
         try {
             if( dataModified ) {
                 int option = mainWindow.showConfirmDialog( TC.get( "Operation.SaveChangesTitle" ), TC.get( "Operation.SaveChangesMessage" ) );
                 // If the data must be saved, load the new file only if the save was succesful
                 if( option == JOptionPane.YES_OPTION )
                     exportGame = saveFile( false );
 
                 // If the data must not be saved, load the new data directly
                 else if( option == JOptionPane.NO_OPTION )
                     exportGame = true;
 
                 // Cancel the action if selected
                 else if( option == JOptionPane.CANCEL_OPTION )
                     exportGame = false;
 
             }
 
             if( exportGame ) {
                 // Ask destiny file
                 //JFileChooser chooser = new JFileChooser();
                 //chooser.setFileFilter( new JARFileFilter() );
                 //chooser.setMultiSelectionEnabled( false );
                 String completeFilePath = null;
                 completeFilePath = mainWindow.showSaveDialog( getCurrentExportSaveFolder( ), new JARFileFilter());
 
                 //int option = chooser.showSaveDialog( mainWindow );
                 //if (option == JFileChooser.APPROVE_OPTION){
                 if( completeFilePath != null ) {
                     //java.io.File destinyFile = chooser.getSelectedFile( );
                     //if (!destinyFile.getAbsolutePath( ).toLowerCase( ).endsWith( ".jar" )){
                     //	destinyFile = new java.io.File (destinyFile.getAbsolutePath( )+".jar");
                     if( !completeFilePath.toLowerCase( ).endsWith( ".jar" ) )
                         completeFilePath = completeFilePath + ".jar";
                     // If the file exists, ask to overwrite
                     java.io.File destinyFile = new File( completeFilePath );
 
                     // Check the destinyFile is not in the project folder
                     if( isValidTargetFile( destinyFile ) ) {
 
                         if( !destinyFile.exists( ) || mainWindow.showStrictConfirmDialog( TC.get( "Operation.SaveFileTitle" ), TC.get( "Operation.OverwriteExistingFile", destinyFile.getName( ) ) ) ) {
                             destinyFile.delete( );
 
                             // Finally, export it
                             loadingScreen.setMessage( TC.get( "Operation.ExportProject.AsJAR" ) );
                             loadingScreen.setVisible( true );
                             if( Writer.exportStandalone( getProjectFolder( ), destinyFile.getAbsolutePath( ) ) ) {
                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                             }
                             else {
                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.NotSuccess.Title" ), TC.get( "Operation.ExportT.NotSuccess.Message" ) );
                             }
                             loadingScreen.setVisible( false );
 
                         }
                     }
                     else {
                         // Show error: The target dir cannot be contained 
                         mainWindow.showErrorDialog( TC.get( "Operation.ExportT.TargetInProjectDir.Title" ), TC.get( "Operation.ExportT.TargetInProjectDir.Message" ) );
                     }
                 }
             }
         }
         catch( Exception e ) {
             loadingScreen.setVisible( false );
             mainWindow.showErrorDialog( TC.get("Operation.FileNotSavedTitle"), TC.get("Operation.FileNotSavedMessage") );
         }
 
     }
 
     public void exportToLOM( ) {
 
         boolean exportFile = true;
         try {
             if( dataModified ) {
                 int option = mainWindow.showConfirmDialog( TC.get( "Operation.SaveChangesTitle" ), TC.get( "Operation.SaveChangesMessage" ) );
                 // If the data must be saved, load the new file only if the save was succesful
                 if( option == JOptionPane.YES_OPTION )
                     exportFile = saveFile( false );
 
                 // If the data must not be saved, load the new data directly
                 else if( option == JOptionPane.NO_OPTION )
                     exportFile = true;
 
                 // Cancel the action if selected
                 else if( option == JOptionPane.CANCEL_OPTION )
                     exportFile = false;
 
             }
 
             if( exportFile ) {
                 // Ask the data of the Learning Object:
                 ExportToLOMDialog dialog = new ExportToLOMDialog( TC.get( "Operation.ExportToLOM.DefaultValue" ) );
                 String loName = dialog.getLomName( );
                 String authorName = dialog.getAuthorName( );
                 String organization = dialog.getOrganizationName( );
                 boolean windowed = dialog.getWindowed( );
                 int type = dialog.getType( );
 
                 boolean validated = dialog.isValidated( );
 
                 if( type == 2 && !hasScormProfiles( SCORM12 ) ) {
                     // error situation: both profiles must be scorm 1.2 if they exist
                     mainWindow.showErrorDialog( TC.get( "Operation.ExportSCORM12.BadProfiles.Title" ), TC.get( "Operation.ExportSCORM12.BadProfiles.Message" ) );
                 }
                 else if( type == 3 && !hasScormProfiles( SCORM2004 ) ) {
                     // error situation: both profiles must be scorm 2004 if they exist
                     mainWindow.showErrorDialog( TC.get( "Operation.ExportSCORM2004.BadProfiles.Title" ), TC.get( "Operation.ExportSCORM2004.BadProfiles.Message" ) );
                 }
                 else if( type == 4 && !hasScormProfiles( AGREGA ) ) {
                     // error situation: both profiles must be scorm 2004 if they exist to export to AGREGA
                     mainWindow.showErrorDialog( TC.get( "Operation.ExportSCORM2004AGREGA.BadProfiles.Title" ), TC.get( "Operation.ExportSCORM2004AGREGA.BadProfiles.Message" ) );
                 }
                 //TODO comprobaciones de perfiles
                // else if( type == 5 ) {
                     // error situation: both profiles must be scorm 2004 if they exist to export to AGREGA
                  //   mainWindow.showErrorDialog( TC.get( "Operation.ExportSCORM2004AGREGA.BadProfiles.Title" ), TC.get( "Operation.ExportSCORM2004AGREGA.BadProfiles.Message" ) );
                 
 
                 if( validated ) {
                     //String loName = this.showInputDialog( TextConstants.getText( "Operation.ExportToLOM.Title" ), TextConstants.getText( "Operation.ExportToLOM.Message" ), TextConstants.getText( "Operation.ExportToLOM.DefaultValue" ));
                     if( loName != null && !loName.equals( "" ) && !loName.contains( " " ) ) {
                         //Check authorName & organization
                         if( authorName != null && authorName.length( ) > 5 && organization != null && organization.length( ) > 5 ) {
 
                             //Ask for the name of the zip
                             String completeFilePath = null;
                             completeFilePath = mainWindow.showSaveDialog( getCurrentExportSaveFolder( ), new FileFilter( ) {
 
                                 @Override
                                 public boolean accept( java.io.File arg0 ) {
 
                                     return arg0.getAbsolutePath( ).toLowerCase( ).endsWith( ".zip" ) || arg0.isDirectory( );
                                 }
 
                                 @Override
                                 public String getDescription( ) {
 
                                     return "Zip files (*.zip)";
                                 }
                             } );
 
                             // If some file was selected set the new file
                             if( completeFilePath != null ) {
                                 // Add the ".zip" if it is not present in the name
                                 if( !completeFilePath.toLowerCase( ).endsWith( ".zip" ) )
                                     completeFilePath += ".zip";
 
                                 // Create a file to extract the name and path
                                 File newFile = new File( completeFilePath );
 
                                 // Check the selected file is contained in a valid folder
                                 if( isValidTargetFile( newFile ) ) {
 
                                     // If the file doesn't exist, or if the user confirms the writing in the file
                                     if( !newFile.exists( ) || mainWindow.showStrictConfirmDialog( TC.get( "Operation.SaveFileTitle" ), TC.get( "Operation.OverwriteExistingFile", newFile.getName( ) ) ) ) {
                                         // If the file exists, delete it so it's clean in the first save
 
                                         try {
                                             if( newFile.exists( ) )
                                                 newFile.delete( );
                                             //LoadingScreen loadingScreen = new LoadingScreen(TextConstants.getText( "Operation.ExportProject.AsJAR" ), getLoadingImage( ), mainWindow);
                                             loadingScreen.setMessage( TC.get( "Operation.ExportProject.AsLO" ) );
                                             loadingScreen.setVisible( true );
                                             this.updateLOMLanguage( );
 
                                             if( type == 0 && Writer.exportAsLearningObject( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ) {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                                             }
                                             else if( type == 1 && Writer.exportAsWebCTObject( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ) {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                                             }
                                             else if( type == 2 && Writer.exportAsSCORM( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ) {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
 
                                             }
                                             else if( type == 3 && Writer.exportAsSCORM2004( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ) {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                                             }
                                             else if( type == 4 && Writer.exportAsAGREGA( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ) {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                                             }
                                             else if( type == 5 && Writer.exportAsLAMSLearningObject( completeFilePath, loName, authorName, organization, windowed, this.currentZipFile, adventureDataControl ) ){
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.Success.Title" ), TC.get( "Operation.ExportT.Success.Message" ) );
                                             }
                                             else {
                                                 mainWindow.showInformationDialog( TC.get( "Operation.ExportT.NotSuccess.Title" ), TC.get( "Operation.ExportT.NotSuccess.Message" ) );
                                             }
 
                                             //loadingScreen.close( );
                                             loadingScreen.setVisible( false );
 
                                         }
                                         catch( Exception e ) {
                                             this.showErrorDialog( TC.get( "Operation.ExportToLOM.LONameNotValid.Title" ), TC.get( "Operation.ExportToLOM.LONameNotValid.Title" ) );
                                             ReportDialog.GenerateErrorReport( e, true, TC.get( "Operation.ExportToLOM.LONameNotValid.Title" ) );
                                         }
 
                                     }
                                 }
                                 else {
                                     // Show error: The target dir cannot be contained 
                                     mainWindow.showErrorDialog( TC.get( "Operation.ExportT.TargetInProjectDir.Title" ), TC.get( "Operation.ExportT.TargetInProjectDir.Message" ) );
                                 }
 
                             }
                         }
                         else {
                             this.showErrorDialog( TC.get( "Operation.ExportToLOM.AuthorNameOrganizationNotValid.Title" ), TC.get( "Operation.ExportToLOM.AuthorNameOrganizationNotValid.Message" ) );
                         }
                     }
                     else {
                         this.showErrorDialog( TC.get( "Operation.ExportToLOM.LONameNotValid.Title" ), TC.get( "Operation.ExportToLOM.LONameNotValid.Message" ) );
                     }
                 }
             }
         }
         catch( Exception e ) {
             loadingScreen.setVisible( false );
             mainWindow.showErrorDialog( "Operation.FileNotSavedTitle", "Operation.FileNotSavedMessage" );
         }
 
     }
 
     /**
      * Check if assessment and adaptation profiles are both scorm 1.2 or scorm
      * 2004
      * 
      * @param scormType
      *            the scorm type, 1.2 or 2004
      * @return
      */
     private boolean hasScormProfiles( int scormType ) {
 
         if( scormType == SCORM12 ) {
             // check that adaptation and assessment profiles are scorm 1.2 profiles
             return chaptersController.hasScorm12Profiles( adventureDataControl );
 
         }
         else if( scormType == SCORM2004 || scormType == AGREGA ) {
             // check that adaptation and assessment profiles are scorm 2004 profiles
             return chaptersController.hasScorm2004Profiles( adventureDataControl );
 
         }
 
         return false;
     }
 
     /**
      * Executes the current project. Firstly, it checks that the game does not
      * present any consistency errors. Then exports the project to the web dir
      * as a temp .ead file and gets it running
      */
     public void run( ) {
 
         stopAutoSave( );
 
         // Check adventure consistency
         if( checkAdventureConsistency( false ) ) {
             this.getSelectedChapterDataControl( ).getConversationsList( ).resetAllConversationNodes( );
             new Timer( ).schedule( new TimerTask( ) {
 
                 @Override
                 public void run( ) {
 
                     if( canBeRun( ) ) {
                         mainWindow.setNormalRunAvailable( false );
                         // First update flags
                         chaptersController.updateVarsFlagsForRunning( );
                         EAdventureDebug.normalRun( Controller.getInstance( ).adventureDataControl.getAdventureData( ), AssetsController.getInputStreamCreator( ) );
                         Controller.getInstance( ).startAutoSave( 15 );
                         mainWindow.setNormalRunAvailable( true );
                     }
 
                 }
 
             }, 1000 );
         }
     }
 
     /**
      * Executes the current project. Firstly, it checks that the game does not
      * present any consistency errors. Then exports the project to the web dir
      * as a temp .ead file and gets it running
      */
     public void debugRun( ) {
 
         stopAutoSave( );
 
         // Check adventure consistency
         if( checkAdventureConsistency( false ) ) {
             this.getSelectedChapterDataControl( ).getConversationsList( ).resetAllConversationNodes( );
             new Timer( ).schedule( new TimerTask( ) {
 
                 @Override
                 public void run( ) {
 
                     if( canBeRun( ) ) {
                         mainWindow.setNormalRunAvailable( false );
                         chaptersController.updateVarsFlagsForRunning( );
                         EAdventureDebug.debug( Controller.getInstance( ).adventureDataControl.getAdventureData( ), AssetsController.getInputStreamCreator( ) );
                         Controller.getInstance( ).startAutoSave( 15 );
                         mainWindow.setNormalRunAvailable( true );
                     }
                 }
 
             }, 1000 );
         }
     }
 
     /**
      * Check if the current project is saved before run. If not, ask user to
      * save it.
      * 
      * @return if false is returned, the game will not be launched
      */
     private boolean canBeRun( ) {
 
         if( dataModified ) {
             if( mainWindow.showStrictConfirmDialog( TC.get( "Run.CanBeRun.Title" ), TC.get( "Run.CanBeRun.Text" ) ) ) {
                 this.saveFile( false );
                 return true;
             }
             else
                 return false;
         }
         else
             return true;
     }
 
     /**
      * Determines if the target file of an exportation process is valid. The
      * file cannot be located neither inside the project folder, nor inside the
      * web folder
      * 
      * @param targetFile
      * @return
      */
     private boolean isValidTargetFile( java.io.File targetFile ) {
 
         java.io.File[] forbiddenParents = new java.io.File[] { ReleaseFolders.webFolder( ), ReleaseFolders.webTempFolder( ), getProjectFolderFile( ) };
         boolean isValid = true;
         for( java.io.File forbiddenParent : forbiddenParents ) {
             if( targetFile.getAbsolutePath( ).toLowerCase( ).startsWith( forbiddenParent.getAbsolutePath( ).toLowerCase( ) ) ) {
                 isValid = false;
                 break;
             }
         }
         return isValid;
     }
 
     /**
      * Determines if the target folder for a new project is valid. The folder
      * cannot be located inside the web folder
      * 
      * @param targetFile
      * @return
      */
     private boolean isValidTargetProject( java.io.File targetFile ) {
 
         java.io.File[] forbiddenParents = new java.io.File[] { ReleaseFolders.webFolder( ), ReleaseFolders.webTempFolder( ) };
         boolean isValid = true;
         for( java.io.File forbiddenParent : forbiddenParents ) {
             if( targetFile.getAbsolutePath( ).toLowerCase( ).startsWith( forbiddenParent.getAbsolutePath( ).toLowerCase( ) ) ) {
                 isValid = false;
                 break;
             }
         }
         return isValid;
     }
 
     /**
      * Exits from the aplication.
      */
     public void exit( ) {
 
         boolean exit = true;
 
         // If the data was not saved, ask for an action (save, discard changes...)
         if( dataModified ) {
             int option = mainWindow.showConfirmDialog( TC.get( "Operation.ExitTitle" ), TC.get( "Operation.ExitMessage" ) );
 
             // If the data must be saved, lexit only if the save was succesful
             if( option == JOptionPane.YES_OPTION )
                 exit = saveFile( false );
 
             // If the data must not be saved, exit directly
             else if( option == JOptionPane.NO_OPTION )
                 exit = true;
 
             // Cancel the action if selected
             else if( option == JOptionPane.CANCEL_OPTION )
                 exit = false;
 
             //if( isTempFile( ) ) {
             //	File file = new File( oldZipFile );
             //	file.deleteOnExit( );
             //	isTempFile = false;
             //}
         }
 
         // Exit the aplication
         if( exit ) {
             ConfigData.storeToXML( );
             ProjectConfigData.storeToXML( );
             //AssetsController.cleanVideoCache( );
             System.exit( 0 );
         }
     }
 
     /**
      * Checks if the adventure is valid or not. It shows information to the
      * user, whether the data is valid or not.
      */
     public boolean checkAdventureConsistency( ) {
 
         return checkAdventureConsistency( true );
     }
 
     public boolean checkAdventureConsistency( boolean showSuccessFeedback ) {
 
         // Create a list to store the incidences
         List<String> incidences = new ArrayList<String>( );
 
         // Check all the chapters
         boolean valid = chaptersController.isValid( null, incidences );
 
         // If the data is valid, show a dialog with the information
         if( valid ) {
             if( showSuccessFeedback )
                 mainWindow.showInformationDialog( TC.get( "Operation.AdventureConsistencyTitle" ), TC.get( "Operation.AdventureConsistentReport" ) );
 
             // If it is not valid, show a dialog with the problems
         }
         else
             new InvalidReportDialog( incidences, TC.get( "Operation.AdventureInconsistentReport" ) );
 
         return valid;
     }
 
     public void checkFileConsistency( ) {
 
     }
 
     /**
      * Shows the adventure data dialog editor.
      */
     public void showAdventureDataDialog( ) {
 
         new AdventureDataDialog( );
     }
 
     /**
      * Shows the LOM data dialog editor.
      */
     public void showLOMDataDialog( ) {
 
         isLomEs = false;
         new LOMDialog( adventureDataControl.getLomController( ) );
     }
 
     /**
      * Shows the LOM for SCORM packages data dialog editor.
      */
     public void showLOMSCORMDataDialog( ) {
 
         isLomEs = false;
         new IMSDialog( adventureDataControl.getImsController( ) );
     }
 
     /**
      * Shows the LOMES for AGREGA packages data dialog editor.
      */
     public void showLOMESDataDialog( ) {
 
         isLomEs = true;
         new LOMESDialog( adventureDataControl.getLOMESController( ) );
     }
 
     /**
      * Shows the GUI style selection dialog.
      */
     public void showGUIStylesDialog( ) {
 
         adventureDataControl.showGUIStylesDialog( );
     }
 
     /**
      * Asks for confirmation and then deletes all unreferenced assets. Checks
      * for animations indirectly referenced assets.
      */
     public void deleteUnsuedAssets( ) {
 
         if( !this.showStrictConfirmDialog( TC.get( "DeleteUnusedAssets.Title" ), TC.get( "DeleteUnusedAssets.Warning" ) ) )
             return;
 
         int deletedAssetCount = 0;
         ArrayList<String> assets = new ArrayList<String>( );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_IMAGE ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_BACKGROUND ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_VIDEO ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_AUDIO ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_CURSOR ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_BUTTON ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_ICON ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_STYLED_TEXT ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_ARROW_BOOK ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
 
         /*  assets.remove( "gui/cursors/arrow_left.png" );
           assets.remove( "gui/cursors/arrow_right.png" ); */
 
         for( String temp : assets ) {
             int references = 0;
             references = countAssetReferences( temp );
             if( references == 0 ) {
                 new File( Controller.getInstance( ).getProjectFolder( ), temp ).delete( );
                 deletedAssetCount++;
             }
         }
 
         assets.clear( );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_ANIMATION_AUDIO ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_ANIMATION_IMAGE ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
         for( String temp : AssetsController.getAssetsList( AssetsController.CATEGORY_ANIMATION ) )
             if( !assets.contains( temp ) )
                 assets.add( temp );
 
         int i = 0;
         while( i < assets.size( ) ) {
             String temp = assets.get( i );
             if( countAssetReferences( AssetsController.removeSuffix( temp ) ) != 0 ) {
                 assets.remove( temp );
                 if( temp.endsWith( "eaa" ) ) {
                     Animation a = Loader.loadAnimation( AssetsController.getInputStreamCreator( ), temp, new EditorImageLoader( ) );
                     for( Frame f : a.getFrames( ) ) {
                         if( f.getUri( ) != null && assets.contains( f.getUri( ) ) ) {
                             for( int j = 0; j < assets.size( ); j++ ) {
                                 if( assets.get( j ).equals( f.getUri( ) ) ) {
                                     if( j < i )
                                         i--;
                                     assets.remove( j );
                                 }
                             }
                         }
                         if( f.getSoundUri( ) != null && assets.contains( f.getSoundUri( ) ) ) {
                             for( int j = 0; j < assets.size( ); j++ ) {
                                 if( assets.get( j ).equals( f.getSoundUri( ) ) ) {
                                     if( j < i )
                                         i--;
                                     assets.remove( j );
                                 }
                             }
                         }
                     }
                 }
                 else {
                     int j = 0;
                     while( j < assets.size( ) ) {
                         if( assets.get( j ).startsWith( AssetsController.removeSuffix( temp ) ) ) {
                             if( j < i )
                                 i--;
                             assets.remove( j );
                         }
                         else
                             j++;
                     }
                 }
             }
             else {
                 i++;
             }
         }
 
         for( String temp2 : assets ) {
             new File( Controller.getInstance( ).getProjectFolder( ), temp2 ).delete( );
             deletedAssetCount++;
         }
 
         if( deletedAssetCount != 0 )
             mainWindow.showInformationDialog( TC.get( "DeleteUnusedAssets.Title" ), TC.get( "DeleteUnusedAssets.AssetsDeleted", new String[] { String.valueOf( deletedAssetCount ) } ) );
         else
             mainWindow.showInformationDialog( TC.get( "DeleteUnusedAssets.Title" ), TC.get( "DeleteUnusedAssets.NoUnsuedAssetsFound" ) );
     }
 
     /**
      * Shows the flags dialog.
      */
     public void showEditFlagDialog( ) {
 
         new VarsFlagsDialog( new VarFlagsController( getVarFlagSummary( ) ) );
     }
 
     /**
      * Sets a new selected chapter with the given index.
      * 
      * @param selectedChapter
      *            Index of the new selected chapter
      */
     public void setSelectedChapter( int selectedChapter ) {
 
         chaptersController.setSelectedChapterInternal( selectedChapter );
         mainWindow.reloadData( );
     }
 
     public void updateVarFlagSummary( ) {
 
         chaptersController.updateVarFlagSummary( );
     }
 
     /**
      * Adds a new chapter to the adventure. This method asks for the title of
      * the chapter to the user, and updates the view of the application if a new
      * chapter was added.
      */
     public void addChapter( ) {
 
         addTool( new AddChapterTool( chaptersController ) );
     }
 
     /**
      * Deletes the selected chapter from the adventure. This method asks the
      * user for confirmation, and updates the view if needed.
      */
     public void deleteChapter( ) {
 
         addTool( new DeleteChapterTool( chaptersController ) );
     }
 
     /**
      * Moves the selected chapter to the previous position of the chapter's
      * list.
      */
     public void moveChapterUp( ) {
 
         addTool( new MoveChapterTool( MoveChapterTool.MODE_UP, chaptersController ) );
     }
 
     /**
      * Moves the selected chapter to the next position of the chapter's list.
      * 
      */
     public void moveChapterDown( ) {
 
         addTool( new MoveChapterTool( MoveChapterTool.MODE_DOWN, chaptersController ) );
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // Methods to edit and get the adventure general data (title and description)
 
     /**
      * Returns the title of the adventure.
      * 
      * @return Adventure's title
      */
     public String getAdventureTitle( ) {
 
         return adventureDataControl.getTitle( );
     }
 
     /**
      * Returns the description of the adventure.
      * 
      * @return Adventure's description
      */
     public String getAdventureDescription( ) {
 
         return adventureDataControl.getDescription( );
     }
 
     /**
      * Returns the LOM controller.
      * 
      * @return Adventure LOM controller.
      * 
      */
     public LOMDataControl getLOMDataControl( ) {
 
         return adventureDataControl.getLomController( );
     }
 
     /**
      * Sets the new title of the adventure.
      * 
      * @param title
      *            Title of the adventure
      */
     public void setAdventureTitle( String title ) {
 
         // If the value is different
         if( !title.equals( adventureDataControl.getTitle( ) ) ) {
             // Set the new title and modify the data
             adventureDataControl.setTitle( title );
         }
     }
 
     /**
      * Sets the new description of the adventure.
      * 
      * @param description
      *            Description of the adventure
      */
     public void setAdventureDescription( String description ) {
 
         // If the value is different
         if( !description.equals( adventureDataControl.getDescription( ) ) ) {
             // Set the new description and modify the data
             adventureDataControl.setDescription( description );
         }
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // Methods that perform specific tasks for the microcontrollers
 
     /**
      * Returns whether the given identifier is valid or not. If the element
      * identifier is not valid, this method shows an error message to the user.
      * 
      * @param elementId
      *            Element identifier to be checked
      * @return True if the identifier is valid, false otherwise
      */
     public boolean isElementIdValid( String elementId ) {
 
         return isElementIdValid( elementId, true );
     }
 
     /**
      * Returns whether the given identifier is valid or not. If the element
      * identifier is not valid, this method shows an error message to the user
      * if showError is true
      * 
      * @param elementId
      *            Element identifier to be checked
      * @param showError
      *            True if the error message must be shown
      * @return True if the identifier is valid, false otherwise
      */
     public boolean isElementIdValid( String elementId, boolean showError ) {
 
         boolean elementIdValid = false;
 
         // Check if the identifier has no spaces
         if( !elementId.contains( " " ) ) {
 
             // If the identifier doesn't exist already
             if( !getIdentifierSummary( ).existsId( elementId ) ) {
 
                 // If the identifier is not a reserved identifier
                 if( !elementId.equals( Player.IDENTIFIER ) && !elementId.equals( TC.get( "ConversationLine.PlayerName" ) ) ) {
                    
                     // If the first character is a letter
                     if( Character.isLetter( elementId.charAt( 0 ) ) ){
                       elementIdValid = isCharacterValid(elementId);
                       if (!elementIdValid)
                           mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorCharacter" ) );
                     }
                     // Show non-letter first character error
                     else if( showError )
                         mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorFirstCharacter" ) );
                 }
 
                 // Show invalid identifier error
                 else if( showError )
                     mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorReservedIdentifier", elementId ) );
             }
 
             // Show repeated identifier error
             else if( showError )
                 mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorAlreadyUsed" ) );
         }
 
         // Show blank spaces error
         else if( showError )
             mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorBlankSpaces" ) );
 
         return elementIdValid;
     }
     
 public boolean isCharacterValid(String elementId){
     Character chId;
     boolean isValid = true;
     int i=1;
     while (i < elementId.length( ) && isValid) {  
       chId = elementId.charAt( i );
       if (chId =='&'   || chId == '%'  || chId == '?'  || chId == ''   ||
           chId ==''  || chId == '!'   || chId== '='   || chId == '$'  || 
           chId == '*' || chId == '/' || chId == '(' || chId == ')' )
             isValid = false;
         i++;
     }
    
     return isValid;
     
     
 }
     /**
      * Returns whether the given identifier is valid or not. If the element
      * identifier is not valid, this method shows an error message to the user.
      * 
      * @param elementId
      *            Element identifier to be checked
      * @return True if the identifier is valid, false otherwise
      */
     public boolean isPropertyIdValid( String elementId ) {
 
         boolean elementIdValid = false;
 
         // Check if the identifier has no spaces
         if( !elementId.contains( " " ) ) {
 
             // If the first character is a letter
             if( Character.isLetter( elementId.charAt( 0 ) ) )
                 elementIdValid = true;
 
             // Show non-letter first character error
             else
                 mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorFirstCharacter" ) );
         }
 
         // Show blank spaces error
         else
             mainWindow.showErrorDialog( TC.get( "Operation.IdErrorTitle" ), TC.get( "Operation.IdErrorBlankSpaces" ) );
 
         return elementIdValid;
     }
 
     /**
      * This method returns the absolute path of the background image of the
      * given scene.
      * 
      * @param sceneId
      *            Scene id
      * @return Path to the background image, null if it was not found
      */
     public String getSceneImagePath( String sceneId ) {
 
         String sceneImagePath = null;
 
         // Search for the image in the list, comparing the identifiers
         for( SceneDataControl scene : getSelectedChapterDataControl( ).getScenesList( ).getScenes( ) )
             if( sceneId.equals( scene.getId( ) ) )
                 sceneImagePath = scene.getPreviewBackground( );
 
         return sceneImagePath;
     }
 
     /**
      * This method returns the trajectory of a scene from its id.
      * 
      * @param sceneId
      *            Scene id
      * @return Trajectory of the scene, null if it was not found
      */
     public Trajectory getSceneTrajectory( String sceneId ) {
 
         Trajectory trajectory = null;
 
         // Search for the image in the list, comparing the identifiers
         for( SceneDataControl scene : getSelectedChapterDataControl( ).getScenesList( ).getScenes( ) )
             if( sceneId.equals( scene.getId( ) ) && scene.getTrajectory( ).hasTrajectory( ) )
                 trajectory = (Trajectory) scene.getTrajectory( ).getContent( );
 
         return trajectory;
     }
 
     /**
      * This method returns the absolute path of the default image of the player.
      * 
      * @return Default image of the player
      */
     public String getPlayerImagePath( ) {
 
         if( getSelectedChapterDataControl( ) != null )
             return getSelectedChapterDataControl( ).getPlayer( ).getPreviewImage( );
         else
             return null;
     }
 
     /**
      * This method returns the absolute path of the default image of the given
      * element (item or character).
      * 
      * @param elementId
      *            Id of the element
      * @return Default image of the requested element
      */
     public String getElementImagePath( String elementId ) {
 
         String elementImage = null;
 
         // Search for the image in the items, comparing the identifiers
         for( ItemDataControl item : getSelectedChapterDataControl( ).getItemsList( ).getItems( ) )
             if( elementId.equals( item.getId( ) ) )
                 elementImage = item.getPreviewImage( );
 
         // Search for the image in the characters, comparing the identifiers
         for( NPCDataControl npc : getSelectedChapterDataControl( ).getNPCsList( ).getNPCs( ) )
             if( elementId.equals( npc.getId( ) ) )
                 elementImage = npc.getPreviewImage( );
         // Search for the image in the items, comparing the identifiers
         for( AtrezzoDataControl atrezzo : getSelectedChapterDataControl( ).getAtrezzoList( ).getAtrezzoList( ) )
             if( elementId.equals( atrezzo.getId( ) ) )
                 elementImage = atrezzo.getPreviewImage( );
 
         return elementImage;
     }
 
     /**
      * Counts all the references to a given asset in the entire script.
      * 
      * @param assetPath
      *            Path of the asset (relative to the ZIP), without suffix in
      *            case of an animation or set of slides
      * @return Number of references to the given asset
      */
     public int countAssetReferences( String assetPath ) {
 
         return adventureDataControl.countAssetReferences( assetPath ) + chaptersController.countAssetReferences( assetPath );
     }
 
     /**
      * Gets a list with all the assets referenced in the chapter along with the
      * types of those assets
      * 
      * @param assetPaths
      * @param assetTypes
      */
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         adventureDataControl.getAssetReferences( assetPaths, assetTypes );
         chaptersController.getAssetReferences( assetPaths, assetTypes );
     }
 
     /**
      * Deletes a given asset from the script, removing all occurrences.
      * 
      * @param assetPath
      *            Path of the asset (relative to the ZIP), without suffix in
      *            case of an animation or set of slides
      */
     public void deleteAssetReferences( String assetPath ) {
 
         adventureDataControl.deleteAssetReferences( assetPath );
         chaptersController.deleteAssetReferences( assetPath );
     }
 
     /**
      * Counts all the references to a given identifier in the entire script.
      * 
      * @param id
      *            Identifier to which the references must be found
      * @return Number of references to the given identifier
      */
     public int countIdentifierReferences( String id ) {
 
         return getSelectedChapterDataControl( ).countIdentifierReferences( id );
     }
 
     /**
      * Deletes a given identifier from the script, removing all occurrences.
      * 
      * @param id
      *            Identifier to be deleted
      */
     public void deleteIdentifierReferences( String id ) {
 
         chaptersController.deleteIdentifierReferences( id );
     }
 
     /**
      * Replaces a given identifier with another one, in all the occurrences in
      * the script.
      * 
      * @param oldId
      *            Old identifier to be replaced
      * @param newId
      *            New identifier to replace the old one
      */
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         getSelectedChapterDataControl( ).replaceIdentifierReferences( oldId, newId );
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // Methods linked with the GUI
 
     /**
      * Updates the chapter menu with the new names of the chapters.
      */
     public void updateChapterMenu( ) {
 
         mainWindow.updateChapterMenu( );
     }
 
     /**
      * Updates the tree of the main window.
      */
     public void updateStructure( ) {
 
         mainWindow.updateStructure( );
     }
 
     /**
      * Reloads the panel of the main window currently being used.
      */
     public void reloadPanel( ) {
 
         mainWindow.reloadPanel( );
     }
 
     public void updatePanel( ) {
 
         mainWindow.updatePanel( );
     }
 
     /**
      * Reloads the panel of the main window currently being used.
      */
     public void reloadData( ) {
 
         mainWindow.reloadData( );
     }
 
     /**
      * Returns the last window opened by the application.
      * 
      * @return Last window opened
      */
     public Window peekWindow( ) {
 
         return mainWindow.peekWindow( );
     }
 
     /**
      * Pushes a new window in the windows stack.
      * 
      * @param window
      *            Window to push
      */
     public void pushWindow( Window window ) {
 
         mainWindow.pushWindow( window );
     }
 
     /**
      * Pops the last window pushed into the stack.
      */
     public void popWindow( ) {
 
         mainWindow.popWindow( );
     }
 
     /**
      * Shows a load dialog to select multiple files.
      * 
      * @param filter
      *            File filter for the dialog
      * @return Full path of the selected files, null if no files were selected
      */
     public String[] showMultipleSelectionLoadDialog( FileFilter filter ) {
 
         return mainWindow.showMultipleSelectionLoadDialog( currentZipPath, filter );
     }
 
     /**
      * Shows a dialog with the options "Yes" and "No", with the given title and
      * text.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @return True if the "Yes" button was pressed, false otherwise
      */
     public boolean showStrictConfirmDialog( String title, String message ) {
 
         return mainWindow.showStrictConfirmDialog( title, message );
     }
 
     /**
      * Shows a dialog with the given set of options.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param options
      *            Array of strings containing the options of the dialog
      * @return The index of the option selected, JOptionPane.CLOSED_OPTION if
      *         the dialog was closed.
      */
     public int showOptionDialog( String title, String message, String[] options ) {
 
         return mainWindow.showOptionDialog( title, message, options );
     }
 
     /**
      * Uses the GUI to show an input dialog.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param defaultValue
      *            Default value of the dialog
      * @return String typed in the dialog, null if the cancel button was pressed
      */
     public String showInputDialog( String title, String message, String defaultValue ) {
 
         return mainWindow.showInputDialog( title, message, defaultValue );
     }
 
     /**
      * Uses the GUI to show an input dialog.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      * @param selectionValues
      *            Possible selection values of the dialog
      * @return Option selected in the dialog, null if the cancel button was
      *         pressed
      */
     public String showInputDialog( String title, String message, Object[] selectionValues ) {
 
         return mainWindow.showInputDialog( title, message, selectionValues );
     }
 
     /**
      * Uses the GUI to show an error dialog.
      * 
      * @param title
      *            Title of the dialog
      * @param message
      *            Message of the dialog
      */
     public void showErrorDialog( String title, String message ) {
 
         mainWindow.showErrorDialog( title, message );
     }
 
     public void showCustomizeGUIDialog( ) {
 
         new CustomizeGUIDialog( this.adventureDataControl );
     }
 
     public boolean isFolderLoaded( ) {
 
         return chaptersController.isAnyChapterSelected( );
     }
 
     public String getEditorMinVersion( ) {
 
         return "1.0b";
     }
 
     public String getEditorVersion( ) {
 
         return "1.0b";
     }
 
     public void updateLOMLanguage( ) {
 
         this.adventureDataControl.getLomController( ).updateLanguage( );
 
     }
 
     public void updateIMSLanguage( ) {
 
         this.adventureDataControl.getImsController( ).updateLanguage( );
     }
 
     public void showAboutDialog( ) {
 
         try {
             JDialog dialog = new JDialog( Controller.getInstance( ).peekWindow( ), TC.get( "About" ), Dialog.ModalityType.TOOLKIT_MODAL );
             dialog.getContentPane( ).setLayout( new BorderLayout( ) );
             File file = new File( ReleaseFolders.LANGUAGE_DIR_EDITOR + "/" + ConfigData.getAboutFile( ) );
             if( file.exists( ) ) {
                 JEditorPane pane = new JEditorPane( );
                 pane.setPage( file.toURI( ).toURL( ) );
                 pane.setEditable( false );
                 dialog.getContentPane( ).add( pane, BorderLayout.CENTER );
                 //dialog.pack( );
                 dialog.setSize( 275, 560 );
                 pane.setMinimumSize( dialog.getSize( ) );
                 Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
                 dialog.setLocation( ( screenSize.width - dialog.getWidth( ) ) / 2, ( screenSize.height - dialog.getHeight( ) ) / 2 );
                 dialog.setVisible( true );
             }
 
         }
         catch( IOException e ) {
             ReportDialog.GenerateErrorReport( e, true, "UNKNOWERROR" );
         }
     }
 
     public AssessmentProfilesDataControl getAssessmentController( ) {
 
         return this.chaptersController.getSelectedChapterDataControl( ).getAssessmentProfilesDataControl( );
     }
 
     public AdaptationProfilesDataControl getAdaptationController( ) {
 
         return this.chaptersController.getSelectedChapterDataControl( ).getAdaptationProfilesDataControl( );
     }
 
     public boolean isCommentaries( ) {
 
         return this.adventureDataControl.isCommentaries( );
     }
 
     public void setCommentaries( boolean b ) {
 
         this.adventureDataControl.setCommentaries( b );
 
     }
 
     /**
      * Returns an int value representing the current language used to display
      * the editor
      * 
      * @return
      */
     public String getLanguage( ) {
 
         return this.languageFile;
     }
 
     /**
      * Sets the current language of the editor. Accepted values are
      * {@value #LANGUAGE_ENGLISH} & {@value #LANGUAGE_ENGLISH}. This method
      * automatically updates the about, language strings, and loading image
      * parameters.
      * 
      * The method will reload the main window always
      * 
      * @param language
      */
     public void setLanguage( String language ) {
 
         setLanguage( language, true );
     }
 
     /**
      * Sets the current language of the editor. Accepted values are
      * {@value #LANGUAGE_ENGLISH} & {@value #LANGUAGE_ENGLISH}. This method
      * automatically updates the about, language strings, and loading image
      * parameters.
      * 
      * The method will reload the main window if reloadData is true
      * 
      * @param language
      */
     public void setLanguage( String language, boolean reloadData ) {
 
         ConfigData.setLanguangeFile( ReleaseFolders.getLanguageFilePath( language ), ReleaseFolders.getAboutFilePath( language ), ReleaseFolders.getLoadingImagePath( language ) );
         languageFile = language;
         TC.loadStrings( ReleaseFolders.getLanguageFilePath4Editor( true, languageFile ) );
         TC.appendStrings( ReleaseFolders.getLanguageFilePath4Editor( false, languageFile ) );
         loadingScreen.setImage( getLoadingImage( ) );
         if( reloadData )
             mainWindow.reloadData( );
     }
 
     public String getLoadingImage( ) {
 
         return ConfigData.getLoadingImage( );
     }
 
     public void showGraphicConfigDialog( ) {
 
         // Show the dialog
         GraphicConfigDialog guiStylesDialog = new GraphicConfigDialog( adventureDataControl.getGraphicConfig( ) );
 
         // If the new GUI style is different from the current, and valid, change the value
         int optionSelected = guiStylesDialog.getOptionSelected( );
         if( optionSelected != -1 && this.adventureDataControl.getGraphicConfig( ) != optionSelected ) {
             adventureDataControl.setGraphicConfig( optionSelected );
         }
     }
 
     // METHODS TO MANAGE UNDO/REDO
 
     public boolean addTool( Tool tool ) {
 
         boolean added = chaptersController.addTool( tool );
         //tsd.update();
         return added;
     }
 
     public void undoTool( ) {
 
         chaptersController.undoTool( );
         //tsd.update();
     }
 
     public void redoTool( ) {
 
         chaptersController.redoTool( );
         //tsd.update();
     }
 
     public void pushLocalToolManager( ) {
 
         chaptersController.pushLocalToolManager( );
     }
 
     public void popLocalToolManager( ) {
 
         chaptersController.popLocalToolManager( );
     }
 
     public void search( ) {
 
         new SearchDialog( );
     }
 
     public boolean getAutoSaveEnabled( ) {
 
         if( ProjectConfigData.existsKey( "autosave" ) ) {
             String temp = ProjectConfigData.getProperty( "autosave" );
             if( temp.equals( "yes" ) ) {
                 return true;
             }
             else {
                 return false;
             }
         }
         return true;
     }
 
     public void setAutoSaveEnabled( boolean selected ) {
 
         if( selected != getAutoSaveEnabled( ) ) {
             ProjectConfigData.setProperty( "autosave", ( selected ? "yes" : "no" ) );
             startAutoSave( 15 );
         }
     }
 
     /**
      * @return the isLomEs
      */
     public boolean isLomEs( ) {
 
         return isLomEs;
     }
 
     public String getDefaultExitCursorPath( ) {
 
         String temp = this.adventureDataControl.getCursorPath( "exit" );
         if( temp != null && temp.length( ) > 0 )
             return temp;
         else
             return "gui/cursors/exit.png";
     }
 
     public AdvancedFeaturesDataControl getAdvancedFeaturesController( ) {
 
         return this.chaptersController.getSelectedChapterDataControl( ).getAdvancedFeaturesController( );
     }
 
     public static Color generateColor( int i ) {
 
         int r = ( i * 180 ) % 256;
         int g = ( ( i + 4 ) * 130 ) % 256;
         int b = ( ( i + 2 ) * 155 ) % 256;
 
         if( r > 250 && g > 250 && b > 250 ) {
             r = 0;
             g = 0;
             b = 0;
         }
 
         return new Color( r, g, b );
     }
 
 }
