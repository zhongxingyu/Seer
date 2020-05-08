 
 package axirassa.services;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import liquibase.database.Database;
 import liquibase.diff.Diff;
 import liquibase.diff.DiffResult;
 import liquibase.exception.DatabaseException;
 import liquibase.integration.commandline.CommandLineUtils;
 
 import org.hibernate.cfg.Configuration;
 
 public class DatabaseDeltaService {
	public static void main (String[] args) throws DatabaseException, ParserConfigurationException, IOException {
 		// pull the database configuration from hibernate
 		Configuration config = new Configuration().configure();
 
 		ClassLoader classloader = DatabaseDeltaService.class.getClassLoader();
		Database target = CommandLineUtils
		        .createDatabaseObject(classloader, config.getProperty("hibernate.connection.url"),
		                              config.getProperty("hibernate.connection.username"),
		                              config.getProperty("hibernate.connection.password"),
		                              config.getProperty("hibernate.connection.driver_class"), null, null);
 
 		Database reference = CommandLineUtils.createDatabaseObject(classloader, "hibernate:hibernate.cfg.xml", null,
 		                                                           null, null, null, null);
 
 		Diff diff = new Diff(reference, target);
 		DiffResult result = diff.compare();
 
 		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n");
 		System.out.println("LIQUIBASE DATABASE DELTA:");
 		System.out.println("=============================================");
 		result.printChangeLog(System.out, target);
 	}
 }
