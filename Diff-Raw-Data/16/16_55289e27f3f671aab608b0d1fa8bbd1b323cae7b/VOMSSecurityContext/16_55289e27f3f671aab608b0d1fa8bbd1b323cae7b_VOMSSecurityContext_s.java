 package org.italiangrid.utils.voms;
 
 
 
 import java.security.cert.X509Certificate;
 import java.util.List;
 
 import org.glite.security.SecurityContext;
 import org.italiangrid.voms.VOMSAttribute;
 import org.italiangrid.voms.VOMSValidators;
 import org.italiangrid.voms.ac.VOMSACValidator;
 
 /**
  * A class representing an X.509 authentication context augmented with VOMS attributes. The context is stored in a {@link ThreadLocal}.
  * The validation and extraction of VOMS attributes is triggered by the {@link #setClientCertChain(X509Certificate[])} method.
  * 
  * Attributes can then be accessed via the {@link #getVOMSAttributes()} method. When VOMS validation fails, no attributes are returned. 
  *   
  * @author andreaceccanti
  *
  */
 public class VOMSSecurityContext extends SecurityContext{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static ThreadLocal<VOMSSecurityContext> theSecurityContexts = new ThreadLocal<VOMSSecurityContext>();
 	
 	private VOMSACValidator validator;
 	
 	
 	@Override
 	/**
 	 * Sets the client cert chain and triggers VOMS attribute extraction and validation
 	 */
 	public void setClientCertChain(X509Certificate[] certChain) {
 		super.setClientCertChain(certChain);
 		
 		if (validator == null)
 			validator = VOMSValidators.newValidator();
 		
		validator.setCertificateChain(certChain);
 	}
 	
 	/**
 	 * Returns the {@link VOMSSecurityContext} associated to the current thread
 	 * 
 	 * @return the {@link VOMSSecurityContext} associated to the current thread
 	 */
 	public static VOMSSecurityContext getCurrentContext(){
 		return theSecurityContexts.get();
 	}
 	
 	/**
 	 * Sets the {@link VOMSSecurityContext} associated to the current thread
 	 * 
 	 * @param ctxt the {@link VOMSSecurityContext} that will be associated to the current thread
 	 */
 	public static void setCurrentContext(VOMSSecurityContext ctxt){
 		
 		theSecurityContexts.set(ctxt);
 	}
 	
 	/**
 	 * Clears the {@link VOMSSecurityContext} associated to the current thread
 	 */
 	public static void clearCurrentContext(){
 		theSecurityContexts.set(null);
 	}
 
 
 	/**
 	 * Returns the {@link VOMSACValidator} used to validate VOMS attributes extracted from client certificate chains
 	 * 
 	 * @return the VOMS validator used to validate VOMS attributes
 	 */
 	public VOMSACValidator getValidator() {
 		return validator;
 	}
 
 	/**
 	 * Sets the {@link VOMSACValidator} used to validate VOMS attributes extracted from client certificate chains
 	 * 
 	 * @param validator the VOMS validator that will be used to validate VOMS attributes
 	 */
 	public void setValidator(VOMSACValidator validator) {
 		this.validator = validator;
 	}
 
 	/**
 	 *  
 	 * @return a list of validated {@link VOMSAttribute} objects.
 	 */
 	public List<VOMSAttribute> getVOMSAttributes(){
		return validator.validate();
 	}
 }
