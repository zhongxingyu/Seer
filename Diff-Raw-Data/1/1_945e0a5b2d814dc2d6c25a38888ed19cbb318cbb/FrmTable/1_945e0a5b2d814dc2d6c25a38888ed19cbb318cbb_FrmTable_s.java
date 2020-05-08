 package ClassAdminFrontEnd;
 
 import javax.swing.*;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseAdapter;
 import java.util.LinkedList;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 
 
 import ClassAdminBackEnd.Global;
 
 import ClassAdminBackEnd.Project;
 import ClassAdminBackEnd.SuperEntity;
 import ClassAdminBackEnd.TableCellListener;
 
 public class FrmTable extends JPanel {
 	private JTable table;
 	private JButton btnAdd;
 	private DefaultTableModel tableModel;
 	private JTextField txtField1;
 	private JTextField txtField2;
 	LinkedList<LinkedList<SuperEntity>> data;
 	Project project;
 
 	private LinkedList<Integer> selected = new LinkedList<Integer>();
 
 
 	public FrmTable(String[] headers, LinkedList<LinkedList<SuperEntity>> data, Project project) {
 		this.data = data;
 		this.project = project;
 		createGUI(headers);
 	}
 
 	private void createGUI(String[] headers) {
 		setLayout(new BorderLayout());
 		JScrollPane pane = new JScrollPane();
 
 		Action action = new AbstractAction() {
 			public void actionPerformed(ActionEvent e) {
 				TableCellListener tcl = (TableCellListener) e.getSource();
 				/*
 				 * System.out.println("Row   : " + tcl.getRow());
 				 * System.out.println("Column: " + tcl.getColumn());
 				 * System.out.println("Old   : " + tcl.getOldValue());
 				 * System.out.println("New   : " + tcl.getNewValue());
 				 */
 
 				if (tcl.getOldValue() != tcl.getNewValue()) {
 					if (data.get(tcl.getRow()).get(tcl.getColumn())
 							.getDetails().getType().getIsTextField()) {
 						data.get(tcl.getRow()).get(tcl.getColumn())
 						.getDetails()
 						.setValue((String) tcl.getNewValue());
 					} else {
 						try {
 							data.get(tcl.getRow())
 							.get(tcl.getColumn())
 							.setMark(
 									(Double.parseDouble((String) tcl
 											.getNewValue())));
 						} catch (Exception ex) {
 							table.getModel().setValueAt(tcl.getOldValue(),
 									tcl.getRow(), tcl.getColumn());
 						}
 					}
 				}
 			}
 		};
 		
 		Object[][] temp = new Object[data.size()][data.get(0).size()];
 		
 		
 		for(int x = 0; x < data.get(1).size();x++){
 			project.getSelected().add(data.get(1).get(x));
 		}
 
 		for (int x = 0; x < data.size(); x++) {
 			for (int y = 0; y < data.get(0).size(); y++) {
 				temp[x][y] = data.get(x).get(y).getValue();
 			}
 		}
 		
 		table = new JTable(){
 			public Component prepareRenderer(TableCellRenderer renderer,int Index_row, int Index_col) {
 				Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
 				//even index, selected or not selected
 				
 				if (project.getSelected().contains(data.get(table.getRowSorter().convertRowIndexToModel(Index_row)).get(Index_col))) {
 					int[] intTest = table.getSelectedRows();
 					boolean temp = false;
 					
 					for(int x = 0; x < intTest.length; x++){
 						if(intTest[x]==convertRowIndexToModel(Index_row)){
 							temp = true;
 						}
 					}
 					if(intTest.length > 0){
 						if(!temp){
 							project.getSelected().remove(data.get(table.getRowSorter().convertRowIndexToModel(Index_row)).get(Index_col));
 							comp.setBackground(Color.white);
 						}
 					}
 						else{
 							comp.setBackground(Color.green);
 							table.addRowSelectionInterval(Index_row, Index_row);
 						}
 					}
 					else {
 						comp.setBackground(Color.white);
 					}
 				
 				if(isCellSelected(Index_row, Index_col)){
 					comp.setBackground(Color.green);
 					project.getSelected().add(data.get(table.getRowSorter().convertRowIndexToModel(Index_row)).get(Index_col));
 				}
 				
 				/*else {
 					comp.setBackground(Color.white);
 				}*/
 				return comp;
 			}
 		};
 		table.setAutoCreateRowSorter(true);
 		
 		
 
 		TableCellListener tcl = new TableCellListener(table, action);
 
 		table.addPropertyChangeListener(tcl);
 
 		table.getSelectionModel().addListSelectionListener(
 				new ListSelectionListener() {
 
 					@Override
 					public void valueChanged(ListSelectionEvent arg0) {
 						int viewRow = table.getSelectedRow();
 						if (viewRow < 0) {
 						} else {
 							int modelRow = table.convertRowIndexToModel(viewRow);
 						}
 					}
 
 				});
 
 		pane.setViewportView(table);
 		JPanel eastPanel = new JPanel();
 
 		btnAdd = new JButton("Add");
 		eastPanel.add(btnAdd);
 		
 		JButton btnView = new JButton("View student");
 		eastPanel.add(btnView);
 		
 		JPanel northPanel = new JPanel();
 
 		txtField1 = new JTextField();
 		txtField2 = new JTextField();
 
 		JLabel lblField1 = new JLabel("Column1   ");
 		JLabel lblField2 = new JLabel("Column2   ");
 		txtField1.setPreferredSize(lblField1.getPreferredSize());
 		txtField2.setPreferredSize(lblField2.getPreferredSize());
 
 		add(northPanel, BorderLayout.NORTH);
 		add(eastPanel, BorderLayout.EAST);
 		add(pane, BorderLayout.CENTER);
 
 		tableModel = new DefaultTableModel(temp, (Object[]) headers);
 		table.setModel(tableModel);
 
 		btnAdd.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				table.repaint();
 				int count = tableModel.getRowCount() + 1;
 				tableModel.addRow(new Object[] { txtField1.getText(),
 						txtField1.getText() });
 
 				
 				//table.getColumnModel().removeColumn(table.getColumnModel().getColumn(1));
 			}
 		});
 		
 		btnView.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				table.repaint();
 				table.getSelectedRow();
 				
				
 				//table.getColumnModel().removeColumn(table.getColumnModel().getColumn(1));
 			}
 		});
 			
 	}
 
 	public class InteractiveTableModelListener implements TableModelListener {
 		public void tableChanged(TableModelEvent evt) {
 			if (evt.getType() == TableModelEvent.UPDATE) {
 				int column = evt.getColumn();
 				int row = evt.getFirstRow();
 				System.out.println("row: " + row + " column: " + column);
 				table.setColumnSelectionInterval(column + 1, column + 1);
 				table.setRowSelectionInterval(row, row);
 				table.repaint();
 			}
 		}
 	}
 }
