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
 package org.eclipse.graphiti.mm.pictograms.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.graphiti.mm.MmPackage;
 import org.eclipse.graphiti.mm.StyleContainer;
 import org.eclipse.graphiti.mm.algorithms.styles.Color;
 import org.eclipse.graphiti.mm.algorithms.styles.Font;
 import org.eclipse.graphiti.mm.algorithms.styles.Style;
 import org.eclipse.graphiti.mm.algorithms.styles.StylesPackage;
 import org.eclipse.graphiti.mm.pictograms.Connection;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.PictogramsPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Diagram</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getStyles <em>Styles</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getGridUnit <em>Grid Unit</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getDiagramTypeId <em>Diagram Type Id</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getConnections <em>Connections</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#isSnapToGrid <em>Snap To Grid</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#isShowGuides <em>Show Guides</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getColors <em>Colors</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getFonts <em>Fonts</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getPictogramLinks <em>Pictogram Links</em>}</li>
  *   <li>{@link org.eclipse.graphiti.mm.pictograms.impl.DiagramImpl#getVerticalGridUnit <em>Vertical Grid Unit</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class DiagramImpl extends ContainerShapeImpl implements Diagram {
 	/**
 	 * The cached value of the '{@link #getStyles() <em>Styles</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getStyles()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Style> styles;
 
 	/**
 	 * The default value of the '{@link #getGridUnit() <em>Grid Unit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGridUnit()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int GRID_UNIT_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getGridUnit() <em>Grid Unit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getGridUnit()
 	 * @generated
 	 * @ordered
 	 */
 	protected int gridUnit = GRID_UNIT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDiagramTypeId() <em>Diagram Type Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramTypeId()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DIAGRAM_TYPE_ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDiagramTypeId() <em>Diagram Type Id</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDiagramTypeId()
 	 * @generated
 	 * @ordered
 	 */
 	protected String diagramTypeId = DIAGRAM_TYPE_ID_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getConnections() <em>Connections</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getConnections()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Connection> connections;
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isSnapToGrid() <em>Snap To Grid</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isSnapToGrid()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean SNAP_TO_GRID_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isSnapToGrid() <em>Snap To Grid</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isSnapToGrid()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean snapToGrid = SNAP_TO_GRID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isShowGuides() <em>Show Guides</em>}'
 	 * attribute. <!-- begin-user-doc -->
 	 * 
 	 * @deprecated This attribute simply exists because of compatibility (after
 	 *             removing it old diagrams would not be readable any more). It
 	 *             is never evaluated, instead the Graphiti framework queries
	 *             the DiagramTypeProvider if guides shall be shown in a diagram
	 *             editor.<br>
 	 * 
 	 *             <!-- end-user-doc -->
 	 * @see #isShowGuides()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean SHOW_GUIDES_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isShowGuides() <em>Show Guides</em>}'
 	 * attribute. <!-- begin-user-doc -->
 	 * 
 	 * @deprecated This attribute simply exists because of compatibility (after
 	 *             removing it old diagrams would not be readable any more). It
 	 *             is never evaluated, instead the Graphiti framework queries
	 *             the DiagramTypeProvider if guides shall be shown in a diagram
	 *             editor.<br>
 	 * 
 	 *             <!-- end-user-doc -->
 	 * @see #isShowGuides()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean showGuides = SHOW_GUIDES_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getColors() <em>Colors</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getColors()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Color> colors;
 
 	/**
 	 * The cached value of the '{@link #getFonts() <em>Fonts</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getFonts()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Font> fonts;
 
 	/**
 	 * The cached value of the '{@link #getPictogramLinks() <em>Pictogram Links</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPictogramLinks()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<PictogramLink> pictogramLinks;
 
 	/**
 	 * The default value of the '{@link #getVerticalGridUnit() <em>Vertical Grid Unit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVerticalGridUnit()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int VERTICAL_GRID_UNIT_EDEFAULT = -1;
 
 	/**
 	 * The cached value of the '{@link #getVerticalGridUnit() <em>Vertical Grid Unit</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVerticalGridUnit()
 	 * @generated
 	 * @ordered
 	 */
 	protected int verticalGridUnit = VERTICAL_GRID_UNIT_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected DiagramImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return PictogramsPackage.Literals.DIAGRAM;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Style> getStyles() {
 		if (styles == null) {
 			styles = new EObjectContainmentWithInverseEList.Resolving<Style>(Style.class, this, PictogramsPackage.DIAGRAM__STYLES, StylesPackage.STYLE__STYLE_CONTAINER);
 		}
 		return styles;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public int getGridUnit() {
 		return gridUnit;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setGridUnit(int newGridUnit) {
 		int oldGridUnit = gridUnit;
 		gridUnit = newGridUnit;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__GRID_UNIT, oldGridUnit, gridUnit));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDiagramTypeId() {
 		return diagramTypeId;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDiagramTypeId(String newDiagramTypeId) {
 		String oldDiagramTypeId = diagramTypeId;
 		diagramTypeId = newDiagramTypeId;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__DIAGRAM_TYPE_ID, oldDiagramTypeId, diagramTypeId));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Connection> getConnections() {
 		if (connections == null) {
 			connections = new EObjectContainmentWithInverseEList.Resolving<Connection>(Connection.class, this, PictogramsPackage.DIAGRAM__CONNECTIONS, PictogramsPackage.CONNECTION__PARENT);
 		}
 		return connections;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSnapToGrid() {
 		return snapToGrid;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSnapToGrid(boolean newSnapToGrid) {
 		boolean oldSnapToGrid = snapToGrid;
 		snapToGrid = newSnapToGrid;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__SNAP_TO_GRID, oldSnapToGrid, snapToGrid));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isShowGuides() {
 		return showGuides;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setShowGuides(boolean newShowGuides) {
 		boolean oldShowGuides = showGuides;
 		showGuides = newShowGuides;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__SHOW_GUIDES, oldShowGuides, showGuides));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Color> getColors() {
 		if (colors == null) {
 			colors = new EObjectContainmentEList.Resolving<Color>(Color.class, this, PictogramsPackage.DIAGRAM__COLORS);
 		}
 		return colors;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Font> getFonts() {
 		if (fonts == null) {
 			fonts = new EObjectContainmentEList.Resolving<Font>(Font.class, this, PictogramsPackage.DIAGRAM__FONTS);
 		}
 		return fonts;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<PictogramLink> getPictogramLinks() {
 		if (pictogramLinks == null) {
 			pictogramLinks = new EObjectResolvingEList<PictogramLink>(PictogramLink.class, this, PictogramsPackage.DIAGRAM__PICTOGRAM_LINKS);
 		}
 		return pictogramLinks;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public int getVerticalGridUnit() {
 		return verticalGridUnit;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVerticalGridUnit(int newVerticalGridUnit) {
 		int oldVerticalGridUnit = verticalGridUnit;
 		verticalGridUnit = newVerticalGridUnit;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PictogramsPackage.DIAGRAM__VERTICAL_GRID_UNIT, oldVerticalGridUnit, verticalGridUnit));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getStyles()).basicAdd(otherEnd, msgs);
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				return ((InternalEList<InternalEObject>)(InternalEList<?>)getConnections()).basicAdd(otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				return ((InternalEList<?>)getStyles()).basicRemove(otherEnd, msgs);
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				return ((InternalEList<?>)getConnections()).basicRemove(otherEnd, msgs);
 			case PictogramsPackage.DIAGRAM__COLORS:
 				return ((InternalEList<?>)getColors()).basicRemove(otherEnd, msgs);
 			case PictogramsPackage.DIAGRAM__FONTS:
 				return ((InternalEList<?>)getFonts()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				return getStyles();
 			case PictogramsPackage.DIAGRAM__GRID_UNIT:
 				return getGridUnit();
 			case PictogramsPackage.DIAGRAM__DIAGRAM_TYPE_ID:
 				return getDiagramTypeId();
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				return getConnections();
 			case PictogramsPackage.DIAGRAM__NAME:
 				return getName();
 			case PictogramsPackage.DIAGRAM__SNAP_TO_GRID:
 				return isSnapToGrid();
 			case PictogramsPackage.DIAGRAM__SHOW_GUIDES:
 				return isShowGuides();
 			case PictogramsPackage.DIAGRAM__COLORS:
 				return getColors();
 			case PictogramsPackage.DIAGRAM__FONTS:
 				return getFonts();
 			case PictogramsPackage.DIAGRAM__PICTOGRAM_LINKS:
 				return getPictogramLinks();
 			case PictogramsPackage.DIAGRAM__VERTICAL_GRID_UNIT:
 				return getVerticalGridUnit();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				getStyles().clear();
 				getStyles().addAll((Collection<? extends Style>)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__GRID_UNIT:
 				setGridUnit((Integer)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__DIAGRAM_TYPE_ID:
 				setDiagramTypeId((String)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				getConnections().clear();
 				getConnections().addAll((Collection<? extends Connection>)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__NAME:
 				setName((String)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__SNAP_TO_GRID:
 				setSnapToGrid((Boolean)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__SHOW_GUIDES:
 				setShowGuides((Boolean)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__COLORS:
 				getColors().clear();
 				getColors().addAll((Collection<? extends Color>)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__FONTS:
 				getFonts().clear();
 				getFonts().addAll((Collection<? extends Font>)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__PICTOGRAM_LINKS:
 				getPictogramLinks().clear();
 				getPictogramLinks().addAll((Collection<? extends PictogramLink>)newValue);
 				return;
 			case PictogramsPackage.DIAGRAM__VERTICAL_GRID_UNIT:
 				setVerticalGridUnit((Integer)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				getStyles().clear();
 				return;
 			case PictogramsPackage.DIAGRAM__GRID_UNIT:
 				setGridUnit(GRID_UNIT_EDEFAULT);
 				return;
 			case PictogramsPackage.DIAGRAM__DIAGRAM_TYPE_ID:
 				setDiagramTypeId(DIAGRAM_TYPE_ID_EDEFAULT);
 				return;
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				getConnections().clear();
 				return;
 			case PictogramsPackage.DIAGRAM__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case PictogramsPackage.DIAGRAM__SNAP_TO_GRID:
 				setSnapToGrid(SNAP_TO_GRID_EDEFAULT);
 				return;
 			case PictogramsPackage.DIAGRAM__SHOW_GUIDES:
 				setShowGuides(SHOW_GUIDES_EDEFAULT);
 				return;
 			case PictogramsPackage.DIAGRAM__COLORS:
 				getColors().clear();
 				return;
 			case PictogramsPackage.DIAGRAM__FONTS:
 				getFonts().clear();
 				return;
 			case PictogramsPackage.DIAGRAM__PICTOGRAM_LINKS:
 				getPictogramLinks().clear();
 				return;
 			case PictogramsPackage.DIAGRAM__VERTICAL_GRID_UNIT:
 				setVerticalGridUnit(VERTICAL_GRID_UNIT_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case PictogramsPackage.DIAGRAM__STYLES:
 				return styles != null && !styles.isEmpty();
 			case PictogramsPackage.DIAGRAM__GRID_UNIT:
 				return gridUnit != GRID_UNIT_EDEFAULT;
 			case PictogramsPackage.DIAGRAM__DIAGRAM_TYPE_ID:
 				return DIAGRAM_TYPE_ID_EDEFAULT == null ? diagramTypeId != null : !DIAGRAM_TYPE_ID_EDEFAULT.equals(diagramTypeId);
 			case PictogramsPackage.DIAGRAM__CONNECTIONS:
 				return connections != null && !connections.isEmpty();
 			case PictogramsPackage.DIAGRAM__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case PictogramsPackage.DIAGRAM__SNAP_TO_GRID:
 				return snapToGrid != SNAP_TO_GRID_EDEFAULT;
 			case PictogramsPackage.DIAGRAM__SHOW_GUIDES:
 				return showGuides != SHOW_GUIDES_EDEFAULT;
 			case PictogramsPackage.DIAGRAM__COLORS:
 				return colors != null && !colors.isEmpty();
 			case PictogramsPackage.DIAGRAM__FONTS:
 				return fonts != null && !fonts.isEmpty();
 			case PictogramsPackage.DIAGRAM__PICTOGRAM_LINKS:
 				return pictogramLinks != null && !pictogramLinks.isEmpty();
 			case PictogramsPackage.DIAGRAM__VERTICAL_GRID_UNIT:
 				return verticalGridUnit != VERTICAL_GRID_UNIT_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
 		if (baseClass == StyleContainer.class) {
 			switch (derivedFeatureID) {
 				case PictogramsPackage.DIAGRAM__STYLES: return MmPackage.STYLE_CONTAINER__STYLES;
 				default: return -1;
 			}
 		}
 		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
 		if (baseClass == StyleContainer.class) {
 			switch (baseFeatureID) {
 				case MmPackage.STYLE_CONTAINER__STYLES: return PictogramsPackage.DIAGRAM__STYLES;
 				default: return -1;
 			}
 		}
 		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (gridUnit: ");
 		result.append(gridUnit);
 		result.append(", diagramTypeId: ");
 		result.append(diagramTypeId);
 		result.append(", name: ");
 		result.append(name);
 		result.append(", snapToGrid: ");
 		result.append(snapToGrid);
 		result.append(", showGuides: ");
 		result.append(showGuides);
 		result.append(", verticalGridUnit: ");
 		result.append(verticalGridUnit);
 		result.append(')');
 		return result.toString();
 	}
 
 } //DiagramImpl
