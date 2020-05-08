 package com.brweber2.term.rule;
 
 /**
  * @author brweber2
  * Copyright: 2012
  */
public class RuleOr {
     RuleBody left;
     RuleBody right;
 
     public RuleOr(RuleBody left, RuleBody right) {
         this.left = left;
         this.right = right;
     }
 
     public RuleBody getLeft() {
         return left;
     }
 
     public RuleBody getRight() {
         return right;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         RuleOr ruleOr = (RuleOr) o;
 
         if (!left.equals(ruleOr.left)) return false;
         if (!right.equals(ruleOr.right)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = left.hashCode();
         result = 31 * result + right.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return "RuleOr{" +
                 "left=" + left +
                 ", right=" + right +
                 '}';
     }
 }
