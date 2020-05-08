 package com.github.sfleiter.cdi_interceptors.impl;
 
 import static org.junit.Assert.*;
 
 import javax.inject.Inject;
 
 import org.jglue.cdiunit.AdditionalClasses;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.github.sfleiter.cdi_interceptors.util.LogbackTestAppender;
 
 @RunWith(CdiRunner.class)
 @AdditionalClasses(value={LoggingInterceptor.class})
 public class LoggingInterceptorTest {
 
     @Before
     public void setUp() throws Exception {}
 
     @Inject
     private InterceptedDefault sut;
     
     @Test
     public void testDefaultNoParamsNullResult() throws Exception {
         sut.run();
         assertEquals("INFO call run() returns null", LogbackTestAppender.getMessage());
     }
 
     @Test
     public void testDefaultIntParam() throws Exception {
         sut.run(42);
         assertEquals("INFO call run(objectParam=42) returns 42", LogbackTestAppender.getMessage());
     }
     
     @Test
     public void testDefaultStringIntParam() throws Exception {
         sut.run("foo", 42);
         assertEquals("INFO call run(stringParam=foo, objectParam=42) returns 42", LogbackTestAppender.getMessage());
     }
     
     @Test
     public void testDefaultStringArrayParam() throws Exception {
         sut.run("foo", new Integer[] {1, 2, 3});
         assertEquals("INFO call run(stringParam=foo, objectParam=Collection[size=3, 1, 2, 3]) returns Collection[size=3, 1, 2, 3]", LogbackTestAppender.getMessage());
     }
     
    @Test @Ignore
     public void testDefaultStringPrimitiveArrayParam() throws Exception {
        sut.run("foo", new int[] {1, 2, 3});
        assertEquals("INFO call run(stringParam=foo, objectParam=Collection[size=3, 1, 2, 3]) returns Collection[size=3, 1, 2, 3]", LogbackTestAppender.getMessage());
     }
     
 }
