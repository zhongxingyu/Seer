 package test.cli.cloudify.cloud.services.ec2;
 
 import framework.tools.SGTestHelper;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 import org.apache.commons.io.FileUtils;
 import test.cli.cloudify.cloud.services.JCloudsCloudService;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class Ec2CloudService extends JCloudsCloudService {
     private static final String EC2_CERT_PROPERTIES = "apps/cloudify/cloud/ec2/ec2-cert.properties";
     public static final String DEFAULT_US_EAST_LINUX_AMI = "us-east-1/ami-76f0061f";
     public static final String DEFAULT_US_EAST_UBUNTU_AMI = "us-east-1/ami-82fa58eb";
     public static final String DEFAULT_EU_WEST_LINUX_AMI = "eu-west-1/ami-c37474b7";
     public static final String DEFAULT_EU_WEST_UBUNTU_AMI = "eu-west-1/ami-c1aaabb5";
 
     public static final String DEFAULT_EC2_LINUX_AMI_USERNAME = "ec2-user";
     public static final String DEFAULT_EC2_UBUNTU_AMI_USERNAME = "ubuntu";
 
    private Properties certProperties = getCloudProperties(EC2_CERT_PROPERTIES);
 
     private String user = certProperties.getProperty("user");
     private String apiKey = certProperties.getProperty("apiKey");
     private String keyPair = certProperties.getProperty("keyPair");
 
 	public Ec2CloudService() {
 		super("ec2");
 	}
 
 	public Ec2CloudService(final String cloudName) {
 		super(cloudName);
 	}
 
 
 	public void setUser(final String user) {
 		this.user = user;
 	}
 
 	@Override
 	public String getUser() {
 		return user;
 	}
 
 	public void setApiKey(final String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	@Override
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public String getRegion() {
 		return System.getProperty("ec2.region", "us-east-1");
 	}
 
 	public void setRegion(final String region) {
 		System.setProperty("ec2.region", region);
 	}
 
 	public void setKeyPair(final String keyPair) {
 		this.keyPair = keyPair;
 	}
 
 	public String getKeyPair() {
 		return keyPair;
 	}
 
 	@Override
 	public void injectCloudAuthenticationDetails()
 			throws IOException {
 
 		getProperties().put("user", this.user);
 		getProperties().put("apiKey", this.apiKey);
 
 		final Map<String, String> propsToReplace = new HashMap<String, String>();
 
 		if (getRegion().contains("eu")) {
 			LogUtils.log("Working in eu region");
 			getProperties().put("locationId", "eu-west-1");
 			setKeyPair("sgtest-eu");
 			if (!getCloudName().contains("win")) {
 				getProperties().put("linuxImageId", DEFAULT_EU_WEST_LINUX_AMI);
 				getProperties().put("ubuntuImageId", DEFAULT_EU_WEST_UBUNTU_AMI);
 				getProperties().put("hardwareId", "m1.small");
 			}
 		} else {
 			getProperties().put("locationId", "us-east-1");
 			if (!getCloudName().contains("win")) {
 				getProperties().put("linuxImageId", DEFAULT_US_EAST_LINUX_AMI);
 				getProperties().put("ubuntuImageId", DEFAULT_US_EAST_UBUNTU_AMI);
 				getProperties().put("hardwareId", "m1.small");
 			}
 		}
 		getProperties().put("keyPair", this.keyPair);
 		getProperties().put("keyFile", this.keyPair + ".pem");
 
 		propsToReplace.put("cloudify-agent-", getMachinePrefix() + "cloudify-agent");
 		propsToReplace.put("cloudify-manager", getMachinePrefix() + "cloudify-manager");
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "
 				+ getNumberOfManagementMachines());
 
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 
 		// add a pem file
 		final String sshKeyPemName = this.keyPair + ".pem";
 		final File fileToCopy =
 				new File(SGTestHelper.getSGTestRootDir() + "/src/main/resources/apps/cloudify/cloud/" + getCloudName()
 						+ "/"
 						+ sshKeyPemName);
 		final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
 		FileUtils.copyFileToDirectory(fileToCopy, targetLocation);
 	}
 
 	@Override
 	public void addOverrides(Properties overridesProps) {
 		overridesProps.put("jclouds.ec2.ami-query", "");
 		overridesProps.put("jclouds.ec2.cc-ami-query", "");		
 	}
 
 }
