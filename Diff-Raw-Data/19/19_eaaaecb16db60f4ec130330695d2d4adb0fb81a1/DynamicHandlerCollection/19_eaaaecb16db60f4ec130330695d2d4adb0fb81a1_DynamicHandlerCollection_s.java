 package eo.frontend.httpserver;
 
 import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.*;
 
 
 import org.eclipse.jetty.server.*;
 import org.eclipse.jetty.server.handler.*;
 
 import com.thoughtworks.xstream.*;
 import com.thoughtworks.xstream.converters.*;
 
 // ================================================================================
 
 public class DynamicHandlerCollection extends AbstractHandler {
     private Map<String, DynamicHandler> handlers_;
     private String base_;
 
     
     public void setHandlers(final Map<String, DynamicHandler> handlers) {
         handlers_ = handlers;
     }
 
     public void setBase(final String base) {
         base_ = base;
     }
 
     public void handle(String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) 
         throws IOException, ServletException {
 
         try {
             //String path = target;
             
             /* DIRTY HACK!!!*/
             //String hn = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
             String hn = baseRequest.getUri().getPath();
 
             //System.out.println(hn);
 
             if (handlers_.containsKey(hn)) {
                 DynamicHandler.Response resp = handlers_.get(hn).handle(baseRequest);
                 
                 /* Serialize handler response */
                 String xml;
                 if (resp.result != null) {
                     XStream xs = new XStream();
                     for (Map.Entry e : resp.aliases.entrySet()) {
                         xs.alias((String)e.getKey(), (Class)e.getValue());
                     }
                     for (SingleValueConverter c : resp.convs) {
                         xs.registerConverter(c);
                     }
                     xml = xs.toXML(resp.result);
                 } else {
                     xml = "";
                 }
 
 
                 String html;
                 if (baseRequest.getParameterValues("_ox") == null) {
                     /* Apply XSLT */
                     String xslt_file;
 
                     int dot_pos = hn.lastIndexOf(".");
                     int slash_pos = hn.lastIndexOf("/") + 1;
                     if (dot_pos > 0) {
                         xslt_file = hn.substring(slash_pos, dot_pos);
                     } else {
                         xslt_file = hn.substring(slash_pos);
                     }
 
                    System.out.println(base_ + "/" + xslt_file + ".xsl");
 
                     Source xml_source = new StreamSource(new StringReader(xml));
                     Source xsl_source = new StreamSource(new File(base_ + "/" + xslt_file + ".xsl"));
                     Writer writer = new StringWriter();
                     Result transform_result = new StreamResult(writer);
                     Transformer trans = TransformerFactory.newInstance().newTransformer(xsl_source);
                     trans.transform(xml_source, transform_result);
                     writer.flush();
 
                     html = writer.toString();
                     response.setContentType("text/html");
                 } else {
                     html = xml;
                     response.setContentType("application/xml");
                 }
                 
                 response.setStatus(HttpServletResponse.SC_OK);                
                 response.setCharacterEncoding("UTF-8");
                 response.setContentLength(html.getBytes().length);
 
                 PrintWriter pw = response.getWriter();
                 pw.print(html + "\n\n");
                 pw.flush();
             }
         }  catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
