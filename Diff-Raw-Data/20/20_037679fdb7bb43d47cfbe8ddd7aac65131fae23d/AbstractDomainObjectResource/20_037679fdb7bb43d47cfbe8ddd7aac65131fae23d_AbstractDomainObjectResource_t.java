 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
 import gov.nih.nci.cabig.ctms.domain.DomainObject;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 
 /**
  * Implements GET for a resource backed by a single domain object.
  *
  * @author Rhett Sutphin
  */
 public abstract class AbstractDomainObjectResource<D extends DomainObject> extends AbstractPscResource {
     private D requestedObject;
     protected StudyCalendarXmlSerializer<D> xmlSerializer;
     private ResourceException objectLoadException;
 
     /**
      * Load the domain object which corresponds to the requested resource.  If
      * there isn't one, this method must return null.
      * @throws ResourceException when some unusual condition is met.  This method should
      *   not throw ResourceException for 404s, but rather for 403s and similar conditions
      *   where you need to short-circuit the usual domain object loading process.
      */
     protected abstract D loadRequestedObject(Request request) throws ResourceException;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         setReadable(true);
         setModifiable(false);
         getVariants().add(new Variant(MediaType.TEXT_XML));
         getVariants().add(new Variant(MediaType.TEXT_CALENDAR));
 
         try {
             requestedObject = loadRequestedObject(request);
         } catch (ResourceException e) {
             // Defer throwing exception until the subclass actually attempts to access the object
             objectLoadException = e;
         }
         setAvailable(requestedObject != null || objectLoadException != null);
        if (requestedObject != null) {
             log.debug("Request {} maps to {}", request.getResourceRef(), requestedObject);
         } else {
             log.debug("Request {} does not map to an existing domain object", request.getResourceRef());
         }
     }
 
     public D getRequestedObject() throws ResourceException {
         if (objectLoadException != null) {
             throw objectLoadException;
         } else {
             return requestedObject;
         }
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         if (variant.getMediaType() == MediaType.TEXT_XML) {
            return createXmlRepresentation(getRequestedObject());
         } else if (variant.getMediaType() == MediaType.TEXT_CALENDAR) {
            return createCalendarRepresentation(getRequestedObject());
         } else {
             return null;
         }
     }
 
     protected Representation createCalendarRepresentation(D object) {
         return null;
 
     }
 
     protected Representation createXmlRepresentation(D object) throws ResourceException {
         return new StringRepresentation(
                 getXmlSerializer().createDocumentString(object), MediaType.TEXT_XML);
     }
 
     ////// CONFIGURATION
 
     public StudyCalendarXmlSerializer<D> getXmlSerializer() throws ResourceException {
         return xmlSerializer;
     }
 
     public void setXmlSerializer(StudyCalendarXmlSerializer<D> studyCalendarXmlFactory) {
         this.xmlSerializer = studyCalendarXmlFactory;
     }
 }
