 package controller;
 
 import java.io.IOException;
import java.util.List;
 
 import view.ContactListWindow;
 import view.SelectMultiContactWindow;
 import client.Client;
 
 /**
  * Controller used by the contact list View and the multiple selection View.
  * @author etudiant
  * @see Client
  * @see ContactListWindow
  */
 public class ContactListController extends Controller {
 	
 	private ContactListWindow clw;
 	private SelectMultiContactWindow smlw;
 	private Client client;
 
 	public ContactListController(ContactListWindow clw, Client client) {
 		this.clw = clw;
 		this.client = client;
 		this.client.setContactListController(this);
 	}
 
 	/**
 	 * Refreshes the contact list.
 	 */
 	public void refresh() {
 		try {
 			clw.refresh(client.getClientLogins(), client);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
	 * Refresh when receiving a DISCONNECT_CLIENT message.
	 * @param toRemove 
	 * @param loginList 
 	 */
	public void refreshDisconnected(String toRemove, List<String> loginList) {
		clw.refreshDisconnected(loginList, toRemove);
 	}
 
 	public ContactListWindow getClw() {
 		return clw;
 	}
 
 	public void setClw(ContactListWindow clw) {
 		this.clw = clw;
 	}
 
 	public Client getClient() {
 		return client;
 	}
 
 	public void setClient(Client client) {
 		this.client = client;
 	}
 
 	public SelectMultiContactWindow getSmlw() {
 		return smlw;
 	}
 
 	public void setSmlw(SelectMultiContactWindow smlw) {
 		this.smlw = smlw;
 	}
 
 	/**
 	 * Calls the Client's disconnect method.
 	 */
 	public void disconnect() {
 		client.disconnect();
 	}
 }
