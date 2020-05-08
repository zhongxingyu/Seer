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
 
 package org.generationcp.ibpworkbench.comp;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.ibpworkbench.IBPWorkbenchApplication;
 import org.generationcp.ibpworkbench.Message;
 import org.generationcp.ibpworkbench.actions.ShowProjectDetailAction;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.api.WorkbenchDataManager;
 import org.generationcp.middleware.pojos.workbench.Project;
 import org.generationcp.middleware.pojos.workbench.ProjectActivity;
 import org.generationcp.middleware.pojos.workbench.Role;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.util.BeanContainer;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Table.CellStyleGenerator;
 import com.vaadin.ui.VerticalLayout;
 
 @Configurable
 public class WorkbenchDashboard extends VerticalLayout implements InitializingBean, InternationalizableComponent {
 
     private static final Logger LOG = LoggerFactory.getLogger(WorkbenchDashboard.class);
     private static final long serialVersionUID = 1L;
 
     private Label lblDashboardTitle;
     
     private Table tblProject;
     
     private Label lblProjectDetailTitle;
     
     private Table tblActivity;
     
     private Table tblRoles;
 
     @Autowired
     private WorkbenchDataManager workbenchDataManager;
     
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     private Project lastOpenedProject;
 
     public WorkbenchDashboard() {
         super();
     }
 
     @Override
     public void afterPropertiesSet() {
         assemble();
     }
 
     protected void initializeComponents() {
         lblDashboardTitle = new Label();
         lblDashboardTitle.setStyleName("gcp-content-title");
         
         lblProjectDetailTitle = new Label();
         lblProjectDetailTitle.setStyleName("gcp-content-title");
 
         initializeProjectTable();
         initializeActivityTable();
         initializeRolesTable();
     }
     
     private void initializeProjectTable() {
         // project table components
         tblProject = new Table() {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             protected String formatPropertyValue(Object rowId, Object colId, Property property) {
                 if (property.getType() == Date.class) {
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                     return property.getValue() == null ? "" : sdf.format((Date) property.getValue());
                 }
 
                 return super.formatPropertyValue(rowId, colId, property);
             }
         };
         tblProject.setImmediate(true); // react at once when something is selected
 
         BeanContainer<String, Project> projectContainer = new BeanContainer<String, Project>(Project.class);
        projectContainer.setBeanIdProperty("projectId");
         tblProject.setContainerDataSource(projectContainer);
 
         tblProject.setColumnCollapsingAllowed(true);
         tblProject.setCellStyleGenerator(new CellStyleGenerator() {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             public String getStyle(Object itemId, Object propertyId) {
                 Item item = tblProject.getItem(itemId);
                 //TODO: remove checking when projects retrieved are only for the user
                 if(lastOpenedProject != null) {
                     if(lastOpenedProject.getProjectId() == 
                        item.getItemProperty("projectId").getValue()) {
                         return "gcp-highlight";
                     }
                 }
                 return "project-table";
             }
         });
     }
     
     private void initializeActivityTable() {
         tblActivity = new Table();
         tblActivity.setImmediate(true);
         
         BeanContainer<Integer, ProjectActivity> container = new BeanContainer<Integer, ProjectActivity>(ProjectActivity.class);
         container.setBeanIdProperty("projectActivityId");
         tblActivity.setContainerDataSource(container);
         
         String[] columns = new String[] {"date", "name", "description"};
         tblActivity.setVisibleColumns(columns);
     }
     
     private void initializeRolesTable() {
         tblRoles = new Table();
         tblRoles.setImmediate(true);
         
         BeanContainer<Integer, Role> container = new BeanContainer<Integer, Role>(Role.class);
         container.setBeanIdProperty("roleId");
         tblRoles.setContainerDataSource(container);
         
         String[] columns = new String[] {"name"};
         tblRoles.setVisibleColumns(columns);
     }
 
     protected void initializeLayout() {
         setWidth("100%");
         setMargin(true);
         setSpacing(true);
 
         lblDashboardTitle.setSizeUndefined();
         addComponent(lblDashboardTitle);
 
         Component projectTableArea = layoutProjectTableArea();
         addComponent(projectTableArea);
         setExpandRatio(projectTableArea, 1.0f);
         
         Component projectDetailArea = layoutProjectDetailArea();
         addComponent(projectDetailArea);
     }
 
     protected void initializeData() {
         //TODO: Verify the try-catch flow
         // Get the list of Projects
         List<Project> projects = null;
         lastOpenedProject = null;
         
         IBPWorkbenchApplication app = IBPWorkbenchApplication.get();
         
         try {
             projects = workbenchDataManager.getProjects();
             lastOpenedProject = workbenchDataManager.getLastOpenedProject(
             		app.getSessionData().getUserData().getUserid());
         } catch (MiddlewareQueryException e) {
             LOG.error("Exception", e);
             throw new InternationalizableException(e, 
                     Message.DATABASE_ERROR, Message.CONTACT_ADMIN_ERROR_DESC);
         }
 
         app.getSessionData().setLastOpenedProject(lastOpenedProject);
 
         // set the Project Table data source
         BeanContainer<String, Project> projectContainer = new BeanContainer<String, Project>(Project.class);
         projectContainer.setBeanIdProperty("projectName");
         for (Project project : projects) {
             projectContainer.addBean(project);
         }
         tblProject.setContainerDataSource(projectContainer);
 
         // set the visible columns on the Project Table
         String[] columns = new String[] { "startDate", "projectName" };
         tblProject.setVisibleColumns(columns);
     }
 
     protected void initializeActions() {
         tblProject.addListener(new ShowProjectDetailAction(tblActivity, tblRoles));
     }
 
     protected void assemble() {
         initializeComponents();
         initializeLayout();
         initializeData();
         initializeActions();
     }
 
     private Component layoutProjectTableArea() {
         tblProject.setWidth("100%");
         tblProject.setHeight("100%");
         return tblProject;
     }
     
     private Component layoutProjectDetailArea() {
         // layout the tables
         HorizontalLayout horizontalLayout = new HorizontalLayout();
         horizontalLayout.setWidth("100%");
         horizontalLayout.setMargin(false);
         horizontalLayout.setSpacing(true);
         
         tblActivity.setWidth("100%");
         horizontalLayout.addComponent(tblActivity);
         horizontalLayout.setExpandRatio(tblActivity, 1.0f);
         
         tblRoles.setWidth("300px");
         horizontalLayout.addComponent(tblRoles);
         
         // layout the project detail area
         VerticalLayout verticalLayout = new VerticalLayout();
         verticalLayout.setWidth("100%");
         verticalLayout.setMargin(false);
         verticalLayout.setSpacing(true);
         
         verticalLayout.addComponent(lblProjectDetailTitle);
         verticalLayout.addComponent(horizontalLayout);
         
         return verticalLayout;
     }
     
     @Override
     public void attach() {
         super.attach();
         
         updateLabels();
     }
 
     @Override
     public void updateLabels() {
         messageSource.setValue(lblDashboardTitle, Message.dashboard);
         messageSource.setCaption(tblProject, Message.project_table_caption);
         
         messageSource.setValue(lblProjectDetailTitle, Message.PROJECT_DETAIL);
         
         messageSource.setColumnHeader(tblProject, "startDate", Message.START_DATE);
         messageSource.setColumnHeader(tblProject, "projectName", Message.project);
         messageSource.setColumnHeader(tblProject, "action", Message.action);
         messageSource.setColumnHeader(tblProject, "status", Message.status);
         messageSource.setColumnHeader(tblProject, "owner", Message.owner);
         
         messageSource.setCaption(tblActivity, Message.ACTIVITIES);
         
         messageSource.setCaption(tblRoles, Message.ROLE_TABLE_TITLE);
         
         tblProject.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public String generateDescription(Component source, Object itemId, Object propertyId) {
                 return messageSource.getMessage(Message.PROJECT_TABLE_TOOLTIP);
             }
         });
         
         tblRoles.setItemDescriptionGenerator(new ItemDescriptionGenerator() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public String generateDescription(Component source, Object itemId, Object propertyId) {
                 Table table = (Table) source;
                 Container container = table.getContainerDataSource();
                 
                 @SuppressWarnings("unchecked")
                 BeanItem<Role> item = (BeanItem<Role>) container.getItem(itemId);
                 Role role = item.getBean();
                 
                 return role == null ? "" : messageSource.getMessage(Message.ROLE_TABLE_TOOLTIP, role.getName());
             }
         });
     }
 }
