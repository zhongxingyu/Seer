 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCClassDecl;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 
 import java.lang.annotation.Annotation;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Target;
 import java.lang.reflect.*;
 import java.util.*;
 
 /**
  * Represent java class. Store information about it. Contains list of class
  * methods.
  *
  * @author hohy
  */
 public class APIClass extends APIItem implements Comparable<APIClass> {
 
     private SortedSet<APIMethod> constructors;
     private SortedSet<APIMethod> methods;
     private SortedSet<APIField> fields;
     /**
      * List of classes that are nested in current class.
      */
     private SortedSet<APIClass> nestedClasses = new TreeSet<>();
 
     /**
      * Indicates that the class is nested in other class or not.
      */
     private boolean nested = false;
     private String extending;
     private List<String> implementing = new LinkedList<>();
     /**
      * Full name of class (contains package name) - expample: java.util.Set
      */
     private String fullName;
 
     /**
      * Maps type variables names to names of their bounded types.
      * Example:
      * T extends Number --> {java.lang.Number}
      * U                --> {java.lang.Object}
      * U extends A & B  --> {A, B}
      */
     private Map<String, String[]> typeParamsMap = new TreeMap<String, String[]>();
     private List<ElementType> annotationTargets;
 
     /**
      * Basic constructor. Can be used for testing.
      *
      * @param name
      */
     public APIClass(String name) {
         if (name.contains(".")) this.name = name.substring(name.lastIndexOf('.') + 1);
         else this.name = name;
         this.fullName = name;
         methods = new TreeSet<APIMethod>();
         constructors = new TreeSet<APIMethod>();
         fields = new TreeSet<APIField>();
         kind = Kind.CLASS;
         modifiers = new LinkedList<Modifier>();
         modifiers.add(Modifier.PUBLIC);
     }
 
     /**
      * Constructor creates new instance of class.
      * Used by source scanner.
      *
      * @param jccd Node in AST of API that represents class
      */
     public APIClass(JCClassDecl jccd) {
         this.name = jccd.name.toString();
         // is class generic?
         if (jccd.typarams.size() > 0) {
 
             // gets all type params
             for (JCTree.JCTypeParameter par : jccd.typarams) {
                 String typeName = par.getName().toString();
 
                 // and theirs bounds
                 List<String> typeBounds = new ArrayList<String>();
                 for (JCExpression typeBound : par.getBounds()) {
                     typeBounds.add(typeBound.type.toString());
                 }
 
                 // if type bound is not specified, set default (java.lang.Object)
                 if (typeBounds.isEmpty()) {
                     typeBounds.add("java.lang.Object");
                 }
 
                 // puts result to typeParams map
                 typeParamsMap.put(typeName, typeBounds.toArray(new String[0]));
             }
         }
 
         this.fullName = jccd.type.tsym.toString();
 
         this.methods = new TreeSet<APIMethod>();
         this.constructors = new TreeSet<APIMethod>();
         this.kind = getKind(jccd.getKind());
         this.modifiers = APIModifier.getModifiersSet(jccd.mods.getFlags());
         if (this.kind == Kind.ENUM) {
             modifiers.add(Modifier.FINAL);
         }
         this.fields = new TreeSet<APIField>();
         if (jccd.getExtendsClause() != null) {
             this.extending = jccd.extending.type.tsym.getQualifiedName().toString();
         }
         if (jccd.getImplementsClause() != null) {
             this.implementing = new LinkedList<String>();
             for (JCExpression e : jccd.getImplementsClause()) {
                 this.implementing.add(e.type.tsym.getQualifiedName().toString());
             }
 
         }
     }
 
     /**
      * Constructor creates new instance of class.
      * Used by bytecode scanner.
      *
      * @param cls Node in AST of API that represents class
      */
     public APIClass(Class cls) {
         name = cls.getSimpleName();
         fullName = cls.getName();
 
         if (cls.isMemberClass()) {
             fullName = cls.getEnclosingClass().getName() + '.' + name;
         }
 
         constructors = new TreeSet<>();
         for (Constructor constr : cls.getDeclaredConstructors()) {
             APIMethod apiconstr = new APIMethod(constr, fullName);
             if (apiconstr.getModifiers().contains(Modifier.PUBLIC)
                     || apiconstr.getModifiers().contains(Modifier.PROTECTED))
                 this.constructors.add(apiconstr);
         }
 
         methods = new TreeSet<>();
         for (Method mth : cls.getDeclaredMethods()) {
             if (!mth.isBridge() && !mth.isSynthetic()) {
                 APIMethod apimth = new APIMethod(mth);
                 if (apimth.getModifiers().contains(Modifier.PUBLIC)
                         || apimth.getModifiers().contains(Modifier.PROTECTED)) {
                     methods.add(apimth);
                 }
             }
         }
 
         modifiers = APIModifier.getModifiersSet(cls.getModifiers());
         fields = new TreeSet<>();
         for (Field f : cls.getDeclaredFields()) {
             APIField apifield = new APIField(f);
             if (apifield.getModifiers().contains(Modifier.PUBLIC)
                     || apifield.getModifiers().contains(Modifier.PROTECTED))
                 fields.add(new APIField(f));
         }
 
         kind = getKind(cls);
         if (kind == Kind.ANNOTATION) {
             System.out.println(cls);
             for (Annotation a : cls.getAnnotations()) {
                 System.out.println(a.annotationType());
 
                 if (a instanceof java.lang.annotation.Target) {
                     annotationTargets = new LinkedList<>();
                     Target annotationTarget = (Target) a;
                     for (ElementType elementType : annotationTarget.value()) {
                         annotationTargets.add(elementType);
                     }
                 }
             }
         }
 
         // Check, if class has some superclass (other than Object or Enum)         
         if (cls.getSuperclass() != null
                 && !cls.getSuperclass().equals(java.lang.Object.class)
                 && !cls.getSuperclass().equals(java.lang.Enum.class)) {
             extending = getTypeName(cls.getSuperclass());//.getName();
         }
 
         implementing = new LinkedList<>();
         for (Class implementedInterface : cls.getInterfaces()) {
            implementing.add(getTypeName(implementedInterface));
         }
 
         // construct typeparams (generics) map
         for (TypeVariable tp : cls.getTypeParameters()) {
             String typeName = tp.getName();
             ArrayList<String> typeBounds = new ArrayList<>();
             for (Type type : tp.getBounds()) {
                 typeBounds.add(getTypeName(type));
             }
             typeParamsMap.put(typeName, typeBounds.toArray(new String[0]));
         }
 
         // add nested classes
         for (Class nestedClass : cls.getDeclaredClasses()) {
             nestedClasses.add(new APIClass(nestedClass));
         }
 
         // set nested flag if this class is nested
         if (cls.isMemberClass()) {
             nested = true;
         }
     }
 
     public void addMethod(APIMethod method) {
         methods.add(method);
     }
 
     public void addField(APIField field) {
         fields.add(field);
     }
 
     public void addConstructor(APIMethod constructor) {
         constructors.add(constructor);
     }
 
     public void addDefaultConstructor() {
         List<Modifier> publicmodifier = new LinkedList<Modifier>();
         publicmodifier.add(Modifier.PUBLIC);
         LinkedList<String> params = new LinkedList<String>();
         List<String> thrown = new LinkedList<String>();
         APIMethod constr = new APIMethod(name, publicmodifier, params, fullName, thrown);
         constr.kind = Kind.CONSTRUCTOR;
         this.constructors.add(constr);
     }
 
     public void deleteDefaultConstructor() {
         List<Modifier> publicmodifier = new LinkedList<Modifier>();
         publicmodifier.add(Modifier.PUBLIC);
         LinkedList<String> params = new LinkedList<String>();
         List<String> thrown = new LinkedList<String>();
         APIMethod constr = new APIMethod(fullName, publicmodifier, params, null, thrown);
         constr.kind = Kind.CONSTRUCTOR;
         this.constructors.remove(constr);
     }
 
     /**
      * Converts this class to String.
      * String format: [modifiers] [kind] [fullName] extends [extending] implements [implementing]\n
      * [fields] [methods]
      *
      * @return String representation of this class
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(APIModifier.modifiersToString(modifiers));
         if (nested) sb.append("nested ");
         sb.append(kind.toString().toLowerCase()).append(' ');
         sb.append(fullName);
 
         if (typeParamsMap.size() > 0) {
             sb.append(" <");
             for (String key : typeParamsMap.keySet()) {
                 sb.append(key).append(' ');
                 for (String typeBound : typeParamsMap.get(key)) {
                     sb.append(typeBound).append(" & ");
                 }
                 sb.delete(sb.length() - 3, sb.length());
                 sb.append(", ");
             }
             sb.delete(sb.length() - 2, sb.length());
             sb.append(">");
         }
 
         if (extending != null) {
             sb.append(" extends ").append(extending);
         }
         if (implementing != null && implementing.size() > 0) {
             sb.append(" implements");
             for (String i : implementing) {
                 sb.append(' ').append(i);
             }
         }
         if (annotationTargets != null && annotationTargets.size() > 0) {
             for (ElementType et : annotationTargets) {
                 sb.append("\n @AnnotationTarget ").append(et);
             }
         }
         if (fields != null) {
             for (APIField f : fields) {
                 sb.append("\n ").append(f.toString());
             }
         }
         if (constructors != null) {
             for (APIMethod c : constructors) {
                 sb.append("\n ").append(c.toString());
             }
         }
         if (methods != null) {
             for (APIMethod m : methods) {
                 sb.append("\n ").append(m.toString());
             }
         }
 
         if (nestedClasses != null) {
             for (APIClass nestedClass : nestedClasses) {
                 sb.append("\n\n").append(nestedClass);
             }
         }
         return sb.toString();
     }
 
     public Set<APIField> getFields() {
         return fields;
     }
 
     /**
      * Return list of method of the class. Return empty list, if class has no methods.
      *
      * @return
      */
     public Set<APIMethod> getMethods() {
         return methods;
     }
 
     /**
      * Return name of class, that class is extending. Return null, if class extending
      * java.lang.Object (default situation).
      *
      * @return
      */
     public String getExtending() {
         return extending;
     }
 
     public void setExtending(String extending) {
         this.extending = extending;
     }
 
     public Map<String, String[]> getTypeParamsMap() {
         return typeParamsMap;
     }
 
     /**
      * Return list of names of interfaces, that class is implmenting. Return empty list,
      * if class implements no interface.
      *
      * @return
      */
     public List<String> getImplementing() {
         return implementing;
     }
 
     /**
      * Return full name of class - with package name.
      * Example: java.io.File
      *
      * @return
      */
     public String getFullName() {
         return fullName;
     }
 
     /**
      * Parse name of package from full name of class.
      *
      * @return name of package
      */
     public String getPackageName() {
         int lastDotPosition = fullName.lastIndexOf('.');
         if (lastDotPosition != -1) {
             return fullName.substring(0, lastDotPosition);
         } else { // full name doesn't contain dot character. Class isn't in any package.
             return "";
         }
     }
 
     private Kind getKind(Class cls) {
         if (cls.isAnnotation()) return Kind.ANNOTATION;
         if (cls.isInterface()) return Kind.INTERFACE;
         if (cls.isEnum()) return Kind.ENUM;
         return Kind.CLASS;
     }
 
     public SortedSet<APIMethod> getConstructors() {
         return constructors;
     }
 
     @Override
     public void accept(IAPIVisitor visitor) {
         visitor.visit(this);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final APIClass other = (APIClass) obj;
         if (this.methods != other.methods && (this.methods == null || !this.methods.equals(other.methods))) {
             return false;
         }
         if (this.constructors != other.constructors && (this.constructors == null || !this.constructors.equals(other.constructors))) {
             return false;
         }
         if (!this.constructors.equals(other.constructors)) return false;
         if (this.fields != other.fields && (this.fields == null || !this.fields.equals(other.fields))) {
             return false;
         }
         if ((this.extending == null) ? (other.extending != null) : !this.extending.equals(other.extending)) {
             return false;
         }
         if (this.implementing != other.implementing && (this.implementing == null || !this.implementing.equals(other.implementing))) {
             return false;
         }
         if ((this.fullName == null) ? (other.fullName != null) : !this.fullName.equals(other.fullName)) {
             return false;
         }
         if (this.nestedClasses != other.nestedClasses && (this.nestedClasses == null || !this.nestedClasses.equals(other.nestedClasses))) {
             return false;
         }
         if (this.annotationTargets != other.annotationTargets && (this.annotationTargets == null || !this.annotationTargets.equals(other.annotationTargets))) {
             return false;
         }
         if (this.typeParamsMap != other.typeParamsMap && (this.typeParamsMap == null || !this.typeParamsMap.equals(other.typeParamsMap))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int compareTo(APIClass t) {
         return this.getName().compareTo(t.getName());
     }
 
     void setExtends(String string) {
         this.extending = string;
     }
 
     void setImplementing(List<String> implement) {
         this.implementing = implement;
     }
 
     public List<ElementType> getAnnotationTargets() {
         return annotationTargets;
     }
 
     public void setAnnotationTargets(List<ElementType> annotationTargets) {
         this.annotationTargets = annotationTargets;
     }
 
     public SortedSet<APIClass> getNestedClasses() {
         return nestedClasses;
     }
 
     public boolean isNested() {
         return nested;
     }
 
     public void setNested(boolean nested) {
         this.nested = nested;
     }
 
     public static ElementType parseAnnotationTarget(String name) throws Exception {
         if (name.equals("ElementType.ANNOTATION_TYPE")) return ElementType.ANNOTATION_TYPE;
         if (name.equals("ElementType.CONSTRUCTOR")) return ElementType.CONSTRUCTOR;
         if (name.equals("ElementType.TYPE")) return ElementType.TYPE;
         if (name.equals("ElementType.FIELD")) return ElementType.FIELD;
         if (name.equals("ElementType.LOCAL_VARIABLE")) return ElementType.LOCAL_VARIABLE;
         if (name.equals("ElementType.METHOD")) return ElementType.METHOD;
         if (name.equals("ElementType.PACKAGE")) return ElementType.PACKAGE;
         if (name.equals("ElementType.PARAMETER")) return ElementType.PARAMETER;
         throw new Exception("Unknown annotation target \"" + name + "\".");
     }
 
     public void addNestedClass(APIClass nestedClass) {
         nestedClasses.add(nestedClass);
     }
 }
