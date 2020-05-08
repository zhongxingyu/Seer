 package com.mpower.test.controller.validator;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.springframework.validation.BindException;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.mpower.controller.validator.PaymentSourceValidator;
 import com.mpower.domain.PaymentSource;
 import com.mpower.domain.Person;
 import com.mpower.service.PaymentSourceService;
 import com.mpower.test.BaseTest;
 
 public class PaymentSourceValidatorTest extends BaseTest {
 
     private PaymentSourceValidator validator;
     private PaymentSource source;
     private BindException errors;
     private Mockery mockery;
     private final Long PERSON_ID = 1L;
 
     @BeforeMethod
     public void setupMocks() {
         mockery = new Mockery();
         validator = new PaymentSourceValidator();
 
         source = new PaymentSource();
         Person person = new Person();
         person.setId(PERSON_ID);
         source.setPerson(person);
 
         errors = new BindException(source, "paymentSource");
 
         final PaymentSourceService service = mockery.mock(PaymentSourceService.class);
         validator.setPaymentSourceService(service);
 
         mockery.checking(new Expectations() {{
             allowing (service).findPaymentSourceProfile(PERSON_ID, "MyProfile"); will(returnValue(new PaymentSource()));
         }});
     }
 
     @Test(groups = { "validatePaymentProfile" })
     public void testValidPaymentProfile() throws Exception {
         source.setProfile(null);
         validator.validatePaymentProfile(source, errors);
 
         mockery.assertIsSatisfied();
         assert errors.hasFieldErrors() == false;
     }
 
     @Test(groups = { "validatePaymentProfile" })
     public void testBlankPaymentProfile() throws Exception {
         source.setProfile("  ");
         validator.validatePaymentProfile(source, errors);
 
         mockery.assertIsSatisfied();
         assert "blankPaymentProfile".equals(errors.getFieldError("profile").getCode());
     }
 
     @Test(groups = { "validatePaymentProfile" })
     public void testExistingPaymentProfileForNewPaymentSource() throws Exception {
        source.setId(null);
         source.setProfile("MyProfile");
         validator.validatePaymentProfile(source, errors);
 
         mockery.assertIsSatisfied();
         assert "paymentProfileAlreadyExists".equals(errors.getFieldError("profile").getCode());
     }
 
     @Test(groups = { "validatePaymentProfile" })
     public void testExistingPaymentProfileForExistingPaymentSource() throws Exception {
         source.setId(new Long(1));
         source.setProfile("MyProfile");
         validator.validatePaymentProfile(source, errors);
 
         mockery.assertIsSatisfied();
         assert errors.hasFieldErrors() == false;
     }
 }
