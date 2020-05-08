 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.parser.modelingunit;
 
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.serializer.ModelingUnitSerializer;
 
 /**
  * Utilitary class which purpose is to format the textual form of a Modeling Unit.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class ModelingUnitFormatter {
 
 	/**
 	 * ModelingUnitFormatter constructor.
 	 */
 	private ModelingUnitFormatter() {
 
 	}
 
 	/**
 	 * Return the formatted form of the given textual form of a Modeling Unit.
 	 * 
 	 * @param modelingUnitToFormat
 	 *            textual form of a Modeling Unit
 	 * @return the formatted form of the given textual form of a Modeling Unit
 	 */
 	public static String format(String modelingUnitToFormat) {
 		String formattedString = modelingUnitToFormat.toString();
 		formattedString = formattedString.replaceAll(";[\t ]*\n[\t ]*", ";\n");
 
 		// We also remove the prefix and suffix for this modeling Unit
 		formattedString = formattedString.replaceAll(ModelingUnitParser.MODELING_UNIT_PREFIX + "\\s*", "");
 		formattedString = formattedString.replace(ModelingUnitParser.MODELING_UNIT_SUFFIX
 				+ ModelingUnitSerializer.LINE_BREAK, "");
 		return formattedString;
 	}
 
 	/**
 	 * Return the indented form of the given modeling unit.
 	 * 
 	 * @param modelingUnitToFormat
 	 *            the modeling unit to indent
 	 * @param serializer
 	 *            the serializer to use for updating element positions.
 	 * @return the indented form of the given modeling unit
 	 */
 	public static String indentAccordingToBrackets(ModelingUnitSerializer serializer,
 			String modelingUnitToFormat) {
 		return indentAccordingToBrackets(serializer, modelingUnitToFormat, 0, 0);
 	}
 
 	/**
 	 * Return the indented form of the given modeling unit.
 	 * 
 	 * @param modelingUnitToFormat
 	 *            the modeling unit to indent
 	 * @param serializer
 	 *            the serializer to use for updating element positions.
 	 * @param initialIndentLevel
 	 *            the initial indentation level of the modelingUnit
 	 * @param initialOffset
 	 *            the start offset of the modelingUnit to serialize
 	 * @return the indented form of the given modeling unit
 	 */
 	public static String indentAccordingToBrackets(ModelingUnitSerializer serializer,
 			String modelingUnitToFormat, int initialIndentLevel, int initialOffset) {
 
 		String indented = "";
 		String[] lines = modelingUnitToFormat.split(IntentKeyWords.INTENT_LINEBREAK);
 		int currentOffset = 0;
 		int nbTabulate = initialIndentLevel;
 		for (int i = 0; i < lines.length; i++) {
			if ((lines[i].contains(IntentKeyWords.INTENT_KEYWORD_CLOSE) && (!lines[i]
					.contains(IntentKeyWords.INTENT_KEYWORD_OPEN))) || lines[i].contains("M@")) {
 				nbTabulate--;
 			}
 			for (int j = 0; j < nbTabulate; j++) {
 
 				lines[i] = "\t" + lines[i];
 			}
 			if (nbTabulate > 0) {
 				serializer.addTabulations(currentOffset + initialOffset, nbTabulate);
 			}
 
 			if (lines[i].contains(IntentKeyWords.INTENT_KEYWORD_OPEN) || lines[i].contains("@M")) {
 				nbTabulate++;
 			}
 			indented += lines[i] + IntentKeyWords.INTENT_LINEBREAK;
 
 			if (lines[i].contains(IntentKeyWords.INTENT_KEYWORD_CLOSE)
 					&& (lines[i].contains(IntentKeyWords.INTENT_KEYWORD_OPEN))) {
 				nbTabulate--;
 			}
 			currentOffset = indented.length();
 
 		}
 		return indented;
 	}
 }
