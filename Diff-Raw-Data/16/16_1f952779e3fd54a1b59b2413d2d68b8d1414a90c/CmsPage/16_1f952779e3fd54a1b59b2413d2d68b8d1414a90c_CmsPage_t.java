 package com.madalla.webapp;
 
 import static com.madalla.webapp.PageParams.RETURN_PAGE;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Application;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.behavior.StringHeaderContributor;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 
 import com.madalla.bo.SiteData;
 import com.madalla.bo.SiteLanguage;
 import com.madalla.service.IDataService;
 import com.madalla.service.IDataServiceProvider;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.login.LoginPanel;
 import com.madalla.webapp.pages.SecureLoginPage;
 import com.madalla.webapp.pages.UserLoginPage;
 import com.madalla.webapp.security.IAuthenticator;
 import com.madalla.wicket.KeywordHeaderContributor;
 
 public class CmsPage extends WebPage {
 
     private static final String PAGE_TITLE = "page.title";
 
     private String pageTitle = "(no title)";
     
 	public CmsPage(){
 	    super();
 	    setPageTitle(getString(PAGE_TITLE));
         add(new Label("title", new PropertyModel<String>(this,"pageTitle")));
 
 		add(Css.YUI_CORE);
 		add(Css.BASE);
 		
 		add(new StringHeaderContributor("<meta name=\"AUTHOR\" content=\"Eugene Malan\" />"));
 		if (!isMetadataOveridden()){
 		    SiteData siteData = getRepositoryService().getSiteData();
 		    if (StringUtils.isNotEmpty(siteData.getMetaDescription())){
 		        add(new StringHeaderContributor("<meta name=\"description\" content=\"" + siteData.getMetaDescription() + "\"/>" ));
 		    }
 		    if (StringUtils.isNotEmpty(siteData.getMetaKeywords())){
 		        add(new KeywordHeaderContributor(siteData.getMetaKeywords()));
 		    }
 		}
 		
         if (hasPopupLogin()) {
 
             //Login feedback
 //            FeedbackPanel feedback = new FeedbackPanel("feedback");
 //            feedback.setOutputMarkupId(true);
 //            add(feedback);
 
             //Logon link
             add(new AjaxFallbackLink<Object>("logon") {
                 private static final long serialVersionUID = 1L;
                 CmsSession session = (CmsSession) getSession();
                 
                 public void onClick(AjaxRequestTarget target) {
                     if (session.isLoggedIn()){
                         session.logout();
                         setResponsePage(getPage());
                     } else {
                         if (target != null){
                             target.appendJavascript("var elm = wicketGet('loginPopup'); wicketShow(elm); elm.focus();");
                         } else {
                             error(getString("label.javascript.error"));
                         }
                     } 
                 }
                 protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                     if(session.isLoggedIn()){
                         replaceComponentTagBody(markupStream, openTag, getString("label.logout"));
                     } else {
                         replaceComponentTagBody(markupStream, openTag, getString("label.login"));
                     }
                 }
             });
 
             add(new LoginPanel("signInPanel", new SecureCredentials(), false){
             	private static final long serialVersionUID = 1L;
             	private static final int loginMax = 4;
             	private int count = 0;
             	
             	@Override
 				protected void preSignIn(String username) {
             		if (getApplication().getConfigurationType().equals(Application.DEVELOPMENT)){
             			return;
             		}
             		IAuthenticator authenticator = getRepositoryService().getUserAuthenticator();
             		if (authenticator.requiresSecureAuthentication(username)){
             			redirectToInterceptPage(new SecureLoginPage(new PageParameters(RETURN_PAGE + "=" + getPage().getClass().getName()), username));
             		}
 				}
                 
 				@Override
                 public boolean signIn(String username, String password) {
                     CmsSession session = (CmsSession) getSession();
                     if (session.login(username, password)) {
                         return true;
                     } else {
                         return false;
                     }
                 }
 
                 @Override
                 protected void onSignInSucceeded() {
                     // If login has been called because the user was not yet
                     // logged in, then continue to the original destination,
                     // otherwise to the Home page
                     if (!continueToOriginalDestination())   {
                         setResponsePage(getPage());
                     }
                 }
 
 				@Override
 				protected void onSignInFailed(String username) {
 					super.onSignInFailed(username);
 					count++;
 					if (count >= loginMax) {
 						redirectToInterceptPage(new UserLoginPage(new PageParameters(RETURN_PAGE + "=" + getPage().getClass().getName()), username));
 					}
 				}
                 
                 
             });
 
         } else {
             //dummy label that will end up hidden on page
             add(new Label("signInPanel"));
         }
         
         if (hasLoginLink()){
             add(new AjaxFallbackLink<Object>("logon") {
                 private static final long serialVersionUID = 1L;
                 CmsSession session = (CmsSession) getSession();
                 
                 public void onClick(AjaxRequestTarget target) {
                     if (session.isLoggedIn()){
                         session.logout();
                         setResponsePage(getPage());
                     } else {
                         if (target != null){
                             setResponsePage(new UserLoginPage(new PageParameters(RETURN_PAGE + "=" + getPage().getClass().getName())));
                         } else {
                             error(getString("label.javascript.error"));
                         }
                     } 
                 }
                 protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                     if(session.isLoggedIn()){
                         replaceComponentTagBody(markupStream, openTag, getString("label.logout"));
                     } else {
                         replaceComponentTagBody(markupStream, openTag, getString("label.login"));
                     }
                 }
             });
         }
         
         if (hasLangDropDown()) {
         	List<SiteLanguage> langs = getRepositoryService().getSiteData().getLocaleList();
         	langs.add(SiteLanguage.ENGLISH);
         	
         	final IModel<SiteLanguage> langModel = new Model<SiteLanguage>();
         	langModel.setObject(SiteLanguage.getLanguage(getSession().getLocale().getLanguage()));
        	DropDownChoice<SiteLanguage> choice = new DropDownChoice<SiteLanguage>("langSelect", langModel, langs, new ChoiceRenderer<SiteLanguage>("locale.displayLanguage"));
         	
         	choice.setNullValid(false);
         	choice.add(new OnChangeAjaxBehavior(){
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void onUpdate(AjaxRequestTarget target) {
 					getSession().setLocale(langModel.getObject().locale);
 					setResponsePage(getPage());
 				}
         		
         	});
         	add(choice);
         	
         }
 
 	}
 	
 	
 	/**
 	 * @return true if you want to set your own metadata, otherwise the metadata will be set from the CMS values
 	 */
 	protected boolean isMetadataOveridden(){
 	    return false;
 	}
 	
 	/**
 	 * @return true if you want to use the built in popup login
 	 */
 	protected boolean hasPopupLogin(){
 	    return false;
 	}
 	
     /**
      * @return true if you want to use the built in popup login
      */
     protected boolean hasLoginLink(){
         return false;
     }
     
     /**
      * 
      * @return true if you want a lang dropdown for dynamic languae switching
      */
     protected boolean hasLangDropDown(){
     	return false;
     }
 
     protected void setPageTitle(String pageTitle) {
         this.pageTitle = pageTitle;
     }
 
     protected String getPageTitle() {
         return pageTitle;
     }
     
     protected IDataService getRepositoryService(){
     	return ((IDataServiceProvider)getApplication()).getRepositoryService();
     }
 }
