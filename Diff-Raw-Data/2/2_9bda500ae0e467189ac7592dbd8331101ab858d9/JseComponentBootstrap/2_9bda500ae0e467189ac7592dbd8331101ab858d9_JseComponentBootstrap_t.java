 /**
  * Copyright 2010-2011 apius.org
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * 
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
 
package org.apius.server;
 
 import org.restlet.Component;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 
 /**
  * <p>
  * If this application is run outside of a J2EE web container as a JSE application
  * then we need to point the application to our JSE-specific bean factory resource 
  * and we need to start the component manually.
  * </p>
  * 
  * @author Paul Morris
  * 
  */
 public class JseComponentBootstrap {
     
     /**
      * Takes two string parameters:
      * 1) Classpath to the bean factory.
      * 2) Name (ref) of the Restlet Component as indicated in the bean factory XML file.
      * 
      * @param path
      * @param name
      * 
      */
     public static void main(String[] args) throws Exception {
         
         String resourceClassPath = args[0];
         String componentBean = args[1];
         
         ClassPathResource resource = new ClassPathResource(resourceClassPath);
         BeanFactory beanFactory = new XmlBeanFactory(resource);
         
         Component component = (Component) beanFactory.getBean(componentBean);
         component.start();
     }
     
 }
  
