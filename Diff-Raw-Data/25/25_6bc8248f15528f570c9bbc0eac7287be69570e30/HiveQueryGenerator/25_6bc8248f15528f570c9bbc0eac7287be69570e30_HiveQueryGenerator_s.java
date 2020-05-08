 package org.hquery.querygen.impl;
 
 import static org.hquery.common.util.HQueryConstants.COMMA;
 import static org.hquery.common.util.HQueryConstants.DOT;
 import static org.hquery.common.util.HQueryConstants.JOIN;
 import static org.hquery.common.util.HQueryConstants.ON;
 import static org.hquery.common.util.HQueryConstants.STARTING_BRACE;
 import static org.hquery.common.util.HQueryConstants.ENDING_BRACE;
 import static org.hquery.common.util.HQueryConstants.SPACE_STRING;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.hquery.querygen.QueryGenerator;
 import org.hquery.querygen.dbobjects.Column;
 import org.hquery.querygen.dbobjects.LogicalOperator;
 import org.hquery.querygen.dbobjects.Table;
 import org.hquery.querygen.dbobjects.VirtualTable;
 import org.hquery.querygen.dialect.HiveDialect;
 import org.hquery.querygen.query.Query;
 import org.hquery.querygen.query.QueryType;
 import org.hquery.querygen.visitor.QueryElementVisitor;
 
 public class HiveQueryGenerator implements QueryGenerator, QueryElementVisitor {
 	private Query queryObejct;
 	private HiveDialect dialect;
 
 	private boolean processingProjectedColumnsOver = false;;
 
 	public boolean isProcessingProjectedColumnsOver() {
 		return processingProjectedColumnsOver;
 	}
 
 	public void setProcessingProjectedColumnsOver(
 			boolean processingProjectedColumns) {
 		this.processingProjectedColumnsOver = processingProjectedColumns;
 	}
 
 	public HiveQueryGenerator(HiveDialect dialect) {
 		this.dialect = dialect;
 	}
 
 	public HiveDialect getDialect() {
 		return dialect;
 	}
 
 	public void setDialect(HiveDialect dialect) {
 		this.dialect = dialect;
 	}
 
 	private String queryString;
 
 	public void setQueryObject(Query queryObejct) {
 		this.queryObejct = queryObejct;
 	}
 
 	public Query getQueryObject() {
 		return this.queryObejct;
 	}
 
 	public String getQueryString() {
 		return queryString;
 	}
 
 	@Override
 	public void generateQuery() {
 		Table table = this.getQueryObject().getTable();
 		table.accept(this);
 		String filterString = (table.getFilter() != null) ? table.getFilter()
 				.getFilterString() : null;
 		if (this.getQueryObject().getQueryType() == QueryType.SELECT_QUERY)
 			queryString = HiveDialect.getSelectQuery(
 					table.isDistinct(),
 					projectedColumnsBuffer.toString(),
 					fromTablesBuffer.toString(),
 					(groupByColumnsBuffer != null) ? groupByColumnsBuffer
 							.toString() : null, filterString);
 	}
 
 	private StringBuffer projectedColumnsBuffer;
 	private StringBuffer fromTablesBuffer;
 	private StringBuffer groupByColumnsBuffer;
 
 	@Override
 	public void visit(Table table) {
 		if (fromTablesBuffer == null) {
 			fromTablesBuffer = new StringBuffer();
 		}
 		if (table instanceof VirtualTable) {
 			fromTablesBuffer.append(STARTING_BRACE);
 			fromTablesBuffer.append(((VirtualTable) table).getQueryString());
 			fromTablesBuffer.append(ENDING_BRACE);
 			fromTablesBuffer.append(SPACE_STRING);
 		}
 		fromTablesBuffer.append(table.getTableName());
 		Map<Column, Map<Table, Column>> joinStructure = table
 				.getJoinStructure();
 		if (joinStructure != null && joinStructure.size() > 0) {
 			Set<Column> columns = joinStructure.keySet();
 			for (Column column : columns) {
 				fromTablesBuffer.append(JOIN);
 				Map<Table, Column> map = joinStructure.get(column);
 				Set<Table> tables = map.keySet();
 				for (Table joinTable : tables) {
 					fromTablesBuffer.append(joinTable);
 					fromTablesBuffer.append(ON);
 					fromTablesBuffer.append(table.getTableName() + DOT + column
 							+ LogicalOperator.EQ + joinTable + DOT
 							+ map.get(joinTable));
 				}
 			}
 		}
 	}
 
 	@Override
 	public void visit(Column column) {
 		StringBuffer tempBuffer = new StringBuffer().append(column
 				.getColumnString());
 		if (!isProcessingProjectedColumnsOver()) {
 			if (projectedColumnsBuffer == null) {
 				projectedColumnsBuffer = new StringBuffer();
 				projectedColumnsBuffer.append(tempBuffer);
 			} else
 				projectedColumnsBuffer.append(COMMA).append(tempBuffer);
 		} else {
 			if (groupByColumnsBuffer == null) {
 				groupByColumnsBuffer = new StringBuffer();
 				groupByColumnsBuffer.append(tempBuffer);
 			} else
 				groupByColumnsBuffer.append(COMMA).append(tempBuffer);
 		}
 
 	}
 }
