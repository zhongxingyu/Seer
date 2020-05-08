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
 
 package org.wso2.carbon.cep.core.internal.config;
 
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wso2.carbon.cep.core.Expression;
 import org.wso2.carbon.cep.core.Query;
 import org.wso2.carbon.cep.core.internal.config.output.OutputHelper;
 import org.wso2.carbon.cep.core.internal.util.CEPConstants;
 
 import javax.xml.namespace.QName;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * This class will help to build Query object from a given OMElement
  */
 public class QueryHelper {
     private static final Log log = LogFactory.getLog(QueryHelper.class);
 
 
     public static Query fromOM(OMElement queryElement) {
         Query query = new Query();
         String name = queryElement.getAttribute(new QName(CEPConstants.CEP_CONF_ATTR_NAME)).getAttributeValue();
         query.setName(name);
 
         Iterator iterator = queryElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_QUERY_IP));
 
        while (iterator != null && iterator.hasNext()) {
             OMElement ipElement = (OMElement) iterator.next();
             String ip = ipElement.getText();
             query.addIP(ip);
 
         }
 
         OMElement expressionElement = queryElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                 CEPConstants.CEP_CONF_ELE_EXPRESSION));
         if (expressionElement != null) {
             query.setExpression(ExpressionHelper.fromOM(expressionElement));
         }
 
 
         OMElement outputOmElement = queryElement.getFirstChildWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                 CEPConstants.CEP_CONF_ELE_OUTPUT));
         if (expressionElement != null && outputOmElement != null) {
             query.setOutput(OutputHelper.fromOM(outputOmElement));
         }
         return query;
     }
 
 
     public static OMElement queryToOM(Query query) {
         OMFactory factory = OMAbstractFactory.getOMFactory();
         OMElement queryChild = factory.createOMElement(new QName(
                 CEPConstants.CEP_CONF_NAMESPACE,
                 CEPConstants.CEP_CONF_ELE_QUERY,
                 CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
         Expression queryExpression = query.getExpression();
         String queryName = query.getName();
         OMElement queryIP = null;
         List<String> ipList = query.getIpList();
         for (String ip : ipList) {
             queryIP = factory.createOMElement(new QName(CEPConstants.CEP_CONF_QUERY_IP));
             queryIP.setText(ip);
             queryChild.addChild(queryIP);
         }
         queryChild.addAttribute(CEPConstants.CEP_CONF_ATTR_NAME, queryName, null);
         OMElement omQueryExpression = ExpressionHelper
                 .expressionToOM(queryExpression);
         queryChild.addChild(omQueryExpression);
         if (query.getOutput() != null) {
             OMElement queryOutput = OutputHelper.outputToOM(query.getOutput());
             queryChild.addChild(queryOutput);
         }
         return queryChild;
     }
 
 
 }
