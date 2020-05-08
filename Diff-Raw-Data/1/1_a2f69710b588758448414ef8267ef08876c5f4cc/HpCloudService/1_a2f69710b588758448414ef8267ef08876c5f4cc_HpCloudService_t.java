 package test.cli.cloudify.cloud.hp;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 import test.cli.cloudify.cloud.AbstractCloudService;
 import framework.tools.SGTestHelper;
 import framework.utils.AssertUtils;
 import framework.utils.IOUtils;
 import framework.utils.ScriptUtils;
 
 public class HpCloudService extends AbstractCloudService {
 	
 	private String tenant = "24912589714038";
 	private String cloudName = "openstack";
 	private String user = "98173213380893";
 	private String apiKey = "C5nobOW90bhnCmE5AQaLaJ0Ubd8UISPxGih";
 	private String pemFileName = "sgtest-hp";
 
 	public String getTenant() {
 		return tenant;
 	}
 
 	public void setTenant(String tenant) {
 		this.tenant = tenant;
 	}
 
 	public String getCloudName() {
 		return cloudName;
 	}
 
 	public void setCloudName(String cloudName) {
 		this.cloudName = cloudName;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	public String getPemFileName() {
 		return pemFileName;
 	}
 
 	public void setPemFileName(String pemFileName) {
 		this.pemFileName = pemFileName;
 	}
 
 
 
 	@Override
 	public void injectAuthenticationDetails() throws IOException {
 		
 		String cloudTestPath = (SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/" + cloudName).replace('\\', '/');
 		String sshKeyPemName = pemFileName + ".pem";
 
 		// cloud plugin should include recipe that includes secret key 
 		File cloudPluginDir = new File(ScriptUtils.getBuildPath() , "tools/cli/plugins/esc/" + cloudName + "/");
 		File originalCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.groovy");
 		File backupCloudDslFile = new File(cloudPluginDir, cloudName + "-cloud.backup");
 
 		// first make a backup of the original file
 		FileUtils.copyFile(originalCloudDslFile, backupCloudDslFile);
 		
 		Map<String, String> propsToReplace = new HashMap<String,String>();
 		propsToReplace.put("ENTER_USER", user);
 		propsToReplace.put("ENTER_API_KEY", apiKey);
 		propsToReplace.put("cloudify_agent_", this.machinePrefix + "cloudify_agent");
 		propsToReplace.put("cloudify_manager", this.machinePrefix + "cloudify_manager");
 		propsToReplace.put("ENTER_KEY_FILE", pemFileName + ".pem");
 		propsToReplace.put("ENTER_TENANT", tenant);
 		propsToReplace.put("hp-cloud-demo", "sgtest");
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "  + numberOfManagementMachines);
		propsToReplace.put("\"openstack.wireLog\": \"false\"", "\"openstack.wireLog\": \"true\"");
 		
 		IOUtils.replaceTextInFile(originalCloudDslFile.getAbsolutePath(), propsToReplace);
 
 		// upload dir needs to contain the sshKeyPem 
 		File targetPem = new File(ScriptUtils.getBuildPath(), "tools/cli/plugins/esc/" + cloudName + "/upload/" + sshKeyPemName);
 		FileUtils.copyFile(new File(cloudTestPath, sshKeyPemName), targetPem);
 		AssertUtils.assertTrue("File not found", targetPem.isFile());
 
 		
 	}
 }
