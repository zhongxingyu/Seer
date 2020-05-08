 package de.deepamehta.mehtagraph;
 
 
 
 public class ConnectedMehtaNode {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private MehtaNode mehtaNode;
     private MehtaEdge connectingMehtaEdge;
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public ConnectedMehtaNode(MehtaNode mehtaNode, MehtaEdge connectingMehtaEdge) {
         this.mehtaNode = mehtaNode;
         this.connectingMehtaEdge = connectingMehtaEdge;
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
     public MehtaNode getMehtaNode() {
         return mehtaNode;
     }
 
     public MehtaEdge getConnectingMehtaEdge() {
         return connectingMehtaEdge;
     }
 }
