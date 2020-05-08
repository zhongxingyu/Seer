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
 package org.apache.servicemix.components.cache;
 
 import javax.xml.namespace.QName;
 import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
 
 import org.apache.servicemix.jbi.resolver.EndpointResolver;
 import org.apache.servicemix.tck.TestSupport;
 import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Node;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 
 /**
  * @version $Revision$
  */
 public class CacheTest extends TestSupport {
     public void testCache() throws Exception {
         EndpointResolver cachedService = client.createResolverForService(new QName("http://servicemix.org/cheese/", "myService"));
         EndpointResolver service = client.createResolverForService(new QName("http://servicemix.org/cheese/", "myServiceImpl"));
 
         Object object = client.request(cachedService, null, null, "<foo id='123'/>");
        if (object instanceof Node) {
            object = new DOMSource((Node) object);
        }
         String text = transformer.toString((Source) object);
 
         System.out.println("Cache: Received response: " + text);
 
         object = client.request(cachedService, null, null, "<foo id='123'/>");
        if (object instanceof Node) {
            object = new DOMSource((Node) object);
        }
         String text2 = transformer.toString((Source) object);
 
         System.out.println("Cache: Received response: " + text2);
 
         assertEquals("Responses should be equal", text, text2);
 
 
         // now lets try the underlying service to check we get different results each time
         object = client.request(service, null, null, "<foo id='123'/>");
        if (object instanceof Node) {
            object = new DOMSource((Node) object);
        }
         text = transformer.toString((Source) object);
 
         System.out.println("ServiceImpl: Received response: " + text);
 
         object = client.request(service, null, null, "<foo id='123'/>");
        if (object instanceof Node) {
            object = new DOMSource((Node) object);
        }
         text2 = transformer.toString((Source) object);
 
         System.out.println("ServiceImpl: Received response: " + text2);
 
         assertTrue("Responses should be different but were both: " + text, !text.equals(text2));
 
     }
 
     protected AbstractXmlApplicationContext createBeanFactory() {
         return new ClassPathXmlApplicationContext("org/apache/servicemix/components/cache/example.xml");
     }
 }
