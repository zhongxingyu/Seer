 package commandproxy.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import commandproxy.core.Constants;
 
 public class LauncherWindows implements Constants{
 	
 	public static Process exec( Vector<String> args ) throws IOException{
 		if( !new File( "air" ).exists() ){
 			Main.fail( "Air-Directory doesn't exist", E_AIR_APP_NOT_FOUND ); 
 		}
 		
 		File executable = null; 
 		for( File dir : new File( "air" ).listFiles() ){
 			File tmp = new File( dir, dir.getName() + ".exe" ); 
 			if( dir.isDirectory() && tmp.exists() ){
 				executable = tmp; 
 				break; 
 			}
 		}
 		
 		if( executable == null ){
 			Main.fail( "Air executable was not found", E_AIR_APP_NOT_FOUND ); 
 		}
 
 		args.add( 0, executable.getAbsolutePath() ); 
 		
 		return Runtime.getRuntime().exec(
			args.toArray( new String[]{} ) 
 		); 
 	}
 }
