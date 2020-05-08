 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.tree.JCTree.JCClassDecl;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * Represent java class. Store information about it. Contains list of class
  * methods.
  * @author hohy
  */
 public class APIClass extends APIItem implements Comparable<APIClass> {
 
     private SortedSet<APIMethod> constructors;
     private SortedSet<APIMethod> methods;
     private SortedSet<APIField> fields;
     private String extending;
     private List<String> implementing = new LinkedList<String>();
     // Full name of class (contains package name) - expample: java.util.Set
     private String fullName;
 
     public APIClass(String name) {
         if(name.contains(".")) this.name = name.substring(name.lastIndexOf('.')+1);
         else this.name = name;
         this.fullName = name;
         methods = new TreeSet<APIMethod>();
         constructors = new TreeSet<APIMethod>();
         fields = new TreeSet<APIField>();
         kind = Kind.CLASS;
         modifiers = new LinkedList<Modifier>();
         modifiers.add(Modifier.PUBLIC);
     }
 
     public APIClass(JCClassDecl jccd, String packageName, Map<String, String> importsMap) {
         this.name = jccd.name.toString();
         this.fullName = packageName + '.' + jccd.name.toString();
         this.methods = new TreeSet<APIMethod>(); 
        this.constructors = new TreeSet<APIMethod>();        
         this.modifiers = APIModifier.getModifiersSet(jccd.mods.getFlags());
         this.fields = new TreeSet<APIField>();
         this.kind = getKind(jccd.getKind());
        //if(this.kind == Kind.CLASS) addDefaultConstructor();
         if(jccd.getExtendsClause() != null) this.extending = findFullClassName(jccd.getExtendsClause().getTree().toString(), importsMap);
         if(jccd.getImplementsClause() != null) {
             this.implementing = new LinkedList<String>();
             for(JCExpression e : jccd.getImplementsClause()) this.implementing.add(findFullClassName(e.toString(), importsMap));
         }
     }
     
     public APIClass(Class cls) {
         this.name = cls.getSimpleName();
         this.fullName = cls.getName();
         this.constructors = new TreeSet<APIMethod>();
         for(Constructor constr : cls.getDeclaredConstructors()) {
             APIMethod apiconstr = new APIMethod(constr);
             if(apiconstr.getModifiers().contains(Modifier.PUBLIC) 
                     || apiconstr.getModifiers().contains(Modifier.PROTECTED)) 
                 this.constructors.add(apiconstr);
         }
         this.methods = new TreeSet<APIMethod>();
         for(Method mth : cls.getDeclaredMethods()) {
             APIMethod apimth = new APIMethod(mth);
             if(apimth.getModifiers().contains(Modifier.PUBLIC) 
                     || apimth.getModifiers().contains(Modifier.PROTECTED)) 
                 this.methods.add(apimth);
         }
         this.modifiers = APIModifier.getModifiersSet(cls.getModifiers());
         this.fields = new TreeSet<APIField>();
         for(Field f : cls.getDeclaredFields()) {
             APIField apifield = new APIField(f);
             if(apifield.getModifiers().contains(Modifier.PUBLIC) 
                     || apifield.getModifiers().contains(Modifier.PROTECTED))
                 this.fields.add(new APIField(f));
         }
         this.kind = getKind(cls);
         // Check, if class has some superclass (other than Object) 
         if(cls.getSuperclass() != null && !cls.getSuperclass().getSimpleName().equals("Object")) this.extending = cls.getSuperclass().getName();
         this.implementing = new LinkedList<String>();
         for(Class intfc : cls.getInterfaces()) {
             this.implementing.add(intfc.getSimpleName());
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
         SortedSet<String> thrown = new TreeSet<String>();
         APIMethod constr = new APIMethod(fullName, publicmodifier, params, null, thrown);
         constr.kind = Kind.CONSTRUCTOR;
         this.constructors.add(constr);
     }
     
     public void deleteDefaultConstructor() {
         List<Modifier> publicmodifier = new LinkedList<Modifier>();
         publicmodifier.add(Modifier.PUBLIC);
         LinkedList<String> params = new LinkedList<String>();
         SortedSet<String> thrown = new TreeSet<String>();
         APIMethod constr = new APIMethod(fullName, publicmodifier, params, null, thrown);
         constr.kind = Kind.CONSTRUCTOR;
         this.constructors.remove(constr);
     }
     
     /**
      * Converts this class to String.
      * String format: [modifiers] [kind] [fullName] extends [extending] implements [implementing]\n
      * [fields] [methods]
      * @return  String representation of this class 
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(APIModifier.modifiersToString(modifiers));
         sb.append(kind.toString().toLowerCase()).append(' ');
         sb.append(fullName);
         if(extending != null) sb.append(" extends ").append(extending);
         if(implementing != null && implementing.size() > 0) {
             sb.append(" implements");
             for(String i : implementing) sb.append(' ').append(i);
         }
         if(fields != null) for(APIField f : fields) sb.append("\n ").append(f.toString());
         if(constructors != null) for(APIMethod c : constructors) sb.append("\n ").append(c.toString());
         if(methods != null) for(APIMethod m : methods) sb.append("\n ").append(m.toString());
         return sb.toString();
     }
 
     public Set<APIField> getFields() {
         return fields;
     }
 
     /**
      * Return list of method of the class. Return empty list, if class has no methods.
      * @return 
      */
     public Set<APIMethod> getMethods() {
         return methods;
     }
 
     /**
      * Return name of class, that class is extending. Return null, if class extending
      * java.lang.Object (default situation).
      * @return 
      */
     public String getExtending() {
         return extending;
     }
 
     /**
      * Return list of names of interfaces, that class is implmenting. Return empty list,
      * if class implements no interface.
      * @return 
      */
     public List<String> getImplementing() {
         return implementing;
     }
 
     /**
      * Return full name of class - with package name.
      * Example: java.io.File
      * @return 
      */
     public String getFullName() {
         return fullName;
     }
 
     private Kind getKind(Class cls) {
         if(cls.isAnnotation()) return Kind.ANNOTATION;
         if(cls.isInterface()) return Kind.INTERFACE;
         if(cls.isEnum()) return Kind.ENUM;        
         return Kind.CLASS;
     }
 
     public SortedSet<APIMethod> getConstructors() {
         return constructors;
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
         if(!this.constructors.equals(other.constructors)) return false;
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
     
     
 }
