 package org.jenkinsci.plugins.ec2axis;
 
 import static org.kohsuke.stapler.Stapler.getCurrentRequest;
 import hudson.Extension;
 import hudson.Util;
 import hudson.matrix.Axis;
 import hudson.matrix.MatrixRun;
 import hudson.matrix.AxisDescriptor;
 import hudson.matrix.LabelAxis;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixProject;
 import hudson.model.AutoCompletionCandidates;
 import hudson.model.Messages;
 import hudson.model.Label;
 import hudson.model.labels.LabelAtom;
 import hudson.plugins.ec2.EC2AxisCloud;
 import hudson.plugins.ec2.EC2Logger;
 import hudson.util.FormValidation;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import jenkins.model.Jenkins;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 public class EC2Axis extends LabelAxis {
 
 	private static final Integer DEFAULT_TIMEOUT = 600;
 	private Integer numberOfSlaves;
 	private boolean alwaysCreateNewNodes = false;
 	private final String ec2label;
 	private final Integer instanceBootTimeoutLimit;
 
 	@DataBoundConstructor
	public EC2Axis(String name, String ec2label, Integer numberOfSlaves, Integer instanceBootTimeoutLimit, boolean alwaysCreateNewNodes) {
 		super(name, Arrays.asList(ec2label.trim()));
		if (instanceBootTimeoutLimit == null)
			this.instanceBootTimeoutLimit = DEFAULT_TIMEOUT;
		else
			this.instanceBootTimeoutLimit = instanceBootTimeoutLimit;
 		this.ec2label = ec2label.trim();
 		this.numberOfSlaves = numberOfSlaves;
 		this.alwaysCreateNewNodes = alwaysCreateNewNodes;
 	}
 
 	public String getEc2label() {
 		return ec2label;
 	}
 		
 	public Integer getNumberOfSlaves() {
 		return numberOfSlaves;
 	}
 	
 	public void setNumberOfSlaves(Integer numberOfSlaves) {
 		this.numberOfSlaves = numberOfSlaves;
 	}
 
 	public Integer getInstanceBootTimeoutLimit() {
 		return instanceBootTimeoutLimit;
 	}
 	
 	public boolean isAlwaysCreateNewNodes(){
 		return alwaysCreateNewNodes;
 	}
 
 	@Override
 	public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
 		EC2AxisCloud cloudToUse = EC2AxisCloud.getCloudToUse(ec2label);
 		if (cloudToUse == null) {
 			throw new RuntimeException("Cloud for label " + ec2label + " not found.");
 		}
 		Ec2AxisDescriptionAction e = new Ec2AxisDescriptionAction(
 				ec2label,
 				numberOfSlaves,
 				cloudToUse.getInstanceType(ec2label),
 				cloudToUse.getSpotPriceIfApplicable(ec2label));
 		context.getBuild().getActions().add(e);
 		
 		EC2Logger ec2Logger = new EC2Logger(context.getListener().getLogger());
 		List<String> allocateSlavesLabels = cloudToUse.allocateSlavesLabels(ec2Logger, ec2label, numberOfSlaves, instanceBootTimeoutLimit, alwaysCreateNewNodes);
 		
 		ec2Logger.println("Will run on the following labels:-------");
 		for (String allocatedSlaveLabel : allocateSlavesLabels) {
 			ec2Logger.println(allocatedSlaveLabel);
 		}
 		ec2Logger.println("-----------");
 		
 		return allocateSlavesLabels;
 	}
 
 	@Override
 	public List<String> getValues() {
 		StaplerRequest currentRequest = getCurrentRequest();
 		if (currentRequest == null)
 			return makeEmptyExecution();
 		
 		return getConfigurationForCurrentBuildBasedOnUrl(currentRequest);
 	}
 
 	private List<String> makeEmptyExecution() {
 		return Arrays.asList("cloud "+ec2label);
 	}
 
 	private List<String> getConfigurationForCurrentBuildBasedOnUrl( StaplerRequest currentRequest) 
 	{
 		String currentRequestUrl = currentRequest.getRequestURI();
 		if (isBuildSelected(currentRequestUrl)) 
 			return getConfigurationsBuildRequestedOnUrl(currentRequestUrl);
 		
 		return getConfigurationsForLastBuild(currentRequestUrl);
 	}
 	
 	private List<String> getConfigurationsBuildRequestedOnUrl(String currentRequestUrl) 
 	{
 		final String projectAndBuildNumberExtractedFromUrl = currentRequestUrl.replaceAll(".*/job/", "");
 		final String[] projectAndBuildNumber = projectAndBuildNumberExtractedFromUrl.split("/");
 		final String projectName = projectAndBuildNumber[0];
 		final String buildNumber = projectAndBuildNumber[1];
 		
 		MatrixBuild build = getProject(projectName).getBuild(buildNumber);
 		return getConfigurationsForBuild(build);
 	}
 	
 	private List<String> getConfigurationsForLastBuild(String currentRequestUrl) 
 	{
 		final String projectNameExtractedFromUrl = currentRequestUrl.replaceAll(".*/job/([^/]*).*", "$1");
 		
 		final MatrixProject matrixProject = getProject(projectNameExtractedFromUrl);
 		if (matrixProject == null)
 			return makeEmptyExecution();
 		final MatrixBuild lastBuild = matrixProject.getLastBuild();
 		
 		return getConfigurationsForBuild(lastBuild);
 	}
 	
 	private List<String> getConfigurationsForBuild(MatrixBuild build) {
 		if (build == null)
 			return makeEmptyExecution();
 		
 		final List<MatrixRun> runs = build.getRuns();
 		final List<String> builtOn = new LinkedList<String>();
 		for (MatrixRun matrixRun : runs) {
 			if (matrixRun.getParentBuild().getNumber() == build.getNumber()) {
 				String runName = matrixRun.getParent().getName();
 				String configurationName = runName.replaceAll(getName()+"=([^,]*).*", "$1"); 
 				builtOn.add(configurationName);
 			}
 		}
 		if (builtOn.size() == 0)
 			return makeEmptyExecution();
 		return builtOn;
 	}
 
 	private boolean isBuildSelected(String requestURI) {
 		return requestURI.matches(".*job//[0-9]+/?$");
 	}
 	
 	private MatrixProject getProject(String projectName) {
 		return (MatrixProject)Jenkins.getInstance().getItem(projectName);
 	}
 
 	@Extension
 	public static class DescriptorImpl extends AxisDescriptor {
 	
 	    @Override
 	    public String getDisplayName() {
 	        return "EC2Axis";
 	    }
 	
 	    @Override
 	    public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
 	        return new EC2Axis(
 	                formData.getString("name"),
 	                formData.getString("ec2label"),
 	                formData.getInt("numberOfSlaves"),
	                formData.getInt("instanceBootTimeoutLimit"),
 	                formData.getBoolean("alwaysCreateNewNodes")
 	        );
 	    }
 	    
 	    public FormValidation doCheckEc2label(@QueryParameter String value) {
 	    	String[] labels = value.split(" ");
 	    	for (String oneLabel : labels) {
 	    		FormValidation validation = checkOneLabel(oneLabel.trim());
 				if (!validation.equals(FormValidation.ok()))
 					return validation;
 			}
 	        return FormValidation.ok();
 	    }
 	
 		private FormValidation checkOneLabel(String oneLabel) {
 			if (Util.fixEmpty(oneLabel)==null)
 				return FormValidation.ok(); 
 			
 			Label l = Jenkins.getInstance().getLabel(oneLabel);
 			if (l.isEmpty()) {
 				for (LabelAtom a : l.listAtoms()) {
 					if (a.isEmpty()) {
 						LabelAtom nearest = LabelAtom.findNearest(a.getName());
 						return FormValidation.warning(Messages.AbstractProject_AssignedLabelString_NoMatch_DidYouMean(a.getName(),nearest.getDisplayName()));
 					}
 				}
 				return FormValidation.warning(Messages.AbstractProject_AssignedLabelString_NoMatch());
 			}
 			return FormValidation.ok();
 		}
 	    
 	    public AutoCompletionCandidates doAutoCompleteEc2label(@QueryParameter String value) {
 	        AutoCompletionCandidates c = new AutoCompletionCandidates();
 	        Set<Label> labels = Jenkins.getInstance().getLabels();
 	        List<String> queries = new AutoCompleteSeeder(value).getSeeds();
 	
 	        for (String term : queries) {
 	            for (Label l : labels) {
 	                if (l.getName().startsWith(term)) {
 	                    c.add(l.getName());
 	                }
 	            }
 	        }
 	        return c;
 	    }
 	}
 }
