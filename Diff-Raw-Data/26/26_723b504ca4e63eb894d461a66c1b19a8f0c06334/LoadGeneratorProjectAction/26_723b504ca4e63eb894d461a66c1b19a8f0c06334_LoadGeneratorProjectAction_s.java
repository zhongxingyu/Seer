 package com.atex;
 
 import hudson.model.Action;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.Project;
 import hudson.model.Run;
 import hudson.util.ChartUtil;
 import hudson.util.ChartUtil.NumberOnlyBuildLabel;
 import hudson.util.DataSetBuilder;
 
 import java.io.IOException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  * Action used for Metrics report on project level.
  * 
  */
 public class LoadGeneratorProjectAction implements Action {
 
 	private final Project<?, ?> project;
 
 	private DataSetBuilder<String, NumberOnlyBuildLabel> tprBuilder;
 	private DataSetBuilder<String, NumberOnlyBuildLabel> rpsBuilder;
 
 	public LoadGeneratorProjectAction(Project<?, ?> project) {
 		this.project = project;
 	}
 
 	public Project<?, ?> getProject() {
 		return project;
 	}
 
 	public void init() {
 		tprBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
 		rpsBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
 
 		for (Object build : project.getBuilds()) {
 
 			AbstractBuild abstractBuild = (AbstractBuild) build;
 
 			if (!abstractBuild.isBuilding()
 					&& abstractBuild.getResult().isBetterOrEqualTo(
 							Result.SUCCESS)) {
 				LoadGeneratorBuildAction action = abstractBuild
 						.getAction(LoadGeneratorBuildAction.class);

				double timePerRequest = action.getTimePerRequest();
				if(timePerRequest != -1) {
					tprBuilder.add(timePerRequest, "Your site", new NumberOnlyBuildLabel(abstractBuild));
				}
				double requestPerSecond = action.getRequestPerSecond();
				if(requestPerSecond != -1) {
					rpsBuilder.add(requestPerSecond, "Your site", new NumberOnlyBuildLabel(abstractBuild));
 				}
 			}
 		}
 	}
 
 	public void doLoadTestTimePerRequestGraph(StaplerRequest request,
 			StaplerResponse response) throws IOException {
 
 		if (shouldReloadGraph(request, response)) {
 			// Draw the common graph:
 			ChartUtil.generateGraph(request, response,
 					GraphUtil.createGraph(tprBuilder, "Avarage rendering time (ms)"), 1200, 600);
 		}
 	}
 	
 	public void doLoadTestRequestsPerSecondGraph(StaplerRequest request,
 			StaplerResponse response) throws IOException {
 
 		if (shouldReloadGraph(request, response)) {
 			// Draw the common graph:
 			ChartUtil.generateGraph(request, response,
 					GraphUtil.createGraph(rpsBuilder, "Avarage requests per second"), 1200, 600);
 
 		}
 	}
 
 	private boolean shouldReloadGraph(StaplerRequest request,
 			StaplerResponse response) throws IOException {
 		return shouldReloadGraph(request, response,
 				project.getLastSuccessfulBuild());
 	}
 
 	public String getIconFileName() {
 		return MetricsPlugin.ICON_FILE_NAME;
 	}
 
 	public String getDisplayName() {
 		return "Load test report";
 	}
 
 	public String getUrlName() {
 		return "loadTest";
 	}
 	
 	protected boolean shouldReloadGraph(StaplerRequest request, StaplerResponse response, Run build) throws IOException {
 	    return !request.checkIfModified(build.getTimestamp(), response);
 	}
 }
