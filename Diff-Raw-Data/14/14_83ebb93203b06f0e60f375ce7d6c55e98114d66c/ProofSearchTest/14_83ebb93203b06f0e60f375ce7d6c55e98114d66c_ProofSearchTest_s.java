 /**
  * @author brweber2
  *         Copyright: 2012
  */
 package com.brweber2.unify;
 
 import com.brweber2.kb.KnowledgeBase;
 import com.brweber2.kb.impl.AKnowledgeBase;
 import com.brweber2.rule.impl.AConjunction;
 import com.brweber2.rule.impl.ARule;
 import com.brweber2.term.impl.AComplexTerm;
 import com.brweber2.term.impl.AVariable;
 import com.brweber2.term.impl.AnAtom;
 import com.brweber2.unify.impl.ABinding;
 import com.brweber2.unify.impl.Unify;
 import jline.ConsoleReader;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class ProofSearchTest
 {
     @Test
     public void testProofSearch()
     {
         try {
             KnowledgeBase kb = new AKnowledgeBase(new ConsoleReader(  ));
 
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Tim" ), new AnAtom( "Gary" ) ) );
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Tim" ), new AnAtom( "Melissa" ) ) );
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Tim" ), new AnAtom( "Bob" ) ) );
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Ralph" ), new AnAtom( "Tim" ) ) );
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Mary" ), new AnAtom( "Nancy" ) ) );
             kb.assertKnowledge( new AComplexTerm( "parent", new AnAtom( "Mark" ), new AnAtom( "Gary" ) ) );
 
             kb.assertKnowledge( new ARule(
                     new AComplexTerm( "related", new AVariable( "X" ), new AVariable( "Y" ) ),
                         new AComplexTerm( "parent", new AVariable( "X" ), new AVariable( "Y" ) ) ) );
             kb.assertKnowledge( new ARule(
                     new AComplexTerm( "related", new AVariable( "Y" ), new AVariable( "X" ) ),  new AConjunction(
                         new AComplexTerm( "parent", new AVariable( "Y" ), new AVariable( "Z" ) ),
                         new AComplexTerm( "related", new AVariable( "Z" ), new AVariable( "X" ) ) ) ) );
 
 //        kb.pose( new AComplexTerm("parent", new AnAtom( "Tim" ), new AVariable( "Y" ) ) );
 //        kb.pose( new AComplexTerm("related", new AnAtom( "Ralph" ), new AVariable( "Y" ) ) );
 
             kb.pose( new AComplexTerm("related", new AnAtom( "Ralph" ), new AnAtom( "Bob" ) ) );
         } catch (Exception e) {
             e.printStackTrace();
             Assert.fail("kaboom " + e.getMessage());
         }
     }
     
     @Test(enabled = true)
     public void testUnifyWithBinding()
     {
         Binding bindings = new ABinding();
         bindings.instantiate( new AVariable( "X" ), new AnAtom( "Ralph" ) );
         bindings.instantiate( new AVariable( "Y" ), new AnAtom( "Bob" ) );
         Unifier unifier = new Unify();
         unifier.unify( new AComplexTerm( "parent", new AVariable( "X" ), new AVariable( "Z" ) ), new AComplexTerm( "parent", new AnAtom( "Ralph" ), new AnAtom( "Tim" ) ), bindings );
     }
 }
