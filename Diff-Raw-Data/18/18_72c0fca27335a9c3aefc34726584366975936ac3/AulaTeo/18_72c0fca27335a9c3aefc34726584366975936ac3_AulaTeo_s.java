 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 
 /**
  *
  * @author Daniel
  */
 public class AulaTeo extends Aula{
     
     private boolean projector;
     
     /** 
     * Crea un aula de laboratori.
     */
     public AulaTeo (String nom, int cap, boolean projector) {
        super(nom, cap);
        this.projector = projector;
     }
 
    AulaTeo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
     /**
      *  
      * @return Retorna si l'aula te projector.
      */
     public boolean getProjector(){
         return projector;
     }
     
     /**
      *  
      * @param projector.
      */    
    public void setProyector(boolean projector){
         this.projector = projector;
     }
 }
