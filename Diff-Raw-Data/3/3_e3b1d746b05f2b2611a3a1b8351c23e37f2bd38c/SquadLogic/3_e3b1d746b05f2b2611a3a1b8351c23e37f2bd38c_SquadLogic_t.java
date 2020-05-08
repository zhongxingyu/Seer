 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseException;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Squads;
 import overwatch.gui.PersonnelPicker;
 import overwatch.gui.PickListener;
 import overwatch.gui.tabs.SquadTab;
 
 
 
 
 
 /**
  * The squadTabLogic
  * @author  John Murphy
  * @version 2
  */
 
 
 
 
 
 public class SquadLogic extends TabController
 {
 	private final SquadTab tab;
 	
 	
 	
 	
 	
 	public SquadLogic(SquadTab tab)
 	{
 		this.tab = tab;
 		attatchEvents();
 		setupTabChangeActions();
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect(){
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	public JPanel getTab(){
 		return tab;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void attatchEvents(){
 		setUpButtonActions();
 		setupListSelectActions();
 		setupPickActions();
 		setupTroopsAssign();
 	}
 	
 	
 	
 	
 	
 	private void setUpButtonActions(){
 		
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				createNew();
 			}
 		});
 		
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();
 			}
 		});
 		
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				delete();	
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void doSave()
 	{
 		try
 		{
 			Integer squadNo   	 = tab.getSelectedItem();
 			String  squadName 	 = tab.name.      field.getText();
 			String commanderName = tab.commander.field.getText();
 			Integer commanderNo	 = Personnel.getNumber(commanderName);
 			
 			if ( ! Squads.exists(squadNo)) {
 				showDeletedError( "Squad" );
 				populateSquadsList();
 				return;
 			}
 			
 			Database.update(
 				"UPDATE Squads "          +
 				"SET name           = '"  + squadName  + "'," +
 				"    commander = " 		  + commanderNo + " "  +
 				"WHERE squadNo = " 		  + squadNo + " ;"
 			);
 			
 			populateSquadsList();
 			tab.setSelectedItem(squadNo);
 		}
 		catch(DatabaseException exception)
 		{
 			Integer squadNo = tab.getSelectedItem();
 
			showDeletedError("Squad was already deleted");
 			populateSquadsList();
 			tab.setSelectedItem(squadNo);
 		}
 	}
 	
 	
 	
 	
 	
 	private void createNew()
 	{
 		Integer squadNo = Squads.create();
 		populateSquadsList();
 		populateFieldsAndPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void delete()
 	{
 		Integer squadNo = tab.getSelectedItem();
 		int mods = Database.update(
 			"DELETE         " +
 			"FROM Squads     " +
 			"WHERE squadNo = " + squadNo + ";"
 		);
 		
 		if(mods <= 0) {
 			showDeletedError("squad");
 		}
 		
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	private void setupListSelectActions(){
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				populateFieldsAndPanels(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void populateSquadsList(){
 		populateFieldsAndPanels(null);
 		
 		tab.setSearchableItems(
 		Database.queryKeyNamePairs("Squads", "squadNo", "name", Integer[].class)
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateAssignPanels( int squadNo ) {
 		// TODO not concurrency safe
 		tab.assignTroops  .setListItems( Squads.getTroops  ( squadNo ));
 		tab.assignVehicles.setListItems( Squads.getVehicles( squadNo ));
 		tab.assignSupplies.setListItems( Squads.getSupplies( squadNo ));
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
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.commander.field.setText(Personnel.getLoginName(picked)) ;		
 			}
 		};
 		
 		tab.commander.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new PersonnelPicker( Gui.getCurrentInstance(), pickListener );
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	private void setupTroopsAssign()
 	{
 		tab.assignTroops.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
