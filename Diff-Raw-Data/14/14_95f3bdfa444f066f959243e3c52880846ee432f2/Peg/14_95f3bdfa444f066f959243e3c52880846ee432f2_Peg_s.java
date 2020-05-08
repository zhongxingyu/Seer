 package SierpinskiTriangle;
 
 import SVGraphics.*;
 import SVGraphics.SVRectangle;
 import static SierpinskiTriangle.SierpinskiTriangle.colors;
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 
 /**
  * Peg class extends SVCustom is used by Tower class.
  *
  * @author Martin Wolfenbarger
  * @version 2013/05/16 
  */
 public class Peg extends SVCustom{
     public Peg(ArrayList<Integer> a, int total) {
         super();
         SVRectangle r = new SVRectangle(new Point(-3,20),6,60);
         r.setBorderRadius(3);
         r.setColor(Color.black);
         addContent(r, "a");
         if(total > 0) this.addDisks(a, total);
     }
     
     /** Draws the disks on this peg
         @param a arraylist of integers
         @param total total disks on all three pegs
      */
     private void addDisks(ArrayList<Integer> a, int total) {
         for(int i = 0; a.size() > i; i++) {
            SVEllipse e = new SVEllipse(new Point(0,-1*i*13+68),30-i*3,14);
            SVEllipse esmall = new SVEllipse(new Point(0,-1*i*13+68),(26-i*3)/2,13);
            SVText t = new SVText(new Point(0,-1*i*13+73),""+a.get(i));
             esmall.setColor(Color.white);
            e.setColor(this.getCurrentColor(a.get(i)));
             esmall.setBorderWidth(1);
             esmall.setBorderColor(Color.lightGray);
             addContent(e, "e"+i);
             addContent(esmall, "esmall"+i);
             addContent(t,"t"+i);
         }
     }
     
     private Color getCurrentColor(int i) {
         try {
             return SierpinskiTriangle.colors.get(i);
         } catch (IndexOutOfBoundsException iobe) {
             return Color.BLACK;
         }
     }
 }
