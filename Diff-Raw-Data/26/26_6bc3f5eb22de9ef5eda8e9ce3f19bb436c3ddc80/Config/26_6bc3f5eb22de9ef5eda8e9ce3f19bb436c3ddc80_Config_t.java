 package pl.inquirer.control.config;
 
 import java.io.File;
 
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

 public class Config {
	private static ApplicationContext context;
	static {
		System.out.println("inits config");
		setContext();
		System.out.println("set to " + context);
	}
 
 	public static File defaultStorageFile() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	public static ApplicationContext getSpringContext() {
		return context;
	}

	private static void setContext() {
		try {
			context = new ClassPathXmlApplicationContext("spring.xml");
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
			context = new ClassPathXmlApplicationContext("spring-noDB.xml");
			System.out.println("noDB");
		}
	}

 }
