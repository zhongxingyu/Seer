 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.model.changeTracking.merging.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.Conflict;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictOption;
 import org.eclipse.emf.emfstore.client.model.changeTracking.merging.conflict.ConflictOption.OptionType;
 
 /**
  * Class offering common methods for the merge dialog.
  * 
  * @author wesendon
  */
 public final class DecisionUtil {
 
 	private DecisionUtil() {
 		// for checkstyle, my love
 	}
 
 	/**
 	 * TODO BRANCH some of this stuff is UI related and isn't supposed to be defined here.
 	 */
 
 	/**
 	 * Length of option label.
 	 */
 	public static final int OPTION_LENGTH = 50;
 
 	/**
 	 * Seperator symbol for detail proivder.
 	 */
 	public static final String SEPERATOR = "#";
 
 	/**
 	 * Editable detail provider.
 	 */
 	public static final String EDITABLE = "editable";
 
 	/**
 	 * Multiline widget detail provider.
 	 */
 	public static final String WIDGET_MULTILINE = "org.eclipse.emf.emfstore.client.ui.merge.widget.multiline";
 
 	/**
 	 * Multiline editable widget detail provider.
 	 */
 	public static final String WIDGET_MULTILINE_EDITABLE = WIDGET_MULTILINE + SEPERATOR + EDITABLE;
 
 	/**
 	 * Option for other involved detail provider.
 	 */
 	public static final String WIDGET_OTHERINVOLVED = "org.eclipse.emf.emfstore.client.ui.merge.widget.otherinvolved";
 
 	/**
 	 * Cuts a text to certain length and adds "..." at the end if needed.
 	 * 
 	 * @param str
 	 *            text
 	 * @param length
 	 *            length
 	 * @param addPoints
 	 *            true, if ending dotts
 	 * @return shortened string
 	 */
 	public static String cutString(String str, int length, boolean addPoints) {
 		if (str == null) {
 			return "";
 		}
 		if (str.length() > length) {
 			str = str.substring(0, length);
 			if (addPoints) {
 				str += "...";
 			}
 			return str;
 		} else {
 			return str;
 		}
 	}
 
 	/**
 	 * Strips line breaking characters from text.
 	 * 
 	 * @param text
 	 *            text
 	 * @return linf of text
 	 */
 	public static String stripNewLine(String text) {
 		if (text == null) {
 			return "";
 		}
 		return text.replaceAll("\n\r|\r\n|\n \r|\r \n|\n|\r", " ");
 	}
 
 	/**
 	 * Get Option by is type.
 	 * 
 	 * @param options
 	 *            list of options
 	 * @param type
 	 *            type
 	 * @return resulting option or null
 	 */
 	public static ConflictOption getConflictOptionByType(List<ConflictOption> options, OptionType type) {
 		for (ConflictOption option : options) {
 			if (option.getType().equals(type)) {
 				return option;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks whether a conflict needs details.
 	 * 
 	 * @param conflict
 	 *            conflict
 	 * @return true, if so
 	 */
 	public static boolean detailsNeeded(Conflict conflict) {
 		if (!conflict.hasDetails()) {
 			return false;
 		}
 		for (ConflictOption option : conflict.getOptions()) {
 			if (!option.isDetailsProvider()) {
 				continue;
 			}
 			if (option.getDetailProvider().startsWith(DecisionUtil.WIDGET_MULTILINE)) {
 				if (option.getOptionLabel().length() > DecisionUtil.OPTION_LENGTH) {
 					return true;
 				}
 			} else {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Uses the object's toString method or returns unset string.
 	 * 
 	 * @param obj
 	 *            obj to string
 	 * @param unset
 	 *            unset string
 	 * @return obj.toString or unset
 	 */
 	public static String getLabel(Object obj, String unset) {
		return (obj != null && obj.toString().length() > 1) ? obj.toString() : unset;
 	}
 
 	/**
 	 * Returns Class and Name of {@link EObject}.
 	 * 
 	 * @param modelElement
 	 *            modelelement
 	 * @return string
 	 */
 	public static String getClassAndName(EObject modelElement) {
 		if (modelElement == null) {
 			return "";
 		}
 		return modelElement.eClass().getName() + " \"" + getModelElementName(modelElement) + "\"";
 	}
 
 	/**
 	 * Returns name for an element by using {@link MergeLabelProvider}.
 	 * 
 	 * @param modelElement specified element
 	 * @return name for element;
 	 */
 	public static String getModelElementName(EObject modelElement) {
 		MergeLabelProvider labelProvider = WorkspaceManager.getObserverBus().notify(MergeLabelProvider.class, true);
 		if (labelProvider == null) {
 			return modelElement.toString();
 		}
 		return labelProvider.getText(modelElement);
 	}
 }
