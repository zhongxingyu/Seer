 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2;
 
 import java.io.IOException;
 
 public class Ec2WinCloudService extends Ec2CloudService {
 
    private static final String DEFAULT_EU_WEST_MEDIUM_WIN_AMI = "eu-west-1/ami-a2b242d5";
     private static final String DEFAULT_US_EAST_MEDIUM_WIN_AMI = "us-east-1/ami-e55a7e8c";
 
     private static final String IMAGE_ID = "imageId";
 
 	public Ec2WinCloudService() {
 		super("ec2-win");
         getBootstrapper().timeoutInMinutes(45);
 	}
 
 	@Override
 	public String getCloudName() {
 		return "ec2-win";
 	}
	
	@Override
	public boolean supportsStorageApi() {
		return false;
	}
 
 	@Override
 	public void injectCloudAuthenticationDetails() throws IOException {
 		getAdditionalPropsToReplace().put("cloudifyagent", getMachinePrefix() + "cloudify-agent");
 		getAdditionalPropsToReplace().put("cloudifymanager", getMachinePrefix() + "cloudify-manager");
 		super.injectCloudAuthenticationDetails();
 		getProperties().put(HARDWARE_ID_PROP, "m1.large");
 
         // need to override the imageId with windows AMI's
 		if (isEU()) {
 			getProperties().put(IMAGE_ID, DEFAULT_EU_WEST_MEDIUM_WIN_AMI);
 		} else {
 			getProperties().put(IMAGE_ID, DEFAULT_US_EAST_MEDIUM_WIN_AMI);
 		}
 	}
 }
