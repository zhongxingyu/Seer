 package cz.vutbr.fit.gja.gjaddr.gui;
 
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Adress;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Contact;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Database;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.Email;
 import cz.vutbr.fit.gja.gjaddr.persistancelayer.PhoneNumber;
 import java.util.List;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * Panel with contacts
  *
  * @author Bc. Jan Kaláb <xkalab00@stud.fit,vutbr.cz>
  */
 class ContactsPanel extends JPanel {
 	static final long serialVersionUID = 0;
 	private final Database db = new Database();
 	private final DefaultTableModel model = new DefaultTableModel(new String[] {"First Name", "Last Name", "Email", "Phone", "Address"}, 0) {
 		static final long serialVersionUID = 0;
 		@Override
 		public boolean isCellEditable(int row, int column) {
 			return false;
 		}
 	};
 
 	/**
 	 * Constructor
 	 *
 	 * @param listener Listener to handle actions outside goups panel
 	 */
 	public ContactsPanel() {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		JLabel label = new JLabel("Contacts");
 		label.setAlignmentX(CENTER_ALIGNMENT);
 		add(label);
 		fillTable(db.getAllContacts());
 		JTable table = new JTable(model);
 		table.setAutoCreateRowSorter(true);
 		JScrollPane scrollPane = new JScrollPane(table);
 		add(scrollPane);
 	}
 
 	/**
 	 * Fill table with data from list
 	 */
	void fillTable(List<Contact> contacts) {
 		model.setRowCount(0);
 		for (Contact c : contacts) {
 			System.out.println(c);
 
 			String separator = "";
 			final StringBuilder emails = new StringBuilder();
 			try {
 				for (Email email : c.getEmails()) {
 					emails.append(separator);
 					emails.append(email.getEmail());
 					separator = ", ";
 				}
 			} catch (NullPointerException e) {
 				//Ignore it, probably just no emails
 			}
 
 			separator = "";
 			final StringBuilder phones = new StringBuilder();
 			try {
 				for (PhoneNumber phone : c.getPhoneNumbers()) {
 					phones.append(separator);
 					phones.append(phone.getEmail());	//WTFLOL
 					separator = ", ";
 				}
 			} catch (NullPointerException e) {
 				//Ignore it, probably just no phone numbers
 			}
 
 			separator = "";
 			final StringBuilder addresses = new StringBuilder();
 			try {
 				for (Adress address : c.getAdresses()) {
 					addresses.append(separator);
 					addresses.append(address.getStreet());
 					addresses.append(" ");
 					addresses.append(address.getNumber());
 					addresses.append(", ");
 					addresses.append(address.getCity());
 					separator = "; ";
 				}
 			} catch (NullPointerException e) {
 				//Ignore it, probably just homeless
 			}
 
 			model.addRow(new String[] {
 				c.getFirstName(),
 				c.getSurName(),
 				emails.toString(),
 				phones.toString(),
 				addresses.toString()
 			});
 		}
 	}
 }
