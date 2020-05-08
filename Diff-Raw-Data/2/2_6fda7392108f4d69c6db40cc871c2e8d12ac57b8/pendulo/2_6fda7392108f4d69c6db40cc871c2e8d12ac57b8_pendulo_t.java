 import processing.core.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 import java.util.HashMap; 
 import java.util.ArrayList; 
 import java.io.File; 
 import java.io.BufferedReader; 
 import java.io.PrintWriter; 
 import java.io.InputStream; 
 import java.io.OutputStream; 
 import java.io.IOException; 
 
 public class pendulo extends PApplet {
 
 Pendulo p;
 
 int tamanoGrafica = 500;
 int colorFondo    = 200;
 int colorGrafica  = 100;
 
 int m = 100;
 int f = 30;
 
 float g   = 9.81f;
 float tau = TWO_PI;
 
 public void setup(){
   frameRate(f);
 	size(tamanoGrafica, tamanoGrafica);
 	hint(ENABLE_STROKE_PURE);
   stroke(colorGrafica);
   fill(colorGrafica);
 
   p = new Pendulo(tamanoGrafica/2, tamanoGrafica/2);
 }
 
 public void draw(){
   background(colorFondo);
   p.animar();
 }
 
 class Pendulo{
   PVector coorOrigen;
   PVector coorMasa;
   float r;
   float th;
   float om;
   float al;
 
   Pendulo(int origenX, int origenY) {
     coorOrigen = new PVector(origenX, origenY);
     r = 1*m;
     th = tau/8;
     om = 0;
     al = 0;
    coorMasa = new PVector(origenX + r*sin(th), origenY + r*cos(th));
   }
 
   public void dibujar(){
     line(coorOrigen.x, coorOrigen.y, coorMasa.x, coorMasa.y);
     ellipse(coorMasa.x, coorMasa.y, 20, 20);
   }
 
   public void actualizar(){
     al = -1*g*sin(th)/(r*f);
     om += al;
     th += om;
     coorMasa.x = coorOrigen.x + r*sin(th);
     coorMasa.y = coorOrigen.y + r*cos(th);
   }
 
   public void animar(){
     actualizar();
     dibujar();
   }
 }
   static public void main(String[] passedArgs) {
     String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "pendulo" };
     if (passedArgs != null) {
       PApplet.main(concat(appletArgs, passedArgs));
     } else {
       PApplet.main(appletArgs);
     }
   }
 }
