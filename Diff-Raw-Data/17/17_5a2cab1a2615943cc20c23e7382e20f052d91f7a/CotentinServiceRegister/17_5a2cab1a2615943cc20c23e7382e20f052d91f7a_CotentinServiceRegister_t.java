 package nu.cotentin.impl.services;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 /**
  * The service register for jersey webservices
  * @author Christian Heike
  *
  */
 @ManagedBean(CotentinServiceRegister.BEAN_NAME)
 @ViewScoped
 public class CotentinServiceRegister implements Serializable {
 
 	public static final String BEAN_NAME = "cotentinServiceRegister";
 	public static final String BEAN_EXPR = "#{" + BEAN_NAME + "}";
 	
 	private static final long serialVersionUID = -8756694138222997262L;
 	
 	private Map<String, JerseyService> services;
 	
 	/**
 	 * Creates a new register for cotentin services
 	 */
 	public CotentinServiceRegister() {
 		services = new HashMap<String, JerseyService>();
 	}
 	
 	/**
 	 * Adds a service to the register
 	 * @param service the service to add
 	 */
 	public void addService(JerseyService service) {
 		services.put(service.getServiceId(), service);
 	}
 	
 	/**
 	 * Returns the jersey service registered to the provided service identifier
 	 * @param serviceId the service identifier to return the service for
 	 * @return the service registered for the provided service id
 	 */
 	public JerseyService getService(String serviceId) {
 		return services.get(serviceId);
 	}
 		
 	/**
 	 * Returns the register for a certain view root by delegating it to. 
 	 * If non is present, creates one and saves it to the view root
 	 * @param viewRoot the view root to get the register for
 	 * @return the register attached to the provided view root
 	 */
 	public static CotentinServiceRegister getRegister(FacesContext ctx) {
 		CotentinServiceRegister register = 
 				ctx.getApplication().evaluateExpressionGet(ctx, BEAN_EXPR, 
 											CotentinServiceRegister.class);
 		if (register == null) {
 			register = new CotentinServiceRegister();
 			ctx.getViewRoot().getViewMap().put(BEAN_NAME, register);
 		}
 		return register;
 	}	
 }
