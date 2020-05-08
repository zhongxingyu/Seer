 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  WMG, University of Warwick
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package uk.co.jwlawson.nof1;
 
 import org.acra.ACRA;
 import org.acra.ReportField;
 import org.acra.ReportingInteractionMode;
 import org.acra.annotation.ReportsCrashes;
 
 import android.app.Application;
 
/**
 * Application. Required to set up ACRA.
 * 
 * @author John Lawson
 * 
 */
 // @formatter:off
 @ReportsCrashes(formKey = "", // will not be used
 		mailTo = "nof1@jwlawson.co.uk", 
 		mode = ReportingInteractionMode.TOAST, 
 		resToastText = R.string.crash_toast_text, 
 		customReportContent = {	ReportField.APP_VERSION_NAME, 
 								ReportField.ANDROID_VERSION,
 								ReportField.BRAND,
 								ReportField.PHONE_MODEL, 
 								ReportField.CRASH_CONFIGURATION,
 								ReportField.CUSTOM_DATA, 
 								ReportField.STACK_TRACE})
 // @formatter:on
 public class App extends Application {
	
 	@Override
 	public void onCreate() {
 		// The following line triggers the initialization of ACRA
 		ACRA.init(this);
 		super.onCreate();
 	}
	
 }
