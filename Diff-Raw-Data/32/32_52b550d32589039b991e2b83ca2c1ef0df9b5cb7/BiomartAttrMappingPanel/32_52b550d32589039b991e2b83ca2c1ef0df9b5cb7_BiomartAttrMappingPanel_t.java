 /*
  Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)
 
  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package edu.ucsd.bioeng.idekerlab.biomartclient.ui;
 
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.data.webservice.AttributeImportQuery;
 import cytoscape.data.webservice.CyWebServiceEvent;
 import cytoscape.data.webservice.WebServiceClientManager;
 import cytoscape.data.webservice.CyWebServiceEvent.WSEventType;
 import cytoscape.layout.Tunable;
 import cytoscape.task.Task;
 import cytoscape.task.TaskMonitor;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.swing.AttributeImportPanel;
 import edu.ucsd.bioeng.idekerlab.biomartclient.BiomartClient;
 import edu.ucsd.bioeng.idekerlab.biomartclient.BiomartStub;
 import edu.ucsd.bioeng.idekerlab.biomartclient.utils.Attribute;
 import edu.ucsd.bioeng.idekerlab.biomartclient.utils.Dataset;
 import edu.ucsd.bioeng.idekerlab.biomartclient.utils.Filter;
 import edu.ucsd.bioeng.idekerlab.biomartclient.utils.XMLQueryBuilder;
 import giny.model.Node;
 
 
 /**
  *
  */
 public class BiomartAttrMappingPanel extends AttributeImportPanel implements PropertyChangeListener {
 
 	private static final long serialVersionUID = 3574198525811249639L;
 
 	private static final Icon LOGO = new ImageIcon(BiomartAttrMappingPanel.class.getResource("/images/logo_biomart2.png"));
 	
 	private Map<String, String> datasourceMap;
 	private Map<String, Map<String, String[]>> attributeMap;
 	private Map<String, List<String>> attributeListOrder;
 	private Map<String, Map<String, String>> attrNameMap;
 	private Map<String, Map<String, String>> filterMap;
 	
 	private final TaskMonitor monitor;
 	
 	private enum SourceType {
 		DATABASE,
 		ATTRIBUTE,
 		FILTER;
 	}
 
 	private boolean cancelFlag = false;
 	private boolean initialized = false;
 		
 	private final BiomartStub stub = (BiomartStub) WebServiceClientManager.getClient("biomart")
 	                                                                      .getClientStub();
 
 	// These databases are not compatible with this UI.
 	private static final List<String> databaseFilter = new ArrayList<String>();
 
 	static {
 		// Database on this list will not appear on the list.
 		databaseFilter.add("compara_mart_pairwise_ga_47");
 		databaseFilter.add("compara_mart_multiple_ga_47");
 		databaseFilter.add("dicty");
 		databaseFilter.add("Pancreatic_Expression");
 	}
 
 	public BiomartAttrMappingPanel(final TaskMonitor monitor) throws Exception {
 		this(LOGO, "", "Available attributes", monitor);
 	}
 
 	/**
 	 * Creates a new BiomartNameMappingPanel object.
 	 *
 	 * @param logo  DOCUMENT ME!
 	 * @param title  DOCUMENT ME!
 	 * @throws Exception
 	 */
 	public BiomartAttrMappingPanel(Icon logo, String title, String attrPanelLabel, final TaskMonitor monitor)
 	    throws Exception {
 		super(logo, title, attrPanelLabel);
 		this.monitor = monitor;
 		
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
 		BiomartMainDialog.getPropertyChangeSupport().addPropertyChangeListener(this);
 		initDataSources();
 	}
 
 	
 	/**
 	 * Access data sources and build GUI.
 	 * 
 	 * @throws Exception
 	 */
 	private void initDataSources() throws Exception {
 		datasourceMap = new HashMap<String, String>();
 		attributeMap = new HashMap<String, Map<String, String[]>>();
 		attributeListOrder = new HashMap<String, List<String>>();
 		filterMap = new HashMap<String, Map<String, String>>();
 		attrNameMap = new HashMap<String, Map<String, String>>();
 
 		monitor.setPercentCompleted(0);
 		loadMartServiceList();
 		loadFilter();
 	}
 
 	protected boolean isInitialized() {
 		return initialized;
 	}
 
 	private void loadMartServiceList() {
 		Map<String, Map<String, String>> reg = null;
 		monitor.setStatus("Accessing Mart service registry...");
 		try {
 			reg = stub.getRegistry();
 		} catch (IOException e1) {
 			monitor.setException(e1, "Could not retreve list of Mart Services from registry.");
 		} catch (ParserConfigurationException e1) {
 			monitor.setException(e1, "Could not parse list of Mart Services.");
 		} catch (SAXException e1) {
 			monitor.setException(e1, "Could not parse list of Mart Services.");
 		}
 		monitor.setPercentCompleted(10);
 		
 		final int registryCount = reg.size();
 		int increment = 90/registryCount;
 		int percentCompleted = 10;
 
 		Map<String, String> datasources;
 		List<String> dsList = new ArrayList<String>();
 		for (String databaseName : reg.keySet()) {
 			
 			Map<String, String> detail = reg.get(databaseName);
 
 			// Add the datasource if its visible
 			if (detail.get("visible").equals("1")
 			    && (databaseFilter.contains(databaseName) == false) && (cancelFlag == false)) {
 				String dispName = detail.get("displayName");
 				monitor.setStatus("Connecting to " + dispName);
 				try {
 					datasources = stub.getAvailableDatasets(databaseName);
 				} catch (IOException e) {
					// If timeout/connection error is found, skip the source. 
					percentCompleted += increment;
					monitor.setPercentCompleted(percentCompleted);
 					continue;
 				}
 
 				for (String key : datasources.keySet()) {
 					dsList.add(dispName + " - " + datasources.get(key));
 					datasourceMap.put(dispName + " - " + datasources.get(key), key);
 				}
 			} else if (cancelFlag) {
 				cancelFlag = false;
 
 				return;
 			}
 			
 			percentCompleted += increment;
 			monitor.setPercentCompleted(percentCompleted);
 		}
 
 		Collections.sort(dsList);
 
 		for (String ds : dsList) {
 			this.databaseComboBox.addItem(ds);
 		}
 
 		initialized = true;
 	}
 
 	private void loadFilter() throws Exception {
 		attributeTypeComboBox.removeAllItems();
 
 		final String selectedDB = databaseComboBox.getSelectedItem().toString();
 		final String selectedDBName = datasourceMap.get(selectedDB);
 
 		fetchData(selectedDBName, SourceType.FILTER);
 	}
 
 	protected void resetButtonActionPerformed(ActionEvent e) {
 		try {
 			updateAttributeList();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	protected void databaseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		try {
 			updateAttributeList();
 			loadFilter();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 
 	private void updateAttributeList() throws Exception {
 		final String selectedDB = databaseComboBox.getSelectedItem().toString();
 		final String selectedDBName = datasourceMap.get(selectedDB);
 
 		List<String> order = attributeListOrder.get(selectedDBName);
 		model = new DefaultListModel();
 		attrList.setModel(model);
 
 		if (order != null) {
 			//			List<String> sortedList = new ArrayList<String>(singleAttrMap.keySet());
 			//			Collections.sort(sortedList);
 			for (String dispAttrName : order) {
 				model.addElement(dispAttrName);
 			}
 		} else {
 			fetchData(selectedDBName, SourceType.ATTRIBUTE);
 		}
 	}
 
 	private void fetchData(final String datasourceName, SourceType type) throws IOException {
 		Map<String, String> returnValMap = new HashMap<String, String>();
 		final List<String> order = new ArrayList<String>();
 		final String selectedDB = databaseComboBox.getSelectedItem().toString();
 		final String selectedDBName = datasourceMap.get(selectedDB);
 
 		if (type.equals(SourceType.ATTRIBUTE)) {
 			Map<String, String[]> attributeVals = stub.getAttributes(datasourceName);
 			Map<String, String> names = new HashMap<String, String>();
 			attributeMap.put(selectedDBName, attributeVals);
 			model.removeAllElements();
 
 			String[] entry;
 			String dispNameWithCategory;
 
 			for (String attr : attributeVals.keySet()) {
 				entry = attributeVals.get(attr);
 
 				if ((entry != null) && (entry[0] != null)) {
 					if ((entry.length > 2) && (entry[2] != null))
 						dispNameWithCategory = entry[2] + ": \t" + entry[0] + "\t  (" + attr + ")";
 					else
 						dispNameWithCategory = " \t" + entry[0] + "\t  (" + attr + ")";
 
 					names.put(dispNameWithCategory, attr);
 					order.add(dispNameWithCategory);
 				}
 			}
 
 			this.attrNameMap.put(selectedDBName, names);
 			Collections.sort(order);
 
 			for (String attrName : order) {
 				model.addElement(attrName);
 			}
 
 			attributeListOrder.put(selectedDBName, order);
 			attrList.repaint();
 		} else if (type.equals(SourceType.FILTER)) {
 			returnValMap = stub.getFilters(datasourceName, false);
 			filterMap.put(selectedDBName, returnValMap);
 
 			List<String> filterNames = new ArrayList<String>(returnValMap.keySet());
 			Collections.sort(filterNames);
 
 			for (String filter : filterNames)
 				attributeTypeComboBox.addItem(filter);
 		}
 	}
 
 	protected void importButtonActionPerformed(ActionEvent evt) {
 		final String datasource = datasourceMap.get(databaseComboBox.getSelectedItem());
 		final Map<String, String> attrMap = this.attrNameMap.get(datasource);
 		final Map<String, String> fMap = filterMap.get(datasource);
 
 		final String keyAttrName = attributeComboBox.getSelectedItem().toString();
 
 		System.out.println("Target attr name found: " + keyAttrName);
 
 		Dataset dataset;
 		Attribute[] attrs;
 		Filter[] filters;
 
 		// Name of the datasource
 		dataset = new Dataset(datasource);
 		//System.out.println("Target Dataset = " + dataset.getName());
 
 		final Object[] selectedAttr = attrList.getSelectedValues();
 		attrs = new Attribute[selectedAttr.length + 1];
 
 		// This is the mapping key
 		String filterName = fMap.get(attributeTypeComboBox.getSelectedItem());
 		String dbName = this.databaseComboBox.getSelectedItem().toString();
 		//System.out.println("Filter Name = " + filterName);
 
 		// Database-specific modification.
 		// This is not the best way, but cannot provide universal solution.
 		if (dbName.contains("REACTOME")) {
 			attrs[0] = new Attribute(stub.toAttributeName("REACTOME", filterName));
 		} else if (dbName.contains("VARIATION")) {
 //			String newName = filterName.replace("_id", "_stable_id");
 //			newName = newName.replace("_ensembl", "");
 			attrs[0] = new Attribute(filterName + "_stable_id");
 		} else {
 			attrs[0] = new Attribute(filterName);
 		}
 
 		for (int i = 1; i <= selectedAttr.length; i++) {
 			attrs[i] = new Attribute(attrMap.get(selectedAttr[i - 1]));
 		}
 
 		// For name mapping, just use ID list filter for query.
 		filters = new Filter[1];
 
 		filters[0] = new Filter(filterName, getIDFilterString(keyAttrName));
 
 		String keyInHeader = null;
 
 		for (String key : attrMap.keySet()) {
 			if (attrMap.get(key).equals(filterName)) {
 				keyInHeader = key.split("\\t")[1];
 				//System.out.println("Key Attr = " + keyInHeader);
 			}
 		}
 
 		// Create Task
 		final ImportAttributeTask task = new ImportAttributeTask(dataset, attrs, filters,
 		                                                         keyInHeader, keyAttrName);
 
 		// Configure JTask Dialog Pop-Up Box
 		final JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayCancelButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pop open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 
 		//firePropertyChange(CLOSE_EVENT, null, null);
 	}
 
 	private static String getIDFilterString(String keyAttrName) {
 		
 		final Tunable tunable = WebServiceClientManager.getClient("biomart").getProps().get("selected_only");	
 		tunable.updateValue();
 		final Object value = tunable.getValue();
 		
 		final List<Node> nodes;
 		if (value != null && Boolean.parseBoolean(value.toString())) {
 			// Selected nodes only
 			nodes = new ArrayList<Node>(Cytoscape.getCurrentNetwork().getSelectedNodes());
 		} else {
 			// Send all nodes in current network
 			nodes = Cytoscape.getCurrentNetwork().nodesList();
 		}
 		
 		final StringBuilder builder = new StringBuilder();
 
 		// If attribute name is ID, then use node id as the key.
 		if (keyAttrName.equals("ID")) {
 			for (Node n : nodes) {
 				builder.append(n.getIdentifier());
 				builder.append(",");
 			}
 		} else {
 			// Use Attributes for mapping
 			final CyAttributes attrs = Cytoscape.getNodeAttributes();
 			Map mapAttrs = CyAttributesUtils.getAttribute(keyAttrName, attrs);
 
 			if ((mapAttrs == null) || (mapAttrs.size() == 0))
 				return null;
 
 			//			List acceptedClasses = Arrays.asList(mapping.getAcceptedDataClasses());
 			//			Class mapAttrClass = CyAttributesUtils.getClass(attrName, attrs);
 			//
 			//			if ((mapAttrClass == null) || !(acceptedClasses.contains(mapAttrClass)))
 			//				return null;
 			for (String key : loadKeySet(mapAttrs)) {
 				builder.append(key);
 				builder.append(",");
 			}
 		}
 
 		String filterStr = builder.toString();
 		filterStr = filterStr.substring(0, filterStr.length() - 1);
 
 		return filterStr;
 	}
 
 	private static Set<String> loadKeySet(final Map mapAttrs) {
 		final Set<String> mappedKeys = new TreeSet<String>();
 
 		final Iterator keyIter = mapAttrs.values().iterator();
 
 		Object o = null;
 
 		while (keyIter.hasNext()) {
 			o = keyIter.next();
 
 			if (o instanceof List) {
 				List list = (List) o;
 
 				for (int i = 0; i < list.size(); i++) {
 					Object vo = list.get(i);
 
 					if (!mappedKeys.contains(vo))
 						mappedKeys.add(vo.toString());
 				}
 			} else {
 				if (!mappedKeys.contains(o))
 					mappedKeys.add(o.toString());
 			}
 		}
 
 		return mappedKeys;
 	}
 
 	@Override
 	protected void importAttributes() {
 		// Build Query
 	}
 
 	private class ImportAttributeTask implements Task {
 		private Dataset dataset;
 		private Attribute[] attrs;
 		private Filter[] filters;
 		private String key;
 		private String keyAttrName;
 		private TaskMonitor taskMonitor;
 
 		public ImportAttributeTask(Dataset dataset, Attribute[] attrs, Filter[] filters,
 		                           String key, String keyAttrName) {
 			this.dataset = dataset;
 			this.attrs = attrs;
 			this.filters = filters;
 			this.key = key;
 			this.keyAttrName = keyAttrName;
 		}
 
 		/**
 		 *  DOCUMENT ME!
 		 *
 		 * @return  DOCUMENT ME!
 		 */
 		public String getTitle() {
 			return "Loading Attributes from Web Service";
 		}
 
 		/**
 		 *  DOCUMENT ME!
 		 */
 		public void halt() {
 			try {
 				WebServiceClientManager.getCyWebServiceEventSupport()
 				                       .fireCyWebServiceEvent(new CyWebServiceEvent("biomart",
 				                                                                    WSEventType.CANCEL,
 				                                                                    null));
 			} catch (Exception e) {
 				taskMonitor.setException(e, "Exception occured while canceling import task.");
 			}
 		}
 
 		/**
 		 *  DOCUMENT ME!
 		 */
 		public void run() {
 			taskMonitor.setStatus("Accessing Biomart Web Service.\n\nIt may take a while.\nPlease wait...");
 			taskMonitor.setPercentCompleted(-1);
 
 			String query2 = XMLQueryBuilder.getQueryString(dataset, attrs, filters);
 
 			AttributeImportQuery qObj = new AttributeImportQuery(query2, key, keyAttrName);
 
 			try {
 				WebServiceClientManager.getCyWebServiceEventSupport()
 				                       .fireCyWebServiceEvent(new CyWebServiceEvent("biomart",
 				                                                                    WSEventType.IMPORT_ATTRIBUTE,
 				                                                                    qObj));
 			} catch (Exception e) {
 				taskMonitor.setException(e, "Could not initialize connection to Biomart.");
 			}
 
 			taskMonitor.setPercentCompleted(100);
 			taskMonitor.setStatus( ((BiomartClient)WebServiceClientManager.getClient("biomart")).getLastStatus() );
 
 //			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), statusMessage, "Import Complete",
 //                    JOptionPane.INFORMATION_MESSAGE);
 		}
 
 		/**
 		 *  DOCUMENT ME!
 		 *
 		 * @param arg0 DOCUMENT ME!
 		 *
 		 * @throws IllegalThreadStateException DOCUMENT ME!
 		 */
 		public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
 			this.taskMonitor = taskMonitor;
 		}
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals("CANCEL")) {
 			this.cancelFlag = true;
 			//System.out.println("Cancelling Biomart client initialization...");
 		}
 	}
 }
