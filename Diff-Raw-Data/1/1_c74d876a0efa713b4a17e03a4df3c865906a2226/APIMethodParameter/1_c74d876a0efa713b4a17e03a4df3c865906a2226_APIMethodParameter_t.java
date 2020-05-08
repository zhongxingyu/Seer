 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import com.sun.tools.javac.tree.JCTree;
 
 import java.lang.reflect.Type;
 
 /**
  * Class represents one parameter of method or constructor.
  */
 public class APIMethodParameter {
 
     /**
      * Name of parameter
      */
     private String name;
 
     /**
      * Full name of class.
      */
     private APIType type;
 
     public APIMethodParameter(String name, APIType type) {
         this.name = name;
         this.type = type;
     }
     
     public APIMethodParameter(String name, String type) {
         this.name = name;
         this.type = new APIType(type);
     }
 
     public APIMethodParameter(String name, JCTree.JCVariableDecl jcvd) {
         if (name == null) {
             this.name = jcvd.name.toString();
         } else {
             this.name = name;
         }
         type = new APIType(jcvd.type);
     }
 
     public APIMethodParameter(String name, Type type) {
         this.name = name;
         this.type = new APIType(type);
     }
 
     public String getName() {
         return name;
     }
 
     public APIType getType() {
         return type;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof APIMethodParameter)) return false;
 
         APIMethodParameter that = (APIMethodParameter) o;
 
         if (type != null ? !type.equals(that.type) : that.type != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return type != null ? type.hashCode() : 0;
     }
 
     @Override
     public String toString() {
         return type + " " + name;
     }
 
     public boolean isPrimitive() {
        if(getType().isArray()) return false;
         switch (getType().getName()) {
             case "byte":
             case "short":
             case "int":
             case "long":
             case "float":
             case "double":
             case "boolean":
             case "char":
                 return true;
             default:
                 return false;
         }
 
     }
 
     public void setType(APIType type) {
         this.type = type;
     }
 }
