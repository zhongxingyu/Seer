 package com.madalla.webapp;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Session;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.authorization.strategies.page.SimplePageAuthorizationStrategy;
 import org.apache.wicket.protocol.http.WebApplication;
 
 import com.madalla.service.blog.IBlogService;
 import com.madalla.service.blog.IBlogServiceProvider;
 import com.madalla.service.cms.IContentAdminService;
 import com.madalla.service.cms.IContentAdminServiceProvider;
 import com.madalla.service.cms.IContentService;
 import com.madalla.service.cms.IContentServiceProvider;
 
 /**
  * Abstract Wicket Application class that needs to extended to enable usage 
  * of the Wicket {@link Panels} provided by the Panels class.
  *  
  * @author Eugene Malan
  *
  */
 public abstract class CmsBlogApplication extends WebApplication implements IContentServiceProvider, IBlogServiceProvider, IContentAdminServiceProvider {
 
     private IContentService contentService;
     private IBlogService blogService;
     private IContentAdminService contentAdminService;
    private final static Log log = LogFactory.getLog(CmsBlogEmailApplication.class);
     
     protected void init() {
     	//initialization checks
     	if (contentService == null){
     		log.fatal("Content Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
     	if (blogService == null){
     		log.fatal("Blog Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
     	if (contentAdminService == null){
     		log.fatal("Content Admin Service is not configured Correctly.");
     		throw new WicketRuntimeException("Service is not configured Correctly.");
     	}
         setupApplicationSpecificConfiguration();
     }
     
     protected void setupApplicationSpecificConfiguration(){
     	getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
     	setupSecurity();
         //getMarkupSettings().setStripWicketTags(true);
     }
     
     protected void setupSecurity(){
         SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
                 ISecureWebPage.class, getHomePage()) {
             protected boolean isAuthorized() {
                 return ((CmsSession)Session.get()).isCmsAdminMode();
             }
         };
  
         getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
     }
     
     public IContentService getContentService(){
         return contentService;
     }
     
     public void setContentService(IContentService contentService){
         this.contentService = contentService;
     }
 
     public void setBlogService(IBlogService blogService) {
         this.blogService = blogService;
     }
 
     public IBlogService getBlogService() {
         return blogService;
     }
 
     public IContentAdminService getContentAdminService() {
         return contentAdminService;
     }
 
     public void setContentAdminService(IContentAdminService contentAdminService) {
         this.contentAdminService = contentAdminService;
     }
 
 }
