 package com.scurab.web.drifmaps.server;
 
 import java.io.InputStream;
 import java.sql.ResultSet;
 import java.util.Properties;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
import org.restlet.Component;
 import com.scurab.web.drifmaps.client.DrifMaps;
 import com.scurab.web.drifmaps.database.Database;
 import com.scurab.web.drifmaps.server.restlet.WebServiceServer;
 
 
 
public class MainApplication implements ServletContextListener
 {
 	private static Properties properties = null;
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0)
 	{
 		
 	}
 	
	private static Component sComponent = null;
 	private static int port = 8182; 	
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0)
 	{	
 		try
 		{
 			System.out.print("Starting Drifmaps...\n");
 			WebServiceServer.start();
 			readPropertiesFile();
 			checkForDataStructure();
 //			Database d = new Database(DataServiceImpl.FILE);
 //			d.createTables();
 		}
 		catch(Exception e)
 		{
 			System.err.print(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	private void checkForDataStructure()
 	{
 		try
 		{
 			Database db = new Database();
 			ResultSet rs = db.executeQuery(Database.Structure.Queries.SELECT_TABLES_COUNT_FROM_SQLITE_MASTER);
 			if(rs.next())
 			{
 				int tables = rs.getInt("Cnt");
 				System.out.print(String.format("[Drifmaps DataStructureCheck] FoundTables:%s\n",tables));
 				if(tables == 0)
 				{
 					System.out.print("[Drifmaps DataStructureCheck] CreatingTables\n");
 					db.createTables();
 				}
 				
 			}
 			rs.close();
 		}
 		catch(Exception e)
 		{
 			System.err.print("[Drifmaps DataStructureCheckError]\n");
 			e.printStackTrace();
 		}
 	}
 	
 	private static final String PROP_FILE = "/app.properties";
 
 	public void readPropertiesFile()
 	{
 		try
 		{
 			InputStream is = DrifMaps.class.getResourceAsStream(PROP_FILE);
 			properties = new Properties();
 			properties.load(is);			
 			for(Object okey : properties.keySet())
 			{
 				String key = okey.toString();
 				System.out.print(String.format("[Drifmaps Properties]: %s='%s'\n",key,properties.getProperty(key)));
 			}
 			
 			if(System.getProperty("os.name").toLowerCase().contains("windows"))
 				Database.DBFILE = properties.getProperty("dbwin");
 			else
 				Database.DBFILE = properties.getProperty("dblinux");
 			is.close();
 		}
 		catch (Exception e)
 		{
 			System.err.println("Failed to read from " + PROP_FILE + " file.");
 		}
 	}
 
 }
