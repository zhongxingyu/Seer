 package proj.Position;
 
import java.io.File;

 import android.R.integer;
 import android.R.string;
import android.os.Environment;
 
 public class Global {
	public final static File SDPathRoot = Environment.getExternalStorageDirectory();
	
 	public final static String MapDirName = "Position";
	//public final static String SDPathRoot = "/";
 	public final static String MapDirRoot = SDPathRoot + MapDirName + "/";
 	// public static int x = 100;
 	public static String MapId = null;
 	// static String LastJson = "last.json";
 	public static String JsonObjNow = null;
 	public static String PointId = null;
 
 }
