 package de.unisiegen.gtitool.ui.style.parser;
 
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import java_cup.runtime.Symbol;
 
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import org.apache.log4j.Logger;
 
 import de.unisiegen.gtitool.core.entities.ParseableEntity;
 import de.unisiegen.gtitool.core.parser.GTIParser;
 import de.unisiegen.gtitool.core.parser.Parseable;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserException;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserMultiException;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserWarningException;
 import de.unisiegen.gtitool.core.parser.exceptions.ScannerException;
 import de.unisiegen.gtitool.core.parser.scanner.AbstractScanner;
 import de.unisiegen.gtitool.core.parser.scanner.GTIScanner;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.preferences.listener.ColorChangedAdapter;
 import de.unisiegen.gtitool.ui.preferences.listener.ExceptionsChangedListener;
 import de.unisiegen.gtitool.ui.style.listener.ParseableChangedListener;
 
 
 /**
  * An implementation of the {@link StyledDocument} interface to enable syntax
  * highlighting using the lexer.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public final class StyledParserDocument extends DefaultStyledDocument
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -2546142812930077554L;
 
 
   /**
    * The {@link Logger} for this class.
    * 
    * @see Logger
    */
   private static final Logger logger = Logger
       .getLogger ( StyledParserDocument.class );
 
 
   /**
    * The {@link Parseable} for which this document was allocated.
    */
   private Parseable parseable;
 
 
   /**
    * The current exceptions from the scanner and parser.
    * 
    * @see #getExceptionList()
    */
   private ArrayList < ScannerException > exceptionList;
 
 
   /**
    * The attributes default style.
    */
   private SimpleAttributeSet normalSet = new SimpleAttributeSet ();
 
 
   /**
    * The attributes for the various {@link Style}s.
    */
   private HashMap < Style, SimpleAttributeSet > attributes = new HashMap < Style, SimpleAttributeSet > ();
 
 
   /**
    * The parser warning color.
    */
   private Color parserWarningColor;
 
 
   /**
    * The parser warning highlighting.
    */
   private Color parserHighlightingColor;
 
 
   /**
    * Flag that indicates if the panel is read only.
    */
   private boolean editable = true;
 
 
   /**
    * The parsed object.
    */
   private Object parsedObject;
 
 
   /**
    * The highlighted {@link ParseableEntity} list.
    */
   private ArrayList < ParseableEntity > highlightedParseableEntityList;
 
 
   /**
    * Allocates a new <code>StyledParserDocument</code> for the given
    * <code>pParseable</code>, where the <code>pParseable</code> is used to
    * determine the scanner for the documents content and thereby dictates the
    * syntax highlighting.
    * 
    * @param pParseable The {@link Parseable} for which to allocate a document.
    * @throws NullPointerException if the <code>pParseable</code> is
    *           <code>null</code>.
    */
   public StyledParserDocument ( Parseable pParseable )
   {
     if ( pParseable == null )
     {
       throw new NullPointerException ( "parseable is null" ); //$NON-NLS-1$
     }
     this.parseable = pParseable;
     this.highlightedParseableEntityList = new ArrayList < ParseableEntity > ();
     StyleConstants.setForeground ( this.normalSet, Color.BLACK );
     StyleConstants.setBold ( this.normalSet, false );
     SimpleAttributeSet stateSet = new SimpleAttributeSet ();
     StyleConstants.setBold ( stateSet, true );
     this.attributes.put ( Style.STATE, stateSet );
     SimpleAttributeSet symbolSet = new SimpleAttributeSet ();
     StyleConstants.setBold ( symbolSet, true );
     this.attributes.put ( Style.SYMBOL, symbolSet );
     initAttributes ();
     this.parserWarningColor = PreferenceManager.getInstance ()
         .getColorItemParserWarning ().getColor ();
     this.parserHighlightingColor = PreferenceManager.getInstance ()
         .getColorItemParserHighlighting ().getColor ();
 
     /*
      * ColorChangedListener
      */
     PreferenceManager.getInstance ().addColorChangedListener (
         new ColorChangedAdapter ()
         {
 
           /**
            * {@inheritDoc}
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedParserHighlighting ( Color pNewColor )
           {
             StyledParserDocument.this.parserHighlightingColor = pNewColor;
             processChanged ();
           }
 
 
           /**
            * {@inheritDoc}
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedParserSymbol ( @SuppressWarnings ( "unused" )
           Color pNewColor )
           {
             initAttributes ();
             processChanged ();
           }
 
 
           /**
            * {@inheritDoc}
            */
           @SuppressWarnings ( "synthetic-access" )
           @Override
           public void colorChangedParserWarning ( Color pNewColor )
           {
             StyledParserDocument.this.parserWarningColor = pNewColor;
             processChanged ();
           }
         } );
   }
 
 
   /**
    * Adds the given {@link ExceptionsChangedListener}.
    * 
    * @param pListener The {@link ExceptionsChangedListener}.
    */
   public final synchronized void addExceptionsChangedListener (
       ExceptionsChangedListener pListener )
   {
     this.listenerList.add ( ExceptionsChangedListener.class, pListener );
   }
 
 
   /**
    * Adds the given {@link ParseableChangedListener}.
    * 
    * @param pListener The {@link ParseableChangedListener}.
    */
   public final synchronized void addParseableChangedListener (
       ParseableChangedListener pListener )
   {
     this.listenerList.add ( ParseableChangedListener.class, pListener );
   }
 
 
   /**
    * Let the listeners know that the exceptions has changed.
    */
   public final void fireExceptionsChanged ()
   {
     ExceptionsChangedListener [] listeners = this.listenerList
         .getListeners ( ExceptionsChangedListener.class );
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].exceptionsChanged ();
     }
   }
 
 
   /**
    * Let the listeners know that the {@link Object} has changed.
    * 
    * @param pNewObject The new {@link Object}.
    */
   public final void fireParseableChanged ( Object pNewObject )
   {
     ParseableChangedListener [] listeners = this.listenerList
         .getListeners ( ParseableChangedListener.class );
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].parseableChanged ( pNewObject );
     }
   }
 
 
   /**
    * Returns the error set.
    * 
    * @return The error set.
    */
   private SimpleAttributeSet getAttributeSetError ()
   {
     SimpleAttributeSet errorSet = new SimpleAttributeSet ();
     StyleConstants.setForeground ( errorSet, Color.RED );
     StyleConstants.setBold ( errorSet, true );
     StyleConstants.setUnderline ( errorSet, true );
     return errorSet;
   }
 
 
   /**
    * Returns the highlighted {@link ParseableEntity} set.
    * 
    * @return The highlighted {@link ParseableEntity} set.
    */
   private SimpleAttributeSet getAttributeSetHighlightedParseableEntity ()
   {
     SimpleAttributeSet highlightedParseableEntitySet = new SimpleAttributeSet ();
     StyleConstants.setBackground ( highlightedParseableEntitySet,
         this.parserHighlightingColor );
     return highlightedParseableEntitySet;
   }
 
 
   /**
    * Returns the warning set.
    * 
    * @return The warning set.
    */
   private SimpleAttributeSet getAttributeSetWarning ()
   {
     SimpleAttributeSet warningSet = new SimpleAttributeSet ();
     StyleConstants.setBackground ( warningSet, this.parserWarningColor );
     return warningSet;
   }
 
 
   /**
    * Returns the current {@link ScannerException}s that were detected while
    * trying to interpret the token stream.
    * 
    * @return The exceptions.
    */
   public final ArrayList < ScannerException > getExceptionList ()
   {
     return this.exceptionList;
   }
 
 
   /**
    * Returns the {@link Object} for the program text within this document.
    * Throws an exception if a parsing error occurred.
    * 
    * @return The {@link Object} for the program text.
    * @throws Exception If a parsing error occurred.
    */
   public final Object getParsedObject () throws Exception
   {
     return this.parsedObject;
   }
 
 
   /**
    * Highlights the {@link ParseableEntity}s.
    */
   private final void highlightedParseableEntities ()
   {
    processChanged ();
     for ( ParseableEntity current : this.highlightedParseableEntityList )
     {
       SimpleAttributeSet highlightedParseableEntitySet = getAttributeSetHighlightedParseableEntity ();
       highlightedParseableEntitySet.addAttribute ( "highlighting", current ); //$NON-NLS-1$
       if ( current.getParserStartOffset () < 0
           && current.getParserEndOffset () < 0 )
       {
         setCharacterAttributes ( getLength (), getLength (),
             highlightedParseableEntitySet, false );
       }
       else
       {
         setCharacterAttributes ( current.getParserStartOffset (), current
             .getParserEndOffset ()
             - current.getParserStartOffset (), highlightedParseableEntitySet,
             false );
       }
     }
   }
 
 
   /**
    * Initializes the attributes.
    */
   private final void initAttributes ()
   {
     StyleConstants
         .setForeground ( this.attributes.get ( Style.STATE ), PreferenceManager
             .getInstance ().getColorItemParserState ().getColor () );
     StyleConstants.setForeground ( this.attributes.get ( Style.SYMBOL ),
         PreferenceManager.getInstance ().getColorItemParserSymbol ()
             .getColor () );
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see AbstractDocument#insertString(int, String, AttributeSet)
    */
   @Override
   public final void insertString ( int pOffset, String pString,
       AttributeSet pAttributeSet ) throws BadLocationException
   {
     super.insertString ( pOffset, pString, pAttributeSet );
     fireParseableChanged ( processChanged () );
   }
 
 
   /**
    * Return the editable value.
    * 
    * @return The editable value.
    */
   public final boolean isEditable ()
   {
     return this.editable;
   }
 
 
   /**
    * Parses the document.
    */
   public final void parse ()
   {
     fireParseableChanged ( processChanged () );
   }
 
 
   /**
    * Processes the document content after a change and returns the parsed object
    * or null, if the text could not be parsed.
    * 
    * @return The parsed object or null, if the text could not be parsed.
    */
   protected final Object processChanged ()
   {
     this.parsedObject = null;
     setCharacterAttributes ( 0, getLength (), this.normalSet, true );
     ArrayList < ScannerException > collectedExceptions = new ArrayList < ScannerException > ();
     try
     {
       /*
        * Start scanner
        */
       int offset = 0;
       String content = getText ( offset, getLength () );
       final GTIScanner scanner = this.parseable.newScanner ( content );
       final LinkedList < Symbol > symbols = new LinkedList < Symbol > ();
       while ( true )
       {
         try
         {
           Symbol symbol = scanner.nextSymbol ();
           if ( symbol == null )
           {
             break;
           }
           symbols.add ( symbol );
           SimpleAttributeSet set = this.attributes.get ( scanner
               .getStyleBySymbol ( symbol ) );
           if ( set == null )
           {
             set = this.normalSet;
           }
           setCharacterAttributes ( offset + symbol.left, symbol.right
               - symbol.left, set, true );
         }
         catch ( ScannerException ecx )
         {
           int newOffset = offset + ecx.getRight ();
           content = content.substring ( ecx.getRight () );
           ecx = new ScannerException ( offset + ecx.getLeft (), offset
               + ecx.getRight (), ecx.getMessage (), ecx.getCause () );
           SimpleAttributeSet errorSet = getAttributeSetError ();
           errorSet.addAttribute ( "exception", ecx ); //$NON-NLS-1$
           setCharacterAttributes ( ecx.getLeft (), ecx.getRight ()
               - ecx.getLeft (), errorSet, false );
           offset = newOffset;
           scanner.restart ( content );
           collectedExceptions.add ( ecx );
         }
       }
       /*
        * Start parser only if the scanner has no exceptions
        */
       if ( collectedExceptions.size () == 0 )
       {
         GTIParser parser = this.parseable.newParser ( new AbstractScanner ()
         {
 
           @Override
           public Style getStyleBySymbolId ( int pId )
           {
             return ( ( AbstractScanner ) scanner ).getStyleBySymbolId ( pId );
           }
 
 
           public Symbol nextSymbol () throws ScannerException
           {
             return ( !symbols.isEmpty () ) ? symbols.poll () : null;
           }
 
 
           public void restart ( @SuppressWarnings ( "unused" )
           String pText )
           {
             throw new UnsupportedOperationException ();
           }
         } );
         try
         {
           this.parsedObject = parser.parse ();
         }
         catch ( ParserMultiException ecx )
         {
           String [] message = ecx.getMessages ();
           int [] startOffset = ecx.getParserStartOffset ();
           int [] endOffset = ecx.getParserEndOffset ();
           for ( int i = 0 ; i < startOffset.length ; i++ )
           {
             ParserException newException = new ParserException (
                 startOffset [ i ], endOffset [ i ], message [ i ] );
             SimpleAttributeSet errorSet = getAttributeSetError ();
             errorSet.addAttribute ( "exception", newException ); //$NON-NLS-1$
             setCharacterAttributes ( startOffset [ i ], endOffset [ i ]
                 - startOffset [ i ], errorSet, false );
             collectedExceptions.add ( newException );
           }
         }
         catch ( ParserWarningException ecx )
         {
           if ( this.editable )
           {
             SimpleAttributeSet warningSet = getAttributeSetWarning ();
             warningSet.addAttribute ( "warning", ecx ); //$NON-NLS-1$
             if ( ecx.getLeft () < 0 && ecx.getRight () < 0 )
             {
               setCharacterAttributes ( getLength (), getLength (), warningSet,
                   false );
             }
             else
             {
               setCharacterAttributes ( ecx.getLeft (), ecx.getRight ()
                   - ecx.getLeft (), warningSet, false );
             }
             collectedExceptions.add ( new ParserWarningException ( ecx
                 .getRight (), ecx.getRight (), ecx.getMessage (), ecx
                 .getInsertText () ) );
           }
         }
         catch ( ParserException ecx )
         {
           SimpleAttributeSet errorSet = getAttributeSetError ();
           errorSet.addAttribute ( "exception", ecx ); //$NON-NLS-1$
           if ( ecx.getLeft () < 0 && ecx.getRight () < 0 )
           {
             setCharacterAttributes ( getLength (), getLength (), errorSet,
                 false );
           }
           else
           {
             setCharacterAttributes ( ecx.getLeft (), ecx.getRight ()
                 - ecx.getLeft (), errorSet, false );
           }
           collectedExceptions.add ( ecx );
         }
       }
     }
     catch ( Exception exc )
     {
       logger.error ( "failed to process changes", exc ); //$NON-NLS-1$
     }
 
     if ( !collectedExceptions.equals ( this.exceptionList ) )
     {
       this.exceptionList = collectedExceptions;
       fireExceptionsChanged ();
     }
     return this.parsedObject;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Document#remove(int, int)
    */
   @Override
   public final void remove ( int pOffset, int pLength )
       throws BadLocationException
   {
     super.remove ( pOffset, pLength );
     fireParseableChanged ( processChanged () );
   }
 
 
   /**
    * Removes the given {@link ExceptionsChangedListener}.
    * 
    * @param pListener The {@link ExceptionsChangedListener}.
    */
   public final synchronized void removeExceptionsChangedListener (
       ExceptionsChangedListener pListener )
   {
     this.listenerList.remove ( ExceptionsChangedListener.class, pListener );
   }
 
 
   /**
    * Removes the given {@link ParseableChangedListener}.
    * 
    * @param pListener The {@link ParseableChangedListener}.
    */
   public final synchronized void removeParseableChangedListener (
       ParseableChangedListener pListener )
   {
     this.listenerList.remove ( ParseableChangedListener.class, pListener );
   }
 
 
   /**
    * Sets the editable value.
    * 
    * @param pEditable The boolean to be set.
    */
   public final void setEditable ( boolean pEditable )
   {
     this.editable = pEditable;
   }
 
 
   /**
    * Sets the exceptions.
    * 
    * @param pExceptions The exceptions to set.
    */
   public final void setException ( Iterable < ScannerException > pExceptions )
   {
     if ( !pExceptions.equals ( this.exceptionList ) )
     {
       this.exceptionList.clear ();
       Iterator < ScannerException > iterator = pExceptions.iterator ();
       while ( iterator.hasNext () )
       {
         this.exceptionList.add ( iterator.next () );
       }
       for ( ScannerException current : this.exceptionList )
       {
         SimpleAttributeSet errorSet = getAttributeSetError ();
         errorSet.addAttribute ( "exception", current ); //$NON-NLS-1$
         if ( current.getLeft () < 0 && current.getRight () < 0 )
         {
           setCharacterAttributes ( getLength (), getLength (), errorSet, false );
         }
         else
         {
           setCharacterAttributes ( current.getLeft (), current.getRight ()
               - current.getLeft (), errorSet, false );
         }
       }
       fireExceptionsChanged ();
     }
   }
 
 
   /**
    * Sets the exception.
    * 
    * @param pException The exception to set.
    */
   public final void setException ( ScannerException pException )
   {
     ArrayList < ScannerException > exceptions = new ArrayList < ScannerException > ();
     exceptions.add ( pException );
     setException ( exceptions );
   }
 
 
   /**
    * Sets the {@link ParseableEntity}s which should be highlighted.
    * 
    * @param pParseableEntities The {@link ParseableEntity}s which should be
    *          highlighted.
    */
   public final void setHighlightedParseableEntity (
       Iterable < ? extends ParseableEntity > pParseableEntities )
   {
     this.highlightedParseableEntityList.clear ();
     for ( ParseableEntity current : pParseableEntities )
     {
       this.highlightedParseableEntityList.add ( current );
     }
     highlightedParseableEntities ();
   }
 
 
   /**
    * Sets the {@link ParseableEntity} which should be highlighted.
    * 
    * @param pParseableEntity The {@link ParseableEntity} which should be
    *          highlighted.
    */
   public final void setHighlightedParseableEntity (
       ParseableEntity pParseableEntity )
   {
     ArrayList < ParseableEntity > parseableEntities = new ArrayList < ParseableEntity > ();
     parseableEntities.add ( pParseableEntity );
     setHighlightedParseableEntity ( parseableEntities );
   }
 }
