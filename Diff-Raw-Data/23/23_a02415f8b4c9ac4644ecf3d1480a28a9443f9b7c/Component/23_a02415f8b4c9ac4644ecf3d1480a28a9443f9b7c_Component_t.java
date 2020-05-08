 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package thirteenducks.cor.mainmenu.components;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import thirteenducks.cor.mainmenu.MainMenu;
 
 /**
  * Superklasse für Grafikkomponenten des Hauptmenüs, z.B. Buttons oder Labels
  * Diese Klasse verwaltet allgemeine Parameter wie Position
  *
  * @TODO: für die Prozentangeben werden int, double und float verwendet, da sollte ein standard her
  * @TODO: wird die MainMenu-Referenz eigentlich benötigt?
  *
  * @author michael
  */
 public class Component {
 
     /**
      * MainMenu-Referenz
      */
     private MainMenu mainMenu;
     /**
      * Der name dieser Komponente (optional)
      */
     private String name;
     /**
      * Die Koordinaten der linken oberen Ecke in Pixel
      */
     private int x1, y1;
     /**
      * Die Koordinaten der rechten unteren Ecke in Pixel
      */
     private int x2, y2;
     /**
      * gibt an, ob der Cursor über der Komponente schwebt
      */
     private boolean mouseHover;
     /**
      * Alpha-Wert der Komponente, steuert Transparenz
      */
     private float alpha;
 
     /**
      * Konstruktor
      *
      * Die Position und Maße werden in Prozent angegeben
      * Beispiel: new Component(m,20,10,50,50) --> Position 20%/10%,   Maße 50%,50%
      *
      *
      * @param m - MainMenu-Referenz
      * @param relX      relative X-Position in %
      * @param relY      relative Y-Position in %
      * @param relWidth  relative Breite in %
      * @param relHeigth relative Höhe in %
      */
     public Component(MainMenu m, double relX, double relY, double relWidth, double relHeigth) {
        if (m != null) { // Pfusch-Workaround, das MainMenu selber ist auch ein Container
            mainMenu = m;
 
            x1 = (int) (0.01 * relX * m.getWidth());
            y1 = (int) (0.01 * relY * m.getHeight());
 
            x2 = x1 + (int) (0.01 * relWidth * m.getWidth());
            y2 = y1 + (int) (0.01 * relHeigth * m.getHeight());
 
        }
         mouseHover = false;
     }
 
     /**
      * Init-Funktion
      * Wird beim Initialisieren der Grafik gerufen
      * Hier können Ressourcen für die Grafik geladen werden, z.B. Bilder
      * @param c     Container der Grafik
      */
     public void init(GameContainer c) {
     }
 
     /**
      * Renderfunktion
      * wird regelmäßig aufgerufen um die Komponente zu zeichnen
      *
      * @param g - Graphics-Objekt, auf dem die Zeichen-Funktionen aufgerufen werden
      */
     public void render(Graphics g) {
     }
 
     /**
      * Wird aufgerufen, wenn der Benutzer etwas anklickt
      * @param button
      * @param x
      * @param y
      * @param clickCount
      */
     public void mouseClickedAnywhere(int button, int x, int y, int clickCount) {
 
         if (getX1() < x && x < getX2() && getY1() < y && y < getY2()) {
             mouseClicked(button, x, y, clickCount);
         }
 
     }
 
     /**
      * Wird aufgerufen, wenn diese Komponente angeklickt wurde
      * @param button
      * @param x
      * @param y
      * @param clickCount
      */
     public void mouseClicked(int button, int x, int y, int clickCount) {
     }
 
     /**
      * Wird bei Mausbewegungen aufgerufen
      * @param x     - die X-Position der Maus
      * @param y     - die Y-Position der Maus
      */
     public void mouseMoved(int x, int y) {
         if (getX1() < x && x < getX2() && getY1() < y && y < getY2()) {
             mouseHoverChanged(true);
         } else {
             mouseHoverChanged(false);
         }
     }
 
     /**
      * Wird gerufen, wenn die Maus anfängt oder aufhört über der Komponente zu hovern
      * @param newstate
      */
     public void mouseHoverChanged(boolean newstate) {
     }
 
     /**
      * Wird bei Tastendruck aufgerufen
      * @param key   der Code der gedrückten Taste
      * @param c     der entsprechende char
      */
     public void keyPressed(int key, char c) {
     }
 
     /**
      * Gibt an, ob die Maus über diesem Objekt shcwebt
      * @return      true, wenn die Maus über diesem Objekt schwebt, sonst false
      */
     public boolean isMouseHover() {
         return mouseHover;
     }
 
     /**
      * @return the x1
      */
     public int getX1() {
         return x1;
     }
 
     /**
      * @return the y1
      */
     public int getY1() {
         return y1;
     }
 
     /**
      * @return the x2
      */
     public int getX2() {
         return x2;
     }
 
     /**
      * @return the y2
      */
     public int getY2() {
         return y2;
     }
 
     /**
      * Gibt die Breite in Pixel zurück
      * @return      die Breite in Pixel
      */
     public int getWidth() {
         return (int) (x2 - x1);
     }
 
     /**
      * Gibt die Höhe in Pixel zurück
      * @return      die Höhe in Pixel
      */
     public int getHeight() {
         return (int) (y2 - y1);
     }
 
     /**
      * Getter für MainMenu
      * @return the mainMenu
      */
     public MainMenu getMainMenu() {
         return mainMenu;
     }
 
     /**
      * Setzt den Alpha-Wert
      * @param alpha     der neue Alpha-Wert
      */
     public void setAlpha(float alpha) {
         this.alpha = alpha;
     }
 
     /**
      * getter für Alpha
      * @return - Der Alpha-Wert dieser Komponente
      */
     public double getAlpha() {
         return this.alpha;
     }
 
     /**
      * Setter für X2
      * @param - X-Koordinate der rechten unteren Ecke
      */
     public void setX2(int x2) {
         this.x2 = x2;
     }
 
     /**
      * Setter für Y2
      * @param - Y-Koordinate der rechten unteren Ecke
      */
     public void setY2(int y2) {
         this.y2 = y2;
     }
 
     /**
      * Getter für name
      * @return - der Name der Komponente
      */
     public String getName() {
         return name;
     }
 
     /**
      * Setter für den Namen der Komponente
      * @param name - der Name den die Komponente haben soll
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Setter für MainMenu
      * @param m - MainMenu-Referenz
      */
     void setMainMenu(MainMenu m) {
         this.mainMenu = m;
     }
 }
