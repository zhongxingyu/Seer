 package gui.turing;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import gui.CustomTable;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import machine.turing.*;
 
 /**
  * This class implements the transition edit dialog
  * @author David Wille
  *
  */
 
 public class PropertiesEdgeEdit extends JDialog implements ActionListener, TableModelListener, ListSelectionListener {
 	
 	static final long serialVersionUID = -3667258249137827980L;
 	private JTable table;
 	private JScrollPane tablePane;
 	private CustomTable model;
 	private JButton cancelButton;
 	private JButton saveButton;
 	private JPanel saveCancelContainer;
 	private ListSelectionModel listSelectionModel;
 	/**
 	 * Stores whether the table is initialized
 	 */
 	private boolean tableInitialized = false;
 	/**
 	 * Stores the number of tapes
 	 */
 	@SuppressWarnings("unused")		// is used in for loops
 	private int numberTapes;
 	/**
 	 * Stores the data returned by the dialog
 	 */
 	private Transition transition;
 	/**
 	 * Stores the columns names
 	 */
 	private String[] head = {"Tape", "Input", "Output", "Action"};
 	/**
 	 * Stores which columns are editable
 	 */
 	private boolean[] editable = {false, true, true, true};
 	
 	/**
 	 * Constructs a dialog to edit the properties of a transition
 	 * @param numberTapes Number of tapes in the current machine
 	 * @param transition Transition that should be edited
 	 */
 	public PropertiesEdgeEdit(int numberTapes, Transition transition) {
 		this.setModal(true);
 		this.numberTapes = numberTapes;
 		Container contentPane = this.getContentPane();
 		
 		if (transition != null) {
 			this.transition = transition;
 		}
 		
 		// window title and size
 		this.setTitle("Edit transition properties");
 		this.setSize(300, 250);
 		this.setResizable(false);
 		
 		// ok and cancel button
 		saveCancelContainer = new JPanel();
 		saveCancelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		BoxLayout okLayout = new BoxLayout(saveCancelContainer, BoxLayout.X_AXIS);
 		saveCancelContainer.setLayout(okLayout);
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
 		saveButton = new JButton("OK");
 		saveButton.addActionListener(this);
 		saveCancelContainer.add(cancelButton);
 		saveCancelContainer.add(Box.createHorizontalGlue());
 		saveCancelContainer.add(saveButton);
 		this.getRootPane().setDefaultButton(saveButton);
 		contentPane.add(saveCancelContainer, BorderLayout.AFTER_LAST_LINE);
 		
 		// add table
 		model = new CustomTable(head, editable);
 		table = new JTable(model);
 		table.getModel().addTableModelListener(this);
 		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		table.setFillsViewportHeight(true);
 		table.setColumnSelectionAllowed(true);
 		table.setRowSelectionAllowed(true);
 		table.setFocusable(false);
 		listSelectionModel = table.getSelectionModel();
 		listSelectionModel.addListSelectionListener(this);
 		
 		ArrayList<Character> read = new ArrayList<Character>();
 		ArrayList<Character> write = new ArrayList<Character>();
 		ArrayList<Character> action = new ArrayList<Character>();
 		
 		for (int i = 0; i < numberTapes; i++) {
 			String[] initialData = new String[4];
 			initialData[0] = "" + i;
 			if (transition != null) {
 				initialData[1] = transition.getRead().get(i).toString();
 				initialData[2] = transition.getWrite().get(i).toString();
 				initialData[3] = transition.getAction().get(i).toString();
 			}
 			else {
 				initialData[1] = "*";
				initialData[2] = "*";
 				initialData[3] = "N";
 				table.setEditingColumn(i);
 				read.add('*');
				write.add('*');
 				action.add('N');
 			}
 			model.addRow(initialData);
 		}
 		
 		if (transition == null) {
 			this.transition = new Transition(UUID.randomUUID().toString(), read, write, action);
 		}
 		
 		tablePane = new JScrollPane(table);
 		tablePane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		
 		contentPane.add(tablePane);
 		tableInitialized = true;
 	}
 	
 	/**
 	 * Shows the Dialog
 	 * @return The data edited by the user
 	 */
 	public Transition showEdit() {
 		this.setVisible(true);
 		
 		return transition;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == saveButton) {
 			if (this.table.isEditing()) {
 				table.getCellEditor().stopCellEditing();
 			}
 			this.setVisible(false);
 			dispose();
 		}
 		else if (e.getSource() == cancelButton) {
 			transition = null;
 			this.setVisible(false);
 			dispose();
 		}
 	}
 
 	@Override
 	public void tableChanged(TableModelEvent e) {
 		if (tableInitialized) {
 			int row = e.getFirstRow();
 			int col = e.getColumn();
 			if (e.getType() == TableModelEvent.UPDATE) {
 				String newData = (String) this.table.getModel().getValueAt(row, col);
 				switch(col) {
 					case 1:
 						transition.getRead().set(row, newData.charAt(0));
 						break;
 					case 2:
 						transition.getWrite().set(row, newData.charAt(0));
 						break;
 					case 3:
 						transition.getAction().set(row, newData.charAt(0));
 						break;
 					default:
 						break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		int row = table.getSelectedRow();
 		if (row == -1) {
 			row = table.getRowCount() - 1;
 		}
 		table.setColumnSelectionInterval(0, 3);
 		table.setRowSelectionInterval(row, row);
 	}
 	
 }
