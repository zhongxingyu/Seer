 
 
 
 package overwatch.gui;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import javax.swing.JTextField;
 import javax.swing.text.Document;
 
 
 
 
 
 /**
  * JTextField with automatic input checking based on CheckedFieldValidator.
  * Goes red when there's bad input.  Test with isValid().
  * 
  * @author  Lee Coakley
  * @version 1
  * @see     CheckedFieldValidator
  */
 
 
 
 
 
 public class CheckedField extends JTextField
 {
 	private ArrayList<CheckedFieldValidator> validators;
 	private Color initialBgCol; 
 	
 	
 	
 	
 	
 	public CheckedField() {
 		super();
 		commonConstructor();
 	}
 	
 	
 	public CheckedField( Document doc, String text, int columns ) {
 		super( doc, text, columns );
 		commonConstructor();
 	}
 
 	
 	public CheckedField( int columns ) {
 		super( columns );
 		commonConstructor();
 	}
 
 	
 	public CheckedField( String text, int columns ) {
 		super( text, columns );
 		commonConstructor();
 	}
 
 	
 	public CheckedField( String text ) {
 		super( text );
 		commonConstructor();
 	}
 	
 	
 	
 	
 	
 	public void addValidator( CheckedFieldValidator v ) {
 		validators.add( v );
 	}
 	
 	
 	
 	
 	public boolean isValid() {
 		return areValidationConditionsMet();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
	private void commonConstructor() {
		validators   = new ArrayList<CheckedFieldValidator>();
		initialBgCol = getBackground();
		
		setupActions();
	}
	
	
	
 	
 	
 	private void setupActions()
 	{
 		this.addKeyListener( new KeyListener() {
 			public void keyTyped   ( KeyEvent e ) {}
 			public void keyPressed ( KeyEvent e ) {}
 			public void keyReleased( KeyEvent e ) { doValidityCheck(); }
 		});
 	}
 	
 	
 	
 	
 	
 	private void doValidityCheck()
 	{
 		if ( ! areValidationConditionsMet())
 			setBackground( Color.RED );
 		else
 			setBackground( initialBgCol );
 	}
 	
 	
 	
 	
 	
 	private boolean areValidationConditionsMet()
 	{
 		for (CheckedFieldValidator v: validators)
 			if ( ! v.check( getText() ))
 				return false;
 		
 		return true;
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
