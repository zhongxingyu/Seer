 package com.ghlh.ui.autotrade.stocksetting;
 
 import java.awt.BorderLayout;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 
 import org.apache.log4j.Logger;
 
 import com.ghlh.data.db.MonitorstockDAO;
 import com.ghlh.data.db.MonitorstockVO;
 import com.ghlh.strategy.AdditionInfoUtil;
 import com.ghlh.ui.AbstractButtonActionListener;
 import com.ghlh.ui.autotrade.AbstractContentPanel;
 import com.ghlh.ui.autotrade.Constants;
 import com.ghlh.ui.autotrade.ContentPanelUtil;
 import com.ghlh.ui.autotrade.StockIdFieldFocusListener;
 import com.ghlh.ui.bean.ComponentsBean;
 import com.ghlh.ui.bean.UIComponentMetadata;
 import com.ghlh.ui.bean.UIComponentType;
 import com.ghlh.ui.gtable.ComboRenderer;
 import com.ghlh.ui.gtable.EditableHeader;
 import com.ghlh.ui.gtable.EditableHeaderTableColumn;
 
 public class StockSettingContentPanel extends AbstractContentPanel {
 	private static Logger logger = Logger
 			.getLogger(StockSettingContentPanel.class);
 
 	private JTable stockTable = null;
 
 	public StockSettingContentPanel(ComponentsBean compomentBean) {
 		this.content.setLayout(new BorderLayout());
 		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		this.content.add(splitPanel, BorderLayout.CENTER);
 		JPanel panel1 = new JPanel();
 		this.addOneComponentEditor(compomentBean, panel1);
 		splitPanel.add(panel1);
 		initTable();
 		JScrollPane scrollPane = new JScrollPane(stockTable);
 		splitPanel.add(scrollPane);
 	}
 
 	private void initTable() {
 		stockTable = new JTable();
 		stockTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		stockTable.setFillsViewportHeight(true);
 		stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		stockTable.setRowSelectionAllowed(true);
 		stockTable.setColumnSelectionAllowed(false);
 		stockTable.setModel(new DefaultTableModel() {
 			public boolean isCellEditable(int row, int column) {
 				if (column == 3) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 
 			public Class getColumnClass(int c) {
 				return getValueAt(0, c).getClass();
 			}
 		});
 
 		initTableData();
 		initComboBoxForStrategyHeader();
 
 		stockTable.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 					public void valueChanged(ListSelectionEvent event) {
						tableRowSelectionActionPerformed(event);
 					}
 				});
 		stockTable.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				clickMonitoring();
 			}
 		});
 	}
 
 	private void switchStragetegy(String strategyLabel) {
 		if (!strategyLabel.equals(currentStrategyLabel)) {
 			this.currentStrategyLabel = strategyLabel;
 			if (strategyLabel.equals(Constants.ALL_STRATEGIES_LABEL)) {
 				currentStrategyName = Constants.ALL_STRATEGIES_NAME;
 			} else {
 				String strategyName = StockSettingUICompomentsImpl
 						.getStrategy(strategyLabel);
 				currentStrategyName = strategyName;
 			}
 			refreshStockTable();
 		}
 	}
 
 	private String currentStrategyLabel = null;
 	private String currentStrategyName = null;
 
 	private void initComboBoxForStrategyHeader() {
 		List comboList = new StockSettingUICompomentsImpl().getStrateNameList();
 		comboList.add(0, Constants.ALL_STRATEGIES_LABEL);
 		currentStrategyLabel = Constants.ALL_STRATEGIES_LABEL;
 		currentStrategyName = Constants.ALL_STRATEGIES_NAME;
 		String[] items = (String[]) comboList.toArray(new String[0]);
 		JComboBox strategyComboHeader = new JComboBox();
 		for (int i = 0; i < items.length; i++) {
 			strategyComboHeader.addItem(items[i]);
 		}
 		strategyComboHeader.setSelectedIndex(0);
 		strategyComboHeader.addItemListener(new ItemListener() {
 			public void itemStateChanged(java.awt.event.ItemEvent ie) {
 				String strategyLabel = ((JComboBox) ie.getSource())
 						.getSelectedItem().toString();
 				switchStragetegy(strategyLabel);
 			}
 		});
 		ComboRenderer renderer = new ComboRenderer(items);
 
 		TableColumnModel columnModel = stockTable.getColumnModel();
 		stockTable.setTableHeader(new EditableHeader(columnModel));
 		EditableHeaderTableColumn col = (EditableHeaderTableColumn) stockTable
 				.getColumnModel().getColumn(0);
 		col.setHeaderValue(strategyComboHeader.getItemAt(0));
 		col.setHeaderRenderer(renderer);
 		col.setHeaderEditor(new DefaultCellEditor(strategyComboHeader));
 	}
 
 	List<MonitorstockVO> msbList = null;
 
 	public void refreshUIMsbList(List<MonitorstockVO> msbList) {
 		this.msbList = msbList;
 	}
 
 	private void clickMonitoring() {
 		int row = stockTable.getSelectedRow();
 		if (stockTable.getSelectedColumn() == 3) {
 			boolean monitoring = ((Boolean) stockTable.getValueAt(row, 3))
 					.booleanValue();
 			// String action = null;
 			// if (monitoring) {
 			// action = "";
 			// } else {
 			// action = "ȡ";
 			// }
 			// String message = "ȷ" + action + "ƱԶ׼?";
 			// String title = "ȷ" + action + "ƱԶ";
 			// int confirm = GUIUtil.showConfirmDialog(message, title);
 			// if (confirm == 0) {
 			((StockSettingButtonActionListener) this.getBal())
 					.updateMonitoring(monitoring);
 			MonitorstockVO currentMsb = msbList.get(row);
 			currentMsb.setOnmonitoring(monitoring + "");
 			// } else {
 			// this.stockTable.setValueAt(new Boolean(!monitoring), row, 3);
 			// }
 		}
 	}
 
 	public boolean isSelectedMSBOnMonitoring() {
 		int row = stockTable.getSelectedRow();
 		boolean result = false;
 		if (row >= 0) {
 			result = ((Boolean) stockTable.getValueAt(row, 3)).booleanValue();
 		}
 		return result;
 	}
 
 	private void initTableData() {
 		String[] columnNames = { "ײ", "Ʊ", "Ʊ", "Ƿ" };
 		((DefaultTableModel) this.stockTable.getModel())
 				.setColumnIdentifiers(columnNames);
 
 		try {
 			msbList = MonitorstockDAO.getMonitorStock();
 			for (int i = 0; i < msbList.size(); i++) {
 				MonitorstockVO msb = msbList.get(i);
 				Vector rowV = convertMonitorStockBeanToVector(msb);
 				((DefaultTableModel) this.stockTable.getModel()).addRow(rowV);
 			}
 		} catch (Exception ex) {
 			logger.error("Read stock list throw", ex);
 		}
 	}
 
 	private boolean onlyMonitoringStatus = false;
 
 	public void refreshStockTable(boolean onlyMonitoring) {
 		onlyMonitoringStatus = onlyMonitoring;
 		refreshStockTable();
 	}
 
 	private void refreshStockTable() {
 		try {
 			initMonitorStockList();
 			int rowCount = ((DefaultTableModel) this.stockTable.getModel())
 					.getRowCount();
 			for (int i = 0; i < rowCount; i++) {
 				((DefaultTableModel) this.stockTable.getModel()).removeRow(0);
 			}

 			for (int i = 0; i < msbList.size(); i++) {
 				MonitorstockVO msb = msbList.get(i);
 				Vector rowV = convertMonitorStockBeanToVector(msb);
 				((DefaultTableModel) this.stockTable.getModel()).addRow(rowV);
 			}
 			((DefaultTableModel) this.stockTable.getModel())
 					.fireTableDataChanged();
 		} catch (Exception ex) {
 			logger.error("Read stock list throw", ex);
 		}
 	}
 
 	private void initMonitorStockList() {
 		if (onlyMonitoringStatus) {
 			if (this.currentStrategyName
 					.equals(Constants.ALL_STRATEGIES_NAME)) {
 				msbList = MonitorstockDAO.getOnlyMonitoringStocks();
 			} else {
 				msbList = MonitorstockDAO
 						.getOnlyMonitoringStocks(currentStrategyName);
 			}
 		} else {
 			if (this.currentStrategyName
 					.equals(Constants.ALL_STRATEGIES_NAME)) {
 				msbList = MonitorstockDAO.getMonitorStock();
 			} else {
 				msbList = MonitorstockDAO
 						.getMonitorStock(currentStrategyName);
 			}
 		}
 	}
 
 	public void addStockInTable(MonitorstockVO msb) {
 		msbList.add(msb);
 		Vector rowV = convertMonitorStockBeanToVector(msb);
 		((DefaultTableModel) this.stockTable.getModel()).addRow(rowV);
 		((DefaultTableModel) this.stockTable.getModel()).fireTableDataChanged();
 	}
 
 	public void updateStockInTableRow(MonitorstockVO msb) {
 		int currentRow = this.stockTable.getSelectedRow();
 		if (currentRow >= 0) {
 			msbList.set(currentRow, msb);
 			updateTableRow(msb, currentRow);
 			((DefaultTableModel) this.stockTable.getModel())
 					.fireTableDataChanged();
 		}
 	}
 
 	public void deleteStockInTableRow() {
 		int currentRow = this.stockTable.getSelectedRow();
 		if (currentRow >= 0) {
 			this.msbList.remove(currentRow);
 			((DefaultTableModel) this.stockTable.getModel())
 					.removeRow(currentRow);
 			((DefaultTableModel) this.stockTable.getModel())
 					.fireTableDataChanged();
 		}
 	}
 
 	private void updateTableRow(MonitorstockVO msb, int currentRow) {
 		((DefaultTableModel) this.stockTable.getModel()).setValueAt(
 				StockSettingUICompomentsImpl.getStrategyName(msb
 						.getTradealgorithm()), currentRow, 0);
 		((DefaultTableModel) this.stockTable.getModel()).setValueAt(
 				msb.getStockid(), currentRow, 1);
 		((DefaultTableModel) this.stockTable.getModel()).setValueAt(
 				msb.getName(), currentRow, 2);
 	}
 
 	public void notSelectRow() {
 		if (this.stockTable != null) {
 			((DefaultTableModel) this.stockTable.getModel())
 					.fireTableDataChanged();
 		}
 	}
 
 	private Vector convertMonitorStockBeanToVector(MonitorstockVO msb) {
 		Vector rowV = new Vector();
 		rowV.add(StockSettingUICompomentsImpl.getStrategyName(msb
 				.getTradealgorithm()));
 		rowV.add(msb.getStockid());
 		rowV.add(msb.getName());
 		rowV.add(new Boolean(msb.getOnmonitoring()));
 		return rowV;
 	}
 
 	public void tableRowSelectionActionPerformed(ListSelectionEvent event) {
 		int selectRow = this.stockTable.getSelectedRow();
 		if (selectRow >= 0) {
 			int currentRow = ((StockSettingButtonActionListener) this.getBal())
 					.getCurrentRow();
 			if (selectRow != currentRow) {
 //				int confirm = ((StockSettingButtonActionListener) this.getBal())
 //						.confirmInputOrChange();
 //				System.out.println("confirm = " + confirm);
 //				if (confirm == 0 || confirm == -1) {
 					putCertainMSBIntoEdit(selectRow);
 					MonitorstockVO msb = msbList.get(selectRow);
 					((StockSettingButtonActionListener) this.getBal())
 							.enterEditStatus(msb, selectRow);
 //				} else {
 //					currentRow = ((StockSettingButtonActionListener) this
 //							.getBal()).getCurrentRow();
 //					if (currentRow == -1) {
 //						this.notSelectRow();
 //					} else {
 //						this.stockTable.getSelectionModel()
 //								.setSelectionInterval(currentRow, currentRow);
 //					}
 //
 //				}
 			}
 		} else {
 			this.notSelectRow();
 		}
 	}
 
 	public void resumeEditStatus() {
 		int selectRow = this.stockTable.getSelectedRow();
 		putCertainMSBIntoEdit(selectRow);
 
 	}
 
 	private void putCertainMSBIntoEdit(int selectRow) {
 		String strategyName = this.stockTable.getValueAt(selectRow, 0)
 				.toString();
 		MonitorstockVO msb = msbList.get(selectRow);
 		String currentStrategyName = ((JComboBox) this.getUIcomponents().get(0))
 				.getSelectedItem().toString();
 		if (!strategyName.equals(currentStrategyName)) {
 			((JComboBox) this.getUIcomponents().get(0))
 					.setSelectedItem(strategyName);
 			this.switchTradeStrategy(strategyName);
 		}
 		setValueToBasicUIComponents(msb);
 		AdditionInfoUtil.setAdditionalInfoToUIComponents(
 				this.getUIcomponents(), msb.getAdditioninfo(),
 				msb.getTradealgorithm());
 	}
 
 	private void setValueToBasicUIComponents(MonitorstockVO msb) {
 		((JTextField) this.getUIcomponents().get(1)).setText(msb.getStockid());
 		((JTextField) this.getUIcomponents().get(2)).setText(msb.getName());
 	}
 
 	public StockSettingContentPanel() {
 	}
 
 	private JPanel componentPanel = new JPanel();
 
 	public JPanel getComponentPanel() {
 		return componentPanel;
 	}
 
 	private void addOneComponentEditor(ComponentsBean compomentBean,
 			JPanel content) {
 		content.setLayout(new BorderLayout());
 		content.add(getButtonPanel(compomentBean.getButtons()),
 				BorderLayout.NORTH);
 
 		FlowLayout contentLayout = new FlowLayout();
 
 		JPanel flowLayoutPanel = new JPanel();
 		flowLayoutPanel.setLayout(contentLayout);
 		contentLayout.setAlignment(FlowLayout.LEFT);
 
 		this.componentPanel = getComponentsPanel(compomentBean);
 
 		flowLayoutPanel.add(this.componentPanel);
 		content.add(flowLayoutPanel, BorderLayout.CENTER);
 
 		initButtonActionListener(compomentBean, flowLayoutPanel);
 		JTextField stockIdField = (JTextField) this.getUIcomponents().get(1);
 		JTextField stockNameField = (JTextField) this.getUIcomponents().get(2);
 		stockIdField.addFocusListener(new StockIdFieldFocusListener(
 				stockNameField));
 
 	}
 
 	public JPanel getComponentsPanel(ComponentsBean componentBean) {
 
 		JPanel result = new JPanel();
 		result.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		result.setLayout(new GridBagLayout());
 
 		List additionalComponents = componentBean.getAdditionalCompoments();
 
 		List components = componentBean.getComponents();
 
 		for (int i = 0; i < components.size(); i++) {
 			GridBagConstraints c = getConstraints((i % 3) * 2, i / 3);
 			c.weightx = 0.5;
 			c.anchor = GridBagConstraints.NORTHEAST;
 			UIComponentMetadata com = (UIComponentMetadata) components.get(i);
 			JLabel label = new JLabel(com.getLabel());
 			result.add(label, c);
 			c = getConstraints((i % 3) * 2 + 1, i / 3);
 			JComponent component = ContentPanelUtil.getJComponent(com);
 			result.add(component, c);
 			this.getUIcomponents().add(component);
 			if (com.getCompomentType() == UIComponentType.COMBOX_FIELD) {
 				((JComboBox) component).addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						comboBoxActionPerformed(e);
 					}
 				});
 			}
 		}
 		basicEndPos = components.size();
 		addAdditionAWTComponents(result, additionalComponents, basicEndPos);
 		return result;
 	}
 
 	private int basicEndPos = 0;
 	private List additionalComponents;
 
 	private void addAdditionAWTComponents(JPanel result,
 			List additionalComponents, int basicEndPos) {
 		if (additionalComponents != null) {
 			this.additionalComponents = additionalComponents;
 			for (int i = 0; i < additionalComponents.size(); i++) {
 				int additionalPos = basicEndPos + i;
 				GridBagConstraints c = getConstraints((additionalPos % 3) * 2,
 						additionalPos / 3);
 				c.weightx = 0.5;
 				c.anchor = GridBagConstraints.NORTHEAST;
 				UIComponentMetadata com = (UIComponentMetadata) additionalComponents
 						.get(i);
 				JLabel label = new JLabel(com.getLabel());
 				result.add(label, c);
 				c = getConstraints((additionalPos % 3) * 2 + 1,
 						additionalPos / 3);
 				JComponent component = ContentPanelUtil.getJComponent(com);
 				result.add(component, c);
 				this.getUIcomponents().add(component);
 
 				additionalAWTComponents.add(label);
 				additionalAWTComponents.add(component);
 			}
 		}
 	}
 
 	private List additionalAWTComponents = new ArrayList();
 
 	public void comboBoxActionPerformed(ActionEvent e) {
 		try {
 			String strategy = ((JComboBox) e.getSource()).getSelectedItem()
 					.toString();
 			switchTradeStrategy(strategy);
 
 		} catch (Exception ex) {
 			logger.error("perform ComboBox Action throw exception:", ex);
 		}
 	}
 
 	public void switchTradeStrategy(String strategy) {
 		for (int i = 0; i < this.additionalAWTComponents.size(); i++) {
 			java.awt.Component component = (java.awt.Component) additionalAWTComponents
 					.get(i);
 			this.getComponentPanel().remove(component);
 			this.getUIcomponents().remove(component);
 			this.additionalAWTComponents.remove(i);
 			i--;
 		}
 		for (int i = 0; i < this.additionalComponents.size(); i++) {
 			UIComponentMetadata com = (UIComponentMetadata) additionalComponents
 					.get(i);
 			((AbstractButtonActionListener) this.getBal()).getComponents()
 					.remove(com);
 		}
 
 		List additionComponent = (new StockSettingUICompomentsImpl())
 				.getAdditionalComponent(strategy);
 		((AbstractButtonActionListener) this.getBal()).getComponents().addAll(
 				additionComponent);
 
 		this.addAdditionAWTComponents(this.getComponentPanel(),
 				additionComponent, basicEndPos);
 		((AbstractButtonActionListener) this.getBal()).setUICompoments(this
 				.getUIcomponents());
 
 		this.getComponentPanel().repaint();
 		this.getComponentPanel().validate();
 	}
 }
