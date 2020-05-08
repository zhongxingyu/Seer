 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cognitive;
 
 import org.cognitive.shadermanager.Shader;
 import org.lwjgl.BufferUtils;
 import java.nio.FloatBuffer;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import java.awt.Font;
 import java.nio.ByteBuffer;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.GL11;
 import static org.lwjgl.opengl.GL11.*;
 
 
 /**
  *
  * @author kristoffer
  */
 public class Graphics {
   
   Camera camera = new Camera(0,0);
   Shader lightingVS;
   Shader lightingFS;
   
   //<editor-fold defaultstate="collapsed" desc="font declarations and inits">
   private static org.newdawn.slick.Font bigFont = new TrueTypeFont(new Font("Georgia", 1, 20), false);
   private static org.newdawn.slick.Font mediumFont = new TrueTypeFont(new Font("Georgia", 1, 16), false);
   private static org.newdawn.slick.Font smallFont = new TrueTypeFont(new Font("Georgia", 1, 10), false);
   //</editor-fold>
   
   public Graphics() {
     lightingVS = new Shader("lighting.vs");
     lightingFS = new Shader("lighting.fs");
   }
   public void render() {
     int mouseX = Mouse.getX();
     int mouseY = (-Mouse.getY()+Window.DISPLAY_HEIGHT);
     
     lightingVS.begin();
     lightingFS.begin();
     glBegin(GL_QUADS);
       glVertex2d(50, 50);
       glVertex2d(50 + 50, 50);
       glVertex2d(50 + 50, 50 + 50);
       glVertex2d(50, 50 + 50);
     glEnd();
 
     lightingVS.end();
     lightingFS.end();
//    
 //    FloatBuffer specularColor = BufferUtils.createFloatBuffer(4);
 //    FloatBuffer lightPosition = BufferUtils.createFloatBuffer(4);
 //    FloatBuffer diffuseColor = BufferUtils.createFloatBuffer(4);
 //    
 //    FloatBuffer constantAttenuation = BufferUtils.createFloatBuffer(4);
 //    FloatBuffer linearAttenuation = BufferUtils.createFloatBuffer(4);
 //    FloatBuffer quadraticAttenuation = BufferUtils.createFloatBuffer(4);
 //
 //    constantAttenuation.put(1.0f).put(1.0f).put(0.0f).put(1.0f).flip();
 //    
 //    linearAttenuation.put(1.0f).put(0.0f).put(0.0f).put(1.0f).flip();
 //    
 //    quadraticAttenuation.put(1.0f).put(0.0f).put(0.0f).put(1.0f).flip();
 //    
 //    glLight(GL_LIGHT0, GL_CONSTANT_ATTENUATION, constantAttenuation);
 //    glLight(GL_LIGHT0, GL_LINEAR_ATTENUATION, linearAttenuation); 
 //    glLight(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, quadraticAttenuation); 
 //    
       FloatBuffer ambientLight = BufferUtils.createFloatBuffer(4);
 //
 //    lightPosition.put(mouseX).put(mouseY).put(1.0f).put(1.0f).flip();
 //    
 //    specularColor.put(3.0f).put(3.0f).put(3.0f).put(1.0f).flip();
 //    
 //    diffuseColor.put(1.0f).put(1.0f).put(1.0f).put(1.0f).flip();
 //    
       ambientLight.put(0.9f).put(0.9f).put(0.9f).put(1.0f).flip();
 //    
 //    glLight(GL_LIGHT0, GL_POSITION, lightPosition);
 //    glLight(GL_LIGHT0, GL_SPECULAR, specularColor); 
 //    glLight(GL_LIGHT0, GL_DIFFUSE, diffuseColor); 
 //    
 //    glEnable(GL_LIGHT0);
 //    glEnable(GL_COLOR_MATERIAL);
 //
       glLightModel(GL_LIGHT_MODEL_AMBIENT, ambientLight);
   }
   public void update() {
     
   }
   //<editor-fold defaultstate="collapsed" desc="drawString">
   public static enum FontSize {
     Small, Medium, Large;
   }
   public static void drawString(int x, int y, String string, Graphics.FontSize size, Color color) {
     glPushAttrib(GL_LIGHTING);
     glDisable(GL_LIGHTING);
     switch (size) {
       case Small:
         smallFont.drawString(x, y, string, color);
         break;
       case Medium:
         mediumFont.drawString(x, y, string, color);
         break;
       case Large:
         bigFont.drawString(x, y, string, color);
         break;
       default:
         mediumFont.drawString(x, y, string, Color.yellow);
         break;
     }
     glPopAttrib();
     
   }
   //</editor-fold>
   //<editor-fold defaultstate="collapsed" desc="various draw methods">
   public void drawFPS(int fps) {
     drawString(500,1, "FPS: " + fps, FontSize.Medium, Color.yellow);
   }
   public static void drawLineBox(int x0, int y0, int x1, int y1, boolean stippled) {
     glPushMatrix();
     glPushAttrib(GL_LIGHTING);
     glPushAttrib(GL_LINE_STIPPLE);
     glPushAttrib(GL_TEXTURE_2D);
     glDisable(GL_LIGHTING);
     if (stippled) {
       glLineStipple(1, (short)0xAAAA);
       glEnable(GL_LINE_STIPPLE);
     }
     glDisable(GL_TEXTURE_2D);
     glColor3d(1.0, 1.0, 1.0); // White is nice
     
     glPushAttrib(GL_FRONT_AND_BACK);
     glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
     // GL11.glRecti(iMouseX, iMouseY, mouseX , mouseY);
     glRecti(x0, y0, x1 , y1);
     glPopAttrib();
     glPopAttrib();
     glPopAttrib();
     glPopAttrib();
     glPopMatrix();
     
   }
   //</editor-fold>
 }
