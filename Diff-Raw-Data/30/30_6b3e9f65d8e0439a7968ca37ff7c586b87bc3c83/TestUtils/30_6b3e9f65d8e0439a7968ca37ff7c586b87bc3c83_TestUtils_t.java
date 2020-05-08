 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.tests;
 
import java.util.Date;
 import java.util.Locale;
import java.util.TimeZone;
 
 /**
  * Utility class for tests.
  */
 public class TestUtils {
 
 	private TestUtils() {
 		// utility class
 	}
 
 	public static boolean isArabLocaleAvailable() {
 		Locale arabLocale = new Locale("ar", "AE");
 		for (Locale availableLocale : Locale.getAvailableLocales()) {
 			if (availableLocale.equals(arabLocale)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
	/**
	 * Because some methods take a GMT time value and returns a
	 * GMT+timezoneOffset equivalent, we create an inverse GMT-timezoneOffset
	 * time value, so that we always get the GMT equivalent in the test. This
	 * ensures the test does not break on machines with different timezones.
	 * 
	 * @param gmtTime
	 *            time in milliseconds since the "epoch".
	 * 
	 */
	public static Date createTZAdjustedDate(long gmtTime) {
		int revOffset = TimeZone.getDefault().getOffset(gmtTime) * -1;
		return new Date(gmtTime + revOffset);
	}

 }
