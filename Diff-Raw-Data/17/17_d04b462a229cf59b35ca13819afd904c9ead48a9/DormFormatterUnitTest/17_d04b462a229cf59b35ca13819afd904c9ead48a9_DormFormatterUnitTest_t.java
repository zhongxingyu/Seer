 package com.zenika.dorm.core.test.util;
 
 import com.zenika.dorm.core.exception.CoreException;
 import com.zenika.dorm.core.util.DormFormatter;
 import org.fest.assertions.Assertions;
 import org.junit.Test;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class DormFormatterUnitTest {
 
     @Test
     public void formatValidMetadataExtensionQualifier() {
        Assertions.assertThat(DormFormatter.formatMetadataExtensionQualifier("com.foo:Bar Bar:Foo_Bar"))
                .isEqualTo("com.foo-Bar_Bar-Foo_Bar");
     }
 
     @Test(expected = CoreException.class)
     public void formatInvalidMetadataExtensionQualifier() {
         Assertions.assertThat(DormFormatter.formatMetadataExtensionQualifier("Foo:Bar-Toto:85"));
     }
 
     @Test
     public void formatValidMetadataVersion() {
         Assertions.assertThat(DormFormatter.formatMetadataVersion("1.0")).isEqualTo("1.0");
         Assertions.assertThat(DormFormatter.formatMetadataVersion("1")).isEqualTo("1");
         Assertions.assertThat(DormFormatter.formatMetadataVersion("1.0-SNAPSHOT")).isEqualTo("1.0-SNAPSHOT");
     }
 
     @Test(expected = CoreException.class)
     public void formatInvalidMetadataVersion() {
         DormFormatter.formatMetadataVersion("1.0 foo");
     }
 
     @Test
     public void formatValidMetadataType() {
         Assertions.assertThat(DormFormatter.formatMetadataType("Type42")).isEqualTo("Type42");
         Assertions.assertThat(DormFormatter.formatMetadataType("Type42-42")).isEqualTo("Type42-42");
     }
 
     @Test(expected = CoreException.class)
     public void formatInvalidMetadataType() {
         Assertions.assertThat(DormFormatter.formatMetadataType("Type42 - 42"));
     }
 
     @Test
     public void formatMetadataQualifier() {
         Assertions.assertThat(DormFormatter.formatMetadataQualifier("foo", "bar", "1.0", "type"))
                 .isEqualTo("foo:bar:1.0:type");
     }
 }
