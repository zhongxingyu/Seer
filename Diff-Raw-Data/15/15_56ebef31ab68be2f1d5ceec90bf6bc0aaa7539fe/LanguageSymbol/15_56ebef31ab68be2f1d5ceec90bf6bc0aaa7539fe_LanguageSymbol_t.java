 package de.unisiegen.tpml.core.languages;
 
 import java_cup.runtime.Symbol;
 
 /**
 * Instances of this class represent symbols that will be returned from the
 * {@link de.unisiegen.tpml.core.languages.LanguageScanner}s.
  *
  * @author Benedikt Meurer
  * @version $Rev$
 * 
 * @see java_cup.runtime.Symbol
 * @see de.unisiegen.tpml.core.languages.LanguageScanner
  */
 public final class LanguageSymbol extends Symbol {
   //
   // Attributes
   //
   
   /**
    * The name of the symbol, i.e. <code>"START"</code> for the start symbol.
    * 
    * @see #getName()
    */
   private String name;
   
   
   
   //
   // Constructors
   //
   
   /**
    * Allocates a new <code>LanguageSymbol</code> with the specified
    * <code>name</code> and the specified symbol <code>id</code>.
    * 
    * The <code>left</code> and <code>right</code> values specify
    * character offsets in the source stream. They may be <code>-1</code>
    * if the offsets aren't available.
    * 
    * The <code>value</code> is the value associated with this symbol,
    * for example, the value assigned to a symbol by the lexer. May be
    * <code>null</code> if no value is available.
    * 
    * @param name the name of the symbol.
    * @param id the unique identifier of the symbol.
    * @param left the left position in the source, or <code>-1</code>.
    * @param right the right position in the source, or <code>-1</code>.
    * @param value the value assigned to the symbol, or <code>null</code>.
    * 
    * @see #getLeft()
    * @see #getRight()
    * @see #getName()
    */
   LanguageSymbol(String name, int id, int left, int right, Object value) {
     super(id, left, right, value);
     this.name = name;
   }
   
   /**
    * Special constructor that allocates a new <code>LanguageSymbol</code>
    * with the given <code>name</code> and symbol <code>id</code>, and sets
    * the parser private state variable to the specified <code>state</code>.
    * 
    * This method should only be used by the <code>startSymbol()</code>
    * method of the {@link LanguageSymbolFactory} class.
    * 
    * @param name the name of the symbol.
    * @param id the unique identifier of the symbol.
    * @param state the initial parse state.
    * 
    * @see LanguageSymbolFactory#startSymbol(String, int, int)
    * @see Symbol#parse_state
    */
   LanguageSymbol(String name, int id, int state) {
     super(id, state);
     this.name = name;
   }
   
 
   
   //
   // Accessors
   //
 
   /**
    * Returns the unique identifier of this symbol.
    * 
    * @return the unique identifier of this symbol.
    */
   public int getId() {
     return this.sym;
   }
   
   /**
    * Returns the left position of the symbol in the source stream
    * or <code>-1</code> if the left position is not known.
    *  
    * @return the left position of the symbol, or <code>-1</code>.
    * 
    * @see #getRight()
    */
   public int getLeft() {
     return this.left;
   }
   
   /**
    * Returns the right position of the symbol in the source stream
    * or <code>-1</code> if the right position is not known.
    * 
    * @return the right position of the symbol, or <code>-1</code>.
    * 
    * @see #getLeft()
    */
   public int getRight() {
     return this.right;
   }
   
   /**
    * Returns the name of the symbol, i.e. <code>"START"</code> in case of
    * the start symbol of the parser.
    * 
    * @return the name of the symbol.
    */
   public String getName() {
     return this.name;
   }
   
   
   
   //
   // Overwritten methods
   //
   
   /**
    * Returns the string representation for this <code>LanguageSymbol</code>,
    * which is simply the name of the symbol.
    *
    * @return the string representation for this symbol.
    * 
    * @see Object#toString()
    */
   @Override
   public String toString() {
     return this.name;
   }
 }
