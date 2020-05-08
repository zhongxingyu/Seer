 package org.maupu.android.tmh.database.filter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.maupu.android.tmh.database.AccountData;
 import org.maupu.android.tmh.database.CategoryData;
 import org.maupu.android.tmh.database.CurrencyData;
 import org.maupu.android.tmh.database.OperationData;
 import org.maupu.android.tmh.util.QueryBuilder;
 
 public class OperationFilter extends AFilter implements IFilter, Cloneable {
 	private static final long serialVersionUID = 1L;
 	
 	private List<Integer> categoryIds = new ArrayList<Integer>();
 	private QueryBuilder queryBuilder;
 	
 	public OperationFilter(QueryBuilder baseQueryBuilder) {
 		this.queryBuilder = baseQueryBuilder;
 	}
 	
 	public OperationFilter() {
 		queryBuilder = getBaseQuery();
 	}
 	
 	public QueryBuilder getQueryBuilder() {
 		// Adding categories
 		if(categoryIds.size() > 0) {
 			String sIn = "(-1";
 			for(Integer id : categoryIds) {
 				sIn += ","+id;
 			}
 			sIn += ")";
 			
 			addFilter(FUNCTION_IN, OperationData.KEY_ID_CATEGORY, sIn);
 		}
 		
 		return queryBuilder;
 	}
 
 	@Override
 	protected QueryBuilder getBaseQuery() {
 		QueryBuilder qsb = new QueryBuilder(new StringBuilder("select "));
 		qsb.setCurrentTableAlias("o");
 		qsb.addSelectToQuery(OperationData.KEY_ID).append(",")
 		.addSelectToQuery(OperationData.KEY_AMOUNT).append(",")
 		.addSelectToQuery(OperationData.KEY_DESCRIPTION).append(",")
 		.addSelectToQuery(OperationData.KEY_ID_ACCOUNT).append(",")
 		.addSelectToQuery(OperationData.KEY_ID_CATEGORY).append(",")
 		.addSelectToQuery(OperationData.KEY_ID_CURRENCY).append(",")
 		.addSelectToQuery(OperationData.KEY_CURRENCY_VALUE).append(",")
 		.addSelectToQuery(OperationData.KEY_DATE).append(",")
 		.addSelectToQuery(OperationData.KEY_LINK_TO).append(",");
 		
 		qsb.setCurrentTableAlias("c");
		qsb.addSelectToQuery(CurrencyData.KEY_SHORT_NAME).append(",")
		.addSelectToQuery(CurrencyData.KEY_CURRENCY_LINKED).append(",");
 
 		qsb.setCurrentTableAlias("a");
 		qsb.addSelectToQuery(AccountData.KEY_ICON).append(",")
 		.addSelectToQuery(AccountData.KEY_NAME, "account").append(",");
 
 		qsb.setCurrentTableAlias("ca");
 		qsb.addSelectToQuery(CategoryData.KEY_NAME, "category").append(",");
 
 		qsb.append("ROUND(o."+OperationData.KEY_AMOUNT+"/o."+OperationData.KEY_CURRENCY_VALUE+",2) convertedAmount, ");
 		qsb.append("ROUND(o.amount,2)||' '||c.shortName amountString, ");
 		qsb.append("strftime('%d-%m-%Y', o.date) dateString ");
 		qsb.append("from "+CategoryData.TABLE_NAME+" as ca, "+AccountData.TABLE_NAME+" as a, "+OperationData.TABLE_NAME+" as o, "+CurrencyData.TABLE_NAME+" as c ");
 		qsb.append("where o.idCategory=ca._id and o.idAccount=a._id and o.idCurrency=c._id ");
 
 		return qsb;
 	}
 
 	@Override
 	public QueryBuilder addFilter(int function, String operand, String value) {
 		QueryBuilder qb = this.queryBuilder;
 		
 		if(qb == null)
 			return null;
 		
 		String strFunc = "";
 		switch(function) {
 		case AFilter.FUNCTION_EQUAL:
 			strFunc = " = ";
 			break;
 		case AFilter.FUNCTION_NOTEQUAL:
 			strFunc = " != ";
 			break;
 		case AFilter.FUNCTION_IN:
 			strFunc = " IN ";
 			break;
 		case AFilter.FUNCTION_NOTIN:
 			strFunc = " NOT IN ";
 			break;
 		default:
 			return qb;
 		}
 		
 		if(OperationData.KEY_ID_ACCOUNT.equals(operand)) {
 			// Filter by account id
 			qb.append(" AND o." + OperationData.KEY_ID_ACCOUNT + " " + strFunc + value);
 		} else if(OperationData.KEY_ID_CATEGORY.equals(operand)) {
 			qb.append(" AND o." + OperationData.KEY_ID_CATEGORY + " " + strFunc + value);
 		} else if(OperationData.KEY_ID_CURRENCY.equals(operand)) {
 			qb.append(" AND o." + OperationData.KEY_ID_CURRENCY + " " + strFunc + value);
 		} else if(OperationData.KEY_AMOUNT.equals(operand)) {
 			// +/- 10% from amount
 			float val  = Float.valueOf(value);
 			int valSup = Math.round(val*1.1f);
 			int valInf = Math.round((int)val*0.9f);
 			
 			qb.append(" AND o." + OperationData.KEY_AMOUNT + " BETWEEN " + valInf +  " AND " + valSup);
 		}
 		
 		return qb.append(" ");
 	}
 	
 	public void filterCategory(int id) {
 		categoryIds.add(id);
 	}
 
 	@Override
 	public QueryBuilder resetFilter() {
 		categoryIds.clear();
 		queryBuilder = getBaseQuery();
 		return queryBuilder;
 	}
 	
 	@Override
 	public OperationFilter clone() {
 		String sqb = queryBuilder.toString();
 		return new OperationFilter(new QueryBuilder(new StringBuilder(sqb)));
 	}
 }
