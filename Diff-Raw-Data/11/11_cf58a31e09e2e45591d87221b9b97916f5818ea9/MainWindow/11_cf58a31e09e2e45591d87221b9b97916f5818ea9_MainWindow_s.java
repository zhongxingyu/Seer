 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Frame;
 import java.awt.event.ItemEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.TreeSet;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.exceptions.CoreException.ErrorType;
 import de.unisiegen.gtitool.core.exceptions.alphabet.AlphabetException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineValidationException;
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 import de.unisiegen.gtitool.core.exceptions.symbol.SymbolException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.grammars.Grammar;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.preferences.listener.LanguageChangedListener;
 import de.unisiegen.gtitool.core.storage.Element;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.EditorPanel;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.Version;
 import de.unisiegen.gtitool.ui.exchange.Exchange;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel.MachineType;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 import de.unisiegen.gtitool.ui.netbeans.helperclasses.RecentlyUsedMenuItem;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.item.OpenedFilesItem;
 import de.unisiegen.gtitool.ui.preferences.item.RecentlyUsedFilesItem;
 import de.unisiegen.gtitool.ui.storage.Storage;
 
 
 /**
  * The main programm window.
  * 
  * @author Benjamin Mies
  * @author Christian Fehler
  * @version $Id$
  */
 public final class MainWindow implements LanguageChangedListener
 {
 
   /**
    * The {@link Logger} for this class.
    */
   private static final Logger logger = Logger.getLogger ( MainWindow.class );
 
 
   /**
    * The {@link MainWindowForm}.
    */
   private MainWindowForm gui;
 
 
   /**
    * Flag signals if Console Preferences should be saved
    */
   private boolean saveConsolePreferences = true;
 
 
   /**
    * The {@link ModifyStatusChangedListener}.
    */
   private ModifyStatusChangedListener modifyStatusChangedListener;
 
 
   /**
    * List contains the recently used files
    */
   private ArrayList < RecentlyUsedMenuItem > recentlyUsedFiles = new ArrayList < RecentlyUsedMenuItem > ();
 
 
   /**
    * Creates new form {@link MainWindow}.
    */
   public MainWindow ()
   {
     this.gui = new MainWindowForm ( this );
     try
     {
       this.gui.setIconImage ( ImageIO.read ( getClass ().getResource (
           "/de/unisiegen/gtitool/ui/icon/gtitool.png" ) ) ); //$NON-NLS-1$
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
     }
     this.gui.setTitle ( "GTI Tool " + Version.VERSION ); //$NON-NLS-1$
     this.gui.setBounds ( PreferenceManager.getInstance ()
         .getMainWindowBounds () );
     // Setting the default states
     setGeneralStates ( false );
     // Save
     setSaveState ( false );
     // Copy
     // Validate
     this.gui.jMenuItemValidate.setEnabled ( false );
     // EnterWord
     this.gui.jMenuItemEnterWord.setEnabled ( false );
     // Edit Machine
     this.gui.jMenuItemEditMachine.setEnabled ( false );
     // Preferences
     this.gui.jMenuItemPreferences.setEnabled ( true );
     // RecentlyUsed
     this.gui.jMenuRecentlyUsed.setEnabled ( false );
     // Draft for
     this.gui.jMenuDraft.setEnabled ( false );
 
     // Toolbar items
     setToolBarEditItemState ( false );
     setToolBarEnterWordItemState ( false );
 
     this.gui.jButtonNextStep.setEnabled ( false );
     this.gui.jButtonPrevious.setEnabled ( false );
     this.gui.jButtonAutoStep.setEnabled ( false );
     this.gui.jButtonStop.setEnabled ( false );
 
     // Console and table visibility
     this.gui.jCheckBoxMenuItemConsole.setSelected ( PreferenceManager
         .getInstance ().getVisibleConsole () );
     this.gui.jCheckBoxMenuItemTable.setSelected ( PreferenceManager
         .getInstance ().getVisibleTable () );
 
     this.gui.setVisible ( true );
     if ( PreferenceManager.getInstance ().getMainWindowMaximized () )
     {
       this.gui.setExtendedState ( this.gui.getExtendedState ()
           | Frame.MAXIMIZED_BOTH );
     }
     // Language changed listener
     PreferenceManager.getInstance ().addLanguageChangedListener ( this );
 
     for ( File file : PreferenceManager.getInstance ()
         .getRecentlyUsedFilesItem ().getFiles () )
     {
       this.recentlyUsedFiles.add ( new RecentlyUsedMenuItem ( this, file ) );
     }
     organizeRecentlyUsedFilesMenu ();
 
     this.modifyStatusChangedListener = new ModifyStatusChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void modifyStatusChanged ( boolean modified )
       {
         setSaveState ( modified );
       }
     };
   }
 
 
   /**
    * Returns the gui.
    * 
    * @return The gui.
    * @see #gui
    */
   public final MainWindowForm getGui ()
   {
     return this.gui;
   }
 
 
   /**
    * Handle the action event of the about item.
    */
   public final void handleAbout ()
   {
     AboutDialog aboutDialog = new AboutDialog ( this.gui );
     aboutDialog.show ();
   }
 
 
   /**
    * Handle Auto Step Stopped by Exception
    */
   public final void handleAutoStepStopped ()
   {
     this.gui.jButtonAutoStep.setSelected ( false );
   }
 
 
   /**
    * Closes the selected {@link EditorPanel}.
    * 
    * @param panel The {@link EditorPanel} to be saved
    * @return True if the panel is closed, false if the close was canceled.
    */
   public final boolean handleClose ( EditorPanel panel )
   {
     if ( panel.isModified () )
     {
       ConfirmDialog confirmDialog = new ConfirmDialog ( this.gui,
           Messages.getString (
               "MainWindow.CloseModifyMessage", panel.getName () ), Messages //$NON-NLS-1$
               .getString ( "MainWindow.CloseModifyTitle" ), true, true, true ); //$NON-NLS-1$
       confirmDialog.show ();
 
       if ( confirmDialog.isConfirmed () )
       {
         handleSave ( panel );
       }
       else if ( confirmDialog.isCanceled () )
       {
         return false;
       }
     }
 
     this.gui.jGTITabbedPaneMain.removeSelectedEditorPanel ();
     // All editor panels are closed
     if ( this.gui.jGTITabbedPaneMain.getSelectedEditorPanel () == null )
     {
       setGeneralStates ( false );
       setSaveState ( false );
 
       // Toolbar items
       this.gui.jButtonAddState.setEnabled ( false );
       this.gui.jButtonAddTransition.setEnabled ( false );
       this.gui.jButtonFinalState.setEnabled ( false );
       this.gui.jButtonMouse.setEnabled ( false );
       this.gui.jButtonStartState.setEnabled ( false );
       this.gui.jButtonEditDocument.setEnabled ( false );
     }
     return true;
   }
 
 
   /**
    * Handle the close all files event
    */
   public final void handleCloseAll ()
   {
     for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
     {
       // Check if the close was canceled
       if ( !handleClose ( current ) )
       {
         break;
       }
     }
   }
 
 
   /**
    * Handles console state changes.
    */
   public final void handleConsoleStateChanged ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ( panel instanceof MachinePanel ) )
     {
 
       if ( ( PreferenceManager.getInstance ().getVisibleConsole () != this.gui.jCheckBoxMenuItemConsole
           .getState () )
           && this.saveConsolePreferences )
       {
         PreferenceManager.getInstance ().setVisibleConsole (
             this.gui.jCheckBoxMenuItemConsole.getState () );
         MachinePanel machinePanel = ( MachinePanel ) panel;
         machinePanel.setVisibleConsole ( this.gui.jCheckBoxMenuItemConsole
             .getState () );
       }
     }
   }
 
 
   /**
    * Handle Edit Alphabet Action in the Toolbar
    */
   public final void handleEditAlphabet ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     panel.handleToolbarEditDocument ();
   }
 
 
   /**
    * Handle Edit Machine button pressed
    */
   public final void handleEditMachine ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
 
     setToolBarEditItemState ( true );
     setToolBarEnterWordItemState ( false );
     MachinePanel machinePanel = ( MachinePanel ) panel;
     machinePanel.handleEditMachine ();
     machinePanel.setVisibleConsole ( this.gui.jCheckBoxMenuItemConsole
         .getState () );
     this.gui.jCheckBoxMenuItemConsole.setEnabled ( true );
     machinePanel.setWordEnterMode ( false );
     this.gui.jMenuItemEnterWord.setEnabled ( true );
     this.gui.jMenuItemEditMachine.setEnabled ( false );
     this.gui.jMenuItemValidate.setEnabled ( true );
   }
 
 
   /**
    * Handle the action event of the enter word item.
    */
   public final void handleEnterWord ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     int errorCount = 0;
     int warningCount = 0;
     try
     {
       machinePanel.clearValidationMessages ();
       machinePanel.getMachine ().validate ();
     }
     catch ( MachineValidationException exc )
     {
       for ( MachineException error : exc.getMachineException () )
       {
         if ( error.getType ().equals ( ErrorType.ERROR ) )
         {
           machinePanel.addError ( error );
           errorCount++ ;
         }
         else if ( error.getType ().equals ( ErrorType.WARNING ) )
         {
           machinePanel.addWarning ( error );
           warningCount++ ;
         }
       }
     }
     if ( errorCount > 0 )
     {
       String message;
       if ( errorCount == 1 )
       {
         message = Messages.getString ( "MainWindow.ErrorMachineCountOne" ); //$NON-NLS-1$
       }
       else
       {
         message = Messages.getString ( "MainWindow.ErrorMachineCount", String //$NON-NLS-1$
             .valueOf ( errorCount ) );
       }
       InfoDialog infoDialog = new InfoDialog ( this.gui, message, Messages
           .getString ( "MainWindow.ErrorMachine" ) ); //$NON-NLS-1$
       infoDialog.show ();
       return;
     }
     setToolBarEditItemState ( false );
     this.gui.jButtonStart.setEnabled ( true );
     machinePanel.handleEnterWord ();
     this.gui.jCheckBoxMenuItemConsole.setEnabled ( false );
     machinePanel.setWordEnterMode ( true );
     this.gui.jMenuItemEnterWord.setEnabled ( false );
     this.gui.jMenuItemEditMachine.setEnabled ( true );
     this.gui.jMenuItemValidate.setEnabled ( false );
   }
 
 
   /**
    * Handles the {@link Exchange}.
    */
   public final void handleExchange ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel == null )
     {
       ExchangeDialog exchangeDialog = new ExchangeDialog ( this, null, null );
       exchangeDialog.show ();
     }
     else
     {
       panel.handleExchange ();
     }
   }
 
 
   /**
    * Handle the new event.
    */
   public final void handleNew ()
   {
     NewDialog newDialog = new NewDialog ( this.gui );
     // newDialog.setLocationRelativeTo ( window ) ;
     newDialog.show ();
     EditorPanel newEditorPanel = newDialog.getEditorPanel ();
     if ( newEditorPanel != null )
     {
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + newDialog.getEditorPanel ().getFileEnding ();
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + newDialog.getEditorPanel ().getFileEnding ();
       }
 
       newEditorPanel.setName ( name );
       this.gui.jGTITabbedPaneMain.addEditorPanel ( newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( newEditorPanel );
 
       setGeneralStates ( true );
       this.gui.jMenuItemValidate.setEnabled ( true );
 
       // toolbar items
       setToolBarEditItemState ( true );
     }
   }
 
 
   /**
    * Handle the new event with a given {@link Element}.
    * 
    * @param element The {@link Element}.
    */
   public final void handleNew ( Element element )
   {
     DefaultMachineModel model = null;
     try
     {
       model = new DefaultMachineModel ( element, null );
     }
     catch ( TransitionSymbolOnlyOneTimeException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     catch ( StateException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     catch ( SymbolException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     catch ( AlphabetException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     catch ( TransitionException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
     catch ( StoreException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
 
     if ( model != null )
     {
       EditorPanel newEditorPanel = new MachinePanel ( this.gui, model, null );
 
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + newEditorPanel.getFileEnding ();
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + newEditorPanel.getFileEnding ();
       }
 
       newEditorPanel.setName ( name );
       this.gui.jGTITabbedPaneMain.addEditorPanel ( newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( newEditorPanel );
 
       setGeneralStates ( true );
       this.gui.jMenuItemValidate.setEnabled ( true );
 
       // toolbar items
       setToolBarEditItemState ( true );
     }
   }
 
 
   /**
    * Handles the open event.
    */
   public final void handleOpen ()
   {
     PreferenceManager prefmanager = PreferenceManager.getInstance ();
     JFileChooser chooser = new JFileChooser ( prefmanager.getWorkingPath () );
     chooser.setMultiSelectionEnabled ( true );
     chooser.setAcceptAllFileFilterUsed ( false );
 
     // Source files
     FileFilter sourceFileFilter = new FileFilter ()
     {
 
       @Override
       public boolean accept ( File file )
       {
         if ( file.isDirectory () )
         {
           return true;
         }
         for ( String current : Machine.AVAILABLE_MACHINES )
         {
           if ( file.getName ().toLowerCase ().matches (
               ".+\\." + current.toLowerCase () ) ) //$NON-NLS-1$
           {
             return true;
           }
         }
         for ( String current : Grammar.AVAILABLE_GRAMMARS )
         {
           if ( file.getName ().toLowerCase ().matches (
               ".+\\." + current.toLowerCase () ) ) //$NON-NLS-1$
           {
             return true;
           }
         }
         return false;
       }
 
 
       @Override
       public String getDescription ()
       {
         StringBuilder result = new StringBuilder ();
         result.append ( Messages.getString ( "MainWindow.OpenSourceFiles" ) ); //$NON-NLS-1$
         result.append ( " (" ); //$NON-NLS-1$
         for ( int i = 0 ; i < Machine.AVAILABLE_MACHINES.length ; i++ )
         {
           result.append ( "*." ); //$NON-NLS-1$
           result.append ( Machine.AVAILABLE_MACHINES [ i ].toLowerCase () );
           if ( i != Machine.AVAILABLE_MACHINES.length - 1 )
           {
             result.append ( "; " ); //$NON-NLS-1$
           }
         }
         if ( ( Machine.AVAILABLE_MACHINES.length > 0 )
             && ( Grammar.AVAILABLE_GRAMMARS.length > 0 ) )
         {
           result.append ( "; " ); //$NON-NLS-1$
         }
         for ( int i = 0 ; i < Grammar.AVAILABLE_GRAMMARS.length ; i++ )
         {
           result.append ( "*." ); //$NON-NLS-1$
           result.append ( Grammar.AVAILABLE_GRAMMARS [ i ].toLowerCase () );
           if ( i != Grammar.AVAILABLE_GRAMMARS.length - 1 )
           {
             result.append ( "; " ); //$NON-NLS-1$
           }
         }
         result.append ( ")" ); //$NON-NLS-1$
         return result.toString ();
       }
     };
 
     // Machine files
     FileFilter machineFileFilter = new FileFilter ()
     {
 
       @Override
       public boolean accept ( File file )
       {
         if ( file.isDirectory () )
         {
           return true;
         }
         for ( String current : Machine.AVAILABLE_MACHINES )
         {
           if ( file.getName ().toLowerCase ().matches (
               ".+\\." + current.toLowerCase () ) ) //$NON-NLS-1$
           {
             return true;
           }
         }
         return false;
       }
 
 
       @Override
       public String getDescription ()
       {
         StringBuilder result = new StringBuilder ();
         result.append ( Messages
             .getString ( "MainWindow.OpenSourceFilesMachine" ) ); //$NON-NLS-1$
         result.append ( " (" ); //$NON-NLS-1$
         for ( int i = 0 ; i < Machine.AVAILABLE_MACHINES.length ; i++ )
         {
           result.append ( "*." ); //$NON-NLS-1$
           result.append ( Machine.AVAILABLE_MACHINES [ i ].toLowerCase () );
           if ( i != Machine.AVAILABLE_MACHINES.length - 1 )
           {
             result.append ( "; " ); //$NON-NLS-1$
           }
         }
         result.append ( ")" ); //$NON-NLS-1$
         return result.toString ();
       }
     };
 
     // Grammar files
     FileFilter grammarFileFilter = new FileFilter ()
     {
 
       @Override
       public boolean accept ( File file )
       {
         if ( file.isDirectory () )
         {
           return true;
         }
         for ( String current : Grammar.AVAILABLE_GRAMMARS )
         {
           if ( file.getName ().toLowerCase ().matches (
               ".+\\." + current.toLowerCase () ) ) //$NON-NLS-1$
           {
             return true;
           }
         }
         return false;
       }
 
 
       @Override
       public String getDescription ()
       {
         StringBuilder result = new StringBuilder ();
         result.append ( Messages
             .getString ( "MainWindow.OpenSourceFilesGrammar" ) ); //$NON-NLS-1$
         result.append ( " (" ); //$NON-NLS-1$
         for ( int i = 0 ; i < Grammar.AVAILABLE_GRAMMARS.length ; i++ )
         {
           result.append ( "*." ); //$NON-NLS-1$
           result.append ( Grammar.AVAILABLE_GRAMMARS [ i ].toLowerCase () );
           if ( i != Grammar.AVAILABLE_GRAMMARS.length - 1 )
           {
             result.append ( "; " ); //$NON-NLS-1$
           }
         }
         result.append ( ")" ); //$NON-NLS-1$
         return result.toString ();
       }
     };
 
     chooser.addChoosableFileFilter ( sourceFileFilter );
     chooser.addChoosableFileFilter ( machineFileFilter );
     chooser.addChoosableFileFilter ( grammarFileFilter );
     chooser.setFileFilter ( sourceFileFilter );
 
     int n = chooser.showOpenDialog ( this.gui );
     if ( ( n == JFileChooser.CANCEL_OPTION )
         || ( chooser.getSelectedFile () == null ) )
     {
       return;
     }
 
     for ( File file : chooser.getSelectedFiles () )
     {
       openFile ( file, true );
     }
 
     PreferenceManager.getInstance ().setWorkingPath (
         chooser.getSelectedFile ().getParentFile ().getAbsolutePath () );
   }
 
 
   /**
    * Handle the action event of the preferences item.
    */
   public final void handlePreferences ()
   {
     PreferencesDialog preferencesDialog = new PreferencesDialog ( this.gui );
     preferencesDialog.show ();
   }
 
 
   /**
    * Handles the quit event.
    */
   public final void handleQuit ()
   {
     // Active file
     File activeFile = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel () == null ? null
         : this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ().getFile ();
 
     // Opened file
     ArrayList < File > openedFiles = new ArrayList < File > ();
     for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
     {
       if ( current.getFile () != null )
       {
         openedFiles.add ( current.getFile () );
       }
     }
     // Close the tabs
     for ( int i = this.gui.jGTITabbedPaneMain.getComponentCount () - 1 ; i >= 0 ; i-- )
     {
       EditorPanel current = this.gui.jGTITabbedPaneMain.getEditorPanel ( i );
       if ( current.isModified () )
       {
         this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( current );
        String fileName = current.getFile () == null ? this.gui.jGTITabbedPaneMain
            .getEditorPanelTitle ( current )
            : current.getFile ().getName ();
 
        ConfirmDialog confirmDialog = new ConfirmDialog ( this.gui, Messages
            .getString ( "MainWindow.CloseModifyMessage", fileName ), Messages //$NON-NLS-1$
            .getString ( "MainWindow.CloseModifyTitle" ), true, true, true ); //$NON-NLS-1$
         confirmDialog.show ();
 
         if ( confirmDialog.isConfirmed () )
         {
           File file = current.handleSave ();
           if ( file != null )
           {
             this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( current, file
                 .getName () );
           }
         }
         else if ( confirmDialog.isCanceled () )
         {
           return;
         }
       }
       this.gui.jGTITabbedPaneMain.removeEditorPanel ( current );
     }
     PreferenceManager.getInstance ().setMainWindowPreferences ( this.gui );
     PreferenceManager.getInstance ().setOpenedFilesItem (
         new OpenedFilesItem ( openedFiles, activeFile ) );
 
     ArrayList < File > files = new ArrayList < File > ();
     for ( RecentlyUsedMenuItem item : this.recentlyUsedFiles )
     {
       files.add ( item.getFile () );
     }
     PreferenceManager.getInstance ().setRecentlyUsedFilesItem (
         new RecentlyUsedFilesItem ( files ) );
 
     // System exit
     System.exit ( 0 );
   }
 
 
   /**
    * Handle redo button pressed
    */
   public final void handleRedo ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel != null )
     {
       panel.handleRedo ();
     }
   }
 
 
   /**
    * Handle the save file event
    * 
    * @param panel The {@link EditorPanel} to be saved
    */
   public final void handleSave ( EditorPanel panel )
   {
     File file = panel.handleSave ();
     if ( file != null )
     {
       RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
       this.recentlyUsedFiles.remove ( item );
       this.recentlyUsedFiles.add ( 0, item );
       organizeRecentlyUsedFilesMenu ();
 
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( ( !current.equals ( this.gui.jGTITabbedPaneMain
             .getSelectedEditorPanel () ) && file.equals ( current.getFile () ) ) )
         {
           this.gui.jGTITabbedPaneMain.removeEditorPanel ( current );
         }
       }
       this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( panel, file.getName () );
     }
   }
 
 
   /**
    * Handle the save all files event
    */
   public final void handleSaveAll ()
   {
     EditorPanel active = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
     {
       this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( current );
       handleSave ( current );
     }
     this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( active );
   }
 
 
   /**
    * Handle the save file as event
    */
   public final void handleSaveAs ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     File file = panel.handleSaveAs ();
     if ( file != null )
     {
       RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
       this.recentlyUsedFiles.remove ( item );
       this.recentlyUsedFiles.add ( 0, item );
       organizeRecentlyUsedFilesMenu ();
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( ( !current.equals ( this.gui.jGTITabbedPaneMain
             .getSelectedEditorPanel () ) && file.equals ( current.getFile () ) ) )
         {
           this.gui.jGTITabbedPaneMain.removeEditorPanel ( current );
         }
       }
       this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( panel, file.getName () );
     }
   }
 
 
   /**
    * Handle TabbedPane state changed event
    */
   public final void handleTabbedPaneStateChanged ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel != null )
     {
       activateMachineButtons ( panel instanceof MachinePanel );
       activateGrammarButtons ( panel instanceof GrammarPanel );
       if ( panel instanceof MachinePanel )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
         this.gui.jCheckBoxMenuItemConsole.setEnabled ( !machinePanel
             .isWordEnterMode () );
         this.saveConsolePreferences = false;
         this.saveConsolePreferences = true;
         machinePanel.setVisibleConsole ( this.gui.jCheckBoxMenuItemConsole
             .getState ()
             && !machinePanel.isWordEnterMode () );
         machinePanel.setVisibleTable ( this.gui.jCheckBoxMenuItemTable
             .getState () );
         setToolBarEditItemState ( !machinePanel.isWordEnterMode () );
         setToolBarEnterWordItemState ( machinePanel.isWordEnterMode () );
         this.gui.jMenuItemEditMachine.setEnabled ( machinePanel
             .isWordEnterMode () );
         this.gui.jMenuItemValidate.setEnabled ( !machinePanel
             .isWordEnterMode () );
         this.gui.jMenuItemEnterWord.setEnabled ( !machinePanel
             .isWordEnterMode () );
 
         // Set the status of the word navigation icons
         this.gui.jButtonStart.setEnabled ( machinePanel.isWordEnterMode ()
             && !machinePanel.isWordNavigation () );
         this.gui.jButtonPrevious.setEnabled ( machinePanel.isWordNavigation () );
         this.gui.jButtonNextStep.setEnabled ( machinePanel.isWordNavigation () );
         this.gui.jButtonAutoStep.setEnabled ( machinePanel.isWordNavigation () );
         this.gui.jButtonStop.setEnabled ( machinePanel.isWordNavigation () );
 
       }
       else
       {
         this.gui.jCheckBoxMenuItemConsole.setEnabled ( false );
         this.saveConsolePreferences = false;
         this.gui.jCheckBoxMenuItemConsole.setState ( false );
         this.saveConsolePreferences = true;
         this.gui.jCheckBoxMenuItemTable.setState ( false );
         setToolBarEditItemState ( false );
         setToolBarEnterWordItemState ( false );
       }
       // Undo
       this.gui.jMenuItemUndo.setEnabled ( panel.isUndoAble () );
       this.gui.jButtonUndo.setEnabled ( panel.isUndoAble () );
       // Redo
       this.gui.jMenuItemRedo.setEnabled ( panel.isRedoAble () );
       this.gui.jButtonRedo.setEnabled ( panel.isRedoAble () );
     }
     // Save status
     setSaveState ();
   }
 
 
   /**
    * Show or hide the buttons needed in the GrammarPanel
    * 
    * @param state the visible state of the buttons
    */
   private void activateGrammarButtons ( boolean state )
   {
     // TODO implement me
 
   }
 
 
   /**
    * Show or hide the buttons needed in the MachinePanel
    * 
    * @param state the visible state of the buttons
    */
   private void activateMachineButtons ( boolean state )
   {
     this.gui.jSeparatorMain1.setVisible ( state );
     this.gui.jButtonMouse.setVisible ( state );
     this.gui.jButtonAddState.setVisible ( state );
     this.gui.jButtonStartState.setVisible ( state );
     this.gui.jButtonFinalState.setVisible ( state );
     this.gui.jButtonAddTransition.setVisible ( state );
     this.gui.jButtonEditDocument.setVisible ( state );
 
     this.gui.jSeparatorMain2.setVisible ( state );
     this.gui.jButtonStart.setVisible ( state );
     this.gui.jButtonPrevious.setVisible ( state );
     this.gui.jButtonNextStep.setVisible ( state );
     this.gui.jButtonAutoStep.setVisible ( state );
     this.gui.jButtonStop.setVisible ( state );
 
   }
 
 
   /**
    * Handles table state changes.
    */
   public final void handleTableStateChanged ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ( panel instanceof MachinePanel ) )
     {
       MachinePanel machinePanel = ( MachinePanel ) panel;
 
       if ( PreferenceManager.getInstance ().getVisibleTable () != this.gui.jCheckBoxMenuItemTable
           .getState () )
       {
         PreferenceManager.getInstance ().setVisibleTable (
             this.gui.jCheckBoxMenuItemTable.getState () );
         machinePanel.setVisibleTable ( this.gui.jCheckBoxMenuItemTable
             .getState () );
       }
     }
 
   }
 
 
   /**
    * Handle Toolbar Add State button value changed
    * 
    * @param state The new State of the Add State Toolbar button
    */
   public final void handleToolbarAddState ( boolean state )
   {
     for ( EditorPanel panel : this.gui.jGTITabbedPaneMain )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarAddState ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar End button value changed
    * 
    * @param state The new State of the End Toolbar button
    */
   public final void handleToolbarEnd ( boolean state )
   {
     for ( EditorPanel panel : this.gui.jGTITabbedPaneMain )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarEnd ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Mouse button value changed
    * 
    * @param state The new State of the Mouse Toolbar button
    */
   public final void handleToolbarMouse ( boolean state )
   {
     for ( EditorPanel panel : this.gui.jGTITabbedPaneMain )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarMouse ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Start button value changed
    * 
    * @param state The new State of the Start Toolbar button
    */
   public final void handleToolbarStart ( boolean state )
   {
     for ( EditorPanel panel : this.gui.jGTITabbedPaneMain )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarStart ( state );
       }
     }
   }
 
 
   /**
    * Handle Toolbar Transition button value changed
    * 
    * @param state The new State of the Transition Toolbar button
    */
   public final void handleToolbarTransition ( boolean state )
   {
     for ( EditorPanel panel : this.gui.jGTITabbedPaneMain )
     {
       if ( ( panel instanceof MachinePanel ) )
       {
         MachinePanel machinePanel = ( MachinePanel ) panel;
 
         machinePanel.handleToolbarTransition ( state );
       }
     }
   }
 
 
   /**
    * Handle undo button pressed
    */
   public final void handleUndo ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel != null )
     {
       panel.handleUndo ();
     }
   }
 
 
   /**
    * Handle the action event of the enter word item.
    */
   public final void handleValidate ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     int errorCount = 0;
     int warningCount = 0;
     try
     {
       machinePanel.clearValidationMessages ();
       machinePanel.getMachine ().validate ();
     }
     catch ( MachineValidationException e )
     {
       for ( MachineException error : e.getMachineException () )
       {
         if ( error.getType ().equals ( ErrorType.ERROR ) )
         {
           machinePanel.addError ( error );
           errorCount++ ;
         }
         else if ( error.getType ().equals ( ErrorType.WARNING ) )
         {
           machinePanel.addWarning ( error );
           warningCount++ ;
         }
       }
     }
     String message;
     if ( errorCount == 1 )
     {
       message = Messages.getString ( "MainWindow.ErrorMachineCountOne" ); //$NON-NLS-1$
     }
     else
     {
       message = Messages.getString ( "MainWindow.ErrorMachineCount", String //$NON-NLS-1$
           .valueOf ( errorCount ) );
     }
     InfoDialog infoDialog = new InfoDialog ( this.gui, message, Messages
         .getString ( "MainWindow.ErrorMachine" ) ); //$NON-NLS-1$
     infoDialog.show ();
   }
 
 
   /**
    * Handle Auto Step Action in the Word Enter Mode
    * 
    * @param event
    */
   public final void handleWordAutoStep ( ItemEvent event )
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     machinePanel.handleWordAutoStep ( event );
   }
 
 
   /**
    * Handle Next Step Action in the Word Enter Mode
    */
   public final void handleWordNextStep ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     machinePanel.handleWordNextStep ();
   }
 
 
   /**
    * Handle Previous Step Action in the Word Enter Mode
    */
   public final void handleWordPreviousStep ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     machinePanel.handleWordPreviousStep ();
   }
 
 
   /**
    * Handle Start Action in the Word Enter Mode
    */
   public final void handleWordStart ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     int errorCount = 0;
     int warningCount = 0;
     try
     {
       machinePanel.clearValidationMessages ();
       machinePanel.getMachine ().validate ();
     }
     catch ( MachineValidationException e )
     {
       for ( MachineException error : e.getMachineException () )
       {
         if ( error.getType ().equals ( ErrorType.ERROR ) )
         {
           machinePanel.addError ( error );
           errorCount++ ;
         }
         else if ( error.getType ().equals ( ErrorType.WARNING ) )
         {
           machinePanel.addWarning ( error );
           warningCount++ ;
         }
       }
     }
     if ( errorCount > 0 )
     {
       String message;
       if ( errorCount == 1 )
       {
         message = Messages.getString ( "MainWindow.ErrorMachineCountOne" ); //$NON-NLS-1$
       }
       else
       {
         message = Messages.getString ( "MainWindow.ErrorMachineCount", String //$NON-NLS-1$
             .valueOf ( errorCount ) );
       }
       InfoDialog infoDialog = new InfoDialog ( this.gui, message, Messages
           .getString ( "MainWindow.ErrorMachine" ) ); //$NON-NLS-1$
       infoDialog.show ();
       return;
     }
 
     if ( machinePanel.handleWordStart () )
     {
       this.gui.jButtonStart.setEnabled ( false );
       this.gui.jButtonNextStep.setEnabled ( true );
       this.gui.jButtonPrevious.setEnabled ( true );
       this.gui.jButtonAutoStep.setEnabled ( true );
       this.gui.jButtonStop.setEnabled ( true );
     }
   }
 
 
   /**
    * Handle Stop Action in the Word Enter Mode
    */
   public final void handleWordStop ()
   {
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( ! ( panel instanceof MachinePanel ) )
     {
       throw new IllegalArgumentException ( "not a machine panel" ); //$NON-NLS-1$
     }
     MachinePanel machinePanel = ( MachinePanel ) panel;
 
     this.gui.jButtonStart.setEnabled ( true );
     this.gui.jButtonNextStep.setEnabled ( false );
     this.gui.jButtonPrevious.setEnabled ( false );
     this.gui.jButtonAutoStep.setEnabled ( false );
     this.gui.jButtonStop.setEnabled ( false );
     machinePanel.handleWordStop ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LanguageChangedListener#languageChanged()
    */
   public final void languageChanged ()
   {
     // File
     MainWindow.this.gui.jMenuFile.setText ( Messages
         .getString ( "MainWindow.File" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuFile.setMnemonic ( Messages.getString (
         "MainWindow.FileMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // New
     MainWindow.this.gui.jMenuItemNew.setText ( Messages
         .getString ( "MainWindow.New" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemNew.setMnemonic ( Messages.getString (
         "MainWindow.NewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.jButtonNew.setToolTipText ( Messages
         .getString ( "MainWindow.NewToolTip" ) ); //$NON-NLS-1$
     // Open
     MainWindow.this.gui.jMenuItemOpen.setText ( Messages
         .getString ( "MainWindow.Open" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemOpen.setMnemonic ( Messages.getString (
         "MainWindow.OpenMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.jButtonOpen.setToolTipText ( Messages
         .getString ( "MainWindow.OpenToolTip" ) ); //$NON-NLS-1$
     // Close
     MainWindow.this.gui.jMenuItemClose.setText ( Messages
         .getString ( "MainWindow.Close" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemClose.setMnemonic ( Messages.getString (
         "MainWindow.CloseMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // CloseAll
     MainWindow.this.gui.jMenuItemCloseAll.setText ( Messages
         .getString ( "MainWindow.CloseAll" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemCloseAll.setMnemonic ( Messages.getString (
         "MainWindow.CloseAllMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Save
     MainWindow.this.gui.jMenuItemSave.setText ( Messages
         .getString ( "MainWindow.Save" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemSave.setMnemonic ( Messages.getString (
         "MainWindow.SaveMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.jButtonSave.setToolTipText ( Messages
         .getString ( "MainWindow.SaveToolTip" ) ); //$NON-NLS-1$
     // SaveAs
     MainWindow.this.gui.jMenuItemSaveAs.setText ( Messages
         .getString ( "MainWindow.SaveAs" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemSaveAs.setMnemonic ( Messages.getString (
         "MainWindow.SaveAsMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.jButtonSaveAs.setToolTipText ( Messages
         .getString ( "MainWindow.SaveAsToolTip" ) ); //$NON-NLS-1$
     // SaveAll
     MainWindow.this.gui.jMenuItemSaveAll.setText ( Messages
         .getString ( "MainWindow.SaveAll" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemSaveAll.setMnemonic ( Messages.getString (
         "MainWindow.SaveAllMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Draft for
     MainWindow.this.gui.jMenuDraft.setText ( Messages
         .getString ( "MainWindow.DraftFor" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuDraft.setMnemonic ( Messages.getString (
         "MainWindow.DraftForMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemDFA.setText ( Messages
         .getString ( "MainWindow.DFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemNFA.setText ( Messages
         .getString ( "MainWindow.NFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemENFA.setText ( Messages
         .getString ( "MainWindow.ENFA" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemPDA.setText ( Messages
         .getString ( "MainWindow.PDA" ) ); //$NON-NLS-1$
     // RecentlyUsed
     MainWindow.this.gui.jMenuRecentlyUsed.setText ( Messages
         .getString ( "MainWindow.RecentlyUsed" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuRecentlyUsed.setMnemonic ( Messages.getString (
         "MainWindow.RecentlyUsedMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Quit
     MainWindow.this.gui.jMenuItemQuit.setText ( Messages
         .getString ( "MainWindow.Quit" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemQuit.setMnemonic ( Messages.getString (
         "MainWindow.QuitMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Edit
     MainWindow.this.gui.jMenuEdit.setText ( Messages
         .getString ( "MainWindow.Edit" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuEdit.setMnemonic ( Messages.getString (
         "MainWindow.EditMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Cut
     MainWindow.this.gui.jMenuItemCut.setText ( Messages
         .getString ( "MainWindow.Cut" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemCut.setMnemonic ( Messages.getString (
         "MainWindow.CutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Copy
     MainWindow.this.gui.jMenuItemCopy.setText ( Messages
         .getString ( "MainWindow.Copy" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemCopy.setMnemonic ( Messages.getString (
         "MainWindow.CopyMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Paste
     MainWindow.this.gui.jMenuItemPaste.setText ( Messages
         .getString ( "MainWindow.Paste" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemPaste.setMnemonic ( Messages.getString (
         "MainWindow.PasteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Undo
     MainWindow.this.gui.jMenuItemUndo.setText ( Messages
         .getString ( "MainWindow.Undo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemUndo.setMnemonic ( Messages.getString (
         "MainWindow.UndoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Redo
     MainWindow.this.gui.jMenuItemRedo.setText ( Messages
         .getString ( "MainWindow.Redo" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemRedo.setMnemonic ( Messages.getString (
         "MainWindow.RedoMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Preferences
     MainWindow.this.gui.jMenuItemPreferences.setText ( Messages
         .getString ( "MainWindow.Preferences" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemPreferences.setMnemonic ( Messages.getString (
         "MainWindow.PreferencesMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // View
     MainWindow.this.gui.jMenuView.setText ( Messages
         .getString ( "MainWindow.View" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuView.setMnemonic ( Messages.getString (
         "MainWindow.ViewMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Console
     MainWindow.this.gui.jCheckBoxMenuItemConsole.setText ( Messages
         .getString ( "MainWindow.Console" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jCheckBoxMenuItemConsole.setMnemonic ( Messages
         .getString ( "MainWindow.ConsoleMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Table
     MainWindow.this.gui.jCheckBoxMenuItemTable.setText ( Messages
         .getString ( "MainWindow.Table" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jCheckBoxMenuItemTable.setMnemonic ( Messages
         .getString ( "MainWindow.TableMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Execute
     MainWindow.this.gui.jMenuExecute.setText ( Messages
         .getString ( "MainWindow.Execute" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuExecute.setMnemonic ( Messages.getString (
         "MainWindow.ExecuteMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Validate
     MainWindow.this.gui.jMenuItemValidate.setText ( Messages
         .getString ( "MainWindow.Validate" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemValidate.setMnemonic ( Messages.getString (
         "MainWindow.ValidateMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // EnterWord
     MainWindow.this.gui.jMenuItemEnterWord.setText ( Messages
         .getString ( "MainWindow.EnterWord" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemEnterWord.setMnemonic ( Messages.getString (
         "MainWindow.EnterWordMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // EnterWord
     MainWindow.this.gui.jMenuItemEditMachine.setText ( Messages
         .getString ( "MainWindow.EditMachine" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemEditMachine.setMnemonic ( Messages.getString (
         "MainWindow.EditMachineMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Extras
     MainWindow.this.gui.jMenuExtras.setText ( Messages
         .getString ( "MainWindow.Extras" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuExtras.setMnemonic ( Messages.getString (
         "MainWindow.ExtrasMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Exchange
     MainWindow.this.gui.jMenuItemExchange.setText ( Messages
         .getString ( "MainWindow.Exchange" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemExchange.setMnemonic ( Messages.getString (
         "MainWindow.ExchangeMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Help
     MainWindow.this.gui.jMenuHelp.setText ( Messages
         .getString ( "MainWindow.Help" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuHelp.setMnemonic ( Messages.getString (
         "MainWindow.HelpMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // About
     MainWindow.this.gui.jMenuItemAbout.setText ( Messages
         .getString ( "MainWindow.About" ) ); //$NON-NLS-1$
     MainWindow.this.gui.jMenuItemAbout.setMnemonic ( Messages.getString (
         "MainWindow.AboutMnemonic" ).charAt ( 0 ) ); //$NON-NLS-1$
     // Mouse
     MainWindow.this.gui.jButtonMouse.setToolTipText ( Messages
         .getString ( "MachinePanel.Mouse" ) ); //$NON-NLS-1$
     // Add state
     MainWindow.this.gui.jButtonAddState.setToolTipText ( Messages
         .getString ( "MachinePanel.AddState" ) ); //$NON-NLS-1$
     // Add transition
     MainWindow.this.gui.jButtonAddTransition.setToolTipText ( Messages
         .getString ( "MachinePanel.AddTransition" ) ); //$NON-NLS-1$
     // Start state
     MainWindow.this.gui.jButtonStartState.setToolTipText ( Messages
         .getString ( "MachinePanel.StartState" ) ); //$NON-NLS-1$
     // Final state
     MainWindow.this.gui.jButtonFinalState.setToolTipText ( Messages
         .getString ( "MachinePanel.FinalState" ) ); //$NON-NLS-1$
     // Edit Document
     MainWindow.this.gui.jButtonEditDocument.setToolTipText ( Messages
         .getString ( "MachinePanel.EditDocument" ) ); //$NON-NLS-1$
     // Previous Step
     MainWindow.this.gui.jButtonPrevious.setToolTipText ( Messages
         .getString ( "MachinePanel.WordModePreviousStep" ) ); //$NON-NLS-1$
     // Start Word
     MainWindow.this.gui.jButtonStart.setToolTipText ( Messages
         .getString ( "MachinePanel.WordModeStart" ) ); //$NON-NLS-1$
     // Next Step
     MainWindow.this.gui.jButtonNextStep.setToolTipText ( Messages
         .getString ( "MachinePanel.WordModeNextStep" ) ); //$NON-NLS-1$
     // Auto Step
     MainWindow.this.gui.jButtonAutoStep.setToolTipText ( Messages
         .getString ( "MachinePanel.WordModeAutoStep" ) ); //$NON-NLS-1$
     // Stop Word
     MainWindow.this.gui.jButtonStop.setToolTipText ( Messages
         .getString ( "MachinePanel.WordModeStop" ) ); //$NON-NLS-1$
   }
 
 
   /**
    * Try to open the given file
    * 
    * @param file The file to open
    * @param addToRecentlyUsed Flag signals if file should be added to recently
    *          used files
    */
   public final void openFile ( File file, boolean addToRecentlyUsed )
   {
     // check if we already have an editor panel for the file
     for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
     {
       if ( file.equals ( current.getFile () ) )
       {
         this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( current );
 
         // reorganize recently used files
         if ( addToRecentlyUsed )
         {
           RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
           this.recentlyUsedFiles.remove ( item );
           this.recentlyUsedFiles.add ( 0, item );
           if ( this.recentlyUsedFiles.size () > 10 )
           {
             this.recentlyUsedFiles.remove ( 10 );
           }
           organizeRecentlyUsedFilesMenu ();
         }
 
         return;
       }
     }
     try
     {
       DefaultMachineModel model = ( DefaultMachineModel ) Storage
           .getInstance ().load ( file );
       EditorPanel newEditorPanel = new MachinePanel ( this.gui, model, file );
 
       this.gui.jGTITabbedPaneMain.addEditorPanel ( newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( newEditorPanel );
       this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( newEditorPanel, file
           .getName () );
       setGeneralStates ( true );
       this.gui.jMenuItemValidate.setEnabled ( true );
 
       // toolbar items
       setToolBarEditItemState ( true );
 
       // reorganize recently used files
       if ( addToRecentlyUsed )
       {
         RecentlyUsedMenuItem item = new RecentlyUsedMenuItem ( this, file );
         this.recentlyUsedFiles.remove ( item );
         this.recentlyUsedFiles.add ( 0, item );
         if ( this.recentlyUsedFiles.size () > 10 )
         {
           this.recentlyUsedFiles.remove ( 10 );
         }
         organizeRecentlyUsedFilesMenu ();
       }
     }
     catch ( StoreException exc )
     {
       InfoDialog infoDialog = new InfoDialog ( this.gui, exc.getMessage (),
           Messages.getString ( "MainWindow.ErrorLoad" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     PreferenceManager.getInstance ().setWorkingPath (
         file.getParentFile ().getAbsolutePath () );
   }
 
 
   /**
    * Organize the recently used files in the menu
    */
   private final void organizeRecentlyUsedFilesMenu ()
   {
     ArrayList < RecentlyUsedMenuItem > notExistingFiles = new ArrayList < RecentlyUsedMenuItem > ();
 
     this.gui.jMenuRecentlyUsed.removeAll ();
 
     for ( RecentlyUsedMenuItem item : this.recentlyUsedFiles )
     {
       if ( item.getFile ().exists () )
       {
         this.gui.jMenuRecentlyUsed.add ( item );
       }
       else
       {
         notExistingFiles.add ( item );
       }
     }
 
     this.recentlyUsedFiles.removeAll ( notExistingFiles );
 
     if ( this.recentlyUsedFiles.size () > 0 )
     {
       this.gui.jMenuRecentlyUsed.setEnabled ( true );
     }
     else
     {
       this.gui.jMenuRecentlyUsed.setEnabled ( false );
     }
   }
 
 
   /**
    * Open all files which was open at last session
    */
   public final void restoreOpenFiles ()
   {
     for ( File file : PreferenceManager.getInstance ().getOpenedFilesItem ()
         .getFiles () )
     {
       openFile ( file, false );
     }
     File activeFile = PreferenceManager.getInstance ().getOpenedFilesItem ()
         .getActiveFile ();
     if ( activeFile != null )
     {
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( current.getFile ().getAbsolutePath ().equals (
             activeFile.getAbsolutePath () ) )
         {
           this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( current );
           break;
         }
       }
     }
   }
 
 
   /**
    * Sets general states for items and buttons.
    * 
    * @param state The new state.
    */
   private final void setGeneralStates ( boolean state )
   {
     // SaveAs
     this.gui.jButtonSaveAs.setEnabled ( state );
     this.gui.jMenuItemSaveAs.setEnabled ( state );
     // SaveAll
     this.gui.jMenuItemSaveAll.setEnabled ( state );
     // Close
     this.gui.jMenuItemClose.setEnabled ( state );
     // Enter word
     this.gui.jMenuItemEnterWord.setEnabled ( state );
     // Cut
     // this.gui.jMenuItemCut.setEnabled ( pState );
     this.gui.jMenuItemCut.setVisible ( false );
     // Copy
     // this.gui.jMenuItemCopy.setEnabled ( pState );
     this.gui.jMenuItemCopy.setVisible ( false );
     // Paste
     // this.gui.jMenuItemPaste.setEnabled ( pState );
     this.gui.jMenuItemPaste.setVisible ( false );
     // Undo
     this.gui.jMenuItemUndo.setEnabled ( false );
     this.gui.jButtonUndo.setEnabled ( false );
     // Redo
     this.gui.jMenuItemRedo.setEnabled ( false );
     this.gui.jButtonRedo.setEnabled ( false );
     // Separator
     this.gui.jSeparatorEdit1.setVisible ( false );
     this.gui.jSeparatorEdit2.setVisible ( false );
     // Edit document
     this.gui.jMenuDraft.setEnabled ( state );
   }
 
 
   /**
    * Sets the state of the save button and item.
    */
   private final void setSaveState ()
   {
     boolean state = false;
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel != null )
     {
       state = panel.isModified ();
     }
     setSaveState ( state );
   }
 
 
   /**
    * Sets the state of the save button and item.
    * 
    * @param state The new state of the save button.
    */
   private final void setSaveState ( boolean state )
   {
     logger.debug ( "set save status to \"" + state + "\"" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     EditorPanel panel = this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ();
     if ( panel != null )
     {
       if ( state )
       {
         this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( panel, "*" //$NON-NLS-1$
             + panel.getName () );
       }
       else
       {
         this.gui.jGTITabbedPaneMain.setEditorPanelTitle ( panel, panel
             .getName () );
       }
     }
     else if ( state == true )
     {
       throw new IllegalArgumentException ( "save button error" ); //$NON-NLS-1$
     }
 
     this.gui.jButtonSave.setEnabled ( state );
     this.gui.jMenuItemSave.setEnabled ( state );
   }
 
 
   /**
    * Set the state of the edit machine toolbar items
    * 
    * @param state the new state
    */
   private final void setToolBarEditItemState ( boolean state )
   {
     this.gui.jButtonAddState.setEnabled ( state );
     this.gui.jButtonAddTransition.setEnabled ( state );
     this.gui.jButtonFinalState.setEnabled ( state );
     this.gui.jButtonMouse.setEnabled ( state );
     this.gui.jButtonStartState.setEnabled ( state );
     this.gui.jButtonEditDocument.setEnabled ( state );
   }
 
 
   /**
    * Set the state of the enter word toolbar items
    * 
    * @param state the new state
    */
   private final void setToolBarEnterWordItemState ( boolean state )
   {
     this.gui.jButtonPrevious.setEnabled ( state );
     this.gui.jButtonStart.setEnabled ( state );
     this.gui.jButtonNextStep.setEnabled ( state );
     this.gui.jButtonAutoStep.setEnabled ( state );
     this.gui.jButtonStop.setEnabled ( state );
   }
 
 
   /**
    * Use the active Editor Panel as draf for a new file
    * 
    * @param type Type of the new file
    */
   public final void handleDraftFor ( MachineType type )
   {
     try
     {
       DefaultMachineModel model = new DefaultMachineModel (
           this.gui.jGTITabbedPaneMain.getSelectedEditorPanel ().getModel ()
               .getElement (), type.toString () );
       EditorPanel newEditorPanel = new MachinePanel ( this.gui, model, null );
 
       TreeSet < String > nameList = new TreeSet < String > ();
       int count = 0;
       for ( EditorPanel current : this.gui.jGTITabbedPaneMain )
       {
         if ( current.getFile () == null )
         {
           nameList.add ( current.getName () );
           count++ ;
         }
       }
 
       String name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
           + "." + type.toString ().toLowerCase (); //$NON-NLS-1$
       while ( nameList.contains ( name ) )
       {
         count++ ;
         name = Messages.getString ( "MainWindow.NewFile" ) + count //$NON-NLS-1$
             + "." + type.toString ().toLowerCase (); //$NON-NLS-1$
       }
 
       newEditorPanel.setName ( name );
       this.gui.jGTITabbedPaneMain.addEditorPanel ( newEditorPanel );
       newEditorPanel
           .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
       this.gui.jGTITabbedPaneMain.setSelectedEditorPanel ( newEditorPanel );
       setGeneralStates ( true );
       this.gui.jMenuItemValidate.setEnabled ( true );
 
       // toolbar items
       setToolBarEditItemState ( true );
 
     }
     catch ( StoreException exc )
     {
       InfoDialog infoDialog = new InfoDialog ( this.gui, exc.getMessage (),
           Messages.getString ( "MainWindow.ErrorLoad" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     catch ( TransitionSymbolOnlyOneTimeException e )
     {
       // Do nothing
     }
     catch ( StateException e )
     {
       // Dot nothing
     }
     catch ( SymbolException e )
     {
       // Do nothing
     }
     catch ( AlphabetException e )
     {
       // Do nothing
     }
     catch ( TransitionException e )
     {
       // Do nothing
     }
 
   }
 }
