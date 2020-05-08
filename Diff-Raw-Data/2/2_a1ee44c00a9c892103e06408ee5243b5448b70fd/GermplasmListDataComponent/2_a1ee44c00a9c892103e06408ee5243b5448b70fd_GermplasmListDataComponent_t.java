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
 
 package org.generationcp.browser.germplasmlist;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.browser.application.Message;
 import org.generationcp.browser.study.containers.RepresentationDatasetQueryFactory;
 import org.generationcp.browser.study.listeners.StudyButtonClickListener;
 import org.generationcp.commons.vaadin.spring.InternationalizableComponent;
 import org.generationcp.commons.vaadin.spring.SimpleResourceBundleMessageSource;
 import org.generationcp.commons.vaadin.util.MessageNotifier;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.api.GermplasmListManager;
 import org.generationcp.middleware.pojos.Factor;
 import org.generationcp.middleware.pojos.GermplasmList;
 import org.generationcp.middleware.pojos.GermplasmListData;
 import org.generationcp.middleware.pojos.Variate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
 
 import com.vaadin.data.Item;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 
 @Configurable
 public class GermplasmListDataComponent extends VerticalLayout implements InitializingBean, InternationalizableComponent {
 
     @SuppressWarnings("unused")
     private static final Logger LOG = LoggerFactory.getLogger(GermplasmListDataComponent.class);
     private static final long serialVersionUID = -6487623269938610915L;
 
     private Table listDataTable;
     
     private GermplasmListManager germplasmListManager;
     private int germplasmListId;
 
     @Autowired
     private SimpleResourceBundleMessageSource messageSource;
     
     public GermplasmListDataComponent(GermplasmListManager germplasmListManager, int germplasmListId){
     	this.germplasmListManager = germplasmListManager;
     	this.germplasmListId = germplasmListId;
     }
     
     @Override
     public void afterPropertiesSet() throws Exception{
         List<GermplasmListData> listData = new ArrayList<GermplasmListData>();
         int listDataCount = this.germplasmListManager.countGermplasmListDataByListId(germplasmListId);
         listData = this.germplasmListManager.getGermplasmListDataByListId(germplasmListId, 0, listDataCount);
         
         // create the Vaadin Table to display the Germplasm List Data
         listDataTable = new Table("");
         listDataTable.setColumnCollapsingAllowed(true);
         listDataTable.setColumnReorderingAllowed(true);
         listDataTable.setPageLength(15); // number of rows to display in the Table
         listDataTable.setSizeFull(); // to make scrollbars appear on the Table component
         
         listDataTable.addContainerProperty("gid", Integer.class, null);
         listDataTable.addContainerProperty("entryId", Integer.class, null);
         listDataTable.addContainerProperty("entryCode", String.class, null);
         listDataTable.addContainerProperty("seedSource", String.class, null);
         listDataTable.addContainerProperty("designation", String.class, null);
         listDataTable.addContainerProperty("groupName", String.class, null);
         listDataTable.addContainerProperty("status", String.class, null);
         
         listDataTable.setColumnHeader("gid", "GID");
         listDataTable.setColumnHeader("entryId", "Entry ID");
         listDataTable.setColumnHeader("entryCode", "Entry Code");
         listDataTable.setColumnHeader("seedSource", "Seed Source");
         listDataTable.setColumnHeader("designation", "Designation");
         listDataTable.setColumnHeader("groupName", "Group Name");
         listDataTable.setColumnHeader("status", "Status");
         
         for (GermplasmListData data : listData) {
             listDataTable.addItem(new Object[] {
                     data.getGid(), data.getEntryId(), data.getEntryCode(), data.getSeedSource(),
                     data.getDesignation(), data.getGroupName(), data.getStatusString()
             }, data.getId());
         }
         
         listDataTable.sort(new Object[]{"entryId"}, new boolean[]{true});
 
         addComponent(listDataTable);
     }
     
     @Override
     public void attach() {
         super.attach();
         updateLabels();
     }
 
     @Override
     public void updateLabels() {
         /*messageSource.setCaption(lblName, Message.name_label);
         messageSource.setCaption(lblTitle, Message.title_label);
         messageSource.setCaption(lblObjective, Message.objective_label);
         messageSource.setCaption(lblType, Message.type_label);
         messageSource.setCaption(lblStartDate, Message.start_date_label);
         messageSource.setCaption(lblEndDate, Message.end_date_label);*/
     }
 
 }
