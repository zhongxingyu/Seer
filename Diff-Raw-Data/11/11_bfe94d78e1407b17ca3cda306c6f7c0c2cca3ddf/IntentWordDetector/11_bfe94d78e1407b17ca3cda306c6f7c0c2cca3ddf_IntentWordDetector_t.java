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
 package org.eclipse.mylyn.docs.intent.client.ui.editor.scanner;
 
 import org.eclipse.jface.text.rules.IWordDetector;
 
 /**
  * Key Words detector for Intent Scanners.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentWordDetector implements IWordDetector {
 
	private static char[] standardCharacters = new char[] {'@', ';', '.',
 	};
 
 	private static char[] modelingUnitCharacters = new char[] {';', '+', '=',
 	};
 
 	private static char[] descriptionUnitCharacters = new char[] {
 			// '_', '*',
 			'(', ')', ',', ':', '?', '!', '-',
 	};
 
 	/**
 	 * Indicates if the Words to detect are coming from a ModelingUnit.
 	 */
 	private boolean isModelingUnit;
 
 	/**
 	 * Generic IntentKeyWordDetector constructor.
 	 */
 	public IntentWordDetector() {
 		this.isModelingUnit = false;
 	}
 
 	/**
 	 * IntentKeyWordDetector constructor.
 	 * 
 	 * @param isModelingUnit
 	 *            indicates if the Words to detect are coming from a ModelingUnit.
 	 */
 	public IntentWordDetector(boolean isModelingUnit) {
 		this.isModelingUnit = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
 	 */
 	public final boolean isWordStart(final char c) {
 		return Character.isLetter(c) || isStandardCharacter(c) || isStyleCharacter(c)
 				|| isModelingUnitCharater(c);
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
 	 */
 	public final boolean isWordPart(final char c) {
 		return Character.isLetterOrDigit(c) || isStandardCharacter(c) || isStyleCharacter(c)
 				|| isModelingUnitCharater(c);
 	}
 
 	/**
 	 * Indicates if the given character matches a modeling unit allowed character ('{', '}' ...).
 	 * 
 	 * @param c
 	 *            the character to study
 	 * @return true if the given character matches a modeling unit allowed character.
 	 */
 	private boolean isModelingUnitCharater(char c) {
 		boolean isModelingUnitCharater = false;
 		if (isModelingUnit) {
 			int count = 0;
 			while (!isModelingUnitCharater && count < modelingUnitCharacters.length) {
 				isModelingUnitCharater = c == modelingUnitCharacters[count];
 				count++;
 			}
 		}
 		return isModelingUnitCharater;
 	}
 
 	/**
 	 * Indicates if the given character is a standard character ('.', ':' ...).
 	 * 
 	 * @param c
 	 *            the character to study
 	 * @return true if the given character matches a modeling unit allowed character.
 	 */
 	private boolean isStandardCharacter(char c) {
 		boolean isStandardCharacter = false;
 		int count = 0;
 		while (!isStandardCharacter && count < standardCharacters.length) {
 			isStandardCharacter = c == standardCharacters[count];
 			count++;
 		}
 		return isStandardCharacter;
 	}
 
 	/**
 	 * Indicates if the given character matches a style declaration (@, *, _, ...).
 	 * 
 	 * @param c
 	 *            the character to study
 	 * @return true if the given character matches a style declaration, false otherwise.
 	 */
 	private boolean isStyleCharacter(char c) {
 		boolean isStyleCharacter = false;
 		int count = 0;
 		while (!isStyleCharacter && count < descriptionUnitCharacters.length) {
 			isStyleCharacter = c == descriptionUnitCharacters[count];
 			count++;
 		}
 		return isStyleCharacter;
 	}
 
 }
