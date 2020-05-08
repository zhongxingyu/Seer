 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package org.acme.example.view;
 
 import gov.nasa.arc.mct.components.AbstractComponent;
 import gov.nasa.arc.mct.components.FeedProvider;
 import gov.nasa.arc.mct.gui.FeedView;
 import gov.nasa.arc.mct.roles.events.AddChildEvent;
 import gov.nasa.arc.mct.roles.events.ReloadEvent;
 import gov.nasa.arc.mct.roles.events.RemoveChildEvent;
 import gov.nasa.arc.mct.services.component.ViewInfo;
 
 import java.awt.BorderLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.AbstractTableModel;
 
 /**
  * This view displays the children of a component in a table format. It makes
  * use of the view properties to keep track of the number of children that will
  * be displayed for a particular component. When the number of children property
  * is changed, it is persisted to the database.
  * 
  */
 public class ShowChildrenInTableView extends FeedView {
 
 	private static final long serialVersionUID = 5541863528199833270L;
 	private static final String MAX_TABLE_ROW = "MAX_TABLE_ROW";
 	private ExampleTableModel tableModel;
 
 	private ControlPanel controlPanel;
 
 	public ShowChildrenInTableView(AbstractComponent ac, ViewInfo vi) {
 		super(ac,vi);
 		setLayout(new BorderLayout());
 
 		// Get the number of rows to be displayed from the view properties.
 		String maxTableRowStr = getViewProperties().getProperty(MAX_TABLE_ROW,
 				String.class);
 		int maxTableRow = 3;
 		if (maxTableRowStr != null) {
 			maxTableRow = Integer.parseInt(maxTableRowStr);
 		}
 
 		tableModel = new ExampleTableModel(maxTableRow);
 		JTable table = new JTable(tableModel);
 
 		controlPanel = new ControlPanel(tableModel, maxTableRow);
 		add(controlPanel, BorderLayout.NORTH);
		JScrollPane pane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		add(pane, BorderLayout.CENTER);
 	}
 
 	@Override
 	public Collection<FeedProvider> getVisibleFeedProviders() {
 		// this implementation could have been optimized to build the initial
 		// list of
 		// components in the constructor and then use the
 		// updateMonitoredGUI(AddChildEvent)
 		// and updateMonitoredGUI(RemoveChildEvent) to change the list saving
 		// the iterations
 		// during each rendering cycle
 
 		List<FeedProvider> feeds = new ArrayList<FeedProvider>(
 				getManifestedComponent().getComponents().size());
 		for (AbstractComponent childComp : getManifestedComponent().getComponents()) {
 			FeedProvider fp = getFeedProvider(childComp);
 			if (fp != null) {
 				feeds.add(fp);
 			}
 		}
 		return feeds;
 	}
 
 	@Override
 	public void synchronizeTime(Map<String, List<Map<String, String>>> data,
 			long syncTime) {
 		updateFromFeed(data);
 	}
 
 	// this method is called during the feed rendering cycle to update the table
 	// based on
 	// new feed data. This method will always be called on the AWT thread, while
 	// the request
 	// will be done off the AWT thread so this method is intended to be
 	// efficient
 	@Override
 	public void updateFromFeed(Map<String, List<Map<String, String>>> data) {
 		// show only one data point
 		if (!data.isEmpty()) {
 			tableModel.setFeedData(data);
 			tableModel.refresh();
 		}
 	}
 
 	/**
 	 * This method will refresh the manifestation when the number of children to
 	 * be displayed is changed.
 	 */
 	@Override
 	public void updateMonitoredGUI() {
 		String maxTableRowStr = getViewProperties().getProperty(MAX_TABLE_ROW,
 				String.class);
 		if (maxTableRowStr != null) {
 			controlPanel.update(Integer.parseInt(maxTableRowStr));
 		}
 	}
 
 	/**
 	 * Refresh the table model in response to the change in children
 	 */
 	@Override
 	public void updateMonitoredGUI(AddChildEvent event) {
 		tableModel.refresh();
 	}
 
 	/**
 	 * Refresh the table model in response to the change in children
 	 */
 	@Override
 	public void updateMonitoredGUI(RemoveChildEvent event) {
 		tableModel.refresh();
 	}
 
 	/**
 	 * Refresh the table model in response to the change in children, this could
 	 * be caused by a aborted change
 	 */
 	@Override
 	public void updateMonitoredGUI(ReloadEvent event) {
 		tableModel.refresh();
 	}
 
 	@Override
 	public void enterLockedState() {
 		controlPanel.enableEdit();
 	}
 
 	@Override
 	public void exitLockedState() {
 		controlPanel.disableEdit();
 	}
 
 	/**
 	 * This inner class provides a panel which allows a user to specify the
 	 * number of children to be displayed in the table of a particular
 	 * component.
 	 * 
 	 */
 	private final class ControlPanel extends JPanel {
 		private static final long serialVersionUID = 6117168640139158850L;
 
 		private JLabel noOfRowsLabel = new JLabel("Num of Rows to display:");
 		private JLabel displayNoOfRowsLabel;
 		private JTextField editNoOfRowField;
 		private String maxTableRowStr;
 		private ExampleTableModel tableModel;
 
 		public ControlPanel(final ExampleTableModel tableModel, int maxTableRow) {
 			setLayout(new BorderLayout());
 			this.tableModel = tableModel;
 			this.maxTableRowStr = String.valueOf(maxTableRow);
 			displayNoOfRowsLabel = new JLabel(maxTableRowStr);
 			editNoOfRowField = new JTextField(maxTableRowStr);
 
 			editNoOfRowField.addFocusListener(new FocusListener() {
 
 				@Override
 				public void focusLost(FocusEvent e) {
 					String currentText = editNoOfRowField.getText().trim();
 					if (maxTableRowStr == null
 							|| !maxTableRowStr.equals(currentText)) {
 						maxTableRowStr = currentText;
 						tableModel.setMaxRowAndSave(Integer
 								.parseInt(currentText));
 					}
 				}
 
 				@Override
 				public void focusGained(FocusEvent e) {
 				}
 			});
 
 			add(noOfRowsLabel, BorderLayout.WEST);
 			add(editNoOfRowField, BorderLayout.CENTER);
 		}
 
 		public void update(int maxTableRow) {
 			String newMaxTableRowStr = String.valueOf(maxTableRow);
 			if (!this.maxTableRowStr.equals(newMaxTableRowStr)) {
 				displayNoOfRowsLabel.setText(newMaxTableRowStr);
 				editNoOfRowField.setText(newMaxTableRowStr);
 				this.maxTableRowStr = newMaxTableRowStr;
 				tableModel.setMaxRow(maxTableRow);
 			}
 		}
 
 		public void enableEdit() {
 			remove(displayNoOfRowsLabel);
 			add(editNoOfRowField, BorderLayout.CENTER);
 		}
 
 		public void disableEdit() {
 			String newMaxTableRowStr = getViewProperties().getProperty(
 					MAX_TABLE_ROW, String.class);
 			if (newMaxTableRowStr != null) {
 				editNoOfRowField.setText(newMaxTableRowStr);
 				displayNoOfRowsLabel.setText(newMaxTableRowStr);
 			}
 
 			if (!maxTableRowStr.equals(newMaxTableRowStr)
 					&& newMaxTableRowStr != null) {
 				this.maxTableRowStr = newMaxTableRowStr;
 				tableModel.setMaxRow(Integer.parseInt(maxTableRowStr));
 			}
 
 			remove(editNoOfRowField);
 			add(displayNoOfRowsLabel, BorderLayout.CENTER);
 		}
 	}
 
 	/**
 	 * A simple table model for the table.
 	 * 
 	 */
 	private class ExampleTableModel extends AbstractTableModel {
 		private static final long serialVersionUID = 866084276868380575L;
 
 		private String[] columnNames = { "Component id", "Display Name",
				"Component Type", "Feed Data", "Shared" };
 		private Object[][] data;
 		private int maxRow;
 
 		private Map<String, List<Map<String, String>>> feedData = Collections
 				.<String, List<Map<String, String>>> emptyMap();
 
 		public ExampleTableModel(int maxRow) {
 			this.maxRow = maxRow;
 			populateData();
 		}
 
 		private void populateData() {
 			AbstractComponent parentComp = getManifestedComponent();
 
 			int numOfChildren = 0;
 			boolean hasChildren = !parentComp.getComponents().isEmpty();
 			if (hasChildren) {
 				numOfChildren = maxRow < parentComp.getComponents().size() ? 
 						maxRow : 
 						parentComp.getComponents().size();
 			}
 
 			data = new Object[numOfChildren][columnNames.length];
 
 			if (numOfChildren > 0) {
 				Iterator<AbstractComponent> children = parentComp.getComponents().iterator();
 				for (int i = 0; i < numOfChildren; i++) {
 					AbstractComponent childComp = children.next();
 					data[i][0] = childComp.getId();
 					data[i][1] = childComp.getDisplayName();
					data[i][2] = childComp.getClass().getName();
					List<Map<String, String>> feedVal = feedData.get(childComp
							.getDisplayName());
 					if (feedVal != null && !feedVal.isEmpty()) {
 						Map<String, String> valMap = feedVal.get(0);

						String feedData = valMap.get("telemetry.value");
 						data[i][3] = feedData == null ? "" : feedData;
 					} else {
 						data[i][3] = "";
 					}
 				}
 			}
 		}
 
 		@Override
 		public int getColumnCount() {
 			return columnNames.length;
 		}
 
 		@Override
 		public int getRowCount() {
 			return data.length;
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			return data[rowIndex][columnIndex];
 		}
 
 		@Override
 		public Class<?> getColumnClass(int columnIndex) {
			return getValueAt(0, columnIndex).getClass();
 		}
 
 		@Override
 		public String getColumnName(int column) {
 			return columnNames[column];
 		}
 
 		public void setFeedData(Map<String, List<Map<String, String>>> feedData) {
 			this.feedData = feedData;
 		}
 
 		public void refresh() {
 			populateData();
 			fireTableStructureChanged();
 		}
 
 		public void setMaxRow(int maxRow) {
 			if (this.maxRow != maxRow) {
 				this.maxRow = maxRow;
 				refresh();
 			}
 		}
 
 		/**
 		 * Set the maximum number of rows to be displayed and persist the
 		 * MAX_TABLE_ROW view property to the database.
 		 * 
 		 * @param maxRow
 		 *            The maximum number of rows to be displayed
 		 */
 		public void setMaxRowAndSave(int maxRow) {
 			if (this.maxRow != maxRow) {
 				setMaxRow(maxRow);
 				getViewProperties().setProperty(MAX_TABLE_ROW,
 						String.valueOf(maxRow));
 				getManifestedComponent().save();
 			}
 		}
 	}
 }
