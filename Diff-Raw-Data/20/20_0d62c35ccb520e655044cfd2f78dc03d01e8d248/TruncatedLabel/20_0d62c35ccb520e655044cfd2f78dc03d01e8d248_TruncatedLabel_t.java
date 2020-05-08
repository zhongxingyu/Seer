 package cz.cuni.mff.odcleanstore.webfrontend.core.components;
 
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
 
 /**
  * A label, which has it's text truncated upto a fixed number of characters.
  * 
  * @author Dušan Rychnovský (dusan.rychnovsky@gmail.com)
  *
  */
 public class TruncatedLabel extends Label
 {
 	private static final long serialVersionUID = 1L;
 	
 	/** the max length of the text to be displayed */
 	private int numOfCharacters;
 	
 	/**
 	 * 
 	 * @param compName
 	 * @param numOfCharacters
 	 */
 	public TruncatedLabel(String compName, int numOfCharacters) 
 	{
 		super(compName);
 		this.numOfCharacters = numOfCharacters;
		
		setEscapeModelStrings(false); // turn of escaping before the string gets truncated
 	}
 
 	/**
 	 * 
 	 * @param compName
 	 * @param model
 	 * @param numOfCharacters
 	 */
 	public TruncatedLabel(String compName, IModel<?> model, int numOfCharacters)
 	{
 		super(compName, model);
 		this.numOfCharacters = numOfCharacters;
 	}
 
 	/**
 	 * 
 	 * @param compName
 	 * @param label
 	 * @param numOfCharacters
 	 */
 	public TruncatedLabel(final String compName, String label, int numOfCharacters)
 	{
 		this(compName, new Model<String>(label), numOfCharacters);
 	}
	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
 	{
 		String labelText = getDefaultModelObjectAsString();
 		String truncatedText = truncateText(labelText);
		String escapedText = escapeText(truncatedText);
 		
		replaceComponentTagBody(markupStream, openTag, escapedText);
 	}
 	
 	/**
 	 * Truncates the exceeding characters of the given string.
 	 * 
 	 * @param text
 	 * @return
 	 */
 	private String truncateText(String text)
 	{
 		if (text.length() <= numOfCharacters)
 			return text;
 		
 		return text.substring(0, numOfCharacters - 1) + " ...";
 	}
	
	private String escapeText(String text) 
	{
		return Strings.escapeMarkup(text, false, false).toString();		
	}
 }
