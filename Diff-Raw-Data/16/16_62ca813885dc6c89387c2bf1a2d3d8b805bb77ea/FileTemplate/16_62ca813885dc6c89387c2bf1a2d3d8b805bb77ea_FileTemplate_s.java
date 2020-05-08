 package net.praqma.hudson.nametemplates;
 
 import hudson.FilePath;
 import java.io.File;
 import java.util.logging.Logger;
 import net.praqma.hudson.CCUCMBuildAction;
 
 public class FileTemplate extends Template {
 	
	private Logger logger = Logger.getLogger( FileTemplate.class.getName() );
 
 	@Override
 	public String parse( CCUCMBuildAction action, String filename ) {
 		try {
			FilePath file = new FilePath(new File(action.getWorkspace() + filename) );
			logger.fine( "FILE: " + file );
 			return file.readToString().trim();
		} catch( Exception e ) {
			
			logger.fine( "E: " + e.getMessage() );
 			return "?";
 		}
 	}

 }
