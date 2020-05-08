 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package acdefender;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /**
  *
  * @author ankat
  */
 public class DefenderPanel extends JPanel implements Runnable, ConstantesDefender {
     /**
      * Thread principal
      */
     Thread t ;
     /**
      * Une instance de la classe Monstre
      */
     Monstre monstre ;
     /**
      * Coordonnée x du Monstre
      */
     private int xMonstre ;
     /**
      * Coordonnée y du Monstre
      */
     private int yMonstre ;
     /**
      * Delai pour l'exécution de la tâche qui fait apparaître les Monstres
      */
     private int delay ;
     /**
      * Contexte graphique
      */
     Graphics graph ;
     /**
      * Un ActionListener permettant l'apparition des monstres
      */
     TaskPerformer taskPerformer ;
     /**
      * Une liste contenant des monstres
      */
     ArrayList<Monstre> listeMonstres = new ArrayList<Monstre>();
     
     public DefenderPanel(){
 //       this.setBackground(Color.red);
         this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
         taskPerformer = new TaskPerformer() ;
         t = new Thread(this);
         t.start();
     } // fin constructeur
     
     public void paintComponent(Graphics g){
         g.setColor(Color.white);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         graph = g ;
        for(Monstre m : listeMonstres) m.drawMonstres(g, xMonstre, yMonstre);
       
  //      monstre = new Monstre() ;
  //      monstre.drawMonstres(g, 500, 200);
         
 
    }
     
  /*   public void goMonstres(){
         xMonstre = monstre.getPosXMonstres() ;
         yMonstre = monstre.getPosYMonstres() ;
         width = this.getWidth() ;
         for(int i = width ; i >= 0 ; --i){
             --xMonstre ;
             monstre.drawMonstres(this.getGraphics(), xMonstre, yMonstre);
             repaint() ;
             try {
                 Thread.sleep(10); // Pour poser le programme
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }	
         }
     } // fin méthode goMonstres
     */
     
     
     @Override
     public void run() {
  //       goMonstres() ;
         delay = 2000 ;
         new Timer(delay, taskPerformer).start();
         
         
     }
     
    
     public class TaskPerformer implements ActionListener{
 
         @Override
         public void actionPerformed(ActionEvent e) {
             monstre = new Monstre();
            xMonstre = monstre.getPosXMonstres() ; yMonstre = monstre.getPosYMonstres() ;
             listeMonstres.add(monstre) ;
             repaint() ;
             try {
                 Thread.sleep(10); // Pour poser le programme
             } catch (InterruptedException ie) {
                 ie.printStackTrace();
             }	
         }
         
     } // fin classe TaskPerformer
     
 } // fin classe DefenderPanel
