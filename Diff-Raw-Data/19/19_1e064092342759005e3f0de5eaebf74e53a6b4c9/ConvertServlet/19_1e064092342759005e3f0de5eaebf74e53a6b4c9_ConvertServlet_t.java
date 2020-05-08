 package com.hacku.swearjar.server;
 
 import com.hacku.swearjar.speechapi.GoogleSpeechAPI;
 import com.hacku.swearjar.speechapi.SpeechResponse;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
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
 
 /**
  * Servlet implementation class ConvertServlet
  */
 @WebServlet(description = "Converts incoming file to .flac format before sending to Google's ASR.  Sends json response back to app.",
 urlPatterns = {"/convert"})
 @MultipartConfig(maxFileSize = 1024 * 1024 * 32)  //TODO consider max file size Accept files upto 32MB 
 public class ConvertServlet extends HttpServlet {
 
     private static final long serialVersionUID = 1L;
    private static final ExecutorService speechServicePool = Executors.newCachedThreadPool();

     private static void initLogFile() {
         try {
             Handler fileHandler = new FileHandler("/tmp/log");
             Logger.getLogger("").addHandler(fileHandler);
 
         } catch (IOException ex) {
             Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Takes an audio file and performs speech recognition. Gives a JSON
      * response containing the recognised speech.
      *
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
      * response)
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         initLogFile();
 
         String baseDir = "/tmp";
         String baseFilename = "SwearJar_"
                 + request.getSession().getCreationTime() //Timestamp
                 + "_" + request.getSession().getId();  //Session ID
 
         String inputExt = ".3gp";
         String outputExt = ".flac";
         String inputFilename = baseDir + "/" + baseFilename + inputExt;
 
         //Read the wav file sent and store it in a .wav file
         Part part = request.getPart("Content");
         InputStream inputStream = part.getInputStream();
         FileOutputStream fos = new FileOutputStream(inputFilename);
         IOUtils.copy(inputStream, fos);
         fos.flush();
         fos.close();
 
         //encode the files as flac
         String[] outputFilenames = transcode(baseDir, baseFilename, inputExt, outputExt);
 
         //Do speech recogntion and print JSON
        SpeechResponse aggregateSpeech = getSpeechResponse(outputFilenames);
        response.getOutputStream().print(aggregateSpeech.toJson());
        Logger.getLogger(ConvertServlet.class.getName()).log(Level.INFO, "response: {0}", aggregateSpeech.toJson());
 
         //Temporary files can be deleted now
         delete(inputFilename);
         for (String filename : outputFilenames) {
             delete(filename);
         }
     }
 
     /**
      * Gets an aggregate SpeechResponse object based on the speech contained in
      * multiple flac files
      *
      * @param speechFiles
      * @return aggregate SpeechResponse
      */
     private static SpeechResponse getSpeechResponse(String[] speechFiles) {
 
         List<Future<SpeechResponse>> futureSpeechResponses = new LinkedList();
 
         for (String filename : speechFiles) {
             //Fire off files to Google on different threads
             GoogleSpeechAPI speechService = new GoogleSpeechAPI(filename);
             futureSpeechResponses.add(speechServicePool.submit(speechService));
         }
 
         SpeechResponse aggregateSpeech = new SpeechResponse();
 
         //Wait for all the responses to become available and collate them
         for (Future<SpeechResponse> futureSpeechResponse : futureSpeechResponses) {
 
             try {
                 //Wait, if necessary, until the response is available
                 SpeechResponse response = futureSpeechResponse.get(20, TimeUnit.SECONDS);
 
                 if (response != null) {
                     aggregateSpeech.concat(response);
                 }
             } catch (InterruptedException ex) {
                 Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ExecutionException ex) {
                 Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
             } catch (TimeoutException ex) {
                 Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
 
         return aggregateSpeech;
     }
 
     /**
      * Causes the calling thread to wait for a maximum of millis for the File at
      * filename to be created
      *
      * @param millis
      * @param filename
      * @deprecated
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
             Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, "error deleting {0}", filename);
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
         String output = "";
 
         try {
             String str = "sox_splitter " + baseDir + " " + baseFilename + " " + inputExt + " " + outputExt;
 
             Process pr = Runtime.getRuntime().exec(str);
 
             int exitStatus = pr.waitFor();
 
             output = IOUtils.toString(pr.getInputStream());
 
             FileOutputStream eos = new FileOutputStream("/tmp/errors");
             IOUtils.copy(pr.getErrorStream(), eos);
             eos.flush();
             eos.close();
 
             System.out.println(System.currentTimeMillis() + " VLC exit code: " + exitStatus);
 
         } catch (IOException e) {
             Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, e);
         } catch (InterruptedException e) {
             Logger.getLogger(ConvertServlet.class.getName()).log(Level.SEVERE, null, e);
         }
 
         return output.split("\n");
     }
 }
