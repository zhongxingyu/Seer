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
 package es.eucm.eadventure.engine.gamelauncher;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.WindowConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 import es.eucm.eadventure.common.auxiliar.ReleaseFolders;
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.common.loader.Loader;
 import es.eucm.eadventure.engine.EAdventure;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.config.ConfigData;
 import es.eucm.eadventure.engine.gamelauncher.gameentry.GameEntry;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * This window shows the available games with their descriptions. The user can
  * select one and launch it.
  */
 /**
  * @updated by Javier Torrente. New functionalities added (02/2008) - Support
  *          for .ead files. Therefore <e-Adventure> files are no longer .zip but
  *          .ead
  * @updated by Enrique Lpez. Functionalities added (10/2008) - Button refresh,
  *          to update the content of a folder. Filter for .ead files. -
  *          Multilanguage support. Two new classes added - "About" and "Open"
  *          tabs. Now the applications is structured in a JTabbedPane, and the
  *          construction of the GUI has been modularized in several methods
  */
 
 public class GameLauncher extends JFrame implements Runnable {
 
     /**
      * Required
      */
     private static final long serialVersionUID = 95641854718010553L;
 
     /**
      * Window's width
      */
     private static final int WINDOW_WIDTH = 690;
 
     /**
      * Window's height
      */
     private static final int WINDOW_HEIGHT = 620;
 
     /**
      * Name of the loaded adventure
      */
     private String adventureName;
 
     /**
      * File dialog with the current directory
      */
     JFileChooser fileDialog;
 
     /**
      * Path to the loaded adventure zip file
      */
     private String adventurePath;
 
     /**
      * List of the games in the current directory
      */
     private JList lstGames;
 
     /**
      * List of the games shown in the window
      */
     private DefaultListModel mdlGames;
 
     /**
      * Text field to show current directory
      */
     private JTextField txtCurrentDir;
 
     /**
      * Text pane to show the description of the selected adventure
      */
     private JTextPane txtDescription;
 
     /**
      * Load button.
      */
     private JButton btnLoad;
 
     /**
      * Combo Box
      */
     private JComboBox combo;
 
     /**
      * Refresh button
      */
     private JButton btnRefresh;
 
     // Creation of the three panels
     JPanel currentDirectoryPanel;
 
     JEditorPane aboutPanel;
 
     JPanel buttonsPanel;
 
     JPanel adventuresPanel;
 
     JPanel centralPanel;
 
     JTabbedPane tabbedPanel;
 
     JPanel global;
 
     JPanel globalTotal;
 
     JEditorPane aboutEditor;
 
     /**
      * Flag to load the selected adventure in the next iteration of the thread
      */
     private boolean load;
 
     /**
      * Flag to close the window in the next iteration of the thread
      */
     private boolean end;
 
     /**
      * Flag to indicate if we are loading a game from the command line
      */
     private boolean initGameLoad;
 
     /**
      * Creates a new GameLaucher. This constructor does actually nothing, you
      * have to call init() for this class to be ready for use.
      */
     public GameLauncher( ) {
 
     }
 
     /**
      * Initializes the frame and loads the games in the current directory
      */
     public void init( File file ) {
 
         // Create the list of icons of the window
         List<Image> icons = new ArrayList<Image>( );
 
         icons.add( new ImageIcon( "gui/Icono-Motor-16x16.png" ).getImage( ) );
         icons.add( new ImageIcon( "gui/Icono-Motor-32x32.png" ).getImage( ) );
         icons.add( new ImageIcon( "gui/Icono-Motor-64x64.png" ).getImage( ) );
         icons.add( new ImageIcon( "gui/Icono-Motor-128x128.png" ).getImage( ) );
         this.setIconImages( icons );
 
         initGameLoad = false;
         // Load the configuration
         //ConfigData.loadFromXML( ReleaseFolders.configFileEngineRelativePath() );
 
         // Setup the window
         setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
         setLocation( ( screenSize.width - WINDOW_WIDTH ) / 2, ( screenSize.height - WINDOW_HEIGHT ) / 2 );
 
         setTitle( TC.get( "MainWindow.GameLauncherTitle" ) );
         setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
         this.addWindowListener( new WindowAdapter( ) {
 
             @Override
             public void windowClosed( WindowEvent e ) {
 
             }
 
             @Override
             public void windowClosing( WindowEvent e ) {
 
                 end = true;
             }
 
         } );
 
         // Creation of the three panels
         currentDirectoryPanel = createCurrentDirectoryPanel( file );
         aboutPanel = createAboutPanel( );
         buttonsPanel = createButtonsPanel( );
         adventuresPanel = createAdventuresPanel( );
 
         //Creation of the panel for the logo
         Icon logo = new ImageIcon( "img/logo-engine.png" );
         JLabel label = new JLabel( logo );
         centralPanel = new JPanel( );
         centralPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.weighty = 0;
         c.weightx = 0;
         c.fill = GridBagConstraints.BOTH;
         centralPanel.add( label, c );
 
         // Embed the the button and the currentDirectory in the global Panel, and this last one
         // together with the adventure Panel in the globalTotal
         global = new JPanel( );
         global.setLayout( new BorderLayout( ) );
 
         global.add( currentDirectoryPanel, BorderLayout.CENTER );
         global.add( buttonsPanel, BorderLayout.SOUTH );
 
         globalTotal = new JPanel( );
         globalTotal.setLayout( new BorderLayout( ) );
         globalTotal.add( adventuresPanel, BorderLayout.CENTER );
         globalTotal.add( global, BorderLayout.SOUTH );
 
         // Creation of the object JTabbedPane
         JTabbedPane tabbedPanel = new JTabbedPane( );
         tabbedPanel.insertTab( TC.get( "MainWindow.TabOpen" ), null, globalTotal, "", 0 );
         tabbedPanel.insertTab( TC.get( "MainWindow.TabAbout" ), null, aboutPanel, "", 1 );
 
         add( tabbedPanel, BorderLayout.CENTER );
         add( centralPanel, BorderLayout.NORTH );
 
         btnLoad.getRootPane( ).setDefaultButton( btnLoad );
         end = false;
 
         // Load adventures in the current directory
         adventureName = "";
         boolean existsExportsFolder = false;
         if( !ReleaseFolders.exportsFolder( ).exists( ) )
             existsExportsFolder = ReleaseFolders.exportsFolder( ).mkdirs( );
         else
             existsExportsFolder = true;
         if( existsExportsFolder )
             loadDir( ReleaseFolders.exportsFolder( ) );
         else
             loadDir( new File( "." ) );
 
         // Bring up the windows
         setVisible( true );
 
         try {
             if( file.getCanonicalPath( ) == "" ) {
                 load = false;
             }
             else if( file.exists( ) ) {
                 lstGames.setSelectedIndex( 1 );
                 lstGames.setSelectedValue( file, false );
                 initGameLoad = true;
                 loadIndividualFile( file );
             }
         }
         catch( IOException e ) {
         }
     }
 
     private JEditorPane createAboutPanel( ) {
 
         aboutEditor = new JEditorPane( );
 
         String chain = "", chainAux = "";
         // We set the editor to use HTML content
         aboutEditor.setContentType( "text/html" );
         
         //about file route
         String dirAboutFile = ReleaseFolders.LANGUAGE_DIR_ENGINE + "/" + ReleaseFolders.getAboutFilePath( ConfigData.getLanguage( ));
         File fichero = new File(dirAboutFile);
         if (!fichero.exists( ))
             dirAboutFile = ReleaseFolders.LANGUAGE_DIR_ENGINE + "/" + ReleaseFolders.getDefaultAboutFilePath( );
      
         try {
             BufferedReader bf = new BufferedReader( new FileReader( dirAboutFile ) );
             while( ( chainAux = bf.readLine( ) ) != null )
                 chain = chain + chainAux;
             bf.close( );
         }
         catch( IOException e ) {
         }
         aboutEditor.setText( chain );
         aboutEditor.setEditable( false );
 
         return aboutEditor;
     }
 
     /*
      * Creates the panel with the availables adventures and the description
      */
     private JPanel createAdventuresPanel( ) {
 
         // Shows available adventures and their descriptions
         JPanel adventuresPanel = new JPanel( );
         adventuresPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
 
         c = new GridBagConstraints( );
         c.insets = new Insets( 10, 10, 2, 10 );
         c.gridy = 0;
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 1;
 
         JTextPane listGamesInfo = new JTextPane( );
         listGamesInfo.setEditable( false );
         listGamesInfo.setBackground( getForeground( ) );
         listGamesInfo.setText( TC.get( "MainWindow.ListGamesText" ) );
         adventuresPanel.add( listGamesInfo, c );
 
         c.insets = new Insets( 0, 10, 10, 10 );
         c.gridy = 1;
         c.weighty = 1;
 
         mdlGames = new DefaultListModel( );
         lstGames = new JList( mdlGames );
         lstGames.addListSelectionListener( new ListSelectionListener( ) {
 
             public void valueChanged( ListSelectionEvent e ) {
 
                               
                 GameEntry ge = (GameEntry) lstGames.getSelectedValue( );
                 if( ge != null ) {
                     // Show the description
                     txtDescription.setText( ge.getDescription( ) );
 
                     // If the adventure is valid, enable the load button
                     if( ge.isValid( ) ) {
                         btnLoad.setText( TC.get( "MainWindow.buttonLoad" ) );
                         btnLoad.setEnabled( true );
                     }
 
                     // Else, disable it
                     else {
                         btnLoad.setText( TC.get( "MainWindow.InvalidAdventure" ) );
                         btnLoad.setEnabled( false );
                     }
                 }
             }
         } );
         lstGames.addMouseListener( new MouseAdapter( ) {
 
             @Override
             public void mouseClicked( MouseEvent e ) {
 
                 if( e.getClickCount( ) > 1 )
                     load( );
             }
         } );
 
         JScrollPane scrollPane = new JScrollPane( lstGames, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         scrollPane.setPreferredSize( new Dimension( 0, 0 ) );
         adventuresPanel.add( scrollPane, c );
 
         c.insets = new Insets( 10, 10, 2, 10 );
         c.gridy = 2;
         c.weighty = 0;
         JTextPane descriptionGamesInfo = new JTextPane( );
         descriptionGamesInfo.setEditable( false );
         descriptionGamesInfo.setBackground( getForeground( ) );
         descriptionGamesInfo.setText( TC.get( "MainWindow.GameDescriptionText" ) );
         adventuresPanel.add( descriptionGamesInfo, c );
 
         c.insets = new Insets( 0, 10, 10, 10 );
         c.gridy = 3;
         c.weighty = 1;
         txtDescription = new JTextPane( );
         txtDescription.setEditable( false );
         scrollPane = new JScrollPane( txtDescription, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
         scrollPane.setPreferredSize( new Dimension( 0, 0 ) );
         adventuresPanel.add( scrollPane, c );
 
         return adventuresPanel;
     }
 
     /*
      * Creates a Panel with the field which establish the current directory
      */
 
     private JPanel createCurrentDirectoryPanel( File file ) {
 
         // Shows and change current directory
         JPanel currentDirectoryPanel = new JPanel( );
         currentDirectoryPanel.setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 4, 10, 2, 10 );
         c.anchor = GridBagConstraints.LINE_START;
         c.gridwidth = 2;
         currentDirectoryPanel.add( new JLabel( TC.get( "MainWindow.FolderText" ) ), c );
         c.insets = new Insets( 0, 10, 10, 3 );
         c.gridy = 1;
         c.gridwidth = 1;
         c.weightx = 8;
         c.fill = GridBagConstraints.HORIZONTAL;
         try {
             txtCurrentDir = new JTextField( new File( "." ).getCanonicalPath( ) );
         }
         catch( IOException e ) {
             e.printStackTrace( );
         }
         txtCurrentDir.setEditable( false );
         currentDirectoryPanel.add( txtCurrentDir, c );
         c.insets = new Insets( 0, 3, 10, 10 );
         c.gridx = 1;
         c.weightx = 0;
         JButton btnExamine = new JButton( TC.get( "MainWindow.buttonExamine" ) );
         btnExamine.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 examine( );
             }
         } );
         currentDirectoryPanel.add( btnExamine, c );
         return currentDirectoryPanel;
     }
 
     /*
      * Creates a Panel with the buttons to interact with the files
      */
     private JPanel createButtonsPanel( ) {
 
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 0, 10, 10, 10 );
         c.gridy = 3;
         c.weighty = 1;
         // Buttons to launch and adventure or exit
         buttonsPanel = new JPanel( );
         buttonsPanel.setLayout( new FlowLayout( ) );
         btnLoad = new JButton( TC.get( "MainWindow.buttonLoad" ) );
         btnLoad.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 load( );
             }
 
         } );
         buttonsPanel.add( btnLoad );
         JButton btnCancel = new JButton( TC.get( "MainWindow.buttonCancel" ) );
         btnCancel.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 end = true;
             }
         } );
         buttonsPanel.add( btnCancel );
 
         //Button to refresh the content of the folder
         btnRefresh = new JButton( TC.get( "MainWindow.buttonRefresh" ) );
         btnRefresh.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent arg0 ) {
 
                 refresh( );
             }
         } );
         buttonsPanel.add( btnRefresh, c );
 
         //Combo panel to switch the language
         final List<String> languages = ReleaseFolders.getLanguages( "engine");
         String[] options = new String[languages.size( ) + 1];
         options[0] = TC.get( "MainWindow.SelectLanguage" );
         for (int i = 0 ; i < languages.size( ); i++)
             options[i+1] = ReleaseFolders.getLanguageName( languages.get( i ) );
         combo = new JComboBox( options );
         combo.addActionListener( new ActionListener( ) {
             public void actionPerformed( ActionEvent arg0 ) {
                 String args[] = new String[ 2 ];
                 args[0] = "";
 
                 if (combo.getSelectedIndex( ) > 0)
                     args[1] = ReleaseFolders.getLanguageFilePath( languages.get( combo.getSelectedIndex() - 1 ) );
 
                 EAdventure.main( args );
 
                 setVisible( false );
                 dispose( );
             }
         }
 
         );
 
         c.gridy++;
         buttonsPanel.add( combo, c );
         return buttonsPanel;
     }
 
     /*
      * (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     public void run( ) {
 
         while( !end ) {
             try {
 
                 if( load ) {
                     // Launch the selected adventure, if any
                     load = false;
 
                     this.setVisible( false );
                     String adventurePath = getAdventurePath( );
                     String adventureName = getAdventureName( );
                     if( adventureName.length( ) > 0 ) {
                         ResourceHandler.setRestrictedMode( false );
                         ResourceHandler.getInstance( ).setZipFile( adventurePath + adventureName + ".ead" );
                         Game.create( );
                         Game.getInstance( ).setAdventurePath( adventurePath );
                         Game.getInstance( ).setAdventureName( adventureName );
                         Game.getInstance( ).run( );
                         Game.delete( );
                         ResourceHandler.getInstance( ).closeZipFile( );
                         ResourceHandler.delete( );
                         this.setVisible( !initGameLoad );
                     }
                     if( initGameLoad )
                         System.exit( 0 );
                 }
                 Thread.sleep( 10 );
             }
             catch( InterruptedException e ) {
             }
         }
 
         ConfigData.storeToXML( );
 
         this.setEnabled( false );
         this.setVisible( false );
         this.setFocusable( false );
 
         if( !initGameLoad )
             System.exit( 0 );
     }
 
     /**
      * Returns the path of the selected adventure
      * 
      * @return the path of the selected adventure
      */
     public String getAdventurePath( ) {
 
         return adventurePath;
     }
 
     /**
      * Returns the name of the selected adventure
      * 
      * @return the name of the selected adventure
      */
     public String getAdventureName( ) {
 
         return adventureName;
     }
 
     /**
      * Prepares the selected adventure to be launched
      */
     private void load( ) {
 
         // If there is any selected adventure
 
         if( lstGames.getSelectedIndex( ) > -1 ) {
             // Get the selected adventure name and path
 
             adventureName = ( (GameEntry) lstGames.getSelectedValue( ) ).getFilename( );
             // Change windows folder separator (\) for universal folder separator (/)
             adventureName = adventureName.replace( "\\", "/" );
             // Get the path to the adventure
             adventurePath = adventureName.substring( 0, adventureName.lastIndexOf( "/" ) + 1 );
             // Get the name of the adventure file
             adventureName = adventureName.substring( adventureName.lastIndexOf( "/" ) + 1 );
             // Remove the extension (.ead) of the adventure file
            adventureName = adventureName.substring( 0, adventureName.lastIndexOf( "." ) );
             // Load the selected adventure in the next iteration of the thread 
             load = true;
         }
     }
 
     /*
      * After selecting an individual file within the dialog box, we load it.
      */
     private void loadIndividualFile( File file ) {
 
         // Get the selected adventure name and path
         try {
             adventureName = file.getCanonicalPath( );
         }
         catch( IOException e ) {
             e.printStackTrace( );
         }
 
         // Change windows folder separator (\) for universal folder separator (/)
         adventureName = adventureName.replace( "\\", "/" );
         // Get the path to the adventure
         adventurePath = adventureName.substring( 0, adventureName.lastIndexOf( "/" ) + 1 );
         // Get the name of the adventure file
         adventureName = adventureName.substring( adventureName.lastIndexOf( "/" ) + 1 );
         // Remove the extension (.ead) of the adventure file
         adventureName = adventureName.substring( 0, adventureName.indexOf( "." ) );
         // Load the selected adventure in the next iteration of the thread 
         load = true;
         //System.out.println( adventurePath);
         //System.out.println( adventureName);
 
     }
 
     /*
      * Refresh the elements located within the text field
      */
     private void refresh( ) {
 
         File file = new File( txtCurrentDir.getText( ) );
         loadDir( file );
     }
 
     /**
      * Shows a dialog to change the current directory
      */
     private void examine( ) {
 
         // Initialize the dialog
 
         if( !ReleaseFolders.exportsFolder( ).exists( ) )
             fileDialog = new JFileChooser( "." );
         else
             fileDialog = new JFileChooser( ReleaseFolders.exportsFolder( ) );
         // Select directories and files
         fileDialog.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
         fileDialog.setFileFilter( new FolderandEADFileFilter( ) );
 
         // If the user clicks OK button, load the adventures in the selected directory,
         // or loads the selected adventure
 
         if( fileDialog.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION )
             if( fileDialog.getSelectedFile( ).isFile( ) ) {
                 loadIndividualFile( fileDialog.getSelectedFile( ) );
             }
             else {
                 loadDir( fileDialog.getSelectedFile( ) );
                 //  btnRefresh.setEnabled( false );
                 load( );
 
             }
     }
 
     /**
      * Loads the adventures in the given directory
      * 
      * @param file
      *            the directory where to search for adventures
      */
     private void loadDir( File file ) {
 
         try{
             // Update list of games in the current dir
             mdlGames.removeAllElements( );
             txtDescription.setText( TC.get( "MainWindow.SelectGameText" ) );
             txtCurrentDir.setText( file.getCanonicalPath( ) );
             List<GameEntry> gameEntries = scanDirectory( file );
             for( int i = 0; i < gameEntries.size( ); i++ )
                 mdlGames.addElement( gameEntries.get( i ) );
         } catch( IOException e ) {
             e.printStackTrace( );
         }
     }
 
     private List<GameEntry> scanDirectory(File directory){
         List<GameEntry> gameEntries = new ArrayList<GameEntry>();
         for (File file :directory.listFiles( )){
             if (file.getAbsolutePath( ).toLowerCase( ).endsWith( ".ead" )){
                 ResourceHandler.setRestrictedMode( false );
                 ResourceHandler.getInstance( ).setZipFile( file.getAbsolutePath( ) );
                 try {
                     DescriptorData descriptor = Loader.loadDescriptorData( ResourceHandler.getInstance( ) );
                     if (descriptor!=null){
                         GameEntry gameEntry = new GameEntry();
                         gameEntry.setDescription( descriptor.getDescription( ) );
                         gameEntry.setTitle( descriptor.getTitle( ) );
                         gameEntry.setValid( true );
                         gameEntry.setFilename( file.getAbsolutePath( ) );
                         gameEntries.add( gameEntry );
                     }
                 } catch (Exception e){};
                 ResourceHandler.getInstance( ).closeZipFile( );
                 ResourceHandler.delete( ); 
             }
         }
         return gameEntries;
     }
     
     /**
      * A filter for the Open Dialog that shows only folders
      */
     private class FolderandEADFileFilter extends FileFilter {
 
         @Override
         public boolean accept( File f ) {
 
             return ( ( f.isDirectory( ) ) || ( f.isFile( ) && ( f.toString( ).toLowerCase( ).endsWith( ".ead" ) == true ) ) );
         }
 
         @Override
         public String getDescription( ) {
 
             return "Folders and .ead files";
         }
     }
 
 } //End
