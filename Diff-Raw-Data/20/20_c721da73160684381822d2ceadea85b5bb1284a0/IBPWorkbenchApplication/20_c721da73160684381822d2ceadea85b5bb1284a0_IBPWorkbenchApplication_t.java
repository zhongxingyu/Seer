 package org.generationcp.ibpworkbench;
 
 import org.generationcp.ibpworkbench.actions.LoginAction;
 import org.generationcp.ibpworkbench.comp.window.LoginWindow;
 import org.generationcp.ibpworkbench.comp.window.WorkbenchDashboardWindow;
 import org.generationcp.ibpworkbench.datasource.helper.DatasourceConfig;
import org.generationcp.middleware.exceptions.ConfigException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 
 import com.vaadin.Application;
 
 public class IBPWorkbenchApplication extends Application {
     private static final long serialVersionUID = 1L;
     
     private LoginWindow loginWindow;
 
     private WorkbenchDashboardWindow dashboardWindow;
     
     private ManagerFactory managerFactory;
     
     public LoginWindow getLoginWindow() {
         return loginWindow;
     }
     
     public WorkbenchDashboardWindow getDashboardWindow() {
         return dashboardWindow;
     }
     
     public ManagerFactory getManagerFactory() {
         return managerFactory;
     }
     
     public WorkbenchDataManager getWorkbenchDataManager() {
    	try{
    		return managerFactory.getWorkbenchDataManager();
    	}catch(ConfigException ex){
    		return null;
    	}
        
     }
     
     @Override
     public void init() {
         assemble();
     }
     
     protected void initialize() {
         setTheme("gcp-default");
         
         // initialize data source
         managerFactory = new DatasourceConfig().getManagerFactory();
     }
     
     protected void initializeComponents() {
         loginWindow = new LoginWindow();
         
         dashboardWindow = new WorkbenchDashboardWindow();
     }
     
     protected void initializeLayout() {
         
     }
     
     protected void initializeActions() {
         new LoginAction(loginWindow.getLoginForm());
     }
     
     protected void assemble() {
         initialize();
         initializeComponents();
         initializeLayout();
         initializeActions();
         
         setMainWindow(loginWindow);
     }
     
     @Override
     public void close() {
         super.close();
         
         // TODO: implement this when we need to do something on session timeout
     }
     
     @Override
     public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
         event.getThrowable().printStackTrace();
     }
 }
