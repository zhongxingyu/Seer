 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package team1073.smartdashboard.extension.angledisplay;
 
 import edu.wpi.first.smartdashboard.gui.StaticWidget;
 import edu.wpi.first.smartdashboard.gui.Widget;
 import edu.wpi.first.smartdashboard.properties.IntegerProperty;
 import edu.wpi.first.smartdashboard.properties.Property;
 import edu.wpi.first.smartdashboard.types.DataType;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 /**
  *
  * @author Kate
  */
 public class AngleDisplay extends Widget{
     
     
     //angles to display in degrees and radians
     private int angle;
     private double angleRad;
     
     //coordinates of the endpoint of the line to angle
     private int x2;
     private int y2;
     
     public final IntegerProperty newVal = new IntegerProperty(this, "Angle", 0);
     public static final DataType[] TYPES = {DataType.NUMBER};
     
     public AngleDisplay() {angle = 0; angleRad = 0;}
     
     @Override
     public void init() {
        angle = 0;
        angleRad = 0.0;
        
        x2 = 150;
        y2 = 125;
        
        setPreferredSize(new Dimension(200,80));
     }
      
     @Override
     protected void paintComponent (Graphics graphics) {
         Graphics2D g = (Graphics2D) graphics;
         //we only want this to display angles between 0 and 90 degrees
         if(angle <= 75 && angle >= 0) {
             x2 = 75 + (int)(75*Math.cos(angleRad));
             y2 = 125 - (int)(75*(Math.sin(angleRad)));
             g.setStroke(new BasicStroke (2.0f));
             graphics.setColor(Color.GREEN);
             graphics.fillArc(0, 50, 150, 150, 0, angle);
             graphics.setColor(Color.BLACK);
             graphics.drawArc(0, 50, 150, 150, 0, angle);
             graphics.drawLine(75, 125, 150, 125); //baseline from center to 0 rad
             graphics.drawLine(75,125, x2, y2);//line from center to set angle
         }
         else {
             graphics.setColor(Color.RED);
             graphics.fillArc(0,50,150,150,0,90);
             Font font = new Font("Arial", Font.BOLD, 14);
             graphics.setFont(font);
             graphics.drawString("INVALID ANGLE", 75, 160);
         }
         Font font = new Font("Arial", Font.BOLD, 14);
         graphics.setFont(font);
         graphics.drawString("Angle: " + Integer.toString(angle), 75, 140);
     }
     
     public void setValue(Object o) {
        int d = angle;
        
       try{d = ((Number)o).intValue();}
       catch(Exception e){
       System.err.println("AngleDisplay setValue exception "+ e);
       }
        
        angle = d;
        angleRad = (Math.PI * angle)/180;
        
        repaint();
     }
     
     @Override
     public void propertyChanged(Property prprt) {
         setValue(prprt.getValue());
     }  
 }
