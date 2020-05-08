 package com.computas.mediasone.backend;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryException;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.ByteArrayOutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 
 // import org.apache.log4j.Logger;
 
 /**
  * Servlet to create a simple Jena-based endpoint to serialize RDF/XML-ABBREV.
  * User: kkj
  * Date: Nov 13, 2008
  * Time: 9:48:49 AM
  */
 public class VirtJenaEndpoint extends HttpServlet {
 
   static final long serialVersionUID = 1;
 
   //  static Logger logger = Logger.getLogger(VirtJenaEndpoint.class);
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
     OutputStream out = res.getOutputStream();
     String endpoint = getServletConfig().getInitParameter("endpoint");
     if (endpoint == null) {
       throw new ServletException("endpoint parameter is not given in web.xml");
     }
     Query sparql;
     try {
       sparql = QueryFactory.create(
               req.getParameterValues("query")[0]);
     } catch (QueryException e) {
       res.sendError(400, "The SPARQL query was malformed: " + e.getMessage());
       e.printStackTrace();
       return;
     }
     if ((sparql.getQueryType() == Query.QueryTypeSelect) || (sparql.getQueryType() == Query.QueryTypeAsk)) {
       res.setContentType(req.getContentType());
       String format = "";
       if (req.getParameter("format") == null || (! "".equals(req.getParameter("format")))) { // Default serialisation is XML
           format = "&format=application%2Fsparql-results%2Bxml";
       }
       URL u = new URL(endpoint + req.getQueryString() + format);
       HttpURLConnection con = (HttpURLConnection) u.openConnection();
       if (con.getResponseCode() == 500) { // We have a proxy error in this case.
           res.sendError(502, con.getResponseMessage());
       }
 
       
 
       ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
       //byteArrayOut.write
 
      //todo Remove hack because of double encoded UTF-8 in results from Virtuoso
       int read = 0;                  // Magic from "Head First Servlets and JSP"
       byte[] bytes = new byte[1024]; // Incredibly opaque code...
       while ((read = con.getInputStream().read(bytes)) != -1) {
         //out.write(bytes, 0, read);
         byteArrayOut.write(bytes,0,read);
       }
      // We do this to ensure that special charachters æ ø å is correct in the GUI
       out.write(byteArrayOut.toString("UTF-8").getBytes("ISO-8859-1"));
       out.flush();
       out.close();
       con.disconnect();
 
       return;
     }
     if (sparql.getQueryType() == Query.QueryTypeUnknown) {
       res.sendError(400, "Unknown SPARQL query type");
       return;
     }
     // Now, we know we will return RDF
 
     String query = sparql.toString();
 
     if (sparql.getQueryType() == Query.QueryTypeDescribe) {
       query = "define sql:describe-mode \"SPO\"\n" + query;
     }
     String outencoding = "UTF-8";
 
     String url = endpoint + "query=" + URLEncoder.encode(query, outencoding);
     /*   while (req.getParameterNames().hasMoreElements()) {
        Object element = req.getParameterNames().nextElement();
        if (!element.equals("query")) {
            for (String s : req.getParameterValues(element.toString()))
                url += element.toString() + "=" + URLEncoder.encode(s, "UTF-8") + "&";
        }
    } */
     URL u = new URL(url);
     HttpURLConnection con = (HttpURLConnection) u.openConnection();
     if (con.getResponseCode() == 500) { // We have a proxy error in this case.
         res.sendError(502, con.getResponseMessage());
         return;
     }
     Model model = ModelFactory.createDefaultModel();
     try {
         model.read(con.getInputStream(), "");
     } catch (Exception e) {
         res.sendError(502, "Invalid RDF received from direct endpoint, " + e.getMessage());          
         return;
     }
     res.setContentType("application/rdf+xml;charset=utf-8");
     model.write(out, "RDF/XML-ABBREV");
     System.gc();
     con.disconnect();
     out.flush();
     out.close();
 
   }
 }
