 package net.praqma.hudson;
 
 /**
  * @author jssu
  *
  */
 public class Version
 {
 	private static final String major    = "1"; // buildnumber.major
 	private static final String minor    = "0"; // buildnumber.minor
	private static final String patch    = "5"; // buildnumber.patch
 	private static final String sequence = ""; // buildnumber.sequence
 
 	public static final  String version  = major + '.' + minor + '.' + patch + ( sequence.length() > 0 ? '.' + sequence : "" );
 }
