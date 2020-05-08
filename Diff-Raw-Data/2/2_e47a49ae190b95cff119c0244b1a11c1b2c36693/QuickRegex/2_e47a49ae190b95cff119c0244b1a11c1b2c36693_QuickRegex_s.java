 package business.EJB.util;
 
 import java.util.regex.Pattern;
 
 public class QuickRegex {
 	
	private static final Pattern p = Pattern.compile("[^a-zA-Z0-9áãâéêíóõôúçÁÃÂÉÊÍÓÕÔÚÇ ]");
 	private static final Pattern q = Pattern.compile("[^0-9]");
 	
 	public static boolean findAN(String s) {
 		return p.matcher(s).find();
 	}
 	
 	public static boolean findN(String s) {
 		return q.matcher(s).find();
 	}
 
 }
