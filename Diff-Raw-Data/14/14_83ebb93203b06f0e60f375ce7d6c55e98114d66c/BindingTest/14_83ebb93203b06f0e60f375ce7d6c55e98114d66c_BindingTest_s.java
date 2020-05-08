 /*
  * Copyright (C) 2012 brweber2
  */
 package com.brweber2.unify;
 
 import com.brweber2.term.impl.AComplexTerm;
 import com.brweber2.term.impl.AVariable;
 import com.brweber2.term.impl.AnAtom;
 import com.brweber2.unify.impl.ABinding;
 import com.brweber2.unify.impl.Unify;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class BindingTest
 {
     @Test
     public void bindToFactWithVariable()
     {
         Binding binding = new ABinding();
         binding.instantiate( new AnAtom( "foo" ), new AVariable( "X" ) );
 
         /*
 
         :- foo(X,foo,Y,Z,quux).
 
         :- X is foo
         ?- foo(Y,X,bar,baz,M).
 
          */
 
         Unifier unifier = new Unify();
         UnifyResult result = unifier.unify(
                 new AComplexTerm( "foo", new AVariable( "X"), new AnAtom( "foo" ), new AVariable( "Y" ), new AVariable( "Z" ), new AnAtom( "quux" )),
                 new AComplexTerm( "foo", new AVariable( "Y" ), new AVariable( "X" ), new AnAtom( "bar" ), new AnAtom( "baz" ), new AVariable( "M" ) ),
                 binding );
 
         Assert.assertTrue( result.succeeded() );
         Assert.assertEquals( result.bindings().getVariables().size(), 3 );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "X" ) ) );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "Y" ) ) );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "M" ) ) );
         Assert.assertFalse( result.bindings().getVariables().contains( new AVariable( "Z" ) ) );
         Assert.assertTrue( result.bindings().isBound( new AVariable( "Y" ) ) );
         Assert.assertNull( result.bindings().resolve( new AVariable( "Y" ) ) );
         Assert.assertEquals( result.bindings().resolve( new AVariable( "X" ) ), new AnAtom( "foo" ) );
         Assert.assertEquals( result.bindings().resolve( new AVariable( "M" ) ), new AnAtom( "quux" ) );
     }
 
     @Test
     public void bindToRuleHeadWithVariable()
     {
         Binding binding = new ABinding();
         binding.instantiate( new AnAtom( "foo" ), new AVariable( "X" ) );
 
         /*
 
         :- foo(X,foo,Y,Z,quux) :- [...].
 
         :- X is foo
         ?- foo(Y,X,bar,baz,M).
 
          */
 
         Unifier unifier = new Unify();
         UnifyResult result = unifier.unifyRuleHead(
                new AComplexTerm( "foo", new AVariable( "X"), new AnAtom( "foo" ), new AVariable( "Y" ), new AVariable( "Z" ), new AnAtom( "quux" )),
                 new AComplexTerm( "foo", new AVariable( "Y" ), new AVariable( "X" ), new AnAtom( "bar" ), new AnAtom( "baz" ), new AVariable( "M" ) ),
                 binding );
 
         Assert.assertTrue( result.succeeded() );
         Assert.assertEquals( result.bindings().getVariables().size(), 3 );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "X" ) ) );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "Y" ) ) );
         Assert.assertTrue( result.bindings().getVariables().contains( new AVariable( "Z" ) ) );
         Assert.assertFalse( result.bindings().getVariables().contains( new AVariable( "M" ) ) );
         Assert.assertTrue( result.bindings().isBound( new AVariable( "X" ) ) );
         Assert.assertNull( result.bindings().resolve( new AVariable( "X" ) ) );
         Assert.assertEquals( result.bindings().resolve( new AVariable( "Y" ) ), new AnAtom( "bar" ) );
         Assert.assertEquals( result.bindings().resolve( new AVariable( "Z" ) ), new AnAtom( "baz" ) );
     }
 
 }
