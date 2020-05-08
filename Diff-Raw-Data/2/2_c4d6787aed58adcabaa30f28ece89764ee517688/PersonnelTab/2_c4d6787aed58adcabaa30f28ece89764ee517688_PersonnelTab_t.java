 
 
 
 package overwatch.gui;
 
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import javax.swing.event.ListSelectionListener;
 import overwatch.db.EnhancedResultSet;
 
 
 
 
 
 /**
  * Implements the Personnel tab for the main interface.
  * The search panel here relates by personNo.
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 2
  */
 
 
 
 
 
 public class PersonnelTab extends GenericPanelButtoned<Integer>
 {
 	private LabelFieldPair            name;
 	private LabelFieldPair            age;
 	private LabelFieldPair            sex;
 	private LabelFieldPair            salary;
 	private LabelFieldEllipsisTriplet rank;
 	private JButton 				  login;
 	
 	
 	
 	
 	
 	public PersonnelTab()
 	{
 		super( "Personnel", "Details" );
 		
 		setupComponents();
 	};
 	
 	
 	
 	
 	
 	public void populateSearchPanel( EnhancedResultSet ers )
 	{
 		System.out.println( ers );
 		Integer[] nums  = ers.getColumnAs( "personNo", Integer[].class );
 		String[]  names = ers.getColumnAs( "name",     String[].class  );
 		
 		NameRefPairList<Integer> pairs = new NameRefPairList<Integer>( nums, names );
 		
 		searchPanel.setSearchableItems( pairs );
 	}
 	
 	
 	
 	
 	
 	// Field validators
 	public void addNameValidator  ( CheckedFieldValidator v ) { name  .field.addValidator( v ); }
 	public void addAgeValidator   ( CheckedFieldValidator v ) { age   .field.addValidator( v ); }
 	public void addSexValidator   ( CheckedFieldValidator v ) { sex   .field.addValidator( v ); }
 	public void addSalaryValidator( CheckedFieldValidator v ) { salary.field.addValidator( v ); }
 	public void addRankValidator  ( CheckedFieldValidator v ) { rank  .field.addValidator( v ); }
 
 	// Buttons
 	public void addChangeLoginListener( ActionListener e ) { login.addActionListener(e); }
 	
 	
 
 	
 		
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void setupComponents()
 	{
 		name    = addLabelledField( "Name:" );
		age     = addLabelledField( "Age:"  );
 		sex     = addLabelledField( "Sex:"  );
 		salary  = addLabelledField( "Salary:" );
 		rank    = addLabelledFieldWithEllipsis( "Rank:" );
 		login   = addToMain( new JButton("Login details..."), "skip 1, alignx right" );	
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
