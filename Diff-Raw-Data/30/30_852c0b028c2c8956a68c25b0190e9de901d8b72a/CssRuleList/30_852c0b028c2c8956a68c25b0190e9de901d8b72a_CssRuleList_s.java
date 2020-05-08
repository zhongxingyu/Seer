 package com.silentmatt.dss.css;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author matt
  */
 public class CssRuleList extends CssRule {
     private final List<CssRule> rules = new ArrayList<CssRule>();
 
     public void addRule(CssRule r) {
         rules.add(r);
     }
 
     public List<CssRule> getRules() {
         return rules;
     }
 
     @Override
     public String toString() {
         return toString(0);
     }
 
     @Override
     public String toString(int nesting) {
         StringBuilder sb = new StringBuilder();
         for (CssRule r : rules) {
             sb.append(r.toString(nesting)).append('\n');
         }
         return sb.toString();
     }
 }
