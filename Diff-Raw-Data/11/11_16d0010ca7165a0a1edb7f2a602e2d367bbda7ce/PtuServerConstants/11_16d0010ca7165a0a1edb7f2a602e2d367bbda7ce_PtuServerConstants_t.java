 package ch.cern.atlas.apvs.ptu.server;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
import java.util.TimeZone;
 
public class PtuServerConstants {
	public static final TimeZone timeZone = TimeZone.getTimeZone("CEST");
 	public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 	public static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	public static final String oracleFormat = "'YYYY-MM-DD HH24:MI:SS'";
	
	static {
		dateFormat.setTimeZone(timeZone);
		timestampFormat.setTimeZone(timeZone);
	}
 }
