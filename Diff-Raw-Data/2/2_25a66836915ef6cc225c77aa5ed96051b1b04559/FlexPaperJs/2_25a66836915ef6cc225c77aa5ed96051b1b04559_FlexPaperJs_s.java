 package org.vaadin.addon.flexpaper.client.ui;
 
 import com.google.gwt.core.client.GWT;
 import com.vaadin.terminal.gwt.client.ValueMap;
 
 
 /**
  * Wrapper arround flexpaper.js.
  * 
  * @author Paul Butenko (paul.butenko@gmail.com)
  */
public class FlexPaperJs {
 	
 	/** The base url. */
 	private static String BASE_URL = GWT.getModuleBaseURL();
 	
 	/**
 	 * Creates the flex paper.
 	 *
 	 * @param divId the div id
 	 * @param config the config
 	 */
 	native public static void createFlexPaper(String divId, ValueMap config) /*-{
 	
 		config['baseUrl'] = @org.vaadin.addon.flexpaper.client.ui.FlexPaperJs::BASE_URL;
 		$wnd.jQuery('#' + divId).FlexPaperViewer({config : config});
 		
 	}-*/;
 }
