 package pl.rawie.timetrack.domain.validator;
 
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import org.springframework.validation.Validator;
 import pl.rawie.timetrack.domain.model.Entry;
 import pl.rawie.timetrack.domain.model.SampleEntry;
 
 import static org.junit.Assert.assertThat;
 import static pl.rawie.timetrack.domain.validator.ValidatorMatchers.hasFieldError;
 
 public class AddEntryValidatorTest {
     private Validator validator = new AddEntryValidator();
 
     private void validate(Entry entry, Matcher<ValidationError>... matchers) {
         try {
             ValidatorUtils.invoke(validator, entry, "entry");
             throw new ValidationError();
         } catch (ValidationError e) {
             for (Matcher<ValidationError> matcher : matchers)
                 assertThat(e, matcher);
         }
     }
 
     @Test
     public void invalidSummary() {
         Entry entry = SampleEntry.builder()
                 .withSummary("")
                 .build();
         validate(entry, hasFieldError("summary", "required"));
     }
 
     @Test
     public void invalidStart() {
         Entry entry = SampleEntry.builder()
                 .withStart(null)
                 .build();
         validate(entry, hasFieldError("start", "required"));
     }
 
     @Test
     public void invalidEnd() {
         Entry entry = SampleEntry.builder()
                 .withEnd(null)
                 .build();
        validate(entry, hasFieldError("start", "required"));
     }
 }
 
 
