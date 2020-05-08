 package de.oose.taskboard.server;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 
 /**
  * Setup google guice injections and start the persistence service
  * 
  * @author markusklink
  * 
  */
 public class GuiceContextListener extends GuiceServletContextListener {
 	@Override
 	protected Injector getInjector() {
 		Injector injector = Guice.createInjector(new ServletModule(),
				new DozerModule());
 		return injector;
 	}
 }
