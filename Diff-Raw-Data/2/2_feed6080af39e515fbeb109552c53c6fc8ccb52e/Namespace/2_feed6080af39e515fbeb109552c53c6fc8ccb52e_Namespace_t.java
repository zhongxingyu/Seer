 package symbolTable.namespace;
 
 import errorHandling.*;
 import java.util.*;
 import symbolTable.types.Method;
 import symbolTable.types.Type;
 
 /**
  *
  * @author kostas
  */
 public class Namespace implements DefinesNamespace{
     
     String name;
     
     protected Map<String, NamespaceElement<? extends Type>> allSymbols = new HashMap<String, NamespaceElement<? extends Type>>();
     
     protected Map<String, NamespaceElement<Type>> fields;
     
     protected Map<String, Map<Method.Signature, NamespaceElement<Method>>> methods;
     
     protected Map<String, NamespaceElement<Namespace>> innerNamespaces;
     
     protected Map<String, NamespaceElement<CpmClass>> innerTypes;
     
     protected Map<String, NamespaceElement<SynonymType>> innerSynonynms;
     
     protected Map<String, TypeDefinition> visibleTypeNames;
     
     protected List<MethodDefinition> methodDefinitions;
     
     protected Set<Namespace> usingDirectives = null;
     
     protected Set<String> conflictsInTypeNames = null;
     
     DefinesNamespace belongsTo;
     
     String fileName;
     
     int line, pos;
     
     private void findAllCandidates(String name,
                                    DefinesNamespace from_scope, 
                                    HashSet<Namespace> visited, 
                                    List<TypeDefinition> typeAgr,
                                    List<Namespace> namespaceAgr,
                                    List<NamespaceElement<? extends Type>> fieldArg,
                                    List<Map<Method.Signature, ? extends MemberElementInfo<Method>>> methAgr,
                                    Namespace firstNamespace,
                                    boolean searchInSupers) {
         
         if(visited.contains(this) == true) return;
         
         visited.add(this);
         
         if(this.innerTypes != null && this.innerTypes.containsKey(name) == true){
             NamespaceElement<CpmClass> tElem = this.innerTypes.get(name);
             typeAgr.add(tElem.element);
         }
         else if(this.innerSynonynms != null && this.innerSynonynms.containsKey(name) == true){
             NamespaceElement<SynonymType> sElem = this.innerSynonynms.get(name);
             typeAgr.add(sElem.element);
         }
         else if(this.innerNamespaces != null && this.innerNamespaces.containsKey(name) == true){
             NamespaceElement<Namespace> nElem = this.innerNamespaces.get(name);
             namespaceAgr.add(nElem.element);
         }
         else if(this.fields != null && this.fields.containsKey(name) == true){
             NamespaceElement<Type> tElem = this.fields.get(name);
             fieldArg.add(tElem);
         }
         else if(this.methods != null && this.methods.containsKey(name) == true){
             methAgr.add(this.methods.get(name));
         }
         
         if((typeAgr.isEmpty() == false || namespaceAgr.isEmpty() == false || fieldArg.isEmpty() == false || methAgr.isEmpty() == false) && this == firstNamespace) return;
         
         if(this.usingDirectives != null && searchInSupers == true){
             for(Namespace namSpace : this.usingDirectives){
                 namSpace.findAllCandidates(name, from_scope, visited, typeAgr, namespaceAgr, fieldArg, methAgr, firstNamespace, true);
             }
         }
     }
     
         protected class NamespaceElement <T> implements MemberElementInfo<T> {
             
             T element;
             
             String fileName;
             
             int line, pos;
             
             public NamespaceElement(T element, String fileName, int line, int pos){
                 this.element = element;
                 this.fileName = fileName;
                 this.line = line;
                 this.pos = pos;
             }
             
             @Override
             public String getFileName(){
                     return this.fileName;
                 }
                 
             @Override
             public int getLine(){
                 return this.line;
             }
 
             @Override
             public int getPos(){
                 return this.pos;
             }
             
             @Override
             public T getElement(){
                 return this.element;
             }
             
             @Override
             public boolean equals(Object o){
                 if(o == null) return false;
                 if(! (o instanceof NamespaceElement)) return false;
                 
                 if(o != this){
                     NamespaceElement<?> othElem = (NamespaceElement<?>) o;
                     
                     if(this.element.equals(othElem.element) == false) return false;
                     
                     if(this.fileName.equals(othElem.fileName) == false) return false;
                     
                     if(this.line != othElem.line) return false;
                     
                     if(this.pos != othElem.pos) return false;
                 }
                 
                 return true;
             }
 
             @Override
             public int hashCode() {
                 int hash = 5;
                 hash = 59 * hash + (this.element != null ? this.element.hashCode() : 0);
                 hash = 59 * hash + (this.fileName != null ? this.fileName.hashCode() : 0);
                 hash = 59 * hash + this.line;
                 hash = 59 * hash + this.pos;
                 return hash;
             }
 
             @Override
             public boolean isStatic() {
                 return false;
             }
 
             @Override
             public boolean isClassMember() {
                 return false;
             }
 
             @Override
             public boolean isDefined() {
                 return true;
             }
 
             @Override
             public void defineStatic(int defLine, int defPos, String defFilename) {
                 throw new UnsupportedOperationException("Definition of a static namespace element is the declaration point.");
             }
 
             @Override
             public int getStaticDefLine() {
                 return this.getLine();
             }
 
             @Override
             public int getStaticDefPos() {
                 return this.getPos();
             }
 
             @Override
             public String getStaticDefFile() {
                 return this.getFileName();
             }
             
         }
 
     private void insertInAllSymbols(String name, NamespaceElement<? extends Type> entry){
         this.allSymbols.put(name, entry);
     }
     
     private String getFieldsFullName(String field_name){
         String rv = this.belongsTo != null ? this.belongsTo.toString() : "";
         if(rv.equals("") == false) rv += "::";
         rv += field_name;
         return rv;
     }
     
     private void checkForConflictsInDecl(String name, Type t, int line, int pos) throws ConflictingDeclaration{
         if(this.allSymbols.containsKey(name) == true){
             NamespaceElement<? extends Type> old_entry = this.allSymbols.get(name);
             String id = this.getFieldsFullName(name);
             throw new ConflictingDeclaration(id, t, old_entry.element, line, pos, old_entry.fileName, old_entry.line, old_entry.pos);
         }
     }
     
     private void checkForConflictsInDecl(String name, TypeDefinition t, int line, int pos) throws ConflictingDeclaration{
         if(this.allSymbols.containsKey(name) == true){
             NamespaceElement<? extends Type> old_entry = this.allSymbols.get(name);
             String id = this.getFieldsFullName(name);
             throw new ConflictingDeclaration(id, t, old_entry.element, line, pos, old_entry.fileName, old_entry.line, old_entry.pos);
         }
     }
     
     private void checkForConflictsInDecl(String name, Namespace namespace, int line, int pos) throws DiffrentSymbol{
         if(this.allSymbols.containsKey(name) == true){
             NamespaceElement<? extends Type> old_entry = this.allSymbols.get(name);
             String id = this.getFieldsFullName(name);
             throw new DiffrentSymbol(id, namespace, old_entry.element, line, pos, old_entry.fileName, old_entry.line, old_entry.pos);
         }
     }
     
     private void checkForConflictsWithNamespaces(String name, Type t, int line, int pos) throws DiffrentSymbol{
         if(this.innerNamespaces != null && this.innerNamespaces.containsKey(name) == true){
             NamespaceElement<Namespace> namespace = this.innerNamespaces.get(name);
             String id = this.getFieldsFullName(name);
             throw new DiffrentSymbol(id, t, namespace.element, line, pos, namespace.line, namespace.pos);
         }
     }
     
     private void checkForConflictsWithNamespaces(String name, TypeDefinition t, int line, int pos) throws DiffrentSymbol{
         if(this.innerNamespaces != null && this.innerNamespaces.containsKey(name) == true){
             NamespaceElement<Namespace> namespace = this.innerNamespaces.get(name);
             String id = this.getFieldsFullName(name);
             throw new DiffrentSymbol(id, t, namespace.element, line, pos, namespace.line, namespace.pos);
         }
     }
     
     private void checkForChangingMeaningOfType(String name, Type new_entry, int line, int pos) throws ChangingMeaningOf {
         if(this.visibleTypeNames.containsKey(name) == true){
             TypeDefinition t = this.visibleTypeNames.get(name);
             String id = this.getFieldsFullName(name);
             throw new ChangingMeaningOf(id, name, new_entry, t, line, pos);
         }
     }
     
     public Namespace(String name, DefinesNamespace belongsTo){
         this.name = name;
         this.belongsTo = belongsTo;
         if(this.belongsTo != null){
             this.visibleTypeNames = new HashMap<String, TypeDefinition>(this.belongsTo.getVisibleTypeNames());
         }
         else{
             this.visibleTypeNames = new HashMap<String, TypeDefinition>();
         }
     }
     
     public void insertField(String name, Type t, String fileName, int line, int pos) throws ConflictingDeclaration, 
                                                                                             ChangingMeaningOf,
                                                                                             DiffrentSymbol {
         
         if(fields == null) fields = new HashMap<String, NamespaceElement<Type>>();
         this.checkForConflictsInDecl(name, t, line, pos);
         this.checkForChangingMeaningOfType(name, t, line, pos);
         this.checkForConflictsWithNamespaces(name, t, line, pos);
         NamespaceElement<Type> elem = new NamespaceElement<Type>(t, fileName, line, pos);
         fields.put(name, elem);
         insertInAllSymbols(name, elem);
     }
     
     public void insertMethod(String name, Method m, String fileName, int line, int pos) throws CannotBeOverloaded,
                                                                                                ConflictingDeclaration,
                                                                                                ChangingMeaningOf,
                                                                                                DiffrentSymbol,
                                                                                                Redefinition{
         
         if(methods == null) methods = new HashMap<String, Map<Method.Signature, NamespaceElement<Method>>>();
         if(methods.containsKey(name) == true){
             Map<Method.Signature, NamespaceElement<Method>> ms = methods.get(name);
             if(ms.containsKey(m.getSignature())){
                 NamespaceElement<Method> old_m = ms.get(m.getSignature());
                 Method old = old_m.element;
                 String id = this.getFieldsFullName(name);
                 if(m.equals(old) == true){
                     if(m.isDefined() && old.isDefined()){
                         throw new Redefinition(id, m, line, pos, old, old_m.fileName, old_m.line, old_m.pos);
                     }
                 }
                 else{
                     throw new CannotBeOverloaded(m.toString(id), old_m.element.toString(id), line, pos, old_m.fileName, old_m.line, old_m.pos);
                 }
             }
             ms.put(m.getSignature(), new NamespaceElement<Method>(m, fileName, line, pos));
         }
         else{
             this.checkForConflictsInDecl(name, m, line, pos);
             this.checkForChangingMeaningOfType(name, m, line, pos);
             this.checkForConflictsWithNamespaces(name, m, line, pos);
             HashMap<Method.Signature, NamespaceElement<Method>> new_entry = new HashMap<Method.Signature, NamespaceElement<Method>>();
             NamespaceElement<Method> elem = new NamespaceElement<Method>(m, fileName, line, pos);
             new_entry.put(m.getSignature(), elem);
             methods.put(name, new_entry);
             insertInAllSymbols(name, elem);
         }
     }
     
     public void insertInnerType(String name, CpmClass t) throws ConflictingDeclaration,
                                                                 DiffrentSymbol,
                                                                 Redefinition {
         
         if(innerTypes == null) innerTypes = new HashMap<String, NamespaceElement<CpmClass>>();
         this.checkForConflictsInDecl(name, t, t.line, t.pos);
         this.checkForConflictsWithNamespaces(name, t, t.line, t.pos);
         if(innerTypes.containsKey(name) == true){
             CpmClass t1 = innerTypes.get(name).element;
             if(t1.isComplete() == false){
                 innerTypes.put(name, new NamespaceElement<CpmClass>(t, t.fileName, t.line, t.pos));
             }
             else if(t.isComplete() == true){
                 throw new Redefinition(t, t1);
             }
             return;
         }
         else if(this.innerSynonynms != null && this.innerSynonynms.containsKey(name) == true){
             NamespaceElement<SynonymType> old_entry = this.innerSynonynms.get(name);
             throw new Redefinition(t, old_entry.element);
         }
         innerTypes.put(name, new NamespaceElement<CpmClass>(t, t.fileName, t.line, t.pos));
         this.visibleTypeNames.put(name, t);
     }
     
     public void insertInnerSynonym(String name, SynonymType syn) throws ConflictingDeclaration,
                                                                         DiffrentSymbol,
                                                                         Redefinition {
         
         if(innerSynonynms == null) innerSynonynms = new HashMap<String, NamespaceElement<SynonymType>>();
         this.checkForConflictsInDecl(name, syn, syn.line, syn.pos);
         this.checkForConflictsWithNamespaces(name, syn, syn.line, syn.pos);
         if(this.innerSynonynms.containsKey(name) == true){
             NamespaceElement<SynonymType> old_entry = this.innerSynonynms.get(name);
             throw new Redefinition(syn, old_entry.element);
         }
         else if(this.innerTypes != null && this.innerTypes.containsKey(name) == true){
             NamespaceElement<CpmClass> old_entry = this.innerTypes.get(name);
             throw new Redefinition(syn, old_entry.element);
         }
         this.innerSynonynms.put(name,  new NamespaceElement<SynonymType>(syn, syn.fileName, syn.line, syn.pos));
         this.visibleTypeNames.put(name, syn);
     }
     
     public Namespace insertInnerNamespace(String name, Namespace namespace) throws DiffrentSymbol{
 
         if(innerNamespaces == null) innerNamespaces = new HashMap<String, NamespaceElement<Namespace>>();
         this.checkForConflictsInDecl(name, namespace, namespace.line, namespace.pos);
        if(this.innerTypes != null && this.innerTypes.containsKey(name) == true) {
             NamespaceElement<CpmClass> old_entry = this.innerTypes.get(name);
             String id = this.getFieldsFullName(name);
             throw new DiffrentSymbol(id, namespace, old_entry.element, namespace.line, namespace.pos, old_entry.line, old_entry.pos);
         }
         else if(this.innerSynonynms != null && this.innerSynonynms.containsKey(name) == true){
             NamespaceElement<SynonymType> old_entry = this.innerSynonynms.get(name);
             String id = this.getFieldsFullName(name);
             throw new DiffrentSymbol(id, namespace, old_entry.element, namespace.line, namespace.pos, old_entry.line, old_entry.pos);
         }
         Namespace rv;
         if(!innerNamespaces.containsKey(name)){
             innerNamespaces.put(name, new NamespaceElement<Namespace>(namespace, namespace.fileName, namespace.line, namespace.pos));
             rv = namespace;
         }
         else{
             /*
              * merging the existing namespace with the extension declaration.
              */
             NamespaceElement<Namespace> elem = this.innerNamespaces.get(name);
             if(elem.fileName == null){
                 elem.fileName = namespace.fileName;
                 elem.line = namespace.line;
                 elem.line = namespace.pos;
             }
             rv = elem.element;
             /*
             if(namespace.fields != null){
                 for(String key : namespace.fields.keySet()){
                     NamespaceElement<Type> elem = namespace.fields.get(key);
                     Type t = elem.element;
                     exists.insertField(name, t, namespace.fileName, elem.line, elem.pos);
                 }
             }
             if(namespace.methods != null){
                 for(String key : namespace.methods.keySet()){
                     HashMap<Method.Signature, NamespaceElement<Method>> ms = namespace.methods.get(key);
                     for(NamespaceElement<Method> m : ms.values()){
                         exists.insertMethod(key, m.element, m.line, m.pos);
                     }
                 }
             }
             if(namespace.innerNamespaces != null){
                 for(String key : namespace.innerNamespaces.keySet()){
                     ///*
                      //* merge again all the inner namespaces.
                      //
                     NamespaceElement<Namespace> n = namespace.innerNamespaces.get(key);
                     exists.insertInnerNamespace(key, n.element);
                 }
             }
             if(namespace.innerTypes != null){
                 for(String key : namespace.innerTypes.keySet()){
                     NamespaceElement<CpmClass> t = namespace.innerTypes.get(key);
                     exists.insertInnerType(key, t.element);
                 }
             }
             if(namespace.innerSynonynms != null){
                 for(String key : namespace.innerSynonynms.keySet()){
                     NamespaceElement<SynonymType> syn = namespace.innerSynonynms.get(key);
                     exists.insertInnerSynonym(key, syn.element);
                 }
             }*/
         }
         return rv;
     }
     
     public void insertMethodDefinition(MethodDefinition methDef){
         if(this.methodDefinitions == null) this.methodDefinitions = new ArrayList<MethodDefinition>();
         
         this.methodDefinitions.add(methDef);
     }
     
     public void setLineAndPos(int line, int pos){
         this.line = line;
         this.pos = pos;
     }
     
     public void setFileName(String fileName){
         this.fileName = fileName;
     }
     
     public String getFileName(){
         return this.fileName;
     }
     
     public int getLine(){
         return this.line;
     }
     
     public int getPos(){
         return this.pos;
     }
     
     @Override
     public String toString(){
         return "namespace " + this.getStringName(new StringBuilder()).toString();
     }
 
     /*
      * DefinesNamespace methods
      */
     
     @Override
     public StringBuilder getStringName(StringBuilder in){
         if(belongsTo == null) return in.append(name);
         StringBuilder parent = this.belongsTo.getStringName(in);
         return parent.append(parent.toString().equals("") ? "" : "::").append(name);
     }
     
     @Override
     public DefinesNamespace getParentNamespace() {
         return this.belongsTo;
     }
 
     @Override
     public TypeDefinition isValidTypeDefinition(String name, boolean ignore_access) throws AccessSpecViolation, AmbiguousReference {
         TypeDefinition rv = null;
         DefinesNamespace curr_namespace = this;
         while(curr_namespace != null){
             /*
              * from_scope is null because all parents are namespaces.
              */
             rv = curr_namespace.findTypeDefinition(name, null, ignore_access);
             if(rv != null) break;
             curr_namespace = curr_namespace.getParentNamespace();
         }
         return rv;
     }
 
     @Override
     public TypeDefinition findTypeDefinition(String name, DefinesNamespace from_scope, boolean ignore_access) throws AmbiguousReference, AccessSpecViolation {
         TypeDefinition rv;
         
         LookupResult res = this.localLookup(name, from_scope, true, ignore_access);
         
         rv = res.isResultType();
         
         return rv;
     }
     
     @Override
     public DefinesNamespace findNamespace(String name, DefinesNamespace from_scope, boolean ignore_access) throws AccessSpecViolation,
                                                                                                                   AmbiguousReference,
                                                                                                                   InvalidScopeResolution{
         DefinesNamespace rv;
         rv = this.findInnerNamespace(name, from_scope, ignore_access);
         if(rv == null && this.belongsTo != null){
             rv = this.belongsTo.findNamespace(name, from_scope, ignore_access);
         }
         return rv;
     }
     
     @Override
     public DefinesNamespace findInnerNamespace(String name, DefinesNamespace from_scope, boolean ignore_access) throws AmbiguousReference, 
                                                                                                                        AccessSpecViolation,
                                                                                                                        InvalidScopeResolution {
         DefinesNamespace rv;
         
         LookupResult res = this.localLookup(name, from_scope, true, ignore_access);
         
         rv = res.doesResultDefinesNamespace();
         
         return rv;
     }
     
     @Override
     public Map<String, TypeDefinition> getVisibleTypeNames() {
         return this.visibleTypeNames;
     }
     
     @Override
     public String getFullName(){
         return this.getStringName(new StringBuilder()).toString();
     }
     
     @Override
     public String getName(){
         return this.name;
     }
     
     @Override
     public LookupResult localLookup(String name, DefinesNamespace from_scope, boolean searchInSupers, boolean ignore_access){
         List<TypeDefinition> candidatesTypes = new ArrayList<TypeDefinition>();
         List<NamespaceElement<? extends Type>> candidateFields = new ArrayList<NamespaceElement<? extends Type>>();
         List<Map<Method.Signature, ? extends MemberElementInfo<Method>>> candidateMethods = new ArrayList<Map<Method.Signature, 
                                                                                                               ? extends MemberElementInfo<Method>>> ();
         List<Namespace> candidateNamespaces = new ArrayList<Namespace>();
 
         this.findAllCandidates(name,
                                from_scope,
                                new HashSet<Namespace>(),
                                candidatesTypes,
                                candidateNamespaces,
                                candidateFields,
                                candidateMethods,
                                this,
                                true);
         
         return new LookupResult(name, candidatesTypes, candidateNamespaces, candidateFields, candidateMethods, null, null, ignore_access);
     }
 
     @Override
     public LookupResult lookup(String name, DefinesNamespace from_scope, boolean searchInSupers, boolean ignore_access){
         LookupResult rv;
         
         DefinesNamespace curr = this;
         
         do{
             rv = curr.localLookup(name, from_scope, searchInSupers, ignore_access);
             curr = curr.getParentNamespace();
         }while(rv.isResultEmpty());
         
         return rv;
     }
     
     @Override
     public boolean isEnclosedInNamespace(DefinesNamespace namespace){
         boolean rv = false;
         
         DefinesNamespace curr = this.belongsTo;
         while(curr != null){
             if(curr == namespace){
                 rv = true;
                 break;
             }
             curr = curr.getParentNamespace();
         }
         
         return rv;
     }
     
     @Override
     public void resetNonClassFields(){
         this.fields = null;
         this.methods = null;
         
         if(this.innerNamespaces != null){
             for(NamespaceElement<Namespace> namElem : this.innerNamespaces.values()){
                 namElem.element.resetNonClassFields();
             }
         }
         
         if(this.methodDefinitions != null){
             for(MethodDefinition methDef : this.methodDefinitions){
                 methDef.resetNonClassFields();
             }
         }
     }
 
     
     private DefinesNamespace isNameSpace(TypeDefinition n_type) throws InvalidScopeResolution {
         DefinesNamespace rv = null;
         if(n_type != null){
             if(n_type instanceof DefinesNamespace){
                 rv = (DefinesNamespace)n_type;
             }
             else{
                 throw new InvalidScopeResolution(); //change this to invalid use of ::
             }
         }
         return rv;
     }
     
     public void insertUsingDirective(Namespace nm){
         if(this.conflictsInTypeNames == null) this.conflictsInTypeNames = new HashSet<String>();
         if(this.usingDirectives == null) this.usingDirectives = new HashSet<Namespace>();
         
         this.usingDirectives.add(nm);
         
         Set<String> typeNames = nm.visibleTypeNames.keySet();
         Set<String> fieldNames = nm.allSymbols.keySet();
         
         for(String typeName : typeNames){
             if(this.conflictsInTypeNames.contains(typeName) == false){
                 if(this.visibleTypeNames.containsKey(typeName) == false){
                     this.visibleTypeNames.put(typeName, nm.visibleTypeNames.get(typeName));
                 }
                 else{
                     this.visibleTypeNames.remove(typeName);
                     this.conflictsInTypeNames.add(typeName);
                 }
             }
         }
         
         for(String fieldName : fieldNames){
             if(this.conflictsInTypeNames.contains(fieldName) == false){
                 if(this.visibleTypeNames.containsKey(fieldName) == true){
                     this.visibleTypeNames.remove(fieldName);
                 }
             }
         }
     }
 }
