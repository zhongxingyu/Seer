 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lasersched;
 
 /**
  *
  * @author Dave
  */
 import java.awt.*;
 import java.awt.print.*;
 import javax.swing.*;
 
 public class PrintUtilities implements Printable {
     
   class Dim {
     private double width;
     private double height;
     public Dim(double x, double y) { // Constructor
         width=x;
         height=y;
     }
         // Methods...
     }
     
   private Component componentToBePrinted;
 
   public static void printComponent(Component c) {
     new PrintUtilities(c).print();
   }
   
   public PrintUtilities(Component componentToBePrinted) {
     this.componentToBePrinted = componentToBePrinted;
   }
   
   public void print() {
     PrinterJob printJob = PrinterJob.getPrinterJob();
     printJob.setPrintable(this);
     if (printJob.printDialog())
       try {
         printJob.print();
       } catch(PrinterException pe) {
         System.out.println("Error printing: " + pe);
       }
   }
 
   public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
     if (pageIndex > 0) {
       return(NO_SUCH_PAGE);
     } else {
       Graphics2D g2d = (Graphics2D)g;
       g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
      disableDoubleBuffering(componentToBePrinted);
      Dim pageSize = new Dim((int)pageFormat.getImageableWidth(),(int)pageFormat.getImageableHeight());
       Dimension compSize = componentToBePrinted.getSize();
       double widthRatio = pageSize.width/compSize.width;
       double heightRatio = pageSize.height/compSize.height;
       double scale = Math.min(widthRatio, heightRatio);
      g2d.scale(scale, scale);
       componentToBePrinted.paint(g2d);
       enableDoubleBuffering(componentToBePrinted);
       return(PAGE_EXISTS);
     }
   }
 
   public static void disableDoubleBuffering(Component c) {
     RepaintManager currentManager = RepaintManager.currentManager(c);
     currentManager.setDoubleBufferingEnabled(false);
   }
 
   public static void enableDoubleBuffering(Component c) {
     RepaintManager currentManager = RepaintManager.currentManager(c);
     currentManager.setDoubleBufferingEnabled(true);
   }
 }
