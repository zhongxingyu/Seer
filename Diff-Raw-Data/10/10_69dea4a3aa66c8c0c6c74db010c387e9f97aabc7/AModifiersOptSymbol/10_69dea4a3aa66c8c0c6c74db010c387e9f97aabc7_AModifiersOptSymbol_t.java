 package cs444.parser.symbols.ast;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.Terminal;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public abstract class AModifiersOptSymbol extends ANonTerminal{
     private boolean hasPublic;
     private boolean hasProtected;
     private boolean hasPrivate;
 
     private boolean hasStatic;
     private boolean hasAbstract;
     private boolean hasFinal;
 
     private boolean hasNative;
 
     public static enum ProtectionLevel{PUBLIC, PROTECTED, PRIVATE, NOT_VALID};
     public static enum ImplementationLevel { ABSTRACT, FINAL, NORMAL };
 
     public final String dclName;
     public TypeSymbol type;
 
    protected AModifiersOptSymbol(String name, String dclName, ANonTerminal modifiersParent,
                                  TypeSymbol type)
             throws IllegalModifierException, UnsupportedException {
 
         super(name);
         this.dclName = dclName;
         this.type = type;
 
         List<Terminal> modifiers = new LinkedList<Terminal>();
 
         ANonTerminal modiferChild = (ANonTerminal)modifiersParent.firstOrDefault("Modifiers");
 
         if(modiferChild != null){
             for(ISymbol child : modiferChild.getChildren()) modifiers.add((Terminal)child);
         }
 
         for(Terminal modifer : modifiers) giveModifier(modifer);
 
         //Joos only
         if(!hasStatic && hasNative) throw new UnsupportedException("native not abstract");
         if(getImplementationLevel() == ImplementationLevel.ABSTRACT){
             if(hasFinal)throw new IllegalModifierException("abstract", "final");
             if(hasStatic)throw new IllegalModifierException("abstract", "static");
             if(hasNative)throw new IllegalModifierException("abstract", "native");
         }
         validate();
 
     }
 
     public ProtectionLevel getProtectionLevel(){
         if(hasPublic) return ProtectionLevel.PUBLIC;
         if(hasProtected) return ProtectionLevel.PROTECTED;
         if(hasPrivate) return ProtectionLevel.PRIVATE;
         return defaultProtectionLevel();
     }
 
     public boolean isStatic(){
         return hasStatic;
     }
 
     public ImplementationLevel getImplementationLevel(){
         if(hasAbstract) return ImplementationLevel.ABSTRACT;
         if(hasFinal) return ImplementationLevel.FINAL;
         return defaultImplementationLevel();
     }
 
     public boolean isNative(){
         return hasNative;
     }
 
     public void checkVisiblilty(String tokenName) throws IllegalModifierException{
         if(hasPublic){
             throw new IllegalModifierException(tokenName, "public");
         }else if(hasPrivate){
             throw new IllegalModifierException(tokenName, "private");
         }else if(hasProtected){
             throw new IllegalModifierException(tokenName, "private");
         }
     }
 
     private void giveModifier(Terminal t) throws IllegalModifierException{
         String name = t.getName();
         if(name.equals("PRIVATE")){
             if(hasAbstract) throw new IllegalModifierException("private", "abstract");
             checkVisiblilty("private");
             hasPrivate = true;
             throw new IllegalModifierException("private");
         }else if(name.equals("PUBLIC")){
             checkVisiblilty("public");
             hasPublic = true;
         }else if(name.equals("PROTECTED")){
             checkVisiblilty("protected");
             hasProtected = true;
         }else if(name.equals("STATIC")){
             if(hasStatic)throw new IllegalModifierException("static", "static");
             if(hasAbstract)throw new IllegalModifierException("static", "abstract");
             hasStatic = true;
         }else if(name.equals("FINAL")){
             if(hasFinal)throw new IllegalModifierException("final", "final");
             if(hasAbstract)throw new IllegalModifierException("final", "abstract");
             hasFinal = true;
         }else if(name.equals("ABSTRACT")){
             //This is checked after because it's possible to imply with interfaces
             if(hasAbstract)throw new IllegalModifierException("abstract", "abstract");
             hasAbstract = true;
         }else if(name.equals("NATIVE")){
             if(hasNative) throw new IllegalModifierException("native", "native");
             if(hasAbstract)throw new IllegalModifierException("native", "abstract");
             hasNative = true;
         }else{
             throw new IllegalModifierException(name);
         }
     }
 
     public boolean empty(){
         return false;
     }
 
     public abstract void validate() throws UnsupportedException;
     public abstract ProtectionLevel defaultProtectionLevel();
     public abstract ImplementationLevel defaultImplementationLevel();
 }
