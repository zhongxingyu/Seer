 package de.hszg.atocc.core.util.grammar;
 
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.automaton.Automaton;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public final class Grammar {
 
     private static final String SPACE = " ";
     private Map<String, List<String>> rules = new HashMap<>();
     private List<String> sortedRules = new LinkedList<>();
 
     public Map<String, List<String>> getRules() {
         final Map<String, List<String>> copyOfRules = new HashMap<>();
 
         for (String lhs : rules.keySet()) {
             copyOfRules.put(lhs, new ArrayList<>(rules.get(lhs)));
         }
 
         return copyOfRules;
     }
 
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
 
         if (!sortedRules.contains(lhs)) {
             sortedRules.add(lhs);
         }
     }
 
     public void appendEpsilonRule(String lhs) {
         appendRule(lhs, Automaton.EPSILON);
     }
 
     @Override
     public String toString() {
         final StringBuilder grammar = new StringBuilder();
 
        for (String lhs : sortedRules) {
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
 
     public Set<String> findLeftHandSidesFor(String rhs) {
         final Set<String> leftHandSides = new HashSet<>();
 
         for (String lhs : rules.keySet()) {
             if (rules.get(lhs).contains(rhs)) {
                 leftHandSides.add(lhs);
             }
         }
 
         return leftHandSides;
     }
 
     public List<String> getLeftHandSides() {
         return sortedRules;
     }
 
     public boolean containsLeftHandSide(String lhs) {
         return rules.containsKey(lhs);
     }
 
     public void removeRightHandSide(String rhs) {
         for (String lhs : rules.keySet()) {
             rules.get(lhs).remove(rhs);
 
             if (rules.get(lhs).isEmpty()) {
                 remove(lhs);
             }
         }
     }
 
     public void remove(String lhs) {
         rules.remove(lhs);
         sortedRules.remove(lhs);
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
 
     public Set<String> findRulesContaining(String x) {
         return findRulesContaining(x, true);
     }
 
     public Set<String> findRulesContaining(String x, boolean recursive) {
         final Set<String> rulesContainingX = new HashSet<>();
 
         for (String rightHandSide : getAllRightHandSides()) {
             final List<String> parts = Arrays.asList(rightHandSide.split(SPACE));
 
             if (parts.contains(x)) {
                 if (!recursive && findLeftHandSidesFor(rightHandSide).contains(x)) {
                     continue;
                 }
 
                 rulesContainingX.add(rightHandSide);
             }
         }
 
         return rulesContainingX;
     }
 
     private void findTerminalsIn(String rhs, final Set<String> terminals) {
         for (String part : rhs.split(SPACE)) {
             if (!rules.containsKey(part.trim())) {
                 terminals.add(part.trim());
             }
         }
     }
 }
