 // IntensityColorsLabelsMappingColorProviderAdapter.java
 package org.eclipse.stem.ui.adapters.color;
 
 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.Map;
 
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.stem.core.graph.DynamicNodeLabel;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.ui.adapters.color.AbstractRelativeValueColorProviderAdapterFactory.PropertySelectionListener;
 import org.eclipse.stem.ui.preferences.VisualizationPreferencePage;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 
 /**
  * This class adapts {@link Node} objects to
  * {@link IntensityColorsLabelsMappingColorProvider} object.
  */
 public class IntensityColorsLabelsMappingColorProviderAdapter extends
 		AbstractRelativeValueColorProviderAdapter implements PropertySelectionListener{
 	/**
 	 * default {@link AlphaComposite}
 	 */
 	//private static final AlphaComposite ONE_COMPOSITE = makeComposite(1);
 	private final Map<String, Color> colorMap = VisualizationPreferencePage.getColorMapping();
 	private ItemPropertyDescriptor selectedProperty;
 
 	/**
 	 *  Constructor
 	 */
 	public IntensityColorsLabelsMappingColorProviderAdapter() {
 		super();
 	} // IntensityColorsLabelsMappingColorProviderAdapter
 
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.AbstractRelativeValueColorProviderAdapter#getRelativeValue()
 	 */
 	@Override
 	public float getRelativeValue() {
 		
 		final Node node = (Node) getTarget();
 		
 		for (final NodeLabel label : node.getLabels()) {
 			if (label instanceof DynamicNodeLabel) {
 				DynamicNodeLabel nodeLabel = (DynamicNodeLabel)label;
 				if (nodeLabel.getDecorator() == selectedDecorator) {
 					setRVPTarget(nodeLabel);
 					return (float) rvp.getRelativeValue(selectedProperty);
 				}
 			}
 		}
 
 		return 0; // We shouldn't reach this point
 	} // getRelativeValue
 
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.AbstractRelativeValueColorProviderAdapter#updateGC(GC, float, boolean)
 	 */
 	@Override
 	public void updateGC(final GC gcToUpdate,
 			final float gainFactor, final boolean useLogScaling) {
 
 		final String selectedPropertyName = selectedProperty.getDisplayName(null);
 
 		// Get the color for the selected property (label)
 		if (colorMap.containsKey(selectedPropertyName)) {
 			foregroundFillColor = colorMap.get(selectedPropertyName);
 		}
 
 		// Update the G2D object with the appropriate intensity of the color
 		float alpha = getRelativeValue();
 		alpha *= gainFactor;
		// SED 8/4/2009. Make sure alpha is between 0 and 1
		if(alpha > 1.0) alpha = 1.0f;
		if(alpha < 0.0) alpha = 0.0f; // should never happen
 		if (alpha < ZERO_RELATIVE_VALUE_THRESHOLD) {
 			gcToUpdate.setAlpha(255);
 			gcToUpdate.setBackground(zero_FillColor);
 		} else {
 			if (useLogScaling == true) {
 				alpha = performLogScaling(alpha);
 			} 
 			//TODO Take care of gain factor
 			gcToUpdate.setAlpha((int)(alpha * 255));
 			gcToUpdate.setBackground(foregroundFillColor);
 		}
 	} // setG2DColor
 
 //	/**
 //	 * @param alpha
 //	 * @return
 //	 */
 //	private static AlphaComposite makeComposite(final float alpha) {
 //		return (AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
 //	} // makeComposite
 
 	/**
 	 * @param v
 	 * @return
 	 */
 	private float performLogScaling(final float v) {
 		double vDouble = v;
 		// make sure our linear scale is a fraction between 0.0 and 1.0
 		if (vDouble > 1.0) {
 			vDouble = 1.0; // saturation
 		} else if (v < 0.0) {
 			vDouble = 0.0; // should not happen
 		}
 
 		// logarithmic color display
 		// set scale 1.0 to 100.0
 		double vScaled100 = vDouble * 99.0;
 		// add 1.0 so we have no zeros
 		vScaled100 += 1.0;
 		// take the log base 10.0
 		double newV = Math.log10(vScaled100);
 		// we now have a number between 1 and 2 so divide by 2
 		newV /= 2.0;
 
 		return (float) newV;
 	} // performLogScaling
 
 	/**
 	 * @see org.eclipse.stem.ui.adapters.color.IntensityColorsLabelsMappingColorProviderAdapterFactory.PropertySelectionListener#propertySelected(org.eclipse.emf.edit.provider.ItemPropertyDescriptor)
 	 */
 	public void propertySelected(ItemPropertyDescriptor selectedProperty) {
 		this.selectedProperty = selectedProperty;		
 	}
 
 } // IntensityColorsLabelsMappingColorProviderAdapter
