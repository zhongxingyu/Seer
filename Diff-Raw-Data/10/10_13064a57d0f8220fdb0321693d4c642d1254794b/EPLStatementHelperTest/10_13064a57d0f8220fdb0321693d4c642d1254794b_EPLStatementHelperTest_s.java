 package org.sciflex.plugins.synapse.esper.mediators.helpers;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.synapse.SynapseConstants;
 import org.apache.synapse.MessageContext;
 import org.apache.synapse.config.Entry;
 import org.apache.synapse.registry.Registry;
 import org.apache.axiom.om.OMFactory;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMAbstractFactory;
 import org.apache.axiom.om.ds.CharArrayDataSource;
 
 import com.espertech.esper.client.EPStatement;
 import com.espertech.esper.client.EPServiceProvider;
 import com.espertech.esper.client.EPServiceProviderManager;
 
 import org.sciflex.plugins.synapse.esper.mediators.TestUtils;
 import org.sciflex.plugins.synapse.esper.mediators.SynapseListener;
 import org.sciflex.plugins.synapse.esper.mediators.SynapseListenerImpl;
 import org.sciflex.plugins.synapse.esper.mediators.XMLMediator;
 import org.sciflex.plugins.synapse.esper.mediators.listener.SuccessAwareSynapseListener;
 import org.sciflex.plugins.synapse.esper.mediators.xml.ConfigurationAwareXMLMediator;
 
 import org.wso2.esb.registry.ESBRegistry;
 
 import junit.framework.TestCase;
 
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 public class EPLStatementHelperTest extends TestCase {
 
     private static final String REQ = "<m0:getQuote xmlns:m0=\"http://services.samples/xsd\"><m0:request><m0:symbol>IBM</m0:symbol></m0:request></m0:getQuote>";
     private String epl = "select symbol from XMLEvent";
     private String syn_listener = SuccessAwareSynapseListener.class.getName();
     protected OMFactory ombuilderFactory;
     private OMElement omele= null;
     private OMElement omConfig= null;
     private String instanceURI_1 = "http://localhost:9999/soap/EPLStatementWithoutRegistryEventListener";
     private String instanceURI_2 = "http://localhost:9999/soap/EPLStatementWithRegistryEventListener";
 
      /**
      * Log associated with the EPL Statement Helper.
      */
     private static final Log log = LogFactory.getLog(EPLStatementHelper.class);
 
     /**
      * This is the epl statement associated with the Helper.
      */
     private String eplStatement = null;
 
     /**
      * Registry key to fetch the EPL Statement.
      */
     private String registryKey = null;
 
     /**
      * EPStatement associated with EPL Statement provided.
      * @see com.espertech.esper.client.EPStatement
      */
     private EPStatement statement = null;
 
     /**
      * Associated EPServiceProvider instance.
      * @see com.espertech.esper.client.EPServiceProvider
      */
     EPServiceProvider provider = null;
 
     /**
      * Associated Synapse Listener.
      * @see org.sciflex.plugins.synapse.esper.mediators.SynapseListener
      */
     SynapseListener listener = null;
 
     /**
      * Time at which the fetched EPL Statement expires. Synapse allows
      * each Registry interface to implement caching. expiryTime is used
      * to determine whether the cached instance is now expired.
      */
     long expiryTime = 0L;
 
     /**
      * create an OMElement having esper configuration.
      */
     public void createOMElement() {
 
         ombuilderFactory = OMAbstractFactory.getOMFactory();
         // String for setting eplStatement
         String payload1  = " <epl-statement key=\"statement/statement_xml.xml\"/>";
 
         omele = ombuilderFactory.createOMElement(new CharArrayDataSource(payload1.toCharArray()), "epl-statement", null);
 
     }
 
     public void createOMConfig() {
 
     ombuilderFactory = OMAbstractFactory.getOMFactory();
 
 
                /**
          *   String for setting esper configuration
          */
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
 
 
     
     public void testInvokeMethodWithoutRegistrySupport() throws Exception {
         listener = new SynapseListenerImpl();
         String registryKey = null;
         String eplStatement = null;
         EPStatement epStatement = null;
 
         XMLMediator xmlMediator = new ConfigurationAwareXMLMediator();
 
         createOMConfig();
         xmlMediator.setConfiguration(omConfig);
         xmlMediator.setInstanceURI(instanceURI_1);
         xmlMediator.setListener(syn_listener);
 
         createOMElement();
         xmlMediator.setStatement(omele);
         xmlMediator.setEventToAddress(instanceURI_1);
         EPServiceProvider epServiceProvider = EPServiceProviderManager.getProvider(instanceURI_1,
             ((ConfigurationAwareXMLMediator)xmlMediator).getConfiguration());
 
         // create eplAwareHelper instance with required parameters
         EPLStatementHelper eplAwareHelper = new EPLStatementAwareHelper(EPLStatementHelper.EPLStatementType.DIRECT,epl,epServiceProvider,listener);
 
         //((EPLStatementAwareHelper)eplAwareHelper).addListener(listener);
         MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext(REQ);
         ((EPLStatementAwareHelper)eplAwareHelper).invoke(synCtx);
 
         eplStatement = ((EPLStatementAwareHelper)eplAwareHelper).getEPLStatement();
         assertTrue("EPLStatement is not set properly", eplStatement != null) ;
 
 
         epStatement = ((EPLStatementAwareHelper)eplAwareHelper).getEPStatement();
         assertTrue("EPStatement is not set properly", epStatement != null) ;
 
     }
 
 
   public void testInvokeMethodWithRegistrySupport() throws Exception {
         listener = new SynapseListenerImpl();
         String registryKey = null;
         String eplStatement = null;
         EPStatement epStatement = null;
 
         Registry reg = new ESBRegistry();
         Properties props = new Properties();
         props.put("root", "file:../examples/conf/resources/registry/");
         props.put("cachableDuration", "1500");
         reg.init(props);
         Entry prop = new Entry();
         prop.setType(Entry.REMOTE_ENTRY);
 
         XMLMediator xmlMediator = new ConfigurationAwareXMLMediator();
 
         createOMConfig();
         xmlMediator.setConfiguration(omConfig);
         xmlMediator.setInstanceURI(instanceURI_2);
         xmlMediator.setListener(syn_listener);
 
         createOMElement();
         xmlMediator.setStatement(omele);
         xmlMediator.setEventToAddress(instanceURI_2);
         EPServiceProvider epServiceProvider = EPServiceProviderManager.getProvider(instanceURI_2,
             ((ConfigurationAwareXMLMediator)xmlMediator).getConfiguration());
 
         // get the registry key from the OMElement and create eplAwareHelper.
         registryKey = omele.getAttributeValue(new QName("key"));
         EPLStatementHelper eplAwareHelper = new EPLStatementAwareHelper(EPLStatementHelper.EPLStatementType.INDIRECT,registryKey,epServiceProvider,listener);
 
         // ((EPLStatementAwareHelper)eplAwareHelper).addListener(listener);
         MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext(REQ);
         synCtx.getConfiguration().setRegistry(reg);
         ((EPLStatementAwareHelper)eplAwareHelper).invoke(synCtx);
 
         eplStatement = ((EPLStatementAwareHelper)eplAwareHelper).getEPLStatement();
         assertTrue("EPLStatement is not set properly", eplStatement != null);
 
 
         epStatement = ((EPLStatementAwareHelper)eplAwareHelper).getEPStatement();
         assertTrue("EPStatement is not set properly", epStatement != null);
 
     }
 
 
     public void testSetEPL() throws Exception {
      listener = new SynapseListenerImpl();
         String registryKey = null;
         String eplStatement = null;
         EPStatement epStatement = null;
         Registry reg = new ESBRegistry();
 
         XMLMediator xmlMediator = new ConfigurationAwareXMLMediator();
 
         createOMConfig();
         xmlMediator.setConfiguration(omConfig);
         xmlMediator.setInstanceURI(instanceURI_1);
         xmlMediator.setListener(syn_listener);
 
         createOMElement();
         xmlMediator.setStatement(omele);
         xmlMediator.setEventToAddress(instanceURI_1);
         EPServiceProvider epServiceProvider = EPServiceProviderManager.getProvider(instanceURI_1,
             ((ConfigurationAwareXMLMediator)xmlMediator).getConfiguration());
 
         MessageContext synCtx = TestUtils.createLightweightSynapseMessageContext(REQ);
         synCtx.getConfiguration().setRegistry(reg);
         // create eplAwareHelper instance with required parameters
         EPLStatementHelper eplAwareHelper = new EPLStatementAwareHelper(EPLStatementHelper.EPLStatementType.DIRECT,epl,epServiceProvider,listener);
 
         eplAwareHelper.setEPL(epl,reg);
         
         eplStatement = ((EPLStatementAwareHelper)eplAwareHelper).getEPLStatement();
         assertTrue("EPLStatement is not set properly", eplStatement != null) ;
 
     }
 
 }
