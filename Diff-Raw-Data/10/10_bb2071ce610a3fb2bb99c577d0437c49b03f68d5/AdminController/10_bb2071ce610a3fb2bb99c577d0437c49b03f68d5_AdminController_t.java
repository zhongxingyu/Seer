 package com.playlife.presentation.controllers;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.playlife.logic.user.PlayLifeUserService;
 import com.playlife.persistence.domainObject.PlayLifeUser;
 import com.playlife.persistence.domainObject.User_Role;
 import com.playlife.persistence.domainObject.User_Type;
 import com.playlife.presentation.converters.JSONConverter;
 import com.playlife.settings.ErrorSetting;
 import com.playlife.utility.LocaleService;
 import com.playlife.utility.exceptions.PresentationException;
 import com.playlife.utility.validators.UserValidator;
 
 @Controller
 @RequestMapping (value = "/admin")
 public class AdminController {
 	@Autowired
 	MessageSource messageSource;
 
 	@Autowired
 	PlayLifeUserService playLifeUserService;
 
 	@Autowired
 	UserValidator userValidator;
 
 	ObjectMapper mapper = JSONConverter.mapper;
 
 	@RequestMapping (value = "/")
 	protected String registerRequest(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
 		LocaleService.resolve(request, response);
 
 		model.put("userCount", playLifeUserService.getUserCount());
 		return redirect(request, "admin/dashboard", false);
 	}
 
 	@RequestMapping (value = "/{action}")
 	protected String mainRequest(@PathVariable String action, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
 		LocaleService.resolve(request, response);
 
 		if (action.equalsIgnoreCase("nav")) return redirect(request, "admin/nav", false);
 		else {
 			model.put("userCount", playLifeUserService.getUserCount());
 			return redirect(request, "admin/dashboard", false);
 		}
 	}
 
 	@RequestMapping (value = "/user/{action}")
 	protected String user_mainRequest(@PathVariable String action, HttpServletRequest request, HttpServletResponse response) throws IOException {
 		LocaleService.resolve(request, response);
 
 		if (action.equalsIgnoreCase("create")) return redirect(request, "admin/user/create", false);
 		else return redirect(request, "admin/user/userList", false);
 	}
 
 	@RequestMapping (value = "/error/{action}")
 	protected String error_mainRequest(@PathVariable String action, HttpServletRequest request, HttpServletResponse response) throws IOException {
 		LocaleService.resolve(request, response);
 
 		return redirect(request, "admin/error/errorList", false);
 	}
 	
	@SuppressWarnings ("unchecked")
 	@RequestMapping(value="/error/detail/{action}")
	protected String errorDetail_mainRequest(@PathVariable String s_fileName, HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
 		LocaleService.resolve(request, response);
 		
		File f_log = new File(request.getRealPath("") + ErrorSetting.ERROR_LOG_PATH+s_fileName+ErrorSetting.ERROR_LOG_EXTENSION);
		if (f_log!=null && f_log.isFile() && f_log.canRead()) {
			model.addAllAttributes(mapper.readValue(f_log, Map.class));
		}
		
 		return redirect(request, "admin/error/detail", false);
 	}
 	
 	@RequestMapping(value="/error/errorList.json")
 	@ResponseBody
 	protected String errorListRequest(@RequestParam (value = "search", required = false) final String search, int start, int end,
 		HttpServletRequest request, HttpServletResponse response) throws IOException {
 		LocaleService.resolve(request, response);
 		redirect(request, "", true);
 
 		List<Map<String, Object>> list_error = new ArrayList<Map<String, Object>>();
 
 		@SuppressWarnings ("deprecation")
 		File f_logFolder = new File(request.getRealPath("") + ErrorSetting.ERROR_LOG_PATH);
 		if (f_logFolder != null && f_logFolder.isDirectory()) {
 			List<File> logFileList = Arrays.asList(f_logFolder.listFiles(new FileFilter() {
 
 				@Override
 				public boolean accept(File pathname) {
 					if (pathname.isFile() && pathname.getName().endsWith(ErrorSetting.ERROR_LOG_EXTENSION) && pathname.canRead()) {
 						if (search == null || pathname.getName().matches(search + ErrorSetting.ERROR_LOG_EXTENSION)) return true;
 					}
 					return false;
 				}
 			}));
 			Collections.sort(logFileList, new Comparator<File>() {
 				@Override
 				public int compare(File o1, File o2) {
 					return (int) (o1.lastModified() - o2.lastModified());
 				}
 			});
 
 			for (int i = start; i < end && i < logFileList.size(); i++) {
 				File f_log = logFileList.get(i);
 				@SuppressWarnings ("unchecked")
 				Map<String, Object> log_error = mapper.readValue(f_log, Map.class);
 				Map<String, Object> error = new HashMap<String, Object>();
 				error.put("time", log_error.get("time"));
 				error.put("ip", log_error.get("ip"));
 				error.put("displayMessage", log_error.get("displayMessage").toString().replace("==/==", "'"));
 				list_error.add(error);
 			}
 		}
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 
 		map_return.put("errorList", list_error);
 		map_return.put("count", list_error.size());
 		map_return.put("status", "ok");
 
 		return mapper.writeValueAsString(map_return);
 	}
 
 	@RequestMapping (value = "/user/userList.json")
 	@ResponseBody
 	protected String user_listRequest(@RequestParam (value = "search", required = false) String search, int start, int end,
 		@RequestParam (value = "order", required = false) String order, @RequestParam (value = "orderBy", required = false) String orderBy,
 		HttpServletRequest request, HttpServletResponse response) throws IOException {
 		LocaleService.resolve(request, response);
 		redirect(request, "", true);
 
 		if (order == null) order = "";
 		if (orderBy == null) orderBy = "";
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		List<PlayLifeUser> users = playLifeUserService.getAll(search, start, end, "userId", "ASC");
 
 		map_return.put("users", users);
 		map_return.put("count", playLifeUserService.getAllCount(search));
 		map_return.put("status", "ok");
 
 		return mapper.writeValueAsString(map_return);
 	}
 
 	@RequestMapping (value = "/user/create.json")
 	@ResponseBody
 	protected String user_listRequest(PlayLifeUser user, String userRole, HttpServletRequest request, HttpServletResponse response) throws IOException {
 		LocaleService.resolve(request, response);
 		redirect(request, "", true);
 		userValidator.validate(user);
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		playLifeUserService.register(user.getUsername(), user.getPassword(), user.getEmail(), User_Type.NORMAL, User_Role.fromString(userRole));
 		map_return.put("status", "ok");
 
 		return mapper.writeValueAsString(map_return);
 	}
 
 	@RequestMapping (value = "/user/bam.json")
 	@ResponseBody
 	protected String bam(PlayLifeUser bamUser, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException {
 		redirect(request, null, true);
 
 		PlayLifeUser user = (PlayLifeUser) request.getSession().getAttribute("user");
 		if (user.getUserId().equals(bamUser.getUserId())) throw new PresentationException(-9999);
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		playLifeUserService.setDisable(bamUser.getUserId(), true);
 		map_return.put("status", "ok");
 		return mapper.writeValueAsString(map_return);
 	}
 
 	@RequestMapping (value = "/user/unbam.json")
 	@ResponseBody
 	protected String unbam(PlayLifeUser bamUser, HttpServletRequest request) throws JsonGenerationException, JsonMappingException, IOException {
 		redirect(request, null, true);
 
 		PlayLifeUser user = (PlayLifeUser) request.getSession().getAttribute("user");
 		if (user.getUserId().equals(bamUser.getUserId())) throw new PresentationException(-9999);
 
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		playLifeUserService.setDisable(bamUser.getUserId(), false);
 		map_return.put("status", "ok");
 		return mapper.writeValueAsString(map_return);
 	}
 
 	private String redirect(HttpServletRequest request, String path, boolean throwException) {
 		PlayLifeUser user = (PlayLifeUser) request.getSession().getAttribute("user");
 		if (user == null || user.getRole() != User_Role.ADMIN || user.isDisabled()) {
 			if (throwException) throw new PresentationException(-9999);
 			return "home";
 		}
 		else return path;
 	}
 
 	@ExceptionHandler (Exception.class)
 	@ResponseBody
 	public String handlerException(HttpServletRequest request, Exception ex) throws JsonGenerationException, JsonMappingException, IOException {
 		Map<String, Object> map_return = new HashMap<String, Object>();
 		map_return.put("error", JSONConverter.constructError(ex, messageSource, LocaleService.getLocale(request)));
 		map_return.put("status", "error");
 		return mapper.writeValueAsString(map_return);
 	}
 }
