 package com.ninja_squad.codestory.scalaskel;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.util.EnumSet;
 import java.util.Map;
 import java.util.Set;
 
 public class ChangeComputer {
 
     public Set<Map<Unite, Integer>> change(final int sum) {
         Set<Map<Unite, Integer>> results = Sets.newHashSet();
         change(sum, new int[Unite.values().length], EnumSet.allOf(Unite.class), results);
         return results;
     }
 
    private void change(final int sum, int[] current, Set<Unite> candidate, Set<Map<Unite, Integer>> results) {
 
         if (sum == 0) {
             results.add(cleanCopy(current));
         } else {
 
             Set<Unite> nextCandidate = EnumSet.copyOf(candidate);
             for (Unite unite : candidate) {
                 int remaining = sum - unite.getValue();
 
                 if (remaining >= 0) {
                     // We could use one more of this unit
                     current[unite.ordinal()]++;
                     change(remaining, current, nextCandidate, results);
                     current[unite.ordinal()]--;
                 } else {
                     break;
                 }
                 nextCandidate.remove(unite);
             }
         }
     }
 
     private Map<Unite, Integer> cleanCopy(int[] candidate) {
         Map<Unite, Integer> copy = Maps.newEnumMap(Unite.class);
         for (Unite unite : Unite.values()) {
             if (candidate[unite.ordinal()] > 0) {
                 copy.put(unite, candidate[unite.ordinal()]);
             }
         }
         return copy;
     }
 
     private static ChangeComputer ourInstance = new ChangeComputer();
 
     public static ChangeComputer getInstance() {
         return ourInstance;
     }
 
     private ChangeComputer() {
     }
 }
