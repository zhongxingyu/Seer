 package de.sofd.viskit.test.jogl.coil;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 import com.sun.opengl.util.texture.TextureIO;
 import de.sofd.lang.Runnable2;
 import java.io.IOException;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import static de.sofd.viskit.test.jogl.coil.Constants.*;
 
 
 /**
  *
  * @author olaf
  */
 public class Coil implements GLDrawableObject {
     float[] locationInWorld = new float[3];
     float rotAngle;   // in degrees
     float rotAngularVelocity;   // in degrees / sec
     float[] color = new float[4];
     boolean isTextured = false;
 
     private static final String COIL_DISP_LIST_ID = "coilDisplayListId";
     private static final String COIL_TEXTURE = "coilTexture";
 
     // these should be per-coil eventually
     static final float coil_radius = 15;
     static final float wire_radius = 3;
     static final float coil_height = 40;
     static final float coil_winding_angle_range = 3 * 2 * (float) Math.PI;
     static final float tex_angular_length_w = 2 * (float) Math.PI / 3;
     static final float tex_angular_length_h = 2 * (float) Math.PI / 20;
     static final float mesh_count_w = 20;
     static final float mesh_count_h = 40 * (coil_winding_angle_range / 2 / (float) Math.PI);
 
     static final float mesh_da_w = 2 * (float) Math.PI / (mesh_count_w - 1);
     static final float mesh_da_h = coil_winding_angle_range / (mesh_count_h - 1);
     static final float tex_dcoord_w = mesh_da_w / tex_angular_length_w;
     static final float tex_dcoord_h = mesh_da_h / tex_angular_length_h;
     static final float coil_bottom = -coil_height/2;
     static final float coil_dh = coil_height / (mesh_count_h - 1);
 
     static {
         // can't TextureIO.newTexture(...) here b/c it needs a context...
 
         SharedContextData.registerContextInitCallback(new Runnable2<SharedContextData, GL>() {
             @Override
             public void run(SharedContextData cd, GL gl1) {
                 GL2 gl = gl1.getGL2();
 
                 System.out.println("initializing coil texture...");
                 Texture coilTexture;
                 try {
                     coilTexture = TextureIO.newTexture(Coil.class.getResourceAsStream("mri_brain.jpg"), true, "jpg");
                 } catch (IOException ex) {
                     throw new RuntimeException("FATAL", ex);
                 }
                 cd.setAttribute(COIL_TEXTURE, coilTexture);
 
                 System.out.println("shared context set to " + getId(cd.getGlContext()) + ", creating GL canvasses of other viewers...");
                 System.out.println("initializing coil display list...");
                 TextureCoords coords = coilTexture.getImageTexCoords();
                 int coilDisplayList = gl.glGenLists(1);  //TODO: error handling
                 cd.setAttribute(COIL_DISP_LIST_ID, coilDisplayList);
                 gl.glNewList(coilDisplayList, gl.GL_COMPILE);
                 for (int mesh_h = 0; mesh_h < mesh_count_h; mesh_h++) {
                     float ah = mesh_h * mesh_da_h;
                     float texCoordH = mesh_h * tex_dcoord_h;
                     gl.glBegin(gl.GL_TRIANGLE_STRIP);
                     for (int mesh_w = 0; mesh_w < mesh_count_w; mesh_w++) {
                         float aw = mesh_w * mesh_da_w;
                         float texCoordW = mesh_w * tex_dcoord_w;
                         float[] objv = new float[3], normv = new float[3];
                         mesh2normv(ah, aw, normv);
                         mesh2objCoord(ah, aw, objv);
                         gl.glTexCoord2f(texCoordW, texCoordH);
                         gl.glNormal3fv(normv, 0);
                         gl.glVertex3fv(objv, 0);
                         mesh2normv(ah + mesh_da_h, aw, normv);
                         mesh2objCoord(ah + mesh_da_h, aw, objv);
                         gl.glTexCoord2f(texCoordW, texCoordH + tex_dcoord_h);
                         gl.glNormal3fv(normv, 0);
                         gl.glVertex3fv(objv, 0);
                     }
                     gl.glEnd();
                 }
                 gl.glEndList();
             }
         });
     }
 
 
     private static void mesh2objCoord(float ah, float aw, float[] result) {
         result[0] = coil_radius * (float)Math.cos(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.cos(ah);
         result[1] = coil_bottom + coil_dh * ah / mesh_da_h + wire_radius * (float)Math.sin(aw);
         result[2] = coil_radius * (float)Math.sin(ah) + wire_radius * (float)Math.cos(aw) * (float)Math.sin(ah);
     }
 
 
     private static void mesh2normv(float ah, float aw, float[] result) {
         float[] tangv1 = new float[3];
         tangv1[0] = -coil_radius * (float)Math.sin(ah) - wire_radius * (float)Math.cos(aw) * (float)Math.sin(ah);
         tangv1[1] = coil_dh/mesh_da_h;
         tangv1[2] = coil_radius * (float)Math.cos(ah) + wire_radius * (float)Math.cos(ah) * (float)Math.cos(aw);
 
         float[] tangv2 = new float[3];
         tangv2[0] = - wire_radius * (float)Math.cos(ah) * (float)Math.sin(aw);
         tangv2[1] = wire_radius * (float)Math.cos(aw);
         tangv2[2] = - wire_radius * (float)Math.sin(ah) * (float)Math.sin(aw);
 
         //cross(tangv1, tangv2, unnormalized);
         float[] unnormalized = LinAlg.cross(tangv2, tangv1, null);
 
         LinAlg.norm(unnormalized, result);
     }
 
 
     @Override
     public void draw(SharedContextData cd, GL gl1) {
         GL2 gl = gl1.getGL2();
         // printf("Drawing coil at %lf, %lf, %lf\n", c.locationInWorld[0], c.locationInWorld[1], c.locationInWorld[2]);
 
         Texture coilTexture = (Texture) cd.getAttribute(COIL_TEXTURE);
         if (isTextured) {
             gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_REPEAT);
             gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_REPEAT);
             gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
             coilTexture.enable();
             coilTexture.bind();
         } else {
             gl.glPushAttrib(gl.GL_COLOR_BUFFER_BIT|gl.GL_CURRENT_BIT);
             gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_AMBIENT_AND_DIFFUSE);
             //gl.glColorMaterial(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR);
             gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SPECULAR, GLCOLOR_WHITE, 0);
             gl.glMaterialfv(gl.GL_FRONT_AND_BACK, gl.GL_SHININESS, mid_shininess, 0);
             gl.glColor3fv(this.color, 0);
         }
         int coilDisplayList = (Integer) cd.getAttribute(COIL_DISP_LIST_ID);
         gl.glCallList(coilDisplayList);
         if (isTextured) {
             coilTexture.disable();
         } else {
             gl.glPopAttrib();
         }
     }
 
 }
