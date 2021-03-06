 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.itests.exam;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.openengsb.core.api.Constants;
 import org.openengsb.core.api.model.RuleConfiguration;
 import org.openengsb.core.api.persistence.ConfigPersistenceService;
 import org.openengsb.core.api.persistence.PersistenceManager;
 import org.openengsb.core.api.persistence.PersistenceService;
 import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 @RunWith(JUnit4TestRunner.class)
 // This one will run each test in it's own container (slower speed)
 // @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
 public class PersistenceIT extends AbstractPreConfiguredExamTestHelper {
 
     private PersistenceService persistence;
     private PersistenceTestObject element;
     private PersistenceTestObject wildcard;
 
     @Before
     public void setUp() throws Exception {
         PersistenceManager manager = getOsgiService(PersistenceManager.class);
         persistence = manager.getPersistenceForBundle(getBundleContext().getBundle());
 
         element = new PersistenceTestObject("42", 42);
         persistence.create(element);
 
         wildcard = new PersistenceTestObject(null, null);
     }
 
     @After
     public void tearDown() throws Exception {
         if (persistence.query(wildcard).isEmpty()) {
             return;
         }
         persistence.delete(wildcard);
     }
 
     @Test
     public void testCreateAndQuery() throws Exception {
         PersistenceTestObject test = new PersistenceTestObject("test", 1);
         persistence.create(test);
         List<PersistenceTestObject> result = persistence.query(new PersistenceTestObject("test", null));
         assertThat(result.size(), is(1));
         assertThat(result.get(0), is(test));
     }
 
     @Test
     public void testConfigPersistenceSetup() throws Exception {
         ConfigPersistenceService configPersistenceService = retrieveAndConfigureRuleCorePersistenceService();
 
         assertThat(configPersistenceService, notNullValue());
         assertTrue(configPersistenceService.supports(RuleConfiguration.class));
     }
 
     @Test
     public void testConfigSimplePersistence() throws Exception {
         ConfigPersistenceService configPersistenceService = retrieveAndConfigureRuleCorePersistenceService();
 
         HashMap<String, String> meta = new HashMap<String, String>();
         meta.put("test1", "test1");
         meta.put("test2", "test2");
         String rule = "rule";
         RuleConfiguration ruleConfiguration = new RuleConfiguration(meta, rule);
         configPersistenceService.persist(ruleConfiguration);
         HashMap<String, String> found = new HashMap<String, String>();
         found.put("test1", "test1");
         found.put("test2", "test2");
         List<RuleConfiguration> result = configPersistenceService.load(meta);
 
         assertThat(result, notNullValue());
         assertThat(result.size(), is(1));
         assertThat(result.get(0).getContent(), is("rule"));
     }
 
     private ConfigPersistenceService retrieveAndConfigureRuleCorePersistenceService() throws IOException {
         return getOsgiService(ConfigPersistenceService.class,
             String.format("(%s=%s)", Constants.CONFIGURATION_ID, RuleConfiguration.TYPE_ID), 30000L);
 
     }
 
     @Test
     public void testUpdateAndQuery() throws Exception {
         element.setString("foo");
 
         persistence.update(persistence.query(wildcard).get(0), element);
 
         List<PersistenceTestObject> result = persistence.query(wildcard);
         assertThat(result.size(), is(1));
         assertThat(result.get(0).getString(), is("foo"));
     }
 
     @Test
     public void testDelete() throws Exception {
         persistence.delete(element);
         List<PersistenceTestObject> result = persistence.query(wildcard);
         assertThat(result.isEmpty(), is(true));
     }
 
 }
