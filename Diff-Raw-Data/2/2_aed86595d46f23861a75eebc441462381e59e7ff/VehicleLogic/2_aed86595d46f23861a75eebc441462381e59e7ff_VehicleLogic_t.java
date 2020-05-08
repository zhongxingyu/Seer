 
 
 
 package overwatch.controllers;
 
 import overwatch.core.Gui;
 import overwatch.db.*;
 import overwatch.gui.*;
 import overwatch.gui.tabs.VehicleTab;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Set up the vehicle tab logic
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 7
  */
 
 
 
 
 
 public class VehicleLogic extends TabController
 {
 	private final VehicleTab tab;
 	
 	
 	
 	
 	
 	public VehicleLogic( VehicleTab vt )
 	{
 		this.tab = vt;
 		attachEvents();
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect() {
 		populateList();
 	}
 
 	
 		
 	
 
 	public JPanel getTab() {
 		return tab;
 	}
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private void doNew()
 	{
 		Integer vehicleNo = Vehicles.create();
 		
 		populateList();
 		tab.setSelectedItem( vehicleNo );
 	}
 	
 	
 	
 	
 	
 	private void doSave()
 	{
 		if ( ! tab.areAllFieldsValid()) {
 			Gui.showErrorDialogue(
 				"Invalid Fields",
 				"Can't save: some fields contain invalid data."
 			);
 			return;
 		}
 		
 		Integer vehicleNo   = tab.getSelectedItem();
 		String  vehicleType = tab.type .field.getText();
 		String  pilotName   = tab.pilot.field.getText();
 		Integer pilotNo     = Personnel.getNumber( pilotName );
 		
 		int mods = Database.update(
 			"update Vehicles   " +
 			"set name     =   '" + vehicleType + "', " +
 			"    pilot    =    " + pilotNo     + " "   +
 			"where vehicleNo = " + vehicleNo   + ";" 
 		);
 		
 		if (mods <= 0) {
 			Gui.showErrorDialogue(
 				"Vehicle Deleted",
 				"The vehicle has been deleted by someone else!"
 			);
 		}
 		
 		populateList();
 		tab.setSelectedItem( vehicleNo );
 	}
 	
 	
 	
 	
 	
 	private void doDelete()
 	{
 		Integer vehicleNo = tab.getSelectedItem();
 		Vehicles.delete( vehicleNo );
 		populateList();
 	}
 	
 	
 	
 	
 	
 	private void populateList()
 	{
 		populateFields( null );
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Vehicles", "vehicleNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateFields(Integer vehicleNo)
 	{
 		if (vehicleNo == null)
 		{
 			tab.setEnableFieldsAndButtons(false);
 			tab.clearFields();
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons(true);
 		
 		EnhancedResultSet ers = Database.query(
 			"SELECT vehicleNo, " +
 			"       name,      " +
 			"		pilot      " +
 		    "FROM Vehicles     " +
 		    "WHERE vehicleNo = " + vehicleNo + ";"
 		);
 		
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "vehicle" );
 			return;
 		}
 		
 		
 		Integer pilot = ers.getElemAs( "pilot", Integer.class );
 		
 		String pilotName = "";
 		if (pilot != null) {
 			pilotName = Database.querySingle( String.class,
 				"select loginName " +
 				"from Personnel   " +
 				"where personNo = " + pilot + ";"
 			);
 		}
 		
 		tab.number.field.setText( "" + ers.getElemAs( "vehicleNo",  Integer.class ));
		tab.type  .field.setText(	   ers.getElemAs( "name",       String .class ));
 		tab.pilot .field.setText(      pilotName );
 	}
 	
 	
 	
 	
 	
 	private void attachEvents()
 	{
 		setupTabChangeActions();
 		setupButtonActions();
 		setupSelectActions();
 		setupFieldValidators();
 		setupPickActions();
 	}
 	
 	
 	
 	
 	
 	private void setupSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {	
 				populateFields(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doNew();
 			}
 		});	
 		
 	
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();
 			}
 		});
 		
 		
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doDelete();	
 			}
 		});	
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addTypeValidator( new CheckedFieldValidator() {
 			public boolean check( String text ){
 				return DatabaseConstraints.isValidName( text );
 			}
 		});
 		
 				
 		tab.addPilotValidator( new CheckedFieldValidator() {
 			public boolean check( String text ){
 				return text.isEmpty()
 					|| DatabaseConstraints.personExists( text );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.pilot.field.setText(Personnel.getLoginName(picked)) ;		
 			}
 		};
 		
 		tab.pilot.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new PersonnelPicker( Gui.getCurrentInstance(), pickListener );
 			}
 		});
 		
 	}
 
 }
