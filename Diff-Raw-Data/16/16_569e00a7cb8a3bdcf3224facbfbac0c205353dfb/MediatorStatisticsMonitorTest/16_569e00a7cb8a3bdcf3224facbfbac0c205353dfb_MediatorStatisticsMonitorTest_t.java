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
 
 package org.sciflex.plugins.synapse.esper.mediators.monitors;
 
 import org.sciflex.plugins.synapse.esper.mediators.listener.SuccessAwareSynapseListener;
 import org.sciflex.plugins.synapse.esper.mediators.editors.StaticEPLEditor;
 import org.sciflex.plugins.synapse.esper.mediators.*;
 import org.sciflex.plugins.synapse.esper.mediators.helpers.EPLStatementHelper;
 
 import org.sciflex.plugins.synapse.esper.mediators.axiom.ConfigurationAwareAxiomMediator;
 import org.sciflex.plugins.synapse.esper.mediators.xml.ConfigurationAwareXMLMediator;
  import org.sciflex.plugins.synapse.esper.core.admin.AdminComponentServiceConstants;
 
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
 
 public class MediatorStatisticsMonitorTest extends TestCase {
 
 
        private static final String REQ = "<m0:getQuote xmlns:m0=\"http://services.samples/xsd\"><m0:request><m0:symbol>IBM</m0:symbol></m0:request></m0:getQuote>";
        private String epl;
        private String syn_listener = SuccessAwareSynapseListener.class.getName();
        protected OMFactory ombuilderFactory;
        private OMElement omele= null;
        private OMElement omConfig_1= null;
        private OMElement omConfig_2= null;
        private String instanceURI_1 ="http://localhost:9999/soap/StaticEPLEditorEventListener";
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
 
         /**
          * Message Context in Use.
          */
         private MessageContext mc = null;
 
 
         public void createOMElement() {
 
             ombuilderFactory = OMAbstractFactory.getOMFactory();
             // String for setting eplStatement
             String payload1  = " <epl-statement key=\"statement/statement_xml.xml\"/>";
 
             omele = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "epl-statement", null);
 
         }
 
 
        public void createOMConfigForXMLMediator() {
 
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
 
          omConfig_1 = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "esper-configuration", null);
 
        }
 
     /**
      * create an OMElement having esper configuration.
      */
 
     public void createOMConfigForAxiomMediator() {
 
     ombuilderFactory = OMAbstractFactory.getOMFactory();
 
 
 
          //  String for setting esper configuration
 
          String payload1  = " <esper-configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                  " xmlns=\"http://www.espertech.com/schema/esper\"\n" +
                  " xsi:noNamespaceSchemaLocation=\"esper-configuration-2-0.xsd\">\n" +
                  " <plugin-event-representation uri=\"types://xml/apacheaxiom/OMNode\"\n" +
                  "  class-name=\"com.espertech.esperio.representation.axiom.AxiomEventRepresentation\"/>\n" +
                  " <plugin-event-type alias=\"AxiomEvent\">\n" +
                  " <resolution-uri value=\"types://xml/apacheaxiom/OMNode/AxiomEvent\"/>\n" +
                  " <initializer>\n" +
                  " <xml-axiom root-element-name=\"getQuote\"\n" +
                  " default-namespace=\"http://services.samples/xsd\">\n" +
                  " <namespace-prefix prefix=\"m0\"\n" +
                  " namespace=\"http://services.samples/xsd\"/>\n" +
                  " <xpath-property property-name=\"symbol\"\n" +
                  " xpath=\"//m0:getQuote/m0:request/m0:symbol\" type=\"string\"/>\n" +
                  " </xml-axiom>\n" +
                  " </initializer>\n" +
                  " </plugin-event-type>\n" +
                  " </esper-configuration>";
 
          omConfig_2 = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "esper-configuration", null);
 
     }
 
     /**
      * initialize xmlMediator instance and SynapseMessageContext along with a registry.
      * @throws Exception
      */
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
              //obtain an instance of ConfigurationAwareXMLMediator
              xmlmediator = new ConfigurationAwareXMLMediator();
 
              //obtain an instance of ConfigurationAwareAxiomMediator
              axiomMediator = new ConfigurationAwareAxiomMediator();
 
              //createEsper Configuration for xml mediator
              createOMConfigForXMLMediator();
 
              xmlmediator.setConfiguration(omConfig_1);
              xmlmediator.setInstanceURI(instanceURI_1);
              xmlmediator.setListener(syn_listener);
              xmlmediator.setStatement(epl);
              xmlmediator.setEventToAddress(instanceURI_1);
 
 
              //createEsper Configuration for axiom mediator
              createOMConfigForAxiomMediator();
 
              axiomMediator.setConfiguration(omConfig_2);
              axiomMediator.setInstanceURI(instanceURI_1);
              axiomMediator.setListener(syn_listener);
              axiomMediator.setStatement(epl);
              axiomMediator.setEventToAddress(instanceURI_1);
 
              mc = TestUtils.getTestContext(REQ);
              mc.getConfiguration().setRegistry(reg);
              mc.getConfiguration().setAxisConfiguration(new AxisConfiguration());
 
 
     }
 
     /**
      * tests Statistic Monitor wrt required parameters
      * @throws Exception
      */
     public void testStatisticMonitorForXMLMediator() throws Exception {
              double avgRequestTime = 0;
              double avgResponseTime = 0;
              double loadAsPercentage = 0.0;
              double maxRequestTime = 0;
              double maxResponseTime = 0;
              double minRequestTime = 0;
              double minResponseTime = 0;
 
              xmlmediator.init(mc.getEnvironment());
              xmlmediator.mediate(mc);
 
              AxisConfiguration conf = StaticAxisConfiguration.getAxisConfiguration();
              conf = mc.getConfiguration().getAxisConfiguration();
              assertTrue("Configuration is not available", conf != null);
 
              Object returnedMap = conf.getParameterValue(AdminComponentServiceConstants.COMPONENT_ROOT_MAP_NAME);
              assertTrue("No Admin Components were found", returnedMap != null);
              assertTrue("Invalid return type", returnedMap instanceof HashMap);
 
              Object statisticMonitorMap = ((HashMap)returnedMap).get(AdminComponentServiceConstants.MEDIATOR_STATISTICS_MONITOR_MAP_NAME);
              assertTrue("No Admin Components were found", statisticMonitorMap != null);
              assertTrue("Invalid return type", statisticMonitorMap instanceof HashMap);
 
              Object[] uniquelyIdentifiableSet = ((HashMap)statisticMonitorMap).values().toArray();
              assertTrue("No uniquely identifialble set returned", uniquelyIdentifiableSet != null);
 
              MediatorStatisticsMonitor statisticMonitor = (MediatorStatisticsMonitor)uniquelyIdentifiableSet[0];
              avgRequestTime = statisticMonitor.getAvgRequestTime();
              assertTrue("Average request time is null", avgRequestTime != 0);
 
            /*  avgResponseTime = statisticMonitor.getAvgResponseTime();
              assertTrue("Average response time is null", avgResponseTime != 0);
                                                                                */
             /* loadAsPercentage = statisticMonitor.getLoadAsPercentage();
              assertTrue("No load has passed the mediator", loadAsPercentage != 0.0);*/
              
 
              maxRequestTime = statisticMonitor.getMaxRequestTime();
              assertTrue("Max request time is null", maxRequestTime != 0);
 
              maxResponseTime = statisticMonitor.getMaxResponseTime();
              assertTrue("Max response time is null", maxResponseTime != 0);
 
 
              minRequestTime = statisticMonitor.getMinRequestTime();
              assertTrue("Min request time is null",  minRequestTime != 0);
 
              minResponseTime = statisticMonitor.getMinResponseTime();
              assertTrue("min response time is null", minResponseTime != 0);
 
              xmlmediator.destroy();
 
     }
 
     /**
      * tests Statistic Monitor wrt required parameters
      * @throws Exception
      */
     public void testStatisticMonitorForAxiomMediator() throws Exception {
              double avgRequestTime = 0;
              double avgResponseTime = 0;
              double loadAsPercentage = 0.0;
              double maxRequestTime = 0;
              double maxResponseTime = 0;
              double minRequestTime = 0;
              double minResponseTime = 0;
 
              axiomMediator.init(mc.getEnvironment());
              axiomMediator.mediate(mc);
 
              AxisConfiguration conf = StaticAxisConfiguration.getAxisConfiguration();
              conf = mc.getConfiguration().getAxisConfiguration();
              assertTrue("Configuration is not available", conf != null);
 
              Object returnedMap = conf.getParameterValue(AdminComponentServiceConstants.COMPONENT_ROOT_MAP_NAME);
              assertTrue("No Admin Components were found", returnedMap != null);
              assertTrue("Invalid return type", returnedMap instanceof HashMap);
 
              Object statisticMonitorMap = ((HashMap)returnedMap).get(AdminComponentServiceConstants.MEDIATOR_STATISTICS_MONITOR_MAP_NAME);
              assertTrue("No Admin Components were found", statisticMonitorMap != null);
              assertTrue("Invalid return type", statisticMonitorMap instanceof HashMap);
 
              Object[] uniquelyIdentifiableSet = ((HashMap)statisticMonitorMap).values().toArray();
              assertTrue("No uniquely identifialble set returned", uniquelyIdentifiableSet != null);
 
             MediatorStatisticsMonitor statisticMonitor = (MediatorStatisticsMonitor)uniquelyIdentifiableSet[1];
              avgRequestTime = statisticMonitor.getAvgRequestTime();
              assertTrue("Average request time is null", avgRequestTime != 0);
 
              /*avgResponseTime = statisticMonitor.getAvgResponseTime();
              assertTrue("Average response time is null", avgResponseTime != 0);*/
 
             /* loadAsPercentage = statisticMonitor.getLoadAsPercentage();
              assertTrue("No load has passed the mediator", loadAsPercentage != 0.0);*/
 
 
              maxRequestTime = statisticMonitor.getMaxRequestTime();
              assertTrue("Max request time is null", maxRequestTime != 0);
 
              maxResponseTime = statisticMonitor.getMaxResponseTime();
              assertTrue("Max response time is null", maxResponseTime != 0);
 
 
              minRequestTime = statisticMonitor.getMinRequestTime();
              assertTrue("Min request time is null",  minRequestTime != 0);
 
              minResponseTime = statisticMonitor.getMinResponseTime();
              assertTrue("min response time is null", minResponseTime != 0);
 
              axiomMediator.destroy();
 
     }
 
 
 }
