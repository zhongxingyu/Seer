 package com.lmiky.jdp.user.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.lmiky.jdp.database.model.PropertyCompareType;
 import com.lmiky.jdp.database.model.PropertyFilter;
 import com.lmiky.jdp.database.model.Sort;
 import com.lmiky.jdp.form.controller.FormController;
 import com.lmiky.jdp.form.model.ValidateError;
 import com.lmiky.jdp.form.util.ValidateUtils;
 import com.lmiky.jdp.service.BaseService;
 import com.lmiky.jdp.session.model.SessionInfo;
 import com.lmiky.jdp.user.pojo.Operator;
 import com.lmiky.jdp.user.pojo.Role;
 import com.lmiky.jdp.user.pojo.User;
 import com.lmiky.jdp.user.service.OperatorService;
 import com.lmiky.jdp.util.EncoderUtils;
 
 /**
  * 用户
  * @author lmiky
  * @date 2013-5-7
  */
 @Controller
 @RequestMapping("/operator")
 public class OperatorController extends FormController<Operator> {
 	
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#getAddAuthorityCode(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected String getAddAuthorityCode(ModelMap modelMap, HttpServletRequest request) {
 		return "jdp_user_operator_add";
 	}
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#getModifyAuthorityCode(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected String getModifyAuthorityCode(ModelMap modelMap, HttpServletRequest request) {
 		return "jdp_user_operator_modify";
 	}
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#getDeleteAuthorityCode(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected String getDeleteAuthorityCode(ModelMap modelMap, HttpServletRequest request) {
 		return "jdp_user_operator_delete";
 	}
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.base.controller.BaseController#getLoadAuthorityCode(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected String getLoadAuthorityCode(ModelMap modelMap, HttpServletRequest request) {
 		return "jdp_user_operator_load";
 	}
 
 	/**
 	 * @author lmiky
 	 * @date 2013-5-7
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/list.shtml")
 	public String list(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse) throws Exception {
 		return executeList(modelMap, request, resopnse);
 	}
 	
 	
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.view.controller.ViewController#appendListAttribute(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void appendListAttribute(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse) throws Exception {
 		super.appendListAttribute(modelMap, request, resopnse);
 		modelMap.put("roles", service.list(Role.class));
 	}
 
 
 
 	/**
 	 * @author lmiky
 	 * @date 2013-5-7
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/load.shtml")
 	public String load(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse,
 			@RequestParam(value = "id", required = false) Long id) throws Exception {
 		return executeLoad(modelMap, request, resopnse, id);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#appendLoadAttribute(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, com.lmiky.jdp.database.pojo.BasePojo)
 	 */
 	@Override
 	protected void appendLoadAttribute(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse, String openMode, Operator pojo) throws Exception {
 		super.appendLoadAttribute(modelMap, request, resopnse, openMode, pojo);
 		OperatorService userService = (OperatorService)service;
 		if(OPEN_MODE_EDIT.equals(openMode) || OPEN_MODE_READ.equals(openMode)) {
 			modelMap.put("userRoles", userService.listUserRoles(pojo.getId()));
 		}
 		if(OPEN_MODE_EDIT.equals(openMode)) {
 			modelMap.put("noUserRoles", userService.listNoUserRoles(pojo.getId()));
 		} else if(OPEN_MODE_CTEATE.equals(openMode)) {
 			List<Sort> sorts = new ArrayList<Sort>();
 			sorts.add(new Sort("name", Sort.SORT_TYPE_ASC, Role.class));
 			modelMap.put("noUserRoles", userService.list(Role.class, null, sorts));
 		}
 	}
 	
 	/**
 	 * @author lmiky
 	 * @date 2013-5-7
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/save.shtml")
 	public String save(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse,
 			@RequestParam(value = "id", required = false) Long id) throws Exception {
 		return executeSave(modelMap, request, resopnse, id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#generateNewPojo(org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected Operator generateNewPojo(ModelMap modelMap, HttpServletRequest request) throws Exception {
 		Operator user = super.generateNewPojo(modelMap, request);
 		Date date = new Date();
 		user.setCreateTime(date);
 		user.setLastSetPasswordTime(date);
 		user.setValid(User.VALID_YES);
 		return user;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#setPojoProperties(com.lmiky.jdp.database.pojo.BasePojo, org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	protected void setPojoProperties(Operator pojo, ModelMap modelMap, HttpServletRequest request) throws Exception {
 		super.setPojoProperties(pojo, modelMap, request);
 		if(!StringUtils.isBlank(request.getParameter("password"))) {
 			pojo.setPassword(EncoderUtils.md5(pojo.getPassword()));
 			pojo.setLastSetPasswordTime(new Date());
 		}
 		String[] selectedRoles = request.getParameterValues("selectedRoles");
 		Set<Role> roles = new HashSet<Role>();
 		if(selectedRoles != null && selectedRoles.length > 0) {
 			for(String roleId : selectedRoles) {
 				Role role = new Role();
 				role.setId(Long.parseLong(roleId));
 				roles.add(role);
 			}
 		}
 		pojo.setRoles(roles);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.form.controller.FormController#validateInput(com.lmiky.jdp.database.pojo.BasePojo, java.lang.String, org.springframework.ui.ModelMap, javax.servlet.http.HttpServletRequest)
 	 */
 	@Override
 	public List<ValidateError> validateInput(Operator pojo, String openMode, ModelMap modelMap, HttpServletRequest request) throws Exception {
 		List<ValidateError> validateErrors = super.validateInput(pojo, openMode, modelMap, request);
 		ValidateUtils.validateRequired(request, "name", "姓名", validateErrors);
 		if(ValidateUtils.validateRequired(request, "loginName", "登陆账号", validateErrors)) {
 			List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
 			propertyFilters.add(new PropertyFilter("loginName", request.getParameter("loginName"), PropertyCompareType.EQ, User.class));
 			if(OPEN_MODE_EDIT.equals(openMode)) {
 				propertyFilters.add(new PropertyFilter("id", pojo.getId(), PropertyCompareType.NE, User.class));
 			}
 			if(service.exist(User.class, propertyFilters)) {
 				validateErrors.add(new ValidateError("loginName", "登陆账号已存在"));
 			}
 		}
 		if(OPEN_MODE_CTEATE.equals(openMode)) {
 			if(ValidateUtils.validateRequired(request, "password", "密码", validateErrors)) {
 				if(ValidateUtils.validateRequired(request, "confirmPassword", "确认密码", validateErrors)) {
 					ValidateUtils.validateEqualTo(request, "password", "密码", "confirmPassword", "确认密码", validateErrors);
 				}
 			}
 		}
 		return validateErrors;
 	}
 	
 	/**
 	 * 删除用户
 	 * @author lmiky
 	 * @date 2013-6-15
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/delete.shtml")
 	public String delete(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse, @RequestParam(value = "id", required = false) Long id) throws Exception {
 		return executeDelete(modelMap, request, resopnse, id);
 	}
 	
 	/**
 	 * 批量删除
 	 * @author lmiky
 	 * @date 2013-6-24
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param ids
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/batchDelete.shtml")
 	public String batchDelete(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse, @RequestParam(value = "batchDeleteId", required = false) Long[] ids) throws Exception {
 		return executeBatchDelete(modelMap, request, resopnse, ids);
 	}
 	
 	/**
 	 * 跳转到修改密码
 	 * @author lmiky
 	 * @date 2013-6-5
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/toModifyPassword.shtml")
 	public String toModifyPassword(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response,
 			@RequestParam(value = "id", required = false) Long id) throws Exception {
 		try {
 			//判断是否有登陆
 			SessionInfo sessionInfo = getSessionInfo(modelMap, request);
 			//检查单点登陆
 			checkSso(sessionInfo, modelMap, request);
 			return "jdp/user/operator/modifyPassword";
 		} catch(Exception e) {
 			return transactException(e, modelMap, request, response);
 		}
 	}
 	
 	/**
 	 * 修改密码
 	 * @author lmiky
 	 * @date 2013-6-4
 	 * @param modelMap
 	 * @param request
 	 * @param resopnse
 	 * @param id
 	 * @return
 	 * @throws Exception
 	 */
 	@RequestMapping("/modifyPassword.shtml")
 	public String modifyPassword(ModelMap modelMap, HttpServletRequest request, HttpServletResponse resopnse,
 			@RequestParam(value = "id", required = false) Long id) throws Exception {
 		try {
 			//判断是否有登陆
 			SessionInfo sessionInfo = getSessionInfo(modelMap, request);
 			//检查单点登陆
 			checkSso(sessionInfo, modelMap, request);
 			User user = service.find(User.class, sessionInfo.getUserId());
 			List<ValidateError> errors = validatePasswordInput(modelMap, request, user);
 			if(errors != null && !errors.isEmpty()) {
 				for(ValidateError error : errors) {
 					putValidateError(modelMap, error);
 				}
 			} else {
 				user.setPassword(EncoderUtils.md5(request.getParameter("password")));
 				user.setLastSetPasswordTime(new Date());
 				service.save(user);
 				//记录日志
 				logOpe(User.class.getName(), sessionInfo.getUserId(), modelMap, request, sessionInfo, OPEN_MODE_EDIT, "修改密码");
 				putMessage(modelMap, "修改成功!");
 			}
			return "jdp/user/operator/modifyPassword";
 		} catch(Exception e) {
 			return transactException(e, modelMap, request, resopnse);
 		}
 	}
 
 	/**
 	 * 检查修改密码输入
 	 * @author lmiky
 	 * @date 2013-6-5
 	 * @param modelMap
 	 * @param request
 	 * @param user
 	 * @return
 	 * @throws Exception 
 	 */
 	public List<ValidateError> validatePasswordInput(ModelMap modelMap, HttpServletRequest request, User user) throws Exception {
 		List<ValidateError> validateErrors = new ArrayList<ValidateError>();
 		if(ValidateUtils.validateRequired(request, "oldPassword", "旧密码", validateErrors)) {
 			String oldPassword = request.getParameter("oldPassword");
 			ValidateUtils.validateEqualTo("oldPassword", EncoderUtils.md5(oldPassword), "旧密码", "password", user.getPassword(), "当前实际密码", validateErrors);
 		}
 		if(ValidateUtils.validateRequired(request, "password", "新密码", validateErrors)) {
 			if(ValidateUtils.validateRequired(request, "confirmPassword", "确认新密码", validateErrors)) {
 				ValidateUtils.validateEqualTo(request, "password", "新密码", "confirmPassword", "确认新密码", validateErrors);
 			}
 		}
 		return validateErrors;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.lmiky.jdp.base.controller.BaseController#getService()
 	 */
 	@Resource(name="operatorService")
 	public void setService(BaseService service) {
 		this.service = service;
 	}
 }
