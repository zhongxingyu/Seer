 package org.hquery.testclient;
 
 import org.hquery.querygen.QueryGenerator;
 import org.hquery.querygen.dbobjects.Column;
 import org.hquery.querygen.dbobjects.Column.DataType;
 import org.hquery.querygen.dbobjects.LogicalOperator;
 import org.hquery.querygen.dbobjects.Table;
 import org.hquery.querygen.dialect.HiveDialect;
 import org.hquery.querygen.filter.Filter;
 import org.hquery.querygen.filter.decorator.FilterDecorator;
 import org.hquery.querygen.impl.HiveQueryGenerator;
 import org.hquery.querygen.query.Query;
 import org.hquery.querygen.query.QueryType;
 
 public class TestHQueryClient {
 
 	public static void main(String[] args) {
 		Query query = new Query();
 
 		Table table1 = new Table("Test_Table1");
 		Table table2 = new Table("Test_Table2");
		Table table3 = new Table("Test_Table3");
 
 		Column testColumn1 = new Column("Col1", DataType.INT)
 				.setOwningTable(table1);
 		Column testColumn2 = new Column("Col2", DataType.STRING)
				.setOwningTable(table1);
 		Column testColumn3 = new Column("Col3", DataType.INT)
				.setOwningTable(table2);
 		table1.addProjectedColumn(testColumn1);
 		table1.addProjectedColumn(testColumn2);
 		table1.addProjectedColumn(testColumn3);
 		table1.addProjectedColumn(new Column("Col4", DataType.FLOAT)
 				.setOwningTable(table2).setFunctionName("count"));
 
 		table1.addGroupByColumn(testColumn1);
 		table1.addGroupByColumn(testColumn2);
 		table1.addGroupByColumn(testColumn3);
 
 		Filter filter = new Filter();
 		filter.setColumn(new Column("Col6", DataType.INT)
 				.setOwningTable(table2));
 		filter.setOperator(LogicalOperator.EQ);
 		filter.setValue("Test Value");
 
 		FilterDecorator filterDecorator = new FilterDecorator(new Column(
 				"Col5", DataType.DOUBLE).setOwningTable(table1),
 				LogicalOperator.GE, "Test Value");
 		filterDecorator.setFilter(filter);
 		filterDecorator.setOuterOperator(LogicalOperator.AND);
 		table1.setFilter(filterDecorator);
 
 		table1.setDistinct(true);
 
		query.setTable(table1.joinTable(table2,
				new Column("src_join", DataType.INT),
				new Column("dest_join", DataType.INT)).joinTable(table3,
				new Column("Col6", DataType.INT).setOwningTable(table2),
				new Column("Col7", DataType.INT).setOwningTable(table3)));
 
 		query.setQueryType(QueryType.SELECT_QUERY);
 
 		QueryGenerator queryGenerator = new HiveQueryGenerator(
 				new HiveDialect());
 
 		queryGenerator.setQueryObject(query);
 		queryGenerator.generateQuery();
 
 		System.out.println("Generated Query was:\n"
 				+ queryGenerator.getQueryString());
 
 	}
 }
