 package gov.cdc.pttadvisor;
 
 import java.io.FileInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.io.BufferedInputStream;
 
 import android.os.AsyncTask;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.util.Log;
 import android.util.SparseArray;
 import android.widget.Toast;
 import android.content.Context;
 
 public class JsonUpdaterTask extends AsyncTask <Void, Void, String> {
 
	private String UPDATEURL = "https://raw.github.com/androidpttadvisor/androidpttadvisor/master/AndroidPTTAdvisor/assets/DTNode.json";
 	// for demo purposes; to be removed later.
	//private String UPDATEURL = "http://gibson.somethingsimple.org/grail.json";
 	private MainView context;
 	
 	public JsonUpdaterTask (Context context) {
 		this.context = (MainView) context;
 	}
 	
 	@Override
 	protected String doInBackground(Void... params) {
 		String webJsonString = null;
 		try {
 			/**
 			 * Pull the remote JSON and save a copy locally
 			 */
 			URL url = new URL(UPDATEURL);
 			URLConnection urlConn = url.openConnection();
 			BufferedInputStream inStream = new BufferedInputStream(urlConn.getInputStream());
 			byte[] data = new byte[512];
 	        int bytesRead = 0;
 			OutputStream outStream = this.context.openFileOutput("jsonFromWeb.json", Context.MODE_PRIVATE);
 	        while((bytesRead = inStream.read(data, 0, data.length)) >= 0) {
 	            outStream.write(data, 0, bytesRead);
 	        }
 			inStream.close();
 			outStream.close();
 			Log.d("JSON Updater", "Pulled new JSON from " + UPDATEURL);
 			 
 			/**
 			 * Read this new JSON as a String
 			 */
 	        webJsonString = fileToString("jsonFromWeb.json");
 			
 	        return webJsonString;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Read a file and spit it out as a String
 	 * @param filename Name of file to read
 	 * @return The String version of that file
 	 */
 	private String fileToString(String filename) {
 		String outString = null;
 		FileInputStream file;
         try {
         	file = this.context.openFileInput(filename);
             byte[] data = new byte[file.available()];
             file.read(data);
             file.close();
             outString = new String(data);
         } catch (IOException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         } 
 		return outString;
 	}
 
 	/**
 	 * Replace a file in local app storage-- intended to replace the old JSON with the new one from the web
 	 * @param source new file
 	 * @param target the file we're replacing with the new one
 	 */
 	private void replaceLocalJson(String source, String target) {
 		File sourceFile = context.getFileStreamPath(source);
 		File targetFile = new File(sourceFile.getParent(), target);
 	    if (targetFile.exists()) {
 	        context.deleteFile(target);        
 	    }
 	    sourceFile.renameTo(targetFile);
 	}
 	
 	/**
 	 * This gets called after doInBackground returns the webJsonString from the web
 	 * @param webJsonString JSON string from the web
 	 */
 	@Override
 	protected void onPostExecute(String webJsonString) {
 		Log.d("JSON Updater", "onPostExecute() got called");
 		
 		/* read in the existing local version of DTNode.json as a string */
 		String localJsonString = fileToString("DTNode.json");
         
 		/**
 		 * Attempt to parse the webJsonString as pttnodes.  If the size of pttnodes is 1+,
 		 * then the parsing was successful and we assume the data is good.
 		 */
 		SparseArray<PTTNode> pttnodes = new JsonHandler(context).parseJson(webJsonString);
 		if (pttnodes.size() == 0) {
 			Log.d("JSON Updater", "JSON from web seems invalid-- disregarding.");
 			return;
 		} else if (webJsonString != null && webJsonString.equals(localJsonString)) {
         	Log.d("JSON Updater", "Web version matches local version!");
         } else {
         	Log.d("JSON Updater", "Web version does not match local version!");
         	AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
             .setTitle("Update Available")
             .setMessage("There is an updated algorithm available.  Would you like to install it and restart PTT Advisor?")
             .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     replaceLocalJson("jsonFromWeb.json", "DTNode.json");
                     Toast toast = Toast.makeText(context, "Updated Algorithm", Toast.LENGTH_SHORT);
                     toast.show();
                     context.initialize();
                 }
             })
             .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
 
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                 	Toast toast = Toast.makeText(context, "Ignoring update", Toast.LENGTH_SHORT);
                     toast.show();
                 }
 
             });
             builder.create().show();
         }
 	}
 
 }
