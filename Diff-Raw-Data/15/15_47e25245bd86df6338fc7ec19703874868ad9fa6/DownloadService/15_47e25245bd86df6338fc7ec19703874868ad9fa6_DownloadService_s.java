 package kap.placity_beta;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class DownloadService extends IntentService{
 
 	private String id;
 	private int counter;
 	
 	public DownloadService() {
 		super("IntentService");
 	}
 	
 	//Get given game-ID from starting intent
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		Bundle b = intent.getExtras();
         id = b.getString("ID");
 	    return super.onStartCommand(intent,flags,startId);
 	}
 
 	//Download of filelist and saving of individual files
 	@Override
 	protected void onHandleIntent(Intent intent) {   
 		counter = 0;
 		String files;
 		files = "";
 		try {
 			files = ServerInterface.getFileList(id);
 			Log.v("DS filelist", files);
 	        for (String list : files.split(";")){
 				save(id, list); //Save files from list
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} //Returns list of download links
		Log.v("test",""+files.split(";").length);
 		while (counter < files.split(";").length) { 
         SystemClock.sleep(200);
 		}
         Intent i = new Intent();
         i.setAction("DownloadStep.ONE");
         sendBroadcast(i);
 	}
 	
 	//Method for saving
 	private void save(String id, String filelink) {
 		Log.v("save", "fetching " + filelink.substring(filelink.lastIndexOf("/")+1));
 		
 		File dir = new File(getApplicationContext().getDir("games", Context.MODE_PRIVATE).getAbsolutePath() + File.separator + id); //Set base directory for game
 		Log.v("game pfad", getApplicationContext().getDir("games", Context.MODE_PRIVATE).getAbsolutePath() + File.separator + id); 
 		
 		dir.delete(); //Clear folder
 		dir.mkdirs(); //Create folder again
 		
 		String name = filelink.substring(filelink.lastIndexOf("/")+1);//Extract filename from link
 		if (name.contains("gametoxml")){ //The xml direct download is saved as source
 			name = "source.xml";
 		};
 		File file = new File(dir, name); //Create a file in that directory
 		
 		try{
 		FileOutputStream myFile = new FileOutputStream(file);
 		myFile.write(ServerInterface.getGameFile(id, filelink));
 		myFile.close();}
 		catch (Exception e){
			Log.v("save error",e.getMessage());
 		}
 		counter += 1;
 		
 	}
 
 }
