 package com.madalla.webapp;
 
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.Application;
 import org.apache.wicket.IRequestTarget;
 import org.apache.wicket.Page;
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.Session;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.protocol.https.HttpsConfig;
 import org.apache.wicket.protocol.https.HttpsRequestCycleProcessor;
 import org.apache.wicket.request.IRequestCycleProcessor;
 import org.apache.wicket.request.target.coding.IndexedParamUrlCodingStrategy;
 import org.apache.wicket.request.target.coding.MixedParamHybridUrlCodingStrategy;
 import org.apache.wicket.settings.IExceptionSettings;
 
 import com.madalla.BuildInformation;
 import com.madalla.bo.SiteLanguage;
 import com.madalla.bo.image.AlbumData;
 import com.madalla.bo.image.ImageData;
 import com.madalla.bo.page.PageData;
 import com.madalla.bo.page.PageMetaLangData;
 import com.madalla.email.IEmailSender;
 import com.madalla.email.IEmailServiceProvider;
 import com.madalla.rpx.Rpx;
 import com.madalla.service.IDataService;
 import com.madalla.service.IDataServiceProvider;
 import com.madalla.service.IRepositoryAdminService;
 import com.madalla.service.IRepositoryAdminServiceProvider;
 import com.madalla.webapp.admin.content.ContentAdminPanel;
 import com.madalla.webapp.admin.image.ImageAdminPanel;
 import com.madalla.webapp.admin.member.MemberAdminPanel;
 import com.madalla.webapp.admin.pages.AdminErrorPage;
 import com.madalla.webapp.admin.pages.AlbumAdminPage;
 import com.madalla.webapp.admin.pages.MainAdminPage;
 import com.madalla.webapp.admin.pages.SecurePasswordPage;
 import com.madalla.webapp.admin.pages.UserLoginPage;
 import com.madalla.webapp.admin.pages.UserPasswordPage;
 import com.madalla.webapp.admin.site.PageAdminPanel;
 import com.madalla.webapp.admin.site.SiteAdminPanel;
 import com.madalla.webapp.admin.site.SiteDataPanel;
 import com.madalla.webapp.admin.site.SiteEmailPanel;
 import com.madalla.webapp.authorization.AuthenticatedCmsApplication;
 import com.madalla.webapp.authorization.RpxCallbackUrlHandler;
 import com.madalla.webapp.cms.editor.ContentEntryPanel;
 import com.madalla.webapp.cms.editor.TranslatePanel;
 import com.madalla.webapp.user.UserAdminPanel;
 import com.madalla.webapp.user.UserProfilePanel;
 import com.madalla.wicket.I18NBookmarkablePageRequestTargetUrlCodingStrategy;
 
 /**
  * Abstract Wicket Application class that needs to extended to enable usage 
  * of the Content Panels.
  *  
  * @author Eugene Malan
  *
  */
 public abstract class CmsApplication extends AuthenticatedCmsApplication implements IDataServiceProvider, IRepositoryAdminServiceProvider, IEmailServiceProvider {
 
 	protected final static Log log = LogFactory.getLog(CmsApplication.class);
 	public static final String SECURE_PASSWORD = "securePassword";
 	public static final String PASSWORD = "password";
 	public static final String MEMBER_REGISTRATION = "memberRegistration";
 	public static final String MEMBER_PASSWORD = "memberPassword";
 	
     private IRepositoryAdminService repositoryAdminService;
     private IEmailSender emailSender;
     private IDataService dataService;
     private Rpx rpxService;
     private BuildInformation buildInformation;
     private String configType;
     
     @Override
     protected void init() {
     	super.init();
     	//initialization checks
     	if (buildInformation == null) {
     		log.fatal("Build Information not configured Correctly.");
     		throw new WicketRuntimeException("Build Information not configured Correctly.");
     	}
     	log.info("Build Information. ecshare version:" + buildInformation.getVersion());
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
     	if (hasRpxService() && (rpxService == null || StringUtils.isEmpty(rpxService.getCallback()))){
     		log.fatal("Rpx service is not configured Correctly.");
     		throw new WicketRuntimeException("Rpx service is not configured Correctly. Configure values using the 'override.properties' file.");
     	}
         setupApplicationSpecificConfiguration();
        
     }
     
     public Session newSession(Request request, Response response) {
         return new CmsSession(request);
     }
     
     private void setupApplicationSpecificConfiguration(){
     	//getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
     	setupPageMounts();
     	setupErrorHandling();
     	setupSecurity();
     }
     
     protected void setupPageMounts(){
     	for (SiteLanguage lang : SiteLanguage.getLanguages()){
     		mount(new I18NBookmarkablePageRequestTargetUrlCodingStrategy(lang.locale, lang.getLanguageCode(), getHomePage()));
     	}
     	mountBookmarkablePage(PASSWORD, UserPasswordPage.class);
     	mountBookmarkablePage(SECURE_PASSWORD, SecurePasswordPage.class);
     	
     	mountBookmarkablePage("control", MainAdminPage.class);
     	mount(new IndexedParamUrlCodingStrategy("admin/album", AlbumAdminPage.class));
     	
     	mount(new RpxCallbackUrlHandler("openid", getHomePage(),getSignInPageClass(), rpxService){
 
 			@Override
 			protected boolean rpxLogin(HashMap<String, String> personalData) {
 				return ((CmsSession)Session.get()).authenticate(personalData);
 			}
     		
     	});
     	mount(new MixedParamHybridUrlCodingStrategy("login", UserLoginPage.class, UserLoginPage.PARAMETERS));
     	
     	if (hasMemberService()){
     		if (getMemberPasswordPage() == null) {
         		log.fatal("Member Service not configured Correctly. Member Service Password Page is null.");
         		throw new WicketRuntimeException("Member Service not configured Correctly. Member Service Password Page is null.");
         	}
     		if (getMemberRegistrationPage() == null) {
         		log.fatal("Member Service not configured Correctly. Member Service Registration Page is null.");
         		throw new WicketRuntimeException("Member Service not configured Correctly. Member Service Registration Page is null.");
         	}
     		mountBookmarkablePage(MEMBER_PASSWORD, getMemberPasswordPage());
     		mountBookmarkablePage(MEMBER_REGISTRATION, getMemberRegistrationPage());
     	}
     	
 		//mount application pages
     	for (Class<? extends Page> page : getAppPages()){
 			final PageData pageData = getRepositoryService().getPage(page.getSimpleName());
 			PageMetaLangData pageInfo = getRepositoryService().getPageMetaLang(SiteLanguage.BASE_LOCALE, pageData);
 			String mountName = StringUtils.defaultIfEmpty(pageInfo.getMountName(), page.getSimpleName());
 			try {
 				mountBookmarkablePage(mountName, page);
 			} catch (WicketRuntimeException e){
 				log.error("Error while mounting Application Page.", e);
 			}
 		}
     	
     	//mount images
     	for(AlbumData album : getRepositoryService().getAlbums()){
     		for (ImageData image : getRepositoryService().getAlbumImages(album)){
     			mountImage(image);
     		}
     	}
     	
     }
     
     @Override
     protected IRequestCycleProcessor newRequestCycleProcessor()
     {
     	HttpsConfig config = new HttpsConfig(80,443);
         return new HttpsRequestCycleProcessor(config){
 
 			@Override
 			protected IRequestTarget checkSecureIncoming(IRequestTarget target) {
 				if (getConfigurationType().equals(Application.DEVELOPMENT) || !getRepositoryService().getSiteData().getSecurityCertificate()){
 					return target;
 				} else {
 					return super.checkSecureIncoming(target);
 				}
 			}
 
 			@Override
 			protected IRequestTarget checkSecureOutgoing(IRequestTarget target) {
 				if (getConfigurationType().equals(Application.DEVELOPMENT) || !getRepositoryService().getSiteData().getSecurityCertificate()){
 					return target;
 				} else {
 					return super.checkSecureOutgoing(target);
 				}
 			}
 			
 			
         	
         };
     }
     
     public Class<? extends WebPage> getMemberRegistrationPage(){
     	return null;
     }
     
     public Class<? extends WebPage> getMemberPasswordPage(){
     	return null;
     }
     
 	protected void mountImage(ImageData image) {
     	getSharedResources().add(image.getResourceReference(), image.getImageFull());
 		mountSharedResource("/" + image.getMountUrl(), Application.class.getName() + "/" +image.getResourceReference());
     }
     private void setupErrorHandling(){
     	
     	if (DEPLOYMENT.equalsIgnoreCase(getConfigurationType())) {
         	//TODO create page for access denied exceptions
     		//TODO figure out why we get unexpected exception instead of access denied for generalAdminPage
         	//getApplicationSettings().setPageExpiredErrorPage(MyExpiredPage.class);
         	//getApplicationSettings().setAccessDeniedPage(MyAccessDeniedPage.class);
         	getApplicationSettings().setInternalErrorPage(getApplicationErrorPage());
         	getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
 		}
     }
     
     protected void setupSecurity(){
     	MetaDataRoleAuthorizationStrategy.authorize(UserProfilePanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(SiteAdminPanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(SiteDataPanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(MemberAdminPanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(ImageAdminPanel.class, "ADMIN");
     	MetaDataRoleAuthorizationStrategy.authorize(ContentAdminPanel.class, "ADMIN");
     	MetaDataRoleAuthorizationStrategy.authorize(PageAdminPanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(SiteEmailPanel.class, "SUPERADMIN");
     	MetaDataRoleAuthorizationStrategy.authorize(UserAdminPanel.class, "SUPERADMIN");
     	MetaDataRoleAuthorizationStrategy.authorize(ContentEntryPanel.class, "USER");
     	MetaDataRoleAuthorizationStrategy.authorize(TranslatePanel.class, "USER");
     }
     
 	@Override
 	protected Class<? extends WebPage> getSignInPageClass() {
 		return UserLoginPage.class;
 	}
 	
 	protected Class<? extends WebPage> getApplicationErrorPage(){
 		return AdminErrorPage.class;
 	}
 
 	@Override
 	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
 		return CmsSession.class;
 	}
 	
     @Override
 	public boolean isMemberAuthorized() {
 		return CmsSession.get().getMemberSession().isSignedIn();
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
     
     /**
      * This supplies a hook point for the admin application to create data for the pages
      * of the application (menu name, meta information). It also makes the menu more dynamic.
      * 
      * @return List of Pages used in the menu OR Collections.emptyList() if no menu
      */
     public abstract List<Class<? extends Page>> getPageMenuList();
     
     /**
      * @return all application pages
      */
     public abstract List<Class <? extends Page>> getAppPages();
     
     public boolean hasRpxService(){
     	return false;
     }
     
     public boolean hasMemberService(){
     	return false;
     }
     
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
 	
 	public BuildInformation getBuildInformation(){
 		return buildInformation;
 	}
 
 	public void setConfigType(String configType) {
 		this.configType = configType;
 	}
 
 	public void setRpxService(Rpx rpxService) {
 		this.rpxService = rpxService;
 	}
 
 	public Rpx getRpxService() {
 		return rpxService;
 	}
 
 }
