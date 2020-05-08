 ///////////////////////////////////////////////////////////////////////////
 //
 //Copyright 2008 Zenoss Inc 
 //Licensed under the Apache License, Version 2.0 (the "License"); 
 //you may not use this file except in compliance with the License. 
 //You may obtain a copy of the License at 
 //    http://www.apache.org/licenses/LICENSE-2.0 
 //Unless required by applicable law or agreed to in writing, software 
 //distributed under the License is distributed on an "AS IS" BASIS, 
 //WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 //See the License for the specific language governing permissions and 
 //limitations under the License.
 //
 ///////////////////////////////////////////////////////////////////////////
 package com.zenoss.zenpacks.zenjmx;
 
 import java.util.List;
 import java.util.Map;
 
 import com.zenoss.zenpacks.zenjmx.call.Utility;
 
 public class ConfigAdapter {
     public static final String PASSWORD = "password";
     public static final String OBJECT_NAME = "objectName";
     public static final String DEVICE = "device";
     public static final String OPERATION_NAME = "operationName";
     public static final String JMX_PROTOCOL = "jmxProtocol";
     public static final String JMX_PORT = "jmxPort";
     public static final String JMX_RAW_SERVICE = "jmxRawService";
     public static final String AUTHENTICATE = "authenticate";
     public static final String MANAGE_IP = "manageIp";
     public static final String DATASOURCE_ID = "datasourceId";
     public static final String ATTRIBUTE_NAME = "attributeName";
     public static final String OPERATION_PARAM_VALUES = "operationParamValues";
     public static final String OPERATION_PARAM_TYPES = "operationParamTypes";
     public static final String USERNAME = "username";
     public static final String RMI_CONTEXT = "rmiContext";
     public static final String DATA_POINT = "dps";
     public static final String DATA_POINT_TYPES = "dptypes";
     public static final String EVENT_KEY = "eventKey";
     public static final String EVENT_CLASS = "eventClass";
     public static final String COMPONENT_KEY = "component";
     public static final String RRD_PATH = "rrdPath";
 
     
     public static final String DELIMITER = ",";
     private Map configMap;
 
     public ConfigAdapter(Map config) {
         configMap = config;
     }
 
     //
     public String getPassword() {
         return (String) configMap.get("password");
     }
 
     public String getOjectName() {
         return (String) configMap.get("objectName");
     }
 
     public String getDevice() {
         return (String) configMap.get("device");
     }
 
     public String getOperationName() {
         return (String) configMap.get("operationName");
     }
 
     public String getJmxProtocol() {
         return (String) configMap.get("jmxProtocol");
     }
 
     public String getJmxPort() {
        return configMap.get("jmxPort").toString();
     }
 
     public String getJmxRawService() {
         return (String) configMap.get("jmxRawService");
     }
 
     public Boolean authenticate() {
         return (Boolean) configMap.get("authenticate");
     }
 
     public String getManageIp() {
         return (String) configMap.get("manageIp");
     }
 
     public String getDatasourceId() {
         return (String) configMap.get("datasourceId");
     }
 
     public String getAttributeName() {
         return (String) configMap.get("attributeName");
     }
     
     public String getAttributePath() {
         return (String) configMap.get("attributePath");
     }
 
 
     /**
      * returns the parameter values for a jmx invoke operation
      * @return String array of parameter values, empty array if none
      */
     public String[] getOperationParamValues() {
 
         String values = (String) configMap.get("operationParamValues");
         values = values.trim();
         String[] paramValues = new String[0];
 
         if (values.length() != 0) {
             paramValues = values.split(DELIMITER);
         }
 
         for (int i = 0; i < paramValues.length; i++) {
             paramValues[i] = paramValues[i].trim();
         }
 
         return paramValues;
 
     }
 
     /**
      * returns the parameter types for a jmx invoke operation
      * 
      * @return String array of java types; empty array if none
      */
     public String[] getOperationParamTypes() {
         String types = ((String) configMap.get("operationParamTypes")).trim();
 
         String[] paramTypes = new String[0];
         if (types.length() != 0) {
             paramTypes = types.split(DELIMITER);
 
         }
         for (int i = 0; i < paramTypes.length; i++) {
             paramTypes[i] = paramTypes[i].trim();
         }
         return paramTypes;
     }
 
     public String getUsername() {
         return (String) configMap.get("username");
     }
 
     public String getRmiContext() {
         return (String) configMap.get("rmiContext");
     }
 
     public String getEventClass() {
         return (String) configMap.get(EVENT_CLASS);
     }
 
     public String getEventKey() {
         return (String) configMap.get(EVENT_KEY);
     }
     public String getComponent() {
       return (String) configMap.get(COMPONENT_KEY);
     }
     public String getRrdPath() {
         return (String) configMap.get(RRD_PATH);
     }
 
     
     public List<String> getDataPoints() {
         // ugly form of downcasting... but XML-RPC doesn't give us a
         // List<String>
         Object[] dataPoints = (Object[])configMap.get(DATA_POINT);
         List<String> dps = Utility.downcast( dataPoints );
         return dps;
     }
     public List<String> getDataPointTypes() {
         // ugly form of downcasting... but XML-RPC doesn't give us a
         // List<String>
         Object[] dpTypes = (Object[])configMap.get(DATA_POINT_TYPES);
         List<String> types = Utility.downcast( dpTypes );
         return types;
     }
     
     
     // rmiContext=jmxrmi}
     // dps=[Ljava.lang.Object;@9a6087,
     // dptypes=[Ljava.lang.Object;@37504d,
 
 }
