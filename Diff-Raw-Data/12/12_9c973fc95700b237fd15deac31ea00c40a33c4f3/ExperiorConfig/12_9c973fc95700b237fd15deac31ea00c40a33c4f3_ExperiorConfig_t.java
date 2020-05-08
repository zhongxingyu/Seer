 /*******************************************************************************
 * Copyright 2012 Ivan Shubin http://mindengine.net
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
 package net.mindengine.oculus.experior;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Properties;
 
 import net.mindengine.oculus.experior.reporter.ReportConfiguration;
 import net.mindengine.oculus.experior.test.TestRunnerConfiguration;
 import net.mindengine.oculus.experior.test.resolvers.actions.ActionResolver;
 import net.mindengine.oculus.experior.test.resolvers.cleanup.CleanupResolver;
 import net.mindengine.oculus.experior.test.resolvers.dataprovider.DataDependencyResolver;
 import net.mindengine.oculus.experior.test.resolvers.dataprovider.DataProviderResolver;
 import net.mindengine.oculus.experior.test.resolvers.errors.ErrorResolver;
 import net.mindengine.oculus.experior.test.resolvers.parameters.ParameterResolver;
 import net.mindengine.oculus.experior.test.resolvers.rollbacks.RollbackResolver;
 import net.mindengine.oculus.experior.test.resolvers.test.TestResolver;
 import net.mindengine.oculus.experior.utils.PropertyUtils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ExperiorConfig {
 
     public static final String SUITE_LISTENER = "suite.listener";
     public static final String OCULUS_URL = "oculus.url";
     public static final String COMPONENT_PROVIDER = "component.provider";
     public static final String TESTRUNNER_RESOLVERS_DATADEPENDENCY = "testrunner.resolvers.datadependency";
     public static final String TESTRUNNER_RESOLVERS_DATAPROVIDER = "testrunner.resolvers.dataprovider";
     public static final String TESTRUNNER_RESOLVERS_PARAMETERS = "testrunner.resolvers.parameters";
     public static final String TESTRUNNER_RESOLVERS_CLEANUP = "testrunner.resolvers.cleanup";
     public static final String TESTRUNNER_RESOLVERS_ACTIONS = "testrunner.resolvers.actions";
     public static final String TESTRUNNER_RESOLVERS_ROLLBACKS = "testrunner.resolvers.rollbacks";
     public static final String TESTRUNNER_RESOLVERS_ERRORS = "testrunner.resolvers.errors";
     public static final String TESTRUNNER_RESOLVERS_TEST = "testrunner.resolvers.test";
     public static final String TESTRUNNER_DUMMY_TEST = "testrunner.dummy.test";
     public static final String TESTRUNNER_SUPPOERTEDANNOTATIONS_FIELDS = "testrunner.supportedAnnotations.fields";
     public static final String TESTRUNNER_SUPPORTEDANNOTATIONS_EVENTS = "testrunner.supportedAnnotations.events";
     public static final String OCULUS_API_AUTH_TOKEN = "oculus.api.auth.token";
     
     private static ExperiorConfig _instance = null;
 
     private Properties properties;
 
     private TestRunnerConfiguration testRunnerConfiguration = null;
     private ReportConfiguration reportConfiguration;
     
     private Log logger = LogFactory.getLog(getClass());
     private ExperiorConfig() throws Exception {
         properties = new Properties();
         
         File file = new File("experior.properties");
         if ( !file.exists() ) {
             file = null;
            URL resource = getClass().getClassLoader().getResource("experior.properties");
             if ( resource != null ) {
                 file = new File(resource.toURI());
             }
         }
 
         if ( file != null && file.exists() ) {
             logger.info("Loading properties from " + file.getAbsolutePath());
             FileInputStream fis = new FileInputStream(file);
             properties.load(fis);
             fis.close();
         }
         PropertyUtils.overridePropertiesWithSystemProperties(properties);
     }
     
 
     
 
     public String get(String name) {
         String str = properties.getProperty(name);
         if(str!=null) {
             return str.trim();
         }
         return null;
     }
     
     public ReportConfiguration getReportConfiguration() {
         if(reportConfiguration==null) {
             reportConfiguration = ReportConfiguration.loadFromProperties(properties);
         }
         return reportConfiguration;
     }
     
     /**
      * Returns the value of the specified property from property file and throws {@link IllegalArgumentException} in case if the property is not found.
      * @param name
      * @return
      */
     public String getMandatoryField(String name) {
         String value = get(name);
         if(value == null) throw new IllegalArgumentException("Cannot find mandatory property: "+name);
         return value;
     }
     
     public TestRunnerConfiguration getTestRunnerConfiguration() {
         if(testRunnerConfiguration==null) {
             testRunnerConfiguration = new TestRunnerConfiguration();
             
             try {
                 testRunnerConfiguration.setCleanupResolver((CleanupResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_CLEANUP)));
                 testRunnerConfiguration.setDataDependencyResolver((DataDependencyResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_DATADEPENDENCY)));
                 testRunnerConfiguration.setDataProviderResolver((DataProviderResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_DATAPROVIDER)));
                 testRunnerConfiguration.setParameterResolver((ParameterResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_PARAMETERS)));
                 testRunnerConfiguration.setActionResolver((ActionResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_ACTIONS)));
                 testRunnerConfiguration.setRollbackResolver((RollbackResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_ROLLBACKS)));
                 testRunnerConfiguration.setErrorResolver((ErrorResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_ERRORS)));
                 testRunnerConfiguration.setTestResolver((TestResolver) createObject(getMandatoryField(TESTRUNNER_RESOLVERS_TEST)));
                 testRunnerConfiguration.setDummyTestClass(Class.forName(getMandatoryField(TESTRUNNER_DUMMY_TEST)));
                 
                 //Reading fields annotations
                 String[] fieldNames = getMandatoryField(TESTRUNNER_SUPPOERTEDANNOTATIONS_FIELDS).split(",");
                 Collection<Class<?>>supportedFields = new LinkedList<Class<?>>();
                 for(String fieldName : fieldNames) {
                     supportedFields.add(Class.forName(fieldName.trim()));
                 }
                 testRunnerConfiguration.setSupportedFieldAnnotations(supportedFields);
                 
                 String[] eventNames = getMandatoryField(TESTRUNNER_SUPPORTEDANNOTATIONS_EVENTS).split(",");
                 Collection<Class<?>>supportedEvents = new LinkedList<Class<?>>();
                 for(String eventName : eventNames) {
                     supportedEvents.add(Class.forName(eventName.trim()));
                 }
                 testRunnerConfiguration.setSupportedEventAnnotations(supportedEvents);
             }
             catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return testRunnerConfiguration;
     }
     
     
     
     private Object createObject(String className) throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
         Class<?> clazz = Class.forName(className.trim());
         return clazz.getConstructor().newInstance();
     }
 
     public static synchronized ExperiorConfig getInstance() {
         if (_instance == null) {
             try {
                 _instance = new ExperiorConfig();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
         return _instance;
     }
 }
