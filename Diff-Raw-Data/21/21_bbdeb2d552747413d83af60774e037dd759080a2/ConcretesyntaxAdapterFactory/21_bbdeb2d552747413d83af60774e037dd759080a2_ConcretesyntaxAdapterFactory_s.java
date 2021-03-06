 /**
  * Copyright (c) 2006-2010 
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Software Technology Group - TU Dresden, Germany 
  *       - initial API and implementation
  * 
  *
  * $Id$
  */
 package org.emftext.sdk.concretesyntax.util;
 
 import java.util.Map;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
 import org.eclipse.emf.ecore.EObject;
import org.emftext.sdk.concretesyntax.*;
 import org.emftext.sdk.concretesyntax.Abstract;
 import org.emftext.sdk.concretesyntax.AbstractTokenDefinition;
 import org.emftext.sdk.concretesyntax.Annotable;
 import org.emftext.sdk.concretesyntax.Annotation;
 import org.emftext.sdk.concretesyntax.AtomicRegex;
 import org.emftext.sdk.concretesyntax.Cardinality;
 import org.emftext.sdk.concretesyntax.CardinalityDefinition;
 import org.emftext.sdk.concretesyntax.Choice;
 import org.emftext.sdk.concretesyntax.CompleteTokenDefinition;
 import org.emftext.sdk.concretesyntax.CompoundDefinition;
 import org.emftext.sdk.concretesyntax.ConcreteSyntax;
 import org.emftext.sdk.concretesyntax.ConcretesyntaxPackage;
 import org.emftext.sdk.concretesyntax.Containment;
 import org.emftext.sdk.concretesyntax.CsString;
 import org.emftext.sdk.concretesyntax.Definition;
 import org.emftext.sdk.concretesyntax.EClassUtil;
 import org.emftext.sdk.concretesyntax.GenClassCache;
 import org.emftext.sdk.concretesyntax.GenPackageDependentElement;
 import org.emftext.sdk.concretesyntax.Import;
 import org.emftext.sdk.concretesyntax.KeyValuePair;
 import org.emftext.sdk.concretesyntax.LineBreak;
 import org.emftext.sdk.concretesyntax.NormalTokenDefinition;
 import org.emftext.sdk.concretesyntax.Option;
 import org.emftext.sdk.concretesyntax.PLUS;
 import org.emftext.sdk.concretesyntax.PartialTokenDefinition;
 import org.emftext.sdk.concretesyntax.Placeholder;
 import org.emftext.sdk.concretesyntax.PlaceholderInQuotes;
 import org.emftext.sdk.concretesyntax.PlaceholderUsingDefaultToken;
 import org.emftext.sdk.concretesyntax.PlaceholderUsingSpecifiedToken;
 import org.emftext.sdk.concretesyntax.QUESTIONMARK;
 import org.emftext.sdk.concretesyntax.QuotedTokenDefinition;
 import org.emftext.sdk.concretesyntax.RegexComposer;
 import org.emftext.sdk.concretesyntax.RegexComposite;
 import org.emftext.sdk.concretesyntax.RegexOwner;
 import org.emftext.sdk.concretesyntax.RegexPart;
 import org.emftext.sdk.concretesyntax.RegexReference;
 import org.emftext.sdk.concretesyntax.Rule;
 import org.emftext.sdk.concretesyntax.STAR;
 import org.emftext.sdk.concretesyntax.Sequence;
 import org.emftext.sdk.concretesyntax.SyntaxElement;
 import org.emftext.sdk.concretesyntax.Terminal;
 import org.emftext.sdk.concretesyntax.TokenDirective;
 import org.emftext.sdk.concretesyntax.TokenPriorityDirective;
 import org.emftext.sdk.concretesyntax.TokenStyle;
 import org.emftext.sdk.concretesyntax.WhiteSpaces;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Adapter Factory</b> for the model.
  * It provides an adapter <code>createXXX</code> method for each class of the model.
  * <!-- end-user-doc -->
  * @see org.emftext.sdk.concretesyntax.ConcretesyntaxPackage
  * @generated
  */
 public class ConcretesyntaxAdapterFactory extends AdapterFactoryImpl {
 	/**
 	 * The cached model package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static ConcretesyntaxPackage modelPackage;
 
 	/**
 	 * Creates an instance of the adapter factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ConcretesyntaxAdapterFactory() {
 		if (modelPackage == null) {
 			modelPackage = ConcretesyntaxPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Returns whether this factory is applicable for the type of the object.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
 	 * <!-- end-user-doc -->
 	 * @return whether this factory is applicable for the type of the object.
 	 * @generated
 	 */
 	@Override
 	public boolean isFactoryForType(Object object) {
 		if (object == modelPackage) {
 			return true;
 		}
 		if (object instanceof EObject) {
 			return ((EObject)object).eClass().getEPackage() == modelPackage;
 		}
 		return false;
 	}
 
 	/**
 	 * The switch that delegates to the <code>createXXX</code> methods.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ConcretesyntaxSwitch<Adapter> modelSwitch =
 		new ConcretesyntaxSwitch<Adapter>() {
 			@Override
 			public Adapter caseGenPackageDependentElement(GenPackageDependentElement object) {
 				return createGenPackageDependentElementAdapter();
 			}
 			@Override
 			public Adapter caseConcreteSyntax(ConcreteSyntax object) {
 				return createConcreteSyntaxAdapter();
 			}
 			@Override
 			public Adapter caseImport(Import object) {
 				return createImportAdapter();
 			}
 			@Override
 			public Adapter caseSyntaxElement(SyntaxElement object) {
 				return createSyntaxElementAdapter();
 			}
 			@Override
 			public Adapter caseRule(Rule object) {
 				return createRuleAdapter();
 			}
 			@Override
 			public Adapter caseChoice(Choice object) {
 				return createChoiceAdapter();
 			}
 			@Override
 			public Adapter caseSequence(Sequence object) {
 				return createSequenceAdapter();
 			}
 			@Override
 			public Adapter caseDefinition(Definition object) {
 				return createDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseCardinalityDefinition(CardinalityDefinition object) {
 				return createCardinalityDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseTerminal(Terminal object) {
 				return createTerminalAdapter();
 			}
 			@Override
 			public Adapter caseCsString(CsString object) {
 				return createCsStringAdapter();
 			}
 			@Override
 			public Adapter caseWhiteSpaces(WhiteSpaces object) {
 				return createWhiteSpacesAdapter();
 			}
 			@Override
 			public Adapter caseLineBreak(LineBreak object) {
 				return createLineBreakAdapter();
 			}
 			@Override
 			public Adapter caseCardinality(Cardinality object) {
 				return createCardinalityAdapter();
 			}
 			@Override
 			public Adapter casePLUS(PLUS object) {
 				return createPLUSAdapter();
 			}
 			@Override
 			public Adapter caseSTAR(STAR object) {
 				return createSTARAdapter();
 			}
 			@Override
 			public Adapter caseQUESTIONMARK(QUESTIONMARK object) {
 				return createQUESTIONMARKAdapter();
 			}
 			@Override
 			public Adapter caseCompoundDefinition(CompoundDefinition object) {
 				return createCompoundDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseTokenDirective(TokenDirective object) {
 				return createTokenDirectiveAdapter();
 			}
 			@Override
 			public Adapter caseRegexComposer(RegexComposer object) {
 				return createRegexComposerAdapter();
 			}
 			@Override
 			public Adapter caseRegexOwner(RegexOwner object) {
 				return createRegexOwnerAdapter();
 			}
 			@Override
 			public Adapter caseRegexPart(RegexPart object) {
 				return createRegexPartAdapter();
 			}
 			@Override
 			public Adapter caseRegexComposite(RegexComposite object) {
 				return createRegexCompositeAdapter();
 			}
 			@Override
 			public Adapter caseAtomicRegex(AtomicRegex object) {
 				return createAtomicRegexAdapter();
 			}
 			@Override
 			public Adapter caseRegexReference(RegexReference object) {
 				return createRegexReferenceAdapter();
 			}
 			@Override
 			public Adapter caseAbstractTokenDefinition(AbstractTokenDefinition object) {
 				return createAbstractTokenDefinitionAdapter();
 			}
 			@Override
 			public Adapter casePartialTokenDefinition(PartialTokenDefinition object) {
 				return createPartialTokenDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseCompleteTokenDefinition(CompleteTokenDefinition object) {
 				return createCompleteTokenDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseNormalTokenDefinition(NormalTokenDefinition object) {
 				return createNormalTokenDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseQuotedTokenDefinition(QuotedTokenDefinition object) {
 				return createQuotedTokenDefinitionAdapter();
 			}
 			@Override
 			public Adapter caseTokenPriorityDirective(TokenPriorityDirective object) {
 				return createTokenPriorityDirectiveAdapter();
 			}
 			@Override
 			public Adapter caseContainment(Containment object) {
 				return createContainmentAdapter();
 			}
 			@Override
 			public Adapter casePlaceholder(Placeholder object) {
 				return createPlaceholderAdapter();
 			}
 			@Override
 			public Adapter casePlaceholderUsingSpecifiedToken(PlaceholderUsingSpecifiedToken object) {
 				return createPlaceholderUsingSpecifiedTokenAdapter();
 			}
 			@Override
 			public Adapter casePlaceholderUsingDefaultToken(PlaceholderUsingDefaultToken object) {
 				return createPlaceholderUsingDefaultTokenAdapter();
 			}
 			@Override
 			public Adapter casePlaceholderInQuotes(PlaceholderInQuotes object) {
 				return createPlaceholderInQuotesAdapter();
 			}
 			@Override
 			public Adapter caseOption(Option object) {
 				return createOptionAdapter();
 			}
 			@Override
 			public Adapter caseAbstract(Abstract object) {
 				return createAbstractAdapter();
 			}
 			@Override
 			public Adapter caseTokenStyle(TokenStyle object) {
 				return createTokenStyleAdapter();
 			}
 			@Override
 			public Adapter caseAnnotation(Annotation object) {
 				return createAnnotationAdapter();
 			}
 			@Override
 			public Adapter caseAnnotable(Annotable object) {
 				return createAnnotableAdapter();
 			}
 			@Override
 			public Adapter caseKeyValuePair(KeyValuePair object) {
 				return createKeyValuePairAdapter();
 			}
 			@Override
 			public Adapter caseGenClassCache(GenClassCache object) {
 				return createGenClassCacheAdapter();
 			}
 			@Override
 			public Adapter caseGenClassCacheEntry(Map.Entry<GenClass, String> object) {
 				return createGenClassCacheEntryAdapter();
 			}
 			@Override
 			public Adapter caseEClassUtil(EClassUtil object) {
 				return createEClassUtilAdapter();
 			}
 			@Override
 			public Adapter defaultCase(EObject object) {
 				return createEObjectAdapter();
 			}
 		};
 
 	/**
 	 * Creates an adapter for the <code>target</code>.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param target the object to adapt.
 	 * @return the adapter for the <code>target</code>.
 	 * @generated
 	 */
 	@Override
 	public Adapter createAdapter(Notifier target) {
 		return modelSwitch.doSwitch((EObject)target);
 	}
 
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.GenPackageDependentElement <em>Gen Package Dependent Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.GenPackageDependentElement
 	 * @generated
 	 */
 	public Adapter createGenPackageDependentElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.ConcreteSyntax <em>Concrete Syntax</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.ConcreteSyntax
 	 * @generated
 	 */
 	public Adapter createConcreteSyntaxAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Import <em>Import</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Import
 	 * @generated
 	 */
 	public Adapter createImportAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Rule <em>Rule</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Rule
 	 * @generated
 	 */
 	public Adapter createRuleAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.SyntaxElement <em>Syntax Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.SyntaxElement
 	 * @generated
 	 */
 	public Adapter createSyntaxElementAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Choice <em>Choice</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Choice
 	 * @generated
 	 */
 	public Adapter createChoiceAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Sequence <em>Sequence</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Sequence
 	 * @generated
 	 */
 	public Adapter createSequenceAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Definition <em>Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Definition
 	 * @generated
 	 */
 	public Adapter createDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.CardinalityDefinition <em>Cardinality Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.CardinalityDefinition
 	 * @generated
 	 */
 	public Adapter createCardinalityDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Terminal <em>Terminal</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Terminal
 	 * @generated
 	 */
 	public Adapter createTerminalAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.CsString <em>Cs String</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.CsString
 	 * @generated
 	 */
 	public Adapter createCsStringAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.WhiteSpaces <em>White Spaces</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.WhiteSpaces
 	 * @generated
 	 */
 	public Adapter createWhiteSpacesAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.LineBreak <em>Line Break</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.LineBreak
 	 * @generated
 	 */
 	public Adapter createLineBreakAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Cardinality <em>Cardinality</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Cardinality
 	 * @generated
 	 */
 	public Adapter createCardinalityAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.PLUS <em>PLUS</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.PLUS
 	 * @generated
 	 */
 	public Adapter createPLUSAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.STAR <em>STAR</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.STAR
 	 * @generated
 	 */
 	public Adapter createSTARAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.QUESTIONMARK <em>QUESTIONMARK</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.QUESTIONMARK
 	 * @generated
 	 */
 	public Adapter createQUESTIONMARKAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.CompoundDefinition <em>Compound Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.CompoundDefinition
 	 * @generated
 	 */
 	public Adapter createCompoundDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.TokenDirective <em>Token Directive</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.TokenDirective
 	 * @generated
 	 */
 	public Adapter createTokenDirectiveAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.RegexComposer <em>Regex Composer</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.RegexComposer
 	 * @generated
 	 */
 	public Adapter createRegexComposerAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.RegexOwner <em>Regex Owner</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.RegexOwner
 	 * @generated
 	 */
 	public Adapter createRegexOwnerAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.RegexPart <em>Regex Part</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.RegexPart
 	 * @generated
 	 */
 	public Adapter createRegexPartAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.RegexComposite <em>Regex Composite</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.RegexComposite
 	 * @generated
 	 */
 	public Adapter createRegexCompositeAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.AtomicRegex <em>Atomic Regex</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.AtomicRegex
 	 * @generated
 	 */
 	public Adapter createAtomicRegexAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.RegexReference <em>Regex Reference</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.RegexReference
 	 * @generated
 	 */
 	public Adapter createRegexReferenceAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.AbstractTokenDefinition <em>Abstract Token Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.AbstractTokenDefinition
 	 * @generated
 	 */
 	public Adapter createAbstractTokenDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.PartialTokenDefinition <em>Partial Token Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.PartialTokenDefinition
 	 * @generated
 	 */
 	public Adapter createPartialTokenDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.CompleteTokenDefinition <em>Complete Token Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.CompleteTokenDefinition
 	 * @generated
 	 */
 	public Adapter createCompleteTokenDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.NormalTokenDefinition <em>Normal Token Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.NormalTokenDefinition
 	 * @generated
 	 */
 	public Adapter createNormalTokenDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.QuotedTokenDefinition <em>Quoted Token Definition</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.QuotedTokenDefinition
 	 * @generated
 	 */
 	public Adapter createQuotedTokenDefinitionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.TokenPriorityDirective <em>Token Priority Directive</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.TokenPriorityDirective
 	 * @generated
 	 */
 	public Adapter createTokenPriorityDirectiveAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Containment <em>Containment</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Containment
 	 * @generated
 	 */
 	public Adapter createContainmentAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Placeholder <em>Placeholder</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Placeholder
 	 * @generated
 	 */
 	public Adapter createPlaceholderAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.PlaceholderUsingSpecifiedToken <em>Placeholder Using Specified Token</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.PlaceholderUsingSpecifiedToken
 	 * @generated
 	 */
 	public Adapter createPlaceholderUsingSpecifiedTokenAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.PlaceholderUsingDefaultToken <em>Placeholder Using Default Token</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.PlaceholderUsingDefaultToken
 	 * @generated
 	 */
 	public Adapter createPlaceholderUsingDefaultTokenAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.PlaceholderInQuotes <em>Placeholder In Quotes</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.PlaceholderInQuotes
 	 * @generated
 	 */
 	public Adapter createPlaceholderInQuotesAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Option <em>Option</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Option
 	 * @generated
 	 */
 	public Adapter createOptionAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Abstract <em>Abstract</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Abstract
 	 * @generated
 	 */
 	public Adapter createAbstractAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.TokenStyle <em>Token Style</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.TokenStyle
 	 * @generated
 	 */
 	public Adapter createTokenStyleAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Annotation <em>Annotation</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Annotation
 	 * @generated
 	 */
 	public Adapter createAnnotationAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.Annotable <em>Annotable</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.Annotable
 	 * @generated
 	 */
 	public Adapter createAnnotableAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.KeyValuePair <em>Key Value Pair</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.KeyValuePair
 	 * @generated
 	 */
 	public Adapter createKeyValuePairAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.GenClassCache <em>Gen Class Cache</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.GenClassCache
 	 * @generated
 	 */
 	public Adapter createGenClassCacheAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>Gen Class Cache Entry</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see java.util.Map.Entry
 	 * @generated
 	 */
 	public Adapter createGenClassCacheEntryAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for an object of class '{@link org.emftext.sdk.concretesyntax.EClassUtil <em>EClass Util</em>}'.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null so that we can easily ignore cases;
 	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @see org.emftext.sdk.concretesyntax.EClassUtil
 	 * @generated
 	 */
 	public Adapter createEClassUtilAdapter() {
 		return null;
 	}
 
 	/**
 	 * Creates a new adapter for the default case.
 	 * <!-- begin-user-doc -->
 	 * This default implementation returns null.
 	 * <!-- end-user-doc -->
 	 * @return the new adapter.
 	 * @generated
 	 */
 	public Adapter createEObjectAdapter() {
 		return null;
 	}
 
 } //ConcretesyntaxAdapterFactory
