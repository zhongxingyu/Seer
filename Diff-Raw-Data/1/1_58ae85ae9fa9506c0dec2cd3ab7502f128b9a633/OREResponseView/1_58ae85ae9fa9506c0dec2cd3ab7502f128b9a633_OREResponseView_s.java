 package net.metadata.auselit.lorestore.servlet;
 
 import static net.metadata.auselit.lorestore.common.OREConstants.ORE_USE_STYLESHEET;
 import static net.metadata.auselit.lorestore.common.OREProperties.DEFAULT_RDF_STYLESHEET_PROP;
 import static net.metadata.auselit.lorestore.servlet.OREResponse.*;
 import static net.metadata.auselit.lorestore.servlet.OREResponse.RESPONSE_RDF_KEY;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.Syntax;
 
 import au.edu.diasb.chico.mvc.BaseView;
 import au.edu.diasb.chico.mvc.MimeTypes;
 
 public class OREResponseView extends BaseView {
     public OREResponseView() {
         super(Logger.getLogger(OREResponseView.class));
     }
     
     @Override
     protected void renderMergedOutputModel(Map<String, Object> map, 
             HttpServletRequest request, HttpServletResponse response) 
     throws IOException {
     	Model responseRDF = (Model) map.get(RESPONSE_RDF_KEY);
         Properties props = (Properties) map.get(ORE_PROPS_KEY);
         String stylesheetParam = request.getParameter(ORE_USE_STYLESHEET);
         String stylesheetURI = (stylesheetParam == null) ? null :
                 (stylesheetParam.length() == 0) ? props.getProperty(DEFAULT_RDF_STYLESHEET_PROP, null) :
                     stylesheetParam;
         
         setResponseHeaders(map, response);
         
         OutputStream os = null;
         // FIXME the mapping of 'accept' types to formats that we understand and hence
         // to the content types that we use needs to be soft, and a lot smarter.
         try {
             if (responseRDF != null) {
             	if (isAcceptable(MimeTypes.XML_RDF_MIMETYPES, request)) { 
                     os = outputRDF(response, responseRDF, stylesheetURI);
                 } else {
                     response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                             "Request response is only available as application/xml");
                 }
             } else if (props != null) {
                 if (isAcceptable(MimeTypes.XML_MIMETYPE, request)) {
                     os = outputProps(response, props);
                 } else {
                     response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
                             "Request response is only available as application/xml");
                 }
             } else {
                 logger.error("No content has been set");
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                         "No content has been set");
             }
         } finally {
             if (os != null) {
                 os.close();
             }
             if (responseRDF != null) {
             	responseRDF.close();
             }
         }
     }
     
     private void setResponseHeaders(Map<String, Object> map,
 			HttpServletResponse response) {
 		Object loc = map.get(LOCATION_HEADER);
 		if (loc != null) {
 			response.setHeader("Location", (String)loc);
 		}
 		Object status = map.get(RETURN_STATUS);
 		if (status != null) {
 			response.setStatus((Integer)status);
 		}
 		
 	}
 
 	private OutputStream outputProps(HttpServletResponse response, Properties props) 
     throws IOException {
         response.setContentType("application/xml");
         OutputStream os = response.getOutputStream();
         props.storeToXML(os, "Danno properties", "UTF-8");
         return os;
     }
 
     private OutputStream outputRDF(HttpServletResponse response,  
     		Model responseRDF, String stylesheetURL) 
     throws IOException {
         response.setContentType("application/xml");
         if (stylesheetURL == null) {
             OutputStream os = response.getOutputStream();
             responseRDF.writeTo(os, Syntax.RdfXml);
             return os;
         } else {
             // I considered tweaking the RDF serializers to add the stylesheet processing
             // instruction.  Sesame would support this, but it would be difficult with Jena.
             // Instead, we serialize to a StringBuffer and do some simple surgery to insert
             // the required stuff.
             StringWriter sw = new StringWriter();
             responseRDF.writeTo(sw, Syntax.RdfXml);
             StringBuffer sb = sw.getBuffer();
             // The insertion point will be immediately after the '?>' of the xml declaration
             // (if present) or at the start of the document.
             int insertionPoint = 0;
             int xmlDeclStart = sb.indexOf("<?xml ");
             if (xmlDeclStart != -1) {
                 insertionPoint = sb.indexOf("?>", xmlDeclStart);
                 if (insertionPoint == -1) {
                     logger.error("Malformed XML decl");
                     response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Malformed XML decl");
                     return null;
                 } else {
                     insertionPoint += "?>".length();
                 }
             }
             // The inserted text may include an xml declaration.  (Some RDF/XML serializers
             // don't include one by default.)
             String insertion = ((insertionPoint == 0) ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?>": "") +
                     "\n<?xml-stylesheet type=\"text/xsl\" href=\"" + stylesheetURL + "\"?>\n";
             sb.insert(insertionPoint, insertion);
             OutputStream os = response.getOutputStream();
             os.write(sb.toString().getBytes());
             return os;
         }
     }
 
 }
