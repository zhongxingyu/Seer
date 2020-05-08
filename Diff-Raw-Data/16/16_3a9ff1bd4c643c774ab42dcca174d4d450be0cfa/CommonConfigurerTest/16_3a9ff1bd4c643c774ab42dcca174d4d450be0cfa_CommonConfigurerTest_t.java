 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springextensions.db4o.config;
 
 import java.io.PrintStream;
 
 import com.db4o.config.CommonConfiguration;
 import com.db4o.config.EnvironmentConfiguration;
 import com.db4o.config.NameProvider;
 import com.db4o.config.QueryConfiguration;
 import com.db4o.config.encoding.StringEncoding;
 import com.db4o.diagnostic.DiagnosticConfiguration;
 import com.db4o.reflect.Reflector;
 import org.testng.Assert;
import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 /**
  * author: olli
  */
 public class CommonConfigurerTest {
 
     private CommonConfiguration commonConfiguration;
 
     private CommonConfigurer commonConfigurer;
 
    @BeforeClass
     public void setup() {
         commonConfiguration = mock(CommonConfiguration.class);
         commonConfigurer = new CommonConfigurer(commonConfiguration);
     }
 
     @Test
     public void testGetConfiguration() {
        // TODO
     }
 
     @Test
     public void testAddAlias() {
        // TODO
     }
 
     @Test
     public void testSetActivationDepth() {
         int activationDepth = 123;
         commonConfigurer.setActivationDepth(activationDepth);
         verify(commonConfiguration).activationDepth(activationDepth);
     }
 
     @Test
     public void testAdd() {
        // TODO
     }
 
     @Test
     public void testSetAllowVersionUpdates() {
         boolean allowVersionUpdates = true;
         commonConfigurer.setAllowVersionUpdates(allowVersionUpdates);
         verify(commonConfiguration).allowVersionUpdates(allowVersionUpdates);
     }
 
     @Test
     public void testSetAutomaticShutDown() {
         boolean automaticShutDown = true;
         commonConfigurer.setAutomaticShutDown(automaticShutDown);
         verify(commonConfiguration).automaticShutDown(automaticShutDown);
     }
 
     @Test
     public void testSetBTreeNodeSize() {
         int bTreeNodeSize = 123;
         commonConfigurer.setBTreeNodeSize(bTreeNodeSize);
         verify(commonConfiguration).bTreeNodeSize(bTreeNodeSize);
     }
 
     @Test
     public void testSetCallbacks() {
         boolean callbacks = true;
         commonConfigurer.setCallbacks(callbacks);
         verify(commonConfiguration).callbacks(callbacks);
     }
 
     @Test
     public void testSetCallConstructors() {
         boolean callConstructors = true;
         commonConfigurer.setCallConstructors(callConstructors);
         verify(commonConfiguration).callConstructors(callConstructors);
     }
 
     @Test
     public void testSetDetectSchemaChanges() {
         boolean detectSchemaChanges = true;
         commonConfigurer.setDetectSchemaChanges(detectSchemaChanges);
         verify(commonConfiguration).detectSchemaChanges(detectSchemaChanges);
     }
 
     @Test
     public void testDiagnostic() {
         DiagnosticConfiguration diagnosticConfiguration = mock(DiagnosticConfiguration.class);
         when(commonConfiguration.diagnostic()).thenReturn(diagnosticConfiguration);
         Assert.assertEquals(diagnosticConfiguration, commonConfigurer.diagnostic());
     }
 
     @Test
     public void testSetExceptionsOnNotStorable() {
         boolean exceptionsOnNotStorable = true;
         commonConfigurer.setExceptionsOnNotStorable(exceptionsOnNotStorable);
         verify(commonConfiguration).exceptionsOnNotStorable(exceptionsOnNotStorable);
     }
 
     @Test
     public void testSetInternStrings() {
         boolean internStrings = true;
         commonConfigurer.setInternStrings(internStrings);
         verify(commonConfiguration).internStrings(internStrings);
     }
 
     @Test
     public void testSetMarkTransient() {
         String markTransient = "";
         commonConfigurer.setMarkTransient(markTransient);
         verify(commonConfiguration).markTransient(markTransient);
     }
 
     @Test
     public void testSetMessageLevel() {
         int messageLevel = 123;
         commonConfigurer.setMessageLevel(messageLevel);
         verify(commonConfiguration).messageLevel(messageLevel);
     }
 
     @Test
     public void testSetOptimizeNativeQueries() {
         boolean optimizeNativeQueries = true;
         commonConfigurer.setOptimizeNativeQueries(optimizeNativeQueries);
         verify(commonConfiguration).optimizeNativeQueries(optimizeNativeQueries);
     }
 
     @Test
     public void testQueries() {
         QueryConfiguration queryConfiguration = mock(QueryConfiguration.class);
         when(commonConfiguration.queries()).thenReturn(queryConfiguration);
         Assert.assertEquals(queryConfiguration, commonConfigurer.queries());
     }
 
     @Test
     public void testSetReflectWith() {
         Reflector reflector = mock(Reflector.class);
         commonConfigurer.setReflectWith(reflector);
         verify(commonConfiguration).reflectWith(reflector);
     }
 
     @Test
     public void testSetOutStream() {
         PrintStream outStream = mock(PrintStream.class);
         commonConfigurer.setOutStream(outStream);
         verify(commonConfiguration).outStream(outStream);
     }
 
     @Test
     public void testSetStringEncoding() {
         StringEncoding stringEncoding = mock(StringEncoding.class);
         commonConfigurer.setStringEncoding(stringEncoding);
         verify(commonConfiguration).stringEncoding(stringEncoding);
     }
 
     @Test
     public void testSetTestConstructors() {
         boolean testConstructors = true;
         commonConfigurer.setTestConstructors(testConstructors);
         verify(commonConfiguration).testConstructors(testConstructors);
     }
 
     @Test
     public void testSetUpdateDepth() {
         int updateDepth = 123;
         commonConfigurer.setUpdateDepth(updateDepth);
         verify(commonConfiguration).updateDepth(updateDepth);
     }
 
     @Test
     public void testSetWeakReferences() {
         boolean weakReferences = true;
         commonConfigurer.setWeakReferences(weakReferences);
         verify(commonConfiguration).weakReferences(weakReferences);
     }
 
     @Test
     public void testSetWeakReferenceCollectionInterval() {
         int weakReferenceCollectionInterval = 1234;
         commonConfigurer.setWeakReferenceCollectionInterval(weakReferenceCollectionInterval);
         verify(commonConfiguration).weakReferenceCollectionInterval(weakReferenceCollectionInterval);
     }
 
     @Test
     public void testRegisterTypeHandler() {
        // TODO
     }
 
     @Test
     public void testEnvironment() {
         EnvironmentConfiguration environmentConfiguration = mock(EnvironmentConfiguration.class);
         when(commonConfiguration.environment()).thenReturn(environmentConfiguration);
         Assert.assertEquals(environmentConfiguration, commonConfigurer.environment());
     }
 
     @Test
     public void testSetNameProvider() {
         NameProvider nameProvider = mock(NameProvider.class);
         commonConfigurer.setNameProvider(nameProvider);
         verify(commonConfiguration).nameProvider(nameProvider);
     }
 
 }
