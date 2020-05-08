 package org.sukrupa.event;
 
 import org.junit.Test;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Errors;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.sukrupa.platform.hamcrest.CollectionMatchers.hasOnly;
 
 public class EventFormTest {
 
     @Test
     public void shouldReturnStudentIdsOfAttendees() {
         EventForm eventForm = new EventForm();
         eventForm.setAttendees("1,2,3");
         assertThat(eventForm.getStudentIdsOfAttendees(), hasOnly("1", "2", "3"));
     }
 
     @Test
     public void shouldReturnStudentIdOfAttendeesWithoutEnter() {
         EventForm eventForm = new EventForm();
         eventForm.setAttendees("34545, 83415, 64262\r\n");
         assertThat(eventForm.getStudentIdsOfAttendees(), hasOnly("34545", "83415", "64262"));
     }
 
     @Test
     public void shouldOnlyAllowStartTimeBetween1And12() {
         EventForm eventForm = createEventFormWithStartTime("13:00");
 
         Errors errors = new BeanPropertyBindingResult(eventForm, "drainer");
 
         boolean invalid = eventForm.isInvalid(errors);
 
         assertThat(invalid, is(true));
         assertThat(errors.getFieldErrorCount("startTime"), is(1));
     }
 
     @Test
     public void shouldNotAllowNegativeTime() {
         EventForm eventForm = createEventFormWithStartTime("-13:00");
 
         Errors errors = new BeanPropertyBindingResult(eventForm, "drainer");
 
         boolean invalid = eventForm.isInvalid(errors);
 
         assertThat(invalid, is(true));
         assertThat(errors.getFieldErrorCount("startTime"), is(1));
     }
 
     @Test
     public void shouldNotAllowSlightlyCrazilyFormattedTime() {
         EventForm eventForm = createEventFormWithStartTime("13.00");
 
         Errors errors = new BeanPropertyBindingResult(eventForm, "drainer");
 
         boolean invalid = eventForm.isInvalid(errors);
 
         assertThat(invalid, is(true));
         assertThat(errors.getFieldErrorCount("startTime"), is(1));
     }
 
     @Test
     public void shouldNotAllowReallyCrazilyFormattedTime() {
         EventForm eventForm = createEventFormWithStartTime("drainers");
 
         Errors errors = new BeanPropertyBindingResult(eventForm, "drainer");
 
         boolean invalid = eventForm.isInvalid(errors);
 
         assertThat(invalid, is(true));
         assertThat(errors.getFieldErrorCount("startTime"), is(1));
     }
 
     @Test
     public void shouldAllowBlankStartTime() {
         EventForm eventForm = createEventFormWithStartTime("");
 
         Errors errors = new BeanPropertyBindingResult(eventForm, "drainers");
 
         eventForm.isInvalid(errors);
 
         assertThat(errors.getFieldErrorCount("startTime"), is(0));
 
     }
 
     @Test
     public void yaelSkillsFormattingTheErrorMessage() {
         String message = new EventForm().invalidTimeErrorMessage("startTime");
 
        assertThat(message, is("Please enter <strong>start time</strong> in the 00:00 format using the 12 hour clock."));
     }
 
     private EventForm createEventFormWithStartTime(String startTime) {
         EventForm eventForm = new EventForm();
         eventForm.setStartTime(startTime);
         eventForm.setEndTime("");
         return eventForm;
     }
 
 
 }
