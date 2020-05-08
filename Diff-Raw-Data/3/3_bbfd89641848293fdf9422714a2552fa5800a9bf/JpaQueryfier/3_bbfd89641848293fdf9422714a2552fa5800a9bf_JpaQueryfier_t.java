 package org.jpaqueryfier;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 public class JpaQueryfier {
 
 	private EntityManager em;
 	private String sql;
 	private QueryParameters parameters = new QueryParameters();
 	private boolean allowNulls = false;
 
 	public JpaQueryfier(String sql) {
 		this.sql = sql;
 		parameters.addFrom(sql);
 	}
 
 	public JpaQueryfier(String sql, EntityManager em) {
 		this(sql);
 		this.em = em;
 	}
 
 	public Query queryfy() {
 		sql = new SQLMicroprocessor(sql, parameters, allowNulls).removeNullParameters();
 		Query query = em.createQuery(sql);
 
 		return definedParametersFor(query);
 	}
 
 	public Query queryfyNative() {
 		sql = new SQLMicroprocessor(sql, parameters, allowNulls).removeNullParameters();
 		Query query = em.createNativeQuery(sql);
 
 		return definedParametersFor(query);
 	}
 
 	public JpaQueryfier allowingNulls() {
 		this.allowNulls = true;
 		return this;
 	}
 
 	public JpaQueryfier with(Object value) {
 		if (value instanceof QueryParameter)
 			return withQueryParameter((QueryParameter) value);
 
 		for (QueryParameter parameter : parameters.get())
 			if (parameter.valueIsNull() && parameter.isNotAlreadyAppended()) {
 				parameter.setValueAndAppend(value);
 				break;
 			}
 		return this;
 	}
 
 	public JpaQueryfier withQueryParameter(QueryParameter queryParameter) {
 		parameters.removeIfAlreadyAdded(queryParameter);
 		parameters.add(queryParameter);
 		queryParameter.append();
 		return this;
 	}
 
 	public JpaQueryfier and(Object value) {
 		return with(value);
 	}
 
 	public List<QueryParameter> getParameters() {
 		return parameters.get();
 	}
 
 	public QueryParameter getParameter(String name) {
 		return parameters.get(name);
 	}
 
 	public String getSql() {
 		return sql;
 	}
 
 	private Query definedParametersFor(Query query) {
 		for (QueryParameter parameter : parameters.get())
			if (parameter.getValue() != null)
				query.setParameter(parameter.getName(), parameter.getValue());
 		return query;
 	}
 
 }
