 
 
 
 package overwatch.controllers;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseConstraints;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.db.Vehicles;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.PersonnelPicker;
 import overwatch.gui.PickListener;
 import overwatch.gui.tabs.VehicleTab;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Set up the vehicle tab logic
  * @author John Murphy
  * @author Lee Coakley
  * @version 6
  */
 
 
 
 
 
 public class VehicleLogic implements TabController
 {
 	private final VehicleTab tab;
 	
 	
 	
 	
 	
 	public VehicleLogic( VehicleTab vt )
 	{
 		this.tab = vt;
 		attachButtonEvents();
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect() {
 		populateTabList();
 	}
 
 	
 		
 	
 
 	public JPanel getTab() {
 		return tab;
 	}
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void attachButtonEvents()
 	{
 		setupTabChangeActions();
 		setupButtonActions();
 		setupSelectActions();
 		setupFieldValidators();
 		setupPick();
 	}
 	
 	
 	
 	
 	
 	private void doSave()
 	{
 		Integer vehicleNo   = tab.getSelectedItem();
 		String  vehicleType = tab.type.field.getText();
 		String  pilotName   = tab.pilot.field.getText();
 		Integer pilotNo     = Personnel.getNumber( pilotName );
 		
 		// TODO
 		if (! Vehicles.exists(vehicleNo)) {
 			Gui.showErrorDialogue( "Failed to save", "The vehicle no longer exists." );
 			populateTabList(); // Reload
 			return;
 		}
 		
 		Database.update(
 			"update Vehicles " +
 			" set type     = '" + vehicleType + "', " +
 			"     personNo = "  + pilotNo     + " "   +
 			"where vehicleNo = " + vehicleNo  + ";" 
 		);
 		
 		populateTabList();
 		tab.setSelectedItem( vehicleNo );
 	}
 	
 	
 	
 	
 	
 	private void populateTabList()
 	{
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Vehicles", "vehicleNo", "type", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	public void setupSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {	
 				populateVehicleFields(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	public void populateVehicleFields(Integer vehicleNo)
 	{
 		if(vehicleNo == null)
 		{
 			tab.setEnableFieldsAndButtons(false);
 			tab.clearFields();
 			return;
 		}
 		else
 		{
 			tab.setEnableFieldsAndButtons(true);
 		}
 		
 		
 		EnhancedResultSet ers = Database.query(
 			"SELECT v.vehicleNo, v.type, v.personNo, p.name AS personName " +
 		    "FROM Vehicles v, Personnel p " +
 		    "WHERE v.vehicleNo = " + vehicleNo + 
 		    "  AND v.personNo  = p.personNo;"
 		);
 		
 		tab.number.field.setText("" + ers.getElemAs( "vehicleNo",  Integer.class ));
 		tab.type  .field.setText(	 ers.getElemAs( "type",       String.class  ));
 		tab.pilot .field.setText(	 ers.getElemAs( "personName", String.class  ));		
 	}
 	
 	
 	
 	
 	
 	public void setupButtonActions()
 	{
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked add new");
 			}
 		});	
 		
 	
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();
 			}
 		});
 		
 		
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Clicked delete");				
 			}
 		});	
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	public void setupFieldValidators()
 	{
 		tab.addTypeValidator( new CheckedFieldValidator() {
 			public boolean check( String text ){
 				return DatabaseConstraints.isValidName( text );
 			}
 		});
 		
 				
 		tab.addPilotValidator( new CheckedFieldValidator() {
 			public boolean check( String text ){
 				return Personnel.exists( Personnel.getNumber( text ) );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	public void setupPick()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
				tab.pilot.field.setText(Personnel.getName(picked)) ;		
 			}
 		};
 		
 		tab.pilot.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new PersonnelPicker( Gui.getCurrentInstance(), pickListener );
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
