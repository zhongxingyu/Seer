 package versionchecker.actions;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 /**
  * VCArtifactTable is a table to represent all the artifacts of the current
  * Eclipse instance. It extends JPanel and will be added directly to the main
  * window of the plugin. It also supports row search/sort/selection.
  */
 public class VCArtifactTable extends JPanel {
 
 	private boolean DEBUG = false;
 	private boolean ALLOW_ROW_SELECTION = true;
 	private Object[][] contents;
 	private int size = 0;
 	private VCArtifact selectedAF;
 
 	public VCArtifactTable(Object[] data) {
 		super(new GridLayout(1, 0));
 
 		final String[] columnNames = { "ID", "Version" };
 
 		// Add artifacts data as table entries
 		contents = new Object[data.length][2];
 		for (int i = 0; i < data.length; i++) {
 			this.addEntry((VCArtifact) data[i]);
 		}
 
 		final JTable table = new JTable(contents, columnNames);
 		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		table.setFillsViewportHeight(true);
 
 		// Setup selection action for row selection
 		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		if (ALLOW_ROW_SELECTION) { // true by default
 			ListSelectionModel rowSM = table.getSelectionModel();
 			rowSM.addListSelectionListener(new ListSelectionListener() {
 				public void valueChanged(ListSelectionEvent e) {
 					// Ignore extra messages.
 					if (e.getValueIsAdjusting())
 						return;
 
 					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
 					if (lsm.isSelectionEmpty()) {
 						System.out.println("No rows are selected.");
 					} else {
 						int selectedRow = lsm.getMinSelectionIndex();
 
 						System.out.println("ID " + contents[selectedRow][0]
 								+ " is now selected.");
 						selectedAF = new VCArtifact(
 								(String) contents[selectedRow][0],
 								(String) contents[selectedRow][1]);
 
 					}
 				}
 			});
 		} else {
 			table.setRowSelectionAllowed(false);
 		}
 
 		if (DEBUG) {
 			table.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					printDebugData(table);
 				}
 			});
 		}
 
 		// Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 
 		// Add the scroll pane to this panel.
 		add(scrollPane);
 	}
 
 	public String getSelectedID() {
		return this.selectedAF.getId();
 	}
 
 	public String getSelectedVer() {
		return this.selectedAF.getVersion();
 	}
 
 	private void addEntry(VCArtifact toAdd) {
 		this.contents[this.size] = new String[2];
 		this.contents[this.size][0] = toAdd.getId();
 		this.contents[this.size][1] = toAdd.getVersion();
 		this.size++;
 	}
 
 	private void printDebugData(JTable table) {
 		int numRows = table.getRowCount();
 		int numCols = table.getColumnCount();
 		javax.swing.table.TableModel model = table.getModel();
 
 		System.out.println("Value of data: ");
 		for (int i = 0; i < numRows; i++) {
 			System.out.print("    row " + i + ":");
 			for (int j = 0; j < numCols; j++) {
 				System.out.print("  " + model.getValueAt(i, j));
 			}
 			System.out.println();
 		}
 		System.out.println("--------------------------");
 	}
 
 }
