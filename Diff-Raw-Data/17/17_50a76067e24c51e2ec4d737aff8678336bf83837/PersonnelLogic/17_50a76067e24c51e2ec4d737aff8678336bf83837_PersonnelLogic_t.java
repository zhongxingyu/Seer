 
 
 
 package overwatch.core;
 
 import overwatch.db.Database;
 import overwatch.db.DatabaseConstraints;
 import overwatch.db.EnhancedResultSet;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.MainFrame;
 import overwatch.gui.NameRefPair;
 import overwatch.gui.PersonnelTab;
 import overwatch.util.Validator;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.math.BigDecimal;
 import java.util.ArrayList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Implements the program logic for the personnel tab.
  * Controls saving, loading, security checking etc.
  * 
  * @author  Lee Coakley
  * @version 1
  */
 
 
 
 
 
 public class PersonnelLogic
 {
 	
 	public static void attachEvents( PersonnelTab tab )
 	{
 		setupSelectActions( tab );
 		setupButtonActions( tab );
 		setupValidators   ( tab );
 	}
 	
 	
 	
 	
 	
 	private static void populateTabList( PersonnelTab tab )
 	{
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Personnel", "personNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private static void populateTabFields( PersonnelTab tab, Integer personNo )
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
 			"		personNo,  " +
 			"		age,       " +
 			"		sex,       " +
 			"		salary     " +
 			"from Ranks     r, " +
 			"	  Personnel p  " +
 			"where personNo =  " + personNo + "   " +
 			"  and p.rankNo = r.rankNo;"
 		);
 		
		tab.number.field.setText( "" + ers.getElemAs( "personNo",   Integer   .class ) );
		tab.name  .field.setText(      ers.getElemAs( "personName", String    .class ) );
		tab.age   .field.setText( "" + ers.getElemAs( "age",        Integer   .class ) );
		tab.sex   .field.setText(      ers.getElemAs( "sex",        String    .class ) );
		tab.salary.field.setText( "" + ers.getElemAs( "salary",     BigDecimal.class ) );
		tab.rank  .field.setText(      ers.getElemAs( "rankName",   String    .class ) );
 	}
 	
 	
 	
 	
 	
 	private static void setupSelectActions( final PersonnelTab tab )
 	{
 		tab.addSearchPanelListSelectionListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				System.out.println( "select" );
 				populateTabFields( tab, tab.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private static void setupButtonActions( PersonnelTab tab )
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
 	
 	
 	
 	
 	
 	private static void setupValidators( PersonnelTab tab )
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
				return Validator.isPositiveBigDecimal( text );
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
 		
 		MainFrame mf = new MainFrame();
 		
 		PersonnelLogic.attachEvents   ( mf.personnelTab );
 		PersonnelLogic.populateTabList( mf.personnelTab );
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
