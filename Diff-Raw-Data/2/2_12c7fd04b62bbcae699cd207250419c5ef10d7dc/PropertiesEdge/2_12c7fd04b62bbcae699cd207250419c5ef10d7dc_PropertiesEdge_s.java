 package gui.turing;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import gui.*;
 
 public class PropertiesEdge extends JPanel implements ActionListener, TableModelListener, ListSelectionListener {
 	
 	static final long serialVersionUID = -3667258249137827980L;
 	private JTable table;
 	private JScrollPane tablePane;
 	private CustomTable model;
 	private JButton addButton;
 	private JButton deleteButton;
 	private int numberTapes;
 	private ListSelectionModel listSelectionModel;
 	private boolean tableInitialized = false;
 	
 	public PropertiesEdge(int numberTapes) {
 		this.numberTapes = numberTapes;
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		// content panel
 		this.setBorder(BorderFactory.createTitledBorder("Properties"));
 
 		// add container
 		addButton = new JButton("Add");
 		deleteButton = new JButton("Delete");
 		addButton.addActionListener(this);
 		deleteButton.addActionListener(this);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.1;
 		c.weighty = 1.0;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.anchor = GridBagConstraints.LAST_LINE_START;
 		c.insets = new Insets(5,5,5,20);
 		this.add(deleteButton, c);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.15;
 		c.weighty = 1.0;
 		c.gridx = 2;
 		c.gridy = 1;
 		c.anchor = GridBagConstraints.LAST_LINE_END;
 		c.insets = new Insets(5,20,5,5);
 		this.add(addButton, c);
 		
 		// table
 		String[] head = {"Input", "Output", "Action"};
 		model = new CustomTable(head, false);
 		table = new JTable(model);
 		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		table.setFillsViewportHeight(true);
 		table.setColumnSelectionAllowed(false);
 		table.setRowSelectionAllowed(true);
 		table.setFocusable(false);
 		listSelectionModel = table.getSelectionModel();
 		listSelectionModel.addListSelectionListener(this);
 		
 		// initialize table
 		// TODO
 		String[] tempData = {"*", "*", "N"};
 		model.addRow(tempData);
 		
 		// scroll panel
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1.0;
		c.weightx = 1.0;
 		c.gridwidth = 3;
 		c.anchor = GridBagConstraints.CENTER;
 		c.insets = new Insets(0,0,0,0);
 		tablePane = new JScrollPane(table);
 		this.add(tablePane, c);
 		tableInitialized = true;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == addButton) {
 			String[] tempData = {"*", "*", "N"};
 			model.addRow(tempData);
 		}
 		else if (e.getSource() == deleteButton) {
 			if (table.getSelectedRow() != -1) {
 				model.deleteRow(table.getSelectedRow());
 			}
 		}
 	}
 	
 	/**
 	 * Responds to data changes in the table
 	 * @param e TableModelEvent that indicates changes
 	 */
 	public void tableChanged(TableModelEvent e) {
 		if (tableInitialized) {
 			int row = e.getFirstRow();
 			int col = e.getColumn();
 			if (e.getType() == TableModelEvent.UPDATE) {
 				// TODO
 			}
 			else if (e.getType() == TableModelEvent.INSERT) {
 				// TODO
 			}
 			else if (e.getType() == TableModelEvent.DELETE) {
 				// TODO
 			}
 		}
 	}
 
 	/**
 	 * Responds to selection changes
 	 * @param e ListSelectionEvent that indicates changes
 	 */
 	public void valueChanged(ListSelectionEvent e) {
 		int row = table.getSelectedRow();
 		if (row == -1) {
 			row = table.getRowCount() - 1;
 		}
 		table.setColumnSelectionInterval(0, 2);
 		table.setRowSelectionInterval(row, row);
 	}
 }
