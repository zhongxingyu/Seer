 package de.unisiegen.gtitool.core.machines;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.State;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.entities.Transition;
 import de.unisiegen.gtitool.core.entities.Word;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineAllSymbolsException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineEpsilonTransitionException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineStateFinalException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineStateNameException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineStateNotReachableException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineStateStartException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.exceptions.machine.MachineValidationException;
 import de.unisiegen.gtitool.core.exceptions.word.WordException;
 import de.unisiegen.gtitool.core.exceptions.word.WordFinishedException;
 import de.unisiegen.gtitool.core.exceptions.word.WordNotAcceptedException;
 import de.unisiegen.gtitool.core.exceptions.word.WordResetedException;
 
 
 /**
  * The abstract class for all machines.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public abstract class AbstractMachine implements Machine
 {
 
   /**
    * This enum is used to indicate which validation elements should be checked
    * during a validation.
    * 
    * @author Christian Fehler
    */
   protected enum ValidationElement
   {
     /**
      * There is a {@link State}, which {@link Transition}s do not contain all
      * {@link Symbol}s.
      */
     ALL_SYMBOLS,
 
     /**
      * There is a {@link Transition} without a {@link Symbol}.
      */
     EPSILON_TRANSITION,
 
     /**
      * There is no final state defined.
      */
     FINAL_STATE,
 
     /**
      * There is more than one start state defined.
      */
     MORE_THAN_ONE_START_STATE,
 
     /**
      * There is no start state is defined.
      */
     NO_START_STATE,
 
     /**
      * There are {@link State}s with the same name.
      */
     STATE_NAME,
 
     /**
      * There is a {@link State} which is not reachable.
      */
     STATE_NOT_REACHABLE,
 
     /**
      * There is a {@link State} with {@link Transition}s with the same
      * {@link Symbol}.
      */
     SYMBOL_ONLY_ONE_TIME
   }
 
 
   /**
    * The active {@link State}s.
    */
   private TreeSet < State > activeStateSet;
 
 
   /**
    * The {@link Alphabet} of this <code>AbstractMachine</code>.
    */
   private Alphabet alphabet;
 
 
   /**
    * The current {@link State} id.
    */
   private int currentStateId = State.ID_NOT_DEFINED;
 
 
   /**
    * The current {@link Transition} id.
    */
   private int currentTransitionId = Transition.ID_NOT_DEFINED;
 
 
   /**
    * The history of this <code>AbstractMachine</code>.
    */
   private ArrayList < ArrayList < Transition > > history;
 
 
   /**
    * The list of the {@link State}s.
    */
   private ArrayList < State > stateList;
 
 
   /**
    * The list of the {@link Transition}.
    */
   private ArrayList < Transition > transitionList;
 
 
   /**
    * The current {@link Word}.
    */
   private Word word = null;
 
 
   /**
    * The validation element list.
    */
   private ArrayList < ValidationElement > validationElementList;
 
 
   /**
    * Allocates a new <code>AbstractMachine</code>.
    * 
    * @param pAlphabet The {@link Alphabet} of this <code>AbstractMachine</code>.
    * @param pValidationElements The validation elements which indicates which
    *          validation elements should be checked during a validation.
    */
   public AbstractMachine ( Alphabet pAlphabet,
       ValidationElement ... pValidationElements )
   {
     // Alphabet
     if ( pAlphabet == null )
     {
       throw new NullPointerException ( "alphabet is null" ); //$NON-NLS-1$
     }
     this.alphabet = pAlphabet;
     // Validation elements
     if ( pValidationElements == null )
     {
       throw new NullPointerException ( "validation elements is null" ); //$NON-NLS-1$
     }
     this.validationElementList = new ArrayList < ValidationElement > ();
     for ( ValidationElement current : pValidationElements )
     {
       this.validationElementList.add ( current );
     }
     // StateList
     this.stateList = new ArrayList < State > ();
     // TransitionList
     this.transitionList = new ArrayList < Transition > ();
     // ActiveStateSet
     this.activeStateSet = new TreeSet < State > ();
     // History
     this.history = new ArrayList < ArrayList < Transition > > ();
   }
 
 
   /**
    * Adds the {@link Transition}s to the history of this
    * <code>AbstractMachine</code>.
    * 
    * @param pTransitions The {@link Transition}s to add.
    */
   private final void addHistory ( Iterable < Transition > pTransitions )
   {
     if ( pTransitions == null )
     {
       throw new NullPointerException ( "transitions is null" ); //$NON-NLS-1$
     }
     if ( !pTransitions.iterator ().hasNext () )
     {
       throw new IllegalArgumentException ( "transitions is empty" ); //$NON-NLS-1$
     }
     ArrayList < Transition > list = new ArrayList < Transition > ();
     for ( Transition current : pTransitions )
     {
       list.add ( current );
     }
     this.history.add ( list );
   }
 
 
   /**
    * Adds the {@link State}s to this <code>AbstractMachine</code>.
    * 
    * @param pStates The {@link State}s to add.
    */
   public final void addState ( Iterable < State > pStates )
   {
     if ( pStates == null )
     {
       throw new NullPointerException ( "states is null" ); //$NON-NLS-1$
     }
     if ( !pStates.iterator ().hasNext () )
     {
       throw new IllegalArgumentException ( "states is empty" ); //$NON-NLS-1$
     }
     for ( State current : pStates )
     {
       addState ( current );
     }
   }
 
 
   /**
    * Adds the {@link State} to this <code>AbstractMachine</code>.
    * 
    * @param pState The {@link State} to add.
    */
   public final void addState ( State pState )
   {
     if ( pState == null )
     {
       throw new NullPointerException ( "state is null" ); //$NON-NLS-1$
     }
     if ( ( pState.isIdDefined () ) && ( this.stateList.contains ( pState ) ) )
     {
       throw new IllegalArgumentException ( "state is already added" ); //$NON-NLS-1$
     }
     if ( !this.alphabet.equals ( pState.getAlphabet () ) )
     {
       throw new IllegalArgumentException ( "not the same alphabet" ); //$NON-NLS-1$
     }
     if ( pState.getId () == State.ID_NOT_DEFINED )
     {
       pState.setId ( ++this.currentStateId );
     }
     else
     {
       if ( pState.getId () > this.currentStateId )
       {
         this.currentStateId = pState.getId ();
       }
     }
     pState.setDefaultName ();
     this.stateList.add ( pState );
     link ( pState );
   }
 
 
   /**
    * Adds the {@link State}s to this <code>AbstractMachine</code>.
    * 
    * @param pStates The {@link State}s to add.
    */
   public final void addState ( State ... pStates )
   {
     if ( pStates == null )
     {
       throw new NullPointerException ( "states is null" ); //$NON-NLS-1$
     }
     if ( pStates.length == 0 )
     {
       throw new IllegalArgumentException ( "states is empty" ); //$NON-NLS-1$
     }
     for ( State current : pStates )
     {
       addState ( current );
     }
   }
 
 
   /**
    * Adds the {@link Transition}s to this <code>AbstractMachine</code>.
    * 
    * @param pTransitions The {@link Transition}s to add.
    */
   public final void addTransition ( Iterable < Transition > pTransitions )
   {
     if ( pTransitions == null )
     {
       throw new NullPointerException ( "transitions is null" ); //$NON-NLS-1$
     }
     if ( !pTransitions.iterator ().hasNext () )
     {
       throw new IllegalArgumentException ( "transitions is empty" ); //$NON-NLS-1$
     }
     for ( Transition current : pTransitions )
     {
       addTransition ( current );
     }
   }
 
 
   /**
    * Adds the {@link Transition} to this <code>AbstractMachine</code>.
    * 
    * @param pTransition The {@link Transition} to add.
    */
   public final void addTransition ( Transition pTransition )
   {
     if ( pTransition == null )
     {
       throw new NullPointerException ( "transition is null" ); //$NON-NLS-1$
     }
     if ( ( pTransition.isIdDefined () )
         && ( this.transitionList.contains ( pTransition ) ) )
     {
       throw new IllegalArgumentException ( "transition is already added" ); //$NON-NLS-1$
     }
     if ( !this.alphabet.equals ( pTransition.getAlphabet () ) )
     {
       throw new IllegalArgumentException ( "not the same alphabet" ); //$NON-NLS-1$
     }
     if ( pTransition.getId () == Transition.ID_NOT_DEFINED )
     {
       pTransition.setId ( ++this.currentTransitionId );
     }
     else
     {
       if ( pTransition.getId () > this.currentTransitionId )
       {
        this.currentStateId = pTransition.getId ();
       }
     }
     this.transitionList.add ( pTransition );
     link ( pTransition );
   }
 
 
   /**
    * Adds the {@link Transition}s to this <code>AbstractMachine</code>.
    * 
    * @param pTransitions The {@link Transition}s to add.
    */
   public final void addTransition ( Transition ... pTransitions )
   {
     if ( pTransitions == null )
     {
       throw new NullPointerException ( "transitions is null" ); //$NON-NLS-1$
     }
     if ( pTransitions.length == 0 )
     {
       throw new IllegalArgumentException ( "transitions is empty" ); //$NON-NLS-1$
     }
     for ( Transition current : pTransitions )
     {
       addTransition ( current );
     }
   }
 
 
   /**
    * Checks if there is a {@link State}, which {@link Transition}s do not
    * contain all {@link Symbol}s.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkAllSymbols ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     for ( State currentState : this.getState () )
     {
       TreeSet < Symbol > currentSymbolSet = new TreeSet < Symbol > ();
       for ( Transition currentTransition : currentState.getTransitionBegin () )
       {
         currentSymbolSet.addAll ( currentTransition.getSymbol () );
       }
       TreeSet < Symbol > notUsedSymbolSet = new TreeSet < Symbol > ();
       for ( Symbol currentSymbol : this.getAlphabet () )
       {
         notUsedSymbolSet.add ( currentSymbol );
       }
       for ( Symbol currentSymbol : currentSymbolSet )
       {
         notUsedSymbolSet.remove ( currentSymbol );
       }
       if ( notUsedSymbolSet.size () > 0 )
       {
         machineExceptionList.add ( new MachineAllSymbolsException (
             currentState, notUsedSymbolSet ) );
       }
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is a {@link Transition} without a {@link Symbol}.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkEpsilonTransition ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     for ( Transition currentTransition : this.getTransition () )
     {
       if ( currentTransition.getSymbol ().size () == 0 )
       {
         machineExceptionList.add ( new MachineEpsilonTransitionException (
             currentTransition ) );
       }
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is no final state defined.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkFinalState ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     boolean found = false;
     loop : for ( State currentState : this.getState () )
     {
       if ( currentState.isFinalState () )
       {
         found = true;
         break loop;
       }
     }
     if ( !found )
     {
       machineExceptionList.add ( new MachineStateFinalException () );
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is more than one start state defined.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkMoreThanOneStartState ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     ArrayList < State > startStateList = new ArrayList < State > ();
     for ( State current : this.getState () )
     {
       if ( current.isStartState () )
 
       {
         startStateList.add ( current );
       }
     }
     if ( startStateList.size () >= 2 )
     {
       machineExceptionList.add ( new MachineStateStartException (
           startStateList ) );
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is no start state defined.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkNoStartState ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     ArrayList < State > startStateList = new ArrayList < State > ();
     for ( State current : this.getState () )
     {
       if ( current.isStartState () )
 
       {
         startStateList.add ( current );
       }
     }
     if ( startStateList.size () == 0 )
     {
       machineExceptionList.add ( new MachineStateStartException (
           startStateList ) );
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there are {@link State}s with the same name.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkStateName ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     ArrayList < State > duplicatedListAll = new ArrayList < State > ();
     firstLoop : for ( int i = 0 ; i < this.getState ().size () ; i++ )
     {
       if ( duplicatedListAll.contains ( this.getState ().get ( i ) ) )
       {
         continue firstLoop;
       }
       ArrayList < State > duplicatedListOne = new ArrayList < State > ();
       for ( int j = i + 1 ; j < this.getState ().size () ; j++ )
       {
         if ( this.getState ().get ( i ).getName ().equals (
             this.getState ().get ( j ).getName () ) )
         {
           duplicatedListOne.add ( this.getState ().get ( j ) );
         }
       }
       if ( duplicatedListOne.size () > 0 )
       {
         duplicatedListOne.add ( this.getState ().get ( i ) );
         for ( State current : duplicatedListOne )
         {
           duplicatedListAll.add ( current );
         }
         machineExceptionList.add ( new MachineStateNameException (
             duplicatedListOne ) );
       }
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is a {@link State} which is not reachable.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkStateNotReachable ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     for ( State current : this.getState () )
     {
       if ( ( current.getTransitionEnd ().size () == 0 )
           && ( !current.isStartState () ) )
       {
         machineExceptionList.add ( new MachineStateNotReachableException (
             current ) );
       }
     }
     return machineExceptionList;
   }
 
 
   /**
    * Checks if there is a {@link State} with {@link Transition}s with the same
    * {@link Symbol}.
    * 
    * @return The list of {@link MachineException}.
    */
   private final ArrayList < MachineException > checkSymbolOnlyOneTime ()
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
     for ( State currentState : this.getState () )
     {
       for ( Symbol currentSymbol : this.getAlphabet () )
       {
         ArrayList < Transition > transitions = new ArrayList < Transition > ();
         for ( Transition currentTransition : currentState.getTransitionBegin () )
         {
           if ( currentTransition.contains ( currentSymbol ) )
           {
             transitions.add ( currentTransition );
           }
         }
         if ( transitions.size () > 1 )
         {
           machineExceptionList.add ( new MachineSymbolOnlyOneTimeException (
               currentState, currentSymbol, transitions ) );
         }
       }
     }
     return machineExceptionList;
   }
 
 
   /**
    * Clears the history of this <code>AbstractMachine</code>.
    */
   private final void clearHistory ()
   {
     this.history.clear ();
   }
 
 
   /**
    * Returns the active {@link State}s.
    * 
    * @return The active {@link State}s.
    */
   public final TreeSet < State > getActiveState ()
   {
     return this.activeStateSet;
   }
 
 
   /**
    * Returns the active {@link State} with the given index.
    * 
    * @param pIndex The index.
    * @return The active {@link State} with the given index.
    */
   public final State getActiveState ( int pIndex )
   {
     Iterator < State > iterator = this.activeStateSet.iterator ();
     for ( int i = 0 ; i < pIndex ; i++ )
     {
       iterator.next ();
     }
     return iterator.next ();
   }
 
 
   /**
    * Returns the {@link Alphabet}.
    * 
    * @return The {@link Alphabet}.
    * @see #alphabet
    */
   public final Alphabet getAlphabet ()
   {
     return this.alphabet;
   }
 
 
   /**
    * Returns the current {@link Symbol}.
    * 
    * @return The current {@link Symbol}.
    * @throws WordException If something with the <code>Word</code> is not
    *           correct.
    */
   public final Symbol getCurrentSymbol () throws WordException
   {
     return this.word.getCurrentSymbol ();
   }
 
 
   /**
    * Returns the {@link State} list.
    * 
    * @return The {@link State} list.
    * @see #stateList
    */
   public final ArrayList < State > getState ()
   {
     return this.stateList;
   }
 
 
   /**
    * Returns the {@link State} with the given index.
    * 
    * @param pIndex The index to return.
    * @return The {@link State} list.
    * @see #stateList
    */
   public final State getState ( int pIndex )
   {
     return this.stateList.get ( pIndex );
   }
 
 
   /**
    * Returns the {@link Transition} list.
    * 
    * @return The {@link Transition} list.
    * @see #transitionList
    */
   public final ArrayList < Transition > getTransition ()
   {
     return this.transitionList;
   }
 
 
   /**
    * Returns the {@link Transition} with the given index.
    * 
    * @param pIndex pIndex The index to return.
    * @return The {@link Transition} list.
    * @see #transitionList
    */
   public final Transition getTransition ( int pIndex )
   {
     return this.transitionList.get ( pIndex );
   }
 
 
   /**
    * Returns true if the {@link Word} is finished, otherwise false.
    * 
    * @return True if this {@link Word} is finished, otherwise false.
    */
   public final boolean isFinished ()
   {
     return this.word.isFinished ()
         || ( ( this.activeStateSet.size () == 0 ) && ( !this.word.isReseted () ) );
   }
 
 
   /**
    * Returns true if this {@link Word} is reseted, otherwise false.
    * 
    * @return True if this {@link Word} is reseted, otherwise false.
    */
   public final boolean isReseted ()
   {
     return this.word.isReseted () && this.history.isEmpty ();
   }
 
 
   /**
    * Returns true if the given {@link Symbol} can be removed from the
    * {@link Alphabet} of this <code>AbstractMachine</code>, otherwise false.
    * 
    * @param pSymbol The {@link Symbol} which should be checked.
    * @return True if the given {@link Symbol} can be removed from the
    *         {@link Alphabet} of this <code>AbstractMachine</code>, otherwise
    *         false.
    */
   public boolean isSymbolRemoveable ( Symbol pSymbol )
   {
     if ( !this.alphabet.contains ( pSymbol ) )
     {
       throw new IllegalArgumentException ( "symbol is not in the alphabet" ); //$NON-NLS-1$
     }
     for ( Transition current : this.transitionList )
     {
       if ( current.contains ( pSymbol ) )
       {
         return false;
       }
     }
     return true;
   }
 
 
   /**
    * Returns true if one of the active {@link State}s is a final {@link State},
    * otherwise false.
    * 
    * @return True if one of the active {@link State}s is a final {@link State},
    *         otherwise false.
    */
   public final boolean isWordAccepted ()
   {
     for ( State current : this.activeStateSet )
     {
       if ( current.isFinalState () )
       {
         return true;
       }
     }
     return false;
   }
 
 
   /**
    * Links the {@link Transition}s with the given {@link State}.
    * 
    * @param pState The {@link State} to which the {@link Transition}s should be
    *          linked.
    */
   private final void link ( State pState )
   {
     for ( Transition current : this.transitionList )
     {
       // State begin
       if ( current.getStateBegin () == null )
       {
         if ( current.getStateBeginId () == pState.getId () )
         {
           current.setStateBegin ( pState );
           pState.addTransitionBegin ( current );
         }
       }
       else if ( current.getStateBegin ().equals ( pState ) )
       {
         pState.addTransitionBegin ( current );
       }
       // State end
       if ( current.getStateEnd () == null )
       {
         if ( current.getStateEndId () == pState.getId () )
         {
           current.setStateEnd ( pState );
           pState.addTransitionEnd ( current );
         }
       }
       else if ( current.getStateEnd ().equals ( pState ) )
       {
         pState.addTransitionEnd ( current );
       }
     }
   }
 
 
   /**
    * Links the given {@link Transition} with the {@link State}s.
    * 
    * @param pTransition The {@link Transition} to link with the {@link State}s.
    */
   private final void link ( Transition pTransition )
   {
     for ( State currentState : this.stateList )
     {
       // State begin
       if ( pTransition.getStateBegin () == null )
       {
         if ( pTransition.getStateBeginId () == currentState.getId () )
         {
           pTransition.setStateBegin ( currentState );
           currentState.addTransitionBegin ( pTransition );
         }
       }
       else if ( pTransition.getStateBegin ().equals ( currentState ) )
       {
         currentState.addTransitionBegin ( pTransition );
       }
       // State end
       if ( pTransition.getStateEnd () == null )
       {
         if ( pTransition.getStateEndId () == currentState.getId () )
         {
           pTransition.setStateEnd ( currentState );
           currentState.addTransitionEnd ( pTransition );
         }
       }
       else if ( pTransition.getStateEnd ().equals ( currentState ) )
       {
         currentState.addTransitionEnd ( pTransition );
       }
     }
   }
 
 
   /**
    * Performs the next step and returns the list of {@link Transition}s, which
    * contains the {@link Symbol}.
    * 
    * @return The list of {@link Transition}s, which contains the {@link Symbol}.
    * @throws WordFinishedException If something with the {@link Word} is not
    *           correct.
    * @throws WordResetedException If something with the {@link Word} is not
    *           correct.
    * @throws WordNotAcceptedException If something with the {@link Word} is not
    *           correct.
    */
   public final ArrayList < Transition > nextSymbol ()
       throws WordFinishedException, WordResetedException,
       WordNotAcceptedException
   {
     if ( getActiveState ().size () == 0 )
     {
       throw new IllegalArgumentException (
           "no active state: machine must be started first" ); //$NON-NLS-1$
     }
     // Check for epsilon transitions
     boolean epsilonTransitionFound = false;
     stateLoop : for ( State activeState : getActiveState () )
     {
       for ( Transition current : activeState.getTransitionBegin () )
       {
         if ( current.isEpsilonTransition () )
         {
           epsilonTransitionFound = true;
           break stateLoop;
         }
       }
     }
     ArrayList < Transition > transitions = new ArrayList < Transition > ();
     TreeSet < State > newActiveStateSet = new TreeSet < State > ();
     // Epsilon transition found
     if ( epsilonTransitionFound )
     {
       for ( State activeState : getActiveState () )
       {
         for ( Transition current : activeState.getTransitionBegin () )
         {
           if ( current.isEpsilonTransition () )
           {
             newActiveStateSet.add ( current.getStateEnd () );
             transitions.add ( current );
           }
         }
       }
     }
     // No epsilon transition found
     else
     {
       Symbol symbol = this.word.nextSymbol ();
       for ( State activeState : getActiveState () )
       {
         for ( Transition current : activeState.getTransitionBegin () )
         {
           if ( current.contains ( symbol ) )
           {
             newActiveStateSet.add ( current.getStateEnd () );
             transitions.add ( current );
           }
         }
       }
     }
     // Set sctive state set
     this.activeStateSet.clear ();
     this.activeStateSet.addAll ( newActiveStateSet );
     // No transition is found
     if ( this.activeStateSet.size () == 0 )
     {
       throw new WordNotAcceptedException ( this.word );
     }
     addHistory ( transitions );
     return transitions;
   }
 
 
   /**
    * Removes the last step and returns the list of {@link Transition}s, which
    * contains the {@link Symbol}.
    * 
    * @return The list of {@link Transition}s, which contains the {@link Symbol}.
    * @throws WordFinishedException If something with the {@link Word} is not
    *           correct.
    * @throws WordResetedException If something with the {@link Word} is not
    *           correct.
    */
   public final ArrayList < Transition > previousSymbol ()
       throws WordFinishedException, WordResetedException
   {
     if ( getActiveState ().size () == 0 )
     {
       throw new IllegalArgumentException (
           "no active state: machine must be started first" ); //$NON-NLS-1$
     }
     ArrayList < Transition > transitions = removeHistory ();
     // Check for epsilon transitions
     boolean epsilonTransitionFound = false;
     transitionLoop : for ( Transition current : transitions )
     {
       if ( current.isEpsilonTransition () )
       {
         epsilonTransitionFound = true;
         break transitionLoop;
       }
     }
     // No epsilon transition found
     if ( !epsilonTransitionFound )
     {
       this.word.previousSymbol ();
     }
     // Set sctive state set
     this.activeStateSet.clear ();
     for ( Transition current : transitions )
     {
       this.activeStateSet.add ( current.getStateBegin () );
     }
     return transitions;
   }
 
 
   /**
    * Removes and returns the last history element.
    * 
    * @return The last history element.
    */
   private final ArrayList < Transition > removeHistory ()
   {
     return this.history.remove ( this.history.size () - 1 );
   }
 
 
   /**
    * Removes the given {@link State}s from this <code>AbstractMachine</code>.
    * 
    * @param pStates The {@link State}s to remove.
    */
   public final void removeState ( Iterable < State > pStates )
   {
     if ( pStates == null )
     {
       throw new NullPointerException ( "states is null" ); //$NON-NLS-1$
     }
     if ( !pStates.iterator ().hasNext () )
     {
       throw new IllegalArgumentException ( "states is empty" ); //$NON-NLS-1$
     }
     for ( State current : pStates )
     {
       removeState ( current );
     }
   }
 
 
   /**
    * Removes the given {@link State} from this <code>AbstractMachine</code>.
    * 
    * @param pState The {@link State} to remove.
    */
   public final void removeState ( State pState )
   {
     this.stateList.remove ( pState );
     for ( Transition current : pState.getTransitionBegin () )
     {
       removeTransition ( current );
     }
     for ( Transition current : pState.getTransitionEnd () )
     {
       removeTransition ( current );
     }
   }
 
 
   /**
    * Removes the given {@link State}s from this <code>AbstractMachine</code>.
    * 
    * @param pStates The {@link State}s to remove.
    */
   public final void removeState ( State ... pStates )
   {
     if ( pStates == null )
     {
       throw new NullPointerException ( "states is null" ); //$NON-NLS-1$
     }
     if ( pStates.length == 0 )
     {
       throw new IllegalArgumentException ( "states is empty" ); //$NON-NLS-1$
     }
     for ( State current : pStates )
     {
       removeState ( current );
     }
   }
 
 
   /**
    * Removes the given {@link Symbol} from this <code>AbstractMachine</code>.
    * 
    * @param pSymbol The {@link Symbol} to remove.
    */
   public final void removeSymbol ( Symbol pSymbol )
   {
     if ( !isSymbolRemoveable ( pSymbol ) )
     {
       throw new IllegalArgumentException ( "symbol is not removeable" ); //$NON-NLS-1$
     }
     this.alphabet.remove ( pSymbol );
   }
 
 
   /**
    * Removes the given {@link Transition}s from this
    * <code>AbstractMachine</code>.
    * 
    * @param pTransitions The {@link Transition}s to remove.
    */
   public final void removeTransition ( Iterable < Transition > pTransitions )
   {
     if ( pTransitions == null )
     {
       throw new NullPointerException ( "transitions is null" ); //$NON-NLS-1$
     }
     if ( !pTransitions.iterator ().hasNext () )
     {
       throw new IllegalArgumentException ( "transitions is empty" ); //$NON-NLS-1$
     }
     for ( Transition current : pTransitions )
     {
       removeTransition ( current );
     }
   }
 
 
   /**
    * Removes the given {@link Transition} from this <code>AbstractMachine</code>.
    * 
    * @param pTransition The {@link Transition} to remove.
    */
   public final void removeTransition ( Transition pTransition )
   {
     this.transitionList.remove ( pTransition );
     for ( State current : this.stateList )
     {
       current.getTransitionBegin ().remove ( pTransition );
       current.getTransitionEnd ().remove ( pTransition );
     }
   }
 
 
   /**
    * Removes the given {@link Transition}s from this
    * <code>AbstractMachine</code>.
    * 
    * @param pTransitions The {@link Transition}s to remove.
    */
   public final void removeTransition ( Transition ... pTransitions )
   {
     if ( pTransitions == null )
     {
       throw new NullPointerException ( "transitions is null" ); //$NON-NLS-1$
     }
     if ( pTransitions.length == 0 )
     {
       throw new IllegalArgumentException ( "transitions is empty" ); //$NON-NLS-1$
     }
     for ( Transition current : pTransitions )
     {
       removeTransition ( current );
     }
   }
 
 
   /**
    * Starts the <code>AbstractMachine</code> after a validation with the given
    * {@link Word}.
    * 
    * @param pWord The {@link Word} to start with.
    * @throws MachineValidationException If the validation fails.
    */
   public final void start ( Word pWord ) throws MachineValidationException
   {
     // Word
     if ( pWord == null )
     {
       throw new NullPointerException ( "word is null" ); //$NON-NLS-1$
     }
     validate ();
     this.word = pWord;
     this.word.start ();
     clearHistory ();
     // Set active states
     this.activeStateSet.clear ();
     for ( State current : this.stateList )
     {
       if ( current.isStartState () )
       {
         this.activeStateSet.add ( current );
       }
     }
   }
 
 
   /**
    * Validates that everything in the <code>AbstractMachine</code> is correct.
    * 
    * @throws MachineValidationException If the validation fails.
    */
   public final void validate () throws MachineValidationException
   {
     ArrayList < MachineException > machineExceptionList = new ArrayList < MachineException > ();
 
     if ( this.validationElementList.contains ( ValidationElement.ALL_SYMBOLS ) )
     {
       machineExceptionList.addAll ( checkAllSymbols () );
     }
 
     if ( this.validationElementList
         .contains ( ValidationElement.EPSILON_TRANSITION ) )
     {
       machineExceptionList.addAll ( checkEpsilonTransition () );
     }
 
     if ( this.validationElementList.contains ( ValidationElement.FINAL_STATE ) )
     {
       machineExceptionList.addAll ( checkFinalState () );
     }
 
     if ( this.validationElementList
         .contains ( ValidationElement.MORE_THAN_ONE_START_STATE ) )
     {
       machineExceptionList.addAll ( checkMoreThanOneStartState () );
     }
 
     if ( this.validationElementList
         .contains ( ValidationElement.NO_START_STATE ) )
     {
       machineExceptionList.addAll ( checkNoStartState () );
     }
 
     if ( this.validationElementList.contains ( ValidationElement.STATE_NAME ) )
     {
       machineExceptionList.addAll ( checkStateName () );
     }
 
     if ( this.validationElementList
         .contains ( ValidationElement.STATE_NOT_REACHABLE ) )
     {
       machineExceptionList.addAll ( checkStateNotReachable () );
     }
 
     if ( this.validationElementList
         .contains ( ValidationElement.SYMBOL_ONLY_ONE_TIME ) )
     {
       machineExceptionList.addAll ( checkSymbolOnlyOneTime () );
     }
 
     // Throw the exception if a warning or an error has occurred.
     if ( machineExceptionList.size () > 0 )
     {
       throw new MachineValidationException ( machineExceptionList );
     }
   }
 }
