 package test.pleocmd;
 
 import org.junit.BeforeClass;
 
 import pleocmd.Log;
 import pleocmd.Log.Type;
 
 public class Testcases { // CS_IGNORE not an utility class
 
 	@BeforeClass
 	public static void logOnlyWarnings() {
 		Log.setMinLogType(Type.Warn);
		Log.setGUIStatusKnown();
 	}
 
 }
