 package cz.admin24.myachievo.web2.base;
 
 import java.util.Iterator;
 
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.vaadin.server.Page;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.Image;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.MenuBar;
 import com.vaadin.ui.MenuBar.Command;
 import com.vaadin.ui.MenuBar.MenuItem;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 import cz.admin24.myachievo.web2.SpringUtils;
 import cz.admin24.myachievo.web2.android.AndroidUrl;
 import cz.admin24.myachievo.web2.calendar.CalendarUrl;
 import cz.admin24.myachievo.web2.dashboard.DashboardUrl;
 import cz.admin24.myachievo.web2.reports.ReportsUrl;
 import cz.admin24.myachievo.web2.service.AchievoConnectorWrapper;
 
 public class SideBar extends VerticalLayout {
 
     private final CssLayout               menu             = new CssLayout();
     private final AchievoConnectorWrapper achievoConnector = SpringUtils.getBean(AchievoConnectorWrapper.class);
 
 
     public SideBar() {
         addStyleName("sidebar");
         setWidth(null);
         setHeight("100%");
 
         // Branding element
         addComponent(new CssLayout() {
             {
                 addStyleName("branding");
                 Label logo = new Label(
                         "<span>MyAchievo</span> Dashboard",
                         ContentMode.HTML);
                 logo.setSizeUndefined();
                 addComponent(logo);
                 // addComponent(new Image(null, new
                 // ThemeResource(
                 // "img/branding.png")));
             }
         });
 
         // Main menu
         addComponent(menu);
         setExpandRatio(menu, 1);
 
         // User menu
         addComponent(new VerticalLayout() {
             {
                 setSizeUndefined();
                 addStyleName("user");
                 Image profilePic = new Image(
                         null,
                         new ThemeResource("img/profile-pic.png"));
                 profilePic.setWidth("34px");
                 addComponent(profilePic);
                 Label userName = new Label(achievoConnector.getFullUserName());
                 userName.setSizeUndefined();
                 addComponent(userName);
 
                 Command cmd = new Command() {
                     @Override
                     public void menuSelected(
                             MenuItem selectedItem) {
                         Notification
                                 .show("Not implemented in this demo");
                     }
                 };
                 MenuBar settings = new MenuBar();
                 MenuItem settingsMenu = settings.addItem("",
                         null);
                 settingsMenu.setStyleName("icon-cog");
                 settingsMenu.addItem("Settings", cmd);
                 settingsMenu.addItem("Preferences", cmd);
                 settingsMenu.addSeparator();
                 settingsMenu.addItem("My Account", cmd);
                 addComponent(settings);
 
                 Button exit = new NativeButton("Exit");
                 exit.addStyleName("icon-cancel");
                 exit.setDescription("Sign Out");
                 addComponent(exit);
                 exit.addClickListener(new ClickListener() {
                     @Override
                     public void buttonClick(ClickEvent event) {
                         getSession().close();
                         // UI.getCurrent().getU
                         Page.getCurrent().reload();
                         SecurityContextHolder.getContext().setAuthentication(null);
                     }
                 });
             }
         });
         buildMenu();
     }
 
 
     private void buildMenu() {
 
         Button b = new NativeButton("Dashboard");
         b.addStyleName("icon-dashboard");
         b.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 clearMenuSelection();
                 event.getButton().addStyleName("selected");
                 navigateTo(new DashboardUrl().toFragment());
             }
         });
         // event.getButton().addStyleName("selected");
 //        menu.addComponent(b);
 
         b = new NativeButton("Calendar");
         b.addStyleName("icon-schedule");
         b.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 clearMenuSelection();
                 event.getButton().addStyleName("selected");
                 navigateTo(new CalendarUrl().toFragment());
             }
         });
         b.addStyleName("selected");
         menu.addComponent(b);
 
         b = new NativeButton("Reports");
         b.addStyleName("icon-reports");
         b.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 clearMenuSelection();
                 event.getButton().addStyleName("selected");
                 navigateTo(new ReportsUrl().toFragment());
             }
         });
         menu.addComponent(b);
 
         b = new NativeButton("Android");
         b.addStyleName("icon-transactions");
         b.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 clearMenuSelection();
                 event.getButton().addStyleName("selected");
                 navigateTo(new AndroidUrl().toFragment());
             }
         });
         menu.addComponent(b);
 
         menu.addStyleName("menu");
         menu.setHeight("100%");
     }
 
 
     protected void navigateTo(String navigationState) {
         UI.getCurrent().getNavigator().navigateTo(navigationState);
     }
 
 
     private void clearMenuSelection() {
         for (Iterator<Component> it = menu.iterator(); it.hasNext();) {
             Component next = it.next();
 
             next.removeStyleName("selected");
 
         }
     }
 }
