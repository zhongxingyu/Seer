 package org.eclipse.alfresco.publisher.core;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.plexus.util.StringUtils;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.security.storage.ISecurePreferences;
 import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
 import org.eclipse.equinox.security.storage.StorageException;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 public class AlfrescoPreferenceHelper {
 
 	private static final String ALFRESCO_HOME = "alfresco.home";
 
 	private static final String WEBAPP_NAME = "webapp.name";
 
 	private static final String SERVER_ABSOLUTE_PATH = "server.absolute.path";
 
 	private static final String SERVER_URL = "server.url";
 
 	private static final String SERVER_RELOAD_LOGIN = "server.reload.login";
 
 	// private static final String SERVER_RELOAD_PASSWORD_KEY =
 	// "server.reload.password.key";
 
 	public static final String AMP_LIB_FILENAME = "amp.lib.filename";
 
 	public static final String AMP_FOLDER_RELATIVE_PATH = "amp.folder.relative.path";
 
 	public static final String AMP_LIB_DEPLOY_ABSOLUTE_PATH = "amp.lib.deploy.absolute.path";
 
 	private static final String DEPLOYMENT_MODE = "deployment.mode";
 
 	private Preferences preference;
 
 	private IProject project;
 
 	public AlfrescoPreferenceHelper(IProject project) {
 		this.project = project;
 		this.preference = getProjectPreferences(project);
 	}
 
 	public String getAlfrescoHome() {
 		return preference.get(ALFRESCO_HOME, "");
 	}
 
 	public String getWebappName() {
 		return preference.get(WEBAPP_NAME, null);
 	}
 
 	public String getWebappAbsolutePath() {
 		if ("Webapp".equals(getDeploymentMode())) {
 			if (getServerPath() == null) {
 				return null;
 			}
 			if (getWebappName() == null) {
 				return null;
 			}
 			return path(getServerPath(), "webapps" ,getWebappName());
 		}
 		return null;
 	}
 
 	public String getServerPath() {
 		return preference.get(SERVER_ABSOLUTE_PATH, null);
 	}
 
 	
 	public String getServerReloadWebscriptURL() {
		return getServerReloadWebscriptURL(getDeploymentMode());
 	}
 	
 	public String getServerReloadWebscriptURL(String mode) {
 		String serverURL = getServerURL();
 		if (serverURL == null) {
 			return null;
 		}
 		
 		if (isAlfresco()) {
 			return serverURL + "/service/index";
 		} else {
 			return serverURL + "/page/index";
 		}
 		
 	}
 	
 	
 
 	public String getServerLogin() {
 		return preference.get(SERVER_RELOAD_LOGIN, null);
 	}
 
 	public String getDeploymentMode() {
 		return preference.get(DEPLOYMENT_MODE, null);
 	}
 
 	public String getServerURL() {
 
 		return preference.get(SERVER_URL, null);
 	}
 
 	public static Preferences getProjectPreferences(IProject project) {
 		Preferences preferences = Platform
 				.getPreferencesService()
 				.getRootNode()
 				.node("project/" + project.getName() + "/"
 						+ "org.eclipse.alfresco.publisher.core");
 		return preferences;
 	}
 
 	public static void storePassword(String projectName, String password)
 			throws StorageException {
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		ISecurePreferences node = root.node("/org/eclipse/alfresco/"
 				+ projectName);
 		node.put("password", password, true /* encrypt */);
 
 	}
 
 	public static String getPassword(String projectName)
 			throws StorageException {
 		ISecurePreferences root = SecurePreferencesFactory.getDefault();
 		ISecurePreferences node = root.node("/org/eclipse/alfresco/"
 				+ projectName);
 		return node.get("password", null);
 
 	}
 
 	public boolean isAlfresco() {
 		return "alfresco".equals(getWebappName());
 	}
 
 	public void stageDeploymentMode(String mode) {
 		preference.put(DEPLOYMENT_MODE, mode);
 
 	}
 
 	public void stageServerPath(String serverPath) {
 		preference.put(SERVER_ABSOLUTE_PATH, serverPath);
 
 	}
 
 	public void stageWebappName(String webappName) {
 		preference.put(WEBAPP_NAME, webappName);
 
 	}
 
 	public void stageAlfrescoHome(String text) {
 		preference.put(ALFRESCO_HOME, text);
 
 	}
 
 	public void stageServerURL(String text) {
 		preference.put(SERVER_URL, text);
 
 	}
 
 	public void stageServerLogin(String text) {
 		preference.put(SERVER_RELOAD_LOGIN, text);
 
 	}
 
 	public void flush() throws BackingStoreException {
 		preference.flush();
 
 	}
 
 	public String getAmpJarName() {
 
 		return preference.get(AMP_LIB_FILENAME, null);
 	}
 
 	public String getAmpJarLocation() {
 
 		if (getTargetAmpLocation() == null)
 			return null;
 
 		if (getAmpJarName() == null)
 			return null;
 		return getTargetAmpLocation() + File.separator + "lib" + getAmpJarName();
 
 	}
 
 	public String getTargetAmpLocation() {
 		
 		return preference.get(AMP_FOLDER_RELATIVE_PATH, null);
 	}
 
 	public String getAmpLib() {
 		String mode = getDeploymentMode();
 		if("Webapp".equals(mode)) {
 			return path(getWebappAbsolutePath(), "WEB-INF", "lib");
 		}else if ("Shared".equals(mode)) {
 			return path(getServerPath(), "shared", "lib");
 		}
 		return null;
 	}
 
 	
 	private String path(String ...strings ) {
 		return StringUtils.join(strings, File.separator);
 	}
 
 	
 }
