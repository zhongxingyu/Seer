 /**
  * Author            : Elan
  * Created On        : 2012-3-29 下午12:31:57
  * 
  * Copyright 2012.  All rights reserved. 
  * 
  */
 package org.pmp.action.admin;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 import org.pmp.action.business.BaseAction;
 import org.pmp.jms.JmsPublisher;
 import org.pmp.json.Includer;
 import org.pmp.json.MyJson;
 import org.pmp.service.admin.IGroupService;
 import org.pmp.service.admin.IRoleService;
 import org.pmp.service.admin.IUserGroupRoleService;
 import org.pmp.service.admin.IUserService;
 import org.pmp.service.business.ISmsSendService;
 import org.pmp.util.Pager;
 import org.pmp.util.SessionHandler;
 import org.pmp.vo.Company;
 import org.pmp.vo.Project;
 import org.pmp.vo.SMSCompany;
 import org.pmp.vo.SMSSend;
 import org.pmp.vo.TbGroup;
 import org.pmp.vo.TbRole;
 import org.pmp.vo.TbUser;
 import org.pmp.vo.TbUserGroupRole;
 
 /**
  * @author Elan
  * @version 1.0
  * @update TODO
  */
 public class UserAction extends BaseAction{
 
     //~ Static Fields ==================================================================================================
     static Logger logger = Logger.getLogger(UserAction.class.getName());
     //~ Instance Fields ================================================================================================
     private IUserService userService;
     private IRoleService roleService;
     private IGroupService groupService;
     private IUserGroupRoleService ugrService;
     private ISmsSendService smsSendService;
     
     private TbUser user;
     private Integer roleId;
     private Integer groupId;
     private Integer userId;
     private String userName;
   
     /* used when deleteUser */
     private String idStr;
    
 
     //~ Methods ========================================================================================================
     public String addUser(){
 	//default password is 123456
 	user.setPassword("123456");
 	//user.setPassword(Encoder.md5("123456"));
 	user.setIssys(false);
 	
 	userService.addUser(user);
 	//user obtain the id after saving
 	
 	TbRole role = roleService.getRoleByID(roleId);
 	TbGroup group = groupService.getGroupByID(groupId);
 	TbUserGroupRole ugr = new TbUserGroupRole();
 	ugr.setTbGroup(group);
 	ugr.setTbRole(role);
 	ugr.setTbUser(user);
 	
 	ugrService.addUGR(ugr);
 	
 	return SUCCESS;
     }
     
     public void checkUser(){
     	TbUser user = userService.getUserByUsername(userName);
     	Map<String, Object> params = new LinkedHashMap<String, Object>();
     	if(user!=null){
     	    params.put("result", "Failed");
     	} else{
     	    params.put("result", "Success");
     	}
      	MyJson json = new MyJson();
      	json.output(json.toJson(params));
     }
     
     public String editUser(){
         TbUser user2 = userService.getUserById(user.getUserId());
     	user.setUsername(user2.getUsername());
     	user.setPassword(user2.getPassword());
     	user.setIssys(user2.isIssys());
     	userService.editUser(user);
     	TbUserGroupRole ugr = ugrService.getUGR_ByUserID(user.getUserId());
     	
     	TbRole role = roleService.getRoleByID(roleId);
     	TbGroup group = groupService.getGroupByID(groupId);
     	ugr.setTbGroup(group);
     	ugr.setTbRole(role);
     	ugr.setTbUser(user);
     	
     	ugrService.editUGR(ugr);
     	return SUCCESS;
     }
     
     public void deleteUser(){
 	List<TbUser> userList = new ArrayList<TbUser>();
 	String[] checkedID = idStr.split(",");
 	for (int i=0;i<checkedID.length;i++){
 	    TbUser user = userService.getUserById(Integer.parseInt(checkedID[i]));
 	    userList.add(user);
 	}
 	userService.batchDelete(userList);
 
 	Map<String,String> result = new HashMap<String, String>();
 	MyJson json = new MyJson();
 	result.put("msg", "用户删除成功");
 	json.output(json.toJson(result));
     }
 
     public String getUserDetail(){
     	TbUserGroupRole ugr = ugrService.getUGR_ByUserID(userId);
     	HttpServletRequest request = ServletActionContext.getRequest();
     	request.setAttribute("ugr", ugr);
     	return SUCCESS;
     }
     
     public void passwordReset(){
 	/* 生成随机6位数字密码 */
 	Integer randomPassword = (int) (System.currentTimeMillis()%10000000);
 	logger.debug("password="+randomPassword);
 	
 	TbUser user = userService.getUserById(userId);
 	user.setPassword(randomPassword.toString());
 	/* 更新该用户的密码 */
 	userService.editUser(user);
 	
 	/* 获得该用户所在公司的OpenMAS账号 */
 	SMSCompany smsc = SessionHandler.getSMSCompany(userId);
 	/* 创建SMSSend对象 */
 	SMSSend smsSend = new SMSSend();
 	smsSend.setSMSCompany(smsc);
 	smsSend.setSmssPerson(SessionHandler.getUser().getUsername());
 	smsSend.setSmssReceiver(user.getMobile());
	smsSend.setSmssState("待发送");
 	smsSend.setSmssTime(new Date());
	smsSend.setSmssContent("您重置后的密码为："+randomPassword+",请妥善保管自己的密码信息！");
 	
 	/* 保存该待发送短信记录 */
 	smsSendService.addSmsSend(smsSend);
 	
 	/* 给消息队列发消息 */
 	JmsPublisher.sendMessgae(smsSend.getSmssId().toString());
     }
     
     public void loadUserList(){
 	Pager pager = getPager();
 	/* set query parameter */
         Map<String,Object> params = getParams();
 	/* set sorter type */
 	String order = getOrder();	 
 	
 	List<?> ugrList = null;
 	if (SessionHandler.getUserGroup().getGroupLevel()==1){
 	    ugrList = ugrService.loadUGRList(params, order, pager);
 	}
 	if (SessionHandler.getUserGroup().getGroupLevel()==2){
 	    String comName = ((Company)SessionHandler.getUserRefDomain()).getComName();
 	    ugrList = ugrService.loadUGRList_ByCom(comName, params, order, pager);
 	}
 	if (SessionHandler.getUserGroup().getGroupLevel()==3){
 	    String proName = ((Project)SessionHandler.getUserRefDomain()).getProName();
 	    ugrList = ugrService.loadUGRList_ByPro(proName, params, order, pager);
 	}
 	
     	/* convert ugrList instance to JsonData */
 	String[] attrs = {"tbUser.userId","tbUser.realname","tbUser.username","tbUser.mobile","tbUser.identify",
 		"tbUser.position","tbUser.enabled","tbGroup.groupName","tbRole.roleName"};
 	List<String> show = Arrays.asList(attrs);
 	Includer includer = new Includer(show);
 	MyJson json = new MyJson(includer);
 	json.output(json.toJson(ugrList, "", pager));
     }
     
     //~ Getters and Setters ============================================================================================
     public IUserService getUserService() {
         return userService;
     }
     public void setUserService(IUserService userService) {
         this.userService = userService;
     }
 
     public TbUser getUser() {
         return user;
     }
 
     public void setUser(TbUser user) {
         this.user = user;
     }
 
     public IRoleService getRoleService() {
         return roleService;
     }
 
     public void setRoleService(IRoleService roleService) {
         this.roleService = roleService;
     }
 
     public IGroupService getGroupService() {
         return groupService;
     }
 
     public void setGroupService(IGroupService groupService) {
         this.groupService = groupService;
     }
 
     public IUserGroupRoleService getUgrService() {
         return ugrService;
     }
 
     public void setUgrService(IUserGroupRoleService ugrService) {
         this.ugrService = ugrService;
     }
 
     public ISmsSendService getSmsSendService() {
         return smsSendService;
     }
 
     public void setSmsSendService(ISmsSendService smsSendService) {
         this.smsSendService = smsSendService;
     }
 
     public Integer getRoleId() {
         return roleId;
     }
 
     public void setRoleId(Integer roleId) {
         this.roleId = roleId;
     }
 
     
     public Integer getGroupId() {
         return groupId;
     }
 
     public void setGroupId(Integer groupId) {
         this.groupId = groupId;
     }
 
     public Integer getUserId() {
         return userId;
     }
 
     public void setUserId(Integer userId) {
         this.userId = userId;
     }
 
     public String getIdStr() {
         return idStr;
     }
 
     public void setIdStr(String idStr) {
         this.idStr = idStr;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 }
