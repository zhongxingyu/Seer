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
 
 public class ConsumerTest
 {
     private static class TestMessageReceiver implements DoveMQMessageReceiver
     {
         @Override
         public void messageReceived(DoveMQMessage message)
         {
             System.out.println("Received message");
         }
     }
     public static void main(String[] args) throws InterruptedException
     {
         ConnectionFactory.initialize("consumer");
        String brokerIP = args[0];
 
        Session session = ConnectionFactory.createSession(brokerIP);
 
         Consumer consumer = session.createConsumer("firstQueue");
         consumer.registerMessageReceiver(new TestMessageReceiver());
         consumer.start();
         System.out.println("waiting for message");
         Thread.sleep(60000);
 
         //session.close();
         ConnectionFactory.shutdown();
     }
 }
