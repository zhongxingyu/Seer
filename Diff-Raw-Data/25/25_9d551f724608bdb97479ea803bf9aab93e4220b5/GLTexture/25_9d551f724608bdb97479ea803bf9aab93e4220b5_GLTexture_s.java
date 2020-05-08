 package org.racenet.framework;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
import android.util.Log;
 
 import org.racenet.framework.interfaces.FileIO;
 import org.racenet.framework.GLGame;
 import org.racenet.framework.GLGraphics;
 
 public class GLTexture {
 	
     GLGraphics glGraphics;
     FileIO fileIO;
     String fileName;
     int textureId;
     int minFilter;
     int magFilter;
     public int width;
     public int height;
     public static String APP_FOLDER = "";
     
     public GLTexture(GLGame glGame, String fileName) {
     	
         glGraphics = glGame.getGLGraphics();
         fileIO = glGame.getFileIO();
         this.fileName = fileName;
         load();
     }
     
     public int getId() {
     	
     	return textureId;
     }
     
     public boolean load() {
     	
         GL10 gl = glGraphics.getGL();
         int[] textureIds = new int[1];
         gl.glGenTextures(1, textureIds, 0);
         textureId = textureIds[0];
         
         InputStream in = null;
         try {
         	
             in = fileIO.readAsset("textures" + File.separator + this.fileName);
             Bitmap bitmap = BitmapFactory.decodeStream(in);
             gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
             GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
             setFilters(GL10.GL_NEAREST, GL10.GL_NEAREST);            
             gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
             width = bitmap.getWidth();
             height = bitmap.getHeight();
             bitmap.recycle();
             
         } catch (IOException e) {
         	
             try {
             	
             	in = fileIO.readFile(APP_FOLDER + File.separator + "textures" + File.separator + this.fileName);
                 Bitmap bitmap = BitmapFactory.decodeStream(in);
                 gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
                 GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
                 setFilters(GL10.GL_NEAREST, GL10.GL_NEAREST);            
                 gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
                 width = bitmap.getWidth();
                 height = bitmap.getHeight();
                 bitmap.recycle();
                 
             } catch (IOException e2) {
             	
             	return false;
             	
             } finally {
             	
                 if (in != null) {
                 
                 	try { in.close(); } catch (IOException e2) {}
                 }
             }
             
         } finally {
         	
             if (in != null) {
             
             	try { in.close(); } catch (IOException e) {}
             }
         }
         
         return true;
     }
     
     public void reload() {
     	
         load();
         bind();
         setFilters(minFilter, magFilter);        
         glGraphics.getGL().glBindTexture(GL10.GL_TEXTURE_2D, 0);
     }
     
     public void setFilters(int min, int mag) {
     	
         minFilter = min;
         magFilter = mag;
         GL10 gl = glGraphics.getGL();
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
     }    
     
     public void bind() {
     	
         GL10 gl = glGraphics.getGL();
         gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
     }
     
     public void dispose() {
     	
         GL10 gl = glGraphics.getGL();
         gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
         int[] textureIds = {textureId};
         gl.glDeleteTextures(1, textureIds, 0);
     }
 }
