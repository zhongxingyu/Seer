 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
 import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
 import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
 import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
 import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduledActivityReportCsvRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.restlets.representations.ScheduledActivityReportJsonRepresentation;
 import edu.northwestern.bioinformatics.studycalendar.service.ReportService;
 import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
 import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
 import gov.nih.nci.security.AuthorizationManager;
 import gov.nih.nci.security.authorization.domainobjects.User;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Variant;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
 
 /**
  * @author Nataliya Shurupova
  * @author Rhett Sutphin
  */
 public class ScheduledActivityReportResource extends AbstractCollectionResource<ScheduledActivitiesReportRow> {
     private ActivityTypeDao activityTypeDao;
     private ReportService reportService;
     private AuthorizationManager csmAuthorizationManager;
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
 
         addAuthorizationsFor(Method.GET,
             STUDY_SUBJECT_CALENDAR_MANAGER,
             STUDY_TEAM_ADMINISTRATOR,
             DATA_READER);
 
         getVariants().add(new Variant(MediaType.APPLICATION_JSON));
         getVariants().add(new Variant(PscMetadataService.TEXT_CSV));
         getVariants().add(new Variant(MediaType.APPLICATION_EXCEL));
     }
 
     @Override
     @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
     public Collection<ScheduledActivitiesReportRow> getAllObjects() throws ResourceException {
         return reportService.searchScheduledActivities(buildFilters());
     }
 
     // exposed for testing
     ScheduledActivitiesReportFilters buildFilters() throws ResourceException {
         ScheduledActivitiesReportFilters filters = new ScheduledActivitiesReportFilters();
 
         applyActivityTypeFilter(filters);
         applyStateFilter(filters);
         applyResponsibleUserFilter(filters);
         applyDateRangeFilters(filters);
 
         filters.setSiteName(FilterParameters.SITE.extractFrom(getRequest()));
         filters.setLabel(FilterParameters.LABEL.extractFrom(getRequest()));
         filters.setStudyAssignedIdentifier(FilterParameters.STUDY.extractFrom(getRequest()));
         filters.setPersonId(FilterParameters.PERSON_ID.extractFrom(getRequest()));
 
         return filters;
     }
 
     private void applyDateRangeFilters(ScheduledActivitiesReportFilters filters) throws ResourceException {
         String start_date = FilterParameters.START_DATE.extractFrom(getRequest());
         String end_date = FilterParameters.END_DATE.extractFrom(getRequest());
         MutableRange<Date> range = new MutableRange<Date>();
         if (start_date != null) {
             try {
                 Date startDate = getApiDateFormat().parse(start_date);
                 range.setStart(startDate);
             } catch (ParseException pe) {
               throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", pe);
             }
         }
         if (end_date != null) {
             try {
                 Date endDate = getApiDateFormat().parse(end_date);
                 range.setStop(endDate);
                 filters.setActualActivityDate(range);
             } catch (ParseException pe) {
               throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unparseable entity", pe);
             }
         }
 
         filters.setActualActivityDate(range);
     }
 
     private void applyResponsibleUserFilter(ScheduledActivitiesReportFilters filters) throws ResourceException {
         String responsible_user = FilterParameters.RESPONSIBLE_USER.extractFrom(getRequest());
         if (responsible_user != null) {
             User csmUser = csmAuthorizationManager.getUser(responsible_user);
             if (csmUser == null) {
                 throw new ResourceException(
                     Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                    "Unknown user for responsible_user filter");
             } else {
                 filters.setResponsibleUser(csmUser);
             }
         }
     }
 
     private void applyStateFilter(
         ScheduledActivitiesReportFilters filters
     ) throws ResourceException {
         String state = FilterParameters.STATE.extractFrom(getRequest());
         if (state != null) {
             ScheduledActivityMode scheduledActivityMode = ScheduledActivityMode.getByName(state);
             if (scheduledActivityMode != null) {
                 filters.setCurrentStateMode(scheduledActivityMode);
             } else {
                 throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY,
                     "Invalid scheduled activity state name for state filter: " + state);
             }
         }
     }
 
     private void applyActivityTypeFilter(ScheduledActivitiesReportFilters filters) {
         String activity_type = FilterParameters.ACTIVITY_TYPE.extractFrom(getRequest());
         if (activity_type != null) {
             ActivityType activityType = activityTypeDao.getById(new Integer(activity_type));
             filters.setActivityType(activityType);
         }
     }
 
     @Override
     public Representation represent(Variant variant) throws ResourceException {
         List<ScheduledActivitiesReportRow> allRows = new ArrayList<ScheduledActivitiesReportRow>(getAllObjects());
         if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
             return new ScheduledActivityReportJsonRepresentation(buildFilters(), allRows);
         }
         if (variant.getMediaType().equals(PscMetadataService.TEXT_CSV)) {
             return new ScheduledActivityReportCsvRepresentation(allRows, ',');
         }
         if (variant.getMediaType().equals(MediaType.APPLICATION_EXCEL)) {
             return new ScheduledActivityReportCsvRepresentation(allRows, '\t');
         }
         return null;
     }
 
     @Override
     public StudyCalendarXmlCollectionSerializer<ScheduledActivitiesReportRow> getXmlSerializer() {
         return null;
     }
 
     @Required
     public void setActivityTypeDao(ActivityTypeDao activityTypeDao) {
         this.activityTypeDao = activityTypeDao;
     }
 
     @Required
     public void setReportService(ReportService reportService) {
         this.reportService = reportService;
     }
 
     @Required
     public void setCsmAuthorizationManager(AuthorizationManager csmAuthorizationManager) {
         this.csmAuthorizationManager = csmAuthorizationManager;
     }
 }
