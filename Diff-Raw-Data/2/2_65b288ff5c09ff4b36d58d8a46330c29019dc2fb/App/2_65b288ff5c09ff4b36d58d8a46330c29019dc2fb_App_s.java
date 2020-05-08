 package com.cefn.filesystem;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.sql.DataSource;
 
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.jdbc.datasource.SimpleDriverDataSource;
 import org.springframework.orm.jpa.EntityManagerFactoryInfo;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 
 import com.cefn.filesystem.impl.FilesystemImpl;
 import com.cefn.filesystem.traversal.DepthFirstFileVisitor;
 import com.cefn.filesystem.traversal.LiveVisitableFactory;
 import com.cefn.filesystem.traversal.StoredVisitableFactory;
 
 /** Accesses objects implementing interfaces with routines backed by a real file system. 
  * Stores the data accessed in this way through JPA annotations on POJO domain objects. 
  * Retrieves the file system data through objects backed by JPA Database retrieval.
 */
 public class App {
 	
 	public static void main(String[] args){
 		new App(args).run();
 	}
 
 	public App(String[] args){
 		
 	}
 
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	public void run(){
 		
 		//Load spring dependency injection system
 		AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
 		//tell it about the configuration object
 		appContext.register(Config.class);
 		//ask it to configure this app instance (in particular inject an EntityManager)
 		this.entityManager = appContext.getBean(EntityManager.class);
 		
 		try{
 
 			/** Constructs data access objects on the fly by traversing file system. */
 			final LiveVisitableFactory liveFactory = new LiveVisitableFactory();
 			
 			/** Constructs data access objects on the fly by loading from database. */
 			final StoredVisitableFactory storedFactory = new StoredVisitableFactory(entityManager);
 			
 			Filesystem filesystemInput = new FilesystemImpl(new URL("file://c"));
 			
 			/* Traverse live file hierarchy depth first, storing data */
 			new DepthFirstFileVisitor(liveFactory) {
 				public void visit(File f) {
 					entityManager.merge(f);
 				}
 			}.visit(filesystemInput);
 			
 			
 			/** Retrieve file system object from database */
 			Filesystem filesystemOutput = (Filesystem)entityManager.createQuery("SELECT fs FROM Filesystem fs").getSingleResult();
 			
 			/* Traverse stored file hierarchy depth first, printing out data */
 			new DepthFirstFileVisitor(storedFactory) {
 				public void visit(File f) {
 					System.out.println("Retrieved file : " + f.getLocation());
 				}
			}.visit(filesystemInput);
 			
 		}
 		catch(MalformedURLException mue){
 			throw new RuntimeException(mue);
 		}
 		
 	}
 	
 	@Configuration
 	public static class Config {
 
 		@Bean
 		DataSource getDataSource(){
 			SimpleDriverDataSource bean = new SimpleDriverDataSource(new org.postgresql.Driver(), "jdbc:postgresql://localhost/cefn", "cefn", "cefn");
 			return bean;
 		}
 		
 		@Bean 
 		EntityManagerFactoryInfo getEntityManagerFactoryInfo(){
 			LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();		
 			return bean;
 		}
 				
 	}
 	
 }
