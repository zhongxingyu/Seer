 package de.unisiegen.gtitool.ui.style.parser;
 
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java_cup.runtime.Symbol;
 
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.Document;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import de.unisiegen.gtitool.core.entities.Entity;
 import de.unisiegen.gtitool.core.parser.GTIParser;
 import de.unisiegen.gtitool.core.parser.Parseable;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserException;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserMultiException;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserWarningException;
 import de.unisiegen.gtitool.core.parser.exceptions.ScannerException;
 import de.unisiegen.gtitool.core.parser.scanner.AbstractScanner;
 import de.unisiegen.gtitool.core.parser.scanner.GTIScanner;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.core.preferences.listener.ColorChangedAdapter;
 import de.unisiegen.gtitool.ui.preferences.PreferenceManager;
 import de.unisiegen.gtitool.ui.style.listener.ExceptionsChangedListener;
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
    * The {@link Parseable} for which this document was allocated.
    */
   private Parseable parseable;
 
 
   /**
    * The current exceptions from the scanner and parser.
    * 
    * @see #getException()
    */
   private ArrayList < ScannerException > exceptionList;
 
 
   /**
    * The extern exceptions.
    * 
    * @see #getException()
    */
   private ArrayList < ScannerException > externExceptionList;
 
 
   /**
    * Flag that indicates if the panel is read only.
    */
   private boolean editable = true;
 
 
   /**
    * The parsed object.
    */
   private Object parsedObject;
 
 
   /**
    * The highlighted {@link Entity} list.
    */
   private ArrayList < Entity > highlightedParseableEntityList;
 
 
   /**
    * The attributes default style.
    */
   private SimpleAttributeSet normalSet = new SimpleAttributeSet ();
 
 
   /**
    * The overwritten {@link Style}.
    */
   private HashMap < String, Style > overwrittenStyle = new HashMap < String, Style > ();
 
 
   /**
    * Allocates a new {@link StyledParserDocument} for the given
    * {@link Parseable}, where the {@link Parseable} is used to determine the
    * scanner for the documents content and thereby dictates the syntax
    * highlighting.
    * 
    * @param parseable The {@link Parseable} for which to allocate a document.
    * @throws NullPointerException if the {@link Parseable} is null.
    */
   public StyledParserDocument ( Parseable parseable )
   {
     if ( parseable == null )
     {
       throw new NullPointerException ( "parseable is null" ); //$NON-NLS-1$
     }
     this.parseable = parseable;
     this.highlightedParseableEntityList = new ArrayList < Entity > ();
 
     this.exceptionList = new ArrayList < ScannerException > ();
     this.externExceptionList = new ArrayList < ScannerException > ();
 
     StyleConstants.setForeground ( this.normalSet, Color.BLACK );
     StyleConstants.setBold ( this.normalSet, false );
 
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
           public void colorChanged ()
           {
             parse ();
           }
         } );
   }
 
 
   /**
    * Adds the given {@link ExceptionsChangedListener}.
    * 
    * @param listener The {@link ExceptionsChangedListener}.
    */
   public final synchronized void addExceptionsChangedListener (
       ExceptionsChangedListener listener )
   {
     this.listenerList.add ( ExceptionsChangedListener.class, listener );
   }
 
 
   /**
    * Adds the given style.
    * 
    * @param token The token.
    * @param style The {@link Style}.
    */
   public final void addOverwrittenStyle ( String token, Style style )
   {
     this.overwrittenStyle.put ( token, style );
   }
 
 
   /**
    * Adds the given {@link ParseableChangedListener}.
    * 
    * @param listener The {@link ParseableChangedListener}.
    */
   public final synchronized void addParseableChangedListener (
       ParseableChangedListener listener )
   {
     this.listenerList.add ( ParseableChangedListener.class, listener );
   }
 
 
   /**
    * Clears the overwritten {@link Style}.
    */
   public final void clearOverwrittenStyle ()
   {
     this.overwrittenStyle.clear ();
   }
 
 
   /**
    * Let the listeners know that the exceptions has changed.
    */
   public final void fireExceptionsChanged ()
   {
     ExceptionsChangedListener [] listeners = this.listenerList
         .getListeners ( ExceptionsChangedListener.class );
     for ( ExceptionsChangedListener current : listeners )
     {
       current.exceptionsChanged ();
     }
   }
 
 
   /**
    * Let the listeners know that the {@link Object} has changed.
    * 
    * @param newObject The new {@link Object}.
    */
   public final void fireParseableChanged ( Object newObject )
   {
     ParseableChangedListener [] listeners = this.listenerList
         .getListeners ( ParseableChangedListener.class );
     for ( ParseableChangedListener current : listeners )
     {
       current.parseableChanged ( newObject );
     }
   }
 
 
   /**
    * Returns the error set.
    * 
    * @return The error set.
    */
   private final SimpleAttributeSet getAttributeSetError ()
   {
     SimpleAttributeSet errorSet = new SimpleAttributeSet ();
     StyleConstants.setForeground ( errorSet, PreferenceManager.getInstance ()
         .getColorItemParserError ().getColor () );
     StyleConstants.setBold ( errorSet, true );
     StyleConstants.setUnderline ( errorSet, true );
     return errorSet;
   }
 
 
   /**
    * Returns the highlighted {@link Entity} set.
    * 
    * @return The highlighted {@link Entity} set.
    */
   private final SimpleAttributeSet getAttributeSetHighlightedParseableEntity ()
   {
     SimpleAttributeSet highlightedParseableEntitySet = new SimpleAttributeSet ();
     StyleConstants.setBackground ( highlightedParseableEntitySet,
         PreferenceManager.getInstance ().getColorItemParserHighlighting ()
             .getColor () );
     return highlightedParseableEntitySet;
   }
 
 
   /**
    * Returns the warning set.
    * 
    * @return The warning set.
    */
   private final SimpleAttributeSet getAttributeSetWarning ()
   {
     SimpleAttributeSet warningSet = new SimpleAttributeSet ();
     StyleConstants.setBackground ( warningSet, PreferenceManager.getInstance ()
         .getColorItemParserWarning ().getColor () );
     return warningSet;
   }
 
 
   /**
    * Returns the current {@link ScannerException}s that were detected while
    * trying to interpret the token stream and the {@link ScannerException}s
    * which were set from extern.
    * 
    * @return The {@link ScannerException}s.
    */
   public final ArrayList < ScannerException > getException ()
   {
     ArrayList < ScannerException > result = new ArrayList < ScannerException > ();
     result.addAll ( this.exceptionList );
     result.addAll ( this.externExceptionList );
     return result;
   }
 
 
   /**
    * Returns the {@link Object} for the program text within this document.
    * Throws an exception if a parsing error occurred.
    * 
    * @return The {@link Object} for the program text.
    */
   public final Object getParsedObject ()
   {
     return this.parsedObject;
   }
 
 
   /**
    * Highlights the {@link Entity}s.
    */
   private final void highlightedParseableEntities ()
   {
     parse ();
     for ( Entity current : this.highlightedParseableEntityList )
     {
       SimpleAttributeSet highlightedParseableEntitySet = getAttributeSetHighlightedParseableEntity ();
       highlightedParseableEntitySet.addAttribute ( "highlighting", current ); //$NON-NLS-1$
       if ( ( current.getParserOffset ().getStart () < 0 )
           && ( current.getParserOffset ().getEnd () < 0 ) )
       {
         setCharacterAttributes ( getLength (), getLength (),
             highlightedParseableEntitySet, false );
       }
       else
       {
         setCharacterAttributes ( current.getParserOffset ().getStart (),
             current.getParserOffset ().getEnd ()
                 - current.getParserOffset ().getStart (),
             highlightedParseableEntitySet, false );
       }
     }
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see AbstractDocument#insertString(int, String, AttributeSet)
    */
   @Override
   public final void insertString ( int offset, String string,
       AttributeSet attributeSet ) throws BadLocationException
   {
     super.insertString ( offset, string, attributeSet );
     fireParseableChanged ( parse () );
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
    * Parses the document and returns the parsed object or null, if the text
    * could not be parsed.
    * 
    * @return The parsed object or null, if the text could not be parsed.
    */
   public final Object parse ()
   {
    this.exceptionList.clear ();
     this.externExceptionList.clear ();
 
     this.parsedObject = null;
     setCharacterAttributes ( 0, getLength (), this.normalSet, true );
     try
     {
       /*
        * Start scanner
        */
       int offset = 0;
       String content = getText ( offset, getLength () );
       final GTIScanner scanner = this.parseable.newScanner ( content );
       final ArrayList < Symbol > symbols = new ArrayList < Symbol > ();
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
           Style style = scanner.getStyleBySymbol ( symbol );
           SimpleAttributeSet set = new SimpleAttributeSet ();
 
           // Use the overwritten
           if ( this.overwrittenStyle.containsKey ( symbol.value.toString () ) )
           {
             Style newStyle = this.overwrittenStyle.get ( symbol.value
                 .toString () );
             StyleConstants.setForeground ( set, newStyle.getColor () );
             StyleConstants.setBold ( set, newStyle.isBold () );
             StyleConstants.setItalic ( set, newStyle.isItalic () );
           }
           // Use the normal scanner style
           else
           {
             StyleConstants.setForeground ( set, style.getColor () );
             StyleConstants.setBold ( set, style.isBold () );
             StyleConstants.setItalic ( set, style.isItalic () );
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
          this.exceptionList.add ( ecx );
         }
       }
       /*
        * Start parser only if the scanner has no exceptions
        */
      if ( this.exceptionList.size () == 0 )
       {
         GTIParser parser = this.parseable.newParser ( new AbstractScanner ()
         {
 
           @Override
           public Style getStyleBySymbolId ( int id )
           {
             return ( ( AbstractScanner ) scanner ).getStyleBySymbolId ( id );
           }
 
 
           public Symbol nextSymbol () throws ScannerException
           {
             if ( symbols.isEmpty () )
             {
               return null;
             }
             return symbols.remove ( 0 );
           }
 
 
           public void restart ( @SuppressWarnings ( "unused" )
           String text )
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
            this.exceptionList.add ( newException );
           }
         }
         catch ( ParserWarningException ecx )
         {
           if ( this.editable )
           {
             SimpleAttributeSet warningSet = getAttributeSetWarning ();
             warningSet.addAttribute ( "warning", ecx ); //$NON-NLS-1$
             if ( ( ecx.getLeft () < 0 ) && ( ecx.getRight () < 0 ) )
             {
               setCharacterAttributes ( getLength (), getLength (), warningSet,
                   false );
             }
             else
             {
               setCharacterAttributes ( ecx.getLeft (), ecx.getRight ()
                   - ecx.getLeft (), warningSet, false );
             }
            this.exceptionList.add ( new ParserWarningException ( ecx
                .getRight (), ecx.getRight (), ecx.getMessage (), ecx
                .getInsertText () ) );
           }
         }
         catch ( ParserException ecx )
         {
           SimpleAttributeSet errorSet = getAttributeSetError ();
           errorSet.addAttribute ( "exception", ecx ); //$NON-NLS-1$
           if ( ( ecx.getLeft () < 0 ) && ( ecx.getRight () < 0 ) )
           {
             setCharacterAttributes ( getLength (), getLength (), errorSet,
                 false );
           }
           else
           {
             setCharacterAttributes ( ecx.getLeft (), ecx.getRight ()
                 - ecx.getLeft (), errorSet, false );
           }
          this.exceptionList.add ( ecx );
         }
       }
     }
     catch ( Exception exc )
     {
       exc.printStackTrace ();
       System.exit ( 1 );
     }
 
    fireExceptionsChanged ();
     return this.parsedObject;
   }
 
 
   /**
    * {@inheritDoc}
    * 
    * @see Document#remove(int, int)
    */
   @Override
   public final void remove ( int offset, int length )
       throws BadLocationException
   {
     super.remove ( offset, length );
     fireParseableChanged ( parse () );
   }
 
 
   /**
    * Removes the given {@link ExceptionsChangedListener}.
    * 
    * @param listener The {@link ExceptionsChangedListener}.
    */
   public final synchronized void removeExceptionsChangedListener (
       ExceptionsChangedListener listener )
   {
     this.listenerList.remove ( ExceptionsChangedListener.class, listener );
   }
 
 
   /**
    * Removes the given token.
    * 
    * @param token The token to remove.
    */
   public final void removeOverwrittenStyle ( String token )
   {
     this.overwrittenStyle.remove ( token );
   }
 
 
   /**
    * Removes the given {@link ParseableChangedListener}.
    * 
    * @param listener The {@link ParseableChangedListener}.
    */
   public final synchronized void removeParseableChangedListener (
       ParseableChangedListener listener )
   {
     this.listenerList.remove ( ParseableChangedListener.class, listener );
   }
 
 
   /**
    * Sets the editable value.
    * 
    * @param editable The boolean to be set.
    */
   public final void setEditable ( boolean editable )
   {
     this.editable = editable;
   }
 
 
   /**
    * Sets the extern {@link ScannerException}s.
    * 
    * @param exceptions The {@link ScannerException}s to set.
    * @see #externExceptionList
    */
   public final void setException ( Iterable < ScannerException > exceptions )
   {
     this.externExceptionList.clear ();
     Iterator < ScannerException > iterator = exceptions.iterator ();
     while ( iterator.hasNext () )
     {
       this.externExceptionList.add ( iterator.next () );
     }
     for ( ScannerException current : this.externExceptionList )
     {
       SimpleAttributeSet errorSet = getAttributeSetError ();
       errorSet.addAttribute ( "exception", current ); //$NON-NLS-1$
       if ( ( current.getLeft () < 0 ) && ( current.getRight () < 0 ) )
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
 
 
   /**
    * Sets the {@link Entity}s which should be highlighted.
    * 
    * @param entities The {@linkEntity}s which should be highlighted.
    */
   public final void setHighlightedParseableEntity ( Entity ... entities )
   {
     this.highlightedParseableEntityList.clear ();
     for ( Entity current : entities )
     {
       this.highlightedParseableEntityList.add ( current );
     }
     highlightedParseableEntities ();
   }
 
 
   /**
    * Sets the {@link Entity} which should be highlighted.
    * 
    * @param entity The {@link Entity} which should be highlighted.
    */
   public final void setHighlightedParseableEntity ( Entity entity )
   {
     ArrayList < Entity > entities = new ArrayList < Entity > ();
     entities.add ( entity );
     setHighlightedParseableEntity ( entities );
   }
 
 
   /**
    * Sets the {@link Entity}s which should be highlighted.
    * 
    * @param entities The {@link Entity}s which should be highlighted.
    */
   public final void setHighlightedParseableEntity (
       Iterable < ? extends Entity > entities )
   {
     this.highlightedParseableEntityList.clear ();
     for ( Entity current : entities )
     {
       this.highlightedParseableEntityList.add ( current );
     }
     highlightedParseableEntities ();
   }
 }
