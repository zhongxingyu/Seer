 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 package org.generationcp.ibpworkbench.actions;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.generationcp.ibpworkbench.comp.window.IContentWindow;
 import org.generationcp.ibpworkbench.datasource.helper.DatasourceConfig;
 import org.generationcp.ibpworkbench.navigation.NavManager;
 import org.generationcp.ibpworkbench.navigation.UriUtils;
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
 import com.vaadin.ui.Component.Event;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.Notification;
 
 @Configurable
 public class LaunchWorkbenchToolAction implements ClickListener, ActionListener {
     private static final long serialVersionUID = 1L;
     
     private final static Logger log = LoggerFactory.getLogger(LaunchWorkbenchToolAction.class);
     
     public static enum ToolEnum {
          GERMPLASM_BROWSER("germplasm_browser")
         ,GERMPLASM_PHENOTYPIC("germplasm_phenotypic")
         ,GDMS("gdms")
         ,FIELDBOOK("fieldbook")
         ,OPTIMAS("optimas")
         ,BREEDING_MANAGER("breeding_manager")
         ,BREEDING_VIEW("breeding_view")
         ;
         
         String toolName;
         
         ToolEnum(String toolName) {
             this.toolName = toolName;
         }
         
         public String getToolName() {
             return toolName;
         }
         
         public static boolean isCorrectTool(String toolName) {
             if(ToolEnum.GERMPLASM_BROWSER.toString().equals(toolName) ||
                ToolEnum.GERMPLASM_PHENOTYPIC.toString().equals(toolName) ||
                ToolEnum.GDMS.toString().equals(toolName) ||
                ToolEnum.FIELDBOOK.toString().equals(toolName) ||
                ToolEnum.OPTIMAS.toString().equals(toolName) ||
                ToolEnum.BREEDING_MANAGER.toString().equals(toolName) ||
                ToolEnum.BREEDING_VIEW.toString().equals(toolName)) {
                 return true;
             } else {
                 return false;
             }
         }
     }
 
     private ToolEnum toolEnum;
 
     private DatasourceConfig dataSourceConfig;
     
     public LaunchWorkbenchToolAction() {
     }
     
     public LaunchWorkbenchToolAction(ToolEnum toolEnum) {
         this.toolEnum = toolEnum;
     }
     
     @Autowired(required = true)
     public void setDataSourceConfig(DatasourceConfig dataSourceConfig) {
         this.dataSourceConfig = dataSourceConfig;
     }
 
     @Override
     public void buttonClick(ClickEvent event) {
         Window window = event.getComponent().getWindow();
         
         launchTool(toolEnum.getToolName(), window, event);
     }
 
     @Override
     public void doAction(Event event) {
         NavManager.breadCrumbClick(this, event);
     }
 
     @Override
     public void doAction(Window window, String uriFragment) {
 
         Map<String, List<String>> map = UriUtils.getUriParameters(uriFragment);
         String toolName = map.get("toolName").get(0);
         
         if(ToolEnum.isCorrectTool(toolName)) {
             launchTool(toolName, window, null);
         } else {
 //            System.out.println("wrong tool id");
         }
     }
     
     private void launchTool(String toolName, Window window, Event event) {
         WorkbenchDataManager workbenchDataManager = dataSourceConfig.getManagerFactory().getWorkbenchDataManager();
         Tool tool = workbenchDataManager.getToolWithName(toolEnum.getToolName());
         if (tool == null) {
             log.warn("Cannot find tool " + toolEnum);
             
             window.showNotification("Launch Error", "Cannot launch tool.", Notification.TYPE_ERROR_MESSAGE);
             
             return;
         } else {
             if (tool.getToolType() == ToolType.NATIVE) {
                 File absoluteToolFile = new File(tool.getPath()).getAbsoluteFile();
                 Runtime runtime = Runtime.getRuntime();
                 try {
                     runtime.exec(absoluteToolFile.getAbsolutePath());
                 }
                 catch (IOException e) {
                     log.error("Cannot launch " + absoluteToolFile.getAbsolutePath(), e);
                     
                     window.showNotification("Launch Error", "Cannot launch tool at " + absoluteToolFile.getAbsolutePath(), Notification.TYPE_ERROR_MESSAGE);
                 }
             } else {
                 Embedded browser = new Embedded("", new ExternalResource(tool.getPath()));
                 browser.setType(Embedded.TYPE_BROWSER);
                 browser.setSizeFull();
                 browser.setHeight("800px");
                 browser.setWidth("100%");
                 
                 if(event != null) {
                     NavManager.navigateApp(window, "/home/openProject/openProjectWorkflow/" + tool.getToolName());
                 }
                 
                IContentWindow contentWindow = (IContentWindow) window;
                 contentWindow.showContent(browser);
             }
         }
     }
 
 }
