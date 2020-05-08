 package se.ithuset.greenbean.supplier;
 
 import org.apache.ftpserver.FtpServer;
 import org.apache.ftpserver.ftplet.FtpException;
import org.apache.log4j.BasicConfigurator;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws FtpException 
 	 */
 	public static void main(String[] args) throws FtpException {
        BasicConfigurator.configure();

 		ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/spring/app-context.xml");
		String[] beanNames = ctx.getBeanDefinitionNames();
		for(String beanName : beanNames) {
			System.out.println(beanName + " of type " + ctx.getBean(beanName).getClass().getName());
		}
 		
 		FtpServer server = ctx.getBean(FtpServer.class);
 		server.start();
 	}
 
 }
