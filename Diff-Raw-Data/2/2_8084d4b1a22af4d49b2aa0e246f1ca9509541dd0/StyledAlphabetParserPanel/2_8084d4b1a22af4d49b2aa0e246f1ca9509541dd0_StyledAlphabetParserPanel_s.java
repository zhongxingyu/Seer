 package de.unisiegen.gtitool.ui.style;
 
 
 import java.util.ArrayList;
 import java.util.TreeSet;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.Entity;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.entities.listener.AlphabetChangedListener;
 import de.unisiegen.gtitool.core.parser.alphabet.AlphabetParseable;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserException;
 import de.unisiegen.gtitool.core.parser.exceptions.ScannerException;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.style.listener.ParseableChangedListener;
 import de.unisiegen.gtitool.ui.style.parser.StyledParserPanel;
 
 
 /**
  * The styled {@link Alphabet} panel class.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class StyledAlphabetParserPanel extends StyledParserPanel
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -6870722718951231990L;
 
 
   /**
    * The parsed {@link Alphabet} can not contain this {@link Symbol}s.
    */
   private TreeSet < Symbol > notRemoveableSymbols = null;
 
 
   /**
    * Allocates a new {@link StyledAlphabetParserPanel}.
    */
   public StyledAlphabetParserPanel ()
   {
     super ( new AlphabetParseable () );
     super.addParseableChangedListener ( new ParseableChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void parseableChanged ( Object newObject )
       {
         fireAlphabetChanged ( ( Alphabet ) newObject );
       }
     } );
   }
 
 
   /**
    * Adds the given {@link AlphabetChangedListener}.
    * 
    * @param listener The {@link AlphabetChangedListener}.
    */
   public final synchronized void addAlphabetChangedListener (
       AlphabetChangedListener listener )
   {
     this.listenerList.add ( AlphabetChangedListener.class, listener );
   }
 
 
   /**
    * Checks the given {@link Alphabet}.
    * 
    * @param alphabet The {@link Alphabet} to check.
    * @return The input {@link Alphabet} or null, if the {@link Alphabet}
    *         contains symbols which can not be removed.
    */
   private final Alphabet checkAlphabet ( Alphabet alphabet )
   {
     ArrayList < ScannerException > exceptionList = new ArrayList < ScannerException > ();
 
    if ( alphabet != null )
     {
       for ( Symbol current : this.notRemoveableSymbols )
       {
         if ( !alphabet.contains ( current ) )
         {
           exceptionList.add ( new ParserException ( current.getParserOffset ()
               .getStart (), current.getParserOffset ().getEnd (), Messages
               .getString ( "AlphabetDialog.SymbolUsed", current ) ) ); //$NON-NLS-1$
         }
       }
     }
 
     if ( exceptionList.size () > 0 )
     {
       setException ( exceptionList );
       return null;
     }
 
     return alphabet;
   }
 
 
   /**
    * Let the listeners know that the {@link Alphabet} has changed.
    * 
    * @param newAlphabet The new {@link Alphabet}.
    */
   private final void fireAlphabetChanged ( Alphabet newAlphabet )
   {
     Alphabet checkedAlphabet = checkAlphabet ( newAlphabet );
     AlphabetChangedListener [] listeners = this.listenerList
         .getListeners ( AlphabetChangedListener.class );
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].alphabetChanged ( checkedAlphabet );
     }
   }
 
 
   /**
    * Returns the {@link Alphabet} for the program text within the document.
    * 
    * @return The {@link Alphabet} for the program text.
    */
   public final Alphabet getAlphabet ()
   {
     try
     {
       Alphabet alphabet = ( Alphabet ) getParsedObject ();
       return checkAlphabet ( alphabet );
     }
     catch ( Exception exc )
     {
       return null;
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see StyledParserPanel#parse()
    */
   @Override
   public final Alphabet parse ()
   {
     Alphabet alphabet = ( Alphabet ) super.parse ();
     return checkAlphabet ( alphabet );
   }
 
 
   /**
    * Removes the given {@link AlphabetChangedListener}.
    * 
    * @param listener The {@link AlphabetChangedListener}.
    */
   public final synchronized void removeAlphabetChangedListener (
       AlphabetChangedListener listener )
   {
     this.listenerList.remove ( AlphabetChangedListener.class, listener );
   }
 
 
   /**
    * Sets the {@link Symbol}s which should be highlighted.
    * 
    * @param symbols The {@link Symbol}s which should be highlighted.
    */
   public final void setHighlightedSymbol ( Iterable < Symbol > symbols )
   {
     setHighlightedParseableEntity ( symbols );
   }
 
 
   /**
    * Sets the {@link Symbol}s which should be highlighted.
    * 
    * @param symbols The {@link Symbol}s which should be highlighted.
    */
   public final void setHighlightedSymbol ( Symbol ... symbols )
   {
     Entity [] entities = new Entity [ symbols.length ];
     for ( int i = 0 ; i < symbols.length ; i++ )
     {
       entities [ i ] = symbols [ i ];
     }
     setHighlightedParseableEntity ( entities );
   }
 
 
   /**
    * Sets the {@link Symbol} which should be highlighted.
    * 
    * @param symbol The {@link Symbol} which should be highlighted.
    */
   public final void setHighlightedSymbol ( Symbol symbol )
   {
     setHighlightedParseableEntity ( symbol );
   }
 
 
   /**
    * Sets the {@link Symbol}s which should not be removeable.
    * 
    * @param notRemoveableSymbols The {@link Symbol}s which should not be
    *          removeable.
    * @see #notRemoveableSymbols
    */
   public final void setNotRemoveableSymbols (
       TreeSet < Symbol > notRemoveableSymbols )
   {
     this.notRemoveableSymbols = notRemoveableSymbols;
   }
 
 
   /**
    * Sets the {@link Alphabet} of the document.
    * 
    * @param alphabet The input {@link Alphabet}.
    */
   public final void setText ( Alphabet alphabet )
   {
     getEditor ().setText ( alphabet.toString () );
   }
 }
