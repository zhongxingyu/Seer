 package org.mdissjava.commonutils.mongo.db;
 
 import java.net.UnknownHostException;
 
 import org.junit.After;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mongodb.MongoException;
 
 public class MongoDBConnectionTest {
 	
 	private MongoDBConnection mdbc;
 	final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	@Test
 	public void testConnection() throws UnknownHostException, MongoException 
 	{
 		this.logger.info("TEST(MongoDB) testConnection");
 		
 		this.mdbc = MongoDBConnection.getInstance();
 		this.mdbc.connect();
 		mdbc.getConnection();
 		
 		//Probe connection
 		this.mdbc.getConnection().getDatabaseNames();
 	}
 	
 	@Test(expected=MongoException.class)
 	public void testConnectionWrongPort() throws UnknownHostException
 	{
 		this.logger.info("TEST(MongoDB) testConnectionWrongPort");
 		
 		this.mdbc = MongoDBConnection.getInstance();
 		this.mdbc.connect("127.0.0.23", 90);
 		
 		//Probe connection
 		this.mdbc.getConnection().getDatabaseNames();
 	}
 	
 	@Test
 	public void testMultiConnection() throws UnknownHostException
 	{
 		this.logger.info("TEST(MongoDB) testMultiConnection");
 		
 		Thread connectionThread = new Thread(new Runnable() 
 		{	
 			@Override
 			public void run() {
 				final int times = 5;
 				
 				for( int i=0; i<=times; i++)
 				{
 					
 					try {
 						MongoDBConnection mdbc = MongoDBConnection.getInstance();
 						mdbc.connect();
 						mdbc.getConnection().getDatabaseNames();
 					} catch (UnknownHostException e) {
 						e.printStackTrace(); //don't do this at home ;)
 					} catch (MongoException e) {
 						e.printStackTrace(); //the same ;)
 					}
 					
 				}
 			}
 		});
 		
 		
 		for (int i=0; i < 10; i++)
 		{
 			connectionThread.run();
 		}
 		
 		
 	}
 
 }
