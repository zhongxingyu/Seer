 package org.eweb4j.solidbase.user.model;
 
 import java.util.Map;
 
 import org.eweb4j.cache.Props;
 import org.eweb4j.mvc.config.MVCConfigConstant;
 import org.eweb4j.mvc.view.CallBackJson;
 
 public class UserCons {
 	private final static String propId = "UserConstant";
 	private static Map<String, String> map = Props.getMap(propId);
 
 	public static String IOC_SERVICE_BEAN_ID() {
 		return map.get("IOC_SERVICE_BEAN_ID");
 	}
 
 	public static String MODEL_NAME() {
 		return map.get("MODEL_NAME");
 	}
 
 	public static String DWZ_SUCCESS_JSON(String _mess) {
 		String openType = OPEN_TYPE();
 		String callbackType = "dialog".equalsIgnoreCase(openType) ? "reloadTab"
 				: "closeCurrent";
 		String mess = _mess == null ? "操作成功" : _mess;
 		String rel = map.get("SHOW_LIST_REL");
 		String path = map.get("SHOW_LIST_PATH");
 		path = MVCConfigConstant.BASE_URL + path;
 		String title = map.get("SHOW_LIST_TITLE");
 		return new CallBackJson("200", mess, rel, path, callbackType, title)
 				.toString();
 	}
 
 	public static String DWZ_SUCCESS_JSON_FOR_ALLOC_DEPART(String _mess) {
 
 		String openType = ALLOC_DEPART_OPEN_TYPE();
 		String callbackType = "dialog".equalsIgnoreCase(openType) ? "reloadTab"
 				: "closeCurrent";
 		callbackType = "reloadTab";
 		String mess = _mess == null ? "操作成功" : _mess;
 		String rel = map.get("ALLOC_DEPART.SHOW_LIST_REL");
 		String path = map.get("ALLOC_DEPART.SHOW_LIST_PATH");
 		path = MVCConfigConstant.BASE_URL + path;
 		String title = map.get("ALLOC_DEPART.SHOW_LIST_TITLE");
 		return new CallBackJson("200", mess, rel, path, callbackType, title)
 				.toString();
 	}
 
 	public static String DWZ_SUCCESS_JSON_FOR_ALLOC_ROLE(String _mess) {
 
 		String openType = ALLOC_ROLE_OPEN_TYPE();
 		String callbackType = "dialog".equalsIgnoreCase(openType) ? "reloadTab"
 				: "closeCurrent";
 		callbackType = "reloadTab";
 		String mess = _mess == null ? "操作成功" : _mess;
		String rel = map.get("ALLOC_ROLE.SHOW_LIST_REL");
 		String path = map.get("ALLOC_ROLE.SHOW_LIST_PATH");
 		path = MVCConfigConstant.BASE_URL + path;
 		String title = map.get("ALLOC_ROLE.SHOW_LIST_TITLE");
 		return new CallBackJson("200", mess, rel, path, callbackType, title)
 				.toString();
 	}
 
 	public static String ALLOC_DEPART_OPEN_TYPE() {
 		return map.get("ALLOC_DEPART.OPEN_TYPE");
 	}
 	
 
 	public static String ALLOC_ROLE_OPEN_TYPE() {
 		return map.get("ALLOC_ROLE.OPEN_TYPE");
 	}
 
 	public static String OPEN_TYPE() {
 		return map.get("OPEN_TYPE");
 	}
 
 	public static String LOCK() {
 		return map.get("LOCK");
 	}
 
 	public static String NORMAL() {
 		return map.get("NORMAL");
 	}
 
 	public static String LOGIN_PAGE() {
 		return map.get("LOGIN_PAGE");
 	}
 
 
 	public static String REGISTER_PAGE() {
 		return map.get("REGISTER_PAGE");
 	}
 
 	
 	public static String LOGIN_SUCCESS_REDIRECT() {
 		return map.get("LOGIN_SUCCESS_REDIRECT");
 	}
 
 	public static String LOGIN_ERR_ATTR_NAME() {
 		return map.get("LOGIN_ERR_ATTR_NAME");
 	}
 
 	public static String LOGIN_USER_ATTR_NAME() {
 		return map.get("LOGIN_USER_ATTR_NAME");
 	}
 
 	public static String REGISTER_SUCCESS_INFO() {
 		return map.get("REGISTER_SUCCESS_INFO");
 	}
 
 	public static String INCONRECT_AUTH_CODE() {
 		return map.get("INCONRECT_AUTH_CODE");
 	}
 
 	public static String INCORECT_PWD_OR_ACC() {
 		return map.get("INCORECT_PWD_OR_ACC");
 	}
 
 	public static String ACC_LOCKED() {
 		return map.get("ACC_LOCKED");
 	}
 
 	public static String ACC_INVALID() {
 		return map.get("ACC_INVALID");
 	}
 
 	public static String INCORECT_REPEAT_PWD() {
 		return map.get("INCORECT_REPEAT_PWD");
 	}
 
 	public static String DUPLICATE_ACC() {
 		return map.get("DUPLICATE_ACC");
 	}
 
 	public static String USER_NOT_FOUND() {
 		return map.get("USER_NOT_FOUND");
 	}
 
 	public static String UPDATE_FAIL() {
 		return map.get("UPDATE_FAIL");
 	}
 
 	public static String LOGIN_VERIFY_MESS() {
 		return map.get("LOGIN_VERIFY_MESS");
 	}
 
 	public static String LOCKED_MESS() {
 		return map.get("LOCKED_MESS");
 	}
 
 	public static String INVALID_MESS() {
 		return map.get("INVALID_MESS");
 	}
 
 	public static String DATA_ACCESS_ERR() {
 		return map.get("DATA_ACCESS_ERR");
 	}
 
 	public static String ALLOC_ROLE_ACTION() {
 		return map.get("ALLOC_ROLE_ACTION");
 	}
 
 	public static String ALLOC_DEPARTMENT_ACTION() {
 		return map.get("ALLOC_DEPARTMENT_ACTION");
 	}
 
 	public static String USER_NOT_SELECTED_MESS() {
 		return "请选择用户！";
 	}
 
 	public static String LOGIN_ACTION_RESULT() {
 		return map.get("LOGIN_ACTION_RESULT");
 	}
 
 	public static String REGISTER_ACTION_RESULT() {
 		return map.get("REGISTER_ACTION_RESULT");
 	}
 
 	public static String ADD_USER_DEPART_ACTOIN() {
 		return map.get("ADD_USER_DEPART_ACTOIN");
 	}
 
 	public static String ALLOC_DEPART_ACTION_RESULT() {
 		return map.get("ALLOC_DEPART_ACTION_RESULT");
 	}
 
 
 	public static String ADD_USER_ROLE_ACTOIN() {
 		return map.get("ADD_USER_ROLE_ACTOIN");
 	}
 
 	public static String ALLOC_ROLE_ACTION_RESULT() {
 		return map.get("ALLOC_ROLE_ACTION_RESULT");
 	}
 
 	public static String EDIT_ACTION_RESULT() {
 		return map.get("EDIT_ACTION_RESULT");
 	}
 
 	public static String GET_USER_DEPART_ACTION_RESULT() {
 		return map.get("GET_USER_DEPART_ACTION_RESULT");
 	}
 
 	public static String GET_USER_ROLE_ACTION_RESULT() {
 		return map.get("GET_USER_ROLE_ACTION_RESULT");
 	}
 
 	public static String PAGING_LOG_ACTION_RESULT() {
 		return map.get("PAGING_LOG_ACTION_RESULT");
 	}
 
 	public static String PAGING_ACTION_RESULT() {
 		return map.get("PAGING_ACTION_RESULT");
 	}
 
 	public static String NEW_ACTION_RESULT() {
 		return map.get("NEW_ACTION_RESULT");
 	}
 	
 	public static String PROFILE_ACTION_RESULT() {
 		return map.get("PROFILE_ACTION_RESULT");
 	}
 }
