 package TweetBuilder;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;

 import android.content.Context;
 import android.util.Log;
 
 public class FileSystemActions {
 	//Saves the zip code as a file.
 	@SuppressWarnings("resource")
 	public static Boolean storeFile(Context context, String filename, String content, Boolean external) {
 		try{
			File file = new File("zip");
			file.delete();
		} catch(Exception e) {
			Log.e("FILE", "No zip file, continue with file creation!");
		}
		try{
 			File file;
 			FileOutputStream outputStream;
 			if(external) {
 				file = new File(context.getExternalFilesDir(null), filename);
 				outputStream = new FileOutputStream(file);
 			} else {
 				outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
 			}
 			outputStream.write(content.getBytes());
 			outputStream.close();
 		} catch(IOException e) {
 			Log.e("WRITE", filename);
 		}
 		return true;
 	}
 	//Reads the local zip file.
 	@SuppressWarnings("resource")
 	public static String readFile(Context context, String filename, Boolean external) {
 		String zip = "";
 		try{
 			File file;
 			FileInputStream fileInput;
 			if(external) {
 				file = new File(context.getExternalFilesDir(null), filename);
 				fileInput = new FileInputStream(file);
 			} else {
 				file = new File(filename);
 				fileInput = context.openFileInput(filename);
 			}
 			BufferedInputStream bis = new BufferedInputStream(fileInput);
 			byte[] contentBytes = new byte[1024];
 			int readBytes = 0;
 			StringBuffer buff = new StringBuffer();
 			while((readBytes = bis.read(contentBytes)) != -1) {
 				zip = new String(contentBytes, 0, readBytes);
 				buff.append(zip);
 			}
 			zip = buff.toString();
 			fileInput.close();
 		} catch(FileNotFoundException e) {
 			Log.e("READ", "File not found: " + filename);
 		} catch(IOException e) {
 			Log.e("READ", "I/O Error: ");
 		}
 		return zip;
 	}
 }
