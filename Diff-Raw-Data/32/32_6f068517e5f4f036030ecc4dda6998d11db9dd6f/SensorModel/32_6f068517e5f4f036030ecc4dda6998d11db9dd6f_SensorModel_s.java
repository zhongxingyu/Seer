 package org.wattdepot.visualization.data.sensor;
 
 import org.wattdepot.visualization.data.AbstractObject;
 
 /**
  * Defines the sensor data.
  * 
  * @author Bret K. Ikehara
  */
 public class SensorModel extends AbstractObject {
 
   /**
    * Serial ID.
    */
   private static final long serialVersionUID = 6632789215916215668L;
   private int radius;
   private int[] color;
   private boolean online, pulse;
 
   /**
    * Default constructor.
    */
   public SensorModel() {
     this.radius = 0;
     this.color = new int[] { 90, 255, 255, 255 };
     this.online = true;
     this.pulse = false;
   }
 
   /**
    * Gets this radius.
    * 
    * @return int
    */
   public int getRadius() {
     return this.radius;
   }
 
   /**
    * Gets this color.
    * 
    * @return int[]
    */
   public int[] getColor() {
     int[] c = new int[3];
     c[0] = this.color[0];
     c[1] = this.color[1];
     c[2] = this.color[2];
     return c;
   }
 
   /**
    * Sets this color.
    * 
    * @param hue int
    * @param saturation int
    * @param brightness int
    */
   public void setColor(int hue, int saturation, int brightness) {
     if (this.color[0] >= 0 && this.color[0] <= 90) {
       this.color[0] = hue;
       this.color[1] = saturation;
       this.color[2] = brightness;
       this.radius = (90 - this.color[0]) / 10 + 10;
     }
   }
 
   /**
    * Sets this color.
    * 
    * @param color int[]
    */
   public void setColor(int[] color) {
     this.setColor(color[0], color[1], color[2]);
   }
 
   /**
    * Is this online.
    * 
    * @return boolean
    */
   public boolean isOnline() {
     return online;
   }
 
   /**
    * Set this online.
    * 
    * @param online boolean
    */
   public void setOnline(boolean online) {
     this.online = online;
   }
 
   /**
    * Is this pulsing.
    * 
    * @return boolean
    */
   public boolean isPulse() {
     return pulse;
   }
 
   /**
    * Sets this pulse.
    * 
    * @param pulse boolean
    */
   public void setPulse(boolean pulse) {
     this.pulse = pulse;
     // set the pulse but
     if (this.pulse) {
       this.online = true;
     }
   }
 }
