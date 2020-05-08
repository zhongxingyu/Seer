 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc;
 
 import com.google.protobuf.Descriptors;
 import com.google.protobuf.Message;
 import java.util.List;
 import java.util.logging.Logger;
import no.rmz.blobee.protobuf.MethodTypeException;
import no.rmz.blobee.protobuf.TypeExctractor;
 import no.rmz.blobee.serviceimpls.SampleServerImpl;
 import no.rmz.blobeetestproto.api.proto.Testservice;
 import org.junit.Test;
 
 
 public final class TypeExtractorTest {
 
     private static final Logger log = Logger.getLogger(
             no.rmz.blobee.rpc.TypeExtractorTest.class.getName());
 
     @Test(timeout=10000)
     public void determineMethodTypesTest() throws MethodTypeException {
 
         final com.google.protobuf.Service service =  new SampleServerImpl();
 
         final Descriptors.ServiceDescriptor descriptor = service.getDescriptorForType();
         final List<Descriptors.MethodDescriptor> methods = descriptor.getMethods();
 
         // Since we know that SampleServerImpl has only one method.
         org.junit.Assert.assertEquals(1, methods.size());
 
         Descriptors.MethodDescriptor md = methods.get(0);
 
         final Message inputType = TypeExctractor.getReqestPrototype(service, md);
         org.junit.Assert.assertNotNull(inputType);
         final Message outputType =  TypeExctractor.getResponsePrototype(service, md);
         org.junit.Assert.assertNotNull(outputType);
 
         final String fullName = md.getFullName();
 
        org.junit.Assert.assertEquals("no.rmz.blobeetestproto.api.proto.RpcService.Invoke", fullName);
         org.junit.Assert.assertTrue(inputType instanceof Testservice.RpcParam);
         org.junit.Assert.assertTrue(outputType instanceof Testservice.RpcResult);
     }
 }
