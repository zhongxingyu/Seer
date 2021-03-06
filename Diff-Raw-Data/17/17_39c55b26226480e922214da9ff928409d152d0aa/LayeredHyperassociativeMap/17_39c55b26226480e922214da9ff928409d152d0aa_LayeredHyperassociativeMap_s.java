 /******************************************************************************
  *                                                                             *
  *  Copyright: (c) Syncleus, Inc.                                              *
  *                                                                             *
  *  You may redistribute and modify this source code under the terms and       *
  *  conditions of the Open Source Community License - Type C version 1.0       *
  *  or any later version as published by Syncleus, Inc. at www.syncleus.com.   *
  *  There should be a copy of the license included with this file. If a copy   *
  *  of the license is not included you are granted no right to distribute or   *
  *  otherwise use this file except through a legal and valid license. You      *
  *  should also contact Syncleus, Inc. at the information below if you cannot  *
  *  find a license:                                                            *
  *                                                                             *
  *  Syncleus, Inc.                                                             *
  *  2604 South 12th Street                                                     *
  *  Philadelphia, PA 19148                                                     *
  *                                                                             *
  ******************************************************************************/
 package com.syncleus.core.dann.examples.hyperassociativemap.visualization;
 
 import com.syncleus.dann.hyperassociativemap.*;
 
 public class LayeredHyperassociativeMap extends HyperassociativeMap
 {
     private HyperassociativeNode layeredNodes[][];
     private static final int NODES_PER_LAYER = 16;
     
     LayeredHyperassociativeMap(int layers)
     {
 		super(3);
 		
         this.layeredNodes = new HyperassociativeNode[layers][NODES_PER_LAYER];
         
         //create the nodes
         for(int layerIndex = 0; layerIndex < layers; layerIndex++)
             for(int nodeIndex = 0; nodeIndex < NODES_PER_LAYER; nodeIndex++)
             {
                 this.layeredNodes[layerIndex][nodeIndex] = new HyperassociativeNode(this, HyperassociativeNode.randomCoordinates(3), 0.02d);
                 this.nodes.add(this.layeredNodes[layerIndex][nodeIndex]);
             }
        
         //connect the nodes
         for(int layerIndex = 0; layerIndex < layers; layerIndex++)
         {
             for(int nodeIndex = 0; nodeIndex < NODES_PER_LAYER; nodeIndex++)
             {
                 HyperassociativeNode currentNode = this.layeredNodes[layerIndex][nodeIndex];
                 for(int toNodeIndex = 0; toNodeIndex < NODES_PER_LAYER; toNodeIndex++)
                 {
                     if(layerIndex < (layers-1))
                     {
                         currentNode.associate(this.layeredNodes[layerIndex+1][toNodeIndex], 1.0);
                         this.layeredNodes[layerIndex+1][toNodeIndex].associate(currentNode, 1.0);
                     }
                     else
                     {
                         currentNode.associate(this.layeredNodes[0][toNodeIndex], 1.0);
                         this.layeredNodes[0][toNodeIndex].associate(currentNode, 1.0);
                     }
                 }
             }
         }
        
        //connect the last layer to itself
     }
 }
