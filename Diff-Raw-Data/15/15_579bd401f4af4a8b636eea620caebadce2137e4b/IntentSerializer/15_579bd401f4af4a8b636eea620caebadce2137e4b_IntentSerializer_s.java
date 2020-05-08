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
 package org.eclipse.mylyn.docs.intent.serializer;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnit;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;

 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitFormatter;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.serializer.ModelingUnitSerializer;
 import org.eclipse.mylyn.docs.intent.serializer.descriptionunit.DescriptionUnitSerializer;
 import org.eclipse.mylyn.docs.intent.serializer.internal.IntentElementSerializer;
 
 /**
  * High-level serializer that delegates the serialization to the correct serializer according to the possible
  * entry points for a Modeling Unit.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentSerializer {
 
 	/**
 	 * The position manager that handle the mapping between Intent element to positions.
 	 */
 	private IntentPositionManager positionManager;
 
 	/**
 	 * Serializer for IntentStructuredElements : IntentDocument, Chapter and Section.
 	 */
 	private IntentElementSerializer documentSerializer;
 
 	/**
 	 * Serializer for Modeling Units.
 	 */
 	private ModelingUnitSerializer modelingUnitSerializer;
 
 	/**
 	 * Serializer for Description Units.
 	 */
 	private DescriptionUnitSerializer descriptionUnitSerializer;
 
 	/**
 	 * IntentSerializer constructor.
 	 */
 	public IntentSerializer() {
 		this.documentSerializer = new IntentElementSerializer(null, null);
 		this.modelingUnitSerializer = new ModelingUnitSerializer();
 		this.descriptionUnitSerializer = new DescriptionUnitSerializer();
 		this.positionManager = new IntentPositionManager();
 	}
 
 	/**
 	 * IntentSerializer constructor.
 	 * 
 	 * @param modelingUnitPrefixDecoration
 	 *            the String to use for prefixing each Modeling Unit (leave null if no use)
 	 * @param modelingUnitSuffixDecoration
 	 *            the String to use for suffixing each Modeling Unit (leave null if no use)
 	 */
 	public IntentSerializer(String modelingUnitPrefixDecoration, String modelingUnitSuffixDecoration) {
 		this.documentSerializer = new IntentElementSerializer(modelingUnitPrefixDecoration,
 				modelingUnitSuffixDecoration);
 		this.modelingUnitSerializer = new ModelingUnitSerializer();
 		this.descriptionUnitSerializer = new DescriptionUnitSerializer();
 		this.positionManager = new IntentPositionManager();
 	}
 
 	/**
 	 * Clears all informations contained by all used serializers (elements positions, current offset...).
 	 */
 	public void clear() {
 		this.positionManager.clear();
 		this.documentSerializer.clear();
 		this.descriptionUnitSerializer.clear();
 	}
 
 	/**
 	 * Serialize the given content and return the textual form of the given element.
 	 * 
 	 * @param elementToSerialize
	 *            the Intent entity to serialize (can be a IntentDocument, a Section, a Chapter, a
	 *            Modeling Unit or a Description Unit).
 	 * @return the textual form of the given elementToSerialize
 	 */
 	public String serialize(EObject elementToSerialize) {
		this.positionManager.clear();
 		if (elementToSerialize == null) {
 			throw new IllegalArgumentException("Cannot serialize a null element.");
 		}
 
 		String serializedForm = null;
 
 		// We have 3 possibilities for the type of the element to serialize :
 		if (elementToSerialize instanceof ModelingUnit) {
 			serializedForm = modelingUnitSerializer.serialize((ModelingUnit)elementToSerialize);
 			serializedForm = ModelingUnitFormatter.indentAccordingToBrackets(modelingUnitSerializer,
 					serializedForm);
 			positionManager.addIntentPositionManagerInformations(modelingUnitSerializer.getPositionManager());
 
 		}
 
 		// If it starts with a IntentDocument's Keyword (like "Section, Document, Chapter..."
 		if (elementToSerialize instanceof IntentStructuredElement) {
 			documentSerializer.setCurrentOffset(0);
 			documentSerializer.setCurrentIndendationLevel(0);
			serializedForm = documentSerializer.serialize((IntentStructuredElement)elementToSerialize);
			this.positionManager.addIntentPositionManagerInformations(documentSerializer.getPositionManager());
 
 		}
 		if (elementToSerialize instanceof DescriptionUnit) {
 			// In the other cases, we consider that the given contentToParse is a DescriptionUnit
 			documentSerializer.setCurrentOffset(0);
 			documentSerializer.setCurrentIndendationLevel(0);
 			serializedForm = descriptionUnitSerializer.serialize((DescriptionUnit)elementToSerialize);
 			this.positionManager.addIntentPositionManagerInformations(descriptionUnitSerializer
 					.getPositionManager());
 
 		}
 
 		if (serializedForm == null) {
 			throw new IllegalArgumentException(
 					"The element to serialize must be a valid Intent element, and not "
 							+ elementToSerialize.eClass().getName());
 		}
 		return serializedForm;
 
 	}
 
 	/**
 	 * Returns the position of the given instruction element.
 	 * 
 	 * @param element
 	 *            the element for witch we want the position
 	 * @return the position of the given instruction element (null if no position).
 	 */
 	public ParsedElementPosition getPositionForElement(EObject element) {
 		return this.positionManager.getPositionForElement(element);
 	}
 
 	/**
 	 * Returns the indentation level of the given element, or -1 if this level is unknown.
 	 * 
 	 * @param element
 	 *            the element from which we want to know the indentation level
 	 * @return the indentation level of the given element, or -1 if this level is unknown
 	 */
 	public int getIndentationLevelForElement(ModelingUnit element) {
 		return positionManager.getIndentationLevel(element);
 	}
 
 	/**
 	 * Indicates if the line at the given offset is a decoration line (and shouldn't be editable, for
 	 * example).
 	 * 
 	 * @param offset
 	 *            the offset of the line
 	 * @return true if the line at the given offset have been decorated, false otherwise.
 	 */
 	public boolean isDecorationLine(int offset) {
 		return positionManager.isDecorationLine(offset);
 	}
 
 	/**
 	 * Update the positions of element located after the given start offset, by adding the gap value to the
 	 * old position.
 	 * 
 	 * @param startOffset
 	 *            the start offset
 	 * @param gap
 	 *            the gap to add to each eleme,t
 	 */
 	public void updatePositions(int startOffset, int gap) {
 		positionManager.updatePositions(startOffset, gap);
 
 	}
 
 	/**
 	 * Returns the element corresponding to the given position.
 	 * 
 	 * @param offset
 	 *            the current offset
 	 * @return the element corresponding to the given position
 	 */
 	public EObject getElementAtPosition(int offset) {
 
 		return positionManager.getElementAtPosition(offset);
 	}
 }
