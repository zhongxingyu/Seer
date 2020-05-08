 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jboss.aerogear.android.authentication.impl;
 
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.CALLS_REAL_METHODS;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 
 import org.jboss.aerogear.MainActivity;
 import org.jboss.aerogear.android.Callback;
 import org.jboss.aerogear.android.authentication.AbstractAuthenticationModule;
 import org.jboss.aerogear.android.authentication.AuthenticationModule;
 import org.jboss.aerogear.android.authentication.AuthorizationFields;
 import org.jboss.aerogear.android.http.HttpProvider;
 import org.jboss.aerogear.android.impl.core.HttpProviderFactory;
 import org.jboss.aerogear.android.impl.helper.Data;
 import org.jboss.aerogear.android.impl.helper.UnitTestUtils;
 import org.jboss.aerogear.android.impl.pipeline.PipeConfig;
 import org.jboss.aerogear.android.impl.pipeline.RestAdapter;
 import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 public class GeneralAuthenticationModuleTest extends ActivityInstrumentationTestCase2<MainActivity> implements AuthenticationModuleTest {
 
     public GeneralAuthenticationModuleTest() {
         super(MainActivity.class);
     }
 
     private static final URL SIMPLE_URL;
 
     static {
         try {
             SIMPLE_URL = new URL("http://localhost:8080/todo-server");
         } catch (MalformedURLException ex) {
             throw new IllegalStateException(ex);
         }
     }
 
     public void testApplySecurityTokenOnURL() throws Exception {
 
         final CountDownLatch latch = new CountDownLatch(1);
 
         HttpProviderFactory factory = mock(HttpProviderFactory.class);
         when(factory.get(anyObject())).thenReturn(mock(HttpProvider.class));
 
         AuthorizationFields authFields = new AuthorizationFields();
         authFields.addQueryParameter("token", TOKEN);
 
         AuthenticationModule urlModule = mock(AuthenticationModule.class);
         when(urlModule.isLoggedIn()).thenReturn(true);
         when(urlModule.getAuthorizationFields()).thenReturn(authFields);
 
         PipeConfig config = new PipeConfig(SIMPLE_URL, Data.class);
         config.setAuthModule(urlModule);
 
         RestAdapter<Data> adapter = new RestAdapter<Data>(Data.class, SIMPLE_URL, config);
         Object restRunner = UnitTestUtils.getPrivateField(adapter, "restRunner");
         UnitTestUtils.setPrivateField(restRunner, "httpProviderFactory", factory);
 
         adapter.read(new Callback<List<Data>>() {
 
             @Override
             public void onSuccess(List<Data> data) {
                 latch.countDown();
             }
 
             @Override
             public void onFailure(Exception e) {
                 latch.countDown();
             }
         });
 
         latch.await(1, TimeUnit.SECONDS);
         ArgumentCaptor urlArg = ArgumentCaptor.forClass(Object.class);
 
        verify(factory).get(urlArg.capture(), Mockito.eq(Integer.MAX_VALUE));
         if (urlArg.getValue() instanceof URL) {
             Assert.assertEquals(SIMPLE_URL.toString() + "?token=" + TOKEN, urlArg.getValue().toString());
         } else if (urlArg.getValue() instanceof Object[]) {
             Assert.assertEquals(SIMPLE_URL.toString() + "?token=" + TOKEN, ((Object[]) urlArg.getValue())[0].toString());
         } else {
             fail("Unknown parameter type");
         }
     }
 
     public void testAbstractMethodsThrowExceptions() throws InterruptedException {
         AuthenticationModule module = mock(AbstractAuthenticationModule.class, CALLS_REAL_METHODS);
         final CountDownLatch latch = new CountDownLatch(3);
         Callback throwIfSuccess = new Callback() {
 
             @Override
             public void onSuccess(Object data) {
                 Assert.assertTrue("This should not be called", false);
             }
 
             @Override
             public void onFailure(Exception e) {
                 latch.countDown();
             }
         };
         module.enroll(new HashMap<String, String>(), throwIfSuccess);
         module.login("username", "password", throwIfSuccess);
         module.logout(throwIfSuccess);
 
         latch.await();
 
     }
 
 }
