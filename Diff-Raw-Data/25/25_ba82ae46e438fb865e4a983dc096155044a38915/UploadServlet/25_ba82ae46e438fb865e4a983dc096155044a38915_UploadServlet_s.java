 package xtremweb.core.http;
 
import xtremweb.core.http.exception.RemoteFileNotSpecifiedException;;
 import xtremweb.core.log.*;
 import xtremweb.core.conf.*;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
   
 import org.mortbay.jetty.*;
 import org.mortbay.jetty.bio.SocketConnector;
 import org.mortbay.jetty.servlet.*;
 import org.mortbay.jetty.handler.*;
 import org.mortbay.util.*;
 
 import org.apache.commons.fileupload.*;
 import org.apache.commons.fileupload.servlet.*;
 import org.apache.commons.fileupload.disk.*;
 
 import java.util.List;
 import java.util.Iterator;
 import java.io.File;
 
 /**
  * <code>UploadServlet</code> is a servlet which allows to upload
  * data to the server.
  * It relies on the Jakarta FileUpload package
  *
  */
 public class UploadServlet extends HttpServlet {
     
     private String _documentRoot; 
     
     private String file_name;
 
     Logger log = LoggerFactory.getLogger("UploadServlet");
  
     
     /**
      * Creates a new <code>UploadServlet</code> instance.
      *
      * @param documentRoot a <code>String</code> value
      */
     public UploadServlet() {
 	_documentRoot = ".";
     }
  
    
     /**
      *  <code>doGet</code> http Get method
      *
      * @param request a <code>HttpServletRequest</code> value
      * @param response a <code>HttpServletResponse</code> value
      * @exception ServletException if an error occurs
      * @exception IOException if an error occurs
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
 	log.debug("Content Type ="+request.getContentType());
 	response.setContentType("text/html");
 	response.setStatus(HttpServletResponse.SC_OK);
 	response.getWriter().println("<h1>File Upload Page</h1><FORM name=\"filesForm\" action=\"fileupload\"  method=\"post\" enctype=\"multipart/form-data\">  File:<input type=\"file\" name=\"file\"/><br/> <input type=\"submit\" name=\"Submit\" value=\"Upload Files\"/></FORM>");
     }
     
     /**
      *  <code>doPost</code> http post method
      *
      * @param request a <code>HttpServletRequest</code> value
      * @param response a <code>HttpServletResponse</code> value
      * @exception ServletException if an error occurs
      * @exception IOException if an error occurs
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	try {
 		FileItem toWrite = null;
 	    log.debug("Content Type ="+request.getContentType());
 	    DiskFileItemFactory factory = new DiskFileItemFactory();
 	    // Configure the factory here, if desired.
 	    ServletFileUpload upload = new ServletFileUpload(factory);
 	    // Configure the uploader here, if desired.
 	    List fileItems = upload.parseRequest(request);
 	    
 	    Iterator itr = fileItems.iterator();
 	    
 	    while(itr.hasNext()) {
 		FileItem fi = (FileItem)itr.next();
 		
 		if(!fi.isFormField()) {
 		    log.debug("\nFIELD NAME: "+fi.getFieldName());
 		    log.debug("\nNAME: "+fi.getName());
 		    log.debug("SIZE: "+fi.getSize());
 		    toWrite = fi;
 		}
 		else {
 			log.debug("enter in is not  a form field");
 		    log.debug("Field ="+fi.getFieldName() + " " + fi.getString());
 		    file_name = fi.getString();
 		}
 	    }
 	    
 	    if (file_name.equals("") || file_name == null)
 	    {
 	    	throw new RemoteFileNotSpecifiedException("The remote file name was not specified");
 	    }
 	    
 	    File fNew= new File(_documentRoot, file_name);
 	    //	File fNew= new File(fi.getName());
 	    log.debug(fNew.getAbsolutePath());
 	    toWrite.write(fNew);
 	    response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.getWriter().println("<h1>File Uploaded</h1>");
 	} catch(Exception e){
 		e.printStackTrace();
 		log.fatal("There was an internal server error : " + e.getMessage());
 		response.setContentType("text/html");
 		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		response.getWriter().println("<h1>"+e.getMessage()+"</h1>");
 	}
 	
 	
     }
     
 }
