 package de.uni_leipzig.simba.saim;
 
import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class Messages
 {
 	private static final String	BUNDLE_NAME		= "de.uni_leipzig.simba.saim.messages"; //$NON-NLS-1$
	private static final ResourceBundle	RESOURCE_BUNDLE	= ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
 	private Messages(){}
 	
 	public static String getString(String key)
 	{
 		try{return RESOURCE_BUNDLE.getString(key);}
 		catch (MissingResourceException e){	return '!' + key + '!';	}
 	}
 }
