 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.model.JavacTypes;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
 import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.*;
 
 
 /**
  * Represents method.
  *
  * @author Jan Hýbl
  */
 public class APIMethod extends APIItem implements Comparable<APIMethod> {
 
     private List<APIMethodParameter> parameters;
     private String returnType;
     private List<String> thrown;
     private String annotationDefaultValue;
     private Map<String, String[]> typeParamsMap = new TreeMap<String, String[]>();
 
     public APIMethod(String name, List<Modifier> modifiers, List<String> params, String returnType, List<String> thrown) {
         this.name = name;
         this.kind = Kind.METHOD;
         this.modifiers = modifiers;
         parameters = new LinkedList<>();
         char pname = 'a';
         for (String ptype : params) {
             parameters.add(new APIMethodParameter(String.valueOf(pname++), ptype));
         }
         this.returnType = returnType;
         this.thrown = thrown;
     }
 
     public APIMethod(JCMethodDecl jcmd, JavacTypes types) {
 
         // constructor or method?
         if (jcmd.name.toString().equals("<init>")) {
             kind = Kind.CONSTRUCTOR;
         } else {
             kind = getKind(jcmd.getKind());
         }
 
         name = jcmd.name.toString();
 
         if (jcmd.typarams.size() > 0) {
 
             // gets all type params
             for (JCTree.JCTypeParameter par : jcmd.typarams) {
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
 
         modifiers = APIModifier.getModifiersSet(jcmd.getModifiers().getFlags());
 
         // cretate list of exceptions thrown by method
         thrown = new LinkedList<>();
         LinkedList<com.sun.tools.javac.code.Type> thrownTypes = new LinkedList<>();
         if (jcmd.getThrows() != null) {
             for (JCExpression e : jcmd.getThrows()) {
                 boolean alreadyThrown = false;
                 for (Iterator<com.sun.tools.javac.code.Type> it = thrownTypes.iterator(); it.hasNext(); ) {
                     com.sun.tools.javac.code.Type type = it.next();
                     if (types.isSubtype(e.type, type)) {
                         alreadyThrown = true;
                     } else if (types.isSubtype(type, e.type)) {
                         it.remove();
                     }
                 }
                 if (!alreadyThrown) {
                     thrownTypes.add(e.type);
                 }
             }
             for (com.sun.tools.javac.code.Type t : thrownTypes) {
                 thrown.add(t.getModelType().toString());
             }
         }
 
         // Constructor should return type fullclassname. Void method returns "void"
         // and other methods return full name of Class from jcmd.getReturnType()
         if (!kind.equals(Kind.CONSTRUCTOR)) {
             if (jcmd.getReturnType() == null) {
                 returnType = "void";
             } else {
                 returnType = jcmd.restype.type.toString();
             }
         } else {
             returnType = jcmd.sym.owner.toString();
         }
 
         // method parameters
         parameters = new LinkedList<>();
         for (JCVariableDecl jcvd : jcmd.getParameters()) {
             parameters.add(new APIMethodParameter(jcvd));
         }
 
         // default value of annotation params.
         if (jcmd.defaultValue != null) {
             annotationDefaultValue = jcmd.defaultValue.toString();
         }
     }
 
     public APIMethod(Method mth) {
 
         this.name = mth.getName();
         this.modifiers = APIModifier.getModifiersSet(mth.getModifiers());
         this.thrown = new LinkedList<String>();
 
         thrown = new LinkedList<>();
         LinkedList<Class> thrownTypes = new LinkedList<>();
         for (java.lang.reflect.Type excType : mth.getExceptionTypes()) {
             Class c = (Class) excType;
             boolean alreadyThrown = false;
             for (Iterator<Class> it = thrownTypes.iterator(); it.hasNext(); ) {
                 Class type = it.next();
                 if (type.isAssignableFrom(c)) {
                     alreadyThrown = true;
                 } else if (c.isAssignableFrom(type)) {
                     it.remove();
                 }
             }
             if (!alreadyThrown) {
                 thrownTypes.add(c);
             }
         }
         for (Class c : thrownTypes) {
             thrown.add(c.getName());
         }
 
         this.parameters = new LinkedList<APIMethodParameter>();
         char pname = 'a';
         for (Type t : mth.getGenericParameterTypes()) {
             this.parameters.add(new APIMethodParameter(String.valueOf(pname++), getTypeName(t)));
         }
        this.returnType = mth.getReturnType().getName();
        if (returnType.startsWith("[L")) { // if method returns array, scanner gets wrong return class name.
             // This fix this error
             returnType = returnType.substring(2, returnType.length() - 1) + "[]";
        }
 
         if (mth.getDefaultValue() != null) {
             this.annotationDefaultValue = mth.getDefaultValue().toString();
         }
         this.kind = Kind.METHOD;
     }
 
     public APIMethod(Constructor c, String fullClassName) {
         this.name = c.getDeclaringClass().getSimpleName();
 
         this.modifiers = APIModifier.getModifiersSet(c.getModifiers());
         this.thrown = new LinkedList<>();
         for (java.lang.reflect.Type excType : c.getExceptionTypes()) {
             this.thrown.add(excType.toString().substring(6));
         }
         this.parameters = new LinkedList<>();
         char pname = 'a';
 
 
         // if declaring class is inner class ( = nested non-static class), javac adds to costructor
         // parameter of outer class instance. But in generated code, original constructor without
         // this added parameter have to be use. So this added parameter should be filtered.
         boolean filterFirstParam = false;
         if (c.getDeclaringClass().isMemberClass()
                 && !java.lang.reflect.Modifier.isStatic(c.getDeclaringClass().getModifiers())) {
             filterFirstParam = true;
         }
 
         for (Type t : c.getGenericParameterTypes()) {
             if (!filterFirstParam) {
                 this.parameters.add(new APIMethodParameter(String.valueOf(pname++), getTypeName(t)));
             } else {
                 filterFirstParam = false;
             }
         }
         this.returnType = fullClassName;
         this.kind = Kind.CONSTRUCTOR;
     }
 
     /**
      * Return string representation of APIMethod.
      * Format is [Modifiers] [TypeParams] method [Name] [ReturnType] ([Parameteres])
      * Example: public static doSomething Object (int, double)
      *
      * @return
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
 
         for (Modifier m : modifiers) sb.append(m).append(' ');
 
         if (!kind.equals(Kind.CONSTRUCTOR)) {
             sb.append("method ");
         } else {
             sb.append("constructor ");
         }
 
         if (typeParamsMap.size() > 0) {
             sb.append('<');
             for (String key : typeParamsMap.keySet()) {
                 sb.append(key).append(' ');
                 for (String typeBound : typeParamsMap.get(key)) {
                     sb.append(typeBound).append(" & ");
                 }
                 sb.delete(sb.length() - 3, sb.length());
                 sb.append(", ");
             }
             sb.delete(sb.length() - 2, sb.length());
             sb.append("> ");
         }
 
         sb.append(returnType).append(' ');
 
         sb.append(name);
 
         sb.append('(');
         for (APIMethodParameter f : parameters) {
             sb.append(f.getType()).append(',');
         }
         if (parameters.size() > 0) {
             sb.deleteCharAt(sb.length() - 1);
         }
         sb.append(')');
 
         if (thrown != null && thrown.size() > 0) {
             sb.append(" throws");
             for (String ex : thrown) {
                 sb.append(" ").append(ex);
             }
         }
 
         if (annotationDefaultValue != null) {
             sb.append(" value: " + annotationDefaultValue);
         }
         return sb.toString();
     }
 
     public List<APIMethodParameter> getParameters() {
         return parameters;
     }
 
     public String getParametersString() {
         StringBuilder sb = new StringBuilder();
         for (APIMethodParameter s : parameters) {
             sb.append(s.getType()).append(',');
         }
         if (parameters.size() > 0) return sb.substring(0, sb.length() - 1);
         else return "";
     }
 
     // Constructor should hava return type null. Void method raturns "void"
     // and other methods retrun full name of class.
     public String getReturnType() {
         return returnType;
     }
 
     public List<String> getThrown() {
         return thrown;
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
         final APIMethod other = (APIMethod) obj;
         if (this.parameters != other.parameters && (this.parameters == null || !this.parameters.equals(other.parameters))) {
             return false;
         }
         if ((this.returnType == null) ? (other.returnType != null) : !this.returnType.equals(other.returnType)) {
             return false;
         }
         if (this.thrown != other.thrown && (this.thrown == null || !this.thrown.equals(other.thrown))) {
             return false;
         }
         if (!super.equals(obj)) return false;
         return true;
     }
 
     @Override
     public int compareTo(APIMethod t) {
         String a = this.getName() + this.getParametersString() + this.returnType;
         String b = t.getName() + t.getParametersString() + t.returnType;
         return a.compareTo(b);
     }
 
     public String getAnnotationDefaultValue() {
         return annotationDefaultValue;
     }
 
     public Map<String, String[]> getTypeParamsMap() {
         return typeParamsMap;
     }
 }
