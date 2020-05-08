 /*
  * Copyright 2009 IT Mill Ltd. Licensed under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
  * or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.generationcp.browser.germplasm;
 
 import org.generationcp.browser.germplasm.listeners.GermplasmButtonClickListener;
 import org.generationcp.browser.germplasm.listeners.GermplasmItemClickListener;
 import org.generationcp.browser.util.Util;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.Database;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vaadin.data.Item;
 import com.vaadin.data.util.IndexedContainer;
 import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 /**
  * The Application's "main" class
  */
 @SuppressWarnings("serial")
 public class GermplasmBrowserMain extends VerticalLayout{
 
     private final static Logger LOG = LoggerFactory.getLogger(GermplasmBrowserMain.class);
 
     private VerticalLayout mainLayout;
    private HorizontalLayout searchFormLayout;
     private Table resultTable;
     private IndexedContainer dataSourceResult;
     private TabSheet tabSheet = new TabSheet();
     private GermplasmDataManager managerGermplasm;
     private GermplasmIndexContainer dataResultIndexContainer;
 
     private GermplasmQueries qQuery;
     private String searchChoice;
     private String searchValue;
     private String instanceChoice;
     private Database instance;
 
     private GermplasmSearchFormComponent searchOption;
 
     public GermplasmBrowserMain(ManagerFactory factory) {
         try {
             initDataSource(factory);
         } catch (Exception e1) {
             // Log into log file
             LOG.error(e1.toString() + "\n" + e1.getStackTrace());
             e1.printStackTrace();
         }
 
         setSpacing(true);
 
         mainLayout = new VerticalLayout();
         mainLayout.setSpacing(true);
         mainLayout.setMargin(true);
 
        searchFormLayout = new HorizontalLayout();
 
         searchOption = new GermplasmSearchFormComponent();
        searchFormLayout.addComponent(searchOption);
 
         Button btnSearch = new Button("Search");
         btnSearch.addStyleName("addTopSpace");
 
         btnSearch.addListener(new GermplasmButtonClickListener(this));
        searchFormLayout.addComponent(btnSearch);
        
         mainLayout.addComponent(searchFormLayout);
 
         try {
             // Set the initial search result in Central
             dataSourceResult = dataResultIndexContainer.getGermplasResultContainer("Names", "", Database.CENTRAL);
         } catch (QueryException e1) {
             // Log into log file
             LOG.error(e1.toString() + "\n" + e1.getStackTrace());
             e1.printStackTrace();
         }
         resultTable = new SearchResultTable(dataSourceResult).getResultTable();
 
         mainLayout.addComponent(resultTable);
 
         resultTable.addListener(new GermplasmItemClickListener(this));
         addComponent(mainLayout);
 
     }
 
     private void displayGermplasmDetailTab(int gid) throws QueryException {
 
         VerticalLayout detailLayout = new VerticalLayout();
         detailLayout.setSpacing(true);
         // int screenWidth = 1028;
 
         if (!Util.isTabExist(tabSheet, String.valueOf(gid))) {
             detailLayout.addComponent(new GermplasmDetail(gid, qQuery, dataResultIndexContainer, mainLayout, tabSheet));
             Tab tab = tabSheet.addTab(detailLayout, String.valueOf(gid), null);
             tab.setClosable(true);
             tabSheet.setSelectedTab(detailLayout);
             mainLayout.addComponent(tabSheet);
         } else {
             Tab tab = Util.getTabAlreadyExist(tabSheet, String.valueOf(gid));
             tabSheet.setSelectedTab(tab.getComponent());
         }
     }
 
     private void initDataSource(ManagerFactory factory) throws Exception {
         managerGermplasm = factory.getGermplasmDataManager();
         qQuery = new GermplasmQueries(factory, managerGermplasm);
         dataResultIndexContainer = new GermplasmIndexContainer(qQuery);
 
     }
 
     // private int getScreenWidth(){
     // WebApplicationContext context =
     // ((WebApplicationContext)this.getContext());
     // WebBrowser wb = context.getBrowser();
     // return wb.getScreenWidth();
     // }
 
     public void searchButtonClickAction() {
 
         searchChoice = searchOption.getChoice();
         searchValue = searchOption.getSearchValue();
         instanceChoice = searchOption.getDatabaseInstance();
 
         if (instanceChoice.equals("Central")) {
             instance = Database.CENTRAL;
         } else {
             instance = Database.LOCAL;
         }
 
         try {
             boolean withNoError = true;
             if (searchValue.length() > 0) {
                 Window window;
                 if (searchChoice.equals("GID")) {
                     try {
                         int gid = Integer.parseInt(searchValue);
                         displayGermplasmDetailTab(gid);
                     } catch (QueryException e) {
                         // Log into log file
                         LOG.error(e.toString() + "\n" + e.getStackTrace());
                         withNoError = false;
                         e.printStackTrace();
                     } catch (NumberFormatException nFE) {
                         // Log into log file
                         LOG.error(nFE.toString() + "\n" + nFE.getStackTrace());
                         withNoError = false;
                         // mainLayout.showNotification("Invalid Input","Must be numeric");
                     }
                 }
                 if (withNoError) {
                     dataSourceResult = dataResultIndexContainer.getGermplasResultContainer(searchChoice, searchValue, instance);
                     resultTable.setCaption("Germplasm Search Result: " + dataSourceResult.size());
                     resultTable.setContainerDataSource(dataSourceResult);
                     mainLayout.requestRepaintAll();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     // Called by GermplasmItemClickListener
     public void resultTableItemClickAction(Table sourceTable, Object itemId, Item item) {
         sourceTable.select(itemId);
         int gid = Integer.valueOf(item.getItemProperty("gid").toString());
         try {
             displayGermplasmDetailTab(gid);
         } catch (QueryException e) {
             // Log into log file
             LOG.error(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
         }
     }
 }
