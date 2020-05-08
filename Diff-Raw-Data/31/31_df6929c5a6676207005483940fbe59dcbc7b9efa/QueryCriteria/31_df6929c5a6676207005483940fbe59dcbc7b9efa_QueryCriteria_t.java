 package cz.cuni.mff.odcleanstore.webfrontend.dao;
 
import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 import cz.cuni.mff.odcleanstore.util.Pair;
 
 /**
  * 
  * @author Dusan
  *
  */
public class QueryCriteria implements Cloneable, Serializable
 {
	private static final long serialVersionUID = 1L;

 	public enum SortOrder { ASC, DESC };
 	
 	private List<Pair<String, Object>> whereClauses;
 	private List<Pair<String, SortOrder>> orderByClauses;
 	
 	/**
 	 * 
 	 */
 	public QueryCriteria()
 	{
 		this.whereClauses = new LinkedList<Pair<String, Object>>();
 		this.orderByClauses = new LinkedList<Pair<String, SortOrder>>();
 	}
 	
 	/**
 	 * 
 	 * @param column
 	 * @param value
 	 */
 	public void addWhereClause(String column, Object value)
 	{
 		this.whereClauses.add(new Pair<String, Object>(column, value));
 	}
 	
 	/**
 	 * 
 	 * @param column
 	 */
 	public void addOrderByClause(String column, SortOrder order)
 	{
 		this.orderByClauses.add(new Pair<String, SortOrder>(column, order));
 	}
 	
 	/**
 	 * 
 	 * @param column
 	 * @param ascending
 	 */
 	public void addOrderByClause(String column, boolean ascending)
 	{
 		SortOrder order = ascending ? SortOrder.ASC : SortOrder.DESC;
 		addOrderByClause(column, order);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String buildWhereClause()
 	{
 		if (whereClauses.isEmpty())
 			return "";
 		
 		StringBuilder builder = new StringBuilder();
 
 		String columnName = whereClauses.get(0).getFirst();
 		builder.append(" WHERE " + columnName + " = ?");
 		
 		for (int i = 1; i < whereClauses.size(); i++)
 		{
 			columnName = whereClauses.get(i).getFirst();
 			builder.append(" AND " + columnName + " = ?");
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Object[] buildWhereClauseParams()
 	{
 		List<Object> result = new LinkedList<Object>();
 		
 		for (Pair<String, Object> criterion : whereClauses)
 			result.add(criterion.getSecond());
 		
 		return result.toArray();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String buildOrderByClause()
 	{
 		if (orderByClauses.isEmpty())
 			return "";
 		
 		StringBuilder builder = new StringBuilder();
 
 		String columnName = orderByClauses.get(0).getFirst();
 		String sortOrder = orderByClauses.get(0).getSecond().toString();
 		builder.append(" ORDER BY " + columnName + " " + sortOrder);
 		
 		for (int i = 1; i < orderByClauses.size(); i++)
 		{
 			columnName = orderByClauses.get(i).getFirst();
 			sortOrder = orderByClauses.get(i).getSecond().toString();
 			
 			builder.append(", " + columnName + " " + sortOrder);
 		}
 		
 		return builder.toString();
 	}
 	
 	@Override
 	public Object clone()
 	{
 		QueryCriteria clone = new QueryCriteria();
 		clone.whereClauses = new LinkedList<Pair<String, Object>>(this.whereClauses);
 		clone.orderByClauses = new LinkedList<Pair<String, SortOrder>>(this.orderByClauses);
 		return clone;
 	}
 }
