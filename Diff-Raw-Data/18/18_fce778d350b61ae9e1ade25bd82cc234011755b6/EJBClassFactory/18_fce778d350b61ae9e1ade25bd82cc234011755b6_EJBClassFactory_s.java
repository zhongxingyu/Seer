 package layr.ejb;
 
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
 import javax.naming.NamingException;
 
 import layr.api.ApplicationContext;
import layr.api.ClassFactory;
import layr.api.Handler;
 import layr.api.RequestContext;
 import layr.exceptions.ClassFactoryException;
 
 public class EJBClassFactory {
 
 	public Object newInstance(ApplicationContext applicationContext,
 			RequestContext requestContext, Class<?> clazz)
 			throws ClassFactoryException {
 		try {
 			EnterpriseJavaBeansContext ejbContext = (EnterpriseJavaBeansContext) applicationContext
 					.getAttribute(EnterpriseJavaBeansContext.class.getCanonicalName());
 			return ejbContext.lookup(clazz);
 		} catch (NamingException e) {
 			throw new ClassFactoryException("Can't lookup ejb for " + clazz.getCanonicalName(), e);
 		}
 	}
 
	@Handler
	public class StatelessClassFactory
		extends EJBClassFactory implements ClassFactory<Stateless> {}

	@Handler
	public class StatefulClassFactory
		extends EJBClassFactory implements ClassFactory<Stateful> {}

	@Handler
	public class SingletonClassFactory
		extends EJBClassFactory implements ClassFactory<Singleton> {}
 }
