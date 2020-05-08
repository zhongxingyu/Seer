 package org.graphlab.net;
 
 import org.graphlab.ExecutionPhase;
import sun.rmi.rmic.iiop.ValueType;
 
 /**
  */
 public interface GraphLabNode {
 
     void remoteReceiveVertexData(int fromNode, int[] vertexIds, Object[] vertexData);
 
     void remoteReceiveGathers(int fromNode, int[] vertexIds, Object[] vertexData);
 
     void remoteStartPhase(ExecutionPhase phase, int fromVertex, int toVertex);
 
     void remoteTopResultsRequested(int topN);
 
 }
