 /**
  * ResultsTab is a tab that goes into ExplorerView. It is used
  * to view the results of a query defined in the query tab. From 
  * this view, a user can also execute Create, Update, and Delete
  * commands on the data they are viewing.
  *
  *  @author Andrew Hollenbach <anh7216@rit.edu>
  *  @author Andrew DeVoe <ard5852@rit.edu>
  */
 package steam.dbexplorer.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashSet;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import steam.dbexplorer.controller.ExplorerController;
 import steam.dbexplorer.dbobject.DBReference;
 
 @SuppressWarnings("serial")
 public class ResultsTab extends JPanel {
 	/*
 	 * GUI elements
 	 */
 	private JButton update;
 	private JButton delete;
 	private JTable results;
 	private JScrollPane scrollPane;
 	private QueryTab queryTab;
 	
 	/**
 	 * A reference to the controller of the ExplorerView.
 	 */
 	private ExplorerController controller;
 	
 	/**
 	 * A list of the constraints associated with the last run query call.
 	 */
 	private ArrayList<String> lastConstraints;
 	
 	/**
 	 * The name of the table currently being displayed.
 	 */
 	private String currentTable;
 	
 	/**
 	 * A set of the rows that have been updated from their original
 	 * values. These rows must be "committed" before changes
 	 * are permanent.
 	 */
 	private HashSet<Integer> rowsChanged = new HashSet<Integer>();
 	
 	/**
 	 * A list of the non-editable columns. Currently this is any
 	 * column that is not a primary key.
 	 */
 	private ArrayList<Integer> notEditableColumns = new ArrayList<Integer>();;
 	
 	/**
 	 * Creates a new results tab
 	 * 
 	 * @param parent The parent element of this tab
 	 * @param controller A reference to the controller used in this tab.
 	 */
 	public ResultsTab(JTabbedPane parent, ExplorerController controller) {
 		super();
 		this.controller = controller;
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		
 		initializeTable();
 				
 		JPanel CUDpanel = createCUDPanel("entry");
 		
 		this.add(scrollPane);
 		this.add(CUDpanel);
 	}
 	
 	/**
 	 * Initializes the JTable with null values.
 	 */
 	private void initializeTable() {
 		results = new JTable();
 		results.setModel(new DefaultTableModel(controller.getData(null,null), controller.getLabels(null)));
 		scrollPane = new JScrollPane(results);
 		results.setFillsViewportHeight(true);
 	}
 	
 	/**
 	 * Updates the JTable. The view will contain any changes that were made to the
 	 * underlying data.
 	 * @param tableName The name of the table to fetch data from
 	 * @param constraintsEnum A enumeration of all the constraints that should
 	 * be placed on the select clause.
 	 */
 	public void updateTable(String tableName, Enumeration<String> constraintsEnum) {
 		updateTable(tableName, Collections.list(constraintsEnum));
 	}
 	
 	/**
 	 * Overload for updateTable that takes an array instead of an 
 	 * enumeration.
 	 * 
 	 * @param tableName The name of the table to fetch data from
 	 * @param constraintsAL A array list of all the constraints that should
 	 * be placed on the select clause.
 	 */
 	public void updateTable(String tableName, ArrayList<String> constraintsAL) {
 		lastConstraints = constraintsAL;
 		//ArrayList<String> constraintsAL = constraintsEnum != null ? Collections.list(constraintsEnum) : new ArrayList<String>();
 		String[] constraints = new String[constraintsAL.size()];
 		constraints = constraintsAL.toArray(constraints);
 		Object[][] data = controller.getData(tableName,constraints);
 		String[] labels = controller.getLabels(tableName);
 		currentTable = tableName;
 		
 		//find out what columns are editable
 		notEditableColumns.clear();
 		for(int i=0;i<labels.length;i++) {
 			if(DBReference.isPrimaryKey(currentTable, labels[i])) {
 				notEditableColumns.add(i);
 			}
 		}
 		
 		DefaultTableModel tableModel = new DefaultTableModel(data,labels) {
 		   @Override
 		   public boolean isCellEditable(int row, int column) {
 			    return !notEditableColumns.contains(column);
 		   }
 		};
 		tableModel.addTableModelListener(new TableModelListener() {
 			@Override
 			public void tableChanged(TableModelEvent event) {
 				//int row = event.getFirstRow();
 				//int col = event.getColumn();
 				//potentially change the color of the row to denote change
 				update.setEnabled(true);
 				rowsChanged.add(event.getFirstRow());
 			}
 		});
 		results.setModel(tableModel);
 		ListSelectionModel selectionModel = results.getSelectionModel();
 		selectionModel.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				delete.setEnabled(true);
 			}
 		});
 		
 		if(tableModel.getRowCount() == 0) {
 			delete.setEnabled(false);
 			update.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Creates the create, update, and delete panels for
 	 * within the Results Tab
 	 * 
 	 * @param addDeleteWhat Usually "entity"
 	 * @return A JPanel containing the add, edit, and delete buttons for 
 	 * the results tab.
 	 */
 	private JPanel createCUDPanel(String addDeleteWhat) {
 		JPanel p = new JPanel();
 		JButton add = new JButton("Add new " + addDeleteWhat);
 		add.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
             	String tableName = controller.getCurrentTable();
             	tableName = DBReference.convertToDBFormat(tableName);
             	new AddEditDialog(new JFrame(), ResultsTab.this, currentTable);
             }
         });
 		p.add(add);
 		update = new JButton("Commit changes");
 		//update.setEnabled(true);
 		update.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
             	try {
             		JSONObject json = new JSONObject();
 	            	for(int row : rowsChanged) {
 	            		for(int col=0;col<results.getColumnCount();col++) {
 		                	json.put(results.getColumnName(col),
 		                			 results.getValueAt(row, col));
 		            	}
 	            		controller.updateEntity(currentTable, json);
 	            	}
 	            	update.setEnabled(false);
	            	rowsChanged.clear();
             	} catch(JSONException e) {
 	            }
             }
         });
 		update.setEnabled(false);
 		p.add(update);
 		delete = new JButton("Remove " + addDeleteWhat);
 		delete.setEnabled(false);
 		delete.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
             	try {
             		JSONObject json = new JSONObject();
 	            	int curRow = results.getSelectedRow();
 	            	for(int col=0;col<results.getColumnCount();col++) {
 	                	json.put(results.getColumnName(col), 
 	                			 results.getValueAt(curRow, col));
 	            	}
 	            	controller.deleteEntity(currentTable, json);
 	            	((DefaultTableModel)results.getModel()).removeRow(curRow);
             	} catch(JSONException ex) {
             	}
             }
         });
 		p.add(delete);
 		
 		return p;
 	}
 
 	/**
 	 * Sets a reference to the query panel. This is useful
 	 * if you ever need to reference the other tab in your
 	 * application
 	 * 
 	 * @param queryPanel
 	 */
 	public void setQueryPanelRef(QueryTab queryPanel) {
 		this.queryTab = queryPanel;
 	}
 	
 	/**
 	 * Adds the newly created element to the table. It does so by re-querying
 	 * with the same parameters and table. This will always add the new 
 	 * element to the current table.
 	 */
 	public void addElemToTable() {
 		updateTable(controller.getCurrentTable(), lastConstraints);
 		//((DefaultTableModel)results.getModel()).addRow(values);
 	}
 }
