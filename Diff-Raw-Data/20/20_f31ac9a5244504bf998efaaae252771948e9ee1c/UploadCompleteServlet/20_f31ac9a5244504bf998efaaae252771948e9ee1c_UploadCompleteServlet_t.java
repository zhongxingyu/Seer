 package com.cs739.app.servlet.master;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cs739.app.model.PlopboxFile;
 import com.cs739.app.model.Replicant;
 import com.cs739.app.service.master.PlopboxFileService;
 import com.cs739.app.service.master.ReplicantService;
 import com.cs739.app.servlet.AbstractPlopboxServlet;
 import com.cs739.app.util.AppConstants;
 import com.cs739.app.util.AppConstants.FileState;
 
 public class UploadCompleteServlet extends AbstractPlopboxServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = -5193187409711672710L;
     private static final Logger log = LoggerFactory
             .getLogger(UploadCompleteServlet.class);
     
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) {
         
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) {
         // Post request with USER_ID & FILE_ID params
         // Saying that the replicant has the file
         log.debug("Got an upload complete");
         
         ServletContext context = request.getSession().getServletContext();
         // Need to get the file that is referenced and change its status to uploaded
         String fileId = request.getParameter(AppConstants.REQUEST_FILE_ID);
         //String userId = request.getParameter(AppConstants.REQUEST_USER_ID);
         String fileName = request.getParameter(AppConstants.REQUEST_FILE_NAME);
         
         List<PlopboxFile> masterFiles = (List<PlopboxFile>) context.getAttribute(AppConstants.MASTER_FILES_LIST);
         // find it
         Long longFileId = Long.parseLong(fileId);
         PlopboxFile uploadedFile = null;
         for (PlopboxFile file : masterFiles) {
             if (file.getId().equals(longFileId)) {
                 // found it
                 uploadedFile = file;
                 uploadedFile.setState(FileState.UPLOADED);
                 uploadedFile.setName(fileName);
                 // save it
                 log.debug("Marked file #" + fileId + " as uploaded.");
                 PlopboxFileService.updatePlopboxFile(file);
                 break;
             }
         }
        // Also store this as on that replicant
        
        
         // Now issue requests to other replicants to grab that file
         Map<Long, Replicant> fileInProgMap = (Map<Long, Replicant>) context.getAttribute(AppConstants.IN_PROGRESS_FILE_REPL_MAP);
         Replicant sourceReplicant = fileInProgMap.get(longFileId);
        sourceReplicant.addFile(fileId);
         PlopboxFileService.removeInProgressFileFromMap(longFileId, fileInProgMap);
         List<Replicant> replicants = (List<Replicant>) context.getAttribute(AppConstants.REPLICANTS);
         if (replicants.size() == 0) {
             log.debug("List of replicants was zero...");
         }
         
         List<Replicant> copycats = ReplicantService.getReplicantsForReplicateCall(sourceReplicant, replicants);
         // Now issue them requests
         for (Replicant r : copycats) {
             // Add a new mapping
             PlopboxFileService.addInProgressFileToMap(longFileId, r, fileInProgMap);
             // Issue request
             String fileUrl = "http://" + sourceReplicant.getHost() + ":" + sourceReplicant.getPort() + "/pb/get?fileId=" + fileId;
             HttpClient httpClient = new DefaultHttpClient();
            HttpGet replicantRequest = new HttpGet("http://" + r.getHost() + ":" + r.getPort() + "/pb/replicate?URL=" + fileUrl + "&fileId=" + fileId + "&fileName=replicated_file");
             try {
                 HttpResponse replicantResponse = httpClient.execute(replicantRequest);
                 if (replicantResponse.getStatusLine().getStatusCode() == 200) {
                     log.debug("Replicate request was good");
                    r.addFile(fileId);
                     PlopboxFileService.removeInProgressFileFromMap(longFileId, fileInProgMap);
                 } else {
                     log.debug("Replicate request failed somehow");
                 }
             } catch (ClientProtocolException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         // Assuming there's no lingering requests for this file, update its status to replicated
         if (fileInProgMap.get(longFileId) == null) {
             uploadedFile.setState(FileState.REPLICATED);
             PlopboxFileService.updatePlopboxFile(uploadedFile);
         }
         
     }
 }
