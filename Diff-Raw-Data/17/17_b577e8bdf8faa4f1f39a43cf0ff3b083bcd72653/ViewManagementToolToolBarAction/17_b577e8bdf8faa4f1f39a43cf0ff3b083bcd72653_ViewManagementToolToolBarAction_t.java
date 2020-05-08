 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.cms.applications.common.ImageButton;
 import org.infoglue.cms.controllers.kernel.impl.simple.AvailableServiceBindingController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.InterceptionPointController;
 import org.infoglue.cms.controllers.kernel.impl.simple.InterceptorController;
 import org.infoglue.cms.controllers.kernel.impl.simple.LanguageController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RolePropertiesController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ServiceDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserPropertiesController;
 import org.infoglue.cms.entities.management.InterceptionPointVO;
 import org.infoglue.cms.treeservice.ss.ManagementNodeImpl;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.net.URLEncoder;
 
 /**
  * This class implements the action class for the framed page in the management tool.
  * 
  * @author Mattias Bogeblad  
  */
 
 public class ViewManagementToolToolBarAction extends WebworkAbstractAction
 {
 	private String title = "";
 	private String name  = "";
 	private String toolbarKey = "";
 	private String url   = "";
 	
 	//All id's that are used
 	private Integer repositoryId = null;
 	private String userName = null;
 	private Integer extranetUserId = null;
 	private String roleName = null;
 	private Integer extranetRoleId = null;
 	private Integer languageId = null;
 	private Integer functionId = null;
 	private Integer serviceDefinitionId = null;
 	private Integer availableServiceBindingId = null;
 	private Integer siteNodeTypeDefinitionId = null;
 	private Integer contentTypeDefinitionId = null;
 	private Integer interceptionPointId = null;
 	private Integer interceptorId = null;
 	private Integer categoryId = null;
 	
 	private String URIEncoding = CmsPropertyHandler.getProperty("URIEncoding");
 	
 	private InterceptionPointVO interceptionPointVO = null;
 	
 	private static HashMap buttonsMap = new HashMap();
 		
 	public String doExecute() throws Exception
     {
     	if(this.interceptionPointId != null)
 	    	this.interceptionPointVO = InterceptionPointController.getController().getInterceptionPointVOWithId(this.interceptionPointId);
     	
         return "success";
     }
     
 	public Integer getInterceptionPointId()
 	{
 		return this.interceptionPointId;
 	}
 
 	public void setInterceptionPointId(Integer interceptionPointId)
 	{
 		this.interceptionPointId = interceptionPointId;
 	}
 
 	public Integer getInterceptorId() 
 	{
 		return this.interceptorId;
 	}
 	
 	public void setInterceptorId(Integer interceptorId) 
 	{
 		this.interceptorId = interceptorId;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return this.repositoryId;
 	}                   
 
 	public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
 	public Integer getExtranetUserId()
 	{
 		return extranetUserId;
 	}
 
 	public void setExtranetUserId(Integer integer)
 	{
 		extranetUserId = integer;
 	}
 
 	public Integer getLanguageId()
 	{
 		return this.languageId;
 	}                   
 
 	public void setLanguageId(Integer languageId)
 	{
 		this.languageId = languageId;
 	}
 
 	public String getRoleName()
 	{
 		return this.roleName;
 	}                   
 
 	public void setRoleName(String roleName)
 	{
 		this.roleName = roleName;
 	}
 
 	public Integer getFunctionId()
 	{
 		return this.functionId;
 	}                   
 
 	public void setFunctionId(Integer functionId)
 	{
 		this.functionId = functionId;
 	}
 
 	public Integer getServiceDefinitionId()
 	{
 		return this.serviceDefinitionId;
 	}                   
 
 	public void setServiceDefinitionId(Integer serviceDefinitionId)
 	{
 		this.serviceDefinitionId = serviceDefinitionId;
 	}
 
 	public Integer getAvailableServiceBindingId()
 	{
 		return this.availableServiceBindingId;
 	}                   
 
 	public void setAvailableServiceBindingId(Integer availableServiceBindingId)
 	{
 		this.availableServiceBindingId = availableServiceBindingId;
 	}
 
 	public Integer getSiteNodeTypeDefinitionId()
 	{
 		return this.siteNodeTypeDefinitionId;
 	}                   
 
 	public void setSiteNodeTypeDefinitionId(Integer siteNodeTypeDefinitionId)
 	{
 		this.siteNodeTypeDefinitionId = siteNodeTypeDefinitionId;
 	}
 
 	public Integer getContentTypeDefinitionId()
 	{
 		return this.contentTypeDefinitionId;
 	}                   
 
 	public void setContentTypeDefinitionId(Integer contentTypeDefinitionId)
 	{
 		this.contentTypeDefinitionId = contentTypeDefinitionId;
 	}
 
 	public Integer getCategoryId()			{ return categoryId; }
 	public void setCategoryId(Integer i)	{ categoryId = i; }
 
 	public String getTitle()
 	{
 		return this.title;
 	}                   
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}                   
 	
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	
 	public String getToolbarKey()
 	{
 		return this.toolbarKey;
 	}                   
 
 	public void setToolbarKey(String toolbarKey)
 	{
 		this.toolbarKey = toolbarKey;
 	}
 
 	public void setUrl(String url)
 	{
 		this.url = url;
 	}
 	
 	public String getUrl()
 	{
 		return this.url;
 	}
 
 	public Integer getExtranetRoleId()
 	{
 		return extranetRoleId;
 	}
 
 	public void setExtranetRoleId(Integer extranetRoleId)
 	{
 		this.extranetRoleId = extranetRoleId;
 	}
 
 	public String getUserName()
 	{
 		return userName;
 	}
 
 	public void setUserName(String userName)
 	{
 		this.userName = userName;
 	}
 
 	public List getButtons()
 	{
 		CmsLogger.logInfo("Title:" + this.title);
 		CmsLogger.logInfo("toolbarKey:" + this.toolbarKey);
 
 		try
 		{
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.repositoryList.header"))
 				return getRepositoriesButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewRepository.header"))
 				return getRepositoryDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUserList.header"))
 				return getSystemUsersButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewSystemUser.header"))
 				return getSystemUserDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewRoleList.header"))
 				return getRolesButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewRole.header"))
 				return getRoleDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguageList.header"))
 				return getLanguagesButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewLanguage.header"))
 				return getLanguageDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPointList.header"))
 				return getInterceptionPointsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptionPoint.header"))
 				return getInterceptionPointButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptorList.header"))
 				return getInterceptorsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewInterceptor.header"))
 				return getInterceptorButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinitionList.header"))
 				return getServiceDefinitionsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewServiceDefinition.header"))
 				return getServiceDefinitionDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBindingList.header"))
 				return getAvailableServiceBindingsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewAvailableServiceBinding.header"))
 				return getAvailableServiceBindingDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinitionList.header"))
 				return getSiteNodeTypeDefinitionsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewSiteNodeTypeDefinition.header"))
 				return getSiteNodeTypeDefinitionDetailsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinitionList.header"))
 				return getContentTypeDefinitionsButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewContentTypeDefinition.header"))
 				return getContentTypeDefinitionDetailsButtons();
			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewCategoryList.header") || this.toolbarKey.equalsIgnoreCase("tool.managementtool.editCategory.header"))
 				return getCategoryButtons();
 			if(this.toolbarKey.equalsIgnoreCase("tool.managementtool.viewUp2DateList.header"))
 				return getAvailablePackagesButtons();
 
 		}
 		catch(Exception e) {}			
 					
 		return null;				
 	}
 	
 
 	private List getRepositoriesButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateRepository!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newRepository"), "tool.managementtool.createRepository.header"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('repository');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteRepository"), "tool.managementtool.deleteRepositories.header"));
 		buttons.add(new ImageButton(true, "javascript:openPopup('ImportRepository!input.action', 'Import', 'width=400,height=250,resizable=no');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.importRepository"), getLocalizedString(getSession().getLocale(), "tool.managementtool.importRepository.header")));	
 		
 		return buttons;
 	}
 	
 	private List getRepositoryDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteRepository.header&yesDestination=" + URLEncoder.encode("DeleteRepository.action?repositoryId=" + this.repositoryId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListRepository.action?title=Repositories", "UTF-8") + "&message=tool.managementtool.deleteRepository.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteRepository"), "tool.managementtool.deleteRepository.header"));
 		buttons.add(new ImageButton(true, "javascript:openPopup('ExportRepository!input.action?repositoryId=" + this.repositoryId + "', 'Export', 'width=400,height=200,resizable=no');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.exportRepository"), getLocalizedString(getSession().getLocale(), "tool.managementtool.exportRepository.header")));	
 		buttons.add(new ImageButton("ViewRepositoryProperties.action?repositoryId=" + this.repositoryId, getLocalizedString(getSession().getLocale(), "images.global.buttons.editProperties"), "Edit Properties", new Integer(22), new Integer(80)));
 		
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewRepository.action?repositoryId=" + this.repositoryId, "UTF-8"), "UTF-8");
 		buttons.add(new ImageButton("ViewAccessRights.action?interceptionPointCategory=Repository&extraParameters=" + this.repositoryId +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header"));
 		buttons.add(new ImageButton("ViewListRepositoryLanguage.action?repositoryId=" + this.repositoryId +"&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.repositoryLanguages"), "tool.managementtool.repositoryLanguages.header"));
 		
 		return buttons;				
 	}
 
 	private List getAvailablePackagesButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("RefreshUpdates.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.refreshUpdates"), "Refresh Updates"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('updatePackage');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.installUpdate"), "Install update"));
 		return buttons;
 	}
 	
 	private List getSystemUsersButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		if(UserControllerProxy.getController().getSupportCreate())
 			buttons.add(new ImageButton("CreateSystemUser!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newSystemUser"), "New System User"));	
 		if(UserControllerProxy.getController().getSupportDelete())
 			buttons.add(new ImageButton(true, "javascript:submitListFormWithPrimaryKey('systemUser', 'userName');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteSystemUser"), "tool.managementtool.deleteSystemUsers.header"));
 		buttons.add(new ImageButton(true, "javascript:toggleSearchForm();", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.searchButton"), "Search Form"));
 		return buttons;
 	}
 
 	private List getSystemUserDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		if(UserControllerProxy.getController().getSupportDelete())
 			buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteSystemUser.header&yesDestination=" + URLEncoder.encode("DeleteSystemUser.action?userName=" + URLEncoder.encode(this.userName, URIEncoding), URIEncoding) + "&noDestination=" + URLEncoder.encode("ViewListSystemUser.action?title=SystemUsers", URIEncoding) + "&message=tool.managementtool.deleteSystemUser.text&extraParameters=" + URLEncoder.encode(this.userName, URIEncoding), getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteSystemUser"), "tool.managementtool.deleteSystemUser.header"));
 		
 		List contentTypeDefinitionVOList = UserPropertiesController.getController().getContentTypeDefinitionVOList(this.userName);
 		if(contentTypeDefinitionVOList.size() > 0)
 			buttons.add(new ImageButton("ViewUserProperties.action?userName=" + URLEncoder.encode(URLEncoder.encode(this.userName, URIEncoding), URIEncoding), getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.viewSystemUserProperties"), "View User Properties"));
 		return buttons;				
 	}
 
 	private List getRolesButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		if(UserControllerProxy.getController().getSupportCreate())
 			buttons.add(new ImageButton("CreateRole!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newRole"), "New Role"));	
 		if(UserControllerProxy.getController().getSupportDelete())
 			buttons.add(new ImageButton(true, "javascript:submitListFormWithPrimaryKey('role', 'roleName');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteRole"), "tool.managementtool.deleteRoles.header"));
 		
 		return buttons;
 	}
 	
 	private List getRoleDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		
 		String yesDestination 	= URLEncoder.encode("DeleteRole.action?roleName=" + URLEncoder.encode(this.roleName, URIEncoding), URIEncoding);
 		String noDestination  	= URLEncoder.encode("ViewListRole.action?title=Roles", URIEncoding);
 		String message 		 	= URLEncoder.encode("Do you really want to delete the role " + URLEncoder.encode(this.roleName, URIEncoding), URIEncoding);
 		//System.out.println("yesDestination:" + yesDestination);
 		//System.out.println("noDestination:" + noDestination);
 		//System.out.println("message:" + message);
 		
 		if(UserControllerProxy.getController().getSupportDelete())
 			buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteRole.header&yesDestination=" + yesDestination + "&noDestination=" + noDestination + "&message=tool.managementtool.deleteRole.text&extraParameters=" + URLEncoder.encode(this.roleName, URIEncoding), getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteRole"), "tool.managementtool.deleteRole.header"));
 		
 		List contentTypeDefinitionVOList = RolePropertiesController.getController().getContentTypeDefinitionVOList(this.roleName);
 		if(contentTypeDefinitionVOList.size() > 0)
 			buttons.add(new ImageButton("ViewRoleProperties.action?roleName=" + URLEncoder.encode(URLEncoder.encode(this.roleName, URIEncoding)), getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.viewRoleProperties"), "View Role Properties"));
 		
 		return buttons;				
 	}
 
 	private List getLanguagesButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateLanguage!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newLanguage"), "New Language"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('language');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguages.header"));
 		return buttons;
 	}
 	
 	private List getLanguageDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = LanguageController.getController().getLanguageVOWithId(this.languageId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteLanguage.header&yesDestination=" + URLEncoder.encode("DeleteLanguage.action?languageId=" + this.languageId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListLanguage.action?title=Languages", "UTF-8") + "&message=tool.managementtool.deleteLanguage.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteLanguage"), "tool.managementtool.deleteLanguage.header"));
 		return buttons;				
 	}
 
 	private List getInterceptionPointsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateInterceptionPoint!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newInterceptionPoint"), "New InterceptionPoint"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('interceptionPoint');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoints.header"));
 		return buttons;
 	}
 	
 	private List getInterceptionPointButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = InterceptionPointController.getController().getInterceptionPointVOWithId(this.interceptionPointId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteInterceptionPoint.header&yesDestination=" + URLEncoder.encode("DeleteInterceptionPoint.action?interceptionPointId=" + this.interceptionPointId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListInterceptionPoint.action?title=InterceptionPoints", "UTF-8") + "&message=tool.managementtool.deleteInterceptionPoint.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteInterceptionPoint"), "tool.managementtool.deleteInterceptionPoint.header"));
 		if(this.interceptionPointVO.getUsesExtraDataForAccessControl().booleanValue() == false)
 			buttons.add(new ImageButton("ViewAccessRights.action?interceptionPointCategory=" + this.interceptionPointVO.getCategory() + "&interceptionPointId=" + this.interceptionPointId + "&returnAddress=ViewInterceptionPoint.action?interceptionPointId=" + this.interceptionPointId + "&colorScheme=ManagementTool", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.accessRights"), "InterceptionPoint Access Rights"));
 		
 		return buttons;				
 	}
 
 	private List getInterceptorsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateInterceptor!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newInterceptor"), "New Interceptor"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('interceptor');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteInterceptor"), "tool.managementtool.deleteInterceptors.header"));
 		return buttons;
 	}
 	
 	private List getInterceptorButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = InterceptorController.getController().getInterceptorVOWithId(this.interceptorId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteInterceptor.header&yesDestination=" + URLEncoder.encode("DeleteInterceptor.action?interceptorId=" + this.interceptorId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListInterceptor.action?title=Interceptors", "UTF-8") + "&message=tool.managementtool.deleteInterceptor.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteInterceptor"), "tool.managementtool.deleteInterceptor.header"));
 		return buttons;				
 	}
 
 	private List getServiceDefinitionsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateServiceDefinition!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newServiceDefinition"), "New ServiceDefinition"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('serviceDefinition');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteServiceDefinition"), "tool.managementtool.deleteServiceDefinitions.header"));
 		return buttons;
 	}
 	
 	private List getServiceDefinitionDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = ServiceDefinitionController.getController().getServiceDefinitionVOWithId(this.serviceDefinitionId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteServiceDefinition.header&yesDestination=" + URLEncoder.encode("DeleteServiceDefinition.action?serviceDefinitionId=" + this.serviceDefinitionId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListServiceDefinition.action?title=ServiceDefinitions", "UTF-8") + "&message=tool.managementtool.deleteServiceDefinition.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteServiceDefinition"), "tool.managementtool.deleteServiceDefinition.header"));
 		return buttons;				
 	}
 
 	private List getAvailableServiceBindingsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateAvailableServiceBinding!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newAvailableServiceBinding"), "New AvailableServiceBinding"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('availableServiceBinding');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteAvailableServiceBinding"), "tool.managementtool.deleteAvailableServiceBindings.header"));
 		return buttons;
 	}
 	
 	private List getAvailableServiceBindingDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = AvailableServiceBindingController.getController().getAvailableServiceBindingVOWithId(this.availableServiceBindingId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteAvailableServiceBinding.header&yesDestination=" + URLEncoder.encode("DeleteAvailableServiceBinding.action?availableServiceBindingId=" + this.availableServiceBindingId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListAvailableServiceBinding.action?title=AvailableServiceBindings", "UTF-8") + "&message=tool.managementtool.deleteAvailableServiceBinding.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteAvailableServiceBinding"), "tool.managementtool.deleteAvailableServiceBinding.header"));
 		return buttons;				
 	}
 
 	private List getSiteNodeTypeDefinitionsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateSiteNodeTypeDefinition!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newSiteNodeTypeDefinition"), "New SiteNodeTypeDefinition"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('siteNodeTypeDefinition');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteSiteNodeTypeDefinition"), "tool.managementtool.deleteSiteNodeTypeDefinitions.header"));
 		return buttons;
 	}
 	
 	private List getSiteNodeTypeDefinitionDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = SiteNodeTypeDefinitionController.getController().getSiteNodeTypeDefinitionVOWithId(this.siteNodeTypeDefinitionId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteSiteNodeTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteSiteNodeTypeDefinition.action?siteNodeTypeDefinitionId=" + this.siteNodeTypeDefinitionId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListSiteNodeTypeDefinition.action?title=SiteNodeTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteSiteNodeTypeDefinition.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteSiteNodeTypeDefinition"), "tool.managementtool.deleteSiteNodeTypeDefinition.header"));
 		return buttons;				
 	}
 
 
 	private List getContentTypeDefinitionsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton("CreateContentTypeDefinition!input.action", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newContentTypeDefinition"), "New ContentTypeDefinition"));	
 		buttons.add(new ImageButton(true, "javascript:submitListForm('contentTypeDefinition');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteContentTypeDefinition"), "tool.managementtool.deleteContentTypeDefinitions.header"));
 		return buttons;
 	}
 	
 	private List getContentTypeDefinitionDetailsButtons() throws Exception
 	{
 		List buttons = new ArrayList();
 		this.name = ContentTypeDefinitionController.getController().getContentTypeDefinitionVOWithId(this.contentTypeDefinitionId).getName();
 		buttons.add(new ImageButton("Confirm.action?header=tool.managementtool.deleteContentTypeDefinition.header&yesDestination=" + URLEncoder.encode("DeleteContentTypeDefinition.action?contentTypeDefinitionId=" + this.contentTypeDefinitionId, "UTF-8") + "&noDestination=" + URLEncoder.encode("ViewListContentTypeDefinition.action?title=ContentTypeDefinitions", "UTF-8") + "&message=tool.managementtool.deleteContentTypeDefinition.text&extraParameters=" + this.name, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteContentTypeDefinition"), "tool.managementtool.deleteContentTypeDefinition.header"));
 		buttons.add(getAccessRightsButton());
 		return buttons;				
 	}
 
 	private List getCategoryButtons() throws Exception
 	{
 		String url = "CategoryManagement!new.action";
 		if(getCategoryId() != null)
 			url += "?model/parentId=" + getCategoryId();
 
 		List buttons = new ArrayList();
 		buttons.add(new ImageButton(url, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.newCategory"), "New Category"));
 
 		if(getCategoryId() != null)
 			buttons.add(new ImageButton(true, "javascript:openPopup('CategoryManagement!displayTreeForMove.action?categoryId=" + getCategoryId() + "', 'Category', 'width=400,height=600,resizable=no,status=yes');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.moveCategory"), "Move Category"));
 
 		buttons.add(new ImageButton(true, "javascript:submitListForm('category');", getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.deleteCategory"), "Delete Category"));
 		return buttons;
 	}
 	
 	private ImageButton getAccessRightsButton() throws Exception
 	{
 		String returnAddress = URLEncoder.encode(URLEncoder.encode("ViewContentTypeDefinition.action?contentTypeDefinitionId=" + this.contentTypeDefinitionId, "UTF-8"), "UTF-8");
 		return new ImageButton("ViewAccessRights.action?interceptionPointCategory=ContentTypeDefinition&extraParameters=" + this.contentTypeDefinitionId +"&colorScheme=ManagementTool&returnAddress=" + returnAddress, getLocalizedString(getSession().getLocale(), "images.managementtool.buttons.accessRights"), "tool.managementtool.accessRights.header");
 	}
 
 }
