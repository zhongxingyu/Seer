 package com.hacku.swearjar.server;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
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
  * Servlet implementation class ConvertServlet
  */
 @WebServlet(description = "Converts incoming file to .flac format before sending to Google's ASR.  Sends json response back to app.",
 urlPatterns = {"/convert"})
 @MultipartConfig(maxFileSize = 1024 * 1024 * 32)  //Accept files upto 32MB
 public class ConvertServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
 
     /**
      * Takes an audio file, transcodes it to flac, then performs speech
      * recognition. Gives a JSON response containing the recognised speech.
      *
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
      * response)
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         String baseDir = "/tmp";
         String baseFilename = "SwearJar_"
                 + request.getSession().getCreationTime() //Timestamp
                 + "_" + request.getSession().getId();  //Session ID
 
         String inputExt = ".3gp";
         String outputExt = ".flac";
        String inputFilename = baseDir + baseFilename + inputExt;
         String flacFilename = baseFilename + outputExt;
 
         //Read the wav file sent and store it in a .wav file
         Part part = request.getPart("Content");
         InputStream inputStream = part.getInputStream();
         FileOutputStream fos = new FileOutputStream(inputFilename);
         IOUtils.copy(inputStream, fos);
         fos.flush();
         fos.close();
 
         //encode the file as flac
         String[] outputFilenames = transcode(baseDir, baseFilename, inputExt, outputExt);
 
         for(String filename : outputFilenames)
         System.out.println(filename);
 
         //Do speech recogntion and return JSON
         for(String filename : outputFilenames){
             //TODO create new threads here
             InputStream speechRecognitionJson = getSpeechResponse(filename);
             if (speechRecognitionJson != null) {
                 String json = IOUtils.toString(speechRecognitionJson);
                 IOUtils.copy(IOUtils.toInputStream(json), response.getOutputStream());
                 System.out.println(json);
             }
         }
 
         //Temporary files can be deleted now
         //delete(inputFilename);
         //delete(flacFilename);
     }
 
     /**
      * Causes the calling thread to wait for a maximum of millis for the File at
      * filename to be created
      *
      * @param millis
      * @param filename
      */
     private static File waitForFileCreation(String filename, int millis) {
         while (millis > 0) {
             try {
                 File file = new File(filename);
                 if (file.exists() && file.canRead()) {
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException ex) {
                         Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     return file;
                 }
 
                 Thread.sleep(1);
                 millis--;
             } catch (InterruptedException ex) {
                 Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return null;
     }
 
     /**
      * Deletes a file if it exists
      *
      * @param filename
      */
     private static void delete(String filename) {
         try {
             new File(filename).delete();
         } catch (NullPointerException ioe) {
             System.err.println("Error deleting: " + filename);
         }
     }
 
     /**
      * Transcodes input file to flac
      *
      * @param inputFile
      * @param outputFile
      * @return array of files created
      */
     private static String[] transcode(String baseDir, String baseFilename, String inputExt, String outputExt) {
         Runtime rt = Runtime.getRuntime();
         String output = "";
         
         try {
 
             String str = "sox_splitter " + baseDir + " " + baseFilename + " " + inputExt + " " + outputExt;
             //"echo test &>> /tmp/output";
                     /*"ffmpeg -i " + //Location of vlc
                     inputFile + " -ar 8000 -sample_fmt s16 "//Location of input 
                     + " " + outputFile;*/
             /*"run \"C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe\" -I --dummy-quiet " + //Location of vlc
              inputFile + //Location of input 
              " --sout=\"#transcode{acodec=flac, channels=1 ab=16 samplerate=16000}"
              + ":std{access=file, mux=raw, dst="
              + outputFile + //Location of output
              "}\" vlc://quit";*/
 
             Process pr = rt.exec(str);
         
             int exitStatus = pr.waitFor();
             
             FileOutputStream fos = new FileOutputStream("/tmp/output");
             IOUtils.copy(pr.getInputStream(), fos);
             fos.flush();
             fos.close();
             
             FileOutputStream eos = new FileOutputStream("/tmp/errors");
             IOUtils.copy(pr.getErrorStream(), eos);
             eos.flush();
             eos.close();
             
             //output = IOUtils.toString(pr.getInputStream());
             
             System.out.println(System.currentTimeMillis() + " VLC exit code: " + exitStatus);
 
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         } finally {            
             return output.split("\n");
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
     private static InputStream getSpeechResponse(String speechFile) {
         FileLock lock = null;
 
         try {
             //File file = waitForFileCreation(speechFile, 1000);
             // Read speech file 
             File file = new File(speechFile);
             FileInputStream inputStream = new FileInputStream(file);
             
             //Wait for file to become available
             FileChannel channel = inputStream.getChannel();
             lock = channel.lock(0, Long.MAX_VALUE, true);//channel.lock(); 
             
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
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             try {
              lock.release();
              } catch (IOException ex) {
              Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
              } catch (NullPointerException npe) {
              Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, npe);
              }
         }
 
 
         return null;
     }
 
     /**
      * Sets up the post request =
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
