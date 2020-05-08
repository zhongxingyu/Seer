 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import overwatch.db.Database;
 import overwatch.gui.tabs.SuppliesTab;
 
 
 /**
  * Supply tab logic
  * @author john
  * version 1
  */
 
 public class SupplyTabLogic {
 	
 	public SupplyTabLogic(SuppliesTab st)
 	{
		attachButtonEvents(st);
 	}
 	
 	
 	
	public void attachButtonEvents(SuppliesTab st)
 	{
 		newSupply(st);
 		deleteSupply(st);
 		saveSupply(st);	
 		populateTabList(st);
 		supplyListChange(st);
 	}
 	
 	
 	
 	
 	private static void populateTabList(SuppliesTab st)
 	{
 		st.setSearchableItems(
 			Database.queryKeyNamePairs( "Supplies", "supplyNo", "type", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	public void supplyListChange(final SuppliesTab st)
 	{
 		st.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 			System.out.println(st.getSelectedItem());	
 			}
 		});
 	}
 	
 	
 	
 	
 	public void newSupply(SuppliesTab st)
 	{
 		st.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked add new");	
 			}
 		});
 	}
 	
 	
 	
 	
 	public void deleteSupply(SuppliesTab st)
 	{
 		st.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked delete");
 			}
 		});
 	}
 	
 	
 	
 	
 	public void saveSupply(SuppliesTab st)
 	{
 		st.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked save");
 			}
 		});
 	}
 
 }
