 package com.jexpect;
 
 import org.junit.Test;
 
 import static com.jexpect.ExceptionHandler.getMessage;
 import static com.jexpect.Expect.expect;
 import static junit.framework.Assert.assertEquals;
 
 public class ToBeNumberTest {
 
   @Test
   public void When_Expected_To_Be_Greater_Than_Fails_Then_Exception_Should_Have_Message() throws Exception {
    assertEquals("Expected 5 > 10",
                  getMessage(new Command() {
                    @Override
                    public void execute() {
                      expect(5L).toBeGreaterThan(10L);
                    }
                  }));
   }
 
   @Test
   public void When_Expected_To_Be_Greater_Than_Null_Fails_Then_Exception_Should_Have_Message() throws Exception {
    assertEquals("Expected 5 > null",
                  getMessage(new Command() {
                    @Override
                    public void execute() {
                      expect(5L).toBeGreaterThan(null);
                    }
                  }));
   }
 }
