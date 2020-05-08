 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *    
  *******************************************************************************/
 package org.ebayopensource.turmeric.policyservice.model;
 
 import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
 
 /**
  * The Class RuleDAOImpl.
  * 
  * @author muguerza
  */
 public class RuleDAOImpl extends AbstractDAO implements RuleDAO {
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#persistRule(org.ebayopensource.turmeric.policyservice.model.Rule)
 	 */
 	@Override
 	public void persistRule(Rule rule) {
 		persistEntity(rule);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#persistCondition(org.ebayopensource.turmeric.policyservice.model.Condition)
 	 */
 	@Override
 	public void persistCondition(Condition condition) {
 		persistEntity(condition);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#persistExpression(org.ebayopensource.turmeric.policyservice.model.Expression)
 	 */
 	@Override
 	public void persistExpression(Expression expression) {
 		persistEntity(expression);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#persistPrimitiveValue(org.ebayopensource.turmeric.policyservice.model.PrimitiveValue)
 	 */
 	@Override
 	public void persistPrimitiveValue(PrimitiveValue primitiveValue) {
 		persistEntity(primitiveValue);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findRuleById(long)
 	 */
 	@Override
 	public Rule findRuleById(long id) {
 		return findEntity(Rule.class, id);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findConditionById(long)
 	 */
 	@Override
 	public Condition findConditionById(long conditionId) {
 		return findEntity(Condition.class, conditionId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findExpressionById(long)
 	 */
 	@Override
 	public Expression findExpressionById(long expressionId) {
 		return findEntity(Expression.class, expressionId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findPrimitiveValueById(long)
 	 */
 	@Override
 	public PrimitiveValue findPrimitiveValueById(long primitiveValueId) {
 		return findEntity(PrimitiveValue.class, primitiveValueId);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findConditionByRuleId(long)
 	 */
 	@Override
 	public Condition findConditionByRuleId(long ruleId) {
 		Rule rule = findRuleById(ruleId);
 		if (rule != null && rule.getCondition() != null) {
 			return rule.getCondition();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findExpressionByConditionId(long)
 	 */
 	@Override
 	public Expression findExpressionByConditionId(long conditionId) {
 		Condition condition = findConditionById(conditionId);
 		if (condition != null && condition.getExpression() != null) {
 			return condition.getExpression();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findPrimitiveValueByExpressionId(long)
 	 */
 	@Override
 	public PrimitiveValue findPrimitiveValueByExpressionId(long expressionId) {
 		Expression expression = findExpressionById(expressionId);
 		if (expression != null && expression.getPrimitiveValue() != null) {
 			return expression.getPrimitiveValue();
 		}
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findExpressionByName(java.lang.String)
 	 */
 	@Override
 	public Expression findExpressionByName(String expressionName) {
 		return getSingleResultOrNull(Expression.class, "name", expressionName);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#removeRule(long)
 	 */
 	@Override
 	public void removeRule(long ruleId) {
 		removeEntity(Rule.class, ruleId);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#removePrimitiveValue(long)
 	 */
 	@Override
 	public void removePrimitiveValue(long primitiveValueId) {
 		removeEntity(PrimitiveValue.class, primitiveValueId);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#findRuleByName(java.lang.String)
 	 */
 	@Override
 	public Rule findRuleByName(String name) {
 		return getSingleResultOrNull(Rule.class, "ruleName", name);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#isRuleNameUsed(java.lang.String)
 	 */
 	@Override
 	public boolean isRuleNameUsed(String ruleName) {
 		return findRuleByName(ruleName) != null;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policyservice.model.RuleDAO#isRuleValid(org.ebayopensource.turmeric.policyservice.model.Rule, boolean)
 	 */
 	@Override
 	public boolean isRuleValid(Rule rule,boolean allowNull) {
 		// value should not be null as well as the type
 		// rulename required
 		boolean valid = false;
 		if(rule != null
 				&& rule.getCondition() != null
 				&& rule.getCondition().getExpression() != null
 				&& rule.getCondition().getExpression().getPrimitiveValue() != null
 				&& rule.getCondition().getExpression().getPrimitiveValue() != null
 				&& rule.getCondition().getExpression().getPrimitiveValue()
 						.getValue() != null
 				&& rule.getCondition().getExpression().getPrimitiveValue()
 						.getValue().length() > 0
 				&& rule.getCondition().getExpression().getPrimitiveValue()
 						.getType() != null 
 				&& (rule.getRuleName() != null || rule.getRuleName().length() != 0)
 				){
 			String conditionRule = rule.getCondition().getExpression().getPrimitiveValue().getValue();
 			valid = isValidStructure(conditionRule);
 			
 		}
 			
 		return valid;
 	}
 
 	private boolean isValidStructure(final String conditionRule) {
 		boolean flag = true;
 		if (conditionRule != null) {
 			String[] operands;
 
 				if (conditionRule.contains("||")) {
 					operands = conditionRule.split("\\|\\|");
 
 					for (String operand : operands) {
 						operand = operand.trim();
						if ( ! (operand.trim().matches("\\w+(:\\w+)?.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
								operand.trim().matches("\\w+:\\w+.SubjectGroup.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
								operand.trim().matches("\\w+:\\w+.SubjectGroup.Subject.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
								operand.trim().matches("\\w+:hits[>,<,==,=>,>=,<=,=<][0-9]+")	|| 
								operand.trim().matches("HITS[>,<,==,=>,>=,<=,=<][0-9]+")) || 
 								! isValidCondition(operand)) {
 							return false;
 						}
 					}
 				}else{
 					if (conditionRule.contains("&&")) {
 						operands = conditionRule.split("\\&\\&");
 						
 						for (String operand : operands) {
 							operand = operand.trim();
							if ( ! (operand.trim().matches("\\w+(:\\w+)?.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
									operand.trim().matches("\\w+:\\w+.SubjectGroup.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
									operand.trim().matches("\\w+:\\w+.SubjectGroup.Subject.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
									operand.trim().matches("\\w+:hits[>,<,==,=>,>=,<=,=<][0-9]+")	|| 
									operand.trim().matches("HITS[>,<,==,=>,>=,<=,=<][0-9]+")) || 
 									! isValidCondition(operand)) {
 								return false;
 							}
 						}
					}else if ( ! (conditionRule.trim().matches("\\w+(:\\w+)?.count[>,<,==,=>,>=,<=,=<][0-9]+") ||   
								conditionRule.trim().matches("\\w+:\\w+.SubjectGroup.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
								conditionRule.trim().matches("\\w+:\\w+.SubjectGroup.Subject.count[>,<,==,=>,>=,<=,=<][0-9]+") ||
								conditionRule.trim().matches("\\w+:hits[>,<,==,=>,>=,<=,=<][0-9]+")	|| 
								conditionRule.trim().matches("HITS[>,<,==,=>,>=,<=,=<][0-9]+")) || 
 								! isValidCondition(conditionRule)) {
 							return false;
 						}
 					
 				}
 		}
 		return flag;
 	}
 	
 	private boolean isValidCondition(final String value) {
 		boolean flag =false;
 		if( value!=null ){
 				String[] expression = {">","<" ,"==" ,"=>",">=","<=","=<"};
 				String[] words;
 				for(String val:expression){
 						if(value ==null){
 							break;
 						}
 						if (value.contains(val)) {
 							words = value.split(val);
 							if(words[1]!=null){
 								words[1] = words[1].trim();
 							}
 							if(words[0]!=null){
 								words[0] = words[0].trim();
 							}
 							try{
 								Integer.valueOf(words[0]);
 								flag = true;
 							}catch (NumberFormatException e) {
 								try{
 									 Integer.valueOf(words[1].trim());
 									 flag = true;
 								}catch (NumberFormatException e1) {
 								}
 							}
 						}
 				}
 		}
 		return flag;
 	}
 }
