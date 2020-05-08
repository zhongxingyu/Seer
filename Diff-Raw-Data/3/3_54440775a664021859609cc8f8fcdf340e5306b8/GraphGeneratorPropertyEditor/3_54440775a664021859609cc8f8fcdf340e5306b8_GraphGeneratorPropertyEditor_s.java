 package org.eclipse.stem.ui.graphgenerators.adapters.graphgeneratorpropertyeditor;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.IItemPropertySource;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.common.CommonPackage;
 import org.eclipse.stem.core.common.Identifiable;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.graphgenerators.GraphGenerator;
 import org.eclipse.stem.graphgenerators.GraphgeneratorsPackage;
 import org.eclipse.stem.graphgenerators.LatticeGraphGenerator;
 import org.eclipse.stem.graphgenerators.MigrationEdgeGraphGenerator;
 import org.eclipse.stem.graphgenerators.SquareLatticeGraphGenerator;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProvider;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapter;
 import org.eclipse.stem.ui.adapters.propertystrings.PropertyStringProviderAdapterFactory;
 import org.eclipse.stem.ui.graphgenerators.wizards.Messages;
 import org.eclipse.stem.ui.widgets.MatrixEditorDialog;
 import org.eclipse.stem.ui.widgets.MatrixEditorWidget.MatrixEditorValidator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 public class GraphGeneratorPropertyEditor extends org.eclipse.stem.ui.editors.GenericPropertyEditor {
 
 	public GraphGeneratorPropertyEditor(Composite parent, int style, IProject project) {
 		super(parent,style, project);
 	}
 
 	/**
 	 * Create the composite
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param projectValidator
 	 */
 	public GraphGeneratorPropertyEditor(final Composite parent, final int style,
 			final GraphGenerator graphGenerator,
 			final ModifyListener projectValidator, IProject project) {
 		super(parent, style, (Identifiable)graphGenerator, projectValidator, project);
 	} // GraphGeneratorPropertyEditor
 
 
 
 	/**
 	 * @param graphGenerator
 	 *            the {@link GraphGenerator} instance to populate.
 	 */
 	public Graph getGraph(final GraphGenerator graphGenerator) {
 		for (final Map.Entry<EStructuralFeature, Text> entry : map.entrySet()) {
			if(entry.getKey().getEContainingClass().getClassifierID() == GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR ) {
 				switch (entry.getKey().getFeatureID()) {
 					case GraphgeneratorsPackage.LATTICE_GRAPH_GENERATOR__XSIZE:
 						((LatticeGraphGenerator)graphGenerator).setXSize(Integer.parseInt(entry.getValue().getText()));
 						break;
 					case GraphgeneratorsPackage.LATTICE_GRAPH_GENERATOR__YSIZE:
 						((LatticeGraphGenerator)graphGenerator).setYSize(Integer.parseInt(entry.getValue().getText()));
 						break;
 					case GraphgeneratorsPackage.SQUARE_LATTICE_GRAPH_GENERATOR__AREA:
 						((SquareLatticeGraphGenerator)graphGenerator).setArea(Double.parseDouble(entry.getValue().getText()));
 						break;
 				}
 			} else if(entry.getKey().getEContainingClass().getClassifierID() == GraphgeneratorsPackage.MIGRATION_EDGE_GRAPH_GENERATOR) {
 				switch (entry.getKey().getFeatureID()) {
 					case GraphgeneratorsPackage.MIGRATION_EDGE_GRAPH_GENERATOR__LOCATION:
 						((MigrationEdgeGraphGenerator)graphGenerator).setLocation(STEMURI.createURI(entry.getValue().getText()));
 						break;
 				} // switch
 			}
 		}
 
 		for (final Map.Entry<EStructuralFeature, Boolean> entry : booleanMap.entrySet()) {
 
 			switch (entry.getKey().getFeatureID()) {
 			case GraphgeneratorsPackage.LATTICE_GRAPH_GENERATOR__USE_NEAREST_NEIGHBORS:
 				((LatticeGraphGenerator)graphGenerator).setUseNearestNeighbors(entry.getValue().booleanValue());
 				break;
 			case GraphgeneratorsPackage.LATTICE_GRAPH_GENERATOR__USE_NEXT_NEAREST_NEIGHBORS:
 				((LatticeGraphGenerator)graphGenerator).setUseNextNearestNeighbors(entry.getValue().booleanValue());
 				break;
 			case GraphgeneratorsPackage.LATTICE_GRAPH_GENERATOR__PERIODIC_BOUNDARIES:
 				((LatticeGraphGenerator)graphGenerator).setPeriodicBoundaries(entry.getValue().booleanValue());
 				break;
 			}
 		}
  
 	 if(graphGenerator.eClass().getClassifierID() == GraphgeneratorsPackage.MIGRATION_EDGE_GRAPH_GENERATOR) 
 		 ((MigrationEdgeGraphGenerator)graphGenerator).setProject(project);
 		// Do it!
 		return graphGenerator.getGraph();
 	} // populate
 
 	/**
 	 * @return <code>true</code> if the contents are valid, <code>false</code>
 	 *         otherwise.
 	 */
 	@Override
 	public boolean validate() {
 		boolean retValue = true;
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(GraphgeneratorsPackage.Literals.LATTICE_GRAPH_GENERATOR__XSIZE);
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = Messages
 							.getString("NGGWizErr1"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidIntValue(text.getText(), 1);
 					if (!retValue) {
 						// No
 						errorMessage = Messages
 								.getString("NGGWizErr2"); //$NON-NLS-1$
 					} // if
 				}
 			} // if text != null
 		} // if Transmission Rate
 
 		// Non-Linearity Coefficient
 		if (retValue) {
 			// Yes
 			final Text text = map
 					.get(GraphgeneratorsPackage.Literals.LATTICE_GRAPH_GENERATOR__YSIZE);
 			if (text != null) {
 				// Yes
 				retValue = !text.getText().equals(""); //$NON-NLS-1$
 				// nothing?
 				if (!retValue) {
 					// Yes
 					errorMessage = Messages
 							.getString("NGGWizErr3"); //$NON-NLS-1$
 				} // if
 				else {
 					// No
 					// Is it a valid value?
 					retValue = isValidIntValue(text.getText(), 1);
 					if (!retValue) {
 						// No
 						errorMessage = Messages
 								.getString("NGGWizErr4"); //$NON-NLS-1$
 					} // if
 				}
 			} // if text != null
 		} // if Non-Linearity Coefficient
 
 		
 		return retValue;
 	} // validate
 
 
 	/**
 	 * @param feature
 	 * @return <code>true</code> if the feature is a dublin core feature that
 	 *         is specified by a user.
 	 */
 	@Override
 	protected boolean isUserSpecifiedProperty(final EStructuralFeature feature) {
 		boolean retValue = false;
 		final EClass containingClass = feature.getEContainingClass();
 		if (containingClass.equals(GraphgeneratorsPackage.eINSTANCE.getGraphGenerator())
 				|| containingClass.getEAllSuperTypes().contains(
 						GraphgeneratorsPackage.eINSTANCE.getGraphGenerator())) {
 			// Yes
 			retValue = true;
 		} // 
 		return retValue;
 	} // isUserSpecifiedGraphGeneratorProperty
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	protected void checkSubclass() {
 		// Disable the check that prevents sub-classing of SWT components
 	}
 
 } // GraphGeneratorPropertyEditor
