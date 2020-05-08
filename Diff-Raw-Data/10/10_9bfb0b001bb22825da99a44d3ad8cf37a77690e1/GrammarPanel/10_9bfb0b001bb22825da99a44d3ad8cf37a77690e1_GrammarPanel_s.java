 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Component;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.event.EventListenerList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 import de.unisiegen.gtitool.core.entities.Production;
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.exceptions.grammar.GrammarException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.grammars.Grammar;
 import de.unisiegen.gtitool.core.preferences.listener.LanguageChangedListener;
 import de.unisiegen.gtitool.core.storage.Modifyable;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.EditorPanel;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.exchange.Exchange;
 import de.unisiegen.gtitool.ui.model.ConsoleColumnModel;
 import de.unisiegen.gtitool.ui.model.DefaultGrammarModel;
 import de.unisiegen.gtitool.ui.model.DefaultModel;
 import de.unisiegen.gtitool.ui.model.GrammarColumnModel;
 import de.unisiegen.gtitool.ui.model.GrammarConsoleTableModel;
 import de.unisiegen.gtitool.ui.model.MachineConsoleTableModel;
 import de.unisiegen.gtitool.ui.netbeans.GrammarPanelForm;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 import de.unisiegen.gtitool.ui.netbeans.helperclasses.EditorPanelForm;
 import de.unisiegen.gtitool.ui.popup.ProductionPopupMenu;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.storage.Storage;
 import de.unisiegen.gtitool.ui.swing.JGTIList;
 import de.unisiegen.gtitool.ui.swing.JGTITable;
 import de.unisiegen.gtitool.ui.swing.dnd.JGTITableModelRows;
 import de.unisiegen.gtitool.ui.swing.dnd.JGTITableTransferHandler;
 
 
 /**
  * @author Benjamin Mies
  * @version $Id$
  */
 public class GrammarPanel implements EditorPanel
 {
 
   /**
    * The {@link GrammarPanelForm}.
    */
   private GrammarPanelForm gui;
 
 
   /**
    * The {@link DefaultGrammarModel}.
    */
   private DefaultGrammarModel model;
 
 
   /**
    * Flag that indicates if the console divider location should be stored.
    */
   private boolean setDividerLocationConsole = true;
 
 
   /**
    * The {@link Grammar}.
    */
   private Grammar grammar;
 
 
   /**
    * The name of this {@link MachinePanel}.
    */
   private String name = null;
 
 
   /**
    * The {@link File} for this {@link MachinePanel}.
    */
   private File file;
 
 
   /**
    * The {@link EventListenerList}.
    */
   private EventListenerList listenerList = new EventListenerList ();
 
 
   /**
    * The {@link MainWindowForm}
    */
   private MainWindowForm mainWindowForm;
 
 
   /**
    * The {@link ModifyStatusChangedListener}.
    */
   private ModifyStatusChangedListener modifyStatusChangedListener;
 
 
   /**
    * The {@link MachineConsoleTableModel} for the warning table.
    */
   private GrammarConsoleTableModel warningTableModel;
 
 
   /**
    * The {@link GrammarConsoleTableModel} for the error table.
    */
   private GrammarConsoleTableModel errorTableModel;
 
 
   /**
    * Allocates a new {@link GrammarPanel}
    * 
    * @param mainWindowForm The {@link MainWindowForm}.
    * @param model The {@link DefaultGrammarModel}.
    * @param file The {@link File}
    */
   public GrammarPanel ( MainWindowForm mainWindowForm,
       DefaultGrammarModel model, File file )
   {
     this.mainWindowForm = mainWindowForm;
     this.model = model;
     this.file = file;
     this.gui = new GrammarPanelForm ();
     this.gui.setGrammarPanel ( this );
     this.grammar = this.model.getGrammar ();
     initialize ();
 
     this.gui.jGTISplitPaneConsole.setDividerLocation ( PreferenceManager
         .getInstance ().getDividerLocationConsole () );
     setVisibleConsole ( this.mainWindowForm.jCheckBoxMenuItemConsole
         .getState () );
     this.gui.jGTISplitPaneConsole.addPropertyChangeListener (
         JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void propertyChange ( PropertyChangeEvent event )
           {
             if ( GrammarPanel.this.setDividerLocationConsole )
             {
               PreferenceManager.getInstance ().setDividerLocationConsole (
                   ( ( Integer ) event.getNewValue () ).intValue () );
             }
             GrammarPanel.this.setDividerLocationConsole = true;
           }
         } );
 
     // Language changed listener
     PreferenceManager.getInstance ().addLanguageChangedListener ( this );
 
     this.gui.jGTITableGrammar.setDragEnabled ( true );
     this.gui.jGTITableGrammar
         .setTransferHandler ( new JGTITableTransferHandler (
             TransferHandler.MOVE )
         {
 
           /**
            * The serial version uid.
            */
           private static final long serialVersionUID = -1544518703030919808L;
 
 
           @SuppressWarnings ( "synthetic-access" )
           @Override
           protected boolean importTableModelRows ( JGTITable jGTITable,
               JGTITableModelRows rows, int targetIndex )
           {
             moveRows ( jGTITable, rows, targetIndex );
             return true;
           }
         } );
   }
 
 
   /**
    * Add a new Error
    * 
    * @param grammarException The {@link MachineException} containing the data
    */
   public final void addError ( GrammarException grammarException )
   {
     this.errorTableModel.addRow ( grammarException );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#addModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public void addModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.add ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * Add a new {@link Production}.
    * 
    * @param production the new Production to add.
    */
   public void addProduction ( Production production )
   {
     this.model.addProduction ( production );
   }
 
 
   /**
    * Add a new Warning
    * 
    * @param grammarException The {@link MachineException} containing the data
    */
   public final void addWarning ( GrammarException grammarException )
   {
     this.warningTableModel.addRow ( grammarException );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#clearValidationMessages()
    */
   public void clearValidationMessages ()
   {
     this.gui.jGTITabbedPaneConsole.setTitleAt ( 0, Messages
         .getString ( "MachinePanel.Error" ) ); //$NON-NLS-1$
     this.gui.jGTITabbedPaneConsole.setTitleAt ( 1, Messages
         .getString ( "MachinePanel.Warning" ) ); //$NON-NLS-1$
 
     this.errorTableModel.clearData ();
     this.warningTableModel.clearData ();
   }
 
 
   /**
    * Let the listeners know that the modify status has changed.
    * 
    * @param forceModify True if the modify is forced, otherwise false.
    */
   public final void fireModifyStatusChanged ( boolean forceModify )
   {
 
     ModifyStatusChangedListener [] listeners = this.listenerList
         .getListeners ( ModifyStatusChangedListener.class );
     if ( forceModify )
     {
       for ( ModifyStatusChangedListener current : listeners )
       {
         current.modifyStatusChanged ( true );
       }
     }
     else
     {
       boolean newModifyStatus = isModified ();
       for ( ModifyStatusChangedListener current : listeners )
       {
         current.modifyStatusChanged ( newModifyStatus );
       }
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see EditorPanel#getFile()
    */
   public File getFile ()
   {
     return this.file;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see EditorPanel#getFileEnding()
    */
   public String getFileEnding ()
   {
     return "." + this.grammar.getGrammarType ().toLowerCase (); //$NON-NLS-1$
   }
 
 
   /**
    * Returns the grammar.
    * 
    * @return The grammar.
    * @see #grammar
    */
   public Grammar getGrammar ()
   {
     return this.grammar;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getGui()
    */
   public EditorPanelForm getGui ()
   {
     return this.gui;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getJTabbedPaneConsole()
    */
   public JTabbedPane getJTabbedPaneConsole ()
   {
     return this.gui.jGTITabbedPaneConsole;
   }
 
 
   /**
    * Returns the {@link MainWindow}.
    * 
    * @return the {@link MainWindow}.
    */
   public MainWindow getMainWindow ()
   {
     return this.mainWindowForm.getLogic ();
   }
 
 
   /**
    * Returns the {@link DefaultModel}.
    * 
    * @return The {@link DefaultModel}.
    * @see #model
    */
   public DefaultModel getModel ()
   {
     return this.model;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see EditorPanel#getName()
    */
   public String getName ()
   {
     return this.file == null ? this.name : this.file.getName ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see EditorPanel#getPanel()
    */
   public JPanel getPanel ()
   {
     return this.gui;
   }
 
 
   /**
    * Returns the parent frame.
    * 
    * @return the parent frame.
    */
   public JFrame getParent ()
   {
     return this.mainWindowForm;
   }
 
 
   /**
    * Handle add production button pressed.
    */
   public void handleAddProduction ()
   {
     ProductionDialog dialog = new ProductionDialog ( getParent (), this.grammar
         .getNonterminalSymbolSet (), this.grammar.getTerminalSymbolSet (),
         this.model, null );
     dialog.show ();
   }
 
 
   /**
    * Handles focus lost event on the console table.
    * 
    * @param event The {@link FocusEvent}.
    */
   public final void handleConsoleTableFocusLost ( @SuppressWarnings ( "unused" )
   FocusEvent event )
   {
     this.gui.jGTITableErrors.clearSelection ();
     this.gui.jGTITableWarnings.clearSelection ();
   }
 
 
   /**
    * Handles the mouse exited event on the console table.
    * 
    * @param event The {@link MouseEvent}.
    */
   public final void handleConsoleTableMouseExited (
       @SuppressWarnings ( "unused" )
       MouseEvent event )
   {
     this.gui.jGTITableErrors.clearSelection ();
     this.gui.jGTITableWarnings.clearSelection ();
   }
 
 
   /**
    * Handles {@link ListSelectionEvent}s on the console table.
    * 
    * @param event The {@link ListSelectionEvent}.
    */
   public final void handleConsoleTableValueChanged (
       @SuppressWarnings ( "unused" )
       ListSelectionEvent event )
   {
     // TODO implement me
   }
 
 
   /**
    * Handle delete production button pressed.
    */
   public void handleDeleteProduction ()
   {
     if ( this.gui.jGTITableGrammar.getRowCount () > this.gui.jGTITableGrammar
         .getSelectedRow ()
         && this.gui.jGTITableGrammar.getSelectedRow () > -1 )
     {
       Production production = this.grammar
           .getProductionAt ( this.gui.jGTITableGrammar.getSelectedRow () );
       ConfirmDialog confirmedDialog = new ConfirmDialog ( getParent (),
           Messages.getString ( "ProductionPopupMenu.DeleteProductionQuestion", //$NON-NLS-1$
               production ), Messages
               .getString ( "ProductionPopupMenu.DeleteProductionTitle" ), true, //$NON-NLS-1$
           true, false );
       confirmedDialog.show ();
       if ( confirmedDialog.isConfirmed () )
       {
         this.model.removeProduction ( production );
         this.gui.repaint ();
       }
     }
   }
 
 
   /**
    * Handle edit production button pressed.
    */
   public void handleEditProduction ()
   {
 
     if ( this.gui.jGTITableGrammar.getRowCount () > this.gui.jGTITableGrammar
         .getSelectedRow ()
         && this.gui.jGTITableGrammar.getSelectedRow () > -1 )
     {
       Production production = this.grammar
           .getProductionAt ( this.gui.jGTITableGrammar.getSelectedRow () );
       JFrame window = ( JFrame ) SwingUtilities.getWindowAncestor ( this.gui );
       ProductionDialog productionDialog = new ProductionDialog ( window,
           this.grammar.getNonterminalSymbolSet (), this.grammar
               .getTerminalSymbolSet (), this.model, production );
       productionDialog.show ();
     }
   }
 
 
   /**
    * Handles the {@link Exchange}.
    */
   public final void handleExchange ()
   {
     ExchangeDialog exchangeDialog = new ExchangeDialog ( this.mainWindowForm
         .getLogic (), this.model.getElement (), this.file );
     exchangeDialog.show ();
   }
 
 
   /**
    * Handle redo button pressed
    */
   public void handleRedo ()
   {
     // TODO implement me
   }
 
 
   /**
    * Handle save as operation
    * 
    * @return filename
    */
   public final File handleSave ()
   {
     if ( this.file == null )
     {
       return handleSaveAs ();
     }
     try
     {
       Storage.getInstance ().store ( this.model, this.file );
     }
     catch ( StoreException e )
     {
       InfoDialog infoDialog = new InfoDialog ( this.mainWindowForm, e
           .getMessage (), Messages.getString ( "MachinePanel.Save" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     resetModify ();
     fireModifyStatusChanged ( false );
     return this.file;
   }
 
 
   /**
    * Handle save as operation
    * 
    * @return filename
    */
   public final File handleSaveAs ()
   {
     try
     {
       FileFilter fileFilter = new FileFilter ()
       {
 
         @SuppressWarnings ( "synthetic-access" )
         @Override
         public boolean accept ( File acceptedFile )
         {
           if ( acceptedFile.isDirectory () )
           {
             return true;
           }
           if ( acceptedFile.getName ().toLowerCase ().matches ( ".+\\." //$NON-NLS-1$
               + GrammarPanel.this.grammar.getGrammarType ().toLowerCase () ) )
           {
             return true;
           }
           return false;
         }
 
 
         @SuppressWarnings ( "synthetic-access" )
         @Override
         public String getDescription ()
         {
           return Messages.getString ( "NewDialog." //$NON-NLS-1$
               + GrammarPanel.this.grammar.getGrammarType () )
               + " (*." //$NON-NLS-1$
               + GrammarPanel.this.grammar.getGrammarType ().toLowerCase ()
               + ")"; //$NON-NLS-1$
         }
       };
 
       SaveDialog saveDialog = new SaveDialog ( this.mainWindowForm,
           PreferenceManager.getInstance ().getWorkingPath (), fileFilter,
           fileFilter );
       saveDialog.show ();
 
       if ( ( !saveDialog.isConfirmed () )
           || ( saveDialog.getSelectedFile () == null ) )
       {
         return null;
       }
 
       if ( saveDialog.getSelectedFile ().exists () )
       {
         ConfirmDialog confirmDialog = new ConfirmDialog ( this.mainWindowForm,
             Messages.getString (
                 "MachinePanel.FileExists", saveDialog.getSelectedFile () //$NON-NLS-1$
                     .getName () ), Messages.getString ( "MachinePanel.Save" ), //$NON-NLS-1$
             true, true, false );
         confirmDialog.show ();
         if ( confirmDialog.isNotConfirmed () )
         {
           return null;
         }
       }
 
       String filename = saveDialog.getSelectedFile ().toString ().matches (
           ".+\\." + this.grammar.getGrammarType ().toLowerCase () ) ? saveDialog //$NON-NLS-1$
           .getSelectedFile ().toString ()
           : saveDialog.getSelectedFile ().toString ()
               + "." + this.grammar.getGrammarType ().toLowerCase (); //$NON-NLS-1$
 
       Storage.getInstance ().store ( this.model, new File ( filename ) );
 
       PreferenceManager.getInstance ().setWorkingPath (
           saveDialog.getCurrentDirectory ().getAbsolutePath () );
       this.file = new File ( filename );
     }
     catch ( StoreException e )
     {
       InfoDialog infoDialog = new InfoDialog ( this.mainWindowForm, e
           .getMessage (), Messages.getString ( "MachinePanel.Save" ) ); //$NON-NLS-1$
       infoDialog.show ();
     }
     resetModify ();
     fireModifyStatusChanged ( false );
     return this.file;
   }
 
 
   /**
    * Handle mouse button event for the JTable.
    * 
    * @param event The {@link MouseEvent}.
    */
   public void handleTableMouseClickedEvent ( MouseEvent event )
   {
     if ( event.getButton () == MouseEvent.BUTTON2 )
       return;
 
     if ( event.getButton () == MouseEvent.BUTTON3 )
     {
 
       ArrayList < Production > productions = new ArrayList < Production > ();
       boolean multiRowChoosen = false;
 
       int [] rows = this.gui.jGTITableGrammar.getSelectedRows ();
       int rowIndex = this.gui.jGTITableGrammar.rowAtPoint ( event.getPoint () );
 
       if ( rows.length > 1 )
         for ( int row : rows )
         {
           if ( row == rowIndex )
             multiRowChoosen = true;
         }
       if ( !multiRowChoosen )
       {
         if ( rowIndex == -1 )
         {
           // Do nothing
         }
         else
         {
 
           // Give the user a visual clue which rowIndex he has clicked on
           this.gui.jGTITableGrammar
               .changeSelection ( rowIndex, 0, false, false );
 
           productions.add ( this.grammar.getProductionAt ( rowIndex ) );
         }
       }
       else
       {
         int [] rowindeces = new int [ rows.length ];
         for ( int i = 0 ; i < rows.length ; i++ )
         {
           productions.add ( this.grammar.getProductionAt ( rowindeces [ i ] ) );
         }
       }
 
       ProductionPopupMenu popupmenu = new ProductionPopupMenu ( this,
           this.model, productions );
 
       popupmenu.show ( ( Component ) event.getSource (), event.getX (), event
           .getY () );
     }
     else if ( event.getClickCount () == 2 )
     {
 
       int rowIndex = this.gui.jGTITableGrammar.rowAtPoint ( event.getPoint () );
       if ( rowIndex == -1 )
       {
         return;
       }
       Production production = this.grammar.getProductionAt ( rowIndex );
 
       JFrame window = ( JFrame ) SwingUtilities.getWindowAncestor ( this.gui );
       ProductionDialog productionDialog = new ProductionDialog ( window,
           this.grammar.getNonterminalSymbolSet (), this.grammar
               .getTerminalSymbolSet (), this.model, production );
       productionDialog.show ();
     }
   }
 
 
   /**
    * Handles the toolbar edit document event.
    */
   public void handleToolbarEditDocument ()
   {
     TerminalDialog terminalDialog = new TerminalDialog ( this.mainWindowForm,
         this.grammar );
     terminalDialog.show ();
     // Must be repainted because of the maybe changed start symbol. 
     this.gui.jGTITableGrammar.repaint ();
   }
 
 
   /**
    * Handle undo button pressed
    */
   public void handleUndo ()
   {
     // TODO implement me
   }
 
 
   /**
    * Initialize the used model
    */
   private void initialize ()
   {
     this.gui.jGTITableGrammar.setModel ( this.grammar );
     this.gui.jGTITableGrammar.setColumnModel ( new GrammarColumnModel () );
     this.gui.jGTITableGrammar.getTableHeader ().setReorderingAllowed ( false );
     if ( this.grammar.getColumnCount () > 0 )
     {
       this.gui.jGTITableGrammar.getSelectionModel ().setSelectionInterval ( 0,
           0 );
     }
 
     // ModifyStatusChangedListener
     this.modifyStatusChangedListener = new ModifyStatusChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void modifyStatusChanged ( boolean modified )
       {
         fireModifyStatusChanged ( modified );
       }
     };
     this.model
         .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
 
     this.errorTableModel = new GrammarConsoleTableModel ();
     this.gui.jGTITableErrors.setModel ( this.errorTableModel );
     this.gui.jGTITableErrors.setColumnModel ( new ConsoleColumnModel () );
     this.gui.jGTITableErrors.getTableHeader ().setReorderingAllowed ( false );
     this.gui.jGTITableErrors
         .setSelectionMode ( ListSelectionModel.SINGLE_SELECTION );
     this.gui.jGTITableErrors.getSelectionModel ().addListSelectionListener (
         new ListSelectionListener ()
         {
 
           public void valueChanged ( ListSelectionEvent event )
           {
             handleConsoleTableValueChanged ( event );
           }
 
         } );
     this.warningTableModel = new GrammarConsoleTableModel ();
     this.gui.jGTITableWarnings.setModel ( this.warningTableModel );
     this.gui.jGTITableWarnings.setColumnModel ( new ConsoleColumnModel () );
     this.gui.jGTITableWarnings.getTableHeader ().setReorderingAllowed ( false );
     this.gui.jGTITableWarnings
         .setSelectionMode ( ListSelectionModel.SINGLE_SELECTION );
     this.gui.jGTITableWarnings.getSelectionModel ().addListSelectionListener (
         new ListSelectionListener ()
         {
 
           public void valueChanged ( ListSelectionEvent event )
           {
             handleConsoleTableValueChanged ( event );
           }
 
         } );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#isModified()
    */
   public final boolean isModified ()
   {
     return ( this.model.isModified () ) || ( this.file == null );
   }
 
 
   /**
    * Signals if this panel is redo able
    * 
    * @return true, if is redo able, false else
    */
   public boolean isRedoAble ()
   {
     // TODO implement me
     return false;
   }
 
 
   /**
    * Signals if this panel is undo able
    * 
    * @return true, if is undo able, false else
    */
   public boolean isUndoAble ()
   {
     // TODO implement me
     return false;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LanguageChangedListener#languageChanged()
    */
   public void languageChanged ()
   {
     this.gui.jGTITabbedPaneConsole.setTitleAt ( 0, Messages
         .getString ( "MachinePanel.Error" ) ); //$NON-NLS-1$
     this.gui.jGTITabbedPaneConsole.setTitleAt ( 1, Messages
         .getString ( "MachinePanel.Warning" ) ); //$NON-NLS-1$
   }
 
 
   /**
    * Moves the rows.
    * 
    * @param jGTITable The {@link JGTIList} into which to import the rows.
    * @param rows The {@link JGTITableModelRows}.
    * @param targetIndex The target index.
    */
   private final void moveRows ( JGTITable jGTITable, JGTITableModelRows rows,
       int targetIndex )
   {
     ArrayList<Production> productions = new ArrayList<Production>();
     
     int[] indeces = rows.getRowIndices ();
     
     int newTargetIndex = targetIndex;
     
     if (indeces.length > 0 && indeces[0] < targetIndex){
       newTargetIndex++;
     }
     
     for (int index : indeces){
       productions.add ( this.grammar.getProductionAt ( index ) );
       
       if (index < targetIndex){
         newTargetIndex--;
       }
     }
     
     for (int i = indeces.length-1 ; i > -1; i-- ){
       this.grammar.getProductions().remove ( indeces[i] );
     }
     
     
     this.grammar.getProductions().addAll ( newTargetIndex , productions );
     
     this.gui.jGTITableGrammar.getSelectionModel ().setSelectionInterval ( newTargetIndex, newTargetIndex + indeces.length - 1 );
     
     fireModifyStatusChanged ( false );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#removeModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final synchronized void removeModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.remove ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#resetModify()
    */
   public void resetModify ()
   {
     this.model.resetModify ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see EditorPanel#setName(java.lang.String)
    */
   public void setName ( String name )
   {
     this.name = name;
   }
 
 
   /**
    * Sets the visibility of the console.
    * 
    * @param visible Visible or not visible.
    */
   public final void setVisibleConsole ( boolean visible )
   {
     if ( visible )
     {
       this.setDividerLocationConsole = false;
       this.gui.jGTISplitPaneConsole
           .setRightComponent ( this.gui.jGTITabbedPaneConsole );
       this.gui.jGTISplitPaneConsole.setDividerSize ( 3 );
       this.gui.jGTISplitPaneConsole.setDividerLocation ( PreferenceManager
           .getInstance ().getDividerLocationConsole () );
     }
     else
     {
       this.setDividerLocationConsole = false;
       this.gui.jGTISplitPaneConsole.setRightComponent ( null );
       this.gui.jGTISplitPaneConsole.setDividerSize ( 0 );
     }
   }
 }
