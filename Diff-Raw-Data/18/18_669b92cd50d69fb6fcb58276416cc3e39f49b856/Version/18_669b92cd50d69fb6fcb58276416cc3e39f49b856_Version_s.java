 package net.praqma.hudson;
 
 /**
  * @author jssu
  * 
  */
 public class Version
 {
 	private static final String major    = "0"; // buildnumber.major
 	private static final String minor    = "3"; // buildnumber.minor
	private static final String patch    = "2-SNAPSHOT"; // buildnumber.patch
 	private static final String sequence = ""; // buildnumber.sequence
 	
 	public static final  String version  = major + '.' + minor + '.' + patch + ( sequence.length() > 0 ? '.' + sequence : "" );
 }
