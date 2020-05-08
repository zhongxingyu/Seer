 /*   This file is part of TP2.
  *
  *   TP2 is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   TP2 is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with TP2.  If not, see <http://www.gnu.org/licenses/>.
  */
 package graphique.component;
 
 import graphique.event.PointsObtenus;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.Random;
 import main.Main;
 import util.Collisionable;
 import util.Dessinable;
 import util.Vecteur;
 
 /**
  * Classe pour les ovnis.
  * @author Guillaume Poirier-Morency && Nafie Hamrani
  */
 public final class Ovni extends Dessinable implements Collisionable {
     ////////////////////////////////////////////////////////////////////////////
     // Variables propres aux ovnis    

     /**
      * Probabilité utilisé pour faire apparaître les ovnis.
      */
     public static final int PROBABILITE_APPARITION_OVNI = 1000;
     /**
      * Détermine si un boss est présent dans le jeu.
      */
     public static boolean isBoss = false;
     /**
      * ID de l'ennemi normal.
      */
     /**
      * ID de l'ennemi supersonique.
      */
     /**
      * ID du boss 1.
      */
     /**
      * ID du boss 2.
      */
     /**
      * ID du boss 3.
      */
     /**
      * ID du boss bonus.
      */
     private static final int ENNEMI_NORMAL = 1,
             ENNEMI_SUPERSONIQUE = 2,
             ENNEMI_BOSS_1 = 3,
             ENNEMI_BOSS_2 = 4,
             ENNEMI_BOSS_3 = 5,
             ENNEMI_BOSS_BONUS = 6;
     /**
      * Random utilisé pour les génération aléatoires de l'ovni.
      */
     private static Random random = new Random();
     ////////////////////////////////////////////////////////////////////////////
     /* Points!
      * Le joueur accumule Main.level fois les points ci-dessous.
      */
     private static final int ENNEMI_BOSS_1_POINTS = 20,
             ENNEMI_BOSS_2_POINTS = 30,
             ENNEMI_BOSS_3_POINTS = 40,
             ENNEMI_NORMAL_POINTS = 3,
             ENNEMI_SUPERSONIQUE_POINTS = 10,
             ENNEMI_BOSS_BONUS_POINTS = 50;
     ////////////////////////////////////////////////////////////////////////////
     private static boolean boss1Killed = false,
             boss2Killed = false,
             boss3Killed = false;
     ////////////////////////////////////////////////////////////////////////////
     /**
      * Entier utilisé pour définir la vitesse de tir de l'ovni
      */
     private int shootRate = 500;
     private int xDirection = 1;
     private int yDirection = 1;
     // Variables locales
     /**
      * 
      */
     private Vecteur position = new Vecteur();
     /**
      * 
      */
     private double vitesseX = 1.0;
     /**
      * 
      */
     private int vie;
     /**
      * 
      */
     private final int VIE_INIT;
     /**
      * 
      */
     private final int ID;
     /**
      * 
      */
     private boolean isOr;
     /**
      * 
      */
     private Rectangle rectangle = new Rectangle();
     ////////////////////////////////////////////////////////////////////////////
 
     /**
      * Constructeur pour l'objet Ovni.
      * @param x
      * @param y
      * @param id est l'id de l'ovni. Voir les constantes pour plus de détails.
      */
     private Ovni(int x, int y, int id) {
         /* On va pas faire ça compliqué. Il y a une méthode en dessous qui
          * s'appelle create ovni vas-y! 
          */
         // Détermine si l'ennemi est or
         isOr = random.nextInt(10) == 1;
         ID = id;
         position.x = x;
         position.y = y;
         configurerOvni(id);
         VIE_INIT = vie;
     }
 
     /**
      * Méthode appelée à chaque draw. Elle décide si un ovni doit être créé ainsi
      * que sa nature.
      */
     public static void createOvni() {
         int i = initializeID();
         int y = new Random().nextInt(350);
         switch (i) {
             case ENNEMI_BOSS_1:
                 Main.composantesDessinables.add(new Ovni(0, 0, i));
                 break;
             case ENNEMI_BOSS_2:
                 Main.composantesDessinables.add(new Ovni(0, y, i));
                 break;
             case ENNEMI_BOSS_3:
                 Main.composantesDessinables.add(new Ovni(0, y, i));
                 break;
             case ENNEMI_NORMAL:
                 Main.composantesDessinables.add(new Ovni(0, y, i));
                 break;
             case ENNEMI_SUPERSONIQUE:
                 Main.composantesDessinables.add(new Ovni(Main.getCanvasSizeX(), y, i));
                 break;
             case ENNEMI_BOSS_BONUS:
                 Main.composantesDessinables.add(new Ovni(Main.getCanvasSizeX(), y, i));
                 return;
         }
     }
 
     /**
      * Initialise le ID de l'ovni.
      * @return le id de l'ovni.
      */
     private static int initializeID() {
         int generateur = new Random().nextInt(PROBABILITE_APPARITION_OVNI);
         if (!isBoss) {
            if (Main.timerSeconds >= 120000) {
                 isBoss = true;
                 if (!boss1Killed) {
                     return ENNEMI_BOSS_1;
                 } else if (!boss2Killed) {
                     return ENNEMI_BOSS_2;
                 } else if (!boss3Killed) {
                     return ENNEMI_BOSS_3;
                 } else {
                     return ENNEMI_BOSS_BONUS;
                 }
             }
             if (generateur <= 25) {
                 return ENNEMI_NORMAL;
             } else if (generateur <= 30) {
                 return ENNEMI_SUPERSONIQUE;
             } else {
                 return 0;
             }
         }
         return 0;
     }
 
     /**
      * Effectue un tir ennemi.     
      */
     private void tirer() {
         // Sa devrait être cool maintenant...
         /* Le numéro du id correspond au id du missile.
          * L'ennemi régulier possède le projectile régulier, etc...
          */
         switch (ID) {
             case ENNEMI_NORMAL:
                 Main.composantesDessinables.add(new ProjectileEnnemi(position, ProjectileEnnemi.PROJECTILE_ENNEMI));
                 break;
             case ENNEMI_SUPERSONIQUE:
                 Main.composantesDessinables.add(new ProjectileEnnemi(position, ProjectileEnnemi.PROJECTILE_ENNEMI_SUPERSONIC));
                 break;
             /* C'est le même id pour les 3 boss, mais l'image change.
              * 
              */
             case ENNEMI_BOSS_1:
             case ENNEMI_BOSS_2:
             case ENNEMI_BOSS_3:
             case ENNEMI_BOSS_BONUS:
                 Main.composantesDessinables.add(new ProjectileEnnemi(position, ProjectileEnnemi.PROJECTILE_BOSS));
                 break;
         }
     }
 
     /**
      * Configure l'ovni en fonction de son id.
      * @param id est l'id de l'ovni. Voir les constantes pour plus d'informations.
      */
     private void configurerOvni(int id) {
         switch (id) {
             case ENNEMI_NORMAL://img enemi et enemi or et vie
                 if (isOr) {
                     image0 = Main.imageBank.ennemiOr;
                     vie = 30;
                 } else {
                     image0 = Main.imageBank.ennemi;
                     vie = 10;
                 }
                 break;
             case ENNEMI_SUPERSONIQUE:// image0 ennemiSupersonic et supersonicor
                 if (isOr) {
                     image0 = Main.imageBank.ennemiSupersonicOr;
                     vie = 50 * Main.level;
                     vitesseX = 3;
                 } else {
                     image0 = Main.imageBank.ennemiSupersonic;
                     vie = 15 * Main.level;
                     vitesseX = 3;
                 }
                 break;
             case ENNEMI_BOSS_1:// image0 boss 1
                 image0 = Main.imageBank.boss;
                 vie = 1000;
                 break;
             case ENNEMI_BOSS_2:// image0 boss 2
                 image0 = Main.imageBank.boss;
                 vie = 1500;
                 break;
             case ENNEMI_BOSS_3:// image0 boss 3
                 image0 = Main.imageBank.boss;
                 vie = 2000;
                 break;
             case ENNEMI_BOSS_BONUS:
                 image0 = Main.imageBank.ennemi;
                 vie = 15 * Main.level;
                 break;
             default:
                 System.out.println("Veuillez entre une identification valide (id) dans le constructuer de l'objet" + id);
         }
     }
 
     /**
      * Action effectuée à chaque draw du canvas principal.
      */
     private void action() {
         switch (ID) {
             case ENNEMI_NORMAL:
                 // mouvement d'un enemi normal 
                 position.x += 3 * vitesseX;
                 break;
             case ENNEMI_SUPERSONIQUE:
                 // mouvement du ennemiSupersonic
                 position.x -= 3 * vitesseX;
                 break;
             case ENNEMI_BOSS_1:
                 // mouvement du boss 1
                 mouvementBoss(15, 300);
                 break;
             case ENNEMI_BOSS_2:
                 // mouvement du boss 2
                 vitesseX = 3;
                 mouvementBoss(15, 745);
                 break;
             case ENNEMI_BOSS_3:
                 // mouvement du boss 3
                 break;
             case ENNEMI_BOSS_BONUS:
                 // Mouvement du boss bonus
                 break;
             default:
                 System.out.println("Veuillez entre une identification valide (id) dans le constructuer de l'objet");
         }
         /* L'expression ternaire fait en sorte que le shooting rate des boss est
          * doublé si deux canons sont actifs sur la carte.
          * 
          */
 
         if ((new Random()).nextInt(shootRate / (Canon.isCanon2ValidTarget ? 2 : 1)) == 1) {
             tirer();
         }
     }
 
     /**
      * Mouvements spécifiques des boss.
      * @param ymin
      * @param ymax 
      */
     private void mouvementBoss(int ymin, int ymax) {
 
         final int XMIN = 0;
         final int XMAX = (int) (Main.getCanvasSizeX() - this.rectangle.getWidth());
         final int YMIN = ymin;
         final int YMAX = ymax;
         int deplacementX = 3;
         int deplacementY = 3;
 //
 //        if (position.x == XMIN) {
 //            setxDirection(1);
 //        } else if (position.x == XMAX) {
 //            setxDirection(-1);
 //        }
 //        if (position.y == YMIN) {
 //            setyDirection(1);
 //        } else if (position.y == YMAX) {
 //            setyDirection(-1);
 //        }
      //   position.x += deplacementX * xDirection * vitesseX;
        // position.y += deplacementY * yDirection * vitesseX;
     }
 
     /**
      * 
      * @param xDirection 
      */
     private void setxDirection(int xDirection) {
         this.xDirection = xDirection;
     }
 
     /**
      * 
      * @param yDirection 
      */
     private void setyDirection(int yDirection) {
         this.yDirection = yDirection;
     }
     ////////////////////////////////////////////////////////////////////////////
     // Méthode de la classe abstraite Dessinable
 
     @Override
     public void dessiner(Graphics g) {
         action();
         g.drawImage(image0, (int) position.x, (int) position.y, null);
         g.setColor(Color.RED);
         g.fillRect((int) position.x, (int) position.y, (int) ((double) image0.getWidth(null) * (((double) vie) / ((double) VIE_INIT))), 10);
         g.setColor(Color.BLACK);
         if (position.x > Main.getCanvasSizeX()) {
             isDessinable = false;
         }
     }
 
     @Override
     public void dessinerDeboguage(Graphics g) {
         action();
         g.drawOval((int) (position.x), (int) (position.y), 100, 100);
         g.drawString("Ovni", (int) (position.x) + 20, (int) (position.y) + 45);
         g.drawString("Type : " + (isOr ? "or" : "normal"), (int) (position.x) + 20, (int) (position.y) + 60);
         g.drawString("Vies : " + vie + " vies", (int) (position.x) + 20, (int) (position.y) + 75);
         if (position.x > Main.getCanvasSizeX()) {
             isDessinable = false;
         }
     }
 
     @Override
     public Rectangle getRectangle() {
         int longeur = 0, hauteur = 0;
         switch (ID) {
             case ENNEMI_NORMAL://Grandeur du rectangle d'un enemi normal 
                 longeur = hauteur = 100;
                 break;
             case ENNEMI_SUPERSONIQUE:// Grandeur du rectangle  du ennemiSupersonic
                 longeur = 150;
                 hauteur = 100;
                 break;
             case ENNEMI_BOSS_1:// Grandeur du rectangle  du boss 1
                 longeur = 450;
                 hauteur = 206;
                 break;
             case ENNEMI_BOSS_2:// Grandeur du rectangle  du boss 2
                 longeur = hauteur = 450;
                 break;
             case ENNEMI_BOSS_3:// Grandeur du rectangle  du boss 3
                 longeur = 500;
                 hauteur = 400;
                 break;
             default:
                 System.out.println("Veuillez entre une identification valide (id) dans le constructuer de l'objet");
         }
         rectangle.x = (int) position.x;
         rectangle.y = (int) position.y;
         rectangle.height = hauteur;
         rectangle.width = longeur;
         return rectangle;
     }
 
     @Override
     public void collision(Collisionable c) {
         if (c instanceof Projectile) {
             this.vie -= c.getDommage();
         }
         if (vie <= 0) {
             this.isDessinable = false;
 
             // Mort des ovnis
             switch (ID) {
                 case Ovni.ENNEMI_BOSS_1:
                     Main.points += ENNEMI_BOSS_1_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, ENNEMI_BOSS_1_POINTS));
                     Main.setGameLevel(Main.LEVEL_2);
                     boss1Killed = true;
                     isBoss = false;
                     Main.timerSeconds = 0;
                     break;
                 case Ovni.ENNEMI_BOSS_2:
                     Main.points += ENNEMI_BOSS_2_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, ENNEMI_BOSS_2_POINTS));
                    Main.setGameLevel(Main.LEVEL_3);
                     boss2Killed = true;
                     isBoss = false;
                     Main.timerSeconds = 0;
                     break;
                 case Ovni.ENNEMI_BOSS_3:
                     Main.points += ENNEMI_BOSS_3_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, ENNEMI_BOSS_3_POINTS));
                    Main.setGameLevel(Main.LEVEL_BONUS);
                     boss3Killed = true;
                     isBoss = false;
                     Main.timerSeconds = 0;
                     break;
                 case Ovni.ENNEMI_NORMAL:
                     // Il ne s'agit pas d'un boss... Mais on donne des points!
                     Main.points += ENNEMI_NORMAL_POINTS * (isOr ? 2 : 1) * Main.level;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, ENNEMI_NORMAL_POINTS * (isOr ? 2 : 1) * Main.level));
                     //Main.tentaculesKilled++;
                     break;
                 case Ovni.ENNEMI_SUPERSONIQUE:
                     // Il ne s'agit pas d'un boss... Mais on donne des points!
                     Main.points += ENNEMI_SUPERSONIQUE_POINTS * (isOr ? 2 : 1) * Main.level;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, ENNEMI_SUPERSONIQUE_POINTS * (isOr ? 2 : 1) * Main.level));
                     break;
                 case Ovni.ENNEMI_BOSS_BONUS:
                     Main.points += ENNEMI_BOSS_BONUS_POINTS;
                     isBoss = false;
                     Main.terminerPartie("Vous avez tué le dernier boss, félicitations!");
                     break;
             }
         }
     }
 
     @Override
     public int getDommage() {
         return 0;
     }
     ////////////////////////////////////////////////////////////////////////////
 }
