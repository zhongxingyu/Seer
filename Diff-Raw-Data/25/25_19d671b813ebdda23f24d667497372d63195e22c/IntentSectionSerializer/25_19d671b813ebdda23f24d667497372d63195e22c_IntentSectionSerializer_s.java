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
 package org.eclipse.mylyn.docs.intent.serializer.internal;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSectionVisibility;
 
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.serializer.descriptionunit.DescriptionUnitSerializer;
 
 /**
  * Serialize an Intent section, and maintain an mapping between serialized elements and their
  * position in their serialized form.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class IntentSectionSerializer {
 
 	/**
 	 * IntentSectionSerializer constructor.
 	 */
 	private IntentSectionSerializer() {
 
 	}
 
 	/**
 	 * Return the serialized form of the given element.
 	 * 
 	 * @param section
 	 *            the element to serialize
 	 * @param serializer
 	 *            the dispatcher to call for serializing sub-elements
 	 * @return the serialized form of the given element
 	 */
 	public static String serialize(IntentSection section, IntentElementSerializer serializer) {
 		String renderedForm = serializer.tabulation();
 		int initalOffset = serializer.getCurrentOffset();
 
 		// Visibility declaration
 		if (!section.getVisibility().equals(IntentSectionVisibility.PUBLIC)) {
 			renderedForm += section.getVisibility().getLiteral().toLowerCase() + IntentKeyWords.INTENT_WHITESPACE;
 		}
 
 		renderedForm += IntentKeyWords.INTENT_KEYWORD_SECTION;
		int intitialLength = renderedForm.length();
 
 		// Header declarations
 		int numberOfHeaders = section.getImportedHeaders().size();
 		if (numberOfHeaders > 0) {
 			renderedForm += IntentKeyWords.INTENT_WHITESPACE + IntentKeyWords.INTENT_KEYWORD_HEADER_REFERENCE_OPEN;
 			for (int i = 0; i < section.getImportedHeaders().size(); i++) {
 				if (i != 0) {
 					renderedForm += IntentKeyWords.INTENT_KEYWORD_HEADER_REFERENCE_SEPARATOR;
 				}
 				renderedForm += section.getImportedHeaders().get(i);
 			}
 			renderedForm += IntentKeyWords.INTENT_KEYWORD_HEADER_CLOSE;
 		}
 		renderedForm += IntentKeyWords.INTENT_WHITESPACE + IntentKeyWords.INTENT_KEYWORD_OPEN;
 
 		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() + 1);
 		renderedForm += IntentKeyWords.INTENT_LINEBREAK;
 
 		// Section Title
 		if (section.getTitle() != null) {
 			DescriptionUnitSerializer descriptionUnitSerializer = new DescriptionUnitSerializer();
 			renderedForm += serializer.tabulation();
 			renderedForm += descriptionUnitSerializer.serializeSectionTitle(section.getTitle(), initalOffset
 					+ renderedForm.length())
 					+ IntentKeyWords.INTENT_LINEBREAK;
 			serializer.getPositionManager().addIntentPositionManagerInformations(
 					descriptionUnitSerializer.getPositionManager());
 		}
 
 		// Contents : subSection and Units
 		for (EObject content : section.getIntentContent()) {
 
 			serializer.setCurrentOffset(initalOffset + renderedForm.length());
 			renderedForm += serializer.serialize(content);
 		}
 
 		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() - 1);
 		renderedForm += serializer.tabulation() + IntentKeyWords.INTENT_KEYWORD_CLOSE + IntentKeyWords.INTENT_LINEBREAK;
 		serializer.setCurrentOffset(initalOffset + renderedForm.length());
 		serializer.setDeclarationPositionForElement(section, initalOffset, renderedForm.length(),
				intitialLength);
 		return renderedForm;
 	}
 }
