 package com.brweber2.kb.impl;
 
 import com.brweber2.kb.Fact;
 import com.brweber2.kb.Functor;
 import com.brweber2.kb.Knowledge;
 import com.brweber2.kb.KnowledgeBase;
 import com.brweber2.kb.Rule;
 import com.brweber2.proofsearch.ProofSearch;
 import com.brweber2.rule.Goal;
 import com.brweber2.term.Atom;
 import com.brweber2.term.ComplexTerm;
 import com.brweber2.term.Numeric;
 import com.brweber2.term.Term;
 import com.brweber2.term.Variable;
 import com.brweber2.unify.Binding;
 import com.brweber2.unify.Unifier;
 import com.brweber2.unify.UnifyResult;
 import com.brweber2.unify.impl.ABinding;
 import com.brweber2.unify.impl.Unify;
 import jline.ConsoleReader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * @author brweber2
  *         Copyright: 2012
  */
 public class AKnowledgeBase implements KnowledgeBase, ProofSearch {
 
     public static boolean TRACE = false;
     public static boolean PROMPT = true;
 
     Map<Functor,Collection<Knowledge>> clauses = new HashMap<Functor,Collection<Knowledge>>();
     Unifier unifier = new Unify();
     Logger log = Logger.getLogger( AKnowledgeBase.class.getName() );
     ConsoleReader consoleReader;
 
     public AKnowledgeBase( ConsoleReader consoleReader )
     {
         this.consoleReader = consoleReader;
     }
 
     public void assertKnowledge( Knowledge knowledge )
     {
         Functor functor = knowledge.getFunctor();
         if ( !clauses.containsKey( functor ) )
         {
             clauses.put( functor, new ArrayList<Knowledge>() );
         }
         clauses.get( functor ).add( knowledge );
     }
     
     public void pose( Goal query )
     {
         log.fine( "asking " + query );
         try
         {
         satisfy( new Goals( this, query ) );
         }
         catch ( ShortCircuitException e )
         {
             // ok
         }
     }
 
     public Collection<Knowledge> getClauses( Goal goal )
     {
         if ( goal instanceof Atom)
         {
             return clauses.get(((Atom)goal).getFunctor());
         }
         else if ( goal instanceof Numeric )
         {
             return clauses.get(((Numeric)goal).getFunctor());
         }
         else if ( goal instanceof Variable )
         {
             ArrayList<Knowledge> list = new ArrayList<Knowledge>();
             for (Functor functor : clauses.keySet()) {
                 list.addAll(clauses.get(functor));
             }
             return list;
         }
         else if ( goal instanceof ComplexTerm )
         {
             return clauses.get(((ComplexTerm)goal).getFunctor());
         }
         else
         {
             throw new RuntimeException("wtf?");
         }
     }
 
     public void satisfy( Goals goals )
     {
         satisfyGoals( goals, new ABinding() );
     }
 
     public void satisfyGoals( Goals goals, Binding parentBinding )
     {
         for ( GoalList goalList : goals.getListOfGoalLists() )
         {
             satisfyGoalList( goalList, parentBinding );
         }
     }
     
     private void trace( String s )
     {
         if ( TRACE )
         {
 //            log.info( s );
             System.err.println( s );
         }
         else
         {
             log.fine( s );
         }
     }
 
     private void satisfyGoalList( GoalList goalList, Binding parentBinding )
     {
         Binding goalsBinding = new ABinding( parentBinding );
         while ( goalList.haveMore() )
         {
             Goal goal = goalList.getNext();
             trace( "Goal: " + goal + " \n\t with binding: " + goalsBinding );
             Clauses clauses = goalList.getNextClause(goal);
 
             while ( clauses.hasMore() )
             {
                 Knowledge clause = clauses.getNext();
                 Binding clauseBinding = new ABinding( goalsBinding );
                 trace( "Clause: " + clause + " \n\t with binding: " + clauseBinding + " \n\t from " + goalsBinding);
                 UnifyResultAndGoalList unifyResultAndGoalList = satisfy( goal, clause, clauseBinding );
                 UnifyResult unifyResult = unifyResultAndGoalList.getUnifyResult();
                 Goals nextGoals = unifyResultAndGoalList.getGoals();
                 if ( unifyResult.succeeded() )
                 {
                     if ( nextGoals.isEmpty() )
                     {
                         if ( !goalList.haveMore() )
                         {
                             // we have a match!!!!
                             print( unifyResult.bindings() );
                         }
                         else
                         {
                             pause( unifyResult.bindings() );
                             goalList.markForBacktracking();
                             clauses.markForBacktracking();
                             satisfyGoalList( goalList, unifyResult.bindings() );
                             System.err.println( "backtracking" );
                             clauses.backtrack();
                             goalList.backtrack();
                         }
                     }
                     else
                     {
                         log.fine( "**** \n\n\ttime to satisfy: " + nextGoals + " \n\t given " + unifyResult.bindings() );
                         satisfyGoals( nextGoals, unifyResult.bindings() );
                     }
                 }
                 else
                 {
                     log.fine( "did not unify " + goal + " and clause " + clause + " with " + clauseBinding );
                 }
             }
            break;
         }
     }
     
     static class UnifyResultAndGoalList
     {
         private UnifyResult unifyResult;
         private Goals goals;
 
         UnifyResultAndGoalList( UnifyResult unifyResult, Goals goals )
         {
             this.unifyResult = unifyResult;
             this.goals = goals;
         }
 
         public UnifyResult getUnifyResult()
         {
             return unifyResult;
         }
 
         public Goals getGoals()
         {
             return goals;
         }
     }
     
     private UnifyResultAndGoalList satisfy( Goal goal, Knowledge clause, Binding binding )
     {
         log.fine( "time to satisfy " + goal + " with " + clause + " given " + binding );
         if ( clause instanceof Fact )
         {
             Fact fact = (Fact) clause;
             UnifyResult result = unifier.unify( fact.getTerm(), (Term) goal, binding );
             if ( result.succeeded() )
             {
                 trace( "FACT RESULT: " + result + " \n\t for " + fact + " \n\t asked " + goal + " \n\t given " + binding );
             }
             return new UnifyResultAndGoalList( result, new Goals( this ) );
         }
         else // if ( clause instanceof Rule )
         {
             Rule rule = (Rule) clause;
             Fact head = rule.getHead();
             log.finer( "time to satisfy rule " + goal + " with " + head + " given " + binding );
             UnifyResult headResult = ask( goal, head, binding );
             trace( "HEAD RESULT: " + headResult + " \n\t for " + head + " \n\t asked " + goal + " \n\t given " + binding );
             if ( headResult.succeeded() )
             {
                 log.fine( "head succeeded, time to check the body" );
                 return new UnifyResultAndGoalList( headResult, new Goals( this, rule.getBody() ) );
             }
             log.finer( "head did not match :(" );
             return new UnifyResultAndGoalList( headResult, new Goals(this) );
         }
     }
     
     private UnifyResult ask( Goal goal, Fact head, Binding binding )
     {
         log.finer( "goal " + goal + " head " + head + " binding: " + binding );
         Binding ruleBinding = new ABinding( binding );
         log.finer( "bbw trying to unify: goal " + goal + " head " + head + " ruleBinding: " + ruleBinding );
         UnifyResult result = unifier.unifyRuleHead( (Term) goal, (Term) head, ruleBinding );
         log.fine( "goal " + goal + " \n\t head " + head + " \n\t after ruleBinding: " + ruleBinding + " \n\t with result " + result );
 //        if ( result.succeeded() )
 //        {
 //            Binding finalBinding = new ABinding( ruleBinding );
 //            log.finer( "goal " + goal + " head " + head + " final binding: " + finalBinding );
 //            return new com.brweber2.unify.impl.UnifyResult( result, finalBinding );
 //        }
         return result;
     }
 
     public void pause( Binding binding )
     {
         try
         {
             if ( PROMPT )
             {
                 System.err.println("... PAUSING ...");
                 binding.dumpVariables();
                 String line = consoleReader.readLine();
                 if ( !";".equals( line.trim() ) )
                 {
                     throw new ShortCircuitException();
                 }
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to read input from the user.", e );
         }
     }
 
     public void print( Binding binding )
     {
         try
         {
             System.out.println("yes");
             binding.dumpVariables();
             if ( PROMPT )
             {
                 String line = consoleReader.readLine();
                 if ( !";".equals( line.trim() ) )
                 {
                     throw new ShortCircuitException();
                 }
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( "Unable to read input from the user.", e );
         }
     }
 
 }
