 package ho.module.lineup.substitution;
 
 import ho.core.gui.theme.HOIconName;
 import ho.core.gui.theme.ThemeManager;
 import ho.core.model.HOVerwaltung;
 import ho.core.util.GUIUtilities;
 import ho.module.lineup.Lineup;
 import ho.module.lineup.substitution.model.MatchOrderType;
 import ho.module.lineup.substitution.model.Substitution;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 
 public class SubstitutionOverview extends JPanel {
 
 	private static final long serialVersionUID = -625638866350314110L;
 	private JTable substitutionTable;
 	private DetailsView detailsView;
 	private EditAction editAction;
 	private RemoveAction removeAction;
 	private Lineup lineup;
 
 	public SubstitutionOverview(Lineup lineup) {
 		this.lineup = lineup;
 		createActions();
 		initComponents();
 		addListeners();
 		refresh();
 
 		if (this.substitutionTable.getRowCount() > 0) {
 			this.substitutionTable.getSelectionModel().setSelectionInterval(0, 0);
 		}
 	}
 
 	public void setLineup(Lineup lineup) {
 		this.lineup = lineup;
 		if (this.substitutionTable.getRowCount() > 0) {
 			this.substitutionTable.getSelectionModel().setSelectionInterval(0, 0);
 		}
 		refresh();
 	}
 
 	private void createActions() {
 		this.editAction = new EditAction();
 		this.editAction.setEnabled(false);
 		this.removeAction = new RemoveAction();
 		this.removeAction.setEnabled(false);
 	}
 
 	private void refresh() {
 		SubstitutionsTableModel model = (SubstitutionsTableModel) this.substitutionTable.getModel();
 		model.setData(this.lineup.getSubstitutionList());
 
 		for (int i = 0; i < model.getRowCount(); i++) {
 			TableRow row = model.getRow(i);
 			if (!PlausibilityCheck.areBothPlayersInLineup(this.lineup, row.getSub())) {
 				row.setError(true);
 			}
 		}
 	}
 
 	private void addListeners() {
 		this.substitutionTable.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 
 					@Override
 					public void valueChanged(ListSelectionEvent e) {
 						if (!e.getValueIsAdjusting()) {
 							tableSelectionChanged();
 						}
 					}
 				});
 	}
 
 	private void tableSelectionChanged() {
 		int selectedRowIndex = this.substitutionTable.getSelectedRow();
 		boolean enable = false;
 		Substitution substitution = null;
 		if (selectedRowIndex != -1) {
 			substitution = ((SubstitutionsTableModel) this.substitutionTable.getModel())
 					.getSubstitution(selectedRowIndex);
 			enable = true;
 		}
 		this.editAction.setEnabled(enable);
 		this.removeAction.setEnabled(enable);
 		this.detailsView.setSubstitution(substitution);
 	}
 
 	private void initComponents() {
 		setLayout(new BorderLayout());
 		this.substitutionTable = new JTable();
 		this.substitutionTable.setModel(new SubstitutionsTableModel());
 		this.substitutionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane tableScrollPane = new JScrollPane();
 		tableScrollPane.getViewport().add(this.substitutionTable);
 		this.substitutionTable.getColumnModel().getColumn(0).setCellRenderer(new WarningRenderer());
 		this.substitutionTable.getColumnModel().getColumn(0).setPreferredWidth(25);
 		this.substitutionTable.getColumnModel().getColumn(0).setMaxWidth(25);
 
 		this.detailsView = new DetailsView();
 		add(this.detailsView, BorderLayout.SOUTH);
 
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
 		splitPane.add(tableScrollPane, 0);
 		splitPane.add(this.detailsView, 1);
 		splitPane.setDividerLocation(160);
 		add(splitPane, BorderLayout.CENTER);
 
 		JPanel buttonPanel = new JPanel(new GridBagLayout());
 
 		JButton editButton = new JButton();
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		gbc.anchor = GridBagConstraints.NORTHWEST;
 		gbc.insets = new Insets(10, 10, 2, 10);
 		editButton.setAction(this.editAction);
 		buttonPanel.add(editButton, gbc);
 
 		JButton removeButton = new JButton();
 		gbc.gridy++;
 		gbc.insets = new Insets(2, 10, 10, 10);
 		removeButton.setAction(this.removeAction);
 		buttonPanel.add(removeButton, gbc);
 
 		JButton substitutionButton = new JButton();
 		gbc.gridy++;
 		gbc.insets = new Insets(10, 10, 2, 10);
 		buttonPanel.add(substitutionButton, gbc);
 		substitutionButton.setAction(new SubstitutionAction());
 
 		JButton behaviorButton = new JButton();
 		gbc.gridy++;
 		gbc.insets = new Insets(2, 10, 2, 10);
 		buttonPanel.add(behaviorButton, gbc);
 		behaviorButton.setAction(new BehaviorAction());
 
 		JButton positionSwapButton = new JButton();
 		gbc.gridy++;
 		gbc.insets = new Insets(2, 10, 2, 10);
 		gbc.weighty = 1.0;
 		buttonPanel.add(positionSwapButton, gbc);
 		positionSwapButton.setAction(new PositionSwapAction());
 
 		add(buttonPanel, BorderLayout.EAST);
 
 		GUIUtilities.equalizeComponentSizes(editButton, removeButton, substitutionButton,
 				behaviorButton, positionSwapButton);
 	}
 
 	private void doNewOrder(MatchOrderType orderType) {
 		SubstitutionEditDialog dlg = getSubstitutionEditDialog(orderType);
 		dlg.setLocationRelativeTo(SubstitutionOverview.this);
 		dlg.setVisible(true);
 
 		if (!dlg.isCanceled()) {
 			Substitution sub = dlg.getSubstitution();
 			this.lineup.getSubstitutionList().add(sub);
 			SubstitutionsTableModel model = (SubstitutionsTableModel) this.substitutionTable
 					.getModel();
 			int idx = model.getRowCount() - 1;
 			model.fireTableRowsInserted(idx, idx);
 
 			for (int i = 0; i < model.getRowCount(); i++) {
 				if (model.getSubstitution(i) == sub) {
 					this.substitutionTable.getSelectionModel().setSelectionInterval(i, i);
 				}
 			}
 
 		}
 	}
 
 	private SubstitutionEditDialog getSubstitutionEditDialog(MatchOrderType orderType) {
 		SubstitutionEditDialog dlg = null;
 		Window windowAncestor = SwingUtilities.getWindowAncestor(SubstitutionOverview.this);
 		if (windowAncestor instanceof Frame) {
 			dlg = new SubstitutionEditDialog((Frame) windowAncestor, orderType);
 		} else {
 			dlg = new SubstitutionEditDialog((Dialog) windowAncestor, orderType);
 		}
 		return dlg;
 	}
 
 	/**
 	 * TableModel for the overview table where existing substitutions are
 	 * listed.
 	 * 
 	 */
 	private class SubstitutionsTableModel extends AbstractTableModel {
 
 		private static final long serialVersionUID = 6969656858380680460L;
 		private List<TableRow> rows = new ArrayList<TableRow>();
 		private String[] columnNames = new String[] { "", "Order", "When", "Standing", "Red cards" };
 
 		public Substitution getSubstitution(int rowIndex) {
 			return this.rows.get(rowIndex).getSub();
 		}
 
 		public void setData(List<Substitution> data) {
 			this.rows.clear();
 			for (Substitution sub : data) {
 				TableRow row = new TableRow();
 				row.setSub((Substitution) sub);
 				this.rows.add(row);
 			}
 			fireTableDataChanged();
 		}
 
 		public void select(Substitution substitution) {
 			for (int i = 0; i < this.rows.size(); i++) {
 
 			}
 		}
 
 		@Override
 		public int getRowCount() {
 			return this.rows.size();
 		}
 
 		@Override
 		public int getColumnCount() {
 			return this.columnNames.length;
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			Substitution sub = this.rows.get(rowIndex).getSub();
 			switch (columnIndex) {
 			case 1:
 				return LanguageStringLookup.getOrderType(sub.getOrderType());
 			case 2:
 				if (sub.getMatchMinuteCriteria() > 0) {
 					return MessageFormat.format(
 							HOVerwaltung.instance().getLanguageString("subs.MinuteAfterX"),
 							Integer.valueOf(sub.getMatchMinuteCriteria()));
 				}
 				return HOVerwaltung.instance().getLanguageString("subs.MinuteAnytime");
 			case 3:
 				return LanguageStringLookup.getStanding(sub.getStanding());
 			case 4:
 				return LanguageStringLookup.getRedCard(sub.getRedCardCriteria());
 			}
 
 			return "";
 		}
 
 		@Override
 		public String getColumnName(int column) {
 			return this.columnNames[column];
 		}
 
 		public TableRow getRow(int rowIndex) {
 			return this.rows.get(rowIndex);
 		}
 	}
 
 	/*
 	 * This class is a simple container for row data.
 	 */
 	private class TableRow {
 		private Substitution sub;
 		private boolean critical;
 		private boolean error;
 
 		public Substitution getSub() {
 			return sub;
 		}
 
 		public void setSub(Substitution sub) {
 			this.sub = sub;
 		}
 
 		public boolean isCritical() {
 			return critical;
 		}
 
 		public void setCritical(boolean critical) {
 			this.critical = critical;
 		}
 
 		public boolean isError() {
 			return error;
 		}
 
 		public void setError(boolean error) {
 			this.error = error;
 		}
 	}
 
 	private class BehaviorAction extends AbstractAction {
 
 		private static final long serialVersionUID = 3753611559396928213L;
 
 		public BehaviorAction() {
 			super(HOVerwaltung.instance().getLanguageString("subs.Behavior"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			doNewOrder(MatchOrderType.NEW_BEHAVIOUR);
 		}
 	}
 
 	private class PositionSwapAction extends AbstractAction {
 
 		private static final long serialVersionUID = 3753611559396928213L;
 
 		public PositionSwapAction() {
 			super(HOVerwaltung.instance().getLanguageString("subs.TypeSwap"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			doNewOrder(MatchOrderType.POSITION_SWAP);
 		}
 	}
 
 	private class SubstitutionAction extends AbstractAction {
 
 		private static final long serialVersionUID = 2005264416271904159L;
 
 		public SubstitutionAction() {
 			super(HOVerwaltung.instance().getLanguageString("subs.TypeSub"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			doNewOrder(MatchOrderType.SUBSTITUTION);
 		}
 	}
 
 	private class RemoveAction extends AbstractAction {
 
 		private static final long serialVersionUID = 715531467612457L;
 
 		public RemoveAction() {
 			super(HOVerwaltung.instance().getLanguageString("subs.Remove"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
			int selectedRowIndex = substitutionTable.getSelectedRow();
			Substitution sub = ((SubstitutionsTableModel) substitutionTable.getModel())
					.getSubstitution(selectedRowIndex);

			lineup.getSubstitutionList().remove(sub);
			((SubstitutionsTableModel) substitutionTable.getModel()).fireTableRowsDeleted(
					selectedRowIndex, selectedRowIndex);
 			detailsView.refresh();
 		}
 	}
 
 	private class EditAction extends AbstractAction {
 
 		private static final long serialVersionUID = 715531467677812457L;
 
 		public EditAction() {
 			super(HOVerwaltung.instance().getLanguageString("subs.Edit"));
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			int selectedRowIndex = substitutionTable.getSelectedRow();
 			final Substitution sub = ((SubstitutionsTableModel) substitutionTable.getModel())
 					.getSubstitution(selectedRowIndex);
 
 			SubstitutionEditDialog dlg = getSubstitutionEditDialog(sub.getOrderType());
 			dlg.setLocationRelativeTo(SubstitutionOverview.this);
 			dlg.init(sub);
 			dlg.setVisible(true);
 
 			if (!dlg.isCanceled()) {
 				sub.merge(dlg.getSubstitution());
 				((SubstitutionsTableModel) substitutionTable.getModel()).fireTableRowsUpdated(
 						selectedRowIndex, selectedRowIndex);
 				detailsView.refresh();
 			}
 		}
 	}
 
 	private class WarningRenderer extends DefaultTableCellRenderer {
 
 		private static final long serialVersionUID = 7013869782046646283L;
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value,
 				boolean isSelected, boolean hasFocus, int row, int column) {
 
 			JLabel component = (JLabel) super.getTableCellRendererComponent(table, value,
 					isSelected, hasFocus, row, column);
 			SubstitutionsTableModel tblModel = (SubstitutionsTableModel) table.getModel();
 			TableRow tblRow = tblModel.getRow(row);
 			if (tblRow.isCritical()) {
 				component.setIcon(ThemeManager.getIcon(HOIconName.EXCLAMATION));
 			} else if (tblRow.isError()) {
 				component.setIcon(ThemeManager.getIcon(HOIconName.EXCLAMATION_RED));
 			} else {
 				component.setIcon(null);
 			}
 			return component;
 		}
 	}
 }
