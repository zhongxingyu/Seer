 package org.apache.jackrabbit.mk.tests.perf;
 
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import org.apache.jackrabbit.mk.testing.MongoMkTestBase;
 import org.apache.jackrabbit.mk.testing.TenantCreator;
 import org.junit.After;
 import org.junit.Test;
 import com.mongodb.Mongo;
 
 public class TenancyTest extends MongoMkTestBase {
 
 	ExecutorService threadExecutor;
 
 	@Test
 	public void create10Tenants() throws InterruptedException,
 			UnknownHostException {
 
 		TenantCreator tc;
 		int tenantsNumber = 3;
 
 		dbWriter.initialCommit("syncOAK");
 		dbWriter.syncMongos(mongosNumber, "syncOAK");
 
 		threadExecutor = Executors.newFixedThreadPool(tenantsNumber);
 		for (int i = 1; i <= tenantsNumber; i++) {
 			tc = new TenantCreator("tenantC" + clusterNodeId + i + "id",
					conf.getMongoPort(), 10);
 			threadExecutor.execute(tc);
 		}
 		threadExecutor.shutdown();
 		threadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
 	}
 
 	@After
 	public void after() throws Exception {
 		Mongo mongo = new Mongo(conf.getHost(), conf.getMongoPort());
 		System.out.println(mongo.getDatabaseNames().size());
 		List<String> databases = mongo.getDatabaseNames();
 		for (String database : databases) {
 			if ((!database.equals("admin")) && (!database.equals("config"))
 					&& (!database.equals("local"))) {
 				mongo.dropDatabase(database);
 			}
 		}
 	}
 }
