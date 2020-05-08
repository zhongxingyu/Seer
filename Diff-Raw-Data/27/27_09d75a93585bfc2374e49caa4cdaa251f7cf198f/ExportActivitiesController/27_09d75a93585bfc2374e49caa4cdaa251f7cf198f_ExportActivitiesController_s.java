 package edu.northwestern.bioinformatics.studycalendar.web.template;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.SourceSerializer;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.springframework.util.FileCopyUtils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Collection;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
 
 // TODO: update the source resource to support CSV (#692) then replace all uses of this controller
 // with the resource.
 public class ExportActivitiesController extends AbstractController implements PscAuthorizedHandler {
     private final Logger log = LoggerFactory.getLogger(getClass());
     private static final Pattern ID_PATTERN = Pattern.compile("/([^/]+)\\.");
     private SourceDao sourceDao;
     private ActivitySourceXmlSerializer activitySourceXmlSerializer;
     private SourceSerializer sourceSerializer;
 
     public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
         return ResourceAuthorization.createCollection(BUSINESS_ADMINISTRATOR);
     }
 
     @Override
     protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
         String identifier = extractIdentifier(request.getPathInfo(), ID_PATTERN);
         String fullPath = request.getPathInfo();
         String extension = fullPath.substring(fullPath.lastIndexOf(".")+1).toLowerCase();
 
         if (identifier == null) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Could not extract study identifier");
             return null;
         }
        Source source = sourceDao.getById(new Integer(identifier));
         if (source == null) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
         if (! (extension.equals("csv") || extension.equals("xls") || extension.equals("xml"))) {
             response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong extension type");
             return null;
         }
         String elt;
 
         if(extension.equals("xml")) {
             response.setContentType("text/xml");
             elt = activitySourceXmlSerializer.createDocumentString(source);
         } else {
             if (extension.equals("csv")) {
                 response.setContentType("text/plain");
                 elt = sourceSerializer.createDocumentString(source, ',');
             } else {
                 response.setContentType("text");
                 elt = sourceSerializer.createDocumentString(source, '\t');
             }
         }
 
         byte[] content = elt.getBytes();
         response.setContentLength(content.length);
         response.setHeader("Content-Disposition","attachment");
         FileCopyUtils.copy(content , response.getOutputStream());
         return null;
     }
 
     String extractIdentifier(String pathInfo, Pattern idPattern) {
         Matcher matcher = idPattern.matcher(pathInfo);
         if (matcher.find()) {
             return matcher.group(1);
         } else {
             log.info("Could not extract identifier from {}", pathInfo);
             return null;
         }
     }
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setActivitySourceXmlSerializer(ActivitySourceXmlSerializer activitySourceXmlSerializer) {
         this.activitySourceXmlSerializer = activitySourceXmlSerializer;
     }
 
     @Required
     public void setSourceSerializer(SourceSerializer sourceSerializer) {
         this.sourceSerializer = sourceSerializer;
     }
 }
 
