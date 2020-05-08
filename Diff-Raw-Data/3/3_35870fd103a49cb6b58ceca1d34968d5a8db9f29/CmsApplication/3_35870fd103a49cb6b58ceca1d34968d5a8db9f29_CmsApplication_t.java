 package com.madalla.webapp;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Page;
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.Session;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.https.HttpsConfig;
 import org.apache.wicket.protocol.https.HttpsRequestCycleProcessor;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.settings.IExceptionSettings;
 
 import com.madalla.BuildInformation;
 import com.madalla.bo.SiteLanguage;
 import com.madalla.email.IEmailSender;
 import com.madalla.email.IEmailServiceProvider;
 import com.madalla.service.IDataService;
 import com.madalla.service.IDataServiceProvider;
 import com.madalla.service.IRepositoryAdminService;
 import com.madalla.service.IRepositoryAdminServiceProvider;
 import com.madalla.webapp.authorization.AppAuthorizationStrategy;
 import com.madalla.webapp.authorization.PageAuthorization;
 import com.madalla.webapp.pages.AdminErrorPage;
 import com.madalla.webapp.pages.SecurePasswordPage;
 import com.madalla.webapp.pages.UserPasswordPage;
 import com.madalla.wicket.I18NBookmarkablePageRequestTargetUrlCodingStrategy;
 
 /**
  * Abstract Wicket Application class that needs to extended to enable usage 
  * of the Content Panels.
  *  
  * @author Eugene Malan
  *
  */
 public abstract class CmsApplication extends WebApplication implements IDataServiceProvider, IRepositoryAdminServiceProvider, IEmailServiceProvider {
 
 	protected final static Log log = LogFactory.getLog(CmsApplication.class);
 
     private IRepositoryAdminService repositoryAdminService;
     private IEmailSender emailSender;
     private IDataService dataService;
     private BuildInformation buildInformation;
     private String configType;
     
     protected void init() {
     	//initialization checks
     	if (buildInformation == null) {
     		log.fatal("Build Information not configured Correctly.");
     		throw new WicketRuntimeException("Build Information not configured Correctly.");
     	}
     	log.info("Build Information. version:" + buildInformation.getVersion());
     	if (repositoryAdminService == null){
     		log.fatal("Content Admin Service is not configured Correctly.");
     		throw new WicketRuntimeException("Repository Admin Service is not configured Correctly.");
     	}
     	if (dataService == null){
     		log.fatal("Repository Data Service is not configured Correctly.");
     		throw new WicketRuntimeException("Repository Data Service is not configured Correctly.");
     	}
     	if (emailSender == null){
     		log.fatal("Email Sender is not configured Correctly.");
     		throw new WicketRuntimeException("Email Service is not configured Correctly.");
     	}
         setupApplicationSpecificConfiguration();
     }
     
     public Session newSession(Request request, Response response) {
         return new CmsSession(request);
     }
     
     private void setupApplicationSpecificConfiguration(){
     	//getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
     	setupSecurity();
     	setupPageMounts();
     	setupErrorHandling();
     }
     
     public void setupPageMounts(){
     	for (SiteLanguage lang : SiteLanguage.getLanguages()){
     		mount(new I18NBookmarkablePageRequestTargetUrlCodingStrategy(lang.locale, lang.getLanguageCode(), getHomePage()));
     	}
     	mountBookmarkablePage("password", UserPasswordPage.class);
     	mountBookmarkablePage("securePassword", SecurePasswordPage.class);
     }
     
     protected void setupErrorHandling(){
     	
     	final String configurationType = getConfigurationType();
     	if (DEPLOYMENT.equalsIgnoreCase(configurationType))
 		{
        	//TODO create page for access denied exceptions
    		//TODO figure out why we get unexpected exception instead of access denied for generalAdminPage
         	//getApplicationSettings().setPageExpiredErrorPage(MyExpiredPage.class);
         	//getApplicationSettings().setAccessDeniedPage(MyAccessDeniedPage.class);
         	getApplicationSettings().setInternalErrorPage(AdminErrorPage.class);
         	getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
 		}
     }
     
     protected void setupSecurity(){
     	
     	//List to hold page authorizations
     	Collection <PageAuthorization> pageAuthorizations = new ArrayList<PageAuthorization>();
     	
     	//Admin Page authorization
     	PageAuthorization adminAuthorization = new PageAuthorization(ISecureAdminPage.class){
 			@Override
 			protected boolean isAuthorized() {
                 return ((CmsSession)Session.get()).isCmsAdminMode();
             }
     	};
     	pageAuthorizations.add(adminAuthorization);
 
     	//Logged in page authorization
     	PageAuthorization loggedInAuthorization = new PageAuthorization(ISecureWebPage.class){
 			@Override
 			protected boolean isAuthorized() {
                 return ((CmsSession)Session.get()).isLoggedIn();
             }
     	};
     	pageAuthorizations.add(loggedInAuthorization);
     	
     	//Super Admin page authorization
     	PageAuthorization superAdminAthorization = new PageAuthorization(ISecureSuperPage.class){
     		@Override
     		protected boolean isAuthorized() {
     			return ((CmsSession)Session.get()).isSuperAdmin();
     		}
     	};
     	pageAuthorizations.add(superAdminAthorization);
     	
     	//create Authorization strategy
     	AppAuthorizationStrategy authorizationStrategy = new AppAuthorizationStrategy(
                 getHomePage(), pageAuthorizations);
  
         getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
     }
     
     /* (non-Javadoc)
      * @see org.apache.wicket.protocol.http.WebApplication#getConfigurationType()
      */
     @Override
 	public String getConfigurationType() {
     	if (configType == null){
     		return super.getConfigurationType();
     	} else {
     		return configType;
     	}
 	}
     
     @Override
     protected IRequestCycleProcessor newRequestCycleProcessor()
     {
     	HttpsConfig config = new HttpsConfig(80,443);
             return new HttpsRequestCycleProcessor(config);
     }
     
     public abstract List<Class<? extends Page>> getPageMenuList();
 
     public IEmailSender getEmailSender() {
         return emailSender;
     }
 
     public void setEmailSender(IEmailSender emailSender) {
         this.emailSender = emailSender;
     }
     
     public IRepositoryAdminService getRepositoryAdminService() {
 		return repositoryAdminService;
 	}
 
 	public void setRepositoryAdminService(
 			IRepositoryAdminService repositoryAdminService) {
 		this.repositoryAdminService = repositoryAdminService;
 	}
 	
 	public IDataService getRepositoryService() {
 		return dataService;
 	}
 	
 	public void setRepositoryService(IDataService dataService){
 		this.dataService = dataService;
 	}
 
 	public void setBuildInformation(BuildInformation buildInformation) {
 		this.buildInformation = buildInformation;
 	}
 
 	public void setConfigType(String configType) {
 		this.configType = configType;
 	}
 
 }
