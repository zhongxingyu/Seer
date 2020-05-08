 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.kmcguire.KFactions;
 
 import java.io.Serializable;
 
 /**
  *
  * @author kmcguire
  */
 public class TypeDataID implements Serializable {
     public int      typeId;
     public byte     dataId;
     
     static final long serialVersionUID = 92430035327747330L;
     
     @Override
     public boolean equals(Object o) {
         TypeDataID          tdi;
         
         if (!(o instanceof TypeDataID))
             return false;
         
         tdi = (TypeDataID)o;
         
         if ((tdi.typeId == typeId)) // && (tdi.dataId == dataId))
             return true;
         return false;
     }
     
     @Override
     public int hashCode() {
        return typeId | (int)dataId;
     }
 }
