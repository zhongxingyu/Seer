 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.core.enums;
 
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * <p>
  * Colored flags (use in statistics).
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public enum ColoredFlags {
     
     RED("red",5, 255, 0, 0, java.awt.Color.red.getRGB()),
     YELLOW("yellow",0, 255, 255, 0, java.awt.Color.yellow.getRGB()),
     NONE("none",-1, -1,0,0,-1);
     private String id;
    private int red;
    private int green;
    private int blue;
     private int order;
     private int rgb;
     
     /**
      * Constructor.
      * @param id
      * @param color
      */
     private ColoredFlags(String id, int order, int red, int green, int blue, int rgb) {
        this.id = id;        
         this.order = order;
        this.red = red;
        this.green = green;
        this.blue = blue;
         this.rgb = rgb;
     }
     
     /**
      * @return Returns the color.
      */
    public Color getColor(Display display) {
        if(display!=null&&red>=0){
            return new Color(display, red,green,blue);
        }
        return null;
     }
     
     /**
      * @return Returns the id.
      */
     public String getId() {
         return id;
     }
     
     /**
      * @return Returns the order.
      */
     public int getOrder() {
         return order;
     }
     
     /**
      * @return Returns the rgb.
      */
     public int getRgb() {
         return rgb;
     }
     
     /**
      * Returns flag by id.
      *
      * @param id String
      * @return StatisticsFlags
      */
     public static ColoredFlags getFlagById(String id){
         for(ColoredFlags flag : values()){
             if(flag.id.equals(id)){
                 return flag;
             }
         }
         return null;
     }
 
 }
