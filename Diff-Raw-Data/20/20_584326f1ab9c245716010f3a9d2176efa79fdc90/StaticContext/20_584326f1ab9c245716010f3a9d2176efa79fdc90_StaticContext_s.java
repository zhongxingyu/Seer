 package org.zwobble.shed.compiler.typechecker;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.zwobble.shed.compiler.Option;
 import org.zwobble.shed.compiler.naming.FullyQualifiedName;
 import org.zwobble.shed.compiler.parsing.nodes.Declaration;
 import org.zwobble.shed.compiler.parsing.nodes.GlobalDeclaration;
 import org.zwobble.shed.compiler.parsing.nodes.Identity;
 import org.zwobble.shed.compiler.types.ClassType;
 import org.zwobble.shed.compiler.types.CoreTypes;
 import org.zwobble.shed.compiler.types.InterfaceType;
 import org.zwobble.shed.compiler.types.ScalarType;
 import org.zwobble.shed.compiler.types.ScalarTypeInfo;
 import org.zwobble.shed.compiler.types.Type;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static org.zwobble.shed.compiler.Option.none;
 import static org.zwobble.shed.compiler.Option.some;
 import static org.zwobble.shed.compiler.typechecker.ShedTypeValue.shedTypeValue;
 import static org.zwobble.shed.compiler.types.Interfaces.interfaces;
 import static org.zwobble.shed.compiler.types.Members.members;
 
 public class StaticContext {
     private final Map<FullyQualifiedName, Type> global = new HashMap<FullyQualifiedName, Type>();
     private final Map<Identity<Declaration>, ValueInfo> types = new HashMap<Identity<Declaration>, ValueInfo>();
     private final Map<ScalarType, ScalarTypeInfo> scalarTypeInfo = new HashMap<ScalarType, ScalarTypeInfo>();
     private final BiMap<Type, Type> metaClasses = HashBiMap.create();
     private final Map<String, GlobalDeclaration> builtIns = new HashMap<String, GlobalDeclaration>();
     
     public void add(Declaration declaration, ValueInfo type) {
         types.put(new Identity<Declaration>(declaration), type);
     }
 
     public VariableLookupResult get(Declaration declaration) {
         Identity<Declaration> key = new Identity<Declaration>(declaration);
         if (types.containsKey(key)) {
             return VariableLookupResult.success(types.get(key));
         } else {
             return VariableLookupResult.notDeclared();            
         }
     }
 
     public void addGlobal(FullyQualifiedName name, Type type) {
         global.put(name, type);
     }
     
     public Option<Type> lookupGlobal(FullyQualifiedName name) {
         if (global.containsKey(name)) {
             return some(global.get(name));
         } else {
             return none();
         }
     }
     
     public void addInfo(ScalarType type, ScalarTypeInfo typeInfo) {
         scalarTypeInfo.put(type, typeInfo);
     }
     
     public ScalarTypeInfo getInfo(ScalarType scalarType) {
         return scalarTypeInfo.get(scalarType);
     }
 
     public Type addClass(Declaration declaration, ClassType type, Iterable<Type> classParameters, ScalarTypeInfo classTypeInfo) {
         List<Type> functionTypeParameters = newArrayList(classParameters);
         functionTypeParameters.add(type);
         return addScalarType(declaration, type, classTypeInfo, interfaces(CoreTypes.functionTypeOf(functionTypeParameters), CoreTypes.CLASS));
     }
 
     public Type addClass(Declaration declaration, ClassType type, ScalarTypeInfo classTypeInfo) {
         return addScalarType(declaration, type, classTypeInfo, interfaces(CoreTypes.CLASS));
     }
 
     public Type addInterface(Declaration declaration, InterfaceType type, ScalarTypeInfo classTypeInfo) {
         return addScalarType(declaration, type, classTypeInfo, interfaces(CoreTypes.CLASS));
     }
     
     private Type addScalarType(Declaration declaration, ScalarType type, ScalarTypeInfo scalarTypeInfo, Set<Type> superClasses) {
         // TODO: forbid user-declared members called Meta 
         FullyQualifiedName metaClassName = type.getFullyQualifiedName().extend("Meta");
         ClassType metaClass = new ClassType(metaClassName);
         ScalarTypeInfo metaClassTypeInfo = new ScalarTypeInfo(superClasses, members());
         
         add(declaration, ValueInfo.unassignableValue(metaClass, shedTypeValue(type)));
         addInfo(type, scalarTypeInfo);
         addInfo(metaClass, metaClassTypeInfo);
         metaClasses.put(type, metaClass);
         // TODO: don't return metaClass (can be accessed through getter, not obvious that we'd return the metaclass from name of methods)
         return metaClass;
     }
     
     public Type getMetaClass(Type type) {
         return metaClasses.get(type);
     }
     
     public Type getTypeFromMetaClass(Type type) {
         return metaClasses.inverse().get(type);
     }
     
     public boolean isMetaClass(Type type) {
         return metaClasses.containsValue(type);
     }
     
     public void addBuiltIn(String name, GlobalDeclaration declaration) {
         builtIns.put(name, declaration);
     }
 
     public Map<String, GlobalDeclaration> getBuiltIns() {
         return builtIns;
     }
 }
