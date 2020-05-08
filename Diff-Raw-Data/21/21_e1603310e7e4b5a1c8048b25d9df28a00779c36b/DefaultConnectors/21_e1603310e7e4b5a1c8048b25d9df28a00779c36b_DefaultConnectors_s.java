 package net.sf.okapi.lib.ui.translation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DefaultConnectors implements IConnectorList {
 
 	private ArrayList<ConnectorInfo> list;
 	
 	public DefaultConnectors () {
 		list = new ArrayList<ConnectorInfo>();
 		setDefaultMapping();
 	}
 	
 	// Temporary hard-coded mapping
 	private void setDefaultMapping () {
 		ConnectorInfo trc = new ConnectorInfo();
 		trc.description = "Pensieve TM Engine (Alpha)";
 		trc.connectorClass = "net.sf.okapi.connectors.pensieve.PensieveTMConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.pensieve.ParametersUI";
 		list.add(trc);
 		
 		trc = new ConnectorInfo();
 		trc.description = "SimpleTM TM Engine";
 		trc.connectorClass = "net.sf.okapi.connectors.simpletm.SimpleTMConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.simpletm.ParametersUI";
 		list.add(trc);
 		
 		trc = new ConnectorInfo();
 		trc.description = "GlobalSight TM Web Services";
 		trc.connectorClass = "net.sf.okapi.connectors.globalsight.GlobalSightTMConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.globalsight.ParametersUI";
 		list.add(trc);
 		
 		trc = new ConnectorInfo();
 		trc.description = "Google Translate Services";
 		trc.connectorClass = "net.sf.okapi.connectors.google.GoogleMTConnector";
 		list.add(trc);
 		
 		trc = new ConnectorInfo();
		trc.description = "MyMemory TM Web Services (Beta)";
 		trc.connectorClass = "net.sf.okapi.connectors.mymemory.MyMemoryTMConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.mymemory.ParametersUI";
 		list.add(trc);
 
 		trc = new ConnectorInfo();
		trc.description = "OpenTran Web Repository (Beta)";
 		trc.connectorClass = "net.sf.okapi.connectors.opentran.OpenTranTMConnector";
 		list.add(trc);
 
 		trc = new ConnectorInfo();
		trc.description = "Translate Toolkit TM Server (Beta)";
 		trc.connectorClass = "net.sf.okapi.connectors.translatetoolkit.TranslateToolkitTMConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.translatetoolkit.ParametersUI";
 		list.add(trc);
 
 		trc = new ConnectorInfo();
		trc.description = "Apertium MT Server (Beta)";
 		trc.connectorClass = "net.sf.okapi.connectors.apertium.ApertiumMTConnector";
 		trc.descriptionProviderClass = "net.sf.okapi.connectors.apertium.Parameters";
 		list.add(trc);
 	}
 
 	public List<ConnectorInfo> getList () {
 		return list;
 	}
 
 }
