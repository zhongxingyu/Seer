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
 
 package org.generationcp.browser.germplasm;
 
 import java.io.Serializable;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.generationcp.middleware.manager.api.GermplasmListManager;
 import org.generationcp.middleware.pojos.GermplasmList;
 import org.generationcp.middleware.pojos.GermplasmListData;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import com.vaadin.ui.TabSheet;
 
 @Configurable
 public class SaveGermplasmListAction implements Serializable, InitializingBean {
 
 
     private static final long serialVersionUID = 1L;
 
     @Autowired
     private ManagerFactory managerFactory;
     
     private GermplasmListManager germplasmListManager;
 
     public SaveGermplasmListAction() {
         
     }
 
     public void addGermplasListNameAndData(String listName,TabSheet tabSheet) throws QueryException{
     	
     	
     	SaveGermplasmListAction saveGermplasmAction= new SaveGermplasmListAction();
 		Date date =  new Date();
 		Format formatter = new SimpleDateFormat("yyyyMMdd");
 		Long currentDate=Long.valueOf(formatter.format(date)); 
 		int userId=1; 
 		String description = "-"; 
 		String type="LST";
 		GermplasmList parent=null;
 		int statusListName=1;
 
 		GermplasmList listNameData = new GermplasmList(null, listName, currentDate, type, userId,
 				description, parent, statusListName);
 
 		int listid=germplasmListManager.addGermplasmList(listNameData);
 
 		GermplasmList germList = germplasmListManager.getGermplasmListById(listid);
 		String entryCode="-";
 		String seedSource="-";
 		String groupName="-";
 		String designation="-";
 		int status=0;
 		int localRecordId=0;
 		int entryid=1;
 
		for (int i = 0 ; i < tabSheet.getComponentCount(); i++) {
 			int gid=Integer.valueOf(tabSheet.getTab(i).getCaption().toString());
 			GermplasmListData germplasmListData = new GermplasmListData(null, germList, gid, entryid, entryCode, seedSource, designation, groupName, status, localRecordId);
 			germplasmListManager.addGermplasmListData(germplasmListData);
 			entryid++;
 		}
 
     	
     }
     
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 
 		this.germplasmListManager = managerFactory.getGermplasmListManager();
 		
 	}
 
 }
