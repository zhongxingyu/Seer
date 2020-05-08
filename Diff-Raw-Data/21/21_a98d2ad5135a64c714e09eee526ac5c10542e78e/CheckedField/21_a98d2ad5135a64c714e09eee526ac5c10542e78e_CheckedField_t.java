 
 
 
 package overwatch.gui;
 
 import java.awt.Color;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import javax.swing.JTextField;
 import javax.swing.text.Document;
 
 
 
 
 
 /**
  * JTextField with automatic input checking based on CheckedFieldValidator.
  * Goes red when there's bad input.  Test with isInputValid().
  * 
  * @author  Lee Coakley
  * @version 2
  * @see     CheckedFieldValidator
  */
 
 
 
 
 
 public class CheckedField extends JTextField
 {
	private String  lastSetText;
 	private boolean isModifiedByUser;
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
 	
 	
 	
 	
 	
 	public boolean isInputValid() {
 		return areValidationConditionsMet();
 	}
 	
 	
 	
 	
 	
 	public void setText( String text ) {
 		super.setText( text );
 		
		lastSetText      = text;
 		isModifiedByUser = false;
 		
 		if (isEnabled() && isEditable())
 			doVisualValidityFeedback();
 	}
 	
 	
 	
 	
 	
 	public Integer getTextAsInt()
 	{
 		try { 
 			return Integer.parseInt( getText() );
 		} catch (NumberFormatException ex) {
 			throw new RuntimeException( ex );
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Returns true if the field was modified since setText was last used.
	 * Note: It doesn't count as modified if the text was changed, but is now exactly the same.
	 * @return boolean
 	 */
 	public boolean isModifiedByUser() {
 		return isModifiedByUser;
 	}
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	private void commonConstructor() {
		lastSetText  = getText();
 		validators   = new ArrayList<CheckedFieldValidator>();
 		initialBgCol = getBackground();
 		
 		setupActions();
 	}
 	
 	
 	
 	
 	
 	private void setupActions()
 	{
 		this.addKeyListener( new KeyAdapter() {
			public void keyReleased( KeyEvent e )
			{
				isModifiedByUser = ! (getText().equals( lastSetText ));				
				doVisualValidityFeedback();
			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void doVisualValidityFeedback()
 	{
 		if ( isEditable()
 		&&! areValidationConditionsMet())
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
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
