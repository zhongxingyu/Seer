 package edu.northwestern.bioinformatics.studycalendar.xml;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
 import org.dom4j.Element;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Enum for the names of attributes defined in psc.xsd.  Enum values should
  * be added for distinct attributes -- i.e., there should be two separate enum
  * values for two attributes named "name" if they are defined separately in the
  * XSD.
  *
  * @author Rhett Sutphin
  */
 public enum XsdAttribute {
     ACTIVITY_NAME("name"),
     ACTIVITY_CODE("code"),
     ACTIVITY_DESC("description"),
     ACTIVITY_TYPE_ID("type-id"),
     ACTIVITY_TYPE("type"),
     ACTIVITY_SOURCE("source"),
 
 
     ACTIVITY_SOURCE_NAME("name"),
     ACTIVITY_PROPERTY_NAMESPACE("namespace"),
     ACTIVITY_PROPERTY_NAME("name"),
     ACTIVITY_PROPERTY_VALUE("value"),
 
     PLANNED_ACTIVITY_LABEL_NAME("label"),
     PLANNED_ACTIVITY_LABEL_ID("planned-activity-label-id"),
     LABEL_NAME("name"),
     LABEL_REP_NUM("repetition-number"),
 
     POPULATION_NAME("name"),
     POPULATION_ABBREVIATION("abbreviation"),
 
     STUDY_SNAPSHOT_ASSIGNED_IDENTIFIER("assigned-identifier"),
     REGISTRATION_FIRST_STUDY_SEGMENT_ID("first-study-segment-id"),
     REGISTRATION_DATE("date"),
     REGISTRATION_SUBJECT_COORDINATOR_NAME("subject-coordinator-name"),
     REGISTRATION_DESIRED_ASSIGNMENT_ID("desired-assignment-id"),
    REGISTRATION_STUDY_SUBJECT_ID("study-subject-id"),
     ASSIGNMENT_STUDY_NAME("study-name"),
     ASSIGNMENT_SITE_NAME("site-name"),
     SUBJECT_FIRST_NAME("first-name"),
     SUBJECT_LAST_NAME("last-name"),
     SUBJECT_BIRTH_DATE("birth-date"),
     SUBJECT_PERSON_ID("person-id"),
     ASSIGNMENT_CURRENT_AMENDMENT("current-amendment-key"),
     ASSIGNMENT_SUBJECT_COORD("subject-coordinator-name"),
     ASSIGNMENT_START_DATE("start-date"),
     ASSIGNMENT_END_DATE("end-date"),
     STUDY_SITE_STUDY_NAME("study-name"),
     STUDY_SITE_SITE_NAME("site-name"),
     SITE_SITE_NAME("site-name"),
     SITE_ASSIGNED_IDENTIFIER("assigned-identifier"),
     SITE_PROVIDER("provider"),
     SUBJECT_GENDER("gender"),
     ASSIGNMENT_ID("id"),
     SCHEDULED_CALENDAR_ASSIGNMENT_ID("assignment-id"),
     SCHEDULED_CALENDAR_ID("id"),
     BLACKOUT_DATE_DESCRIPTION("description"),
     BLACKOUT_DATE_DAY("day"),
     BLACKOUT_DATE_MONTH("month"),
     BLACKOUT_DATE_SITE_ID("site-identifier"),
     BLACKOUT_DATE_YEAR("year"),
     BLACKOUT_DATE_DAY_OF_WEEK("day-of-the-week"),
     BLACKOUT_DATE_WEEK_NUMBER("week-number"),
     BLACKOUT_DATE_ID("id"),
     SCHEDULED_STUDY_SEGMENT_ID("id"),
     SCHEDULED_STUDY_SEGMENT_START_DATE("start-date"),
     SCHEDULED_STUDY_SEGMENT_START_DAY("start-day"),
     SCHEDULED_STUDY_SEGMENT_STUDY_SEGMENT_ID("study-segment-id"),
     SCHEDULED_ACTIVITY_ID("id"),
     SCHEDULED_ACTIVITY_IDEAL_DATE("ideal-date"),
     SCHEDULED_ACTIVITY_NOTES("notes"),
     SCHEDULED_ACTIVITY_DETAILS("details"),
     SCHEDULED_ACTIVITY_REPETITION_NUMBER("repetition-number"),
     SCHEDULED_ACTIVITY_PLANNED_ACITIVITY_ID("planned-activity-id"),
     SCHEDULED_ACTIVITY_STATE_REASON("reason"),
     SCHEDULED_ACTIVITY_STATE_DATE("date"),
     SCHEDULED_ACTIVITY_STATE_STATE("state"),
     AMENDMENT_APPROVAL_DATE("date"),
     AMENDMENT_APPROVAL_AMENDMENT("amendment"),
     AMENDMENT_PREVIOUS_AMENDMENT_KEY("previous-amendment-key"),
     AMENDMENT_NAME("name"),
     AMENDMENT_DATE("date"),
     PLAN_TREE_NODE_NAME("name"),
     STUDY_ASSIGNED_IDENTIFIER("assigned-identifier"),
     STUDY_PROVIDER("provider"),
     SECONDARY_IDENTIFIER_TYPE("type"),
     SECONDARY_IDENTIFIER_VALUE("value"),
     LAST_MODIFIED_DATE("last-modified-date"),
     RELEASED_DATE("released-date"),
     UPDATED_DATE("updated-date"),
 
     NEXT_STUDY_SEGMENT_SCHEDULE_START_DATE("start-date"),
     NEXT_STUDY_SEGMENT_SCHEDULE_START_DAY("start-day"),
     NEXT_STUDY_SEGMENT_SCHEDULE_STUDY_SEGMENT_ID("study-segment-id"),
     NEXT_STUDY_SEGMENT_SCHEDULE_MODE("mode"),
 
     NOTIFICATION_MESSAGE("message"),
     NOTIFICATION_TITLE("title"),
     NOTIFICATION_ACTION_REQUIRED("action-required"),
     NOTIFICATION_DISMISSED("dismissed"),
     NOTIFICATION_ID("id"),
 
     USER_ROLE_NAME("name"),
     ALL("all");
 
     private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 
     private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
 
     private String attributeName;
 
     private XsdAttribute(String attrname) {
         this.attributeName = attrname;
     }
 
     public String xmlName() {
         return attributeName;
     }
 
     public String from(Element elt) {
         return elt.attributeValue(xmlName());
     }
 
     public Date fromDate(Element elt) {
         String dateString = from(elt);
         if (dateString == null) {
             return null;
         }
 
         try {
             return formatter.parse(dateString);
         } catch (ParseException pe) {
             throw new StudyCalendarValidationException("Problem parsing date %s", pe, dateString);
         }
     }
 
     /**
      * Parse element in to date time format as in ISO8601-style
      *
      * @param elt
      * @return
      */
     public Date fromDateTime(Element elt) {
         String dateString = from(elt);
         if (dateString == null) {
             return null;
         }
 
         try {
             return dateTimeFormat.parse(dateString);
         } catch (ParseException pe) {
             throw new StudyCalendarValidationException("Problem parsing date %s", pe, dateString);
         }
     }
 
     public void addTo(Element elt, Object value) {
         elt.addAttribute(xmlName(), value == null ? null : value.toString());
     }
 
     public void addToDateTime(Element elt, Date value) {
         elt.addAttribute(xmlName(), value == null ? null : dateTimeFormat.format(value));
     }
 
     public void addTo(Element elt, Date value) {
         elt.addAttribute(xmlName(), value == null ? null : formatter.format(value));
     }
 
 }
