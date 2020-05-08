 package uk.ac.ebi.ae15;
 
 import org.apache.commons.logging.Log;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.dom.DOMSource;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.regex.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class QueryServlet extends HttpServlet {
 
     // logging macinery
     private static final Log log = org.apache.commons.logging.LogFactory.getLog(QueryServlet.class);
 
     // Respond to HTTP GET requests from browsers.
     public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
     {
         log.debug(
             new StringBuilder("Processing request: ")
                 .append(request.getRequestURL())
                 .append("?")
                 .append(request.getQueryString())
         );
 
 
         String type = "xml";
         String stylesheet = "default";
 
         Pattern p = Pattern.compile("servlets/query/([^/]+)/?([^/]*)");
         Matcher m = p.matcher(request.getRequestURL());
         if ( m.find() )
         {
             stylesheet = m.group(1);
             if ( 0 < m.group(2).length() )
                 type = m.group(2);
         }
         
         // Set content type for HTML/XML
         response.setContentType("text/" + type + "; charset=ISO-8859-1");
 
         // Disable cache no matter what (or we fucked on IE side)
         response.addHeader("Pragma", "no-cache");
         response.addHeader("Cache-Control", "no-cache");
         response.addHeader("Cache-Control", "must-revalidate");
         response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past
 
         // Output goes to the response PrintWriter.
         PrintWriter out = response.getWriter();
         try {
             //get the real path for xml and xsl files.
             String ctxRoot = Application.Preferences().getProperty("ae.webapp.root");
             // Get the XML input document and the stylesheet, both in the servlet
             // engine document directory.
 
             //TODO: make paths configurable
             Source xmlSource = new DOMSource(Application.Experiments().getExperiments());
             Source xslSource = new StreamSource( new URL("file", "", ctxRoot + "WEB-INF/server-assets/stylesheets/" + stylesheet + "-" + type + ".xsl").openStream() );
             TransformerFactory tFactory = TransformerFactory.newInstance();
 
             tFactory.setURIResolver(
                 new AppURIResolver( ctxRoot + "WEB-INF/server-assets/stylesheets/" )
             );
 
             // Generate the transformer.
             Transformer transformer = tFactory.newTransformer(xslSource);
 
             // Stuff transformer with all the parameters supplied via the request
             Enumeration e = request.getParameterNames();
             while ( e.hasMoreElements() ) {
                 String name = (String)e.nextElement();
                 String value = request.getParameter(name);
                 if ( null != name && null != value )
                     transformer.setParameter( name, value );
             }
 
             log.debug("experiments filtering: about to start transformer.transform()");
             // Perform the transformation, sending the output to the response.
             transformXml( transformer, xmlSource, new StreamResult(out) );
             log.debug("experiments filtering: transformer.transform() completed");
         }
         // If an Exception occurs, return the error to the client.
         catch ( Exception x ) {
             log.debug( "Caught an exception:", x );
         }
         // Close the PrintWriter.
         out.close();
     }
 
     synchronized private static void transformXml( Transformer transformer, Source in, Result out ) throws TransformerException
     {
         transformer.transform(in, out);
     }
 }
 
 class AppURIResolver implements URIResolver {
 
     // logging macinery
     private static final Log log = org.apache.commons.logging.LogFactory.getLog(AppURIResolver.class);
 
     public AppURIResolver ( String rootPath )
     {
         root = rootPath;
     }
 
     public Source resolve( String href, String base ) throws TransformerException
     {
         Source src = null;
         try {
             src = new StreamSource( new URL("file", "", root + href ).openStream() );
         } catch ( Exception x ) {
             log.debug( "Caught an exception:", x );
             throw new TransformerException(x.getMessage());
         }
 
     return src;
     }
 
     private String root;
 
 }
