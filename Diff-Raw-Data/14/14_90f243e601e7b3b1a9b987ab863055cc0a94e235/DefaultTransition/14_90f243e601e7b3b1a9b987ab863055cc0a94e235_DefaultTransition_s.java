 package de.unisiegen.gtitool.core.entities;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import javax.swing.event.EventListenerList;
 
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.entities.listener.PrettyStringChangedListener;
 import de.unisiegen.gtitool.core.entities.listener.TransitionChangedListener;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolNotInAlphabetException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.i18n.Messages;
 import de.unisiegen.gtitool.core.parser.ParserOffset;
 import de.unisiegen.gtitool.core.parser.style.PrettyPrintable;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 import de.unisiegen.gtitool.core.parser.style.PrettyToken;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.core.parser.style.PrettyString.PrettyStringMode;
 import de.unisiegen.gtitool.core.storage.Attribute;
 import de.unisiegen.gtitool.core.storage.Element;
 import de.unisiegen.gtitool.core.storage.Modifyable;
 import de.unisiegen.gtitool.core.storage.Storable;
 import de.unisiegen.gtitool.core.storage.exceptions.StoreException;
 
 
 /**
  * The {@link DefaultTransition} entity.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class DefaultTransition implements Transition
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = 7649068993385065572L;
 
 
   /**
    * This {@link DefaultTransition} is a selected {@link DefaultTransition}.
    */
   private boolean selected = false;
 
 
   /**
    * This {@link Transition} is a active {@link Transition}.
    */
   private boolean active = false;
 
 
   /**
    * The {@link Alphabet} of this {@link DefaultTransition}.
    */
   private Alphabet alphabet = null;
 
 
   /**
    * This {@link Transition} is a error {@link Transition}.
    */
   private boolean error = false;
 
 
   /**
    * The id of this {@link DefaultTransition}.
    */
   private int id = ID_NOT_DEFINED;
 
 
   /**
    * The initial {@link Word} which is read from the {@link Stack}.
    */
   private Word initialPushDownWordRead = new DefaultWord ();
 
 
   /**
    * The {@link Word} which should be written on the {@link Stack}.
    */
   private Word initialPushDownWordWrite = new DefaultWord ();
 
 
   /**
    * The initial set of {@link Symbol}s.
    */
   private TreeSet < Symbol > initialSymbolSet;
 
 
   /**
    * The {@link EventListenerList}.
    */
   private EventListenerList listenerList = new EventListenerList ();
 
 
   /**
    * The offset of this {@link DefaultTransition} in the source code.
    * 
    * @see #getParserOffset()
    * @see #setParserOffset(ParserOffset)
    */
   private ParserOffset parserOffset = NO_PARSER_OFFSET;
 
 
   /**
    * The push down {@link Alphabet} of this {@link DefaultTransition}.
    */
   private Alphabet pushDownAlphabet;
 
 
   /**
    * The {@link Word} which is read from the {@link Stack}.
    */
   private Word pushDownWordRead = new DefaultWord ();
 
 
   /**
    * The {@link Word} which should be written on the {@link Stack}.
    */
   private Word pushDownWordWrite = new DefaultWord ();
 
 
   /**
    * The {@link State} where the {@link DefaultTransition} begins.
    */
   private State stateBegin;
 
 
   /**
    * The {@link State} id where the {@link DefaultTransition} begins.
    */
   private int stateBeginId;
 
 
   /**
    * The {@link State} where the {@link DefaultTransition} ends.
    */
   private State stateEnd;
 
 
   /**
    * The {@link State} id where the {@link DefaultTransition} ends.
    */
   private int stateEndId;
 
 
   /**
    * The set of {@link Symbol}s.
    */
   private TreeSet < Symbol > symbolSet;
 
 
   /**
    * The cached stack {@link PrettyString}.
    */
   private PrettyString cachedStackPrettyString = null;
 
 
   /**
    * The cached {@link PrettyString}.
    */
   private PrettyString cachedPrettyString = null;
 
 
   /**
    * The {@link PrettyStringChangedListener}.
    */
   private PrettyStringChangedListener prettyStringChangedListener;
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    */
   public DefaultTransition ()
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
    // SymbolSet
     this.symbolSet = new TreeSet < Symbol > ();
 
     try
     {
       add ( new DefaultSymbol () );
     }
     catch ( TransitionException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return;
     }
 
     this.initialSymbolSet = new TreeSet < Symbol > ();

    try
    {
      add ( new DefaultSymbol () );
    }
    catch ( TransitionException exc )
    {
      exc.printStackTrace ();
      System.exit ( 1 );
      return;
    }
 
     resetModify ();
   }
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    * 
    * @param alphabet The {@link Alphabet} of this {@link DefaultTransition}.
    * @param pushDownAlphabet The push down {@link Alphabet} of this
    *          {@link DefaultTransition}.
    * @param pushDownWordRead The {@link Word} which is read from the
    *          {@link Stack}.
    * @param pushDownWordWrite The {@link Word} which should be written on the
    *          {@link Stack}.
    * @param stateBegin The {@link State} where the {@link DefaultTransition}
    *          begins.
    * @param stateEnd The {@link State} where the {@link DefaultTransition} ends.
    * @param symbols The array of {@link Symbol}s.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    */
   public DefaultTransition ( Alphabet alphabet, Alphabet pushDownAlphabet,
       Word pushDownWordRead, Word pushDownWordWrite, State stateBegin,
       State stateEnd, Iterable < Symbol > symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     // Alphabet
     setAlphabet ( alphabet );
     // PushDownAlphabet
     setPushDownAlphabet ( pushDownAlphabet );
     // PushDownWordRead
     setPushDownWordRead ( pushDownWordRead );
     // PushDownWordWrite
     setPushDownWordWrite ( pushDownWordWrite );
     // StateBegin
     setStateBegin ( stateBegin );
     // StateEnd
     setStateEnd ( stateEnd );
     // Symbols
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     resetModify ();
   }
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    * 
    * @param alphabet The {@link Alphabet} of this {@link DefaultTransition}.
    * @param pushDownAlphabet The push down {@link Alphabet} of this
    *          {@link DefaultTransition}. *
    * @param pushDownWordRead The {@link Word} which is read from the
    *          {@link Stack}.
    * @param pushDownWordWrite The {@link Word} which should be written on the
    *          {@link Stack}.
    * @param stateBegin The {@link State} where the {@link DefaultTransition}
    *          begins.
    * @param stateEnd The {@link State} where the {@link DefaultTransition} ends.
    * @param symbols The array of {@link Symbol}s.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    */
   public DefaultTransition ( Alphabet alphabet, Alphabet pushDownAlphabet,
       Word pushDownWordRead, Word pushDownWordWrite, State stateBegin,
       State stateEnd, Symbol ... symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     setAlphabet ( alphabet );
     setPushDownAlphabet ( pushDownAlphabet );
     setPushDownWordRead ( pushDownWordRead );
     setPushDownWordWrite ( pushDownWordWrite );
     setStateBegin ( stateBegin );
     setStateEnd ( stateEnd );
 
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     resetModify ();
   }
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    * 
    * @param element The {@link Element}. *
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws StoreException If the {@link Element} can not be parsed.
    */
   public DefaultTransition ( Element element )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException, StoreException
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
     // Check if the element is correct
     if ( !element.getName ().equals ( "Transition" ) ) //$NON-NLS-1$
     {
       throw new IllegalArgumentException (
           "element " + Messages.QUOTE + element.getName () //$NON-NLS-1$
               + Messages.QUOTE + " is not a transition" ); //$NON-NLS-1$
     }
 
     // Symbols
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     // Attribute
     boolean foundId = false;
     boolean foundStateBeginId = false;
     boolean foundStateEndId = false;
     for ( Attribute current : element.getAttribute () )
     {
       if ( current.getName ().equals ( "id" ) ) //$NON-NLS-1$
       {
         setId ( current.getValueInt () );
         foundId = true;
       }
       else if ( current.getName ().equals ( "stateBeginId" ) ) //$NON-NLS-1$
       {
         setStateBeginId ( current.getValueInt () );
         foundStateBeginId = true;
       }
       else if ( current.getName ().equals ( "stateEndId" ) ) //$NON-NLS-1$
       {
         setStateEndId ( current.getValueInt () );
         foundStateEndId = true;
       }
       else
       {
         throw new StoreException ( Messages
             .getString ( "StoreException.AdditionalAttribute" ) ); //$NON-NLS-1$
       }
     }
 
     // Not all attribute values found
     if ( ( !foundId ) || ( !foundStateBeginId ) || ( !foundStateEndId ) )
     {
       throw new StoreException ( Messages
           .getString ( "StoreException.MissingAttribute" ) ); //$NON-NLS-1$
     }
 
     // Element
     boolean foundPushDownWordRead = false;
     boolean foundPushDownWordWrite = false;
     for ( Element current : element.getElement () )
     {
       if ( current.getName ().equals ( "Symbol" ) ) //$NON-NLS-1$
       {
         add ( new DefaultSymbol ( current ) );
       }
       else if ( current.getName ().equals ( "PushDownWordRead" ) ) //$NON-NLS-1$
       {
         current.setName ( "Word" ); //$NON-NLS-1$
         setPushDownWordRead ( new DefaultWord ( current ) );
         foundPushDownWordRead = true;
       }
       else if ( current.getName ().equals ( "PushDownWordWrite" ) ) //$NON-NLS-1$
       {
         current.setName ( "Word" ); //$NON-NLS-1$
         setPushDownWordWrite ( new DefaultWord ( current ) );
         foundPushDownWordWrite = true;
       }
       else
       {
         throw new StoreException ( Messages
             .getString ( "StoreException.AdditionalElement" ) ); //$NON-NLS-1$
       }
     }
 
     // Not all element values found
     if ( ( !foundPushDownWordRead ) || ( !foundPushDownWordWrite ) )
     {
       throw new StoreException ( Messages
           .getString ( "StoreException.MissingElement" ) ); //$NON-NLS-1$
     }
 
     resetModify ();
   }
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    * 
    * @param pushDownWordRead The {@link Word} which is read from the
    *          {@link Stack}.
    * @param pushDownWordWrite The {@link Word} which should be written on the
    *          {@link Stack}.
    * @param symbols The array of {@link Symbol}s.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    */
   public DefaultTransition ( Word pushDownWordRead, Word pushDownWordWrite,
       Iterable < Symbol > symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     setPushDownWordRead ( pushDownWordRead );
     setPushDownWordWrite ( pushDownWordWrite );
 
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     resetModify ();
   }
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    * 
    * @param pushDownWordRead The {@link Word} which is read from the
    *          {@link Stack}.
    * @param pushDownWordWrite The {@link Word} which should be written on the
    *          {@link Stack}.
    * @param symbols The array of {@link Symbol}s.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    */
   public DefaultTransition ( Word pushDownWordRead, Word pushDownWordWrite,
       Symbol ... symbols ) throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     this.prettyStringChangedListener = new PrettyStringChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void prettyStringChanged ()
       {
         firePrettyStringChanged ();
       }
     };
 
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     setPushDownWordRead ( pushDownWordRead );
     setPushDownWordWrite ( pushDownWordWrite );
 
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     resetModify ();
   }
 
 
   /**
    *{@inheritDoc}
    * 
    * @see Transition#add(Iterable)
    */
   public final void add ( Iterable < Symbol > symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     for ( Symbol current : symbols )
     {
       add ( current );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#add(Symbol)
    */
   public final void add ( Symbol symbol )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     if ( ( this.alphabet != null ) && ( !symbol.isEpsilon () )
         && ( !this.alphabet.contains ( symbol ) ) )
     {
       ArrayList < Symbol > tmpList = new ArrayList < Symbol > ();
       tmpList.add ( symbol );
       throw new TransitionSymbolNotInAlphabetException ( this, this.alphabet,
           tmpList );
     }
     if ( this.symbolSet.contains ( symbol ) )
     {
       ArrayList < Symbol > tmpList = new ArrayList < Symbol > ();
       for ( Symbol current : this.symbolSet )
       {
         if ( symbol.equals ( current ) )
         {
           tmpList.add ( current );
           break;
         }
       }
       tmpList.add ( symbol );
       throw new TransitionSymbolOnlyOneTimeException ( this, tmpList );
     }
 
     // the symbol must be cloned because of the different possible styles
     if ( symbol.isEpsilon () )
     {
       Symbol newSymbol = new DefaultSymbol ();
 
       newSymbol
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.symbolSet.add ( newSymbol );
     }
     else
     {
       Symbol newSymbol = new DefaultSymbol ( symbol.getName () );
 
       newSymbol
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.symbolSet.add ( newSymbol );
     }
 
     fireTransitionChanged ();
     fireModifyStatusChanged ();
   }
 
 
   /**
    *{@inheritDoc}
    * 
    * @see Transition#add(Symbol[])
    */
   public final void add ( Symbol ... symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     for ( Symbol current : symbols )
     {
       add ( current );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#addModifyStatusChangedListener(ModifyStatusChangedListener)
    */
   public final void addModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.add ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see PrettyPrintable#addPrettyStringChangedListener(PrettyStringChangedListener)
    */
   public final void addPrettyStringChangedListener (
       PrettyStringChangedListener listener )
   {
     this.listenerList.add ( PrettyStringChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#addTransitionChangedListener(TransitionChangedListener)
    */
   public final void addTransitionChangedListener (
       TransitionChangedListener listener )
   {
     this.listenerList.add ( TransitionChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Comparable#compareTo(Object)
    */
   public final int compareTo ( Transition other )
   {
     return this.id < other.getId () ? -1 : ( this.id > other.getId () ? 1 : 0 );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#contains(Symbol)
    */
   public final boolean contains ( Symbol symbol )
   {
     return this.symbolSet.contains ( symbol );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Object#equals(Object)
    */
   @Override
   public final boolean equals ( Object other )
   {
     if ( other instanceof DefaultTransition )
     {
       DefaultTransition defaultTransition = ( DefaultTransition ) other;
       if ( ( this.id == ID_NOT_DEFINED )
           || ( defaultTransition.id == ID_NOT_DEFINED ) )
       {
         throw new IllegalArgumentException ( "id is not defined" ); //$NON-NLS-1$
       }
       return this.id == defaultTransition.id;
     }
     return false;
   }
 
 
   /**
    * Let the listeners know that the modify status has changed.
    */
   private final void fireModifyStatusChanged ()
   {
     ModifyStatusChangedListener [] listeners = this.listenerList
         .getListeners ( ModifyStatusChangedListener.class );
     boolean newModifyStatus = isModified ();
     for ( ModifyStatusChangedListener current : listeners )
     {
       current.modifyStatusChanged ( newModifyStatus );
     }
   }
 
 
   /**
    * Let the listeners know that the {@link PrettyString} has changed.
    */
   private final void firePrettyStringChanged ()
   {
     this.cachedPrettyString = null;
     this.cachedStackPrettyString = null;
 
     PrettyStringChangedListener [] listeners = this.listenerList
         .getListeners ( PrettyStringChangedListener.class );
     for ( PrettyStringChangedListener current : listeners )
     {
       current.prettyStringChanged ();
     }
   }
 
 
   /**
    * Let the listeners know that the {@link Transition} has changed.
    */
   private final void fireTransitionChanged ()
   {
     TransitionChangedListener [] listeners = this.listenerList
         .getListeners ( TransitionChangedListener.class );
     for ( TransitionChangedListener current : listeners )
     {
       current.transitionChanged ( this );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getAlphabet()
    */
   public final Alphabet getAlphabet ()
   {
     return this.alphabet;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Storable#getElement()
    */
   public final Element getElement ()
   {
     Element newElement = new Element ( "Transition" ); //$NON-NLS-1$
     newElement.addAttribute ( new Attribute ( "id", this.id ) ); //$NON-NLS-1$
     newElement.addAttribute ( new Attribute ( "stateBeginId", //$NON-NLS-1$
         getStateBeginId () ) );
     newElement.addAttribute ( new Attribute ( "stateEndId", getStateEndId () ) ); //$NON-NLS-1$
     Element pushDownWordReadElement = this.pushDownWordRead.getElement ();
     pushDownWordReadElement.setName ( "PushDownWordRead" ); //$NON-NLS-1$
     newElement.addElement ( pushDownWordReadElement );
     Element pushDownWordWriteElement = this.pushDownWordWrite.getElement ();
     pushDownWordWriteElement.setName ( "PushDownWordWrite" ); //$NON-NLS-1$
     newElement.addElement ( pushDownWordWriteElement );
     for ( Symbol current : this.symbolSet )
     {
       newElement.addElement ( current );
     }
     return newElement;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getId()
    */
   public final int getId ()
   {
     return this.id;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#getParserOffset()
    */
   public final ParserOffset getParserOffset ()
   {
     return this.parserOffset;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getPushDownAlphabet()
    */
   public final Alphabet getPushDownAlphabet ()
   {
     return this.pushDownAlphabet;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getPushDownWordRead()
    */
   public final Word getPushDownWordRead ()
   {
     return this.pushDownWordRead;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getPushDownWordWrite()
    */
   public final Word getPushDownWordWrite ()
   {
     return this.pushDownWordWrite;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getStateBegin()
    */
   public final State getStateBegin ()
   {
     return this.stateBegin;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getStateBeginId()
    */
   public final int getStateBeginId ()
   {
     return this.stateBegin == null ? this.stateBeginId : this.stateBegin
         .getId ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getStateEnd()
    */
   public final State getStateEnd ()
   {
     return this.stateEnd;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getStateEndId()
    */
   public final int getStateEndId ()
   {
     return this.stateEnd == null ? this.stateEndId : this.stateEnd.getId ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getSymbol()
    */
   public final TreeSet < Symbol > getSymbol ()
   {
     return this.symbolSet;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getSymbol(int)
    */
   public final Symbol getSymbol ( int index )
   {
     Iterator < Symbol > iterator = this.symbolSet.iterator ();
     for ( int i = 0 ; i < index ; i++ )
     {
       iterator.next ();
     }
     return iterator.next ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#getTransitionType()
    */
   public final TransitionType getTransitionType ()
   {
     if ( ( this.symbolSet.size () == 1 )
         && this.symbolSet.first ().isEpsilon () )
     {
       return TransitionType.EPSILON_ONLY;
     }
     if ( this.symbolSet.size () >= 1 )
     {
       boolean epsilonFound = false;
       for ( Symbol current : this.symbolSet )
       {
         if ( current.isEpsilon () )
         {
           epsilonFound = true;
           break;
         }
       }
       if ( epsilonFound )
       {
         return TransitionType.EPSILON_SYMBOL;
       }
       return TransitionType.SYMBOL;
     }
 
     throw new IllegalArgumentException ( "unknown transition type: " //$NON-NLS-1$
         + this.symbolSet );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Object#hashCode()
    */
   @Override
   public final int hashCode ()
   {
     if ( this.id == ID_NOT_DEFINED )
     {
       throw new IllegalArgumentException ( "id is not defined" ); //$NON-NLS-1$
     }
     return this.id;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#isActive()
    */
   public final boolean isActive ()
   {
     return this.active;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#isError()
    */
   public final boolean isError ()
   {
     return this.error;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#isIdDefined()
    */
   public final boolean isIdDefined ()
   {
     return this.id != ID_NOT_DEFINED;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#isModified()
    */
   public final boolean isModified ()
   {
     return ( ( !this.symbolSet.equals ( this.initialSymbolSet ) )
         || ( !this.pushDownWordRead.equals ( this.initialPushDownWordRead ) ) || ( !this.pushDownWordWrite
         .equals ( this.initialPushDownWordWrite ) ) );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#isSelected()
    */
   public final boolean isSelected ()
   {
     return this.selected;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Iterable#iterator()
    */
   public final Iterator < Symbol > iterator ()
   {
     return this.symbolSet.iterator ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#remove(Iterable)
    */
   public final void remove ( Iterable < Symbol > symbols )
   {
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     for ( Symbol current : symbols )
     {
       remove ( current );
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#remove(Symbol)
    */
   public final void remove ( Symbol symbol )
   {
     if ( !this.symbolSet.contains ( symbol ) )
     {
       throw new IllegalArgumentException ( "symbol is not in this transition" ); //$NON-NLS-1$
     }
 
     symbol
         .removePrettyStringChangedListener ( this.prettyStringChangedListener );
 
     this.symbolSet.remove ( symbol );
 
     fireTransitionChanged ();
     fireModifyStatusChanged ();
     firePrettyStringChanged ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#remove(Symbol[])
    */
   public final void remove ( Symbol ... symbols )
   {
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     for ( Symbol current : symbols )
     {
       remove ( current );
     }
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
    * {@inheritDoc}
    * 
    * @see PrettyPrintable#removePrettyStringChangedListener(PrettyStringChangedListener)
    */
   public final void removePrettyStringChangedListener (
       PrettyStringChangedListener listener )
   {
     this.listenerList.remove ( PrettyStringChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#removeTransitionChangedListener(TransitionChangedListener)
    */
   public final void removeTransitionChangedListener (
       TransitionChangedListener listener )
   {
     this.listenerList.remove ( TransitionChangedListener.class, listener );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#replace(Iterable)
    */
   public void replace ( Iterable < Symbol > symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     for ( Symbol current : this.symbolSet )
     {
       current
           .removePrettyStringChangedListener ( this.prettyStringChangedListener );
     }
 
     this.symbolSet.clear ();
     add ( symbols );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#replace(Symbol)
    */
   public void replace ( Symbol symbol )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     for ( Symbol current : this.symbolSet )
     {
       current
           .removePrettyStringChangedListener ( this.prettyStringChangedListener );
     }
 
     this.symbolSet.clear ();
     add ( symbol );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#replace(Symbol[])
    */
   public void replace ( Symbol ... symbols )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     for ( Symbol current : this.symbolSet )
     {
       current
           .removePrettyStringChangedListener ( this.prettyStringChangedListener );
     }
 
     this.symbolSet.clear ();
     add ( symbols );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Modifyable#resetModify()
    */
   public final void resetModify ()
   {
     this.initialSymbolSet.clear ();
     this.initialSymbolSet.addAll ( this.symbolSet );
 
     this.initialPushDownWordRead = new DefaultWord ();
     this.initialPushDownWordRead.add ( this.pushDownWordRead );
 
     this.initialPushDownWordWrite = new DefaultWord ();
     this.initialPushDownWordWrite.add ( this.pushDownWordWrite );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setActive(boolean)
    */
   public final void setActive ( boolean active )
   {
     if ( this.active != active )
     {
       this.active = active;
       firePrettyStringChanged ();
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setAlphabet(Alphabet)
    */
   public final void setAlphabet ( Alphabet alphabet )
   {
     if ( alphabet == null )
     {
       throw new NullPointerException ( "alphabet is null" ); //$NON-NLS-1$
     }
     if ( this.alphabet != null )
     {
       throw new IllegalArgumentException ( "alphabet is already set" ); //$NON-NLS-1$
     }
     this.alphabet = alphabet;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setError(boolean)
    */
   public final void setError ( boolean error )
   {
     if ( this.error != error )
     {
       this.error = error;
       firePrettyStringChanged ();
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setId(int)
    */
   public final void setId ( int id )
   {
     if ( this.id != ID_NOT_DEFINED )
     {
       throw new IllegalArgumentException ( "id is already setted" ); //$NON-NLS-1$
     }
     this.id = id;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#setParserOffset(de.unisiegen.gtitool.core.parser.ParserOffset)
    */
   public final void setParserOffset ( ParserOffset parserOffset )
   {
     this.parserOffset = parserOffset;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setPushDownAlphabet(Alphabet)
    */
   public final void setPushDownAlphabet ( Alphabet pushDownAlphabet )
   {
     if ( pushDownAlphabet == null )
     {
       throw new NullPointerException ( "push down alphabet is null" ); //$NON-NLS-1$
     }
     this.pushDownAlphabet = pushDownAlphabet;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setPushDownWordRead(Word)
    */
   public final void setPushDownWordRead ( Word pushDownWordRead )
   {
     if ( pushDownWordRead == null )
     {
       throw new NullPointerException ( "push down word read is null" ); //$NON-NLS-1$
     }
 
     if ( this.pushDownWordRead == null )
     {
       pushDownWordRead
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.pushDownWordRead = pushDownWordRead;
 
       fireModifyStatusChanged ();
       firePrettyStringChanged ();
       return;
     }
 
     if ( !this.pushDownWordRead.equals ( pushDownWordRead ) )
     {
       this.pushDownWordRead
           .removePrettyStringChangedListener ( this.prettyStringChangedListener );
 
       pushDownWordRead
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.pushDownWordRead = pushDownWordRead;
 
       fireModifyStatusChanged ();
       firePrettyStringChanged ();
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setPushDownWordWrite(Word)
    */
   public final void setPushDownWordWrite ( Word pushDownWordWrite )
   {
     if ( pushDownWordWrite == null )
     {
       throw new NullPointerException ( "push down word write is null" ); //$NON-NLS-1$
     }
 
     if ( this.pushDownWordWrite == null )
     {
       pushDownWordWrite
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.pushDownWordWrite = pushDownWordWrite;
 
       fireModifyStatusChanged ();
       firePrettyStringChanged ();
       return;
     }
 
     if ( !this.pushDownWordWrite.equals ( pushDownWordWrite ) )
     {
       this.pushDownWordWrite
           .removePrettyStringChangedListener ( this.prettyStringChangedListener );
 
       pushDownWordWrite
           .addPrettyStringChangedListener ( this.prettyStringChangedListener );
 
       this.pushDownWordWrite = pushDownWordWrite;
 
       fireModifyStatusChanged ();
       firePrettyStringChanged ();
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setSelected(boolean)
    */
   public final void setSelected ( boolean selected )
   {
     if ( this.selected != selected )
     {
       this.selected = selected;
       firePrettyStringChanged ();
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setStateBegin(State)
    */
   public final void setStateBegin ( State stateBegin )
   {
     if ( stateBegin == null )
     {
       throw new NullPointerException ( "state begin is null" ); //$NON-NLS-1$
     }
     this.stateBegin = stateBegin;
   }
 
 
   /**
    * Sets the {@link State} id where the {@link DefaultTransition} begins.
    * 
    * @param stateBeginId The {@link State} id to set.
    */
   private final void setStateBeginId ( int stateBeginId )
   {
     if ( this.stateBegin != null )
     {
       throw new IllegalArgumentException (
           "can not set the id if there is a state" ); //$NON-NLS-1$
     }
     this.stateBeginId = stateBeginId;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#setStateEnd(State)
    */
   public final void setStateEnd ( State stateEnd )
   {
     if ( stateEnd == null )
     {
       throw new NullPointerException ( "state end is null" ); //$NON-NLS-1$
     }
     this.stateEnd = stateEnd;
   }
 
 
   /**
    * Sets the {@link State} id where the {@link DefaultTransition} ends.
    * 
    * @param stateEndId The {@link State} id to set.
    */
   private final void setStateEndId ( int stateEndId )
   {
     if ( this.stateEnd != null )
     {
       throw new IllegalArgumentException (
           "can not set the id if there is a state" ); //$NON-NLS-1$
     }
     this.stateEndId = stateEndId;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#size()
    */
   public final int size ()
   {
     return this.symbolSet.size ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see PrettyPrintable#toPrettyString()
    */
   public final PrettyString toPrettyString ()
   {
     if ( this.symbolSet.size () == 0 )
     {
       throw new RuntimeException ( "symbol set is empty" ); //$NON-NLS-1$
     }
 
     if ( ( this.cachedPrettyString == null )
         || PrettyString.MODE.equals ( PrettyStringMode.CACHING_OFF ) )
     {
       this.cachedPrettyString = new PrettyString ();
       this.cachedPrettyString.addPrettyToken ( new PrettyToken (
           "{", Style.NONE ) ); //$NON-NLS-1$
       boolean first = true;
       for ( Symbol current : this.symbolSet )
       {
         if ( !first )
         {
           this.cachedPrettyString.addPrettyToken ( new PrettyToken (
               ", ", Style.NONE ) ); //$NON-NLS-1$
         }
         first = false;
         this.cachedPrettyString.addPrettyPrintable ( current );
       }
       this.cachedPrettyString.addPrettyToken ( new PrettyToken (
           "}", Style.NONE ) ); //$NON-NLS-1$
 
       if ( ( this.pushDownWordRead.size () > 0 )
           || ( this.pushDownWordWrite.size () > 0 ) )
       {
         this.cachedPrettyString.addPrettyToken ( new PrettyToken (
             " ", Style.NONE ) ); //$NON-NLS-1$
         if ( this.pushDownWordRead.size () == 0 )
         {
           this.cachedPrettyString.addPrettyToken ( new PrettyToken (
               "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
         }
         else
         {
           this.cachedPrettyString.addPrettyPrintable ( this.pushDownWordRead );
         }
         this.cachedPrettyString.addPrettyToken ( new PrettyToken (
             "\u2191", Style.KEYWORD ) ); //$NON-NLS-1$
         this.cachedPrettyString.addPrettyToken ( new PrettyToken (
             " ", Style.NONE ) ); //$NON-NLS-1$
         if ( this.pushDownWordWrite.size () == 0 )
         {
           this.cachedPrettyString.addPrettyToken ( new PrettyToken (
               "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
         }
         else
         {
           this.cachedPrettyString.addPrettyPrintable ( this.pushDownWordWrite );
         }
         this.cachedPrettyString.addPrettyToken ( new PrettyToken (
             "\u2193", Style.KEYWORD ) ); //$NON-NLS-1$
       }
     }
 
     return this.cachedPrettyString;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Transition#toStackOperationPrettyString()
    */
   public final PrettyString toStackOperationPrettyString ()
   {
     if ( this.symbolSet.size () == 0 )
     {
       throw new RuntimeException ( "symbol set is empty" ); //$NON-NLS-1$
     }
 
     if ( ( this.cachedStackPrettyString == null )
         || PrettyString.MODE.equals ( PrettyStringMode.CACHING_OFF ) )
     {
       // does not use the active style of symbols
       this.cachedStackPrettyString = new PrettyString ();
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           "(", Style.NONE ) ); //$NON-NLS-1$
       this.cachedStackPrettyString.addPrettyPrintable ( this.stateBegin );
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           ", ", Style.NONE ) ); //$NON-NLS-1$
 
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           "{", Style.NONE ) ); //$NON-NLS-1$
       boolean first = true;
       for ( Symbol current : this.symbolSet )
       {
         if ( !first )
         {
           this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
               ", ", Style.NONE ) ); //$NON-NLS-1$
         }
         first = false;
         if ( current.isEpsilon () )
         {
           this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
               "\u03B5", //$NON-NLS-1$
               Style.SYMBOL ) );
         }
         else
         {
           this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
               current.getName (), Style.SYMBOL ) );
         }
       }
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           "}", Style.NONE ) ); //$NON-NLS-1$
 
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           ", ", Style.NONE ) ); //$NON-NLS-1$
       if ( getPushDownWordRead ().size () == 0 )
       {
         this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
             "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
       }
       else
       {
         this.cachedStackPrettyString
             .addPrettyPrintable ( this.pushDownWordRead );
       }
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           "), (", Style.NONE ) ); //$NON-NLS-1$
 
       this.cachedStackPrettyString.addPrettyPrintable ( this.stateEnd );
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           ", ", Style.NONE ) ); //$NON-NLS-1$
       if ( getPushDownWordWrite ().size () == 0 )
       {
         this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
             "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
       }
       else
       {
         this.cachedStackPrettyString
             .addPrettyPrintable ( this.pushDownWordWrite );
       }
       this.cachedStackPrettyString.addPrettyToken ( new PrettyToken (
           ")", Style.NONE ) ); //$NON-NLS-1$
 
       if ( this.selected )
       {
         this.cachedStackPrettyString
             .overwriteColor ( Style.TRANSITION_SELECTED );
       }
     }
 
     return this.cachedStackPrettyString;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#toString()
    */
   @Override
   public final String toString ()
   {
     if ( this.symbolSet.size () == 0 )
     {
       // used from the parser
       return "";//$NON-NLS-1$
     }
 
     StringBuilder result = new StringBuilder ();
     result.append ( "{" ); //$NON-NLS-1$
     boolean first = true;
     for ( Symbol current : this.symbolSet )
     {
       if ( !first )
       {
         result.append ( ", " ); //$NON-NLS-1$
       }
       first = false;
       result.append ( current );
     }
     result.append ( "}" ); //$NON-NLS-1$
 
     if ( ( this.pushDownWordRead.size () > 0 )
         || ( this.pushDownWordWrite.size () > 0 ) )
     {
       result.append ( " " ); //$NON-NLS-1$
       if ( this.pushDownWordRead.size () == 0 )
       {
         result.append ( "\u03B5" ); //$NON-NLS-1$
       }
       else
       {
         result.append ( this.pushDownWordRead );
       }
       result.append ( "\u2191 " ); //$NON-NLS-1$
       if ( this.pushDownWordWrite.size () == 0 )
       {
         result.append ( "\u03B5" ); //$NON-NLS-1$
       }
       else
       {
         result.append ( this.pushDownWordWrite );
       }
       result.append ( "\u2193" ); //$NON-NLS-1$
     }
     return result.toString ();
   }
 }
