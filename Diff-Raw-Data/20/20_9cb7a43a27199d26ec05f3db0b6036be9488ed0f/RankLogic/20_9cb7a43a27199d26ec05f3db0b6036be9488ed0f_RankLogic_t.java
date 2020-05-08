 
 
 
 package overwatch.controllers;
 
 import java.awt.event.*;
 import javax.swing.JPanel;
 import javax.swing.event.*;
 import overwatch.core.Gui;
 import overwatch.db.*;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.tabs.RankTab;
 import overwatch.util.Validator;
 
 
 
 
 
 /**
  * RankTab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 7
  */
 
 
 
 
 
 public class RankLogic extends TabController
 {
 	private final RankTab tab;
 		
 	
 	
 	
 	
 	public RankLogic(RankTab rt)
 	{
 		this.tab = rt;
 		attachEvents();
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
 	
 	
 	
 	private void doSave()
 	{
 		Integer rankNo = tab.getSelectedItem();
 		
 		if ( ! tab.areAllFieldsValid()) {
 			showFieldValidationError();
 			return;
 		}
 		
		try {
 			String rankName  = tab.name.         field.getText();
 			String rankLevel = tab.securityLevel.field.getText();
 			
 			if ( ! Ranks.exists(rankNo)) {
 				showDeletedError( "rank" );
 				populateTabList(); // Reload
 				return;
 			}
 			
 			Database.update(
 				"UPDATE Ranks "          +
 				"SET name           = '" + rankName  + "'," +
 				"    privilegeLevel = "  + rankLevel + " "  +
 				"WHERE rankNo = " + rankNo + " ;"
 			);
 			
 			populateTabList();
 			tab.setSelectedItem(rankNo);
 		}
 		catch (DatabaseIntegrityException ex) {
 			Gui.showErrorDialogue(
 				"Rank Already Exists",
 				"A rank with that name already exists. Choose a different name."
 			);
 		}
 		
 		populateTabList();
 		tab.setSelectedItem(rankNo);
 	}
 	
 	
 	
 	
 	
 	private void doNew()
 	{
 		Integer rankNo = Ranks.create();
 				
 		populateTabList();
 		tab.setSelectedItem( rankNo );
 	}
 	
 	
 	
 	
 	
 	private void doDelete()
 	{
 		Integer rankNum = tab.getSelectedItem();
 		int mods = Database.update(
 			"DELETE         " +
 			"FROM Ranks     " +
 			"WHERE rankNo = " + rankNum + ";"
 		);
 		
 		if(mods <= 0) {
 			showDeletedError( "rank" );
 		}
 		
 		populateTabList();
 	}
 	
 	
 	
 	
 	
 	private void populateTabList()
 	{
 		populateFields( null );
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Ranks", "rankNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateFields(Integer rankNo)
 	{
 		if (rankNo == null) {
 			tab.setEnableFieldsAndButtons(false);
 			tab.clearFields();
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( ! Ranks.isHardcoded(rankNo) );
 		
 		
 		EnhancedResultSet ers = Database.query(
 		    "SELECT rankNo, name, privilegeLevel " +
 		    "FROM Ranks     " +
		    "WHERE rankNo = " + rankNo
		);
 		
 		if ( ! ers.isEmpty()) {
 			tab.number       .field.setText("" + ers.getElemAs( "rankNo",         Integer.class ));
 			tab.name         .field.setText(     ers.getElemAs( "name",           String .class ));
 			tab.securityLevel.field.setText("" + ers.getElemAs( "privilegeLevel", Integer.class ));		
 		}
 		else {
 			Gui.showErrorDialogue("No longer exists", "The selected rank no longer exists");
 			populateTabList();
 		}
 	}
 	
 	
 	
 	
 	
 	private void attachEvents() {
 		setupTabChangeActions();
 		setupButtonActions();
 		setupListSelectActions();
 		setupFieldValidators();
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addNameValidator(new CheckedFieldValidator() {
 			public boolean check(String text)
 			{
 				if ( ! DatabaseConstraints.isValidName( text ))
 					return false;
 				
 				boolean edited  = tab.name.field.isModifiedByUser();
 				boolean badEdit = (edited && Database.queryUnique( "Ranks", "name", "'"+text+"'" ));
 				return ! badEdit;
 			}
 		});
 		
 		
 		tab.addPrivilegesValidator(new CheckedFieldValidator() {
 			public boolean check(String text) {
 				return Validator.isInt( text );
 			}
 		} );
 	}
 	
 	
 	
 	
 	
 	private void setupListSelectActions()
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
 	
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doDelete();
 			}
 		});		
 	
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();
 			}
 		});
 	}
 
 	
 }
