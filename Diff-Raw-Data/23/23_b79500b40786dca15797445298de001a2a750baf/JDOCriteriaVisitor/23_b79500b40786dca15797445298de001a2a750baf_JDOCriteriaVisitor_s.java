 /*
  * Copyright (c) 2008-2011 Ivan Khalopik.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.greatage.domain.jdo;
 
 import org.greatage.domain.AbstractCriteriaVisitor;
 import org.greatage.domain.Criteria;
 import org.greatage.domain.Entity;
 import org.greatage.domain.JunctionCriteria;
 import org.greatage.domain.PropertyCriteria;
 import org.greatage.domain.SortCriteria;
 
 import javax.jdo.Query;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class JDOCriteriaVisitor<PK extends Serializable, E extends Entity<PK>>
 		extends AbstractCriteriaVisitor<PK, E> {
 	private final Map<String, Object> parameters = new HashMap<String, Object>();
 	private final List<String> names = new ArrayList<String>();
 
 	private final Query query;
 	private List<String> junction = new ArrayList<String>();
 	private int level;
 
 	public JDOCriteriaVisitor(final Query query) {
 		this.query = query;
 	}
 
 	@Override
 	public void visit(final Criteria<PK, E> criteria) {
 		level++;
 		super.visit(criteria);
 		level--;
		if (level == 0) {
 			query.setFilter(getJunction(junction, " && "));
 		}
 	}
 
 	public Map<String, Object> getParameters() {
 		return parameters;
 	}
 
 	@Override
 	protected void visitJunction(final JunctionCriteria<PK, E> criteria) {
 		final List<String> parent = this.junction;
 		junction = new ArrayList<String>();
 
 		for (Criteria<PK, E> child : criteria.getChildren()) {
 			visit(child);
 		}
 
 		final List<String> temp = junction;
 		junction = parent;
 
 		final String function = criteria.getOperator() == JunctionCriteria.Operator.AND ? " && " : " || ";
 		final String filter = getJunction(temp, function);
 		addCriterion(filter, criteria.isNegative());
 	}
 
 	@Override
 	protected void visitProperty(final PropertyCriteria<PK, E> criteria) {
 		final StringBuilder criterion = new StringBuilder();
 
 		// in expression workaround
 		if (criteria.getOperator() == PropertyCriteria.Operator.IN) {
 			final String parameterName = allocateName(criteria.getProperty());
 			criterion.append(":").append(parameterName).append(".contains(");
 			if (criteria.getPath() != null) {
 				criterion.append(criteria.getPath()).append('.');
 			}
 			criterion.append(criteria.getProperty());
 			criterion.append(")");
 
 			addCriterion(criterion.toString(), criteria.isNegative());
 			parameters.put(parameterName, criteria.getValue());
 			return;
 		}
 
 		if (criteria.getPath() != null) {
 			criterion.append(criteria.getPath()).append('.');
 		}
 		criterion.append(criteria.getProperty());
 		switch (criteria.getOperator()) {
 			case EQUAL:
 				criterion.append(" == ");
 				break;
 			case NOT_EQUAL:
 				criterion.append(" != ");
 				break;
 			case GREATER_THAN:
 				criterion.append(" > ");
 				break;
 			case GREATER_OR_EQUAL:
 				criterion.append(" >= ");
 				break;
 			case LESS_THAN:
 				criterion.append(" < ");
 				break;
 			case LESS_OR_EQUAL:
 				criterion.append(" <= ");
 				break;
 			case LIKE:
 				break;
 		}
 		final String parameterName = allocateName(criteria.getProperty());
 		criterion.append(":").append(parameterName);
 		addCriterion(criterion.toString(), criteria.isNegative());
 		parameters.put(parameterName, criteria.getValue());
 	}
 
 	@Override
 	protected void visitSort(final SortCriteria<PK, E> criteria) {
 		//To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	private void addCriterion(final String criterion, final boolean negative) {
 		junction.add(negative ? "!(" + criterion + ")" : criterion);
 	}
 
 	private String getJunction(final List<String> children, final String function) {
 		if (children.isEmpty()) {
 			return "";
 		}
 		if (children.size() == 1) {
 			return children.get(0);
 		}
 		final StringBuilder criterion = new StringBuilder();
 		for (String child : children) {
 			if (criterion.length() > 0) {
 				criterion.append(function);
 			}
 			criterion.append(child);
 		}
 		return criterion.toString();
 	}
 
 	private String allocateName(final String name) {
 		String result = name + "_";
 		int i = 0;
 		while (names.contains(result)) {
 			result = name + "_" + i++;
 		}
 		names.add(result);
 		return result;
 	}
 }
