 package hudson.notify;
 
 import hudson.matrix.MatrixConfiguration;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Run;
 
 import java.io.IOException;
 import java.util.List;
 
 /****
  * 
  * @author tang
  */
 
 public abstract class BuildNumberSychronize {
 	
 	// upstram build
 	final AbstractBuild<?, ?> upstrambuild;
 	
 	final BuildListener listener;
 	
 	public BuildNumberSychronize(AbstractBuild<?, ?> upstrambuild,BuildListener listener){
 		this.upstrambuild = upstrambuild;
 		this.listener = listener;
 	}
 
 	int nextUpStreamBuildNumber;
 	
     
 	public BuildListener getListener() {
 		return listener;
 	}
 
 	// need implement
 	abstract int setNextDownStreamBuildNumber(AbstractBuild<?, ?> upstrambuild);
 
 	private List<AbstractProject> getDownStreamProjects(){
 		if(upstrambuild.getProject() instanceof MatrixConfiguration){
 			MatrixConfiguration configuration = (MatrixConfiguration)upstrambuild.getProject();
 		    return configuration.getParent().getDownstreamProjects();
 		}else{
 			return upstrambuild.getProject().getDownstreamProjects();
 		}
 	}
 
 	public void sychronizeBuildNumber() throws Exception {
 		nextUpStreamBuildNumber=setNextDownStreamBuildNumber(upstrambuild);
 		List<AbstractProject> downStreamProjects = getDownStreamProjects();
 		for (AbstractProject downstreamProject : downStreamProjects) {
 			downstreamProjectClear(nextUpStreamBuildNumber,downstreamProject);
			downstreamProject.updateNextBuildNumber(nextUpStreamBuildNumber);
 			// reset the build number of downstream, may be problem in high version hudson
 			downstreamProject.onCopiedFrom(null);
 			downstreamProject.save();
 		}
 	}
     
 	/**
 	 * This is a hook before updateNextBuildNumber
 	 * @param nextDownStreamBuildNumber
 	 * @param downstreamProject
 	 * @throws IOException 
 	 */
 	public void downstreamProjectClear(int nextDownStreamBuildNumber,
 			AbstractProject downstreamProject) throws IOException {
 	     //doNothing
 	}
 
 }
