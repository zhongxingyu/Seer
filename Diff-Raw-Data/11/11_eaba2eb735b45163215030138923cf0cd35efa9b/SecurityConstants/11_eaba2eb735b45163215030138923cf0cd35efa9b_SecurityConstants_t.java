 package org.cloudifysource.quality.iTests.test.cli.cloudify.security;
 
 import iTests.framework.tools.SGTestHelper;
 
 public class SecurityConstants {
    public static final String CREDENTIALS_FOLDER = System.getProperty("iTests.credentialsFolder",
             SGTestHelper.getSGTestRootDir() + "/src/main/resources/credentials");
 
 	public static final String USER_PWD_ALL_ROLES = "Superuser";
 	public static final String USER_PWD_CLOUD_ADMIN = "Amanda";
 	public static final String USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER = "Dana";
 	public static final String USER_PWD_APP_MANAGER = "Dan";
 	public static final String USER_PWD_APP_MANAGER_AND_VIEWER = "Don";
 	public static final String USER_PWD_VIEWER = "John";
 	public static final String USER_PWD_NO_ROLE = "Jane";
 	
 	public static final String USER_SPECIAL_CHARACTERS_VIEWER = "Test!@#$%^&*()_-=`~:.?'Test";
 	public static final String PWD_SPECIAL_CHARACTERS_VIEWER = "Test!@#$%^&*()_-=`~:.?'Test";
 
 	public static final String ALL_ROLES_DESCRIPTIN = USER_PWD_ALL_ROLES + " (all roles)";
 	public static final String CLOUD_ADMIN_DESCRIPTIN = USER_PWD_CLOUD_ADMIN + " (cloud admin)";
 	public static final String CLOUD_ADMIN_AND_APP_MANAGER_DESCRIPTION = USER_PWD_CLOUD_ADMIN_AND_APP_MANAGER + " (cloud admin and app manager)";
 	public static final String APP_MANAGER_DESCRIPTIN = USER_PWD_APP_MANAGER + " (app manager)";
 	public static final String APP_MANAGER_AND_VIEWER_DESCRIPTIN = USER_PWD_APP_MANAGER_AND_VIEWER + " (app manager and viewer)";
 	public static final String VIEWER_DESCRIPTIN = USER_PWD_VIEWER + " (viewer)";
 	public static final String NO_ROLE_DESCRIPTIN = USER_PWD_NO_ROLE + " (no roles)";
 	
 	public static final String GE_GROUP = "GE";
 	public static final String BEZEQ_GROUP = "Bezeq";
 	public static final String CELLCOM_GROUP = "Cellcom";
 	
 	public static final String CLOUDADMINS_GROUP = "ROLE_CLOUDADMINS";
 	public static final String APPMANAGERS_GROUP = "ROLE_APPMANAGERS";
 	public static final String VIEWERS_GROUP = "ROLE_VIEWERS";
 
 	public static final String SGTEST_ROOT_DIR = SGTestHelper.getSGTestRootDir().replace('\\', '/');
 	public static final String BUILD_SECURITY_FILE_PATH = SGTestHelper.getBuildDir().replace('\\', '/') + "/config/security/spring-security.xml";
 	public static final String DEFAULT_KEYSTORE_FILE_PATH = CREDENTIALS_FOLDER + "/security/keystore";
 	public static final String DEFAULT_KEYSTORE_PASSWORD = "password";
 	public static final String LDAP_SECURITY_FILE_PATH = CREDENTIALS_FOLDER + "/security/ldap-spring-security.xml";
 
 	public static final String ACCESS_DENIED_MESSAGE = "Permission not granted, access is denied.";
 	public static final String UNAUTHORIZED = "Unauthorized";
 
 }
