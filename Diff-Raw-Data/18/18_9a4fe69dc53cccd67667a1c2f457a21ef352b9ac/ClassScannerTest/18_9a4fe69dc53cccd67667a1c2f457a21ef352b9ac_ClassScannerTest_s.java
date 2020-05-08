 package net.contextfw.web.application.internal.util;
 
import static org.junit.Assert.*;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Test;
 
 public class ClassScannerTest {
 
     @Test
     public void testClassScanning() {
         List<Class<?>> classes = ClassScanner.getClasses(
                "net.contextfw.web.application.internal.util");
         
         Set<Class<?>> clsSet = new HashSet<Class<?>>();
         clsSet.addAll(classes);
         
         assertTrue(clsSet.contains(ClassScannerTest.class));
         assertTrue(clsSet.contains(ResourceScannerTest.class));
         assertTrue(clsSet.contains(ClassScanner.class));
         assertTrue(clsSet.contains(ResourceScanner.class));
     }
     
 }
 
