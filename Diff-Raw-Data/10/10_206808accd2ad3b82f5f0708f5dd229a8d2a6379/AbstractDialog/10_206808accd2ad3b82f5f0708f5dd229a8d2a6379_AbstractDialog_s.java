 package com.alexrnl.commons.translation;
 
 
 /**
  * This class is defining the translation for a dialog.<br />
  * A standard dialog is composed of a title and a message, this class factorise the definition of
  * the keys for these data and uses the {@link #toString()} method to build the key.<br />
  * This class also implements the {@link ParametrableTranslation} interface which returns an empty
  * parameter array by default (i.e. no parameter are accepted by this dialog); override the method
  * {@link #getParameters()} if necessary.
  * Example of implementation (with the use of the parameters):
  * <pre>
  * public class HelloDialog extends AbstractDialog {
 * 	private helloKey;
 * 	private parameters;
  * 
  * 	public HelloDialog (String name) {
  * 		super();
  * 		this.helloKey = "commons.dialogs.hello";
  * 		this.parameters = new Object[1];
 * 		Object[0] = name;
  * 	}
  * 
  *	public String toString () {
  *		return helloKey;
  *	}
  *
  *	public Object[] getParameters () {
 *		return parameters
  *	}
  *	
  * }
  * </pre>
  * @author Alex
  */
 public abstract class AbstractDialog implements ParametrableTranslation {
 	
 	/**
 	 * The title of the dialog.
 	 * @return the translation of the dialog's title.
 	 */
 	public String title () {
 		return toString() + "." + "title";
 	}
 	
 	/**
 	 * The message of the dialog.
 	 * @return the translation of the dialog's message.
 	 */
 	public String message () {
 		return toString() + "." + "message";
 	}
 
 	/**
 	 * Provide the key to the dialog, which allow to define the {@link #title()} and
 	 * {@link #message()} method at this level.
 	 */
 	@Override
 	public abstract String toString ();
 
 	/**
 	 * By default, all dialogs are parametrable
 	 */
 	@Override
 	public Object[] getParameters () {
 		return new Object[0];
 	}
 }
