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
 package no.rmz.blobee.rpc.server;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.Descriptors.MethodDescriptor;
 import com.google.protobuf.Descriptors.ServiceDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 public final class ServiceAnnotationMapper {
 
     private static final Logger log =
             Logger.getLogger(ServiceAnnotationMapper.class.getName());
 
     private ServiceAnnotationMapper() {
     }
 
     public static MethodDescriptor getMethodDescriptor(
             final Class serviceInterface,
             final String methodName) throws RpcServerException   {
         try {
             checkNotNull(methodName);
             checkNotNull(serviceInterface);
 
             final String rectifiedMethodName =
                     methodName.substring(0, 1).toUpperCase() + // XXX VERY UGLY
                     methodName.substring(1);
 
             final Method getDescriptor = serviceInterface.getMethod("getDescriptor");
             final ServiceDescriptor serviceDescriptor =
                     (ServiceDescriptor) getDescriptor.invoke(null);


             final MethodDescriptor methodDesc =
                     serviceDescriptor.findMethodByName(rectifiedMethodName);
             final MethodDescriptor unrectifiedMethodDesc =
                    serviceDescriptor.findMethodByName(methodName);

             // XXX This is a stupid heuristic.  It works, but it
             //     should _NOT_ be necessary, and it actually is indicative
             //     of a very stupid bug somewhere.

            return (methodDesc != null)? methodDesc: unrectifiedMethodDesc;
         } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
              throw new RpcServerException(ex);
         }
     }
 }
