 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.Rectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 import org.jgraph.JGraph;
 import org.jgraph.graph.DefaultEdge;
 import org.jgraph.graph.DefaultGraphCell;
 import org.jgraph.graph.DefaultGraphModel;
 import org.jgraph.graph.GraphConstants;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.DefaultState;
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.core.entities.Transition;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 import de.unisiegen.gtitool.core.exceptions.word.WordFinishedException;
 import de.unisiegen.gtitool.core.exceptions.word.WordNotAcceptedException;
 import de.unisiegen.gtitool.core.exceptions.word.WordResetedException;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.EditorPanel;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.jgraphcomponents.DefaultStateView;
 import de.unisiegen.gtitool.ui.jgraphcomponents.DefaultTransitionView;
 import de.unisiegen.gtitool.ui.jgraphcomponents.GPCellViewFactory;
 import de.unisiegen.gtitool.ui.model.ConsoleColumnModel;
 import de.unisiegen.gtitool.ui.model.ConsoleTableModel;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.netbeans.MachinesPanelForm;
 import de.unisiegen.gtitool.ui.netbeans.MainWindowForm;
 import de.unisiegen.gtitool.ui.popup.DefaultPopupMenu;
 import de.unisiegen.gtitool.ui.popup.StatePopupMenu;
 import de.unisiegen.gtitool.ui.popup.TransitionPopupMenu;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.listener.ColorChangedAdapter;
 import de.unisiegen.gtitool.ui.preferences.listener.LanguageChangedListener;
 import de.unisiegen.gtitool.ui.storage.Storage;
 
 
 /**
  * The Panel containing the diagramm and table representing a machine
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public final class MachinePanel implements EditorPanel, LanguageChangedListener
 {
 
   /**
    * Selects the item with the given index in the console table or clears the
    * selection after a delay.
    * 
    * @author Christian Fehler
    */
   protected final class SleepTimerTask extends TimerTask
   {
 
     /**
      * The index.
      */
     protected int index;
 
 
     /**
      * The console table.
      */
     protected JTable consoleTable;
 
 
     /**
      * Initilizes the <code>SleepTimerTask</code>.
      * 
      * @param pConsoleTable The console table.
      * @param pIndex The index.
      */
     public SleepTimerTask ( JTable pConsoleTable, int pIndex )
     {
       this.index = pIndex;
       this.consoleTable = pConsoleTable;
     }
 
 
     /**
      * Selects the item with the given index in the color list or clears the
      * selection after a delay.
      * 
      * @see TimerTask#run()
      */
     @Override
     public final void run ()
     {
       if ( this.index == -1 )
       {
         SwingUtilities.invokeLater ( new Runnable ()
         {
 
           public void run ()
           {
             SleepTimerTask.this.consoleTable.getSelectionModel ()
                 .clearSelection ();
             SleepTimerTask.this.consoleTable.repaint ();
           }
         } );
       }
       else
       {
         SwingUtilities.invokeLater ( new Runnable ()
         {
 
           public void run ()
           {
             SleepTimerTask.this.consoleTable.getSelectionModel ()
                 .setSelectionInterval ( SleepTimerTask.this.index,
                     SleepTimerTask.this.index );
             SleepTimerTask.this.consoleTable.repaint ();
           }
         } );
       }
     }
   }
 
 
   //
   // Attributes
   //
   /** the parent window */
   private MainWindowForm parent;
 
 
   /** The {@link MachinesPanelForm} */
   private MachinesPanelForm gui;
 
 
   /** The {@link DefaultMachineModel} */
   private DefaultMachineModel model;
 
 
   /** The {@link Machine} */
   private Machine machine;
 
 
   /** The {@link JGraph} containing the diagramm */
   private JGraph graph;
 
 
   /** The {@link DefaultGraphModel} for this graph */
   private DefaultGraphModel graphModel;
 
 
   /** The {@link MouseAdapter} for the mouse icon in the toolbar */
   private MouseAdapter mouse;
 
 
   /** The {@link MouseAdapter} for the add State icon in the toolbar */
   private MouseAdapter addState;
 
 
   /** The {@link MouseAdapter} for the transition icon in the toolbar */
   private MouseAdapter transition;
 
 
   /** The {@link MouseAdapter} for the transition icon in the toolbar */
   private MouseMotionAdapter transitionMove;
 
 
   /** The {@link MouseAdapter} for the start icon in the toolbar */
   private MouseAdapter start;
 
 
   /** The {@link MouseAdapter} for the end icon in the toolbar */
   private MouseAdapter end;
 
 
   /** The source state for a new Transition */
   private DefaultStateView firstState;
 
 
   /** The tmp state for a new Transition */
   private DefaultGraphCell tmpState;
 
 
   /** The tmp transition */
   private DefaultEdge tmpTransition;
 
 
   /** Signals if drag in progress */
   private boolean dragged;
 
 
   /** The {@link Alphabet} of this Machine */
   private Alphabet alphabet;
 
 
   /** The push down {@link Alphabet} of this Machine */
   private Alphabet pushDownAlphabet;
 
 
   /** The zoom factor for this graph */
   private double zoomFactor;
 
 
   /** The {@link ConsoleTableModel} for the warning table */
   private ConsoleTableModel warningTableModel;
 
 
   /** The {@link ConsoleTableModel} for the error table */
   private ConsoleTableModel errorTableModel;
 
 
   /** The actual highlighted error states */
   private ArrayList < DefaultStateView > oldErrorStates = new ArrayList < DefaultStateView > ();
 
 
   /** The actual highlighted error transitions */
   private ArrayList < DefaultTransitionView > oldErrorTransitions = new ArrayList < DefaultTransitionView > ();
 
 
   /** The {@link JPopupMenu} */
   private JPopupMenu popup;
 
 
   /**
    * The {@link Timer} of the console table.
    */
   private Timer consoleTimer = null;
 
 
   /**
    * The File for this MachinePanel
    */
   private File fileName;
 
 
   /**
    * Flag that indicates if the console divider location should be stored.
    */
   private boolean setDividerLocationConsole = true;
 
 
   /**
    * Flag that indicates if the table divider location should be stored.
    */
   private boolean setDividerLocationTable = true;
 
 
   /**
    * Flag signals if we are in the enter word mode
    */
   private boolean enterWordMode = false;
 
 
   /**
    * Flag signals if console is visible
    */
   private boolean consoleVisible = PreferenceManager.getInstance ()
       .getVisibleConsole ();
 
 
   /**
    * Flag signals if table is visible
    */
   private boolean tableVisible = PreferenceManager.getInstance ()
       .getVisibleTable ();
 
 
   /**
    * Create a new Machine Panel Object
    * 
    * @param pParent The parent frame
    * @param pModel the {@link DefaultMachineModel} of this panel
    * @param pFileName the filename for this Machine Panel
    */
   public MachinePanel ( MainWindowForm pParent, DefaultMachineModel pModel,
       File pFileName )
   {
     this.parent = pParent;
     this.model = pModel;
     this.machine = pModel.getMachine ();
     this.fileName = pFileName;
     this.alphabet = this.machine.getAlphabet ();
     this.pushDownAlphabet = this.machine.getPushDownAlphabet ();
     this.gui = new MachinesPanelForm ();
     this.gui.setMachinePanel ( this );
     this.graph = this.model.getJGraph ();
     this.graphModel = this.model.getGraphModel ();
     this.zoomFactor = ( ( double ) PreferenceManager.getInstance ()
         .getZoomFactorItem ().getFactor () ) / 100;
     intitializeMouseAdapter ();
     this.graph.addMouseListener ( this.mouse );
     this.gui.diagrammContentPanel.setViewportView ( this.graph );
 
     this.errorTableModel = new ConsoleTableModel ();
     this.gui.jDragTableErrors.setModel ( this.errorTableModel );
     this.gui.jDragTableErrors.setColumnModel ( new ConsoleColumnModel () );
    this.gui.jDragTableErrors.getTableHeader ().setReorderingAllowed ( false );
     this.gui.jDragTableErrors
         .setSelectionMode ( ListSelectionModel.SINGLE_SELECTION );
     this.gui.jDragTableErrors.getSelectionModel ().addListSelectionListener (
         new ListSelectionListener ()
         {
 
           public void valueChanged ( ListSelectionEvent pEvent )
           {
             handleConsoleTableValueChanged ( pEvent );
           }
 
         } );
     this.warningTableModel = new ConsoleTableModel ();
     this.gui.jDragTableWarnings.setModel ( this.warningTableModel );
     this.gui.jDragTableWarnings.setColumnModel ( new ConsoleColumnModel () );
    this.gui.jDragTableWarnings.getTableHeader ().setReorderingAllowed ( false );
     this.gui.jDragTableWarnings
         .setSelectionMode ( ListSelectionModel.SINGLE_SELECTION );
     this.gui.jDragTableWarnings.getSelectionModel ().addListSelectionListener (
         new ListSelectionListener ()
         {
 
           public void valueChanged ( ListSelectionEvent pEvent )
           {
             handleConsoleTableValueChanged ( pEvent );
           }
 
         } );
     this.gui.jDragTableMachine.setModel ( this.machine );
     this.gui.jDragTableMachine.getTableHeader ().setReorderingAllowed ( false );
 
     PreferenceManager.getInstance ().addLanguageChangedListener ( this );
 
     PreferenceManager.getInstance ().addColorChangedListener (
         new ColorChangedAdapter ()
         {
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedStartState ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedStartState ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultStateView state = ( DefaultStateView ) object;
                 if ( state.getState ().isStartState () )
                   GraphConstants.setGradientColor ( state.getAttributes (),
                       pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedState ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedState ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultStateView state = ( DefaultStateView ) object;
                 if ( !state.getState ().isStartState () )
                   GraphConstants.setGradientColor ( state.getAttributes (),
                       pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedSymbol ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedSymbol ( @SuppressWarnings ( "unused" )
           Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultTransitionView t = ( DefaultTransitionView ) object;
                 GraphConstants.setLabelColor ( t.getAttributes (),
                     PreferenceManager.getInstance ().getColorItemSymbol ()
                         .getColor () );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
 
           /**
            * {@inheritDoc}
            * 
            * @see ColorChangedAdapter#colorChangedTransition ( Color )
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedTransition ( Color pNewColor )
           {
             for ( Object object : DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) )
             {
               try
               {
                 DefaultTransitionView t = ( DefaultTransitionView ) object;
                 GraphConstants.setLineColor ( t.getAttributes (), pNewColor );
 
               }
               catch ( ClassCastException e )
               {
                 // Do nothing
               }
             }
             MachinePanel.this.graphModel.cellsChanged ( DefaultGraphModel
                 .getAll ( MachinePanel.this.graphModel ) );
 
           }
 
         } );
     /*
      * Divider Location
      */
     this.gui.jSplitPaneConsole.setDividerLocation ( PreferenceManager
         .getInstance ().getDividerLocationConsole () );
     setVisibleConsole ( this.parent.jCheckBoxMenuItemConsole.getState () );
     this.gui.jSplitPaneConsole.addPropertyChangeListener (
         JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void propertyChange ( PropertyChangeEvent evt )
           {
             if ( MachinePanel.this.setDividerLocationConsole )
             {
               PreferenceManager.getInstance ().setDividerLocationConsole (
                   ( ( Integer ) evt.getNewValue () ).intValue () );
             }
           }
         } );
     this.gui.jSplitPaneTable.setDividerLocation ( PreferenceManager
         .getInstance ().getDividerLocationTable () );
     setVisibleTable ( this.parent.jCheckBoxMenuItemTable.getState () );
     this.gui.jSplitPaneTable.addPropertyChangeListener (
         JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener ()
         {
 
           @SuppressWarnings ( "synthetic-access" )
           public void propertyChange ( PropertyChangeEvent evt )
           {
             if ( MachinePanel.this.setDividerLocationTable )
             {
               PreferenceManager.getInstance ().setDividerLocationTable (
                   ( ( Integer ) evt.getNewValue () ).intValue () );
             }
           }
         } );
 
     this.gui.wordPanel.setVisible ( false );
     this.gui.wordPanel.setAlphabet ( this.alphabet );
   }
 
 
   /**
    * Add a new Error
    * 
    * @param e The {@link MachineException} containing the data
    */
   public void addError ( MachineException e )
   {
     this.errorTableModel.addRow ( e );
   }
 
 
   /**
    * Add a new Warning
    * 
    * @param e The {@link MachineException} containing the data
    */
   public void addWarning ( MachineException e )
   {
     this.warningTableModel.addRow ( e );
   }
 
 
   /**
    * Remove all highlighting
    */
   public void clearHighlight ()
   {
     for ( DefaultTransitionView view : this.oldErrorTransitions )
     {
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemTransition ().getColor () );
     }
 
     for ( DefaultStateView view : this.oldErrorStates )
     {
       if ( view.getState ().isStartState () )
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemStartState ()
                 .getColor () );
       else
       {
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
     }
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
   }
 
 
   /**
    * Clear all Error and Warning messages
    */
   public void clearValidationMessages ()
   {
     this.errorTableModel.clearData ();
     this.warningTableModel.clearData ();
 
   }
 
 
   /**
    * Create a standard Popup Menu
    * 
    * @return the new created Popup Menu
    */
   private DefaultPopupMenu createPopupMenu ()
   {
     int factor = ( new Double ( this.zoomFactor * 100 ) ).intValue ();
     return new DefaultPopupMenu ( this, this.machine, factor );
   }
 
 
   /**
    * Create a new Popup Menu for the given State
    * 
    * @param pState the State for to create a popup menu
    * @return the new created Popup Menu
    */
   private StatePopupMenu createStatePopupMenu ( DefaultStateView pState )
   {
     return new StatePopupMenu ( this.parent, this.graph, this.model, pState );
   }
 
 
   /**
    * Create a temporary Object to paint the Transiton on Mouse move
    * 
    * @param x the x position of the new state view
    * @param y the y position of the new state view
    * @return {@link DefaultGraphCell} the new created tmp Object
    */
   @SuppressWarnings ( "unchecked" )
   private DefaultGraphCell createTmpObject ( double x, double y )
   {
     String viewClass = "de.unisiegen.gtitool.ui.jgraphcomponents.StateView"; //$NON-NLS-1$
 
     DefaultGraphCell cell = new DefaultGraphCell ();
 
     // set the view class (indirection for the renderer and the editor)
     GPCellViewFactory.setViewClass ( cell.getAttributes (), viewClass );
 
     // Set bounds
     GraphConstants.setBounds ( cell.getAttributes (), new Rectangle2D.Double (
         x, y, 1, 1 ) );
 
     GraphConstants.setBorder ( cell.getAttributes (), BorderFactory
         .createRaisedBevelBorder () );
 
     // Set the line width
     GraphConstants.setLineWidth ( cell.getAttributes (), 2 );
 
     // Add a Floating Port
     cell.addPort ();
 
     return cell;
 
   }
 
 
   /**
    * Create a new Popup Menu for the given Transition
    * 
    * @param pTransition the Transition for to create a popup menu
    * @return the new created Popup Menu
    */
   private TransitionPopupMenu createTransitionPopupMenu (
       DefaultTransitionView pTransition )
   {
     return new TransitionPopupMenu ( this.graph, this.gui, this.model,
         pTransition, this.alphabet );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getAlphabet()
    */
   public Alphabet getAlphabet ()
   {
     return this.machine.getAlphabet ();
   }
 
 
   /**
   * Get the file for this Machine Panel
   * 
   * @return the File of this Machine Panel
   */
  public File getFile ()
  {
    return this.fileName;
  }


  /**
    * Getter for the {@link Machine}
    * 
    * @return the {@link Machine} of this panel
    */
   public Machine getMachine ()
   {
     return this.machine;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see de.unisiegen.gtitool.ui.EditorPanel#getPanel()
    */
   public JPanel getPanel ()
   {
     return this.gui;
   }
 
 
   /**
    * Handles {@link FocusEvent}s on the console table.
    * 
    * @param pEvent The {@link FocusEvent}.
    */
   public void handleConsoleTableFocusLost ( @SuppressWarnings ( "unused" )
   FocusEvent pEvent )
   {
     if ( this.consoleTimer != null )
     {
       this.consoleTimer.cancel ();
     }
     this.gui.jDragTableErrors.clearSelection ();
     this.gui.jDragTableWarnings.clearSelection ();
     this.clearHighlight ();
   }
 
 
   /**
    * Handles the mouse exited event on the console table.
    * 
    * @param pEvent The {@link MouseEvent}.
    */
   public final void handleConsoleTableMouseExited (
       @SuppressWarnings ( "unused" )
       MouseEvent pEvent )
   {
     if ( this.consoleTimer != null )
     {
       this.consoleTimer.cancel ();
     }
     this.gui.jDragTableErrors.clearSelection ();
     this.gui.jDragTableWarnings.clearSelection ();
     this.clearHighlight ();
   }
 
 
   /**
    * Handles the mouse moved event on the console table.
    * 
    * @param pEvent The {@link MouseEvent}.
    */
   public final void handleConsoleTableMouseMoved ( MouseEvent pEvent )
   {
     JTable table;
     if ( pEvent.getSource () == this.gui.jDragTableErrors )
     {
       table = this.gui.jDragTableErrors;
     }
     else if ( pEvent.getSource () == this.gui.jDragTableWarnings )
     {
       table = this.gui.jDragTableWarnings;
     }
     else
     {
       throw new IllegalArgumentException ( "wrong event source" ); //$NON-NLS-1$
     }
     int index = table.rowAtPoint ( pEvent.getPoint () );
     if ( this.consoleTimer != null )
     {
       this.consoleTimer.cancel ();
     }
     this.consoleTimer = new Timer ();
     if ( index == -1 )
     {
       table.setCursor ( new Cursor ( Cursor.DEFAULT_CURSOR ) );
       this.consoleTimer.schedule ( new SleepTimerTask ( table, -1 ), 250 );
     }
     else
     {
       table.setCursor ( new Cursor ( Cursor.HAND_CURSOR ) );
       this.consoleTimer.schedule ( new SleepTimerTask ( table, index ), 250 );
     }
   }
 
 
   /**
    * Handles {@link ListSelectionEvent}s on the console table.
    * 
    * @param pEvent The {@link ListSelectionEvent}.
    */
   @SuppressWarnings ( "unchecked" )
   public final void handleConsoleTableValueChanged (
       @SuppressWarnings ( "unused" )
       ListSelectionEvent pEvent )
   {
     JTable table;
     if ( pEvent.getSource () == this.gui.jDragTableErrors.getSelectionModel () )
     {
       table = this.gui.jDragTableErrors;
     }
     else if ( pEvent.getSource () == this.gui.jDragTableWarnings
         .getSelectionModel () )
     {
       table = this.gui.jDragTableWarnings;
     }
     else
     {
       throw new IllegalArgumentException ( "wrong event source" ); //$NON-NLS-1$
     }
     int index = table.getSelectedRow ();
     if ( index == -1 )
     {
       clearHighlight ();
     }
     else
     {
       highlightStates ( ( ( ConsoleTableModel ) table.getModel () )
           .getStates ( index ) );
       highlightTransitions ( ( ( ConsoleTableModel ) table.getModel () )
           .getTransitions ( index ) );
     }
   }
 
 
   /**
    * Handle Edit Machine button pressed
    */
   public void handleEditMachine ()
   {
     this.gui.wordPanel.setVisible ( false );
     this.model.getJGraph ().setEnabled ( true );
   }
 
 
   /**
    * Handle Enter Word button pressed
    */
   public void handleEnterWord ()
   {
     this.gui.wordPanel.setVisible ( true );
     this.model.getJGraph ().setEnabled ( false );
   }
 
 
   /**
    * Handle save as operation
    * 
    * @return filename
    */
   public String handleSave ()
   {
     if ( this.fileName == null )
     {
       return handleSaveAs ();
 
     }
 
     try
     {
       Storage.getInstance ().store ( this.model, this.fileName.toString () );
       JOptionPane.showMessageDialog ( this.parent, Messages
           .getString ( "MachinePanel.DataSaved" ), Messages //$NON-NLS-1$
           .getString ( "MachinePanel.Save" ), JOptionPane.INFORMATION_MESSAGE ); //$NON-NLS-1$
 
     }
     catch ( StoreException e )
     {
       JOptionPane.showMessageDialog ( this.parent, e.getMessage (), Messages
           .getString ( "MachinePanel.Save" ), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
     }
     return this.fileName.getName ();
   }
 
 
   /**
    * Handle save as operation
    * 
    * @return filename
    */
   public String handleSaveAs ()
   {
     try
     {
       PreferenceManager prefmanager = PreferenceManager.getInstance ();
       JFileChooser chooser = new JFileChooser ( prefmanager.getWorkingPath () );
       chooser.setMultiSelectionEnabled ( false );
       chooser.setAcceptAllFileFilterUsed ( false );
       chooser.addChoosableFileFilter ( new FileFilter ()
       {
 
         @SuppressWarnings ( "synthetic-access" )
         @Override
         public boolean accept ( File file )
         {
           if ( file.isDirectory () )
           {
             return true;
           }
           if ( file.getName ().toLowerCase ().matches ( ".+\\." //$NON-NLS-1$
               + MachinePanel.this.machine.getMachineType ().toLowerCase () ) )
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
               + MachinePanel.this.machine.getMachineType () )
               + " (*." //$NON-NLS-1$
               + MachinePanel.this.machine.getMachineType ().toLowerCase ()
               + ")"; //$NON-NLS-1$
         }
       } );
       int n = chooser.showSaveDialog ( this.parent );
       if ( n == JFileChooser.CANCEL_OPTION
           || chooser.getSelectedFile () == null )
         return null;
 
       String filename = chooser.getSelectedFile ().toString ().matches (
           ".+\\." + this.machine.getMachineType ().toLowerCase () ) ? chooser //$NON-NLS-1$
           .getSelectedFile ().toString () : chooser.getSelectedFile ()
           .toString ()
           + "." //$NON-NLS-1$
           + this.machine.getMachineType ().toLowerCase ();
 
       Storage.getInstance ().store ( this.model, filename );
       JOptionPane
           .showMessageDialog (
               this.parent,
               Messages.getString ( "MachinePanel.DataSaved" ), Messages.getString ( "MachinePanel.Save" ), JOptionPane.INFORMATION_MESSAGE ); //$NON-NLS-1$//$NON-NLS-2$
       prefmanager.setWorkingPath ( chooser.getCurrentDirectory ()
           .getAbsolutePath () );
       this.fileName = new File ( filename );
 
     }
     catch ( StoreException e )
     {
       JOptionPane.showMessageDialog ( this.parent, e.getMessage (), Messages
           .getString ( "MachinePanel.Save" ), JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
     }
     return this.fileName.getName ();
   }
 
 
   /**
    * Handle Toolbar Add State button value changed
    * 
    * @param state The new State of the Add State Toolbar button
    */
   public void handleToolbarAddState ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.addState );
     else
       this.graph.removeMouseListener ( this.addState );
   }
 
 
   /**
    * Handle Toolbar Alphabet button action event
    */
   public void handleToolbarAlphabet ()
   {
     // TODOChristian
     AlphabetDialog alphabetDialog = new AlphabetDialog ( this.parent,
         this.alphabet, this.machine );
     alphabetDialog.show ();
     if ( alphabetDialog.DIALOG_RESULT == AlphabetDialog.DIALOG_CONFIRMED )
     {
       System.out.println ( this.alphabet );
       System.out.println ( this.machine.getAlphabet () );
     }
   }
 
 
   /**
    * Handle Toolbar End button value changed
    * 
    * @param state The new State of the End Toolbar button
    */
   public void handleToolbarEnd ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.end );
     else
       this.graph.removeMouseListener ( this.end );
   }
 
 
   /**
    * Handle Toolbar Mouse button value changed
    * 
    * @param state The new State of the Mouse Toolbar button
    */
   public void handleToolbarMouse ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.mouse );
     else
       this.graph.removeMouseListener ( this.mouse );
   }
 
 
   /**
    * Handle Toolbar Start button value changed
    * 
    * @param state The new State of the Start Toolbar button
    */
   public void handleToolbarStart ( boolean state )
   {
     if ( state )
       this.graph.addMouseListener ( this.start );
     else
       this.graph.removeMouseListener ( this.start );
   }
 
 
   /**
    * Handle Toolbar Transition button value changed
    * 
    * @param state The new State of the Transition Toolbar button
    */
   public void handleToolbarTransition ( boolean state )
   {
     if ( state )
     {
       this.graph.addMouseListener ( this.transition );
       this.graph.addMouseMotionListener ( this.transitionMove );
     }
     else
     {
       this.graph.removeMouseListener ( this.transition );
       this.graph.removeMouseMotionListener ( this.transitionMove );
     }
   }
 
 
   /**
    * Handle Auto Step Action in the Word Enter Mode
    */
   public void handleWordAutoStep ()
   {
     // TODO Auto-generated method stub
   }
 
 
   /**
    * Handle Next Step Action in the Word Enter Mode
    */
   public void handleWordNextStep ()
   {
     try
     {
       ArrayList < Transition > activeTransitions = this.machine.nextSymbol ();
       ArrayList < DefaultTransitionView > inactiveTransitions = new ArrayList < DefaultTransitionView > ();
       inactiveTransitions.addAll ( this.model.getTransitionViewList () );
 
       for ( Transition current : activeTransitions )
       {
         DefaultTransitionView transitionView = this.model
             .getTransitionViewForTransition ( current );
         GraphConstants.setGradientColor ( transitionView.getAttributes (),
             PreferenceManager.getInstance ().getColorItemActiveTransition ()
                 .getColor () );
         inactiveTransitions.remove ( transitionView );
       }
 
       for ( DefaultTransitionView current : inactiveTransitions )
       {
         GraphConstants.setGradientColor ( current.getAttributes (),
             PreferenceManager.getInstance ().getColorItemTransition ()
                 .getColor () );
       }
 
       ArrayList < DefaultStateView > inactiveStates = new ArrayList < DefaultStateView > ();
       inactiveStates.addAll ( this.model.getStateViewList () );
 
       for ( State current : this.machine.getActiveState () )
       {
         DefaultStateView state = this.model.getStateViewForState ( current );
         GraphConstants.setGradientColor ( state.getAttributes (),
             PreferenceManager.getInstance ().getColorItemActiveState ()
                 .getColor () );
         inactiveStates.remove ( state );
       }
 
       for ( DefaultStateView current : inactiveStates )
       {
         GraphConstants.setGradientColor ( current.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
 
       this.graphModel.cellsChanged ( DefaultGraphModel
           .getAll ( this.graphModel ) );
 
       try
       {
         this.gui.wordPanel.styledWordParserPanel
             .setHighlightedSymbol ( this.machine.getReadedSymbols () );
       }
       catch ( WordResetedException exc )
       {
         this.gui.wordPanel.styledWordParserPanel.setHighlightedSymbol ();
       }
     }
     catch ( WordFinishedException exc )
     {
       JOptionPane.showMessageDialog ( this.parent, exc.getDescription (), exc
           .getMessage (), JOptionPane.INFORMATION_MESSAGE );
     }
     catch ( WordResetedException exc )
     {
       JOptionPane.showMessageDialog ( this.parent, exc.getDescription (), exc
           .getMessage (), JOptionPane.INFORMATION_MESSAGE );
     }
     catch ( WordNotAcceptedException exc )
     {
       JOptionPane.showMessageDialog ( this.parent, exc.getDescription (), exc
           .getMessage (), JOptionPane.INFORMATION_MESSAGE );
     }
   }
 
 
   /**
    * Handle Previous Step Action in the Word Enter Mode
    */
   public void handleWordPreviousStep ()
   {
     try
     {
       ArrayList < Transition > activeTransitions = this.machine
           .previousSymbol ();
       ArrayList < DefaultTransitionView > inactiveTransitions = new ArrayList < DefaultTransitionView > ();
       inactiveTransitions.addAll ( this.model.getTransitionViewList () );
 
       for ( Transition current : activeTransitions )
       {
         DefaultTransitionView transitionView = this.model
             .getTransitionViewForTransition ( current );
         GraphConstants.setGradientColor ( transitionView.getAttributes (),
             PreferenceManager.getInstance ().getColorItemActiveTransition ()
                 .getColor () );
         inactiveTransitions.remove ( transitionView );
       }
 
       for ( DefaultTransitionView current : inactiveTransitions )
       {
         GraphConstants.setGradientColor ( current.getAttributes (),
             PreferenceManager.getInstance ().getColorItemTransition ()
                 .getColor () );
       }
 
       ArrayList < DefaultStateView > inactiveStates = new ArrayList < DefaultStateView > ();
       inactiveStates.addAll ( this.model.getStateViewList () );
 
       for ( State current : this.machine.getActiveState () )
       {
         DefaultStateView state = this.model.getStateViewForState ( current );
         GraphConstants.setGradientColor ( state.getAttributes (),
             PreferenceManager.getInstance ().getColorItemActiveState ()
                 .getColor () );
         inactiveStates.remove ( state );
       }
       for ( DefaultStateView current : inactiveStates )
       {
         GraphConstants.setGradientColor ( current.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
       this.graphModel.cellsChanged ( DefaultGraphModel
           .getAll ( this.graphModel ) );
 
       /*
        * After the last previous step the current symbol is not defined.
        */
       try
       {
         this.gui.wordPanel.styledWordParserPanel
             .setHighlightedSymbol ( this.machine.getReadedSymbols () );
       }
       catch ( WordResetedException exc )
       {
         this.gui.wordPanel.styledWordParserPanel.setHighlightedSymbol ();
       }
     }
     catch ( WordFinishedException exc )
     {
       JOptionPane.showMessageDialog ( this.parent, exc.getDescription (), exc
           .getMessage (), JOptionPane.INFORMATION_MESSAGE );
     }
     catch ( WordResetedException exc )
     {
       JOptionPane.showMessageDialog ( this.parent, exc.getDescription (), exc
           .getMessage (), JOptionPane.INFORMATION_MESSAGE );
     }
   }
 
 
   /**
    * Handle Start Action in the Word Enter Mode
    * 
    * @return true if started else false
    */
   public boolean handleWordStart ()
   {
     if ( this.gui.wordPanel.styledWordParserPanel.getWord () == null )
     {
       // TODO i18n
       JOptionPane.showMessageDialog ( this.parent, "Kein Wort eingegeben", //$NON-NLS-1$
           "Error", JOptionPane.ERROR_MESSAGE ); //$NON-NLS-1$
       return false;
     }
     // Reset all highlightings
     for ( DefaultTransitionView current : this.model.getTransitionViewList () )
     {
       GraphConstants.setGradientColor ( current.getAttributes (),
           PreferenceManager.getInstance ().getColorItemTransition ()
               .getColor () );
     }
 
     for ( DefaultStateView current : this.model.getStateViewList () )
     {
       GraphConstants.setGradientColor ( current.getAttributes (),
           PreferenceManager.getInstance ().getColorItemState ().getColor () );
     }
 
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
 
     this.gui.wordPanel.styledWordParserPanel.setEditable ( false );
 
     this.machine.start ( this.gui.wordPanel.styledWordParserPanel.getWord () );
 
     DefaultStateView state = this.model.getStateViewForState ( this.machine
         .getActiveState ( 0 ) );
     GraphConstants.setGradientColor ( state.getAttributes (), PreferenceManager
         .getInstance ().getColorItemActiveState ().getColor () );
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
     return true;
   }
 
 
   /**
    * Handle Stop Action in the Word Enter Mode
    */
   public void handleWordStop ()
   {
     // Reset all highlightings
     for ( DefaultTransitionView current : this.model.getTransitionViewList () )
     {
       GraphConstants.setGradientColor ( current.getAttributes (),
           PreferenceManager.getInstance ().getColorItemTransition ()
               .getColor () );
     }
 
     for ( DefaultStateView current : this.model.getStateViewList () )
     {
       GraphConstants.setGradientColor ( current.getAttributes (),
           PreferenceManager.getInstance ().getColorItemState ().getColor () );
     }
 
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
 
     this.gui.wordPanel.styledWordParserPanel.setHighlightedSymbol ();
     this.gui.wordPanel.styledWordParserPanel.setEditable ( true );
   }
 
 
   /**
    * Highlight the affected states
    * 
    * @param states list with all states that are affected
    */
   private void highlightStates ( ArrayList < State > states )
   {
     for ( DefaultStateView view : this.oldErrorStates )
     {
       if ( view.getState ().isStartState () )
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemStartState ()
                 .getColor () );
       else
       {
         GraphConstants.setGradientColor ( view.getAttributes (),
             PreferenceManager.getInstance ().getColorItemState ().getColor () );
       }
     }
     this.oldErrorStates.clear ();
     for ( State state : states )
     {
       DefaultStateView view = this.model.getStateViewForState ( state );
       this.oldErrorStates.add ( view );
       GraphConstants.setGradientColor ( view.getAttributes (),
           PreferenceManager.getInstance ().getColorItemErrorState ()
               .getColor () );
     }
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
   }
 
 
   /**
    * Highlight the affected transitions
    * 
    * @param transitions list with all transitions that are affected
    */
   private void highlightTransitions ( ArrayList < Transition > transitions )
   {
     for ( DefaultTransitionView view : this.oldErrorTransitions )
     {
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemTransition ().getColor () );
     }
     this.oldErrorTransitions.clear ();
 
     for ( Transition t : transitions )
     {
       DefaultTransitionView view = this.model
           .getTransitionViewForTransition ( t );
       this.oldErrorTransitions.add ( view );
       GraphConstants.setLineColor ( view.getAttributes (), PreferenceManager
           .getInstance ().getColorItemErrorTransition ().getColor () );
     }
     this.graphModel
         .cellsChanged ( DefaultGraphModel.getAll ( this.graphModel ) );
   }
 
 
   /**
    * Initialize the Mouse Adapter of the Toolbar
    */
   @SuppressWarnings ( "synthetic-access" )
   private void intitializeMouseAdapter ()
   {
     this.mouse = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse has been clicked on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // Return if pressed Button is not the left mouse button
         if ( e.getButton () != MouseEvent.BUTTON3 )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open a new popup menu
         DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
             .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                 .getY () );
         if ( object == null )
           MachinePanel.this.popup = createPopupMenu ();
         else if ( object instanceof DefaultTransitionView )
           MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
         else
           MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
         if ( MachinePanel.this.popup != null )
           MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
               .getX (), e.getY () );
       }
     };
 
     this.addState = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @SuppressWarnings ( "synthetic-access" )
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new DefaultState ( MachinePanel.this.alphabet,
               MachinePanel.this.pushDownAlphabet, false, false );
 
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
 
         switch ( PreferenceManager.getInstance ().getMouseSelectionItem () )
         {
           case WITHOUT_RETURN_TO_MOUSE :
           {
             // Do nothing
             break;
           }
           case WITH_RETURN_TO_MOUSE :
           {
             // Return to the normal Mouse after every click
             MachinePanel.this.parent.jButtonMouse.setSelected ( true );
             break;
           }
         }
 
       }
     };
 
     this.transition = new MouseAdapter ()
     {
 
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3
             && MachinePanel.this.firstState == null )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         // if drag in progress return
         if ( MachinePanel.this.dragged )
           return;
 
         if ( MachinePanel.this.firstState == null )
         {
           MachinePanel.this.firstState = ( DefaultStateView ) MachinePanel.this.graph
               .getSelectionCellAt ( e.getPoint () );
           if ( MachinePanel.this.firstState == null )
             return;
         }
         else
         {
           DefaultStateView target = null;
           try
           {
 
             target = ( DefaultStateView ) MachinePanel.this.graph
                 .getNextCellForLocation ( MachinePanel.this.tmpState, e
                     .getPoint ().getX (), e.getPoint ().getY () );
 
             MachinePanel.this.graphModel.remove ( new Object []
             { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           }
 
           catch ( ClassCastException exc )
           {
             MachinePanel.this.graphModel.remove ( new Object []
             { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
           }
           TransitionDialog dialog = new TransitionDialog (
               MachinePanel.this.parent, MachinePanel.this.alphabet,
               MachinePanel.this.firstState.getState (), target == null ? null
                   : target.getState () );
           dialog.show ();
           if ( dialog.DIALOG_RESULT == TransitionDialog.DIALOG_CONFIRMED )
           {
             Transition newTransition = dialog.getTransition ();
             if ( target == null )
             {
 
               try
               {
                 State newState = new DefaultState ( MachinePanel.this.alphabet,
                     MachinePanel.this.pushDownAlphabet, false, false );
                 target = MachinePanel.this.model.createStateView ( e
                     .getPoint ().x
                     / MachinePanel.this.zoomFactor, e.getPoint ().y
                     / MachinePanel.this.zoomFactor, newState );
                 newTransition.setStateEnd ( target.getState () );
 
               }
               catch ( StateException e1 )
               {
                 e1.printStackTrace ();
                 System.exit ( 1 );
                 return;
               }
 
             }
 
             MachinePanel.this.model.createTransitionView ( newTransition,
                 MachinePanel.this.firstState, target );
             dialog.dispose ();
           }
           switch ( PreferenceManager.getInstance ().getMouseSelectionItem () )
           {
             case WITHOUT_RETURN_TO_MOUSE :
             {
               // Do nothing
               break;
             }
             case WITH_RETURN_TO_MOUSE :
             {
               // Return to the normal Mouse after every click
               MachinePanel.this.parent.jButtonMouse.setSelected ( true );
               break;
             }
           }
           MachinePanel.this.firstState = null;
           MachinePanel.this.tmpTransition = null;
           MachinePanel.this.tmpState = null;
         }
       }
 
 
       @Override
       public void mouseReleased ( MouseEvent e )
       {
         if ( e.getButton () != MouseEvent.BUTTON1 )
           return;
 
         if ( !MachinePanel.this.dragged || MachinePanel.this.firstState == null )
           return;
 
         DefaultStateView target = null;
 
         try
         {
           target = ( DefaultStateView ) MachinePanel.this.graph
               .getNextCellForLocation ( MachinePanel.this.tmpState, e
                   .getPoint ().getX (), e.getPoint ().getY () );
 
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           if ( target != null && target.equals ( MachinePanel.this.firstState ) )
             return;
         }
         catch ( ClassCastException exc )
         {
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
         }
 
         TransitionDialog dialog = new TransitionDialog (
             MachinePanel.this.parent, MachinePanel.this.alphabet,
             MachinePanel.this.firstState.getState (), target == null ? null
                 : target.getState () );
         dialog.show ();
         if ( dialog.DIALOG_RESULT == TransitionDialog.DIALOG_CONFIRMED )
         {
           Transition newTransition = dialog.getTransition ();
           if ( target == null )
           {
             try
             {
               State newState;
               newState = new DefaultState ( MachinePanel.this.alphabet,
                   MachinePanel.this.pushDownAlphabet, false, false );
               target = MachinePanel.this.model.createStateView (
                   e.getPoint ().x / MachinePanel.this.zoomFactor,
                   e.getPoint ().y / MachinePanel.this.zoomFactor, newState );
               newTransition.setStateEnd ( target.getState () );
             }
             catch ( StateException e1 )
             {
               e1.printStackTrace ();
               System.exit ( 1 );
               return;
             }
           }
 
           MachinePanel.this.model.createTransitionView ( newTransition,
               MachinePanel.this.firstState, target );
           dialog.dispose ();
 
         }
         switch ( PreferenceManager.getInstance ().getMouseSelectionItem () )
         {
           case WITHOUT_RETURN_TO_MOUSE :
           {
             // Do nothing
             break;
           }
           case WITH_RETURN_TO_MOUSE :
           {
             // Return to the normal Mouse after every click
             MachinePanel.this.parent.jButtonMouse.setSelected ( true );
             break;
           }
         }
         MachinePanel.this.firstState = null;
         MachinePanel.this.tmpTransition = null;
         MachinePanel.this.tmpState = null;
         MachinePanel.this.dragged = false;
       }
 
     };
 
     this.transitionMove = new MouseMotionAdapter ()
     {
 
       @Override
       public void mouseDragged ( MouseEvent e )
       {
         double x, y;
         if ( MachinePanel.this.firstState == null )
         {
           MachinePanel.this.dragged = true;
           MachinePanel.this.firstState = ( DefaultStateView ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
         }
 
         else
         {
           // Remove old tmp state and transition
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
           x = e.getX () / MachinePanel.this.zoomFactor;
           y = e.getY () / MachinePanel.this.zoomFactor;
           MachinePanel.this.tmpState = createTmpObject ( x, y );
           MachinePanel.this.graph.getGraphLayoutCache ().insert (
               MachinePanel.this.tmpState );
 
           MachinePanel.this.tmpTransition = new DefaultEdge ( "" ); //$NON-NLS-1$
           GraphConstants.setLineEnd ( MachinePanel.this.tmpTransition
               .getAttributes (), GraphConstants.ARROW_CLASSIC );
           GraphConstants.setEndFill ( MachinePanel.this.tmpTransition
               .getAttributes (), true );
 
           MachinePanel.this.graph.getGraphLayoutCache ().insertEdge (
               MachinePanel.this.tmpTransition,
               MachinePanel.this.firstState.getChildAt ( 0 ),
               MachinePanel.this.tmpState.getChildAt ( 0 ) );
         }
       }
 
 
       /**
        * Invoked when the mouse button has been moved on a component (with no
        * buttons no down).
        */
       @Override
       public void mouseMoved ( MouseEvent e )
       {
         double x, y;
 
         if ( MachinePanel.this.firstState != null )
         {
           // Remove old tmp state and transition
           MachinePanel.this.graphModel.remove ( new Object []
           { MachinePanel.this.tmpState, MachinePanel.this.tmpTransition } );
 
           x = e.getX () / MachinePanel.this.zoomFactor;
           y = e.getY () / MachinePanel.this.zoomFactor;
           // x = MachinePanel.this.firstStatePosition.x < x ? x - 5 : x + 5;
           // y = MachinePanel.this.firstStatePosition.y < x ? y - 3 : y + 10;
           MachinePanel.this.tmpState = createTmpObject ( x, y );
           MachinePanel.this.graph.getGraphLayoutCache ().insert (
               MachinePanel.this.tmpState );
 
           MachinePanel.this.tmpTransition = new DefaultEdge ( "" ); //$NON-NLS-1$
           GraphConstants.setLineEnd ( MachinePanel.this.tmpTransition
               .getAttributes (), GraphConstants.ARROW_CLASSIC );
           GraphConstants.setEndFill ( MachinePanel.this.tmpTransition
               .getAttributes (), true );
 
           MachinePanel.this.graph.getGraphLayoutCache ().insertEdge (
               MachinePanel.this.tmpTransition,
               MachinePanel.this.firstState.getChildAt ( 0 ),
               MachinePanel.this.tmpState.getChildAt ( 0 ) );
         }
       }
     };
 
     this.start = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new DefaultState ( MachinePanel.this.alphabet,
               MachinePanel.this.pushDownAlphabet, true, false );
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
 
         switch ( PreferenceManager.getInstance ().getMouseSelectionItem () )
         {
           case WITHOUT_RETURN_TO_MOUSE :
           {
             // Do nothing
             break;
           }
           case WITH_RETURN_TO_MOUSE :
           {
             // Return to the normal Mouse after every click
             MachinePanel.this.parent.jButtonMouse.setSelected ( true );
             break;
           }
         }
 
       }
     };
 
     this.end = new MouseAdapter ()
     {
 
       /**
        * Invoked when the mouse button has been clicked (pressed and released)
        * on a component.
        */
       @Override
       public void mouseClicked ( MouseEvent e )
       {
         // if Middle Mouse Button was pressed return
         if ( e.getButton () == MouseEvent.BUTTON2 )
           return;
 
         // if an popup menu is open close it and do nothing more
         if ( e.getButton () == MouseEvent.BUTTON1
             && MachinePanel.this.popup != null )
         {
           MachinePanel.this.popup = null;
           return;
         }
 
         // Open popup menu if left button was pressed
         if ( e.getButton () == MouseEvent.BUTTON3 )
         {
           DefaultGraphCell object = ( DefaultGraphCell ) MachinePanel.this.graph
               .getFirstCellForLocation ( e.getPoint ().getX (), e.getPoint ()
                   .getY () );
           if ( object == null )
             MachinePanel.this.popup = createPopupMenu ();
           else if ( object instanceof DefaultTransitionView )
             MachinePanel.this.popup = createTransitionPopupMenu ( ( DefaultTransitionView ) object );
           else
             MachinePanel.this.popup = createStatePopupMenu ( ( DefaultStateView ) object );
 
           if ( MachinePanel.this.popup != null )
             MachinePanel.this.popup.show ( ( Component ) e.getSource (), e
                 .getX (), e.getY () );
           return;
         }
 
         try
         {
           State newState = new DefaultState ( MachinePanel.this.alphabet,
               MachinePanel.this.pushDownAlphabet, false, true );
           MachinePanel.this.model.createStateView ( e.getPoint ().x
               / MachinePanel.this.zoomFactor, e.getPoint ().y
               / MachinePanel.this.zoomFactor, newState );
         }
         catch ( StateException e1 )
         {
           e1.printStackTrace ();
           System.exit ( 1 );
         }
 
         switch ( PreferenceManager.getInstance ().getMouseSelectionItem () )
         {
           case WITHOUT_RETURN_TO_MOUSE :
           {
             // Do nothing
             break;
           }
           case WITH_RETURN_TO_MOUSE :
           {
             // Return to the normal Mouse after every click
             MachinePanel.this.parent.jButtonMouse.setSelected ( true );
             break;
           }
         }
 
       }
     };
   }
 
 
   /**
    * Getter for the flag if console is visible
    * 
    * @return true if console is visible, else false
    */
   public boolean isConsoleVisible ()
   {
     return this.consoleVisible;
   }
 
 
   /**
    * Getter for the flag if table is visible
    * 
    * @return true if table is visible, else false
    */
   public boolean isTableVisible ()
   {
     return this.tableVisible;
   }
 
 
   /**
    * Getter for the flag if we are in word enter mode
    * 
    * @return true if we are in word enter mode, else false
    */
   public boolean isWordEnterMode ()
   {
     return this.enterWordMode;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LanguageChangedListener#languageChanged()
    */
   public final void languageChanged ()
   {
     this.gui.jTabbedPaneConsole.setTitleAt ( 0, Messages
         .getString ( "MachinePanel.Error" ) ); //$NON-NLS-1$
     this.gui.jTabbedPaneConsole.setTitleAt ( 1, Messages
         .getString ( "MachinePanel.Warning" ) ); //$NON-NLS-1$
   }
 
 
   /**
    * Set flag if console is visible
    * 
    * @param consoleVisible flag if console is visible
    */
   public void setConsoleVisible ( boolean consoleVisible )
   {
     this.consoleVisible = consoleVisible;
   }
 
 
   /**
    * Set the file for this Machine Panel
    * 
    * @param pFileName the file for this Machine Panel
    */
   public void setFileName ( File pFileName )
   {
     this.fileName = pFileName;
   }
 
 
   /**
    * Set the value of the table visible Flag
    * 
    * @param tableVisible The new value
    */
   public void setTableVisible ( boolean tableVisible )
   {
     this.tableVisible = tableVisible;
   }
 
 
   /**
    * Sets the visibility of the console.
    * 
    * @param pVisible Visible or not visible.
    */
   public void setVisibleConsole ( boolean pVisible )
   {
     this.consoleVisible = pVisible;
     if ( pVisible )
     {
       this.gui.jSplitPaneConsole
           .setRightComponent ( this.gui.jTabbedPaneConsole );
       this.gui.jSplitPaneConsole.setDividerSize ( 3 );
       this.gui.jSplitPaneConsole.setDividerLocation ( PreferenceManager
           .getInstance ().getDividerLocationConsole () );
       this.setDividerLocationConsole = true;
     }
     else
     {
       this.setDividerLocationConsole = false;
       this.gui.jSplitPaneConsole.setRightComponent ( null );
       this.gui.jSplitPaneConsole.setDividerSize ( 0 );
     }
   }
 
 
   /**
    * Sets the visibility of the table.
    * 
    * @param pVisible Visible or not visible.
    */
   public void setVisibleTable ( boolean pVisible )
   {
     if ( pVisible )
     {
       this.gui.jSplitPaneTable.setRightComponent ( this.gui.jScrollPaneMachine );
       this.gui.jSplitPaneTable.setDividerSize ( 3 );
       this.gui.jSplitPaneTable.setDividerLocation ( PreferenceManager
           .getInstance ().getDividerLocationTable () );
       this.setDividerLocationTable = true;
     }
     else
     {
       this.setDividerLocationTable = false;
       this.gui.jSplitPaneTable.setRightComponent ( null );
       this.gui.jSplitPaneTable.setDividerSize ( 0 );
     }
   }
 
 
   /**
    * Set the value of the word Enter Mode Flag
    * 
    * @param wordEnterMode The new value
    */
   public void setWordEnterMode ( boolean wordEnterMode )
   {
     this.enterWordMode = wordEnterMode;
     if ( !wordEnterMode )
     {
       this.gui.jSplitPaneConsole
           .setRightComponent ( this.gui.jTabbedPaneConsole );
       this.gui.jSplitPaneConsole.setDividerSize ( 3 );
       this.gui.jSplitPaneConsole.setDividerLocation ( PreferenceManager
           .getInstance ().getDividerLocationConsole () );
       this.setDividerLocationConsole = true;
     }
     else
     {
       this.setDividerLocationConsole = false;
       this.gui.jSplitPaneConsole.setRightComponent ( null );
       this.gui.jSplitPaneConsole.setDividerSize ( 0 );
     }
   }
 
 
   /**
    * Set the zoom factor for this panel
    * 
    * @param pFactor the new zoom factor
    */
   public void setZoomFactor ( double pFactor )
   {
     this.zoomFactor = pFactor;
     this.graph.setScale ( pFactor );
   }
 }
