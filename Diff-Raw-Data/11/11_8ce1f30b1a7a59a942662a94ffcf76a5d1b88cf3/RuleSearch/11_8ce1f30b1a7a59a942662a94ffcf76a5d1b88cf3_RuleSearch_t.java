 package com.brweber2.kb;
 
 import com.brweber2.term.Term;
 import com.brweber2.term.rule.Rule;
 import com.brweber2.term.rule.RuleAnd;
 import com.brweber2.term.rule.RuleBody;
 import com.brweber2.term.rule.RuleOr;
 import com.brweber2.unification.UnificationResult;
 import com.brweber2.unification.UnificationScope;
 import com.brweber2.unification.UnificationSuccess;
 
 /**
  * @author brweber2
  *         Copyright: 2012
  */
 public class RuleSearch {
 
     private ProofSearch proofSearch;
 
     public RuleSearch(ProofSearch proofSearch) {
         this.proofSearch = proofSearch;
     }
     
     public UnificationResult unifyRuleBody( UnificationScope scope, RuleBody ruleBody )
     {
         if ( ruleBody instanceof RuleAnd )
         {
             RuleAnd ruleAnd = (RuleAnd) ruleBody;
             RuleBody left = ruleAnd.getLeft();
             RuleBody right = ruleAnd.getRight();
             UnificationResult finalResult = new UnificationResult();
             UnificationResult currentResult = finalResult;
             UnificationResult leftResult = unifyRuleBody(new UnificationScope(scope), left);
             while ( leftResult != null )
             {
                 if ( leftResult.getSuccess() == UnificationSuccess.YES )
                 {
                     UnificationResult rightResult = unifyRuleBody(new UnificationScope(leftResult.getScope()),right);
                     while ( rightResult != null )
                     {
                         if ( rightResult.getSuccess() == UnificationSuccess.YES )
                         {
                             currentResult = currentResult.next(new UnificationResult(rightResult.getScope(), left, right));
                         }
                         rightResult = rightResult.getNext();
                     }
                 }
 
                 leftResult = leftResult.getNext();
             }
             if ( finalResult != currentResult )
             {
                 return finalResult.getNext();
             }
             return finalResult;
         }
         else if ( ruleBody instanceof RuleOr )
         {
             RuleOr ruleOr = (RuleOr) ruleBody;
             RuleBody left = ruleOr.getLeft();
             RuleBody right = ruleOr.getRight();
 
             UnificationResult finalResult = new UnificationResult();
             UnificationResult currentResult = finalResult;
             
             UnificationResult leftResult = unifyRuleBody( new UnificationScope(scope), left );
             while ( leftResult != null )
             {
                 if ( leftResult.getSuccess() == UnificationSuccess.YES )
                 {
                     currentResult = currentResult.next(new UnificationResult(leftResult.getScope(),left,right));
                 }
 
                 leftResult = leftResult.getNext();
             }
             UnificationResult rightResult = unifyRuleBody( new UnificationScope(scope), right );
             while ( rightResult != null )
             {
                 if ( rightResult.getSuccess() == UnificationSuccess.YES )
                 {
                     currentResult = currentResult.next(new UnificationResult(rightResult.getScope(),left,right));
                 }
                 rightResult = rightResult.getNext();
             }
             if ( finalResult != currentResult )
             {
                 return finalResult.getNext();
             }
             return finalResult;
         }
         else
         {
            return proofSearch.ask(scope, (Term) ruleBody);
         }
     }
 
     public UnificationResult ask(Term question, Rule rule) {
         return ask( new UnificationScope(), question, rule );
     }
     
     public UnificationResult ask(UnificationScope scope, Term question, Rule rule) {
         // does the question unify with head?
         
         UnificationResult headResult = proofSearch.getUnifier().unify(scope, question, rule.getHead() );
         if ( headResult.getSuccess() == UnificationSuccess.YES )
         {
             // now we have to see if all the conditions in body hold
             return unifyRuleBody( headResult.getUnifyScope(), rule.getBody() );
         }
         else
         {
             return headResult;
         }
     }
 }
