 package tpneuneu;
 
 import graphisme.JCanvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Point;
 
 public abstract class Neuneu extends Mangeable{
 
   protected String name;
   protected int sexe;
   protected int niveau;
   protected int posX;
   protected int posY;
   protected static int nameId=-1;
 
 
   
 
   
   // getters
   public int getPosX(){
       return this.posX;
   }
   
     public int getPosY(){
       return this.posY;
   }
     
     public void setPosX(int i){
      posX=i;
   }  
     
   public void setPosY(int j){
      posY=j;
   }
     /**
      * 
      * @param repas repas à manger
      */
    public Neuneu(Color color, Point pos, Dimension dim, JCanvas jc){
                 super(color,pos,dim,jc);
    }; 
     
     
   public void manger(Mangeable repas) {
       if (repas instanceof Neuneu) { //if eat a Neuneu
           repas.setNiveau(0);
           this.setNiveau(100); //set energie of neuneu eaten to 0 (as dead) and to 100 for the eater
       }
       else{ //if eat a nourriture
           if(repas.niveau+this.niveau>100){
                 repas.setNiveau(100-repas.niveau+this.niveau); //set new niveau
                 this.setNiveau(100); //Neuneu has full energy
           }
           else{
                 repas.setNiveau(0); //no more food
                 this.setNiveau(this.niveau+repas.niveau);
           }
       }
   }

   public void seReproduire() {
   }
 
   public abstract void seDeplacer();
 
   public void majPresence(Case c) {
       c.listPresence.add((Mangeable)this);
   }
 
   /**
    * 
    */
   public Neuneu plusProcheVoisin(Loft loft) {
       int i=0;
       
       do { 
           for (int m=-1; m<=1; m+=2){
               for (int n=-1; n<=1; n=+2){
                     for (Mangeable element : loft.plateau[this.posX+i*m][this.posY+i*n].listPresence)
                           if(element instanceof Neuneu) {
                               return (Neuneu) element;
                           }
               }
           }
       } while (true);
   }
 
   public Nourriture plusProcheNourriture(Loft loft) {
       int i=0;
       
       do { 
           for (int m=-1; m<=1; m+=2){
               for (int n=-1; n<=1; n=+2){
                     for (Mangeable element : loft.plateau[this.posX+i*m][this.posY+i*n].listPresence)
                           if(element instanceof Nourriture) {
                               return (Nourriture) element;
                           }
               }
           }
       } while (true);
   }
   
 
 
 }
