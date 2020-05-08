 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.nls;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Provides internationalized UI strings.
  * 
  * @since 2.0
  */
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.riena.ui.ridgets.nls.messages"; //$NON-NLS-1$
 
 	public static String AbstractValidDate_error_invalidDate;
 
 	public static String MaxLength_error_tooLong;
 
 	public static String MinLength_error_nullValue;
 	public static String MinLength_error_tooShort;
 
 	public static String NotEmpty_error_empty;
 
 	public static String RequiredField_error_nullValue;
 	public static String RequiredField_error_blankValue;
 
 	public static String ValidCharacters_error_invalidChar;
 
 	public static String ValidEmailAddress_error_notValid;
 
 	public static String ValidExpression_error_noMatch;
 
 	public static String ValidInteger_error_hasDecSep;
 	public static String ValidInteger_error_hasMinus;
 	public static String ValidInteger_error_alienChar;
 	public static String ValidInteger_error_cannotParse;
 
 	public static String ValidDecimal_error_noDecSep;
 	public static String ValidDecimal_error_trailingGroupSep;
 	public static String ValidDecimal_error_alienChar;
 	public static String ValidDecimal_error_cannotParse;
	public static String ValidDecimal_error_maxLength;
	public static String ValidDecimal_error_numberOfFractionDigits;
 
 	public static String ValidRange_error_cannotParse;
 	public static String ValidRange_error_outOfRange;
 
 	static {
 		// initialize resource bundle
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 
 	private Messages() {
 	}
 }
