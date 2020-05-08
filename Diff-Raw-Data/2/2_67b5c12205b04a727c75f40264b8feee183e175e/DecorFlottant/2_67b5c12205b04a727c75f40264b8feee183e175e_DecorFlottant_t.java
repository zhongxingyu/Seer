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
 package graphique.event;
 
 import java.awt.Graphics;
 import java.util.Random;
 
 import main.Main;
 
 import util.Dessinable;
 
 /**
  * Classe pour les objets Dessinable comme les nuages et les bancs de poissons.
  * @author Guillaume Poirier-Morency && Nafie Hamrani
  */
 public final class DecorFlottant extends Dessinable {
     ////////////////////////////////////////////////////////////////////////////
     // Variables propres aux décors flottants (nuages, poissons, sous-marins, etc...)
     /**
      * 
      */
     private static final int PROBABILITE_APPARITION_OBJET_FLOTTANT = 1000;
     ////////////////////////////////////////////////////////////////////////////
     // Varaibles locales
     /**
      * TODO Javadoc ici
      */
     /**
      * TODO Javadoc ici
      */
     private double positionX, positionY;
     ////////////////////////////////////////////////////////////////////////////
     
     /**
      * TODO Javadoc ici
      * @param x TODO Javadoc ici
      * @param y TODO Javadoc ici
      */
     private DecorFlottant(int x, int y) {
         image0 = Main.imageBank.decorFlottant;
         this.positionX = x;
         this.positionY = y;
     }
 
     /**
      * TODO Javadoc ici
      */
     public static void createNuage() {
         if ((new Random()).nextInt(PROBABILITE_APPARITION_OBJET_FLOTTANT) == 1) {
 
             Main.composantesDessinables.add(new DecorFlottant(0, 100));
         }
     }
 
     /**
      * TODO Javadoc ici
      */
     private void action() {
         if ((new Random()).nextInt(PowerUp.PROBABILITE_APPARITION_POWERUP) == 1) {
             Main.composantesDessinables.add(new PowerUp((int) positionX, (int) positionY));
         }
     }
 
     @Override
     public void dessiner(Graphics g) {
         action();
         if (positionX > Main.getCanvasSizeX()) {
             isDessinable = false;
         } else {
            g.drawImage(Main.imageBank.decorFlottant, (int) positionX, (int) positionY, null);
             positionX += 0.5;
         }
     }
 
     @Override
     public void dessinerDeboguage(Graphics g) {
         action();
         if (positionX > Main.getCanvasSizeX()) {
             isDessinable = false;
         } else {
             g.drawRect((int) (int) positionX, (int) positionY, 200, 100);
             g.drawString("Décor flottant", (int) positionX + 100, (int) positionY + 50);
             positionX += 0.5;
         }
     }
 }
