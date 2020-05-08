/*
 * Copyright (C) 2006-2007 MOSTRARE INRIA Project
 * 
 * This file is part of XCRF, an implementation of CRFs for trees (http://treecrf.gforge.inria.fr)
 * 
 * XCRF is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * XCRF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with XCRF; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mostrare.tree.io.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

import mostrare.crf.tree.EdgeFeature;
import mostrare.crf.tree.NodeFeature;
import mostrare.crf.tree.NodeFeatureInverse;
import mostrare.crf.tree.PureNodeFeature;
import mostrare.crf.tree.TriangleFeature;
import mostrare.crf.tree.TriangleFeatureWithObservation;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.tree.io.CRFWriter;

public class CRFToJsonWriter implements CRFWriter
{

	@Override
	public void writeCRF(CRFWithConstraintNode crf, String crfSavePath) throws IOException
	{
		if(crfSavePath.endsWith(".json"))
			System.err.println("The save path for the learned CRF should be end with .json");
		else writeCRFToJSON(crf, crfSavePath);
	} 
	   
	public void writeCRFToJSON(CRFWithConstraintNode crf, String crfSavePath) {
		
		// all labels, i.e., transforms names
		JsonArrayBuilder builderlabel = Json.createArrayBuilder();
		
		for(int index=0; index<crf.getAnnotationsEnum().getAnnotationStringArray().length;index++)
			builderlabel.add(crf.getAnnotationsEnum().getAnnotationStringArray()[index]);
		   
		JsonArray arrlabel = builderlabel.build();
		
		// all characteristics
        JsonArrayBuilder buildercharacter = Json.createArrayBuilder();
		
		for(int index=0; index<crf.getCharactersEnum().getCharacterStringArray().length;index++)
			buildercharacter.add(crf.getCharactersEnum().getCharacterStringArray()[index]);
		   
		JsonArray arrcharacter = buildercharacter.build();
		
		// all constraints
		JsonObjectBuilder builderNodeTransform=Json.createObjectBuilder();
		Map<String, ArrayList<String>> nodetransformmap=crf.nodetransformmap;

		for(String nodeType : nodetransformmap.keySet()) {
			
			 List<String> transformForNode = new ArrayList<String>();
			 
			 for(int indexall=0; indexall<nodetransformmap.get(nodeType).size();indexall++) {
			    transformForNode.add(nodetransformmap.get(nodeType).get(indexall));
			 }
			 
			 JsonArrayBuilder buildertempory = Json.createArrayBuilder(); 
			 for(int index=0; index<transformForNode.size(); index++) {
				 buildertempory.add(transformForNode.get(index));  
			 }
			 JsonArray arrtempory = buildertempory.build();
			 
			 builderNodeTransform.add(nodeType, arrtempory);
		}
		   
		JsonObject nodeTransformConstraint=builderNodeTransform.build();
		
		
		// node-observation features
		JsonArrayBuilder builderobservation = Json.createArrayBuilder();
		
		for(int index=0; index<crf.getFeatures1Number(); index++) {
			NodeFeature feature=(NodeFeature)crf.getFeatures1()[index];
			double weight=crf.getWeight1(index);
			JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			JsonObject objecttempory=buildertempory.add("NodeType", feature.getNodetype())
					.add("Characteristic", crf.getCharacterText(feature.getCharacterIndex()))
					.add("NodeLabel", crf.getAnnotationText(feature.getAnnotationIndex()))
					.add("Weight", weight).build();
			builderobservation.add(objecttempory);
		}
		
		JsonArray arrObservationFeature = builderobservation.build();
		
		// node-observation inverse features
		JsonArrayBuilder builderobservationinverse = Json.createArrayBuilder();
				
		for(int index=0; index<crf.getFeatures1InverseNumber(); index++) {
			 NodeFeatureInverse feature=(NodeFeatureInverse)crf.getFeatures1Inverse()[index];
			 double weight=crf.getWeight1Inverse(index);
			 JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			 JsonObject objecttempory=buildertempory.add("NodeType", feature.getNodetype())
							.add("Characteristic", crf.getCharacterText(feature.getCharacterIndex()))
							.add("NodeLabel", crf.getAnnotationText(feature.getAnnotationIndex()))
							.add("Weight", weight).build();
			 builderobservationinverse.add(objecttempory);
		 }
				
		JsonArray arrObservationInverseFeature = builderobservationinverse.build();		
		
		// node features
		JsonArrayBuilder builderNodeFeature = Json.createArrayBuilder();
				
		for(int index=0; index<crf.getFeatures1PureNumber(); index++) {
			 PureNodeFeature feature=(PureNodeFeature) crf.getFeatures1Pure()[index];
			 double weight=crf.getWeight1Pure(index);
			 JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			 JsonObject objecttempory=buildertempory.add("NodeType", feature.getNodetype())
							.add("NodeLabel", crf.getAnnotationText(feature.getAnnotationIndex()))
							.add("Weight", weight).build();
			 builderNodeFeature.add(objecttempory);
		 }
				
		JsonArray arrNodeFeature = builderNodeFeature.build();
		
		// edge features
        JsonArrayBuilder builderEdgeFeature = Json.createArrayBuilder();
		for(int index=0; index<crf.getFeatures2Number(); index++) {
			EdgeFeature feature=(EdgeFeature)crf.getFeatures2()[index];
			double weight=crf.getWeight2(index);
			JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			JsonObject objecttempory=buildertempory.add("ParentNodeType", feature.getParentNodeType())
					.add("ParentNodeLabel", crf.getAnnotationText(feature.getParentAnnotationIndex()))
					.add("ChildNodeType", feature.getChildNodeType())
					.add("ChildNodeLabel", crf.getAnnotationText(feature.getChildAnnotationIndex()))
					.add("Weight", weight).build();
			builderEdgeFeature.add(objecttempory);
		}
		
		JsonArray arrEdgeFeature = builderEdgeFeature.build();
	
		//triangle features
		 JsonArrayBuilder builderTriangleFeature = Json.createArrayBuilder();
			
			for(int index=0; index<crf.getFeatures3Number(); index++) {
				TriangleFeature feature=(TriangleFeature)crf.getFeatures3()[index];
				double weight=crf.getWeight3(index);
				JsonObjectBuilder buildertempory=Json.createObjectBuilder();
				JsonObject objecttempory=buildertempory.add("ParentNodeType", feature.getParentNodeType())
						.add("ParentNodeLabel", crf.getAnnotationText(feature.getParentAnnotationIndex()))
						.add("LeftChildNodeType", feature.getLeftchildNodeType())
						.add("LeftChildNodeLabel", crf.getAnnotationText(feature.getLeftchildAnnotationIndex()))
						.add("RightChildNodeType", feature.getRightchildNodeType())
						.add("RightChildNodeLabel", crf.getAnnotationText(feature.getRightchildAnnotationIndex()))
						.add("Weight", weight).build();
				builderTriangleFeature.add(objecttempory);
			}
			
		   JsonArray arrTriangleFeature = builderTriangleFeature.build();
		   
		 //triangle features with observation
			 JsonArrayBuilder builderTriangleFeatureWithObservation = Json.createArrayBuilder();
				
			for(int index=0; index<crf.getFeatures3ObservationNumber(); index++) {
					TriangleFeatureWithObservation feature=(TriangleFeatureWithObservation)crf.getFeatures3Observation()[index];
					double weight=crf.getWeight3Observation(index);
					JsonObjectBuilder buildertempory=Json.createObjectBuilder();
					JsonObject objecttempory=buildertempory.add("ParentNodeType", feature.getParentNodeType())
							.add("ParentNodeLabel", crf.getAnnotationText(feature.getParentAnnotationIndex()))
							.add("LeftChildNodeType", feature.getLeftchildNodeType())
							.add("LeftChildNodeLabel", crf.getAnnotationText(feature.getLeftchildAnnotationIndex()))
							.add("RightChildNodeType", feature.getRightchildNodeType())
							.add("RightChildNodeLabel", crf.getAnnotationText(feature.getRightchildAnnotationIndex()))
							.add("Weight", weight).build();
					builderTriangleFeatureWithObservation.add(objecttempory);
			}
				
			JsonArray arrTriangleFeatureWithObservation = builderTriangleFeatureWithObservation.build();
				
		   JsonObject crfObject = Json.createObjectBuilder()
	               .add("Label", arrlabel)
	               .add("Character", arrcharacter)
	               .add("Constraint", nodeTransformConstraint)
	               .add("ObservationFeature", arrObservationFeature)
	               .add("ObservationInverseFeature", arrObservationInverseFeature)
				   .add("NodeFeature", arrNodeFeature)
				   .add("EdgeFeature", arrEdgeFeature)
				   .add("TriangleFeature", arrTriangleFeature)
				   .add("TriangleFeatureWithObservation", arrTriangleFeatureWithObservation)
				   .build(); 
		   
		   Map<String, Boolean> config = new HashMap<>();
		   config.put(JsonGenerator.PRETTY_PRINTING, true);
		   try (PrintWriter pw = new PrintWriter(crfSavePath)
		    ; JsonWriter jsonWriter = Json.createWriterFactory(config).createWriter(pw)) {
		      jsonWriter.writeObject(crfObject);
		   } catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			  e1.printStackTrace();
		   }
	 }
}
