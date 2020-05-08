 package org.systemsbiology.gaggle.admin;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import gov.pnnl.mif.api.types.MifProcessor;
 
 public class Handler implements MifProcessor {
   public Serializable listen(Serializable input) {
     Map<String,String> in = (Map<String,String>) input;
     if(in.get("type").equals("pollClients")) {
       // TODO: Send list of all clients as returned by ClientStateManager
     }
     return input;
   }
 
   public String getInputType() {
     return Serializable.class.getName();
   }
   public String getOutputType() {
     return Serializable.class.getName();
   }
 }
