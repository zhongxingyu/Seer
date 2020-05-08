 package project.efg.server.servlets;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.FileUploadBase;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.servlet.ServletRequestContext;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.log4j.Logger;
 
 import project.efg.server.utils.FileUploadListener;
 
 import com.missiondata.fileupload.MonitoredDiskFileItemFactory;
 
 public abstract class UploadServlet extends HttpServlet
 {
 	protected String realPath;
 	
 	static Logger log = null;
 	
 	/**
 	 * This method is called when the servlet is first started up.
 	 * 
 	 * @params config the ServletConfig object for this servlet.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		try {
 			log = Logger.getLogger(UploadServlet.class);
 		} catch (Exception ee) {
 		}
 		realPath = getServletContext().getRealPath("/");
 	
 	}
 	/**
 	 * This method is called when the servlet is shutdown. Closes the database
 	 * then disconnects the servlet's thread from its Session. It's complement
 	 * is the init method.
 	 */
 	public void destroy() {
 		super.destroy();
 	}
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
     doPost(request,response);
   }
 
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
 	  
 	 
 	    HttpSession session = request.getSession();
 	
 	    if("status".equals(request.getParameter("c")))
 	    {
 	    	
 	      doStatus(session, response);
 	    }
 	    else
 	    {
 	     doFileUpload(session, request, response);
 	    }
 	
   }
   public  abstract void doCustomProcessing(HttpServletResponse response,File[] importedFiles);
   public abstract File getRepository(FileItem fileItem);
   private void doFileUpload(HttpSession session, 
 		  HttpServletRequest request, 
 		  HttpServletResponse response) throws IOException
   {
 	  List files = new ArrayList();
     try
     {
     	 if( !isMultipartContent(request)){
     		 return;
     	 }
       FileUploadListener listener = new FileUploadListener(request.getContentLength());
       session.setAttribute("FILE_UPLOAD_STATS", listener.getFileUploadStats());
       FileItemFactory factory = new MonitoredDiskFileItemFactory(listener);
       ServletFileUpload upload = new ServletFileUpload(factory);
       List items = upload.parseRequest(request);
       boolean hasError = false;
       for (Iterator i = items.iterator(); i.hasNext();)
       {
         FileItem fileItem = (FileItem) i.next();
         if (!fileItem.isFormField())
         {
         	if(fileItem.getSize() > 0){
        	    	String fileName = fileItem.getName();
 	    	
 		        if (fileName != null && !fileName.trim().equals("")) {
 		        	
 		            fileName = FilenameUtils.getName(fileName);
 			        File file = new File(getRepository(fileItem),fileName);
 			        files.add(file);
 			        fileItem.write(file);
 		        }
         	}
         }
         
       }
       
       if(!hasError)
       {
     	  File[] arrFiles = null;
     	  if(files.size() > 0) {
     		 arrFiles = new File[files.size()]; 
     		 for(int index = 0; index < files.size(); index++) {
     			 arrFiles[index] = (File)files.get(index);
     		 }
      	  }
     	  doCustomProcessing(response,arrFiles);
     	 
     	  sendCompleteResponse(response,null);
       }
       else {
     	  sendCompleteResponse(response, "Could not process uploaded file. Please see log for details.");
       }
     }
     catch (Exception e)
     {
     	e.printStackTrace();
     	log.error(e.getMessage());
       sendCompleteResponse(response, e.getMessage());
     }
  
     
   }
 	 public static final boolean isMultipartContent(
 	          HttpServletRequest request) {
 	       if (!"post".equals(request.getMethod().toLowerCase())) {
 	           return false;
 	      }
 	       return FileUploadBase.isMultipartContent(
 	               new ServletRequestContext(request));
 	   }
   private void doStatus(HttpSession session, HttpServletResponse response) throws IOException
   {
     // Make sure the status response is not cached by the browser
     response.addHeader("Expires", "0");
     response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate");
     response.addHeader("Cache-Control", "post-check=0, pre-check=0");
     response.addHeader("Pragma", "no-cache");
 
     FileUploadListener.FileUploadStats fileUploadStats = 
     	(FileUploadListener.FileUploadStats) session.getAttribute("FILE_UPLOAD_STATS");
     if(fileUploadStats != null)
     {
       long bytesProcessed = fileUploadStats.getBytesRead();
       long sizeTotal = fileUploadStats.getTotalSize();
       long percentComplete = (long)Math.floor(((double)bytesProcessed / (double)sizeTotal) * 100.0);
       long timeInSeconds = fileUploadStats.getElapsedTimeInSeconds();
       double uploadRate = bytesProcessed / (timeInSeconds + 0.00001);
       double estimatedRuntime = sizeTotal / (uploadRate + 0.00001);
 
       response.getWriter().println("<b>Upload Status:</b><br/>");
 
      if(bytesProcessed != sizeTotal)
       {
         response.getWriter().println("<div class=\"prog-border\"><div class=\"prog-bar\" style=\"width: " + percentComplete + "%;\"></div></div>");
         response.getWriter().println("Uploaded: " + bytesProcessed + " out of " + sizeTotal + " bytes (" + percentComplete + "%) " + Math.round(uploadRate / 1024) + " Kbs <br/>");
         response.getWriter().println("Runtime: " + formatTime(timeInSeconds) + " out of " + formatTime(estimatedRuntime) + " " + formatTime(estimatedRuntime - timeInSeconds) + " remaining <br/>");
       }
       else
       {
         response.getWriter().println("Uploaded: " + bytesProcessed + " out of " + sizeTotal + " bytes<br/>");
        //response.getWriter().println("Complete.<br/>");
        response.getWriter().println("<b>Upload complete.</b>");
       }
     }
   }
 
   protected void sendCompleteResponse(HttpServletResponse response, String message) throws IOException
   {
     if(message == null)
     {
       response.getOutputStream().print("<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate(''); }</script></head><body onload='killUpdate()'></body></html>");
     }
     else
     {
       response.getOutputStream().print("<html><head><script type='text/javascript'>function killUpdate() { window.parent.killUpdate('" + message + "'); }</script></head><body onload='killUpdate()'></body></html>");
     }
   }
 
   private String formatTime(double timeInSeconds)
   {
     long seconds = (long)Math.floor(timeInSeconds);
     long minutes = (long)Math.floor(timeInSeconds / 60.0);
     long hours = (long)Math.floor(minutes / 60.0);
 
     if(hours != 0)
     {
       return hours + "hours " + (minutes % 60) + "minutes " + (seconds % 60) + "seconds";
     }
     else if(minutes % 60 != 0)
     {
       return (minutes % 60) + "minutes " + (seconds % 60) + "seconds";
     }
     else
     {
       return (seconds % 60) + " seconds";
     }
   }
 }
