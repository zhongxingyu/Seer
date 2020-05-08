 import java.io.*;
 import java.util.Vector;
 
 public class logic {
	private Vector<String> verdict;
 	private File path;
 	
 	public logic( String inPath ) {
 		path = new File( inPath );
		verdict = new Vector<String>();
 	}
 
 	public int start() {		
 		
 		traverse( path );
 		
 		return 0;
 	}
 	
 	private boolean isFileOK( File inPath) {
 		
 		if ( inPath.canRead() ) {
 			// Also check if file has supported file suffix, e.g. .exe, .msi ...
 			return true;
 		}
 		else {
 			misc.log( "Info: " + inPath.getAbsolutePath() + "is not readable." );
 			return false;
 		}
 	}
 	
 	private void traverse( File inPath ) {
 		if ( inPath.isDirectory() ) {
 			File fileList[] = inPath.listFiles();
 			
 			for (int count=0; count < fileList.length; count++ ) {
 				traverse( fileList[ count ] );
 			}
 		} else if ( inPath.isFile() ) {
 			if ( isFileOK( inPath ) ) {				
 				extractor Xtractor = new extractor( inPath.getAbsolutePath() );
 				preprocess pprocess = new preprocess( Xtractor.getLicense() );
				pprocess.start();
				classifier cfier = new classifier( pprocess.getInstances() );
				cfier.start();
 				
				//Temporarily just adding path to verdict vector
				verdict.add( inPath.getAbsolutePath() );
				//verdict.add( cfier.getVerdict() );
 			}
 		} else {
 			misc.log( "Info: " + inPath.getAbsolutePath() + "is neither file nor directory." );
 		}
 	}
 	
	public Vector<String> getVerdict() {
		return verdict;
 	}
 }
