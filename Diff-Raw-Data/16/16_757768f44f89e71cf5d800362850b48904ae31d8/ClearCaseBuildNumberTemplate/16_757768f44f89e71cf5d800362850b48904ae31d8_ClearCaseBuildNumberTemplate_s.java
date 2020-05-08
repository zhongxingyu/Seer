 package net.praqma.hudson.nametemplates;
 
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.utils.BuildNumber;
 import net.praqma.hudson.exception.TemplateException;
 import net.praqma.hudson.scm.CCUCMState.State;
 
 public class ClearCaseBuildNumberTemplate extends Template {
 
 	@Override
 	public String parse( State state, String args ) throws TemplateException {
 		
 		try {
 			Project project = state.getStream().getProject();
 			return BuildNumber.getBuildNumber(project);
 		} catch (Exception e) {
			throw new TemplateException( "Could not get version number from ClearCase" );
 		}
 	}
 
 }
