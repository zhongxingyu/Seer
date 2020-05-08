 
 package axirassa.services;
 
 import java.io.IOException;
 
 import liquibase.Liquibase;
 import liquibase.database.Database;
 import liquibase.exception.LiquibaseException;
 import liquibase.integration.commandline.CommandLineUtils;
 import liquibase.resource.ClassLoaderResourceAccessor;
 import liquibase.resource.ResourceAccessor;
 
 import org.hibernate.cfg.Configuration;
 
 public class DatabaseMigrateService {
 	public static final String MASTER_CHANGELOG = "axirassa/model/changelog/db.changelog-master.xml";
 
 
	public static void main (String[] args) throws LiquibaseException, IOException {
 		Configuration config = new Configuration().configure();
 		ClassLoader classloader = DatabaseMigrateService.class.getClassLoader();
 
		Database target = CommandLineUtils
		        .createDatabaseObject(classloader, config.getProperty("hibernate.connection.url"),
		                              config.getProperty("hibernate.connection.username"),
		                              config.getProperty("hibernate.connection.password"),
		                              config.getProperty("hibernate.connection.driver_class"), null, null);
 
 		ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(classloader);
 		Liquibase liquibase = new Liquibase(MASTER_CHANGELOG, resourceAccessor, target);
 
 		System.out.println("\n\n\n\n\n\n\n\n");
 		System.out.println("LIQUIBASE EXECUTING DATABASE MIGRATION");
 		System.out.println("=============================================");
 		liquibase.update("");
 	}
 }
