 /*
  * Copyright (c) 2006, 2008 Borland Software Corp.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Alexander Shatalin (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.ecore.providers;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.gmf.ecore.edit.parts.EAnnotation2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EAnnotationDetailsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EAnnotationEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EAnnotationReferencesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EAnnotationSourceEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EAttributeEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClass2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassAttributesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassClassAnnotationsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassESuperTypesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassNameEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EClassOperationsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EDataType2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EDataTypeDataTypeAnnotationsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EDataTypeEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EDataTypeNameEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnum2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnumEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnumEnumAnnotationsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnumLiteralEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnumLiteralsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EEnumNameEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EOperationEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackage2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackage3EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackageClassesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackageDataTypesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackageEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackageEnumsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackageNameEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackagePackageAnnotationsEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EPackagePackagesEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReference2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReferenceEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReferenceLowerBoundUpperBound2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReferenceLowerBoundUpperBoundEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReferenceName2EditPart;
 import org.eclipse.gmf.ecore.edit.parts.EReferenceNameEditPart;
 import org.eclipse.gmf.ecore.edit.parts.EStringToStringMapEntryEditPart;
 import org.eclipse.gmf.ecore.part.EcoreVisualIDRegistry;
 import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
 import org.eclipse.gmf.runtime.common.core.service.IOperation;
 import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
 import org.eclipse.gmf.runtime.diagram.core.providers.IViewProvider;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateDiagramViewOperation;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateEdgeViewOperation;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateNodeViewOperation;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewForKindOperation;
 import org.eclipse.gmf.runtime.diagram.core.services.view.CreateViewOperation;
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.FigureUtilities;
 import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
 import org.eclipse.gmf.runtime.emf.type.core.IElementType;
 import org.eclipse.gmf.runtime.emf.type.core.IHintedType;
 import org.eclipse.gmf.runtime.notation.BasicCompartment;
 import org.eclipse.gmf.runtime.notation.BasicDecorationNode;
 import org.eclipse.gmf.runtime.notation.Connector;
 import org.eclipse.gmf.runtime.notation.DecorationNode;
 import org.eclipse.gmf.runtime.notation.Diagram;
 import org.eclipse.gmf.runtime.notation.Edge;
 import org.eclipse.gmf.runtime.notation.FontStyle;
 import org.eclipse.gmf.runtime.notation.Location;
 import org.eclipse.gmf.runtime.notation.MeasurementUnit;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.RelativeBendpoints;
import org.eclipse.gmf.runtime.notation.Routing;
 import org.eclipse.gmf.runtime.notation.Shape;
 import org.eclipse.gmf.runtime.notation.TitleStyle;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.gmf.runtime.notation.datatype.RelativeBendpoint;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontData;
 
 /**
  * @generated
  */
 public class EcoreViewProvider extends AbstractProvider implements IViewProvider {
 
 	/**
 	 * @generated
 	 */
 	public final boolean provides(IOperation operation) {
 		if (operation instanceof CreateViewForKindOperation) {
 			return provides((CreateViewForKindOperation) operation);
 		}
 		assert operation instanceof CreateViewOperation;
 		if (operation instanceof CreateDiagramViewOperation) {
 			return provides((CreateDiagramViewOperation) operation);
 		} else if (operation instanceof CreateEdgeViewOperation) {
 			return provides((CreateEdgeViewOperation) operation);
 		} else if (operation instanceof CreateNodeViewOperation) {
 			return provides((CreateNodeViewOperation) operation);
 		}
 		return false;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean provides(CreateViewForKindOperation op) {
 		/*
 		 if (op.getViewKind() == Node.class)
 		 return getNodeViewClass(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint()) != null;
 		 if (op.getViewKind() == Edge.class)
 		 return getEdgeViewClass(op.getSemanticAdapter(), op.getContainerView(), op.getSemanticHint()) != null;
 		 */
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean provides(CreateDiagramViewOperation op) {
 		return EPackageEditPart.MODEL_ID.equals(op.getSemanticHint()) && EcoreVisualIDRegistry.getDiagramVisualID(getSemanticElement(op.getSemanticAdapter())) != -1;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean provides(CreateNodeViewOperation op) {
 		if (op.getContainerView() == null) {
 			return false;
 		}
 		IElementType elementType = getSemanticElementType(op.getSemanticAdapter());
 		EObject domainElement = getSemanticElement(op.getSemanticAdapter());
 		int visualID;
 		if (op.getSemanticHint() == null) {
 			// Semantic hint is not specified. Can be a result of call from CanonicalEditPolicy.
 			// In this situation there should be NO elementType, visualID will be determined
 			// by VisualIDRegistry.getNodeVisualID() for domainElement.
 			if (elementType != null || domainElement == null) {
 				return false;
 			}
 			visualID = EcoreVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement);
 		} else {
 			visualID = EcoreVisualIDRegistry.getVisualID(op.getSemanticHint());
 			if (elementType != null) {
 				if (!EcoreElementTypes.isKnownElementType(elementType) || (!(elementType instanceof IHintedType))) {
 					return false; // foreign element type
 				}
 				String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
 				if (!op.getSemanticHint().equals(elementTypeHint)) {
 					return false; // if semantic hint is specified it should be the same as in element type
 				}
 				if (domainElement != null && visualID != EcoreVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement)) {
 					return false; // visual id for node EClass should match visual id from element type
 				}
 			} else {
 				if (!EPackageEditPart.MODEL_ID.equals(EcoreVisualIDRegistry.getModelID(op.getContainerView()))) {
 					return false; // foreign diagram
 				}
 				switch (visualID) {
 				case EClassEditPart.VISUAL_ID:
 				case EAttributeEditPart.VISUAL_ID:
 				case EOperationEditPart.VISUAL_ID:
 				case EAnnotation2EditPart.VISUAL_ID:
 				case EDataType2EditPart.VISUAL_ID:
 				case EEnum2EditPart.VISUAL_ID:
 				case EStringToStringMapEntryEditPart.VISUAL_ID:
 				case EEnumLiteralEditPart.VISUAL_ID:
 				case EPackage2EditPart.VISUAL_ID:
 				case EAnnotationEditPart.VISUAL_ID:
 				case EDataTypeEditPart.VISUAL_ID:
 				case EEnumEditPart.VISUAL_ID:
 				case EClass2EditPart.VISUAL_ID:
 				case EPackage3EditPart.VISUAL_ID:
 					if (domainElement == null || visualID != EcoreVisualIDRegistry.getNodeVisualID(op.getContainerView(), domainElement)) {
 						return false; // visual id in semantic hint should match visual id for domain element
 					}
 					break;
 				default:
 					return false;
 				}
 			}
 		}
 		return EClassEditPart.VISUAL_ID == visualID || EPackage2EditPart.VISUAL_ID == visualID || EAnnotationEditPart.VISUAL_ID == visualID || EDataTypeEditPart.VISUAL_ID == visualID
 				|| EEnumEditPart.VISUAL_ID == visualID || EAttributeEditPart.VISUAL_ID == visualID || EOperationEditPart.VISUAL_ID == visualID || EAnnotation2EditPart.VISUAL_ID == visualID
 				|| EClass2EditPart.VISUAL_ID == visualID || EPackage3EditPart.VISUAL_ID == visualID || EDataType2EditPart.VISUAL_ID == visualID || EEnum2EditPart.VISUAL_ID == visualID
 				|| EStringToStringMapEntryEditPart.VISUAL_ID == visualID || EEnumLiteralEditPart.VISUAL_ID == visualID;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected boolean provides(CreateEdgeViewOperation op) {
 		IElementType elementType = getSemanticElementType(op.getSemanticAdapter());
 		if (!EcoreElementTypes.isKnownElementType(elementType) || (!(elementType instanceof IHintedType))) {
 			return false; // foreign element type
 		}
 		String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
 		if (elementTypeHint == null || (op.getSemanticHint() != null && !elementTypeHint.equals(op.getSemanticHint()))) {
 			return false; // our hint is visual id and must be specified, and it should be the same as in element type
 		}
 		int visualID = EcoreVisualIDRegistry.getVisualID(elementTypeHint);
 		EObject domainElement = getSemanticElement(op.getSemanticAdapter());
 		if (domainElement != null && visualID != EcoreVisualIDRegistry.getLinkWithClassVisualID(domainElement)) {
 			return false; // visual id for link EClass should match visual id from element type
 		}
 		return true;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Diagram createDiagram(IAdaptable semanticAdapter, String diagramKind, PreferencesHint preferencesHint) {
 		Diagram diagram = NotationFactory.eINSTANCE.createDiagram();
 		diagram.getStyles().add(NotationFactory.eINSTANCE.createDiagramStyle());
 		diagram.setType(EPackageEditPart.MODEL_ID);
 		diagram.setElement(getSemanticElement(semanticAdapter));
 		diagram.setMeasurementUnit(MeasurementUnit.PIXEL_LITERAL);
 		return diagram;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createNode(IAdaptable semanticAdapter, View containerView, String semanticHint, int index, boolean persisted, PreferencesHint preferencesHint) {
 		final EObject domainElement = getSemanticElement(semanticAdapter);
 		final int visualID;
 		if (semanticHint == null) {
 			visualID = EcoreVisualIDRegistry.getNodeVisualID(containerView, domainElement);
 		} else {
 			visualID = EcoreVisualIDRegistry.getVisualID(semanticHint);
 		}
 		switch (visualID) {
 		case EClassEditPart.VISUAL_ID:
 			return createEClass_2001(domainElement, containerView, index, persisted, preferencesHint);
 		case EPackage2EditPart.VISUAL_ID:
 			return createEPackage_2002(domainElement, containerView, index, persisted, preferencesHint);
 		case EAnnotationEditPart.VISUAL_ID:
 			return createEAnnotation_2003(domainElement, containerView, index, persisted, preferencesHint);
 		case EDataTypeEditPart.VISUAL_ID:
 			return createEDataType_2004(domainElement, containerView, index, persisted, preferencesHint);
 		case EEnumEditPart.VISUAL_ID:
 			return createEEnum_2005(domainElement, containerView, index, persisted, preferencesHint);
 		case EAttributeEditPart.VISUAL_ID:
 			return createEAttribute_3001(domainElement, containerView, index, persisted, preferencesHint);
 		case EOperationEditPart.VISUAL_ID:
 			return createEOperation_3002(domainElement, containerView, index, persisted, preferencesHint);
 		case EAnnotation2EditPart.VISUAL_ID:
 			return createEAnnotation_3003(domainElement, containerView, index, persisted, preferencesHint);
 		case EClass2EditPart.VISUAL_ID:
 			return createEClass_3004(domainElement, containerView, index, persisted, preferencesHint);
 		case EPackage3EditPart.VISUAL_ID:
 			return createEPackage_3005(domainElement, containerView, index, persisted, preferencesHint);
 		case EDataType2EditPart.VISUAL_ID:
 			return createEDataType_3006(domainElement, containerView, index, persisted, preferencesHint);
 		case EEnum2EditPart.VISUAL_ID:
 			return createEEnum_3007(domainElement, containerView, index, persisted, preferencesHint);
 		case EStringToStringMapEntryEditPart.VISUAL_ID:
 			return createEStringToStringMapEntry_3008(domainElement, containerView, index, persisted, preferencesHint);
 		case EEnumLiteralEditPart.VISUAL_ID:
 			return createEEnumLiteral_3009(domainElement, containerView, index, persisted, preferencesHint);
 		}
 		// can't happen, provided #provides(CreateNodeViewOperation) is correct
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Edge createEdge(IAdaptable semanticAdapter, View containerView, String semanticHint, int index, boolean persisted, PreferencesHint preferencesHint) {
 		IElementType elementType = getSemanticElementType(semanticAdapter);
 		String elementTypeHint = ((IHintedType) elementType).getSemanticHint();
 		switch (EcoreVisualIDRegistry.getVisualID(elementTypeHint)) {
 		case EAnnotationReferencesEditPart.VISUAL_ID:
 			return createEAnnotationReferences_4001(containerView, index, persisted, preferencesHint);
 		case EReferenceEditPart.VISUAL_ID:
 			return createEReference_4002(getSemanticElement(semanticAdapter), containerView, index, persisted, preferencesHint);
 		case EReference2EditPart.VISUAL_ID:
 			return createEReference_4003(getSemanticElement(semanticAdapter), containerView, index, persisted, preferencesHint);
 		case EClassESuperTypesEditPart.VISUAL_ID:
 			return createEClassESuperTypes_4004(containerView, index, persisted, preferencesHint);
 		}
 		// can never happen, provided #provides(CreateEdgeViewOperation) is correct
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEClass_2001(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Shape node = NotationFactory.eINSTANCE.createShape();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
 		node.setType(EcoreVisualIDRegistry.getType(EClassEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		stampShortcut(containerView, node);
 		// initializeFromPreferences 
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle nodeFontStyle = (FontStyle) node.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (nodeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			nodeFontStyle.setFontName(fontData.getName());
 			nodeFontStyle.setFontHeight(fontData.getHeight());
 			nodeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			nodeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			nodeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
 		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FILL_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getFillStyle_FillColor(), FigureUtilities.RGBToInteger(fillRGB));
 		Node label5001 = createLabel(node, EcoreVisualIDRegistry.getType(EClassNameEditPart.VISUAL_ID));
 		createCompartment(node, EcoreVisualIDRegistry.getType(EClassAttributesEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EClassOperationsEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EClassClassAnnotationsEditPart.VISUAL_ID), true, false, true, true);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEPackage_2002(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Shape node = NotationFactory.eINSTANCE.createShape();
 		node.getStyles().add(NotationFactory.eINSTANCE.createHintedDiagramLinkStyle());
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
 		node.setType(EcoreVisualIDRegistry.getType(EPackage2EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		stampShortcut(containerView, node);
 		// initializeFromPreferences 
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle nodeFontStyle = (FontStyle) node.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (nodeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			nodeFontStyle.setFontName(fontData.getName());
 			nodeFontStyle.setFontHeight(fontData.getHeight());
 			nodeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			nodeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			nodeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
 		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FILL_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getFillStyle_FillColor(), FigureUtilities.RGBToInteger(fillRGB));
 		Node label5002 = createLabel(node, EcoreVisualIDRegistry.getType(EPackageNameEditPart.VISUAL_ID));
 		createCompartment(node, EcoreVisualIDRegistry.getType(EPackageClassesEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EPackagePackagesEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EPackageDataTypesEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EPackageEnumsEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EPackagePackageAnnotationsEditPart.VISUAL_ID), true, false, true, true);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEAnnotation_2003(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Shape node = NotationFactory.eINSTANCE.createShape();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
 		node.setType(EcoreVisualIDRegistry.getType(EAnnotationEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		stampShortcut(containerView, node);
 		// initializeFromPreferences 
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle nodeFontStyle = (FontStyle) node.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (nodeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			nodeFontStyle.setFontName(fontData.getName());
 			nodeFontStyle.setFontHeight(fontData.getHeight());
 			nodeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			nodeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			nodeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
 		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FILL_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getFillStyle_FillColor(), FigureUtilities.RGBToInteger(fillRGB));
 		Node label5003 = createLabel(node, EcoreVisualIDRegistry.getType(EAnnotationSourceEditPart.VISUAL_ID));
 		createCompartment(node, EcoreVisualIDRegistry.getType(EAnnotationDetailsEditPart.VISUAL_ID), true, false, true, true);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEDataType_2004(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Shape node = NotationFactory.eINSTANCE.createShape();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
 		node.setType(EcoreVisualIDRegistry.getType(EDataTypeEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		stampShortcut(containerView, node);
 		// initializeFromPreferences 
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle nodeFontStyle = (FontStyle) node.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (nodeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			nodeFontStyle.setFontName(fontData.getName());
 			nodeFontStyle.setFontHeight(fontData.getHeight());
 			nodeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			nodeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			nodeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
 		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FILL_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getFillStyle_FillColor(), FigureUtilities.RGBToInteger(fillRGB));
 		Node label5004 = createLabel(node, EcoreVisualIDRegistry.getType(EDataTypeNameEditPart.VISUAL_ID));
 		createCompartment(node, EcoreVisualIDRegistry.getType(EDataTypeDataTypeAnnotationsEditPart.VISUAL_ID), true, false, true, true);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEEnum_2005(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Shape node = NotationFactory.eINSTANCE.createShape();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createBounds());
 		node.setType(EcoreVisualIDRegistry.getType(EEnumEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		stampShortcut(containerView, node);
 		// initializeFromPreferences 
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle nodeFontStyle = (FontStyle) node.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (nodeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			nodeFontStyle.setFontName(fontData.getName());
 			nodeFontStyle.setFontHeight(fontData.getHeight());
 			nodeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			nodeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			nodeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
 		org.eclipse.swt.graphics.RGB fillRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FILL_COLOR);
 		ViewUtil.setStructuralFeatureValue(node, NotationPackage.eINSTANCE.getFillStyle_FillColor(), FigureUtilities.RGBToInteger(fillRGB));
 		Node label5005 = createLabel(node, EcoreVisualIDRegistry.getType(EEnumNameEditPart.VISUAL_ID));
 		createCompartment(node, EcoreVisualIDRegistry.getType(EEnumLiteralsEditPart.VISUAL_ID), true, false, true, true);
 		createCompartment(node, EcoreVisualIDRegistry.getType(EEnumEnumAnnotationsEditPart.VISUAL_ID), true, false, true, true);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEAttribute_3001(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EAttributeEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEOperation_3002(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EOperationEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEAnnotation_3003(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EAnnotation2EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEClass_3004(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EClass2EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEPackage_3005(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EPackage3EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEDataType_3006(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EDataType2EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEEnum_3007(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EEnum2EditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEStringToStringMapEntry_3008(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EStringToStringMapEntryEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Node createEEnumLiteral_3009(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Node node = NotationFactory.eINSTANCE.createNode();
 		node.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		node.setType(EcoreVisualIDRegistry.getType(EEnumLiteralEditPart.VISUAL_ID));
 		ViewUtil.insertChildView(containerView, node, index, persisted);
 		node.setElement(domainElement);
 		return node;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Edge createEAnnotationReferences_4001(View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Connector edge = NotationFactory.eINSTANCE.createConnector();
 		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
 		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
 		ArrayList points = new ArrayList(2);
 		points.add(new RelativeBendpoint());
 		points.add(new RelativeBendpoint());
 		bendpoints.setPoints(points);
 		edge.setBendpoints(bendpoints);
 		ViewUtil.insertChildView(containerView, edge, index, persisted);
 		edge.setType(EcoreVisualIDRegistry.getType(EAnnotationReferencesEditPart.VISUAL_ID));
 		edge.setElement(null);
 		// initializePreferences
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle edgeFontStyle = (FontStyle) edge.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (edgeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			edgeFontStyle.setFontName(fontData.getName());
 			edgeFontStyle.setFontHeight(fontData.getHeight());
 			edgeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			edgeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			edgeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
		Routing routing = Routing.get(prefStore.getInt(IPreferenceConstants.PREF_LINE_STYLE));
		if (routing != null) {
			ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		}
 		return edge;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Edge createEReference_4002(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Connector edge = NotationFactory.eINSTANCE.createConnector();
 		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
 		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
 		ArrayList points = new ArrayList(2);
 		points.add(new RelativeBendpoint());
 		points.add(new RelativeBendpoint());
 		bendpoints.setPoints(points);
 		edge.setBendpoints(bendpoints);
 		ViewUtil.insertChildView(containerView, edge, index, persisted);
 		edge.setType(EcoreVisualIDRegistry.getType(EReferenceEditPart.VISUAL_ID));
 		edge.setElement(domainElement);
 		// initializePreferences
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle edgeFontStyle = (FontStyle) edge.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (edgeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			edgeFontStyle.setFontName(fontData.getName());
 			edgeFontStyle.setFontHeight(fontData.getHeight());
 			edgeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			edgeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			edgeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
		Routing routing = Routing.get(prefStore.getInt(IPreferenceConstants.PREF_LINE_STYLE));
		if (routing != null) {
			ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		}
 		Node label6001 = createLabel(edge, EcoreVisualIDRegistry.getType(EReferenceNameEditPart.VISUAL_ID));
 		label6001.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		Location location6001 = (Location) label6001.getLayoutConstraint();
 		location6001.setX(0);
 		location6001.setY(40);
 		Node label6003 = createLabel(edge, EcoreVisualIDRegistry.getType(EReferenceLowerBoundUpperBoundEditPart.VISUAL_ID));
 		label6003.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		Location location6003 = (Location) label6003.getLayoutConstraint();
 		location6003.setX(0);
 		location6003.setY(60);
 		return edge;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Edge createEReference_4003(EObject domainElement, View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Connector edge = NotationFactory.eINSTANCE.createConnector();
 		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
 		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
 		ArrayList points = new ArrayList(2);
 		points.add(new RelativeBendpoint());
 		points.add(new RelativeBendpoint());
 		bendpoints.setPoints(points);
 		edge.setBendpoints(bendpoints);
 		ViewUtil.insertChildView(containerView, edge, index, persisted);
 		edge.setType(EcoreVisualIDRegistry.getType(EReference2EditPart.VISUAL_ID));
 		edge.setElement(domainElement);
 		// initializePreferences
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle edgeFontStyle = (FontStyle) edge.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (edgeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			edgeFontStyle.setFontName(fontData.getName());
 			edgeFontStyle.setFontHeight(fontData.getHeight());
 			edgeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			edgeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			edgeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
		Routing routing = Routing.get(prefStore.getInt(IPreferenceConstants.PREF_LINE_STYLE));
		if (routing != null) {
			ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		}
 		Node label6002 = createLabel(edge, EcoreVisualIDRegistry.getType(EReferenceName2EditPart.VISUAL_ID));
 		label6002.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		Location location6002 = (Location) label6002.getLayoutConstraint();
 		location6002.setX(0);
 		location6002.setY(40);
 		Node label6004 = createLabel(edge, EcoreVisualIDRegistry.getType(EReferenceLowerBoundUpperBound2EditPart.VISUAL_ID));
 		label6004.setLayoutConstraint(NotationFactory.eINSTANCE.createLocation());
 		Location location6004 = (Location) label6004.getLayoutConstraint();
 		location6004.setX(0);
 		location6004.setY(60);
 		return edge;
 	}
 
 	/**
 	 * @generated
 	 */
 	public Edge createEClassESuperTypes_4004(View containerView, int index, boolean persisted, PreferencesHint preferencesHint) {
 		Connector edge = NotationFactory.eINSTANCE.createConnector();
 		edge.getStyles().add(NotationFactory.eINSTANCE.createFontStyle());
 		RelativeBendpoints bendpoints = NotationFactory.eINSTANCE.createRelativeBendpoints();
 		ArrayList points = new ArrayList(2);
 		points.add(new RelativeBendpoint());
 		points.add(new RelativeBendpoint());
 		bendpoints.setPoints(points);
 		edge.setBendpoints(bendpoints);
 		ViewUtil.insertChildView(containerView, edge, index, persisted);
 		edge.setType(EcoreVisualIDRegistry.getType(EClassESuperTypesEditPart.VISUAL_ID));
 		edge.setElement(null);
 		// initializePreferences
 		final IPreferenceStore prefStore = (IPreferenceStore) preferencesHint.getPreferenceStore();
 
 		org.eclipse.swt.graphics.RGB lineRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_LINE_COLOR);
 		ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getLineStyle_LineColor(), FigureUtilities.RGBToInteger(lineRGB));
 		FontStyle edgeFontStyle = (FontStyle) edge.getStyle(NotationPackage.Literals.FONT_STYLE);
 		if (edgeFontStyle != null) {
 			FontData fontData = PreferenceConverter.getFontData(prefStore, IPreferenceConstants.PREF_DEFAULT_FONT);
 			edgeFontStyle.setFontName(fontData.getName());
 			edgeFontStyle.setFontHeight(fontData.getHeight());
 			edgeFontStyle.setBold((fontData.getStyle() & SWT.BOLD) != 0);
 			edgeFontStyle.setItalic((fontData.getStyle() & SWT.ITALIC) != 0);
 			org.eclipse.swt.graphics.RGB fontRGB = PreferenceConverter.getColor(prefStore, IPreferenceConstants.PREF_FONT_COLOR);
 			edgeFontStyle.setFontColor(FigureUtilities.RGBToInteger(fontRGB).intValue());
 		}
		Routing routing = Routing.get(prefStore.getInt(IPreferenceConstants.PREF_LINE_STYLE));
		if (routing != null) {
			ViewUtil.setStructuralFeatureValue(edge, NotationPackage.eINSTANCE.getRoutingStyle_Routing(), routing);
		}
 		return edge;
 	}
 
 	/**
 	 * @generated
 	 */
 	private void stampShortcut(View containerView, Node target) {
 		if (!EPackageEditPart.MODEL_ID.equals(EcoreVisualIDRegistry.getModelID(containerView))) {
 			EAnnotation shortcutAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
 			shortcutAnnotation.setSource("Shortcut"); //$NON-NLS-1$
 			shortcutAnnotation.getDetails().put("modelID", EPackageEditPart.MODEL_ID); //$NON-NLS-1$
 			target.getEAnnotations().add(shortcutAnnotation);
 		}
 	}
 
 	/**
 	 * @generated
 	 */
 	private Node createLabel(View owner, String hint) {
 		DecorationNode rv = NotationFactory.eINSTANCE.createDecorationNode();
 		rv.setType(hint);
 		ViewUtil.insertChildView(owner, rv, ViewUtil.APPEND, true);
 		return rv;
 	}
 
 	/**
 	 * @generated
 	 */
 	private Node createCompartment(View owner, String hint, boolean canCollapse, boolean hasTitle, boolean canSort, boolean canFilter) {
 		//SemanticListCompartment rv = NotationFactory.eINSTANCE.createSemanticListCompartment();
 		//rv.setShowTitle(showTitle);
 		//rv.setCollapsed(isCollapsed);
 		Node rv;
 		if (canCollapse) {
 			rv = NotationFactory.eINSTANCE.createBasicCompartment();
 		} else {
 			rv = NotationFactory.eINSTANCE.createDecorationNode();
 		}
 		if (hasTitle) {
 			TitleStyle ts = NotationFactory.eINSTANCE.createTitleStyle();
 			ts.setShowTitle(true);
 			rv.getStyles().add(ts);
 		}
 		if (canSort) {
 			rv.getStyles().add(NotationFactory.eINSTANCE.createSortingStyle());
 		}
 		if (canFilter) {
 			rv.getStyles().add(NotationFactory.eINSTANCE.createFilteringStyle());
 		}
 		rv.setType(hint);
 		ViewUtil.insertChildView(owner, rv, ViewUtil.APPEND, true);
 		return rv;
 	}
 
 	/**
 	 * @generated
 	 */
 	private EObject getSemanticElement(IAdaptable semanticAdapter) {
 		if (semanticAdapter == null) {
 			return null;
 		}
 		EObject eObject = (EObject) semanticAdapter.getAdapter(EObject.class);
 		if (eObject != null) {
 			return EMFCoreUtil.resolve(TransactionUtil.getEditingDomain(eObject), eObject);
 		}
 		return null;
 	}
 
 	/**
 	 * @generated
 	 */
 	private IElementType getSemanticElementType(IAdaptable semanticAdapter) {
 		if (semanticAdapter == null) {
 			return null;
 		}
 		return (IElementType) semanticAdapter.getAdapter(IElementType.class);
 	}
 }
