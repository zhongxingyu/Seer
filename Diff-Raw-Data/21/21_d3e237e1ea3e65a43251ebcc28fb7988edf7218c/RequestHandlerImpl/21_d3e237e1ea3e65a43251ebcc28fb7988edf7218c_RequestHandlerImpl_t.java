 /**
  * Licensed to the Austrian Association for Software Tool Integration (AASTI)
  * under one or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information regarding copyright
  * ownership. The AASTI licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.core.services.internal;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
 
 import org.openengsb.core.api.Constants;
 import org.openengsb.core.api.remote.MethodCall;
 import org.openengsb.core.api.remote.MethodReturn;
 import org.openengsb.core.api.remote.MethodReturn.ReturnType;
 import org.openengsb.core.api.remote.RequestHandler;
 import org.openengsb.core.common.OpenEngSBCoreServices;
 import org.osgi.framework.Filter;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.InvalidSyntaxException;
 
 public class RequestHandlerImpl implements RequestHandler {
 
     @Override
     public MethodReturn handleCall(MethodCall call) {
         Object service = retrieveOpenEngSBService(call);
         Object[] args = call.getArgs();
        Method method = findMethod(service, call.getMethodName(), getArgTypes(call));
         return invokeMethod(service, method, args);
     }
 
     private Object retrieveOpenEngSBService(MethodCall call) {
         String serviceId = retrieveServiceId(call);
         Filter filter = createFilterForServiceId(serviceId);
         return OpenEngSBCoreServices.getServiceUtilsService().getService(filter);
     }
 
     private String retrieveServiceId(MethodCall call) {
         String serviceId = call.getMetaData().get("serviceId");
         if (serviceId == null) {
             throw new IllegalArgumentException("missing definition of serviceid in methodcall");
         }
         return serviceId;
     }
 
     private Filter createFilterForServiceId(String serviceId) {
         try {
             return FrameworkUtil.createFilter(String.format("(%s=%s)", Constants.ID_KEY, serviceId));
         } catch (InvalidSyntaxException e) {
             throw new IllegalArgumentException(e);
         }
     }
 
     private MethodReturn invokeMethod(Object service, Method method, Object[] args) {
         MethodReturn resultContainer = new MethodReturn();
         try {
             Object result = method.invoke(service, args);
             if (method.getReturnType().getName().equals("void")) {
                 resultContainer.setType(ReturnType.Void);
             } else {
                 resultContainer.setType(ReturnType.Object);
                 resultContainer.setArg(result);
             }
         } catch (InvocationTargetException e) {
             resultContainer.setType(ReturnType.Exception);
             resultContainer.setArg(e.getCause());
         } catch (IllegalAccessException e) {
             resultContainer.setType(ReturnType.Exception);
             resultContainer.setArg(e);
         }
         return resultContainer;
     }
 
     private Method findMethod(Object service, String methodName, Class<?>[] argTypes) {
         Method method;
         try {
             method = service.getClass().getMethod(methodName, argTypes);
         } catch (NoSuchMethodException e) {
             throw new IllegalArgumentException(e);
         }
         return method;
     }
 
    private Class<?>[] getArgTypes(MethodCall args) {
        List<Class<?>> clazzes = new ArrayList<Class<?>>();
        for (String clazz : args.getClasses()) {
            try {
                clazzes.add(Thread.currentThread().getContextClassLoader().loadClass(clazz));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("The classes defined could not be found", e);
            }
         }
        return clazzes.toArray(new Class<?>[0]);
     }
 
 }
