 package layr.ejb;
 
 import javax.naming.NamingException;
 
 import layr.api.ApplicationContext;
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
 
 }
