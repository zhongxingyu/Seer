 package com.java.gwt.libertycinema.client;
 
 
 import java.lang.Throwable;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import com.java.gwt.libertycinema.client.BodyPanel;
 import com.java.gwt.libertycinema.client.services.LoginService;
 import com.java.gwt.libertycinema.client.services.LoginServiceAsync;
 import com.java.gwt.libertycinema.client.views.HomeLinkCommand;
 import com.java.gwt.libertycinema.client.views.ImageUpload;
 import com.java.gwt.libertycinema.shared.LoginInfo;
 
 
 public class TopNavBar {
 
     private BodyPanel body;
 
     private VerticalPanel headerPanel = new VerticalPanel();
     private final MenuBar menu = new MenuBar();
 
     public TopNavBar(BodyPanel body) {
         this.body = body;
     }
 
     private MenuBar setUpTopNavButtons() {
     	menu.addItem("Home", new HomeLinkCommand(body));
     	menu.addItem("Gallery", new HomeLinkCommand(body));
     	menu.addItem("Testimonials", new HomeLinkCommand(body));
     	menu.addItem("Permissions", new HomeLinkCommand(body));
     	menu.addItem("Technical Details", new HomeLinkCommand(body));
     	menu.addItem("Contact Us", new HomeLinkCommand(body));
     
     	LoginServiceAsync loginService = GWT.create(LoginService.class);
     	loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo> () {
     		public void onFailure(Throwable e) {
     		    Window.alert("Ajax request failed, couldn't load admin module");
     		}
     		public void onSuccess(LoginInfo loginInfo) {
     		    if(loginInfo.isAdminUser()) {
    			menu.addItem("Admin", getAdminMenu());
     		    }
     		}
     	    });
     	return menu;
     }
 
     private HorizontalPanel setUpTopNavLogo() {
         HorizontalPanel logoPanel = new HorizontalPanel();
     	logoPanel.add(new HTML("<h2>Liberty Cinema</h2>"));
     	return logoPanel;
     }
 
     public VerticalPanel getTopBarPanel() {
     	headerPanel.add(setUpTopNavLogo());
     	headerPanel.add(setUpTopNavButtons());
     	return headerPanel;
     }
 
     public MenuBar getAdminMenu() {
     	MenuBar adminMenu = new MenuBar(true);
     	adminMenu.addItem("Static Pages", new HomeLinkCommand(body));
     	adminMenu.addItem("Images", new ImageUpload(body));
     	return adminMenu;
     }
 }
