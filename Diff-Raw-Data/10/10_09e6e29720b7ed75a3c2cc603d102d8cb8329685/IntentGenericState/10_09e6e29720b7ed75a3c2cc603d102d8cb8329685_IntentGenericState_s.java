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
 package org.eclipse.mylyn.docs.intent.parser.internal.state;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.serializer.IntentPositionManager;
 
 /**
  * Represents a generic State of a IntentDocument parsor StateMachine.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentGenericState {
 
 	/**
 	 * The position manager that handle the mapping between Intent element to positions.
 	 */
 	protected IntentPositionManager positionManager;
 
 	/**
 	 * The previous state of the stateMachine.
 	 */
 	protected IntentGenericState previous;
 
 	/**
 	 * The element currently being created.
 	 */
 	protected EObject currentElement;
 
 	/**
 	 * The current offset.
 	 */
 	protected int fOffset;
 
 	private int fDeclarationLength;
 
 	/**
 	 * BuilderState constructor.
 	 * 
 	 * @param offset
 	 *            the offset of the current element
 	 * @param declarationLength
 	 *            the declaration length of the current element
 	 * @param currentElement
 	 *            the current parsed element
 	 * @param previous
 	 *            the previous state of the state machine.
 	 * @param positionManager
 	 *            the positionManager where to register positions
 	 */
 	public IntentGenericState(int offset, int declarationLength, IntentGenericState previous,
 			EObject currentElement, IntentPositionManager positionManager) {
 		this.fOffset = offset;
 		this.previous = previous;
 		this.currentElement = currentElement;
 		this.positionManager = positionManager;
 		this.fDeclarationLength = declarationLength;
 	}
 
 	/**
 	 * Returns the previous state in the state machine.
 	 * 
 	 * @return the previous state in the state machine
 	 */
 	protected IntentGenericState previousState() {
 		return previous;
 	}
 
 	/**
 	 * Returns the offset of the current element.
 	 * 
 	 * @return the offset of the current element
 	 */
 	public int getOffset() {
 		return fOffset;
 	}
 
 	/**
 	 * Returns the declaration length of the current element.
 	 * 
 	 * @return the declaration length of the current element
 	 */
 	public int getDeclarationLength() {
 		return fDeclarationLength;
 	}
 
 	/**
 	 * Indicates the beginning of a Chapter.
 	 * 
 	 * @param offset
 	 *            the begin offset of the chapter
 	 * @param declarationLength
 	 *            the declaration length of the chapter
 	 * @param title
 	 *            the section title
 	 * @return the new state of the state machine
 	 * @throws ParseException
 	 *             if the title cannot be parsed
 	 */
 	public IntentGenericState beginChapter(int offset, int declarationLength, String title)
 			throws ParseException {
		throw new ParseException("Can't open any chapter here.", offset, declarationLength);
 	}
 
 	/**
 	 * Indicates the end of a Chapter.
 	 * 
 	 * @param offset
 	 *            the ending offset of the chapter
 	 * @return the new state of the state machine
 	 */
 	public IntentGenericState endStructuredElement(int offset) {
 		return this;
 	}
 
 	/**
 	 * Indicates the beginning of a IntentSection.
 	 * 
 	 * @param offset
 	 *            the Section offset
 	 * @param declarationLength
 	 *            the Section declaration length
 	 * @param title
 	 *            the section title
 	 * @return the new state of the state machine
 	 * @throws ParseException
 	 *             if the title cannot be parsed
 	 */
 	public IntentGenericState beginSection(int offset, int declarationLength, String title)
 			throws ParseException {
		throw new ParseException("Can't open any section here.", offset, declarationLength);
 	}
 
 	/**
 	 * Indicates the detection of section options (visibility).
 	 * 
 	 * @param visibility
 	 *            the visibility of the section ("hidden", "internal" or null)
 	 * @return the new state of the state machine
 	 */
 	public IntentGenericState sectionOptions(String visibility) {
 		return this;
 	}
 
 	/**
 	 * Indicates the end of a Modeling Unit with the given content.
 	 * 
 	 * @param offset
 	 *            the Modeling Unit offset
 	 * @param length
 	 *            the Modeling Unit length
 	 * @param modelingUnitContent
 	 *            the content of this modeling Unit
 	 * @return the new state of the state machine
 	 * @throws ParseException
 	 *             if the modeling unit parser detect any parse error
 	 */
 	public IntentGenericState modelingUnitContent(int offset, int length, String modelingUnitContent)
 			throws ParseException {
		throw new ParseException("Can't open any modeling unit here.", offset, length);
 	}
 
 	/**
 	 * Indicates a Description Unit with the given Content.
 	 * 
 	 * @param offset
 	 *            the Description Unit offset
 	 * @param length
 	 *            the Description Unit length
 	 * @param descriptionUnitContent
 	 *            the content of the description Unit
 	 * @return the new state of the state machine
 	 * @throws ParseException
 	 *             if the description unit parser detect any parse error
 	 */
 	public IntentGenericState descriptionUnitContent(int offset, int length, String descriptionUnitContent)
 			throws ParseException {
 		return this;
 	}
 
 	public EObject getCurrentElement() {
 		return currentElement;
 	}
 }
