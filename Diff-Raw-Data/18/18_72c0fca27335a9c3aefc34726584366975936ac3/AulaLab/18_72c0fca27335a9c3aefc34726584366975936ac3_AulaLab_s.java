 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 
 /**
  *
  * @author Daniel
  */
 public class AulaLab extends Aula{
     
     private boolean material;
     
     /** 
     * Crea un Aula de laboratori.
     */
     public AulaLab (String nom, int cap, boolean mat) {
         super(nom, cap);
         this.material = mat;
     }
 
    AulaLab() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /**
      *  
      * @return Retorna si l'aula te material.
      */
     public boolean getMaterial(){
         return material;
     }
     
     /**
      *  
      * @param mar A material
      */    
     public void setMaterial(boolean material){
         this.material = material;
     }
 }
 
