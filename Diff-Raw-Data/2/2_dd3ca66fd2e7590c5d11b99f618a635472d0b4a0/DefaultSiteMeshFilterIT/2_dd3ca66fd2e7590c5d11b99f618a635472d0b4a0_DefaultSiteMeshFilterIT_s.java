 package org.stu.arch;
 
 import org.sitemesh.builder.SiteMeshFilterBuilder;
 import org.testng.annotations.Test;
 
import static org.junit.Assert.fail;

 public class DefaultSiteMeshFilterIT{
     @Test
     public void testCustomConfiguration() {
         SiteMeshFilterBuilder siteMeshFilterBuilder = new SiteMeshFilterBuilder();
         new DefaultSiteMeshFilter().applyCustomConfiguration(siteMeshFilterBuilder);
     }
 }
