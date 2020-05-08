 package cs444.types;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import cs444.CompilerException;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.AMethodSymbol;
 import cs444.parser.symbols.ast.AModifiersOptSymbol;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.ConstructorSymbol;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.parser.symbols.ast.MethodOrConstructorSymbol;
 import cs444.parser.symbols.ast.TypeSymbol;
 import cs444.types.exceptions.ImplicitStaticConversionException;
 import cs444.types.exceptions.UndeclaredException;
 
 public abstract class APkgClassResolver {
 
     public final String name;
     public final String fullName;
     public final String pkg;
     protected boolean isBuilt = false;
     protected final boolean isFinal;
 
     protected static final String DEFAULT_PKG = "!default";
     protected static final String LANG = "java.lang";
     protected static final String OBJECT = LANG + ".Object";
 
     protected final Set<String> assignableTo = new HashSet<String>();
     protected final Map<String, PkgClassResolver> namedMap = new HashMap<String, PkgClassResolver>();
 
     protected final Map<String, DclSymbol> fieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> sfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> hfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> hsfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, AMethodSymbol> methodMap = new HashMap<String, AMethodSymbol>();
     protected final Map<String, AMethodSymbol> smethodMap = new HashMap<String, AMethodSymbol>();
     protected final Map<String, ConstructorSymbol> constructors = new HashMap<String, ConstructorSymbol>();
 
     private final Map<DclSymbol, Integer> order = new HashMap<DclSymbol, Integer>();
     private final Map<Integer, DclSymbol> revorder = new HashMap<Integer, DclSymbol>();
     protected final Set<DclSymbol> addAll = new HashSet<DclSymbol>();
     private int onField = 0;
 
     public static enum Castable { UP_CAST, DOWN_CAST, NOT_CASTABLE };
 
     protected APkgClassResolver(String name, String pkg, boolean isFinal){
         this.name = name;
         this.pkg = pkg;
         if(pkg == null) fullName = name;
         else fullName = pkg + "." + name;
         this.isFinal = isFinal;
         assignableTo.add(fullName);
         assignableTo.add(OBJECT);
 
         Set<String> alsoAssignsTo = JoosNonTerminal.defaultAssignables.get(name);
         if(alsoAssignsTo != null) assignableTo.addAll(alsoAssignsTo);
     }
 
     protected void addTo(DclSymbol add){
         order.put(add, onField);
         revorder.put(onField, add);
         onField++;
     }
 
     protected static String generateUniqueName(String name, Iterable<String> types) {
         StringBuilder sb = new StringBuilder(name + "-");
 
         for (String type : types) sb.append(type + "*");
 
         return sb.toString();
     }
 
     protected static String generateUniqueName(MethodOrConstructorSymbol methodSymbol, String name) throws UndeclaredException {
         List<String> types = new LinkedList<String>();
         APkgClassResolver resolver = methodSymbol.resolver;
         for(DclSymbol param : methodSymbol.params){
             String type = resolver.getClass(param.type.value, true).fullName;
             if(param.getType().isArray) type = ArrayPkgClassResolver.getArrayName(type);
             types.add(type);
         }
         return generateUniqueName(name, types);
     }
 
     public abstract APkgClassResolver getClass(String name, boolean die) throws UndeclaredException;
 
     protected abstract void build(Set<PkgClassResolver> visited, boolean mustBeInterface, boolean mustBeClass) throws CompilerException;
 
     public void build() throws CompilerException {
         build(new HashSet<PkgClassResolver>(), false, false);
     }
 
     private DclSymbol getDcl(String name, boolean isStatic, APkgClassResolver pkgClass, boolean allowClass) throws UndeclaredException, ImplicitStaticConversionException {
 
         Map<String, DclSymbol> getFrom = isStatic ? sfieldMap : fieldMap;
         Map<String, DclSymbol> notFrom = isStatic ? fieldMap : sfieldMap;
 
         DclSymbol retVal = getFrom.get(name);
 
         if(retVal == null){
             if(notFrom.containsKey(name)) throw new ImplicitStaticConversionException(name);
             APkgClassResolver klass = allowClass ? getClass(name, false) : null;
             return (klass == null)? null : DclSymbol.getClassSymbol(name, klass);
         }
 
         verifyCanRead(retVal, pkgClass);
 
         return retVal;
     }
 
     public List<DclSymbol> findDcl(String name, boolean isStatic, APkgClassResolver pkgClass, boolean allowClass) throws UndeclaredException, ImplicitStaticConversionException {
         String [] nameParts = name.split("\\.");
 
         DclSymbol retVal;
         if(nameParts.length == 1){
             retVal = getDcl(name, isStatic, pkgClass, allowClass);
             if(retVal == null) throw new UndeclaredException(name, fullName);
 
             return  Arrays.asList(new DclSymbol []{ retVal });
         }
 
         DclSymbol dcl = getDcl(nameParts[0], isStatic, pkgClass, allowClass);
         List<DclSymbol> dclList = new LinkedList<DclSymbol>();
 
         APkgClassResolver pkgResolver = null;
 
         int i = 1;
 
         if(dcl != null){
             dclList.add(dcl);
             pkgResolver = getClass(dcl.type.value, true);
         }else{
             StringBuilder sb = new StringBuilder(nameParts[0]);
             pkgResolver = getClass(nameParts[0], false);
 
             //At least one must be a field
             int maxSearch = allowClass ? nameParts.length : nameParts.length - 1;
             for(; pkgResolver == null && i < maxSearch; i++){
                 sb.append("." + nameParts[i]);
                 pkgResolver = getClass(sb.toString(), false);
             }
             if(pkgResolver != null && i != nameParts.length) dcl = pkgResolver.getDcl(nameParts[i], true, this, false);
             else if(pkgResolver != null) dcl = DclSymbol.getClassSymbol(pkgResolver.fullName, pkgResolver);
             i++;
         }
 
         if(pkgResolver == null) throw new UndeclaredException(name, fullName);
         dclList.add(dcl);
 
         for(; i < nameParts.length; i++){
             if(dcl.type.isArray) pkgResolver = pkgResolver.getArrayVersion();
             dcl = pkgResolver.getDcl(nameParts[i], dcl.type.isClass, pkgClass, false);
             if(dcl == null) throw new UndeclaredException(name, fullName);
             dclList.add(dcl);
             pkgResolver = pkgResolver.getClass(dcl.type.value, true);
         }
 
         return dclList;
     }
 
     public List<DclSymbol> findDcl(String name, boolean isStatic, boolean allowClass) throws UndeclaredException, ImplicitStaticConversionException {
         return findDcl(name, isStatic, this, allowClass);
     }
 
     public AMethodSymbol findMethod(String name, boolean isStatic, Iterable<String> paramTypes, APkgClassResolver pkgClass) throws UndeclaredException {
         final Map<String, AMethodSymbol> getFrom = isStatic ? smethodMap : methodMap;
         String uniqueName = generateUniqueName(name, paramTypes);
         AMethodSymbol retVal = getFrom.get(uniqueName);
 
         if(retVal == null) throw new UndeclaredException(uniqueName, fullName);
 
         verifyCanRead(retVal, pkgClass);
 
         return retVal;
     }
 
     private void verifyCanRead(AModifiersOptSymbol retVal, APkgClassResolver pkgClass) throws UndeclaredException{
         if(pkgClass == this) return;
         if(retVal.getProtectionLevel() == ProtectionLevel.PRIVATE) throw new UndeclaredException(name, fullName);
         if(retVal.getProtectionLevel() == ProtectionLevel.PUBLIC) return;
        if(assignableTo.contains(pkgClass.fullName)) return;
         final APkgClassResolver dclResolver = retVal.dclInResolver;
         if(pkgClass.pkg.equals(dclResolver.pkg)) return;
         if(pkgClass.assignableTo.contains(dclResolver.fullName) && pkgClass.pkg.startsWith(dclResolver.pkg)) return;
         if(pkgClass.assignableTo.contains(dclResolver.fullName) && retVal.isStatic()) return;
         throw new UndeclaredException(name, fullName);
     }
 
     public abstract APkgClassResolver getSuper() throws UndeclaredException;
 
     public String getSuperName()throws UndeclaredException{
         return getSuper().fullName;
     }
 
     public abstract APkgClassResolver accessor() throws CompilerException;
 
     public APkgClassResolver getArrayVersion(){
         APkgClassResolver resolver = PkgClassInfo.instance.getSymbol(ArrayPkgClassResolver.getArrayName(fullName));
         if(resolver != null) return resolver;
         resolver = new ArrayPkgClassResolver(this);
         PkgClassInfo.instance.putSymbol(resolver);
         return resolver;
     }
 
     protected abstract boolean isAbstract();
     protected abstract boolean isPrimative();
 
     public Castable getCastablility(APkgClassResolver other){
         if(other == this) return Castable.UP_CAST;
 
         //everyone can return null, but
         if(other == TypeSymbol.getPrimative(JoosNonTerminal.NULL).getTypeDclNode() && !isPrimative())
             return Castable.UP_CAST;
 
         Set<String> special = JoosNonTerminal.specialAssignables.get(fullName);
         if(special != null && special.contains(other.fullName))
             return Castable.DOWN_CAST;
 
         if(assignableTo.contains(other.fullName)) return Castable.DOWN_CAST;
         if(other.assignableTo.contains(fullName)) return Castable.UP_CAST;
         return Castable.NOT_CASTABLE;
     }
 
     public ConstructorSymbol getConstructor(List<String> types, APkgClassResolver resolver) throws UndeclaredException {
         String name = generateUniqueName("this", types);
         ConstructorSymbol cs = constructors.get(name);
         if(cs == null) throw new UndeclaredException(name, fullName);
         verifyCanRead(cs, resolver);
         return cs;
     }
 
     protected abstract Iterable<DclSymbol> getDcls();
 
     public void checkFields() throws CompilerException{
         FieldLinker myLinker = new FieldLinker(fullName);
         for(DclSymbol dcl : getDcls()){
             dcl.accept(myLinker);
         }
     }
 
     public abstract APkgClassResolver findClass(String name) throws UndeclaredException;
 
     public abstract void linkLocalNamesToDcl() throws CompilerException;
 
     public List<DclSymbol> findDclOn(String lookupFirst, boolean empty, int on) throws UndeclaredException, ImplicitStaticConversionException {
         DclSymbol now = revorder.get(on);
         List<DclSymbol> list = findDcl(lookupFirst, now.isStatic(), empty);
         DclSymbol dcl = list.get(list.size() - 1);
         if(order.get(dcl) >= on) throw new UndeclaredException(dcl.dclName, fullName);
         if(now.isStatic() && !dcl.isStatic()) throw new UndeclaredException(dcl.dclName, fullName);
         return list;
     }
 }
