 package at.jku.busem.diff.data;
 
 import at.jku.busem.main.objects.Attribute;
 
 import java.util.List;
 
 public class CompareHelper {
 
     public static boolean checkIfAttributesAreEqual(List<Attribute> l1, List<Attribute> l2) {
        if (l1.size()!=l2.size()) return false;
         boolean allTrue = true;
         for (Attribute a1 : l1) {
             boolean thisTrue = false;
             for (Attribute a2 : l2) {
                 if (a2.getType().equals(a1.getType())) {
                     boolean stringCheck = true;
                     boolean doubleCheck = true;
                     boolean intCheck = true;
                     boolean objectCheck = true;
                     boolean boolCheck = true;
                     for (String s : a2.getStringValue()) {
                         if (!a1.getStringValue().contains(s)) {
                             stringCheck = false;
                             break;
                         }
                     }
                     for (double d : a2.getDoubleValue()) {
                         if (!a1.getDoubleValue().contains(d)) {
                             doubleCheck = false;
                             break;
                         }
                     }
                     for (int i : a2.getIntValue()) {
                         if (!a1.getIntValue().contains(i)) {
                             intCheck = false;
                             break;
                         }
                     }
                     for (boolean b : a2.getBoolValue()) {
                         if (!a1.getBoolValue().contains(b) || a1.getBoolValue().size() != a2.getBoolValue().size()) {
                             boolCheck = false;
                             break;
                         }
                     }
                     if (a1.getObjectValue().size() != a2.getObjectValue().size()) {
                         objectCheck = false;
                     }
                     thisTrue = stringCheck && intCheck && doubleCheck && objectCheck && boolCheck;
                     if (!thisTrue) break;
                 }
             }
             allTrue = thisTrue;
             if (!allTrue) break;
         }
         return allTrue;
     }
 
 }
