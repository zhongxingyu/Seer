 package org.geworkbench.engine.cascript;
 
 /**
 * This is a stub, to be filled in by Behrooz.
  *
 * @author John Watkinson
  */
 public class CasReturn extends CasDataType {
 
    CasDataType dataType;
 
     public CasReturn(CasDataType dataType) {
        this.dataType = dataType;
     }
 
     public CasDataType getRetValue() {
        return dataType;
     }
 
 }
