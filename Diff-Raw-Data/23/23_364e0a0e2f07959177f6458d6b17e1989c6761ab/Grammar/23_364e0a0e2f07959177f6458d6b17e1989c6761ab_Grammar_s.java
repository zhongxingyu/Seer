 package de.hszg.atocc.core.util.grammar;
 
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.automaton.Automaton;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public final class Grammar {
 
     private Map<String, List<String>> rules = new HashMap<>();
 
     public void appendRule(String lhs, List<String> rhs) {
         for (String r : rhs) {
             appendRule(lhs, r);
         }
     }
 
     public void appendRule(String lhs, String rhs) {
         if (!rules.containsKey(lhs)) {
             rules.put(lhs, new ArrayList<String>());
         }
 
         rules.get(lhs).add(rhs);
     }
 
     public void appendEpsilonRule(String lhs) {
         appendRule(lhs, Automaton.EPSILON);
     }
 
     @Override
     public String toString() {
         final StringBuilder grammar = new StringBuilder();
 
         for (String lhs : rules.keySet()) {
             final List<String> rhs = rules.get(lhs);
 
             if (!rhs.isEmpty()) {
                 grammar.append(String.format("%s -> %s\n", lhs,
                         CollectionHelper.makeString(rhs, " | ")));
             }
         }
 
         return grammar.toString();
     }
 
     public Set<String> getAllRightHandSides() {
         final Set<String> allRhs = new HashSet<>();
 
         for (List<String> rhs : rules.values()) {
             allRhs.addAll(rhs);
         }
 
         return allRhs;
     }
 
     public Collection<String> getRightHandSidesFor(String lhs) {
         return rules.get(lhs);
     }
 
    public Set<String> getLeftHandSides() {
        return rules.keySet();
     }
 
     public boolean containsLeftHandSide(String lhs) {
         return rules.containsKey(lhs);
     }
 
     public void removeRightHandSide(String rhs) {
         for (String lhs : rules.keySet()) {
             rules.get(lhs).remove(rhs);
         }
     }
 
     public void remove(String lhs) {
         rules.remove(lhs);
     }
 
     public Set<String> findTerminals() {
         final Set<String> terminals = new HashSet<>();
 
         for (List<String> rightHandSides : rules.values()) {
             for (String rhs : rightHandSides) {
                 findTerminalsIn(rhs, terminals);
             }
         }
 
         return terminals;
     }
 
     private void findTerminalsIn(String rhs, final Set<String> terminals) {
         for (String part : rhs.split(" ")) {
             if (!rules.containsKey(part.trim())) {
                 terminals.add(part.trim());
             }
         }
     }
 
     // public Set<String> findNonTerminals() {
     //
     // }
 }
