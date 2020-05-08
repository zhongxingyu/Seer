 /**
  * Author            : Elan
  * Created On        : 2012-4-18 下午05:04:30
  * 
  * Copyright 2012.  All rights reserved. 
  * 
  */
 package org.pmp.util;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 import org.pmp.service.admin.IModuleService;
 import org.pmp.service.admin.IUserGroupRoleService;
 import org.pmp.service.admin.IUserService;
 import org.pmp.service.business.ICompanyService;
 import org.pmp.service.business.IProjectService;
 import org.pmp.service.business.ISmsCompanyService;
 import org.pmp.vo.Company;
 import org.pmp.vo.Project;
 import org.pmp.vo.SMSCompany;
 import org.pmp.vo.TbGroup;
 import org.pmp.vo.TbRole;
 import org.pmp.vo.TbUser;
 import org.pmp.vo.TbUserGroupRole;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 /**
  * @author Elan
  * @version 1.0
  * @update TODO
  */
 public class SessionHandler {
 
     //~ Static Fields ==================================================================================================
     static Logger logger = Logger.getLogger(SessionHandler.class.getName());
     
     //~ Methods ========================================================================================================
     public static void putUserIntoSession(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 	String principal = authentication.getPrincipal().toString();
 	String [] aa = principal.split(";");
 	String bb = null;
 	for (int i=0;i<aa.length;i++){
 	    if (aa[i].contains("Username")){
 		bb = aa[i];
 		break;
 	    }
 	}
 	String [] cc = bb.split(":");
 	String username = cc[2].trim();
 	session.put("username", username);
 	/* get user instance */
 	IUserService userService = (IUserService)SpringContextUtil.getBean("userService");
 	TbUser user = userService.getUserByUsername(username);
 	session.put("user", user);
 	
 	/* get the user's group and role instance */
 	IUserGroupRoleService ugrService = (IUserGroupRoleService)SpringContextUtil.getBean("ugrService");
 	TbUserGroupRole ugr = ugrService.getUGR_ByUserID(user.getUserId());
 	TbGroup group = ugr.getTbGroup();
 	TbRole role = ugr.getTbRole();
 	session.put("group", group);
 	session.put("role", role);
 	session.put("rolename", role.getRoleName());
 	
 	/* get the user's refDomain instance */
 	String refDomain = group.getRefDomain().trim();
 	Integer level = group.getGroupLevel();
 	Object obj = null;
 	if(level == 2){
 	    ICompanyService companyService = (ICompanyService)SpringContextUtil.getBean("companyService");
 	    obj = companyService.getCompanyByName(refDomain);
 	} else if(level == 3){
 	    IProjectService projectService = (IProjectService)SpringContextUtil.getBean("projectService");
 	    obj = projectService.getProjectByName(refDomain);
 	}
 	session.put("refDomain", obj);
 	
 	/* get the user's SMSComapny instance */
 	SMSCompany smsc = null;
 	ISmsCompanyService smscService = (ISmsCompanyService)SpringContextUtil.getBean("smsCompanyService");
 	if (obj instanceof Company){
 	    smsc = smscService.getSMSCompanyByComID(((Company)obj).getComId());
 	}
 	if (obj instanceof Project){
 	    Integer comId = ((Project)obj).getCompany().getComId();
 	    smsc = smscService.getSMSCompanyByComID(comId);
 	}
 	/* By default, the system level users related SMSCompany's ID is 1 */
 	if (refDomain.equals("系统层")){
 	    smsc = smscService.getSMSCompanyByID(1);
 	}
 	session.put("smsc", smsc);
 	
 	/* get user related moduleList */
 	IModuleService moduleService = (IModuleService)SpringContextUtil.getBean("moduleService");
 	List<?> moduleList = moduleService.getModuleListByRoleID(role.getRoleId());
 	session.put("moduleList", moduleList);
     }
     
     public static TbUser getUser(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	return (TbUser)session.get("user");
     }
 
     public static TbGroup getUserGroup(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	return (TbGroup)session.get("group");
     }
     
     public static TbRole getUserRole(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	return (TbRole)session.get("role");
     }
     
     public static Object getUserRefDomain(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	return session.get("refDomain");
     }
     
     public static SMSCompany getSMSCompany(){
 	Map<String,Object> session = ServletActionContext.getContext().getSession();
 	return (SMSCompany)session.get("smsc");
     }
     
     public static SMSCompany getSMSCompany(Integer userId){
 	SMSCompany smsc = null;
 	IUserGroupRoleService ugrService = (IUserGroupRoleService)SpringContextUtil.getBean("ugrService");
 	ISmsCompanyService smscService = (ISmsCompanyService)SpringContextUtil.getBean("smsCompanyService");
 	TbUserGroupRole ugr = ugrService.getUGR_ByUserID(userId);
 	String refDomain = ugr.getTbGroup().getRefDomain();
 	Integer groupLevel = ugr.getTbGroup().getGroupLevel();
 	if(groupLevel == 2){
 	    ICompanyService companyService = (ICompanyService)SpringContextUtil.getBean("companyService");
 	    Company com = companyService.getCompanyByName(refDomain);
 	    smsc = smscService.getSMSCompanyByComID(com.getComId());
 	} else if(groupLevel == 3){
 	    IProjectService projectService = (IProjectService)SpringContextUtil.getBean("projectService");
 	    Project pro = projectService.getProjectByName(refDomain);
 	    Integer comId = pro.getCompany().getComId();
 	    smsc = smscService.getSMSCompanyByComID(comId);
 	}
 	return smsc;
     }
 }
