 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import com.sun.codemodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.lang.reflect.Array;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 10.2.12
  * Time: 14:11
  */
 public class ExtenderGenerator extends ClassGenerator {
 
     public ExtenderGenerator(GeneratorConfiguration configuration) {
         super(configuration);
     }
 
     @Override
     public void visit(APIClass apiClass) {
 
         // extender can be generated for classes and interfaces
         if ((!apiClass.getKind().equals(APIItem.Kind.CLASS)) && (!apiClass.getKind().equals(APIItem.Kind.INTERFACE))) {
             return;
         }
 
         // check if extender for this class is enabled in jobConfiguration.
         if (!isEnabled(apiClass.getFullName(), WhitelistRule.RuleItem.EXTENDER)) {
             return;
         }
 
         //  check if tested class is not final
         if (apiClass.getModifiers().contains(APIModifier.FINAL)) {
             return;
         }
 
         // check if class is not deprecated. If it does and in job configuration
         // are deprecated items disabled, this class is skipped.
         if (apiClass.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // code can be generated only for public or protected classes.
         if (!(apiClass.getModifiers().contains(APIModifier.PUBLIC)
                 || apiClass.getModifiers().contains(APIModifier.PROTECTED))) {
             return;
         }
 
         // check if extender has at least one protected or public constructor.
         // If it hasn't, extender can't be generated.
         if (apiClass.getConstructors().isEmpty()) {
             return;
         } else { // and these constructor can't be disabled in configuration.
             boolean validConstructor = false;
             for (APIMethod constructor : apiClass.getConstructors()) {
                 if (isEnabled(methodSignature(constructor, apiClass.getFullName()), WhitelistRule.RuleItem.EXTENDER)
                         &&  (!constructor.isDepreacated() || !jobConfiguration.isSkipDeprecated())) {
                     validConstructor = true;
                     break;
                 }
             }
             if (!validConstructor) return;
         }
 
 
         try {
             visitingClass = apiClass;
             int classMods = 0;
 //            if (visitingClass.getModifiers().contains(APIModifier.ABSTRACT)
 //                    || apiClass.getKind() == APIItem.Kind.INTERFACE) {
 //                classMods = JMod.PUBLIC | JMod.ABSTRACT;
             if (!visitingClass.isNested()) {
                 classMods = JMod.PUBLIC;
             }
 
             // if original method is static, extender has to be static too.
             if (visitingClass.getModifiers().contains(APIModifier.STATIC)) {
                 classMods = classMods | JMod.STATIC;
             }
 
             // if tested item is interface, create Implementator, otherwise Extender
             if (visitingClass.getKind() == APIItem.Kind.INTERFACE) {
                 String className = generateName(configuration.getImplementerClassIdentifier(), apiClass.getName());
                 cls = declareNewClass(classMods, currentPackageName, className, visitingClass.isNested());
                 cls._implements(getTypeRef(visitingClass.getType(), visitingClass.getTypeParamsMap().keySet()));
             } else {
                 String className = generateName(configuration.getExtenderClassIdentifier(), apiClass.getName());
                 cls = declareNewClass(classMods, currentPackageName, className, visitingClass.isNested());
                 cls._extends(getTypeRef(visitingClass.getType(), visitingClass.getTypeParamsMap().keySet()));
             }
 
             if (!apiClass.getTypeParamsMap().isEmpty()) {
                 for (String typeName : apiClass.getTypeParamsMap().keySet()) {
                     JTypeVar type = cls.generify(typeName);
                     for (APIType bound : visitingClass.getTypeParamsMap().get(typeName)) {
                         JClass typeBound = getTypeRef(bound, visitingClass.getTypeParamsMap().keySet());
                         if(!bound.equals(new APIType("java.lang.Object"))) {
                             type.bound(typeBound);
                         }
                     }
                 }
             }
 
             // visit all constructors
             for (APIMethod constructor : apiClass.getConstructors()) {
                 visitConstructor(constructor);
             }
 
             // visit all methods
             for (APIMethod method : apiClass.getMethods()) {
                 method.accept(this);
             }
 
             // create method for fields test if there are any field
             if (apiClass.getFields().size() > 0) {
                 JMethod fieldsMethod = cls.method(JMod.PUBLIC, cm.VOID, configuration.getFieldTestIdentifier());
                 fieldsMethodBlock = fieldsMethod.body();
             }
             // visit all fields
             for (APIField field : apiClass.getFields()) {
                 field.accept(this);
             }
 
             // implement all abstract method of ancestors (super class and implemented interfaces) of current class
             if(visitingClass.getModifiers().contains(APIModifier.ABSTRACT)) {
                 implementAbstractMethods();
             }
 
             classStack.push(cls);
 
             // visit all nested classes
             for (APIClass nestedClass : apiClass.getNestedClasses()) {
                 nestedClass.accept(this);
             }
 
             cls = classStack.pop();
         } catch (JClassAlreadyExistsException e) {
             e.printStackTrace();
         }
     }
 
     private class MethodMetadata {
         private String name;
         private String returnTypeName;
         private List<String> parameterNames;
 
         public MethodMetadata(JMethod method) {
             this.name = method.name();
             this.parameterNames = new LinkedList<>();
 
             Map<String, APIType[]> methodTypeParams = convertTypeParams(method.typeParams());
             for(JVar param : method.params()) {
                 String paramType = prepareSType(new APIType(param.type().erasure().fullName()),
                         visitingClass.getTypeParamsMap(),
                         methodTypeParams, null);
                   this.parameterNames.add(paramType);
             }
             
             String returnType = prepareSType(new APIType(method.type().erasure().fullName()),
                             visitingClass.getTypeParamsMap(),
                             methodTypeParams, null);
             this.returnTypeName = returnType;
         }
 
         public MethodMetadata(APIMethod mth, Map<String, APIType[]>classTypeParams,
                               Map<String, APIType[]>methodTypeParam, Map<String, APIType> ancestorTypeParam) {
             this.name = mth.getName();
             this.parameterNames = new LinkedList<>();
 
             for(APIMethodParameter param : mth.getParameters()) {
                 String paramType = prepareSType(param.getType(), classTypeParams, methodTypeParam, ancestorTypeParam);
                 this.parameterNames.add(paramType);
             }
             this.returnTypeName = prepareSType(mth.getReturnType(),classTypeParams,methodTypeParam, ancestorTypeParam);
         }
 
         private boolean compareTypes(Class clsA, String clsAName, Class clsB, String clsBName) throws Exception {
             if((clsA == null) || (clsB == null)) {
                 if(clsAName.equals(clsBName) || clsAName.equals("java.lang.Object")) {
                     return true;
                 } else {
                     APIClass apiClsB = findClass(clsBName);
                     return apiClsB.isDescendantOf(clsAName);
                 }
             } else {
                 return clsA.isAssignableFrom(clsB);
             } 
         }
         
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (!(o instanceof MethodMetadata)) return false;
 
             MethodMetadata that = (MethodMetadata) o;
 
             if (!name.equals(that.name)) return false;
             if (parameterNames.size() != that.parameterNames.size()) return false;
             for (int i = 0; i < parameterNames.size(); i++) {
                 String thisParam = this.parameterNames.get(i);
                 String thatParam = that.parameterNames.get(i);
                 if(!thisParam.equals(thatParam)) {
                     try {
                         APIClass thatParamClass = findClass(thatParam);
                         if (!thatParamClass.isDescendantOf(thisParam)) {
                             return false;
                         }
                     } catch (Exception e) {
                         System.err.println(e);
                         return false;
                     }
                 }
             }
 
             if(!this.returnTypeName.equals(that.returnTypeName)) {
                 try {
                     APIClass thatReturnType = findClass(that.returnTypeName);
                     return thatReturnType.isDescendantOf(this.returnTypeName);
                 } catch (Exception e) {
                     System.err.println(e);
                     return false;
                 }
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             return name.hashCode();
         }
 
         public String getName() {
             return name;
         }
 
         @Override
         public String toString() {
             return returnTypeName + " " + name + "(" + parameterNames + ")";
         }
     }
     
     private void implementAbstractMethods() {
         Set<MethodMetadata> alreadyImplemented = new HashSet<>();
         Set<MethodMetadata> toImplement = new HashSet<>();        
         
         // collecting already implemented methods in current class
         for (JMethod implementedMethod : cls.methods()) {
             alreadyImplemented.add(new MethodMetadata(implementedMethod));
         }
 
         // collecting final methods implemented in visitingClass.
         for (APIMethod mth : visitingClass.getMethods()) {
             if (mth.getModifiers().contains(APIModifier.FINAL)) {
                 alreadyImplemented.add(new MethodMetadata(mth,visitingClass.getTypeParamsMap(),mth.getTypeParamsMap(),null));
             }
         }
 
 
         Stack<APIType> classesToCheck = new Stack<>();
         // get list of implemented and missing methods from ancestors        
         if (visitingClass.getExtending() != null) {
             classesToCheck.push(visitingClass.getExtending());
         } else {
             classesToCheck.push(new APIType("java.lang.Object"));
         }
         
         for (APIType iface : visitingClass.getImplementing()) {
             classesToCheck.push(iface);
         }
         
         while (!classesToCheck.empty()) {
             try {
                 APIClass cls = findClass(classesToCheck.pop().getName());
 
                 // prirad typeargumenty ke spravnym generickym typum
                 Map<String, APIType> clsTypeParamMap = new LinkedHashMap<>();
                 int i = -1;
                 for (String s : cls.getTypeParamsMap().keySet()) {
                     i++;
                     for(APIType implIntrfc : visitingClass.getImplementing()) {
                        if(implIntrfc.getName().equals(cls.getFullName())) {
                              clsTypeParamMap.put(s,implIntrfc.getTypeArgs().get(i));
                         }
                     }
                 }
 
                 for (APIMethod mth : cls.getMethods()) {
                     if (mth.getModifiers().contains(APIModifier.ABSTRACT)) {
                         if(isEnabled(methodSignature(mth, visitingClass.getFullName()), WhitelistRule.RuleItem.EXTENDER)) {
                             toImplement.add(new MethodMetadata(mth, cls.getTypeParamsMap(), mth.getTypeParamsMap(),
                                     clsTypeParamMap));
                         } else {
                             setAbstractModifier();
                         }                        
                     } else {
                         alreadyImplemented.add(new MethodMetadata(mth, cls.getTypeParamsMap(), mth.getTypeParamsMap(),clsTypeParamMap));
                     }
                 }
 
                 if (cls.getExtending() != null) {
                     classesToCheck.push(cls.getExtending());
                 }
 
                 for (APIType iface : cls.getImplementing()) {
                     classesToCheck.push(iface);
                 }                
             } catch (ClassNotFoundException e) {
                 System.err.println("Class not found " + e.getMessage());
                 e.printStackTrace();
             }
         }
         
         // now, we should have list of all implemented and unimplemented methods.
         for (MethodMetadata method : toImplement) {
             // check if method is not already implemented
             if(!alreadyImplemented.contains(method)) {
                 // check if method is enabled in configuration.
                 List<APIModifier> modifiers = new LinkedList<>();
                 modifiers.add(APIModifier.PUBLIC);
                 List<String> thrown = new LinkedList<>();
                 APIMethod newMethod = new APIMethod(method.getName(),modifiers,method.parameterNames,method.returnTypeName, thrown);
                 addOverridingMethod(newMethod);
                 alreadyImplemented.add(method);
             }
         }
     }
 
     private static Map<String, APIType[]> convertTypeParams(JTypeVar[] typeVars) {
         Map<String, APIType[]> result = new HashMap<>();
         for (JTypeVar typeVar : typeVars) {
             String name = typeVar.fullName();
             APIType[] t = new APIType[1];
             t[0] = new APIType(typeVar._extends().fullName());
             result.put(name,t);
         }
         return result;
     }
 
     private String prepareSType(APIType type, Map<String, APIType[]> classTypeParams,
                                 Map<String, APIType[]> methodTypeParams, Map<String, APIType> ancestorTypeParam) {
         String result = type.getName();
         // try to find in ancestor type params.
         if(ancestorTypeParam != null && ancestorTypeParam.containsKey(type.getName())) {
             result = ancestorTypeParam.get(type.getName()).getName();
         // try to find it in class type params.
         } else if(classTypeParams.containsKey(type.getName())) {
             //type.addTypeParameter(classTypeParams.get(type.getName())[0]);
             result =  classTypeParams.get(type.getName())[0].getName();
         // if it wasn't found, try to find it in method type params.
         } else if(methodTypeParams.containsKey(type.getName())) {
             //type.addTypeParameter(methodTypeParams.get(type.getName())[0]);
             result =  methodTypeParams.get(type.getName())[0].getName();
         }
 
         if(type.isArray()) {
             //Class c = prepareCType(type.getTypeArgs().get(0),classTypeParams,methodTypeParams);
             String fieldDescriptor = prepareSType(type.getTypeArgs().get(0),
                     classTypeParams,methodTypeParams, ancestorTypeParam);
             //if(c.isPrimitive()) { // this is ugly... but I have no better idea, how to get class of array of primitive type.
                 switch (fieldDescriptor) {
                     case "boolean" : fieldDescriptor = "Z"; break;
                     case "byte"    : fieldDescriptor = "B"; break;
                     case "char"    : fieldDescriptor = "C"; break;
                     case "short"   : fieldDescriptor = "S"; break;
                     case "int"     : fieldDescriptor = "I"; break;
                     case "long"    : fieldDescriptor = "L"; break;
                     case "float"   : fieldDescriptor = "F"; break;
                     case "double"  : fieldDescriptor = "D"; break;
                     default        : fieldDescriptor = "L" + fieldDescriptor + ";";
                 }
                 fieldDescriptor = "[" + fieldDescriptor;
                 return fieldDescriptor;
         }
         return result;
     }
 
     private Class prepareCType(APIType type, Map<String, APIType[]> classTypeParams, Map<String, APIType[]> methodTypeParams) {
         APIType result = type;
         // try to find it in class type params.
         if(classTypeParams.containsKey(type.getName())) {
             result = classTypeParams.get(type.getName())[0];
             type.addTypeParameter(classTypeParams.get(type.getName())[0]);
         }
         // if it wasn't found, try to find it in method type params.
         if(methodTypeParams.containsKey(type.getName())) {
             result = methodTypeParams.get(type.getName())[0];
             type.addTypeParameter(methodTypeParams.get(type.getName())[0]);
         }
 
         if(type.isArray()) {
             try {
                 Class c = prepareCType(type.getTypeArgs().get(0),classTypeParams,methodTypeParams);
                 String fieldDescriptor = c.getName();
                 if(c.isPrimitive()) { // this is ugly... but I have no better idea, how to get class of array of primitive type.
                     switch (c.getName()) {
                         case "boolean" : fieldDescriptor = "Z"; break;
                         case "byte"    : fieldDescriptor = "B"; break;
                         case "char"    : fieldDescriptor = "C"; break;
                         case "short"   : fieldDescriptor = "S"; break;
                         case "int"     : fieldDescriptor = "I"; break;
                         case "long"    : fieldDescriptor = "L"; break;
                         case "float"   : fieldDescriptor = "F"; break;
                         case "double"  : fieldDescriptor = "D";
                     }
                     fieldDescriptor = "[" + fieldDescriptor;
                 } else {
                     fieldDescriptor = "[L" + fieldDescriptor + ";";
                 }
 
                 return Class.forName(fieldDescriptor);
             } catch (ClassNotFoundException e) {
                 System.err.println("prepareCType1 - Type cant be verified: " + type);
                 return null;
             } catch (NullPointerException e) {
                 //System.err.println("prepareCType1 - Class not found in type " + type + " in class " + visitingClass.getFullName());
                 return null;
             }
         }
 
         // is it primitive type
         switch (type.getName()) {
             case "boolean" : return boolean.class;
             case "byte"    : return byte.class;
             case "char"    : return char.class;
             case "short"   : return short.class;
             case "int"     : return int.class;
             case "long"    : return long.class;
             case "float"   : return float.class;
             case "double"  : return double.class;
             case "void"    : return void.class;
         }
 
         try {
             return Class.forName(result.getName());
         } catch (ClassNotFoundException e) {
             try {
                 return Class.forName(result.getFlatName());
             } catch (ClassNotFoundException e1) {
                 //System.err.println("prepareCType2 - Cant find: " + type.getName());
                 return null;
             }
         }
     }
 
 
     private void visitConstructor(APIMethod constructor) {
         // Check if constructor is enabled in job configuration.
         if (!isEnabled(methodSignature(constructor, visitingClass.getFullName()), WhitelistRule.RuleItem.EXTENDER)) {
             return;
         }
 
         if (constructor.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // create new constructor
         JMethod constr = cls.constructor(JMod.PUBLIC);
         // define body of the constructor. Body contains only super(...) call.
         JBlock body = constr.body();
         JInvocation superInv = body.invoke("super");
 
         // add generic types
         if (!constructor.getTypeParamsMap().isEmpty()) {
             for (String typeName : constructor.getTypeParamsMap().keySet()) {
                 JTypeVar type = constr.generify(typeName);
                 for (APIType bound : constructor.getTypeParamsMap().get(typeName)) {
                     JClass typeBound = getTypeRef(bound, constructor.getTypeParamsMap().keySet());
                     if (!bound.equals(new APIType("java.lang.Object"))) {
                         type.bound(typeBound);
                     }
                 }
             }
         }
 
         // define params of the constructor.
         for (APIMethodParameter param : constructor.getParameters()) {
             JType type = getTypeRef(param.getType(), constructor.getTypeParamsMap().keySet());
             constr.param(type, String.valueOf(param.getName()));
             superInv.arg(JExpr.ref(String.valueOf(param.getName())));
         }
 
         for (String exception : constructor.getThrown()) {
             constr._throws(getTypeRef(exception, constructor.getTypeParamsMap().keySet()));
         }
     }
 
     /**
      * Generates test of the field.
      * Final fields are tested by assigning their value to new field of same type. Ex: {@code int x = super.x;}.
      * New local variable x hides original super field x, but it doesn't mind.
      * Non-final fields are tested by assigning some value to them. Ex: {@code File f = null; fileField = f;}
      *
      * @param apiField
      */
     @Override
     public void visit(APIField apiField) {
 
         String signature = visitingClass.getFullName() + "." + apiField.getName();
         if (!isEnabled(signature, WhitelistRule.RuleItem.EXTENDER)) {
             return;
         }
 
         if (apiField.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
         // Type of the field has to be public or protected class
         if(!checkTypeAccessModifier(APIModifier.PROTECTED, apiField.getVarType(), visitingClass.getTypeParamsMap().keySet())) {
             return;
         }
 
         if (apiField.getModifiers().contains(APIModifier.FINAL)) { // Final fields
             // original field
             JFieldRef fld;
             if (apiField.getModifiers().contains(APIModifier.STATIC)) {
                 fld = getTypeRef(visitingClass.getFullName(), visitingClass.getTypeParamsMap().keySet()).staticRef(apiField.getName());
             } else {
                 fld = JExpr._super().ref(apiField.getName());
             }
             // create new local variable and assing original value to it
             fieldsMethodBlock.decl(getTypeRef(apiField.getVarType(), visitingClass.getTypeParamsMap().keySet()), apiField.getName(), fld);
         } else {
             // create new field of same type as original
             String fldName = generateName(configuration.getFieldTestVariableIdentifier(), apiField.getName());
             JClass typeRef = getTypeRef(apiField.getVarType(), visitingClass.getTypeParamsMap().keySet());
             JVar var = fieldsMethodBlock.decl(typeRef, fldName, getPrimitiveValue(apiField.getVarType().getName()));
             fieldsMethodBlock.assign(JExpr.ref(apiField.getName()), var);
         }
         //fieldsMethodBlock.directStatement(" ");
 
     }
 
     @Override
     public void visit(APIMethod method) {
         // check if method is enabled in configuration.
         if (!isEnabled(methodSignature(method, visitingClass.getFullName()), WhitelistRule.RuleItem.EXTENDER)) {
             // if abstract method is skipped, extender has to be abstract,
             // because there is no implementation of the method.
             if(method.getModifiers().contains(APIModifier.ABSTRACT)) {
                 cls.mods().setAbstract(true);
             }
             return;
         }
 
         // Extender can't override final methods
         if (method.getModifiers().contains(APIModifier.FINAL)) {
             return;
         }
 
         // Extender can't override static methods
         if (method.getModifiers().contains(APIModifier.STATIC)) {
             return;
         }
 
         // only public or protected method can be tested by extender
         if (!(method.getModifiers().contains(APIModifier.PUBLIC)
                 || method.getModifiers().contains(APIModifier.PROTECTED))) {
             return;
         }
 
         // skip deprecated method is it is configured
         if (method.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             // if abstract method is skipped, extender has to be abstract,
             // because there is no implementation of the method.
             if(method.getModifiers().contains(APIModifier.ABSTRACT)) {
                 cls.mods().setAbstract(true);
             }
             return;
         }
 
         addOverridingMethod(method);
 
     }
 
     /**
      * Add new method to current generated class which overrides original method specified as parameter.
      * @param method
      */
     private void addOverridingMethod(APIMethod method) {
 
         // return type have to be public or protected class
         if (!checkTypeAccessModifier(APIModifier.PROTECTED, method.getReturnType(),method.getTypeParamsMap().keySet())) {
             if(method.getModifiers().contains(APIModifier.ABSTRACT)) {
                 cls.mods().setAbstract(true);
             }
             return;
         }
 
         // all methods params has to be public or protected classes
         for (APIMethodParameter paramType : method.getParameters()) {
 
             if (!checkTypeAccessModifier(APIModifier.PROTECTED,paramType.getType(), method.getTypeParamsMap().keySet())) {
                 if(method.getModifiers().contains(APIModifier.ABSTRACT)) {
                     cls.mods().setAbstract(true);
                 }
                 return;
             }
 
         }
 
         JClass extenderReturnType = getTypeRef(method.getReturnType(), method.getTypeParamsMap().keySet());
         // define new method
         JMethod mthd = cls.method(JMod.PUBLIC, extenderReturnType, method.getName());
 
         if (!method.getTypeParamsMap().isEmpty()) {
             for (String typeName : method.getTypeParamsMap().keySet()) {
                 JTypeVar type = mthd.generify(typeName);
                 for (APIType bound : method.getTypeParamsMap().get(typeName)) {
                     JClass typeBound = getTypeRef(bound, method.getTypeParamsMap().keySet());
                     if (!bound.equals(new APIType("java.lang.Object"))) {
                         type.bound(typeBound);
                     }
                 }
             }
         }
 
         // set body of the method. = return super.method(...);
         mthd.body()._throw(JExpr._new(cm.ref(UnsupportedOperationException.class)));
 
         // add params to method. New method has same params as overridden method.
         for (APIMethodParameter param : method.getParameters()) {
             boolean array = false;
             if (param.getType().isArray()) {
                 array = true;
             }
             JClass paramType = getTypeRef(param.getType(),method.getTypeParamsMap().keySet());
 
             if (array) {
                 paramType.array();
             }
             mthd.param(paramType, String.valueOf(param.getName()));
         }
 
         // add override annotation to the method.
         JAnnotationUse annotation = mthd.annotate(cm.ref(java.lang.Override.class));
 
         // add list of thrown methods. Must be same as list of original class
         for (String thrown : method.getThrown()) {
             mthd._throws(getTypeRef(thrown, method.getTypeParamsMap().keySet()));
         }
     }
 
     protected static String getParamArg(String className) {
         if (className.contains("<")) {
             return className.substring(className.indexOf('<') + 1, className.lastIndexOf('>')).trim();
         }
         return null;
     }
 
     /**
      * Checks if given type is public or protected. Type could be simple class (<code>java.util.List</code>) or complex type
      * (<code>java.util.Map<java.lang.String, java.lang.List<java.io.File>></code>). Result is true if every class
      * used in type definition is public or protected.
      *
      * @param type           Definition of the type
      * @param genericClasses List of the generics classes.
      * @return
      */
     protected boolean isTypePublicOrProtected(String type, Collection<String> genericClasses) {
         boolean result = true;
         // Split complex type to individual classes
         Set<String> classNames = getTypesList(type);
         // check public accessibility of every single class
         for (String className : classNames) {
             // check if it's generic class or wildcard
             if (!((genericClasses != null && genericClasses.contains(className)) || (className.equals("?")))) {
                 // if class is not generic, use isClassPublic method to determine if class is public
                 if (!isClassPublicOrProtected(className)) {
                     result = false;
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * Finds class with given name and checks if the class has public modifier.
      *
      * @param name full class name
      * @return true or false if class is public or not. If class is not found (it's not part of API), returns false
      */
     protected boolean isClassPublicOrProtected(String name) {
 
         if (visitingClass.getTypeParamsMap().keySet().contains(name)) {
             return true;
         }
 
         try {
             APIClass c = findClass(name);
             if (c.getModifiers().contains(APIModifier.PUBLIC)
                     || (c.getModifiers().contains(APIModifier.PROTECTED))) {
                 return true;
             } else {
                 return false;
             }
         } catch (ClassNotFoundException e) {
             try {
                 APIClass nc = visitingClass.getNestedClass(name);
                 return (nc.getModifiers().contains(APIModifier.PUBLIC)
                         || (nc.getModifiers().contains(APIModifier.PROTECTED)));
             } catch (ClassNotFoundException e1) {
                 System.err.println("isClassPublicOrProtected() - Class not found: " + name);
             }
         }
 
         return true;
     }
 
 }
