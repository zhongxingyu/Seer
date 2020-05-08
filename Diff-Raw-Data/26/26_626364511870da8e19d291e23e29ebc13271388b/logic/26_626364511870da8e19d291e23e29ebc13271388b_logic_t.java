 import java.io.*;
 import java.util.Vector;
 
 public class logic {
	private Vector<verdict> classificationResult;
 	private File path;
 	
 	public logic( String inPath ) {
 		path = new File( inPath );
		classificationResult = new Vector<verdict>();
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
 				
				if ( pprocess.start() != 0 ) {
					misc.log( "Error: preprocess failed for file: " + inPath.getAbsolutePath() );
					System.exit( 1 );
				}
				
				classifier cfier = new classifier( "/some/where/classifier.model", pprocess.getInstances() );
				cfier.start();
				classificationResult.add( new verdict( inPath.getAbsolutePath(), cfier.getVerdict() ) );
 			}
 		} else {
 			misc.log( "Info: " + inPath.getAbsolutePath() + "is neither file nor directory." );
 		}
 	}
 	
	public Vector<verdict> getVerdict() {
		return classificationResult;
 	}
 }
