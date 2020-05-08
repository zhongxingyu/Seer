 import processing.core.*; 
 import processing.xml.*; 
 
 import java.applet.*; 
 import java.awt.Dimension; 
 import java.awt.Frame; 
 import java.awt.event.MouseEvent; 
 import java.awt.event.KeyEvent; 
 import java.awt.event.FocusEvent; 
 import java.awt.Image; 
 import java.io.*; 
 import java.net.*; 
 import java.text.*; 
 import java.util.*; 
 import java.util.zip.*; 
 import java.util.regex.*; 
 
 public class Hockey extends PApplet {
 
 int x1;
 int y1;
 int rx = width/2-50;
 int ry = height/2;
 int bx = width/2+50;
 int by = height/2;
 int i = 0;
 int o = 3;
 float x = 0;
 float y = 0;
 float xspeed = 10;
 float yspeed = 10;
 float r = 32;
 PFont font;
 int rs = 0;
 int bs = 0;
 int g1x = width-25;
 int g1y = height/2-50;
 int g2x = -75;
 int g3y = height/2-50;
 String rds = "SCORE --->";
 String bds = "<--- SCORE";
 boolean Blue = false;
 int BlueC;
 
 
 public void setup() {
   size(640, 480);
   smooth();
   font = loadFont( "Score.vlw" );
   textFont( font );
 }
 public void draw() {
   
   x1 = mouseX;
   y1 = mouseY;
   button();
     background(255);
     table();
     stroke(0, 0, 255);
     strokeWeight(1);
     fill(0, 0, 255);
   ellipse(bx, by, 50, 50);
   stroke(255, 0, 0);
   strokeWeight(1);
   fill(255, 0, 0);
   ellipse(rx, ry, 50, 50);
   stroke(0);
   fill(0);
   ellipse(bx, by , 25, 25);
   ellipse(rx, ry , 25, 25);
   strokeWeight(5);
   fill(0);
   text( bs,50, 50 );
   text( rs,width-100, 50 );
   
    x = x + xspeed;
   y= y + yspeed;
 
   // Remember, || means "or."
   if ((x > width) || (x < 0)) {
     // If the object reaches either edge, multiply speed by -1 to turn it around.
     xspeed = xspeed * -1;
     
   }
   
    // Remember, || means "or."
   if ((y > height) || (y < 0)) {
     // If the object reaches either edge, multiply speed by -1 to turn it around.
     yspeed = yspeed * -1;
     
   }
   if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed>0) {
     yspeed = random(-25);
     xspeed = random(25) * -1;
     
   }
     if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed>0) {
     yspeed = random(25);
     xspeed = random(25) * 1;
     
   }
       if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed<0) {
     yspeed = random(-25);
     xspeed = random(25) * -1;
     
   }
     if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed<0 && yspeed<0) {
     yspeed = random(25);
     xspeed = random(25) * 1;
     
    
   }
   if(x<0 && y<0) {
     x = width/2;
     y = height/2;
   }
   if(x>width && y<0) {
     x = width/2;
     y = height/2;
   }
   if(x>width && y>height) {
     x = width/2;
     y = height/2;
   }
     if(x>width && y<0) {
     x = width/2;
     y = height/2;
   }
     if(x>width-25 && y>(height/2)-50 && y<(height/2)+50) {
    rs++;
     x = width/2;
     y = height/2;
     text( rds,width/2-100, 50 );
     }
         if(x<25 && y>(height/2)-50 && y<(height/2)+50) {
    bs++;
     x = width/2;
     y = height/2;
     text( bds,width/2-100, 50 );
     
     }
   
   stroke(0);
   strokeWeight(1);
   fill(175);
   ellipse(x,y,25,25);
 
   
 
   
 
 }
 public void button() {
   if( x1 > 0 && x1 < (width/2)-25 && y1 > 0 && y1 < height ){
     stroke(0, 0, 255);
   strokeWeight(5);
   point(x1, y1);
   bx = mouseX;
   by = mouseY;
 }
   if( x1 > (width/2)+25 && x1 < width && y1 > 0 && y1 < height ){
     stroke(255, 0, 0);
   strokeWeight(5);
   point(x1, y1);
   
   rx = mouseX;
   ry = mouseY;
   }
 
   // Display circle at x location
 
   println(rx + " " + ry);
 
 }
 public void table(){
 
 for(int o=0; o<700; o+=15) {
   for(int i=0; i<700; i+=15) {
     strokeWeight(2);
     stroke(125,125,125);
     point(i, o);
   }
 }
     
   strokeWeight(5);
   stroke(173, 173, 173, 125);
   line(width/2, 0, width/2, height);
   noFill();
   stroke(0, 0, 255);
   strokeWeight(5);
   ellipse(width/2, height/2, 100, 100);
   strokeWeight(2);
   rect(width-25, height/2-50, 100, 100);
   stroke(255, 0, 0);
   strokeWeight(2);
   rect(-75, height/2-50, 100, 100);
   ellipse(width/2, height/2, 50, 50);
   
 
 }
 public void mousePressed() {
     if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed>0) {
     yspeed = random(-25) - 50;
     xspeed = (xspeed + 50) * -1;
     r = 64;
   }
     if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed>0) {
     yspeed = random(25) + 50;
     xspeed = (xspeed + 50) * 1;
     r = 64;
   }
       if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed<0) {
     yspeed =random(-25) - 50;
     xspeed = (xspeed + 50) * -1;
     r = 64;
   }
     if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed<0 && yspeed<0) {
     yspeed = random(25);
     xspeed = (xspeed + 50) * 1;
     
     r = 64;
   }
 }
 public void mouseDragged() {
     if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed>0) {
     yspeed = random(-25) - 50;
     xspeed = (xspeed + 50) * -1;
     r = 64;
   }
     if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed>0) {
     yspeed = random(25) + 50;
     xspeed = (xspeed + 50) * 1;
     r = 64;
   }
       if ( y>=by-50 && y<=(by+50) && x>=(bx-50) && x<=bx+50 && x<=rx+50 && xspeed<0) {
     yspeed =random(-25) - 50;
     xspeed = (xspeed + 50) * -1;
     r = 64;
   }
     if ( y>=ry-50 && y<=(ry+50) && x>=(rx-50) && x<=rx+50 && xspeed<0 && yspeed<0) {
     yspeed = random(25);
     xspeed = (xspeed + 50) * 1;
     
     r = 64;
   }
 }
 public void keyPressed() {
   if (key == 'c'){
   x = width/2;
   y = width/2;
   xspeed = 10;
   yspeed = 10;
   }
 }
 
   
 
 
   
   static public void main(String args[]) {
     PApplet.main(new String[] { "--bgcolor=#ECE9D8", "Hockey" });
   }
 }
