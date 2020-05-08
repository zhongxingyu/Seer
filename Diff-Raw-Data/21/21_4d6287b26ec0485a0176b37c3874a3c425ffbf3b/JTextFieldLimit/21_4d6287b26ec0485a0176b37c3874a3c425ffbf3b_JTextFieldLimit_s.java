 package ui;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.PlainDocument;
 
 /**
 * For use with creating a text file to input data into.
 * 
 * @author trevor
 *
  */
 public class JTextFieldLimit extends PlainDocument 
 {
 	private static final long serialVersionUID = -90890825246303078L;
 
 	private int limit;
 
 	/**
	 * Constructs our JtextField with given max 
	 * size of buffer allowed
	 * 
	 * @param limit buffer size
 	 */
 	public JTextFieldLimit(int limit)
 	{
 		super();
 		this.limit = limit;
 	}
 	
 	/**
	 * Allows for string to be written to file 
	 * given that buffer is not overloaded (limit).
	 * 
 	 */
 	public void insertString(int offset, String string, AttributeSet attribute) throws BadLocationException 
 	{
 		if (string == null)
 			return;
 		
 		if (getLength() + string.length() <= limit)
 		{
 			super.insertString(offset, string, attribute);
 		}
 	}
 }
