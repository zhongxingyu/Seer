 /**
  * 
  */
 package org.cotrix.web.portlet;
 
 import javax.enterprise.inject.spi.Bean;
 import javax.enterprise.inject.spi.BeanManager;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.cotrix.common.ApplicationLifecycle;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CotrixServletContextListener implements ServletContextListener {
 	
 	private ApplicationLifecycle lifecycle;
 
 
 	@Override
 	public void contextInitialized(ServletContextEvent e) {
 
 		lifecycle = lifecycle(e.getServletContext());
 		
 		lifecycle.start();
 		
 	}
 
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		
 		lifecycle.stop();
 		
 	}
 
 	
 	//helper
 	private ApplicationLifecycle lifecycle(ServletContext ctx) {
 		
 		
		BeanManager manager = (BeanManager) ctx.getAttribute(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
 		
 		Bean<?> bean = manager.resolve(manager.getBeans(ApplicationLifecycle.class));
 		
 		Object lifecycle = manager.getReference(bean,ApplicationLifecycle.class, manager.createCreationalContext(bean));
 		
 		return ApplicationLifecycle.class.cast(lifecycle);
 		
 		
 	}
 }
