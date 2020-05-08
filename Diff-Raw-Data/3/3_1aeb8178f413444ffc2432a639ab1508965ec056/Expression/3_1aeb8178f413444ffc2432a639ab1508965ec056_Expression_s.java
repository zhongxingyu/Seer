 package com.silentmatt.dss;
 
 import com.silentmatt.dss.css.CssColorTerm;
 import com.silentmatt.dss.css.CssExpression;
 import com.silentmatt.dss.css.CssTerm;
 import com.silentmatt.dss.term.Term;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents the value part of a declaration (right-hand side).
  *
  * An expression is a list of {@link Term}s, separated by spaces, commas, or slashes.
  *
  * @author Matthew Crumley
  */
 public class Expression {
     private final List<Term> terms = new ArrayList<Term>();
 
     public Expression() {
     }
 
     public Expression(Term term) {
         terms.add(term);
     }
 
     /**
      * Gets the child terms of the expression.
      *
      * @return The Terms contained in the expression
      */
     public List<Term> getTerms() {
         return terms;
     }
 
     /**
      * Gets the expression as a String.
      *
      * @return A string of the form "term [&lt;separator&gt; term]*".
      */
     @Override
     public String toString() {
         StringBuilder txt = new StringBuilder();
         boolean first = true;
         for (Term t : terms) {
             if (first) {
                 first = false;
             } else {
                 if (t.getSeperator() == null) {
                     txt.append(" ");
                 }
                 else {
                     txt.append(t.getSeperator());
                     if (!t.getSeperator().equals(' ')) {
                         txt.append(" ");
                     }
                 }
             }
             txt.append(t.toString());
         }
         return txt.toString();
     }
 
     public Expression substituteValues(EvaluationState state, DeclarationList container, boolean withParams, boolean doCalculations) {
         Expression newValue = new Expression();
 
         for (Term primitiveValue : getTerms()) {
             Expression sub = primitiveValue.substituteValues(state, container, withParams, doCalculations);
             if (sub != null) {
                 for (Term t : sub.getTerms()) {
                     newValue.getTerms().add(t);
                 }
             }
         }
 
         return newValue;
     }
 
     public CssExpression evaluate(EvaluationState state, DeclarationList container) {
         // TODO: should doCalculations be true?
         Expression newValue = substituteValues(state, container, state.getParameters() != null, true);
         CssExpression result = new CssExpression();
         for (Term t : newValue.getTerms()) {
             CssTerm cssTerm;
             if (t.isColor()) {
                 cssTerm = new CssColorTerm(t.toColor());
             }
             else {
                 cssTerm = new CssTerm(t.toString());
             }
             cssTerm.setSeperator(t.getSeperator());
             result.getTerms().add(cssTerm);
         }
         return result;
     }
 }
