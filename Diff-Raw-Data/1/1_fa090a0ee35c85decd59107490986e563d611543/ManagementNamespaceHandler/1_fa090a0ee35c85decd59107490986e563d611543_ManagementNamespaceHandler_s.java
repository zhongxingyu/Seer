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
 package fr.xebia.management.config;
 
 import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
 
 /**
  * {@link org.springframework.beans.factory.xml.NamespaceHandler} for the '
  * <code>management</code>' namespace.
  */
 public class ManagementNamespaceHandler extends NamespaceHandlerSupport {
 
     public void init() {
         registerBeanDefinitionParser("servlet-context-aware-mbean-server", new ServletContextAwareMBeanServerDefinitionParser());
         registerBeanDefinitionParser("profile-aspect", new ProfileAspectDefinitionParser());
         registerBeanDefinitionParser("application-version-mbean", new WebApplicationMavenInformationDefinitionParser());
         registerBeanDefinitionParser("jms-connection-factory-wrapper", new SpringManagedConnectionFactoryDefinitionParser());
         registerBeanDefinitionParser("eh-cache-management-service", new EhCacheManagementServiceDefinitionParser());
 
     }
 
 }
