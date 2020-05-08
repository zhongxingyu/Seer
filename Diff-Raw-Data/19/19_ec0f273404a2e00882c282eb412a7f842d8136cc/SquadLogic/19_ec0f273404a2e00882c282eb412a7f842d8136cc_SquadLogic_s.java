 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseException;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Squads;
 import overwatch.db.Supplies;
 import overwatch.db.Vehicles;
 import overwatch.gui.NameRefPairList;
 import overwatch.gui.PersonnelPicker;
 import overwatch.gui.PickListener;
 import overwatch.gui.SquadSupplyPicker;
 import overwatch.gui.SquadTroopPicker;
 import overwatch.gui.SquadVehiclePicker;
 import overwatch.gui.tabs.SquadTab;
 
 
 
 
 
 /**
  * Implements squad tab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 3
  */
 
 
 
 
 
 public class SquadLogic extends TabController<SquadTab>
 {
 
 	public SquadLogic( SquadTab tab ) {
 		super( tab );
 	}
 	
 	
 	public void respondToTabSelect(){
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////	
 	
 	private void doCreate()
 	{
 		Integer squadNo = Squads.create();
 		populateSquadsList();
 		populateFieldsAndPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void doSave( Integer squadNo )
 	{
 		String  squadName 	  = tab.name.     field.getText();
 		String  commanderName = tab.commander.field.getText();
 		Integer commanderNo	  = Personnel.getNumber( commanderName );
 		
 					
 		if ( ! Squads.exists(squadNo)) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		
 		int modRows = Database.update(
 			"UPDATE Squads "          +
 			"SET name           = '"  + squadName  + "'," +
 			"    commander = " 		  + commanderNo + " "  +
 			"WHERE squadNo = " 		  + squadNo + " ;"
 		);
 		
 		if (modRows <= 0) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		
 		populateSquadsList();
 		tab.setSelectedItem(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void delete( Integer squadNo )
 	{
 		int mods = Database.update(
 			"DELETE          " +
 			"FROM Squads     " +
 			"WHERE squadNo = " + squadNo + ";"
 		);
 		
 		if(mods <= 0) {
 			showDeletedError("squad");
 		}
 		
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	private void populateSquadsList(){
 		populateFieldsAndPanels(null);
 		
 		tab.setSearchableItems(
 		Database.queryKeyNamePairs("Squads", "squadNo", "name", Integer[].class)
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateAssignPanels( int squadNo ) {
 		try {		
 			tab.assignTroops  .setListItems( Squads.getTroops  ( squadNo ));
 			tab.assignVehicles.setListItems( Squads.getVehicles( squadNo ));
 			tab.assignSupplies.setListItems( Squads.getSupplies( squadNo ));
 		}
 		catch (DatabaseException ex) {
 			showDeletedError("Squads");
 		}
 	}
 	
 	
 	
 	
 	
 	private void populateFieldsAndPanels(Integer squadNo)
 	{
 		if(squadNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( true );
 		
 		EnhancedResultSet ers = Database.query(
 			"SELECT squadNo,  " +
 			"		name,     " +
 			"       commander " +
 		    "FROM Squads " +
 		    "WHERE squadNo  = " + squadNo  + ";"
 		);
 		
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "squad" );
 			return;
 		}
 		
 		
 		Integer commander = ers.getElemAs( "commander", Integer.class );
 		
 		String commanderName = "";
 		if (commander != null) {
 			commanderName = Database.querySingle( String.class,
 				"select loginName " +
 				"from Personnel   " +
 				"where personNo = " + commander + ";"
 			);
 		}
 		
 		tab.number   .field.setText( "" + ers.getElemAs( "squadNo",    Integer.class ));
 		tab.name     .field.setText(      ers.getElemAs( "name",       String .class ));
 		tab.commander.field.setText(      commanderName );
 		
 		//Populate the subpanels
 		populateAssignPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	protected void attachEvents(){
 		setUpButtonActions();
 		setupListSelectActions();
 		setupPickActions();
 		setupTroopAssignActions();
 		setupVehicleAssignActions();
 		setupSupplyAssignActions();
 		setupTabChangeActions();
 	}
 	
 	
 	
 	
 	
 	private void setUpButtonActions(){
 		
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doCreate();
 			}
 		});
 		
 		
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave( tab.getSelectedItem() );
 			}
 		});
 		
 		
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				delete( tab.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupListSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				populateFieldsAndPanels(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.commander.field.setText( Personnel.getLoginName(picked) );
 			}
 		};
 		
 		
 		tab.commander.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new PersonnelPicker( tab.commander.button, pickListener );
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	
 	private void setupTroopAssignActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignTroops.addItem(picked,Personnel.getLoginName(picked)) ;		
 			}
 		};
 		
 		
 		
 		tab.assignTroops.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 	= Squads.getTroopsNotInSquads();
 				ArrayList<Integer> listOfTroops = tab.assignTroops.getItems();	
 				
 				SquadTroopPicker p = new SquadTroopPicker( tab.assignTroops.getAddButton(), pickListener, nrp );	
 				
				for(int i=0; i<listOfTroops.size(); i++){
					p.removeItem(listOfTroops.get(i));
				}
				
				//p.removeItems(overwatch.util.Util.arrayListToArray(listOfTroops));  Did not work due to not being able to convert it to a integer
 				p.setVisible(true);				
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupVehicleAssignActions()
 	{
 		final PickListener<Integer> vehiclePick = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignVehicles.addItem( picked, Vehicles.getVehicleType(picked) );
 			}
 		};
 		
 		
 		tab.assignVehicles.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 		= Vehicles.getAllVehiclesNotInSquads();
 				ArrayList<Integer> listOfVehicles 	= tab.assignVehicles.getItems();
 				
 				SquadVehiclePicker vp = new SquadVehiclePicker( tab.assignVehicles.getAddButton(), vehiclePick, nrp );		
 				
				for(int i=0; i<listOfVehicles.size(); i++){
					vp.removeItem(listOfVehicles.get(i));
				}	
 				
 				vp.setVisible(true);
 			}
 		});
 	}
 	
 	
 	
 	
 	private void setupSupplyAssignActions()
 	{
 		final PickListener<Integer> supplyPickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignSupplies.addItem( picked, Supplies.getSupplyName(picked) );
 			}
 		};
 		
 		
 		tab.assignSupplies.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 		= Supplies.getAllSupplys();
 				ArrayList<Integer> listOfSupplies	= tab.assignSupplies.getItems();
 				
				SquadSupplyPicker ss = new SquadSupplyPicker( tab.assignSupplies.getAddButton(), supplyPickListener, Supplies.getAllSupplys() );
 				
				for(int i=0; i<listOfSupplies.size(); i++){
					ss.removeItem(listOfSupplies.get(i));
				}
 				
 				ss.setVisible(true);				
 			}
 		});
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
