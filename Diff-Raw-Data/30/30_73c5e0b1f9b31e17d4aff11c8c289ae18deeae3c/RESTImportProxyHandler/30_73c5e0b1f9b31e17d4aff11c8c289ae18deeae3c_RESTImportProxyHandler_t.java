 package rsaother;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Hashtable;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.ImportReference;
 import org.osgi.service.remoteserviceadmin.ImportRegistration;
 import org.restlet.resource.ClientResource;
 
 public class RESTImportProxyHandler implements InvocationHandler {
 	
 	private Class<?> interfaceClass;
 	private String baseUrl;//note: should end on "/"
 	
 	public RESTImportProxyHandler(Class<?> interfaceClass, String baseUrl){
 		this.interfaceClass=interfaceClass;
 		this.baseUrl=baseUrl;
 	}
 	
 	public ImportRegistration getImportRegistration(BundleContext context, EndpointDescription descr){
 		Class<?>[] classes = new Class<?>[]{interfaceClass};
 		String[] classnames = new String[]{interfaceClass.getName()};
 		
 		Object proxy = Proxy.newProxyInstance(interfaceClass.getClassLoader(), classes, this);
 		
 		Hashtable<String, Object> properties = new Hashtable();
 		properties.put("service.imported", "true");
 		
 		ServiceRegistration<?> servicereg = context.registerService(classnames, proxy, properties);
 		
 		RESTImportReference importref = new RESTImportReference(servicereg, descr);
 		
 		ROSGiImportRegistration registObject = new ROSGiImportRegistration(importref);
 		
 		return registObject;
 	}
 	
 
 	@Override
 	public Object invoke(Object arg0, Method arg1, Object[] arg2)
 			throws Throwable {
 		// Get the right suburl...
 		String urlfortheobject = baseUrl+methodToID(arg1);
 		
 		// Get the IRemoteRestCall object for this method...
		ClientResource cr = RESTServiceAdmin.clientsByString.get(urlfortheobject);
		if(cr == null) {
			cr = new ClientResource(urlfortheobject);
			RESTServiceAdmin.clientsByString.put(urlfortheobject, cr);
		}
 		IRemoteRestCall resource = cr.wrap(IRemoteRestCall.class, arg1.getReturnType());
 		
 		// Call
 		Object returnObject = resource.doCall(arg2);
		cr.getResponseEntity().exhaust();
		cr.getResponseEntity().release();
 		
 		// return
 		return returnObject;
 	}
 	
 	
 	/**
 	 * Static method
 	 */
 	public static String methodToID(Method m) {
 		Class<?>[] params = m.getParameterTypes();
 		
 		String joined = m.getName();
 		
 		for(int i=0;i<params.length;i++) {
 			joined += "/" + params[i].getCanonicalName();
 		}
 		
 		return joined;
 	}
 
 	
 	/**
 	 * ImportReference class
 	 */
 	private class ROSGiImportRegistration implements ImportRegistration {
 
 		ImportReference importref;
 		
 		public ROSGiImportRegistration(ImportReference importref){
 			this.importref = importref;
 		}
 		
 		@Override
 		public ImportReference getImportReference() {
 			return importref;
 		}
 
 		@Override
 		public void close() {}
 
 		@Override
 		public Throwable getException() {
 			return null;// not implemented
 		}
 		
 		public EndpointDescription getEndpointDescription() {
 			return null;// not implemented
 		}
 	}
 }
