 package org.pentaho.pac.client;
 
 import org.pentaho.pac.client.common.ui.dialog.MessageDialog;
 import org.pentaho.pac.client.home.HomePanel;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.utils.ExceptionParser;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PentahoAdminConsole extends DockPanel implements IRefreshableAdminConsole {
   
   public static final PacLocalizedMessages MSGS = (PacLocalizedMessages)GWT.create(PacLocalizedMessages.class);
   public static final String DEFAULT_HOMEPAGE_URL = "http://www.pentaho.com/console_home"; //$NON-NLS-1$  
   protected AdminConsoleToolbar toolbar = new AdminConsoleToolbar(this);
   protected AdminConsoleMasterDetailsPanel adminConsoleMasterDetailsPanel = new AdminConsoleMasterDetailsPanel();
   
   public enum AdminConsolePageId {
     HOME_PAGE, ADMIN_PAGE
   };
   
   public PentahoAdminConsole() {
     super();
     
     HorizontalPanel topPanel = buildTopPanel() ;
     topPanel.setWidth("100%");    //$NON-NLS-1$
     add(topPanel, DockPanel.NORTH);
     setCellWidth(topPanel, "100%"); //$NON-NLS-1$    
     
     add(adminConsoleMasterDetailsPanel, DockPanel.CENTER);
     adminConsoleMasterDetailsPanel.setWidth("100%");//$NON-NLS-1$
     adminConsoleMasterDetailsPanel.setHeight("100%");   //$NON-NLS-1$
     setCellWidth(adminConsoleMasterDetailsPanel, "100%"); //$NON-NLS-1$
     setCellHeight(adminConsoleMasterDetailsPanel, "100%"); //$NON-NLS-1$    
     
     AsyncCallback<String> homepageUrlcallback = new AsyncCallback<String>() {
       public void onSuccess(String result) {
         adminConsoleMasterDetailsPanel.addPage(AdminConsolePageId.HOME_PAGE.ordinal(), PentahoAdminConsole.MSGS.home(), new HomePanel(result));
         adminConsoleMasterDetailsPanel.addPage(AdminConsolePageId.ADMIN_PAGE.ordinal(), PentahoAdminConsole.MSGS.administration(), new AdministrationTabPanel());   
         adminConsoleMasterDetailsPanel.selectPage(AdminConsolePageId.HOME_PAGE.ordinal());
       }
       public void onFailure(Throwable caught) {
         adminConsoleMasterDetailsPanel.addPage(AdminConsolePageId.HOME_PAGE.ordinal(), PentahoAdminConsole.MSGS.home(), new HomePanel(DEFAULT_HOMEPAGE_URL));
         adminConsoleMasterDetailsPanel.addPage(AdminConsolePageId.ADMIN_PAGE.ordinal(), PentahoAdminConsole.MSGS.administration(), new AdministrationTabPanel());   
         adminConsoleMasterDetailsPanel.selectPage(AdminConsolePageId.HOME_PAGE.ordinal());
       }
     };
     PacServiceFactory.getPacService().getHomepageUrl(homepageUrlcallback);  
     setStyleName("main-panel"); //$NON-NLS-1$
    refresh();
   }
   
   protected HorizontalPanel buildTopPanel() {
     HorizontalPanel topPanel = new HorizontalPanel();
     SimplePanel logo = new SimplePanel();
     logo.setStyleName("logo"); //$NON-NLS-1$
     topPanel.setWidth("100%");  //$NON-NLS-1$
     topPanel.add(logo);
     topPanel.add(toolbar);
     topPanel.setCellWidth(toolbar, "100%"); //$NON-NLS-1$
     return topPanel;
   }
 
   static public PacLocalizedMessages getLocalizedMessages() {
     return MSGS;
   }
   
   public void refresh() {
     for (Widget page : adminConsoleMasterDetailsPanel.getPages()) {
       if (page instanceof IRefreshableAdminPage) {
         ((IRefreshableAdminPage)page).refresh();
       }
     }
   }
   
 }
  
