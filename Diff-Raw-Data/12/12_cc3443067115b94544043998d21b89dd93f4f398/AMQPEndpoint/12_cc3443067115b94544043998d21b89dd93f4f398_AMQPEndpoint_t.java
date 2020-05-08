 /*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 package org.apache.axis2.transport.amqp;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.description.AxisService;
 import org.apache.axis2.description.Parameter;
 import org.apache.axis2.description.ParameterInclude;
 import org.apache.axis2.transport.base.ParamUtils;
 import org.apache.axis2.transport.base.ProtocolEndpoint;
 import org.apache.axis2.transport.base.threads.WorkerPool;
 import org.apache.axis2.transport.amqp.ctype.ContentTypeRuleSet;
 import org.apache.axis2.addressing.EndpointReference;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 
 /**
  * Class that links an Axis2 service to a AMQP destination. Additionally, it contains
  * all the required information to process incoming AMQP messages and to inject them
  * into Axis2.
  */
 public class AMQPEndpoint extends ProtocolEndpoint {
     private static final Log log = LogFactory.getLog(AMQPEndpoint.class);
     
     private final AMQPListener listener;
     private final WorkerPool workerPool;
     
     private AMQPConnectionFactory cf;
     private String destinationName;
     private int destinationType = AMQPConstants.GENERIC;
     private String replyDestinationName;
     private String replyDestinationType = AMQPConstants.DESTINATION_TYPE_GENERIC;
     private Set<EndpointReference> endpointReferences = new HashSet<EndpointReference>();
     private ContentTypeRuleSet contentTypeRuleSet;
     private ServiceTaskManager serviceTaskManager;
 
     public AMQPEndpoint(AMQPListener listener, WorkerPool workerPool) {
         this.listener = listener;
         this.workerPool = workerPool;
     }
 
     public String getJndiDestinationName() {
         return destinationName;
     }
 
     private void setDestinationType(String destinationType) {
         if (AMQPConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
             this.destinationType = AMQPConstants.TOPIC;
         } else if (AMQPConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
             this.destinationType = AMQPConstants.QUEUE;
         } else {
             this.destinationType = AMQPConstants.GENERIC;
         }
     }
 
     private void setReplyDestinationType(String destinationType) {
         if (AMQPConstants.DESTINATION_TYPE_TOPIC.equalsIgnoreCase(destinationType)) {
             this.replyDestinationType = AMQPConstants.DESTINATION_TYPE_TOPIC;
         } else if (AMQPConstants.DESTINATION_TYPE_QUEUE.equalsIgnoreCase(destinationType)) {
             this.replyDestinationType = AMQPConstants.DESTINATION_TYPE_QUEUE;
         } else {
             this.replyDestinationType = AMQPConstants.DESTINATION_TYPE_GENERIC;
         }
     }
 
     public String getJndiReplyDestinationName() {
         return replyDestinationName;
     }
 
     public String getReplyDestinationType() {
         return replyDestinationType;
     }
 
     @Override
     public EndpointReference[] getEndpointReferences(String ip) {
         return endpointReferences.toArray(new EndpointReference[endpointReferences.size()]);
     }
 
     private void computeEPRs() {
         List<EndpointReference> eprs = new ArrayList<EndpointReference>();
         for (Object o : getService().getParameters()) {
             Parameter p = (Parameter) o;
             if (AMQPConstants.PARAM_PUBLISH_EPR.equals(p.getName()) && p.getValue() instanceof String) {
                 if ("legacy".equalsIgnoreCase((String) p.getValue())) {
                     // if "legacy" specified, compute and replace it
                     endpointReferences.add(new EndpointReference(getEPR()));
                 } else {
                     endpointReferences.add(new EndpointReference((String) p.getValue()));
                 }
             }
         }
 
         if (eprs.isEmpty()) {
             // if nothing specified, compute and return legacy EPR
             endpointReferences.add(new EndpointReference(getEPR()));
         }
     }
 
     /**
      * Get the EPR for the given AMQP connection factory and destination
      * the form of the URL is
      * amqp:/<destination>?[<key>=<value>&]*
      *
      * @return the EPR as a String
      */
     private String getEPR() {
         StringBuffer sb = new StringBuffer();
 
         sb.append(
             AMQPConstants.AMQP_PREFIX).append(destinationName);
         sb.append("?").append(AMQPConstants.PARAM_DEST_TYPE).append("=").append(
             destinationType == AMQPConstants.TOPIC ?
                 AMQPConstants.DESTINATION_TYPE_TOPIC : AMQPConstants.DESTINATION_TYPE_QUEUE);
 
         if (contentTypeRuleSet != null) {
             String contentTypeProperty = contentTypeRuleSet.getDefaultContentTypeProperty();
             if (contentTypeProperty != null) {
                 sb.append("&");
                 sb.append(AMQPConstants.CONTENT_TYPE_PROPERTY_PARAM);
                 sb.append("=");
                 sb.append(contentTypeProperty);
             }
         }
 
 		for (Map.Entry<String, String> entry : cf.getParameters().entrySet()) {
 			sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
         }
         return sb.toString();
     }
 
     public ContentTypeRuleSet getContentTypeRuleSet() {
         return contentTypeRuleSet;
     }
 
     public AMQPConnectionFactory getCf() {
         return cf;
     }
 
     public ServiceTaskManager getServiceTaskManager() {
         return serviceTaskManager;
     }
 
     public void setServiceTaskManager(ServiceTaskManager serviceTaskManager) {
         this.serviceTaskManager = serviceTaskManager;
     }
 
     @Override
     public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
         // We only support endpoints configured at service level
         if (!(params instanceof AxisService)) {
             return false;
         }
         
         AxisService service = (AxisService)params;
         
         cf = listener.getConnectionFactory(service);
         if (cf == null) {
             return false;
         }
 
         Parameter destParam = service.getParameter(AMQPConstants.PARAM_DESTINATION);
         if (destParam != null) {
             destinationName = (String)destParam.getValue();
         } else {
             // Assume that the JNDI destination name is the same as the service name
             destinationName = service.getName();
         }
         
         Parameter destTypeParam = service.getParameter(AMQPConstants.PARAM_DEST_TYPE);
         if (destTypeParam != null) {
             String paramValue = (String) destTypeParam.getValue();
             if (AMQPConstants.DESTINATION_TYPE_QUEUE.equals(paramValue) ||
                     AMQPConstants.DESTINATION_TYPE_TOPIC.equals(paramValue) )  {
                 setDestinationType(paramValue);
             } else {
                 throw new AxisFault("Invalid destinaton type value " + paramValue);
             }
         } else {
             log.debug("AMQP destination type not given. default queue");
             destinationType = AMQPConstants.QUEUE;
         }
 
         Parameter replyDestTypeParam = service.getParameter(AMQPConstants.PARAM_REPLY_DEST_TYPE);
         if (replyDestTypeParam != null) {
             String paramValue = (String) replyDestTypeParam.getValue();
             if (AMQPConstants.DESTINATION_TYPE_QUEUE.equals(paramValue) ||
                     AMQPConstants.DESTINATION_TYPE_TOPIC.equals(paramValue) )  {
                 setReplyDestinationType(paramValue);
             } else {
                 throw new AxisFault("Invalid destinaton type value " + paramValue);
             }
         } else {
             log.debug("AMQP reply destination type not given. default queue");
             destinationType = AMQPConstants.QUEUE;
         }
         
         replyDestinationName = ParamUtils.getOptionalParam(service,AMQPConstants.PARAM_REPLY_DESTINATION);
         
         computeEPRs(); // compute service EPR and keep for later use        
         
         serviceTaskManager = ServiceTaskManagerFactory.createTaskManagerForService(cf, service, workerPool);
         serviceTaskManager.setJmsMessageReceiver(new AMQPMessageReceiver(listener, cf, this));
         
         return true;
     }
 }
