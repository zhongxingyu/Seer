 package controllers;
 
 import models.*;
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 import play.mvc.Http;
 import play.vfs.VirtualFile;
 import utils.GestureListBinder;
 import utils.PartialContent;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.List;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
 
     /**
      * Allows an asset file to be shared partially.
      * Code derived from https://gist.github.com/1781977.
      * @param relPath - A path relative to the /public directory.
      */
     
     private static void renderPartial(String relPath){
         response.setHeader("Accept-Ranges", "bytes");
         VirtualFile f = VirtualFile.fromRelativePath(relPath);
         if (!f.exists()){
             notFound();
         }
         InputStream underlyingFile = f.inputstream();
         File realFile = f.getRealFile();
         Http.Header rangeHeader = request.headers.get("range");
        //System.out.println("header is "+rangeHeader);
         if (rangeHeader != null) {
            //System.out.println("Content is "+rangeHeader.value());
             throw new PartialContent(realFile, relPath);
         } else {
 
             renderBinary(underlyingFile,
                     relPath, realFile.length(),
                     MimeTypes.getContentType(relPath), false);
         }
     }
 
     public static void cues(String fileName){
         renderPartial("/public/assets/cues/"+fileName);
     }
     
     public static void video(String fileName){
         renderPartial("/public/assets/videos/"+fileName);
     }
 
     public static void iostest(){
         render();
     }
 
     public static void submitData(ScreenResolution screen,String captured){
         GestureListBinder b = new GestureListBinder();
         try {
             List<CapturedGesture> gs = b.deserialise(captured);
             CaptureSession thisSession = new CaptureSession(gs,screen);
             thisSession.save();
         } catch (Exception e) {
             e.printStackTrace();
         }
         render();
     }
 
     public static void overview(){
         renderArgs.put("numNew",CaptureSession.getNotDownloaded().size());
         renderArgs.put("numOld",CaptureSession.getDownloaded().size());
         render();
     }
 
     public static void getNewCSV(){
         List<CaptureSession> sessions = CaptureSession.getNotDownloaded();
         response.contentType = "text/csv";
         for (CaptureSession s:sessions){
             try {
                 response.writeChunk(s.downloadCSV().call());
             } catch (Exception e){
                 e.printStackTrace();
             }
         }       
     }
     
     public static void getOldCSV(){
         List<CaptureSession> sessions = CaptureSession.getDownloaded();
         response.contentType = "text/csv";
         for (CaptureSession s:sessions){
             try {
                 response.writeChunk(s.downloadCSV().call());
             } catch (Exception e){
                 e.printStackTrace();
             }
         }
     }
     
     public static void getAllCSV(){
         List<CaptureSession> sessions = CaptureSession.all().fetch();
         response.contentType = "text/csv";
         for (CaptureSession s:sessions){
             try {
                 response.writeChunk(s.downloadCSV().call());
             } catch (Exception e){
                 e.printStackTrace();
             }
         } 
     }
 
     public static void clearData(){
         Touch.deleteAll();
         GestureInstant.deleteAll();
         CapturedGesture.deleteAll();
         CaptureSession.deleteAll();
         overview();
     }
 }
