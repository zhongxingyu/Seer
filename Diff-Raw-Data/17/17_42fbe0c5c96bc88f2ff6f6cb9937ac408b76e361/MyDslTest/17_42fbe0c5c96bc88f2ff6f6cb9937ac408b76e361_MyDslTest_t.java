 package org.xtext.example.mydsl;
 
 import org.eclipse.xtext.junit4.InjectWith;
 import org.eclipselabs.xtext.utils.unittesting.XtextRunner2;
 import org.eclipselabs.xtext.utils.unittesting.XtextTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(XtextRunner2.class)
 @InjectWith(MyDslInjectorProvider.class)
 public class MyDslTest extends XtextTest {
 
     @Test
    public void testMydsl() {
        testFileNoSerializer("test.mydsl");
     }
 }
