 /*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.common.counting;
 
 
 /**
  * Implementation of the GMX-V specification, v. 1.0 (http://www.lisa.org/fileadmin/standards/GMX-V.html) 
  * 
  * @version 0.1 07.07.2009
  */
 
 public class GMX {
 
 	final public static String extNamePrefix = "x-";
 	
 // 3.2. Word Count Categories
 	
 	/**
 	 * Total word count - an accumulation of the word counts, both translatable and non-translatable, 
 	 * from the individual text units that make up the document.
 	 */
 	final public static String TotalWordCount = "TotalWordCount";
 	
 	/**
 	 * An accumulation of the word count for text that has been marked as 'protected', or otherwise 
 	 * not translatable (XLIFF text enclosed in <mrk mtype="protected"> elements). 
 	 */
 	final public static String ProtectedWordCount = "ProtectedWordCount";
 
 	/**
 	 * An accumulation of the word count for text units that have been matched unambiguously with a 
 	 * prior translation and thus require no translator input.
 	 */
 	final public static String ExactMatchedWordCount = "ExactMatchedWordCount";
 
 	/**
 	 * An accumulation of the word count for text units that have been matched against a leveraged 
 	 * translation memory database.
 	 */
 	final public static String LeveragedMatchedWordCount = "LeveragedMatchedWordCount";
 
 	/**
 	 * An accumulation of the word count for repeating text units that have not been matched in any 
 	 * other form. Repetition matching is deemed to take precedence over fuzzy matching.
 	 */
 	final public static String RepetitionMatchedWordCount = "RepetitionMatchedWordCount";
 
 	/**
 	 * An accumulation of the word count for text units that have been fuzzy matched against a 
 	 * leveraged translation memory database. 
 	 */
 	final public static String FuzzyMatchedWordCount = "FuzzyMatchedWordCount";
 
 	/**
 	 * An accumulation of the word count for text units that have been identified as containing 
 	 * only alphanumeric words. 
 	 */
 	final public static String AlphanumericOnlyTextUnitWordCount = "AlphanumericOnlyTextUnitWordCount";
 
 	/**
 	 * An accumulation of the word count for text units that have been identified as containing 
 	 * only numeric words.
 	 */
 	final public static String NumericOnlyTextUnitWordCount = "NumericOnlyTextUnitWordCount";
 
 	/**
 	 * An accumulation of the word count from measurement-only text units.
 	 */
 	final public static String MeasurementOnlyTextUnitWordCount = "MeasurementOnlyTextUnitWordCount";
 	
 // 3.3. Auto Text Word Count Categories	
 
 	/**
 	 * An accumulation of the word count for simple numeric values, e.g. 10.
 	 */
 	final public static String SimpleNumericAutoTextWordCount = "SimpleNumericAutoTextWordCount";
 
 	/**
 	 * An accumulation of the word count for complex numeric values which include decimal 
 	 * and/or thousands separators, e.g. 10,000.00.
 	 */
 	final public static String ComplexNumericAutoTextWordCount = "ComplexNumericAutoTextWordCount";
 
 	/**
 	 * An accumulation of the word count for identifiable measurement values, e.g. 10.50 mm. 
 	 * Measurement values take precedent over the above numeric categories. No double counting 
 	 * of these categories is allowed.
 	 */
 	final public static String MeasurementAutoTextWordCount = "MeasurementAutoTextWordCount";
 
 	/**
 	 * An accumulation of the word count for identifiable alphanumeric words, e.g. AEG321. 
 	 */
 	final public static String AlphaNumericAutoTextWordCount = "AlphaNumericAutoTextWordCount";
 
 	/**
 	 * An accumulation of the word count for identifiable dates, e.g. 25 June 1992.
 	 */
 	final public static String DateAutoTextWordCount = "DateAutoTextWordCount";
 
 	/**
	 * An accumulation of the word count for identifiable trade marks, e.g. "Weapons of Mass Destruction™".
 	 */
 	final public static String TMAutoTextWordCount = "TMAutoTextWordCount";
 
 // 3.4. Character Count Categories	
 
 	/**
 	 * An accumulation of the character counts, both translatable and non-translatable, from the 
 	 * individual text units that make up the document. This count includes all non white space 
 	 * characters in the document (please refer to Section 2.7. White Space Characters for details 
 	 * of what constitutes white space characters), excluding inline markup and punctuation characters 
 	 * (please refer to Section 2.10. Punctuation Characters for details of what constitutes 
 	 * punctuation characters).
 	 */
 	final public static String TotalCharacterCount = "TotalCharacterCount";
 
 	/**
 	 * The total of all punctuation characters in the canonical form of text in the document that 
 	 * DO NOT form part of the character count as per section 2.10. Punctuation Characters.
 	 */
 	final public static String PunctuationCharacterCount = "PunctuationCharacterCount";
 
 	/**
 	 * The total of all white space characters in the canonical form of the text units in the document. 
 	 * Please refer to section 2.7. White Space Characters for a detailed explanation of how white 
 	 * space characters are identified and counted.
 	 */
 	final public static String WhiteSpaceCharacterCount = "WhiteSpaceCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text that has been marked as 'protected', or 
 	 * otherwise not translatable (XLIFF text enclosed in <mrk mtype="protected"> elements).
 	 */
 	final public static String ProtectedCharacterCount = "ProtectedCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text units that have been matched unambiguously 
 	 * with a prior translation and require no translator input.
 	 */
 	final public static String ExactMatchedCharacterCount = "ExactMatchedCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text units that have been matched against a 
 	 * leveraged translation memory database.
 	 */
 	final public static String LeveragedMatchedCharacterCount = "LeveragedMatchedCharacterCount";
 
 	/**
 	 * An accumulation of the character count for repeating text units that have not been matched 
 	 * in any other form. Repetition matching is deemed to take precedence over fuzzy matching.
 	 */
 	final public static String RepetitionMatchedCharacterCount = "RepetitionMatchedCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text units that have a fuzzy match against a 
 	 * leveraged translation memory database.
 	 */
 	final public static String FuzzyMatchedCharacterCount = "FuzzyMatchedCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text units that have been identified as 
 	 * containing only alphanumeric words.
 	 */
 	final public static String AlphanumericOnlyTextUnitCharacterCount = "AlphanumericOnlyTextUnitCharacterCount";
 
 	/**
 	 * An accumulation of the character count for text units that have been identified as 
 	 * containing only numeric words.
 	 */
 	final public static String NumericOnlyTextUnitCharacterCount = "NumericOnlyTextUnitCharacterCount";
 
 	/**
 	 * An accumulation of the character count from measurement-only text units.
 	 */
 	final public static String MeasurementOnlyTextUnitCharacterCount = "MeasurementOnlyTextUnitCharacterCount";
 	
 // 3.5. Auto Text Character Count Categories	
 
 	/**
 	 * An accumulation of the character count for simple numeric values, e.g. 10. 
 	 */
 	final public static String SimpleNumericAutoTextCharacterCount = "SimpleNumericAutoTextCharacterCount";
 
 	/**
 	 * An accumulation of the character count for complex numeric values which include decimal 
 	 * and/or thousands separators, e.g. 10,000.00.
 	 */
 	final public static String ComplexNumericAutoTextCharacterCount = "ComplexNumericAutoTextCharacterCount";
 
 	/**
 	 * An accumulation of the character count for identifiable measurement values, e.g. 10.50 mm. 
 	 * Measurement values take precedent over the above numeric categories. No double counting of these categories is allowed.
 	 */
 	final public static String MeasurementAutoTextCharacterCount = "MeasurementAutoTextCharacterCount";
 
 	/**
 	 * An accumulation of the character count for identifiable alphanumeric words, e.g. AEG321.
 	 */
 	final public static String AlphaNumericAutoTextCharacterCount = "AlphaNumericAutoTextCharacterCount";
 
 	/**
 	 * An accumulation of the character count for identifiable dates, e.g. 25 June 1992. 
 	 */
 	final public static String DateAutoTextCharacterCount = "DateAutoTextCharacterCount";
 
 	/**
	 * An accumulation of the character count for identifiable trade marks, e.g. "Weapons of Mass Destruction�". 
 	 */
 	final public static String TMAutoTextCharacterCount = "TMAutoTextCharacterCount";
 
 // 3.6. Inline Element Count Categories	
 
 	/**
 	 * The actual non-linking inline element count for unqualified (see Section 2.14.2 
 	 * Unqualified Text Units) text units. Please refer to Section 2.11. Inline Element Counts 
 	 * for a detailed explanation and examples for this category.
 	 */
 	final public static String TranslatableInlineCount = "TranslatableInlineCount";
 	
 // 3.7. Linking Inline Element Count Categories	
 
 	/**
 	 * The actual linking inline element count for unqualified (see Section 2.14.2 Unqualified 
 	 * Text Units) text units. Please refer to Section 2.12. Linking Inline Elements for a 
 	 * detailed explanation and examples for this category.
 	 */
 	final public static String TranslatableLinkingInlineCount = "TranslatableLinkingInlineCount";
 	
 // 3.8. Text Unit Counts	
 
 	/**
 	 * The total number of text units.
 	 */
 	final public static String TextUnitCount = "TextUnitCount";
 	
 // 3.9. Other Count Categories	
 
 	/**
 	 * The total number of files.
 	 */
 	final public static String FileCount = "FileCount";
 
 	/**
 	 * The total number of pages.
 	 */
 	final public static String PageCount = "PageCount";
 
 	/**
 	 * A count of the total number of screens.
 	 */
 	final public static String ScreenCount = "ScreenCount";
 	
 // 3.10. Project Specific Count Categories	
 
 	/**
 	 * The word count for text units that are identical within all files within a given project. 
 	 * The word count for the primary occurrence is not included in this count, only that of 
 	 * subsequent matches.
 	 */
 	final public static String ProjectRepetionMatchedWordCount = "ProjectRepetionMatchedWordCount";
 
 	/**
 	 * The word count for fuzzy matched text units within all files within a given project. The 
 	 * word count for the primary occurrence is not included in this count, only that of 
 	 * subsequent matches. 
 	 */
 	final public static String ProjectFuzzyMatchedWordCount = "ProjectFuzzyMatchedWordCount";
 
 	/**
 	 * The character count for text that is identical within all files within a given project. 
 	 * The character count for the primary occurrence is not included in this count, only that 
 	 * of subsequent matches.
 	 */
 	final public static String ProjectRepetionMatchedCharacterCount = "ProjectRepetionMatchedCharacterCount";
 
 	/**
 	 * The character count for fuzzy matched text within all files within a given project. The character 
 	 * count for the primary occurrence is not included in this count, only that of subsequent matches. 
 	 */
 	final public static String ProjectFuzzyMatchedCharacterCount = "ProjectFuzzyMatchedCharacterCount";
 
 	
 }
