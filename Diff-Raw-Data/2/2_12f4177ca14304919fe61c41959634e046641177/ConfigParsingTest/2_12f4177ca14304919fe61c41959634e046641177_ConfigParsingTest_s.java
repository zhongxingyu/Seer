 /*
  * Copyright (c) 2011 Julien Nicoulaud <julien.nicoulaud@gmail.com>
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
 package net.nicoulaj.benchmark.mockwebapp.test.config;
 
 import net.nicoulaj.benchmark.mockwebapp.config.Mapping;
 import net.nicoulaj.benchmark.mockwebapp.config.MockWebAppConfig;
 import net.nicoulaj.benchmark.mockwebapp.config.then.ThenStatement;
 import net.nicoulaj.benchmark.mockwebapp.config.when.WhenStatement;
 import net.nicoulaj.benchmark.mockwebapp.test.AbstractMockWebAppTest;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import static org.testng.Assert.*;
 
 /**
 * Tests for {@link MockWebAppConfig.Parser}.
  *
  * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
  * @since 1.0.0
  */
 public class ConfigParsingTest extends AbstractMockWebAppTest {
 
     /**
      * Assert parsing an invalid config file throws an exception.
      *
      * @param file the invalid config file to use for the test.
      * @throws Exception should always happen.
      */
     @Test(dataProvider = "invalidConfigFilesDataProvider",
           expectedExceptions = Exception.class)
     public void parsingInvalidConfigShouldThrowException(File file) throws Exception {
         MockWebAppConfig.Parser.parseConfig(file);
     }
 
     /**
      * Assert parsing a valid config file does not throw any exception.
      *
      * @param file the valid config file to use for the test.
      * @throws Exception should never happen.
      */
     @Test(dataProvider = "validConfigFilesDataProvider")
     public void parsingValidConfigShouldNotThrowException(File file) throws Exception {
         MockWebAppConfig.Parser.parseConfig(file);
     }
 
     /**
      * Assert parsing a valid config file returns a coherent object.
      *
      * @param file the valid config file to use for the test.
      * @throws Exception should never happen.
      */
     @Test(dataProvider = "validConfigFilesDataProvider")
     public void unmarshallingValidConfigReturnsCoherentObjectsHierarchy(File file) throws Exception {
         final MockWebAppConfig cfg = MockWebAppConfig.Parser.parseConfig(file);
         assertNotNull(cfg, "null config");
 
         assertNotNull(cfg.mappings, "null mappings list");
         assertFalse(cfg.mappings.isEmpty(), "empty mappings list");
 
         for (Object mapping : cfg.mappings) {
             assertNotNull(mapping, "null mapping");
             assertTrue(mapping instanceof Mapping, "mapping with wrong type");
 
             assertNotNull(((Mapping) mapping).whenStatements, "null when statements list");
             assertFalse(((Mapping) mapping).whenStatements.isEmpty(), "empty when statements list");
             for (Object matcher : ((Mapping) mapping).whenStatements) {
                 assertNotNull(matcher, "null when statement");
                 assertTrue(matcher instanceof WhenStatement, "when statement with wrong type");
             }
 
             assertNotNull(((Mapping) mapping).thenStatements, "null then statements list");
             assertFalse(((Mapping) mapping).thenStatements.isEmpty(), "empty then statements list");
             for (Object processor : ((Mapping) mapping).thenStatements) {
                 assertNotNull(processor, "null then statement");
                 assertTrue(processor instanceof ThenStatement, "then statement with wrong type");
             }
         }
     }
 
     /**
      * Assert validating a {@link MockWebAppConfig} without any {@link Mapping} defined throws an error.
      *
      * @throws Throwable should always happen.
      */
     @Test(expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "At least one mapping should be declared")
     public void configWithoutMappingsShouldBeInvalid() throws Throwable {
         new MockWebAppConfig().validate();
     }
 
     /**
      * Assert validating a {@link MockWebAppConfig} with a {@link Mapping}  without any {@link WhenStatement}
      * defined throws an error.
      *
      * @param file the valid config file to use for the test.
      * @throws Throwable should always happen.
      */
     @Test(dataProvider = "validConfigFilesDataProvider",
           expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "\"when\" statements list is empty")
     public void mappingWithoutWhenStatementShouldBeInvalid(File file) throws Throwable {
         final MockWebAppConfig cfg = MockWebAppConfig.Parser.parseConfig(file);
         cfg.mappings.get(0).whenStatements = new ArrayList<WhenStatement>();
         cfg.validate();
     }
 
     /**
      * Assert validating a {@link MockWebAppConfig} with a {@link Mapping}  without any {@link ThenStatement}
      * defined throws an error.
      *
      * @param file the valid config file to use for the test.
      * @throws Throwable should always happen.
      */
     @Test(dataProvider = "validConfigFilesDataProvider",
           expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "\"then\" statements list is empty")
     public void mappingWithoutThenStatementShouldBeInvalid(File file) throws Throwable {
         final MockWebAppConfig cfg = MockWebAppConfig.Parser.parseConfig(file);
         cfg.mappings.get(0).thenStatements = new ArrayList<ThenStatement>();
         cfg.validate();
     }
 }
