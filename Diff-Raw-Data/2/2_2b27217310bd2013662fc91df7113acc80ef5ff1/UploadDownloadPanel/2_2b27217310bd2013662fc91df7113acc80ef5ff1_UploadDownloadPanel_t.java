 package ho.module.lineup;
 
 import ho.core.db.DBManager;
 import ho.core.gui.HOMainFrame;
 import ho.core.gui.theme.HOIconName;
 import ho.core.gui.theme.ThemeManager;
 import ho.core.model.HOVerwaltung;
 import ho.core.model.match.MatchKurzInfo;
 import ho.core.model.match.MatchType;
 import ho.core.net.OnlineWorker;
 import ho.core.util.GUIUtilities;
 import ho.core.util.HOLogger;
 import ho.core.util.XMLUtils;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class UploadDownloadPanel extends JPanel {
 
 	private static final long serialVersionUID = -5314050322847463180L;
 	private JTable matchesTable;
 	private JButton uploadButton;
 	private JButton downloadButton;
 	private JButton refreshButton;
 
 	public UploadDownloadPanel() {
 		initComponents();
 		addListeners();
 	}
 
 	private void initComponents() {
 		this.refreshButton = new JButton(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.refresh"));
 		this.refreshButton.setToolTipText(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.refresh.tooltip"));
 		this.downloadButton = new JButton(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.download"));
 		this.downloadButton.setToolTipText(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.download.tooltip"));
 		this.downloadButton.setEnabled(false);
 		this.uploadButton = new JButton(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.upload"));
 		this.uploadButton.setToolTipText(HOVerwaltung.instance().getLanguageString(
 				"lineup.upload.btn.upload.tooltip"));
 		this.uploadButton.setEnabled(false);
 
 		JPanel buttonPanel = new JPanel(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.insets = new Insets(10, 8, 4, 10);
 		buttonPanel.add(this.refreshButton, gbc);
 
 		gbc.gridy = 1;
 		gbc.insets = new Insets(4, 8, 4, 10);
 		buttonPanel.add(this.uploadButton, gbc);
 
 		gbc.gridy = 2;
 		gbc.insets = new Insets(4, 8, 10, 10);
 		gbc.weightx = 1.0;
 		buttonPanel.add(this.downloadButton, gbc);
 
 		GUIUtilities.equalizeComponentSizes(this.refreshButton, this.uploadButton,
 				this.downloadButton);
 
 		MatchesTableModel model = new MatchesTableModel(getMatchesFromDB());
 		this.matchesTable = new JTable();
 		this.matchesTable.setModel(model);
 		this.matchesTable.setAutoCreateRowSorter(true);
 		this.matchesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		// as default, sort by date
 		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
 		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
 		this.matchesTable.getRowSorter().setSortKeys(sortKeys);
 
 		// TODO use column identifiers instead of index
 		TableColumn matchTypeColumn = this.matchesTable.getColumnModel().getColumn(1);
 		matchTypeColumn.setCellRenderer(new MatchTypeCellRenderer());
 		matchTypeColumn.setMaxWidth(25);
 
 		// TODO use column identifiers instead of index
 		TableColumn ordersSetColumn = this.matchesTable.getColumnModel().getColumn(5);
 		ordersSetColumn.setCellRenderer(new OrdersSetCellRenderer());
 		ordersSetColumn.setMaxWidth(25);
 
 		setLayout(new GridBagLayout());
 		gbc = new GridBagConstraints();
 		gbc.insets = new Insets(10, 10, 10, 10);
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.weightx = 1.0;
 		gbc.weighty = 1.0;
 		add(new JScrollPane(this.matchesTable), gbc);
 
 		gbc = new GridBagConstraints();
 		gbc.insets = new Insets(10, 10, 10, 10);
 		gbc.gridx = 1;
 		gbc.anchor = GridBagConstraints.NORTH;
 		add(buttonPanel, gbc);
 	}
 
 	private List<MatchKurzInfo> getMatchesFromDB() {
 		MatchKurzInfo[] matches = DBManager.instance().getMatchesKurzInfo(
 				HOVerwaltung.instance().getModel().getBasics().getTeamId());
 
 		Timestamp today = new Timestamp(System.currentTimeMillis());
 		List<MatchKurzInfo> data = new ArrayList<MatchKurzInfo>();
 		for (MatchKurzInfo match : matches) {
 			if (match.getMatchDateAsTimestamp().after(today)) {
 				data.add(match);
 			}
 		}
 		return data;
 	}
 
 	private void upload() {
 		MatchKurzInfo match = getSelectedMatch();
 
 		String result = OnlineWorker.uploadMatchOrder(match.getMatchID(), match.getMatchTyp(),
 				HOVerwaltung.instance().getModel().getAufstellung());
 
 		int messageType = JOptionPane.PLAIN_MESSAGE;
 		String message = null;
 		try {
 			Document doc = XMLUtils.createDocument(result);
 			String successStr = XMLUtils.getAttributeValueFromNode(doc, "MatchData", "OrdersSet");
 			if (successStr != null) {
 				boolean success = Boolean.parseBoolean(successStr);
 				if (success) {
 					messageType = JOptionPane.PLAIN_MESSAGE;
 					message = HOVerwaltung.instance().getLanguageString("lineup.upload.success");
 				} else {
 					messageType = JOptionPane.ERROR_MESSAGE;
 					message = HOVerwaltung.instance().getLanguageString("lineup.upload.fail")
 							+ XMLUtils.getTagData(doc, "Reason");
 				}
 			} else {
 				messageType = JOptionPane.ERROR_MESSAGE;
 				message = HOVerwaltung.instance().getLanguageString(
 						"lineup.upload.result.parseerror");
 				HOLogger.instance().log(UploadDownloadPanel.class, message + "\n" + result);
 			}
 		} catch (SAXException e) {
 			messageType = JOptionPane.ERROR_MESSAGE;
 			message = HOVerwaltung.instance().getLanguageString("lineup.upload.result.parseerror");
 			HOLogger.instance().log(UploadDownloadPanel.class, message + "\n" + result);
 			HOLogger.instance().log(UploadDownloadPanel.class, e);
 		}
 
 		JOptionPane.showMessageDialog(HOMainFrame.instance(), message, HOVerwaltung.instance()
 				.getLanguageString("lineup.upload.title"), messageType);
 	}
 
 	private void download() {
 		MatchKurzInfo match = getSelectedMatch();
 		OnlineWorker.downloadMatchData(match.getMatchID(), match.getMatchTyp(), true);
 	}
 
 	private void refreshMatchListFromHT() {
 		OnlineWorker.getMatches(HOVerwaltung.instance().getModel().getBasics().getTeamId(), true,
				true, true);
 		((MatchesTableModel) this.matchesTable.getModel()).setData(getMatchesFromDB());
 	}
 
 	private MatchKurzInfo getSelectedMatch() {
 		int tableIndex = this.matchesTable.getSelectedRow();
 		if (tableIndex != -1) {
 			int modelIndex = this.matchesTable.convertRowIndexToModel(tableIndex);
 			return ((MatchesTableModel) this.matchesTable.getModel()).getMatch(modelIndex);
 		}
 		return null;
 	}
 
 	private void addListeners() {
 		this.uploadButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				upload();
 			}
 		});
 
 		this.refreshButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				refreshMatchListFromHT();
 			}
 		});
 
 		this.downloadButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				download();
 			}
 		});
 
 		this.matchesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting()) {
 					boolean enableButtons = matchesTable.getSelectedRow() != -1;
 					uploadButton.setEnabled(enableButtons);
 					downloadButton.setEnabled(enableButtons);
 				}
 			}
 		});
 	}
 
 	private class MatchesTableModel extends AbstractTableModel {
 
 		private static final long serialVersionUID = 7917970964575188677L;
 		private List<MatchKurzInfo> data;
 		private String[] columns;
 
 		public MatchesTableModel() {
 			initColumnNames();
 			this.data = new ArrayList<MatchKurzInfo>();
 		}
 
 		public MatchesTableModel(List<MatchKurzInfo> list) {
 			initColumnNames();
 			this.data = new ArrayList<MatchKurzInfo>(list);
 		}
 
 		public MatchKurzInfo getMatch(int modelRowIndex) {
 			return this.data.get(modelRowIndex);
 		}
 
 		public void setData(List<MatchKurzInfo> list) {
 			this.data = new ArrayList<MatchKurzInfo>(list);
 			fireTableDataChanged();
 		}
 
 		@Override
 		public int getRowCount() {
 			return data.size();
 		}
 
 		@Override
 		public int getColumnCount() {
 			return columns.length;
 		}
 
 		@Override
 		public String getColumnName(int column) {
 			return columns[column];
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			MatchKurzInfo match = this.data.get(rowIndex);
 			switch (columnIndex) {
 			case 0:
 				return match.getMatchDate();
 			case 1:
 				return match.getMatchTyp();
 			case 2:
 				return match.getHeimName();
 			case 3:
 				return match.getGastName();
 			case 4:
 				return match.getMatchID();
 			case 5:
 				return match.isOrdersGiven();
 			}
 			return null;
 		}
 
 		private void initColumnNames() {
 			this.columns = new String[6];
 			this.columns[0] = HOVerwaltung.instance().getLanguageString(
 					"lineup.upload.colheadline.date");
 			this.columns[1] = "";
 			this.columns[2] = HOVerwaltung.instance().getLanguageString(
 					"lineup.upload.colheadline.hometeam");
 			this.columns[3] = HOVerwaltung.instance().getLanguageString(
 					"lineup.upload.colheadline.awayteam");
 			this.columns[4] = HOVerwaltung.instance().getLanguageString(
 					"lineup.upload.colheadline.matchid");
 			this.columns[5] = "";
 		}
 	}
 
 	private class MatchTypeCellRenderer extends DefaultTableCellRenderer {
 
 		private static final long serialVersionUID = -6068887874289410058L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value,
 				boolean isSelected, boolean hasFocus, int row, int column) {
 			JLabel component = (JLabel) super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 			component.setText(null);
 			MatchType type = (MatchType) value;
 			Icon icon = ThemeManager.getIcon(HOIconName.MATCHTYPES[type.getIconArrayIndex()]);
 			component.setIcon(icon);
 			return component;
 		}
 	}
 
 	private class OrdersSetCellRenderer extends DefaultTableCellRenderer {
 
 		private static final long serialVersionUID = -6068887874289410058L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value,
 				boolean isSelected, boolean hasFocus, int row, int column) {
 			JLabel component = (JLabel) super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 			component.setText(null);
 			if ((Boolean) value) {
 				component.setIcon(ThemeManager.getIcon(HOIconName.ORDER_SET));
 			} else {
 				component.setIcon(null);
 			}
 			return component;
 		}
 	}
 }
