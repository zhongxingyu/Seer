 package myhomeaudio.server.http.helper.node;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import myhomeaudio.node.Configuration;
 import myhomeaudio.server.http.HTTPMimeType;
 import myhomeaudio.server.http.StatusCode;
 import myhomeaudio.server.http.helper.ByteHelper;
 import myhomeaudio.server.http.helper.Helper;
 import myhomeaudio.server.http.helper.HelperInterface;
 import myhomeaudio.server.node.NodeCommands;
 
 import org.apache.http.HttpStatus;
 import org.json.simple.JSONObject;
 
 public class PlayHelper extends ByteHelper implements HelperInterface,
 		NodeCommands, StatusCode {
 
 	public String getOutput(ArrayList<String> uriSegments, byte[] data) {
 
 		// Set the content-type
 		this.contentType = HTTPMimeType.MIME_JSON;
 		this.httpStatus = HttpStatus.SC_BAD_REQUEST;
 
 		// Create the JSON object to represent the response
 		JSONObject body = new JSONObject();
 		body.put("status", STATUS_FAILED);
 
 		FileOutputStream outFile;
 		try {
 			outFile = new FileOutputStream("song.mp3");
 			outFile.write(data);
 			outFile.close();
 			
 			Process child = Runtime.getRuntime().exec("killall mplayer");
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}

			System.out.println("Playing song...");
			Process child2 = Runtime.getRuntime().exec("mplayer -really-quiet song.mp3 > ./null");
 			body.put("status", STATUS_OK);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		this.httpStatus = HttpStatus.SC_OK;
 
 		return body.toString();
 	}
 }
