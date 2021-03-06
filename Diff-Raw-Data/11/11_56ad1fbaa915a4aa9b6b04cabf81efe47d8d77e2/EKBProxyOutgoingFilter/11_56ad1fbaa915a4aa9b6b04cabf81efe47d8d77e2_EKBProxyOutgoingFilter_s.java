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
 
 package org.openengsb.core.common.remote;
 
 import java.util.Map;
 
 import org.openengsb.core.api.model.OpenEngSBModel;
 import org.openengsb.core.api.model.OpenEngSBModelWrapper;
 import org.openengsb.core.api.remote.FilterAction;
 import org.openengsb.core.api.remote.FilterConfigurationException;
 import org.openengsb.core.api.remote.FilterException;
 import org.openengsb.core.api.remote.MethodCallRequest;
 import org.openengsb.core.api.remote.MethodResultMessage;
 import org.openengsb.core.common.util.ModelUtils;
 
 /**
  * This filter takes a {@link MethodCallRequest} and checks if any parameter is type of OpenEngSBModel. If so, it
  * converts it to the corresponding OpenEngSBModelWrapper. The new object is then passed on to the next filter. The
  * returned {@link MethodResultMessage} is checked for OpenEngSBModelWrapper. If this is the case, it is converted to a
  * OpenEngSBModelObject again.
  * 
  * <code>
  * <pre>
  *      [MethodCallRequest]   > Filter > [MethodCallRequest]     > ...
  *                                                                  |
  *                                                                  v
  *      [MethodResultMessage] < Filter < [MethodResultMessage]   < ...
  * </pre>
  * </code>
  */
 public class EKBProxyOutgoingFilter extends
         AbstractFilterChainElement<MethodCallRequest, MethodResultMessage> {
 
     private FilterAction next;
 
     public EKBProxyOutgoingFilter() {
         super(MethodCallRequest.class, MethodResultMessage.class);
     }
 
     @Override
     public MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metadata) throws FilterException {
         Object[] parameters = input.getMethodCall().getArgs();
         if (parameters != null) {
             for (int i = 0; i < parameters.length; i++) {
                 if (OpenEngSBModel.class.isAssignableFrom(parameters[i].getClass())) {
                     OpenEngSBModel model = (OpenEngSBModel) parameters[i];
                     OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(model);
                     parameters[i] = wrapper;
                 }
             }
             input.getMethodCall().setArgs(parameters);
         }
 
         MethodResultMessage message = (MethodResultMessage) next.filter(input, metadata);
 
        if (message.getResult().getArg().getClass().equals(OpenEngSBModelWrapper.class)) {
             OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) message.getResult().getArg();
             Object modelObject = ModelUtils.generateModelOutOfWrapper(wrapper);
             message.getResult().setArg(modelObject);
         }
         return message;
     }
 
     @Override
     public void setNext(FilterAction next) throws FilterConfigurationException {
         checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
         this.next = next;
     }
 }
