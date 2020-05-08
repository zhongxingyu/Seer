 package net.praqma.hudson.nametemplates;
 
 import hudson.FilePath;
 import java.io.File;
 import java.util.logging.Logger;
 import net.praqma.hudson.CCUCMBuildAction;
 
 public class FileTemplate extends Template {
 	
	private static final Logger logger = Logger.getLogger( FileTemplate.class.getName() );
 
 	@Override
 	public String parse( CCUCMBuildAction action, String filename ) {
 		try {
			FilePath file = new FilePath(new File(action.getWorkspace() + System.getProperty("file.separator") + filename) );            
            logger.fine( String.format( "Full path to the remote %s", file.absolutize() ) );
			logger.fine( String.format( "FILE: %s", file ) );
 			return file.readToString().trim();
		} catch( Exception e ) {			
			logger.fine( "E: " + e.getMessage() );            
 			return "?";
 		}
 	}
 }
