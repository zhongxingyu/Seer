 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.parser.modelingunit;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentFactory;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSectionOrParagraphReference;
 import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.genericunit.TypeLabel;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.AffectationOperator;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.AnnotationDeclaration;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstructionReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.IntentSectionReferenceinModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.LabelinModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitFactory;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstructionReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NewObjectValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.TypeReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.parser.linker.ModelingUnitLinker;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.parser.utils.FileToStringConverter;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.parser.utils.Location;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.parser.utils.ModelingUnitContentManager;
 
 /**
  * Parser of a Modeling Unit implementation.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class ModelingUnitParserImpl implements ModelingUnitParser {
 
 	/**
 	 * Common expression for literals.
 	 */
 	private static final String STRING_REGEX = "([a-zA-z0-9.:_-]+)"; //$NON-NLS-1$
 
 	/**
 	 * Common expression for quoted strings.
 	 */
 	private static final String STRING_WITH_QUOTES_REGEX = "(\"[^\"]*\")"; //$NON-NLS-1$
 
 	/**
 	 * Common token delimiter.
 	 */
 	private static final String TOKEN_DELIMITER = ","; //$NON-NLS-1$
 
 	/**
 	 * StandaloneParsingTests constructor.
 	 */
 	public ModelingUnitParserImpl() {
 		init();
 	}
 
 	/**
 	 * Returns true if the given String can be parsed by this parser.
 	 * 
 	 * @param contentToParse
 	 *            the content to inspect
 	 * @return true if the given string can be parsed by this parser (i.e is a modeling Unit : '@M ... M@')
 	 */
 	public boolean isParserFor(String contentToParse) {
 		return contentToParse.startsWith(MODELING_UNIT_PREFIX)
 				&& contentToParse.endsWith(MODELING_UNIT_SUFFIX);
 	}
 
 	/**
 	 * Launch the parser in standalone mode and register EPackages.
 	 */
 	private void init() {
 		registerEPackages();
 	}
 
 	/**
 	 * Register the EPackages needed by the parser.
 	 */
 	private void registerEPackages() {
 		GenericUnitPackage.eINSTANCE.eClass();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParser#parseFile(java.lang.String)
 	 */
 	public EObject parseFile(String filePath) throws ParseException, IOException {
 		String contentToParse = FileToStringConverter.getFileAsString(new File(filePath));
 		return parseString(0, contentToParse);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.parser.modelingunit.ModelingUnitParser#parseString(int,
 	 *      java.lang.String)
 	 */
 	public EObject parseString(int rootOffset, String stringToParse) throws ParseException {
 		// Root creation
 		ModelingUnit modelingUnit = ModelingUnitFactory.eINSTANCE.createModelingUnit();
 		Pattern modelingUnitPattern = Pattern.compile("@M([ \t\f]+" + STRING_REGEX + ")?([ \t\f]+\\[(" //$NON-NLS-1$ //$NON-NLS-2$
 				+ STRING_REGEX + ")\\])?\\s*"); //$NON-NLS-1$
 		Matcher matcher = modelingUnitPattern.matcher(stringToParse);
 		matcher.find();
 		if (matcher.group(2) != null) {
 			modelingUnit.setUnitName(matcher.group(2));
 		}
 		if (matcher.group(4) != null) {
 			ResourceReference ref = ModelingUnitFactory.eINSTANCE.createResourceReference();
 			ref.setLineBreak(true);
 			ref.setIntentHref(matcher.group(4));
 			modelingUnit.setResource(ref);
 		}
 
 		int startOffset = matcher.group().length();
 		int endOffset = stringToParse.lastIndexOf("M@");
 
 		// Content detection
 		ModelingUnitContentManager<UnitInstruction> manager = new ModelingUnitContentManager<UnitInstruction>();
 		manager.addAllContent(getResourceDeclarations(rootOffset, stringToParse));
 		manager.addAllContent(getInstanciationInstructions(rootOffset, stringToParse, true));
 		manager.addAllContent(getContributionInstructions(rootOffset, stringToParse, true));
 		manager.addAllContent(getIntentSectionReferencesinModelingUnit(stringToParse));
 		manager.addAllContent(getAnnotationDeclarations(stringToParse));
 		manager.addAllContent(getLabelsinModelingUnit(stringToParse));
 		manager.validateContent(stringToParse, startOffset, endOffset, rootOffset);
 
 		modelingUnit.getInstructions().addAll(manager.getContent().values());
 
 		// Link resolver call
 		new ModelingUnitLinker().resolveInternalLinks(modelingUnit);
 		return modelingUnit;
 	}
 
 	/**
 	 * Detects and instantiates {@link ContributionInstruction} occurrences in the given string.
 	 * 
 	 * @param rootOffset
 	 *            the root offset, used to compute errors locations
 	 * @param string
 	 *            the string to analyze
 	 * @param lineBreak
 	 *            indicates whether the instruction happens after a line break or not
 	 * @return the map of occurrences found by start offset
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<Location, UnitInstruction> getContributionInstructions(int rootOffset, String string,
 			boolean lineBreak) throws ParseException {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern startPattern = Pattern.compile("^\\s*" + STRING_REGEX + "\\s*\\{\\s*", Pattern.MULTILINE //$NON-NLS-1$ //$NON-NLS-2$
 				| Pattern.DOTALL);
 
 		Matcher matcher = startPattern.matcher(string);
 		int index = 0;
 		while (matcher.find(index)) {
 			ContributionInstruction instance = ModelingUnitFactory.eINSTANCE.createContributionInstruction();
 			instance.setLineBreak(lineBreak);
 
 			ModelingUnitInstructionReference ref = ModelingUnitFactory.eINSTANCE
 					.createModelingUnitInstructionReference();
 			ref.setIntentHref(matcher.group(1));
 			instance.setReferencedElement(ref);
 
 			// Content detection
 			index = matcher.group().length() + matcher.start();
 
 			try {
 				int endIndex = getEndIndex(string, index, '}');
 				String stringContent = string.substring(index, endIndex);
 
 				ModelingUnitContentManager<UnitInstruction> manager = new ModelingUnitContentManager<UnitInstruction>();
 				manager.addAllContent(getStructuralFeatureAffectations(rootOffset + index, stringContent));
 				manager.addAllContent(getIntentSectionReferencesinModelingUnit(stringContent));
 				manager.addAllContent(getAnnotationDeclarations(stringContent));
 				manager.addAllContent(getLabelsinModelingUnit(stringContent));
 				manager.validateContent(stringContent, index + rootOffset);
 
 				instance.getContributions().addAll(
 						(Collection<? extends ModelingUnitInstruction>)manager.getContent().values());
 				index = endIndex;
 			} catch (IndexOutOfBoundsException e) {
 				int spaceLength = 0;
 				Matcher spaceMatcher = Pattern.compile("^\\s+").matcher(matcher.group());
 				if (spaceMatcher.find()) {
 					spaceLength += spaceMatcher.group().length();
 				}
 				throw new ParseException(
 						Messages.getString(
 								"ModelingUnitParserImpl.INCORRECT_CONTRIBUTION_END", matcher.group().trim()), spaceLength + rootOffset + matcher.start(), matcher.group().trim().length()); //$NON-NLS-1$
 			}
 
 			res.put(new Location(matcher.start(), index), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates {@link InstanciationInstruction} occurrences in the given string.
 	 * 
 	 * @param rootOffset
 	 *            the root offset, used to compute errors locations
 	 * @param string
 	 *            the string to analyze
 	 * @param lineBreak
 	 *            indicates whether the instruction happens after a line break or not
 	 * @return the map of occurrences found by start offset
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<Location, UnitInstruction> getInstanciationInstructions(int rootOffset, String string,
 			boolean lineBreak) throws ParseException {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern startPattern = Pattern.compile("new\\s+" + STRING_REGEX + "(\\s+" + STRING_REGEX //$NON-NLS-1$ //$NON-NLS-2$
 				+ ")?\\s*\\{\\s*", Pattern.MULTILINE | Pattern.DOTALL); //$NON-NLS-1$
 
 		Matcher matcher = startPattern.matcher(string);
 		int index = 0;
 		while (matcher.find(index)) {
 			InstanciationInstruction instance = ModelingUnitFactory.eINSTANCE
 					.createInstanciationInstruction();
 			instance.setLineBreak(lineBreak);
 			TypeReference typeReference = ModelingUnitFactory.eINSTANCE.createTypeReference();
 			if (matcher.group(3) != null) {
 				instance.setName(matcher.group(3));
 			}
 			typeReference.setIntentHref(matcher.group(1));
 			instance.setMetaType(typeReference);
 
 			// Content detection
 			index = matcher.group().length() + matcher.start();
 			try {
 				int endIndex = getEndIndex(string, index, '}');
 				String stringContent = string.substring(index, endIndex);
 				ModelingUnitContentManager<UnitInstruction> manager = new ModelingUnitContentManager<UnitInstruction>();
 				manager.addAllContent(getStructuralFeatureAffectations(rootOffset + index, stringContent));
 				manager.validateContent(stringContent, rootOffset + index);
 
 				instance.getStructuralFeatures().addAll(
 						(Collection<? extends StructuralFeatureAffectation>)manager.getContent().values());
 				index = endIndex;
 			} catch (IndexOutOfBoundsException e) {
 				throw new ParseException(
 						Messages.getString(
 								"ModelingUnitParserImpl.INCORRECT_INSTANCIATION_END", matcher.group().trim()), rootOffset + matcher.start(), matcher.group().trim().length()); //$NON-NLS-1$
 			}
 
 			res.put(new Location(matcher.start(), index), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates {@link ResourceDeclaration} occurrences in the given string.
 	 * 
 	 * @param rootOffset
 	 *            the root offset, used to compute errors locations
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private Map<Location, UnitInstruction> getResourceDeclarations(int rootOffset, String string)
 			throws ParseException {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern startPattern = Pattern.compile("Resource\\s+" + STRING_REGEX + "\\s*\\{", //$NON-NLS-1$ //$NON-NLS-2$
 				Pattern.MULTILINE | Pattern.DOTALL);
 
 		Matcher matcher = startPattern.matcher(string);
 		int index = 0;
 		while (matcher.find(index)) {
 			ResourceDeclaration instance = ModelingUnitFactory.eINSTANCE.createResourceDeclaration();
 			instance.setLineBreak(true); // fixed by default
 			instance.setName(matcher.group(1));
 
 			// Content detection
 			index = matcher.group().length() + matcher.start();
 			try {
 				int endIndex = getEndIndex(string, index, '}');
 				String stringContent = string.substring(index, endIndex);
 
 				ModelingUnitContentManager<Object> manager = new ModelingUnitContentManager<Object>();
 				for (Affectation affectation : getAllAffectations(rootOffset + index, stringContent)) {
 					if ("URI".equals(affectation.key)) { //$NON-NLS-1$
 						instance.setUri(affectation.values.get(0));
 						manager.addContent(affectation.location, "URI");
 					} else if ("contentType".equals(affectation.key)) { //$NON-NLS-1$
 						instance.setContentType(affectation.values.get(0));
 						manager.addContent(affectation.location, "contentType");
 					} else if ("content".equals(affectation.key)) { //$NON-NLS-1$
 						for (String value : affectation.values) {
 							ModelingUnitInstructionReference ref = ModelingUnitFactory.eINSTANCE
 									.createModelingUnitInstructionReference();
 							ref.setIntentHref(value);
 							instance.getContent().add(ref);
 						}
 						manager.addContent(affectation.location, "content");
 					}
 				}
 				manager.validateContent(stringContent, rootOffset + index);
 				index = endIndex;
 			} catch (IndexOutOfBoundsException e) {
 				throw new ParseException(
 						Messages.getString(
 								"ModelingUnitParserImpl.INCORRECT_RESOURCE_DECLARATION_END", matcher.group().trim()), rootOffset + matcher.start(), matcher.group().length()); //$NON-NLS-1$
 			}
 			res.put(new Location(matcher.start(), index), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates {@link IntentSectionReferenceinModelingUnit} occurrences in the given string.
 	 * 
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 */
 	private Map<Location, UnitInstruction> getIntentSectionReferencesinModelingUnit(String string) {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern pattern = Pattern.compile("@see\\s+" + STRING_WITH_QUOTES_REGEX + "(\\s+(" //$NON-NLS-1$ //$NON-NLS-2$
 				+ STRING_WITH_QUOTES_REGEX + "))?"); //$NON-NLS-1$
 		Matcher matcher = pattern.matcher(string);
 
 		while (matcher.find()) {
 			IntentSectionReferenceinModelingUnit instance = ModelingUnitFactory.eINSTANCE
 					.createIntentSectionReferenceinModelingUnit();
 			instance.setLineBreak(true); // fixed by default
 
 			IntentSectionOrParagraphReference ref = IntentDocumentFactory.eINSTANCE
 					.createIntentSectionOrParagraphReference();
 			ref.setIntentHref(matcher.group(1));
 			instance.setReferencedObject(ref);
 			if (matcher.group(3) != null) {
 				instance.setTextToPrint(matcher.group(3));
 			}
 
 			res.put(new Location(matcher.start(), matcher.end()), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates lazy {@link LabelinModelingUnit} occurrences in the given string.
 	 * 
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 */
 	private Map<Location, UnitInstruction> getLabelsinModelingUnit(String string) {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern pattern = Pattern.compile("@(lazy)?label\\s+" + STRING_WITH_QUOTES_REGEX + "(\\s+(" //$NON-NLS-1$ //$NON-NLS-2$
 				+ STRING_WITH_QUOTES_REGEX + "))?"); //$NON-NLS-1$
 		Matcher matcher = pattern.matcher(string);
 
 		while (matcher.find()) {
 			LabelinModelingUnit instance = ModelingUnitFactory.eINSTANCE.createLabelinModelingUnit();
 			instance.setLabelValue(matcher.group(2));
 
 			instance.setLineBreak(true); // fixed by default
 
 			if ("lazy".equals(matcher.group(1))) { //$NON-NLS-1$
 				instance.setType(TypeLabel.LAZY);
 			} else {
 				instance.setType(TypeLabel.EXPLICIT);
 			}
 			if (matcher.group(4) != null) {
 				instance.setTextToPrint(matcher.group(4));
 			}
 			res.put(new Location(matcher.start(), matcher.end()), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates {@link AnnotationDeclaration} occurrences in the given string.
 	 * 
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 */
 	private Map<Location, UnitInstruction> getAnnotationDeclarations(String string) {
 		Map<Location, UnitInstruction> res = new HashMap<Location, UnitInstruction>();
 		Pattern pattern = Pattern.compile("@Annotation\\s+" + STRING_REGEX + "(.+)"); //$NON-NLS-1$ //$NON-NLS-2$
 		Matcher matcher = pattern.matcher(string);
 		while (matcher.find()) {
 			AnnotationDeclaration instance = ModelingUnitFactory.eINSTANCE.createAnnotationDeclaration();
 
 			instance.setLineBreak(true); // fixed by default
 			instance.setAnnotationID(matcher.group(1));
 
 			// Parameters map detection
 			for (String token : customTokenizer(matcher.group(2), TOKEN_DELIMITER)) {
 				Matcher entryMatcher = Pattern.compile(STRING_REGEX + "\\s*=\\s*" + STRING_WITH_QUOTES_REGEX, //$NON-NLS-1$
 						Pattern.MULTILINE | Pattern.DOTALL).matcher(token);
 				if (entryMatcher.find()) {
 					instance.getMap().put(entryMatcher.group(1), entryMatcher.group(2));
 				}
 			}
 
 			res.put(new Location(matcher.start(), matcher.end()), instance);
 		}
 		return res;
 	}
 
 	/**
 	 * Detects and instantiates {@link StructuralFeatureAffectation} occurrences in the given string.
 	 * 
 	 * @param rootOffset
 	 *            the root offset, used to compute errors locations
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private Map<Location, UnitInstruction> getStructuralFeatureAffectations(int rootOffset, String string)
 			throws ParseException {
 		ModelingUnitContentManager<UnitInstruction> manager = new ModelingUnitContentManager<UnitInstruction>();
 		for (Affectation affectation : getAllAffectations(rootOffset, string)) {
 			StructuralFeatureAffectation instance = ModelingUnitFactory.eINSTANCE
 					.createStructuralFeatureAffectation();
 			instance.setLineBreak(true); // fixed by default
 			instance.setName(affectation.key);
 			if (affectation.hasMultipleOperator) {
 				instance.setUsedOperator(AffectationOperator.MULTI_VALUED_AFFECTATION);
 			}
 			for (String valueContent : affectation.values) {
 				ValueForStructuralFeature value = getValueForStructuralFeature(rootOffset
 						+ affectation.keyLength, valueContent);
 				if (value != null) {
 					instance.getValues().add(value);
 				}
 			}
 			manager.addContent(affectation.location, instance);
 		}
 		return manager.getContent();
 	}
 
 	/**
 	 * Detects and instantiates {@link ValueForStructuralFeature} occurrences in the given string.
 	 * 
 	 * @param rootOffset
 	 *            the root offset, used to compute errors locations
 	 * @param string
 	 *            the string to analyze
 	 * @return the map of occurrences found by start offset
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private ValueForStructuralFeature getValueForStructuralFeature(int rootOffset, String string)
 			throws ParseException {
 		ValueForStructuralFeature res = null;
 		if (Pattern.compile(STRING_WITH_QUOTES_REGEX).matcher(string).matches()) {
 			NativeValueForStructuralFeature nativeValue = ModelingUnitFactory.eINSTANCE
 					.createNativeValueForStructuralFeature();
 			nativeValue.setValue(string.trim());
 			res = nativeValue;
 		} else if (Pattern.compile(STRING_REGEX).matcher(string).matches()) {
 			ReferenceValueForStructuralFeature referenceValue = ModelingUnitFactory.eINSTANCE
 					.createReferenceValueForStructuralFeature();
 			InstanciationInstructionReference referencedInstanciation = ModelingUnitFactory.eINSTANCE
 					.createInstanciationInstructionReference();
 			referencedInstanciation.setIntentHref(string);
 			referenceValue.setReferencedElement(referencedInstanciation);
 			res = referenceValue;
 		} else {
 			Map<Location, UnitInstruction> instructions = getInstanciationInstructions(rootOffset, string,
 					false);
 			if (!instructions.isEmpty()) {
 				NewObjectValueForStructuralFeature newValue = ModelingUnitFactory.eINSTANCE
 						.createNewObjectValueForStructuralFeature();
 				newValue.setValue((InstanciationInstruction)instructions.values().iterator().next());
 				res = newValue;
 			}
 		}
 		if (res == null) {
 			throw new ParseException("Unrecognized structural feature value", rootOffset, string.length());
 		}
 		return res;
 	}
 
 	/**
 	 * A data structure to store affectations.
 	 */
 	private class Affectation {
 
 		boolean hasMultipleOperator;
 
 		Location location;
 
 		int keyLength;
 
 		String key;
 
 		List<String> values = new ArrayList<String>();
 
 	}
 
 	/**
 	 * Computes the list of affectation in the given String.
 	 * 
 	 * @param offset
 	 *            the root offset
 	 * @param string
 	 *            the string to analyze
 	 * @return the list of affectations
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private List<Affectation> getAllAffectations(int offset, String string) throws ParseException {
 		List<Affectation> allAffectations = new ArrayList<Affectation>();
 		allAffectations.addAll(getSingleAffectations(offset, string));
 		allAffectations.addAll(getMultipleAffectations(offset, string));
 		return allAffectations;
 	}
 
 	/**
 	 * Computes the list of single affectation in the given String.
 	 * 
 	 * @param offset
 	 *            the root offset
 	 * @param string
 	 *            the string to analyze
 	 * @return the list of single affectations
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private List<Affectation> getSingleAffectations(int offset, String string) throws ParseException {
 		List<Affectation> res = new ArrayList<Affectation>();
 		Pattern startPattern = Pattern.compile(STRING_REGEX + "\\s*(\\+?)=\\s*"); //$NON-NLS-1$
 		Matcher matcher = startPattern.matcher(string);
 		int index = 0;
 		int middleOffset = 0;
 		while (matcher.find(index)) {
 			index = matcher.group().length() + matcher.start();
 			middleOffset = index;
 			try {
 				int endIndex = getEndIndex(string, index, ';');
 				String valuesContent = string.substring(index, endIndex);
 				if (!valuesContent.startsWith("[")) {
 					Affectation affectation = new Affectation();
 					affectation.hasMultipleOperator = !"".equals(matcher.group(2)); //$NON-NLS-1$
 					affectation.key = matcher.group(1);
 					affectation.values.add(valuesContent);
 					affectation.keyLength = middleOffset;
 					// index = the last endIndex
 					affectation.location = new Location(matcher.start(), endIndex);
 					res.add(affectation);
 				}
 				index = endIndex;
 			} catch (IndexOutOfBoundsException e) {
 				throw new ParseException(
 						Messages.getString(
 								"ModelingUnitParserImpl.INCORRECT_SINGLE_AFFECTATION_END", matcher.group().trim()), offset + matcher.start(), matcher.group().length()); //$NON-NLS-1$
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Computes the list of multiple affectation in the given String.
 	 * 
 	 * @param offset
 	 *            the root offset
 	 * @param string
 	 *            the string to analyze
 	 * @return the list of multiple affectations
 	 * @throws ParseException
 	 *             if there is an unclosed block
 	 */
 	private List<Affectation> getMultipleAffectations(int offset, String string) throws ParseException {
 		List<Affectation> res = new ArrayList<Affectation>();
 		Pattern startPattern = Pattern.compile(STRING_REGEX + "\\s*\\+=\\s*\\["); //$NON-NLS-1$
 		Matcher matcher = startPattern.matcher(string);
 		int index = 0;
 		int middleOffset = 0;
 		while (matcher.find(index)) {
 			Affectation affectation = new Affectation();
 			affectation.hasMultipleOperator = true;
 			affectation.key = matcher.group(1);
 			index = matcher.group().length() + matcher.start();
 			middleOffset = index;
 			try {
 				int endIndex = getEndIndex(string, index, ']');
 				String valuesContent = string.substring(index, endIndex);
 				affectation.values.addAll(customTokenizer(valuesContent, TOKEN_DELIMITER));
 				index = endIndex;
 			} catch (IndexOutOfBoundsException e) {
 				throw new ParseException(
 						Messages.getString(
 								"ModelingUnitParserImpl.INCORRECT_MULTIPLE_AFFECTATION_END", matcher.group().trim()), offset + matcher.start(), matcher.group().length()); //$NON-NLS-1$
 			}
 			affectation.keyLength = middleOffset;
 			affectation.location = new Location(matcher.start(), getEndIndex(string, index, ';'));
 			res.add(affectation);
 		}
 		return res;
 	}
 
 	/**
 	 * Computes the ending index of the current element.
 	 * 
 	 * @param string
 	 *            the entire string
 	 * @param start
 	 *            the offset where to start the lookup
 	 * @param end
 	 *            the looked up char
 	 * @return the ending index
 	 * @throws IndexOutOfBoundsException
 	 *             if no ending index has been found
 	 */
 	private static int getEndIndex(String string, int start, char end) throws IndexOutOfBoundsException {
 		char[] charArray = string.toCharArray();
 		for (int i = start; i < charArray.length; i++) {
 
 			char c = charArray[i];
 			// CHECKSTYLE:OFF : We modify the i control variable in order to "jump".
 			switch (charArray[i]) {
 				case '"':
 					i = string.indexOf('"', i + 1);
 					break;
 
 				case '{':
 					i = getEndIndex(string, i + 1, '}');
 					break;
 
 				case '[':
 					i = getEndIndex(string, i + 1, ']');
 					break;
 
 				default:
 					if (c == end) {
 						return i;
 					}
 					break;
 			}
 			// CHECKSTYLE:ON
 		}
 		throw new IndexOutOfBoundsException();
 	}
 
 	/**
 	 * A custom Tokenizer.
 	 * <ul>
 	 * <li>tokenizes aware of quotes</li>
 	 * <li>if no delimiter is found, returns one token anyway</li>
 	 * <li>trims all returned tokens</li>
 	 * </ul>
 	 * 
 	 * @param string
 	 *            the string to tokenize
 	 * @param delimiter
 	 *            the tokens delimiter
 	 * @return the list of found tokens
 	 */
 	private static List<String> customTokenizer(String string, String delimiter) {
 		final String skipQualifier = "@SKIPPED_QUOTE_";
 
 		// Detects and replace quoted strings by qualifiers
 		String textWithSkippedQuotes = string;
 		int index = 0;
 		List<String> skippedQuotes = new ArrayList<String>();
 		Matcher m = Pattern.compile(STRING_WITH_QUOTES_REGEX).matcher(string);
 		while (m.find()) {
 			if (m.group(1) != null) {
 				textWithSkippedQuotes = textWithSkippedQuotes.replaceFirst(m.group(1), skipQualifier //$NON-NLS-1$
 						+ index++);
 				skippedQuotes.add(m.group(1));
 			}
 		}
 
 		// Tokenizes, then replace quoted string qualifiers and trims
 		List<String> res = new ArrayList<String>();
 		Pattern quotesPattern = Pattern.compile(skipQualifier + "([0-9])+"); //$NON-NLS-1$
 		if (textWithSkippedQuotes.contains(delimiter)) {
 			StringTokenizer tokenizer = new StringTokenizer(textWithSkippedQuotes, ","); //$NON-NLS-1$
 			while (tokenizer.hasMoreTokens()) {
 				String token = tokenizer.nextToken();
 				Matcher quotesMatcher = quotesPattern.matcher(token);
 				while (quotesMatcher.find()) {
 					token = token.replaceFirst(quotesMatcher.group(),
 							skippedQuotes.get(new Integer(quotesMatcher.group(1))));
 				}
 				res.add(token.trim());
 			}
 		} else {
 			Matcher quotesMatcher = quotesPattern.matcher(textWithSkippedQuotes);
 			while (quotesMatcher.find()) {
 				textWithSkippedQuotes = textWithSkippedQuotes.replaceFirst(quotesMatcher.group(),
 						skippedQuotes.get(new Integer(quotesMatcher.group(1))));
 			}
 			res.add(textWithSkippedQuotes);
 		}
 		return res;
 	}
 
 }
