 // RelativePhysicalGeographicRelationshipPropertyFileSpecification.java
 package org.eclipse.stem.internal.data.geography.specifications;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.text.ParseException;
 
 import org.eclipse.stem.core.graph.EdgeLabel;
 import org.eclipse.stem.definitions.labels.LabelsFactory;
 import org.eclipse.stem.definitions.labels.RelativePhysicalRelationshipLabel;
 import org.eclipse.stem.definitions.labels.RelativePhysicalRelationshipLabelValue;
 import org.eclipse.stem.internal.data.geography.propertydata.RelativePhysicalGeographicRelationshipPropertyData;
 import org.eclipse.stem.internal.data.propertydata.PropertyData;
 import org.eclipse.stem.internal.data.propertydata.RelationshipPropertyData;
 
 /**
  * This class represents a {@link org.eclipse.stem.core.graph.Graph} that
  * contains {@link org.eclipse.stem.core.graph.Edge}s representing a
  * "relative physical" relationship between two
  * {@link org.eclipse.stem.core.graph.Node}s.
  */
 public class RelativePhysicalGeographicRelationshipPropertyFileSpecification
 		extends GeographicRelationshipPropertyFileSpecification {
 
 	/**
 	 * This is the name used to identify "relativephysical" edges. These are
 	 * typically "containment" relationships between countries and lower level
 	 * administrative entities. e.g., USA->States->Counties
 	 */
 	public final static String RELATIVE_PHYSICAL_EDGE_NAME = "relativephysical"; //$NON-NLS-1$
 
 	@Override
 	protected String getRelationshipName() {
 		return RELATIVE_PHYSICAL_EDGE_NAME;
 	} // getRelationshipName
 
 	@Override
 	protected EdgeLabel createEdgeLabel(
 			final RelationshipPropertyData geoRelationshipGraphData) {
 		final RelativePhysicalGeographicRelationshipPropertyData relationshipData = (RelativePhysicalGeographicRelationshipPropertyData) geoRelationshipGraphData;
 		final RelativePhysicalRelationshipLabel retValue = LabelsFactory.eINSTANCE
 				.createRelativePhysicalRelationshipLabel();
 
 		retValue
 				.setURI(createRelationshipEdgeLabelURI(geoRelationshipGraphData));
 
 		final RelativePhysicalRelationshipLabelValue currentValue = LabelsFactory.eINSTANCE
 				.createRelativePhysicalRelationshipLabelValue();
 		currentValue.setAdjacent(false);
 		currentValue.setRelationship(relationshipData
 				.getRelativePhysicalRelationship());
 
 		retValue.setCurrentValue(currentValue);
 
 		return retValue;
 	} // createEdgeLabel
 
 	@Override
 	protected PropertyData createPropertyDataInstanceFromProperty(
 			final String dataPropertyKey, final String propertyValue) {
 		RelativePhysicalGeographicRelationshipPropertyData retValue = null;
 		try {
 			retValue = new RelativePhysicalGeographicRelationshipPropertyData(
 					dataPropertyKey, propertyValue);
 		} catch (final ParseException e) {
			e.printStackTrace();
			System.err.println("Key:"+dataPropertyKey+" Value:"+propertyValue);
 			System.err.println(e.getMessage());
 		}
 		return retValue;
 	} // createPropertyDataInstanceFromProperty
 
 	@Override
 	protected String getRelationshipSpecifier(
 			final RelationshipPropertyData relationshipGraphData) {
 		final RelationshipPropertyData relationshipData = relationshipGraphData;
 		final StringBuilder sb = new StringBuilder("/"); //$NON-NLS-1$
 		sb.append(relationshipData.getRelativePhysicalRelationship().toString()
 				.toLowerCase());
 		return sb.toString();
 	} // getRelationshipSpecifier
 
 } // RelativePhysicalGeographicRelationshipPropertyFileSpecification
