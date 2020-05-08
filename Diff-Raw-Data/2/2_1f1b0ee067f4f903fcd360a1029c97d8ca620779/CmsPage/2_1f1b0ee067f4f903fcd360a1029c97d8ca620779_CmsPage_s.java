 package com.madalla.webapp;
 
 import static com.madalla.webapp.scripts.scriptaculous.Scriptaculous.PROTOTYPE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.CROSSFADE;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.CROSSFADE_CSS;
 import static com.madalla.webapp.scripts.utility.ScriptUtils.FAST_INIT;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Page;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.Url.QueryParameter;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.emalan.cms.IDataService;
 import org.emalan.cms.bo.SiteLanguage;
 import org.emalan.cms.bo.image.AlbumData;
 import org.emalan.cms.bo.image.ImageData;
 import org.emalan.cms.bo.page.PageData;
 import org.emalan.cms.bo.page.PageMetaLangData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.madalla.service.ApplicationService;
 import com.madalla.service.IApplicationServiceProvider;
 import com.madalla.util.security.SecureCredentials;
 import com.madalla.webapp.admin.pages.SecureLoginPage;
 import com.madalla.webapp.admin.pages.UserLoginPage;
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
 	
 	private static final long serialVersionUID = 1L;
 	private static final Logger log = LoggerFactory.getLogger(CmsPage.class);
 	private static final String META_NAME = "<meta name=\"{0}\" content=\"{1}\"/>";
 	private static final String META_HTTP = "<meta http-equiv=\"{0}\" content=\"{1}\"/>";
 
 	public abstract class LoginLink extends AjaxFallbackLink<Object> implements IHeaderContributor {
 		private static final long serialVersionUID = 858485459938698866L;
 
 		final CmsSession session ;
 		public LoginLink(String name, CmsSession session){
 			super(name);
 			this.session = session;
 		}
 
 		@Override
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
 
 		@Override
 		public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
 			if (session.isLoggedIn()) {
 				replaceComponentTagBody(markupStream, openTag, getString("label.logout"));
 			} else {
 				replaceComponentTagBody(markupStream, openTag, getString("label.login"));
 			}
 		}
 
 		protected abstract void onClickAction(AjaxRequestTarget target);
 
 	}
 	
 	private final PageMetaLangData pageInfo;
 
 	public CmsPage(PageParameters parameters){
 		super(parameters);
 		PageData pageData = getRepositoryService().getPage(getPageName());
 		pageInfo = getRepositoryService().getPageMetaLang(getLocale(), pageData);
 		commonInit();
 	}
 
 	public CmsPage() {
 		super();
 		PageData pageData = getRepositoryService().getPage(getPageName());
 		pageInfo = getRepositoryService().getPageMetaLang(getLocale(), pageData);
 		commonInit();
 	}
 
 	private void commonInit(){
 
 		if (isHomePage()) {
 			setLocaleFromUrl(getRequest().getUrl());
 		}
 
 		//store last page so we can return from Admin Pages to last used site page
 		getAppSession().setLastSitePage(getPageReference());
 		getPageReference();
 
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
 
 
 
 	}
 	
 	@Override
 	public void renderHead(IHeaderResponse response) {
 		response.renderCSSReference(Css.YUI_CORE);
 		response.renderCSSReference(Css.BASE);
 		
 		if (hasCrossfadeSupport()){
 			response.renderJavaScriptReference(PROTOTYPE);
 			response.renderJavaScriptReference(JavascriptResources.ANIMATOR);
 			response.renderJavaScriptReference(FAST_INIT);
 			response.renderJavaScriptReference(CROSSFADE);
 			response.renderCSSReference(CROSSFADE_CSS);
 		}
 
 		response.renderJavaScriptReference(JavascriptResources.ANIMATOR);
 		
 		response.renderString(MessageFormat.format(META_HTTP, "lang", pageInfo.getLang()));
 		
 
 		if (!isMetadataOveridden()) {
 			if (StringUtils.isNotEmpty(pageInfo.getTitle())){
 				response.renderString("<title>" + pageInfo.getTitle() + "</title>");
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getAuthor())){
 				response.renderString(MessageFormat.format(META_NAME, "author", pageInfo.getAuthor()));
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getDescription())) {
 				response.renderString(MessageFormat.format(META_NAME, "description", pageInfo.getDescription()));
 			}
 			if (StringUtils.isNotEmpty(pageInfo.getKeywords())) {
 				response.renderString(MessageFormat.format(META_NAME, "keywords", pageInfo.getKeywords()));
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
 
 		//Animator to open and close login popup
 		final Animator animator = new Animator(700)
 			.addSubject(AnimatorSubject.numeric("loginPopup","opacity", 0.0, 1.0))
 			.addSubject(AnimatorSubject.discrete("loginPopup", "display", "none","", 0.1));
 
 		//Link that opens login popup
 		add(new LoginLink("logon", getAppSession()){
 			private static final long serialVersionUID = 1L;
 
 
 			@Override
 			protected void onClickAction(AjaxRequestTarget target) {
 				target.appendJavaScript(animator.toggle());
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
 
 		//link that closes the popup
 		add(new AjaxLink<String>("closeLogin"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick(AjaxRequestTarget target) {
 				target.appendJavaScript(animator.toggle());
 
 			}
 
 		});
 
 		add(new LoginPanel("signInPanel", new SecureCredentials()) {
 			private static final long serialVersionUID = 1L;
 			private static final int loginMax = 4;
 			private int count = 0;
 
 			@Override
 			protected void preSignIn(String username) {
 				if (getApplication().getConfigurationType().equals(RuntimeConfigurationType.DEVELOPMENT)) {
 					return;
 				}
 				IAuthenticator authenticator = getApplicationService().getUserAuthenticator();
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
 					PageParameters parameters = new PageParameters();
 					parameters.set("username", username);
 					redirectToInterceptPage(new UserLoginPage(parameters));
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
 
 	private void setLocaleFromUrl(Url url){
 	    List<String> segments = url.getSegments();
 		log.info("setLocaleFromUrl - " + segments);
		if (segments.get(0) != null) {
 		    String s = segments.get(0);
 	        if (s.length() == 2){
 	            getSession().setLocale(SiteLanguage.getLanguage(s).locale);
 	        }
 		}
 		
 	}
 	
 	private ApplicationService getApplicationService() {
 	    return ((IApplicationServiceProvider) getApplication()).getApplicationService();
 	}
 
 	private IDataService getRepositoryService() {
 		return getApplicationService().getRepositoryService();
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
 		final AlbumData album = getRepositoryService().getAlbum(id);
 		IModel<List<ImageData>> imagesModel = new LoadableDetachableModel<List<ImageData>>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<ImageData> load() {
 				return getRepositoryService().getAlbumImages(album);
 			}
 		};
 		Panel panel = new AlbumPanel(id, album, imagesModel);
 		add(panel);
 		return panel;
 	}
 
 	/**
 	 * Add exhibit panel to page. This method shows the new preferred way to construct components so that
 	 * the components do not need to know where the data comes from.
 	 * 
 	 * @param id
 	 * @return Panel
 	 */
 	protected Panel addExhibitPanel(String id) {
 		final AlbumData album = getRepositoryService().getAlbum(id);
 		
 		IModel<List<ImageData>> imagesModel = new LoadableDetachableModel<List<ImageData>>() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected List<ImageData> load() {
 				return getRepositoryService().getAlbumImages(album);
 			}
 		};
 		Panel panel = new ExhibitPanel(id, album, imagesModel);
 		add(panel);
 		return panel;
 	}
 
 	protected CmsSession getAppSession(){
 		return (CmsSession) getSession();
 	}
 
 	protected List<IMenuItem>getPageMetaData(){
 		Collection<Class<? extends Page>> pages = ((CmsApplication)getApplication()).getPageMenuList();
 		return getPageMetaData(pages);
 	}
 
 	protected List<IMenuItem> getPageMetaData(Collection<Class<? extends Page>> pages){
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
