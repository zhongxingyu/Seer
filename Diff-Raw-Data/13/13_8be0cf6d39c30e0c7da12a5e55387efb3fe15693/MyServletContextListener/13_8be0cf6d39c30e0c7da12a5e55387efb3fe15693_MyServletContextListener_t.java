 package com.crs.service;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 public class MyServletContextListener implements ServletContextListener {
 	SchedulerService scheduler = new SchedulerService();
 	@Override
 	public void contextDestroyed(ServletContextEvent event) {
 		// TODO Auto-generated method stub
 		try {
 			scheduler.destroyScheduler();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent event) {
 		// TODO Auto-generated method stub
		EmailService svc = new EmailService();
		svc.sendSimpleMail("Testing from Subramanian", "Test Email", "sarumu4@uicedu");
 			try {
 				scheduler.run();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		
 	}
 
 }
