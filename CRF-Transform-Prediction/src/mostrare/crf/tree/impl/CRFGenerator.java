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
package mostrare.crf.tree.impl;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * CRF generator that takes CRFs written in XML as input.
 * 
 * @author missi
 */
public class CRFGenerator
{

	/**
	 * Logger for this class
	 */
	private static final Logger	logger		= Logger.getLogger(CRFGenerator.class);

	private static CRFGenerator	instance	= null;

	static
	{
		instance = new CRFGenerator();
	}

	private CRFGenerator()
	{

	}

	public static CRFGenerator getInstance()
	{
		return instance;
	}
	
	public CRFWithConstraintNode generateCRF (String crfPath)
	{       
		  JSONParser parser = new JSONParser();
		  CRFWithConstraintNode crfResult = null;
	      try { 
	           Object obj = parser.parse(new FileReader(crfPath)); 
               // estbalish label set
	           JSONObject jsonObject = (JSONObject)obj;
	           JSONArray labelList = (JSONArray) jsonObject.get("Label"); 
	   		   String[] rawAnnotations = new String[labelList.size()];
	   		   String[] annotations = new String[labelList.size()];
	   		   Map<String, Integer> annotationsInv = new HashMap<String, Integer>();

	   		   int emptyAnnotationIndex=-1;
	   		   for(int index=0; index<rawAnnotations.length; index++) {
	   			   rawAnnotations[index]=labelList.get(index).toString();
	   			   if(rawAnnotations[index].equals("EMPTY"))
	   				  emptyAnnotationIndex=index;
	   		   }
	   		   
	   		   if (emptyAnnotationIndex > 0)
			   {
				 annotations[0] = rawAnnotations[emptyAnnotationIndex];
				 if (emptyAnnotationIndex == labelList.size() - 1)
					System.arraycopy(rawAnnotations, 0, annotations, 1, labelList.size() - 1);
				 else
				 {
					System.arraycopy(rawAnnotations, 0, annotations, 1, emptyAnnotationIndex);
					System.arraycopy(rawAnnotations, emptyAnnotationIndex + 1, annotations,
							emptyAnnotationIndex + 1, labelList.size() - emptyAnnotationIndex - 1);
				 }
				 emptyAnnotationIndex = 0;
			   }
			   else
				  System.arraycopy(rawAnnotations, 0, annotations, 0, labelList.size());
			   
	   		   for (int i = 0; i < labelList.size(); i += 1)
				    annotationsInv.put(annotations[i], i);
	   		   
	   		   // establish character set
	   		   JSONArray characterList = (JSONArray) jsonObject.get("Character"); 
	   		   String[] characters = new String[characterList.size()];
	   		   Map<String, Integer> charactersInv = new HashMap<String, Integer>();

	   		   for(int index=0; index<characters.length; index++) {
	   			   characters[index]=characterList.get(index).toString();
	   		   }
			   
	   		   for (int i = 0; i < characterList.size(); i += 1)
	   			   charactersInv.put(characters[i], i);
	   		   
	   		   // establish constraint
	   		   Map<String, ArrayList<String>> mapnodetransform = new HashMap<String, ArrayList<String>>();
	   		   JSONObject constraintList = (JSONObject) jsonObject.get("Constraint"); 
	   		   Set<String> keyset= constraintList.keySet();
	   		   for(String a: keyset) {
		   		   JSONArray labelListkey = (JSONArray) constraintList.get(a); 
		   		   
                   ArrayList<String> allowedlabels = new ArrayList<String>(); 
           
                   for (int i=0;i<labelListkey.size();i++){ 
                	   allowedlabels.add(labelListkey.get(i).toString());
                   } 
                    
                   mapnodetransform.put(a.toString(), allowedlabels);
	   		   }
	   		   
	   		    // generate features and weights
	   		   TreeFeaturesBuilder featuresBuilder = new TreeFeaturesBuilder(TreeFeatureFactory
	   				.getInstance()); 
	   		   
	   		   // node observation  feature
	   		   JSONArray ObservationFeatureList = (JSONArray) jsonObject.get("ObservationFeature"); 
	   		   if(ObservationFeatureList!=null)
	   		    for(int innerindex=0; innerindex<ObservationFeatureList.size(); innerindex++) {
		           JSONObject observationFeature = (JSONObject) ObservationFeatureList.get(innerindex);
                   String nodeTypeinner=observationFeature.get("NodeType").toString();
                   String characteristicinner=observationFeature.get("Characteristic").toString();
                   String nodeLabelinner=observationFeature.get("NodeLabel").toString();
                   double weight=Double.parseDouble(observationFeature.get("Weight").toString());
                   int nodeLabelindex=annotationsInv.get(nodeLabelinner);
                   int characteristicinnerindex=charactersInv.get(characteristicinner);
       	   		   featuresBuilder.addFeature1(nodeTypeinner, nodeLabelindex, characteristicinnerindex,
	   					 weight);
	   		    }
	   		   
	   		  // node observation inverse feature
	   		   JSONArray InverseObservationFeatureList = (JSONArray) jsonObject.get("ObservationInverseFeature"); 
	   		   if(InverseObservationFeatureList!=null)
	   		    for(int innerindex=0; innerindex<InverseObservationFeatureList.size(); innerindex++) {
		           JSONObject observationInverseFeature = (JSONObject) InverseObservationFeatureList.get(innerindex);
                   String nodeTypeinner=observationInverseFeature.get("NodeType").toString();
                   String characteristicinner=observationInverseFeature.get("Characteristic").toString();
                   String nodeLabelinner=observationInverseFeature.get("NodeLabel").toString();
                   double weight=Double.parseDouble(observationInverseFeature.get("Weight").toString());
                   int nodeLabelindex=annotationsInv.get(nodeLabelinner);
                   int characteristicinnerindex=charactersInv.get(characteristicinner);
       	   		   featuresBuilder.addFeature1Inverse (nodeTypeinner, nodeLabelindex, characteristicinnerindex,
	   					 weight);
	   		    }
	   		   
	   		   // node fature
	   		   JSONArray nodeFeatureList = (JSONArray) jsonObject.get("NodeFeature"); 
	   		   
	   		   if(nodeFeatureList!=null)
	   		    for(int innerindex=0; innerindex<nodeFeatureList.size(); innerindex++) {
		           JSONObject nodeFeature = (JSONObject) nodeFeatureList.get(innerindex);
                   String nodeTypeinner=nodeFeature.get("NodeType").toString();
                   String nodeLabelinner=nodeFeature.get("NodeLabel").toString();
                   double weight=Double.parseDouble(nodeFeature.get("Weight").toString());
                   int nodeLabelindex=annotationsInv.get(nodeLabelinner);
       	   		   featuresBuilder.addFeature1Pure(nodeTypeinner, nodeLabelindex, weight);
	   		    }
	   		   
	   		   // edge feature
	   		   JSONArray edgeFeatureList = (JSONArray) jsonObject.get("EdgeFeature"); 
	   		   if(edgeFeatureList!=null)
	   		     for(int innerindex=0; innerindex<edgeFeatureList.size(); innerindex++) {
		           JSONObject edgeFeature = (JSONObject) edgeFeatureList.get(innerindex);
                   String parentnodeType=edgeFeature.get("ParentNodeType").toString();
                   String parentnodelabel=edgeFeature.get("ParentNodeLabel").toString();
                   String childnodeType=edgeFeature.get("ChildNodeType").toString();
                   String childnodelabel=edgeFeature.get("ChildNodeLabel").toString();
                   double weight=Double.parseDouble(edgeFeature.get("Weight").toString());
                  
                   int parentnodeLabelindex=annotationsInv.get(parentnodelabel);
                   int childnodeLabelindex=annotationsInv.get(childnodelabel);
                  
    	   		   featuresBuilder.addFeature2(parentnodeType, childnodeType, parentnodeLabelindex, childnodeLabelindex, 
	   					weight);
	   		   }  
	   		   
	   		   // triangle feature
	   		   JSONArray triangleFeatureList = (JSONArray) jsonObject.get("TriangleFeature");  
	   		   if(triangleFeatureList!=null)
	   		    for(int innerindex=0; innerindex<triangleFeatureList.size(); innerindex++) {
		          JSONObject triangleFeature = (JSONObject) triangleFeatureList.get(innerindex);
                  String parentnodeType=triangleFeature.get("ParentNodeType").toString();
                  String parentnodelabel=triangleFeature.get("ParentNodeLabel").toString();
                  String leftchildnodeType=triangleFeature.get("LeftChildNodeType").toString();
                  String leftchildnodelabel=triangleFeature.get("LeftChildNodeLabel").toString();
                  String rightchildnodeType=triangleFeature.get("RightChildNodeType").toString();
                  String rightchildnodelabel=triangleFeature.get("RightChildNodeLabel").toString();
                  double weight=Double.parseDouble(triangleFeature.get("Weight").toString());
                  
                  int parentnodeLabelindex=annotationsInv.get(parentnodelabel);
                  int leftchildnodeLabelindex=annotationsInv.get(leftchildnodelabel);
                  int rightchildnodeLabelindex=annotationsInv.get(rightchildnodelabel);

 	   		      featuresBuilder.addFeature3(parentnodeType, leftchildnodeType, rightchildnodeType, parentnodeLabelindex, 
 	   		    		leftchildnodeLabelindex, rightchildnodeLabelindex, weight); 
	   		    }
	   		   
	   		   // triangle feature with observation
	   		   JSONArray triangleFeatureWithObservationList = (JSONArray) jsonObject.get("TriangleFeatureWithObservation"); 
	   		   if(triangleFeatureWithObservationList!=null)
	   		     for(int innerindex=0; innerindex<triangleFeatureWithObservationList.size(); innerindex++) {
		           JSONObject triangleFeatureObservation = (JSONObject) triangleFeatureWithObservationList.get(innerindex);
                   String parentnodeType=triangleFeatureObservation.get("ParentNodeType").toString();
                   String parentnodelabel=triangleFeatureObservation.get("ParentNodeLabel").toString();
                   String leftchildnodeType=triangleFeatureObservation.get("LeftChildNodeType").toString();
                   String leftchildnodelabel=triangleFeatureObservation.get("LeftChildNodeLabel").toString();
                   String rightchildnodeType=triangleFeatureObservation.get("RightChildNodeType").toString();
                   String rightchildnodelabel=triangleFeatureObservation.get("RightChildNodeLabel").toString();
                   double weight=Double.parseDouble(triangleFeatureObservation.get("Weight").toString());
                  
                   int parentnodeLabelindex=annotationsInv.get(parentnodelabel);
                   int leftchildnodeLabelindex=annotationsInv.get(leftchildnodelabel);
                   int rightchildnodeLabelindex=annotationsInv.get(rightchildnodelabel);

 	   		       featuresBuilder.addFeature3Observation(parentnodeType, leftchildnodeType, rightchildnodeType, parentnodeLabelindex, 
 	   		    		leftchildnodeLabelindex, rightchildnodeLabelindex, weight); 
	   		    }
	   		   
	   		    CRFWithConstraintNode crfResult_ = new CRFWithConstraintNode (annotations,
	   					emptyAnnotationIndex, characters, mapnodetransform, featuresBuilder);
	   			
	   			crfResult = crfResult_;

	      } catch (Exception e) {
	        e.printStackTrace();
	     }
	      
 		 return crfResult;
	  }
}
