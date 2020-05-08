 /*
  * Copyright 2005-2006 The Apache Software Foundation.
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
 package org.apache.servicemix.tck;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 
 import junit.framework.Assert;
 
 import org.apache.servicemix.jbi.event.ExchangeEvent;
 import org.apache.servicemix.jbi.event.ExchangeListener;
 
 public class ExchangeCompletedListener extends Assert implements ExchangeListener {
 
     private Map exchanges = new HashMap();
 
     private long timeout;
 
     public ExchangeCompletedListener() {
         this(1000);
     }
 
     public ExchangeCompletedListener(long timeout) {
         this.timeout = timeout;
     }
 
     public void exchangeSent(ExchangeEvent event) {
         synchronized (exchanges) {
             exchanges.put(event.getExchange().getExchangeId(), event.getExchange());
             exchanges.notify();
         }
     }
 
     public void assertExchangeCompleted() throws Exception {
         long start = System.currentTimeMillis();
         MessageExchange active = null;
         while (true) {
             synchronized (exchanges) {
                 for (Iterator it = exchanges.values().iterator(); it.hasNext();) {
                     active = null;
                     MessageExchange me = (MessageExchange) it.next();
                     if (me.getStatus() == ExchangeStatus.ACTIVE) {
                         active = me;
                         break;
                     }
                 }
                 if (active == null) {
                     break;
                 }
                 long remain = timeout - (System.currentTimeMillis() - start);
                 if (remain <= 0) {
                     assertTrue("Exchange is ACTIVE: " + active, active.getStatus() != ExchangeStatus.ACTIVE);
                 } else {
                     exchanges.wait(remain);
                 }
             }
         }
     }
 
 }
