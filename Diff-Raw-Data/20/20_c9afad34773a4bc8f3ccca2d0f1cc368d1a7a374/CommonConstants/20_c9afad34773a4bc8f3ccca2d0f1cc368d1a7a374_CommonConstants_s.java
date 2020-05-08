 package com.asu.edu.constants;
 
 import java.util.HashMap;
 
 public interface CommonConstants {
 
 	public static final String USER = "userVO";
 	public static final String SECURE_FILE_ID_TOKEN = "SECURE_TOKEN";
 	public static final String ROLE_DEPARTMENT_MGR = "ROLE_DEPARTMENT_MGR";
 	public static final String ROLE_REGULAR_EMP = "ROLE_REGULAR_EMP";
 	public static final String ROLE_GUEST_USR = "ROLE_GUEST_USR";
 	public static final String ROLE_CORPORATE_MGR = "ROLE_CORPORATE_MGR";
 	public static final String ROLE_ADMIN = "ROLE_ADMIN";
 
 	public static final String LOG_FILES_PATH = "/tmp";
 
 	public static final int ROLE_DEPARTMENT_MGR_ID = 4;
 	public static final int ROLE_REGULAR_EMP_ID = 3;
 	public static final int ROLE_GUEST_USR_ID = 2;
 	public static final int ROLE_CORPORATE_MGR_ID = 5;
 	public static final int ROLE_ADMIN_ID = 1;
 
 	public static final String CHECKIN_OUT = "checkin_out";
 	public static final String FILE_UPDATE = "file_update";
 	public static final String DELETE = "delete";
 
 	public static final String REQ_PARAM_FILE_ID = "fileId";
 	public static final String REQ_PARAM_DEPTID = "deptId";
 	public static final String REQ_PARAM_PARENTID = "parentId";
 	
 	public static final String C300 = "Updating file name should be same as original file name";
 
 	static HashMap<String, String> ERROR_CODES = new HashMap<String, String>();
 
 }
