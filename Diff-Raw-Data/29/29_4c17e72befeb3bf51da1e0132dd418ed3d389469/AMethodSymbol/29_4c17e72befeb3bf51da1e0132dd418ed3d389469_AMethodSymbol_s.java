 package cs444.parser.symbols.ast;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public abstract class AMethodSymbol extends MethodOrConstructorSymbol{
 
     public AMethodSymbol(String nodeName, MethodHeader header, ANonTerminal from, ANonTerminal body, TypeSymbol type)
             throws IllegalModifierException, UnsupportedException {
         super(nodeName, header, from, body, type);
     }

 }
