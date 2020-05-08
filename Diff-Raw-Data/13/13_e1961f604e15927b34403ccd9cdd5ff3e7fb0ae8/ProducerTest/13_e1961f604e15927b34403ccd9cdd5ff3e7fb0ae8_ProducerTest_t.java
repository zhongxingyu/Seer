 /**
  * Copyright 2012 Tejeswar Das
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package net.dovemq.api;
 
 public class ProducerTest
 {
     public static void main(String[] args) throws InterruptedException
     {
         ConnectionFactory.initialize("producer");
        String brokerIP = args[0];
 
        Session session = ConnectionFactory.createSession(brokerIP);
         System.out.println("created session");
 
         Producer producer = session.createProducer("firstQueue");
         System.out.println("created producer");
 
         DoveMQMessage message = MessageFactory.createMessage();
         String payload = "Hello World";
         message.addPayload(payload.getBytes());
         producer.sendMessage(message);
         System.out.println("sent message");
         Thread.sleep(10000);
 
         //session.close();
         ConnectionFactory.shutdown();
     }
 }
