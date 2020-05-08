 /*
  * Copyright 2013 Apache Software Foundation.
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
 package org.apacheextras.camel.component.rcode;
 
 import org.apache.camel.test.junit4.CamelTestSupport;
 import org.junit.Before;
 import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.rosuda.REngine.Rserve.RConnection;
 import org.rosuda.REngine.Rserve.RserveException;
 
 /**
  *
  * @author cemmersb
  */
 public class RCodeProducerTest extends CamelTestSupport {
   
   final String user = "test";
   final String password = "test123";
   
   RConnection rConnection = mock(RConnection.class, RETURNS_DEEP_STUBS);
 
   @Before
   @Override
   public void setUp() throws Exception {
     // We supply a fake factory to mock the RConnection Instance.
    RConnectionFactory.SingletonHolder.instance = new RConnectionFactory() {
       @Override
       public RConnection createConnection(RCodeConfiguration rCodeConfiguration) throws RserveException {
         return rConnection;
       }
     };
 
     when(rConnection.needLogin()).thenReturn(Boolean.TRUE);
     doAnswer(new Answer<Void>() {
       @Override
       public Void answer(InvocationOnMock invocation) {
         return null;
       }
     }).when(rConnection).login(user, password);
     super.setUp();
   }
 }
