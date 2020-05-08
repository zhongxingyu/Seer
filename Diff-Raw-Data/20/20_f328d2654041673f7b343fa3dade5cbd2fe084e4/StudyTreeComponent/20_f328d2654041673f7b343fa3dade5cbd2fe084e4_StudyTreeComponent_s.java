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
 
 package org.generationcp.browser.study;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.browser.application.Message;
 import org.generationcp.browser.study.listeners.StudyButtonClickListener;
 import org.generationcp.browser.study.listeners.StudyItemClickListener;
 import org.generationcp.browser.study.listeners.StudyTreeExpandListener;
 import org.generationcp.browser.util.Util;
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.api.StudyDataManager;
 import org.generationcp.middleware.manager.api.TraitDataManager;
 import org.generationcp.middleware.pojos.Study;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.ui.AbstractSelect;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.Tree;
 import com.vaadin.ui.VerticalLayout;
 
 @Configurable
 public class StudyTreeComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {
 
     private static final long serialVersionUID = -3481988646509402160L;
 
     private final static Logger LOG = LoggerFactory.getLogger(StudyTreeComponent.class);
     
     public final static String REFRESH_BUTTON_ID = "StudyTreeComponent Refresh Button";
 
     @Autowired
     private StudyDataManager studyDataManager;
     
     private Tree studyTree;
     private static TabSheet tabSheetStudy;
     private HorizontalLayout studyBrowserMainLayout;
     
     @Autowired
     private TraitDataManager traitDataManager;
     
     private Button refreshButton;
     
     private Database database;
 
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
     
     public StudyTreeComponent(HorizontalLayout studyBrowserMainLayout, Database database) {
         this.studyBrowserMainLayout = studyBrowserMainLayout;
         this.database = database;
     }
 
     // Called by StudyButtonClickListener
     public void createTree() {
         this.removeComponent(studyTree);
         studyTree.removeAllItems();
         studyTree = createStudyTree(Database.LOCAL);
         this.addComponent(studyTree);
     }
 
     private Tree createStudyTree(Database database) {
         List<Study> studyParent = new ArrayList<Study>();
 
         try {
             studyParent = this.studyDataManager.getAllTopLevelStudies(0, 
                                 (int) studyDataManager.countAllTopLevelStudies(database), 
                                 database);
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             if (getWindow() != null){
                 MessageNotifier.showWarning(getWindow(), 
                         messageSource.getMessage(Message.ERROR_DATABASE),
                     messageSource.getMessage(Message.ERROR_IN_GETTING_TOP_LEVEL_STUDIES));
             }
             studyParent = new ArrayList<Study>();
         }
 
         Tree studyTree = new Tree();
 
         for (Study ps : studyParent) {
             studyTree.addItem(ps.getId());
             studyTree.setItemCaption(ps.getId(), ps.getName());
         }
 
         studyTree.addListener(new StudyTreeExpandListener(this));
         studyTree.addListener(new StudyItemClickListener(this));
 
         return studyTree;
     }
 
     // Called by StudyItemClickListener
     public void studyTreeItemClickAction(int studyId) throws InternationalizableException{
         try {
             Study study = this.studyDataManager.getStudyByID(studyId);
             //don't show study details if study record is a Folder ("F")
             if (!hasChildStudy(studyId) && !study.getType().equals("F")) {
                 createStudyInfoTab(studyId);
             }
         } catch (NumberFormatException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             MessageNotifier.showWarning(getWindow(), 
                     messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                     messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             MessageNotifier.showWarning(getWindow(), 
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
         }
     }
 
     public void addStudyNode(int parentStudyId) throws InternationalizableException{
         List<Study> studyChildren = new ArrayList<Study>();
         try {
             studyChildren = this.studyDataManager.getStudiesByParentFolderID(parentStudyId, 0, 
                     (int) studyDataManager.countAllStudyByParentFolderID(parentStudyId, database));
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             MessageNotifier.showWarning(getWindow(), 
                     messageSource.getMessage(Message.ERROR_DATABASE), 
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
             studyChildren = new ArrayList<Study>();
         }
 
         for (Study sc : studyChildren) {
             studyTree.addItem(sc.getId());
             studyTree.setItemCaption(sc.getId(), sc.getName());
             studyTree.setParent(sc.getId(), parentStudyId);
             // check if the study has sub study
             if (hasChildStudy(sc.getId())) {
                 studyTree.setChildrenAllowed(sc.getId(), true);
             } else {
                 studyTree.setChildrenAllowed(sc.getId(), false);
             }
         }
     }
 
     private void createStudyInfoTab(int studyId) throws InternationalizableException {
         VerticalLayout layout = new VerticalLayout();
 
         if (!Util.isTabExist(tabSheetStudy, getStudyName(studyId))) {
             layout.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId),
                     studyDataManager, traitDataManager));
             Tab tab = tabSheetStudy.addTab(layout, getStudyName(studyId), null);
             tab.setClosable(true);
 
             studyBrowserMainLayout.addComponent(tabSheetStudy);
             studyBrowserMainLayout.setExpandRatio(tabSheetStudy, 1.0f);
             tabSheetStudy.setSelectedTab(layout);
         } else {
             Tab tab = Util.getTabAlreadyExist(tabSheetStudy, getStudyName(studyId));
             tabSheetStudy.setSelectedTab(tab.getComponent());
         }
     }
 
     private String getStudyName(int studyId) throws InternationalizableException {
         try {
             return this.studyDataManager.getStudyByID(studyId).getName();
         } catch (MiddlewareQueryException e) {
             throw new InternationalizableException(e, Message.ERROR_DATABASE, Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID);
         }
     }
 
     private boolean hasChildStudy(int studyId) {
 
         List<Study> studyChildren = new ArrayList<Study>();
 
         try {
             studyChildren = this.studyDataManager.getStudiesByParentFolderID(studyId, 0, 1);
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             MessageNotifier.showWarning(getWindow(), 
                     messageSource.getMessage(Message.ERROR_DATABASE), 
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
             studyChildren = new ArrayList<Study>();
         }
         if (!studyChildren.isEmpty()) {
             return true;
         }
         return false;
     }
     
     @Override
     public void afterPropertiesSet() {
     	setSpacing(true);
         setMargin(true);
         
        tabSheetStudy = new TabSheet();

         studyTree = createStudyTree(database);
 
         refreshButton = new Button(); // "Refresh"
         refreshButton.setData(REFRESH_BUTTON_ID);
         
         if (database == Database.LOCAL) {
 
             refreshButton.addListener(new StudyButtonClickListener(this));
             addComponent(refreshButton);
         }
 
         // add tooltip
         studyTree.setItemDescriptionGenerator(new AbstractSelect.ItemDescriptionGenerator() {
 
             private static final long serialVersionUID = -2669417630841097077L;
 
             @Override
             public String generateDescription(Component source, Object itemId, Object propertyId) {
                 return messageSource.getMessage(Message.STUDY_DETAILS_LABEL); // "Click to view study details"
             }
         });
 
         addComponent(studyTree);
     }
     
     @Override
     public void attach() {
         super.attach();
         updateLabels();
     }
     
     @Override
     public void updateLabels() {
         messageSource.setCaption(refreshButton, Message.REFRESH_LABEL);
     }
 
 }
