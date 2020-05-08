 package test.cli.cloudify.cloud.services.ec2;
 
 import java.io.IOException;
 
 public class Ec2WinCloudService extends Ec2CloudService {
 	
 	private static final String cloudName = "ec2-win";
 	
 	private static final String DEFAULT_MEDIUM_WIN_AMI = "us-east-1/ami-6cb90605";
 	
 	public Ec2WinCloudService(String uniqueName) {
 		super(uniqueName, cloudName);
 	}
 	
 	@Override
 	public String getCloudName() {
 		return cloudName;
 	}
 
 	@Override
 	public void injectServiceAuthenticationDetails() throws IOException {
 		super.injectServiceAuthenticationDetails();
 		getAdditionalPropsToReplace().put("cloudifyagent", this.machinePrefix + "cloudify-agent");
 		getAdditionalPropsToReplace().put("cloudifymanager", this.machinePrefix + "cloudify-manager");
 		if (getRegion().contains("eu")) {
			getAdditionalPropsToReplace().put("locationId \"us-east-1c\"", "locationId \"eu-west-1\"");
 			getAdditionalPropsToReplace().put('"' + DEFAULT_MEDIUM_WIN_AMI + '"', '"' + "eu-west-1/ami-911616e5" + '"');			
 		}
 	}
 	
 	
 
 }
