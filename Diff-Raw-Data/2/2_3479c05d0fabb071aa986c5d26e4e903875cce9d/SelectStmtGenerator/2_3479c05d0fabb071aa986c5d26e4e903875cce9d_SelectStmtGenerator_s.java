 package com.xaf.sql.query;
 
 /**
  * Title:        The Extensible Application Platform
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.util.*;
 import com.xaf.db.*;
 import com.xaf.value.*;
 
 public class SelectStmtGenerator
 {
 	private QueryDefinition queryDefn;
 	private QuerySelect select;
 	private HashSet joins = new HashSet();
 	private List selectClause = new ArrayList();
 	private List fromClause = new ArrayList();
 	private List whereJoinClause = new ArrayList();
 	private List orderByClause = new ArrayList();
 	private List bindParams = new ArrayList();
 	private boolean valid;
 
     public SelectStmtGenerator(QuerySelect select)
     {
 		this.queryDefn = select.getQueryDefn();
 		this.select = select;
     }
 
 	public QueryDefinition getQueryDefn() { return queryDefn; }
 	public QuerySelect getQuerySelect() { return select; }
 	public List getBindParams() { return bindParams; }
 	public boolean isValid() { return valid; }
 
 	public void addJoin(QueryField field)
 	{
 		if(field == null)
 			throw new RuntimeException("Null field");
 
 		QueryJoin join = field.getJoin();
 		if(join != null && ! joins.contains(join))
 		{
 			fromClause.add(join.getFromClauseExpr());
 			String whereCriteria = join.getCriteria();
 			if(whereCriteria != null)
 				whereJoinClause.add(whereCriteria);
 			joins.add(join);
 		}
 	}
 
 	public void addParam(SingleValueSource bindParam)
 	{
 		bindParams.add(bindParam);
 	}
 
     public void addParam(ListValueSource bindParamList)
     {
         bindParams.add(bindParamList);
     }
 
 	public String toString(ValueContext vc)
 	{
 		valid = false;
 		if(queryDefn == null)
 			return "Query Definition is NULL";
 
 		StringBuffer errorMsg = new StringBuffer();
 		if(queryDefn.getErrors() != null)
 		{
 			List errors = queryDefn.getErrors();
 			for(int i = 0; i < errors.size(); i++)
 				errorMsg.append(errors.get(i) + ".\n");
 		}
 		if(select == null)
 		{
 			errorMsg.append("Query select is NULL.");
 		}
 		else
 		{
 			if(select.getErrors() != null)
 			{
 				List errors = select.getErrors();
 				for(int i = 0; i < errors.size(); i++)
 					errorMsg.append(errors.get(i) + ".\n");
 			}
 		}
 		if(errorMsg.length() > 0)
 			return errorMsg.toString();
 
 		List showFields = select.getReportFields();
 		int showFieldsCount = showFields.size();
 		for(int sf = 0; sf < showFieldsCount; sf++)
 		{
 			QueryField field = (QueryField) showFields.get(sf);
             String selClauseAndLabel = field.getSelectClauseExprAndLabel();
             if(selClauseAndLabel != null)
     			selectClause.add(field.getSelectClauseExprAndLabel());
 			addJoin(field);
 		}
 
 		List allConditions = select.getConditions();
         List usedConditions = new ArrayList();
 		int allCondsCount = allConditions.size();
 		for(int c = 0; c < allCondsCount; c++)
 		{
 			QueryCondition cond = (QueryCondition) allConditions.get(c);
             if(cond.removeIfValueIsNull())
             {
                 SingleValueSource vs = cond.getValue();
                 if (vs instanceof ListValueSource)
                 {
                     String[] values = ((ListValueSource)vs).getValues(vc);
                     if (values == null || values.length == 0 || (values.length == 1 && (values[0] == null || values[0].length() == 0)))
                         continue;
                 }
                 else
                 {
                     String value = vs.getValue(vc);
                     if(value == null || value.length() == 0)
                         continue;
                 }
             }
 
             usedConditions.add(cond);
 			QueryField field = cond.getField();
 			if(field != null)
 				addJoin(field);
 			else
 				return "Condition '"+c+"' has no field.";
 		}
 
 		StringBuffer sql = new StringBuffer();
 
 		int selectCount = selectClause.size();
 		int selectLast = selectCount-1;
 		sql.append("select ");
 		if(select.distinctRowsOnly())
 			sql.append("distinct \n");
 		else
 			sql.append("\n");
 		for(int sc = 0; sc < selectCount; sc++)
 		{
 			sql.append("  " + selectClause.get(sc));
 			if(sc != selectLast)
 				sql.append(", ");
 			sql.append("\n");
 		}
 
 		int fromCount = fromClause.size();
 		int fromLast = fromCount-1;
 		sql.append("from \n");
 		for(int fc = 0; fc < fromCount; fc++)
 		{
 			sql.append("  " + fromClause.get(fc));
 			if(fc != fromLast)
 				sql.append(", ");
 			sql.append("\n");
 		}
 
 		boolean haveJoinWheres = false;
 		int whereCount = whereJoinClause.size();
 		int whereLast = whereCount-1;
 		if(whereCount > 0)
 		{
 			sql.append("where\n  (\n");
 			for(int wc = 0; wc < whereCount; wc++)
 			{
 				sql.append("  " + whereJoinClause.get(wc));
 				if(wc != whereLast)
 					sql.append(" and ");
 				sql.append("\n");
 			}
 			sql.append("  )");
 			haveJoinWheres = true;
 		}
 
 		boolean haveCondWheres = false;
         int usedCondsCount = usedConditions.size();
 		if(usedCondsCount > 0)
 		{
 			if(haveJoinWheres)
 				sql.append(" and (\n");
 			else
 				sql.append("where\n  (\n");
 
     		int condsUsedLast = usedCondsCount-1;
 			for(int c = 0; c < usedCondsCount; c++)
 			{
 				QueryCondition cond = (QueryCondition) usedConditions.get(c);
 				addJoin(cond.getField());
 				sql.append("  (" + cond.getWhereCondExpr(vc, select, this) + ")");
 				if(c != condsUsedLast)
 					sql.append(cond.getConnectorSql());
 				sql.append("\n");
 			}
 
 			sql.append("  )\n");
 			haveCondWheres = true;
 		}
 
 		List whereExprs = select.getWhereExpressions();
 		if(whereExprs != null && whereExprs.size() > 0)
 		{
 			int whereExprsLast = whereExprs.size() - 1;
 			boolean first = false;
 			if(! haveJoinWheres && ! haveCondWheres)
 			{
 				sql.append("where\n  (\n");
 				first = true;
 			}
 
             int whereExprsCount = whereExprs.size();
 			for(int we = 0; we < whereExprsCount; we++)
 			{
 				SqlWhereExpression expr = (SqlWhereExpression) whereExprs.get(we);
 				if(first)
 					first = false;
 				else
 					sql.append(expr.getConnectorSql());
 
 				sql.append(" (");
 				sql.append(expr.getWhereCondExpr(this));
 				sql.append("  )\n");
 			}
 		}
 
 		List orderBys = select.getOrderBy();
 		int orderBysCount = orderBys.size();
 		int orderBysLast = orderBysCount-1;
 		if(orderBysCount > 0)
 		{
 			sql.append("order by\n");
 			for(int ob = 0; ob < orderBysCount; ob++)
 			{
 				QuerySortFieldRef sortRef = (QuerySortFieldRef) orderBys.get(ob);
 				QueryField[] fields = sortRef.getFields(vc);
 				if(fields == null)
 				{
 					return "Order by field '" + sortRef.getFieldName().getId() + "' did not evaluate to an appropriate QueryField.\n";
 				}
 				else
 				{
 					int lastField = fields.length-1;
 					for(int i = 0; i < fields.length; i++)
 					{
 						QueryField field = fields[i];
 						if(field == null)
 							return "Order by field ["+i+"] in '" + sortRef.getFieldName().getId() + "' did not evaluate to an appropriate QueryField.\n";
 
 						sql.append("  " + field.getOrderByClauseExpr());
 						if(sortRef.isDescending())
							sql.append(" descending");
 
 						if(i != lastField)
 						{
 							sql.append(", ");
 							sql.append("\n");
 						}
 					}
 				}
 
 				if(ob != orderBysLast)
 					sql.append(", ");
 				sql.append("\n");
 			}
 		}
 
 		valid = true;
 		return sql.toString();
 	}
 }
