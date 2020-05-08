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
 
 package org.generationcp.ibpworkbench.comp.window;
 
 import org.generationcp.ibpworkbench.actions.CreateContactAction;
 import org.generationcp.ibpworkbench.actions.HomeAction;
 import org.generationcp.ibpworkbench.actions.OpenNewProjectAction;
 import org.generationcp.ibpworkbench.actions.OpenProjectDashboardAction;
 import org.generationcp.ibpworkbench.actions.SignoutAction;
 import org.generationcp.ibpworkbench.comp.WorkbenchDashboard;
 import org.generationcp.ibpworkbench.navigation.CrumbTrail;
 import org.generationcp.ibpworkbench.navigation.NavUriFragmentChangedListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UriFragmentUtility;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.VerticalSplitPanel;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.themes.BaseTheme;
 
 @Configurable
 public class WorkbenchDashboardWindow extends Window implements IContentWindow, InitializingBean{
 
     private static final long serialVersionUID = 1L;
     private Logger log = LoggerFactory.getLogger(WorkbenchDashboardWindow.class);
 
     private Label workbenchTitle;
     private Button homeButton;
     private Button signOutButton;
     private Button accountButton;
     private Button helpButton;
 
     private Label actionsTitle;
     private Button createProjectButton;
     private Button createContactButton;
     private Label recentTitle;
     private Label usersGuideTitle;
     private Label hint1;
 
     private VerticalSplitPanel verticalSplitPanel;
 
     private HorizontalSplitPanel contentAreaSplitPanel;
 
     private VerticalLayout mainContent;
 
     private WorkbenchDashboard workbenchDashboard;
 
     private CrumbTrail crumbTrail;
 
     private UriFragmentUtility uriFragUtil;
     private NavUriFragmentChangedListener uriChangeListener;
 
     public WorkbenchDashboardWindow() {
         log.debug("{} instead created.", WorkbenchDashboardWindow.class);
     }
 
     /**
      * Assemble the UI after all dependencies has been set.
      */
     @Override
     public void afterPropertiesSet() throws Exception {
         assemble();
     }
 
     protected void initializeComponents() {
         // workbench header components
         workbenchTitle = new Label("Workbench");
         workbenchTitle.setStyleName("gcp-window-title");
 
         homeButton = new Button("Home");
         homeButton.setStyleName(BaseTheme.BUTTON_LINK);
         homeButton.setSizeUndefined();
 
         signOutButton = new Button("Signout");
         signOutButton.setStyleName(BaseTheme.BUTTON_LINK);
         signOutButton.setSizeUndefined();
 
         accountButton = new Button("Account");
         accountButton.setStyleName(BaseTheme.BUTTON_LINK);
         accountButton.setSizeUndefined();
 
         helpButton = new Button("Help");
         helpButton.setStyleName(BaseTheme.BUTTON_LINK);
         helpButton.setSizeUndefined();
 
         // left area components
         actionsTitle = new Label("Actions");
         actionsTitle.setStyleName("gcp-section-title");
         actionsTitle.setSizeUndefined();
 
         createProjectButton = new Button("Create Project");
         createProjectButton.setWidth("120px");
 
         createContactButton = new Button("Create Contact");
         createContactButton.setWidth("120px");
 
         recentTitle = new Label("Recent");
         recentTitle.setStyleName("gcp-section-title");
         recentTitle.setSizeUndefined();
 
         usersGuideTitle = new Label("User's Guide");
         usersGuideTitle.setStyleName("gcp-section-title");
         usersGuideTitle.setSizeUndefined();
 
         hint1 = new Label("Click on the workflow " +
                 "\nthumbnail to " +
                 "\nview more details about a " +
                 "\nproject and to go to main " +
                 "\nworkflow diagram.");
         hint1.setContentMode(Label.CONTENT_PREFORMATTED);
         hint1.setSizeUndefined();
 
         workbenchDashboard = new WorkbenchDashboard();
 
         verticalSplitPanel = new VerticalSplitPanel();
         contentAreaSplitPanel = new HorizontalSplitPanel();
 
         mainContent = new VerticalLayout();
         crumbTrail = new CrumbTrail();
         crumbTrail.setMargin(true);
         crumbTrail.setSpacing(true);
 
         uriFragUtil = new UriFragmentUtility();
         uriChangeListener = new NavUriFragmentChangedListener();
 
         uriFragUtil.addListener(uriChangeListener);
     }
 
     protected void initializeLayout() {
         setSizeFull();
 
         VerticalLayout layout = new VerticalLayout();
         layout.setSizeFull();
 
         // add the vertical split panel
         verticalSplitPanel.setSplitPosition(50, Sizeable.UNITS_PIXELS);
         verticalSplitPanel.setLocked(true);
         verticalSplitPanel.setSizeFull();
 
         layout.addComponent(verticalSplitPanel);
 
         // add the workbench header
         Component workbenchHeader = layoutWorkbenchHeader();
         verticalSplitPanel.addComponent(workbenchHeader);
 
         // add the content area split panel
         contentAreaSplitPanel.setSplitPosition(200, Sizeable.UNITS_PIXELS);
         contentAreaSplitPanel.setLocked(false);
 
         // layout the left area of the content area split panel
         Component leftArea = layoutLeftArea();
         contentAreaSplitPanel.addComponent(leftArea);
 
         mainContent.addComponent(crumbTrail);
         mainContent.addComponent(workbenchDashboard);
 
         // layout the right area of the content area split panel
         // contentAreaSplitPanel.addComponent(workbenchDashboard);
         contentAreaSplitPanel.addComponent(mainContent);
 
         verticalSplitPanel.addComponent(contentAreaSplitPanel);
 
         setContent(layout);
     }
 
     protected void initializeActions() {
         homeButton.addListener(new HomeAction());
         signOutButton.addListener(new SignoutAction());
         createProjectButton.addListener(new OpenNewProjectAction());
         createContactButton.addListener(new CreateContactAction());
 
         workbenchDashboard.setProjectThumbnailClickHandler(new OpenProjectDashboardAction());
         workbenchDashboard.addProjectTableListener(new OpenProjectDashboardAction());
     }
 
     protected void assemble() {
         initializeComponents();
         initializeLayout();
         initializeActions();
     }
 
     private Component layoutWorkbenchHeader() {
         HorizontalLayout headerLayout = new HorizontalLayout();
         headerLayout.setWidth("100%");
         headerLayout.setHeight("100%");
         headerLayout.setMargin(false, true, false, true);
         headerLayout.setSpacing(false);
 
         // workbench title area
         headerLayout.addComponent(workbenchTitle);
         headerLayout.setComponentAlignment(workbenchTitle, Alignment.MIDDLE_CENTER);
 
         headerLayout.addComponent(uriFragUtil);
 
         // right side button area
         HorizontalLayout headerRightLayout = new HorizontalLayout();
         headerRightLayout.setSizeUndefined();
         headerRightLayout.setMargin(false);
         headerRightLayout.setSpacing(true);
 
         headerRightLayout.addComponent(homeButton);
         headerRightLayout.setComponentAlignment(homeButton, Alignment.TOP_LEFT);
 
         headerRightLayout.addComponent(new Label("|"));
 
         headerRightLayout.addComponent(signOutButton);
         headerRightLayout.setComponentAlignment(signOutButton, Alignment.TOP_LEFT);
 
         headerRightLayout.addComponent(new Label("|"));
 
         headerRightLayout.addComponent(accountButton);
         headerRightLayout.setComponentAlignment(accountButton, Alignment.TOP_LEFT);
 
         headerRightLayout.addComponent(new Label("|"));
 
         headerRightLayout.addComponent(helpButton);
         headerRightLayout.setComponentAlignment(helpButton, Alignment.TOP_LEFT);
 
         headerLayout.addComponent(headerRightLayout);
         headerLayout.setComponentAlignment(headerRightLayout, Alignment.MIDDLE_RIGHT);
 
         return headerLayout;
     }
 
     private Component layoutLeftArea() {
         VerticalLayout leftLayout = new VerticalLayout();
         leftLayout.setWidth("100%");
         leftLayout.setHeight(null);
         leftLayout.setSpacing(true);
 
         leftLayout.addComponent(actionsTitle);
         leftLayout.setComponentAlignment(actionsTitle, Alignment.TOP_CENTER);
 
         leftLayout.addComponent(createProjectButton);
         leftLayout.setComponentAlignment(createProjectButton, Alignment.TOP_CENTER);
 
         // TODO: These are commented out to remove non-working elements for June
         // milestone
         // leftLayout.addComponent(createContactButton);
         // leftLayout.setComponentAlignment(createContactButton,
         // Alignment.TOP_CENTER);
         //
         // leftLayout.addComponent(recentTitle);
         // leftLayout.setComponentAlignment(recentTitle, Alignment.TOP_CENTER);
 
         leftLayout.addComponent(usersGuideTitle);
         leftLayout.setComponentAlignment(usersGuideTitle, Alignment.TOP_CENTER);
         leftLayout.addComponent(hint1);
 
         return leftLayout;
     }
 
     /**
      * Show the specified {@link Component} on the right side area of the
      * Workbench's split panel.
      * 
      * @param content
      */
     public void showContent(Component content) {
 
         // contentAreaSplitPanel.removeComponent(contentAreaSplitPanel.getSecondComponent());
         // contentAreaSplitPanel.addComponent(content);
 
        mainContent.removeComponent(mainContent.getComponent(1));
         mainContent.addComponent(content);
     }
 
     public WorkbenchDashboard getWorkbenchDashboard() {
         return workbenchDashboard;
     }
 
     public void setWorkbenchDashboard(WorkbenchDashboard workbenchDashboard) {
         this.workbenchDashboard = workbenchDashboard;
     }
 
     public CrumbTrail getCrumbTrail() {
         return crumbTrail;
     }
 
     public void setCrumbTrail(CrumbTrail crumbTrail) {
         this.crumbTrail = crumbTrail;
     }
 
     public void setUriFragment(String fragment) {
         uriFragUtil.setFragment(fragment);
     }
 
 }
