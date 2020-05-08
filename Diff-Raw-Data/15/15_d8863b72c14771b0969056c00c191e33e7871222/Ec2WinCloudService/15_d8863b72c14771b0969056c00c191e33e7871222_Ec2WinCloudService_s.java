 package test.cli.cloudify.cloud.services.ec2;
 
 import java.io.IOException;
 
 public class Ec2WinCloudService extends Ec2CloudService {
 	
 	private static final String DEFAULT_EU_WEST_MEDIUM_WIN_AMI = "eu-west-1/ami-911616e5";
 	
 	public Ec2WinCloudService() {
 		super("ec2-win");
 	}
 	
 	@Override
 	public String getCloudName() {
 		return "ec2-win";
 	}
 
 	@Override
 	public void injectCloudAuthenticationDetails() throws IOException {
 		getAdditionalPropsToReplace().put("cloudifyagent", getMachinePrefix() + "cloudify-agent");
 		getAdditionalPropsToReplace().put("cloudifymanager", getMachinePrefix() + "cloudify-manager");
 		super.injectCloudAuthenticationDetails();
 		if (getRegion().contains("eu")) {
 			getProperties().put("imageId", DEFAULT_EU_WEST_MEDIUM_WIN_AMI);
			getProperties().put("hardwareId", "m1.large");
 		}
 	}
 }
