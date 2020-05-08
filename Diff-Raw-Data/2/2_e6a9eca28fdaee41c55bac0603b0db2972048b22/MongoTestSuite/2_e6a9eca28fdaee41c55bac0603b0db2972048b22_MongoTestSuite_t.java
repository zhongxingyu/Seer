 package com.gigaspaces.persistency.qa.itest;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 import com.gigaspaces.logger.GSLogConfigLoader;
 import com.gigaspaces.persistency.qa.helper.MongoDBController;
 
 @RunWith(Suite.class)
 @SuiteClasses(value = {
 	BasicMongoTest.class,
 	BasicQueriesMongoTest.class  ,
 	AdvanceQueriesMongoTest.class,
 	DifferentTypesQueryMongoTest.class,
 	InitialDataLoadMongoTest.class,
 	MetadataSpaceTypeDescriptorConversionTest.class,
 	MultiTypeMongoTest.class, 
 	PojoWithPrimitiveTypesMongoTest.class,
 	ReadByIdsMongoTest.class,
 	WriteAndRemoveMongoTest.class,
  	ReadByIdWithPropertyAddedLaterMongoTest.class,
  	DataIteratorWithPropertyAddedLaterMongoTest.class, 	
  	InnerClassMongoTest.class
 	///TestMongoArchiveOperationHandler.class,
 })
 public class MongoTestSuite {
 
 	private static final AtomicInteger runningNumber = new AtomicInteger(0);
 	private static volatile boolean isSuiteMode = false;
 
 	private static final MongoDBController mongoController = new MongoDBController();
 	
 	@BeforeClass
 	public static void beforeSuite() {
 		GSLogConfigLoader.getLoader();
 		isSuiteMode = true;
 		mongoController.start(false);
 	}
 
 	@AfterClass
 	public static void afterSuite() {
 		isSuiteMode = false;
 		mongoController.stop();
 	}
 
 	public static String createDatabaseAndReturnItsName() {
 
 		String dbName = "space" + runningNumber.incrementAndGet();
 		mongoController.createDb(dbName);
 		return dbName;
 	}
 
 	public static void dropDb(String dbName) {
 		mongoController.dropDb(dbName);
 	}
 
 	public static boolean isSuiteMode() {
 		return isSuiteMode;
 	}
 
 	public static int getPort() {
 
 		return mongoController.getPort();
 	}
 
 }
