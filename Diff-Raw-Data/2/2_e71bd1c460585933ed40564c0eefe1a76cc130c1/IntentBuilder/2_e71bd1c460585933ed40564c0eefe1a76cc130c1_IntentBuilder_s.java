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
 package org.eclipse.mylyn.docs.intent.parser.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.document.IntentChapter;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentFactory;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 import org.eclipse.mylyn.docs.intent.parser.internal.state.IntentGenericState;
 import org.eclipse.mylyn.docs.intent.parser.internal.state.SChapter;
 import org.eclipse.mylyn.docs.intent.parser.internal.state.SDocument;
 import org.eclipse.mylyn.docs.intent.parser.internal.state.SSection;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.serializer.IntentPositionManager;
 
 /**
  * Builder for an IntentStructuredElement : use a state machine to build Intent elements according to the
  * signal sent by a IntentParser.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IntentBuilder {
 
 	private IntentPositionManager positionManager;
 
 	/**
 	 * CurrentRoot of the document : can be a IntentStructuredElement (Chapter, Section), a Modeling Unit or a
 	 * Description Unit.
 	 */
 	private EObject currentRoot;
 
 	/**
 	 * List of the created roots of the document.
 	 */
 	private List<EObject> roots;
 
 	/**
 	 * Represents the current State of the parser.
 	 */
 	private IntentGenericState currentState;
 
 	/**
 	 * Represents the currentImbricationLevel.
 	 */
 	private int currentImbricationLevel;
 
 	/**
 	 * IntentBuilder constructor.
 	 * 
 	 * @param positionManager
 	 *            the positionManager where to register positions
 	 */
 	public IntentBuilder(IntentPositionManager positionManager) {
 		roots = new ArrayList<EObject>();
 		this.positionManager = positionManager;
 	}
 
 	/**
 	 * Returns the generated roots of the parsed content.
 	 * 
 	 * @return the generated roots of the parsed content
 	 */
 	public List<EObject> getRoots() {
 		return roots;
 	}
 
 	/**
 	 * Returns the generated root of the parsed content.
 	 * 
 	 * @return the generated root of the parsed content
 	 * @throws ParseException
 	 *             if there isn't exactly one generated root
 	 */
 	public EObject getRoot() throws ParseException {
 		if (roots.size() != 1) {
 			throw new ParseException("The elements described should be enclosed in a super element. ");
 		}
 		if (currentImbricationLevel > 0) {
 			addStatusOnElement(roots.get(0), "Syntax error : insert \"}\" to close this element.");
 		}
 		return roots.get(0);
 	}
 
 	/**
 	 * Increases the current imbrication level.
 	 */
 	public void increaseImbricationLevel() {
 		this.currentImbricationLevel++;
 	}
 
 	/**
 	 * Decreases the current imbrication level.
 	 */
 	public void decreaseImbricationLevel() {
 		this.currentImbricationLevel--;
 	}
 
 	/**
 	 * Indicates the beginning of a Document.
 	 * 
 	 * @param offset
 	 *            the begin offset of the document
 	 * @param declarationLength
 	 *            the declaration length of the document
 	 * @throws ParseException
 	 *             if the document can't be opened in the current state of the parser
 	 */
 	public void beginDocument(int offset, int declarationLength) throws ParseException {
 		if (currentState == null) {
 			currentRoot = IntentDocumentFactory.eINSTANCE.createIntentDocument();
 			roots.add(currentRoot);
 			currentState = new SDocument(offset, declarationLength, null, (IntentDocument)currentRoot,
 					positionManager);
 			increaseImbricationLevel();
 		} else {
 			throw new ParseException("Can't open any document here.", offset, declarationLength);
 		}
 	}
 
 	/**
 	 * Indicates the beginning of a Chapter.
 	 * 
 	 * @param offset
 	 *            the begin offset of the Chapter
 	 * @param declarationLength
 	 *            the declaration length of the Chapter
 	 */
 	public void beginChapter(int offset, int declarationLength) {
 		increaseImbricationLevel();
 		if (currentState == null) {
 			currentRoot = IntentDocumentFactory.eINSTANCE.createIntentChapter();
 			roots.add(currentRoot);
 			currentState = new SChapter(offset, declarationLength, null, (IntentChapter)currentRoot,
 					positionManager);
 
 		} else {
 			currentState = currentState.beginChapter(offset, declarationLength);
 		}
 	}
 
 	/**
 	 * Indicates the beginning of a IntentSection.
 	 * 
 	 * @param offset
 	 *            the begin offset of the Section
 	 * @param declarationLength
 	 *            the declaration length of the Section
 	 */
 	public void beginSection(int offset, int declarationLength) {
 		increaseImbricationLevel();
 		if (currentState == null) {
 			currentRoot = IntentDocumentFactory.eINSTANCE.createIntentSection();
 			roots.add(currentRoot);
 			currentState = new SSection(offset, declarationLength, null, (IntentSection)currentRoot,
 					positionManager);
 		} else {
 			currentState = currentState.beginSection(offset, declarationLength);
 		}
 	}
 
 	/**
 	 * Indicates the end of a Structured Element (Document, Chapter or Section).
 	 * 
 	 * @param offset
 	 *            the ending offset of the structured element
 	 * @throws ParseException
 	 *             if there is no structured element to end
 	 */
 	public void endStructuredElement(int offset) throws ParseException {
 		if (currentImbricationLevel == 0) {
			throw new ParseException("There is no element to close.");
 		}
 		if (currentState != null) {
 			currentState = currentState.endStructuredElement(offset);
 			decreaseImbricationLevel();
 		} else {
 			addStatusOnElement(getRoot(), "Syntax Error on token \"{\" : no element to close.");
 		}
 
 	}
 
 	/**
 	 * Adds a status with the given message on the given element.
 	 * 
 	 * @param element
 	 *            the incorrect element
 	 * @param statusMessage
 	 *            the message of the status to add
 	 */
 	public void addStatusOnElement(EObject element, String statusMessage) {
 
 		if (element instanceof IntentGenericElement) {
 			CompilationStatus compilationStatus = CompilerFactory.eINSTANCE.createCompilationStatus();
 			compilationStatus.setMessage(statusMessage);
 			compilationStatus.setSeverity(CompilationStatusSeverity.ERROR);
 			compilationStatus.setTarget((IntentGenericElement)element);
 			compilationStatus.setType(CompilationMessageType.INVALID_REFERENCE_ERROR);
 			((IntentGenericElement)element).getCompilationStatus().add(compilationStatus);
 		}
 
 	}
 
 	/**
 	 * Indicates the detection of section options (visibility and header References).
 	 * 
 	 * @param visibility
 	 *            the visibility of the section ("hidden", "internal" or null)
 	 * @param headerReferences
 	 *            list of textual references to header declarations
 	 */
 	public void sectionOptions(String visibility, List<String> headerReferences) {
 		// System.out.println(getTab() + "IntentBuilder.sectionOptions() / " + currentState);
 		currentState = currentState.sectionOptions(visibility, headerReferences);
 	}
 
 	/**
 	 * Indicates a Modeling Unit with the given content.
 	 * 
 	 * @param offset
 	 *            the begin offset of the modeling unit
 	 * @param modelingUnitContent
 	 *            the content of this modeling Unit
 	 * @throws ParseException
 	 *             if the modeling unit parser detect any parse error
 	 */
 	public void modelingUnitContent(int offset, String modelingUnitContent) throws ParseException {
 		currentState = currentState.modelingUnitContent(offset, modelingUnitContent.length(),
 				modelingUnitContent);
 	}
 
 	/**
 	 * Indicates a Description Unit with the given Content.
 	 * 
 	 * @param offset
 	 *            the begin offset of the description unit
 	 * @param descriptionUnitContent
 	 *            the content of the description Unit
 	 * @throws ParseException
 	 *             if the description unit parser detect any parse error.
 	 */
 	public void descriptionUnitContent(int offset, String descriptionUnitContent) throws ParseException {
 		currentState = currentState.descriptionUnitContent(offset, descriptionUnitContent.length(),
 				formatUsingImbricationLevel(descriptionUnitContent, false));
 	}
 
 	/**
 	 * Uses the current imbrication level to remove non-relevant tabulations.
 	 * 
 	 * @param content
 	 *            the content to format
 	 * @param isModelingUnit
 	 *            indicates if the content describes a modelingUnit
 	 * @return the given content from which we removed any non-relevant tabulations
 	 */
 	private String formatUsingImbricationLevel(String content, boolean isModelingUnit) {
 		String intentContent = "";
 		String[] lines = content.split("\n");
 
 		// If the content is a Modeling Unit, we must temporary increase the imbrication level
 		int temporaryIncrease = 0;
 		if (isModelingUnit) {
 			temporaryIncrease++;
 		}
 
 		// For each line
 		for (int i = 0; i < lines.length; i++) {
 			String currentLine = lines[i];
 
 			// We remove the N first tabulations, with N equals to the currentImbricationLevel
 			for (int j = 0; j < this.currentImbricationLevel + temporaryIncrease; j++) {
 				if (currentLine.indexOf("\t") == 0) {
 					currentLine = currentLine.replaceFirst("\t", "");
 				}
 			}
 			intentContent += currentLine + "\n";
 		}
 		return intentContent;
 	}
 }
