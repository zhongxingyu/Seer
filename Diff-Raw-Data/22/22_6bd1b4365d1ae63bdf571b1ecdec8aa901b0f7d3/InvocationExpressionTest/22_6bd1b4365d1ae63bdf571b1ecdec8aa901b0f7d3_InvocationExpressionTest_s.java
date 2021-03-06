 /*
  * Copyright (C) 2010 SonarSource SA
  * All rights reserved
  * mailto:contact AT sonarsource DOT com
  */
 package com.sonar.csharp.parser.rules.expressions;
 
 import static com.sonar.sslr.test.parser.ParserMatchers.parse;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.sonar.csharp.api.CSharpGrammar;
 import com.sonar.csharp.parser.CSharpParser;
 
 public class InvocationExpressionTest {
 
   CSharpParser p = new CSharpParser();
   CSharpGrammar g = p.getGrammar();
 
   @Before
   public void init() {
     p.setRootRule(g.invocationExpression);
   }
 
   @Test
   public void testOk() {
     g.primaryExpression.mock();
     g.argumentList.mock();
     assertThat(p, parse("primaryExpression()"));
     assertThat(p, parse("primaryExpression(argumentList)"));
   }
 
   @Test
   public void testRealLife() throws Exception {
     assertThat(p, parse("GetAssemblies()"));
    // TODO 2nd fails while it passes in ExpressionTest
    // assertThat(p, parse("dbCommand.Dispose()"));
   }
 
 }
