 /*******************************************************************************
  * Copyright (c) 2007 Pascal Essiembre.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Pascal Essiembre - initial API and implementation
  ******************************************************************************/
 package org.eclipse.babel.editor.resource.validator;
 
 import java.util.Locale;
 
 import org.eclipse.babel.core.message.MessagesBundleGroup;
 import org.eclipse.babel.core.message.checks.DuplicateValueCheck;
 import org.eclipse.babel.core.message.checks.MissingValueCheck;
import org.eclipse.babel.editor.MessagesEditorMarkers;
 import org.eclipse.babel.editor.preferences.MsgEditorPreferences;
 
 
 /**
  * @author Pascal Essiembre
  *
  */
 public class MessagesBundleGroupValidator {    
     
     //TODO Re-think... ??
 
     public static void validate(
             MessagesBundleGroup messagesBundleGroup, IValidationMarkerStrategy markerStrategy) {
         Locale[] locales = messagesBundleGroup.getLocales();
         for (int i = 0; i < locales.length; i++) {
             Locale locale = locales[i];
             validate(messagesBundleGroup, locale, markerStrategy);
         }
     }
     
     public static void validate(
             MessagesBundleGroup messagesBundleGroup,
             Locale locale,
             IValidationMarkerStrategy markerStrategy) {
         //TODO check if there is a matching EclipsePropertiesEditorResource already open.
         //else, create MessagesBundle from PropertiesIFileResource
         
     	DuplicateValueCheck duplicateCheck =
     		MsgEditorPreferences.getInstance().getReportDuplicateValues()
 	    		? new DuplicateValueCheck()
 	    		: null;
     	String[] keys = messagesBundleGroup.getMessageKeys();
     	for (int i = 0; i < keys.length; i++) {
 			String key = keys[i];
             if (MsgEditorPreferences.getInstance().getReportMissingValues()) {
 	            if (MissingValueCheck.MISSING_KEY.checkKey(
 	                    messagesBundleGroup,
 	                    messagesBundleGroup.getMessage(key, locale))) {
 	                markerStrategy.markFailed(new ValidationFailureEvent(
 	                        messagesBundleGroup, locale, key,
 	                        MissingValueCheck.MISSING_KEY));
 	            }
             }
             if (duplicateCheck != null) {
             	if (!MsgEditorPreferences.getInstance().getReportDuplicateValuesOnlyInRootLocales()
             			|| (locale == null || locale.toString().length() == 0)) {
             		//either the locale is the root locale either
             		//we report duplicated on all the locales anyways.
 		            if (duplicateCheck.checkKey(
 		                    messagesBundleGroup,
 		                    messagesBundleGroup.getMessage(key, locale))) {
 		                markerStrategy.markFailed(new ValidationFailureEvent(
 		                        messagesBundleGroup, locale, key, duplicateCheck));
 		            }
 	            	duplicateCheck.reset();
             	}
             }
         }
    	
		/*
		 * KLUDGE to fix 286365: The notification system could do with some clean-up.
		 * 
		 * The MessagesEditorMarkers is an observable object. If it
		 * finds anything that indicates that markers are required (e.g. if
		 * multiple keys have the same text) then it marks itself as 'changed'.
		 * It does not notify the observers because that would not be very
		 * efficient. We must therefore notify the observers here (noting that
		 * notifyObservers will in fact do nothing if none of the above calls to
		 * markFailed in fact set the observable as having changed).
		 */
    	if (markerStrategy instanceof MessagesEditorMarkers) {
    		((MessagesEditorMarkers)markerStrategy).notifyObservers(null);
    	}
     }
     
 }
