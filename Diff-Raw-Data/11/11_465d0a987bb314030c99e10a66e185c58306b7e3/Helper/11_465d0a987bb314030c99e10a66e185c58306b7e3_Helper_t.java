 package grails.soot.utils;
 
 import soot.*;
 
 import java.util.List;
 
 /**
  * User: chanwit
  */
 public class Helper {
 
    // find a default constructor
     public static boolean isConstructor(Body b) {
        return b.getMethod().getName().equals("<init>") && (b.getMethod().getParameterCount() == 0);
     }
 
     public static boolean isClosureType(RefType refType) {
         SootClass closureClass = Scene.v().getSootClass("groovy.lang.Closure");
        //SootClass objectClass = Scene.v().getSootClass("java.lang.Object");
         SootClass sootClass = refType.getSootClass();
         while (sootClass.hasSuperclass()) {
            sootClass = sootClass.getSuperclass();
            if (sootClass.equals(closureClass)) return true;
         }
         return false;
     }
 
     public static boolean hasMethodName(Body b, String name) {
         return b.getMethod().getName().equals(name);
     }
 
     public static boolean listContainsBox(List<ValueBox> boxes, ValueBox boxToFind) {
         for (ValueBox valueBox : boxes) {
             if (valueBox.getValue().equivTo(boxToFind.getValue())) {
                 return true;
             }
         }
         return false;
     }
 }
