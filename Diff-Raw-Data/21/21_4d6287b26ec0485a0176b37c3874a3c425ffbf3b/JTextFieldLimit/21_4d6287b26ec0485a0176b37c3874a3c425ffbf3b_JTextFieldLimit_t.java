 package ui;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.PlainDocument;
 
 /**
 * JTextFieldLimit allows the amount of text input
 * for any JTextField to be limited to a certain
 * amount of characters
  */
 public class JTextFieldLimit extends PlainDocument 
 {
 	private static final long serialVersionUID = -90890825246303078L;
 
 	private int limit;
 
 	/**
	 * Construct the document and set the max number
	 * of allowed text input
 	 */
 	public JTextFieldLimit(int limit)
 	{
 		super();
 		this.limit = limit;
 	}
 	
 	/**
	 * Each time a character is added to the JTextField, check
	 * if the limit has been reached. If the limit has not been
	 * met, the text will be added
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
