 package com.hacku.swearjar.server;
 
 import java.io.ByteArrayInputStream;
 //import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
 //import javaFlacEncoder.FLAC_FileEncoder;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /**
  * Servlet implementation class SwearJarServlet
  */
@WebServlet(description = "Converts incoming file to .flac format before sending to Google's ASR.  Sends json response back to app.",
 urlPatterns = {"/convert"})
 @MultipartConfig(maxFileSize = 1024 * 1024 * 1024)
 public class SwearJarServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Takes an uncompressed wav file, encodes it as flac, then performs 
      * speech recognition.  Gives a JSON response containing the recognised 
      * speech.
      * 
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
      * response)
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
         //TODO Make these filenames timestamped
         //TODO They're only temp files so figure out how to delete them
        String wavFilename = "C:\\Users\\Neil\\Desktop\\test2.3gp";
         String flacFilename = "C:\\Users\\Neil\\Desktop\\test2.flac";
         
         
         //Read the wav file sent and store it in a .wav file
         Part part = request.getPart("Content");
         InputStream inputStream = part.getInputStream();
         FileOutputStream fos = new FileOutputStream(wavFilename);
         IOUtils.copy(inputStream, fos);
         fos.flush();
         fos.close();
 
         //encode the wav file as flac
         //FLAC_FileEncoder encoder = new FLAC_FileEncoder();
         //encoder.encode(new File(wavFilename), new File(flacFilename));
         transcode(wavFilename, flacFilename);
         
         //Do speech recogntion and return JSON
         InputStream speechRecognitionJson = getSpeechResponse(flacFilename);
         IOUtils.copy(speechRecognitionJson, response.getOutputStream());
     }
 
     /**
     * Transcodes input file to flac
      * 
      * @param inputFile
      * @param outputFile
      */
     private void transcode(String inputFile, String outputFile) {
     	Runtime rt = Runtime.getRuntime();
     	try {
     		
     		String str = 
     				"run \"C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe\" -I --dummy-quiet " +  //Location of vlc
     				inputFile +							//Location of input 
     				" --sout=\"#transcode{acodec=flac, channels=1 ab=16 samplerate=8000}" +
     				":std{access=file, mux=raw, dst=" +
     				outputFile +						//Location of output
     				"}\" vlc://quit";
     		
 			Process pr = rt.exec(str);
                        pr.waitFor();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
		} catch (InterruptedException e) {
                    e.printStackTrace();
                }
 	}
 
 	/**
      * Takes the audio at the specified path and sends it off to Google via HTTP
      * POST. Packages the JSON response from Google into a SpeechResponse
      * object.
      *
      * @param speechFile path to the audio file
      * @return SpeechResponse containing recognised speech, null if error occurs
      */
     public static InputStream getSpeechResponse(String speechFile) {
         try {
             // Read speech file
             InputStream inputStream = new FileInputStream(speechFile);
             ByteArrayInputStream data = new ByteArrayInputStream(
                     IOUtils.toByteArray(inputStream));
 
             // Set up the POST request
             HttpPost postRequest = getPost(data);
 
             // Do the request to google
             HttpClient client = new DefaultHttpClient();
             HttpResponse response = client.execute(postRequest);
 
             //return the JSON stream
             return response.getEntity().getContent();
 
         } catch (FileNotFoundException ex) {
             ex.printStackTrace();
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
         
         return null;
     }
 
     /**
      * Sets up the post request
      *
      * @param data audio file
      * @return HttpPost object with parameters initialised to audio file
      */
     private static HttpPost getPost(ByteArrayInputStream data) {
         HttpPost postRequest = new HttpPost(
                 "https://www.google.com/speech-api/v1/recognize"
                 + "?xjerr=1&pfilter=0&client=chromium&lang=en-US&maxresults=1");
 
         // Specify Content and Content-Type parameters for POST request
         MultipartEntity entity = new MultipartEntity();
         entity.addPart("Content", new InputStreamBody(data, "Content"));
        postRequest.setHeader("Content-Type", "audio/x-flac; rate=8000");
         postRequest.setEntity(entity);
         return postRequest;
     }
 }
