 package com.virtualdisk.network.util;
 
 public class DataNodeIdentifier
 {
     protected int nodeId;
     protected String nodeAddress;
     protected int nodePort;
 
     public DataNodeIdentifier(int id, String address, int port)
     {
         nodeId = id;
         nodeAddress = address;
         nodePort = port;
     }
 
     public int getNodeId()
     {
         return nodeId;
     }
 
     public String getNodeAddress()
     {
         return nodeAddress;
     }
 
     public int getPort()
     {
         return nodePort;
     }
 
    public final boolean equals(Object pThat)
     {
        if (this == pThat)
         {
            return true;
         }
        if (!(pThat instanceof DataNodeIdentifier))
         {
            return false;
         }
        DataNodeIdentifier that = (DataNodeIdentifier)pThat;
        return (  (nodeId == that.getNodeId())
               && (nodeAddress.equals(that.getNodeAddress())));
     }
 
     public final int hashCode()
     {
        return nodeId + nodeAddress.hashCode();
     }
 
     public String toString()
     {
         return "id: { id:" + nodeId + " host:" + nodeAddress + " port:" + nodePort + " }";
     }
 }
 
