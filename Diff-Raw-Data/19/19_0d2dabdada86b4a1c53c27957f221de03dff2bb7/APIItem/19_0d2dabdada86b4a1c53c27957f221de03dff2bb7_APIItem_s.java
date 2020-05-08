 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import cz.cvut.fit.hybljan2.apitestingcg.apimodel.APIModifier.Modifier;
 
 import java.lang.reflect.Type;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Jan Hýbl
  */
 public abstract class APIItem {
     protected String name;
     protected List<Modifier> modifiers;
 
     protected Kind kind;
 
     /**
      * Method used in visitor design pattern.
      *
      * @param visitor
      */
     public abstract void accept(IAPIVisitor visitor);
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Modifier> getModifiers() {
         return modifiers;
     }
 
     public void setModifiers(List<Modifier> modifiers) {
         this.modifiers = modifiers;
     }
 
     public Kind getType() {
         return kind;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final APIItem other = (APIItem) obj;
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
 
         //public static is equal to static public, so create set from list and compare it
         Set modsA = new HashSet(modifiers);
         Set modsB = new HashSet(other.modifiers);
         if (!modsA.equals(modsB)) return false;
 
         if (this.kind != other.kind) {
             return false;
         }
         return true;
     }
 
     protected Kind getKind(com.sun.tools.javac.tree.JCTree.JCClassDecl.Kind kind) {
         switch (kind) {
             case CLASS:
                 return Kind.CLASS;
             case METHOD:
                 return Kind.METHOD;
             case INTERFACE:
                 return Kind.INTERFACE;
             case ANNOTATION_TYPE:
                 return Kind.ANNOTATION;
             case ENUM:
                 return Kind.ENUM;
             case VARIABLE:
                 return Kind.VARIABLE;
         }
         return null;
     }
 
     protected String getTypeName(Type t) {
         String rawName = t.toString();
         if (t instanceof Class) {
             Class c = (Class) t;
             rawName = c.getName();
             if (c.isArray()) {
                 rawName += "[]";
             }
             return rawName;
         }
         if (rawName.contains("class")) rawName = rawName.substring(6);
         if (rawName.contains("interface")) rawName = rawName.substring(10);
 
         if (rawName.startsWith("[L")) { // if method returns array, scanner gets wrong return class name.
             // This fix this error
             return rawName.substring(2, rawName.length() - 1) + "[]";
         } else {
             return rawName;
         }
     }
 
     public enum Kind {
         CLASS, METHOD, INTERFACE, ANNOTATION, ENUM, VARIABLE, PACKAGE, CONSTRUCTOR
     }
 
 }
