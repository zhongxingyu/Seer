 /*
 	Java version checker
 	@author Sam Saint-Pettersen, 2010
 	Released into the public domain
 	
 	Originally written for use with 
 	the NSIS installation script for Gaudi
 	
 	But use as you like. No warranty.
 	
 	Usage: java -cp . JavaCheck
 	Output: x.x.x_x (Java version)
 	Exit code: -1 (neither true or false)
 	
 	Usage: java -cp . JavaCheck <desired version>
 	Output: x.x.x_x (Java version)
	Exit code: 0 (false) / 1 (true)(Java installed, is desired version or greater)
 */
 import java.util.regex.*;
 
 class JavaCheck {
 	public static void main(String[] args) {
 		short returned = 0; // Return exit code 0 for false, default assumption
 		String detectedVer = System.getProperty("java.version");
 		System.out.println(detectedVer);
 		// When minimal version argument is provided, return either exit code 1 or 0
 		if(args.length == 1) {
 			Pattern p = Pattern.compile("\\d\\.\\d");
 			Matcher m = p.matcher(detectedVer); m.find();
 			double minimalVersion = Double.valueOf(args[0]).doubleValue();
 			double detec_versionNum = Double.valueOf(m.group(0)).doubleValue();
 			if(detec_versionNum >= minimalVersion) {
 				returned = 1; // Return exit code 1 for true
 			}
 			System.exit(returned);
 		}
 		// Otherwise, just display detected version 
 		// and return exit code -1 for neither true or false
 		else System.exit(-1);
 	}
 }
