 /*
  * ServeStream: A HTTP stream browser/player for Android
  * Copyright 2010 William Seemann
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.sourceforge.servestream.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.net.ssl.HttpsURLConnection;
 
 public class PLSPlaylistParser {
 	public final static String TAG = "ServeStream.PLSPlaylistParser";
 
 	private URL playlistURL = null;
     private ArrayList<MediaFile> playlistFiles = null;
     private int numberOfFiles = 0;
     
     private MediaFile mediaFile = null;
    private boolean processingEntry = false;
     
 	/**
 	 * Default constructor
 	 */
 	public PLSPlaylistParser(URL playlistURL) {
 		this.playlistURL = playlistURL;
 		this.playlistFiles = new ArrayList<MediaFile>();
 	}
 	
 	/**
 	 * Retrieves the files listed in a .pls file
 	 */
     public void retrievePLSFiles() {
     	
     	if (playlistURL == null)
     		return;
     	
 		HttpURLConnection conn = null;
         String line = null;
         BufferedReader reader = null;;
         
         try {
         	
         	if (playlistURL.getProtocol().equalsIgnoreCase("http")) {
         		conn = (HttpURLConnection) playlistURL.openConnection();
         	} else if (playlistURL.getProtocol().equalsIgnoreCase("https")) {
         		conn = (HttpsURLConnection) playlistURL.openConnection();        		
         	}
         	
 		    conn.setRequestMethod("GET");
 		    
 		    // Start the query
 		    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		    conn.connect();
		    
 		    while ((line = reader.readLine()) != null) {
 		    	if (!line.trim().equals("")) {
 			    	String [] parsedLine = line.split("\\=");
 			    	
 			    	if (parsedLine.length == 2) {
                         if (parsedLine[0].trim().contains("File")) {
                        	if (processingEntry)
                                 savePlaylistFile();
                        	else
                        		processingEntry = true;
                        	
         		    		mediaFile = new MediaFile();
                             mediaFile.setURL(parsedLine[1].trim());
                         } else if (parsedLine[0].trim().contains("Title")) {
                             
                         } else if (parsedLine[0].trim().contains("Length")) {
                             savePlaylistFile();
                         }			
 			    	}
 		    	}           
             }
		    
		    // added in case the file doesn't follow the standard pls
		    // structure:
		    // FileX:
		    // TitleX:
		    // LengthX:
        	if (processingEntry) {
                savePlaylistFile();
        	}
 
         } catch (Exception ex) {
         	ex.printStackTrace();
         } finally {
         	closeReader(reader);
         	closeHttpConnection(conn);
         }
     }
     
     public void savePlaylistFile() {
     	numberOfFiles = numberOfFiles + 1;
     	mediaFile.setTrackNumber(numberOfFiles);
     	playlistFiles.add(mediaFile);
    	processingEntry = false;
     }
 
 	/**
 	 * @return the playlistFiles
 	 */
 	public ArrayList<MediaFile> getPlaylistFiles() {
 		return playlistFiles;
 	}
 
 	/**
 	 * @return the numberOfFiles
 	 */
 	public int getNumberOfFiles() {
 		return numberOfFiles;
 	}
 	
 	/**
 	 * Closes a BufferedReader
 	 * 
 	 * @param reader The reader to close
 	 */
     private void closeReader(BufferedReader reader) {
     	
     	if (reader == null)
     		return;
 
     	try {
     	    reader.close();
     	} catch (IOException ex) {
     		
     	}
     }
     
 	/**
 	 * Closes a HttpURLConnection
 	 * 
 	 * @param conn The connection to close
 	 */
     private void closeHttpConnection(HttpURLConnection conn) {
     	
     	if (conn == null)
     		return;
     	
     	conn.disconnect();
     }
 }
 
