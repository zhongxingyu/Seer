 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package team1073.smartdashboard.extension.speeddisplay;
 
 import edu.wpi.first.smartdashboard.gui.StaticWidget;
 import edu.wpi.first.smartdashboard.gui.Widget;
 import edu.wpi.first.smartdashboard.properties.Property;
 import edu.wpi.first.smartdashboard.properties.DoubleProperty;
 import edu.wpi.first.smartdashboard.types.DataType;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 /**
  *
  * @author Kate
  */
 public class speedDisplay extends StaticWidget {
 public static final DataType[] TYPES = { DataType.NUMBER };
 
 private double actualValue = 0.0;
 public final DoubleProperty newVal = new DoubleProperty(this, "Calc Val", 0.0);
 
    // @Override
        public void setValue(Object value){
         this.actualValue = ((Number)value).doubleValue();
         repaint();
     }
 
     @Override
     public void init() {
         setPreferredSize(new Dimension(64, 200));
     }
     
     @Override
     public void propertyChanged(Property prprt) {
         setValue(prprt.getValue());
     }
     
     @Override
     protected void paintComponent(Graphics g){
     Dimension size  = getSize(); 
     
     g.setColor(Color.BLACK);
     g.fillRect(0, 0, 64, 200);
     
     int barHeight = (int)(size.height * actualValue/100.0);
     g.setColor(Color.GREEN);
     g.fillRect(0, 200 - barHeight, 64, barHeight);
     g.setColor(Color.BLACK);
     g.drawRect(0, 0, 64 - 1, 200 - 1);
     
    g.drawString("Speed: " + Integer.toString(Math.abs((int)actualValue)), 0, 21);
     }
    
 }
