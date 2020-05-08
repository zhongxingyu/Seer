 package org.liveSense.misc.queryBuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.liveSense.misc.queryBuilder.clauses.LimitClause;
 import org.liveSense.misc.queryBuilder.clauses.OrderByClause;
 import org.liveSense.misc.queryBuilder.exceptions.QueryBuilderException;
 import org.liveSense.misc.queryBuilder.operators.AndOperator;
 import org.liveSense.misc.queryBuilder.operators.Operator;
 
 public abstract class QueryBuilder {
 	
 	private Object params;
 	private LimitClause limit = new LimitClause(-1, -1);
 	private List<OrderByClause> orderBy;
 		
 	public Object getParams() {
 		return params;
 	}
 	
 	public void setParams(Object params) {
 		this.params = params;
 	}
 
 	public String buildParameters() throws QueryBuilderException {
 		return buildParameters(params);
 	}
 	
 	public String buildParameters(Object params) throws QueryBuilderException {
		return buildParameters(null, params);
	}

	public String buildParameters(Class<?> clazz, Object params) throws QueryBuilderException {
 		if (params == null) return "";
 		if (!(params instanceof Operator)) {
 			params = new AndOperator(params);
 		}
		return OperatorAndCriteriaProcessor.processOperator(clazz, (Operator)params);
 	}
 
 	public LimitClause getLimit() {
 		return limit;
 	}
 
 	public void setLimit(LimitClause limit) {
 		this.limit = limit;
 	}
 
 	public List<OrderByClause> getOrderBy() {
 		return orderBy;
 	}
 
 	public void setOrderBy(List<OrderByClause> orderBy) {
 		this.orderBy = orderBy;
 	}

 	public void setOrderBy(OrderByClause[] orderBy) {
 		List<OrderByClause> orderByList = new ArrayList<OrderByClause>();		
 		for (OrderByClause orderByClause : orderBy) {
 			orderByList.add(orderByClause);
 		}
 		this.orderBy = orderByList;
 	}
 	
 	public void setOrderBy(OrderByClause orderBy) {
 		List<OrderByClause> orderByList = new ArrayList<OrderByClause>();		
 		orderByList.add(orderBy);
 		this.orderBy = orderByList;
 	}	
 	public abstract String getQuery();
 	
 	
 }
