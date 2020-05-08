 package org.injustice.rawchicken.util;
 
import org.injustice.rawchicken.RawChickenPicker;
 import org.powerbot.game.api.methods.input.Mouse;
 
 import java.awt.*;
 
 import static org.injustice.rawchicken.util.Methods.format;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Azmat
  * Date: 29/03/13
  * Time: 20:35
  * To change this template use File | Settings | File Templates.
  */
 public class Paint {
     public static void onRepaint(Graphics g1) {
         Graphics2D g = (Graphics2D) g1;
         Point loc = new Point(10, 220);
         Color green = new Color(20, 126, 59, 150);
         if (!Var.hidePaint) {
             int chickensHour = (int) (Var.chickensPicked * 3600000d / Var.runTime.getElapsed());
             int cashMade = Var.chickenPrice * Var.chickensPicked;
             int cashHour = (int) (cashMade * 3600000d / Var.runTime.getElapsed());
 
             BasicStroke stroke1 = new BasicStroke(1);
             g.setStroke(stroke1);
             Font font1 = new Font("Tahoma", 0, 15);
             Font font2 = new Font("Tahoma", 0, 9);
             Font font3 = new Font("Tahoma", 0, 11);
             Color grey = new Color(0, 0, 0, 100);
 
             g.setColor(grey);
             g.fillRoundRect(5, 178, 190, 120, 7, 7);
             g.setColor(Color.GREEN);
             g.setStroke(stroke1);
             g.drawRoundRect(5, 178, 190, 120, 7, 7);
             g.drawRect(5, 178, 190, 21);
             g.setFont(font1);
 
             g.drawString("Injustice's Chicken Picker", 8, 196);
             g.setFont(font2);
            g.drawString("v" + RawChickenPicker.VERSION, 173, 196);
             g.setFont(font3);
            g.drawString("Chicken Picked: " + format(Var.chickensPicked, 2), loc.x, loc.y);
            g.drawString("Chicken PH: " + format(chickensHour, 2), loc.x, loc.y + 15);
            g.drawString("Cash Made: " + format(cashMade, 2), loc.x, loc.y + 30);
            g.drawString("Cash PH: " + format(cashHour, 2), loc.x, loc.y + 45);
            g.drawString("Runtime: " + Var.runTime.toElapsedString(), loc.x, loc.y + 60);
            g.drawString("Status: " + Var.status, loc.x, loc.y + 75);
             g.drawString("Click to hide", loc.x + 123, loc.y + 75);
             g.drawString("Show tiles", loc.x + 133, loc.y + 45);
             g.drawString("Show mouse", loc.x + 122, loc.y + 60);
             g.drawString("Take screenshot", loc.x + 104, loc.y);
             g.setColor(green);
             g.drawRoundRect(132, 285, 59, 11, 3, 3);   // hide paint
             g.drawRoundRect(131, 270, 61, 11, 3, 3);   // show mouse
             g.drawRoundRect(140, 255, 52, 11, 3, 3);   // show tiles
             g.drawRoundRect(112, 210, 81, 11, 3, 3);   // take screenshot
             if (Var.showMouse) drawMouse(g);
             if (Var.showTiles) Methods.drawTiles(g, Var.CHICKEN_AREA, Var.RAW_CHICKEN_ID, Color.GREEN, Color.BLUE, Color.BLACK);
         } else {
             Font font3 = new Font("Tahoma", 0, 11);
             g.setFont(font3);
             g.setColor(Color.GREEN);
             g.drawString("Show paint", loc.x + 126, loc.y + 75);
             g.setColor(green);
             g.drawRoundRect(132, 285, 59, 11, 3, 3);  // show paint
         }
     }
 
     /**
      * @param g1 Graphics
      * @author not me, will find - phl0w/pxpc2, not sure
      */
     private static void drawMouse(Graphics g1) {
         Point p = Mouse.getLocation();
         Color[] gradient = new Color[]{new Color(255, 0, 0),
                 new Color(255, 0, 255), new Color(0, 0, 255),
                 new Color(0, 255, 255), new Color(0, 255, 0),
                 new Color(255, 255, 0), new Color(255, 0, 0)};
         Color outerCircle = gradient[0];
         g1.setColor(gradient[0]);
         int circleRadius = 7;
         int circleDiameter = circleRadius * 2;
         g1.drawLine(p.x + circleRadius, p.y, p.x + 2000, p.y);
         g1.drawLine(p.x - 2000, p.y, p.x - circleRadius, p.y);
 // Vertical
         g1.drawLine(p.x, p.y + circleRadius, p.x, p.y + 2000);
         g1.drawLine(p.x, p.y - 2000, p.x, p.y - circleRadius);
         for (int r = gradient.length - 1; r > 0; r--) {
             int steps = 200 / ((gradient.length - 1) * 2);
             for (int i = steps; i > 0; i--) {
                 float ratio = (float) i / (float) steps;
                 int red = (int) (gradient[r].getRed() * ratio + gradient[r - 1]
                         .getRed() * (1 - ratio));
                 int green = (int) (gradient[r].getGreen() * ratio + gradient[r - 1]
                         .getGreen() * (1 - ratio));
                 int blue = (int) (gradient[r].getBlue() * ratio + gradient[r - 1]
                         .getBlue() * (1 - ratio));
                 Color stepColor = new Color(red, green, blue);
                 g1.setColor(stepColor);
 // Horizontal
                 g1.drawLine(p.x + circleRadius, p.y, p.x
                         + ((i * 5) + (100 * r)), p.y);
                 g1.drawLine(p.x - ((i * 5) + (100 * r)), p.y, p.x
                         - circleRadius, p.y);
 // Vertical
                 g1.drawLine(p.x, p.y + circleRadius, p.x, p.y
                         + ((i * 5) + (100 * r)));
                 g1.drawLine(p.x, p.y - ((i * 5) + (100 * r)), p.x, p.y
                         - circleRadius);
             }
         }
         g1.setColor(outerCircle);
         final long mpt = System.currentTimeMillis() - Mouse.getPressTime();
         if (Mouse.getPressTime() == -1 || mpt >= 200) {
             g1.drawOval(p.x - circleRadius / 3, p.y - circleRadius / 3,
                     circleDiameter / 3, circleDiameter / 3);
         }
         if (mpt < 200) {
             g1.drawLine(p.x - circleRadius, p.y + circleRadius, p.x
                     + circleRadius, p.y - circleRadius);
             g1.drawLine(p.x - circleRadius, p.y - circleRadius, p.x
                     + circleRadius, p.y + circleRadius);
         }
         g1.setColor(outerCircle);
         g1.drawOval(p.x - circleRadius, p.y - circleRadius, circleDiameter,
                 circleDiameter);
     }
 }
