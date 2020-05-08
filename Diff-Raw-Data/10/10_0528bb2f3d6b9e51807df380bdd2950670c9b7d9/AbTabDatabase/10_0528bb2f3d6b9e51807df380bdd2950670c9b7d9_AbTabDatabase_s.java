 package abook.gui.tabs;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 
 import abook.gui.dialogs.AbDialogAddContact;
 import abook.profile.AbPerson;
 import abook.profile.InitProfile;
 
 /**
  * Tab with table of database.
  * 
  * @author xjanda17
  *
  */
 public class AbTabDatabase implements AbITabComponent {
 	
 	protected JScrollPane panel;
 	protected final String name = "Database";
 	protected final String tooltip = "Table of all contacts";
 	protected JTable table;
 	protected DefaultTableModel tableModel;
 	
 	/**
 	 * Creates new tab.
 	 */
 	public AbTabDatabase() {
 		
 		createTable();
 		
 		this.panel = new JScrollPane(table);;
 	}
 
 	/**
 	 * Creates new table with database.
 	 */
 	@SuppressWarnings("serial")
     private void createTable() {
 		
 		tableModel = new DefaultTableModel();
 		
 		// make column titles //
		tableModel.addColumn("ID");
 		tableModel.addColumn("Name");
 		tableModel.addColumn("Surname");
 		tableModel.addColumn("Location");
 		tableModel.addColumn("Groups");
 		
 		table = new JTable(tableModel) {
 			
 			@Override
 			public boolean isCellEditable(int row,int column) {
 				return false;
 			}
 		};
 		
 		TableColumn col = table.getColumnModel().getColumn(0);
 		col.setPreferredWidth(20);
 		
 		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(tableModel);
 		table.setRowSorter(sorter);
 		table.addMouseListener(new MouseAdapter() {
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					String a = ((String) (table.getModel().getValueAt(table.getSelectedRow(), 0)));
 					new AbDialogAddContact(InitProfile.getProfile().getContact(new Integer(a))).setVisible(true);
 				}
 			}
 		});
 		
 		actualizeTab();
 		
 	}
 	
 	@Override
 	public void actualizeTab() {
 		
 		// clear table //
 		int rowCount = tableModel.getRowCount();
 		for(int i = 0; i < rowCount; i++) {
 			tableModel.removeRow(0);
 		}
 		
 		// add new rows //
 		List<AbPerson> listOfPersons = InitProfile.getProfile().getListOfAbPersons();
 		List<String> listOfSelectedGroups = InitProfile.getProfile().getListOfSelectedGroups();
 		String pattern = InitProfile.getProfile().getSearchText();
 		boolean selected;
 		for(AbPerson person : listOfPersons) {
 			
 			// search filter //
 			if(new String(person.getFirstName() + " " + person.getLastName()).toLowerCase().indexOf(pattern) < 0) continue;
 			
 			// group filter //
 			selected = false;
 			for(String group : person.getListOfGroups()) {
 				if(listOfSelectedGroups.contains(group)) {
 					selected = true;
 					break;
 				}
 			}
 			
 			if(!selected) continue;
 			
 			String[] row = new String[5];
 			row[0] = Integer.toString(person.getId());
 			row[1] = person.getFirstName();
 			row[2] = person.getLastName();
 			row[3] = person.getCity();
 			row[4] = "";
 			
 			for(String group : person.getListOfGroups()) {
 				if(!row[4].isEmpty()) row[4] += ", ";
 				row[4] += group;
 			}
 			
 			tableModel.addRow(row);
 		}
 		
 		table.repaint();
 	}
 
 	@Override
 	public JComponent getWidget() {
 		return panel;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getTooltip() {
 		return tooltip;
 	}
 
 }
