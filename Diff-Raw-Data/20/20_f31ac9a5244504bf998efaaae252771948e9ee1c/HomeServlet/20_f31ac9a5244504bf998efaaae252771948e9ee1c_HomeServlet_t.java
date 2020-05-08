 package com.cs739.app.servlet.master;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cs739.app.model.PlopboxFile;
 import com.cs739.app.model.Replicant;
 import com.cs739.app.service.CookieService;
 import com.cs739.app.service.master.PlopboxFileService;
 import com.cs739.app.service.master.ReplicantService;
 import com.cs739.app.servlet.AbstractPlopboxServlet;
 import com.cs739.app.util.AppConstants;
 import com.cs739.app.util.AppConstants.FileState;
 import com.cs739.app.util.AppConstants.MasterPages;
 
 public class HomeServlet extends AbstractPlopboxServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = -6255601670951679699L;
     private static final Logger log = LoggerFactory
             .getLogger(HomeServlet.class);
 
     @SuppressWarnings("unchecked")
     protected void doGet(HttpServletRequest request, HttpServletResponse response) {
         log.debug("Bringin up the home page");
         String username = CookieService.getCookieValueFromRequest(request, AppConstants.USERNAME);
         String userId = CookieService.getCookieValueFromRequest(request, AppConstants.USER_ID);
         try {
             if (username == null && userId == null) {
                 log.debug("username nor userId were not set in cookie, redirecting");
                 // User is not logged in, redirect to login page
                 response.sendRedirect("login");
             } else {
                 // Need to pick a replicant to set the form values, and a new file ID
                 // First create a new file in anticipation of new upload
                 log.debug("Creating a new file in anticipatio of upload");
                 PlopboxFile newFile = new PlopboxFile(new Long(userId));
                 // Save it to generate its ID
                 PlopboxFileService.saveNewPlopboxFile(newFile);
                 // Add this file to the Master's in-memory list of files
                 log.debug("new file ID: " + newFile.getId());
                 ServletContext context = request.getSession().getServletContext(); 
                 List<PlopboxFile> masterFiles = (List<PlopboxFile>) context.getAttribute(AppConstants.MASTER_FILES_LIST);
                 log.debug("MASTER SIZE ON START");
                 log.debug(masterFiles.size()+"");
                 masterFiles.add(newFile);
                 context.setAttribute(AppConstants.MASTER_FILES_LIST, masterFiles);
                 // Now pick a replicant
                 List<Replicant> replicants = (List<Replicant>) context.getAttribute(AppConstants.REPLICANTS);
                 Replicant r = ReplicantService.chooseReplicantForUpload(replicants);
                 // Send it a prepare request
                 HttpClient httpClient = new DefaultHttpClient();
                 String replicantUrl = "http://" + r.getHost() + ":" + r.getPort() + "/pb/prepare?userId=" + userId + "&fileId=" + newFile.getId();
                 // TODO: this should be post
                 HttpGet prepareRequest = new HttpGet(replicantUrl);
                 HttpResponse replicantResponse = httpClient.execute(prepareRequest);
                 log.debug("Here's what the replicant had to say about prepare: ");
                 log.debug(replicantResponse.getStatusLine().getStatusCode() + "");
                 // Mark file in-progress in map
                 PlopboxFileService.addInProgressFileToMap(newFile.getId(), r, ((Map<Long, Replicant>)context.getAttribute(AppConstants.IN_PROGRESS_FILE_REPL_MAP)));
                 log.debug(masterFiles.size() + "");
                 // Also need to get a list of this user's replicated files
                 List<PlopboxFile> userFiles = new ArrayList<PlopboxFile>();
                 for (PlopboxFile file : masterFiles) {
                     log.debug(file.getOwnerId() + "");
                     log.debug(userId);
                     log.debug(file.getState() + "");
                     if (file.getOwnerId().equals(new Long(userId)) && file.getState().equals(FileState.REPLICATED)) {
                         log.debug("HEY found a file");
                         // Set the replicant that will serve the user
                         Replicant server = ReplicantService.chooseReplicantForDownload(file.getId().toString(), replicants);
                        log.debug("Server chosen: " + server.getId());
                         file.setLocation("http://" + server.getHost() + ":" + server.getPort() + "/pb/get?fileId=" + file.getId());
                         userFiles.add(file);
                     }
                 }
                 log.debug(userFiles.size() + "");
                 // Set user info & file info
                 request.setAttribute(AppConstants.REPLICANT_PORT, r.getPort());
                 request.setAttribute(AppConstants.NEW_FILE_ID, newFile.getId());
                 request.setAttribute(AppConstants.USERNAME, username);
                 request.setAttribute(AppConstants.USER_ID, userId);
                 request.setAttribute(AppConstants.USER_FILES, userFiles);
                 forward(request, response, MasterPages.HOME.toString());
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
