 /**
  * <p>Title: ExperimentDataCategoryGridPanel Class>
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Hrishikesh Rajpathak/Deepak Shingan
  * @version 1.4
  */
 
 package edu.wustl.cab2b.client.ui.experiment;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 
 import org.jdesktop.swingx.JXTableHeader;
 import org.jdesktop.swingx.decorator.Filter;
 import org.jdesktop.swingx.decorator.FilterPipeline;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.wustl.cab2b.client.ui.controls.Cab2bButton;
 import edu.wustl.cab2b.client.ui.controls.Cab2bPanel;
 import edu.wustl.cab2b.client.ui.filter.CaB2BFilterInterface;
 import edu.wustl.cab2b.client.ui.filter.CaB2BPatternFilter;
 import edu.wustl.cab2b.client.ui.filter.Cab2bFilterPopup;
 import edu.wustl.cab2b.client.ui.filter.EnumeratedFilterPopUp;
 import edu.wustl.cab2b.client.ui.filter.FilterComponent;
 import edu.wustl.cab2b.client.ui.filter.PatternPopup;
 import edu.wustl.cab2b.client.ui.filter.RangeFilter;
 import edu.wustl.cab2b.common.util.Utility;
 import edu.wustl.common.querysuite.queryobject.DataType;
 
 /**
  * This class displays the experiment table. Also provides filtering tool for
  * the class
  * 
  * @author hrishikesh_rajpathak
  * @author deepak_shingan
  * 
  */
 public class ExperimentDataCategoryGridPanel extends Cab2bPanel {
 
 	private JTabbedPane tabComponent;
 
 	/**
 	 * Panel to display experiment data when data category node is selected
 	 * First tab panel on tab Component
 	 */
 	private Cab2bPanel experimentDataPanel;
 
 	/**
 	 * Panel to display analysis performed on experiment Second tab panel on tab
 	 * component
 	 */
 	private Cab2bPanel analysisDataPanel;
 	
 	/**
 	 * Panel to display analysis performed on experiment Second tab panel on tab
 	 * component
 	 */
 	private Cab2bPanel visualizeDataPanel;
 
 	/** Button to save data category */
 	private Cab2bButton saveDataCategoryButton;
 
 	private Cab2bButton prevButton;
 
 	/**
 	 * Table to display records on Experiment Data panels, when user selects any
 	 * data category node
 	 */
 	private ExperimentTableModel table;
 
	private Vector tableColumnVector = new Vector();
 
	private Vector tableDataRecordVector = new Vector();
 
 	private JScrollPane theScrollPane = new JScrollPane();
 
 	public static ArrayList<String> values = new ArrayList<String>();
 
 	private static Map<String, CaB2BFilterInterface> filterMap = new HashMap<String, CaB2BFilterInterface>();
 
 	public ExperimentDataCategoryGridPanel() {
		initGUI();
 	}
 
 	/**
 	 * Adding a filter to the filter map
 	 */
 	public static void addFilter(String columnName, CaB2BFilterInterface filter) {
 		filterMap.put(columnName, filter);
 	}
 
 	public static void clearMap() {
 		filterMap.clear();
 	}
 
 	public static Vector getFilterMap() {
 		Vector<CaB2BFilterInterface> vector = new Vector<CaB2BFilterInterface>();
 		for (CaB2BFilterInterface filter : filterMap.values()) {
 			vector.add(filter);
 		}
 		return vector;
 	}
 
 	public ExperimentDataCategoryGridPanel(Vector columnVector, Vector dataRecordVector) {
 		tableColumnVector = columnVector;
 		tableDataRecordVector = dataRecordVector;
 		initGUI();
 	}
 
 	/**
 	 * Building/refreshing the table
 	 */
 	public void refreshTable(Object columnVector[], Object[][] dataRecordVector,
 			Map<String, AttributeInterface> attributeMap) {
 		this.removeAll();
 		experimentDataPanel.removeAll();
 		table = new ExperimentTableModel(false, dataRecordVector, columnVector, attributeMap);
 		table.setColumnSelectionAllowed(true);
 		MouseListener mouseListener = new myMouseListener();
 		// add the listener specifically to the header
 		table.addMouseListener(mouseListener);
 		table.getTableHeader().addMouseListener(mouseListener);
 		theScrollPane.getViewport().add(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		experimentDataPanel.add("hfill vfill", theScrollPane);
 
 		Cab2bPanel northPanel = new Cab2bPanel();
 		northPanel.add(saveDataCategoryButton);
 		this.add(northPanel, BorderLayout.NORTH);
 
 		tabComponent.add("Experiment Data", experimentDataPanel);
 		//tabComponent.add("Analysis", analysisDataPanel);
 		tabComponent.add("Chart", visualizeDataPanel);
 		this.add(tabComponent, BorderLayout.CENTER);
 
 		Cab2bPanel bottomPanel = new Cab2bPanel();
 		bottomPanel.add(prevButton);
 		this.add(bottomPanel, BorderLayout.SOUTH);
 		this.updateUI();
 	}
 
 	/**
 	 * Initializing the GUI. Building the table initially.
 	 */
 	public void initGUI() {
 		this.setLayout(new BorderLayout());
 		tabComponent = new JTabbedPane();
 		tabComponent.setBorder(null);
 		experimentDataPanel = new Cab2bPanel();
 		experimentDataPanel.setBorder(null);
 		analysisDataPanel = new Cab2bPanel();
 		visualizeDataPanel = new Cab2bPanel();
 
 		table = new ExperimentTableModel(false, tableDataRecordVector, tableColumnVector);
 		table.setColumnSelectionAllowed(true);
 		MouseListener mouseListener = new myMouseListener();
 		// add the listener specifically to the header
 		table.addMouseListener(mouseListener);
 		table.getTableHeader().addMouseListener(mouseListener);
 
 		/* Adding scrollpane */
 		theScrollPane.getViewport().add(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		experimentDataPanel.add("hfill vfill", theScrollPane);
 
 		saveDataCategoryButton = new Cab2bButton("Save Data Category");
 		saveDataCategoryButton.setPreferredSize(new Dimension(160, 22));
 
 		Cab2bPanel northPanel = new Cab2bPanel();
 		northPanel.add(saveDataCategoryButton);
 		this.add(northPanel, BorderLayout.NORTH);
 
 		tabComponent.add("Experiment Data", experimentDataPanel);
 		//tabComponent.add("Analysis", analysisDataPanel);
 		tabComponent.add("Chart", visualizeDataPanel);
 		this.add(tabComponent, BorderLayout.CENTER);
 
 		prevButton = new Cab2bButton("Previous");
 		Cab2bPanel bottomPanel = new Cab2bPanel();
         prevButton.setEnabled(false);
 		bottomPanel.add(prevButton);
 		this.add(bottomPanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Action listener for the header click for filteration purpose.
 	 */
 	class myMouseListener extends MouseAdapter {
 
 		public void mouseReleased(MouseEvent e) {
 			showPopup(e);
 		}
 
 		private void showPopup(MouseEvent e) {
 			Object obj = e.getComponent();
 
 			if (obj instanceof JXTableHeader) {
 
 				int columnIndex = ((JXTableHeader) e.getComponent()).columnAtPoint(e.getPoint());
 				Cab2bFilterPopup filterPopup = null;
 				String columnName = table.getColumnName(columnIndex);
 				CaB2BFilterInterface oldFilter = null;
 				if (filterMap.containsKey(columnName)) {
 					oldFilter = filterMap.get(columnName);
 				}
 				AttributeInterface attribute = table.getColumnAttribute(columnName);
 				if (Utility.isEnumerated(attribute)) {
 					Collection<PermissibleValueInterface> permissibleValueCollection = Utility
 							.getPermissibleValues(attribute);
 					filterPopup = new EnumeratedFilterPopUp(columnName, columnIndex,
 							permissibleValueCollection, (CaB2BPatternFilter) oldFilter);
 				} else {
 					DataType dataType = Utility
 							.getDataType(attribute.getAttributeTypeInformation());
 					// If the clicked column is of type String.
 					if (DataType.String == dataType) {
 
 						filterPopup = new PatternPopup((CaB2BPatternFilter) oldFilter, columnName,
 								columnIndex);
 						// If the clicked column is of type int/long
 					} else if (DataType.Long == dataType || DataType.Integer == dataType
 							|| DataType.Double == dataType || DataType.Float == dataType) {
 						int len = table.getRowCount();
 						float columnVal[] = null;
 						if (oldFilter == null) {
 							columnVal = new float[len];
 							for (int i = 0; i < len; i++) {
 								String val = (String) table.getValueAt(i, columnIndex);
 								if (val != null) {
 									float f = Float.parseFloat(val);
 									columnVal[i] = f;
 								}
 
 							}
 						}
 						filterPopup = new FilterComponent("Range Filter", columnVal, columnName,
 								columnIndex, (RangeFilter) oldFilter);
 					}
 
 				}
 				filterPopup.showInDialog();
 				applyFilter();
 			}
 
 		}
 
 		/**
 		 * Applying filter according to the the values in the map
 		 */
 		public void applyFilter() {
 			int len = filterMap.size();
 			Filter[] filters = new Filter[len];
 			int i = 0;
 			for (CaB2BFilterInterface filter : filterMap.values()) {
 				filters[i] = (Filter) filter.copy();
 				i++;
 			}
 			table.setFilters(new FilterPipeline(filters));
 		}
 	}
 
 	/**
 	 * @return the tabComponent
 	 */
 	public JTabbedPane getTabComponent() {
 		return tabComponent;
 	}
 
 	/**
 	 * @param tabComponent the tabComponent to set
 	 */
 	public void setTabComponent(JTabbedPane tabComponent) {
 		this.tabComponent = tabComponent;
 	}
 
 	/**
 	 * @return the table
 	 */
 	public ExperimentTableModel getTable() {
 		return table;
 	}
 
 	/**
 	 * @param table the table to set
 	 */
 	public void setTable(ExperimentTableModel table) {
 		this.table = table;
 	}
 
 	/**
 	 * @return the visualizeDataPanel
 	 */
 	public Cab2bPanel getVisualizeDataPanel() {
 		return visualizeDataPanel;
 	}
 
 	/**
 	 * @param visualizeDataPanel the visualizeDataPanel to set
 	 */
 	public void setVisualizeDataPanel(Cab2bPanel visualizeDataPanel) {
 		this.visualizeDataPanel = visualizeDataPanel;
 	}
 
 }
