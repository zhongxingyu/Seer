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
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /**
  * Panneau du jeu qui est aussi le contrôleur
  *
  * @author ankat
  */
 public class DefenderPanel extends JPanel implements Runnable, ConstantesDefender {
 
     Thread t;           // Thread principal
     //--------------------LES MONSTRES-----------------------
     Monstre monstre;    // Une instance de la classe Monstre
     private int delay;  // Delai pour l'exécution de la tâche qui fait apparaître les Monstres
     Graphics graph;     // Contexte graphique
     TaskPerformer taskPerformer; // Un ActionListener permettant l'apparition des monstres
     ArrayList<Monstre> listeMonstres = new ArrayList<Monstre>(); // Une liste contenant des monstres
     //-------------------LA BULLET----------------------------
     Bullet bullet; // Une instance de la classe Bullet
     private int XClic; // Coordonnées x du clic de souris
     private int YClic; // Coordonnées y du clic de souris
     private float a;   // Coefficient directeur de la droit y = ax+b que forme la bullet jusqu'au clic
     private int b;     // L'ordonnée à l'origine de la droit y = ax+b que forme la bullet jusqu'au clic
     // Pour dessiner une tourelle fixe
     private final ImageIcon imageTourelle = new ImageIcon(getClass().getResource("/tourelle100*90.jpeg"));
 
     public DefenderPanel() {
         this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
         this.addMouseListener(new ClickListener());
         bullet = new Bullet();
         taskPerformer = new TaskPerformer();
         t = new Thread(this);
         t.start();
 
     } // fin constructeur
 
     @Override
     public void paintComponent(Graphics g) {
         g.setColor(Color.white);
         g.fillRect(0, 0, this.getWidth(), this.getHeight());
         graph = g;
 
         // Dessine la bullet
         bullet.drawBullet(g, bullet.getPosXBullet(), bullet.getPosYBullet());
 
         // Dessine la tourelle fixe
         g.drawImage(imageTourelle.getImage(), 0, Y_DEPART_BULLET, null);
 
         // Boucle permettant de dessiner les monstres de la liste
         for (int i = 0; i < listeMonstres.size(); ++i) {
             int posX = listeMonstres.get(i).getPosXMonstres();
             int posY = listeMonstres.get(i).getPosYMonstres();
             listeMonstres.get(i).drawMonstres(g, posX, posY);
 
             // Move monster for next place
             listeMonstres.get(i).run();
            
            // Test collision
            if ( (bullet.getPosXBullet() >= listeMonstres.get(i).getPosXMonstres() && bullet.getPosXBullet() <= (listeMonstres.get(i).getPosXMonstres() + 86) )
                        && (bullet.getPosYBullet() >= listeMonstres.get(i).getPosYMonstres() && bullet.getPosYBullet() <= (listeMonstres.get(i).getPosYMonstres() + 100) ) ) {
                    listeMonstres.remove(i);
            }
         }
        
         // Après avoir placé tous les monstres on repeint la fenêtre
         repaint();
 
         if (DEBUG) {
             System.out.println("taille listeMonstres : " + listeMonstres.size()); // DEBUG
         }
     } // fin méthode paintComponent
 
     @Override
     public void run() {
         delay = 1500;
         new Timer(delay, taskPerformer).start();
//        colision();
     }
 
     //--------------------------------------------------------------------------
     //                      AUTRES METHODES DE CLASSE
     //--------------------------------------------------------------------------
     public void colision() {
         while (true) {
             for (int i = 0; i < listeMonstres.size(); ++i) {
 
                 if ( (bullet.getPosXBullet() >= listeMonstres.get(i).getPosXMonstres() && bullet.getPosXBullet() <= (listeMonstres.get(i).getPosXMonstres() + 86) )
                         && (bullet.getPosYBullet() >= listeMonstres.get(i).getPosYMonstres() && bullet.getPosYBullet() <= (listeMonstres.get(i).getPosYMonstres() + 100) ) ) {
                     listeMonstres.remove(i);
                 } // fin if
 
             } // fin for
             
  //          if(DEBUG){
  //              System.out.println("Je suis dans colision");
   //          }
             
         } // fin while
     } // fin méthode colision
 
     //-----------------------------------------------------------------------------
     //                      CLASSES INTERNES
     //-----------------------------------------------------------------------------
     /**
      * Tâche qui permet de faire apparaître des monstres en créant des nouveaux
      * monstres et en les ajoutant à la liste
      */
     public class TaskPerformer implements ActionListener {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             monstre = new Monstre();
             listeMonstres.add(monstre);
             new Thread(monstre).start();
            
         }
     } // fin classe TaskPerformer
 
     /**
      * Permet de réagit aux clics de souris pour tirer avec la Bullet
      */
     public class ClickListener extends MouseAdapter {
 
         public void mousePressed(MouseEvent e) {
 
             // Informe la bullet du clic effectué pour lui permettre de lancer sa méthode goBullet()
             bullet.setIsClick(true);
             // Récupère les coordonnées où on a cliqué
             XClic = e.getX();
             YClic = e.getY();
             // Calcul du coefficient directeur a = (y2 - y1) / (x2 - x1)
             a = (float) (YClic - Y_DEPART_BULLET) / (XClic - X_DEPART_BULLET);
             // b l'ordonnée à l'origine, donc coordonnées de départ (y pour x = 0)
             b = Y_DEPART_BULLET;
             // Envoie les nouvelles coordonnées à la bullet
             bullet.setA(a);
             bullet.setB(b);
 
             if (DEBUG) {
                 System.out.println("a = " + a + " b = " + b); // DEBUG
             }
 
             // lancement d'un nouveau thread
             new Thread(bullet).start();
            
            
 
         } // fin méthode mousePressed
     } // fin classe ClickListener
 } // fin classe DefenderPanel
