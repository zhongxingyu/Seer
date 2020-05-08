 package models;
 
 import org.lwjgl.util.glu.Sphere;
 import org.newdawn.slick.opengl.Texture;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public class UserObject extends Model3D {
     float[] a = {-1.0f, -1.0f, 1.0f};
     float[] b = {1.0f, -1.0f, 1.0f};
     float[] c = {-1.0f, 1.0f, 1.0f};
     float[] d = {1.0f, 1.0f, 1.0f};
 
     float[] e = {-1.0f, -1.0f, -1.0f};
     float[] f = {1.0f, -1.0f, -1.0f};
     float[] g = {-1.0f, 1.0f, -1.0f};
     float[] h = {1.0f, 1.0f, -1.0f};
 
     public UserObject(Texture texture) {
         this.texture = texture;
     }
 
     @Override
     public void render() {
         glEnable(GL_TEXTURE_2D);
         glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
         glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 
         glEnable(GL_TEXTURE_GEN_S);
         glEnable(GL_TEXTURE_GEN_T);
         glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_OBJECT_LINEAR);
         glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_SPHERE_MAP);
 
         glColor4f(1.0f, 1.0f, 1.0f, 0);
         Sphere s = new Sphere();
         s.draw(1.0f, 16, 16);
 
         glDisable(GL_TEXTURE_GEN_S);
         glDisable(GL_TEXTURE_GEN_T);
 
         glDisable(GL_TEXTURE_2D);
     }
 }
