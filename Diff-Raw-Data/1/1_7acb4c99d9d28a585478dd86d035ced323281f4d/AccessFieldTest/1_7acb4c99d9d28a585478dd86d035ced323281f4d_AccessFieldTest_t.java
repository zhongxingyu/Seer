 package no.niths.domain;
 
 import no.niths.application.rest.exception.BadRequestException;
 import no.niths.domain.signaling.AccessField;
 
 import org.junit.Test;
 
 public class AccessFieldTest {
 
     @Test(expected = BadRequestException.class)
     public void testCreateAccessFieldWithInvalidRanges() {
         AccessField accessField = new AccessField();
         accessField.setMinRange(2);
         accessField.setMaxRange(1);
     }
 }
