 package cz.cvut.fit.hybljan2.apitestingcg.generator;
 
 import com.sun.codemodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.*;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.GeneratorConfiguration;
 import cz.cvut.fit.hybljan2.apitestingcg.configuration.model.WhitelistRule;
 
 import java.lang.annotation.ElementType;
 import java.util.Arrays;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jan Hýbl
  * Date: 20.2.12
  * Time: 11:22
  */
 public class AnnotationGenerator extends ClassGenerator {
 
     public AnnotationGenerator(GeneratorConfiguration configuration) {
         super(configuration);
     }
 
     @Override
     public void visit(APIClass apiClass) {
         // annotations tests can be generated only for annotations
         if (!apiClass.getType().equals(APIItem.Kind.ANNOTATION)) return;
 
         // check if this annotation is enabled in jobConfiguration.
         if (!isEnabled(apiClass.getFullName(), WhitelistRule.RuleItem.ANNOTATION)) return;
 
         // check if class is not deprecated. If it does and in job configuration
         // are deprecated items disabled, this class is skipped.
         if (apiClass.isDepreacated() && jobConfiguration.isSkipDeprecated()) {
             return;
         }
 
 
         // check if annotation has specified targets. If it hasn't, targets are all elements.
         if (apiClass.getAnnotationTargets() == null) {
             apiClass.setAnnotationTargets(Arrays.asList(ElementType.values()));
         }
 
         // only public classes can be tested.
         if (!apiClass.getModifiers().contains(APIModifier.Modifier.PUBLIC)) {
             return;
         }
 
         cls = null;
 
         // generate test class
         generateTestClass(apiClass, false);
 
         cls = null;
 
         // check if any annotation param has defined default value. If it has, it has to be tested.
         for (APIMethod annotationParam : apiClass.getMethods()) {
             if (annotationParam.getAnnotationDefaultValue() != null) {
                 generateTestClass(apiClass, true);
                 break;
             }
         }
 
 
     }
 
     private void initClass(String className) throws JClassAlreadyExistsException {
         if (cls == null) {
             cls = cm._class(currentPackageName + '.' + className);
         }
     }
 
     private void generateTestClass(APIClass apiClass, boolean setDefaultValues) {
         // declare new class
         try {
             String className = currentPackageName + '.';
             className += setDefaultValues
                     ? generateName(configuration.getAnnotationClassIdentifier(), apiClass.getName()) + "DV"
                     : generateName(configuration.getAnnotationClassIdentifier(), apiClass.getName());
             if (apiClass.getAnnotationTargets().contains(ElementType.TYPE)) {
                 initClass(className);
                 annotate(cls, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.FIELD)) {
                 initClass(className);
                JFieldVar fld = cls.field(JMod.NONE, cm.INT, "annotatedField" + counter++);
                 annotate(fld, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.LOCAL_VARIABLE)) {
                 initClass(className);
                 JMethod method = cls.method(JMod.NONE, cm.VOID, "localVarMethod");
                 JVar localVar = method.body().decl(cm.INT, "localVariable");
                 annotate(localVar, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.METHOD)) {
                 initClass(className);
                 JMethod method = cls.method(JMod.NONE, cm.VOID, "annotatedMethod");
                 annotate(method, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.PARAMETER)) {
                 initClass(className);
                 JMethod method = cls.method(JMod.NONE, cm.VOID, "parameterMethod");
                 JVar param = method.param(cm.INT, "param");
                 annotate(param, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.CONSTRUCTOR)) {
                 initClass(className);
                 JMethod method = cls.constructor(JMod.NONE);
                 annotate(method, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.ANNOTATION_TYPE)) {
                 String name = setDefaultValues ? "AnnotationTypeDV" : "AnnotationType";
                 JDefinedClass annotation = cm._class(currentPackageName + '.' + name, ClassType.ANNOTATION_TYPE_DECL);
                 annotate(annotation, apiClass, setDefaultValues);
             }
 
             if (apiClass.getAnnotationTargets().contains(ElementType.PACKAGE)) {
                 String packageName = setDefaultValues ? currentPackageName + ".annotatedPackageDV" : currentPackageName + ".annotatedPackage";
                 JPackage a = cm._package(packageName);
                 annotate(a, apiClass, setDefaultValues);
             }
 
         } catch (JClassAlreadyExistsException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private JAnnotationUse annotate(JAnnotatable item, APIClass annotation, boolean setDefaults) {
         JAnnotationUse result = item.annotate(getClassRef(annotation.getFullName()));
         for (APIMethod method : annotation.getMethods()) {
             if (setDefaults || method.getAnnotationDefaultValue() == null) {
                 try {
                     result.param(method.getName(), getAnotationParamValue(method.getReturnType()));
                 } catch (Exception e) {
                     System.err.println("Cant set param value." + e.getMessage());
                 }
             }
         }
         return result;
     }
 
     public JExpression getAnotationParamValue(String name) throws Exception {
         name = name.trim();
         if (name.equals("byte")) return JExpr.lit(0);
         if (name.equals("short")) return JExpr.lit(0);
         if (name.equals("int")) return JExpr.lit(0);
         if (name.equals("long")) return JExpr.lit(0);
         if (name.equals("float")) return JExpr.lit(0.0);
         if (name.equals("double")) return JExpr.lit(0.0);
         if (name.equals("boolean")) return JExpr.lit(false);
         if (name.equals("char")) return JExpr.lit('a');
         if (name.equals("java.lang.String")) return JExpr.lit("A");
         if (name.equals("java.lang.Class")) return JExpr.dotclass(getClassRef("java.io.File"));
         if (name.endsWith("]")) {
             String arrayType = name.substring(0, name.indexOf("["));
             return JExpr.newArray(getClassRef(arrayType)).add(getAnotationParamValue(arrayType));
         }
 
         // generic class
         int idx = name.indexOf('<');
         if (name.startsWith("java.lang.Class") && idx >= 0) {
             String typeParam = name.substring(idx + 1, name.lastIndexOf('>'));
             return getAnotationParamValue(typeParam);
         }
 
         idx = name.indexOf("extends");
         if (idx >= 0) {
             return getAnotationParamValue(name.substring(idx + 8));
         }
 
         APIClass paramType = findClass(name);
         if (paramType.getType().equals(APIItem.Kind.ENUM)) {
             // visit all fields
             for (APIField field : paramType.getFields()) {
                 if (field.getVarType().equals(paramType.getFullName())) { // test if field is enum field or just variable
                     return getClassRef(paramType.getFullName()).staticRef(field.getName());
                 }
             }
         } else {
             return JExpr.dotclass(getClassRef(paramType.getFullName()));
         }
         throw new Exception("Unknown annotation parameter type: " + name);
     }
 
     @Override
     public void visit(APIField apiField) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void visit(APIMethod method) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
