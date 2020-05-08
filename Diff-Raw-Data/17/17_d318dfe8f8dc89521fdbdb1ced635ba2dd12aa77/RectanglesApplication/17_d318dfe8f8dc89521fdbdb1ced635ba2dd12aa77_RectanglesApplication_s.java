 package com.odobo.web;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.springframework.context.ApplicationContext;
 
 /**
  * Application object for your web application. If you want to run this application without deploying, run the Start class.
  * 
  * @see com.odobo.Start#main(String[])
  */
 public class RectanglesApplication extends WebApplication
 {    	
 	
 	private static ApplicationContext applicationContext;
 	
 	/**
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	@Override
 	public Class<? extends WebPage> getHomePage()
 	{
 		return RectanglesPage.class;
 	}
 
 	/**
 	 * @see org.apache.wicket.Application#init()
 	 */
 	@Override
 	public void init()
 	{
 		super.init();
 
 		applicationContext = new ContextLoader().loadContext();
 	}
 
 	public static ApplicationContext getApplicationContext() {
 		return applicationContext;
 	}
 
 	public static void setApplicationContext(ApplicationContext applicationContext) {
 		RectanglesApplication.applicationContext = applicationContext;
 	}
 }
