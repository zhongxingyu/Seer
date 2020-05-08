 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 
 import model.BmtTask;
 import model.BntTask;
 import model.CompetitionIdentifier;
 import model.Task;
 
 public class CompetitionPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private static final int GAP = 10;
 	private String competitionName;
 	private JButton addButton;
 	private JButton updateButton;
 	private JButton deleteButton;
 	private JButton upButton;
 	private JButton downButton;
 	private JScrollPane sequenceTableScrollPane;
 	private JTable sequenceTable;
 	protected SequenceTableModel sequenceTableModel;
 	private DefaultTableCellRenderer tableCellRenderer;
 	protected JPanel eastPanel;
 
 	public CompetitionPanel(BorderLayout borderLayout) {
 		super(borderLayout);
 		init();
 	}
 
 	private void init() {
 		createTableScrollPaneInPanel();
 		createEastPanel();
 	}
 
 	class SequenceTableModel extends DefaultTableModel {
 		private static final long serialVersionUID = 1L;
 
 		public Class getColumnClass(int column) {
 			if (column >= 1)
 				return Boolean.class;
 			else
 				return String.class;
 		}
 
 		public void clearColumn(int c) {
 			for (int i = 0; i < this.getRowCount(); i++) {
 				this.setValueAt(null, i, c);
 			}
 		}
 
 		public boolean isCellEditable(int row, int col) {
 			if (col == 0)
 				return false;
 			else
 				return true;
 		}
 	}
 
 	protected final void createButtons() {
 		addButton = new JButton();
 		addButton.setAlignmentX(CENTER_ALIGNMENT);
 		eastPanel.add(addButton);
 		eastPanel.add(Box.createVerticalStrut(GAP));
 		updateButton = new JButton();
 		updateButton.setAlignmentX(CENTER_ALIGNMENT);
 		eastPanel.add(updateButton);
 		eastPanel.add(Box.createVerticalStrut(GAP));
 		deleteButton = new JButton();
 		deleteButton.setAlignmentX(CENTER_ALIGNMENT);
 		eastPanel.add(deleteButton);
 		eastPanel.add(Box.createVerticalStrut(GAP));
 		upButton = new JButton();
 		upButton.setAlignmentX(CENTER_ALIGNMENT);
 		eastPanel.add(Box.createVerticalGlue());
 		eastPanel.add(upButton);
 		downButton = new JButton();
 		downButton.setAlignmentX(CENTER_ALIGNMENT);
 		eastPanel.add(Box.createVerticalStrut(GAP));
 		eastPanel.add(downButton);
 		eastPanel.add(Box.createVerticalGlue());
 	}
 
 	private final void createTableScrollPaneInPanel() {
 		sequenceTableScrollPane = new JScrollPane(
 				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		sequenceTableModel = new SequenceTableModel();
 		sequenceTableModel.addColumn("Subgoals");
 		sequenceTable = new JTable(sequenceTableModel);
		sequenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		sequenceTable.getColumnModel().getColumn(0).setPreferredWidth(180);
 		tableCellRenderer = new DefaultTableCellRenderer();
 		sequenceTable.getColumn("Subgoals").setCellRenderer(tableCellRenderer);
 		tableCellRenderer.setHorizontalAlignment(JLabel.CENTER);
 		sequenceTableScrollPane.setViewportView(sequenceTable);
 		sequenceTableScrollPane.setPreferredSize(sequenceTable
 				.getPreferredSize());
 		this.add(sequenceTableScrollPane, BorderLayout.WEST);
 	}
 
 	private final void createEastPanel() {
 		eastPanel = new JPanel();
 		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
 		this.add(eastPanel, BorderLayout.CENTER);
 	}
 
 	public String getCompetitionName() {
 		return competitionName;
 	}
 
 	public JTable getSequenceTable() {
 		return sequenceTable;
 	}
 
 	public void connectDelete(Action delete) {
 		deleteButton.setAction(delete);
 	}
 
 	public void connectAdd(Action add) {
 		addButton.setAction(add);
 	}
 
 	public void connectUpdate(Action update) {
 		updateButton.setAction(update);
 	}
 
 	public void connectUp(Action up) {
 		upButton.setAction(up);
 	}
 
 	public void connectDown(Action down) {
 		downButton.setAction(down);
 	}
 
 	public void setButtonDimension() {
 		int width = 0;
 		Component[] comp = eastPanel.getComponents();
 		// remember the widest Button
 		for (int i = comp.length - 1; i >= comp.length - 11; i--) {
 			if (comp[i].getPreferredSize().width > width) {
 				width = comp[i].getPreferredSize().width;
 			}
 		}
 		// set all Button widths to the widest one
 		for (int i = comp.length - 1; i >= comp.length - 11; i--) {
 			// don't change the glues!
 			if (comp[i].getPreferredSize().width != 0) {
 				Dimension dim = comp[i].getPreferredSize();
 				dim.width = width;
 				comp[i].setMaximumSize(dim);
 			}
 		}
 	}
 
 	public Task getSelectedTask() {
 		return null;
 	}
 
 	public void setTaskBoxSected(Task task) {
 		;
 	}
 
 	public SequenceTableModel getSequenceTableModel() {
 		return sequenceTableModel;
 	}
 
 	public DefaultTableCellRenderer getTableCellRenderer() {
 		return tableCellRenderer;
 	}
 
 	public JPanel getEastPanel() {
 		return eastPanel;
 	}
 
 	public Component getSequenceTableScrollPane() {
 		return sequenceTableScrollPane;
 	}
 }
