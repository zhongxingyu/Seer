 package de.unisiegen.gtitool.ui.style;
 
 
 import java.util.ArrayList;
 
 import de.unisiegen.gtitool.core.entities.DefaultNonterminalSymbol;
 import de.unisiegen.gtitool.core.entities.DefaultProductionWord;
 import de.unisiegen.gtitool.core.entities.DefaultTerminalSymbol;
 import de.unisiegen.gtitool.core.entities.NonterminalSymbol;
 import de.unisiegen.gtitool.core.entities.NonterminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.ProductionWord;
 import de.unisiegen.gtitool.core.entities.ProductionWordMember;
 import de.unisiegen.gtitool.core.entities.TerminalSymbol;
 import de.unisiegen.gtitool.core.entities.TerminalSymbolSet;
 import de.unisiegen.gtitool.core.entities.listener.ProductionWordChangedListener;
 import de.unisiegen.gtitool.core.exceptions.nonterminalsymbol.NonterminalSymbolException;
 import de.unisiegen.gtitool.core.exceptions.terminalsymbol.TerminalSymbolException;
 import de.unisiegen.gtitool.core.parser.exceptions.ParserException;
 import de.unisiegen.gtitool.core.parser.exceptions.ScannerException;
 import de.unisiegen.gtitool.core.parser.productionword.ProductionWordParseable;
 import de.unisiegen.gtitool.core.parser.style.Style;
 import de.unisiegen.gtitool.ui.Messages;
 import de.unisiegen.gtitool.ui.style.listener.ParseableChangedListener;
 import de.unisiegen.gtitool.ui.style.parser.StyledParserPanel;
 
 
 /**
  * The styled {@link ProductionWord} panel class.
  * 
  * @author Christian Fehler
  * @version $Id: StyledProductionWordParserPanel.java 532 2008-02-04 23:54:55Z
  *          fehler $
  */
 public final class StyledProductionWordParserPanel extends StyledParserPanel
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = -2385625169180684832L;
 
 
   /**
    * Every parsed symbol in this set is a {@link NonterminalSymbol}.
    */
   private NonterminalSymbolSet nonterminalSymbolSet = null;
 
 
   /**
    * Every parsed symbol in this set is a {@link TerminalSymbol}.
    */
   private TerminalSymbolSet terminalSymbolSet = null;
 
 
   /**
    * Allocates a new {@link StyledProductionWordParserPanel}.
    */
   public StyledProductionWordParserPanel ()
   {
     super ( new ProductionWordParseable () );
     super.addParseableChangedListener ( new ParseableChangedListener ()
     {
 
       @SuppressWarnings ( "synthetic-access" )
       public void parseableChanged ( Object newObject )
       {
         fireProductionWordChanged ( ( ProductionWord ) newObject );
       }
     } );
   }
 
 
   /**
    * Adds the given {@link ProductionWordChangedListener}.
    * 
    * @param listener The {@link ProductionWordChangedListener}.
    */
   public final synchronized void addProductionWordChangedListener (
       ProductionWordChangedListener listener )
   {
     this.listenerList.add ( ProductionWordChangedListener.class, listener );
   }
 
 
   /**
    * Checks the given {@link ProductionWord}.
    * 
    * @param productionWord The {@link ProductionWord} to check.
    * @return The input {@link ProductionWord} with the right
    *         {@link NonterminalSymbol}s and {@link TerminalSymbol}s.
    */
   private final ProductionWord checkProductionWord (
       ProductionWord productionWord )
   {
     if ( this.nonterminalSymbolSet == null )
     {
       throw new RuntimeException ( "nonterminal symbol set is not set" ); //$NON-NLS-1$
     }
     if ( this.terminalSymbolSet == null )
     {
       throw new RuntimeException ( "terminal symbol set is not set" ); //$NON-NLS-1$
     }
 
     if ( productionWord != null )
     {
      ProductionWord newProductionWord = null;

       try
       {
        ArrayList < ProductionWordMember > memberList = new ArrayList < ProductionWordMember > ();

         ArrayList < ScannerException > exceptionList = new ArrayList < ScannerException > ();
         for ( ProductionWordMember current : productionWord )
         {
           // Nonterminal
           boolean foundNonterminal = false;
           nonterminalLoop : for ( NonterminalSymbol currentNonterminal : this.nonterminalSymbolSet )
           {
             if ( current.getName ().equals ( currentNonterminal.getName () ) )
             {
               foundNonterminal = true;
               NonterminalSymbol newNonterminalSymbol = new DefaultNonterminalSymbol (
                   current.getName () );
               newNonterminalSymbol
                   .setParserOffset ( current.getParserOffset () );
              memberList.add ( newNonterminalSymbol );
               break nonterminalLoop;
             }
           }
           // Terminal
           boolean foundTerminal = false;
           terminalLoop : for ( TerminalSymbol currentTerminal : this.terminalSymbolSet )
           {
             if ( current.getName ().equals ( currentTerminal.getName () ) )
             {
               foundTerminal = true;
               TerminalSymbol newTerminalSymbol = new DefaultTerminalSymbol (
                   current.getName () );
               newTerminalSymbol.setParserOffset ( current.getParserOffset () );
              memberList.add ( newTerminalSymbol );
               break terminalLoop;
             }
           }
 
           if ( ( !foundNonterminal ) && ( !foundTerminal ) )
           {
             exceptionList.add ( new ParserException ( current
                 .getParserOffset ().getStart (), current.getParserOffset ()
                 .getEnd (), Messages.getString (
                 "ProductionWord.SymbolNotFound", //$NON-NLS-1$
                 current.getName () ) ) );
           }
         }
 
         // Check for exceptions
         if ( exceptionList.size () > 0 )
         {
           setException ( exceptionList );
          return null;
         }

        newProductionWord = new DefaultProductionWord ( memberList );
        newProductionWord.setParserOffset ( productionWord.getParserOffset () );
       }
       catch ( NonterminalSymbolException exc )
       {
         exc.printStackTrace ();
         System.exit ( 1 );
       }
       catch ( TerminalSymbolException exc )
       {
         exc.printStackTrace ();
         System.exit ( 1 );
       }

       return newProductionWord;
     }
     return productionWord;
   }
 
 
   /**
    * Let the listeners know that the {@link ProductionWord} has changed.
    * 
    * @param newProductionWord The new {@link ProductionWord}.
    */
   private final void fireProductionWordChanged (
       ProductionWord newProductionWord )
   {
     ProductionWord checkedProductionWord = checkProductionWord ( newProductionWord );
     ProductionWordChangedListener [] listeners = this.listenerList
         .getListeners ( ProductionWordChangedListener.class );
     for ( int n = 0 ; n < listeners.length ; ++n )
     {
       listeners [ n ].productionWordChanged ( checkedProductionWord );
     }
   }
 
 
   /**
    * Returns the {@link ProductionWord} for the program text within the
    * document.
    * 
    * @return The {@link ProductionWord} for the program text.
    */
   public final ProductionWord getProductionWord ()
   {
     try
     {
       ProductionWord productionWord = ( ProductionWord ) getParsedObject ();
       return checkProductionWord ( productionWord );
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
   public final ProductionWord parse ()
   {
     ProductionWord productionWord = ( ProductionWord ) super.parse ();
     return checkProductionWord ( productionWord );
   }
 
 
   /**
    * Removes the given {@link ProductionWordChangedListener}.
    * 
    * @param listener The {@link ProductionWordChangedListener}.
    */
   public final synchronized void removeProductionWordChangedListener (
       ProductionWordChangedListener listener )
   {
     this.listenerList.remove ( ProductionWordChangedListener.class, listener );
   }
 
 
   /**
    * Sets the {@link NonterminalSymbolSet}. Every parsed symbol in this set is
    * a {@link NonterminalSymbol}.
    * 
    * @param nonterminalSymbolSet The {@link NonterminalSymbolSet} to set.
    */
   public final void setNonterminalSymbolSet (
       NonterminalSymbolSet nonterminalSymbolSet )
   {
     if ( this.terminalSymbolSet != null )
     {
       for ( TerminalSymbol currentTerminal : this.terminalSymbolSet )
       {
         for ( NonterminalSymbol currentNonterminal : nonterminalSymbolSet )
         {
           if ( currentTerminal.getName ().equals (
               currentNonterminal.getName () ) )
           {
             throw new RuntimeException (
                 "nonterminals and terminals are not disjunct: " + currentTerminal.getName () ); //$NON-NLS-1$
           }
         }
       }
     }
     this.nonterminalSymbolSet = nonterminalSymbolSet;
 
     // Set the overwritten style
     for ( NonterminalSymbol current : this.nonterminalSymbolSet )
     {
       addOverwrittenStyle ( current.getName (), Style.NONTERMINAL_SYMBOL );
     }
   }
 
 
   /**
    * Sets the {@link ProductionWord} of the document.
    * 
    * @param word The input {@link ProductionWord}.
    */
   public final void setProductionWord ( ProductionWord word )
   {
     getEditor ().setText ( word.toString () );
   }
 
 
   /**
    * Sets the {@link TerminalSymbolSet}. Every parsed symbol in this set is a
    * {@link TerminalSymbol}.
    * 
    * @param terminalSymbolSet The {@link TerminalSymbolSet} to set.
    */
   public final void setTerminalSymbolSet ( TerminalSymbolSet terminalSymbolSet )
   {
     if ( this.nonterminalSymbolSet != null )
     {
       for ( NonterminalSymbol currentNonterminal : this.nonterminalSymbolSet )
       {
         for ( TerminalSymbol currentTerminal : terminalSymbolSet )
         {
           if ( currentNonterminal.getName ().equals (
               currentTerminal.getName () ) )
           {
             throw new RuntimeException (
                 "nonterminals and terminals are not disjunct: " + currentNonterminal.getName () ); //$NON-NLS-1$
           }
         }
       }
     }
     this.terminalSymbolSet = terminalSymbolSet;
 
     // Set the overwritten style
     for ( TerminalSymbol current : this.terminalSymbolSet )
     {
       addOverwrittenStyle ( current.getName (), Style.TERMINAL_SYMBOL );
     }
   }
 }
