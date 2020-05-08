 package net.nowhere.maxim.comparator;
 
 import java.util.Comparator;
 
 public class NumericStringComparator implements Comparator<String> {
     @Override
     public int compare(String o1, String o2) {
         if (o1.equals(o2) || o1.contentEquals(o2)) {
             return 0;
         }
 
         for (int index = 0; index < o1.length() && index < o2.length(); index++) {
             if (Character.isDigit(o1.charAt(index)) && Character.isDigit(o2.charAt(index))) {
                 int numberWeight1 = getNumericWeight(o1, index);
                 int numberWeight2 = getNumericWeight(o2, index);
                 if (numberWeight1 == numberWeight2) {
                     if (index + 1 < o1.length() && index + 1 < o2.length())
                         return compare(o1.substring(index + 1), o2.substring(index + 1));
                     return o1.compareTo(o2);
                 }
                return numberWeight1 - numberWeight2;
             } else {
                int result = (int) o1.charAt(index) - (int) o2.charAt(index);
                 if (result == 0) {
                     result = compare(o1.substring(index + 1), o2.substring(index + 1));
                 }
                 return result;
             }
         }
         return o1.compareTo(o2);
     }
 
     private int getNumericWeight(String stringWithNumbers, int index) {
         int value = Integer.parseInt(stringWithNumbers.substring(index, index + 1));
         if (value == 0) {
             return value;
         }
         int offset = 0;
         int factor = 1;
        while (Character.isDigit(stringWithNumbers.charAt(index + offset))) {
             offset++;
         }
         while (offset > 1) {
             factor *= 10;
             offset--;
         }
         return factor * value;
     }
 }
