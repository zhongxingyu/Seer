 package com.pigote.dpfinal;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.TextView;
 
 import com.pigote.dpfinal.MWReaderXmlParser.Entry;
 
 public class DoRead extends AsyncTask<String, String, String>{
 	
 	private TextView translated;
 	private List<Entry> entries = new ArrayList<Entry>();
 	private String[] originalString;
 	@Override
 	protected String doInBackground(String... params) {
 		// params comes from the execute() call: params[0] is the url.
         try {
             return goRead(params[0]);
         } catch (Exception e) {
         	//toastMsg("Unable to connect to reading service");
         	Log.d("myDebug", "goRead Exception! " + e.getMessage());
             return null;
         }
 	}
 	
     // onPostExecute displays the results of the AsyncTask.
     @Override
     protected void onPostExecute(String result) {
         if (result!=null){
         	//make array from db of audio files in local storage
         	//play file array
         	int i = 0;
         	List<Uri> toPlay = new ArrayList<Uri>();
         	while(i<originalString.length){
         		toPlay.add(DPfinal.getDBHandler().getUri(originalString[i++]));
         	}
        	Intent intent = new Intent(Intent.ACTION_VIEW, toPlay.get(0)); 
         	//TODO START HERE get fileloc from cursor, play wav list with intent
         	//startActivity(intent);
         	Activity activity = DPfinal.getActivity();
         	translated = (TextView) activity.findViewById(R.id.translatedText);
         	translated.setText("Array played");
         }
     }
    
      // Do the web service tango here
     private String goRead(String toRead) throws Exception {
     	originalString = toRead.split(" ");
     	// Query local db, fill array of missing words
     	String[] missing = DPfinal.getDBHandler().getMissingWords(toRead);
     	// go online, find missing words and add to db
     	InputStream is = null;
     	MWReaderXmlParser xmlParser = new MWReaderXmlParser();
     	
     	final String pre = "http://www.dictionaryapi.com/api/v1/references/collegiate/xml/";
     	final String post = "?key=df7d3120-f7c0-4150-bcf4-93e04f72f6db";
         	        
         for (int i = 0; i<missing.length; i++){
         	if (!missing[i].equals("*")){
 	        	try {
 	        		URL url = new URL(pre+missing[i].toLowerCase(Locale.ENGLISH)+post);
 	        		//URL url = new URL(pre+"the"+post);
 	        		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	        		conn.setReadTimeout(10000 /* milliseconds */);
 		            conn.setConnectTimeout(15000 /* milliseconds */);
 		            conn.setRequestMethod("GET");
 		            conn.setDoInput(true);
 		            // Starts the query
 		            conn.connect();
 		            int response = conn.getResponseCode();
 		            Log.d("myDebug", "Trying to get : " + missing[i].toLowerCase(Locale.ENGLISH));
 		            if (response==200){
 		            	is = conn.getInputStream();
			            entries.add(xmlParser.parse(is));
			            entries.get(i).addWord(missing[i].toLowerCase(Locale.ENGLISH));
			            storeWavToExternal(entries.get(i));
 		            } else {
 		            	Activity activity = DPfinal.getActivity();
 		            	translated = (TextView) activity.findViewById(R.id.translatedText);
 		            	translated.setText("Connection error" + response);
 		            }
 		            conn.disconnect();
 		            
 		         } finally {
 		             if (is != null) {
 		                 is.close();
 		             } 
 		         }
         	}
         } 
         
        for (int w =0; w<entries.size(); w++){
         	DPfinal.getDBHandler().addEntry(entries.get(w));
         }
         return toRead;
      }
 
 	private void storeWavToExternal(Entry entry) {
 		try {
 	        //set the download URL, a url that points to a file on the internet
 	        //this is the file to be downloaded
 	        URL url = entry.sound;
 
 	        //create the new connection
 	        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
 
 	        //set up some things on the connection
 	        urlConnection.setRequestMethod("GET");
 	        urlConnection.setDoOutput(true);
 
 	        //and connect!
 	        urlConnection.connect();
 
 	        //set the path where we want to save the file
 	        //in this case, going to save it on the root directory of the
 	        //sd card.
 	        File SDCardDir = DPfinal.getActivity().getExternalFilesDir("wavs");
 	        
 	        //check that SDCardDir is not null (we have SDCard!!)
 	        if (SDCardDir==null)
 	        	throw new IOException("SD CARD DIR == NULL");
 	        
 	        //create a new file, specifying the path, and the filename
 	        //which we want to save the file as.
 	        File file = new File(SDCardDir,entry.word+".wav");
 
 	        //this will be used to write the downloaded data into the file we created
 	        FileOutputStream fileOutput = new FileOutputStream(file);
 
 	        //this will be used in reading the data from the internet
 	        InputStream inputStream = urlConnection.getInputStream();
 
 	        //this is the total size of the file
 	        //int totalSize = urlConnection.getContentLength();
 	        //variable to store total downloaded bytes
 	        //int downloadedSize = 0;
 
 	        //create a buffer...
 	        byte[] buffer = new byte[1024];
 	        int bufferLength = 0; //used to store a temporary size of the buffer
 
 	        //now, read through the input buffer and write the contents to the file
 	        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
 	                //add the data in the buffer to the file in the file output stream (the file on the sd card
 	                fileOutput.write(buffer, 0, bufferLength);
 	                //add up the size so we know how much is downloaded
 	                //downloadedSize += bufferLength;
 	        }
 	        //close the output stream when done
 	        fileOutput.close();
 	        URL myURL = new URL("http://"+file.getAbsolutePath());
 	        entry.sound = myURL;
 		//catch some possible errors...
 		} catch (MalformedURLException e) {
 			Log.d("myDebug", "Malformed URL Exc!" + e.toString());
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.d("myDebug", "IO Exc!" + e.toString());
 		    e.printStackTrace();
 		}
 		
 	}
 }
