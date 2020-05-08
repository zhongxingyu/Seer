 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.sns.twitter;
 
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import twitter4j.StatusUpdate;
 import twitter4j.Twitter;
 
 /**
  * @author jun.ozeki
  */
 public class Twitter4jTest {
 
     @Mock private Twitter mock;
     
     @Before
     public void before() {
         MockitoAnnotations.initMocks(this);
     }
 
     @Test
     public void Topicのつぶやき() throws Exception {
         // setup
         String tweet = "つぶやき";
         TweetTopic topic = new TweetTopic(tweet);
        when(mock.updateStatus(tweet)).thenReturn(null);
         Twitter4j target = new Twitter4j(mock);
         // exercise
         target.post(topic);
         // verify
         verify(mock).updateStatus(new StatusUpdate(tweet));
     }
     
     @Test
     public void Topicメッセージ分割() throws Exception {
         // setup
         String tweet = "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと"
                      + "あいうえお\nかきくけこ\nさしすせそ\nたちつてと";
         TweetTopic topic = new TweetTopic(tweet);
//        topic.
        when(mock.updateStatus(tweet)).thenReturn(null);
         Twitter4j target = new Twitter4j(mock);
         // exercise
         target.post(topic);
         // verify
         verify(mock).updateStatus(new StatusUpdate(tweet));
     }
 }
