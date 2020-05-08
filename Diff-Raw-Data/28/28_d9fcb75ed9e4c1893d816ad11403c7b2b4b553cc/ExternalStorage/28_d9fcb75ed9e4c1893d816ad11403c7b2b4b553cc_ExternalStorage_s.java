 package no.ntnu.falldetection.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import android.os.Environment;
 import android.util.Log;
 
 public class ExternalStorage {
 	
 	private FileWriter fw;
 	private BufferedWriter bw;
 	
 	private String filePostfix;
 	private String textPrefix;
 	
 	private File root = Environment.getExternalStorageDirectory();
 	private File falldir;
 	
 	private SimpleDateFormat fileFormat = new SimpleDateFormat("MM-dd-HH-mm");
	private SimpleDateFormat textFormat = new SimpleDateFormat("HH-mm-ss");
 	
 	private Calendar cal = Calendar.getInstance();
 	
 	public ExternalStorage(){
 		falldir = new File(root + "/" + "falldetection");
 		if(!falldir.exists()){
 			falldir.mkdir();
 			Log.e("storage", "Directory created!");
 		}
 		
 		filePostfix = fileFormat.format(cal.getTime());
 		
 		//Create a new file for the session, using Month-day-hour-minute as a unique file identifier.
 			try {
 				fw = new FileWriter(falldir + "/" + "accellog" + filePostfix + ".txt");
 				bw = new BufferedWriter(fw);
 				bw.write("Initial test write");
 				bw.newLine();
 			} catch (IOException e) {
 				Log.e("storage","Failed to write to file");
 				e.printStackTrace();
 			}	
 	}
 	
 	public void writeToFile(String text){
 		try {
 			textPrefix = textFormat.format(cal.getTime());
 			bw.write(textPrefix + ": " + text);
 			bw.newLine();
 		} catch (IOException e) {
 			Log.e("storage", "Unable to write to file");
 			e.printStackTrace();
 		}
 	}
 }
