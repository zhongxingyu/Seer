 package com.brweber2.kb;
 
 import com.brweber2.term.Term;
 import com.brweber2.term.rule.Rule;
 import com.brweber2.unification.UnificationResult;
 import com.brweber2.unification.UnificationScope;
 import com.brweber2.unification.UnificationSuccess;
 import com.brweber2.unification.Unify;
 
 import java.util.logging.Logger;
 
 /**
  * @author brweber2
  *         Copyright: 2012
  */
 public class ProofSearch {
 
     private static Logger log = Logger.getLogger(ProofSearch.class.getName());
     
     private Unify unifier;
     private KnowledgeBase knowledgeBase;
     private RuleSearch ruleSearch;
 
     public ProofSearch(Unify unifier, KnowledgeBase knowledgeBase) {
         this.unifier = unifier;
         this.knowledgeBase = knowledgeBase;
         this.ruleSearch = new RuleSearch(this);
     }
 
     public Unify getUnifier() {
         return unifier;
     }
 
     public UnificationResult ask( Term question )
     {
         return ask( new UnificationScope(), question );
     }
     
     public UnificationResult ask( UnificationScope scope, Term question )
     {
         UnificationResult result = new UnificationResult();
         UnificationResult baseResult = result;
         for (Term term : knowledgeBase.getTerms()) {   // todo this should be a map lookup for faster indexing...
             UnificationResult thisResult = unifier.unify(new UnificationScope(scope), question, term);
             if ( thisResult.getSuccess() == UnificationSuccess.YES )
             {
                 result.next(thisResult);
                 result = thisResult;
             }
         }
         for (Rule rule : knowledgeBase.getRules()) {  // todo this should be a map lookup for faster indexing...
             UnificationResult thisResult = ruleSearch.ask(new UnificationScope(scope), question, rule);
             if ( thisResult.getSuccess() == UnificationSuccess.YES )
             {
                 result.next(thisResult);
                 result = thisResult;
             }
         }
         if ( baseResult == result )
         {
             log.info("unable to answer " + question + " with scope: " + scope);
             return baseResult;
         }
         log.info("answered " + question + " with scope: " + scope);
         return baseResult.getNext();
     }
 
     public void askAndPrint( Term question )
     {
         printUnificationResult( ask( question ) );
     }
 
     public void printUnificationResult( UnificationResult result )
     {
         if ( result.getSuccess() == UnificationSuccess.YES )
         {
             while ( result.getNext() != null )
             {
                 System.out.println(result.toString());
                 result = result.getNext();
             }
         }
         System.out.println(result.getSuccess().name().toLowerCase());
     }
 }
