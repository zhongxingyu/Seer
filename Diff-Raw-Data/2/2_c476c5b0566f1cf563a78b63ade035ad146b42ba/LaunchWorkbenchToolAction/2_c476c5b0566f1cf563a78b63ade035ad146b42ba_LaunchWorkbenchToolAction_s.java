 package org.generationcp.ibpworkbench.actions;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.generationcp.ibpworkbench.comp.window.IContentWindow;
 import org.generationcp.ibpworkbench.datasource.helper.DatasourceConfig;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.workbench.Tool;
 import org.generationcp.middleware.pojos.workbench.ToolType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 
 @Configurable
 public class LaunchWorkbenchToolAction implements ClickListener {
     private static final long serialVersionUID = 1L;
     
     private final static Logger log = LoggerFactory.getLogger(LaunchWorkbenchToolAction.class);
     
     public static enum ToolId {
          GERMPLASM_BROWSER("germplasm_browser")
         ,GERMPLASM_PHENOTYPIC("germplasm_phenotypic")
         ,GDMS("gdms")
         ,FIELDBOOK("fieldbook")
         ,OPTIMAS("optimas")
         ;
         
         String toolId;
         
         ToolId(String toolId) {
             this.toolId = toolId;
         }
         
         public String getToolId() {
             return toolId;
         }
     }
 
     private ToolId toolId;
 
     private DatasourceConfig dataSourceConfig;
     
     public LaunchWorkbenchToolAction(ToolId toolId) {
         this.toolId = toolId;
     }
     
     @Autowired(required = true)
     public void setDataSourceConfig(DatasourceConfig dataSourceConfig) {
         this.dataSourceConfig = dataSourceConfig;
     }
 
     @Override
     public void buttonClick(ClickEvent event) {
         Window window = event.getComponent().getWindow();
         
         WorkbenchDataManager workbenchDataManager = dataSourceConfig.getManagerFactory().getWorkbenchDataManager();
         Tool tool = workbenchDataManager.getToolWithName(toolId.getToolId());
         if (tool == null) {
             log.warn("Cannot find tool " + toolId);
             
             window.showNotification("Launch Error", "Cannot launch tool.", Notification.TYPE_ERROR_MESSAGE);
             
             return;
         }
         
         if (tool.getToolType() == ToolType.NATIVE) {
            File absoluteToolFile = new File(tool.getPath());
             Runtime runtime = Runtime.getRuntime();
             try {
                 runtime.exec(absoluteToolFile.getAbsolutePath());
             }
             catch (IOException e) {
                 log.error("Cannot launch " + absoluteToolFile.getAbsolutePath(), e);
                 
                 window.showNotification("Launch Error", "Cannot launch tool at " + absoluteToolFile.getAbsolutePath(), Notification.TYPE_ERROR_MESSAGE);
             }
         }
         else {
             Embedded browser = new Embedded("", new ExternalResource(tool.getPath()));
             browser.setType(Embedded.TYPE_BROWSER);
             browser.setSizeFull();
             
             IContentWindow contentWindow = (IContentWindow) event.getComponent().getWindow();
             contentWindow.showContent(browser);
         }
     }
 
 }
