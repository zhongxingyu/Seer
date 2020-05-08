 /***************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  **************************************************************/
 
 package org.generationcp.browser.application;
 
 import org.generationcp.browser.germplasm.GidByPhenotypicQueries;
 import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
 import org.generationcp.browser.germplasm.TraitDataIndexContainer;
 import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
 import org.generationcp.browser.study.listeners.StudyButtonClickListener;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * This class extends a VerticalLayout and is basically a container for the
  * components to be shown on the Welcome tab of the application. From the
  * Welcome tab, you can access the other tabs of the main application window
  * thru the use of the buttons.
  * 
  * @author Kevin Manansala
  * 
  */
 public class WelcomeTab extends VerticalLayout{
 
     private final static Logger LOG = LoggerFactory.getLogger(WelcomeTab.class);
     private static final long serialVersionUID = -917787404988386915L;
     private int screenWidth;
     
     private VerticalLayout rootLayoutForGermplasmBrowser;
     
     private VerticalLayout rootLayoutForStudyBrowser;
     private ManagerFactory factory;
     private TabSheet theTabSheet;
     
     private VerticalLayout rootLayoutForGermplasmByPheno;
     private GidByPhenotypicQueries gidByPhenoQueries;
     private TraitDataIndexContainer traitDataCon;
 
     public WelcomeTab(TabSheet tabSheet, final ManagerFactory factory, VerticalLayout rootLayoutsForOtherTabs[]) {
 	super();
 	this.factory = factory;
 	this.setSpacing(true);
 	this.setMargin(true);
 	this.theTabSheet = tabSheet;
 	this.gidByPhenoQueries = new GidByPhenotypicQueries(factory,
 		factory.getStudyDataManager());
 	this.traitDataCon = new TraitDataIndexContainer(factory, factory.getTraitDataManager());
 
 	Label welcomeLabel = new Label("<h1>Welcome to the Germplasm and Study Browser</h1>");
 	welcomeLabel.setContentMode(Label.CONTENT_XHTML);
 	this.addComponent(welcomeLabel);
 
 	Label questionLabel = new Label("<h3>What do you want to do?</h3>");
 	questionLabel.setContentMode(Label.CONTENT_XHTML);
 	this.addComponent(questionLabel);
 
 	Button germplasmButton = new Button("I want to browse Germplasm information");
 	germplasmButton.setWidth(400, UNITS_PIXELS);
 	this.rootLayoutForGermplasmBrowser = rootLayoutsForOtherTabs[0];
 
 	germplasmButton.addListener(new GermplasmButtonClickListener(this));
 	this.addComponent(germplasmButton);
 
 	Button studyButton = new Button("I want to browse Studies and their Datasets");
 	studyButton.setWidth(400, UNITS_PIXELS);
 	rootLayoutForStudyBrowser = rootLayoutsForOtherTabs[1];
 
 	studyButton.addListener(new StudyButtonClickListener(this));
 	
 	this.addComponent(studyButton);
 
 	Button germplasmByPhenoButton = new Button("I want to retrieve Germplasms by Phenotypic Data");
 	germplasmByPhenoButton.setWidth(400, UNITS_PIXELS);
	final VerticalLayout rootLayoutForGermplasmByPheno = rootLayoutsForOtherTabs[2];
 
 	germplasmByPhenoButton.addListener(new GermplasmButtonClickListener(this));
 	this.addComponent(germplasmByPhenoButton);
     }
     
     // Called by GermplasmButtonClickListener
     public void browserGermplasmInfoButtonClickAction(){
 	if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
 	    rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMainApplication(factory));
 	    rootLayoutForGermplasmBrowser.addStyleName("addSpacing");
 	}
 
 	theTabSheet.setSelectedTab(rootLayoutForGermplasmBrowser);
     }
     
     // Called by StudyButtonClickListener
     public void browseStudiesAndDataSets(){
 	if (rootLayoutForStudyBrowser.getComponentCount() == 0) {
 	    rootLayoutForStudyBrowser.addComponent(new StudyBrowserMainApplication(factory));
 	    rootLayoutForStudyBrowser.addStyleName("addSpacing");
 	}
 
 	theTabSheet.setSelectedTab(rootLayoutForStudyBrowser);
 	
     }
     
     // Called by GermplasmButtonClickListener
     public void searchGermplasmByPhenotyicDataButtonClickAction(){
 	// when the button is clicked, content for the tab is
 	// dynamically created
 	// creation of content is done only once
 	if (rootLayoutForGermplasmByPheno.getComponentCount() == 0) {
 	    try {
 		rootLayoutForGermplasmByPheno.addComponent(new SearchGermplasmByPhenotypicTab(
 			gidByPhenoQueries, traitDataCon));
 	    } catch (QueryException e) {
 		// Log into log file
 		LOG.error(e.toString() + "\n" + e.getStackTrace());
 		e.printStackTrace();
 	    }
 	}
 	theTabSheet.setSelectedTab(rootLayoutForGermplasmByPheno);
 
     }
 }
