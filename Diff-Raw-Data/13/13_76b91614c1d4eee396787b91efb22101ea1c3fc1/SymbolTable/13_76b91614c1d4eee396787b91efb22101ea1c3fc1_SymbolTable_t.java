 package symbolTable;
 
 import errorHandling.AccessSpecViolation;
 import errorHandling.AmbiguousReference;
 import errorHandling.CannotBeOverloaded;
 import errorHandling.ChangingMeaningOf;
 import errorHandling.ConflictingDeclaration;
 import errorHandling.ConflictingRVforVirtual;
 import errorHandling.DiffrentSymbol;
 import errorHandling.DoesNotNameType;
 import errorHandling.ErrorMessage;
 import errorHandling.InvalidCovariantForVirtual;
 import errorHandling.InvalidScopeResolution;
 import errorHandling.NotDeclared;
 import errorHandling.Redefinition;
 import errorHandling.SameNameAsParentClass;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 import symbolTable.namespace.CpmClass;
 import symbolTable.namespace.DefinesNamespace;
 import symbolTable.namespace.NamedType;
 import symbolTable.namespace.Namespace;
 import symbolTable.namespace.SynonymType;
 import symbolTable.types.Method;
 import symbolTable.types.Type;
 
 /**
  *
  * @author kostas
  */
 public class SymbolTable extends Namespace{
     
     private class ScopeStackElem{
         ScopeType t;
         
         DefinesNamespace scope;
         
         CpmClass.AccessSpecifier access;
 
         public ScopeStackElem() {
             this.t = type;
             this.access = current_access;
             if(type == ScopeType.Class) this.scope = current_class;
             if(type == ScopeType.Namespace) this.scope = current_namespace;
         }
         
     }
     
     
     enum ScopeType{
         Class,
         Namespace
     }
     
     ScopeType type = ScopeType.Namespace;
     
     Namespace current_namespace = this;
     
     CpmClass current_class = null;
     
     Stack<ScopeStackElem> scopes = new Stack<ScopeStackElem>();
     
     HashMap<Type, Type> cachedTypes = new HashMap<Type, Type>();
     
     CpmClass.AccessSpecifier current_access = null;
     
     private String getNameAndPosInfo(String antlrs, Integer location[]){
         String parts[] = antlrs.split(";");
         location[0] = Integer.parseInt(parts[1]);
         location[1] = Integer.parseInt(parts[2]);
         return parts[0];
     }
     
     private Type getTypeFromCache(Type t){
         return cachedTypes.get(t);
     }
     
     private void addTypeToCache(Type t){
         this.cachedTypes.put(t, t);
     }
     
     private Type typeFromCache(Type t){
         Type stored_t = getTypeFromCache(t);
         if(stored_t == null){
             addTypeToCache(t);
             stored_t = t;
         }
         return stored_t;
     }
     
     public SymbolTable(){
         super("", null);
         super.visibleTypeNames = new HashMap<String, NamedType>();
     }
     
     public void setCurrentAccess(CpmClass.AccessSpecifier access){
         this.current_access = access;
     }
     
     public CpmClass.AccessSpecifier getCurrentAccess(){
         return this.current_access;
     }
     
     public void setCurrentScope(CpmClass scope){
         scopes.push(new ScopeStackElem());
         this.type = ScopeType.Class;
         this.current_class = scope;
     }
     
     public void setCurrentScope(Namespace scope){
         scopes.push(new ScopeStackElem());
         this.type = ScopeType.Namespace;
         this.current_namespace = scope;
     }
     
     public void endScope(){
         ScopeStackElem prev = scopes.pop();
         this.type = prev.t;
         this.current_access = prev.access;
         if(this.type == ScopeType.Class){
             this.current_class = (CpmClass) prev.scope;
             this.current_namespace = null;
         }
         else if(this.type == ScopeType.Namespace){
             this.current_namespace = (Namespace) prev.scope;
             this.current_class = null;
         }
     }
     
     public void insertField(String name, Type t, boolean isStatic, int line, int pos) throws ConflictingDeclaration,
                                                                                              ChangingMeaningOf,
                                                                                              DiffrentSymbol {
         
         t = typeFromCache(t);
         if(this.type == ScopeType.Class){
             current_class.insertField(name, t, this.current_access, isStatic, line, pos);
         }
         else if(this.type == ScopeType.Namespace){
             current_namespace.insertField(name, t, line, pos);
         }
     }
     
     public void insertMethod(String name, Method m, boolean isStatic, int line, int pos) throws ConflictingDeclaration, 
                                                                                                 ChangingMeaningOf,
                                                                                                 CannotBeOverloaded,
                                                                                                 DiffrentSymbol,
                                                                                                 ConflictingRVforVirtual,
                                                                                                 InvalidCovariantForVirtual {
 
         Method.Signature signature = m.getSignature();
         Type rv = signature.getReturnValue();
         rv = typeFromCache(rv);
         signature.setReturnValue(rv);
         
        ArrayList<Type> params = signature.getParameters();
         
        if(params != null){
            ArrayList<Type> newParams = new ArrayList<Type>();
            for(Type t : params){
                newParams.add(typeFromCache(t));
            }
            signature.setParameters(newParams);
         }
         /*
          * TODO: get also m from cache
          */
         if(this.type == ScopeType.Class){
             current_class.insertMethod(name, m, this.current_access, isStatic, line, pos);
         }
         else if(this.type == ScopeType.Namespace){
             current_namespace.insertMethod(name, m, line, pos);
         }
     }
     
     public void insertInnerType(String name, CpmClass cpm_class, boolean isStatic) throws SameNameAsParentClass,
                                                                                           ConflictingDeclaration,
                                                                                           Redefinition,
                                                                                           DiffrentSymbol {
         
         if(this.type == ScopeType.Class){
             current_class.insertInnerType(name, cpm_class, this.current_access, isStatic);
         }
         else if(this.type == ScopeType.Namespace){
             current_namespace.insertInnerType(name, cpm_class);
         }
     }
     
     public void insertInnerSyn(String name, SynonymType syn) throws SameNameAsParentClass,
                                                                     ConflictingDeclaration,
                                                                     Redefinition,
                                                                     DiffrentSymbol{
         
         syn.setSynonym(this.typeFromCache(syn.getSynonym()));
         if(this.type == ScopeType.Class){
             current_class.insertInnerSynonymType(name, syn, this.current_access, false);
         }
         else if(this.type == ScopeType.Namespace){
             current_namespace.insertInnerSynonym(name, syn);
         }
     }
     
     public void insertNamespace(String name, Namespace inner_namespace) throws ErrorMessage {
         if(this.type == ScopeType.Class) throw new ErrorMessage("Error: this should not be happening");
         current_namespace.insertInnerNamespace(name, inner_namespace);
     }
     
     public DefinesNamespace getCurrentNamespace(){
         DefinesNamespace rv = null;
         if(this.type == ScopeType.Class){
             rv = this.current_class;
         }
         else if(this.type == ScopeType.Namespace){
             rv = this.current_namespace;
         }
         return rv;
     }
     
     public boolean isCurrentNamespaceClass(){
         return this.type == ScopeType.Class;
     }
     
     public NamedType getNamedTypeFromNestedNameId(ArrayList<String> chain, boolean explicitGlobalScope, boolean allowNull, boolean ignore_access) 
                                                                                                         throws AccessSpecViolation, 
                                                                                                                AmbiguousReference,
                                                                                                                NotDeclared,
                                                                                                                InvalidScopeResolution,
                                                                                                                DoesNotNameType {
         NamedType rv = null;
         DefinesNamespace curr = this.getCurrentNamespace();
         int line, pos;
         Integer location[] = new Integer[2];
         if(chain.size() == 1){
             String t_name;
             t_name = this.getNameAndPosInfo(chain.get(0), location);
             line = location[0];
             pos = location[1];
             try{
                 if(explicitGlobalScope == true){
                     rv = super.findNamedType(t_name, curr, ignore_access);
                 }
                 else{
                     rv = curr.isValidNamedType(t_name, ignore_access);
                 }
             }
             catch(AmbiguousReference ambiguous){
                 ambiguous.setLastValid(null);
                 ambiguous.referenceTypeError(line, pos);
                 ambiguous.finalizeErrorMessage();
                 ambiguous.setIsPending();
                 throw ambiguous;
             }
             catch(AccessSpecViolation access_viol){
                 access_viol.setContextError(line, pos);
                 throw access_viol;
             }
 
             if(rv == null && allowNull == false) throw new DoesNotNameType(t_name);
             
         }
         else{
             DefinesNamespace runner;
             String namespaceName = this.getNameAndPosInfo(chain.get(0), location);
             line = location[0];
             pos = location[1];
             DefinesNamespace prev = null;
             int i = 0;
             try{
                 if(explicitGlobalScope == true){
                     runner = super.findNamespace(namespaceName, curr, ignore_access);
                 }
                 else{
                     runner = curr.findNamespace(namespaceName, curr, ignore_access);
                 }
 
                 if(runner == null) throw new NotDeclared(namespaceName, line, pos);
 
                 for(i = 1 ; i < chain.size() - 1 ; ++i){
                     prev = runner;
                     namespaceName = this.getNameAndPosInfo(chain.get(i), location);
                     line = location[0];
                     pos = location[1];
                     runner = runner.findInnerNamespace(namespaceName, curr, ignore_access);
                     if(runner == null) throw new NotDeclared(prev + "::" + namespaceName, line, pos);
                 }
                 namespaceName = this.getNameAndPosInfo(chain.get(i), location);
                 line = location[0];
                 pos = location[1];
                 rv = runner.findNamedType(namespaceName, curr, ignore_access);
 
                 if(rv == null) throw new DoesNotNameType(namespaceName);
             }
             catch(AmbiguousReference ambigoous){
                 ambigoous.setLastValid(prev);
                 ambigoous.referenceTypeError(line, pos);
                 if(i == chain.size() - 1) ambigoous.setIsPending();
                 if(ambigoous.isPending() == true){
                     ambigoous.finalizeErrorMessage();
                 }
                 else{
                     ambigoous.finalizeErrorMessage(line, pos);
                 }
                 throw ambigoous;
             }
             catch(InvalidScopeResolution invalid){
                 invalid.setMessage(prev, namespaceName, line, pos);
                 throw invalid;
             }
             catch(AccessSpecViolation access_viol){
                 access_viol.setContextError(line, pos);
                 throw access_viol;
             }
         }
         
         return rv;
     }
     
 }
