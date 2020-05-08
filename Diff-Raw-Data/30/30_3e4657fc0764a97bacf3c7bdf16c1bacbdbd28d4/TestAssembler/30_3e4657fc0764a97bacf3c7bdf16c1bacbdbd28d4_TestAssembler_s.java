 package org.hquery.testclient;
 
 import org.apache.hadoop.hive.metastore.api.MetaException;
 import org.apache.hadoop.hive.metastore.api.NoSuchObjectException;
 import org.apache.thrift.TException;
 import org.hquery.assembler.HQueryAssembler;
 import org.hquery.common.util.HQueryUtil;
 import org.hquery.common.util.UserPreferences;
 import org.hquery.common.util.UserPreferences.FileType;
 import org.hquery.metastore.MetaInformationService;
 import org.hquery.querygen.dbobjects.Column;
 import org.hquery.querygen.dbobjects.Column.DataType;
 import org.hquery.querygen.dbobjects.LogicalOperator;
 import org.hquery.querygen.dbobjects.Table;
 import org.hquery.querygen.filter.Filter;
 import org.hquery.querygen.filter.decorator.FilterDecorator;
 import org.hquery.querygen.query.Query;
 import org.hquery.querygen.query.QueryType;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class TestAssembler {
 	public static void main(String[] args) throws Exception{
 		testHQuery1();
		//testHQuery2();
 
 	}
 
 	public static void testHQuery2() {
 		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
 				"spring-config.xml");
 		HQueryAssembler assembler = (HQueryAssembler) ctx
 				.getBean("hQueryAssembler");
 
 		Query query = new Query();
 
 		Table records = new Table("records");
 		Column year = new Column("year", DataType.STRING)
 				.setOwningTable(records);
 		Column temperature = new Column("temperature", DataType.INT)
 				.setOwningTable(records).setFunctionName("max");
 		Column quality = new Column("quality", DataType.INT)
 				.setOwningTable(records);
 		records.addProjectedColumn(year);
 		records.addProjectedColumn(temperature);
 		records.addGroupByColumn(year);
 
 		Filter filter = new Filter();
 		filter.setColumn(quality);
 		filter.setOperator(LogicalOperator.EQ);
 		filter.setValue("9");
 
 		FilterDecorator firstDecorator = new FilterDecorator(quality,
 				LogicalOperator.EQ, "5");
 		firstDecorator.setFilter(filter);
 		firstDecorator.setOuterOperator(LogicalOperator.OR);
 
 		FilterDecorator secondDecorator = new FilterDecorator(quality,
 				LogicalOperator.EQ, "4");
 		secondDecorator.setFilter(firstDecorator);
 		secondDecorator.setOuterOperator(LogicalOperator.OR);
 
 		FilterDecorator thirdDecorator = new FilterDecorator(quality,
 				LogicalOperator.EQ, "1");
 		thirdDecorator.setFilter(secondDecorator);
 		thirdDecorator.setOuterOperator(LogicalOperator.OR);
 
 		FilterDecorator fourhDecorator = new FilterDecorator(quality,
 				LogicalOperator.EQ, "1");
 		fourhDecorator.setFilter(thirdDecorator);
 		fourhDecorator.setOuterOperator(LogicalOperator.OR);
 
 		FilterDecorator fifthDecorator = new FilterDecorator(quality,
 				LogicalOperator.EQ, "0");
 		fifthDecorator.setFilter(fourhDecorator);
 		fifthDecorator.setOuterOperator(LogicalOperator.OR);
 
 		FilterDecorator sixthDecorator = new FilterDecorator(temperature,
 				LogicalOperator.NOT_EQ, "9999");
 		sixthDecorator.setFilter(fifthDecorator.setGrouped(true));
 		sixthDecorator.setOuterOperator(LogicalOperator.AND);
 
 		records.setFilter(sixthDecorator);
 		query.setTable(records);
 		query.setQueryType(QueryType.SELECT_QUERY);
 
 		UserPreferences pref = new UserPreferences();
 		pref.setOutputFileType(FileType.CSV_TYPE);
 		pref.setOutputFile("/Users/subhasig/test.txt");
 
 		String sessionId = assembler.executeQuery(query, pref);
 		System.out.println("Session Id: " + sessionId);
 
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		assembler.printRTStatus(sessionId);
 
 		try {
 			Thread.sleep(Long.parseLong(HQueryUtil.getResourceString(
 					"hquery-conf", "hquery.cooldown.period"))); //giving other daemon thread a chance to graceful stop 
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void testHQuery1() throws NoSuchObjectException,
 			MetaException, TException {
 		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
 				"spring-config.xml");
 		HQueryAssembler assembler = (HQueryAssembler) ctx
 				.getBean("hQueryAssembler");
 
 		MetaInformationService metaInfoService = assembler
 				.getMetaInformationService();
 
 		System.out.println("Databases: " + metaInfoService.listAllDatabases());
 		System.out.println("Tables: " + metaInfoService.listAllTables());
 		System.out.println("Fields: "
 				+ metaInfoService.listFields("test_clickdata"));
 
 		ctx.registerShutdownHook();
 	}
 }
