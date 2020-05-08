 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.senacor.mocking;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 /**
  *
  * @author mfochler
  */
 public class MockitoLimitations {
     
     // The class we test
     @InjectMocks
     private Activity instance;
     
     // The class we want to mock
     @Mock
     private Brick brickmock;
     
     @Before
     public void createMocks(){
         // *** create test instance ***
         instance = new ActivityImpl();
 
         // *** create mocks ***
         MockitoAnnotations.initMocks(this);
     }
     
     @Test
     public void testToString() {
         // mock function
         Mockito.when(brickmock.toString()).thenAnswer(new Answer() {
 
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 // we call sysout to prove the call of the mock
                 System.out.println("called toString()");
                 return "mocked toString()";
             }
         });
         
         // call the function to test
         String actual = instance.toString();
         
         // verify the mocked function
         // Mockito can not differ
         // Mockito.verify(brickmock).toString();
         // but the function was mocked
         Assert.assertEquals("no mocked toString()", "mocked toString()", actual);
     }
     
     @Test
     public void testFinalize() throws Throwable {
         // mock function
         Mockito.doAnswer(new Answer() {
 
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 // we call sysout to prove the call of the mock
                System.out.println("called finalize()");
                 return null;
             }
         }).when(brickmock).finalize();
         
         // call the function to test
         instance.finalize();
         
         // verify the mocked function
         Mockito.verify(brickmock).finalize();
     }
 }
