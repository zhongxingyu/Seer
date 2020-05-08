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
     
     private boolean proyector;
     
     /** 
     * Create a AulaLab.
     */
     public AulaTeo (String nomAula, int capacitatAula, boolean pro) {
         super(nomAula, capacitatAula);
         proyector = pro;
     }
     
     /**
      *  
      * @return Returns if the AulaLab has proyector.
      */
     public boolean getProyector(){
         return proyector;
     }
     
     /**
      *  
     * @param pro A proyector.
      */    
     public void setProyector(boolean pro){
         proyector = pro;
     }
 }
