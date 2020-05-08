 package de.unisiegen.gtitool.core.parser;
 
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 import de.unisiegen.gtitool.core.entities.Symbol;
import de.unisiegen.gtitool.core.entities.Word;
 import de.unisiegen.gtitool.core.parser.alphabet.AlphabetParseable;
 import de.unisiegen.gtitool.core.parser.symbol.SymbolParseable;
import de.unisiegen.gtitool.core.parser.word.WordParseable;
 
 
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
    String symbolText = "a";
     try
     {
      Symbol symbol = ( Symbol ) symbolParseable.newParser ( symbolText )
           .parse ();
       System.out.println ( symbol );
     }
     catch ( Exception e )
     {
       e.printStackTrace ();
     }
    /*
     * Word
     */
    WordParseable wordParseable = new WordParseable ();
    String wordText = "abba";
    try
    {
      Word word = ( Word ) wordParseable.newParser ( wordText ).parse ();
      System.out.println ( word );
    }
    catch ( Exception e )
    {
      e.printStackTrace ();
    }
   }
 }
