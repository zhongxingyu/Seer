 package suncertify.client.ui;
 
 import static suncertify.shared.App.DEP_DATASERVICE;
 import static suncertify.shared.App.DEP_TABLE;
 import static suncertify.shared.App.DEP_TABLE_MODEL;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.rmi.RemoteException;
import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 
 import suncertify.db.RecordNotFoundException;
 import suncertify.server.DataService;
 import suncertify.shared.App;
 import suncertify.shared.Contractor;
 
 /**
  * This class is responsible for creating the buttons used to interact with the search results displayed in the JTable.
  * 
  * @author Sean Dunne
  */
 public class SearchResultsButtonPanel extends JPanel {
 
 	private static final long serialVersionUID = 4889568662774126914L;
 
 	private JButton deleteBtn;
 	private JButton bookBtn;
 	private JButton unbookBtn;
 	private JButton createBtn;
 	private JButton editBtn;
 
 	private JTable table = (JTable) App.getDependancy(DEP_TABLE);
 
 	/**
 	 * Create a JPanel with the buttons for interacting with the search results.
 	 */
 	public SearchResultsButtonPanel() {
 		final GridLayout layout = new GridLayout(1, 2);
 		this.setLayout(layout);
 		this.add(this.createLeftButtonArea());
 		this.add(this.createRightButtonArea());
 
 		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());
 		table.getModel().addTableModelListener(new TableModelListener());
 	}
 
 	/**
 	 * This method creates a JPanel containing the booking and un-booking buttons.
 	 * 
 	 * @return A JPanel containing the booking and un-booking buttons.
 	 */
 	private JPanel createLeftButtonArea() {
 		final FlowLayout lLayout = new FlowLayout();
 		final JPanel lPanel = new JPanel(lLayout);
 		lLayout.setAlignment(FlowLayout.LEFT);
 
 		bookBtn = new JButton("Book");
 		bookBtn.addActionListener(new BookListener());
 		unbookBtn = new JButton("Remove Booking");
 		unbookBtn.addActionListener(new UnBookListener());
 		lPanel.add(bookBtn);
 		lPanel.add(unbookBtn);
 
 		return lPanel;
 	}
 
 	/**
 	 * This method creates a JPanel containing the buttons to interact with the Contractors displayed in the search results JTable.
 	 * 
 	 * @return A JPanel with buttons allowing the user interact with search results.
 	 */
 	private JPanel createRightButtonArea() {
 		final FlowLayout rLayout = new FlowLayout();
 		final JPanel rPanel = new JPanel(rLayout);
 		rLayout.setAlignment(FlowLayout.RIGHT);
 
 		createBtn = new JButton("Create");
 		editBtn = new JButton("Edit");
 		rPanel.add(createBtn);
 		rPanel.add(editBtn);
 
 		deleteBtn = new JButton("Delete (0)");
 		deleteBtn.addActionListener(new DeleteListener());
 		rPanel.add(deleteBtn);
 
 		return rPanel;
 	}
 
 	/**
 	 * This method is responsible for enabling and disabling of the buttons based on the selection in the JTable.
 	 */
 	private void setButtonState() {
 		deleteBtn.setText("Delete (" + table.getSelectedRowCount() + ")");
 
 		boolean isOneRowSelected = table.getSelectedRowCount() == 1;
 		bookBtn.setEnabled(isOneRowSelected);
 		unbookBtn.setEnabled(isOneRowSelected);
 		createBtn.setEnabled(isOneRowSelected);
 		editBtn.setEnabled(isOneRowSelected);
 
 		final String custId = (String) table.getValueAt(table.getSelectedRow(), table.getColumnCount() - 1);
 		if (custId != null && custId.isEmpty()) {
 			unbookBtn.setEnabled(false);
 		} else {
 			bookBtn.setEnabled(false);
 		}
 	}
 
 	/**
 	 * This listener deletes the data records that are selected in the JTable from the server.
 	 * 
 	 * @author Sean Dunne
 	 */
 	private class DeleteListener implements ActionListener {
 
 		private SearchResultsTableModel tableModel = (SearchResultsTableModel) App.getDependancy(DEP_TABLE_MODEL);
 		private DataService dataService = (DataService) App.getDependancy(DEP_DATASERVICE);
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void actionPerformed(ActionEvent event) {
			final ArrayList<Contractor> deletedContractors = new ArrayList<>();
			for (int i : table.getSelectedRows()) {
				deletedContractors.add(this.tableModel.getContractorAt(i));
			}
 
			for (Contractor contractor : deletedContractors) {
 				try {
 					this.dataService.delete(contractor);
 				} catch (RecordNotFoundException e) {
 					App.showError(e.getMessage());
 				} catch (RemoteException e) {
 					App.showErrorAndExit("The remote server is no longer available.");
 				}
 			}
 		}
 	}
 
 	/**
 	 * This listener sets the state of the buttons when the user changes the selection on the JTable.
 	 * 
 	 * @author Sean Dunne
 	 */
 	private class TableSelectionListener implements ListSelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			setButtonState();
 		}
 	}
 
 	/**
 	 * This listener sets the state of the buttons when the TableModel data changes.
 	 * 
 	 * @author Sean Dunne
 	 */
 	private class TableModelListener implements javax.swing.event.TableModelListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void tableChanged(TableModelEvent e) {
 			setButtonState();
 		}
 	}
 
 	/**
 	 * This listener books the selected Contractor on the JTable.
 	 * 
 	 * @author Sean Dunne
 	 */
 	private class BookListener implements ActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			final String id = JOptionPane.showInputDialog("Enter Customer ID");
 			if (id != null) {
 				if (id.matches("^(\\d{8}|)$")) {
 					table.setValueAt(id, table.getSelectedRow(), table.getColumnCount() - 1);
 					table.requestFocus();
 				} else {
 					App.showError("The Customer ID must be 8 digits.");
 				}
 			}
 		}
 	}
 
 	/**
 	 * This listener removes the booking on the selected Contractor in the JTable.
 	 * 
 	 * @author Sean Dunne
 	 */
 	private class UnBookListener implements ActionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			table.setValueAt("", table.getSelectedRow(), table.getColumnCount() - 1);
 			table.requestFocus();
 		}
 	}
 }
