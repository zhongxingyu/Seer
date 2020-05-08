 package ch.fhnw.cbip.compiler.scanner;
 
 import ch.fhnw.cbip.compiler.scanner.IToken;
 import ch.fhnw.cbip.compiler.scanner.enums.Terminal;
 
 public abstract class AbstractToken implements IToken {
   /**
    * Terminal of this token
    */
   private final Terminal terminal;
   
   /**
    * Source code line
    */
   private final int line;
 
   /**
    * Creates a token
    * @param terminal terminal of the token
    * @param line source code line of the token
    */
   public AbstractToken(Terminal terminal, int line) {
     this.terminal = terminal;
     this.line = line;
   }
   
   /**
   * @return the token's terminal smybol
    */
   protected Terminal getTerminal() {
     return terminal;
   }
   
   public int getLine() {
     return line;
   }
 
   @Override
   public String toString() {
     return terminal.toString();
   }
 }
