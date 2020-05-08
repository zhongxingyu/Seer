 /**
  * <copyright>
  * 
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  * 
  * </copyright>
  */
 package org.eclipse.graphiti.mm.pictograms;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.graphiti.mm.MmPackage;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Package</b> for the model.
  * It contains accessors for the meta objects to represent
  * <ul>
  *   <li>each class,</li>
  *   <li>each feature of each class,</li>
  *   <li>each enum,</li>
  *   <li>and each data type</li>
  * </ul>
  * <!-- end-user-doc -->
  * @see org.eclipse.graphiti.mm.pictograms.PictogramsFactory
  * @model kind="package"
  * @generated
  */
 public interface PictogramsPackage extends EPackage {
 	/**
 	 * The package name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNAME = "pictograms";
 
 	/**
 	 * The package namespace URI.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_URI = "http://eclipse.org/graphiti/mm/pictograms";
 
 	/**
 	 * The package namespace name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_PREFIX = "pi";
 
 	/**
 	 * The singleton instance of the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	PictogramsPackage eINSTANCE = org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl.init();
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.PictogramElementImpl <em>Pictogram Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramElementImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getPictogramElement()
 	 * @generated
 	 */
 	int PICTOGRAM_ELEMENT = 3;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT__PROPERTIES = MmPackage.GRAPHICS_ALGORITHM_CONTAINER__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT__VISIBLE = MmPackage.GRAPHICS_ALGORITHM_CONTAINER_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT__GRAPHICS_ALGORITHM = MmPackage.GRAPHICS_ALGORITHM_CONTAINER_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT__ACTIVE = MmPackage.GRAPHICS_ALGORITHM_CONTAINER_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT__LINK = MmPackage.GRAPHICS_ALGORITHM_CONTAINER_FEATURE_COUNT + 3;
 
 	/**
 	 * The number of structural features of the '<em>Pictogram Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_ELEMENT_FEATURE_COUNT = MmPackage.GRAPHICS_ALGORITHM_CONTAINER_FEATURE_COUNT + 4;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.AnchorContainerImpl <em>Anchor Container</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.AnchorContainerImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getAnchorContainer()
 	 * @generated
 	 */
 	int ANCHOR_CONTAINER = 6;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__PROPERTIES = PICTOGRAM_ELEMENT__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__VISIBLE = PICTOGRAM_ELEMENT__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__GRAPHICS_ALGORITHM = PICTOGRAM_ELEMENT__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__ACTIVE = PICTOGRAM_ELEMENT__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__LINK = PICTOGRAM_ELEMENT__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER__ANCHORS = PICTOGRAM_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Anchor Container</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_CONTAINER_FEATURE_COUNT = PICTOGRAM_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ShapeImpl <em>Shape</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ShapeImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getShape()
 	 * @generated
 	 */
 	int SHAPE = 0;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__PROPERTIES = ANCHOR_CONTAINER__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__VISIBLE = ANCHOR_CONTAINER__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__GRAPHICS_ALGORITHM = ANCHOR_CONTAINER__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__ACTIVE = ANCHOR_CONTAINER__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__LINK = ANCHOR_CONTAINER__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__ANCHORS = ANCHOR_CONTAINER__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Container</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE__CONTAINER = ANCHOR_CONTAINER_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Shape</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int SHAPE_FEATURE_COUNT = ANCHOR_CONTAINER_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ContainerShapeImpl <em>Container Shape</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ContainerShapeImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getContainerShape()
 	 * @generated
 	 */
 	int CONTAINER_SHAPE = 1;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__PROPERTIES = SHAPE__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__VISIBLE = SHAPE__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__GRAPHICS_ALGORITHM = SHAPE__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__ACTIVE = SHAPE__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__LINK = SHAPE__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__ANCHORS = SHAPE__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Container</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__CONTAINER = SHAPE__CONTAINER;
 
 	/**
 	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE__CHILDREN = SHAPE_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Container Shape</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONTAINER_SHAPE_FEATURE_COUNT = SHAPE_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl <em>Diagram</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getDiagram()
 	 * @generated
 	 */
 	int DIAGRAM = 2;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__PROPERTIES = CONTAINER_SHAPE__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__VISIBLE = CONTAINER_SHAPE__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__GRAPHICS_ALGORITHM = CONTAINER_SHAPE__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__ACTIVE = CONTAINER_SHAPE__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__LINK = CONTAINER_SHAPE__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__ANCHORS = CONTAINER_SHAPE__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Container</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__CONTAINER = CONTAINER_SHAPE__CONTAINER;
 
 	/**
 	 * The feature id for the '<em><b>Children</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__CHILDREN = CONTAINER_SHAPE__CHILDREN;
 
 	/**
 	 * The feature id for the '<em><b>Styles</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__STYLES = CONTAINER_SHAPE_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Grid Unit</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__GRID_UNIT = CONTAINER_SHAPE_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Diagram Type Id</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__DIAGRAM_TYPE_ID = CONTAINER_SHAPE_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>Connections</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__CONNECTIONS = CONTAINER_SHAPE_FEATURE_COUNT + 3;
 
 	/**
 	 * The feature id for the '<em><b>Name</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__NAME = CONTAINER_SHAPE_FEATURE_COUNT + 4;
 
 	/**
 	 * The feature id for the '<em><b>Snap To Grid</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__SNAP_TO_GRID = CONTAINER_SHAPE_FEATURE_COUNT + 5;
 
 	/**
 	 * The feature id for the '<em><b>Show Guides</b></em>' attribute. <!--
 	 * begin-user-doc -->
 	 * 
 	 * @deprecated This attribute simply exists because of compatibility (after
 	 *             removing it old diagrams would not be readable any more). It
 	 *             is never evaluated, instead the Graphiti framework queries
	 *             the DiagramTypeProvider if guides shall be shown in a diagram
	 *             editor.<br>
 	 * 
 	 *             <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__SHOW_GUIDES = CONTAINER_SHAPE_FEATURE_COUNT + 6;
 
 	/**
 	 * The feature id for the '<em><b>Colors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__COLORS = CONTAINER_SHAPE_FEATURE_COUNT + 7;
 
 	/**
 	 * The feature id for the '<em><b>Fonts</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__FONTS = CONTAINER_SHAPE_FEATURE_COUNT + 8;
 
 	/**
 	 * The feature id for the '<em><b>Pictogram Links</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__PICTOGRAM_LINKS = CONTAINER_SHAPE_FEATURE_COUNT + 9;
 
 	/**
 	 * The feature id for the '<em><b>Vertical Grid Unit</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM__VERTICAL_GRID_UNIT = CONTAINER_SHAPE_FEATURE_COUNT + 10;
 
 	/**
 	 * The number of structural features of the '<em>Diagram</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int DIAGRAM_FEATURE_COUNT = CONTAINER_SHAPE_FEATURE_COUNT + 11;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ConnectionImpl <em>Connection</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ConnectionImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getConnection()
 	 * @generated
 	 */
 	int CONNECTION = 4;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__PROPERTIES = ANCHOR_CONTAINER__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__VISIBLE = ANCHOR_CONTAINER__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__GRAPHICS_ALGORITHM = ANCHOR_CONTAINER__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__ACTIVE = ANCHOR_CONTAINER__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__LINK = ANCHOR_CONTAINER__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__ANCHORS = ANCHOR_CONTAINER__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Start</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__START = ANCHOR_CONTAINER_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>End</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__END = ANCHOR_CONTAINER_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__PARENT = ANCHOR_CONTAINER_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>Connection Decorators</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION__CONNECTION_DECORATORS = ANCHOR_CONTAINER_FEATURE_COUNT + 3;
 
 	/**
 	 * The number of structural features of the '<em>Connection</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_FEATURE_COUNT = ANCHOR_CONTAINER_FEATURE_COUNT + 4;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.AnchorImpl <em>Anchor</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.AnchorImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getAnchor()
 	 * @generated
 	 */
 	int ANCHOR = 5;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__PROPERTIES = PICTOGRAM_ELEMENT__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__VISIBLE = PICTOGRAM_ELEMENT__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__GRAPHICS_ALGORITHM = PICTOGRAM_ELEMENT__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__ACTIVE = PICTOGRAM_ELEMENT__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__LINK = PICTOGRAM_ELEMENT__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__PARENT = PICTOGRAM_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__OUTGOING_CONNECTIONS = PICTOGRAM_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__INCOMING_CONNECTIONS = PICTOGRAM_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The feature id for the '<em><b>Referenced Graphics Algorithm</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR__REFERENCED_GRAPHICS_ALGORITHM = PICTOGRAM_ELEMENT_FEATURE_COUNT + 3;
 
 	/**
 	 * The number of structural features of the '<em>Anchor</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int ANCHOR_FEATURE_COUNT = PICTOGRAM_ELEMENT_FEATURE_COUNT + 4;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.FixPointAnchorImpl <em>Fix Point Anchor</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.FixPointAnchorImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getFixPointAnchor()
 	 * @generated
 	 */
 	int FIX_POINT_ANCHOR = 7;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__PROPERTIES = ANCHOR__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__VISIBLE = ANCHOR__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__GRAPHICS_ALGORITHM = ANCHOR__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__ACTIVE = ANCHOR__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__LINK = ANCHOR__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__PARENT = ANCHOR__PARENT;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__OUTGOING_CONNECTIONS = ANCHOR__OUTGOING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__INCOMING_CONNECTIONS = ANCHOR__INCOMING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Referenced Graphics Algorithm</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__REFERENCED_GRAPHICS_ALGORITHM = ANCHOR__REFERENCED_GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Location</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR__LOCATION = ANCHOR_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Fix Point Anchor</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FIX_POINT_ANCHOR_FEATURE_COUNT = ANCHOR_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.BoxRelativeAnchorImpl <em>Box Relative Anchor</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.BoxRelativeAnchorImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getBoxRelativeAnchor()
 	 * @generated
 	 */
 	int BOX_RELATIVE_ANCHOR = 8;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__PROPERTIES = ANCHOR__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__VISIBLE = ANCHOR__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__GRAPHICS_ALGORITHM = ANCHOR__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__ACTIVE = ANCHOR__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__LINK = ANCHOR__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__PARENT = ANCHOR__PARENT;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__OUTGOING_CONNECTIONS = ANCHOR__OUTGOING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__INCOMING_CONNECTIONS = ANCHOR__INCOMING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Referenced Graphics Algorithm</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__REFERENCED_GRAPHICS_ALGORITHM = ANCHOR__REFERENCED_GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Relative Width</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__RELATIVE_WIDTH = ANCHOR_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Relative Height</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR__RELATIVE_HEIGHT = ANCHOR_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>Box Relative Anchor</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int BOX_RELATIVE_ANCHOR_FEATURE_COUNT = ANCHOR_FEATURE_COUNT + 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ChopboxAnchorImpl <em>Chopbox Anchor</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ChopboxAnchorImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getChopboxAnchor()
 	 * @generated
 	 */
 	int CHOPBOX_ANCHOR = 9;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__PROPERTIES = ANCHOR__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__VISIBLE = ANCHOR__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__GRAPHICS_ALGORITHM = ANCHOR__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__ACTIVE = ANCHOR__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__LINK = ANCHOR__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__PARENT = ANCHOR__PARENT;
 
 	/**
 	 * The feature id for the '<em><b>Outgoing Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__OUTGOING_CONNECTIONS = ANCHOR__OUTGOING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Incoming Connections</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__INCOMING_CONNECTIONS = ANCHOR__INCOMING_CONNECTIONS;
 
 	/**
 	 * The feature id for the '<em><b>Referenced Graphics Algorithm</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR__REFERENCED_GRAPHICS_ALGORITHM = ANCHOR__REFERENCED_GRAPHICS_ALGORITHM;
 
 	/**
 	 * The number of structural features of the '<em>Chopbox Anchor</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CHOPBOX_ANCHOR_FEATURE_COUNT = ANCHOR_FEATURE_COUNT + 0;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ConnectionDecoratorImpl <em>Connection Decorator</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ConnectionDecoratorImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getConnectionDecorator()
 	 * @generated
 	 */
 	int CONNECTION_DECORATOR = 10;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__PROPERTIES = SHAPE__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__VISIBLE = SHAPE__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__GRAPHICS_ALGORITHM = SHAPE__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__ACTIVE = SHAPE__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__LINK = SHAPE__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__ANCHORS = SHAPE__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Container</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__CONTAINER = SHAPE__CONTAINER;
 
 	/**
 	 * The feature id for the '<em><b>Location Relative</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__LOCATION_RELATIVE = SHAPE_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Location</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__LOCATION = SHAPE_FEATURE_COUNT + 1;
 
 	/**
 	 * The feature id for the '<em><b>Connection</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR__CONNECTION = SHAPE_FEATURE_COUNT + 2;
 
 	/**
 	 * The number of structural features of the '<em>Connection Decorator</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int CONNECTION_DECORATOR_FEATURE_COUNT = SHAPE_FEATURE_COUNT + 3;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl <em>Free Form Connection</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getFreeFormConnection()
 	 * @generated
 	 */
 	int FREE_FORM_CONNECTION = 11;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__PROPERTIES = CONNECTION__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__VISIBLE = CONNECTION__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__GRAPHICS_ALGORITHM = CONNECTION__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__ACTIVE = CONNECTION__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__LINK = CONNECTION__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__ANCHORS = CONNECTION__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Start</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__START = CONNECTION__START;
 
 	/**
 	 * The feature id for the '<em><b>End</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__END = CONNECTION__END;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__PARENT = CONNECTION__PARENT;
 
 	/**
 	 * The feature id for the '<em><b>Connection Decorators</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__CONNECTION_DECORATORS = CONNECTION__CONNECTION_DECORATORS;
 
 	/**
 	 * The feature id for the '<em><b>Bendpoints</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION__BENDPOINTS = CONNECTION_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Free Form Connection</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int FREE_FORM_CONNECTION_FEATURE_COUNT = CONNECTION_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ManhattanConnectionImpl <em>Manhattan Connection</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.ManhattanConnectionImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getManhattanConnection()
 	 * @generated
 	 */
 	int MANHATTAN_CONNECTION = 12;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__PROPERTIES = CONNECTION__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Visible</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__VISIBLE = CONNECTION__VISIBLE;
 
 	/**
 	 * The feature id for the '<em><b>Graphics Algorithm</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__GRAPHICS_ALGORITHM = CONNECTION__GRAPHICS_ALGORITHM;
 
 	/**
 	 * The feature id for the '<em><b>Active</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__ACTIVE = CONNECTION__ACTIVE;
 
 	/**
 	 * The feature id for the '<em><b>Link</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__LINK = CONNECTION__LINK;
 
 	/**
 	 * The feature id for the '<em><b>Anchors</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__ANCHORS = CONNECTION__ANCHORS;
 
 	/**
 	 * The feature id for the '<em><b>Start</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__START = CONNECTION__START;
 
 	/**
 	 * The feature id for the '<em><b>End</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__END = CONNECTION__END;
 
 	/**
 	 * The feature id for the '<em><b>Parent</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__PARENT = CONNECTION__PARENT;
 
 	/**
 	 * The feature id for the '<em><b>Connection Decorators</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION__CONNECTION_DECORATORS = CONNECTION__CONNECTION_DECORATORS;
 
 	/**
 	 * The number of structural features of the '<em>Manhattan Connection</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MANHATTAN_CONNECTION_FEATURE_COUNT = CONNECTION_FEATURE_COUNT + 0;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.graphiti.mm.pictograms.impl.PictogramLinkImpl <em>Pictogram Link</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramLinkImpl
 	 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getPictogramLink()
 	 * @generated
 	 */
 	int PICTOGRAM_LINK = 13;
 
 	/**
 	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_LINK__PROPERTIES = MmPackage.PROPERTY_CONTAINER__PROPERTIES;
 
 	/**
 	 * The feature id for the '<em><b>Pictogram Element</b></em>' container reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_LINK__PICTOGRAM_ELEMENT = MmPackage.PROPERTY_CONTAINER_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Business Objects</b></em>' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_LINK__BUSINESS_OBJECTS = MmPackage.PROPERTY_CONTAINER_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>Pictogram Link</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int PICTOGRAM_LINK_FEATURE_COUNT = MmPackage.PROPERTY_CONTAINER_FEATURE_COUNT + 2;
 
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.Shape <em>Shape</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Shape</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Shape
 	 * @generated
 	 */
 	EClass getShape();
 
 	/**
 	 * Returns the meta object for the container reference '{@link org.eclipse.graphiti.mm.pictograms.Shape#getContainer <em>Container</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the container reference '<em>Container</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Shape#getContainer()
 	 * @see #getShape()
 	 * @generated
 	 */
 	EReference getShape_Container();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.ContainerShape <em>Container Shape</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Container Shape</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ContainerShape
 	 * @generated
 	 */
 	EClass getContainerShape();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.ContainerShape#getChildren <em>Children</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Children</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ContainerShape#getChildren()
 	 * @see #getContainerShape()
 	 * @generated
 	 */
 	EReference getContainerShape_Children();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.Diagram <em>Diagram</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Diagram</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram
 	 * @generated
 	 */
 	EClass getDiagram();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getGridUnit <em>Grid Unit</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Grid Unit</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getGridUnit()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_GridUnit();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getDiagramTypeId <em>Diagram Type Id</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Diagram Type Id</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getDiagramTypeId()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_DiagramTypeId();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getConnections <em>Connections</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Connections</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getConnections()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EReference getDiagram_Connections();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getName <em>Name</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Name</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getName()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_Name();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.Diagram#isSnapToGrid <em>Snap To Grid</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Snap To Grid</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#isSnapToGrid()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_SnapToGrid();
 
 	/**
 	 * Returns the meta object for the attribute '
 	 * {@link org.eclipse.graphiti.mm.pictograms.Diagram <em>Show Guides</em>}'.
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @deprecated This attribute simply exists because of compatibility (after
 	 *             removing it old diagrams would not be readable any more). It
 	 *             is never evaluated, instead the Graphiti framework queries
	 *             the DiagramTypeProvider if guides shall be shown in a diagram
	 *             editor.<br>
 	 * 
 	 *             <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Show Guides</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_ShowGuides();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getColors <em>Colors</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Colors</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getColors()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EReference getDiagram_Colors();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getFonts <em>Fonts</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Fonts</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getFonts()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EReference getDiagram_Fonts();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getPictogramLinks <em>Pictogram Links</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Pictogram Links</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getPictogramLinks()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EReference getDiagram_PictogramLinks();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.Diagram#getVerticalGridUnit <em>Vertical Grid Unit</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Vertical Grid Unit</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Diagram#getVerticalGridUnit()
 	 * @see #getDiagram()
 	 * @generated
 	 */
 	EAttribute getDiagram_VerticalGridUnit();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.PictogramElement <em>Pictogram Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Pictogram Element</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramElement
 	 * @generated
 	 */
 	EClass getPictogramElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.PictogramElement#isVisible <em>Visible</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Visible</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramElement#isVisible()
 	 * @see #getPictogramElement()
 	 * @generated
 	 */
 	EAttribute getPictogramElement_Visible();
 
 	/**
 	 * Returns the meta object for the containment reference '{@link org.eclipse.graphiti.mm.pictograms.PictogramElement#getGraphicsAlgorithm <em>Graphics Algorithm</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference '<em>Graphics Algorithm</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramElement#getGraphicsAlgorithm()
 	 * @see #getPictogramElement()
 	 * @generated
 	 */
 	EReference getPictogramElement_GraphicsAlgorithm();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.PictogramElement#isActive <em>Active</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Active</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramElement#isActive()
 	 * @see #getPictogramElement()
 	 * @generated
 	 */
 	EAttribute getPictogramElement_Active();
 
 	/**
 	 * Returns the meta object for the containment reference '{@link org.eclipse.graphiti.mm.pictograms.PictogramElement#getLink <em>Link</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference '<em>Link</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramElement#getLink()
 	 * @see #getPictogramElement()
 	 * @generated
 	 */
 	EReference getPictogramElement_Link();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.Connection <em>Connection</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Connection</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Connection
 	 * @generated
 	 */
 	EClass getConnection();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.graphiti.mm.pictograms.Connection#getStart <em>Start</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Start</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Connection#getStart()
 	 * @see #getConnection()
 	 * @generated
 	 */
 	EReference getConnection_Start();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.graphiti.mm.pictograms.Connection#getEnd <em>End</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>End</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Connection#getEnd()
 	 * @see #getConnection()
 	 * @generated
 	 */
 	EReference getConnection_End();
 
 	/**
 	 * Returns the meta object for the container reference '{@link org.eclipse.graphiti.mm.pictograms.Connection#getParent <em>Parent</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the container reference '<em>Parent</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Connection#getParent()
 	 * @see #getConnection()
 	 * @generated
 	 */
 	EReference getConnection_Parent();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.Connection#getConnectionDecorators <em>Connection Decorators</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Connection Decorators</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Connection#getConnectionDecorators()
 	 * @see #getConnection()
 	 * @generated
 	 */
 	EReference getConnection_ConnectionDecorators();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.Anchor <em>Anchor</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Anchor</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Anchor
 	 * @generated
 	 */
 	EClass getAnchor();
 
 	/**
 	 * Returns the meta object for the container reference '{@link org.eclipse.graphiti.mm.pictograms.Anchor#getParent <em>Parent</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the container reference '<em>Parent</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Anchor#getParent()
 	 * @see #getAnchor()
 	 * @generated
 	 */
 	EReference getAnchor_Parent();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.graphiti.mm.pictograms.Anchor#getOutgoingConnections <em>Outgoing Connections</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Outgoing Connections</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Anchor#getOutgoingConnections()
 	 * @see #getAnchor()
 	 * @generated
 	 */
 	EReference getAnchor_OutgoingConnections();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.graphiti.mm.pictograms.Anchor#getIncomingConnections <em>Incoming Connections</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Incoming Connections</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Anchor#getIncomingConnections()
 	 * @see #getAnchor()
 	 * @generated
 	 */
 	EReference getAnchor_IncomingConnections();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.graphiti.mm.pictograms.Anchor#getReferencedGraphicsAlgorithm <em>Referenced Graphics Algorithm</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Referenced Graphics Algorithm</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.Anchor#getReferencedGraphicsAlgorithm()
 	 * @see #getAnchor()
 	 * @generated
 	 */
 	EReference getAnchor_ReferencedGraphicsAlgorithm();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.AnchorContainer <em>Anchor Container</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Anchor Container</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.AnchorContainer
 	 * @generated
 	 */
 	EClass getAnchorContainer();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.AnchorContainer#getAnchors <em>Anchors</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Anchors</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.AnchorContainer#getAnchors()
 	 * @see #getAnchorContainer()
 	 * @generated
 	 */
 	EReference getAnchorContainer_Anchors();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.FixPointAnchor <em>Fix Point Anchor</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Fix Point Anchor</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.FixPointAnchor
 	 * @generated
 	 */
 	EClass getFixPointAnchor();
 
 	/**
 	 * Returns the meta object for the containment reference '{@link org.eclipse.graphiti.mm.pictograms.FixPointAnchor#getLocation <em>Location</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference '<em>Location</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.FixPointAnchor#getLocation()
 	 * @see #getFixPointAnchor()
 	 * @generated
 	 */
 	EReference getFixPointAnchor_Location();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor <em>Box Relative Anchor</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Box Relative Anchor</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor
 	 * @generated
 	 */
 	EClass getBoxRelativeAnchor();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor#getRelativeWidth <em>Relative Width</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Relative Width</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor#getRelativeWidth()
 	 * @see #getBoxRelativeAnchor()
 	 * @generated
 	 */
 	EAttribute getBoxRelativeAnchor_RelativeWidth();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor#getRelativeHeight <em>Relative Height</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Relative Height</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor#getRelativeHeight()
 	 * @see #getBoxRelativeAnchor()
 	 * @generated
 	 */
 	EAttribute getBoxRelativeAnchor_RelativeHeight();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.ChopboxAnchor <em>Chopbox Anchor</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Chopbox Anchor</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ChopboxAnchor
 	 * @generated
 	 */
 	EClass getChopboxAnchor();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.ConnectionDecorator <em>Connection Decorator</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Connection Decorator</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ConnectionDecorator
 	 * @generated
 	 */
 	EClass getConnectionDecorator();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#isLocationRelative <em>Location Relative</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Location Relative</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#isLocationRelative()
 	 * @see #getConnectionDecorator()
 	 * @generated
 	 */
 	EAttribute getConnectionDecorator_LocationRelative();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#getLocation <em>Location</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Location</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#getLocation()
 	 * @see #getConnectionDecorator()
 	 * @generated
 	 */
 	EAttribute getConnectionDecorator_Location();
 
 	/**
 	 * Returns the meta object for the container reference '{@link org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#getConnection <em>Connection</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the container reference '<em>Connection</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ConnectionDecorator#getConnection()
 	 * @see #getConnectionDecorator()
 	 * @generated
 	 */
 	EReference getConnectionDecorator_Connection();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.FreeFormConnection <em>Free Form Connection</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Free Form Connection</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.FreeFormConnection
 	 * @generated
 	 */
 	EClass getFreeFormConnection();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.graphiti.mm.pictograms.FreeFormConnection#getBendpoints <em>Bendpoints</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Bendpoints</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.FreeFormConnection#getBendpoints()
 	 * @see #getFreeFormConnection()
 	 * @generated
 	 */
 	EReference getFreeFormConnection_Bendpoints();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.ManhattanConnection <em>Manhattan Connection</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Manhattan Connection</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.ManhattanConnection
 	 * @generated
 	 */
 	EClass getManhattanConnection();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.graphiti.mm.pictograms.PictogramLink <em>Pictogram Link</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Pictogram Link</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramLink
 	 * @generated
 	 */
 	EClass getPictogramLink();
 
 	/**
 	 * Returns the meta object for the container reference '{@link org.eclipse.graphiti.mm.pictograms.PictogramLink#getPictogramElement <em>Pictogram Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the container reference '<em>Pictogram Element</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramLink#getPictogramElement()
 	 * @see #getPictogramLink()
 	 * @generated
 	 */
 	EReference getPictogramLink_PictogramElement();
 
 	/**
 	 * Returns the meta object for the reference list '{@link org.eclipse.graphiti.mm.pictograms.PictogramLink#getBusinessObjects <em>Business Objects</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference list '<em>Business Objects</em>'.
 	 * @see org.eclipse.graphiti.mm.pictograms.PictogramLink#getBusinessObjects()
 	 * @see #getPictogramLink()
 	 * @generated
 	 */
 	EReference getPictogramLink_BusinessObjects();
 
 	/**
 	 * Returns the factory that creates the instances of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the factory that creates the instances of the model.
 	 * @generated
 	 */
 	PictogramsFactory getPictogramsFactory();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Defines literals for the meta objects that represent
 	 * <ul>
 	 *   <li>each class,</li>
 	 *   <li>each feature of each class,</li>
 	 *   <li>each enum,</li>
 	 *   <li>and each data type</li>
 	 * </ul>
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	interface Literals {
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ShapeImpl <em>Shape</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ShapeImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getShape()
 		 * @generated
 		 */
 		EClass SHAPE = eINSTANCE.getShape();
 
 		/**
 		 * The meta object literal for the '<em><b>Container</b></em>' container reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference SHAPE__CONTAINER = eINSTANCE.getShape_Container();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ContainerShapeImpl <em>Container Shape</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ContainerShapeImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getContainerShape()
 		 * @generated
 		 */
 		EClass CONTAINER_SHAPE = eINSTANCE.getContainerShape();
 
 		/**
 		 * The meta object literal for the '<em><b>Children</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONTAINER_SHAPE__CHILDREN = eINSTANCE.getContainerShape_Children();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl <em>Diagram</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getDiagram()
 		 * @generated
 		 */
 		EClass DIAGRAM = eINSTANCE.getDiagram();
 
 		/**
 		 * The meta object literal for the '<em><b>Grid Unit</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__GRID_UNIT = eINSTANCE.getDiagram_GridUnit();
 
 		/**
 		 * The meta object literal for the '<em><b>Diagram Type Id</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__DIAGRAM_TYPE_ID = eINSTANCE.getDiagram_DiagramTypeId();
 
 		/**
 		 * The meta object literal for the '<em><b>Connections</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference DIAGRAM__CONNECTIONS = eINSTANCE.getDiagram_Connections();
 
 		/**
 		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__NAME = eINSTANCE.getDiagram_Name();
 
 		/**
 		 * The meta object literal for the '<em><b>Snap To Grid</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__SNAP_TO_GRID = eINSTANCE.getDiagram_SnapToGrid();
 
 		/**
 		 * The meta object literal for the '<em><b>Show Guides</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__SHOW_GUIDES = eINSTANCE.getDiagram_ShowGuides();
 
 		/**
 		 * The meta object literal for the '<em><b>Colors</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference DIAGRAM__COLORS = eINSTANCE.getDiagram_Colors();
 
 		/**
 		 * The meta object literal for the '<em><b>Fonts</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference DIAGRAM__FONTS = eINSTANCE.getDiagram_Fonts();
 
 		/**
 		 * The meta object literal for the '<em><b>Pictogram Links</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference DIAGRAM__PICTOGRAM_LINKS = eINSTANCE.getDiagram_PictogramLinks();
 
 		/**
 		 * The meta object literal for the '<em><b>Vertical Grid Unit</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute DIAGRAM__VERTICAL_GRID_UNIT = eINSTANCE.getDiagram_VerticalGridUnit();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.PictogramElementImpl <em>Pictogram Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramElementImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getPictogramElement()
 		 * @generated
 		 */
 		EClass PICTOGRAM_ELEMENT = eINSTANCE.getPictogramElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Visible</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute PICTOGRAM_ELEMENT__VISIBLE = eINSTANCE.getPictogramElement_Visible();
 
 		/**
 		 * The meta object literal for the '<em><b>Graphics Algorithm</b></em>' containment reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference PICTOGRAM_ELEMENT__GRAPHICS_ALGORITHM = eINSTANCE.getPictogramElement_GraphicsAlgorithm();
 
 		/**
 		 * The meta object literal for the '<em><b>Active</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute PICTOGRAM_ELEMENT__ACTIVE = eINSTANCE.getPictogramElement_Active();
 
 		/**
 		 * The meta object literal for the '<em><b>Link</b></em>' containment reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference PICTOGRAM_ELEMENT__LINK = eINSTANCE.getPictogramElement_Link();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ConnectionImpl <em>Connection</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ConnectionImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getConnection()
 		 * @generated
 		 */
 		EClass CONNECTION = eINSTANCE.getConnection();
 
 		/**
 		 * The meta object literal for the '<em><b>Start</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONNECTION__START = eINSTANCE.getConnection_Start();
 
 		/**
 		 * The meta object literal for the '<em><b>End</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONNECTION__END = eINSTANCE.getConnection_End();
 
 		/**
 		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONNECTION__PARENT = eINSTANCE.getConnection_Parent();
 
 		/**
 		 * The meta object literal for the '<em><b>Connection Decorators</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONNECTION__CONNECTION_DECORATORS = eINSTANCE.getConnection_ConnectionDecorators();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.AnchorImpl <em>Anchor</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.AnchorImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getAnchor()
 		 * @generated
 		 */
 		EClass ANCHOR = eINSTANCE.getAnchor();
 
 		/**
 		 * The meta object literal for the '<em><b>Parent</b></em>' container reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ANCHOR__PARENT = eINSTANCE.getAnchor_Parent();
 
 		/**
 		 * The meta object literal for the '<em><b>Outgoing Connections</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ANCHOR__OUTGOING_CONNECTIONS = eINSTANCE.getAnchor_OutgoingConnections();
 
 		/**
 		 * The meta object literal for the '<em><b>Incoming Connections</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ANCHOR__INCOMING_CONNECTIONS = eINSTANCE.getAnchor_IncomingConnections();
 
 		/**
 		 * The meta object literal for the '<em><b>Referenced Graphics Algorithm</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ANCHOR__REFERENCED_GRAPHICS_ALGORITHM = eINSTANCE.getAnchor_ReferencedGraphicsAlgorithm();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.AnchorContainerImpl <em>Anchor Container</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.AnchorContainerImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getAnchorContainer()
 		 * @generated
 		 */
 		EClass ANCHOR_CONTAINER = eINSTANCE.getAnchorContainer();
 
 		/**
 		 * The meta object literal for the '<em><b>Anchors</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference ANCHOR_CONTAINER__ANCHORS = eINSTANCE.getAnchorContainer_Anchors();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.FixPointAnchorImpl <em>Fix Point Anchor</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.FixPointAnchorImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getFixPointAnchor()
 		 * @generated
 		 */
 		EClass FIX_POINT_ANCHOR = eINSTANCE.getFixPointAnchor();
 
 		/**
 		 * The meta object literal for the '<em><b>Location</b></em>' containment reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference FIX_POINT_ANCHOR__LOCATION = eINSTANCE.getFixPointAnchor_Location();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.BoxRelativeAnchorImpl <em>Box Relative Anchor</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.BoxRelativeAnchorImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getBoxRelativeAnchor()
 		 * @generated
 		 */
 		EClass BOX_RELATIVE_ANCHOR = eINSTANCE.getBoxRelativeAnchor();
 
 		/**
 		 * The meta object literal for the '<em><b>Relative Width</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute BOX_RELATIVE_ANCHOR__RELATIVE_WIDTH = eINSTANCE.getBoxRelativeAnchor_RelativeWidth();
 
 		/**
 		 * The meta object literal for the '<em><b>Relative Height</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute BOX_RELATIVE_ANCHOR__RELATIVE_HEIGHT = eINSTANCE.getBoxRelativeAnchor_RelativeHeight();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ChopboxAnchorImpl <em>Chopbox Anchor</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ChopboxAnchorImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getChopboxAnchor()
 		 * @generated
 		 */
 		EClass CHOPBOX_ANCHOR = eINSTANCE.getChopboxAnchor();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ConnectionDecoratorImpl <em>Connection Decorator</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ConnectionDecoratorImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getConnectionDecorator()
 		 * @generated
 		 */
 		EClass CONNECTION_DECORATOR = eINSTANCE.getConnectionDecorator();
 
 		/**
 		 * The meta object literal for the '<em><b>Location Relative</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute CONNECTION_DECORATOR__LOCATION_RELATIVE = eINSTANCE.getConnectionDecorator_LocationRelative();
 
 		/**
 		 * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute CONNECTION_DECORATOR__LOCATION = eINSTANCE.getConnectionDecorator_Location();
 
 		/**
 		 * The meta object literal for the '<em><b>Connection</b></em>' container reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference CONNECTION_DECORATOR__CONNECTION = eINSTANCE.getConnectionDecorator_Connection();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl <em>Free Form Connection</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.FreeFormConnectionImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getFreeFormConnection()
 		 * @generated
 		 */
 		EClass FREE_FORM_CONNECTION = eINSTANCE.getFreeFormConnection();
 
 		/**
 		 * The meta object literal for the '<em><b>Bendpoints</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference FREE_FORM_CONNECTION__BENDPOINTS = eINSTANCE.getFreeFormConnection_Bendpoints();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.ManhattanConnectionImpl <em>Manhattan Connection</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.ManhattanConnectionImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getManhattanConnection()
 		 * @generated
 		 */
 		EClass MANHATTAN_CONNECTION = eINSTANCE.getManhattanConnection();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.graphiti.mm.pictograms.impl.PictogramLinkImpl <em>Pictogram Link</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramLinkImpl
 		 * @see org.eclipse.graphiti.mm.pictograms.impl.PictogramsPackageImpl#getPictogramLink()
 		 * @generated
 		 */
 		EClass PICTOGRAM_LINK = eINSTANCE.getPictogramLink();
 
 		/**
 		 * The meta object literal for the '<em><b>Pictogram Element</b></em>' container reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference PICTOGRAM_LINK__PICTOGRAM_ELEMENT = eINSTANCE.getPictogramLink_PictogramElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Business Objects</b></em>' reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference PICTOGRAM_LINK__BUSINESS_OBJECTS = eINSTANCE.getPictogramLink_BusinessObjects();
 
 	}
 
 } //PictogramsPackage
