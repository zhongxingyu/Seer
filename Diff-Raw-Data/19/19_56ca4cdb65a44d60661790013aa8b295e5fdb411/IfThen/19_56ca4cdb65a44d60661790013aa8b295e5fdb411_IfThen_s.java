 /*
  * @copyright Copyright 2012 Andrew Brown. All rights reserved.
  * @license GNU/GPL.
  */
 package aima.core.search.nondeterministic;
 
 import java.util.ArrayList;
 
 /**
  * Represents a chain of if-then-else statements for use with AND-OR search;
  * explanation given on page 135.
  * @author Andrew Brown
  */
 public class IfThen {
 
    ArrayList<Conditional> conditionals = new ArrayList<>();
 
     /**
      * Adds a conditional to the if-then chain
      * @param antecedent
      * @param consequent 
      */
     public void add(Object antecedent, Object consequent) {
         this.conditionals.add(new Conditional(antecedent, consequent));
     }
 
     /**
      * Uses this if-then chain to return a result based on the given query
      * @param state
      * @return 
      */
     public Object makeDecision(Object query) {
         // if-then
         for (Conditional c : this.conditionals) {
             if (c.antecedent.equals(query)) {
                 return c.consequent;
             }
         }
         // else
         return this.conditionals.get(this.conditionals.size() - 1).consequent;
     }
 
     /**
      * Returns a string representation of the if-then chain; see page 134 for
      * the book example.
      * @return 
      */
     public String toString() {
         StringBuilder s = new StringBuilder();
         // write if-then-else
         if (this.conditionals.size() > 1) {
             for (int i = 0; i < this.conditionals.size() - 1; i++) {
                 Conditional c = this.conditionals.get(i);
                 s.append("if ");
                 s.append(c.antecedent);
                 s.append(" then ");
                 s.append(c.consequent);
                 s.append(" else ");
             }
         }
         // write last else
         Conditional last = this.conditionals.get(this.conditionals.size() - 1);
         s.append(last.consequent);
         // return
         return s.toString();
     }
 }
 
 /**
  * Represents a simple conditional (if-then) statement internally for the if-then
  * chain.
  * @author Andrew Brown
  */
 class Conditional {
 
     Object antecedent;
     Object consequent;
 
     /**
      * Constructor
      * @param antecedent
      * @param consequent 
      */
     public Conditional(Object antecedent, Object consequent) {
         this.antecedent = antecedent;
         this.consequent = consequent;
     }
 }
