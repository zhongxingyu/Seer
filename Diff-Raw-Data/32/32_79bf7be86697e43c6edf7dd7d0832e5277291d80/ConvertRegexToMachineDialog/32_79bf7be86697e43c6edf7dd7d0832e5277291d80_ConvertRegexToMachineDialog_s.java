 package de.unisiegen.gtitool.ui.logic;
 
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 
 import org.jgraph.graph.DefaultGraphModel;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.DefaultPositionState;
 import de.unisiegen.gtitool.core.entities.DefaultState;
 import de.unisiegen.gtitool.core.entities.DefaultSymbol;
 import de.unisiegen.gtitool.core.entities.DefaultTransition;
 import de.unisiegen.gtitool.core.entities.DefaultWord;
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.entities.Transition;
 import de.unisiegen.gtitool.core.entities.InputEntity.EntityType;
 import de.unisiegen.gtitool.core.entities.regex.ConcatenationNode;
 import de.unisiegen.gtitool.core.entities.regex.DisjunctionNode;
 import de.unisiegen.gtitool.core.entities.regex.EpsilonNode;
 import de.unisiegen.gtitool.core.entities.regex.KleeneNode;
 import de.unisiegen.gtitool.core.entities.regex.LeafNode;
 import de.unisiegen.gtitool.core.entities.regex.RegexNode;
 import de.unisiegen.gtitool.core.entities.regex.TokenNode;
 import de.unisiegen.gtitool.core.exceptions.state.StateException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolNotInAlphabetException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.machines.Machine.MachineType;
 import de.unisiegen.gtitool.core.machines.dfa.DefaultDFA;
 import de.unisiegen.gtitool.core.machines.enfa.DefaultENFA;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 import de.unisiegen.gtitool.core.parser.style.PrettyToken;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.core.regex.DefaultRegex;
 import de.unisiegen.gtitool.core.util.ObjectPair;
 import de.unisiegen.gtitool.logger.Logger;
 import de.unisiegen.gtitool.ui.convert.Converter;
 import de.unisiegen.gtitool.ui.i18n.Messages;
 import de.unisiegen.gtitool.ui.jgraph.DefaultStateView;
 import de.unisiegen.gtitool.ui.jgraph.DefaultTransitionView;
 import de.unisiegen.gtitool.ui.jgraph.JGTIGraph;
 import de.unisiegen.gtitool.ui.jgraph.StateView;
 import de.unisiegen.gtitool.ui.logic.interfaces.LogicClass;
 import de.unisiegen.gtitool.ui.model.ConvertMachineTableColumnModel;
 import de.unisiegen.gtitool.ui.model.ConvertMachineTableModel;
 import de.unisiegen.gtitool.ui.model.DefaultMachineModel;
 import de.unisiegen.gtitool.ui.model.DefaultRegexModel;
 import de.unisiegen.gtitool.ui.netbeans.ConvertMachineDialogForm;
 import de.unisiegen.gtitool.ui.netbeans.ConvertRegexToMachineDialogForm;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.redoundo.RedoUndoItem;
 import de.unisiegen.gtitool.ui.utils.LayoutManager;
 
 
 /**
  * The LogicClass for the RegexToMachine Converter
  * 
  * @author Simon Meurer
  */
 public class ConvertRegexToMachineDialog implements
     LogicClass < ConvertRegexToMachineDialogForm >, Converter
 {
 
   /**
    * Does the next step after a delay.
    * 
    * @author Simon Meurer
    */
   private final class AutoStepTimerTask extends TimerTask
   {
 
     /**
      * {@inheritDoc}
      * 
      * @see TimerTask#run()
      */
     @Override
     public final void run ()
     {
       SwingUtilities.invokeLater ( new Runnable ()
       {
 
         @SuppressWarnings ( "synthetic-access" )
         public void run ()
         {
           if ( ConvertRegexToMachineDialog.this.endReached )
           {
             handleStop ();
           }
           else
           {
             performNextStep ( true );
           }
         }
       } );
     }
   }
 
 
   /**
    * The {@link Position}.
    * 
    * @author Christian Fehler
    */
   private class Position
   {
 
     /**
      * The x position.
      */
     private double x;
 
 
     /**
      * The y position.
      */
     private double y;
 
 
     /**
      * Allocates a new {@link Position}.
      * 
      * @param x The x position.
      * @param y The y position.
      */
     public Position ( double x, double y )
     {
       this.x = x;
       this.y = y;
     }
 
 
     /**
      * Returns the x position.
      * 
      * @return The x position.
      * @see #x
      */
     public final double getX ()
     {
       return this.x;
     }
 
 
     /**
      * Returns the y position.
      * 
      * @return The y position.
      * @see #y
      */
     public final double getY ()
     {
       return this.y;
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @see Object#toString()
      */
     @Override
     public final String toString ()
     {
       return "x: " + this.x + ", y: " + this.y; //$NON-NLS-1$//$NON-NLS-2$
     }
   }
 
 
   /**
    * The {@link Step} enum.
    * 
    * @author Simon Meurer
    */
   private enum Step
   {
 
     /**
      * The convert concat step
      */
     CONVERT_CONCAT,
 
     /**
      * The convert disjunction step
      */
     CONVERT_DISJUNCTION,
 
     /**
      * The convert Epsilon step
      */
     CONVERT_EPSILON,
 
     /**
      * The convert kleene step
      */
     CONVERT_KLEENE,
 
     /**
      * The convert Token step
      */
     CONVERT_TOKEN,
 
     /**
      * The finish step.
      */
     FINISH,
 
     /**
      * The initial step
      */
     INITIAL;
 
     /**
      * {@inheritDoc}
      * 
      * @see Enum#toString()
      */
     @Override
     public final String toString ()
     {
       switch ( this )
       {
         case CONVERT_CONCAT :
           return "convert concatenation"; //$NON-NLS-1$
         case CONVERT_DISJUNCTION :
           return "convert disjunction"; //$NON-NLS-1$
         case CONVERT_EPSILON :
           return "convert epsilon"; //$NON-NLS-1$
         case CONVERT_KLEENE :
           return "convert kleene"; //$NON-NLS-1$
         case CONVERT_TOKEN :
           return "convert token"; //$NON-NLS-1$
         case INITIAL :
           return "initial"; //$NON-NLS-1$
         case FINISH :
           return "finish"; //$NON-NLS-1$
       }
       throw new RuntimeException ( "unsupported step" );//$NON-NLS-1$
     }
   }
 
 
   /**
    * The {@link StepItem}.
    * 
    * @author Christian Fehler
    */
   private class StepItem
   {
 
     /**
      * The act count
      */
     private int actCount = 0;
 
 
     /**
      * Is in this step the errorState created
      */
     private boolean errorCreated = false;
 
 
     /**
      * The last marked Position state for unmarking
      */
     private DefaultPositionState markedPositionState;
 
 
     /**
      * The last controlled Symbol
      */
     private Symbol controlledSymbol;
 
 
     /**
      * The active {@link Step}
      */
     private Step activeStep;
 
 
     /**
      * The active {@link RegexNode}
      */
     private RegexNode actNode;
 
 
     /**
      * The added states
      */
     private ArrayList < String > addedStates;
 
 
     /**
      * The added {@link DefaultTransitionView}s
      */
     private ArrayList < DefaultTransitionView > addedTransitions;
 
 
     /**
      * The added Symbols to Transitions
      */
     private ArrayList < ObjectPair < DefaultTransitionView, Symbol > > addedSymbolsToTransition;
 
 
     /**
      * The {@link RedoUndoItem} for the concatenation
      */
     private RedoUndoItem redoUndoItem;
 
 
     /**
      * The final states that where made normal
      */
     private ArrayList < DefaultStateView > setFinalFalse;
 
 
     /**
      * The start states that where made normal
      */
     private ArrayList < DefaultStateView > setStartFalse;
 
 
     /**
      * Allocates a new {@link StepItem}.
      * 
      * @param activeStep The active {@link Step}
      * @param addedStates The added states
      * @param redoUndoItem The {@link RedoUndoItem} for Concatenation
      * @param addedTransitions The added Transitions
      * @param setStartFalse The start states that where made normal
      * @param setFinalFalse The final states that where made normal
      * @param count The act count
      * @param actNode The active {@link RegexNode}
      * @param errorCreated Is error state created
      * @param markedPositionState The last marked position state
      * @param controlledSymbol The last controlled Symbol
      * @param addedSymbolsToTransition The added Symbols to Transitions
      */
     public StepItem (
         Step activeStep,
         ArrayList < String > addedStates,
         RedoUndoItem redoUndoItem,
         ArrayList < DefaultTransitionView > addedTransitions,
         ArrayList < DefaultStateView > setStartFalse,
         ArrayList < DefaultStateView > setFinalFalse,
         int count,
         RegexNode actNode,
         boolean errorCreated,
         DefaultPositionState markedPositionState,
         Symbol controlledSymbol,
         ArrayList < ObjectPair < DefaultTransitionView, Symbol > > addedSymbolsToTransition )
     {
       if ( activeStep == null )
       {
         throw new IllegalArgumentException ( "active step is null" ); //$NON-NLS-1$
       }
       this.activeStep = activeStep;
       this.addedStates = addedStates;
       this.redoUndoItem = redoUndoItem;
       this.setFinalFalse = setFinalFalse;
       this.setStartFalse = setStartFalse;
       this.addedTransitions = addedTransitions;
       this.actCount = count;
       this.actNode = actNode;
       this.errorCreated = errorCreated;
       this.markedPositionState = markedPositionState;
       this.controlledSymbol = controlledSymbol;
       this.addedSymbolsToTransition = addedSymbolsToTransition;
     }
 
 
     /**
      * Returns the actCount.
      * 
      * @return The actCount.
      * @see #actCount
      */
     public int getActCount ()
     {
       return this.actCount;
     }
 
 
     /**
      * Returns the controlledSymbol.
      * 
      * @return The controlledSymbol.
      * @see #controlledSymbol
      */
     public Symbol getControlledSymbol ()
     {
       return this.controlledSymbol;
     }
 
 
     /**
      * Returns the errorCreated.
      * 
      * @return The errorCreated.
      * @see #errorCreated
      */
     public boolean isErrorCreated ()
     {
       return this.errorCreated;
     }
 
 
     /**
      * Returns the addedSymbolsToTransition.
      * 
      * @return The addedSymbolsToTransition.
      * @see #addedSymbolsToTransition
      */
     public ArrayList < ObjectPair < DefaultTransitionView, Symbol >> getAddedSymbolsToTransition ()
     {
       return this.addedSymbolsToTransition;
     }
 
 
     /**
      * Returns the markedPositionState.
      * 
      * @return The markedPositionState.
      * @see #markedPositionState
      */
     public DefaultPositionState getMarkedPositionState ()
     {
       return this.markedPositionState;
     }
 
 
     /**
      * Returns the activeStep.
      * 
      * @return The activeStep.
      * @see #activeStep
      */
     public final Step getActiveStep ()
     {
       return this.activeStep;
     }
 
 
     /**
      * Returns the actNode.
      * 
      * @return The actNode.
      * @see #actNode
      */
     public RegexNode getActNode ()
     {
       return this.actNode;
     }
 
 
     /**
      * Returns the addedStates.
      * 
      * @return The addedStates.
      * @see #addedStates
      */
     public ArrayList < String > getAddedStates ()
     {
       return this.addedStates;
     }
 
 
     /**
      * Returns the addedTransitions.
      * 
      * @return The addedTransitions.
      * @see #addedTransitions
      */
     public ArrayList < DefaultTransitionView > getAddedTransitions ()
     {
       return this.addedTransitions;
     }
 
 
     /**
      * Returns the redoUndoItem.
      * 
      * @return The redoUndoItem.
      * @see #redoUndoItem
      */
     public RedoUndoItem getRedoUndoItem ()
     {
       return this.redoUndoItem;
     }
 
 
     /**
      * Returns the setFinalFalse.
      * 
      * @return The setFinalFalse.
      * @see #setFinalFalse
      */
     public ArrayList < DefaultStateView > getSetFinalFalse ()
     {
       return this.setFinalFalse;
     }
 
 
     /**
      * Returns the setStartFalse.
      * 
      * @return The setStartFalse.
      * @see #setStartFalse
      */
     public ArrayList < DefaultStateView > getSetStartFalse ()
     {
       return this.setStartFalse;
     }
   }
 
 
   /**
    * The {@link Logger} for this class.
    */
   private static final Logger logger = Logger
       .getLogger ( ConvertRegexToMachineDialog.class );
 
 
   /**
    * The actual {@link Step}
    */
   private Step actualStep = null;
 
 
   /**
    * The auto step {@link Timer}.
    */
   private Timer autoStepTimer = null;
 
 
   /**
    * The count of states
    */
   private int count = 0;
 
 
   /**
    * The {@link DefaultRegex}
    */
   private DefaultRegex defaultRegex;
 
 
   /**
    * Flag indicates if the empty state is already created
    */
   private boolean emptyStateCreated = false;
 
 
   /**
    * Flag that indicates if the end is reached.
    */
   private boolean endReached = false;
 
 
   /**
    * The {@link ConvertRegexToMachineDialogForm}
    */
   private ConvertRegexToMachineDialogForm gui;
 
 
   /**
    * The converted {@link JGTIGraph} containing the diagramm.
    */
   private JGTIGraph jGTIGraphConverted;
 
 
   /**
    * The original {@link JGTIGraph} containing the diagramm.
    */
   private JGTIGraph jGTIGraphOriginal;
 
 
   /**
    * The converted {@link DefaultMachineModel}.
    */
   private DefaultMachineModel modelConverted;
 
 
   /**
    * The original {@link DefaultMachineModel}.
    */
   private DefaultRegexModel modelOriginal;
 
 
   /**
    * The {@link RegexPanel}
    */
   private RegexPanel panel;
 
 
   /**
    * The parent {@link JFrame}
    */
   private JFrame parent;
 
 
   /**
    * The Position Map containing the position of a State
    */
   private HashMap < String, Position > positionMap;
 
 
   /**
    * An {@link ArrayList} of {@link DefaultPositionState}s
    */
   private ArrayList < DefaultPositionState > positionStates;
 
 
   /**
    * A {@link HashMap} containing the {@link DefaultStateView} for a
    * {@link DefaultPositionState}
    */
   private HashMap < DefaultPositionState, DefaultStateView > positionStateViewList = new HashMap < DefaultPositionState, DefaultStateView > ();
 
 
   /**
    * The {@link RegexNode}
    */
   private RegexNode regexNode;
 
 
   /**
    * The {@link ConvertMachineTableModel}.
    */
   private ConvertMachineTableModel convertMachineTableModel;
 
 
   /**
    * The state view list containing the start and final {@link DefaultStateView}
    * for a {@link RegexNode}
    */
   private HashMap < RegexNode, ArrayList < DefaultStateView > > stateViewList = new HashMap < RegexNode, ArrayList < DefaultStateView > > ();
 
 
   /**
    * The {@link StepItem} list.
    */
   private ArrayList < StepItem > stepItemList = new ArrayList < StepItem > ();
 
 
   /**
    * The {@link EntityType} that should be converted to.
    */
   private EntityType entityType;
 
 
   /**
    * The {@link ConvertMachineTableColumnModel}.
    */
   private ConvertMachineTableColumnModel tableColumnModel = new ConvertMachineTableColumnModel ();
 
 
   /**
    * Adds a outline comment.
    * 
    * @param prettyString The {@link PrettyString} to add.
    */
   private final void addOutlineComment ( PrettyString prettyString )
   {
     this.convertMachineTableModel.addRow ( prettyString );
     this.gui.jGTITableOutline.changeSelection ( this.convertMachineTableModel
         .getRowCount () - 1, ConvertMachineTableModel.OUTLINE_COLUMN, false,
         false );
   }
 
 
   /**
    * Creates new from {@link ConvertRegexToMachineDialog}
    * 
    * @param parent The parent {@link JFrame}
    * @param panel The {@link RegexPanel}
    */
   public ConvertRegexToMachineDialog ( JFrame parent, RegexPanel panel )
   {
     this.parent = parent;
     this.panel = panel;
   }
 
 
   /**
    * Cancels the auto step timer.
    */
   private final void cancelAutoStepTimer ()
   {
     if ( this.autoStepTimer != null )
     {
       this.autoStepTimer.cancel ();
       this.autoStepTimer = null;
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Converter#convert(de.unisiegen.gtitool.core.entities.InputEntity.EntityType,
    *      de.unisiegen.gtitool.core.entities.InputEntity.EntityType, boolean)
    */
   public void convert ( @SuppressWarnings ( "unused" )
   EntityType fromEntityType, EntityType toEntityType,
       @SuppressWarnings ( "unused" )
       boolean complete )
   {
     this.gui = new ConvertRegexToMachineDialogForm ( this, this.parent );
 
     this.entityType = toEntityType;
     Alphabet a = this.panel.getRegex ().getAlphabet ();
     this.defaultRegex = this.panel.getRegex ().clone ();
     if ( this.entityType.equals ( MachineType.ENFA ) )
     {
       this.defaultRegex.setRegexNode ( this.defaultRegex.getRegexNode ().toCoreSyntax (), this.defaultRegex.getRegexNode ().toCoreSyntax ().toString ());
       this.regexNode = this.defaultRegex.getRegexNode ();
       this.modelConverted = new DefaultMachineModel ( new DefaultENFA ( a, a,
           false ) );
       this.gui
           .setTitle ( Messages
               .getString (
                   "ConvertRegexToMachineDialog.Title", Messages.getString ( "ConvertRegexToMachineDialog.ENFA" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
     }
     else if ( this.entityType.equals ( MachineType.DFA ) )
     {
       this.modelConverted = new DefaultMachineModel ( new DefaultDFA ( a, a,
           false ) );
       this.positionStates = new ArrayList < DefaultPositionState > ();
 
       this.defaultRegex.setRegexNode ( new ConcatenationNode (
           this.defaultRegex.getRegexNode ().toCoreSyntax (), new TokenNode ( "#" ) ), //$NON-NLS-1$
           this.defaultRegex.getRegexNode ().toCoreSyntax ().toString () );
       this.regexNode = this.defaultRegex.getRegexNode ();
       this.gui
           .setTitle ( Messages
               .getString (
                   "ConvertRegexToMachineDialog.Title", Messages.getString ( "ConvertRegexToMachineDialog.DFA" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
     }
     else
     {
       throw new RuntimeException ( "unsupported convert to Type" ); //$NON-NLS-1$
     }
     this.modelOriginal = new DefaultRegexModel ( this.defaultRegex );
     this.modelOriginal.initializeGraph ();
     this.modelOriginal.createTree ();
 
     this.convertMachineTableModel = new ConvertMachineTableModel ();
     this.gui.jGTITableOutline.setModel ( this.convertMachineTableModel );
     this.gui.jGTITableOutline.setColumnModel ( this.tableColumnModel );
     this.gui.jGTITableOutline.getTableHeader ().setReorderingAllowed ( false );
     this.gui.jGTITableOutline.getSelectionModel ().setSelectionMode (
         ListSelectionModel.SINGLE_SELECTION );
 
     this.jGTIGraphOriginal = this.modelOriginal.getJGTIGraph ();
 
     this.jGTIGraphOriginal.setEnabled ( false );
     this.gui.jGTIScrollPaneOriginal.setViewportView ( this.jGTIGraphOriginal );
 
     this.jGTIGraphConverted = this.modelConverted.getJGTIGraph ();
     this.jGTIGraphConverted.setEnabled ( false );
     this.gui.jGTIScrollPaneConverted.setViewportView ( this.jGTIGraphConverted );
 
     ArrayList < RegexNode > lastPos = this.regexNode.lastPos ();
     this.count = ( ( LeafNode ) lastPos.get ( lastPos.size () - 1 ) )
         .getPosition () + 1;
     this.positionMap = new HashMap < String, Position > ();
 
     while ( !this.endReached )
     {
       performNextStep ( false );
     }
     new LayoutManager ( this.modelConverted, null ).doLayout ();
     for ( DefaultStateView current : this.modelConverted.getStateViewList () )
     {
       int yOffset = current.getState ().isLoopTransition () ? StateView.LOOP_TRANSITION_OFFSET
           : 0;
       this.positionMap.put ( current.getState ().getName (), new Position (
           current.getPositionX (), current.getPositionY () + yOffset ) );
     }
     while ( !this.stepItemList.isEmpty () )
     {
       performPreviousStep ( false );
     }
     this.endReached = this.regexNode.isMarked ();
     setStatus ();
     show ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see LogicClass#getGUI()
    */
   public ConvertRegexToMachineDialogForm getGUI ()
   {
     return this.gui;
   }
 
 
   /**
    * Returns the panel.
    * 
    * @return The panel.
    * @see #panel
    */
   public RegexPanel getRegexPanel ()
   {
     return this.panel;
   }
 
 
   /**
    * Returns the modelConverted.
    * 
    * @return The modelConverted.
    * @see #modelConverted
    */
   public DefaultMachineModel getModelConverted ()
   {
     return this.modelConverted;
   }
 
 
   /**
    * Returns the modelOriginal.
    * 
    * @return The modelOriginal.
    * @see #modelOriginal
    */
   public DefaultRegexModel getModelOriginal ()
   {
     return this.modelOriginal;
   }
 
 
   /**
    * Returns the convertMachineTableModel.
    * 
    * @return The convertMachineTableModel.
    * @see #convertMachineTableModel
    */
   public ConvertMachineTableModel getConvertMachineTableModel ()
   {
     return this.convertMachineTableModel;
   }
 
 
   /**
    * Returns the tableColumnModel.
    * 
    * @return The tableColumnModel.
    * @see #tableColumnModel
    */
   public ConvertMachineTableColumnModel getTableColumnModel ()
   {
     return this.tableColumnModel;
   }
 
 
   /**
    * Returns the next unmarked PositionState
    * 
    * @return The next unmarked PositionState
    */
   private DefaultPositionState getNextUnmarkedState ()
   {
     for ( DefaultPositionState state : this.positionStates )
     {
       if ( !state.isMarked () )
       {
         return state;
       }
     }
     return null;
   }
 
 
   /**
    * Returns the position of the {@link State} or null if the position is not
    * defined.
    * 
    * @param state The {@link State}.
    * @return The position of the {@link State} or null if the position is not
    *         defined.
    */
   private final Position getPosition ( State state )
   {
     return this.positionMap.get ( state.getName () );
   }
 
 
   /**
    * Handles the action on the auto step button.
    */
   public void handleAutoStep ()
   {
     logger.debug ( "handleAutoStep", "handle auto step" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     setStatus ();
 
     startAutoStepTimer ();
   }
 
 
   /**
    * Handles the action on the begin step button.
    */
   public void handleBeginStep ()
   {
     while ( !this.stepItemList.isEmpty () )
     {
       handlePreviousStep ();
     }
   }
 
 
   /**
    * Handles the action on the cancel button.
    */
   public void handleCancel ()
   {
     this.regexNode.unmarkAll ();
     this.gui.dispose ();
   }
 
 
   /**
    * Handles the action on the end step button.
    */
   public void handleEndStep ()
   {
     while ( !this.endReached )
     {
       handleNextStep ();
     }
   }
 
 
   /**
    * Handles the action on the next step button.
    */
   public void handleNextStep ()
   {
     performNextStep ( true );
     setStatus ();
   }
 
 
   /**
    * Handles the action on the ok button.
    */
   public void handleOk ()
   {
     logger.debug ( "handleOk", "handle ok" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     cancelAutoStepTimer ();
 
     this.gui.setVisible ( false );
 
     while ( !this.endReached )
     {
       performNextStep ( false );
     }

     this.panel.getMainWindow ().handleNew ( this.modelConverted.getElement (),
         true );
 
     this.regexNode.unmarkAll ();
     this.gui.dispose ();
   }
 
 
   /**
    * Handles the action on the previous step button.
    */
   public void handlePreviousStep ()
   {
     performPreviousStep ( true );
   }
 
 
   /**
    * Handles the action on the print button.
    */
   public void handlePrint ()
   {
     PrintDialog dialog = new PrintDialog ( this.parent, this );
     dialog.show ();
   }
 
 
   /**
    * Handles the action on the stop button.
    */
   public void handleStop ()
   {
     logger.debug ( "handleStop", "handle stop" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     cancelAutoStepTimer ();
 
     this.gui.jGTIToolBarToggleButtonAutoStep.setSelected ( false );
     setStatus ();
   }
 
 
   /**
    * Performs the next step.
    * 
    * @param manual Indicates if step was made manually
    */
   private final void performNextStep ( boolean manual )
   {
     ArrayList < String > addedStates = new ArrayList < String > ();
     ArrayList < DefaultStateView > setFinalFalse = new ArrayList < DefaultStateView > ();
     ArrayList < DefaultStateView > setStartFalse = new ArrayList < DefaultStateView > ();
     ArrayList < DefaultTransitionView > addedTransitions = new ArrayList < DefaultTransitionView > ();
     int c = this.count;
     RedoUndoItem redoUndoItem = null;
     boolean errorCreated = false;
     DefaultPositionState markedPositionState = null;
     Symbol controlledSymbol = null;
     RegexNode node = null;
     ArrayList < ObjectPair < DefaultTransitionView, Symbol > > addedSymbolsToTransition = new ArrayList < ObjectPair < DefaultTransitionView, Symbol > > ();
 
     PrettyString pretty = new PrettyString ();
     if ( this.entityType.equals ( MachineType.ENFA ) )
     {
       node = this.regexNode.getNextNodeForNFA ();
 
       // Token
       if ( node instanceof TokenNode )
       {
         this.actualStep = Step.CONVERT_TOKEN;
         TokenNode token = ( TokenNode ) node;
 
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepConvertToken", token.toPrettyString () ) ); //$NON-NLS-1$
 
         DefaultState start;
         try
         {
           start = new DefaultState ( "s" + token.getPosition () ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         start.setStartState ( true );
         // this.modelConverted.getMachine ().addState ( start );
         DefaultStateView startView = this.modelConverted.createStateView ( 0,
             0, start, false );
         Position p = getPosition ( start );
         if ( p != null )
         {
           startView.move ( p.getX (), p.getY () );
         }
 
         DefaultState fin;
         try
         {
           fin = new DefaultState ( "f" + token.getPosition () ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         fin.setFinalState ( true );
         // this.modelConverted.getMachine ().addState ( fin );
         DefaultStateView finView = this.modelConverted.createStateView ( 0, 0,
             fin, false );
         p = getPosition ( fin );
         if ( p != null )
         {
           finView.move ( p.getX (), p.getY () );
         }
         addedStates.add ( startView.getState ().getName () );
         addedStates.add ( finView.getState ().getName () );
 
         ArrayList < DefaultStateView > views = new ArrayList < DefaultStateView > ();
         views.add ( startView );
         views.add ( finView );
         this.stateViewList.put ( token, views );
 
         ArrayList < Symbol > symbols = new ArrayList < Symbol > ();
         symbols.add ( new DefaultSymbol ( token.getName () ) );
         Transition t;
         Alphabet a = this.modelOriginal.getRegex ().getAlphabet ();
         try
         {
           t = new DefaultTransition ( a, a, new DefaultWord (),
               new DefaultWord (), start, fin, symbols );
 
           addedTransitions.add ( this.modelConverted.createTransitionView ( t,
               startView, finView, true, false, true ) );
         }
         catch ( TransitionSymbolNotInAlphabetException exc )
         {
           exc.printStackTrace ();
         }
         catch ( TransitionSymbolOnlyOneTimeException exc )
         {
           exc.printStackTrace ();
         }
       }
       // Epsilon
       if ( node instanceof EpsilonNode )
       {
         this.actualStep = Step.CONVERT_EPSILON;
         EpsilonNode token = ( EpsilonNode ) node;
 
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepConvertEpsilon", token.toPrettyString () ) ); //$NON-NLS-1$
 
         DefaultState start;
         try
         {
           start = new DefaultState ( "s" + token.getPosition () ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         start.setStartState ( true );
         // this.modelConverted.getMachine ().addState ( start );
         DefaultStateView startView = this.modelConverted.createStateView ( 0,
             0, start, false );
         Position p = getPosition ( start );
         if ( p != null )
         {
           startView.move ( p.getX (), p.getY () );
         }
 
         DefaultState fin;
         try
         {
           fin = new DefaultState ( "f" + token.getPosition () ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         fin.setFinalState ( true );
         // this.modelConverted.getMachine ().addState ( fin );
         DefaultStateView finView = this.modelConverted.createStateView ( 0, 0,
             fin, false );
         p = getPosition ( fin );
         if ( p != null )
         {
           finView.move ( p.getX (), p.getY () );
         }
         addedStates.add ( startView.getState ().getName () );
         addedStates.add ( finView.getState ().getName () );
 
         ArrayList < DefaultStateView > views = new ArrayList < DefaultStateView > ();
         views.add ( startView );
         views.add ( finView );
         this.stateViewList.put ( token, views );
 
         ArrayList < Symbol > symbols = new ArrayList < Symbol > ();
         symbols.add ( new DefaultSymbol () );
         Transition t;
         Alphabet a = this.modelOriginal.getRegex ().getAlphabet ();
         try
         {
           t = new DefaultTransition ( a, a, new DefaultWord (),
               new DefaultWord (), start, fin, symbols );
 
           addedTransitions.add ( this.modelConverted.createTransitionView ( t,
               startView, finView, true, false, true ) );
         }
         catch ( TransitionSymbolNotInAlphabetException exc )
         {
           exc.printStackTrace ();
         }
         catch ( TransitionSymbolOnlyOneTimeException exc )
         {
           exc.printStackTrace ();
         }
       }
       // Disjunction
       if ( node instanceof DisjunctionNode )
       {
         this.actualStep = Step.CONVERT_DISJUNCTION;
         DisjunctionNode dis = ( DisjunctionNode ) node;
 
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepConvertDisjunction", dis.toPrettyString () ) ); //$NON-NLS-1$
 
         DefaultStateView start1 = this.stateViewList.get (
             dis.getChildren ().get ( 0 ) ).get ( 0 );
         DefaultStateView start2 = this.stateViewList.get (
             dis.getChildren ().get ( 1 ) ).get ( 0 );
         DefaultStateView final1 = this.stateViewList.get (
             dis.getChildren ().get ( 0 ) ).get ( 1 );
         DefaultStateView final2 = this.stateViewList.get (
             dis.getChildren ().get ( 1 ) ).get ( 1 );
 
         if ( final1 == null || final2 == null || start1 == null
             || start2 == null )
         {
           throw new IllegalArgumentException ( "A State is null" ); //$NON-NLS-1$
         }
 
         DefaultState start;
         try
         {
           start = new DefaultState ( "s" + this.count ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         start.setStartState ( true );
         // this.modelConverted.getMachine ().addState ( start );
 
         DefaultState fin;
         try
         {
           fin = new DefaultState ( "f" + this.count++ ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         fin.setFinalState ( true );
         // this.modelConverted.getMachine ().addState ( fin );
 
         DefaultTransition startStart1 = new DefaultTransition ();
         startStart1.setStateBegin ( start );
         startStart1.setStateEnd ( start1.getState () );
         DefaultTransition startStart2 = new DefaultTransition ();
         startStart2.setStateBegin ( start );
         startStart2.setStateEnd ( start2.getState () );
         DefaultTransition final1Final = new DefaultTransition ();
         final1Final.setStateBegin ( final1.getState () );
         final1Final.setStateEnd ( fin );
         DefaultTransition final2Final = new DefaultTransition ();
         final2Final.setStateBegin ( final2.getState () );
         final2Final.setStateEnd ( fin );
 
         DefaultStateView startView = this.modelConverted.createStateView ( 0,
             0, start, false );
         DefaultStateView finView = this.modelConverted.createStateView ( 0, 0,
             fin, false );
         Position p = getPosition ( start );
         if ( p != null )
         {
           startView.move ( p.getX (), p.getY () );
         }
         p = getPosition ( fin );
         if ( p != null )
         {
           finView.move ( p.getX (), p.getY () );
         }
         addedStates.add ( startView.getState ().getName () );
         addedStates.add ( finView.getState ().getName () );
 
         start1.getState ().setStartState ( false );
         start2.getState ().setStartState ( false );
         final1.getState ().setFinalState ( false );
         final2.getState ().setFinalState ( false );
         setStartFalse.add ( start1 );
         setStartFalse.add ( start2 );
         setFinalFalse.add ( final1 );
         setFinalFalse.add ( final2 );
 
         addedTransitions.add ( this.modelConverted.createTransitionView (
             startStart1, startView, start1, true, false, true ) );
         addedTransitions.add ( this.modelConverted.createTransitionView (
             startStart2, startView, start2, true, false, true ) );
         addedTransitions.add ( this.modelConverted.createTransitionView (
             final1Final, final1, finView, true, false, true ) );
         addedTransitions.add ( this.modelConverted.createTransitionView (
             final2Final, final2, finView, true, false, true ) );
 
         ArrayList < DefaultStateView > views = new ArrayList < DefaultStateView > ();
         views.add ( startView );
         views.add ( finView );
 
         this.stateViewList.put ( dis, views );
       }
       // Concatenation
       if ( node instanceof ConcatenationNode )
       {
         this.actualStep = Step.CONVERT_CONCAT;
         ConcatenationNode con = ( ConcatenationNode ) node;
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepConvertConcatenation", con.toPrettyString () ) ); //$NON-NLS-1$
 
         DefaultStateView start1 = this.stateViewList.get (
             con.getChildren ().get ( 0 ) ).get ( 0 );
         DefaultStateView start2 = this.stateViewList.get (
             con.getChildren ().get ( 1 ) ).get ( 0 );
         DefaultStateView final1 = this.stateViewList.get (
             con.getChildren ().get ( 0 ) ).get ( 1 );
         DefaultStateView final2 = this.stateViewList.get (
             con.getChildren ().get ( 1 ) ).get ( 1 );
 
         final1.getState ().setFinalState ( false );
         setFinalFalse.add ( final1 );
 
         for ( Transition t : start2.getState ().getTransitionBegin () )
         {
           try
           {
             addedTransitions.add ( this.modelConverted.createTransitionView (
                 new DefaultTransition ( t.getAlphabet (), t.getAlphabet (), t
                     .getPushDownWordRead (), t.getPushDownWordWrite (), final1
                     .getState (), t.getStateEnd (), t.getSymbol () ), final1,
                 this.modelConverted.getStateViewForState ( t.getStateEnd () ),
                 true, false, true ) );
 
           }
           catch ( TransitionSymbolNotInAlphabetException exc )
           {
             exc.printStackTrace ();
           }
           catch ( TransitionSymbolOnlyOneTimeException exc )
           {
             exc.printStackTrace ();
           }
         }
 
         redoUndoItem = this.modelConverted.removeState ( start2, false );
 
         ArrayList < DefaultStateView > views = new ArrayList < DefaultStateView > ();
         views.add ( start1 );
         views.add ( final2 );
 
         this.stateViewList.put ( con, views );
       }
       // Kleene Node
       if ( node instanceof KleeneNode )
       {
         this.actualStep = Step.CONVERT_KLEENE;
         KleeneNode k = ( KleeneNode ) node;
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepConvertKleene", k.toPrettyString () ) ); //$NON-NLS-1$
 
         DefaultStateView start1 = this.stateViewList.get (
             k.getChildren ().get ( 0 ) ).get ( 0 );
         DefaultStateView final1 = this.stateViewList.get (
             k.getChildren ().get ( 0 ) ).get ( 1 );
 
         DefaultState start;
         try
         {
           start = new DefaultState ( "s" + this.count ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         start.setStartState ( true );
         // this.modelConverted.getMachine ().addState ( start );
 
         DefaultState fin;
         try
         {
           fin = new DefaultState ( "f" + this.count++ ); //$NON-NLS-1$
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         fin.setFinalState ( true );
         // this.modelConverted.getMachine ().addState ( fin );
 
         DefaultStateView startView = this.modelConverted.createStateView ( 0,
             0, start, false );
         DefaultStateView finView = this.modelConverted.createStateView ( 0, 0,
             fin, false );
         Position p = getPosition ( start );
         if ( p != null )
         {
           startView.move ( p.getX (), p.getY () );
         }
         p = getPosition ( fin );
         if ( p != null )
         {
           finView.move ( p.getX (), p.getY () );
         }
         addedStates.add ( startView.getState ().getName () );
         addedStates.add ( finView.getState ().getName () );
 
         // From start to final
         Transition t = new DefaultTransition ();
         t.setStateBegin ( start );
         t.setStateEnd ( fin );
         addedTransitions.add ( this.modelConverted.createTransitionView ( t,
             startView, finView, true, false, true ) );
 
         // From start to begin of N(s)
         t = new DefaultTransition ();
         t.setStateBegin ( start );
         t.setStateEnd ( start1.getState () );
         addedTransitions.add ( this.modelConverted.createTransitionView ( t,
             startView, start1, true, false, true ) );
 
         // From end to begin of N(s)
         t = new DefaultTransition ();
         t.setStateBegin ( final1.getState () );
         t.setStateEnd ( start1.getState () );
         addedTransitions.add ( this.modelConverted.createTransitionView ( t,
             final1, start1, true, false, true ) );
 
         // From end of N(s) to final
         t = new DefaultTransition ();
         t.setStateBegin ( final1.getState () );
         t.setStateEnd ( fin );
         addedTransitions.add ( this.modelConverted.createTransitionView ( t,
             final1, finView, true, false, true ) );
 
         start1.getState ().setStartState ( false );
         final1.getState ().setFinalState ( false );
         setStartFalse.add ( start1 );
         setFinalFalse.add ( final1 );
 
         ArrayList < DefaultStateView > views = new ArrayList < DefaultStateView > ();
         views.add ( startView );
         views.add ( finView );
 
         this.stateViewList.put ( k, views );
       }
 
       this.endReached = this.regexNode.isMarked ();
 
       if ( manual )
       {
         setStatus ();
         updateGraph ();
       }
     }
     // DFA
     else if ( this.entityType.equals ( MachineType.DFA ) )
     {
       this.actualStep = Step.INITIAL;
       // Initial Step
       if ( this.positionStates.isEmpty () )
       {
         HashSet < Integer > positions = new HashSet < Integer > ();
         String name = ""; //$NON-NLS-1$
         ArrayList < RegexNode > firstPos = this.defaultRegex.getRegexNode ()
             .firstPos ();
         for ( RegexNode tmpNode : firstPos )
         {
           LeafNode leaf = ( LeafNode ) tmpNode;
           positions.add ( new Integer ( leaf.getPosition () ) );
           name += leaf.getPosition ();
 
         }
         DefaultPositionState state;
         try
         {
           state = new DefaultPositionState ( name, positions );
         }
         catch ( StateException exc )
         {
           exc.printStackTrace ();
           System.exit ( 1 );
           return;
         }
         this.positionStates.add ( state );
         state.setStartState ( true );
         DefaultStateView view = this.modelConverted.createStateView ( 0, 0,
             state, false );
         this.positionStateViewList.put ( state, view );
         addedStates.add ( state.getName () );
         Position p = getPosition ( state );
         if ( p != null )
         {
           view.move ( p.getX (), p.getY () );
         }
         pretty
             .add ( Messages
                 .getPrettyString (
                     "ConvertRegexToMachineDialog.StepCreateStartState", this.regexNode.toPrettyString (), new PrettyString ( new PrettyToken ( name, Style.NONE ) ) ) ); //$NON-NLS-1$
 
       }
       // Now the algorithm goes
       else
       {
         DefaultPositionState positionState = getNextUnmarkedState ();
         if ( positionState == null )
         {
           throw new RuntimeException ( "Internal Error: PositionState is null" ); //$NON-NLS-1$
         }
         if ( !this.controlledSymbols.containsKey ( positionState ) )
         {
           this.controlledSymbols.put ( positionState,
               new ArrayList < Symbol > () );
         }
         markedPositionState = positionState;
         Symbol a = getNextUnControlledSymbol ( positionState );
         controlledSymbol = a;
         if ( a != null )
         {
           this.controlledSymbols.get ( positionState ).add ( a );
           HashSet < Integer > u = new HashSet < Integer > ();
           for ( Integer p : positionState.getPositions () )
           {
             if ( a.getName ().equals (
                 this.defaultRegex.symbolAtPosition ( p.intValue () ) ) )
             {
               for ( Integer n : this.defaultRegex.followPos ( p.intValue () ) )
               {
                 u.add ( n );
               }
             }
           }
           String name = ""; //$NON-NLS-1$
           for ( Integer i : u )
           {
             name += i;
           }
           DefaultPositionState uState;
           try
           {
             uState = new DefaultPositionState ( name, u );
           }
           catch ( StateException exc )
           {
             exc.printStackTrace ();
             System.exit ( 1 );
             return;
           }
 
           if ( !u.isEmpty () )
           {
             if ( !this.positionStates.contains ( uState ) )
             {
               LeafNode end = ( LeafNode ) this.regexNode.lastPos ().get ( 0 );
               if ( uState.getPositions ().contains (
                   new Integer ( end.getPosition () ) ) )
               {
                 uState.setFinalState ( true );
               }
               this.positionStates.add ( uState );
               DefaultStateView view = this.modelConverted.createStateView ( 0,
                   0, uState, false );
               this.positionStateViewList.put ( uState, view );
               addedStates.add ( uState.getName () );
               Position p = getPosition ( uState );
               if ( p != null )
               {
                 view.move ( p.getX (), p.getY () );
               }
             }
 
           }
           else
           {
             try
             {
              uState.setName ( Messages
                  .getString ( "ConvertRegexToMachineDialog.ErrorState" ) ); //$NON-NLS-1$
             }
             catch ( StateException exc )
             {
               exc.printStackTrace ();
               System.exit ( 1 );
               return;
             }
             if ( !this.emptyStateCreated )
             {
               errorCreated = true;
               this.positionStates.add ( uState );
               DefaultStateView view = this.modelConverted.createStateView ( 0,
                   0, uState, false );
               this.positionStateViewList.put ( uState, view );
               this.emptyStateCreated = true;
               addedStates.add ( uState.getName () );
               Position p = getPosition ( uState );
               if ( p != null )
               {
                 view.move ( p.getX (), p.getY () );
               }
             }
           }
           if ( this.positionStateViewList.get ( uState ) != null
               && this.positionStateViewList.get ( positionState ) != null )
           {
             ArrayList < Symbol > s = new ArrayList < Symbol > ();
             s.add ( a );
             try
             {
               DefaultTransitionView old = null;
               for ( DefaultTransitionView t : this.modelConverted
                   .getTransitionViewList () )
               {
                 if ( t.getSourceView ().equals (
                     this.positionStateViewList.get ( positionState ) )
                     && t.getTargetView ().equals (
                         this.positionStateViewList.get ( uState ) ) )
                 {
                   old = t;
                 }
               }
               if ( old == null )
               {
                 addedTransitions.add ( this.modelConverted
                     .createTransitionView ( new DefaultTransition (
                         this.defaultRegex.getAlphabet (), this.defaultRegex
                             .getAlphabet (), new DefaultWord (),
                         new DefaultWord (), this.positionStateViewList.get (
                             positionState ).getState (),
                         this.positionStateViewList.get ( uState ).getState (),
                         a ), this.positionStateViewList.get ( positionState ),
                         this.positionStateViewList.get ( uState ), true, false,
                         true ) );
                 pretty
                     .add ( Messages
                         .getPrettyString (
                             "ConvertRegexToMachineDialog.StepTransitionForSymbol", a.toPrettyString () ) ); //$NON-NLS-1$
 
               }
               else
               {
                 pretty
                     .add ( Messages
                         .getPrettyString (
                             "ConvertRegexToMachineDialog.StepSymbolToTransition", a.toPrettyString (), old.getTransition ().toPrettyString () ) ); //$NON-NLS-1$
                 old.getTransition ().add ( a );
                 addedSymbolsToTransition
                     .add ( new ObjectPair < DefaultTransitionView, Symbol > (
                         old, a ) );
               }
             }
             catch ( TransitionSymbolNotInAlphabetException exc )
             {
               exc.printStackTrace ();
               System.exit ( 1 );
               return;
             }
             catch ( TransitionSymbolOnlyOneTimeException exc )
             {
               exc.printStackTrace ();
               System.exit ( 1 );
               return;
             }
           }
 
         }
         else
         {
           positionState.mark ();
           pretty
               .add ( Messages
                   .getPrettyString (
                       "ConvertRegexToMachineDialog.StepMarkState", positionState.toPrettyString () ) ); //$NON-NLS-1$
 
         }
       }
 
       boolean ready = true;
       for ( DefaultPositionState state : this.positionStates )
       {
         if ( !state.isMarked () )
         {
           ready = false;
         }
       }
       if ( this.positionStates.isEmpty () )
       {
         ready = false;
       }
 
       this.endReached = ready;
 
       if ( manual )
       {
         setStatus ();
         updateGraph ();
       }
     }
     addOutlineComment ( pretty );
     this.stepItemList.add ( new StepItem ( this.actualStep, addedStates,
         redoUndoItem, addedTransitions, setStartFalse, setFinalFalse, c, node,
         errorCreated, markedPositionState, controlledSymbol,
         addedSymbolsToTransition ) );
   }
 
 
   /**
    * HashMap for the controlled {@link Symbol}s for a
    * {@link DefaultPositionState}
    */
   private HashMap < DefaultPositionState, ArrayList < Symbol > > controlledSymbols = new HashMap < DefaultPositionState, ArrayList < Symbol > > ();
 
 
   /**
    * Returns the next uncontrolled {@link Symbol} for a given
    * {@link DefaultPositionState}
    * 
    * @param state The {@link DefaultPositionState}
    * @return The next uncontrolled {@link Symbol} for a given
    *         {@link DefaultPositionState}
    */
   private Symbol getNextUnControlledSymbol ( DefaultPositionState state )
   {
     ArrayList < Symbol > c = this.controlledSymbols.get ( state );
     ArrayList < Symbol > rest = new ArrayList < Symbol > ();
     rest.addAll ( this.defaultRegex.getAlphabet ().get () );
     rest.removeAll ( c );
     rest.remove ( new DefaultSymbol ( "#" ) ); //$NON-NLS-1$
     try
     {
       return rest.get ( 0 );
     }
     catch ( IndexOutOfBoundsException e )
     {
       return null;
     }
   }
 
 
   /**
    * Performs previous step
    * 
    * @param manual Indicates if step was made manually
    */
   private void performPreviousStep ( boolean manual )
   {
     StepItem stepItem = this.stepItemList
         .remove ( this.stepItemList.size () - 1 );
 
     for ( String name : stepItem.getAddedStates () )
     {
       for ( int i = 0 ; i < this.modelConverted.getStateViewList ().size () ; i++ )
       {
         if ( this.modelConverted.getStateViewList ().get ( i ).getState ()
             .getName ().equals ( name ) )
         {
           if ( this.entityType.equals ( MachineType.DFA ) )
           {
             this.positionStates.remove ( this.modelConverted
                 .getStateViewList ().get ( i ).getState () );
           }
           this.modelConverted.removeState ( this.modelConverted
               .getStateViewList ().get ( i ), false );
         }
       }
     }
 
     for ( DefaultStateView view : stepItem.getSetFinalFalse () )
     {
       view.getState ().setFinalState ( true );
     }
     for ( DefaultStateView view : stepItem.getSetStartFalse () )
     {
       view.getState ().setStartState ( true );
     }
     for ( DefaultTransitionView view : stepItem.getAddedTransitions () )
     {
       this.modelConverted.removeTransition ( view, false );
     }
     if ( stepItem.getActiveStep ().equals ( Step.CONVERT_CONCAT ) )
     {
       stepItem.getRedoUndoItem ().undo ();
     }
     if ( stepItem.isErrorCreated () )
     {
       this.emptyStateCreated = false;
     }
     DefaultPositionState posState = stepItem.getMarkedPositionState ();
     if ( posState != null )
     {
       posState.unMark ();
     }
     Symbol s = stepItem.getControlledSymbol ();
     if ( s != null )
     {
       this.controlledSymbols.get ( posState ).remove ( s );
     }
     for ( ObjectPair < DefaultTransitionView, Symbol > pair : stepItem
         .getAddedSymbolsToTransition () )
     {
       pair.getFirst ().getTransition ().remove ( pair.getSecond () );
     }
 
     this.actualStep = stepItem.getActiveStep ();
 
     RegexNode actNode = stepItem.getActNode ();
     if ( actNode != null )
     {
       actNode.unmark ();
     }
 
     // outline
     this.convertMachineTableModel.removeLastRow ();
     this.gui.jGTITableOutline.changeSelection ( this.convertMachineTableModel
         .getRowCount () - 1, ConvertMachineTableModel.OUTLINE_COLUMN, false,
         false );
 
     this.count = stepItem.getActCount ();
 
     if ( manual )
     {
       setStatus ();
       updateGraph ();
     }
   }
 
 
   /**
    * Sets the button status.
    */
   private final void setStatus ()
   {
     if ( this.entityType.equals ( MachineType.ENFA ) )
     {
       this.endReached = this.regexNode.isMarked ();
     }
     else
     {
       boolean ready = true;
       for ( DefaultPositionState state : this.positionStates )
       {
         if ( !state.isMarked () )
         {
           ready = false;
         }
       }
       if ( this.positionStates.isEmpty () )
       {
         ready = false;
       }
 
       this.endReached = ready;
     }
     if ( this.gui.jGTIToolBarToggleButtonAutoStep.isSelected () )
     {
       this.gui.jGTIToolBarButtonBeginStep.setEnabled ( false );
       this.gui.jGTIToolBarButtonPreviousStep.setEnabled ( false );
       this.gui.jGTIToolBarButtonNextStep.setEnabled ( false );
       this.gui.jGTIToolBarToggleButtonAutoStep.setEnabled ( false );
       this.gui.jGTIToolBarButtonStop.setEnabled ( true );
       this.gui.jGTIToolBarButtonEndStep.setEnabled ( false );
     }
     else
     {
       boolean beginReached = this.stepItemList.isEmpty ();
       this.gui.jGTIToolBarButtonBeginStep.setEnabled ( !beginReached );
       this.gui.jGTIToolBarButtonPreviousStep.setEnabled ( !beginReached );
       this.gui.jGTIToolBarButtonNextStep.setEnabled ( !this.endReached );
       this.gui.jGTIToolBarToggleButtonAutoStep.setEnabled ( !this.endReached );
       this.gui.jGTIToolBarButtonStop.setEnabled ( false );
       this.gui.jGTIToolBarButtonEndStep.setEnabled ( !this.endReached );
     }
   }
 
 
   /**
    * Shows the {@link ConvertMachineDialogForm}.
    */
   public final void show ()
   {
     logger.debug ( "show", "show the convert machine dialog" ); //$NON-NLS-1$ //$NON-NLS-2$
 
     Rectangle rect = PreferenceManager.getInstance ()
         .getConvertMachineDialogBounds ();
     if ( ( rect.x == PreferenceManager.DEFAULT_CONVERT_MACHINE_DIALOG_POSITION_X )
         || ( rect.y == PreferenceManager.DEFAULT_CONVERT_MACHINE_DIALOG_POSITION_Y ) )
     {
       rect.x = this.parent.getBounds ().x + ( this.parent.getWidth () / 2 )
           - ( this.gui.getWidth () / 2 );
       rect.y = this.parent.getBounds ().y + ( this.parent.getHeight () / 2 )
           - ( this.gui.getHeight () / 2 );
     }
     this.gui.setBounds ( rect );
     this.gui.setVisible ( true );
   }
 
 
   /**
    * Starts the auto step timer.
    */
   @SuppressWarnings ( "synthetic-access" )
   private final void startAutoStepTimer ()
   {
     cancelAutoStepTimer ();
 
     this.autoStepTimer = new Timer ();
     int time = PreferenceManager.getInstance ().getAutoStepItem ()
         .getAutoStepInterval ();
     this.autoStepTimer.schedule ( new AutoStepTimerTask (), time, time );
   }
 
 
   /**
    * Updates the {@link JGTIGraph}s.
    */
   private final void updateGraph ()
   {
     this.modelOriginal.getGraphModel ().cellsChanged (
         DefaultGraphModel.getAll ( this.modelOriginal.getGraphModel () ) );
     this.modelConverted.getGraphModel ().cellsChanged (
         DefaultGraphModel.getAll ( this.modelConverted.getGraphModel () ) );
   }
 }
