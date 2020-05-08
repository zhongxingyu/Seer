 package org.wv.stepsovc.web;
 
 import static org.mockito.Mockito.mock;
 
 public class MockFactory {
 
    public static <T> T create(Class<T> c) {
        return mock(c);
     }
 }
