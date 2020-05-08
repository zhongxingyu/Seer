 package kkckkc.syntaxpane.parse.grammar.textmate;
 
 import kkckkc.syntaxpane.style.ScopeSelector;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class TextmateScopeSelectorParser {
     public static ScopeSelector parse(String s) {
         ScopeSelector selector = new ScopeSelector();
 
         StringTokenizer tok = new StringTokenizer(s, ",");
         while (tok.hasMoreTokens()) {
             String t = tok.nextToken();
             selector.addRule(parseRule(t));
         }
 
         return selector;
     }
 
     public static ScopeSelector.Rule parseRule(String t) {
         List<String> positiveRule = new ArrayList<String>(5);
         List<String> negativeRule = new ArrayList<String>(5);
 
        int positionOfMinus = t.indexOf(" - ");
 
         // Parse positive rules
         String s = positionOfMinus < 0 ? t : t.substring(0, positionOfMinus);
         StringTokenizer tok = new StringTokenizer(s, " ");
         while (tok.hasMoreTokens()) {
             String i = tok.nextToken();
             positiveRule.add(i);
         }
 
         // Parse negative rules
         if (positionOfMinus >= 0) {
             s = t.substring(positionOfMinus + 1);
             tok = new StringTokenizer(s, " ");
             while (tok.hasMoreTokens()) {
                 String i = tok.nextToken();
                 negativeRule.add(i);
             }
         }
 
         return new ScopeSelector.Rule(positiveRule.size() == 0 ? null : positiveRule, negativeRule.size() == 0 ? null : negativeRule);
     }
 
 }
