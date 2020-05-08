 /*
  * Copyright 2008-2010 Xebia and the original author or authors.
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
 package fr.xebia.springframework.jms;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Queue;
 import javax.jms.Topic;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.jms.UncategorizedJmsException;
 import org.springframework.jms.listener.DefaultMessageListenerContainer;
 import org.springframework.jmx.export.naming.SelfNaming;
 
 /**
  * JMX enabled {@link DefaultMessageListenerContainer}.
  * 
  * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
  */
 public class ManagedDefaultMessageListenerContainer extends DefaultMessageListenerContainer implements
         ManagedDefaultMessageListenerContainerMBean, BeanNameAware, SelfNaming {
 
     private String beanName;
 
     private ObjectName objectName;
 
     public ObjectName getObjectName() throws MalformedObjectNameException {
         if (objectName == null) {
             String destinationName = getDestinationName();
             Destination destination = getDestination();
             if (destinationName == null && destination != null) {
                 try {
                     if (destination instanceof Queue) {
                         Queue queue = (Queue) destination;
                         destinationName = queue.getQueueName();
 
                     } else if (destination instanceof Topic) {
                         Topic topic = (Topic) destination;
                         destinationName = topic.getTopicName();
                     }
                 } catch (JMSException e) {
                     throw new UncategorizedJmsException(e);
                 }
             }
             objectName = ObjectName.getInstance("javax.jms:type=MessageListenerContainer,name=" + ObjectName.quote(beanName)
                     + ",destination=" + destinationName);
         }
         return objectName;
     }
 
     @Override
     public void setBeanName(String name) {
         this.beanName = name;
        super.setBeanName(beanName);
     }
 
 }
