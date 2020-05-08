 package cs444.parser.symbols.ast;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 
 public class MethodSymbol extends AModifiersOptSymbol{
 
     public MethodSymbol(String dclName, ANonTerminal from, TypeSymbol type, ANonTerminal body)
         throws IllegalModifierException, UnsupportedException {
 
         super("Method", dclName, getModifiersParent(from), type);
 
         if(body != null) children.addAll(body.children);
     }
 
     private static ANonTerminal getModifiersParent(ANonTerminal from) {
         return (ANonTerminal)from.firstOrDefault("MethodHeader");
     }
 
     @Override
     public void validate() throws UnsupportedException {
         ImplementationLevel implLvl = getImplementationLevel();
         if(isStatic() && implLvl == ImplementationLevel.FINAL)
             throw new UnsupportedException("A static method cannot be final");

        if(hasBody() && (isNative() || implLvl == ImplementationLevel.ABSTRACT))
        	throw new UnsupportedException("Abstract or Native method with a body");
        if(!hasBody() && !isNative() && implLvl != ImplementationLevel.ABSTRACT)
        	throw new UnsupportedException("Only Abstract or Native methods don't require a body");
 
         super.validate();
     }
 
     private boolean hasBody() {
         return children.size() > 0;
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
