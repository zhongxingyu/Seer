 package net.praqma.hudson.nametemplates;
 
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.hudson.exception.TemplateException;
 import net.praqma.hudson.remoting.RemoteUtil;
 import net.praqma.hudson.scm.CCUCMState.State;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ClearCaseVersionNumberTemplate extends Template {
 
 	private static Logger logger = Logger.getLogger( ClearCaseVersionNumberTemplate.class.getName() );
 	
 	@Override
 	public String parse( State state, String args ) throws TemplateException {
 
 		try {
 			logger.fine( "STREAM: " + state.getStream() );
 			logger.fine( "PROJECT: " + state.getStream().getProject() );
 			Project project = state.getStream().getProject();
 			return RemoteUtil.getClearCaseVersion( state.getWorkspace(), project );
 		} catch( Exception e ) {
 			logger.warning( "Getting cc version error: " + e.getMessage() );
			logger.log( Level.WARNING, "", e );
 			return "unknownccversion";
 		}
 	}
 
 }
