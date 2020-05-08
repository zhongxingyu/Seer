 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Site;
 import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.SubjectJsonRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import org.json.JSONException;
 import org.restlet.Request;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.BeanWrapperImpl;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * @author Rhett Sutphin
  */
 public class SubjectResource extends AbstractDomainObjectResource<Subject> {
     // TODO: do this a better way.
     private static final String[] UPDATEABLE_PROPERTIES =
         { "firstName", "lastName", "personId", "dateOfBirth", "properties", "gender" };
 
     private SubjectDao subjectDao;
 
     @Override
     public void doInit() {
         super.doInit();
         getAllowedMethods().add(Method.PUT);
         getVariants().clear();
         getVariants().add(new Variant(MediaType.APPLICATION_JSON));
 
         if (getRequestedObjectDuringInit() != null) {
             Collection<Site> subjectSites = subjectDao.getSiteParticipation(getRequestedObjectDuringInit());
             log.debug("Requested subject is associated with these sites: {}", subjectSites);
             for (Site site : subjectSites) {
                 addAuthorizationsFor(Method.GET, site, PscRole.SUBJECT_MANAGER,
                     PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.DATA_READER);
                 addAuthorizationsFor(Method.PUT, site, PscRole.SUBJECT_MANAGER);
             }
         } else {
             addAuthorizationsFor(Method.PUT, PscRole.SUBJECT_MANAGER);
            addAuthorizationsFor(Method.GET, PscRole.SUBJECT_MANAGER,
                    PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.DATA_READER);
         }
     }
 
     @Override
     protected Subject loadRequestedObject(Request request) throws ResourceException {
         String ident = UriTemplateParameters.SUBJECT_IDENTIFIER.extractFrom(request);
         log.debug("Looking for subject with person or grid ID '{}'", ident);
         Subject found = subjectDao.getByGridIdOrPersonId(ident);
         log.debug(" - found: {}", found);
         return found;
     }
 
     @Override
     public Representation get(Variant variant) throws ResourceException {
         if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
             return new SubjectJsonRepresentation(getRequestedObject(), getRootRef());
         } else {
             return super.get(variant);
         }
     }
 
     @Override
     protected Representation put(Representation representation, Variant variant) throws ResourceException {
         if (getRequestedObject() == null) {
             setClientErrorReason("This resource can not create new subjects. New subjects may only be created during registration.");
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Update only");
         }
         SubjectJsonRepresentation inRep;
         try {
             inRep = new SubjectJsonRepresentation(new JsonRepresentation(representation).getJsonObject());
         } catch (JSONException e) {
             throw new ResourceException(
                 Status.CLIENT_ERROR_NOT_ACCEPTABLE, "The entity is not proper JSON", e);
         } catch (IOException e) {
             throw new ResourceException(
                 Status.CLIENT_ERROR_BAD_REQUEST, "The entity could not be parsed", e);
         }
 
         BeanWrapper in = new BeanWrapperImpl(inRep.getSubject());
         BeanWrapper out = new BeanWrapperImpl(getRequestedObject());
 
         for (String property : UPDATEABLE_PROPERTIES) {
             out.setPropertyValue(property, in.getPropertyValue(property));
         }
         subjectDao.save(getRequestedObject());
 
         return new SubjectJsonRepresentation(getRequestedObject(), getRootRef());
     }
 
     ////// CONFIGURATION
 
     @Required
     public void setSubjectDao(SubjectDao subjectDao) {
         this.subjectDao = subjectDao;
     }
 }
