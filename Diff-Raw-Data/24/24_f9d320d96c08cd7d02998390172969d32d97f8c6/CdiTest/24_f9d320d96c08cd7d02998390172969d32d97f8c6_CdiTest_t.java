 package org.acme;
 
 import static org.junit.Assert.*;
 
 import java.io.Serializable;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.cotrix.common.cdi.Session;
 import org.jglue.cdiunit.AdditionalClasses;
 import org.jglue.cdiunit.CdiRunner;
 import org.jglue.cdiunit.ContextController;
 import org.jglue.cdiunit.DummyHttpRequest;
 import org.jglue.cdiunit.ProducesAlternative;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(CdiRunner.class)
 @AdditionalClasses(CdiTest.SessionBean.class)
 public class CdiTest {
 
 	@Inject
 	Bean bean;
 	
 	@Inject
 	SessionBeanManager manager;
 	
 	@Inject
 	ContextController contextController; 
 
 	
 	@Test
 	public void sessionBeansAreInjected() {
 
 
 		//work in session
 		contextController.openSession(new DummyHttpRequest());
 		
 		//show it's a proxy
		System.out.println("Foo instance is a proxy: "+bean.sbean().getClass());
 		
 		
		System.out.println("Foo dependency is a proxy: "+bean);
 		
 		manager.populateSessionWith(10);
 
 		//show it's a proxy
		System.out.println("first access to Foo");
 		
 		//the injected session bean comes from session
 		assertEquals(10,bean.sbean().a());
 		
 	
 	}
 	
 	@ApplicationScoped //must be as we're injecting a scoped bean inside
 	static class Bean {
 		
 		@Inject
 		SessionBean sbean;
 		
 		SessionBean sbean() {
 			return sbean;
 		}
 	}
 	
 	@SuppressWarnings("serial") 
 	static class SessionBean implements Serializable {
 		
 		int a;
 		
 		public SessionBean() {
 			System.out.println("created non-proxy bean "+this);
 		}
 		
 		int a() {
 			return a;
 		}
 	}
 	
 	
 	static class SessionBeanManager {
 		
 		@Inject
 		Session session;
 		
 		public void populateSessionWith(int a) {
 			
 			//populate session bean
 			SessionBean sbean = new SessionBean();
 			sbean.a=a;
 			session.data().put("bean",sbean);
 		}
 		
 		@Produces @ProducesAlternative  
 		@SessionScoped
 		SessionBean bean() {
 			SessionBean bean = (SessionBean) session.data().get("bean");
 			System.out.println("CDI is invoking the producer that returns the SessionBean "+bean);
 			return bean;
 		}
 	}
 }
