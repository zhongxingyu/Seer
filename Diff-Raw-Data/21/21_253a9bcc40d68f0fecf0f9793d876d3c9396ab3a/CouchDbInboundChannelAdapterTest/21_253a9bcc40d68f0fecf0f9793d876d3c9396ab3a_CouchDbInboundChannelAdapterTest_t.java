 /*
  * Copyright 2011 the original author or authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opencredo.couchdb.inbound;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.opencredo.couchdb.CouchDbIntegrationTest;
 import org.opencredo.couchdb.DummyDocument;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSource;
 import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.endpoint.AbstractEndpoint;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.net.URI;
 
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author Tareq Abedrabbo
  * @since 31/01/2011
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class CouchDbInboundChannelAdapterTest extends CouchDbIntegrationTest {
 
     @Autowired
    @Qualifier("inboundChannelAdapter")
    private AbstractEndpoint inboundChannelAdapter;

    @Autowired
     private MessagingTemplate messagingTemplate;
 
 
     @Test
     public void receiveChanges() throws Exception {
 
        // start the poller here to avoid polling CouchDB before it's ready
        inboundChannelAdapter.start();

         DummyDocument document = new DummyDocument("hello");
         putDocument(document);
 
         Message<Object> message = messagingTemplate.receive();
         assertThat(message, notNullValue());
         Object payload = message.getPayload();
         assertThat(payload, instanceOf(URI.class));
     }
 }
