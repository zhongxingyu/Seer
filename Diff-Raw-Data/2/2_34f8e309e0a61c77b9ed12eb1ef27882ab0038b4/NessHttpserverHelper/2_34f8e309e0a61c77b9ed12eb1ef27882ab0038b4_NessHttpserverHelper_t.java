 /**
  * Copyright (C) 2012 Ness Computing, Inc.
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
 package com.nesscomputing.testing;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import com.google.common.collect.Iterables;
 import com.google.inject.Injector;
 
 import com.nesscomputing.httpserver.HttpConnector;
 import com.nesscomputing.httpserver.HttpServer;
 
 /**
  * Provides linking to ness-httpserver component to
  * retrieve the actual host / port used for a tested service.
  * Written as a separate class so that ness-httpserver can be optional
  * in case that this is not used at all.
  */
final class NessHttpserverHelper
 {
 
     private NessHttpserverHelper() { }
 
     static URI getServiceUri(Injector injector)
     {
         HttpServer server = injector.getInstance(HttpServer.class);
         HttpConnector connector = Iterables.getOnlyElement(server.getConnectors().values());
         try {
             return new URI(connector.getScheme(), null, connector.getAddress(), connector.getPort(), null, null, null);
         } catch (URISyntaxException e) {
             throw new IllegalStateException(e);
         }
     }
 }
