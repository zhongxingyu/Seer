 package jlogic;
 
import java.util.Map;
import java.util.HashMap;
 import java.util.ArrayList;
 
 public final class Knowledge {
    private HashMap<String, Predicate> predicates = new HashMap<String, Predicate>();
 
     public Knowledge(Rule[] rules) {
         HashMap<String, ArrayList<Rule>> rulesByName = groupRules(rules);
 
         for (Map.Entry<String, ArrayList<Rule>> entry : rulesByName.entrySet()) {
            predicates.put(entry.getKey(), new Predicate((Rule[]) entry.getValue().toArray()));
         }
     }
 
     public Predicate getPredicate(String fullName) {
         return predicates.get(fullName);
     }
 
     public Predicate getPredicate(String name, int arity) {
         return predicates.get(name + '/' + arity);
     }
 
     private static HashMap<String, ArrayList<Rule>> groupRules(Rule[] rules) {
         HashMap<String, ArrayList<Rule>> result = new HashMap<String, ArrayList<Rule>>();
 
         for (Rule rule : rules) {
             ArrayList<Rule> list = result.get(rule.getHead().getFullName());
             if (list == null) {
                 list = new ArrayList<Rule>();
                 result.put(rule.getHead().getFullName(), list);
             }
             list.add(rule);
         }
 
         return result;
     }
 
 }
