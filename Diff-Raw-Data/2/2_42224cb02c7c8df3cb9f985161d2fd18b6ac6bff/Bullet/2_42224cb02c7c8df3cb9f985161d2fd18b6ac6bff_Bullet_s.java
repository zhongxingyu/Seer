 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package acdefender;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 /**
  *
  * @author ankat
  */
 public class Bullet implements Runnable, ConstantesDefender {
     
     private int posXDepartBullet = X_DEPART_BULLET ; // Position x de départ de la bullet
     private int posYDepartBullet = Y_DEPART_BULLET ; // Position y de départ de la bullet
     private int posXBullet ; // Position x de la Bullet en cours de tir
     private int posYBullet ; // Position y de la Bullet en cours de tir
     private volatile boolean isClick ; // Permet de savoir si un clic de souris a eu lieu
     private float a ;   // Coefficient directeur de la droit y = ax+b que forme la bullet jusqu'au clic
     private int b ;     // L'ordonnée à l'origine de la droit y = ax+b que forme la bullet jusqu'au clic
     
     public Bullet(){
         posXBullet = posXDepartBullet ;
         posYBullet = posYDepartBullet ;
         isClick = false ;
     } // fin constructeur
 
     
     
     /**
      * Dessine la Bullet
      * @param g : Graphics, le contexte graphic
      * @param x : int, coordonnées x de la bullet
      * @param y : int, coordonnées y de la bullet
      */
     public void drawBullet(Graphics g, int x, int y){
         
         if(DEBUG){
             System.out.println("x == " + x + "... y == " + y); // DEBUG
         }
         g.setColor(Color.BLUE);
         g.fillOval(x, y, TAILLE_BULLET, TAILLE_BULLET);
         
         
     } // fin méthode drawBullet
     
     
     @Override
     public void run() {
         
         if(DEBUG){
                     System.out.println("Je suis dans la méthode run de Bullet") ; // DEBUG
                 }
         
         while(isClick){
             // Réinitialise la position de départ de la bullet
             posXBullet = X_DEPART_BULLET ; posYBullet = Y_DEPART_BULLET ;
             
             for(int i = 0 ; i < FRAME_WIDTH ; ++i){
                 ++posXBullet ;
                 posYBullet = (int) ((a*posXBullet) + b) ; // y = ax+b
                 
                 try {
                Thread.sleep(5); // Pour poser le programme
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }	
                 
                 if(DEBUG){
                     System.out.println("Je suis dans la méthode run de Bullet") ;
                     System.out.println("xBullet = " + posXBullet + " yBullet = " + posYBullet ); // DEBUG
                 }
                 
                 
             } // fin for
             isClick = false ;
    
         } // fin while
         
     } // fin méthode run
     
     
     
     
     //--------------------------------------------------------------------------
     //                      GETTERS AND SETTERS
     //--------------------------------------------------------------------------
 
     public int getPosXBullet() {
         return posXBullet;
     }
 
     public int getPosYBullet() {
         return posYBullet;
     }
 
     public void setPosXBullet(int posXBullet) {
         this.posXBullet = posXBullet;
     }
 
     public void setPosYBullet(int posYBullet) {
         this.posYBullet = posYBullet;
     }
 
     public void setIsClick(boolean isClick) {
         this.isClick = isClick;
     }
 
     public void setA(float a) {
         this.a = a;
     }
 
     public void setB(int b) {
         this.b = b;
     }
 
     
     
     
     
     
 } // fin classe Bullet
