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
 * Panneau du jeu qui est aussi le contrôleur 
  * @author ankat
  */
 public class DefenderPanel extends JPanel implements Runnable, ConstantesDefender {
 
     
     Thread t;           // Thread principal
     Monstre monstre;    // Une instance de la classe Monstre
     private int delay;  // Delai pour l'exécution de la tâche qui fait apparaître les Monstres
     Graphics graph;     // Contexte graphique
     TaskPerformer taskPerformer; // Un ActionListener permettant l'apparition des monstres
     ArrayList<Monstre> listeMonstres = new ArrayList<Monstre>(); // Une liste contenant des monstres
 
     public DefenderPanel() {
         this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
         taskPerformer = new TaskPerformer();
         t = new Thread(this);
         t.start();
     } // fin constructeur
 
     @Override
     public void paintComponent(Graphics g) {
         g.setColor(Color.white);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         graph = g;
         // Boucle permettant de dessiner les monstres de la liste
         for (int i = 0; i < listeMonstres.size(); ++i) {
             int posX = listeMonstres.get(i).getPosXMonstres();
             int posY = listeMonstres.get(i).getPosYMonstres();
             listeMonstres.get(i).drawMonstres(g, posX, posY);
             
             // Move monster for next place
             listeMonstres.get(i).run();
         }
         
         // Après avoir placer tous les monstres on repeint la fenêtre
         repaint();
 
         if(DEBUG){
             System.out.println("taille listeMonstres : " + listeMonstres.size()); // DEBUG
         }
     } // fin méthode paintComponent
 
     @Override
     public void run() {
         delay = 1000;
         new Timer(delay, taskPerformer).start();
     }
 
     
     /**
      * Tâche qui permet de faire apparaître des monstres en créant des nouveaux
      * monstres et en les ajoutant à la liste
      */
     public class TaskPerformer implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             monstre = new Monstre();
             listeMonstres.add(monstre);
             new Thread (monstre).start();
             
 //            try {
 //                Thread.sleep(1); // Pour poser le programme
 //            } catch (InterruptedException ie) {
 //                System.err.println("Erreur: Thread.sleep(5) du actionPerformed"+
 //                                   " --> TaskPerformer");
 //            }
         }
     } // fin classe TaskPerformer
     
     
 } // fin classe DefenderPanel
