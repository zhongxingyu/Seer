 package test.cli.cloudify.recipes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 
 import org.cloudifysource.dsl.Service;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 
 import framework.utils.ScriptUtils;
 
 public class RecipeTest extends AbstractLocalCloudTest {
 	private String recipesDirPath = ScriptUtils.getBuildPath() + "/recipes/services";
 	public static volatile boolean portReleasedBeforTimeout;
 	protected static volatile boolean portTakenBeforTimeout;
 
 	public RecipeTest(){
 		super();	
 	}
 
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		portReleasedBeforTimeout = false;
 		portTakenBeforTimeout = false;
 	}
 
 	@Override
 	@AfterMethod
 	public void afterTest() {}
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
     public void testMongod() throws IOException, InterruptedException{
         String mongodDirPath = recipesDirPath + "/mongodb/mongod";
         String output = runCommand("test-recipe " + mongodDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " mongod-service.groovy");
         assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
     public void testMongos() throws IOException, InterruptedException{
         String mongosDirPath = recipesDirPath + "/mongodb/mongos";
         String output = runCommand("test-recipe " + mongosDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " mongos-service.groovy");
         assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
     public void testMongoCfg() throws IOException, InterruptedException{
         String mongoCfgDirPath = recipesDirPath + "/mongodb/mongoConfig";
        String output = runCommand("test-recipe " + mongoCfgDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " mongo-cfg-service.groovy");
         assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
     }
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testSolr() throws IOException, InterruptedException{
 		String solrDirPath = recipesDirPath + "/solr";
 		String output = runCommand("test-recipe " + solrDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " solr-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testHsqldb() throws IOException, InterruptedException{
 		String hsqldbDirPath = recipesDirPath + "/hsqldb";
 		String output = runCommand("test-recipe " + hsqldbDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " hsqldb-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed") && output.contains("Server socket opened successfully"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = false)
 	public void testElasticsearch() throws IOException, InterruptedException{
 		String elasticsearchDirPath = recipesDirPath + "/elasticsearch";
 		String output = runCommand("test-recipe " + elasticsearchDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " elasticsearch-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testActivemq() throws IOException, InterruptedException{
 		String activemqDirPath = recipesDirPath + "/activemq";
 		String output = runCommand("test-recipe " + activemqDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " activemq-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testTomcat() throws IOException, InterruptedException{
 		String tomcatDirPath = recipesDirPath + "/tomcat";
 		String output = runCommand("test-recipe " + tomcatDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " tomcat-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void installTomcat() throws IOException, InterruptedException{
 		super.beforeTest();
 		String tomcatDirPath = recipesDirPath + "/tomcat";
 		runCommand("connect " + restUrl + ";install-service --verbose " + tomcatDirPath);
 		runCommand("connect " + restUrl + ";uninstall-service --verbose tomcat");
 		
 	}
 	 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = false)
 	public void testJboss() throws IOException, InterruptedException{
 		String jbossDirPath = recipesDirPath + "/jboss";
 		String output = runCommand("test-recipe --verbose " + jbossDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " jboss-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = false)
 	public void installJboss() throws IOException, InterruptedException{
 		super.beforeTest();
 		String jbossDirPath = recipesDirPath + "/jboss";
 		runCommand("connect " + restUrl +  ";install-service --verbose " + jbossDirPath);
 		runCommand("connect " + restUrl +  ";uninstall-service --verbose jboss-service");
 	}	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
 	public void testCassandraByPort() throws IOException, InterruptedException, DSLException{
 		String cassandraDirPath = recipesDirPath + "/cassandra";
 		String CassandraServiceGroovyPath = cassandraDirPath + "/" + "cassandra-service.groovy";
 		Service service = ServiceReader.getServiceFromFile(new File(CassandraServiceGroovyPath) , new File(cassandraDirPath)).getService();
 		ArrayList<Integer> ports = (ArrayList<Integer>) service.getPlugins().get(0).getConfig().get("Port");
 		int port = ports.get(0);
 		new Thread(new RecipeTestUtil.AsinchronicPortCheck(port)).start();
 		String output = runCommand("test-recipe " + cassandraDirPath + " " + RecipeTestUtil.DEFAULT_RECIPE_TEST_TIMEOUT + " cassandra-service.groovy");
 		assertTrue("test-recipe failed and runCommand didn't throw an Exception as it should !!", output.contains("Recipe test completed"));
 		assertTrue("cassandra didn't take it's port and runCommand didn't throw an Exception as it should !!" , portTakenBeforTimeout);
 		assertTrue("cassandra didn't release it's port befor timeout and runCommand didn't throw an Exception as it should !!" , portReleasedBeforTimeout);
 	}
 	
 	
 	
 }
