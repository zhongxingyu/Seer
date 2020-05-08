 package com.madalla.webapp;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.CSSPackageResource;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.PropertyModel;
 
 import com.madalla.service.IDataServiceProvider;
 import com.madalla.webapp.cms.admin.ContentAdminPanel;
 import com.madalla.webapp.css.Css;
 import com.madalla.webapp.images.admin.ImageAdminPanel;
 import com.madalla.webapp.pages.AdminPanelLink;
 import com.madalla.webapp.site.SiteAdminPanel;
 import com.madalla.webapp.site.SiteDataPanel;
 import com.madalla.webapp.user.UserProfilePanel;
 
 public abstract class AdminPage extends WebPage {
 	
 	private static final long serialVersionUID = -2837757448336709448L;
 	
 	private String pageTitle = "(no title)";
 	private Class<? extends Page> returnPage;
 	
 	public AdminPage(){
 	    setPageTitle(getString("page.title"));
         add(new Label("title", new PropertyModel<String>(this,"pageTitle")));
 
         add(Css.YUI_CORE);
         add(Css.BASE);
         add(CSSPackageResource.getHeaderContribution(AdminPage.class,"AdminPage.css"));
         add(Css.CSS_BUTTONS);
         add(Css.CSS_FORM);
         setupMenu();
 	}
 	
 	protected void setupMenu(){
 		
 		add(new AdminPanelLink("UserProfile"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				getPage().replace(new UserProfilePanel(ID));
 			}
 			
 		});
 		add(new AdminPanelLink("SiteAdmin"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				getPage().replace(new SiteAdminPanel(ID));
 			}
 			
 		});
 		add(new AdminPanelLink("Data"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				getPage().replace(new SiteDataPanel(ID));
 			}
 			
 		});
 		add(new AdminPanelLink("Image"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				getPage().replace(new ImageAdminPanel(ID));
 			}
 			
 		});
 		add(new AdminPanelLink("Content"){
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				if (((IDataServiceProvider)getApplication()).getRepositoryService().isAdminApp()){
 					getPage().replace(ContentAdminPanel.newAdminInstance(ID));
 		    	} else {
 		    		getPage().replace(ContentAdminPanel.newInstance(ID));
 		    	}
 			}
 			
 		});
 		
 		add(new Link<Object>("returnLink"){
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void onClick() {
 				if (returnPage == null) {
 					returnPage = getApplication().getHomePage();
 				}
				setResponsePage(returnPage);
				//setResponsePage(getSession().getPageFactory().newPage(returnPage));
 			}
 			
 		});
 	}
 	
 	public CmsSession getAppSession(){
         return (CmsSession)getSession();
     }
 
     public void setPageTitle(String pageTitle) {
         this.pageTitle = pageTitle;
     }
 
     public String getPageTitle() {
         return pageTitle;
     }
 
 	public void setReturnPage(Class<? extends Page> returnPage) {
 		this.returnPage = returnPage;
 	}
 	
 	public Class<? extends Page> getReturnPage(){
 		return returnPage;
 	}
 
 }
