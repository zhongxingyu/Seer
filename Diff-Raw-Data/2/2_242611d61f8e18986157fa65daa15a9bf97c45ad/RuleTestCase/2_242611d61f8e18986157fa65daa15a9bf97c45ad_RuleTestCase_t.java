 package org.nnsoft.t2t.core;
 
 /*
  *    Copyright 2011-2012 The 99 Software Foundation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 import static org.testng.Assert.*;
 
 import org.openrdf.model.Statement;
 import org.openrdf.model.impl.StatementImpl;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.query.algebra.StatementPattern;
 import org.openrdf.query.algebra.Var;
 import org.testng.Assert;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Davide Palmisano ( dpalmisano@gmail.com )
  */
 public class RuleTestCase
 {
 
     private Rule rule;
 
     private Statement statement;
 
     @BeforeTest
     public void setUp()
     {
         statement =
             new StatementImpl( new URIImpl( "http://davidepalmisano.com" ),
                                new URIImpl( "http://xmlns.org/foaf/01/knows" ), new URIImpl( "http://matteo.mo.ci" ) );
         StatementPattern statementPattern = new StatementPattern();
         Var s = new Var( "s" );
         Var p = new Var( "p", new URIImpl( "http://xmlns.org/foaf/01/knows" ) );
         Var o = new Var( "o" );
         statementPattern.setSubjectVar( s );
         statementPattern.setPredicateVar( p );
         statementPattern.setObjectVar( o );
        Set<StatementPattern> apply = new HashSet<StatementPattern>();
 
         StatementPattern statementPattern1 = new StatementPattern();
         Var s1 = new Var( "o" );
         Var p1 = new Var( "p", new URIImpl( "http://xmlns.org/foaf/01/knows" ) );
         Var o1 = new Var( "s" );
         statementPattern1.setSubjectVar( s1 );
         statementPattern1.setPredicateVar( p1 );
         statementPattern1.setObjectVar( o1 );
         apply.add( statementPattern1 );
 
         StatementPattern statementPattern2 = new StatementPattern();
         Var s2 = new Var( "s" );
         Var p2 = new Var( "p", new URIImpl( "http://xmlns.org/foaf/01/knows" ) );
         Var o2 = new Var( "o" );
         statementPattern2.setSubjectVar( s2 );
         statementPattern2.setPredicateVar( p2 );
         statementPattern2.setObjectVar( o2 );
         apply.add( statementPattern2 );
         rule = new Rule( statementPattern, apply );
     }
 
     @AfterTest
     public void tearDown()
     {
     }
 
     @Test
     public void testMatch()
         throws RuleExecutionException
     {
         assertTrue( rule.match( statement ) );
     }
 
     @Test
     public void testApply()
         throws RuleExecutionException
     {
         Set<Statement> statements = rule.apply( statement );
         assertNotNull( statements );
         assertTrue( statements.size() > 0 );
         Statement newExpectedStatement =
             new StatementImpl( new URIImpl( "http://matteo.mo.ci" ), new URIImpl( "http://xmlns.org/foaf/01/knows" ),
                                new URIImpl( "http://davidepalmisano.com" ) );
         assertTrue( statements.contains( statement ) );
         assertTrue( statements.contains( newExpectedStatement ) );
     }
 
 }
