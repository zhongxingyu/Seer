 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 // Singleton class
 
 public class Connector {
 	
 	private Connector()
 	{
 		
 	}
 	
 	private static Connector ref;
 	Session session = null;
 	
 	public static Connector getConnector() {
 		if (ref == null) {
 			ref = new Connector();
 		}
 		return ref;
 	}
 	
 	public Object clone() throws CloneNotSupportedException
 	{
 			throw new CloneNotSupportedException();		
 	}
 	
 	public void createSession() {
 		Configuration configuration = new Configuration();
         configuration.configure();
 		Properties prop = new Properties();
 		try {
 			prop.load(new FileInputStream("trademarket.props"));			
 		} catch (FileNotFoundException e) {
 		prop.put("hibernate.connection.username", "root");
 		prop.put("hibernate.connection.url", "jdbc:mysql://localhost/trademarket");
 		prop.put("hibernate.connection.password", "");
 		System.out.println("Config file not found - using defaults");
		configuration.addProperties(prop);
 		} catch (IOException e) {
 		e.printStackTrace();
 		}
 		SessionFactory sessionFactory = configuration.buildSessionFactory();
 		session = sessionFactory.openSession();
 		
 	}
 	
 	public Session getSession() {
 		if (session == null) {
 			createSession();			
 		}
 		return session;
 	}
 	
 	
 	public void closeSession() {
 		session.flush();
 		session.close();
 		session = null;
 	}
 	
 	
 	
 
 }
