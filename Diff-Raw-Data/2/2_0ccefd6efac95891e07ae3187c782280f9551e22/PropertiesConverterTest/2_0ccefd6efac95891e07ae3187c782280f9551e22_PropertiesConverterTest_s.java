 /**
  * Copyright 2010 CosmoCode GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.cosmocode.palava.core.inject;
 
 import java.util.Map;
 import java.util.Properties;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Named;
 import com.google.inject.name.Names;
 
 import de.cosmocode.junit.UnitProvider;
 
 /**
  * Tests {@link PropertiesConverter}.
  *
  * @author Willi Schoenborn
  */
 public final class PropertiesConverterTest implements UnitProvider<PropertiesConverter> {
 
     private static final TypeLiteral<Properties> LITERAL = TypeLiteral.get(Properties.class);
     
     @Override
     public PropertiesConverter unit() {
         return new PropertiesConverter();
     }
     
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with a file.
      */
     @Test
     public void file() {
         Assert.assertEquals(ImmutableMap.of("key", "value"), unit().convert(
             "file:src/test/resources/present.properties", LITERAL));
     }
 
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with a classpath resource.
      */
     @Test
     public void classpath() {
         Assert.assertEquals(ImmutableMap.of("key", "value"), unit().convert("classpath:present.properties", LITERAL));
     }
 
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with an http url.
      */
     @Test
     public void http() {
         final Properties actual = Properties.class.cast(unit().convert(
             "http://rocoto.googlecode.com/svn/tags/2.0/configuration/src/test/resources/log4j.properties", LITERAL));
         Assert.assertEquals("ERROR", actual.getProperty("log4j.logger.java"));
     }
 
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with a missing file.
      */
     @Test(expected = RuntimeException.class)
     public void fileMissing() {
         unit().convert("file:src/test/resources/missing.properties", LITERAL);
     }
 
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with a missing classpath resource.
      */
     @Test(expected = RuntimeException.class)
     public void classpathMissing() {
         unit().convert("classpath:missing.properties", LITERAL);
     }
 
     /**
      * Tests {@link PropertiesConverter#convert(String, TypeLiteral)} with a missing http url.
      */
     @Test(expected = RuntimeException.class)
     public void httpMissing() {
        unit().convert("http://example.com/missing.properties", LITERAL);
     }
     
     /**
      * Static injectee class used by {@link PropertiesConverterTest#map()}.
      *
      * @since 2.7
      * @author Willi Schoenborn
      */
     static final class Injectee {
 
         @Inject
         public Injectee(@Named("file") Map<String, String> map) {
             Assert.assertTrue("value".equals(map.get("key")));
         }
         
     }
     
     /**
      * Tests {@link PropertiesConverter} bindings for {@link Map} injection.
      */
     @Test
     public void map() {
         final Injector injector = Guice.createInjector(
             new TypeConverterModule(),
             new Module() {
                 
                 @Override
                 public void configure(Binder binder) {
                     binder.bind(String.class).annotatedWith(Names.named("file")).
                         toInstance("file:src/test/resources/present.properties");
                     
                 }
             }
         );
         injector.getInstance(Injectee.class);
     }
 
 }
