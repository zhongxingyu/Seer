 package com.cloudbees.gasp.service;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.ResultReceiver;
 import android.test.ServiceTestCase;
 
 import com.cloudbees.gasp.fragment.TwitterAuthenticationFragment;
 import com.cloudbees.gasp.fragment.TwitterResponderFragment;
 import com.cloudbees.gasp.model.TwitterStatuses;
 import com.cloudbees.gasp.model.TwitterTokenResponse;
import com.cloudbees.gasp.twitter.TwitterAPI;
import com.cloudbees.gasp.twitter.TwitterAuthentication;
 import com.google.gson.Gson;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Copyright (c) 2013 Mark Prichard, CloudBees
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
  */
 
 public class TwitterServiceTest extends ServiceTestCase<RESTIntentService> {
     private static final String TAG = TwitterServiceTest.class.getName();
 
     private CountDownLatch signal;
     private TwitterAuthenticationFragment twitterAuthenticationFragment = new TwitterAuthenticationFragment();
     private TwitterResponderFragment twitterResponderFragment = new TwitterResponderFragment();
 
     public TwitterServiceTest() {
         super(RESTIntentService.class);
     }
 
     protected void setUp() {
         signal = new CountDownLatch(1);
     }
 
     public void testTwitterAPI() throws InterruptedException {
         Intent intent = new Intent(getContext(), RESTIntentService.class);
        intent.setData(Uri.parse(TwitterAuthentication.getTwitterApiOAuthToken()));
 
         try {
             // This test equivalent to TwitterAuthenticationFragment service call
             Bundle params = new Bundle();
             params.putString("grant_type", "client_credentials");
 
             Bundle headers = new Bundle();
            headers.putString("Authorization", TwitterAuthentication.getEncodedBase64Credentials());
 
             intent.putExtra(RESTIntentService.EXTRA_HTTP_VERB, RESTIntentService.POST);
             intent.putExtra(RESTIntentService.EXTRA_HEADERS, headers);
             intent.putExtra(RESTIntentService.EXTRA_PARAMS, params);
             intent.putExtra(RESTIntentService.EXTRA_RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                 @Override
                 protected void onReceiveResult(int resultCode, Bundle resultData) {
                     if (resultData != null && resultData.containsKey(RESTIntentService.REST_RESULT)) {
                         assert (true);
                         assertFalse(resultData.getString(RESTIntentService.REST_RESULT).isEmpty());
                         // Test TwitterTokenType model class
                         TwitterTokenResponse twitterToken =
                                 new Gson().fromJson(resultData.getString(RESTIntentService.REST_RESULT),
                                         TwitterTokenResponse.class);
                         assertTrue(twitterToken.getToken_type().matches("bearer"));
                         assertFalse(twitterToken.getAccess_token().isEmpty());
                     } else {
                         assert (false);
                     }
                 }
             });
 
             startService(intent);
 
             // Allow 20 secs for the async REST call to complete
             signal.await(20, TimeUnit.SECONDS);
 
             // This test equivalent to TwitterResponderFragment service call
            intent.setData(Uri.parse(TwitterAPI.getTwitterApiSearch()));
 
             params = new Bundle();
             params.putString("q", "cloudbees");
             params.putString("count", "10");
 
             headers = new Bundle();
            headers.putString("Authorization", "Bearer " + TwitterAuthentication.getEncodedBase64Credentials());
 
             intent.putExtra(RESTIntentService.EXTRA_PARAMS, params);
             intent.putExtra(RESTIntentService.EXTRA_HEADERS, headers);
             intent.putExtra(RESTIntentService.EXTRA_RESULT_RECEIVER, new ResultReceiver(new Handler()) {
                 @Override
                 protected void onReceiveResult(int resultCode, Bundle resultData) {
                     if (resultData != null && resultData.containsKey(RESTIntentService.REST_RESULT)) {
                         assert (true);
                         assertFalse(resultData.getString(RESTIntentService.REST_RESULT).isEmpty());
                         // Test TwitterStatuses model classes
                         TwitterStatuses twitterStatuses =
                                 new Gson().fromJson(resultData.getString(RESTIntentService.REST_RESULT),
                                         TwitterStatuses.class);
                         assertNotNull(twitterStatuses);
                         assertTrue(twitterStatuses.getStatuses().length == 10);
                     } else {
                         assert (false);
                     }
                 }
             });
 
             startService(intent);
             // Allow 20 secs for the async REST call to complete
             signal.await(20, TimeUnit.SECONDS);
 
         } catch (Exception e) {
             fail();
         }
     }
 
     protected void tearDown() {
         signal.countDown();
     }
 }
