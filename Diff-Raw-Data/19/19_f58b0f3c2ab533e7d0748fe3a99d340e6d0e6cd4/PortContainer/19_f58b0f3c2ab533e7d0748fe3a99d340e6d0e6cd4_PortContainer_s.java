 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package celestial.Ship;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.io.Serializable;
 
 public class PortContainer implements Serializable {
     /*
      * Represents a docking port in a way that allows the port to be aware of
      * the size of the ship, and allows a ship to be aware of 
      */
 
     private double x;
     private double y;
     private int width;
     private int height;
     private double ax;
     private double ay;
     protected Station parent;
     private Ship client;
     //timing
    private double maxHold = 360;
     private double time = 0;
 
     public PortContainer(Station parent, double x, double y, int w, int h, double ax, double ay) {
         this.x = x;
         this.y = y;
         this.width = w;
         this.height = h;
         this.ax = ax;
         this.ay = ay;
         this.parent = parent;
     }
 
     public void periodicUpdate(double tpf) {
         if (client != null) {
             if (client.getState() == Ship.State.ALIVE) {
                 if (!client.isDocked()) {
                     //iterate timer
                     time += tpf;
                     //check for grace period
                     if (maxHold > time) {
                         //it might still be trying to dock
                         if (client.collideWith(getBound())) {
                             client.setDocked(true);
                         }
                     } else {
                         client.cmdAbortDock();
                         client = null;
                         time = 0;
                     }
                 } else {
                     client.setX(getPortX() - width / 2);
                     client.setY(getPortY() - height / 2);
                     client.setVx(0);
                     client.setVy(0);
                 }
             } else {
                 //client died somehow
                 client = null;
             }
         } else {
             //nobody is docked
         }
     }
 
     public Rectangle getBound() {
         return new Rectangle((int) getPortX() - width / 2, (int) getPortY() - height / 2, width, height);
     }
 
     public void render(Graphics f) {
         if (client == null) {
             f.setColor(Color.GRAY);
             f.drawOval((int) x, (int) y, width, height);
         } else if (time < maxHold && !client.isDocked()) {
             f.setColor(Color.YELLOW);
             f.drawOval((int) x, (int) y, width, height);
         }
     }
 
     public boolean canFit(Ship ship) {
         if (ship.getWidth() / 2 <= width && ship.getHeight() / 2 <= height) {
             return true;
         } else {
             return false;
         }
     }
 
     public double getPortX() {
         return x + getParent().getX() + height / 2;
     }
 
     public double getPortY() {
         return y + getParent().getY() + width / 2;
     }
 
     public double getAlignX() {
         return getParent().getX() + ax;
     }
 
     public double getAlignY() {
         return getParent().getY() + ay;
     }
 
     public Ship getClient() {
         return client;
     }
 
     public void setClient(Ship docked) {
         this.client = docked;
     }
 
     public boolean isAvailable(Ship test) {
         if (client != null) {
             if (client == test) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return true;
         }
     }
 
     public Station getParent() {
         return parent;
     }
 
     public void setParent(Station parent) {
         this.parent = parent;
     }
 
     public void kickOut() {
         if(client != null) {
             client.cmdUndock();
         }
     }
 }
