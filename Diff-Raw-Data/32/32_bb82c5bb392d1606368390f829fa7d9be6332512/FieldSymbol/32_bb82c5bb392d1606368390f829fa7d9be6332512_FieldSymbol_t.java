 package cs444.parser.symbols.ast;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 
 public class FieldSymbol extends AModifiersOptSymbol{
 
     public FieldSymbol(String dclName, ANonTerminal from, TypeSymbol type, ANonTerminal initVal)
             throws IllegalModifierException, UnsupportedException {
 
         super("Field", dclName, from, type);
         if(initVal != null) children.addAll(initVal.children);
     }
 
     @Override
     public void validate() throws UnsupportedException {
         ImplementationLevel lvl = getImplementationLevel();
         if(lvl == ImplementationLevel.ABSTRACT )throw new UnsupportedException("abstract fields");
 
         if(lvl == ImplementationLevel.FINAL ) throw new UnsupportedException("final fields");
 
         if(type.equals("void"))throw new UnsupportedException("void fields");

        super.validate();
     }
 
     @Override
     public ProtectionLevel defaultProtectionLevel() {
         // We don't support package private members
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
