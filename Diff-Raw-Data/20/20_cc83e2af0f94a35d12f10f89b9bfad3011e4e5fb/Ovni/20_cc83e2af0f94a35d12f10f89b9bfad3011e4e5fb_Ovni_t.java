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
     private static final int PROBABILITE_APPARITION_OVNI = 1000;
     /**
      * Détermine si un boss est présent dans le jeu.
      */
     private static boolean isBoss = false;
     /**
      * ID de l'ennemi normal.
      */
     private static final int ENNEMI_NORMAL = 1;
     /**
      * ID de l'ennemi supersonique.
      */
     private static final int ENNEMI_SUPERSONIQUE = 2;
     /**
      * ID du boss 1.
      */
     private static final int ENNEMI_BOSS_1 = 3;
     /**
      * ID du boss 2.
      */
     private static final int ENNEMI_BOSS_2 = 4;
     /**
      * ID du boss 3.
      */
     private static final int ENNEMI_BOSS_3 = 5;
     /**
      * 
      */
     private static int ENNEMY_SHOOT_RATE = 500;
     /**
      * Random utilisé pour les génération aléatoires de l'ovni.
      */
     private static Random random = new Random();
     private static final int ENNEMI_BOSS_1_POINTS = 200,
             ENNEMI_BOSS_2_POINTS = 300,
             ENNEMI_BOSS_3_POINTS = 400,
            ENNEMI_NORMAL_POINTS = 30,
            ENNEMI_SUPERSONIQUE_POINTS = 100;
     ////////////////////////////////////////////////////////////////////////////
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
 
     /**
      * 
      * @param x
      * @param y
      * @param id 
      */
     private Ovni(int x, int y, int id) {
 
         /* On va pas faire ça compliqué. Il y a une méthode en dessous qui
          * s'appelle create ovni vas-y! 
          */
 
         if (isBoss) {
             isDessinable = false;
         }
         // Détermine si l'ennemi est or
         isOr = random.nextInt(10) == 1 ? true : false;
         this.ID = id;
         position.x = x;
         position.y = y;
         configurerOvni(id);
         VIE_INIT = vie;
     }
 
     /**
      * 
      */
     public static void createOvni() {
         // Ici tu mets l'algo de génération aléatoire pour le id
 
         int i = initializeID();
         int y = new Random().nextInt(250);
         switch (i) {
             case ENNEMI_BOSS_1:
                 Main.composantesDessinables.add(new Ovni(0, y, i));
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
             case 0:
                 return;
         }
 
 
 
 
         /* Bon je te laisse coder l'algorithme de génération en fonction de tes
          * besoins. Le constructeur peut être private maintenant!
          * Comme ça on ne surcharge pas le main avec du code et...
          * TU TE CHARGE DES MISSILES ENNEMIS MUHAHA!
          * C'est pour ça que je te fais une méthode tirer()...
          * 
          */
     }
 
     /**
      * Initialise le ID de l'ovni.
      * @return le id de l'ovni.
      */
     private static int initializeID() {
         int generateur = new Random().nextInt(PROBABILITE_APPARITION_OVNI);
         if (!isBoss) {
             if (Main.timerSeconds >= 360000) {
                 isBoss = true;
                 return ENNEMI_BOSS_3;
             } else if (Main.timerSeconds >= 240000) {
                 isBoss = true;
                 return ENNEMI_BOSS_2;
             } else if (Main.timerSeconds >= 120000) {
                 isBoss = true;
                 return ENNEMI_BOSS_1;
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
             // TODO Condition pour ENNEMI_OR ou non.
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
                 Main.composantesDessinables.add(new ProjectileEnnemi(position, ProjectileEnnemi.PROJECTILE_BOSS));
                 break;
         }
     }
 
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
             default:
 
                 System.out.println("Veuillez entre une identification valide (id) dans le constructuer de l'objet" + id);
         }
     }
 
     /**
      * 
      */
     private void action() {
         switch (ID) {
             case ENNEMI_NORMAL:// mouvement d'un enemi normal 
                 position.x += 3 * vitesseX;
                 break;
             case ENNEMI_SUPERSONIQUE:// mouvement du ennemiSupersonic
                 position.x -= 3 * vitesseX;
                 break;
             case ENNEMI_BOSS_1:// mouvement du boss 1
                 mouvtBoss12(15, 300);
                 break;
             case ENNEMI_BOSS_2:// mouvement du boss 2
                 vitesseX = 3;
                 mouvtBoss12(15, 745);
                 break;
             case ENNEMI_BOSS_3:// mouvement du boss 3
                 break;
             default:
                 System.out.println("Veuillez entre une identification valide (id) dans le constructuer de l'objet");
 
         }
         if ((new Random()).nextInt(ENNEMY_SHOOT_RATE) == 1) {
             tirer();
         }
     }
 
     private void mouvtBoss12(int ymin, int ymax) {
         final int XMIN = 1;
         final int XMAX = (int) Main.getCanvasSizeX() - 1;
         final int YMIN = ymin;
         final int YMAX = ymax;
         int deplacementX = 3;
         int deplacementY = 3;
 
         if (position.x == XMIN) {
             deplacementX = 3;
         } else if (position.x == XMAX) {
             deplacementX = -3;
         }
         if (position.y == YMIN) {
             deplacementX = 3;
         } else if (position.y == YMAX) {
             deplacementY = -3;
         }
         position.x += deplacementX * vitesseX;
         position.y += deplacementY * vitesseX;
     }
 
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
                 hauteur = 300;
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
            
             // Mort des boss
             switch (ID) {
                 case Ovni.ENNEMI_BOSS_1:
                     Main.points += ENNEMI_BOSS_1_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, "" + ENNEMI_BOSS_1_POINTS));
                     Main.setGameLevel(Main.LEVEL_2);
                     isBoss = false;
                     break;
                 case Ovni.ENNEMI_BOSS_2:
                     Main.points += ENNEMI_BOSS_2_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, "" + ENNEMI_BOSS_2_POINTS));
                     Main.setGameLevel(Main.LEVEL_3);
                     isBoss = false;
                     break;
                 case Ovni.ENNEMI_BOSS_3:
                     Main.points += ENNEMI_BOSS_3_POINTS;
                     Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, "" + ENNEMI_BOSS_3_POINTS));
                     Main.setGameLevel(Main.LEVEL_BONUS);
                     isBoss = false;
                     break;
                 case Ovni.ENNEMI_NORMAL:
                     // Il ne s'agit pas d'un boss... Mais on donne des points!
                    Main.points += ENNEMI_NORMAL_POINTS * (isOr?2:1);
                    Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, "" + ENNEMI_NORMAL_POINTS* (isOr?2:1)));
                     break;
                 case Ovni.ENNEMI_SUPERSONIQUE:
                     // Il ne s'agit pas d'un boss... Mais on donne des points!
                    Main.points += ENNEMI_SUPERSONIQUE_POINTS* (isOr?2:1);
                    Main.composantesDessinables.add(new PointsObtenus((int) position.x, (int) position.y, "" + ENNEMI_SUPERSONIQUE_POINTS* (isOr?2:1)));
                     break;
             }
         }
     }
 
     @Override
     public int getDommage() {
         return 0;
     }
 }
