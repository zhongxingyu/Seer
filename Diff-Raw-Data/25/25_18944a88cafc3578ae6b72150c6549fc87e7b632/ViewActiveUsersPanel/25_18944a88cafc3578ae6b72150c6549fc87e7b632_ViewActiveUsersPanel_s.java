 package GUI;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import main.User;
 
 public class ViewActiveUsersPanel extends JPanel{
 	
 	private JTable table;
 	private JScrollPane scroller;
 	private GridBagConstraints c;
 	
 	
 	public ViewActiveUsersPanel() {
 		setLayout(new GridBagLayout());
 		
 		c = new GridBagConstraints();
 		
 		Driver.getAccessTracker().setUsersWithTools();
 		
 		ArrayList<User> usersWithTools = Driver.getAccessTracker().getUsersWithTools();
 		ArrayList<User> currentUsers = Driver.getAccessTracker().getCurrentUsers();
 
 		ArrayList<User> activeUsers = removeDuplicates(usersWithTools, currentUsers);
 		
 		JLabel title = new JLabel("View Active Users");
 		title.setFont(new Font("SansSerif", Font.BOLD, 38));
 		title.setHorizontalAlignment(JLabel.CENTER);
 
 		String[] columns = {"Name", "Machine Certifications", "Currently Using", "Checked Out Tools", "System Priviledges"};
 		int size = activeUsers.size();
 		String data[][] = new String[size][7];
 		if (size > 0) {
 			for (int i=0; i<size; i++) {
 				User u = activeUsers.get(i);
 				data[i][0] = u.getFirstName() + " " + u.getLastName() + " [" + u.getDepartment() + "]";
 				data[i][1] = u.getCertifiedMachines().toString();
 				if (currentUsers.contains(u)) {
 					u = currentUsers.get(currentUsers.indexOf(u));
 					data[i][2] = u.getCurrentEntry().getMachinesUsed().toString();
 				} else {
 					data[i][2] = "[]";
 				}
 				
 				data[i][3] = u.getToolsCheckedOut().toString();
 				
 				if (u.isSystemAdmin()) {
 					data[i][4] = "System Administrator";
 				} else if (u.isAdmin()) {
 					data[i][4] = "Administrator";
 				} else {
 					data[i][4] = "Basic User";
 				}
 			}
 		}
 		
 		table = new JTable(data, columns);
 		scroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 0.1;
 		c.weightx = 1;
		
 		add(title, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weighty = 0.9;
 		c.weightx = 1;
		
 		add(scroller, c);
 	}
 
 	public ArrayList<User> removeDuplicates(ArrayList<User> listA, ArrayList<User> listB) {
 		ArrayList<User> newList = new ArrayList<User>();
 		newList = listA;
 		for (User u : listB) {
 			if (!(newList.contains(u))) {
 				newList.add(u);
 			}
 		}
 		return newList;
 	}
 }
