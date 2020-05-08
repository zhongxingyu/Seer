 package com.github.podd.prototype;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * PODD prototype servlet. Currently supports operations on artifacts only.
  * 
  * @author kutila
  * @created 2012/10/25
  * 
  */
 @SuppressWarnings("serial")
 public class PoddServlet extends PoddBaseServlet
 {
     public static final String MIME_TYPE_RDF_XML = "application/rdf+xml";
     public static final String MIME_TYPE_TURTLE = "text/turtle";
     public static final String MIME_TYPE_NTRIPLES = "text/plain";
     public static final String MIME_TYPE_JSON = "application/json";
     
     /**
      * A common method to handle GET/POST/DELETE requests
      * 
      * TODO: - check content-type and accept headers - send OK only after ensuring consistency of
      * operation - Use Apache Any23 to try and identify the incoming RDF serialization format from
      * the stream
      * 
      * @param request
      * @param response
      * @throws ServletException
      * @throws IOException
      */
     @Override
     protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
         throws ServletException, IOException
     {
         if(!this.isValidSession(request, response))
         {
             return;
         }
         
         final PoddServletHelper helper =
                 (PoddServletHelper)this.getServletContext()
                         .getAttribute(PoddServletContextListener.PODD_SERVLET_HELPER);
         
         final PrintWriter out = response.getWriter();
         
         final String pathInfo = request.getPathInfo();
         final String httpMethod = request.getMethod();
         
         // ---- handle the PODD operations ----
         
         if(PoddBaseServlet.HTTP_POST.equals(httpMethod) && pathInfo.startsWith("/artifact/new"))
         {
             this.log.info("ADD new artifact");
             final InputStream in = request.getInputStream();
             final String contentType = PoddServlet.MIME_TYPE_RDF_XML;// request.getContentType();
             try
             {
                 final InferredOWLOntologyID loadedArtifactUri = helper.loadPoddArtifact(in, contentType);
                 response.setContentType(PoddServlet.MIME_TYPE_JSON);
                 // FIXME: should be encapsulated in JSON format
                 out.write(loadedArtifactUri.getOntologyIRI().toString()); 
             }
             catch(final Exception e)
             {
                 this.log.error("Failed to add artifact due to: ", e);
                 // TODO: status code will change depending on cause of failure
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Failed to add artifact due to: " + e.getMessage());
             }
         }
         
         else if(PoddBaseServlet.HTTP_POST.equals(httpMethod)
                 && (pathInfo.startsWith("/artifact/edit/replace/") || pathInfo.startsWith("/artifact/edit/merge/")))
         {
             this.log.info("EDIT artifact");
             try
             {
                 String artifactURI = null;
                 boolean isReplace = false;
                 if(pathInfo.startsWith("/artifact/edit/merge/"))
                 {
                     isReplace = false;
                     artifactURI = PoddServletHelper.extractUri(pathInfo.substring(21));
                 }
                 else
                 {
                     isReplace = true;
                     artifactURI = PoddServletHelper.extractUri(pathInfo.substring(23));
                 }
                 
                 final boolean checkFileReference = true; // this could be a user or system parameter
                 
                 final InputStream in = request.getInputStream();
                 final String contentType = PoddServlet.MIME_TYPE_RDF_XML; // request.getContentType();
                 final String editedURI =
                         helper.editArtifact(artifactURI, in, contentType, isReplace, checkFileReference);
                 response.setContentType(PoddServlet.MIME_TYPE_JSON);
                 out.write(editedURI); // should be in JSON
             }
             catch(final Exception e)
             {
                 this.log.error("EDIT Artifact failed with: " + e.toString());
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Failed to EDIT artifact due to: " + e.getMessage());
             }
         }
         
         else if(PoddBaseServlet.HTTP_GET.equals(httpMethod)
                 && (pathInfo.startsWith("/artifact/base/") || pathInfo.startsWith("/artifact/inferred/")))
         {
             this.log.info("GET artifact");
             try
             {
                 boolean includeInferred = false;
                 String artifactURI = null;
                 if(pathInfo.startsWith("/artifact/base/"))
                 {
                     includeInferred = false;
                     artifactURI = PoddServletHelper.extractUri(pathInfo.substring(15));
                 }
                 else
                 {
                     includeInferred = true;
                     artifactURI = PoddServletHelper.extractUri(pathInfo.substring(19));
                 }
                 
                 response.setContentType(PoddServlet.MIME_TYPE_RDF_XML);
                 final String rdfContent =
                         helper.getArtifact(artifactURI, PoddServlet.MIME_TYPE_RDF_XML, includeInferred);
                 out.write(rdfContent);
             }
             catch(final Exception e)
             {
                 this.log.error("GET Artifact failed with: " + e.toString());
                 
                 int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                 response.sendError(statusCode, "Failed to GET artifact due to: " + e.getMessage());
             }
         }
         
         else if(PoddBaseServlet.HTTP_DELETE.equals(httpMethod) && pathInfo.startsWith("/artifact/"))
         {
             this.log.info("DELETE artifact");
             try
             {
                 final String artifactURI = PoddServletHelper.extractUri(pathInfo.substring(10));
                 helper.deleteArtifact(artifactURI);
                 out.write("Successfully deleted " + artifactURI);
             }
             catch(final Exception e)
             {
                 this.log.error("DELETE Artifact failed with: " + e.toString());
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to DELETE artifact due to: "
                         + e.getMessage());
             }
         }
         
         else if(PoddBaseServlet.HTTP_POST.equals(httpMethod) && pathInfo.startsWith("/reset"))
         {
             this.log.info("RESET PODD");
             try
             {
                 helper.resetPodd();
                 out.write("Successfully reset PODD");
             }
             catch(final Exception e)
             {
                 this.log.error("RESET PODD failed with: " + e.toString());
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                         "Failed to RESET PODD due to: " + e.getMessage());
             }
         }
         else
         {
             this.log.info("Unsupported service request: " + httpMethod + ":" + pathInfo);
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request not supported");
         }
         out.flush();
         out.close();
     }
     
 }
