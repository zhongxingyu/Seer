 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 
 public class FinesGUITable extends JPanel {
 	private boolean DEBUG = false;
 	final JTable table;
 	public FinesGUITable(Object[][] fines) {
 		super(new GridLayout(1,0));
 
 		String[] columnNames = {"Amount",
 				"Issued Date",
 		"Paid Date"};
 
 		Object[][] data = fines;
 
 		//table = new JTable(data, columnNames);
 		table = new JTable();
 		table.setModel(new MyTableModel( data, columnNames));
 		// Just new maybe?
 		PaidDateListener paidDateListener = new PaidDateListener(table);
 		
 		
		
 		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		table.setFillsViewportHeight(true);
 
 		if (DEBUG) {
 			table.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					printDebugData();
 				}
 			});
 		}
 
 		//Create the scroll pane and add the table to it.
 		JScrollPane scrollPane = new JScrollPane(table);
 
 		//Add the scroll pane to this panel.
 		add(scrollPane);
 	}
 	/*
 	 * 
 	 */
 	public String getFinePaid(int row, int col){
 		Object paidDate = table.getValueAt(row, col);
 		if(paidDate instanceof String) return (String) paidDate;
 		return "";
 	}
 
 
 	private void printDebugData() {
 		int numRows = table.getRowCount();
 		int numCols = table.getColumnCount();
 		javax.swing.table.TableModel model = table.getModel();
 
 		System.out.println("Value of data: ");
 		for (int i=0; i < numRows; i++) {
 			System.out.print("    row " + i + ":");
 			for (int j=0; j < numCols; j++) {
 				System.out.print("  " + model.getValueAt(i, j));
 			}
 			System.out.println();
 		}
 		System.out.println("--------------------------");
 	}
 
 
 
 }
 
 
 
 class MyTableModel extends AbstractTableModel {
 	private static final long serialVersionUID = 1L;
 	private Object[][] data;
	private String[] headers;
 
 	public MyTableModel(Object[][] data, String[] headers) {
 		super();
 		this.data = data;
		this.headers = headers;
 	}
 
 	@Override
 	public int getColumnCount() {
		return headers.length;
 	}
 
 	@Override
 	public int getRowCount() {
 		return data.length;
 	}
 
 	@Override
 	public Object getValueAt(int row, int col) {
 			return data[row][col];
 	}
 
 	@Override
 	public Class<?> getColumnClass(int col) {
 		if (col < 2) {
 			return String.class;
 		} else {
 			return String.class;
 		}
 	}
 	/**
 	 * This says that we can only edit the paid date
 	 */
     public boolean isCellEditable(int row, int col) {
         if (col < 2) {
             return false;
         } else {
             return true;
         }
     }
     
     public void setValueAt(Object value, int row, int col) {
         data[row][col] = value;
         fireTableCellUpdated(row, col);
     }
 
 
 
 }
 
 
