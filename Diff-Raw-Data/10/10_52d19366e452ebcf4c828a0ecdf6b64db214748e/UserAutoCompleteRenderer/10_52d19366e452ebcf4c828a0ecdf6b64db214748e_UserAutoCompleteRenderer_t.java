 package com.madalla.webapp.user;
 
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
 
 import com.madalla.bo.security.UserData;
 
public class UserAutoCompleteRenderer extends AbstractAutoCompleteTextRenderer<UserData> {
 
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * A singleton instance
 	 */
	public static final IAutoCompleteRenderer<UserData> INSTANCE = new UserAutoCompleteRenderer();
 
 	/**
 	 * @see AbstractAutoCompleteTextRenderer#getTextValue(Object)
 	 */
	protected String getTextValue(UserData user)
 	{
 		return user.getName();
 	}
 
 }
