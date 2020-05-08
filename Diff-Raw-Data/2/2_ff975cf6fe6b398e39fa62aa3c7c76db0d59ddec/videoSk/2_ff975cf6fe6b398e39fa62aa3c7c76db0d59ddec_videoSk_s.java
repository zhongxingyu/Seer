 package videosktest;
 
 import processing.core.*;
 import processing.data.*;
 import processing.event.*;
 import processing.opengl.*;
 
 import processing.video.*;
 import com.google.zxing.*;
 import java.awt.image.BufferedImage;
 
 import com.google.zxing.*;
 import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
 import com.google.zxing.common.HybridBinarizer;
 
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.IOException;
 
 public class videoSk extends PApplet {
 
     Capture cam; //Set up the camera
     com.google.zxing.Reader reader = new com.google.zxing.qrcode.QRCodeReader();
     int WIDTH = 640;
     int HEIGHT = 480;
     PImage cover;  //This will have the cover image
     String lastISBNAcquired = "";  //This is the last ISBN we acquired
 
 // Grabs the image file    
     public void setup() {
         size(640, 480);
     
         // The font "Meta-Bold.vlw" must be located in the 
         // current sketch's "data" directory to load successfully
        
 
         cam = new Capture(this, WIDTH, HEIGHT);
         cam.start();
     }
 
     public void draw() {
         if (cam.available() == true) {
             cam.read();
             pushMatrix();
             scale(-1, 1);
             translate(-cam.width, 0);
             image(cam, 0, 0);
             popMatrix();
 
 
             //image(cam, 0,0);
             try {
                 // Now test to see if it has a QR code embedded in it
                 LuminanceSource source = new BufferedImageLuminanceSource((BufferedImage) cam.getImage());
                 BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                 Result result = reader.decode(bitmap);
                 Thread.sleep(1000);
                 //Once we get the results, we can do some display
                 if (result.getText() != null) {
                     println(result.getText());
                     ResultPoint[] points = result.getResultPoints();
                     //Draw some ellipses on at the control points
                     for (int i = 0; i < points.length; i++) {
                         // fill(#ff8c00);
                         ellipse(points[i].getX(), points[i].getY(), 20, 20);
                         // fill(#ff0000);
                         text(i, points[i].getX(), points[i].getY());
                     }
 
                     //Superimpose the cover on the image
                     if (cover != null) {
                         image(cover, points[1].getX(), points[1].getY());
                     }
                 }
             } catch (Exception e) {
 //         println(e.toString()); 
             }
         }
     }
 
     static public void main(String[] passedArgs) {
 
        String[] appletArgs = new String[]{"videosk.videoSk"};
 
         if (passedArgs != null) {
             PApplet.main(concat(appletArgs, passedArgs));
 
         } else {
 
             PApplet.main(appletArgs);
 
         }
 
     }
 }
