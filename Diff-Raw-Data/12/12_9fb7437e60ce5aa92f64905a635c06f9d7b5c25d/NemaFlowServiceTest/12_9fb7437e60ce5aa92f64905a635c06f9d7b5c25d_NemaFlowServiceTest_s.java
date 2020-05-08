 package org.imirsel.nema.flowservice;
 
 import java.util.Date;
 import java.util.UUID;
 import java.util.logging.Logger;
 
 import org.imirsel.nema.dao.FlowDao;
 import org.imirsel.nema.model.Flow;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Main class for the NEMA Flow Service.
  * 
  * @author shirk
  */
 public class NemaFlowServiceTest {
 	private static final Logger logger = 
 		Logger.getLogger(FlowServiceApp.class.getName());
 	
 	/**
 	 * Main method for executing the application.
 	 * 
 	 * @param args Arguments for the application.
 	 */
 	public static void main(String[] args) {
 		ApplicationContext ctx  =  null;
	    ctx = new ClassPathXmlApplicationContext("org/imirsel/nema/flowservice/config/applicationContext.xml");
 	    
 		FlowService flowService = (FlowService)ctx.getBean("flowService");
 		
		for(int i=0; i<5; i++) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			flowService.executeJob(UUID.randomUUID().toString(), 
 					"Test: " + new Date().toString(), "Test job " + i, 
 					1, 1L, "shirk@uiuc.edu");
 		}
		
 	}
 
 }
