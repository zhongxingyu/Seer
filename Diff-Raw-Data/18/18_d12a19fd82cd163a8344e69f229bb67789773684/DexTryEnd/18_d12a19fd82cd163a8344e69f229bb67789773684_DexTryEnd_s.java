 package uk.ac.cam.db538.dexter.dex.code.elem;
 
 import lombok.Getter;
 
 public class DexTryEnd extends DexCodeElement {
 
   @Getter private final int id;
 	
   public DexTryEnd(int id) {
     this.id = id;
   }
 
   @Override
   public String toString() {
     return "TRYEND" + Integer.toString(this.getId());
   }
 
   @Override
   public boolean cfgEndsBasicBlock() {
     return true;
   }
 }
