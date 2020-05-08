 package com.xlthotel.core.admin.controller;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 
 
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.xlthotel.core.admin.orm.entity.AdminUserGroup;
 import com.xlthotel.core.admin.orm.entity.AdminUserInfo;
 import com.xlthotel.core.admin.orm.entity.Role;
 import com.xlthotel.core.admin.service.AdminPermissionService;
 import com.xlthotel.core.admin.service.AdminUserGroupService;
 import com.xlthotel.core.admin.service.AdminUserInfoService;
 import com.xlthotel.core.admin.service.HotelService;
 import com.xlthotel.core.admin.service.RoleService;
 import com.xlthotel.foundation.common.Condition;
 import com.xlthotel.foundation.common.Constant;
 import com.xlthotel.foundation.common.DateUtils;
 import com.xlthotel.foundation.common.MD5Utils;
 import com.xlthotel.foundation.common.PageOrder;
 import com.xlthotel.foundation.common.SimpleOrderImpl;
 
 import com.xlthotel.foundation.common.SimpleConditionImpl;
 
 import com.xlthotel.foundation.common.SimpleServletRequestUtils;
 import com.xlthotel.foundation.exception.XltHotelException;
 import com.xlthotel.foundation.orm.entity.Hotel;
 
 
 @Controller
 @RequestMapping("/servlet/admin/useinfo.do")
 public class AdminUserinfoSetController {
 
 	@Autowired
 	private AdminUserInfoService adminUserInfoService;
 	@Autowired
 	private AdminPermissionService adminPermissionService;
 	@Autowired
 	RoleService oleService;
 	@Autowired
 	AdminUserGroupService adminUserGroupService;
 	@Autowired
 	private HotelService hotelService;
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=permissonList")
 	public ModelAndView permissonList(Model model) {
 
 		List listPerMain = this.adminPermissionService.findPermissionsByTypeId(0);
 		List listPer = this.adminPermissionService.findPermissionsByTypeId(1);
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		returnModel.put("listPer", listPer);
 		returnModel.put("listPerMain", listPerMain);
 		return new ModelAndView("/userinfo/permission", returnModel);
 	}
 
 	/***************************************************************************************************************************/
 	@RequestMapping(method = RequestMethod.GET, params = "method=roleList")
 	public ModelAndView roleList(HttpServletRequest request,
 			HttpServletResponse response, Model model) {
 		String error_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "error_msg","");
 		String succeed_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "succeed_msg","");
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		if (!error_msg.isEmpty()) {
 			returnModel.put("error_msg", error_msg);
 		}
 		if (!succeed_msg.isEmpty()) {
 			returnModel.put("succeed_msg", succeed_msg);
 		}
 		List listRole = this.oleService.findRolesAll();
 		List permissionMainList = this.adminPermissionService
 				.findPermissionsByTypeId(0);
 		List permissionList = this.adminPermissionService
 		.findPermissionsByTypeId(1);
 		returnModel.put("listRole", listRole);
 		returnModel.put("permissionList", permissionList);
 		returnModel.put("permissionMainList", permissionMainList);
 		return new ModelAndView("/userinfo/roleList", returnModel);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveRoleList")
 	public ModelAndView saveRoleList(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 		String error_msg = "";
 		String succeed_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "roleId","");
 			String roleName = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "roleName","");
 			String rolePerssion[] = SimpleServletRequestUtils.getStringParameters(request, "box_per");
 			if (roleName == null || roleName.equals("")) {
 				error_msg = "角色名称不能为空,请重新操作.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView("redirect:/userinfo/roleList",
 						returnModel);
 			}
 			if (rolePerssion == null || rolePerssion.length == 0) {
 				error_msg = "请选择角色对应的权限,请重新操作.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=roleList",
 						returnModel);
 			}
 			Role role = new Role();
 			role.setTypeId(1);// 非系统用户 系统用户为1 默认不允许删除
 			if (id != null && id.length() != 0) {
 				role = this.oleService.findRoleByID(id);
 			}
 			role.setRoleName(roleName);
 			role.setPermissions(this.setPermissionListToLong(Arrays
 					.asList(rolePerssion)));
 			Role role_save = this.oleService.saveRole(role);
 			if (role_save != null) {
 				succeed_msg = "保存角色信息成功.";
 				returnModel.put("succeed_msg", succeed_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=roleList",
 						returnModel);
 			} else {
 				error_msg = "保存角色信息失败,请稍后重试.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=roleList",
 						returnModel);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			error_msg = "保存角色信息失败,请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=roleList",
 					returnModel);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=intEditRole")
 	public ModelAndView intEditRole(HttpServletRequest request,
 			HttpServletResponse response) {
 		String error_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String id = request.getParameter("roleId");
 			Role role = new Role();
 			if (id == null || id.length() == 0) {
 				returnModel.put("error_msg", "初始化角色信息失败:请稍后重试.");
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=roleList",
 						returnModel);
 			}
 			role = this.oleService.findRoleByID(id);
 			role.setId(id);
 			List permissionMainList = this.adminPermissionService.findPermissionsByTypeId(0);
 			List permissionList = this.adminPermissionService.findPermissionsByTypeId(1);
 			returnModel.put("permissionList", permissionList);
 			returnModel.put("permissionMainList", permissionMainList);
 			returnModel.put("role", role);
 			return new ModelAndView("/userinfo/roleEdit", returnModel);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			error_msg = "初始化角色信息失败:请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=roleList",
 					returnModel);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=delRole")
 	public void delRole(HttpServletRequest request, HttpServletResponse response)
 			throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String id = request.getParameter("id");
 			Role role = this.oleService.findRoleByID(id);
 			if (role != null) {
 				if (role.getTypeId() < 1) {
 					response.getWriter().print("C");
 				} else {
 					this.oleService.removeRole(role);
 					response.getWriter().print("Y");
 				}
 			} else {
 				response.getWriter().print("N");
 			}
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除角色信息出现异常。", e);
 		}
 
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=checkRole")
 	public void checkRole(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String roleId = SimpleServletRequestUtils.getStringParameter(
 					request, "roleId", "");
 			String html = "";
 			PrintWriter out = response.getWriter();
 
 			if (roleId.isEmpty()) {
 				html = "<div><table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" bgcolor=\"#eaeaea\" align=\"center\" width=\"100%\"><tbody><tr height=\"40\"></tr><tr><td class=\"package_td\" height=\"40\" colspan=\"2\">系统异常，角色公告ID缺失，请联系管理员。</td></tr></tbody></table></div>";
 
 			}
 
 			Role role = this.oleService.findRoleByID(roleId);
 			if (role.getPermissions() != null) {
 				
 				String htmlConten = "";
 				String htmlPre = "<div><table border=\"0\" cellpadding=\"0\" cellspacing=\"1\" bgcolor=\"#eaeaea\" align=\"center\" width=\"100%\"><tbody><tr><td class=\"package_td\" height=\"40\" colspan=\"2\">";
 				for(int i = 0;i<role.getPermissions().size();i++){
 					
 					if(i==role.getPermissions().size()-1){
 						htmlConten +=role.getPermissions().get(i);
 					}else{
 						htmlConten +=role.getPermissions().get(i) + ",";
 					}
 					if(i>0&&(i+1)%6==0){htmlConten += "<br/>";}
 				}
 				String htmlNext = "</td></tr></tbody></table></div>";
 				out.println(htmlPre+htmlConten+htmlNext);
 				out.flush();
 				out.close();
 			}else{
 				out.println(html);
 				out.flush();
 				out.close();
 			}
 			
 			
 			
 		} catch (Exception e) {
 			throw new XltHotelException("<li>查看公告出现异常。", e);
 		}
 	}
 
 	/***************************************************************************************************************************/
 	@RequestMapping(method = RequestMethod.GET, params = "method=groupList")
 	public ModelAndView groupList(HttpServletRequest request,
 			HttpServletResponse response) {
 		String error_msg = request.getParameter("error_msg");
 		String succeed_msg = request.getParameter("succeed_msg");
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		if (error_msg != null && !error_msg.isEmpty()) {
 			returnModel.put("error_msg", error_msg);
 		}
 		if (succeed_msg != null && !succeed_msg.isEmpty()) {
 			returnModel.put("succeed_msg", succeed_msg);
 		}
 		List listGroup = this.adminUserGroupService.findAdminUserGroupsAll();
 		List listRole = this.oleService.findRolesAll();
 		returnModel.put("listGroup", listGroup);
 		returnModel.put("listRole", listRole);
 		return new ModelAndView("/userinfo/groupList", returnModel);
 	}
 
 	@SuppressWarnings("unchecked")
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveGroupList")
 	public ModelAndView saveGroupList(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 		String error_msg = "";
 		String succeed_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String id = request.getParameter("groupId");
 			String groupName = request.getParameter("groupName");
 			String groupDesc = request.getParameter("groupDesc");
 			String groupRole[] = request.getParameterValues("box_per");
 			if (groupName == null || groupName.equals("")) {
 				error_msg = "用户组名称不能为空,请重新操作.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView("redirect:/userinfo/groupList",
 						returnModel);
 			}
 			if (groupRole == null || groupRole.length == 0) {
 				error_msg = "请选择用户组对应的角色,请重新操作.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=groupList",
 						returnModel);
 			}
 			AdminUserGroup ug = new AdminUserGroup();
 			ug.setTypeID(1);// 非系统用户 系统用户为1 默认不允许删除
 			if (id != null && id.length() != 0) {
 				ug = this.adminUserGroupService.findAdminUserGroupByID(id);
 			}
 			ug.setGroupName(groupName);
 			ug.setGroupDesc(groupDesc);
 			ug.getRoles().clear();
 			for (String roleId : Arrays.asList(groupRole)) {
 				ug.getRoles().add(this.oleService.findRoleByID(roleId));
 			}
 			AdminUserGroup ug_save = this.adminUserGroupService
 					.saveAdminUserGroup(ug);
 			if (ug_save != null) {
 				succeed_msg = "保存用户组信息成功.";
 				returnModel.put("succeed_msg", succeed_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=groupList",
 						returnModel);
 			} else {
 				error_msg = "保存用户组信息失败,请稍后重试.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=groupList",
 						returnModel);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			error_msg = "保存用户组信息失败,请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=groupList",
 					returnModel);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=intEditGroup")
 	public ModelAndView intEditGroup(HttpServletRequest request,
 			HttpServletResponse response) {
 		String error_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String id = request.getParameter("groupId");
 			AdminUserGroup group = new AdminUserGroup();
 			if (id == null || id.length() == 0) {
 				returnModel.put("error_msg", "初始化用户组信息失败:请稍后重试.");
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=groupList",
 						returnModel);
 			}
 			group = this.adminUserGroupService.findAdminUserGroupByID(id);
 			group.setId(id);
 			List roleList = this.oleService.findRolesAll();
 			returnModel.put("roleList", roleList);
 			returnModel.put("group", group);
 			return new ModelAndView("/userinfo/groupEdit", returnModel);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			error_msg = "初始化用户组信息失败:请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=roleList",
 					returnModel);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=delGroup")
 	public void delGroup(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String id = request.getParameter("id");
 			AdminUserGroup group = this.adminUserGroupService
 					.findAdminUserGroupByID(id);
 
 			if (group != null) {
 				if (group.getTypeID() == 0) {
 					response.getWriter().print("C");
 				} else {
 					this.adminUserGroupService.removeAdminUserGroup(group);
 					response.getWriter().print("Y");
 				}
 			} else {
 				response.getWriter().print("N");
 			}
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除用户组信息出现异常。", e);
 		}
 
 	}
 
 	private List<Long> setPermissionListToLong(List<String> list) {
 		List<Long> listLong = new ArrayList<Long>();
 		for (String i : list) {
 			listLong.add(Long.valueOf(i));
 		}
 		// this.setPermissionValues(permissionList);
 		return listLong;
 	}
 
 	/***************************************************************************************************************************/
 	@RequestMapping(method = RequestMethod.GET, params = "method=userList")
 	public ModelAndView userList(HttpServletRequest request,
 			HttpServletResponse response) {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String succeed_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "succeed_msg","");
 		if (!succeed_msg.isEmpty()) {
 			returnModel.put("succeed_msg", succeed_msg);
 		}
 		Condition condition = new SimpleConditionImpl();
 		condition.setEntityName("AdminUserInfo");
 		List<AdminUserInfo> listuser = this.adminUserInfoService
 				.findUserInfos(condition);
 		List groupList = this.adminUserGroupService.findAdminUserGroupsAll();
 		returnModel.put("groupList", groupList);
 		returnModel.put("listuser", listuser);
 		return new ModelAndView("/userinfo/userList", returnModel);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=userSearch")
 	public ModelAndView userSearch(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 		try {
 			Map<String, Object> returnModel = new HashMap<String, Object>();
 			String userId = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "userId", "");
 			String groupId = (String) SimpleServletRequestUtils
 			.getStringParameter(request, "groupId", "");
 			String userName = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "userName", "");
 			String regdate_1 = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "regdate_1", "");
 			String regdate_2 = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "regdate_2", "");
 			String status = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "status", "");
 			String page = (String) SimpleServletRequestUtils
 					.getStringParameter(request, "page", "");
 			Condition condition = new SimpleConditionImpl();
 			if (StringUtils.isNotBlank(userId)) {
 				condition.putCondition("userId", "like", "userId", userId);
 			}
 			if (StringUtils.isNotBlank(userName)) {
 				condition
 						.putCondition("userName", "like", "userName", userName);
 			}
 			if (StringUtils.isNotBlank(status)) {
 				condition.putCondition("status", "=", "status",
 						Integer.parseInt(status));
 			}
 			if (StringUtils.isNotBlank(groupId)) {
 				condition.putCondition("groupId", "=", "groupId",
 						groupId);
 			}
 			if (StringUtils.isNotBlank(regdate_2)) {
 				condition.putCondition(
 						"regTime",
 						"<",
 						"regTime2",
 						DateUtils.parse("yyyy-MM-dd hh:mm:ss", regdate_2
 								+ " 00:00:00"));
 			}
 			if (StringUtils.isNotBlank(regdate_1)) {
 				condition.putCondition(
 						"regTime",
 						">",
 						"regTime1",
 						DateUtils.parse("yyyy-MM-dd hh:mm:ss", regdate_1
 								+ " 00:00:00"));
 			}
 			condition.setEntityName("AdminUserInfo");
 			List<AdminUserInfo> listuser = this.adminUserInfoService
 					.findUserInfos(condition);
 			List groupList = this.adminUserGroupService.findAdminUserGroupsAll();
 			returnModel.put("groupList", groupList);
 			returnModel.put("listuser", listuser);
 			returnModel.put("userId", userId);
 			returnModel.put("selectGroup", groupId);
 			returnModel.put("userName", userName);
 			returnModel.put("regdate_1", regdate_1);
 			returnModel.put("regdate_2", regdate_2);
 			returnModel.put("status", status);
 			returnModel.put("page", page);
 			return new ModelAndView("/userinfo/userList", returnModel);
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>查询出现异常。", e);
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=delUser")
 	public void delUser(HttpServletRequest request, HttpServletResponse response)
 			throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "userId","");
 
 			AdminUserInfo aui = this.adminUserInfoService.findUserInfoById(id);
 			if (aui != null) {
 				if (aui.getTypeId() == 0) {
 					response.getWriter().print("C");
 				} else {
 					this.adminUserInfoService.removeAdminUserInfo(aui);
 					response.getWriter().print("Y");
 				}
 			} else {
 				response.getWriter().print("N");
 			}
 
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除用户组信息出现异常。", e);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=lockUser")
 	public void lockUser(HttpServletRequest request,
 			HttpServletResponse response) throws XltHotelException {
 
 		try {
 			response.setHeader("Pragma", "No-cache");
 			response.setHeader("Cache-Control", "no-cache");
 			response.setDateHeader("Expires", 0);
 			response.setContentType("text/html;charset=utf-8");
 			String id = (String) SimpleServletRequestUtils.getStringParameter(
 					request, "userId","");
 
 			AdminUserInfo aui = this.adminUserInfoService.findUserInfoById(id);
 			if (aui != null) {
 				if (aui.getTypeId() == 0) {
 					response.getWriter().print("C");
 				} else {
 					if (aui.getStatus() == 0) {
 						aui.setStatus(1);
 						this.adminUserInfoService.saveAdminUserInfo(aui);
 						response.getWriter().print("Y,1");
 					} else {
 						aui.setStatus(0);
 						this.adminUserInfoService.saveAdminUserInfo(aui);
 						response.getWriter().print("Y,0");
 					}
 
 				}
 			} else {
 				response.getWriter().print("N");
 			}
 
 			PrintWriter out = response.getWriter();
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new XltHotelException("<li>删除用户组信息出现异常。", e);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=intViewUser")
 	public ModelAndView intViewUser(HttpServletRequest request,
 			HttpServletResponse response) {
 		String error_msg = "";
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		try {
 			String id = request.getParameter("userId");
 			AdminUserInfo au = new AdminUserInfo();
 			if (id == null || id.length() == 0) {
 				returnModel.put("error_msg", "初始化用户信息失败:请稍后重试.");
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=userList",
 						returnModel);
 			}
 			au = this.adminUserInfoService.findUserInfoById(id);
 			if (au != null) {
 				returnModel.put("user", au);
 				if (au.getTypeId() == 0) {
 					returnModel.put("createName", "系统用户.");
 				} else {
 					AdminUserInfo createAu = this.adminUserInfoService
 							.findUserInfoById(au.getCreateId());
 					if (createAu != null) {
 						returnModel.put("createName", createAu.getUserName());
 					} else {
 						returnModel.put("createName", "该用户已经被删除.");
 					}
 				}
 				AdminUserGroup gr = au.getAdminUserGroup();
 				if (gr != null) {
 					returnModel.put("groupName", gr.getGroupName());
 				} else {
 					returnModel.put("groupName", "用户所在的用户组已经不存在");
 				}
 				return new ModelAndView("/userinfo/userView", returnModel);
 			} else {
 				returnModel.put("error_msg", "初始化用户信息失败:请稍后重试.");
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=userList",
 						returnModel);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			error_msg = "初始化用户信息失败:请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=userList",
 					returnModel);
 		}
 
 	}
 
 	@RequestMapping(method = RequestMethod.GET, params = "method=intNewUser")
 	public ModelAndView intNewUser(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String error_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "error_msg","");
 		if (!error_msg.isEmpty()) {
 			returnModel.put("error_msg", error_msg);
 		}
 		List<Hotel> hotelList = getDefaultHotelList();
 		List groupList = this.adminUserGroupService.findAdminUserGroupsAll();
 		returnModel.put("groupList", groupList);
 		returnModel.put("hotelList", hotelList);
 		return new ModelAndView("/userinfo/userNew", returnModel);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveUser")
 	public ModelAndView saveUser(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String error_msg = request.getParameter("error_msg");
 		String succeed_msg = request.getParameter("succeed_msg");
 		String userId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "userId","");
 		String userName = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "userName","");
 		String password = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "password","");
 		String repassword = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "repassword","");
 		String sex = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "sex","");
 		String mobile = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "mobile","");
 		String email = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "email","");
 		String groupId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "groupId","");
 		String status = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "status","");
 		String hotelId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "hotelId","");
 		//检查userId是否重复
 		AdminUserInfo au_dup = this.adminUserInfoService.findUserInfoByUserId(userId);
 		if(au_dup!=null){
 			error_msg = "用户Id重复,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intNewUser",
 					returnModel);
 		}
 		if(password.isEmpty() || repassword.isEmpty()){
 			error_msg = "密码为空,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intNewUser",
 					returnModel);
 		}
 		if(groupId.isEmpty()){
 			error_msg = "还未选择用户分组,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intNewUser",
 					returnModel);
 		}
 		String dec_password = "";
 		if(password.equals(repassword)){
 			dec_password = MD5Utils.MD5forPwd(password);
 		}else{
 			error_msg = "两次创建密码不一致,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intNewUser",
 					returnModel);
 		}
 		try{
 		AdminUserInfo au = new AdminUserInfo();
 		au.setPasswd(password);
 		au.setRePasswd(dec_password);
 		au.setEmail(email);
 		au.setSex(Integer.parseInt(sex));
 		au.setStatus(0);
 		au.setUserName(userName);
 		au.setUserId(userId);
 		au.setMobile(mobile);
 		au.setTypeId(1);
 		if(hotelId.isEmpty()){
 			au.setHotelId("0");
 		}else{
 			au.setHotelId(hotelId);
 		}
 		au.setAdminUserGroup(this.adminUserGroupService.findAdminUserGroupByID(groupId));
 		AdminUserInfo au_session =  (AdminUserInfo)request.getSession().getAttribute(Constant.USER_SESSION_KEY);
 		au.setCreateId(au_session.getCreateId());
 		au.setRegTime(new Date());
 		AdminUserInfo ug_save = this.adminUserInfoService.saveAdminUserInfo(au);
 			if (ug_save != null) {
 				succeed_msg = "新增用户信息成功.";
 				returnModel.put("succeed_msg", succeed_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=userList",
 						returnModel);
 			} else {
 				error_msg = "新增用户信息失败,请稍后重试.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=intNewUser",
 						returnModel);
 			}
 		}
 			catch(Exception e){
 				e.printStackTrace();
 				error_msg = "新增用户信息失败,请稍后重试.";
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=intNewUser",
 						returnModel);
 			}
 			
 	}
 	
 	@RequestMapping(method = RequestMethod.GET, params = "method=intEditUser")
 	public ModelAndView intEditUser(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String error_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "error_msg","");
 		if (!error_msg.isEmpty()) {
 			returnModel.put("error_msg", error_msg);
 		}
 		String succeed_msg = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "succeed_msg","");
 		if (!succeed_msg.isEmpty()) {
 			returnModel.put("succeed_msg", succeed_msg);
 		}
 		String id = SimpleServletRequestUtils.getStringParameter(request, "id","");
 		if(id.isEmpty()){
 			error_msg = "编辑用户初始化信息失败,请稍后重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=userList",
 					returnModel);
 		}
 		AdminUserInfo au_edit = this.adminUserInfoService.findUserInfoById(id);
 		if(au_edit==null){
 			error_msg = "编辑用户不存在,请重试.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView(
 					"redirect:/servlet/admin/useinfo.do?method=userList",
 					returnModel);
 		}
 		List<Hotel> hotelList = getDefaultHotelList();
 		returnModel.put("au_edit", au_edit);
 		returnModel.put("hotelList", hotelList);
 		List groupList = this.adminUserGroupService.findAdminUserGroupsAll();
 		returnModel.put("groupList", groupList);
 		returnModel.put("id", id);
 		return new ModelAndView("/userinfo/userEdit", returnModel);
 	}
 	
 	@RequestMapping(method = RequestMethod.POST, params = "method=saveUserEdit")
 	public ModelAndView saveUserEdit(HttpServletRequest request,
 			HttpServletResponse response) throws Exception {
 		Map<String, Object> returnModel = new HashMap<String, Object>();
 		String error_msg = request.getParameter("error_msg");
 		String succeed_msg = request.getParameter("succeed_msg");
 		String id = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "id","");
 		String userId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "userId","");
 		String userName = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "userName","");
 		String password = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "password","");
 		String repassword = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "repassword","");
 		String sex = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "sex","");
 		String mobile = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "mobile","");
 		String email = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "email","");
 		String groupId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "groupId","");
 		String status = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "status","");
 		String hotelId = (String) SimpleServletRequestUtils.getStringParameter(
 				request, "hotelId","");
 		//检查userId是否重复
 		AdminUserInfo au_edit = this.adminUserInfoService.findUserInfoById(id);
 		if(au_edit==null){
 			error_msg = "编辑用户初始化失败,请重新操作.";
 			returnModel.put("error_msg", error_msg);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=userList",
 					returnModel);
 		}
 		if(password.isEmpty() || repassword.isEmpty()){
 			error_msg = "密码为空,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			returnModel.put("id", id);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intEditUser",
 					returnModel);
 		}
 		if(groupId.isEmpty()){
 			error_msg = "还未选择用户分组,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			returnModel.put("id", id);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intEditUser",
 					returnModel);
 		}
 		String dec_password = "";
 		if(password.equals(repassword)){
 			dec_password = MD5Utils.MD5forPwd(password);
 		}else{
 			error_msg = "两次创建密码不一致,请重新创建用户.";
 			returnModel.put("error_msg", error_msg);
 			returnModel.put("id", id);
 			return new ModelAndView("redirect:/servlet/admin/useinfo.do?method=intEditUser",
 					returnModel);
 		}
 		try{
 			au_edit.setPasswd(password);
 			au_edit.setRePasswd(dec_password);
 			au_edit.setEmail(email);
 			au_edit.setSex(Integer.parseInt(sex));
 			au_edit.setUserName(userName);
			au_edit.setUserId(userId);
 			au_edit.setMobile(mobile);
 			if(hotelId.isEmpty()){
 				au_edit.setHotelId("0");
 			}else{
 				au_edit.setHotelId(hotelId);
 			}
 			au_edit.setAdminUserGroup(this.adminUserGroupService.findAdminUserGroupByID(groupId));
 			au_edit.setStatus(Integer.parseInt(status));
 			AdminUserInfo ug_save = this.adminUserInfoService.saveAdminUserInfo(au_edit);
 			if (ug_save != null) {
 				succeed_msg = "编辑用户信息成功.";
 				returnModel.put("id", id);
 				returnModel.put("succeed_msg", succeed_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=intEditUser",
 						returnModel);
 			} else {
 				error_msg = "编辑用户信息失败,请稍后重试.";
 				returnModel.put("id", id);
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=intEditUser",
 						returnModel);
 			}
 		}
 			catch(Exception e){
 				e.printStackTrace();
 				error_msg = "编辑用户信息失败,请稍后重试.";
 				returnModel.put("id", id);
 				returnModel.put("error_msg", error_msg);
 				return new ModelAndView(
 						"redirect:/servlet/admin/useinfo.do?method=intEditUser",
 						returnModel);
 			}
 			
 	}
 	
 	private List<Hotel> getDefaultHotelList() {
 		PageOrder hotelListOrder = new SimpleOrderImpl();
 		hotelListOrder.setOrderColumn("name");
 		hotelListOrder.setSequence(PageOrder.Sequence.ASC.toString());
 		List<Hotel> hotelList = this.hotelService.getHotelList(null, null, hotelListOrder);
 		return hotelList;
 	}
 }
