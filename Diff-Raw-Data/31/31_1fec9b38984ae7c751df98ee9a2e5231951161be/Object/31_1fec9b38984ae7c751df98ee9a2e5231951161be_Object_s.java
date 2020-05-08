 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Version1;
 
 import java.io.*;
 import static org.lwjgl.opengl.GL11.*;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.*;
 import org.newdawn.slick.util.ResourceLoader;
 
 /**
  *
  * @author Ramen
  */
 public class Object
 {
     private Texture texture;
     private String imgLoc;
     public float xPos, yPos, width, height, tOffX, tOffY, tOffW, tOffH, texW, texH;
     
     public Object()
     {
         ;
     }
     
     public void init(String iLoc, float x, float y, float w, float h, float tx, float ty, float tw, float th)
     {
         xPos = x;
         yPos = y;
         width = w;
         height = h;
         tOffX = tx;
         tOffY = ty;
         tOffW = tw;
         tOffH = th;
         imgLoc = iLoc;
         System.out.println(imgLoc);
         try
         {
             texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(imgLoc));
         }
         catch (IOException e)
         {
             texture = null;
         }
         texW = texture.getTextureWidth();
         texH = texture.getTextureHeight();
     }
     
     public void render()
     {
         texture.getTextureHeight();
         Color.white.bind();
         if(texture != null)
         {
             texture.bind();
         }
         glBegin(GL_QUADS);
            glTexCoord2f(tOffX / texW, (tOffY + tOffH) / texH); glVertex2f(xPos, yPos);
            glTexCoord2f((tOffX + tOffW) / texW, (tOffY + tOffH) / texH); glVertex2f(xPos + width, yPos);
            glTexCoord2f((tOffX + tOffW) / texW, tOffY / texH); glVertex2f(xPos + width, yPos + height);
            glTexCoord2f(tOffX / texW, tOffY / texH); glVertex2f(xPos, yPos + height);
         glEnd();
     }
 }
