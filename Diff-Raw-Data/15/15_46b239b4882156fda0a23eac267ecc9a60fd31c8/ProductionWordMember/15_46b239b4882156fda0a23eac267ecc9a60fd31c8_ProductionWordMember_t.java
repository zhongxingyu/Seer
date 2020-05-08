 package de.unisiegen.gtitool.core.entities;
 
 
 import java.io.Serializable;
 
 import de.unisiegen.gtitool.core.parser.ParserOffset;
 import de.unisiegen.gtitool.core.parser.style.PrettyPrintable;
 import de.unisiegen.gtitool.core.storage.Storable;
 
 
 /**
  * The {@link ProductionWordMember} entity.
  * 
  * @author Christian Fehler
  * @version $Id$
  */
 public interface ProductionWordMember extends Storable, PrettyPrintable,
     Cloneable, Serializable
 {
 
   /**
    * Returns the name of this {@link ProductionWordMember}.
    * 
    * @return The name of this {@link ProductionWordMember}.
    */
   public String getName ();
 
 
   /**
    * Returns the {@link ParserOffset}.
    * 
    * @return The {@link ParserOffset}.
    */
   public ParserOffset getParserOffset ();
 
 
   /**
    * Returns true if this {@link NonterminalSymbol} is a error
    * {@link NonterminalSymbol}, otherwise false.
    * 
    * @return True if this {@link NonterminalSymbol} is a error
    *         {@link NonterminalSymbol}, otherwise false.
    */
   public boolean isError ();
 
 
   /**
    * Sets the error value.
    * 
    * @param error The error value to set.
    */
   public void setError ( boolean error );
 
 
   /**
    * Sets the {@link ParserOffset}.
    * 
    * @param parserOffset The new {@link ParserOffset}.
    */
   public void setParserOffset ( ParserOffset parserOffset );
 }
