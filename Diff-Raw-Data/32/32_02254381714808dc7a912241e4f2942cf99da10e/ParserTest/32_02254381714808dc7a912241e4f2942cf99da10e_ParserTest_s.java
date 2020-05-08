 package de.unisiegen.gtitool.core.parser;
 
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.Symbol;
 import de.unisiegen.gtitool.core.parser.alphabet.AlphabetParseable;
 import de.unisiegen.gtitool.core.parser.symbol.SymbolParseable;
 
 
 /**
  * The test class of the parser.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 @SuppressWarnings (
 { "all" } )
 public class ParserTest
 {
 
   public static void main ( String [] arguments )
   {
     /*
      * Alphabet
      */
     AlphabetParseable alphabetParseable = new AlphabetParseable ();
     String alphabetText = "{a,b,c}";
     try
     {
       Alphabet alphabet = ( Alphabet ) alphabetParseable.newParser (
           alphabetText ).parse ();
       System.out.println ( alphabet );
     }
     catch ( Exception e )
     {
       e.printStackTrace ();
     }
     /*
      * Symbol
      */
     SymbolParseable symbolParseable = new SymbolParseable ();
    String symbolText = "{a,b,c}";
     try
     {
      Symbol symbol = ( Symbol ) alphabetParseable.newParser ( symbolText )
           .parse ();
       System.out.println ( symbol );
     }
     catch ( Exception e )
     {
       e.printStackTrace ();
     }
   }

 }
