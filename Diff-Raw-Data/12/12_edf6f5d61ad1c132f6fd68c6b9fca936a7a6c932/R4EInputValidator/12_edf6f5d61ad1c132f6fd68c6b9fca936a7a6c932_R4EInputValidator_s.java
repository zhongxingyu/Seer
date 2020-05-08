 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to validate that a String received
  * corresponds to an existing folder
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.io.File;
 
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.WildcardFileFilter;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EInputValidator implements IInputValidator {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field FOLDER_VALIDATION_ERROR_MESSAGE. (value is ""Folder does not exist"")
 	 */
 	private static final String FOLDER_VALIDATION_ERROR_MESSAGE = "Folder does not exist";
 
 	/**
 	 * Field EMPTY_VALIDATION_ERROR_MESSAGE. (value is ""No input given"")
 	 */
 	private static final String EMPTY_VALIDATION_ERROR_MESSAGE = "No Input given";
 
 	/**
 	 * Field GROUP_WILDCARD_NAME. (value is ""*_group_root.xrer"")
 	 */
 	private static final String GROUP_WILDCARD_NAME = "*_group_root.xrer";
 
 	/**
 	 * Field FOLDER_GROUP_EXISTS_VALIDATION_ERROR_MESSAGE. (value is ""Folder already contains a group file"")
 	 */
 	private static final String FOLDER_GROUP_EXISTS_VALIDATION_ERROR_MESSAGE = "Folder already contains a Group File";
 
 	/**
 	 * Field FILE_EXISTS_VALIDATION_ERROR_MESSAGE. (value is ""File already exists"")
 	 */
 	private static final String FILE_EXISTS_VALIDATION_ERROR_MESSAGE = "File already exists";
 
	//TODO:  Bug 347176: This is a temporary fix that should be removed later
	/**
	 * Field INVALID_CHARACTER_VALIDATION_ERROR_MESSAGE. (value is ""Invalid Character detected in Path"")
	 */
	private static final String INVALID_CHARACTER_VALIDATION_ERROR_MESSAGE = "Invalid Character detected in Path";

 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method isValid.
 	 * 
 	 * @param newText
 	 *            String
 	 * @return String
 	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(String)
 	 */
 	public String isFolderValid(String newText) { // $codepro.audit.disable booleanMethodNamingConvention
 		if (null == newText || 0 == newText.length()) {
 			return FOLDER_VALIDATION_ERROR_MESSAGE;
 		}
		//TODO:  Bug 347176: This is a temporary fix that should be removed later
		if (newText.contains(" ") || newText.contains("#") || newText.contains("?")) {
			return INVALID_CHARACTER_VALIDATION_ERROR_MESSAGE;
		}
 		final File folder = new File(newText);
 		if (folder.exists()) {
 			return null;
 		}
 		return FOLDER_VALIDATION_ERROR_MESSAGE;
 	}
 
 	/**
 	 * Method isFolderEmpty.
 	 * 
 	 * @param newText
 	 *            String
 	 * @return String
 	 */
 	public String isFolderEmpty(String newText) { // $codepro.audit.disable booleanMethodNamingConvention
 		final File dir = new File(newText);
 		final File[] files = dir.listFiles(new WildcardFileFilter(GROUP_WILDCARD_NAME));
 
 		if (files.length > 0) {
 			return FOLDER_GROUP_EXISTS_VALIDATION_ERROR_MESSAGE;
 		}
 		return null;
 	}
 
 	/**
 	 * Method isFileExists.
 	 * 
 	 * @param newText
 	 *            String
 	 * @return String
 	 */
 	public String isFileExists(String newText) { // $codepro.audit.disable booleanMethodNamingConvention
 		final File file = new File(newText);
 		if (file.exists()) {
 			return FILE_EXISTS_VALIDATION_ERROR_MESSAGE;
 		}
 		return null;
 	}
 
 	/**
 	 * Method isEmpty.
 	 * 
 	 * @param newText
 	 *            String
 	 * @return String
 	 */
 	public String isValid(String newText) { // $codepro.audit.disable booleanMethodNamingConvention
 		if (null == newText || 0 == newText.length()) {
 			return EMPTY_VALIDATION_ERROR_MESSAGE;
 		}
 		return null;
 	}
 
 }
