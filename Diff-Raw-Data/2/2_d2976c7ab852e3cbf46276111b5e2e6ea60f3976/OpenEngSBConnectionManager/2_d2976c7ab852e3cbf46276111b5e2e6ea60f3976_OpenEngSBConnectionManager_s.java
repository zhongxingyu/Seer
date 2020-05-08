 package org.openengsb.xlinkSQLViewer.xlink;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.jms.JMSException;
 
 import org.apache.log4j.Logger;
 import org.openengsb.connector.usernamepassword.Password;
 import org.openengsb.core.api.ConnectorManager;
 import org.openengsb.core.api.ConnectorValidationFailedException;
 import org.openengsb.core.api.model.ModelDescription;
 import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
 import org.openengsb.core.api.xlink.model.ModelToViewsTuple;
 import org.openengsb.core.api.xlink.model.XLinkConnectorView;
 import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
 import org.openengsb.domain.SQLCode.SQLCodeDomainEvents;
 import org.openengsb.domain.SQLCode.model.SQLCreate;
 import org.openengsb.loom.java.ProxyConnectorFactory;
 import org.openengsb.loom.java.jms.JmsProtocolHandler;
 import org.openengsb.xlinkSQLViewer.ui.SqlViewerGUI;
 
 /**
  * ConnectionManager for the OpenEngSB
  */
 public class OpenEngSBConnectionManager {
 	
 	private static Logger logger = Logger.getLogger(OpenEngSBConnectionManager.class.getName());
 	
 	/*Supplied program arguments*/
     private ProxyConnectorFactory domainFactory;
     private JmsProtocolHandler jmsConfig;
     private ConnectorManager cm;
     
     /*Supplied program arguments*/
     private String xlinkBaseUrl;
     private String domainId;
     private String programname;  
     private String openengsbUser;
     private String openengsbPassword;
 
     /**Id of the registered OpenEngSB-Connector*/
     private String connectorUUID;
     
     /**Flag indicating the connection status*/
     private boolean connected = false;
     
     /**HostIP of the local system, used to identify the Host during an XLink call*/
     private String hostIp;
     
     /**XLinkUrlBlueprint received during XLinkRegistration*/
     private XLinkUrlBlueprint blueprint;
     
     /**Proxy EventInterface to send events to the OpenEngSB*/
     private SQLCodeDomainEvents domainEvents;
 
 	/** Only possible OpenEngSBConnectionManager instance */
 	private static OpenEngSBConnectionManager instance = null;
 	
 	private OpenEngSBConnectionManager(String xlinkBaseUrl, 
 			String domainId, String programname,
 			String openengsbUser, String openengsbPassword,
 			String hostIp) {
 		super();
 		this.xlinkBaseUrl = xlinkBaseUrl;
 		this.domainId = domainId;
 		this.programname = programname;
 		this.connected = false;
 		this.hostIp = hostIp;
 		this.openengsbUser = openengsbUser;
 		this.openengsbPassword = openengsbPassword;
 	}	
 	
 	/**
 	 * Initializes the Connectors only instance.
 	 */
 	public static void initInstance(String xlinkBaseUrl, 
 			String domainId, String programname, 
 			String openengsbUser, String openengsbPassword, 
 			String hostIp) {
 		instance = new OpenEngSBConnectionManager(xlinkBaseUrl, domainId, 
 				programname, openengsbUser, openengsbPassword, hostIp);
 	}	
 	
 	/**
 	 * Returns the Connectors only instance.
 	 */
 	public static OpenEngSBConnectionManager getInstance() {
 		if (instance == null) {
 			logger.warn("getInstance():OpenEngSBConnectionManager was not initialized.");
 		}
 		return instance;
 	}
 
 	/***
 	 * Creates/Registers the connector at the OpenEngSB and registers the connector to XLink
 	 * @throws DomainNotLinkableException 
 	 */
 	public void connectToOpenEngSbWithXLink(SqlViewerGUI gui) throws JMSException, ConnectorValidationFailedException {
         /*Create/Register the connector*/
		jmsConfig = new JmsProtocolHandler(xlinkBaseUrl);
         domainFactory = new ProxyConnectorFactory(jmsConfig, openengsbUser, new Password(openengsbPassword));
         connectorUUID = domainFactory.createConnector(domainId);
         LinkableConnector handler = new LinkableConnector(gui);
         domainFactory.registerConnector(connectorUUID, handler);
         
         /*Fetch Event Interface*/
         domainEvents = domainFactory.getRemoteProxy(SQLCodeDomainEvents.class, null);
         
         /*Register to XLink*/
         cm = domainFactory.getRemoteProxy(ConnectorManager.class, null);     
         blueprint = cm.connectToXLink(connectorUUID, hostIp, 
         		programname, initModelViewRelation());
         
 		connected = true;
 		gui.changeStatusToConnected();
 		logger.info("connected to openEngSB and XLink");
 	}
 
 	/**
 	 * Creates the Array of Model/View relations, offered by the Tool, for XLink
 	 */
 	private ModelToViewsTuple[] initModelViewRelation(){
         ModelToViewsTuple[] modelsToViews 
 	    	= new ModelToViewsTuple[1];  
 	    Map<String, String> descriptions  = new HashMap<String, String>();
 	    descriptions.put("en", "This view opens the values in a SQLViewer.");
 	    descriptions.put("de", "Dieses Tool oeffnet die Werte in einem SQLViewer.");
 	    List<XLinkConnectorView> views = new ArrayList<XLinkConnectorView>();
 	    views.add(new XLinkConnectorView(SqlViewerGUI.viewId, SqlViewerGUI.viewName, descriptions));         
 	    modelsToViews[0] = 
 	            new ModelToViewsTuple(
 	                    new ModelDescription(
 	                    		SQLCreate.class.getName(),
 	                            "3.0.0.SNAPSHOT")
 	                    , views.toArray(new XLinkConnectorView[0]));
 	    return modelsToViews;
 	}
 	
 	/*private String getHostName(){
 		String hostname = null;
         try {
             InetAddress addr = InetAddress.getLocalHost();
             hostname = addr.getHostName();
             logger.log(Level.INFO,"Host "+hostname);
         } catch (UnknownHostException e) {
         }   
         return hostname;
 	}*/
 	
 	public boolean isConnected() {
 		return connected;
 	}
 	
 	/**
 	 * Sends DataUpdates to the OpenEnbSB
 	 */
 	public void raiseDataUpdate(SQLCreate newCreate){
 		domainEvents.raiseDataUpdate(newCreate);
 	}
 	
 	/**
 	 * Unregisters the connector from XLink and removes it from the OpenEngSB
 	 */
 	public void disconnect(){
 		cm.disconnectFromXLink(connectorUUID, hostIp);
 		domainFactory.unregisterConnector(connectorUUID);
     	domainFactory.deleteConnector(connectorUUID);
     	jmsConfig.destroy();
     	logger.info("disconnected from openEngSB and XLink");
 	}
 
 	public XLinkUrlBlueprint getBluePrint() {
 		return blueprint;
 	}
 	
 }
