 /*
  * DynaQueryBuilder.java created on 2011-01-30
  *
  * Created by Brushing Bits Labs
  * http://www.brushingbits.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.brushingbits.jnap.persistence.hibernate;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.Criteria;
 import org.hibernate.QueryException;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.util.Assert;
 
 /**
  * @author Daniel Rochetti
  * @since 1.0
  */
 public class DynaQueryBuilder {
 
 	private static final String BETWEEN = "Between";
 	private static final String EQUAL = "Equal";
 	private static final String GREATER_THAN = "GreaterThan";
 	private static final String GREATER_THAN_OR_EQUAL = "GreaterThanEqual";
 	private static final String IS_EMPTY = "IsEmpty";
 	private static final String IS_NOT_EMPTY = "IsNotEmpty";
 	private static final String IS_NOT_NULL = "IsNotNull";
 	private static final String IS_NULL = "IsNull";
	private static final String LESS_THAN = "LessThan";
	private static final String LESS_THAN_OR_EQUAL = "LessThanEqual";
	private static final String LIKE = "Like";
	private static final String LIKE_IC = "LikeIc";
	private static final String NOT_EQUAL = "NotEqual";
 
 	private static Pattern DYNA_QUERY_PATTERN = Pattern.compile("^(findBy|countBy|findUniqueBy)([A-Z]\\w*)");
 
 	private static Pattern DYNA_QUERY_OPERATOR_PATTERN = Pattern.compile("Or|And");
 
 	private static Pattern DYNA_QUERY_EXPRESSION_PATTERN = Pattern.compile(StringUtils.join(new String[] {
 			BETWEEN, EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IS_EMPTY, IS_NOT_EMPTY,
 			IS_NOT_NULL, IS_NULL, LESS_THAN, LESS_THAN_OR_EQUAL, LIKE, LIKE_IC, NOT_EQUAL
 	}, '|'));
 
 	private Session session;
 	private String entityName;
 	private String dynaQuery;
 	private Object[] params;
 	private int propertyValueIndex = 0;
 	private Map<String, DynaQueryBuilder.CriterionBuilder> criterionBuilderMap;
 
 	private DynaQueryBuilder(Session session, String entityName, String dynaQuery, Object... params) {
 		Assert.notNull(session);
 		Assert.hasText(entityName);
 		Assert.hasText(dynaQuery);
 		this.session = session;
 		this.entityName = entityName;
 		this.dynaQuery = dynaQuery;
 		this.params = params == null ? ArrayUtils.EMPTY_OBJECT_ARRAY : params;
 		buildCriterionBuilderStrategy();
 	}
 
 	/**
 	 * 
 	 */
 	private void buildCriterionBuilderStrategy() {
 		this.criterionBuilderMap = new HashMap<String, DynaQueryBuilder.CriterionBuilder>();
 		this.criterionBuilderMap.put(BETWEEN, new BetweenCriterionBuilder());
 		this.criterionBuilderMap.put(EQUAL, new EqualCriterionBuilder());
 		this.criterionBuilderMap.put(GREATER_THAN, new GreaterThanCriterionBuilder());
 		this.criterionBuilderMap.put(GREATER_THAN_OR_EQUAL, new GreaterThanEqualCriterionBuilder());
 		this.criterionBuilderMap.put(IS_EMPTY, new IsEmptyCriterionBuilder());
 		this.criterionBuilderMap.put(IS_NOT_EMPTY, new IsNotEmptyCriterionBuilder());
 		this.criterionBuilderMap.put(IS_NOT_NULL, new IsNotNullCriterionBuilder());
 		this.criterionBuilderMap.put(IS_NULL, new IsNullCriterionBuilder());
 		this.criterionBuilderMap.put(LESS_THAN, new LessThanCriterionBuilder());
 		this.criterionBuilderMap.put(LESS_THAN_OR_EQUAL, new LessThanEqualCriterionBuilder());
 		this.criterionBuilderMap.put(LIKE, new LikeCriterionBuilder(false));
 		this.criterionBuilderMap.put(LIKE_IC, new LikeCriterionBuilder(true));
 		this.criterionBuilderMap.put(NOT_EQUAL, new NotEqualCriterionBuilder());
 	}
 
 	public Criteria build() throws QueryException {
 		Matcher matcher = DYNA_QUERY_PATTERN.matcher(this.dynaQuery);
 		if (!matcher.matches()) {
 			throw new QueryException("The DynaQuery syntax is incorrect. It must start with 'findBy' " +
 					"or 'countBy' expression followed by property expressions and operators.", this.dynaQuery);
 		}
 		Criteria criteria = this.createCriteria(matcher.group(1).equals("countBy"));
 		final String dynaQueryExpression = matcher.group(2);
 		String[] properties = DYNA_QUERY_OPERATOR_PATTERN.split(dynaQueryExpression);
 		Matcher logicalOperatorsMatcher = DYNA_QUERY_OPERATOR_PATTERN.matcher(dynaQueryExpression);
		Criterion currentCondition = null;
 		for (String property : properties) {
//			criteria.add(this.createCriterion(property));
 //			if (logicalOperatorsMatcher.find()) {
 //				criteria.
//			} else {
//				
 //			}
//			criteria.add(condition);
 		}
 		return criteria;
 	}
 
 	private Criteria createCriteria(boolean countQuery) {
 		Criteria criteria = this.session.createCriteria(entityName);
 		if (countQuery) {
 			criteria.setProjection(Projections.rowCount());
 		}
 		return criteria;
 	}
 
 	private Criterion createCriterion(String property) {
 		Matcher matcher = DYNA_QUERY_EXPRESSION_PATTERN.matcher(property);
 		String propertyName = property;
 		String criterionType = EQUAL;
 		if (matcher.matches()) {
 			criterionType = matcher.group();
 			propertyName = StringUtils.remove(propertyName, criterionType);
 		}
 		propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
 		return this.criterionBuilderMap.get(criterionType).build(propertyName);
 	}
 
 	Object getNextPropertyValue() {
 		return getPropertyValues(1)[0];
 	}
 
 	Object[] getPropertyValues(int size) {
 		if (this.params.length < (propertyValueIndex + size)) {
 			Object[] values = ArrayUtils.subarray(this.params, propertyValueIndex, propertyValueIndex + size);
			propertyValueIndex += size;
 			return values;
 		} else {
 			throw new QueryException(MessageFormat.format("The number of parameters ({0}) does not "
 					+ "satisfy DynaQuery expression", Integer.toString(this.params.length)), this.dynaQuery);
 		}
 	}
 
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private interface CriterionBuilder {
 
 		/**
 		 * 
 		 * @param property
 		 * @return
 		 */
 		Criterion build(String property);
 
 	}
 
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class BetweenCriterionBuilder implements CriterionBuilder {
 
 		public Criterion build(String propertyName) {
 			return Restrictions.between(propertyName, DynaQueryBuilder.this.getNextPropertyValue(),
 					DynaQueryBuilder.this.getNextPropertyValue());
 		}
 
 	}
 
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class EqualCriterionBuilder implements CriterionBuilder {
 
 		public Criterion build(String propertyName) {
 			return Restrictions.eq(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 
 	}
 
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class GreaterThanCriterionBuilder implements CriterionBuilder {
 
 		public Criterion build(String propertyName) {
 			return Restrictions.gt(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 
 	}
 
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class GreaterThanEqualCriterionBuilder implements CriterionBuilder {
 
 		public Criterion build(String propertyName) {
 			return Restrictions.ge(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class IsEmptyCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.isEmpty(propertyName);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class IsNotEmptyCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.isNotEmpty(propertyName);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class IsNotNullCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.isNotNull(propertyName);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class IsNullCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.isNull(propertyName);
 		}
 		
 	}
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class LessThanCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.lt(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class LessThanEqualCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.le(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class LikeCriterionBuilder implements CriterionBuilder {
 
 		private boolean ignoreCase;
 
 		public LikeCriterionBuilder(boolean ignoreCase) {
 			this.ignoreCase = ignoreCase;
 		}
 
 		public Criterion build(String propertyName) {
 			Object propertyValue = DynaQueryBuilder.this.getNextPropertyValue();
 			if (!(propertyValue instanceof String)) {
 				throw new QueryException("Error creating DynaQuery. You can only use 'Like' "
 						+ "operator on String properties.",	DynaQueryBuilder.this.dynaQuery);
 			}
 			return this.ignoreCase
 				? Restrictions.ilike(propertyName, (String) propertyValue, MatchMode.ANYWHERE)
 				: Restrictions.like(propertyName, (String) propertyValue, MatchMode.ANYWHERE);
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @author Daniel Rochetti
 	 * @since 1.0
 	 */
 	private class NotEqualCriterionBuilder implements CriterionBuilder {
 		
 		public Criterion build(String propertyName) {
 			return Restrictions.ne(propertyName, DynaQueryBuilder.this.getNextPropertyValue());
 		}
 		
 	}
 
 }
