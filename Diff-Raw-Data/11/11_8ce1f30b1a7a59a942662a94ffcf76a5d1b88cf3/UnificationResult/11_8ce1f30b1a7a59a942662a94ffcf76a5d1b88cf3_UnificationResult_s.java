 package com.brweber2.unification;
 
 import com.brweber2.term.Term;
 import com.brweber2.term.Variable;
 import com.brweber2.term.rule.RuleBody;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author brweber2
  * Copyright: 2012
  */
 public class UnificationResult {
     private final UnificationSuccess success;
     private final UnificationScope scope;
     private UnificationResult next;
     // unifying terms
     private RuleBody term1;
     private RuleBody term2;
 
     public UnificationResult() {
         this.success = UnificationSuccess.NO;
         this.scope = new UnificationScope();
     }
 
     public UnificationResult( UnificationScope scope, RuleBody term1, RuleBody term2) {
         this.success = UnificationSuccess.YES;        
         this.scope = scope;
         this.term1 = term1;
         this.term2 = term2;
     }
 
     public UnificationResult next( UnificationResult result )
     {
        if ( this.equals(result) )
        {
            return this;
        }
         this.next = result;
         return result;
     }
 
     public UnificationResult getNext() {
         return next;
     }
 
     public UnificationSuccess getSuccess() {
         return success;
     }
 
     public UnificationScope getScope() {
         return scope;
     }
 
     public RuleBody getTerm1() {
         return term1;
     }
 
     public RuleBody getTerm2() {
         return term2;
     }
 
     public String toString()
     {
         StringBuilder str = new StringBuilder();
         if ( success == UnificationSuccess.YES )
         {
             if ( !scope.isEmpty() )
             {
                 for (Variable variable : scope.keys()) {
                     str.append(variable.prettyPrint() + ": " + scope.resolve(variable).prettyPrint() + "\n");
                 }
             }
         }
         str.append(success.name().toLowerCase());
         str.append("\n");
         return str.toString();
     }
 
     public void print()
     {
         System.out.println(toString());
     }
 
     public UnificationScope getUnifyScope() {
         return scope;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         UnificationResult that = (UnificationResult) o;
 
         if (next != null ? !next.equals(that.next) : that.next != null) return false;
         if (!scope.equals(that.scope)) return false;
         if (success != that.success) return false;
         if (!term1.equals(that.term1)) return false;
         if (!term2.equals(that.term2)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = success.hashCode();
         result = 31 * result + scope.hashCode();
         result = 31 * result + (next != null ? next.hashCode() : 0);
         result = 31 * result + term1.hashCode();
         result = 31 * result + term2.hashCode();
         return result;
     }
 }
