 package cs444.types;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.types.exceptions.ClashException;
 import cs444.types.exceptions.DuplicateDeclarationException;
 import cs444.types.exceptions.UndeclaredException;
 
 public class PkgClassInfo {
     private final Map<String, Map<String, PkgClassResolver>> nameSpaces = new HashMap<String, Map<String, PkgClassResolver>>();
     private final Map<String, PkgClassResolver> symbolMap = new HashMap<String, PkgClassResolver>();
 
     public static final PkgClassInfo instance = new PkgClassInfo();
 
     private PkgClassInfo(){
         addInitialSymbols();
     }
 
     public void addClassOrInterface(AInterfaceOrClassSymbol symbol) throws DuplicateDeclarationException, UndeclaredException, ClashException{
         PkgClassResolver resolver = PkgClassResolver.getResolver(symbol);
 
        if(nameSpaces.containsKey(resolver.fullName))throw new DuplicateDeclarationException(resolver.fullName, resolver.fullName);
 
         String pkg = resolver.pkg;
 
         Map<String, PkgClassResolver> pkgs = null;
 
         String [] innerPkgs = pkg.split("\\.");
         StringBuilder sb = new StringBuilder();
 
         for(int i = 0; i < innerPkgs.length; i++){
             String newNameSpace = sb.toString() + innerPkgs[i];
             pkgs = nameSpaces.get(newNameSpace);
 
             if(pkgs == null){
                 if(pkgClashWithType(sb.toString(), innerPkgs[i])){
                     throw new ClashException("package " + newNameSpace, "class " + newNameSpace);
                 }
 
                 pkgs = new HashMap<String, PkgClassResolver>();
                 nameSpaces.put(newNameSpace, pkgs);
             }
 
             sb.append(innerPkgs[i] + ".");
         }
 
         pkgs.put(resolver.name, resolver);
         symbolMap.put(resolver.fullName, resolver);
     }
 
     private boolean pkgClashWithType(String pkgNamePrefix, String pkgNameSuffix) {
         if (pkgNamePrefix.equals("")) return false;
 
         String outerPkgName = pkgNamePrefix.substring(0, pkgNamePrefix.length()-1);
         Map<String, PkgClassResolver> nameSpc = nameSpaces.get(outerPkgName);
         return (nameSpc != null && nameSpc.containsKey(pkgNameSuffix));
     }
 
     public PkgClassResolver getSymbol(String name){
         return symbolMap.get(name);
     }
 
     public Iterable<PkgClassResolver> getSymbols(){
         return symbolMap.values();
     }
 
     public Iterable<Entry<String, PkgClassResolver>> getNamespaceParts(String nameSpace){
         return nameSpaces.get(nameSpace).entrySet();
     }
 
     private void addInitialSymbols() {
         symbolMap.put("boolean", PkgClassResolver.getPrimativeResolver("boolean"));
         symbolMap.put("byte", PkgClassResolver.getPrimativeResolver("byte"));
         symbolMap.put("short", PkgClassResolver.getPrimativeResolver("short"));
         symbolMap.put("int", PkgClassResolver.getPrimativeResolver("int"));
         symbolMap.put("char", PkgClassResolver.getPrimativeResolver("char"));
         symbolMap.put("void", PkgClassResolver.getPrimativeResolver("void"));
     }
 
     // NOTE: this method is for tests only
     public void clear(){
         this.nameSpaces.clear();
         this.symbolMap.clear();
         addInitialSymbols();
     }
 }
