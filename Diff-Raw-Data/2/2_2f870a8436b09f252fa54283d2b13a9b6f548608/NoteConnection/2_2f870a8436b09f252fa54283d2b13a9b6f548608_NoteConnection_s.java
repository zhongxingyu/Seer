 package org.openstreetmap.josm.plugins.notes.api.util;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.coor.LatLon;
 import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
 import org.openstreetmap.josm.gui.progress.ProgressMonitor;
 import org.openstreetmap.josm.io.ChangesetClosedException;
 import org.openstreetmap.josm.io.OsmApi;
 import org.openstreetmap.josm.io.OsmApiException;
 import org.openstreetmap.josm.io.OsmApiPrimitiveGoneException;
 import org.openstreetmap.josm.io.OsmConnection;
 import org.openstreetmap.josm.io.OsmTransferCanceledException;
 import org.openstreetmap.josm.io.OsmTransferException;
 import org.openstreetmap.josm.plugins.notes.Note;
 import org.openstreetmap.josm.plugins.notes.NotesXmlParser;
 import org.openstreetmap.josm.tools.Utils;
 import org.openstreetmap.josm.data.Bounds;
 import org.xml.sax.SAXException;
 
 public class NoteConnection extends OsmConnection {
 	
 	private static String version = "0.6";
 	private String serverUrl;
 	private static NoteConnection instance;
 	
 	
 	public List<Note> getNotesInBoundingBox(Bounds bounds) throws OsmTransferException {
 		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
 		
 		String url = new StringBuilder()
 			.append("notes?bbox=")
 			.append(bounds.getMin().lon())
 			.append(",").append(bounds.getMin().lat())
             .append(",").append(bounds.getMax().lon())
             .append(",").append(bounds.getMax().lat())
             .toString();
 		String response = sendRequest("GET", url, null, monitor, false, true);
 		return parseNotes(response);
 	}
 	
 	
 	public void closeNote(Note note, String closeMessage) throws OsmTransferException {
 		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
 		StringBuilder urlBuilder = new StringBuilder()
 			.append("notes/")
 			.append(note.getId())
 			.append("/close");
 		if(closeMessage != null && !closeMessage.trim().isEmpty()) {
 			urlBuilder.append("?text=");
 			urlBuilder.append(closeMessage);
 		}
 		
 		sendRequest("POST", urlBuilder.toString(), null, monitor, true, false);
 	}
 	
 	public Note createNote(LatLon latlon, String text) throws OsmTransferException {
 		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
 		String url = new StringBuilder()
			.append("note?lat=")
 			.append(latlon.lat())
 			.append("&lon=")
 			.append(latlon.lon())
 			.append("&text=")
 			.append(text).toString();
 		
 		String response = sendRequest("POST", url, null, monitor, true, false);
 		List<Note> newNote = parseNotes(response);
 		if(newNote.size() != 0) {
 			return newNote.get(0);
 		}
 		return null;
 	}
 	
 	public Note AddCommentToNote(Note note, String comment) throws OsmTransferException {
 		if(comment == null || comment.trim().isEmpty()) {
 			return note;
 		}
 		ProgressMonitor monitor = NullProgressMonitor.INSTANCE;
 		String url = new StringBuilder()
 			.append("notes/")
 			.append(note.getId())
 			.append("/comment?text=")
 			.append(comment).toString();
 		
 		String response = sendRequest("POST", url, null, monitor, true, false);
 		List<Note> modifiedNote = parseNotes(response);
 		if(modifiedNote.size() !=0) {
 			return modifiedNote.get(0);
 		}
         return note;
 	}
 	
 	private List<Note> parseNotes(String notesXml) {
 		try {
             return NotesXmlParser.parseNotes(notesXml);
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (IOException e) {
         	e.printStackTrace();
         }
 		return new ArrayList<Note>();
 		
 	}
 	
 	/**
 	 * The rest of this class is basically copy/paste from OsmApi.java in core. Since the 
 	 * methods are private/protected, I copied them here. Hopefully notes functionality will
 	 * be integrated into core at some point since this is now a core OSM API feature 
 	 * and this copy/paste business can go away.
 	 */
 	
 	protected NoteConnection(String serverUrl) {
 		this.serverUrl = serverUrl;
 	}
 	
     static public NoteConnection getNoteConnection(String serverUrl) {
     	if(instance == null) {
     		instance = new NoteConnection(serverUrl);
     	}
     	return instance;
     }
 	
 	public static NoteConnection getNoteConnection() {
 		String serverUrl = Main.pref.get("osm-server.url", OsmApi.DEFAULT_API_URL);
         if (serverUrl == null)
             throw new IllegalStateException(tr("Preference ''{0}'' missing. Cannot initialize OsmApi.", "osm-server.url"));
         return getNoteConnection(serverUrl);
 	}
 	
     private String sendRequest(String requestMethod, String urlSuffix,String requestBody, ProgressMonitor monitor, boolean doAuthenticate, boolean fastFail) throws OsmTransferException {
         StringBuffer responseBody = new StringBuffer();
         int retries = fastFail ? 0 : getMaxRetries();
         while(true) { // the retry loop
             try {
                 URL url = new URL(new URL(getBaseUrl()), urlSuffix);
                 System.out.print(requestMethod + " " + url + "... ");
                 // fix #5369, see http://www.tikalk.com/java/forums/httpurlconnection-disable-keep-alive
                 activeConnection = Utils.openHttpConnection(url, false);
                 activeConnection.setConnectTimeout(fastFail ? 1000 : Main.pref.getInteger("socket.timeout.connect",15)*1000);
                 if (fastFail) {
                     activeConnection.setReadTimeout(1000);
                 }
                 activeConnection.setRequestMethod(requestMethod);
                 if (doAuthenticate) {
                     addAuth(activeConnection);
                 }
 
                 if (requestMethod.equals("PUT") || requestMethod.equals("POST") || requestMethod.equals("DELETE")) {
                     activeConnection.setDoOutput(true);
                     activeConnection.setRequestProperty("Content-type", "text/xml");
                     OutputStream out = activeConnection.getOutputStream();
 
                     // It seems that certain bits of the Ruby API are very unhappy upon
                     // receipt of a PUT/POST message without a Content-length header,
                     // even if the request has no payload.
                     // Since Java will not generate a Content-length header unless
                     // we use the output stream, we create an output stream for PUT/POST
                     // even if there is no payload.
                     if (requestBody != null) {
                         BufferedWriter bwr = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                         bwr.write(requestBody);
                         bwr.flush();
                     }
                     Utils.close(out);
                 }
 
                 activeConnection.connect();
                 System.out.println(activeConnection.getResponseMessage());
                 int retCode = activeConnection.getResponseCode();
 
                 if (retCode >= 500) {
                     if (retries-- > 0) {
                         sleepAndListen(retries, monitor);
                         System.out.println(tr("Starting retry {0} of {1}.", getMaxRetries() - retries,getMaxRetries()));
                         continue;
                     }
                 }
 
                 // populate return fields.
                 responseBody.setLength(0);
 
                 // If the API returned an error code like 403 forbidden, getInputStream
                 // will fail with an IOException.
                 InputStream i = null;
                 try {
                     i = activeConnection.getInputStream();
                 } catch (IOException ioe) {
                     i = activeConnection.getErrorStream();
                 }
                 if (i != null) {
                     // the input stream can be null if both the input and the error stream
                     // are null. Seems to be the case if the OSM server replies a 401
                     // Unauthorized, see #3887.
                     //
                     BufferedReader in = new BufferedReader(new InputStreamReader(i));
                     String s;
                     while((s = in.readLine()) != null) {
                         responseBody.append(s);
                         responseBody.append("\n");
                     }
                 }
                 String errorHeader = null;
                 // Look for a detailed error message from the server
                 if (activeConnection.getHeaderField("Error") != null) {
                     errorHeader = activeConnection.getHeaderField("Error");
                     System.err.println("Error header: " + errorHeader);
                 } else if (retCode != 200 && responseBody.length()>0) {
                     System.err.println("Error body: " + responseBody);
                 }
                 activeConnection.disconnect();
 
                 errorHeader = errorHeader == null? null : errorHeader.trim();
                 String errorBody = responseBody.length() == 0? null : responseBody.toString().trim();
                 switch(retCode) {
                 case HttpURLConnection.HTTP_OK:
                     return responseBody.toString();
                 case HttpURLConnection.HTTP_GONE:
                     throw new OsmApiPrimitiveGoneException(errorHeader, errorBody);
                 case HttpURLConnection.HTTP_CONFLICT:
                     if (ChangesetClosedException.errorHeaderMatchesPattern(errorHeader))
                         throw new ChangesetClosedException(errorBody, ChangesetClosedException.Source.UPLOAD_DATA);
                     else
                         throw new OsmApiException(retCode, errorHeader, errorBody);
                 case HttpURLConnection.HTTP_FORBIDDEN:
                     OsmApiException e = new OsmApiException(retCode, errorHeader, errorBody);
                     e.setAccessedUrl(activeConnection.getURL().toString());
                     throw e;
                 default:
                     throw new OsmApiException(retCode, errorHeader, errorBody);
                 }
             } catch (UnknownHostException e) {
                 throw new OsmTransferException(e);
             } catch (SocketTimeoutException e) {
                 if (retries-- > 0) {
                     continue;
                 }
                 throw new OsmTransferException(e);
             } catch (ConnectException e) {
                 if (retries-- > 0) {
                     continue;
                 }
                 throw new OsmTransferException(e);
             } catch(IOException e){
                 throw new OsmTransferException(e);
             } catch(OsmTransferCanceledException e){
                 throw e;
             } catch(OsmTransferException e) {
                 throw e;
             }
         }
     }
     
     protected int getMaxRetries() {
         int ret = Main.pref.getInteger("osm-server.max-num-retries", OsmApi.DEFAULT_MAX_NUM_RETRIES);
         return Math.max(ret,0);
     }
     
     private void sleepAndListen(int retry, ProgressMonitor monitor) throws OsmTransferCanceledException {
         System.out.print(tr("Waiting 10 seconds ... "));
         for(int i=0; i < 10; i++) {
             if (monitor != null) {
                 monitor.setCustomText(tr("Starting retry {0} of {1} in {2} seconds ...", getMaxRetries() - retry,getMaxRetries(), 10-i));
             }
             if (cancel)
                 throw new OsmTransferCanceledException();
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException ex) {}
         }
         System.out.println(tr("OK - trying again."));
     }
     
     public String getBaseUrl() {
         StringBuffer rv = new StringBuffer(serverUrl);
         if (version != null) {
             rv.append("/");
             rv.append(version);
         }
         rv.append("/");
         // this works around a ruby (or lighttpd) bug where two consecutive slashes in
         // an URL will cause a "404 not found" response.
         int p; while ((p = rv.indexOf("//", 6)) > -1) { rv.delete(p, p + 1); }
         return rv.toString();
     }
 }
