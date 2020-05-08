 package org.eclipse.stem.internal.data.geography.specifications;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.text.ParseException;
 
 import org.eclipse.stem.core.graph.EdgeLabel;
 import org.eclipse.stem.definitions.labels.CommonBorderRelationshipLabel;
 import org.eclipse.stem.definitions.labels.CommonBorderRelationshipLabelValue;
 import org.eclipse.stem.definitions.labels.LabelsFactory;
 import org.eclipse.stem.internal.data.geography.propertydata.CommonBorderGeographicRelationshipPropertyData;
 import org.eclipse.stem.internal.data.propertydata.PropertyData;
 import org.eclipse.stem.internal.data.propertydata.RelationshipPropertyData;
 
 /**
  * This class represents a {@link org.eclipse.stem.core.graph.Graph} that
  * contains {@link org.eclipse.stem.core.graph.Edge}s representing the
  * "shares a common border" relationship between two
  * {@link org.eclipse.stem.core.graph.Node}s.
  */
 public class CommonBorderGeographicRelationshipPropertyFileSpecification extends
 		GeographicRelationshipPropertyFileSpecification {
 
 	/**
 	 * This is the name used to identify "common border" edges.
 	 */
 	public final static String COMMON_BORDER_EDGE_NAME = "commonborder"; //$NON-NLS-1$
 
 	@Override
 	protected String getRelationshipName() {
 		return COMMON_BORDER_EDGE_NAME;
 	} // getRelationshipName
 
 	/**
 	 * @param isoKeyA
 	 *            the ISO3166-1 based key of the region represented by node A
 	 * @param isoKeyB
 	 *            the ISO3166-1 based key of the region represented by node B
 	 * @param geoRelationshipPropertyData
 	 *            the data that defines the relationship represented by the
 	 *            {@link org.eclipse.stem.core.graph.Edge}
 	 * @return a {@link org.eclipse.stem.core.graph.Label} that defines the
 	 *         relationship represented by the
 	 *         {@link org.eclipse.stem.core.graph.Edge}
 	 */
 	@Override
 	protected EdgeLabel createEdgeLabel(
 			final RelationshipPropertyData geoRelationshipPropertyData) {
 		final CommonBorderGeographicRelationshipPropertyData borderData = (CommonBorderGeographicRelationshipPropertyData) geoRelationshipPropertyData;
 		final CommonBorderRelationshipLabel retValue = LabelsFactory.eINSTANCE
 				.createCommonBorderRelationshipLabel();
 
 		retValue
 				.setURI(createRelationshipEdgeLabelURI(geoRelationshipPropertyData));
 
 		final CommonBorderRelationshipLabelValue currentValue = LabelsFactory.eINSTANCE
 				.createCommonBorderRelationshipLabelValue();
 		currentValue.setBorderLength(borderData.getLength());
 
 		retValue.setCurrentValue(currentValue);
 
 		return retValue;
 	} // createEdgeLabel
 
 	@Override
 	protected PropertyData createPropertyDataInstanceFromProperty(
 			final String dataPropertyKey, final String propertyValue) {
 		CommonBorderGeographicRelationshipPropertyData retValue = null;
 		try {
 			retValue = new CommonBorderGeographicRelationshipPropertyData(
 					dataPropertyKey, propertyValue);
 		} catch (final ParseException e) {
			e.printStackTrace();
			System.err.println("key:"+dataPropertyKey+" value:"+propertyValue);
 			System.err.println(e.getMessage());
 		}
 		return retValue;
 	} // createPropertyDataInstanceFromProperty
 } // CommonBorderGeographicRelationshipPropertyFileSpecification
