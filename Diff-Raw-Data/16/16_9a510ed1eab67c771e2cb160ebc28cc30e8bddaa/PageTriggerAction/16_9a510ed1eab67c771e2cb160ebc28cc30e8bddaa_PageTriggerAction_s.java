 /**
  * Author            : Elan
  * Created On        : 2012-5-14 下午02:19:36
  * 
  * Copyright 2012.  All rights reserved. 
  *
  * Revision History
  * 
  *    Date       Modifier       Comments
  * ----------    -------------  --------------------------------------------
  * 
  */
 package org.pmp.action.business;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 import org.pmp.constant.HouseState;
 import org.pmp.json.MyJson;
 import org.pmp.service.business.IBuildingService;
 import org.pmp.service.business.ICompanyService;
 import org.pmp.service.business.IHouseOwnerService;
 import org.pmp.service.business.IHouseService;
 import org.pmp.service.business.IProjectService;
 import org.pmp.service.business.IPublicRepairItemService;
 import org.pmp.util.JsonConvert;
 import org.pmp.util.Pager;
 import org.pmp.util.SessionHandler;
 import org.pmp.vo.Building;
 import org.pmp.vo.Company;
 import org.pmp.vo.HouseOwner;
 import org.pmp.vo.Owner;
 import org.pmp.vo.Project;
 import org.pmp.vo.PublicRepairItem;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 /**
  * @author Elan
  * @version 1.0
  * @update TODO
  */
 public class PageTriggerAction extends ActionSupport{
 
     //~ Static Fields ==================================================================================================
     private static Logger logger = Logger.getLogger(PageTriggerAction.class.getName());
     //~ Instance Fields ================================================================================================
     private IProjectService projectService;
     private IHouseService houseService;
     private IBuildingService buildingService;
     private IHouseOwnerService houseOwnerService;
     private ICompanyService companyService;
     private IPublicRepairItemService publicRepairItemService;
     
     private Integer proId;
     private Integer builId;
     private Integer houseId;
     private Integer isEmpty;
     private Integer fbiId;
     
     //~ Methods ========================================================================================================
 
     /**
      * @Title: getAllCompany
      * @Description: 在smsc_add.jsp和smsc_edit.jsp页面调用
      */
     public String getAllCompany(){
     	Pager pager2 = new Pager(1000,1);
     	Map<String,Object> params = new HashMap<String,Object>();
     	String order = "order by comId asc";
     	List<Company> companyList = companyService.loadCompanyList_ByChinaMobile(params, order, pager2);
     	HttpServletRequest request = ServletActionContext.getRequest();
 	request.setAttribute("companyList", companyList);
     	return SUCCESS;
     }
     
     public String selectProject_ByAuth(){
 	List<Project> list = null;
 	Object obj = SessionHandler.getUserRefDomain();
 	if (obj instanceof Company){
 	    Pager pager = new Pager(1000,1);
 	    list = projectService.loadProjectList_ByCompany(((Company)obj).getComId(), new HashMap<String,Object>(), "", pager);
 	} else{
 	    list = new ArrayList<Project>();
 	    list.add((Project)obj);
 	}
 	
 	HttpServletRequest request = ServletActionContext.getRequest();
 	request.setAttribute("proList", list);
 	return SUCCESS;
     }
 
     public void selectBuilding_ByPro(){
 	List<?> buildingList = buildingService.loadBuildingList_ByProject(proId, new HashMap<String,Object>(), "order by builId asc", new Pager(1000,1));
 	String[] attrs = {"builId","builNum"};
 	List<String> show = Arrays.asList(attrs);
 	String data = JsonConvert.list2Json(buildingList, "org.pmp.vo.Building", show);
 	JsonConvert.output(data);
     }
     
     public void selectHouse_ByBuil(){
 	Map<String,Object> map = new HashMap<String,Object>();
 	if(isEmpty!=null)map.put("isempty", HouseState.EMPTY);
         List<?> houseList = houseService.loadHouseList_ByBuilding(builId, map, "order by houseId asc", new Pager(1000,1));
         String[] attrs = {"houseId","houseNum"};
         List<String> show = Arrays.asList(attrs);
         String data = JsonConvert.list2Json(houseList, "org.pmp.vo.House", show);
         JsonConvert.output(data);
     }
     
     /* 在ef_item_add.jsp页面，由Ajax异步调用 */
     public void getBuilInfo(){
 	Building buil = buildingService.getBuildingById(builId);
 	StringBuilder info = new StringBuilder();
 	info.append("共"+buil.getFloorCount()+"层,每层"+buil.getUnitCount()+"单元");
 	if (buil.getSkipFloor()!=null && !buil.getSkipFloor().equals("")){
 	    info.append(",跳过"+buil.getSkipFloor()+"层");
 	}
 	String data = "{\"info\":\""+info+"\"}";
 	MyJson.print(data);
     }
     
     /* 在fix_owner_add.jsp页面，由Struts标签触发调用 */
     public void getHouseOwnerInfo(){
 	HouseOwner ho = houseOwnerService.getHouseOwner_ByHouse(houseId);
 	Owner owner = ho.getOwner();
 	StringBuilder data = new StringBuilder();
 	data.append("{\"hoId\":\""+ho.getHoId()+"\",\"ownerName\":\""+owner.getOwnerName()+"\",");
 	String contact;
 	if (owner.getMobile()!=null){
 	    contact = owner.getMobile();
 	} else if (owner.getHomePhone()!=null){
 	    contact = owner.getHomePhone();
 	} else {
 	    contact = "";
 	}
 	data.append("\"contactPhone\":\""+contact+"\"}");
 	MyJson.print(data.toString());
     }
     
     /* 在fix_public_add.jsp页面，由Struts标签触发调用 */
    public String getEquipList(){
 	PublicRepairItem item = publicRepairItemService.getPublicRepairItemByID(fbiId);
 	String[] equips = item.getEquipList().split(",");
 	List<String> list = Arrays.asList(equips);
 	
	HttpServletRequest request = ServletActionContext.getRequest();
	request.setAttribute("equipList", list);
	return SUCCESS;
     }
     
     //~ Getters and Setters ============================================================================================
 
     public IProjectService getProjectService() {
         return projectService;
     }
 
     public void setProjectService(IProjectService projectService) {
         this.projectService = projectService;
     }
 
     public IHouseService getHouseService() {
         return houseService;
     }
 
     public void setHouseService(IHouseService houseService) {
         this.houseService = houseService;
     }
 
     public IBuildingService getBuildingService() {
         return buildingService;
     }
 
     public void setBuildingService(IBuildingService buildingService) {
         this.buildingService = buildingService;
     }
 
     public IHouseOwnerService getHouseOwnerService() {
         return houseOwnerService;
     }
 
     public void setHouseOwnerService(IHouseOwnerService houseOwnerService) {
         this.houseOwnerService = houseOwnerService;
     }
 
     public ICompanyService getCompanyService() {
         return companyService;
     }
 
     public void setCompanyService(ICompanyService companyService) {
         this.companyService = companyService;
     }
 
     public Integer getProId() {
         return proId;
     }
 
     public void setProId(Integer proId) {
         this.proId = proId;
     }
 
     public Integer getBuilId() {
         return builId;
     }
 
     public void setBuilId(Integer builId) {
         this.builId = builId;
     }
 
     public Integer getHouseId() {
         return houseId;
     }
 
     public void setHouseId(Integer houseId) {
         this.houseId = houseId;
     }
 
     public Integer getIsEmpty() {
         return isEmpty;
     }
 
     public void setIsEmpty(Integer isEmpty) {
         this.isEmpty = isEmpty;
     }
 
     public IPublicRepairItemService getPublicRepairItemService() {
         return publicRepairItemService;
     }
 
     public void setPublicRepairItemService(
     	IPublicRepairItemService publicRepairItemService) {
         this.publicRepairItemService = publicRepairItemService;
     }
 
     public Integer getFbiId() {
         return fbiId;
     }
 
     public void setFbiId(Integer fbiId) {
         this.fbiId = fbiId;
     }
 
 }
