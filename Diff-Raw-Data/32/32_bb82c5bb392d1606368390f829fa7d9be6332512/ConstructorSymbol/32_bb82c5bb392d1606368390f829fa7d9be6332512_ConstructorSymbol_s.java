 package cs444.parser.symbols.ast;
 
 import java.util.List;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class ConstructorSymbol extends AModifiersOptSymbol{
     //TODO eventually make a class for arguments
     public final Iterable<ISymbol> args;
 
     public ConstructorSymbol(String dclName, ANonTerminal from, List<ISymbol> children, Iterable<ISymbol> args)
             throws IllegalModifierException, UnsupportedException {
 
         super("ConstructorDeclaration", dclName, from, null);
 
         this.args = args;
         this.children.addAll(children);
     }
 
     @Override
     public void validate() throws UnsupportedException {
         if(getImplementationLevel() != ImplementationLevel.NORMAL) throw new UnsupportedException("Unimlemented and final constructors");
        if(getProtectionLevel() == ProtectionLevel.NOT_VALID) throw new UnsupportedException("Package private protection for constructors");
         if(isNative())throw new UnsupportedException("native constructors");
     }
 
     @Override
     public ProtectionLevel defaultProtectionLevel() {
         return ProtectionLevel.NOT_VALID;
     }
 
     @Override
     public ImplementationLevel defaultImplementationLevel() {
         return ImplementationLevel.NORMAL;
     }
 
     @Override
     public boolean isCollapsable() {
         return false;
     }
 
 }
