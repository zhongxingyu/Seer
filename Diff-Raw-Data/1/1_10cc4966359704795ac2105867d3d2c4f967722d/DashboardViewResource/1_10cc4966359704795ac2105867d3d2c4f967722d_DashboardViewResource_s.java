 package com.marthym.oikonomos.main.client.resources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundleWithLookup;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.resources.client.ImageResource.ImageOptions;
 
 public interface DashboardViewResource extends ClientBundleWithLookup {
 	public DashboardViewResource INSTANCE = GWT.create(DashboardViewResource.class);
 
 	@Source("img/logout.png")
 	@ImageOptions(height=32, width=32)
 	ImageResource logout();
 	
 	@Source("img/profile.png")
 	ImageResource profile();
 
 	@Source("img/profile.png")
 	@ImageOptions(height=32, width=32)
 	ImageResource profile32();
 
 	@Source("img/lm-accounts.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmaccount();
 	
 	@Source("img/lm-budgetary.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmbudgetary_line();
 	
 	@Source("img/lm-categories.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmcategory();
 
 	@Source("img/lm-payees.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmpayee();
 
 	@Source("img/lm-reports.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmreport();
 
 	@Source("img/lm-schedulers.png")
 	@ImageOptions(height=24, width=24)
 	ImageResource lmscheduler();
 
 	@Source("img/lm-accounts.png")
 	ImageResource mvAccounts();
 
 	@Source("img/lm-categories.png")
 	ImageResource mvCategories();
 
 	@Source("img/lm-payees.png")
 	ImageResource mvPayees();
 
 	public interface DashboardViewCss extends CssResource {
 	      String content();
 	      String logout();
 	      String clear();
 	      String center();
 	      String right();
 	      @ClassName("profile-nav-top")String profileNavBar();
 	      @ClassName("main-view") String mainView();
 	      @ClassName("profile-main-view") String profileMainView();
 	      @ClassName("accounts-main-view") String accountsMainView();
 	      @ClassName("categories-main-view") String categoriesMainView();
 	      @ClassName("payess-main-view") String payeesMainView();
 	      
 	      String transactionsView();
 	      String scroll();
 	      String disclosure();
 	   }
 
 	@Source("DashboardView.css")
 	DashboardViewCss style();
 }
