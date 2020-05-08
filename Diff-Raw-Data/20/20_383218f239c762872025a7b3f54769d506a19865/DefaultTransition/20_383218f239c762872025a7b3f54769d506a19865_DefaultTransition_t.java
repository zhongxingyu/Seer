 package de.unisiegen.gtitool.core.entities;
 
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import javax.swing.event.EventListenerList;
 
 import de.unisiegen.gtitool.core.Messages;
 import de.unisiegen.gtitool.core.entities.listener.ModifyStatusChangedListener;
 import de.unisiegen.gtitool.core.entities.listener.TransitionChangedListener;
 import de.unisiegen.gtitool.core.exceptions.symbol.SymbolException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolNotInAlphabetException;
 import de.unisiegen.gtitool.core.exceptions.transition.TransitionSymbolOnlyOneTimeException;
 import de.unisiegen.gtitool.core.parser.ParserOffset;
 import de.unisiegen.gtitool.core.parser.style.PrettyPrintable;
 import de.unisiegen.gtitool.core.parser.style.PrettyString;
 import de.unisiegen.gtitool.core.parser.style.PrettyToken;
 import de.unisiegen.gtitool.core.parser.style.Style;
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
    * The {@link Alphabet} of this {@link DefaultTransition}.
    */
   private Alphabet alphabet = null;
 
 
   /**
    * The push down {@link Alphabet} of this {@link DefaultTransition}.
    */
   private Alphabet pushDownAlphabet;
 
 
   /**
    * The id of this {@link DefaultTransition}.
    */
   private int id = ID_NOT_DEFINED;
 
 
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
    * The initial set of {@link Symbol}s.
    */
   private TreeSet < Symbol > initialSymbolSet;
 
 
   /**
    * The {@link Word} which is read from the {@link Stack}.
    */
   private Word pushDownWordRead = new DefaultWord ();
 
 
   /**
    * The initial {@link Word} which is read from the {@link Stack}.
    */
   private Word initialPushDownWordRead = new DefaultWord ();
 
 
   /**
    * The {@link Word} which should be written on the {@link Stack}.
    */
   private Word pushDownWordWrite = new DefaultWord ();
 
 
   /**
    * The {@link Word} which should be written on the {@link Stack}.
    */
   private Word initialPushDownWordWrite = new DefaultWord ();
 
 
   /**
    * This {@link Transition} is a error {@link Transition}.
    */
   private boolean error = false;
 
 
   /**
    * This {@link Transition} is a active {@link Transition}.
    */
   private boolean active = false;
 
 
   /**
    * Allocates a new {@link DefaultTransition}.
    */
   public DefaultTransition ()
   {
     // SymbolSet
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     // Reset modify
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
 
     // Reset modify
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
 
     // Reset modify
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
    * @throws SymbolException If something with the {@link Symbol} is not
    *           correct.
    * @throws StoreException If the {@link Element} can not be parsed.
    */
   public DefaultTransition ( Element element )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException, SymbolException, StoreException
   {
     // Check if the element is correct
     if ( !element.getName ().equals ( "Transition" ) ) //$NON-NLS-1$
     {
       throw new IllegalArgumentException ( "element \"" + element.getName () //$NON-NLS-1$
           + "\" is not a transition" ); //$NON-NLS-1$
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
 
     // Reset modify
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
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     // PushDownWordRead
     setPushDownWordRead ( pushDownWordRead );
     // PushDownWordWrite
     setPushDownWordWrite ( pushDownWordWrite );
     // Symbols
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     // Reset modify
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
     this.symbolSet = new TreeSet < Symbol > ();
     this.initialSymbolSet = new TreeSet < Symbol > ();
 
     // PushDownWordRead
     setPushDownWordRead ( pushDownWordRead );
     // PushDownWordWrite
     setPushDownWordWrite ( pushDownWordWrite );
     // Symbols
     if ( symbols == null )
     {
       throw new NullPointerException ( "symbols is null" ); //$NON-NLS-1$
     }
     add ( symbols );
 
     // Reset modify
     resetModify ();
   }
 
 
   /**
    * Appends the specified {@link Symbol}s to the end of this
    * {@link DefaultTransition}.
    * 
    * @param symbols The {@link Symbol}s to be appended to this
    *          {@link DefaultTransition}.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
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
    * Appends the specified {@link Symbol} to the end of this
    * {@link DefaultTransition}.
    * 
    * @param symbol The {@link Symbol} to be appended to this
    *          {@link DefaultTransition}.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
    */
   public final void add ( Symbol symbol )
       throws TransitionSymbolNotInAlphabetException,
       TransitionSymbolOnlyOneTimeException
   {
     if ( ( this.alphabet != null ) && ( !this.alphabet.contains ( symbol ) ) )
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
    // The symbol must be cloned because of the different possible styles
    this.symbolSet.add ( symbol.clone () );
     fireTransitionChanged ();
     fireModifyStatusChanged ();
   }
 
 
   /**
    * Appends the specified {@link Symbol}s to the end of this
    * {@link DefaultTransition}.
    * 
    * @param symbols The {@link Symbol}s to be appended to this
    *          {@link DefaultTransition}.
    * @throws TransitionSymbolNotInAlphabetException If something with the
    *           {@link DefaultTransition} is not correct.
    * @throws TransitionSymbolOnlyOneTimeException If something with the
    *           {@link DefaultTransition} is not correct.
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
   public final synchronized void addModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.add ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * Adds the given {@link TransitionChangedListener}.
    * 
    * @param listener The {@link TransitionChangedListener}.
    */
   public final synchronized void addTransitionChangedListener (
       TransitionChangedListener listener )
   {
     this.listenerList.add ( TransitionChangedListener.class, listener );
   }
 
 
   /**
    * Removes all {@link Symbol}s.
    */
   public final void clear ()
   {
     this.symbolSet.clear ();
     fireTransitionChanged ();
     fireModifyStatusChanged ();
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Object#clone()
    */
   @Override
   public final DefaultTransition clone ()
   {
     DefaultTransition newDefaultTransition = null;
     try
     {
       newDefaultTransition = new DefaultTransition ( this.alphabet.clone (),
           this.pushDownAlphabet.clone (), this.pushDownWordRead.clone (),
           this.pushDownWordWrite.clone (), this.stateBegin, this.stateEnd );
       for ( Symbol current : this.symbolSet )
       {
         newDefaultTransition.add ( current.clone () );
       }
     }
     catch ( TransitionException exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
       return null;
     }
     return newDefaultTransition;
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
    * Returns true if this {@link DefaultTransition} contains the given
    * {@link Symbol}. Otherwise false.
    * 
    * @param symbol The {@link Symbol}.
    * @return True if this {@link DefaultTransition} contains the given
    *         {@link Symbol}. Otherwise false.
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
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].modifyStatusChanged ( newModifyStatus );
     }
   }
 
 
   /**
    * Let the listeners know that the {@link Transition} has changed.
    */
   private final void fireTransitionChanged ()
   {
     TransitionChangedListener [] listeners = this.listenerList
         .getListeners ( TransitionChangedListener.class );
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].transitionChanged ( this );
     }
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
    * Returns the id.
    * 
    * @return The id.
    * @see #id
    */
   public final int getId ()
   {
     return this.id;
   }
 
 
   /**
    * {@inheritDoc}
    */
   public final ParserOffset getParserOffset ()
   {
     return this.parserOffset;
   }
 
 
   /**
    * Returns the push down {@link Alphabet}.
    * 
    * @return The push down {@link Alphabet}.
    * @see #pushDownAlphabet
    */
   public final Alphabet getPushDownAlphabet ()
   {
     return this.pushDownAlphabet;
   }
 
 
   /**
    * Returns the {@link Word} which is read from the {@link Stack}.
    * 
    * @return The {@link Word} which is read from the {@link Stack}.
    */
   public final Word getPushDownWordRead ()
   {
     return this.pushDownWordRead;
   }
 
 
   /**
    * Returns the {@link Word} which should be written on the {@link Stack}.
    * 
    * @return The {@link Word} which should be written on the {@link Stack}.
    */
   public final Word getPushDownWordWrite ()
   {
     return this.pushDownWordWrite;
   }
 
 
   /**
    * Returns the {@link State} where the {@link DefaultTransition} begins.
    * 
    * @return The {@link State} where the {@link DefaultTransition} begins.
    * @see #stateBegin
    */
   public final State getStateBegin ()
   {
     return this.stateBegin;
   }
 
 
   /**
    * Returns the {@link State} id where the {@link DefaultTransition} begins.
    * 
    * @return The {@link State} id where the {@link DefaultTransition} begins.
    * @see #stateBeginId
    */
   public final int getStateBeginId ()
   {
     return this.stateBegin == null ? this.stateBeginId : this.stateBegin
         .getId ();
   }
 
 
   /**
    * Returns the {@link State} where the {@link DefaultTransition} ends.
    * 
    * @return The {@link State} where the {@link DefaultTransition} ends.
    * @see #stateEnd
    */
   public final State getStateEnd ()
   {
     return this.stateEnd;
   }
 
 
   /**
    * Returns the {@link State} id where the {@link DefaultTransition} ends.
    * 
    * @return The {@link State} id where the {@link DefaultTransition} ends.
    * @see #stateEndId
    */
   public final int getStateEndId ()
   {
     return this.stateEnd == null ? this.stateEndId : this.stateEnd.getId ();
   }
 
 
   /**
    * Returns the symbolSet.
    * 
    * @return The symbolSet.
    * @see #symbolSet
    */
   public final TreeSet < Symbol > getSymbol ()
   {
     return this.symbolSet;
   }
 
 
   /**
    * Returns the {@link Symbol} with the given index.
    * 
    * @param index The index.
    * @return The {@link Symbol} with the given index.
    * @see #symbolSet
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
    * Returns true if this {@link Transition} is a active {@link Transition},
    * otherwise false.
    * 
    * @return True if this {@link Transition} is a active {@link Transition},
    *         otherwise false.
    */
   public final boolean isActive ()
   {
     return this.active;
   }
 
 
   /**
    * Returns true, if this {@link DefaultTransition} is a epsilon
    * {@link DefaultTransition}, otherwise false.
    * 
    * @return True, if this {@link DefaultTransition} is a epsilon
    *         {@link DefaultTransition}, otherwise false.
    */
   public final boolean isEpsilonTransition ()
   {
     return this.symbolSet.size () == 0;
   }
 
 
   /**
    * Returns true if this {@link Transition} is a error {@link Transition},
    * otherwise false.
    * 
    * @return True if this {@link Transition} is a error {@link Transition},
    *         otherwise false.
    * @see #error
    */
   public final boolean isError ()
   {
     return this.error;
   }
 
 
   /**
    * Returns true if the id of this {@link DefaultTransition} is defined,
    * otherwise false.
    * 
    * @return True if the id of this {@link DefaultTransition} is defined,
    *         otherwise false.
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
    * Returns an iterator over the {@link Symbol}s in this
    * {@link DefaultTransition}.
    * 
    * @return An iterator over the {@link Symbol}s in this
    *         {@link DefaultTransition}.
    */
   public final Iterator < Symbol > iterator ()
   {
     return this.symbolSet.iterator ();
   }
 
 
   /**
    * Remove the given {@link Symbol}s from this {@link DefaultTransition}.
    * 
    * @param symbols The {@link Symbol}s to remove.
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
    * Removes the given {@link Symbol} from this {@link DefaultTransition}.
    * 
    * @param symbol The {@link Symbol} to remove.
    */
   public final void remove ( Symbol symbol )
   {
     if ( !this.symbolSet.contains ( symbol ) )
     {
       throw new IllegalArgumentException ( "symbol is not in this transition" ); //$NON-NLS-1$
     }
     this.symbolSet.remove ( symbol );
     fireTransitionChanged ();
     fireModifyStatusChanged ();
   }
 
 
   /**
    * Remove the given {@link Symbol}s from this {@link DefaultTransition}.
    * 
    * @param symbols The {@link Symbol}s to remove.
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
   public final synchronized void removeModifyStatusChangedListener (
       ModifyStatusChangedListener listener )
   {
     this.listenerList.remove ( ModifyStatusChangedListener.class, listener );
   }
 
 
   /**
    * Removes the given {@link TransitionChangedListener}.
    * 
    * @param listener The {@link TransitionChangedListener}.
    */
   public final synchronized void removeTransitionChangedListener (
       TransitionChangedListener listener )
   {
     this.listenerList.remove ( TransitionChangedListener.class, listener );
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
     this.initialPushDownWordRead = this.pushDownWordRead.clone ();
     this.initialPushDownWordWrite = this.pushDownWordWrite.clone ();
   }
 
 
   /**
    * Sets the active value.
    * 
    * @param active The active value to set.
    */
   public final void setActive ( boolean active )
   {
     this.active = active;
   }
 
 
   /**
    * Sets the {@link Alphabet} of this {@link DefaultTransition}.
    * 
    * @param alphabet The {@link Alphabet} to set.
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
    * Sets the error value.
    * 
    * @param error The error value to set.
    * @see #error
    */
   public final void setError ( boolean error )
   {
     this.error = error;
   }
 
 
   /**
    * Sets the id.
    * 
    * @param id The id to set.
    * @see #id
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
    */
   public final void setParserOffset ( ParserOffset parserOffset )
   {
     this.parserOffset = parserOffset;
   }
 
 
   /**
    * Sets the push down {@link Alphabet} of this {@link DefaultTransition}.
    * 
    * @param pushDownAlphabet The push down {@link Alphabet} to set.
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
    * Sets the {@link Word} which is read from the {@link Stack}.
    * 
    * @param pushDownWordRead The {@link Word} to set.
    */
   public final void setPushDownWordRead ( Word pushDownWordRead )
   {
     if ( pushDownWordRead == null )
     {
       throw new NullPointerException ( "push down word read is null" ); //$NON-NLS-1$
     }
     this.pushDownWordRead = pushDownWordRead;
     fireModifyStatusChanged ();
   }
 
 
   /**
    * The {@link Word} which should be written on the {@link Stack}.
    * 
    * @param pushDownWordWrite The {@link Word} to set.
    */
   public final void setPushDownWordWrite ( Word pushDownWordWrite )
   {
     if ( pushDownWordWrite == null )
     {
       throw new NullPointerException ( "push down word write is null" ); //$NON-NLS-1$
     }
     this.pushDownWordWrite = pushDownWordWrite;
     fireModifyStatusChanged ();
   }
 
 
   /**
    * Sets the {@link State} where the {@link DefaultTransition} begins.
    * 
    * @param stateBegin The {@link State} to set.
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
    * Sets the {@link State} where the {@link DefaultTransition} ends.
    * 
    * @param stateEnd The {@link State} to set.
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
    * Returns the number of {@link Symbol}s in this {@link DefaultTransition}.
    * 
    * @return The number of {@link Symbol}s in this {@link DefaultTransition}.
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
     PrettyString prettyString = new PrettyString ();
     if ( this.symbolSet.size () == 0 )
     {
       if ( this.error )
       {
         prettyString.addPrettyToken ( new PrettyToken (
             "\u03B5", Style.SYMBOL_ERROR ) ); //$NON-NLS-1$
       }
       else if ( this.active )
       {
         prettyString.addPrettyToken ( new PrettyToken (
             "\u03B5", Style.SYMBOL_ACTIVE ) ); //$NON-NLS-1$
       }
       else
       {
         prettyString
             .addPrettyToken ( new PrettyToken ( "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
       }
     }
     else
     {
       prettyString.addPrettyToken ( new PrettyToken ( "{", Style.NONE ) ); //$NON-NLS-1$
       boolean first = true;
       for ( Symbol current : this.symbolSet )
       {
         if ( !first )
         {
           prettyString.addPrettyToken ( new PrettyToken ( ", ", Style.NONE ) ); //$NON-NLS-1$
         }
         first = false;
         prettyString.addPrettyPrintable ( current );
       }
       prettyString.addPrettyToken ( new PrettyToken ( "}", Style.NONE ) ); //$NON-NLS-1$
     }
     if ( ( this.pushDownWordRead.size () > 0 )
         || ( this.pushDownWordWrite.size () > 0 ) )
     {
       prettyString.addPrettyToken ( new PrettyToken ( " ", Style.NONE ) ); //$NON-NLS-1$
       if ( this.pushDownWordRead.size () == 0 )
       {
         prettyString
             .addPrettyToken ( new PrettyToken ( "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
       }
       else
       {
         prettyString.addPrettyPrintable ( this.pushDownWordRead );
       }
       prettyString
           .addPrettyToken ( new PrettyToken ( "\u2191", Style.KEYWORD ) ); //$NON-NLS-1$
       prettyString.addPrettyToken ( new PrettyToken ( " ", Style.NONE ) ); //$NON-NLS-1$
       if ( this.pushDownWordWrite.size () == 0 )
       {
         prettyString
             .addPrettyToken ( new PrettyToken ( "\u03B5", Style.SYMBOL ) ); //$NON-NLS-1$
       }
       else
       {
         prettyString.addPrettyPrintable ( this.pushDownWordWrite );
       }
       prettyString
           .addPrettyToken ( new PrettyToken ( "\u2193", Style.KEYWORD ) ); //$NON-NLS-1$
     }
     return prettyString;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#toString()
    */
   @Override
   public final String toString ()
   {
     StringBuilder result = new StringBuilder ();
     if ( this.symbolSet.size () == 0 )
     {
       result.append ( "\u03B5" ); //$NON-NLS-1$
     }
     else
     {
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
     }
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
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Entity#toStringDebug()
    */
   public final String toStringDebug ()
   {
     String lineBreak = System.getProperty ( "line.separator" ); //$NON-NLS-1$
     StringBuilder result = new StringBuilder ();
     result.append ( "Alphabet:    " + this.alphabet.toString () + lineBreak ); //$NON-NLS-1$
     result.append ( "Begin state: " + this.stateBegin.toString () + lineBreak ); //$NON-NLS-1$
     result.append ( "End state:   " + this.stateEnd.toString () + lineBreak ); //$NON-NLS-1$
     result.append ( "Symbols:     " + this.symbolSet.toString () ); //$NON-NLS-1$
     return result.toString ();
   }
 }
