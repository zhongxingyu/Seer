 package org.motechproject.ghana.national.validator.field;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.mobileforms.api.domain.FormError;
 import org.motechproject.openmrs.omod.validator.MotechIdVerhoeffValidator;
 import org.motechproject.openmrs.omod.validator.VerhoeffValidator;
 
 import java.lang.annotation.Annotation;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class MotechIdValidatorTest {
     private MotechIdValidator motechIdValidator;
 
     @Before
     public void setUp() {
         initMocks(this);
         motechIdValidator = new MotechIdValidator();
     }
 
     @Test
     public void shouldValidateIfTheAnnotatedIdFieldIsAValidMotechId() throws IllegalAccessException, InstantiationException {
         motechIdValidator = spy(motechIdValidator);
         String fieldValue = "1234567";
         String fieldName = "motechId";
 
         final Class<? extends VerhoeffValidator> validatorClass = MotechIdVerhoeffValidator.class;
         MotechId motechId = new MotechId() {
 
             @Override
             public Class<? extends VerhoeffValidator> validator() {
                 return validatorClass;
             }
 
             @Override
             public Class<? extends Annotation> annotationType() {
                 return MotechId.class;
             }
         };
         VerhoeffValidator motechIdVerhoeffValidatorMock = mock(VerhoeffValidator.class);
 
         when(motechIdValidator.getValidatorInstance(validatorClass)).thenReturn(motechIdVerhoeffValidatorMock);
        when(motechIdVerhoeffValidatorMock.isValid(fieldValue)).thenReturn(false);
         assertThat(motechIdValidator.validate(fieldValue, fieldName, String.class, motechId), is(equalTo(new FormError(fieldName, "is invalid"))));

        when(motechIdVerhoeffValidatorMock.isValid(fieldValue)).thenReturn(true);
        assertThat(motechIdValidator.validate(fieldValue, fieldName, String.class, motechId), is(equalTo(null)));
     }
 }
