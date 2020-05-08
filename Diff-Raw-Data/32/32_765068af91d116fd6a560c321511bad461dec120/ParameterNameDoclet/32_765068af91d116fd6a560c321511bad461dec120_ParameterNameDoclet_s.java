 package org.seasar.diigu;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.ConstructorDoc;
 import com.sun.javadoc.Doclet;
 import com.sun.javadoc.MethodDoc;
 import com.sun.javadoc.Parameter;
 import com.sun.javadoc.RootDoc;
 import com.sun.javadoc.Type;
 
 public class ParameterNameDoclet extends Doclet {
     protected static final Set PRIMITIVE_TYPES = new HashSet();
     static {
         PRIMITIVE_TYPES.add("boolean");
         PRIMITIVE_TYPES.add("byte");
         PRIMITIVE_TYPES.add("short");
         PRIMITIVE_TYPES.add("int");
         PRIMITIVE_TYPES.add("long");
         PRIMITIVE_TYPES.add("float");
         PRIMITIVE_TYPES.add("double");
         PRIMITIVE_TYPES.add("char");
         PRIMITIVE_TYPES.add("void");
     }
 
     public ParameterNameDoclet() {
     }
 
     public static boolean start(final RootDoc rootdoc) {
         final ClassDoc classDocs[] = rootdoc.classes();
         for (int i = 0; i < classDocs.length; i++) {
             final ClassDoc classDoc = classDocs[i];
             enhanceClassFile(classDoc);
         }
         return true;
     }
 
     protected static void enhanceClassFile(final ClassDoc classDoc) {
         final ParameterNameEnhancer enhancer = new ParameterNameEnhancer(
                 toBinaryName(classDoc));
         boolean dirty = false;
         dirty |= enhanceConstructor(enhancer, classDoc);
         dirty |= enhanceMethod(enhancer, classDoc);
         if (dirty) {
             enhancer.save();
         }
     }
 
     protected static boolean enhanceConstructor(
             final ParameterNameEnhancer enhancer, final ClassDoc classDoc) {
         boolean dirty = false;
         final ConstructorDoc[] constructorDocs = classDoc.constructors();
         for (int i = 0; i < constructorDocs.length; ++i) {
            dirty |= enhanceConstructor(enhancer, constructorDocs[i]);
         }
         return dirty;
     }
 
     protected static boolean enhanceConstructor(
             final ParameterNameEnhancer enhancer,
             final ConstructorDoc constructorDoc) {
         final Parameter[] parameters = constructorDoc.parameters();
         final int numParameters = parameters.length;
         if (numParameters == 0) {
             return false;
         }
 
         final String[] parameterTypes = new String[numParameters];
         final String[] parameterNames = new String[numParameters];
         for (int i = 0; i < numParameters; ++i) {
             final Parameter parameter = parameters[i];
             parameterTypes[i] = toBinaryName(parameter.type());
             parameterNames[i] = parameter.name();
         }
         enhancer.setConstructorParameterNames(parameterTypes, parameterNames);
         return true;
     }
 
     protected static boolean enhanceMethod(
             final ParameterNameEnhancer enhancer, final ClassDoc classDoc) {
         boolean dirty = false;
         final MethodDoc[] methodDocs = classDoc.methods();
         for (int i = 0; i < methodDocs.length; ++i) {
             dirty |= enhanceMethod(enhancer, methodDocs[i]);
         }
         return dirty;
     }
 
     protected static boolean enhanceMethod(
             final ParameterNameEnhancer enhancer, final MethodDoc methodDoc) {
         final Parameter[] parameters = methodDoc.parameters();
         final int numParameters = parameters.length;
         if (numParameters == 0) {
             return false;
         }
 
         final String[] parameterTypes = new String[numParameters];
         final String[] parameterNames = new String[numParameters];
         for (int i = 0; i < numParameters; ++i) {
             final Parameter parameter = parameters[i];
             final Type type = parameter.type();
             parameterTypes[i] = toBinaryName(type);
             parameterNames[i] = parameter.name();
         }
         final String methodName = methodDoc.name();
         enhancer.setMethodParameterNames(methodName, parameterTypes,
                 parameterNames);
         return true;
     }
 
     protected static String toBinaryName(final Type type) {
         final String dimension = type.dimension();
         final String qualifiedTypeName = type.qualifiedTypeName();
         if (PRIMITIVE_TYPES.contains(qualifiedTypeName)) {
             return qualifiedTypeName + dimension;
         }
 
         final String typeName = type.typeName();
         assert qualifiedTypeName.endsWith(typeName);
         if (typeName.indexOf('.') == -1) {
             return qualifiedTypeName + dimension;
         }
 
         final String packageName = qualifiedTypeName.substring(0,
                 qualifiedTypeName.length() - typeName.length());
         return packageName + typeName.replace('.', '$');
     }
 }
