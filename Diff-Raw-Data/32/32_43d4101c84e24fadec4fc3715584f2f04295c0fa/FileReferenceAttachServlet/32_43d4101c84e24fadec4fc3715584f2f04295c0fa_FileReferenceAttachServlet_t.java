 package com.github.podd.prototype;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.URI;
 import org.semanticweb.owlapi.model.OWLException;
 
 /**
  * This servlet implements the File Reference attachment service in the PODD prototype. It delegates
  * all File Reference specific tasks to an instance of the FileReferenceUtils class.
  * 
  * 
  * @author kutila
  */
 @SuppressWarnings("serial")
 public class FileReferenceAttachServlet extends PoddBaseServlet
 {
     
     @Override
     protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
         throws ServletException, IOException
     {
         if(!this.isValidSession(request, response))
         {
             return;
         }
         
         final String httpMethod = request.getMethod();
         if(!PoddBaseServlet.HTTP_POST.equals(httpMethod))
         {
             this.log.info("Unsupported service request method: " + httpMethod);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request Method not supported");
             return;
         }
         
         this.log.info("REFERENCE attach");
         final FileReferenceUtils utils =
                 ((PoddServletHelper)this.getServletContext().getAttribute(
                         PoddServletContextListener.PODD_SERVLET_HELPER)).getFileReferenceUtils();
         
         final FileReference fileReference = utils.constructFileReferenceFromMap(request.getParameterMap());
         
         if(fileReference == null)
         {
             final String message = "Not enough information to create file reference";
             this.log.error(message);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
             return;
         }
         
         try
         {
             utils.checkFileExists(fileReference);
         }
         catch(IOException | PoddException e)
         {
             final String message = "Referenced file is not valid. " + e.getMessage();
             this.log.error(message, e);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, message + e.getMessage());
             return;
         }
         
         final PoddServletHelper helper =
                 (PoddServletHelper)this.getServletContext()
                         .getAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
         try
         {
             final URI fileReferenceUri = helper.attachReference(fileReference, false);
             response.setStatus(HttpServletResponse.SC_OK);
             response.setContentType(PoddServlet.MIME_TYPE_JSON);
             final PrintWriter out = response.getWriter();
             out.write(fileReferenceUri.stringValue()); // should be encapsulated in JSON format
             out.flush();
             out.close();
         }
        catch(final PoddException | OpenRDFException | OWLException e)
         {
             final String message = "REFERENCE attach failed. " + e.getMessage();
             this.log.error(message);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, message); // client error
             return;
         }
         
     }
     
 }
