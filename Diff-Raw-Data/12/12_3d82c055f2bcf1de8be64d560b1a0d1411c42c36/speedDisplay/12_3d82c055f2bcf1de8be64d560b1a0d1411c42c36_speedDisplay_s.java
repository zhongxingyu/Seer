 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package team1073.smartdashboard.extension.guiwidgets;
 
 import edu.wpi.first.smartdashboard.gui.StaticWidget;
 import edu.wpi.first.smartdashboard.gui.Widget;
 import edu.wpi.first.smartdashboard.properties.Property;
 import edu.wpi.first.smartdashboard.properties.DoubleProperty;
 import edu.wpi.first.smartdashboard.types.DataType;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
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
     protected void paintComponent(Graphics graphics){
     Graphics2D g = (Graphics2D) graphics;
     Dimension size  = getSize(); 
     
     g.setColor(Color.BLACK);
     g.fillRect(50, 50, 64, 200);
     
    int barHeight = (int)(size.height * actualValue/100.0);
     g.setColor(Color.GREEN);
     g.fillRect(50, 250 - barHeight, 64, barHeight);
     g.setColor(Color.BLACK);
     g.drawRect(50, 50, 64 - 1, 200 - 1);
     
     Font font = new Font("Arial", Font.BOLD, 16);
     graphics.setFont(font);
     g.drawString("Speed: " + Integer.toString(Math.abs((int)actualValue)), 47, 272);
     }
    
 }
