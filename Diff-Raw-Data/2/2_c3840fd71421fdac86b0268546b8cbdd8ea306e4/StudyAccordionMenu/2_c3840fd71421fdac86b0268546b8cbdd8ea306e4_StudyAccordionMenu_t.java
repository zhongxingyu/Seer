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
 
 import org.generationcp.browser.study.listeners.StudySelectedTabChangeListener;
 import org.generationcp.browser.i18n.ui.I18NAccordion;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.api.StudyDataManager;
 import org.generationcp.middleware.manager.api.TraitDataManager;
 
 import com.github.peholmst.i18n4vaadin.I18N;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.VerticalLayout;
 
 public class StudyAccordionMenu extends I18NAccordion{
 
     private static final long serialVersionUID = -1409312205229461614L;
     private int studyId;
     private VerticalLayout layoutVariate;
     private VerticalLayout layoutFactor;
     private VerticalLayout layoutEffect;
 
     private StudyDataManager studyDataManager;
     private TraitDataManager traitDataManager;
 
     public StudyAccordionMenu(int studyId, StudyDetailComponent studyDetailComponent, StudyDataManager studyDataManager,
             TraitDataManager traitDataManager, I18N i18n) {
     	super(i18n);
         this.studyId = studyId;
         this.studyDataManager = studyDataManager;
         this.traitDataManager = traitDataManager;
         // Have it take all space available in the layout.
         this.setSizeFull();
 
         layoutVariate = new VerticalLayout();
         layoutFactor = new VerticalLayout();
         layoutEffect = new VerticalLayout();
         this.addTab(studyDetailComponent, i18n.getMessage("studyDetails.text")); //"Study Details"
         this.addTab(layoutFactor, i18n.getMessage("factors.text")); //"Factors"
         this.addTab(layoutVariate, i18n.getMessage("variates.text")); //"Variates"
        this.addTab(layoutEffect, i18n.getMessage("datasets.text")); //"Effects"
 
         this.addListener(new StudySelectedTabChangeListener(this));
     }
 
     public void selectedTabChangeAction() {
         Component selected = this.getSelectedTab();
         Tab tab = this.getTab(selected);
         if (tab.getCaption().equals(getI18N().getMessage("factors.text"))){ //"Factors"
             if (layoutFactor.getComponentCount() == 0) {
                 try {
                     layoutFactor.addComponent(new StudyFactorComponent(studyDataManager, traitDataManager, studyId, getI18N()));
                     layoutFactor.setMargin(true);
                     layoutFactor.setSpacing(true);
                 } catch (QueryException e) {
                     e.printStackTrace();
                 }
             }
         } else if (tab.getCaption().equals(getI18N().getMessage("variates.text"))){ //"Variates"
             if (layoutVariate.getComponentCount() == 0) {
                 try {
                     layoutVariate.addComponent(new StudyVariateComponent(studyDataManager, traitDataManager, studyId, getI18N()));
                     layoutVariate.setMargin(true);
                     layoutVariate.setSpacing(true);
                 } catch (QueryException e) {
                     e.printStackTrace();
                 }
             }
         } else if (tab.getCaption().equals(getI18N().getMessage("datasets.text"))){ //"Datasets"
             if (layoutEffect.getComponentCount() == 0) {
                 layoutEffect.addComponent(new StudyEffectComponent(studyDataManager, studyId, this, getI18N()));
 
             }
         }
 
     }
 
 }
