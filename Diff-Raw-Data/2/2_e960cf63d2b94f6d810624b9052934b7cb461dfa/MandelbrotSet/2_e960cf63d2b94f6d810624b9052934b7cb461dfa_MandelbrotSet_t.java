 package basicj.alpha1_3;
 
 import basicj.BasicJ;
 
 public class MandelbrotSet extends BasicJ {
 
     public MandelbrotSet() {
         screen(676, 598);
         autoFlush(0);
         double xmax = 0.5;
         double xmin = -2.1;
         double ymax = 1.15;
         double ymin = -1.15;
         double dxw = (xmax - xmin)/width();
         double dyh = (ymax - ymin)/height();
         
         double ix = xmin;
         
         for(int x = 0; x < width(); x++) {
             double iy = ymin;
             for(int y = 0; y < height(); y++) {
                 double x1 = 0;
                 double y1 = 0;
                 int color = 0;
                 while(color < 50 && sqrt((x1*x1) + (y1*y1)) < 2) {
                     color++;
                     double temp = (x1*x1) - (y1*y1) + ix;
                     y1 = 2*x1*y1 + iy;
                     x1 = temp;
                 }
                 color = rint((double) 511*color/50);
                 if(color > 255) {
                     if(color == 511) {
                         color(black);
                     } else {
                         color(color - 255, color - 255, 255);
                     }
                 } else {
                     color(0, 0, color);
                 }
                 point(x, y);
                 iy += dyh;
             }
             ix += dxw;
             if(x % (width()/10) == 0)
                 System.out.println("Done..." + rint((float) 100*x/(width() - 1)) + "%");
         }
         flush();
        save("test/basicj/alpha1_3/Mandelbrot Set.bmp");
     }
     
     public static void main(String[] args) {
         new MandelbrotSet();
     }
 
 }
