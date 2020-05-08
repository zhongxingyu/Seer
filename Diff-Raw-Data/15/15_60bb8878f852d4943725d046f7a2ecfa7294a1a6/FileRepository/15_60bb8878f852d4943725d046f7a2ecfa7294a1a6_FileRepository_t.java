 package gwtupload.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 
import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public interface FileRepository {
 
  void saveData(Integer fileToken, String contentType, InputStream data, HttpServletRequest request);
 
   void saveProgress(Integer fileToken, long currentBytes, long totalBytes);
  
   void saveError(Integer fileToken, String message);
   
   FileProgress loadProgress(Integer fileToken);
 
   void sendData(Integer fileToken, HttpServletResponse response) throws IOException;
 
 }
