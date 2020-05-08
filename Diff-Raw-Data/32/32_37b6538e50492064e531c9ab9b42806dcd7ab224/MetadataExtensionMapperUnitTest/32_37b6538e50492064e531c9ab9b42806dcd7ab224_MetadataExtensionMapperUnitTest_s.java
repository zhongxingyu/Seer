 package com.zenika.dorm.core.test.model.mapper;
 
 import com.zenika.dorm.core.model.DormMetadataExtension;
import com.zenika.dorm.core.model.impl.DefaultDormMetadataExtension;
 import com.zenika.dorm.core.model.mapper.MetadataExtensionMapper;
 import org.fest.assertions.Assertions;
 import org.junit.Test;
 
import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class MetadataExtensionMapperUnitTest {
 
     @Test
     public void fromExtension() {
 
         DormMetadataExtension extension = new TestMetadataExtension("testfoo", "testbar");
 
         Map<String, String> properties = MetadataExtensionMapper.fromExtension(extension);
 
         Assertions.assertThat(properties.get("foo")).isEqualTo("testfoo");
         Assertions.assertThat(properties.get("bar")).isNull();
     }
 
     /**
     * Should fail because DefaultDormOrigin is immutable
     */
    @Test
    public void toExtension() {

        // create null origin with fake attribute "name"
        DormMetadataExtension extension = new DefaultDormMetadataExtension("fake");

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("name", "foo");

        MetadataExtensionMapper.toExtension(extension, properties);

        Assertions.assertThat(((DefaultDormMetadataExtension) extension).getName()).isEqualTo("foo");
    }

    /**
      * Extension for test purposes
      */
     public class TestMetadataExtension implements DormMetadataExtension {
 
         private String foo;
        private transient String bar;

        public TestMetadataExtension() {
        }
 
         public TestMetadataExtension(String foo, String bar) {
             this.foo = foo;
             this.bar = bar;
         }
 
         @Override
         public String getQualifier() {
             return null;
         }
 
         @Override
         public String getExtensionName() {
             return null;
         }
 
         @Override
         public DormMetadataExtension createFromMap(Map<String, String> properties) {
             return null;
         }
     }
 }
