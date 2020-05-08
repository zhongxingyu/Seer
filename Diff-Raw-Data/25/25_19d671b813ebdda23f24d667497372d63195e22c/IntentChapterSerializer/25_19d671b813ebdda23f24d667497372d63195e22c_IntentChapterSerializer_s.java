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
 import org.eclipse.mylyn.docs.intent.core.document.IntentChapter;
 
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.serializer.descriptionunit.DescriptionUnitSerializer;
 
 /**
  * Serialize an Intent chapter, and maintain an mapping between serialized elements and their
  * position in their serialized form.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class IntentChapterSerializer {
 
 	/**
 	 * ChapterSerializer constructor.
 	 */
 	private IntentChapterSerializer() {
 
 	}
 
 	/**
 	 * Return the serialized form of the given element.
 	 * 
 	 * @param chapter
 	 *            the element to serialize
 	 * @param serializer
 	 *            the dispatcher to call for serializing sub-elements
 	 * @return the serialized form of the given element
 	 */
 	public static String serialize(IntentChapter chapter, IntentElementSerializer serializer) {
 		String renderedForm = serializer.tabulation();
 		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() + 1);
 		int initalOffset = serializer.getCurrentOffset();
 
 		renderedForm += IntentKeyWords.INTENT_KEYWORD_CHAPTER;
		int intitialLength = renderedForm.length();
 		renderedForm += IntentKeyWords.INTENT_WHITESPACE + IntentKeyWords.INTENT_KEYWORD_OPEN + IntentKeyWords.INTENT_LINEBREAK;
 
 		// Chapter Title
 		if (chapter.getTitle() != null) {
 			DescriptionUnitSerializer descriptionUnitSerializer = new DescriptionUnitSerializer();
 			renderedForm += serializer.tabulation();
 			renderedForm += descriptionUnitSerializer.serializeSectionTitle(chapter.getTitle(), initalOffset
 					+ renderedForm.length())
 					+ IntentKeyWords.INTENT_LINEBREAK;
 			serializer.getPositionManager().addIntentPositionManagerInformations(
 					descriptionUnitSerializer.getPositionManager());
 		}
 
 		// Content : subsection and Description Units
 		for (EObject content : chapter.getIntentContent()) {
 			serializer.setCurrentOffset(initalOffset + renderedForm.length());
 			renderedForm += serializer.serialize(content);
 		}
 
 		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() - 1);
 		renderedForm += serializer.tabulation() + IntentKeyWords.INTENT_KEYWORD_CLOSE + IntentKeyWords.INTENT_LINEBREAK;
 		serializer.setCurrentOffset(initalOffset + renderedForm.length());
 		serializer.setDeclarationPositionForElement(chapter, initalOffset, renderedForm.length(),
				intitialLength);
 		return renderedForm;
 	}
 }
