 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
 import org.restlet.Context;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.util.Collection;
 
 /**
  * @author Saurabh Agrawal
  */
 public class SitesResource extends AbstractCollectionResource<Site> {
     private SiteDao siteDao;
     private StudyCalendarXmlCollectionSerializer<Site> xmlSerializer;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
        addAuthorizationsFor(Method.GET, PscRole.valuesWithSiteScoped());
     }
 
     @Override
     public Collection<Site> getAllObjects() {
         return siteDao.getVisibleSites(getCurrentUser().getVisibleSiteParameters());
     }
 
     @Required
     public void setSiteDao(SiteDao siteDao) {
         this.siteDao = siteDao;
     }
 
     public StudyCalendarXmlCollectionSerializer<Site> getXmlSerializer() {
        return xmlSerializer;
     }
 
     @Required
     public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<Site> xmlSerializer) {
         this.xmlSerializer = xmlSerializer;
     }
 
 
 }
