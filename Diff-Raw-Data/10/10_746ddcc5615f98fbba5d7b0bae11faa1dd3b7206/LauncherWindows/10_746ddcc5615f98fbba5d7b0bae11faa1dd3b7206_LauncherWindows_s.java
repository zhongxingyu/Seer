 package commandproxy.launcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 
 import commandproxy.core.Constants;
 
 public class LauncherWindows implements Constants{
 	private final static File AIR = new File( "air" );
 	private final static File PUBLISHER_ID = new File( "air\\META-INF\\AIR\\publisherid" );
 	
 	
 	public static Process exec( Vector<String> args ) throws IOException{
 		if( !AIR.exists() ){
 			Main.fail( "Air-Directory doesn't exist", E_AIR_APP_NOT_FOUND ); 
 		}
 		
 		// We really need this publisher-id file 
 		// to be able to run this! 
		if( !PUBLISHER_ID.exists() ){
			Main.generatePubID( PUBLISHER_ID );
 		}
 		
 		// So far so good!
 		File executable = null; 
 		for( File file : AIR.listFiles() ){
 			if( file.getName().endsWith( "-air.exe" ) ){
 				executable = file; 
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
