 package edu.northwestern.bioinformatics.studycalendar.xml.validators;
 
 import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator.*;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.BindException;
 import org.springframework.validation.ValidationUtils;
 
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 public class ActivityXMLValidatorTest extends StudyCalendarTestCase {
     private InputStream valid, invalid;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
        valid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/validators/data/ActivityXMLReaderTest.xml");
        invalid = new FileInputStream("src/test/java/edu/northwestern/bioinformatics/studycalendar/xml/validators/data/ActivityXMLReaderTest-Invalid.xml");
     }
 
     public void testValidate() {
         BindException errors = new BindException(valid, StringUtils.EMPTY);
         ValidationUtils.invokeValidator(ACTIVITY_VALIDATOR_INSTANCE, valid, errors);
 
         assertFalse(errors.hasErrors());
     }
 
     public void testInvalid() {
         BindException errors = new BindException(invalid, StringUtils.EMPTY);
         ValidationUtils.invokeValidator(ACTIVITY_VALIDATOR_INSTANCE, invalid, errors);
 
         assertTrue(errors.hasErrors());
         assertEquals("Wrong error count", 1, errors.getErrorCount());
         assertEquals("Wrong error code", "error.file.not.valid", errors.getGlobalError().getCode());
     }
 }
