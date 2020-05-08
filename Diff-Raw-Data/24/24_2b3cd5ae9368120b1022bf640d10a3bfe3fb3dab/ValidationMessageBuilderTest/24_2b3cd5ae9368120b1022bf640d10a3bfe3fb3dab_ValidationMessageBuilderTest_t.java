 package org.burgers.email.service.validation;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ValidationMessageBuilderTest {
     @InjectMocks
     private ValidationMessageBuilder builder;
     private SimpleValidationContext context;
 
     @Before
     public void setup(){
         context = new SimpleValidationContext();
         context.error("field1", "missing");
         context.error("field2", "fail1");
         context.error("field2", "fail2");
     }
 
     @Test
     public void build(){
         String message = builder.build(context);
 
         System.out.println(message);
 
        assertEquals("\nMessage Validation Failed:\n\tfield1:\n\t\tmissing\n\tfield2:\n\t\tfail1\n\t\tfail2\n", message);
     }
 
 }
