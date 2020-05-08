 	package main.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 import main.model.Customer;
 import main.model.Reservation;
 import main.util.Log;
 import main.view.*;
 
 /**
  * A controller for the customer container.
  */
 public class CustomerController {
 
 	/**
 	 * The container to use when the customer table is updated.
 	 */
 	private CustomerContainer container;
 
 	/**
 	 * Creates a controller for a given CustomerContainer by initializing it with an array 
 	 * of all available customers and set's up the relevant listeners.
 	 */
 	public CustomerController(CustomerContainer container) {
 		// Store the container for use later on
 		this.container = container;
 
 		// Retrieve the customers to show - initially we just want a list over all available customers
 		Customer[] customers = Customer.getAll();
 	
 		// Adds a listener to the search button
 		container.buttonSearch.addActionListener(getSearchListener());
 		
 		// Adds a listener to the delete button
 		container.buttonDelete.addActionListener(getDeleteListener(customers));
 		
 		// Show the table
 		showCustomerTable(customers);
 	}
 	
 	/**
 	 * Shows the given array of customers and set's the appropriate listeners up.
 	 */
 	private void showCustomerTable(final Customer[] customers) {
 		// Show the customers in the array and add a listener for mouse events in the table.
 		CustomerTable table = container.showCustomers(customers);
 		
 		// If the customers array is empty, don't bother setting the listener, otherwise move on!
 		if (customers != null) {
 			// Set the listener for the table
			table.addMouseListener(getTableListener(customers, table));
 		}
 	}
 	
 	/**
 	 * Creates an action listener that opens a new customer search window.
 	 */
 	private ActionListener getSearchListener() {
 		return new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Create the search window.
 				CustomerWindow window = new CustomerWindow();
 				
 				// Add listeners to the window
 				window.buttonSubmit.addActionListener(getWindowSearchListener(window));
 			}
 		};
 	}
 	
 	/**
 	 * Creates a MouseAdapter that listens to the CustomerTable for events.
 	 */
	private MouseAdapter getTableListener(final Customer[] customers, final CustomerTable table) {
 		// Create a new mouse adapter
 		return new MouseAdapter() {
 			// On click: Edit customer in a new window (if the row exists)
 			public void mouseClicked(MouseEvent e) {
 				// If double-click then create an edit window
 				if (e.getClickCount() == 2) {
 					// Find the row in question
					int selectedRow  = table.getSelectedRow();
 				
					if (selectedRow >= 0) {
						// Initialize the window
						final CustomerWindow window = new CustomerWindow(customers[selectedRow]);
		
						// And define the action listener for the save button
						window.buttonSubmit.addActionListener(getWindowSaveListener(window));
					}
 				}
 			}
 			// On drag: Disable delete button if more than one row is selected
 			public void mouseReleased(MouseEvent e) {
 				int selectedRows = container.getCustomerTable().getSelectedRowCount();
 				// If the selected row isn't 1 then disable the delete button
 				if (selectedRows != 1) {
 					container.buttonDelete.setEnabled(false);
 				} else {
 					// Enable the delete button
 					container.buttonDelete.setEnabled(true);
 				}
 			}
 		};
 	}
 	
 	private ActionListener getWindowSaveListener(final CustomerWindow window) {
 		return new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int customerId = window.customerId;
 				if (customerId != 0) {
 					String firstName = window.fieldFirstName.getText();
 					String lastName  = window.fieldLastName.getText();
 					String email	 = window.fieldEmail.getText();
 					String phone	 = window.fieldPhone.getText();
 					String address	 = window.fieldAddress.getText();
 					
 					// A list of errors, if any.
 					ArrayList<String> errorList = new ArrayList<String>();
 					
 					// Check for empty fields
 					if (firstName.isEmpty()) errorList.add("firstName");
 					if (lastName.isEmpty())  errorList.add("lastName");
 					if (email.isEmpty()) 	 errorList.add("email");
 					if (phone.isEmpty()) 	 errorList.add("phone");
 					if (address.isEmpty()) 	 errorList.add("address");
 					
 					if (errorList.size() > 0) {
 						String message = "Unable to update customer with empty fields: ";
 						for (int n = 0; n < errorList.size(); n++) {
 							// Add a field to the message (and a "," if we're not iterating over the last element)
 							message += errorList.get(n) + ((n < errorList.size() - 1) ? ", " : ".");
 						}
 						
 						// Show the message
 						JOptionPane.showMessageDialog(window, message);
 					} else {
 						// Update the customer in the database.
 						Customer customer = new Customer(firstName, lastName, email, phone, address);
 						Customer.update(customer, customerId);
 						
 						// Update the customer table
 						showCustomerTable(Customer.getAll());
 						
 						// Close the window
 						window.dispose();
 					}
 				} else {
 					Log.warning("Unable to update a customer without an id. Please make sure the customer is stored in the database.");
 				}
 			}
 		};
 	}
 
 	private ActionListener getWindowSearchListener(final CustomerWindow window) {
 		return new ActionListener() {
 	
 			public void actionPerformed(ActionEvent e) {	
 				window.buttonSubmit.addActionListener(new ActionListener() {
 	
 					public void actionPerformed(ActionEvent e) {						
 						// And assign fields
 						HashMap<String, String> fields = new HashMap<String, String>();
 						String firstName = window.fieldFirstName.getText();
 						String lastName  = window.fieldLastName.getText();
 						String email     = window.fieldEmail.getText();
 						String phone     = window.fieldPhone.getText();
 						String address   = window.fieldAddress.getText();
 						
 						if (firstName.length() > 0)
 							fields.put("firstName", firstName);
 						
 						if (lastName.length() > 0)
 							fields.put("lastName", lastName);
 						
 						if (email.length() > 0)
 							fields.put("email", email);
 						
 						if (phone.length() > 0)
 							fields.put("phone", phone);
 							
 						if (address.length() > 0)
 							fields.put("address", address);
 						
 						// Retrieve the customers that match the search criteria
 						Customer[] customers = Customer.searchWhere(fields);
 						
 						// Show them
 						showCustomerTable(customers);
 						
 						// And finally close the open window
 						window.dispose();
 					}
 				});
 			}
 		};
 	}
 	
 	private ActionListener getDeleteListener(final Customer[] customers) {
 		// Define the action listener for the delete button
 		return new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				// Retrieve the selected row
 				int selectedRow = container.getCustomerTable().getSelectedRow();
 				
 				Customer customer = customers[selectedRow];
 				
 				if (customer == null) {
 					Log.error("Error in retrieving customer from table");
 				} else if (customer.id == 0) {
 					Log.error("Unable to delete customer without id");
 				} else {
 					int confirmed = JOptionPane.showConfirmDialog(container, "Are you sure you want to delete customer " + customer.firstName + " " + customer.lastName + "?");
 					
 					if (confirmed == 0) {
 						// Examine if there are any reservations by the
 						// customer in the future. If so we can't
 						// delete the customer.
 						HashMap<String, String> fields = new HashMap<String, String>();
 						fields.put("customer", customer.id + "");
 						
 						// Do the search
 						Reservation[] reservations = Reservation.searchWhere(fields);
 						
 						// Create a future flag
 						boolean hasFutureReservations = false;
 						
 						// Examine if the reservations is in the future
 						if (reservations != null) {
 							for (Reservation r : reservations) {
 								if (r.period != null) {
 									// If the end of the period is after this moment
 									if (r.period.end.after(new Date())) {
 										// Then the reservation must be in the future
 										hasFutureReservations = true;
 									}
 								}
 							}
 						} 
 						
 						// Check if there's any reservations
 						if (hasFutureReservations) {					
 							JOptionPane.showMessageDialog(container, "Unable to delete customer with future reservations.");
 						} else {
 							Customer.delete(Customer.table, customer.id);
 							
 							// Update the table
 							showCustomerTable(Customer.getAll());
 						}
 					}
 				}
 			}
 		};
 	}
 
 }
