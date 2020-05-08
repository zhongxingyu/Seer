 package com.cheesymountain.woe;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import android.app.Activity;
 import android.content.Context;
 
 public class Database extends Activity {
 	
 	private static final String MY_EVERBIE = "everbie_data.txt";
 	
	public Database() throws FileNotFoundException, IOException{
		this.save();
	}
	
 	public void save() throws IOException, FileNotFoundException{
 		OutputStream os = openFileOutput(MY_EVERBIE, Context.MODE_PRIVATE);
 		OutputStreamWriter osw = new OutputStreamWriter(os);
 		Everbie everbie = Everbie.getEverbie();
 		String data = "\""+everbie.getName()+
 				"\""+Integer.toString(everbie.getHealth())+
 				"\""+Integer.toString(everbie.getFullness())+
 				"\""+Integer.toString(everbie.getToxicity());
 		osw.write(data);
 		os.close();
 	}
 	
	@SuppressWarnings("unused")
 	public void load() throws IOException, FileNotFoundException{
 		FileInputStream fis = openFileInput(MY_EVERBIE);
 		InputStreamReader fisReader = new InputStreamReader(fis);
 		char[] newData = null;
 		fisReader.read(newData);
 		if(newData == null || newData.length == 0){
			throw new RuntimeException();
 		}
		String dataString = null;
 		for (int i=0;i<newData.length;i++){
 			dataString = dataString+newData[i];
 		}
 	}
}
