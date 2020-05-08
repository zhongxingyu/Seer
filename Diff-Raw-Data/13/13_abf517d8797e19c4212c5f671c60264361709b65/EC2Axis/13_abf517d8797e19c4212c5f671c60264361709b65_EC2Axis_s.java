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
 
 	final private Integer numberOfSlaves;
 	private final String ec2label;
 
 	@DataBoundConstructor
 	public EC2Axis(String name, String ec2label, Integer numberOfSlaves ) {
 		super(name, Arrays.asList(ec2label.trim()));
 		this.ec2label = ec2label.trim();
 		this.numberOfSlaves = numberOfSlaves;
 	}
 
 	public String getEc2label() {
 		return ec2label;
 	}
 		
 	public Integer getNumberOfSlaves() {
 		return numberOfSlaves;
 	}
 
 	@Override
 	public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
 		EC2AxisCloud cloudToUse = EC2AxisCloud.getCloudToUse(ec2label);
 		return cloudToUse.allocateSlavesLabels(ec2label, numberOfSlaves);
 	}
 
 	@Override
 	public List<String> getValues() {
 		StaplerRequest currentRequest = getCurrentRequest();
 		if (currentRequest == null)
			return Arrays.asList("cloud<"+ec2label+">");
 		
 		return getConfigurationForCurrentBuildBasedOnUrl(currentRequest);
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
 		final MatrixBuild lastBuild = matrixProject.getLastBuild();
 		
 		return getConfigurationsForBuild(lastBuild);
 	}
 	
 	private List<String> getConfigurationsForBuild(MatrixBuild build) {
 		if (build == null)
 			return Arrays.asList();
 		final List<MatrixRun> runs = build.getRuns();
 		final List<String> builtOn = new LinkedList<String>();
 		for (MatrixRun matrixRun : runs) {
 			if (matrixRun.getParentBuild().getNumber() == build.getNumber()) {
 				String runName = matrixRun.getParent().getName();
				String configurationName = runName.split("=")[1];
 				builtOn.add(configurationName);
 			}
 		}
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
 	                formData.getInt("numberOfSlaves")
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
