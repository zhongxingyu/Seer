 
 
 
 package overwatch.controllers;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseConstraints;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Ranks;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.PickListener;
 import overwatch.gui.RankPicker;
 import overwatch.gui.tabs.PersonnelTab;
 import overwatch.util.Validator;
 import java.math.BigDecimal;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Implements the program logic for the personnel tab.
  * Controls saving, loading, security checking etc.
  * 
  * @author  Lee Coakley
  * @version 4
  */
 
 
 
 
 
 public class PersonnelLogic implements TabController
 {
 	private final PersonnelTab tab;
 	
 	
 	
 	
 	
 	/**
 	 * Plug the GUI tab into the controller.
 	 * @param tab
 	 */
 	public PersonnelLogic( PersonnelTab tab )
 	{
 		this.tab = tab;
 		
 		attachEvents();
 		//populateList();
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect() {	
 		populateList();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private void attachEvents()
 	{
 		setupTabChangeActions();
 		setupSelectActions   ();
 		setupButtonActions   ();
 		setupPickActions     ();
 		setupFieldValidators ();
 	}
 	
 	
 	
 	
 	
 	private void respondToRankPicker( Integer rankNo )
 	{
 		tab.rank.field.setText( Ranks.getName( rankNo ) );
 	}
 	
 	
 	
 	
 	
 	private void populateList()
 	{
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Personnel", "personNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateTabFields( Integer personNo )
 	{
 		if (personNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			return;
 		} else {
 			tab.setEnableFieldsAndButtons( true );
 		}
 		
 		EnhancedResultSet ers = Database.query(
 			"select r.name as rankName,   " +
 			"		p.name as personName, " +
 			"		personNo,    " +
 			"		age,         " +
 			"		sex,         " +
 			"		salary       " +
 			"from Ranks     r,   " +
 			"	  Personnel p    " +
 			"where p.personNo =  " + personNo + "   " +
 			"  and p.rankNo   = r.rankNo;"
 		);
 		
 		tab.number.field.setText( "" + ers.getElemAs( "personNo",   Integer   .class ) );
 		tab.name  .field.setText(      ers.getElemAs( "personName", String    .class ) );
 		tab.age   .field.setText( "" + ers.getElemAs( "age",        Integer   .class ) );
 		tab.sex   .field.setText(      ers.getElemAs( "sex",        String    .class ) );
 		tab.salary.field.setText( "" + ers.getElemAs( "salary",     BigDecimal.class ) );
 		tab.rank  .field.setText(      ers.getElemAs( "rankName",   String    .class ) );
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
		Gui.getCurrentInstance().addTabSelectNotify( this );	
 	}
 	
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> rankPickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				respondToRankPicker( picked );
 			}
 		};
 		
 		
 		tab.rank.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new RankPicker( Gui.getCurrentInstance(), rankPickListener );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				populateTabFields( tab.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		tab.addNewListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				System.out.println( "new" );
 			}
 		});
 		
 		
 		tab.addSaveListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				System.out.println( "save" );
 			}
 		});
 		
 		
 		tab.addDeleteListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				System.out.println( "delete" );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addNameValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidName( text );
 			}
 		});
 		
 		
 		tab.addAgeValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return Validator.isPositiveInt( text );
 			}
 		});
 		
 		
 		tab.addSexValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidSex( text );
 			}
 		});
 		
 		
 		tab.addSalaryValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidSalary( text );
 			}
 		});
 		
 		
 		tab.addRankValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.rankExists( text );
 			}
 		});
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static void main( String[] args )
 	{
 		Gui.setNativeStyle();
 		
 		Gui gui = new Gui();
 		
 		new PersonnelLogic( gui.personnelTab );
 		
 		gui.pack();
 		gui.setVisible( true );
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
