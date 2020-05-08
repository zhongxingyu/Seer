 /**
  * <copyright>
  *
  * Copyright (c) 2010 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: PivotValidator.java,v 1.8 2011/04/25 09:49:15 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.util;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Map;
 
import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.pivot.Annotation;
 import org.eclipse.ocl.examples.pivot.AnyType;
 import org.eclipse.ocl.examples.pivot.AssociationClass;
 import org.eclipse.ocl.examples.pivot.AssociationClassCallExp;
 import org.eclipse.ocl.examples.pivot.AssociativityKind;
 import org.eclipse.ocl.examples.pivot.BagType;
 import org.eclipse.ocl.examples.pivot.BooleanLiteralExp;
 import org.eclipse.ocl.examples.pivot.CallExp;
 import org.eclipse.ocl.examples.pivot.CallOperationAction;
 import org.eclipse.ocl.examples.pivot.ClassifierType;
 import org.eclipse.ocl.examples.pivot.CollectionItem;
 import org.eclipse.ocl.examples.pivot.CollectionKind;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralExp;
 import org.eclipse.ocl.examples.pivot.CollectionLiteralPart;
 import org.eclipse.ocl.examples.pivot.CollectionRange;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ConstructorExp;
 import org.eclipse.ocl.examples.pivot.ConstructorPart;
 import org.eclipse.ocl.examples.pivot.DataType;
 import org.eclipse.ocl.examples.pivot.Detail;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.EnumLiteralExp;
 import org.eclipse.ocl.examples.pivot.Enumeration;
 import org.eclipse.ocl.examples.pivot.EnumerationLiteral;
 import org.eclipse.ocl.examples.pivot.ExpressionInOcl;
 import org.eclipse.ocl.examples.pivot.Feature;
 import org.eclipse.ocl.examples.pivot.FeatureCallExp;
 import org.eclipse.ocl.examples.pivot.IfExp;
 import org.eclipse.ocl.examples.pivot.IntegerLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.IterateExp;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.IteratorExp;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.LetExp;
 import org.eclipse.ocl.examples.pivot.Library;
 import org.eclipse.ocl.examples.pivot.LiteralExp;
 import org.eclipse.ocl.examples.pivot.LoopExp;
 import org.eclipse.ocl.examples.pivot.MessageExp;
 import org.eclipse.ocl.examples.pivot.MessageType;
 import org.eclipse.ocl.examples.pivot.MultiplicityElement;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.NavigationCallExp;
 import org.eclipse.ocl.examples.pivot.NullLiteralExp;
 import org.eclipse.ocl.examples.pivot.NumericLiteralExp;
 import org.eclipse.ocl.examples.pivot.OclExpression;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OperationCallExp;
 import org.eclipse.ocl.examples.pivot.OperationTemplateParameter;
 import org.eclipse.ocl.examples.pivot.OrderedSetType;
 import org.eclipse.ocl.examples.pivot.PackageableElement;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.PrimitiveLiteralExp;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.PropertyCallExp;
 import org.eclipse.ocl.examples.pivot.RealLiteralExp;
 import org.eclipse.ocl.examples.pivot.SelfType;
 import org.eclipse.ocl.examples.pivot.SendSignalAction;
 import org.eclipse.ocl.examples.pivot.SequenceType;
 import org.eclipse.ocl.examples.pivot.SetType;
 import org.eclipse.ocl.examples.pivot.Signal;
 import org.eclipse.ocl.examples.pivot.State;
 import org.eclipse.ocl.examples.pivot.StateExp;
 import org.eclipse.ocl.examples.pivot.StringLiteralExp;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateParameterSubstitution;
 import org.eclipse.ocl.examples.pivot.TemplateParameterType;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.TupleLiteralExp;
 import org.eclipse.ocl.examples.pivot.TupleLiteralPart;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeExp;
 import org.eclipse.ocl.examples.pivot.TypeTemplateParameter;
 import org.eclipse.ocl.examples.pivot.TypedElement;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.UnlimitedNaturalLiteralExp;
 import org.eclipse.ocl.examples.pivot.UnspecifiedType;
 import org.eclipse.ocl.examples.pivot.UnspecifiedValueExp;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.Variable;
 import org.eclipse.ocl.examples.pivot.VariableDeclaration;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.VoidType;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Validator</b> for the model.
  * <!-- end-user-doc -->
  * @see org.eclipse.ocl.examples.pivot.PivotPackage
  * @generated
  */
 public class PivotValidator
 		extends EObjectValidator {
 
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final PivotValidator INSTANCE = new PivotValidator();
 
 	/**
 	 * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
 	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
 	 * @generated
 	 */
 	public static final String DIAGNOSTIC_SOURCE = "org.eclipse.ocl.examples.pivot"; //$NON-NLS-1$
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is Boolean' of 'Boolean Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int BOOLEAN_LITERAL_EXP__TYPE_IS_BOOLEAN = 1;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is Item Type' of 'Collection Item'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_ITEM__TYPE_IS_ITEM_TYPE = 2;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Bag Kind Is Bag' of 'Collection Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_LITERAL_EXP__BAG_KIND_IS_BAG = 3;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Sequence Kind Is Sequence' of 'Collection Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_LITERAL_EXP__SEQUENCE_KIND_IS_SEQUENCE = 4;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Ordered Set Kind Is Ordered Set' of 'Collection Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_LITERAL_EXP__ORDERED_SET_KIND_IS_ORDERED_SET = 5;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Set Kind Is Set' of 'Collection Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_LITERAL_EXP__SET_KIND_IS_SET = 6;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collection Kind Is Concrete' of 'Collection Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int COLLECTION_LITERAL_EXP__COLLECTION_KIND_IS_CONCRETE = 7;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Unique Name' of 'Constraint'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int CONSTRAINT__UNIQUE_NAME = 8;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Not Own Self' of 'Element'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ELEMENT__NOT_OWN_SELF = 9;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is Enumeration Type' of 'Enum Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ENUM_LITERAL_EXP__TYPE_IS_ENUMERATION_TYPE = 10;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Condition Type Is Boolean' of 'If Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int IF_EXP__CONDITION_TYPE_IS_BOOLEAN = 11;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is Integer' of 'Integer Literal Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int INTEGER_LITERAL_EXP__TYPE_IS_INTEGER = 12;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate One Initializer' of 'Iterate Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATE_EXP__ONE_INITIALIZER = 13;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Body Type Conforms To Result Type' of 'Iterate Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATE_EXP__BODY_TYPE_CONFORMS_TO_RESULT_TYPE = 14;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is Result Type' of 'Iterate Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATE_EXP__TYPE_IS_RESULT_TYPE = 15;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Iterator Type Is Source Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ITERATOR_TYPE_IS_SOURCE_ELEMENT_TYPE = 16;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Sorted By Element Type Is Source Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__SORTED_BY_ELEMENT_TYPE_IS_SOURCE_ELEMENT_TYPE = 17;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Sorted By Is Ordered If Source Is Ordered' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__SORTED_BY_IS_ORDERED_IF_SOURCE_IS_ORDERED = 18;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Sorted By Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__SORTED_BY_HAS_ONE_ITERATOR = 19;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Reject Or Select Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__REJECT_OR_SELECT_TYPE_IS_BOOLEAN = 20;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Reject Or Select Type Is Source Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__REJECT_OR_SELECT_TYPE_IS_SOURCE_TYPE = 21;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Reject Or Select Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__REJECT_OR_SELECT_HAS_ONE_ITERATOR = 22;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate One Body Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ONE_BODY_TYPE_IS_BOOLEAN = 23;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate One Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ONE_TYPE_IS_BOOLEAN = 24;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate One Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ONE_HAS_ONE_ITERATOR = 25;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Is Unique Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__IS_UNIQUE_TYPE_IS_BOOLEAN = 26;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Is Unique Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__IS_UNIQUE_HAS_ONE_ITERATOR = 27;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate For All Body Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__FOR_ALL_BODY_TYPE_IS_BOOLEAN = 28;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate For All Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__FOR_ALL_TYPE_IS_BOOLEAN = 29;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Exists Body Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__EXISTS_BODY_TYPE_IS_BOOLEAN = 30;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Exists Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__EXISTS_TYPE_IS_BOOLEAN = 31;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Nested Type Is Body Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_NESTED_TYPE_IS_BODY_TYPE = 32;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Nested Type Is Bag' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_NESTED_TYPE_IS_BAG = 33;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Nested Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_NESTED_HAS_ONE_ITERATOR = 34;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Element Type Is Source Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_ELEMENT_TYPE_IS_SOURCE_ELEMENT_TYPE = 35;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Type Is Unordered' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_TYPE_IS_UNORDERED = 36;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Collect Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__COLLECT_HAS_ONE_ITERATOR = 37;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Closure Element Type Is Source Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__CLOSURE_ELEMENT_TYPE_IS_SOURCE_ELEMENT_TYPE = 38;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Closure Source Element Type Is Body Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__CLOSURE_SOURCE_ELEMENT_TYPE_IS_BODY_ELEMENT_TYPE = 39;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Closure Type Is Unique Collection' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__CLOSURE_TYPE_IS_UNIQUE_COLLECTION = 40;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Closure Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__CLOSURE_HAS_ONE_ITERATOR = 41;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Any Body Type Is Boolean' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ANY_BODY_TYPE_IS_BOOLEAN = 42;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Any Type Is Source Element Type' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ANY_TYPE_IS_SOURCE_ELEMENT_TYPE = 43;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Any Has One Iterator' of 'Iterator Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int ITERATOR_EXP__ANY_HAS_ONE_ITERATOR = 44;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Type Is In Type' of 'Let Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int LET_EXP__TYPE_IS_IN_TYPE = 45;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate No Initializers' of 'Loop Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int LOOP_EXP__NO_INITIALIZERS = 46;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Source Is Collection' of 'Loop Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int LOOP_EXP__SOURCE_IS_COLLECTION = 47;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Target Is Not ACollection' of 'Message Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int MESSAGE_EXP__TARGET_IS_NOT_ACOLLECTION = 48;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate One Call Or One Send' of 'Message Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int MESSAGE_EXP__ONE_CALL_OR_ONE_SEND = 49;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Compatible Return' of 'Operation'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int OPERATION__COMPATIBLE_RETURN = 50;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Argument Count' of 'Operation Call Exp'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int OPERATION_CALL_EXP__ARGUMENT_COUNT = 51;
 
 	/**
 	 * The {@link org.eclipse.emf.common.util.Diagnostic#getCode() code} for constraint 'Validate Compatible Initialiser' of 'Property'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final int PROPERTY__COMPATIBLE_INITIALISER = 52;
 
 	/**
 	 * A constant with a fixed name that can be used as the base value for additional hand written constants.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 52;
 
 	/**
 	 * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public PivotValidator() {
 		super();
 	}
 
 	/**
 	 * Returns the package of this validator switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EPackage getEPackage() {
 	  return PivotPackage.eINSTANCE;
 	}
 
 	/**
 	 * Calls <code>validateXXX</code> for the corresponding classifier of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected boolean validate(int classifierID, Object value,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		switch (classifierID)
 		{
 			case PivotPackage.ANNOTATION:
 				return validateAnnotation((Annotation)value, diagnostics, context);
 			case PivotPackage.ANY_TYPE:
 				return validateAnyType((AnyType)value, diagnostics, context);
 			case PivotPackage.ASSOCIATION_CLASS:
 				return validateAssociationClass((AssociationClass)value, diagnostics, context);
 			case PivotPackage.ASSOCIATION_CLASS_CALL_EXP:
 				return validateAssociationClassCallExp((AssociationClassCallExp)value, diagnostics, context);
 			case PivotPackage.BAG_TYPE:
 				return validateBagType((BagType)value, diagnostics, context);
 			case PivotPackage.BOOLEAN_LITERAL_EXP:
 				return validateBooleanLiteralExp((BooleanLiteralExp)value, diagnostics, context);
 			case PivotPackage.CALL_EXP:
 				return validateCallExp((CallExp)value, diagnostics, context);
 			case PivotPackage.CALL_OPERATION_ACTION:
 				return validateCallOperationAction((CallOperationAction)value, diagnostics, context);
 			case PivotPackage.CLASS:
 				return validateClass((org.eclipse.ocl.examples.pivot.Class)value, diagnostics, context);
 			case PivotPackage.CLASSIFIER_TYPE:
 				return validateClassifierType((ClassifierType)value, diagnostics, context);
 			case PivotPackage.COLLECTION_ITEM:
 				return validateCollectionItem((CollectionItem)value, diagnostics, context);
 			case PivotPackage.COLLECTION_LITERAL_EXP:
 				return validateCollectionLiteralExp((CollectionLiteralExp)value, diagnostics, context);
 			case PivotPackage.COLLECTION_LITERAL_PART:
 				return validateCollectionLiteralPart((CollectionLiteralPart)value, diagnostics, context);
 			case PivotPackage.COLLECTION_RANGE:
 				return validateCollectionRange((CollectionRange)value, diagnostics, context);
 			case PivotPackage.COLLECTION_TYPE:
 				return validateCollectionType((CollectionType)value, diagnostics, context);
 			case PivotPackage.COMMENT:
 				return validateComment((Comment)value, diagnostics, context);
 			case PivotPackage.CONSTRAINT:
 				return validateConstraint((Constraint)value, diagnostics, context);
 			case PivotPackage.CONSTRUCTOR_EXP:
 				return validateConstructorExp((ConstructorExp)value, diagnostics, context);
 			case PivotPackage.CONSTRUCTOR_PART:
 				return validateConstructorPart((ConstructorPart)value, diagnostics, context);
 			case PivotPackage.DATA_TYPE:
 				return validateDataType((DataType)value, diagnostics, context);
 			case PivotPackage.DETAIL:
 				return validateDetail((Detail)value, diagnostics, context);
 			case PivotPackage.ELEMENT:
 				return validateElement((Element)value, diagnostics, context);
 			case PivotPackage.ENUM_LITERAL_EXP:
 				return validateEnumLiteralExp((EnumLiteralExp)value, diagnostics, context);
 			case PivotPackage.ENUMERATION:
 				return validateEnumeration((Enumeration)value, diagnostics, context);
 			case PivotPackage.ENUMERATION_LITERAL:
 				return validateEnumerationLiteral((EnumerationLiteral)value, diagnostics, context);
 			case PivotPackage.EXPRESSION_IN_OCL:
 				return validateExpressionInOcl((ExpressionInOcl)value, diagnostics, context);
 			case PivotPackage.FEATURE:
 				return validateFeature((Feature)value, diagnostics, context);
 			case PivotPackage.FEATURE_CALL_EXP:
 				return validateFeatureCallExp((FeatureCallExp)value, diagnostics, context);
 			case PivotPackage.IF_EXP:
 				return validateIfExp((IfExp)value, diagnostics, context);
 			case PivotPackage.INTEGER_LITERAL_EXP:
 				return validateIntegerLiteralExp((IntegerLiteralExp)value, diagnostics, context);
 			case PivotPackage.INVALID_LITERAL_EXP:
 				return validateInvalidLiteralExp((InvalidLiteralExp)value, diagnostics, context);
 			case PivotPackage.INVALID_TYPE:
 				return validateInvalidType((InvalidType)value, diagnostics, context);
 			case PivotPackage.ITERATE_EXP:
 				return validateIterateExp((IterateExp)value, diagnostics, context);
 			case PivotPackage.ITERATION:
 				return validateIteration((Iteration)value, diagnostics, context);
 			case PivotPackage.ITERATOR_EXP:
 				return validateIteratorExp((IteratorExp)value, diagnostics, context);
 			case PivotPackage.LAMBDA_TYPE:
 				return validateLambdaType((LambdaType)value, diagnostics, context);
 			case PivotPackage.LET_EXP:
 				return validateLetExp((LetExp)value, diagnostics, context);
 			case PivotPackage.LIBRARY:
 				return validateLibrary((Library)value, diagnostics, context);
 			case PivotPackage.LITERAL_EXP:
 				return validateLiteralExp((LiteralExp)value, diagnostics, context);
 			case PivotPackage.LOOP_EXP:
 				return validateLoopExp((LoopExp)value, diagnostics, context);
 			case PivotPackage.MESSAGE_EXP:
 				return validateMessageExp((MessageExp)value, diagnostics, context);
 			case PivotPackage.MESSAGE_TYPE:
 				return validateMessageType((MessageType)value, diagnostics, context);
 			case PivotPackage.MORE_PIVOTABLE:
 				return validateMorePivotable((MorePivotable)value, diagnostics, context);
 			case PivotPackage.MULTIPLICITY_ELEMENT:
 				return validateMultiplicityElement((MultiplicityElement)value, diagnostics, context);
 			case PivotPackage.NAMEABLE:
 				return validateNameable((Nameable)value, diagnostics, context);
 			case PivotPackage.NAMED_ELEMENT:
 				return validateNamedElement((NamedElement)value, diagnostics, context);
 			case PivotPackage.NAMESPACE:
 				return validateNamespace((Namespace)value, diagnostics, context);
 			case PivotPackage.NAVIGATION_CALL_EXP:
 				return validateNavigationCallExp((NavigationCallExp)value, diagnostics, context);
 			case PivotPackage.NULL_LITERAL_EXP:
 				return validateNullLiteralExp((NullLiteralExp)value, diagnostics, context);
 			case PivotPackage.NUMERIC_LITERAL_EXP:
 				return validateNumericLiteralExp((NumericLiteralExp)value, diagnostics, context);
 			case PivotPackage.OCL_EXPRESSION:
 				return validateOclExpression((OclExpression)value, diagnostics, context);
 			case PivotPackage.OPAQUE_EXPRESSION:
 				return validateOpaqueExpression((OpaqueExpression)value, diagnostics, context);
 			case PivotPackage.OPERATION:
 				return validateOperation((Operation)value, diagnostics, context);
 			case PivotPackage.OPERATION_CALL_EXP:
 				return validateOperationCallExp((OperationCallExp)value, diagnostics, context);
 			case PivotPackage.OPERATION_TEMPLATE_PARAMETER:
 				return validateOperationTemplateParameter((OperationTemplateParameter)value, diagnostics, context);
 			case PivotPackage.ORDERED_SET_TYPE:
 				return validateOrderedSetType((OrderedSetType)value, diagnostics, context);
 			case PivotPackage.PACKAGE:
 				return validatePackage((org.eclipse.ocl.examples.pivot.Package)value, diagnostics, context);
 			case PivotPackage.PACKAGEABLE_ELEMENT:
 				return validatePackageableElement((PackageableElement)value, diagnostics, context);
 			case PivotPackage.PARAMETER:
 				return validateParameter((Parameter)value, diagnostics, context);
 			case PivotPackage.PARAMETERABLE_ELEMENT:
 				return validateParameterableElement((ParameterableElement)value, diagnostics, context);
 			case PivotPackage.PIVOTABLE:
 				return validatePivotable((Pivotable)value, diagnostics, context);
 			case PivotPackage.PRECEDENCE:
 				return validatePrecedence((Precedence)value, diagnostics, context);
 			case PivotPackage.PRIMITIVE_LITERAL_EXP:
 				return validatePrimitiveLiteralExp((PrimitiveLiteralExp)value, diagnostics, context);
 			case PivotPackage.PRIMITIVE_TYPE:
 				return validatePrimitiveType((PrimitiveType)value, diagnostics, context);
 			case PivotPackage.PROPERTY:
 				return validateProperty((Property)value, diagnostics, context);
 			case PivotPackage.PROPERTY_CALL_EXP:
 				return validatePropertyCallExp((PropertyCallExp)value, diagnostics, context);
 			case PivotPackage.REAL_LITERAL_EXP:
 				return validateRealLiteralExp((RealLiteralExp)value, diagnostics, context);
 			case PivotPackage.SELF_TYPE:
 				return validateSelfType((SelfType)value, diagnostics, context);
 			case PivotPackage.SEND_SIGNAL_ACTION:
 				return validateSendSignalAction((SendSignalAction)value, diagnostics, context);
 			case PivotPackage.SEQUENCE_TYPE:
 				return validateSequenceType((SequenceType)value, diagnostics, context);
 			case PivotPackage.SET_TYPE:
 				return validateSetType((SetType)value, diagnostics, context);
 			case PivotPackage.SIGNAL:
 				return validateSignal((Signal)value, diagnostics, context);
 			case PivotPackage.STATE:
 				return validateState((State)value, diagnostics, context);
 			case PivotPackage.STATE_EXP:
 				return validateStateExp((StateExp)value, diagnostics, context);
 			case PivotPackage.STRING_LITERAL_EXP:
 				return validateStringLiteralExp((StringLiteralExp)value, diagnostics, context);
 			case PivotPackage.TEMPLATE_BINDING:
 				return validateTemplateBinding((TemplateBinding)value, diagnostics, context);
 			case PivotPackage.TEMPLATE_PARAMETER:
 				return validateTemplateParameter((TemplateParameter)value, diagnostics, context);
 			case PivotPackage.TEMPLATE_PARAMETER_SUBSTITUTION:
 				return validateTemplateParameterSubstitution((TemplateParameterSubstitution)value, diagnostics, context);
 			case PivotPackage.TEMPLATE_PARAMETER_TYPE:
 				return validateTemplateParameterType((TemplateParameterType)value, diagnostics, context);
 			case PivotPackage.TEMPLATE_SIGNATURE:
 				return validateTemplateSignature((TemplateSignature)value, diagnostics, context);
 			case PivotPackage.TEMPLATEABLE_ELEMENT:
 				return validateTemplateableElement((TemplateableElement)value, diagnostics, context);
 			case PivotPackage.TUPLE_LITERAL_EXP:
 				return validateTupleLiteralExp((TupleLiteralExp)value, diagnostics, context);
 			case PivotPackage.TUPLE_LITERAL_PART:
 				return validateTupleLiteralPart((TupleLiteralPart)value, diagnostics, context);
 			case PivotPackage.TUPLE_TYPE:
 				return validateTupleType((TupleType)value, diagnostics, context);
 			case PivotPackage.TYPE:
 				return validateType((Type)value, diagnostics, context);
 			case PivotPackage.TYPE_EXP:
 				return validateTypeExp((TypeExp)value, diagnostics, context);
 			case PivotPackage.TYPE_TEMPLATE_PARAMETER:
 				return validateTypeTemplateParameter((TypeTemplateParameter)value, diagnostics, context);
 			case PivotPackage.TYPED_ELEMENT:
 				return validateTypedElement((TypedElement)value, diagnostics, context);
 			case PivotPackage.TYPED_MULTIPLICITY_ELEMENT:
 				return validateTypedMultiplicityElement((TypedMultiplicityElement)value, diagnostics, context);
 			case PivotPackage.UNLIMITED_NATURAL_LITERAL_EXP:
 				return validateUnlimitedNaturalLiteralExp((UnlimitedNaturalLiteralExp)value, diagnostics, context);
 			case PivotPackage.UNSPECIFIED_TYPE:
 				return validateUnspecifiedType((UnspecifiedType)value, diagnostics, context);
 			case PivotPackage.UNSPECIFIED_VALUE_EXP:
 				return validateUnspecifiedValueExp((UnspecifiedValueExp)value, diagnostics, context);
 			case PivotPackage.VALUE_SPECIFICATION:
 				return validateValueSpecification((ValueSpecification)value, diagnostics, context);
 			case PivotPackage.VARIABLE:
 				return validateVariable((Variable)value, diagnostics, context);
 			case PivotPackage.VARIABLE_DECLARATION:
 				return validateVariableDeclaration((VariableDeclaration)value, diagnostics, context);
 			case PivotPackage.VARIABLE_EXP:
 				return validateVariableExp((VariableExp)value, diagnostics, context);
 			case PivotPackage.VISITABLE:
 				return validateVisitable((Visitable)value, diagnostics, context);
 			case PivotPackage.VISITOR:
 				return validateVisitor((Visitor<?, ?>)value, diagnostics, context);
 			case PivotPackage.VOID_TYPE:
 				return validateVoidType((VoidType)value, diagnostics, context);
 			case PivotPackage.ASSOCIATIVITY_KIND:
 				return validateAssociativityKind((AssociativityKind)value, diagnostics, context);
 			case PivotPackage.COLLECTION_KIND:
 				return validateCollectionKind((CollectionKind)value, diagnostics, context);
 			case PivotPackage.BOOLEAN:
 				return validateBoolean((Boolean)value, diagnostics, context);
 			case PivotPackage.INT:
 				return validateInt((Integer)value, diagnostics, context);
 			case PivotPackage.INTEGER:
 				return validateInteger((BigInteger)value, diagnostics, context);
 			case PivotPackage.LIBRARY_FEATURE:
 				return validateLibraryFeature((LibraryFeature)value, diagnostics, context);
 			case PivotPackage.OBJECT:
 				return validateObject(value, diagnostics, context);
 			case PivotPackage.REAL:
 				return validateReal((BigDecimal)value, diagnostics, context);
 			case PivotPackage.STRING:
 				return validateString((String)value, diagnostics, context);
 			case PivotPackage.THROWABLE:
 				return validateThrowable((Throwable)value, diagnostics, context);
 			case PivotPackage.UNLIMITED_NATURAL:
 				return validateUnlimitedNatural((BigInteger)value, diagnostics, context);
 			default:
 				return true;
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateAnnotation(Annotation annotation,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)annotation, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)annotation, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(annotation, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateAnyType(AnyType anyType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)anyType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)anyType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(anyType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateType(Type type, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)type, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)type, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(type, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNamedElement(NamedElement namedElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)namedElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)namedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(namedElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNamespace(Namespace namespace,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)namespace, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)namespace, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(namespace, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateElement(Element element,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)element, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)element, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(element, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateNotOwnSelf constraint of '<em>Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateElement_validateNotOwnSelf(Element element, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return element.validateNotOwnSelf(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateClass(org.eclipse.ocl.examples.pivot.Class class_,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)class_, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)class_, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(class_, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateClassifierType(ClassifierType classifierType, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)classifierType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)classifierType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(classifierType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateProperty(Property property,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)property, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)property, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(property, diagnostics, context);
 		if (result || diagnostics != null) result &= validateProperty_validateCompatibleInitialiser(property, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateCompatibleInitialiser constraint of '<em>Property</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateProperty_validateCompatibleInitialiser(Property property, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return property.validateCompatibleInitialiser(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTypedElement(TypedElement typedElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)typedElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)typedElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(typedElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTypedMultiplicityElement(
 			TypedMultiplicityElement typedMultiplicityElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)typedMultiplicityElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)typedMultiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(typedMultiplicityElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateUnlimitedNaturalLiteralExp(
 			UnlimitedNaturalLiteralExp unlimitedNaturalLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)unlimitedNaturalLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)unlimitedNaturalLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(unlimitedNaturalLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateUnspecifiedType(UnspecifiedType unspecifiedType, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)unspecifiedType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)unspecifiedType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(unspecifiedType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMultiplicityElement(
 			MultiplicityElement multiplicityElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)multiplicityElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)multiplicityElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(multiplicityElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNameable(Nameable nameable,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint((EObject)nameable, diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateParameterableElement(
 			ParameterableElement parameterableElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)parameterableElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)parameterableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(parameterableElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePivotable(Pivotable pivotable,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint((EObject)pivotable, diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePrecedence(Precedence precedence,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)precedence, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)precedence, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(precedence, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateParameter(
 			TemplateParameter templateParameter, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateParameter, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateParameter, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateSignature(
 			TemplateSignature templateSignature, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateSignature, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateSignature, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateSignature, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateableElement(
 			TemplateableElement templateableElement,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateableElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateableElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateBinding(TemplateBinding templateBinding,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateBinding, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateBinding, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateBinding, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateParameterSubstitution(
 			TemplateParameterSubstitution templateParameterSubstitution,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateParameterSubstitution, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateParameterSubstitution, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateParameterSubstitution, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateAssociationClass(AssociationClass associationClass,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)associationClass, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)associationClass, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(associationClass, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOperation(Operation operation,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)operation, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(operation, diagnostics, context);
 		if (result || diagnostics != null) result &= validateOperation_validateCompatibleReturn(operation, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateCompatibleReturn constraint of '<em>Operation</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOperation_validateCompatibleReturn(Operation operation, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return operation.validateCompatibleReturn(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateParameter(Parameter parameter,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)parameter, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)parameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(parameter, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOperationTemplateParameter(
 			OperationTemplateParameter operationTemplateParameter,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)operationTemplateParameter, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)operationTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(operationTemplateParameter, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateComment(Comment comment,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)comment, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)comment, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(comment, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateConstraint(Constraint constraint,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)constraint, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(constraint, diagnostics, context);
 		if (result || diagnostics != null) result &= validateConstraint_validateUniqueName(constraint, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateUniqueName constraint of '<em>Constraint</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateConstraint_validateUniqueName(Constraint constraint, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return constraint.validateUniqueName(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateConstructorExp(ConstructorExp constructorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)constructorExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)constructorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(constructorExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateConstructorPart(ConstructorPart constructorPart, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)constructorPart, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)constructorPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(constructorPart, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePackage(
 			org.eclipse.ocl.examples.pivot.Package package_,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)package_, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)package_, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(package_, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTypeTemplateParameter(
 			TypeTemplateParameter typeTemplateParameter,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)typeTemplateParameter, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)typeTemplateParameter, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(typeTemplateParameter, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateAssociationClassCallExp(
 			AssociationClassCallExp associationClassCallExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)associationClassCallExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)associationClassCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(associationClassCallExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNavigationCallExp(
 			NavigationCallExp navigationCallExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)navigationCallExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)navigationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(navigationCallExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateFeatureCallExp(FeatureCallExp featureCallExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)featureCallExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)featureCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(featureCallExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCallExp(CallExp callExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)callExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)callExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(callExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCallOperationAction(
 			CallOperationAction callOperationAction,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)callOperationAction, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)callOperationAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(callOperationAction, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOclExpression(OclExpression oclExpression,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)oclExpression, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)oclExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(oclExpression, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateBagType(BagType bagType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)bagType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)bagType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(bagType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionType(CollectionType collectionType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)collectionType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)collectionType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(collectionType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateDataType(DataType dataType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)dataType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)dataType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(dataType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateDetail(Detail detail, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)detail, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)detail, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(detail, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateBooleanLiteralExp(
 			BooleanLiteralExp booleanLiteralExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)booleanLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(booleanLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateBooleanLiteralExp_validateTypeIsBoolean(booleanLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTypeIsBoolean constraint of '<em>Boolean Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateBooleanLiteralExp_validateTypeIsBoolean(BooleanLiteralExp booleanLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return booleanLiteralExp.validateTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePrimitiveLiteralExp(
 			PrimitiveLiteralExp primitiveLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)primitiveLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)primitiveLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(primitiveLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLiteralExp(LiteralExp literalExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)literalExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)literalExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(literalExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionItem(CollectionItem collectionItem,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)collectionItem, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(collectionItem, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionItem_validateTypeIsItemType(collectionItem, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTypeIsItemType constraint of '<em>Collection Item</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionItem_validateTypeIsItemType(CollectionItem collectionItem, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionItem.validateTypeIsItemType(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralPart(
 			CollectionLiteralPart collectionLiteralPart,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)collectionLiteralPart, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)collectionLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(collectionLiteralPart, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp(
 			CollectionLiteralExp collectionLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)collectionLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionLiteralExp_validateBagKindIsBag(collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionLiteralExp_validateSequenceKindIsSequence(collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionLiteralExp_validateOrderedSetKindIsOrderedSet(collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionLiteralExp_validateSetKindIsSet(collectionLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateCollectionLiteralExp_validateCollectionKindIsConcrete(collectionLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateBagKindIsBag constraint of '<em>Collection Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp_validateBagKindIsBag(CollectionLiteralExp collectionLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionLiteralExp.validateBagKindIsBag(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSequenceKindIsSequence constraint of '<em>Collection Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp_validateSequenceKindIsSequence(CollectionLiteralExp collectionLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionLiteralExp.validateSequenceKindIsSequence(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateOrderedSetKindIsOrderedSet constraint of '<em>Collection Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp_validateOrderedSetKindIsOrderedSet(CollectionLiteralExp collectionLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionLiteralExp.validateOrderedSetKindIsOrderedSet(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSetKindIsSet constraint of '<em>Collection Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp_validateSetKindIsSet(CollectionLiteralExp collectionLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionLiteralExp.validateSetKindIsSet(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectionKindIsConcrete constraint of '<em>Collection Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionLiteralExp_validateCollectionKindIsConcrete(CollectionLiteralExp collectionLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return collectionLiteralExp.validateCollectionKindIsConcrete(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionRange(CollectionRange collectionRange,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)collectionRange, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)collectionRange, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(collectionRange, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateEnumLiteralExp(EnumLiteralExp enumLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)enumLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(enumLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateEnumLiteralExp_validateTypeIsEnumerationType(enumLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTypeIsEnumerationType constraint of '<em>Enum Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateEnumLiteralExp_validateTypeIsEnumerationType(EnumLiteralExp enumLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return enumLiteralExp.validateTypeIsEnumerationType(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateEnumerationLiteral(
 			EnumerationLiteral enumerationLiteral, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)enumerationLiteral, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)enumerationLiteral, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(enumerationLiteral, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateEnumeration(Enumeration enumeration,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)enumeration, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)enumeration, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(enumeration, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateExpressionInOcl(ExpressionInOcl expressionInOcl,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)expressionInOcl, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)expressionInOcl, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(expressionInOcl, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateFeature(Feature feature,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)feature, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)feature, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(feature, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOpaqueExpression(OpaqueExpression opaqueExpression,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)opaqueExpression, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)opaqueExpression, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(opaqueExpression, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVariable(Variable variable,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)variable, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)variable, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(variable, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVariableDeclaration(
 			VariableDeclaration variableDeclaration,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)variableDeclaration, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)variableDeclaration, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(variableDeclaration, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIfExp(IfExp ifExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)ifExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(ifExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIfExp_validateConditionTypeIsBoolean(ifExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateConditionTypeIsBoolean constraint of '<em>If Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIfExp_validateConditionTypeIsBoolean(IfExp ifExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return ifExp.validateConditionTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIntegerLiteralExp(
 			IntegerLiteralExp integerLiteralExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)integerLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(integerLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIntegerLiteralExp_validateTypeIsInteger(integerLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTypeIsInteger constraint of '<em>Integer Literal Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIntegerLiteralExp_validateTypeIsInteger(IntegerLiteralExp integerLiteralExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return integerLiteralExp.validateTypeIsInteger(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNumericLiteralExp(
 			NumericLiteralExp numericLiteralExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)numericLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)numericLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(numericLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateInvalidLiteralExp(
 			InvalidLiteralExp invalidLiteralExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)invalidLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)invalidLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(invalidLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateInvalidType(InvalidType invalidType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)invalidType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)invalidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(invalidType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIterateExp(IterateExp iterateExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)iterateExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateNoInitializers(iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateSourceIsCollection(iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIterateExp_validateOneInitializer(iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIterateExp_validateBodyTypeConformsToResultType(iterateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIterateExp_validateTypeIsResultType(iterateExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateOneInitializer constraint of '<em>Iterate Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIterateExp_validateOneInitializer(IterateExp iterateExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iterateExp.validateOneInitializer(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateBodyTypeConformsToResultType constraint of '<em>Iterate Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIterateExp_validateBodyTypeConformsToResultType(IterateExp iterateExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iterateExp.validateBodyTypeConformsToResultType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateTypeIsResultType constraint of '<em>Iterate Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIterateExp_validateTypeIsResultType(IterateExp iterateExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iterateExp.validateTypeIsResultType(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteration(Iteration iteration, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)iteration, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(iteration, diagnostics, context);
 		if (result || diagnostics != null) result &= validateOperation_validateCompatibleReturn(iteration, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)iteratorExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateNoInitializers(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateSourceIsCollection(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateIteratorTypeIsSourceElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateSortedByElementTypeIsSourceElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateSortedByIsOrderedIfSourceIsOrdered(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateSortedByHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateRejectOrSelectTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateRejectOrSelectTypeIsSourceType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateRejectOrSelectHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateOneBodyTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateOneTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateOneHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateIsUniqueTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateIsUniqueHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateForAllBodyTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateForAllTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateExistsBodyTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateExistsTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectNestedTypeIsBodyType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectNestedTypeIsBag(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectNestedHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectElementTypeIsSourceElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectTypeIsUnordered(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateCollectHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateClosureElementTypeIsSourceElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateClosureSourceElementTypeIsBodyElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateClosureTypeIsUniqueCollection(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateClosureHasOneIterator(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateAnyBodyTypeIsBoolean(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateAnyTypeIsSourceElementType(iteratorExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateIteratorExp_validateAnyHasOneIterator(iteratorExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateIteratorTypeIsSourceElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateIteratorTypeIsSourceElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateIteratorTypeIsSourceElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSortedByElementTypeIsSourceElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateSortedByElementTypeIsSourceElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateSortedByElementTypeIsSourceElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSortedByIsOrderedIfSourceIsOrdered constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateSortedByIsOrderedIfSourceIsOrdered(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateSortedByIsOrderedIfSourceIsOrdered(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSortedByHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateSortedByHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateSortedByHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateRejectOrSelectTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateRejectOrSelectTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateRejectOrSelectTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateRejectOrSelectTypeIsSourceType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateRejectOrSelectTypeIsSourceType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateRejectOrSelectTypeIsSourceType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateRejectOrSelectHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateRejectOrSelectHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateRejectOrSelectHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateOneBodyTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateOneBodyTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateOneBodyTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateOneTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateOneTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateOneTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateOneHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateOneHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateOneHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateIsUniqueTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateIsUniqueTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateIsUniqueTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateIsUniqueHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateIsUniqueHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateIsUniqueHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateForAllBodyTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateForAllBodyTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateForAllBodyTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateForAllTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateForAllTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateForAllTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateExistsBodyTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateExistsBodyTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateExistsBodyTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateExistsTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateExistsTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateExistsTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectNestedTypeIsBodyType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectNestedTypeIsBodyType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectNestedTypeIsBodyType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectNestedTypeIsBag constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectNestedTypeIsBag(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectNestedTypeIsBag(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectNestedHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectNestedHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectNestedHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectElementTypeIsSourceElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectElementTypeIsSourceElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectElementTypeIsSourceElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectTypeIsUnordered constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectTypeIsUnordered(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectTypeIsUnordered(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateCollectHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateCollectHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateCollectHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateClosureElementTypeIsSourceElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateClosureElementTypeIsSourceElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateClosureElementTypeIsSourceElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateClosureSourceElementTypeIsBodyElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateClosureSourceElementTypeIsBodyElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateClosureSourceElementTypeIsBodyElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateClosureTypeIsUniqueCollection constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateClosureTypeIsUniqueCollection(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateClosureTypeIsUniqueCollection(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateClosureHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateClosureHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateClosureHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateAnyBodyTypeIsBoolean constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateAnyBodyTypeIsBoolean(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateAnyBodyTypeIsBoolean(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateAnyTypeIsSourceElementType constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateAnyTypeIsSourceElementType(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateAnyTypeIsSourceElementType(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateAnyHasOneIterator constraint of '<em>Iterator Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateIteratorExp_validateAnyHasOneIterator(IteratorExp iteratorExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return iteratorExp.validateAnyHasOneIterator(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLambdaType(LambdaType lambdaType, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)lambdaType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)lambdaType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(lambdaType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLoopExp(LoopExp loopExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)loopExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateNoInitializers(loopExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLoopExp_validateSourceIsCollection(loopExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateNoInitializers constraint of '<em>Loop Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLoopExp_validateNoInitializers(LoopExp loopExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return loopExp.validateNoInitializers(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateSourceIsCollection constraint of '<em>Loop Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLoopExp_validateSourceIsCollection(LoopExp loopExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return loopExp.validateSourceIsCollection(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLetExp(LetExp letExp, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)letExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(letExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateLetExp_validateTypeIsInType(letExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTypeIsInType constraint of '<em>Let Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLetExp_validateTypeIsInType(LetExp letExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return letExp.validateTypeIsInType(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLibrary(Library library, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)library, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)library, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(library, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMessageExp(MessageExp messageExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)messageExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateMessageExp_validateTargetIsNotACollection(messageExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateMessageExp_validateOneCallOrOneSend(messageExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateTargetIsNotACollection constraint of '<em>Message Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMessageExp_validateTargetIsNotACollection(MessageExp messageExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return messageExp.validateTargetIsNotACollection(diagnostics, context);
 	}
 
 	/**
 	 * Validates the validateOneCallOrOneSend constraint of '<em>Message Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMessageExp_validateOneCallOrOneSend(MessageExp messageExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return messageExp.validateOneCallOrOneSend(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMessageType(MessageType messageType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)messageType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)messageType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(messageType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateMorePivotable(MorePivotable morePivotable, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return validate_EveryDefaultConstraint((EObject)morePivotable, diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateSignal(Signal signal, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)signal, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)signal, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(signal, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateNullLiteralExp(NullLiteralExp nullLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)nullLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)nullLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(nullLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOperationCallExp(OperationCallExp operationCallExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)operationCallExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(operationCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateOperationCallExp_validateArgumentCount(operationCallExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the validateArgumentCount constraint of '<em>Operation Call Exp</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOperationCallExp_validateArgumentCount(OperationCallExp operationCallExp, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return operationCallExp.validateArgumentCount(diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateOrderedSetType(OrderedSetType orderedSetType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)orderedSetType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)orderedSetType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(orderedSetType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePackageableElement(
 			PackageableElement packageableElement, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)packageableElement, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)packageableElement, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(packageableElement, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePrimitiveType(PrimitiveType primitiveType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)primitiveType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)primitiveType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(primitiveType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validatePropertyCallExp(PropertyCallExp propertyCallExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)propertyCallExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)propertyCallExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(propertyCallExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateRealLiteralExp(RealLiteralExp realLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)realLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)realLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(realLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateSelfType(SelfType selfType, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		if (!validate_NoCircularContainment((EObject)selfType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)selfType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(selfType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateSendSignalAction(SendSignalAction sendSignalAction,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)sendSignalAction, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)sendSignalAction, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(sendSignalAction, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateSequenceType(SequenceType sequenceType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)sequenceType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)sequenceType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(sequenceType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateSetType(SetType setType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)setType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)setType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(setType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateState(State state, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)state, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)state, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(state, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateStateExp(StateExp stateExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)stateExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)stateExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(stateExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateStringLiteralExp(StringLiteralExp stringLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)stringLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)stringLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(stringLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTemplateParameterType(
 			TemplateParameterType templateParameterType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)templateParameterType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)templateParameterType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(templateParameterType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTupleLiteralExp(TupleLiteralExp tupleLiteralExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)tupleLiteralExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)tupleLiteralExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(tupleLiteralExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTupleLiteralPart(TupleLiteralPart tupleLiteralPart,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)tupleLiteralPart, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)tupleLiteralPart, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(tupleLiteralPart, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTupleType(TupleType tupleType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)tupleType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)tupleType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(tupleType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateTypeExp(TypeExp typeExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)typeExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)typeExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(typeExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateUnspecifiedValueExp(
 			UnspecifiedValueExp unspecifiedValueExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)unspecifiedValueExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)unspecifiedValueExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(unspecifiedValueExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateValueSpecification(
 			ValueSpecification valueSpecification, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)valueSpecification, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)valueSpecification, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(valueSpecification, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVariableExp(VariableExp variableExp,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)variableExp, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)variableExp, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(variableExp, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVisitable(Visitable visitable,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint((EObject)visitable, diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVisitor(Visitor<?, ?> visitor,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint((EObject)visitor, diagnostics, context);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateVoidType(VoidType voidType,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		if (!validate_NoCircularContainment((EObject)voidType, diagnostics, context)) return false;
 		boolean result = validate_EveryMultiplicityConforms((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryDataValueConforms((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryReferenceIsContained((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryProxyResolves((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_UniqueID((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryKeyUnique((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validate_EveryMapEntryUnique((EObject)voidType, diagnostics, context);
 		if (result || diagnostics != null) result &= validateElement_validateNotOwnSelf(voidType, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateAssociativityKind(
 			AssociativityKind associativityKind, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateCollectionKind(CollectionKind collectionKind,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateBoolean(boolean boolean_,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateInt(int int_, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateInteger(BigInteger integer,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateLibraryFeature(LibraryFeature libraryFeature, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateObject(Object object, DiagnosticChain diagnostics, Map<Object, Object> context)
 	{
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateReal(BigDecimal real, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateString(String string, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateThrowable(Throwable throwable,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean validateUnlimitedNatural(BigInteger unlimitedNatural,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
 	/**
 	 * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return PivotPlugin.INSTANCE;
 	}
 
 } //PivotValidator
