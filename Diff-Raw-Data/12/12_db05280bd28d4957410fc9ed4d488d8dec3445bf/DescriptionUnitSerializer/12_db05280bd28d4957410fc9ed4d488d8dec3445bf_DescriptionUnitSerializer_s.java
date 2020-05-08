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
 package org.eclipse.mylyn.docs.intent.serializer.descriptionunit;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnit;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.markup.serializer.WikiTextSerializer;
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.serializer.IntentPositionManager;
 import org.eclipse.mylyn.docs.intent.serializer.descriptionunit.internal.DescriptionUnitElementsDispatcher;
 import org.eclipse.mylyn.docs.intent.serializer.genericunit.GenericUnitSerializer;
 
 /**
  * Serializer for Description Units.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class DescriptionUnitSerializer {
 
 	/**
 	 * The serialize to use for serializing pure documentation content (here WikiText).
 	 */
 	private WikiTextSerializer markupSerializer;
 
 	/**
 	 * The dispatcher to use for serializing description units elements (like Description blocs).
 	 */
 	private DescriptionUnitElementsDispatcher descriptionUnitDispatcher;
 
 	/**
 	 * The Generic Unit dispatcher that handles all the generic unit common elements (like labels or
 	 * references).
 	 */
 	private GenericUnitSerializer genericDispatcher;
 
 	/**
 	 * The position manager.
 	 */
 	private IntentPositionManager positionManager;
 
 	/**
 	 * The current Offset of this Serializer.
 	 */
 	private int currentOffset;
 
 	/**
 	 * DescriptionUnitSerializer constructor.
 	 */
 	public DescriptionUnitSerializer() {
 		this.markupSerializer = new WikiTextSerializer();
 		this.descriptionUnitDispatcher = new DescriptionUnitElementsDispatcher(this);
 		this.genericDispatcher = new GenericUnitSerializer();
 		this.positionManager = new IntentPositionManager();
 	}
 
 	/**
 	 * Clears all informations contained by the serializer (elements positions, current offset...).
 	 */
 	public void clear() {
 		this.positionManager.clear();
 	}
 
 	/**
 	 * Returns the current Offset of this Serializer.
 	 * 
 	 * @return the current Offset of this Serializer
 	 */
 	public int getCurrentOffset() {
 		return currentOffset;
 	}
 
 	/**
 	 * Sets the current Offset of this Serializer.
 	 * 
 	 * @param offset
 	 *            the current Offset of this Serializer
 	 */
 	public void setCurrentOffset(int offset) {
 		this.currentOffset = offset;
 	}
 
 	/**
 	 * Return the serialized form of the given Description Unit.
 	 * 
 	 * @param elementToSerialize
 	 *            the descriptionUnit to serialize
 	 * @return the serialized form of the given Description Unit
 	 */
 	public String serialize(DescriptionUnit elementToSerialize) {
 		return this.serialize(elementToSerialize, "");
 	}
 
 	/**
 	 * Return the serialized form of the given Description Unit.
 	 * 
 	 * @param elementToSerialize
 	 *            the descriptionUnit to serialize
 	 * @param tabulationPrefix
 	 *            the prefix to use for each new line
 	 * @return the serialized form of the given Description Unit
 	 */
 	public String serialize(DescriptionUnit elementToSerialize, String tabulationPrefix) {
 		return this.serialize(elementToSerialize, "", 0);
 	}
 
 	/**
 	 * Return the serialized form of the given Description Unit.
 	 * 
 	 * @param elementToSerialize
 	 *            the descriptionUnit to serialize
 	 * @param tabulationPrefix
 	 *            the prefix to use for each new line
 	 * @param initialOffset
 	 *            the starting offset of this descriptionUnit
 	 * @return the serialized form of the given Description Unit
 	 */
 	public String serialize(DescriptionUnit elementToSerialize, String tabulationPrefix, int initialOffset) {
 		// We set the correct prefix to the used dispatchers
 		this.descriptionUnitDispatcher.setTabulationPrefix(tabulationPrefix);
 		this.genericDispatcher.setTabulationPrefix(tabulationPrefix);
 		// We calculate the prefix of this description unit
 		String renderedDescriptionUnit = "";
		String prefixForDescriptionUnit = IntentKeyWords.INTENT_LINEBREAK + tabulationPrefix;
 		int declarationOffset = initialOffset + prefixForDescriptionUnit.length();
 
 		// We use the correct dispatcher in order to serialize each instruction
 		for (UnitInstruction instruction : elementToSerialize.getInstructions()) {
 			this.setCurrentOffset(renderedDescriptionUnit.length() + declarationOffset);
 			if (instruction instanceof DescriptionUnitInstruction) {
 				renderedDescriptionUnit += descriptionUnitDispatcher.doSwitch(instruction);
 			} else {
 				String serializedInstruction = genericDispatcher.doSwitch(instruction);
 				this.getPositionManager().setPositionForInstruction(instruction, getCurrentOffset(),
 						serializedInstruction.length());
 				renderedDescriptionUnit += serializedInstruction;
 			}
 		}
 
 		this.positionManager.setPositionForInstruction(elementToSerialize, declarationOffset,
 				renderedDescriptionUnit.length());
 
 		// We finally add the prefix calculated before
 		renderedDescriptionUnit = prefixForDescriptionUnit + renderedDescriptionUnit;
 		return renderedDescriptionUnit;
 
 	}
 
 	/**
 	 * Serialize the given Section Title.
 	 * 
 	 * @param sectionTitle
 	 *            the section title to serialize
 	 * @param currentSerializerOffset
 	 *            the current offset
 	 * @return the serialized form of the given section title
 	 */
 	public String serializeSectionTitle(EObject sectionTitle, int currentSerializerOffset) {
 		String serializedForm = markupSerializer.serialize(sectionTitle);
 		this.positionManager.setPositionForInstruction(sectionTitle, currentSerializerOffset,
 				serializedForm.length());
 		return serializedForm;
 	}
 
 	/**
 	 * Returns the markupSerializer used to serialize documentation.
 	 * 
 	 * @return the markupSerializer
 	 */
 	public WikiTextSerializer getMarkupSerializer() {
 		return markupSerializer;
 	}
 
 	/**
 	 * Returns the position manager of this serializer.
 	 * 
 	 * @return the position manager of this serializer.
 	 */
 	public IntentPositionManager getPositionManager() {
 		return this.positionManager;
 	}
 }
