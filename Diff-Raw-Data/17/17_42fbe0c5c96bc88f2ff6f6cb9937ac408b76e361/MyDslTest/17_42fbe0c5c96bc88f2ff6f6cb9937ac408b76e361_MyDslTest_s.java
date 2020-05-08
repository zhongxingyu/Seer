 package org.xtext.example.mydsl;
 
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.xtext.junit4.InjectWith;
 import org.eclipselabs.xtext.utils.unittesting.XtextRunner2;
 import org.eclipselabs.xtext.utils.unittesting.XtextTest;
import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(XtextRunner2.class)
 @InjectWith(MyDslInjectorProvider.class)
 public class MyDslTest extends XtextTest {
    @Before
    public void before() {
        super.before();
        suppressSerialization();
    }
 
     @Test
    public void test_mod4j_busmod() {
        testFile("res/test.mydsl");
     }
 }
