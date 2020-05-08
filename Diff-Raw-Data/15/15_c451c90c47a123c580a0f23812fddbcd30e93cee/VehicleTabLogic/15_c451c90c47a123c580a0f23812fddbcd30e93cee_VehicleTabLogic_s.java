 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import overwatch.db.Database;
 import overwatch.gui.tabs.VehicleTab;
 
 
 /**
  * Set up the vehicle tab logic
  * @author john
  * Version 1
  */
 
 public class VehicleTabLogic {
 	
 	
 	
 	
 	public VehicleTabLogic(VehicleTab vt)
 	{
		attatchButtonEvents(vt);
 	}
 	
 	
 	
	public void attatchButtonEvents(VehicleTab vt)
 	{
 		addNew(vt);
 		deleteVehicle(vt);
 		saveVehicle(vt);
 		populateTabList(vt);
 		vehicleListChange(vt);
 	}
 	
 	
 	
 	
 	private static void populateTabList(VehicleTab vt)
 	{
 		vt.setSearchableItems(
 			Database.queryKeyNamePairs( "Vehicles", "vehicleNo", "type", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	public void vehicleListChange(final VehicleTab vt)
 	{
 		vt.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				System.out.println(vt.getSelectedItem());	
 			}
 		});
 	}
 	
 	
 	
 	public void addNew(VehicleTab vt)
 	{
 		vt.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked add new");
 			}
 		});
 	}
 	
 	
 	
 	public void deleteVehicle(VehicleTab vt)
 	{
 		vt.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked delete");				
 			}
 		});
 	}
 	
 	
 	
 	public void saveVehicle(VehicleTab vt)
 	{
 		vt.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked save");				
 			}
 		});
 	}
 
 }
