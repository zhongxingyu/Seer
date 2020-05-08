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
 import org.generationcp.commons.exceptions.InternationalizableException;
 import org.generationcp.browser.study.containers.StudyDataIndexContainer;
 import org.generationcp.browser.study.listeners.StudyItemClickListener;
 import org.generationcp.browser.util.Util;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.manager.Season;
 import org.generationcp.middleware.manager.api.StudyDataManager;
 import org.generationcp.middleware.manager.api.TraitDataManager;
 import org.generationcp.middleware.pojos.Study;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * 
  * @author Joyce Avestro
  * 
  */
 @Configurable
 public class StudySearchMainComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent{
 
     private static final long serialVersionUID = 1L;
 
     private final static Logger LOG = LoggerFactory.getLogger(StudySearchMainComponent.class);
 
     private VerticalLayout mainLayout;
     private VerticalLayout searchResultLayout;
     private static TabSheet tabSheetStudy;
 
     private StudySearchInputComponent searchInputComponent;
     private Table searchResultTable;
     
     private HorizontalLayout studyBrowserMainLayout;
 
     private StudyDataIndexContainer studyDataIndexContainer;
 
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
 
     @Autowired
     private StudyDataManager studyDataManager;
 
     @Autowired
     private TraitDataManager traitDataManager;
 
     public StudySearchMainComponent(HorizontalLayout studyBrowserMainLayout) throws InternationalizableException {
         this.studyBrowserMainLayout = studyBrowserMainLayout;
     }
 
     @Override
     public void afterPropertiesSet() throws Exception { 
 
         studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, null, 0);
        tabSheetStudy = new TabSheet();
 
         setSpacing(true);
         mainLayout = new VerticalLayout();
         mainLayout.setSpacing(true);
         mainLayout.setMargin(true);
         
         searchInputComponent = new StudySearchInputComponent(this);
         searchResultLayout = new VerticalLayout();
         searchResultLayout.setVisible(false);
 
         mainLayout.addComponent(searchInputComponent);
         mainLayout.addComponent(searchResultLayout);
 
         addComponent(mainLayout);
         
         studyBrowserMainLayout.addComponent(this);
         studyBrowserMainLayout.setExpandRatio(this, 1.0f);  
 
     }
 
     @Override
     public void attach() {
         super.attach();
         updateLabels();
     }
 
     @Override
     public void updateLabels() {
     }
     
     public void searchStudy(String name, String country, Season season, Integer date){
         IndexedContainer dataSourceResult = studyDataIndexContainer.getStudies(name, country, season, date);
         searchResultTable = new StudySearchResultTable(dataSourceResult).getResultTable();
         searchResultTable.setCaption(messageSource.getMessage(Message.SEARCH_RESULT_LABEL) + ": " + dataSourceResult.size());
         searchResultTable.addListener(new StudyItemClickListener(this));
         searchResultTable.setWidth(8, UNITS_CM);
         searchResultTable.setHeight(8, UNITS_CM);
         searchResultLayout.removeAllComponents();
         searchResultLayout.addComponent(searchResultTable);
         searchResultLayout.setVisible(true);
         mainLayout.requestRepaintAll();
 
     }
     
     public void studyItemClickAction(Integer studyId) {
         studyDataIndexContainer = new StudyDataIndexContainer(studyDataManager, null, studyId);
 
         try {
             Study study = this.studyDataManager.getStudyByID(studyId);
             //don't show study details if study record is a Folder ("F")
             if (!hasChildStudy(studyId) && !study.getType().equals("F")) {
                 createStudyInfoTab(studyId);
             }
         } catch (NumberFormatException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_INVALID_FORMAT),
                     messageSource.getMessage(Message.ERROR_IN_NUMBER_FORMAT));
         } catch (MiddlewareQueryException e) {
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
             MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID),
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDY_DETAIL_BY_ID));
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
             MessageNotifier.showWarning(getWindow(), messageSource.getMessage(Message.ERROR_DATABASE),
                     messageSource.getMessage(Message.ERROR_IN_GETTING_STUDIES_BY_PARENT_FOLDER_ID));
             studyChildren = new ArrayList<Study>();
         }
         if (!studyChildren.isEmpty()) {
             return true;
         }
         return false;
     }
 
     private void createStudyInfoTab(int studyId) throws InternationalizableException {
         VerticalLayout layout = new VerticalLayout();
 
         if (!Util.isTabExist(tabSheetStudy, getStudyName(studyId))) {
             layout.addComponent(new StudyAccordionMenu(studyId, new StudyDetailComponent(this.studyDataManager, studyId), studyDataManager,
                     traitDataManager));
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
 
     
 }
