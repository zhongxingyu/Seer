 package com.retroduction.carma.report.generator.html;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class TransitionDescription {
	private static final String BUNDLE_NAME = "com.mutation.report.generator.html.TransitionDescription"; //$NON-NLS-1$
 
 	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
 
 	public TransitionDescription() {
 	}
 
 	public String getString(String key) {
 		try{
 			return RESOURCE_BUNDLE.getString(key);
 		}catch (MissingResourceException e){
 			return "" +key +" (no description available)";
 		}
 	}
 }
