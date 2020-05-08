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
 package org.apache.servicemix.jbi.servicedesc;
 
 import org.apache.servicemix.jbi.framework.ComponentNameSpace;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 
 import javax.xml.namespace.QName;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import junit.framework.TestCase;
 
 public class InternalEndpointTest extends TestCase {
 
     public void testSerializeDeserialize() throws Exception {
         ComponentNameSpace cns = new ComponentNameSpace("myContainer", "myName", "myId");
         InternalEndpoint e = new InternalEndpoint(cns, "myEndpoint", new QName("myService"));
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(e);
         oos.close();
         
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bais);
         Object out = ois.readObject();
         
         assertNotNull(out);
         assertTrue(out instanceof InternalEndpoint);
         InternalEndpoint outE = (InternalEndpoint) out; 
         assertNotNull(outE.getComponentNameSpace());
         assertNotNull(outE.getServiceName());
         assertNotNull(outE.getEndpointName());
     }
    
    public void testEquals() throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace("myContainer", "myName", "myId");
        InternalEndpoint e1 = new InternalEndpoint(cns, "myEndpoint1", new QName("myService"));
        InternalEndpoint e2 = new InternalEndpoint(cns, "myEndpoint2", new QName("myService"));
        assertFalse(e1.equals(e2));
        e2 = new InternalEndpoint(cns, "myEndpoint", new QName("myService2"));
        assertFalse(e1.equals(e2));
        ComponentNameSpace cns2 = new ComponentNameSpace("myContainer", "myName", "myId2");
        e2 = new InternalEndpoint(cns2, "myEndpoint1", new QName("myService"));
        assertFalse(e1.equals(e2));
        cns2 = new ComponentNameSpace("myContainer", "myName", "myId");
        e2 = new InternalEndpoint(cns2, "myEndpoint1", new QName("myService"));
        assertTrue(e1.equals(e2));
    }
 
 }
