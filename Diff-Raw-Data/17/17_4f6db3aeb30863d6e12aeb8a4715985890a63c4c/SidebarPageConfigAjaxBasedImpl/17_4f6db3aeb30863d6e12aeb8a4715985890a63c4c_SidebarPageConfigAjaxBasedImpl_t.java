 /* 
 	Description:
 		ZK Essentials
 	History:
 		Created by dennis
 
 Copyright (C) 2012 Potix Corporation. All Rights Reserved.
 */
 package org.strasa.web.page.navigation.user;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import org.strasa.web.page.util.navigation.SidebarPage;
 import org.strasa.web.page.util.navigation.SidebarPageConfig;
 
 public class SidebarPageConfigAjaxBasedImpl implements SidebarPageConfig{
 	
 	HashMap<String,SidebarPage> pageMap = new LinkedHashMap<String,SidebarPage>();
 	public SidebarPageConfigAjaxBasedImpl(){		
 		pageMap.put("upload_menu1",new SidebarPage("upload_menu1","New Study","/images/fn.png","/user/uploadstudy/index.zul"));
		pageMap.put("upload_menu2",new SidebarPage("upload_menu2","Study","/images/study16.png","/user/maintenance/edituploadedstudies.zul"));
		pageMap.put("upload_menu3",new SidebarPage("upload_menu2","Germplasm","/images/Germplasm16.png","/user/home.zul"));
 		pageMap.put("upload_menu4",new SidebarPage("upload_menu4","Genotyping Data","/images/fn.png","/user/home.zul"));
		pageMap.put("browse_menu1",new SidebarPage("browse_menu1","Studies","/images/study16.png","/user/browsestudy/index.zul"));
		pageMap.put("browse_menu2",new SidebarPage("browse_menu2","Germplasm","/images/Germplasm16.png","/user/browsegermplasm/browsegermplasm.zul"));
		pageMap.put("browse_menu3",new SidebarPage("browse_menu3","Across Study Query","/images/study16.png","/user/crossstudyquery/crossstudyquery.zul"));
 		pageMap.put("analysis_menu1",new SidebarPage("analysis_menu1","Phenotypic Analysis","/images/fn.png","/user/home.zul"));
//		pageMap.put("maintenance_menu1",new SidebarPage("maintenance_menu1","Uploaded Studies","/images/fn.png","/user/maintenance/edituploadedstudies.zul"));
 		pageMap.put("maintenance_menu2",new SidebarPage("maintenance_menu2","Program","/images/fn.png","/user/maintenance/editprogram.zul"));
 		pageMap.put("maintenance_menu3",new SidebarPage("maintenance_menu3","Project","/images/fn.png","/user/maintenance/editproject.zul"));
		pageMap.put("maintenance_menu4",new SidebarPage("maintenance_menu4","Profile","/images/user16.png","/user/maintenance/edituser.zul"));
 
 	}
 	
 	public List<SidebarPage> getPages(){
 		return new ArrayList<SidebarPage>(pageMap.values());
 	}
 	
 	public SidebarPage getPage(String name){
 		return pageMap.get(name);
 	}
 	
 }
