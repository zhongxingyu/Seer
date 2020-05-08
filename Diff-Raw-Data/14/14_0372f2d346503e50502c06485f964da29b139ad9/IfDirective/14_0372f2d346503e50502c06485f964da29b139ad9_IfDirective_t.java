 package com.silentmatt.dss.directive;
 
 import com.silentmatt.dss.EvaluationState;
 import com.silentmatt.dss.Rule;
 import com.silentmatt.dss.bool.BooleanExpression;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 /**
  *
  * @author Matthew Crumley
  */
 public class IfDirective extends Rule {
     private final BooleanExpression condition;
     private final List<Rule> ifRules;
     private final List<Rule> elseRules;
     private List<Rule> rules = null;
 
     public IfDirective(BooleanExpression condition, List<Rule> ifRules, List<Rule> elseRules) {
         super();
         this.condition = condition;
         this.ifRules = ifRules;
         this.elseRules = elseRules;
     }
 
     public BooleanExpression getCondition() {
         return condition;
     }
 
     public List<Rule> getIfRules() {
         return ifRules;
     }
 
     public List<Rule> getElseRules() {
         return elseRules;
     }
 
     @Override
     public String toString() {
         return toString(0);
     }
 
     // FIXME: not nested properly
     public String toString(int nesting) {
         StringBuilder txt = new StringBuilder();
         txt.append("@if ").append(condition).append(" {\n");
 
         for (Rule rule : ifRules) {
             txt.append(rule.toString(nesting + 1));
             txt.append("\n");
         }
 
         txt.append("}");
 
         if (elseRules != null) {
             txt.append("\n@else {\n");
 
             for (Rule rule : elseRules) {
                 txt.append(rule.toString(nesting + 1));
                 txt.append("\n");
             }
 
             txt.append("}");
         }
         return txt.toString();
     }
 
     public String toCssString(int nesting) {
         StringBuilder txt = new StringBuilder();
 
         if (rules != null) {
             for (Rule rule : rules) {
                 txt.append(rule.toCssString(nesting));
                 txt.append("\n");
             }
         }
 
         return txt.toString();
     }
 
     @Override
     public void evaluate(EvaluationState state, List<Rule> container) throws MalformedURLException, IOException {
         Boolean result = condition.evaluate(state);
         if (result == null) {
             state.getErrors().SemErr("Invalid condition: " + condition);
         }
         rules = result ? ifRules : elseRules;
 
         if (rules != null) {
            Rule.evaluateRules(state, rules);
         }
     }
 }
