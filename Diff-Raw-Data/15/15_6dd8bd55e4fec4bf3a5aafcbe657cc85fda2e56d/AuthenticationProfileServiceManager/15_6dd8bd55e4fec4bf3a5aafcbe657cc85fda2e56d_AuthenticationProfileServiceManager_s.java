 package org.cagrid.gaards.websso.authentication;
 
 import gov.nih.nci.cagrid.common.Runner;
 import gov.nih.nci.cagrid.common.ThreadManager;
 import gov.nih.nci.cagrid.metadata.exceptions.ResourcePropertyRetrievalException;
 
 import java.net.MalformedURLException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.gaards.authentication.client.AuthenticationClient;
 import org.cagrid.gaards.dorian.client.GridUserClient;
 import org.cagrid.gaards.dorian.federation.TrustedIdentityProvider;
 import org.cagrid.gaards.websso.beans.AuthenticationServiceInformation;
 import org.cagrid.gaards.websso.beans.DorianInformation;
 import org.cagrid.gaards.websso.exception.GenericException;
 import org.cagrid.gaards.websso.exception.InvalidResourceException;
 import org.cagrid.gaards.websso.utils.AuthenticationServiceHandle;
 import org.cagrid.gaards.websso.utils.DorianServiceHandle;
 import org.cagrid.gaards.websso.utils.WebSSOProperties;
 import org.springframework.beans.factory.InitializingBean;
 
 public class AuthenticationProfileServiceManager extends Runner implements InitializingBean{
 
 	private static AuthenticationProfileServiceManager instance;
 	private final Log log = LogFactory.getLog(getClass());
 	private ThreadManager threadManager;
 	private WebSSOProperties webSSOProperties;
 	private List<DorianServiceHandle> dorianServices;
 
 	private Object mutex;
 	private boolean firstRun;
 
 	private AuthenticationProfileServiceManager() {
 		this.mutex = new Object();
 		this.firstRun = true;
 	}
 	
 	public void setWebSSOProperties(WebSSOProperties webSSOProperties) {
 		this.webSSOProperties = webSSOProperties;
 	}
 	
 	private void startup() {
 		try {
 			this.threadManager = new ThreadManager();
 			threadManager.executeInBackground(this);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public synchronized static AuthenticationProfileServiceManager getInstance() {
 		if (instance == null) {
 			instance= new AuthenticationProfileServiceManager();
 		}
 		return instance;
 	}
 
 	public void execute() {
 		while (true) {
 			if (firstRun) {
 				synchronized (mutex) {
 					syncServices();
 					firstRun = false;
 				}
 			} else {
 				syncServices();
 			}
 			try {
 				Thread.sleep(120000);
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	public synchronized void syncServices() {
 		List<DorianServiceHandle> doriansHandle = new ArrayList<DorianServiceHandle>();
 		List<DorianInformation> doriansInformation = webSSOProperties.getDoriansInformation();
 		for (DorianInformation dorian : doriansInformation) {
 			DorianServiceHandle handle = new DorianServiceHandle(dorian);
 			try {
 				loadAuthenticationServices(handle);
 			} catch (Exception e) {
 				throw new RuntimeException(e);
 			}
 			doriansHandle.add(handle);
 		}
 		synchronized (mutex) {
 			this.dorianServices = doriansHandle;
 		}
 	}
 
 	public List<DorianServiceHandle> getDorianServices() {
 		synchronized (mutex) {
 			return dorianServices;
 		}
 	}
 	
 	public void loadAuthenticationServices(DorianServiceHandle handle) throws InvalidResourceException,GenericException{
         GridUserClient client = null;
 		List<TrustedIdentityProvider> idps = null;
 		try {
 			client = handle.getUserClient();
 			idps = client.getTrustedIdentityProviders();
 		} catch (Exception e) {
 			handleException(e);
 		}
     	List<AuthenticationServiceHandle> authenticationServices=new ArrayList<AuthenticationServiceHandle>();
         if (idps != null) {
             for (int i = 0; i < idps.size(); i++) {
             	String displayName = idps.get(i).getDisplayName();
 				String authenticationServiceURL = idps.get(i).getAuthenticationServiceURL();
 				log.debug("Authenticated Service display Name "+displayName);
 				log.debug("Authenticated Service authenticationServiceURL "+authenticationServiceURL);
 				
 				if(authenticationServiceURL == null){  
 					log.warn("The URL for " + displayName + " is null. Skipping.");                    
 					continue;
 				}
 
 				String authenticationServiceIdentity = idps.get(i).getAuthenticationServiceIdentity();
 				AuthenticationServiceInformation information=new AuthenticationServiceInformation(displayName,authenticationServiceURL,authenticationServiceIdentity);
             	
 				AuthenticationServiceHandle ashandle = new AuthenticationServiceHandle(information);
 				AuthenticationClient authenticationClient;
 				try {
 					authenticationClient = ashandle.getAuthenticationClient();
					ashandle.getAuthenticationServiceInformation().setAuthenticationServiceProfiles(authenticationClient.getSupportedAuthenticationProfiles());
 				}catch (Exception e) {
 					handleException(e);
				}
	            authenticationServices.add(ashandle);
             }
         }
         handle.setAuthenticationServices(authenticationServices);
 	}
 	
 	public List<DorianInformation> getDorianInformationList() {
 		return webSSOProperties.getDoriansInformation();
 	}
 
 	public List<AuthenticationServiceInformation> getAuthenticationServiceInformationList(String dorianDisplayName){
 		List<AuthenticationServiceInformation> authenticationServices=new ArrayList<AuthenticationServiceInformation>();
 		List<DorianServiceHandle> dorians  =this.getDorianServices();
 		for (DorianServiceHandle dorianHandle : dorians) {
 			if (dorianHandle.getDorianInformation().getDisplayName().equals(
 					dorianDisplayName)) {
 				List<AuthenticationServiceHandle> authenticationHandles = dorianHandle.getAuthenticationServices();
 				for (AuthenticationServiceHandle authenticationServiceHandle : authenticationHandles) {
 					AuthenticationServiceInformation serviceInformation = authenticationServiceHandle
 							.getAuthenticationServiceInformation();
 					authenticationServices.add(serviceInformation);
 				}
 				break;
 			}
 		}
 		return authenticationServices;
 	}
 	
 	public Set<QName> getAuthenticationProfilesList(
 			List<AuthenticationServiceInformation> authenticationServices,
 			String authenticationServiceURL) {
 		Set<QName> authenticationServiceProfiles = new HashSet<QName>();
 		for (AuthenticationServiceInformation authenticationServiceInformation : authenticationServices) {
 			if (authenticationServiceInformation.getAuthenticationServiceURL().equals(authenticationServiceURL)) {
 				Set<QName> tempasProfiles = authenticationServiceInformation.getAuthenticationServiceProfiles();
 				if(tempasProfiles!=null && tempasProfiles.size()>0){
 					authenticationServiceProfiles.addAll(tempasProfiles);
 				}else{
 					log.error("Authentication Profile was not specified for Authentication Service "+authenticationServiceURL +" in Credential Provider Database");
 				}
 				break;
 			}
 		}
 		return authenticationServiceProfiles;
 	}
 	
 	private void handleException(Exception e) throws InvalidResourceException,GenericException{
		if (e instanceof MalformedURLException) {
 			throw new InvalidResourceException("malformed URL has occurred", e);
 		} else if (e instanceof ResourcePropertyRetrievalException) {
 			throw new InvalidResourceException("error occured retrieving resource property", e);
 		} else if (e instanceof RemoteException) {
 			throw new GenericException("communication-related exception occured", e);
 		} else {
 			throw new GenericException("error occured", e);
 		}
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		this.startup();
 	}
 }
