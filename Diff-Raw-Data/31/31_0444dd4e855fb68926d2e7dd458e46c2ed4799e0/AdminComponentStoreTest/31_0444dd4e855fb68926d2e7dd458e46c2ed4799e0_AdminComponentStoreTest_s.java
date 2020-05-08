 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.mediators.helpers;
 
 import org.sciflex.plugins.synapse.esper.mediators.listener.SuccessAwareSynapseListener;
 import org.sciflex.plugins.synapse.esper.mediators.editors.StaticEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.*;
 import org.sciflex.plugins.synapse.esper.mediators.axiom.ConfigurationAwareAxiomMediator;
 import org.sciflex.plugins.synapse.esper.mediators.xml.ConfigurationAwareXMLMediator;
 
 
 import org.apache.axiom.om.ds.CharArrayDataSource;
 import org.apache.axiom.om.OMFactory;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMAbstractFactory;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.synapse.config.Entry;
 import org.apache.synapse.registry.Registry;
 import org.apache.synapse.MessageContext;
 import org.apache.axis2.engine.AxisConfiguration;
 
 import org.wso2.esb.registry.ESBRegistry;
 
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 import com.espertech.esper.client.EPStatement;
 
 
 import java.util.Properties;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
/**
 * Created by IntelliJ IDEA.
 * User: harsha
 * Date: Nov 25, 2008
 * Time: 8:24:50 PM
 * To change this template use File | Settings | File Templates.
 */
 public class AdminComponentStoreTest extends TestCase {
 
 
        private static final String REQ = "<m0:getQuote xmlns:m0=\"http://services.samples/xsd\"><m0:request><m0:symbol>IBM</m0:symbol></m0:request></m0:getQuote>";
        private String epl;
        private String syn_listener = SuccessAwareSynapseListener.class.getName();
        protected OMFactory ombuilderFactory;
        private OMElement omele= null;
        private OMElement omConfig= null;
        private String instanceURI_1 ="http://localhost:9999/soap/StaticEPLEditorEventListener";
        private String instanceURI_2 = "http://localhost:9999/soap/DynamicEPLEditorEventListener";
        private Entry prop;
       /**
         * Associated EPServiceProvider instance.
        * @see com.espertech.esper.client.EPServiceProvider
        */
        private EPServiceProvider provider_1;
        private EPServiceProvider provider_2;
         /**
          * Log associated with the Static EPL Editor.
          */
         private static final Log log = LogFactory.getLog(StaticEPLEditor.class);
 
         /**
          * The {@link org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper} whom the Static EPL Editor will assit.
          */
         private EPLStatementHelper ownerOfXMLMediator;
         private EPLStatementHelper ownerOfAxiomMediator;
 
        /**
        *  xml meditor instance
        */
         private XMLMediator xmlmediator;
 
        /**
         * Axiom Mediator instance
         */
         private AxiomMediator axiomMediator;
 
         /**
         * Associated Synapse Listener.
         * @see org.sciflex.plugins.synapse.esper.mediators.SynapseListener
         */
         SynapseListener listener;
         /**
         *  Registry instance
         */
         private Registry reg;
         /**
          * Unique Identifier of this object.
          */
         private String uid = null;
 
         /**
          * Unique Identifier of this object's parent.
          */
         private String parentUID = null;
 
 
         public void createOMElement() {
 
             ombuilderFactory = OMAbstractFactory.getOMFactory();
             // String for setting eplStatement
             String payload1  = " <epl-statement key=\"statement/statement_xml.xml\"/>";
 
             omele = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "epl-statement", null);
 
         }
 
 
        public void createOMConfig() {
 
           ombuilderFactory = OMAbstractFactory.getOMFactory();
 
 
 
          //  String for setting esper configuration
 
          String payload1  = " <esper-configuration>\n" +
                       " <event-type alias=\"XMLEvent\">\n" +
                       " <xml-dom root-element-name=\"getQuote\"\n" +
                       " default-namespace=\"http://services.samples/xsd\">\n" +
                       " <namespace-prefix prefix=\"m0\" namespace=\"http://services.samples/xsd\"/>\n" +
                       " <xpath-property property-name=\"symbol\"\n" +
                       " xpath=\"//m0:getQuote/m0:request/m0:symbol\" type=\"string\"/>\n" +
                       " </xml-dom>\n" +
                       " </event-type>\n" +
                       " </esper-configuration>";
 
          omConfig = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "esper-configuration", null);
 
        }
 
 
     public void setUp() throws Exception {
               listener = new SynapseListenerImpl();
               String registryKey = null;
               String eplStatement = null;
               EPStatement epStatement = null;
               epl = "select symbol from XMLEvent";
               reg = new ESBRegistry();
 
               //configure registry
               Properties props = new Properties();
               props.put("root", "file:../examples/conf/resources/registry/");
               props.put("cachableDuration", "1500");
               reg.init(props);
               prop = new Entry();
               prop.setType(Entry.REMOTE_ENTRY);
 
               xmlmediator = new ConfigurationAwareXMLMediator();
               createOMElement();
 
               xmlmediator.setConfiguration(omele);
               xmlmediator.setInstanceURI(instanceURI_1);
               xmlmediator.setListener(syn_listener);
               xmlmediator.setStatement(epl);
               xmlmediator.setEventToAddress(instanceURI_1);
 
 
     }
 
     
     public void testMediateMethod() throws Exception {
 
              AdminComponentStore adminComponentStore = new AdminComponentStore(xmlmediator);
             MessageContext mc = TestUtils.getTestContext(REQ);
              adminComponentStore.invoke(mc);
              AxisConfiguration conf = mc.getConfiguration().getAxisConfiguration();
              xmlmediator.mediate(mc);
             if (conf == null){
                System.out.println("FIX ME: make sure having axis configuration from mc properly.");
             }else{
                   Map returnedMap = (Map) conf.getParameter(AdminComponentStoreConstants.COMPONENT_ROOT_MAP_NAME);
                   assertTrue("test is failed", returnedMap instanceof HashMap); 
             }
 
     }
 
 
 
 }
