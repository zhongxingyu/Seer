 /*
  * Copyright 2014 Sorcersoft.com S.A.
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
 
 package sorcer.core;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sorcer.config.BeanListener;
 import sorcer.config.ServiceBeanListener;
 
 import java.util.Set;
 
 /**
  * @author Rafał Krupiński
  */
 public class PlatformModule extends AbstractModule {
     private static final Logger log = LoggerFactory.getLogger(PlatformModule.class);
 
     @Override
     protected void configure() {
         /*
         * Before creating a ServiceProvider or a subclass, make sure ServiceBeanListener is available
         */
         bind(ServiceBeanListener.class).toProvider(new Provider<ServiceBeanListener>() {
             @Inject
             Set<BeanListener> beanListeners;
 
             @Override
             public ServiceBeanListener get() {
                 ServiceBeanListener instance = (ServiceBeanListener) ServiceBeanListener.getBeanListener();
                if (instance != null) {
                    log.warn("ServiceBeanListener already created");
                    return instance;
                }
                 log.info("Creating ServiceBeanListener");
                 instance = new ServiceBeanListener(beanListeners);
                 ServiceBeanListener.setBeanListener(instance);
                 return instance;
             }
         }).asEagerSingleton();
     }
 }
