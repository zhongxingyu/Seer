 package com.madalla.webapp;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Session;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
 import org.apache.wicket.protocol.http.WebApplication;
 
 import com.madalla.service.blog.IBlogService;
 import com.madalla.service.blog.IBlogServiceProvider;
 import com.madalla.service.cms.IRepositoryAdminService;
 import com.madalla.service.cms.IRepositoryAdminServiceProvider;
 import com.madalla.service.cms.IRepositoryService;
 import com.madalla.service.cms.IRepositoryServiceProvider;
 
 /**
  * Abstract Wicket Application class that needs to extended to enable usage 
  * of the Wicket {@link Panels} provided by the Panels class.
  *  
  * @author Eugene Malan
  *
  */
 public abstract class CmsBlogApplication extends WebApplication implements IRepositoryServiceProvider, IBlogServiceProvider, IRepositoryAdminServiceProvider {
 
     private IRepositoryService repositoryService;
     private IBlogService blogService;
     private IRepositoryAdminService repositoryAdminService;
 
 	private final static Log log = LogFactory.getLog(CmsBlogApplication.class);
     
     protected void init() {
     	//initialization checks
     	if (repositoryService == null){
     		log.fatal("Content Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
     	if (blogService == null){
     		log.fatal("Blog Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
     	if (repositoryAdminService == null){
     		log.fatal("Content Admin Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
         setupApplicationSpecificConfiguration();
     }
     
     private void setupApplicationSpecificConfiguration(){
     	//getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
     	setupSecurity();
        //getMarkupSettings().setStripWicketTags(true);
     }
     
     private void setupSecurity(){
         SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
                 ISecureWebPage.class, getHomePage()) {
             protected boolean isAuthorized() {
                 return ((CmsSession)Session.get()).isCmsAdminMode();
             }
         };
  
         getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
     }
     
     public IRepositoryService getRepositoryService(){
         return repositoryService;
     }
     
     public void setRepositoryService(IRepositoryService repositoryService){
         this.repositoryService = repositoryService;
     }
 
     public void setBlogService(IBlogService blogService) {
         this.blogService = blogService;
     }
 
     public IBlogService getBlogService() {
         return blogService;
     }
     
     public IRepositoryAdminService getRepositoryAdminService() {
 		return repositoryAdminService;
 	}
 
 	public void setRepositoryAdminService(
 			IRepositoryAdminService repositoryAdminService) {
 		this.repositoryAdminService = repositoryAdminService;
 	}
 
 
 
 }
