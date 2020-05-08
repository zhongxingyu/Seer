 package org.rulemaker.engine.matcher;
 
 import java.util.Map;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.rulemaker.engine.EngineContext;
 import org.rulemaker.engine.matcher.exception.MatchingException;
 import org.rulemaker.model.Term;
 
 public class ExpressionTermMatcherTest {
 	
 	@Test
 	public void shouldBuildAnExpressionTermMatcherForAnExpressionTerm() throws Exception {
 		Term term = new Term("age", Term.TermType.EXPRESSION, "17 + 2");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(new EngineContext(null), term);
 		assertTrue(matcher instanceof ExpressionTermMatcher);
 	}
 	
 	@Test
 	public void shouldMatchObjectWithMatchingExpressionValue() throws Exception {
 		Term term = new Term("age", Term.TermType.EXPRESSION, "17 + 2");
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(new EngineContext(null), term);
 		targetObject.setAge(19);
 		assertTrue(matcher.matches(targetObject));
 	}
 	
 	@Test
 	public void shouldMatchObjectWithMatchingExpressionValueWithVariables() throws Exception {
 		Term term = new Term("age", Term.TermType.EXPRESSION, "17 + X");
 		EngineContext context = new EngineContext(null);
 		Map<String, Object> variablesMap = context.getGobalVariablesMap();
 		variablesMap.put("X", 2);
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(context, term);
 		targetObject.setAge(19);
 		assertTrue(matcher.matches(targetObject));
 	}
 	
 	@Test
 	public void shouldMatchObjectWithMatchingExpressionValueWithVariablesWithMultiplesInstances() throws Exception {
 		Term term = new Term("age", Term.TermType.EXPRESSION, "17 + X + 2*X");
 		EngineContext context = new EngineContext(null);
 		Map<String, Object> variablesMap = context.getGobalVariablesMap();
 		variablesMap.put("X", 2);
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(context, term);
 		targetObject.setAge(23);
 		assertTrue(matcher.matches(targetObject));
 	}
 	
 	@Test
 	public void shouldNotMatchObjectWithMatchingExpressionValueWithVariablesWithNoMatchingValues() throws Exception {
 		Term term = new Term("age", Term.TermType.EXPRESSION, "17 + X");
 		EngineContext context = new EngineContext(null);
 		Map<String, Object> variablesMap = context.getGobalVariablesMap();
 		variablesMap.put("X", 2);
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(context, term);
 		targetObject.setAge(-1);
 		assertFalse(matcher.matches(targetObject));
 	}
 	
 	@Test
 	public void shouldNotMatchObjectWithNoMatchingTerm() throws Exception {
 		Term term = new Term("weight", Term.TermType.EXPRESSION, "17 + X");
 		EngineContext context = new EngineContext(null);
 		Map<String, Object> variablesMap = context.getGobalVariablesMap();
 		variablesMap.put("X", 2);
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(context, term);
 		targetObject.setAge(-1);
 		assertFalse(matcher.matches(targetObject));
 	}
 	
 	@Test(expected = MatchingException.class)
 	public void shouldRegisterAnExceptionWhenThereIsAFailEvaluatingTheExpression() throws Exception {
		Term term = new Term("height", Term.TermType.EXPRESSION, "17 + ");
 		EngineContext context = new EngineContext(null);
 		Map<String, Object> variablesMap = context.getGobalVariablesMap();
 		//variablesMap.put("X", 2);
 		Person targetObject = new Person("John");
 		TermMatcher matcher = TermMatcher.Factory.buildTermMatcher(context, term);
 		targetObject.setAge(-1);
 		matcher.matches(targetObject);
 	}
 }
