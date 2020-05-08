 package ohayon.android.show;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import ohayon.android.fireworks.FireworkAndTime;
 import android.content.Context;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 public class SavedShow {
 
 	private Gson gson;
 	private ArrayList<FireworkAndTime> fireworks;
 
 	public SavedShow() {
 		this.gson = new Gson();
 		this.fireworks = new ArrayList<FireworkAndTime>();
 	}
 
 	public void addFireworks(ArrayList<FireworkAndTime> fats) {
 		fireworks.addAll(fats);
 	}
 
 	public void saveShow(Context context) {
 		String gsonStr = gson.toJson(fireworks);
 		FileOutputStream outStream;
 		try {
 			outStream = context.openFileOutput("savedShow.NOFW", Context.MODE_PRIVATE);
 			outStream.write(gsonStr.getBytes());
 			outStream.flush();
 			outStream.close();
 		} catch (IOException e) {
 			Log.e("IOEXception SavedShow.java", e.getMessage());
 		}
 	}
 	
 	public ArrayList<FireworkAndTime> getSavedShow(Context context){
 		ArrayList<FireworkAndTime> fats = new ArrayList<FireworkAndTime>();
 		String gsonStr = null;
 		try {
 			FileInputStream inStream = context.openFileInput("savedShow.NOFW");
 			InputStreamReader inputReader = new InputStreamReader(inStream);
 			BufferedReader buffer = new BufferedReader(inputReader);
 			StringBuilder strBuilder = new StringBuilder();
 			
 			while ( (gsonStr = buffer.readLine()) != null){
 				strBuilder.append(gsonStr);
 			}
 			
 			inStream.close();
 		} catch (FileNotFoundException e) {
 			Log.e("FileNotFoundEception SavedShow.java", e.getMessage());
 		} catch (IOException e) {
 			Log.e("IOEXception SavedShow.java", e.getMessage());
 		}
 		
		fats = gson.fromJson(gsonStr, ArrayList.class);
 		return fats;
 	}
 }
