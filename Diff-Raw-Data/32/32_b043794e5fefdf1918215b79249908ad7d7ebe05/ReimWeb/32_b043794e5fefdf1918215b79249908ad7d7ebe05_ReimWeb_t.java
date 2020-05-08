 package com.reimDev.reimWeb.client;
 
 import com.reimDev.reimWeb.client.service.util.ServiceHandler;
 import com.reimDev.reimWeb.client.ui.pages.BlogPage;
 import com.reimDev.reimWeb.client.ui.pages.admin.AdminBlogPage;
 import com.reimDev.reimWeb.client.ui.pages.util.PageBuilder;
 import com.reimDev.reimWeb.client.ui.pages.util.PageContainer;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class ReimWeb implements EntryPoint {
 
 	public void onModuleLoad() {
 		
 		ServiceHandler.getInstance().getInitService().initiate(new AsyncCallback<Void>() {
 			public void onSuccess(Void result) {				
 				//TODO keep in mind that carrying all the pages around might slow pagination down
 				
 				/*mount pages */
 				
 				//user
 				PageContainer.getInstance().mount("blog", new BlogPage());		
 				
 				//admin
 				PageContainer.getInstance().mount("admin/blog", new AdminBlogPage());
						
 				
 				//set Content
 				String pageName = Window.Location.getParameter("page");		
 				PageBuilder.setPage(PageContainer.getInstance().getPage(pageName));
 			}
 			public void onFailure(Throwable caught) {		
 				PageBuilder.setPage(PageContainer.getInstance().error500Page(caught.getMessage()));
 			}
 		});
 	}
 }
