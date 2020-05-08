 package cs444.types;
 
 import java.util.*;
 
 import cs444.CompilerException;
 import cs444.codegen.*;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.*;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.cleanup.factories.FieldCleaner;
 import cs444.parser.symbols.ast.cleanup.factories.FieldFlattener;
 import cs444.parser.symbols.ast.cleanup.factories.LookupLinkCleanFacory;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.types.exceptions.StaticToNonStaticConversion;
 import cs444.types.exceptions.UndeclaredException;
 
 public abstract class APkgClassResolver {
     public final String name;
     public final String fullName;
     public final String pkg;
     protected boolean isBuilt = false;
     protected final boolean isFinal;
     protected APkgClassResolver superClass;
     protected final List<PkgClassResolver> implInterfs = new LinkedList<PkgClassResolver>();
 
     public static final String DEFAULT_PKG = "?default?";
 
     private static final ASTSymbolFactory [] CLEANERS =
         { new LookupLinkCleanFacory(), new FieldFlattener(), new FieldCleaner() };
 
     protected final Set<String> assignableTo = new HashSet<String>();
     protected final Map<String, PkgClassResolver> namedMap = new HashMap<String, PkgClassResolver>();
 
     protected final Map<String, DclSymbol> fieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> sfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> hfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, DclSymbol> hsfieldMap = new HashMap<String, DclSymbol>();
     protected final Map<String, AMethodSymbol> methodMap = new HashMap<String, AMethodSymbol>();
     protected final Map<String, AMethodSymbol> smethodMap = new HashMap<String, AMethodSymbol>();
     protected final Map<String, AMethodSymbol> hmethodMap = new HashMap<String, AMethodSymbol>();
     protected final Map<String, AMethodSymbol> hsmethodMap = new HashMap<String, AMethodSymbol>();
 
     protected final Map<String, ConstructorSymbol> constructors = new HashMap<String, ConstructorSymbol>();
 
     private final Map<DclSymbol, Integer> order = new HashMap<DclSymbol, Integer>();
     private final Map<Integer, DclSymbol> revorder = new HashMap<Integer, DclSymbol>();
     private int onField = 0;
 
     public static enum Castable { UP_CAST, DOWN_CAST, NOT_CASTABLE };
 
     protected APkgClassResolver(final String name, final String pkg, final boolean isFinal){
 
         this.name = name;
         this.pkg = pkg;
         if(pkg == null) fullName = name;
         else fullName = pkg + "." + name;
         this.isFinal = isFinal;
         assignableTo.add(fullName);
         assignableTo.add(JoosNonTerminal.OBJECT);
 
         final Set<String> alsoAssignsTo = JoosNonTerminal.defaultAssignables.get(name);
         if(alsoAssignsTo != null) assignableTo.addAll(alsoAssignsTo);
     }
 
 
     protected void addTo(final DclSymbol add){
         order.put(add, onField);
         revorder.put(onField, add);
         onField++;
     }
 
     public static String generateUniqueName(final String name, final Iterable<String> types) {
         final StringBuilder sb = new StringBuilder(name + "$");
 
         for (final String type : types) sb.append(type + "@");
 
         return sb.toString();
     }
 
 
     public static String generateUniqueName(final MethodOrConstructorSymbol methodSymbol, final String name) throws UndeclaredException {
 
         if(methodSymbol.isNative()){
             return methodSymbol.dclName;
         }
 
         final List<String> types = new LinkedList<String>();
         final APkgClassResolver resolver = methodSymbol.resolver;
         for(final DclSymbol param : methodSymbol.params){
             String type = resolver.getClass(param.type.value, true).fullName;
             if(param.getType().isArray) type = ArrayPkgClassResolver.getArrayName(type);
             types.add(type);
         }
         return generateUniqueName(name, types);
     }
 
     public static String generateFullId(final MethodOrConstructorSymbol methodSymbol){
         final String name = methodSymbol instanceof ConstructorSymbol ? "this" : methodSymbol.dclName;
         String value = null;
         try{
            value = methodSymbol.dclInResolver.fullName + "." + generateUniqueName(methodSymbol, name);
         }catch(final UndeclaredException e){ /*Should never happen based on where it is called from*/}
         return value;
     }
 
     public String generateSIT(){
        return fullName + "@SIT";
     }
 
     public String generateSubtypeIT() {
        return fullName + "@Subtype";
     }
 
     public static String getUniqueNameFor(final DclSymbol fieldDcl) {
         return  fieldDcl.dclInResolver.fullName + "_field_" + fieldDcl.dclName;
     }
 
     public abstract APkgClassResolver getClass(String name, boolean die) throws UndeclaredException;
 
     protected abstract void build(Set<PkgClassResolver> visited, boolean mustBeInterface, boolean mustBeClass) throws CompilerException;
 
     public void build() throws CompilerException {
         build(new HashSet<PkgClassResolver>(), false, false);
     }
 
     private DclSymbol getDcl(final String name, final boolean isStatic, final APkgClassResolver pkgClass,
             final boolean allowClass, final boolean fromSuper) throws UndeclaredException, StaticToNonStaticConversion {
 
         Map<String, DclSymbol> getFrom = isStatic ? sfieldMap : fieldMap;
         Map<String, DclSymbol> notFrom = isStatic ? fieldMap : sfieldMap;
 
         DclSymbol retVal = getFrom.get(name);
         if(null == retVal && !isStatic) retVal = notFrom.get(name);
 
         if(retVal == null){
             final APkgClassResolver klass = allowClass ? getClass(name, false) : null;
             return (klass == null)? null : DclSymbol.getClassSymbol(name, klass);
         }
 
         //This is not an else because if I allow implicit static conversion later it may no longer be null
         if(retVal.dclInResolver == this && fromSuper){
             getFrom = isStatic ? hsfieldMap : hfieldMap;
             notFrom = isStatic ? hfieldMap : hsfieldMap;
             retVal = getFrom.get(name);
             if(null == retVal && !isStatic) retVal = notFrom.get(name);
         }
 
         verifyCanRead(retVal, pkgClass);
 
         return retVal;
     }
 
     public List<DclSymbol> findDcl(final String name, final boolean isStatic, final APkgClassResolver pkgClass,
             final boolean allowClass, final boolean fromSuper) throws UndeclaredException, StaticToNonStaticConversion {
 
         final String [] nameParts = name.split("\\.");
 
         DclSymbol retVal;
         if(nameParts.length == 1){
             retVal = getDcl(name, isStatic, pkgClass, allowClass, fromSuper);
             if(retVal == null) throw new UndeclaredException(name, fullName);
             return  Arrays.asList(new DclSymbol []{ retVal });
         }
 
         DclSymbol dcl = getDcl(nameParts[0], isStatic, pkgClass, allowClass, fromSuper);
         final List<DclSymbol> dclList = new LinkedList<DclSymbol>();
 
         APkgClassResolver pkgResolver = null;
 
         int i = 1;
 
         if(dcl != null){
             pkgResolver = getClass(dcl.type.value, true);
         }else{
             final StringBuilder sb = new StringBuilder(nameParts[0]);
             pkgResolver = getClass(nameParts[0], false);
 
             //At least one must be a field
             final int maxSearch = allowClass ? nameParts.length : nameParts.length - 1;
             for(; pkgResolver == null && i < maxSearch; i++){
                 sb.append("." + nameParts[i]);
                 pkgResolver = getClass(sb.toString(), false);
             }
 
             dclList.add(getDcl(sb.toString(), isStatic, pkgClass, allowClass, false));
 
             if(pkgResolver != null && i != nameParts.length) dcl = pkgResolver.getDcl(nameParts[i], true, this, false, false);
             else if(pkgResolver != null) dcl = DclSymbol.getClassSymbol(pkgResolver.fullName, pkgResolver);
             i++;
         }
 
         if(pkgResolver == null) throw new UndeclaredException(name, fullName);
         dclList.add(dcl);
 
         for(; i < nameParts.length; i++){
             if(dcl.type.isArray) pkgResolver = pkgResolver.getArrayVersion();
             dcl = pkgResolver.getDcl(nameParts[i], dcl.type.isClass, pkgClass, false, false);
             if(dcl == null) throw new UndeclaredException(name, fullName);
             dclList.add(dcl);
             pkgResolver = pkgResolver.getClass(dcl.type.value, true);
         }
 
         return dclList;
     }
 
     public List<DclSymbol> findDcl(final String name, final boolean isStatic, final boolean allowClass, final boolean fromSuper) throws UndeclaredException, StaticToNonStaticConversion {
         return findDcl(name, isStatic, this, allowClass, fromSuper);
     }
 
     public AMethodSymbol findMethod(final String name, final boolean isStatic, final List<String> paramTypes,
             final APkgClassResolver pkgClass, final boolean fromSuper)  throws UndeclaredException {
 
         final String uniqueName = generateUniqueName(name, paramTypes);
         final AMethodSymbol retVal = safeFindMethod(name, isStatic, paramTypes, fromSuper);
         if(retVal == null) throw new UndeclaredException(uniqueName, fromSuper ? getSuper().fullName : fullName);
         verifyCanRead(retVal, pkgClass);
         return retVal;
     }
 
     private AMethodSymbol safeFindHelper(final String name, final String uniqueName, final Map<String, AMethodSymbol> from,
             final Map<String, AMethodSymbol> notFrom, final List<String> paramTypes){
 
         AMethodSymbol retVal = from.get(uniqueName);
         if(null == retVal && notFrom != null) retVal = notFrom.get(uniqueName);
         //NOTE if I change the native name to include the params then I don't need this
         if(retVal == null){
             retVal = from.get(name);
             if(retVal != null){
                 if(paramTypes.size() != retVal.params.size()) return null;
                 for(int i = 0; i < paramTypes.size(); i++){
                     if(!paramTypes.get(i).equals(retVal.params.get(i).getType().value)) return null;
                 }
             }
         }
         return retVal;
     }
 
     public AMethodSymbol safeFindMethod(final String name, final boolean isStatic, final List<String> paramTypes, final boolean fromSuper){
         final String uniqueName = generateUniqueName(name, paramTypes);
         Map<String, AMethodSymbol> from = isStatic ? smethodMap : methodMap;
         AMethodSymbol retVal = safeFindHelper(name, uniqueName, from, isStatic ? null : smethodMap, paramTypes);
 
         if(retVal != null && retVal.dclInResolver == this && fromSuper){
             from = isStatic ? hsmethodMap : hmethodMap;
             retVal = safeFindHelper(name, uniqueName, from, isStatic ? null : hsmethodMap, paramTypes);
         }
 
         return retVal;
     }
 
     private void verifyCanRead(final AModifiersOptSymbol retVal, final APkgClassResolver pkgClass) throws UndeclaredException{
         final ProtectionLevel protection = retVal.getProtectionLevel();
         if(pkgClass == this || protection == ProtectionLevel.PUBLIC) return;
         if(protection == ProtectionLevel.PRIVATE)throw new UndeclaredException(retVal.dclName, fullName);
 
         final APkgClassResolver dclResolver = retVal.dclInResolver;
         //TODO make a way to see if it should allow same pkg to use it in JoosNonTerminal
         if(pkgClass.pkg.equals(dclResolver.pkg)) return;
 
         if(protection == ProtectionLevel.PROTECTED && pkgClass.assignableTo.contains(dclResolver.fullName)){
             if(assignableTo.contains(pkgClass.fullName))return;
             if(retVal.isStatic()) return;
         }
 
         throw new UndeclaredException(retVal.dclName, fullName);
     }
 
     public APkgClassResolver getSuper() {
         return superClass;
     }
 
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
 
     public Castable getCastablility(final APkgClassResolver other){
         if(other == this) return Castable.UP_CAST;
 
         //everyone can return null, but
         if(other == TypeSymbol.getPrimative(JoosNonTerminal.NULL).getTypeDclNode() && !isPrimative())
             return Castable.UP_CAST;
 
         Set<String> special = JoosNonTerminal.specialAssignables.get(fullName);
         if(special != null && special.contains(other.fullName)) return Castable.DOWN_CAST;
 
         special = JoosNonTerminal.specialAssignables.get(other.fullName);
         if(special != null && special.contains(fullName)) return Castable.UP_CAST;
 
         if(assignableTo.contains(other.fullName)) return Castable.DOWN_CAST;
         if(other.assignableTo.contains(fullName)) return Castable.UP_CAST;
         return Castable.NOT_CASTABLE;
     }
 
     public ConstructorSymbol getConstructor(final List<String> types, final APkgClassResolver resolver) throws UndeclaredException {
         final String name = generateUniqueName("this", types);
         final ConstructorSymbol cs = constructors.get(name);
         if(cs == null){
             throw new UndeclaredException(name, fullName);
         }
         verifyCanRead(cs, resolver);
         return cs;
     }
 
     protected abstract Iterable<DclSymbol> getDcls();
 
     public abstract void checkFields(final Collection<Platform<?, ?>> platform) throws CompilerException;
 
     public abstract APkgClassResolver findClass(String name) throws UndeclaredException;
 
     public abstract void linkLocalNamesToDcl(final Collection<Platform<?, ?>> platforms) throws CompilerException;
 
     public abstract void analyzeReachability() throws CompilerException;
 
     public abstract void generateCode(CodeGenVisitor<?, ?> visitor);
 
     public abstract boolean shouldGenCode();
 
     public abstract void reduceToConstantExprs() throws CompilerException;
 
     public abstract void computeFieldOffsets(Platform<?, ?> platform);
     public abstract long getRefStackSize(final SizeHelper<?, ?> sizeHelper);
     public abstract long getRealSize(final SizeHelper<?, ?> sizeHelper);
     public abstract long getStackSize(final Platform<?, ?> platform);
     public abstract Iterable<DclSymbol> getUninheritedStaticFields();
 
     public abstract Iterable<DclSymbol> getUninheritedNonStaticFields();
 
     public void addToSelectorIndexedTable(final SelectorIndexedTable<?, ?> sit) {
         final String classSITLbl = this.generateSIT();
 
         if(!this.isAbstract()){
             sit.addClass(classSITLbl);
         }
 
         for (final AMethodSymbol method : methodMap.values()) {
             if(method.isStatic()) continue;
 
             String selector = null;
             try {
                 selector = generateUniqueName(method, method.dclName);
             } catch (final UndeclaredException e) {
                 // should not get here
                 e.printStackTrace();
             }
             sit.addSelector(selector);
 
             if (!this.isAbstract()) sit.addIndex(classSITLbl, selector, generateFullId(method));
         }
     }
 
     public void addToSubtypeIndexedTable(final SubtypeIndexedTable<?, ?> subtit) {
         final String subtypeITLbl = generateSubtypeIT();
 
         if (!this.isAbstract()){
             subtit.addSubtype(subtypeITLbl);
         }
 
         subtit.addSuperType(this.fullName);
         for (final String superType : this.assignableTo) {
             subtit.addSuperType(superType);
         }
 
         if(!this.isAbstract()){
             subtit.addIndex(subtypeITLbl, this.fullName);
             for (final String superType : this.assignableTo) {
                 subtit.addIndex(subtypeITLbl, superType);
             }
         }
     }
 
     private void cleanHelper(final Map<String, ? extends AModifiersOptSymbol> from) throws CompilerException{
         for(final AModifiersOptSymbol method : from.values()){
             for(final ASTSymbolFactory cleaner : CLEANERS){
                 if(method.dclInResolver == this) cleaner.convertAll(method);
             }
         }
     }
 
     boolean isClean = false;
     public void clean() throws CompilerException{
         if(isClean) return;
         isClean = true;
         cleanHelper(methodMap);
         cleanHelper(smethodMap);
         cleanHelper(constructors);
         cleanHelper(fieldMap);
         cleanHelper(sfieldMap);
     }
 }
