 package de.unisiegen.gtitool.ui.model;
 
 
 import java.util.ArrayList;
 
 import javax.swing.event.EventListenerList;
 
 import de.unisiegen.gtitool.core.Messages;
 import de.unisiegen.gtitool.core.entities.DefaultNonterminalSymbol;
 import de.unisiegen.gtitool.core.entities.DefaultNonterminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.DefaultProduction;
 import de.unisiegen.gtitool.core.entities.DefaultTerminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.NonterminalSymbol;
 import de.unisiegen.gtitool.core.entities.NonterminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.Production;
 import de.unisiegen.gtitool.core.entities.TerminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.exceptions.nonterminalsymbol.NonterminalSymbolException;
 import de.unisiegen.gtitool.core.exceptions.nonterminalsymbolset.NonterminalSymbolSetException;
 import de.unisiegen.gtitool.core.exceptions.terminalsymbol.TerminalSymbolException;
 import de.unisiegen.gtitool.core.exceptions.terminalsymbolset.TerminalSymbolSetException;
 import de.unisiegen.gtitool.core.grammars.Grammar;
 import de.unisiegen.gtitool.core.grammars.cfg.DefaultCFG;
 import de.unisiegen.gtitool.core.grammars.rg.DefaultRG;
 import de.unisiegen.gtitool.core.machines.Machine;
 import de.unisiegen.gtitool.core.storage.Attribute;
 import de.unisiegen.gtitool.core.storage.Element;
 import de.unisiegen.gtitool.core.storage.Modifyable;
 import de.unisiegen.gtitool.core.storage.Storable;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 import de.unisiegen.gtitool.ui.redoundo.ProductionsListChangedItem;
 import de.unisiegen.gtitool.ui.redoundo.RedoUndoHandler;
 import de.unisiegen.gtitool.ui.redoundo.RedoUndoItem;
 
 
 /**
  * The Model for the {@link Grammar}s
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public class DefaultGrammarModel implements DefaultModel, Storable, Modifyable
 
 {
 
   /**
    * Signals the grammar type.
    */
   public enum GrammarType
   {
     /**
      * The grammar type is rg
      */
     RG,
 
     /**
      * The grammar type is cfg
      */
     CFG;
   }
 
 
   /**
    * The {@link RedoUndoHandler}
    */
   private RedoUndoHandler redoUndoHandler;
 
 
   /**
    * The {@link Machine} version.
    */
   private static final int GRAMMAR_VERSION = 734;
 
 
   /**
    * The {@link Grammar}.
    */
   private Grammar grammar;
 
 
   /**
    * The {@link ModifyStatusChangedListener}.
    */
   private ModifyStatusChangedListener modifyStatusChangedListener;
 
 
   /**
    * List of listeners
    */
   private EventListenerList listenerList = new EventListenerList ();
 
 
   /**
    * Allocate a new {@link DefaultGrammarModel}.
    * 
    * @param grammar The {@link Grammar}
    */
   public DefaultGrammarModel ( Grammar grammar )
   {
     this.grammar = grammar;
     initializeModifyStatusChangedListener ();
   }
 
 
   /**
    * Allocate a new {@link DefaultGrammarModel}.
    * 
    * @param element The {@link Element}.
    * @param overwrittenMachineType The overwritten machine type which is used
    *          instead of the loaded machine type if it is not null.
    * @throws NonterminalSymbolSetException If something with the
    *           {@link NonterminalSymbolSet} is not correct.
    * @throws NonterminalSymbolException If something with the
    *           {@link NonterminalSymbolSet} is not correct.
    * @throws StoreException If the {@link Element} can not be parsed.
    * @throws TerminalSymbolSetException If something with the
    *           {@link TerminalSymbolSet} is not correct.
    * @throws TerminalSymbolException If something with the
    *           {@link TerminalSymbolSet} is not correct.
    */
   public DefaultGrammarModel ( Element element, String overwrittenMachineType )
       throws NonterminalSymbolSetException, NonterminalSymbolException,
       StoreException, TerminalSymbolSetException, TerminalSymbolException
   {
 
     // Attribute
     boolean foundGrammarVersion = false;
     String grammarType = null;
     for ( Attribute attribute : element.getAttribute () )
     {
       if ( attribute.getName ().equals ( "grammarType" ) ) //$NON-NLS-1$
       {
         if ( overwrittenMachineType == null )
         {
           grammarType = attribute.getValue ();
         }
         else
         {
           grammarType = overwrittenMachineType;
         }
       }
       else if ( attribute.getName ().equals ( "grammarVersion" ) ) //$NON-NLS-1$
       {
         foundGrammarVersion = true;
         if ( GRAMMAR_VERSION != attribute.getValueInt () )
         {
           throw new StoreException ( Messages
               .getString ( "StoreException.IncompatibleVersion" ) ); //$NON-NLS-1$
         }
       }
       else
       {
         throw new StoreException ( Messages
             .getString ( "StoreException.AdditionalAttribute" ) ); //$NON-NLS-1$
       }
     }
 
     NonterminalSymbolSet nonterminalSymbolSet = null;
     TerminalSymbolSet terminalSymbolSet = null;
     NonterminalSymbol startSymbol = null;
 
     for ( Element current : element.getElement () )
     {
       if ( current.getName ().equals ( "NonterminalSymbolSet" ) ) //$NON-NLS-1$
       {
         nonterminalSymbolSet = new DefaultNonterminalSymbolSet ( current );
       }
 
       else if ( current.getName ().equals ( "TerminalSymbolSet" ) ) //$NON-NLS-1$
       {
         terminalSymbolSet = new DefaultTerminalSymbolSet ( current );
       }
 
       else if ( current.getName ().equals ( "NonterminalSymbol" ) ) //$NON-NLS-1$
       {
         startSymbol = new DefaultNonterminalSymbol ( current );
       }
 
     }
 
     if ( nonterminalSymbolSet == null || terminalSymbolSet == null
         || grammarType == null || startSymbol == null || !foundGrammarVersion )
     {
       throw new StoreException ( Messages
           .getString ( "StoreException.MissingAttribute" ) ); //$NON-NLS-1$
     }
 
     ArrayList < Production > productions = new ArrayList < Production > ();
 
     for ( Element current : element.getElement () )
     {
       if ( current.getName ().equals ( "Production" ) ) //$NON-NLS-1$
       {
         productions.add ( new DefaultProduction ( current ) );
       }
 
       else if ( ( !current.getName ().equals ( "NonterminalSymbolSet" ) ) //$NON-NLS-1$
           && ( !current.getName ().equals ( "TerminalSymbolSet" ) ) //$NON-NLS-1$
           && ( !current.getName ().equals ( "NonterminalSymbol" ) ) ) //$NON-NLS-1$
       {
         throw new StoreException ( Messages
             .getString ( "StoreException.AdditionalElement" ) ); //$NON-NLS-1$
       }
     }
     if ( grammarType.equalsIgnoreCase ( "CFG" ) ) //$NON-NLS-1$
     {
       this.grammar = new DefaultCFG ( nonterminalSymbolSet, terminalSymbolSet,
           startSymbol );
     }
     if ( grammarType.equalsIgnoreCase ( "RG" ) ) //$NON-NLS-1$
     {
       this.grammar = new DefaultRG ( nonterminalSymbolSet, terminalSymbolSet,
           startSymbol );
     }
     for ( Production current : productions )
     {
       this.grammar.addProduction ( current );
     }
     this.grammar.resetModify ();
     initializeModifyStatusChangedListener ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Machine#addModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final void addModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.add ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * Add a new production to list.
    * 
    * @param production The production to add.
    * @param createUndoStep Flag signals if an undo step should be created.
    */
   public void addProduction ( Production production, boolean createUndoStep )
   {
     ArrayList < Production > productions = new ArrayList < Production > ();
     productions.addAll ( this.grammar.getProduction () );
     this.grammar.addProduction ( production );
 
     if ( createUndoStep )
     {
       RedoUndoItem item = new ProductionsListChangedItem ( this.grammar,
           productions );
       this.redoUndoHandler.addItem ( item );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Storable#getElement()
    */
   public final Element getElement ()
   {
     Element newElement = new Element ( "GrammarModel" ); //$NON-NLS-1$
     newElement.addAttribute ( new Attribute ( "grammarType", this.grammar //$NON-NLS-1$
         .getGrammarType () ) );
     newElement.addAttribute ( new Attribute ( "grammarVersion", //$NON-NLS-1$
         GRAMMAR_VERSION ) );
     newElement.addElement ( this.grammar.getStartSymbol ().getElement () );
     newElement.addElement ( this.grammar.getNonterminalSymbolSet ()
         .getElement () );
     newElement.addElement ( this.grammar.getTerminalSymbolSet ().getElement () );
 
     for ( Production current : this.grammar.getProduction () )
     {
       newElement.addElement ( current.getElement () );
     }
     return newElement;
   }
 
 
   /**
    * Returns the {@link Grammar}.
    * 
    * @return the {@link Grammar}.
    */
   public Grammar getGrammar ()
   {
     return this.grammar;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#isModified()
    */
   public boolean isModified ()
   {
    if ( this.grammar.isModified () )
    {
      return true;
    }
    return false;
   }
 
 
   /**
    * Remove a {@link Production} from list.
    * 
    * @param index The index of the {@link Production}.
    */
   public void removeProduction ( int index )
   {
     this.grammar.removeProduction ( index );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#resetModify()
    */
   public void resetModify ()
   {
     this.grammar.resetModify ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#removeModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final void removeModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.remove ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * Initialize the {@link ModifyStatusChangedListener}.
    */
   private final void initializeModifyStatusChangedListener ()
   {
     this.modifyStatusChangedListener = new ModifyStatusChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void modifyStatusChanged ( boolean modified )
       {
         fireModifyStatusChanged ( modified );
       }
     };
     this.grammar
         .addModifyStatusChangedListener ( this.modifyStatusChangedListener );
   }
 
 
   /**
    * Let the listeners know that the modify status has changed.
    * 
    * @param forceModify True if the modify is forced, otherwise false.
    */
   private final void fireModifyStatusChanged ( boolean forceModify )
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
    * Set a new {@link RedoUndoHandler}
    * 
    * @param redoUndoHandler the new {@link RedoUndoHandler}
    */
   public final void setRedoUndoHandler ( RedoUndoHandler redoUndoHandler )
   {
     this.redoUndoHandler = redoUndoHandler;
   }
 
 }
