 package org.rulemaker.engine.matcher;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.rulemaker.engine.EngineContext;
 import org.rulemaker.engine.expressions.ExpressionSolver;
import org.rulemaker.engine.expressions.exception.InvalidExpressionException;
 import org.rulemaker.engine.matcher.exception.MatchingException;
 import org.rulemaker.model.Term;
 
 public class ExpressionTermMatcher extends TermMatcher {
 	
 	private ExpressionSolver expressionSolver;
 
 	protected ExpressionTermMatcher(ExpressionSolver solver, EngineContext context,
 			Term termPattern) {
 		super(context, termPattern);
 		this.expressionSolver = solver;
 	}
 
 	@Override
 	public boolean matches(Object object) throws MatchingException {
 		Term term = getTermPattern();
 		String identifier = term.getIdentifier();
 		try {
 			Object objectValue = PropertyUtils.getProperty(object, identifier);
 			String expressionPattern = term.getExpressionValue();
 			Object expressionValue = expressionSolver.eval(getGlobalVariablesMap(), expressionPattern);
 			if (objectValue != null) {
 				return objectValue.equals(expressionValue);
 			} else {
 				return expressionValue == null;
 			}			
		} catch (InvalidExpressionException e) {
 			throw new MatchingException(e);
		} catch (Exception e) {
			// No matching property to evaluate expression, return false
			return false;
		}
 	}
 
 }
