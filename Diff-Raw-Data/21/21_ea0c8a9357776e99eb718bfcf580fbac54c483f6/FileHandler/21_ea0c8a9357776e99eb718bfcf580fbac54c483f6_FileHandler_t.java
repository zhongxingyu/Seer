 package lib;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
import android.util.Log;

 
 public class FileHandler {
 
 	public static boolean searchFile(FileInputStream fis, String searchStr) throws IOException{
		System.out.println("searchString: " + searchStr);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		String str = null;
		Log.d("a", "************************************");
 		while((str = br.readLine()) != null) {
			Log.d("b", str);
 			if(str.startsWith(searchStr)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void writeFile(FileOutputStream fos, String writeString) throws IOException {
		Log.d("************************************", "msg");
		Log.d("writeFile: ", writeString);
		fos.write(writeString.getBytes());
 	}
 }
