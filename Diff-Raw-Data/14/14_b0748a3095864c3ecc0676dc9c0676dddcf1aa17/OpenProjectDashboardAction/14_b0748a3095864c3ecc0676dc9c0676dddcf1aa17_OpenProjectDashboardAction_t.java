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
 
 import java.util.List;
 import java.util.Map;
 
 import org.generationcp.ibpworkbench.comp.ProjectDashboard;
 import org.generationcp.ibpworkbench.comp.ProjectThumbnailPanel;
 import org.generationcp.ibpworkbench.comp.window.IContentWindow;
 import org.generationcp.ibpworkbench.datasource.helper.DatasourceConfig;
 import org.generationcp.ibpworkbench.navigation.NavManager;
 import org.generationcp.ibpworkbench.navigation.UriUtils;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.event.ItemClickEvent.ItemClickListener;
 import com.vaadin.event.MouseEvents;
 import com.vaadin.event.MouseEvents.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Component.Event;
 import com.vaadin.ui.Window;
 
 @Configurable
 public class OpenProjectDashboardAction implements ItemClickListener, MouseEvents.ClickListener, ActionListener {
     private static final long serialVersionUID = 1L;
 
     @Autowired(required = true)
     private DatasourceConfig dataSourceConfig;
     
     @Override
     public void itemClick(ItemClickEvent event) {
         Component component = event.getComponent();
         IContentWindow window = (IContentWindow) component.getWindow();
         
         @SuppressWarnings("unchecked")
         BeanItem<Project> item = (BeanItem<Project>) event.getItem();
         
         Project project = item.getBean();
         if (project == null) return;
         
         ProjectDashboard projectDashboard = new ProjectDashboard(project);
         projectDashboard.addProjectThumbnailPanelListener(new OpenProjectWorkflowAction());
         
         String viewId = "/home/openProject?projectId=" + project.getProjectId(); 
         NavManager.navigateApp(component.getWindow(), viewId, project.getProjectName());
         
         
         window.showContent(projectDashboard);
     }
     
     @Override
     public void click(ClickEvent event) {
         Component component = event.getComponent();
         IContentWindow window = (IContentWindow) component.getWindow();
        
         Project project = (Project) ((ProjectThumbnailPanel) component).getData();
         if (project == null) {
             return;
         }
 
         ProjectDashboard projectDashboard = new ProjectDashboard(project);
         projectDashboard.addProjectThumbnailPanelListener(new OpenProjectWorkflowAction());
 
         window.showContent(projectDashboard);
         
        String viewId = "/home/openProject?projectId=" + project.getProjectId();
        NavManager.navigateApp((Window) window, viewId, project.getProjectName());
     }
 
     @Override
     public void doAction(Event event) {
         NavManager.breadCrumbClick(this, event);
     }
     
     @Override
     public void doAction(Window window, String uriFragment) {
         
         //get parameters from uri fragment
         
         //retrieve project record using id from uri fragment
         //instantiate ProjectDashboard
         
         //call window.showContent
         IContentWindow w = (IContentWindow) window;
         Map<String, List<String>> params = UriUtils.getUriParameters(uriFragment);
                 
         WorkbenchDataManager manager = dataSourceConfig.getManagerFactory().getWorkbenchDataManager();
         List<Project> projects = manager.getProjects();
         
         Project p = null;
         Long projectId = Long.parseLong(params.get("projectId").get(0));
 
         for(Project proj : projects) {
             if(proj.getProjectId().equals(projectId)) {
                 p = proj;
             }
         }
         
         ProjectDashboard projectDashboard = new ProjectDashboard(p);
         projectDashboard.addProjectThumbnailPanelListener(new OpenProjectWorkflowAction());
         
         w.showContent(projectDashboard);
         
         NavManager.navigateApp(window, uriFragment, p.getProjectName());
     }
 }
