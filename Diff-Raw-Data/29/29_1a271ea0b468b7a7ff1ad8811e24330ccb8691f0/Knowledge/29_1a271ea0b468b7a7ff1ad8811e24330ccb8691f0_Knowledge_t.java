 package jlogic;
 
 import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
 
 public final class Knowledge {
    private final HashMap<String, Predicate> predicates = new HashMap<String, Predicate>();
 
     public Knowledge(Rule[] rules) {
         HashMap<String, ArrayList<Rule>> rulesByName = groupRules(rules);
 
         for (Map.Entry<String, ArrayList<Rule>> entry : rulesByName.entrySet()) {
            Rule[] ruleArray = new Rule[entry.getValue().size()];
            predicates.put(entry.getKey(), new Predicate(entry.getValue().toArray(ruleArray)));
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, Predicate> entry : predicates.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":\n");
            builder.append(entry.getValue());
         }

        return builder.toString();
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
            assert rule != null;

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
