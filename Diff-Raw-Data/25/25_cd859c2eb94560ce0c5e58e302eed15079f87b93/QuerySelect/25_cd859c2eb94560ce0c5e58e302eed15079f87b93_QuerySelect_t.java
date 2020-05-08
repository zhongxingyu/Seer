 package com.xaf.sql.query;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import javax.naming.*;
 
 import org.w3c.dom.*;
 
 import com.xaf.db.*;
 import com.xaf.report.*;
 import com.xaf.value.*;
 
 public class QuerySelect
 {
 	private QueryDefinition queryDefn;
 	private String name;
 	private String caption;
 	private boolean distinctRows;
 	private boolean isDirty;
     private boolean alwaysDirty;
 	private List reportFields = new ArrayList();
 	private List orderBy = new ArrayList();
 	private List conditions = new ArrayList();
 	private List whereExprs;
 	private List errors;
 
 	private String selectSql;
 	private List bindParams;
 
     public QuerySelect(QueryDefinition queryDefn)
     {
 		this.queryDefn = queryDefn;
 		this.isDirty = true;
 		this.distinctRows = true;
     }
 
     public void setAlwaysDirty(boolean flag) { alwaysDirty = flag; }
 	public String getName() { return name; }
 	public String getCaption() { return caption; }
 	public boolean distinctRowsOnly() { return distinctRows; }
 	public QueryDefinition getQueryDefn() { return queryDefn; }
 	public List getReportFields() { return reportFields; }
 	public List getConditions() { return conditions; }
 	public List getWhereExpressions() { return whereExprs; }
 	public List getOrderBy() { return orderBy; }
 	public List getErrors() { return errors; }
 
 	public List getBindParams() { return bindParams; }
     public String getErrorSql() { return selectSql; }
 	public String getSql(ValueContext vc)
 	{
 		if(isDirty || alwaysDirty)
 		{
 			SelectStmtGenerator selectStmt = new SelectStmtGenerator(this);
 			selectSql = selectStmt.toString(vc);
 			if(! selectStmt.isValid())
 				return null;
 
 			bindParams = selectStmt.getBindParams();
 			isDirty = false;
 		}
 		return selectSql;
 	}
 
     public String getBindParamsDebugHtml(ValueContext vc)
     {
 		if(bindParams == null)
 			return "NONE";
 
         StringBuffer result = new StringBuffer();
 		result.append("<p><br>BIND PARAMETERS:<ol>");
 
 		int bindCount = bindParams.size();
 		if(bindCount > 0)
 		{
             for(int i = 0; i < bindCount; i++)
             {
                 Object bindObj = bindParams.get(i);
                 if (bindObj instanceof ListValueSource)
                 {
                     ListValueSource vs = (ListValueSource) bindObj;
                     String[] values = vs.getValues(vc);
                     for (int j=0; j < values.length; j++)
                     {
                         result.append("<li><code><b>");
                         result.append(vs.getId());
                         result.append("</b> = ");
                         result.append(values[j]);
                         result.append("</code></li>");
                     }
                 }
                 else
                 {
                     SingleValueSource vs = (SingleValueSource) bindObj;
                     result.append("<li><code><b>");
                     result.append(vs.getId());
                     result.append("</b> = ");
                     result.append(vs.getValue(vc));
                     result.append("</code></li>");
                 }
 
             }
 		}
 
         result.append("</ol>");
         return result.toString();
     }
 
 	public void addError(String group, String message)
 	{
 		if(errors == null) errors = new ArrayList();
 		errors.add(group + ": " + message);
 		isDirty = true;
 	}
 
 	public void addReportField(QueryField field)
 	{
 		reportFields.add(field);
 		isDirty = true;
 	}
 
 	public void addReportField(String fieldName)
 	{
 		if(fieldName.equals("*"))
 		{
 			List fields = queryDefn.getFieldsList();
 			for(Iterator i = fields.iterator(); i.hasNext(); )
 				addReportField((QueryField) i.next());
 		}
 		else
 		{
 			QueryField field = queryDefn.getField(fieldName);
 			if(field == null)
 				addError("query-select-addField", "field '"+ fieldName +"' not found");
 			else
 				addReportField(field);
 		}
 	}
 
 	public void addReportFields(String[] fieldNames)
 	{
 		for(int i = 0; i < fieldNames.length; i++)
 			addReportField(fieldNames[i]);
 	}
 
 	public void addOrderBy(QuerySortFieldRef field)
 	{
 		orderBy.add(field);
 		if(field.isStatic())
 			isDirty = true;
 		else
 			alwaysDirty = true;
 	}
 
 	public void addOrderBy(String fieldName, boolean descending)
 	{
 		QuerySortFieldRef sortRef = new QuerySortFieldRef(queryDefn, fieldName);
 		if(descending)
 			sortRef.setDescending();
 
 		if(sortRef.isStatic())
 		{
 			QueryField[] fields = sortRef.getFields(null);
 			for(int i = 0; i < fields.length; i++)
 			{
 				if(fields[i] == null)
 				{
 					addError("query-select-addOrderBy", "field '"+ fieldName +"' not found");
 					break;
 				}
 			}
 		}
 
 		addOrderBy(sortRef);
 	}
 
 	public void addOrderBy(String[] fieldNames)
 	{
 		for(int i = 0; i < fieldNames.length; i++)
 			addOrderBy(fieldNames[i], false);
 	}
 
 	public void addCondition(QueryCondition condition)
 	{
         if(condition.removeIfValueIsNull())
             alwaysDirty = true;
 
 		conditions.add(condition);
 		isDirty = true;
 	}
 
 	public void addCondition(String fieldName, String comparison, String value, String connector)
 	{
 		boolean isValid = true;
 		QueryField field = queryDefn.getField(fieldName);
 		if(field == null)
 		{
 			if(errors == null) errors = new ArrayList();
 			errors.add("select-condition-field: field '"+ fieldName +"' not found");
 			isValid = false;
 		}
 
 		SqlComparison comp = SqlComparisonFactory.getComparison(comparison);
 		if(comp == null)
 		{
 			if(errors == null) errors = new ArrayList();
 			errors.add("select-condition-comparison: comparison '"+ comparison +"' not found");
 			isValid = false;
 		}
 
 		if(isValid)
 			addCondition(new QueryCondition(field, comp, value, connector));
 	}
 
 	public void addWhereExpr(SqlWhereExpression expr)
 	{
 		if(whereExprs == null)
 			whereExprs = new ArrayList();
 		whereExprs.add(expr);
 		isDirty = true;
 	}
 
 	public void addWhereExpr(String expr, String connector)
 	{
 		addWhereExpr(new SqlWhereExpression(expr, connector));
 	}
 
 	public ResultSet execute(DatabaseContext dc, ValueContext vc, Object[] overrideParams) throws NamingException, SQLException
 	{
 		if(getSql(vc) == null)
 			return null;
 
         String dataSourceId = queryDefn.getDataSource() != null ?queryDefn.getDataSource().getValue(vc) : null;
 		Connection conn = dc.getConnection(vc, dataSourceId);
 		int rsType = dc.getScrollableResultSetType(conn);
 
         PreparedStatement stmt = null;
         stmt =
             rsType == DatabaseContext.RESULTSET_NOT_SCROLLABLE ?
                 dc.getConnection(vc, dataSourceId).prepareStatement(selectSql) :
                 dc.getConnection(vc, dataSourceId).prepareStatement(selectSql, rsType, ResultSet.CONCUR_READ_ONLY);
 
 		if(overrideParams != null)
 		{
 			for(int i = 0; i < overrideParams.length; i++)
 				stmt.setObject(i+1, overrideParams[i]);
 		}
 		else
 		{
 			int paramsCount = bindParams.size();
            int index = 1;
            // the 'paramsCount' does not represent the actual number of bind
            // parameters. Each entry of paramCount might be a ListValueSource
            // which will contain additional bind params.
			for (int i=0; i < paramsCount;i++)
 			{
                 Object bindObj = bindParams.get(i);
                 if (bindObj instanceof ListValueSource)
                 {
                     // if its a ListValueSource, loop and get the values
                     String[] values = ((ListValueSource) bindObj).getValues(vc);
                    int q;
			        for(q = 0; q < values.length; q++)
 			        {
				        stmt.setString(index+q, values[q]);
 			        }
                    index = index + q;
                 }
                 else
                 {
    				stmt.setString(index, ((SingleValueSource) bindObj).getValue(vc));
                    index++;
                 }
 

 			}
 		}
 
 		if(stmt.execute())
 			return stmt.getResultSet();
 		else
 			return null;
 	}
 
 	public ResultSet execute(DatabaseContext dc, ValueContext vc) throws NamingException, SQLException
 	{
 		return execute(dc, vc, null);
 	}
 
 	public List inherit(List dest, List source)
 	{
 		if(source == null)
 		    return dest;
 
 		if(dest == null)
 			dest = new ArrayList();
 
 		int len = source.size();
 		for(int i = 0; i < len; i++)
 			dest.add(source.get(i));
 
 		return dest;
 	}
 
 	public void importFromSelect(QuerySelect select)
 	{
 		distinctRows = select.distinctRowsOnly();
 
 		inherit(reportFields, select.getReportFields());
 		inherit(conditions, queryDefn.getDefaultConditions());
 		inherit(conditions, select.getConditions());
 		inherit(orderBy, select.getOrderBy());
 
 		// whereExprs and errors can be null, so treat them special
 
 		whereExprs = inherit(whereExprs, select.getWhereExpressions());
 		whereExprs = inherit(whereExprs, queryDefn.getWhereExprs());
 		errors = inherit(errors, select.getErrors());
 	}
 
 	public void importFromXml(Element elem)
 	{
 		Map queryFields = queryDefn.getFieldsMap();
 
 		name = elem.getAttribute("id");
 		caption = elem.getAttribute("caption");
 		String value = elem.getAttribute("distinct");
 		if(value != null && value.equals("no"))
 			distinctRows = false;
 
 		NodeList children = elem.getChildNodes();
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
 			String childName = node.getNodeName();
 			if(childName.equals("display"))
 			{
 				addReportField(((Element) node).getAttribute("field"));
 			}
 			else if(childName.equals("order-by"))
 			{
 				Element obElem = (Element) node;
 				addOrderBy(obElem.getAttribute("field"), obElem.getAttribute("descending").equals("yes") ? true : false);
 			}
 			else if(childName.equals("condition"))
 			{
 				QueryCondition cond = new QueryCondition();
 				cond.importFromXml(queryDefn, (Element) node);
 				addCondition(cond);
 			}
 			else if(childName.equals("where-expr"))
 			{
 				SqlWhereExpression expr = new SqlWhereExpression();
 				expr.importFromXml((Element) node);
 				addWhereExpr(expr);
 			}
 		}
 	}
 }
