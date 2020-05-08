 /*
    Copyright 2011 Kalixia, SARL.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package com.kalixia.tapestry.rio.services;
 
 import org.apache.tapestry5.ioc.MappedConfiguration;
 import org.apache.tapestry5.ioc.OrderedConfiguration;
 import org.apache.tapestry5.services.InjectionProvider;
 import java.util.concurrent.TimeUnit;
 
 public class RioModule {
 
    public void contributeFactoryDefaults(MappedConfiguration<String, String> configuration) {
        configuration.add(RioConstants.DISCOVERY_GROUPS, "all");
        configuration.add(RioConstants.DISCOVERY_TIMEOUT, "30");                    // default time out for discovery is 30...
        configuration.add(RioConstants.DISCOVERY_UNIT, TimeUnit.SECONDS.name());    // ... seconds
     }
 
     public static void contributeInjectionProvider(OrderedConfiguration<InjectionProvider> configuration) {
         configuration.addInstance("ServiceAssociation", ServiceAssociationInjectionProvider.class);
     }
 
 }
