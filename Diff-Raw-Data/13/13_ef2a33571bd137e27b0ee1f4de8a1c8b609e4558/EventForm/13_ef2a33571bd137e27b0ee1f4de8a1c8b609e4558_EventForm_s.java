 package org.sukrupa.event;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Sets;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.sukrupa.platform.date.Date;
 import org.sukrupa.student.Student;
 import org.sukrupa.student.StudentRepository;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 public class EventForm implements Validator {
 
     private static final String VALID_TIME_FORMAT = "(|\\d\\d?:\\d\\d)";
     private int id;
     private String title;
     private String date;
     private String venue;
     private String coordinator;
     private String description;
     private String notes;
     private List<String> attendees;
 
     private String endTime;
     private String endTimeAmPm;
     private String startTime;
     private String startTimeAmPm;
 
 
     public EventForm() {
     }
 
     public EventForm(int id,                String title,       String date,
                      String endTime,        String venue,       String coordinator,
                      String description,    String notes,       List<String> attendingStudents,
                      String startTime) {
         this.id = id;
         this.title = title;
         this.date = date;
         this.endTime = endTime;
         this.venue = venue;
         this.coordinator = coordinator;
         this.description = description;
         this.notes = notes;
         this.attendees = attendingStudents;
         this.startTime = startTime;
     }
 
     public Event createEvent() {
         return new Event(getTitle(),
                 Date.parse(getDate(), new Time(getEndTime(), getEndTimeAmPm())),
                 getVenue(),
                 getCoordinator(),
                 getDescription(),
                 getNotes(),
                 Date.parse(getDate(), new Time(getStartTime(), getStartTimeAmPm())));
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDate() {
         return date;
     }
 
     public void setDate(String date) {
         this.date = date;
     }
 
     public String getEndTime() {
         return returnNullIfEmpty(endTime);
     }
 
     public void setEndTime(String endTime) {
         this.endTime = endTime;
     }
 
     public String getVenue() {
         return returnNullIfEmpty(venue);
     }
 
     public void setVenue(String venue) {
         this.venue = venue;
     }
 
     public String getCoordinator() {
         return returnNullIfEmpty(coordinator);
     }
 
     public void setCoordinator(String coordinator) {
         this.coordinator = coordinator;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getNotes() {
         return returnNullIfEmpty(notes);
     }
 
     private String returnNullIfEmpty(String value) {
         return (value.isEmpty()) ? null : value;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
     public List<String> getAttendees() {
         return attendees;
     }
 
     public String getAttendeesIdsForDisplay() {
         return getAttendees().toString();
     }
 
     public Set<String> getStudentIdsOfAttendees() {
         Set<String> attendingStudentsID= new HashSet<String>();
         try{
             for(String attendingStudent:attendees){
                 attendingStudentsID.add(attendingStudent);
             }
        } catch (NullPointerException e) {};
         return attendingStudentsID;
     }
 
     public void setAttendingStudents(List<String> attendingStudents) {
         this.attendees = attendingStudents;
     }
 
     public void setEndTimeAmPm(String amPm) {
         this.endTimeAmPm = amPm;
     }
 
     public String getEndTimeAmPm() {
         return endTimeAmPm;
     }
 
     public void setStartTime(String startTime) {
         this.startTime = startTime;
     }
 
     public void setStartTimeAmPm(String startTimeAmPm) {
         this.startTimeAmPm = startTimeAmPm;
     }
 
     public String getStartTime() {
         return startTime;
     }
 
     public String getStartTimeAmPm() {
         return startTimeAmPm;
     }
 
     public boolean isInvalid(Errors errors) {
         validate(this, errors);
         return errors.hasErrors();
     }
 
     @Override
     public boolean supports(Class<?> clazz) {
         return EventForm.class.isAssignableFrom(clazz);
     }
 
     @Override
     public void validate(Object target, Errors errors) {
         EventForm eventForm = (EventForm) target;
 
         validateTimeField(errors, eventForm.startTime, "startTime");
         validateTimeField(errors, eventForm.endTime, "endTime");
     }
 
     private void validateTimeField(Errors errors, String field, String fieldName) {
         boolean validTimeFormat = field.matches(VALID_TIME_FORMAT);
         if (!validTimeFormat) {
             errors.rejectValue(fieldName, "", invalidTimeErrorMessage(fieldName));
         }
 
         if (validTimeFormat && !isBlank(field)) {
             int hours = Integer.parseInt(field.split(":")[0]);
             int minutes = Integer.parseInt(field.split(":")[1]);
             if ((hours > 12) || (hours < 1) || (minutes > 59) || (minutes < 0)) {
                 errors.rejectValue(fieldName, "", invalidTimeErrorMessage(fieldName));
             }
         }
     }
 
     public String invalidTimeErrorMessage(String fieldName) {
         String[] strings = StringUtils.splitByCharacterTypeCamelCase(fieldName);
         strings[1]= strings[1].toLowerCase();
        return String.format("Please enter <strong>%s</strong> in the 00:00 format using the 12 hour clock.", StringUtils.join(strings, " "));
     }
 
 }
