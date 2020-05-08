 package Controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import Model.AddressBook;
 import View.AddressBookDialog;
 
 public class ShowAddressBookListener implements ActionListener{
 
 	AddressBook addressBook;
 	
 	public ShowAddressBookListener(AddressBook addressBook){
 		this.addressBook = addressBook;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		@SuppressWarnings("unused")
 		AddressBookDialog abd = new AddressBookDialog(addressBook);
 		
 	}
 
 }
 
 /*
 new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				AddressBookDialog abd = new AddressBookDialog();
 				abd.setVisible(true);
 			}
 		}
 */
