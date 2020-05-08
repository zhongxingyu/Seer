 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import iTests.framework.utils.LogUtils;
 import org.apache.commons.io.FileUtils;
 import iTests.framework.utils.IOUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.AbstractCloudService;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.JCloudsCloudService;
 
 public class Ec2CloudService extends JCloudsCloudService {
 
     private static final String EC2_CERT_PROPERTIES = CREDENTIALS_FOLDER + "/cloud/ec2/ec2-cred.properties";
 
     private final Properties certProperties = getCloudProperties(EC2_CERT_PROPERTIES);
 
     protected static final String EU_WEST_REGION = "eu-west-1";
     protected static final String US_EAST_REGION = "us-east-1";
 
     public static final String DEFAULT_EU_WEST_LINUX_AMI = EU_WEST_REGION + "/ami-c37474b7";
     public static final String DEFAULT_EU_WEST_UBUNTU_AMI = EU_WEST_REGION + "/ami-c1aaabb5";
 
     private String user = certProperties.getProperty("user");
     private String apiKey = certProperties.getProperty("apiKey");
     private String keyPair = certProperties.getProperty("keyPair");
     private String availabilityZone = "";
 
     public String getAvailabilityZone() {
         return availabilityZone;
     }
 
     public void setAvailabilityZone(String availabilityZone) {
         this.availabilityZone = availabilityZone;
     }
 
 
 
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
 
 	@Override
 	public String getRegion() {
 		return System.getProperty("ec2.region", EU_WEST_REGION);
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
 
 		final Map<String, String> propsToReplace = new HashMap<String, String>();
 
 		if (isEU()) {
             // switch the AMI's. since they are per region.
             getProperties().put(LINUX_IMAGE_ID_PROP, DEFAULT_EU_WEST_LINUX_AMI);
             getProperties().put(UBUNTU_IMAGE_ID_PROP, DEFAULT_EU_WEST_UBUNTU_AMI);
 			keyPair = "ec2-sgtest-eu";
 		}
         // now we can set the keyPair and keyFile
         getProperties().put(LOCATION_ID_PROP, getRegion() + availabilityZone);
 		getProperties().put(KEYPAIR_PROP, this.keyPair);
 		getProperties().put(KEYFILE_PROP, this.keyPair + ".pem");
         getProperties().put(USER_PROP, user);
         getProperties().put(API_KEY_PROP, apiKey);
 
 		propsToReplace.put("cloudify-agent-", getMachinePrefix() + "cloudify-agent");
 		propsToReplace.put("cloudify-manager", getMachinePrefix() + "cloudify-manager");
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines "
 				+ getNumberOfManagementMachines());
 
         propsToReplace.put("cloudify-storage-volume", getVolumePrefix());
 
         LogUtils.log("In ec2 service. logstash enabled: " + AbstractCloudService.enableLogstash);
         if(AbstractCloudService.enableLogstash){
             LogUtils.log("adding jclouds overrides");
            propsToReplace.put("\"jclouds.ec2.ami-query\":\"\",", "\"jclouds.ec2.ami-query\":\"\",\"jclouds.compute.poll-status.initial-period\":\"10000l\",\"jclouds.compute.poll-status.max-period\":\"100000l\",");
         }
 
         IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 
 		// add a pem file
 		final String sshKeyPemName = this.keyPair + ".pem";
 		final File fileToCopy = new File(CREDENTIALS_FOLDER + "/cloud/" + getCloudName() + "/" + sshKeyPemName);
 		final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
 		FileUtils.copyFileToDirectory(fileToCopy, targetLocation);
 	}
 
     protected boolean isEU() {
         return getRegion().contains("eu");
     }
 
 	@Override
 	public void addOverrides(Properties overridesProps) {
 		overridesProps.put("jclouds.ec2.ami-query", "");
 		overridesProps.put("jclouds.ec2.cc-ami-query", "");
 	}
 
 	public String getCertProperty(final String key) {
 		return this.certProperties.getProperty(key);
 	}
 }
