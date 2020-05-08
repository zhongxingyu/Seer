 package cz.cvut.fit.hybljan2.apitestingcg.apimodel;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author Jan Hýbl
  */
 public class APIModifier {
 
     public enum Modifier {
         PUBLIC, PROTECTED, PRIVATE, ABSTRACT, FINAL, NATIVE, STRICTFP, STATIC,
         SYNCHRONIZED, TRANSIENT, VOLATILE;
 
         @Override
         public String toString() {
             return super.toString().toLowerCase();
         }
     }
 
     /**
      * Method that translate java.lang.reflect.Modifier to our APIModifier.
      *
      * @param modifiers
      * @return
      */
     public static List<Modifier> getModifiersSet(int modifiers) {
         List<Modifier> result = new LinkedList<Modifier>();
         if (java.lang.reflect.Modifier.isAbstract(modifiers)) result.add(Modifier.ABSTRACT);
         if (java.lang.reflect.Modifier.isNative(modifiers)) result.add(Modifier.NATIVE);
         if (java.lang.reflect.Modifier.isPrivate(modifiers)) result.add(Modifier.PRIVATE);
         if (java.lang.reflect.Modifier.isProtected(modifiers)) result.add(Modifier.PROTECTED);
         if (java.lang.reflect.Modifier.isPublic(modifiers)) result.add(Modifier.PUBLIC);
         if (java.lang.reflect.Modifier.isStatic(modifiers)) result.add(Modifier.STATIC);
         if (java.lang.reflect.Modifier.isFinal(modifiers)) result.add(Modifier.FINAL);
         if (java.lang.reflect.Modifier.isStrict(modifiers)) result.add(Modifier.STRICTFP);
         if (java.lang.reflect.Modifier.isSynchronized(modifiers)) result.add(Modifier.SYNCHRONIZED);
         if (java.lang.reflect.Modifier.isTransient(modifiers)) result.add(Modifier.TRANSIENT);
         if (java.lang.reflect.Modifier.isVolatile(modifiers)) result.add(Modifier.VOLATILE);
         return result;
     }
 
     /**
      * Method that translate javax.lang.model.element.Modifier to our APIModifier.
      *
      * @param flags Set of modifiers
      * @return
      */
     public static List<Modifier> getModifiersSet(Set<javax.lang.model.element.Modifier> flags) {
         List<Modifier> result = new LinkedList<Modifier>();
         if (flags.contains(javax.lang.model.element.Modifier.ABSTRACT)) result.add(Modifier.ABSTRACT);
         if (flags.contains(javax.lang.model.element.Modifier.NATIVE)) result.add(Modifier.NATIVE);
         if (flags.contains(javax.lang.model.element.Modifier.PRIVATE)) result.add(Modifier.PRIVATE);
         if (flags.contains(javax.lang.model.element.Modifier.PROTECTED)) result.add(Modifier.PROTECTED);
         if (flags.contains(javax.lang.model.element.Modifier.PUBLIC)) result.add(Modifier.PUBLIC);
         if (flags.contains(javax.lang.model.element.Modifier.STATIC)) result.add(Modifier.STATIC);
         if (flags.contains(javax.lang.model.element.Modifier.FINAL)) result.add(Modifier.FINAL);
         if (flags.contains(javax.lang.model.element.Modifier.STRICTFP)) result.add(Modifier.STRICTFP);
         if (flags.contains(javax.lang.model.element.Modifier.SYNCHRONIZED)) result.add(Modifier.SYNCHRONIZED);
         if (flags.contains(javax.lang.model.element.Modifier.TRANSIENT)) result.add(Modifier.TRANSIENT);
         if (flags.contains(javax.lang.model.element.Modifier.VOLATILE)) result.add(Modifier.VOLATILE);
         return result;
     }
 
     /**
      * Method convert list of modifiers to string. Modifiers has always same order.
      */
     public static String modifiersToString(List<Modifier> mods) {
         if (mods == null) return "";
         StringBuilder sb = new StringBuilder();
         if (mods.contains(Modifier.PUBLIC)) sb.append("public ");
         if (mods.contains(Modifier.PROTECTED)) sb.append("protected ");
         if (mods.contains(Modifier.PRIVATE)) sb.append("private ");
         if (mods.contains(Modifier.ABSTRACT)) sb.append("abstract ");
         if (mods.contains(Modifier.STATIC)) sb.append("static ");
         if (mods.contains(Modifier.FINAL)) sb.append("final ");
         if (mods.contains(Modifier.NATIVE)) sb.append("native ");
         if (mods.contains(Modifier.STRICTFP)) sb.append("strictfp ");
         if (mods.contains(Modifier.SYNCHRONIZED)) sb.append("synchronized ");
         if (mods.contains(Modifier.TRANSIENT)) sb.append("transient ");
         if (mods.contains(Modifier.VOLATILE)) sb.append("volatile ");
         return sb.toString();
     }
 }
