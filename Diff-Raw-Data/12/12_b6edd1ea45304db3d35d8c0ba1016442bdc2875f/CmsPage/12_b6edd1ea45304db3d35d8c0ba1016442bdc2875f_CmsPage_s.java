 package com.madalla.webapp;
 
 import static com.madalla.webapp.blog.BlogParameters.BLOG_ENTRY_ID;
 import static com.madalla.webapp.scripts.scriptaculous.Scriptaculous.PROTOTYPE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.CROSSFADE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.FAST_INIT;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Application;
 import org.apache.wicket.Page;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.behavior.StringHeaderContributor;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.JavascriptPackageResource;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 import com.madalla.bo.SiteLanguage;
 import com.madalla.bo.page.PageData;
 import com.madalla.bo.page.PageMetaLangData;
 import com.madalla.service.IDataService;
 import com.madalla.service.IDataServiceProvider;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.webapp.admin.pages.SecureLoginPage;
 import com.madalla.webapp.admin.pages.UserLoginPage;
 import com.madalla.webapp.blog.BlogHomePanel;
 import com.madalla.webapp.cms.ContentLinkPanel;
 import com.madalla.webapp.cms.ContentPanel;
 import com.madalla.webapp.cms.InlineContentPanel;
 import com.madalla.webapp.components.email.EmailFormPanel;
 import com.madalla.webapp.components.image.album.AlbumPanel;
 import com.madalla.webapp.components.image.exhibit.ExhibitPanel;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.login.LoginPanel;
 import com.madalla.webapp.modal.EcModalWindow;
 import com.madalla.webapp.scripts.JavascriptResources;
 import com.madalla.webapp.scripts.utility.ScriptUtils;
 import com.madalla.webapp.security.IAuthenticator;
 import com.madalla.wicket.animation.Animator;
 import com.madalla.wicket.animation.AnimatorSubject;
 /**
  * Base class for Application Pages that supplies Content and other functionality.
  * <p>
  * The Content Admin will supply and allow editing of meta information for this page. So make
  * sure you do not duplicate the following (title, author, keywords, description).
  * </p>
  * <p>
  * Layout functionality is supplied using YUI, so make sure to leverage that for your page layout and
  * in your style sheets. Popup login functionality and a Language switcher can be activated by overiding
  * the relevant methods.
  * </p>
  * 
  * @author Eugene Malan
  *
  */
 public abstract class CmsPage extends WebPage {
 
 	private static final String META_NAME = "<meta name=\"{0}\" content=\"{1}\"/>";
 	private static final String META_HTTP = "<meta http-equiv=\"{0}\" content=\"{1}\"/>";
 		
 	public abstract class LoginLink extends AjaxFallbackLink<Object> implements IHeaderContributor {
 		private static final long serialVersionUID = 858485459938698866L;
 
 		final CmsSession session ;
 		public LoginLink(String name, CmsSession session){
 			super(name);
 			this.session = session;
 		}
 		
 		public void onClick(AjaxRequestTarget target) {
 			if (session.isSignedIn()) {
 				session.signOut();
 				setResponsePage(getPage());
 			} else {
 				if (target != null) {
 					onClickAction(target);
 				} else {
 					error(getString("label.javascript.error"));
 				}
 			}
 		}
 
 		protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
 			if (session.isLoggedIn()) {
 				replaceComponentTagBody(markupStream, openTag, getString("label.logout"));
 			} else {
 				replaceComponentTagBody(markupStream, openTag, getString("label.login"));
 			}
 		}
 		
 		protected abstract void onClickAction(AjaxRequestTarget target);
 		
 	}
 	
 	public CmsPage(PageParameters parameters){
 		super(parameters);
 		commonInit();
 	}
 
 	public CmsPage() {
 		super();
 		commonInit();
 	}
 	
 	private void commonInit(){
 		add(Css.YUI_CORE);
 		add(Css.BASE);
 		
 		if (isHomePage()) {
 			setLocaleFromUrl(getRequest().getURL());
 		}
 		PageData pageData = getRepositoryService().getPage(getPageName());
 		PageMetaLangData pageInfo = getRepositoryService().getPageMetaLang(getLocale(), pageData);
 		processPageMetaInformation(pageInfo);
 		
 		//used to return to Site from Admin Pages
 		getAppSession().setLastSitePage(getPageMapEntry());
 
 		if (hasPopupLogin()) {
 			setupPopupLogin();
 		} else {
 			add(new Label("signInPanel")); //hidden
 			add(new Label("closeLogin"));
 		}
 
 		if (hasLoginLink()) {
 			setupLoginLink();
 		}
 
 		if (hasLangDropDown()) {
 			setupLangDropDown();
 		}
 		
 		if (hasInfoDialog()) {
 			setupInfoDialog();
 		} else {
 			add(new Label("infoDialog").setVisible(false));
 		}
 		
 		if (hasCrossfadeSupport()){
 	    	add(JavascriptPackageResource.getHeaderContribution(PROTOTYPE));
 			add(JavascriptPackageResource.getHeaderContribution(JavascriptResources.ANIMATOR));
 	        add(JavascriptPackageResource.getHeaderContribution(FAST_INIT));
 	        
 	        add(JavascriptPackageResource.getHeaderContribution(CROSSFADE));
 			add(ScriptUtils.CROSSFADE_CSS);
 		}
 		
 	}
 	
 	private void processPageMetaInformation(PageMetaLangData pageInfo){
 		
 		add(new StringHeaderContributor(MessageFormat.format(META_HTTP, "lang", pageInfo.getLang())));
 		
 		if (!isMetadataOveridden()) { //TODO Store this as field on Page
 			if (StringUtils.isNotEmpty(pageInfo.getTitle())){
 				add(new StringHeaderContributor("<title>" + pageInfo.getTitle() + "</title>"));
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getAuthor())){
 				add(new StringHeaderContributor(MessageFormat.format(META_NAME, "author", pageInfo.getAuthor())));
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getDescription())) {
 				add(new StringHeaderContributor(MessageFormat.format(META_NAME, "description", pageInfo.getDescription())));
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getKeywords())) {
 				add(new StringHeaderContributor(MessageFormat.format(META_NAME, "keywords", pageInfo.getKeywords())));
 			}
 		}
 	}
 	
 	private void setupInfoDialog(){
 		final ModalWindow modal;
 		add(modal = new EcModalWindow("infoDialog"));
 		modal.setInitialHeight(200);
 		modal.setInitialWidth(300);
 		modal.setTitle(new Model<String>(getString("label.siteinfo")));
 		modal.setContent(new InfoPanel("content"));
 		
 		add(new AjaxLink<Void>("infoDialogLink"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				modal.show(target);
 			}
 			
 		});
 	}
 	
 	private void setupPopupLogin(){
 		
 		add(JavascriptPackageResource.getHeaderContribution(JavascriptResources.ANIMATOR));
 		
 		//Animator to open and close login popup
 		final Animator animator = new Animator(700)
 			.addSubject(AnimatorSubject.numeric("loginPopup","opacity", 0.0, 1.0))
 			.addSubject(AnimatorSubject.discrete("loginPopup", "display", "none","", 0.1));
 		
 		add(new LoginLink("logon", getAppSession()){
 			private static final long serialVersionUID = 1L;
 
 			
 			@Override
 			protected void onClickAction(AjaxRequestTarget target) {
 				target.appendJavascript(animator.toggle());
 			}
 
 			@Override
 			protected void onComponentTag(ComponentTag tag) {
 				tag.put("rel", "nofollow");
 				super.onComponentTag(tag);
 			}
 
 			public void renderHead(IHeaderResponse response) {
 				animator.setUniqueId(getMarkupId());
 				animator.renderHead(response);
 			}
 			
 			
 		});
 		
 		add(new AjaxLink<String>("closeLogin"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				target.appendJavascript(animator.toggle());
 				
 			}
 			
 		});
 
 		add(new LoginPanel("signInPanel", new SecureCredentials(), false) {
 			private static final long serialVersionUID = 1L;
 			private static final int loginMax = 4;
 			private int count = 0;
 
 			@Override
 			protected void preSignIn(String username) {
 				if (getApplication().getConfigurationType().equals(Application.DEVELOPMENT)) {
 					return;
 				}
 				IAuthenticator authenticator = getRepositoryService().getUserAuthenticator();
 				if (authenticator.requiresSecureAuthentication(username)) {
 					redirectToInterceptPage(new SecureLoginPage(username));
 				}
 			}
 
 			@Override
 			public boolean signIn(String username, String password) {
 				return getAppSession().signIn(username, password);
 			}
 
 			@Override
 			protected void onSignInSucceeded() {
 				// If login has been called because the user was not yet
 				// logged in, then continue to the original destination,
 				// otherwise to the Home page
 				if (!continueToOriginalDestination()) {
 					setResponsePage(getPage());
 				}
 			}
 
 			@Override
 			protected void onSignInFailed(String username) {
 				super.onSignInFailed(username);
 				count++;
 				if (count >= loginMax) {
 					redirectToInterceptPage(new UserLoginPage(new PageParameters("username="+username)));
 				}
 			}
 
 		});
 
 	}
 	
 	private void setupLoginLink(){
 		add(new LoginLink("logon", getAppSession()){
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onClickAction(AjaxRequestTarget target) {
 				setResponsePage(UserLoginPage.class);
 				//redirectToInterceptPage(new UserLoginPage(getPageClass()));
 			}
 
 			public void renderHead(IHeaderResponse response) {
 				// no contribution, Sorry!
 			}
 			
 		});
 	}
 	
 	private void setupLangDropDown(){
 		List<SiteLanguage> langs = getRepositoryService().getSiteData().getLocaleList();
 		langs.add(SiteLanguage.ENGLISH);
 
 		final IModel<SiteLanguage> langModel = new Model<SiteLanguage>();
 		langModel.setObject(SiteLanguage.getLanguage(getSession().getLocale().getLanguage()));
 		DropDownChoice<SiteLanguage> choice = new DropDownChoice<SiteLanguage>("langSelect", langModel, langs,
 				new ChoiceRenderer<SiteLanguage>("locale.displayLanguage"));
 
 		choice.setNullValid(false);
 		choice.add(new OnChangeAjaxBehavior() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				getSession().setLocale(langModel.getObject().locale);
 				setResponsePage(getPage());
 			}
 
 		});
 		add(choice);
 	}
 	
 	private void setLocaleFromUrl(String url){
 		String[] urlfrags = url.split("/");
 		String s = urlfrags[urlfrags.length - 1];
 		if (s.length() == 2){
 			getSession().setLocale(SiteLanguage.getLanguage(s).locale);
 		}
 	}
 
 	private IDataService getRepositoryService() {
 		return ((IDataServiceProvider) getApplication()).getRepositoryService();
 	}
 
 	private boolean isHomePage() {
 		return getApplication().getHomePage().equals(this.getPageClass());
 	}
 
 	public String getPageName(){
 		return getPageClass().getSimpleName();
 	}
 
 	/**
 	 * @return true if you want to set your own metadata, otherwise the metadata
 	 *         will be set from the CMS values
 	 */
 	protected boolean isMetadataOveridden() {
 		return false;
 	}
 
 	/**
 	 * @return true if you want to use the built in popup login
 	 */
 	protected boolean hasPopupLogin() {
 		return false;
 	}
 
 	/**
 	 * @return true if you want to use the built in popup login
 	 */
 	protected boolean hasLoginLink() {
 		return false;
 	}
 
 	/**
 	 * 
 	 * @return true if you want a lang dropdown for dynamic languae switching
 	 */
 	protected boolean hasLangDropDown() {
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @return true if you want modal popup with site information
 	 * 
 	 * You must add 'infoDialogLink' to page
 	 */
 	protected boolean hasInfoDialog() {
 		return false;
 	}
 	
 	/**
 	 * Crossfade support allows you to identify a Rotating list by adding a class
 	 * of .crossfade
 	 * 
 	 * @return true if you want support for Crossfade
 	 */
 	protected boolean hasCrossfadeSupport() {
 		return false;
 	}
 
 	
 	/**
 	 * Add a Text Content Panel to this page. NOTE: be careful when using this method
 	 * from an abstract class as new Panels will be created for each implementation.
 	 * 
 	 * @param id
 	 */
 	protected Panel addContentPanel(String id){
 		Panel panel = new ContentPanel(id, getPageName());
 		add(panel);
 		return panel;
 	}
 	
 	protected Panel addContentInlinePanel(String id){
 		Panel panel = new InlineContentPanel(id, getPageName());
 		add(panel);
 		return panel;
 	}
 	
 	protected Panel addContentLinkPanel(String id){
 		Panel panel = new ContentLinkPanel(id, getPageName());
 		add(panel);
 		return panel;
 	}
 	
 	/**
 	 * @param id
 	 * @param subject - this will appear as subject in the email
 	 * @return
 	 */
 	protected Panel addEmailPanel(String id, String subject){
 		Panel panel = new EmailFormPanel(id, subject);
 		add(panel);
 		return panel;
 	}
 	
 	protected Panel addImagePanel(String id, String albumName){
 		Panel panel = new AlbumPanel(id, albumName);
 		add(panel);
 		return panel;
 	}
 	
 	protected Panel addExhibitPanel(String id) {
 		Panel panel = new ExhibitPanel(id);
 		add(panel);
 		return panel;
 	}
 	
 	protected Panel addBlogPanel(String id, String blogName, PageParameters parameters){
 		String blogEntryId = parameters.getString(BLOG_ENTRY_ID);
     	Panel panel = new BlogHomePanel("blogPanel", "mainBlog", blogEntryId);
     	add(panel);
     	return panel;
 	}
 	
 	protected CmsSession getAppSession(){
 		return (CmsSession) getSession();
 	}
 	
 	protected List<IMenuItem>getPageMetaData(){
 		List<Class<? extends Page>> pages = ((CmsApplication)getApplication()).getPageMenuList();
 		return getPageMetaData(pages);
 	}
 	
 	protected List<IMenuItem> getPageMetaData(List<Class<? extends Page>> pages){
 		final List<IMenuItem> items = new ArrayList<IMenuItem>();
 		for(final Class<? extends Page> page: pages){
 
 			final PageData pageData = getRepositoryService().getPage(page.getSimpleName());
 
 			items.add(new IMenuItem(){
 
 				private static final long serialVersionUID = 1L;
 
 				public Class<? extends Page> getItemClass() {
 					return page;
 				}
 
 				public String getItemName() {
 					PageMetaLangData pageInfo = getRepositoryService().getPageMetaLang(getSession().getLocale(), pageData);
 					return StringUtils.defaultIfEmpty(pageInfo.getDisplayName(), page.getSimpleName());
 				}
 				
 			});
 		}
 		return items;
 	}
 	
 }
