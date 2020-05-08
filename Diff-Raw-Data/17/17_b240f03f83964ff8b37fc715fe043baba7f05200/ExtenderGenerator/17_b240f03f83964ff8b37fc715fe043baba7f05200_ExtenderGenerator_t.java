 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import com.sun.codemodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
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
             if (visitingClass.getExtending() != null) {
                 implementAbstractMethods(visitingClass.getExtending().getName());
             }
             for (APIType type : visitingClass.getImplementing()) {
                 implementAbstractMethods(type.getName());
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
 
     private void implementAbstractMethods(String className) {
         try {
             APIClass cls = findClass(className);
             for (APIMethod mth : cls.getMethods()) {
                if (mth.getModifiers().contains(APIModifier.ABSTRACT)
                        && !visitingClass.getMethods().contains(mth)) {
                     // all generic types in overridden method has to be replaced with their bounds.
                     // Type params has to be deleted.
                     mth.setReturnType(simplifyType(mth.getReturnType(),cls.getTypeParamsMap(),mth.getTypeParamsMap()));
                     for (APIMethodParameter p : mth.getParameters()) {
                         p.setType(simplifyType(p.getType(),cls.getTypeParamsMap(),mth.getTypeParamsMap()));
                     }
                     mth.getTypeParamsMap().clear();
                     addOverridingMethod(mth);
                 }
             }
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     private APIType simplifyType(APIType type, Map<String, APIType[]> clsGenerics, Map<String,APIType[]> mtdGenerics) {
         type.setBound(APIType.BoundType.NULL);
         type.getTypeArgs().clear();
         APIType[] genericTypes = clsGenerics.get(type.getName());
         if(genericTypes != null) {
             if(genericTypes.length > 0) {
                 type.setName(genericTypes[0].getName());
             } else {
                 type.setName("java.lang.Object");
             }
         } else {
             genericTypes = mtdGenerics.get(type.getName());
             if(genericTypes != null) {
                 if(genericTypes.length > 0) {
                     type.setName(genericTypes[0].getName());
                 } else {
                     type.setName("java.lang.Object");
                 }
             }
         }
         return type;
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
 
         if (method.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
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
             return;
         }
 
         // all methods params has to be public or protected classes
         for (APIMethodParameter paramType : method.getParameters()) {
 
             if (!checkTypeAccessModifier(APIModifier.PROTECTED,paramType.getType(), method.getTypeParamsMap().keySet())) {
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
