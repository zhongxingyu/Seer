 package de.sofd.viskit.test.jogl.coil;
 
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureCoords;
 import com.sun.opengl.util.texture.TextureData;
 import com.sun.opengl.util.texture.TextureIO;
 import de.sofd.lang.Runnable2;
 import de.sofd.util.DynScope;
 import de.sofd.viskit.image3D.jogl.util.GLShader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.ShortBuffer;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import javax.media.opengl.GL3;
 import javax.media.opengl.glu.GLU;
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
 
     private static TextureData coilTextureData;
 
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
                 GLU glu = GLU.createGLU(gl);
 
                 System.out.println("initializing coil texture...");
                 // initialize the Texture's underlying TextureData (which doesn't need a GL context)
                 // independently and reuse it if/when the context is re-initialized. The Texture object essentially
                 // uses the TextureData's image (pixel) data and metadata and binds that into a GL texture in
                 // the currently active GL context. Unlike the Texture itself, the TextureData is independent of
                 // the context and still valid after the context is lost. The TextureData in turn (and thus the Texture)
                 // will directly use the pixel buffer of the BufferedImage supplied here, i.e. it won't copy
                 // it internally[1]. So that buffer will be passed to glTex(Sub)Image2D. At that point however, the
                 // GL driver will copy it internally, so there will still be 2 copies of the data -- one in the
                 // BufferedImage, one in the GL driver (the latter possibly in VRAM, depending on driver internals).
                 // To get rid of the former and still use the texture,
                 // it might work to essentially create the Texture normally, then grab its generated internal
                 // texture ID (which is known to GL) from it and leave the Texture, TextureData and BufferedImage objects
                 // unreferenced (and thus eligible for GC). In that case, only the GL-internal copy of the texture
                 // should remain. The disadvantage of that would be that on GL context re-initializations,
                 // the pixel data would be lost and would have to be re-read from the original storage area
                 // (classpath/filesystem/etc.). Some mixed strategy might be in order here -- reserve some memory
                 // for BufferedImages, other memory for GL-managed textures. Always use the various estimateSize()
                 // methods to get actual byte sizes of textures so as to create better caches. Also, when using
                 // GLJPanel instead of GLCanvas, it should be considered that the GL context may be re-initialized
                 // much more frequently (essentially on every resize of the component...), so a larger GL-external
                 // and smaller GL-internal cache may be in order...
                 //
                 // [1] May not be the case with all kinds of BufferedImage; investigate in the vincinity of
                 //      private void AWTTextureData.createFromImage(BufferedImage image) / case BufferedImage.TYPE_3BYTE_BGR
                 //      / setupLazyCustomConversion
 
                 if (null == coilTextureData) {
                     System.out.println("reading coil texture data from backing original store...");
                     try {
                         System.out.print("(READING TEXTURE DATA)... ");
                         long t0 = System.currentTimeMillis();
                         //coilTextureData = TextureIO.newTextureData(Coil.class.getResourceAsStream("mri_brain.jpg"), true, "jpg");
                         //coilTextureData = TextureIO.newTextureData(new FileInputStream("/home/olaf/gi/resources/DICOM-Testbilder/1578/f0003563_00620.dcm"), true, "dcm");  // with mipmapping
                         //coilTextureData = TextureIO.newTextureData(new FileInputStream("/home/olaf/gi/resources/DICOM-Testbilder/1578/f0003563_00620.dcm"), false, "dcm"); // w/o mipmapping
                         //coilTextureData = TextureIO.newTextureData(new FileInputStream("/shares/projects/DICOM-Testbilder/1578/f0003563_00620.dcm"), true, "dcm");
                         //coilTextureData = createByteLuminanceTexData(true);  // with mipmapping
                         //coilTextureData = createByteLuminanceTexData(false);  // without mipmapping
                         //coilTextureData = createShortLuminanceTexData(true);  // with mipmapping
                         coilTextureData = createShortLuminanceTexData(false);  // without mipmapping
                         long t1 = System.currentTimeMillis();
                         System.out.println("" + (t1-t0) + " ms.");
                     } catch (Exception ex) {
                         throw new RuntimeException("FATAL", ex);
                     }
                     coilTextureData.flush();
                 }
                 System.out.print("(CREATING TEXTURE)... ");
 
                 // force Texture to create POT texture dimensions because they're faster to create
                 // TODO: texture looks wrong (too dark), apparently because of a faulty manual mipmap generation in Texture.java
                 //       (these following two lines make Texture.java think that automatic mipmap creation isn't available,
                 //       so it'll create mipmaps manually (using gluBuild2DMipmaps), which results in these faulty-looking textures).
                 //       Maybe this is the point where the Texture class is no longer suitable to use.
                 //System.setProperty("jogl.texture.nonpot", "true");
                 //System.setProperty("jogl.texture.notexrect", "true");
 
                 long t0 = System.currentTimeMillis();
                 Texture coilTexture = new Texture(coilTextureData);
                 long t1 = System.currentTimeMillis();
                 System.out.println("" + (t1-t0) + " ms. Texture " + coilTexture.getWidth() + "x" + coilTexture.getHeight() + ", size (est.): " + coilTexture.getEstimatedMemorySize());
                 // ===> Es ist schnell, wenn kein Mipmapping oder Texturgrößen
                 //      Zweierpotenzen (oder beides). Kein Mipmapping hat jedoch deutlich sichtbare Qualitaetseinbussen zur Folge.
                 // ===> Man sollte Mipmapping machen und Texturgroessen auf Zweierpotenzen aufrunden.
 
                 int err = gl.glGetError();
                 if (err != GL.GL_NO_ERROR) {
                     System.err.println("After texture creation:");
                 }
                 while (err != GL.GL_NO_ERROR) {
                     System.err.println("GL error: " + glu.gluErrorString(err));
                     err = gl.glGetError();
                 }
                 cd.setAttribute(COIL_TEXTURE, coilTexture);
 
                 //gl.glTexParameteri(coilTexture.getTarget(), GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);  // Texture class's default without mipmapping
                 //gl.glTexParameteri(coilTexture.getTarget(), GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);  // Texture class's default with mipmapping
 
                 //gl.glTexParameteri(coilTexture.getTarget(), GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);  // Texture class's default in any case
 
                 System.out.println("shared context set to " + getId(cd.getGlContext()) + ", creating GL canvasses of other viewers...");
                 System.out.println("initializing coil display list...");
                 TextureCoords coords = coilTexture.getImageTexCoords();  // TODO: use this
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
             GLShader shader = (GLShader) DynScope.get("shader");
             shader.bind();
             shader.bindUniform("tex", 0);
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
             GLShader shader = (GLShader) DynScope.get("shader");
             shader.unbind();
         } else {
             gl.glPopAttrib();
         }
     }
 
 
     private static TextureData createByteLuminanceTexData(boolean mipmap) {
         return new TextureData(   GL.GL_RGB, // int internalFormat,
                                   TEX_W, // int width,
                                   TEX_H, // int height,
                                   0,     // int border,
                                   GL.GL_LUMINANCE, // int pixelFormat,
                                   GL.GL_UNSIGNED_BYTE, // int pixelType,
                                   mipmap, // boolean mipmap,
                                   false, // boolean dataIsCompressed,
                                   false, // boolean mustFlipVertically,  // TODO: correct?
                                   createTextureDataUnsigned8bitLuminance(), // Buffer buffer,
                                   null // Flusher flusher);
                                   );
     }
 
     private static TextureData createShortLuminanceTexData(boolean mipmap) {
         return new TextureData(   GL3.GL_R16_SNORM, // int internalFormat,  // GL_*_SNORM result in GL_INVALID_ENUM and all-white texels on tack (GeForce 8600 GT/nvidia 190.42)
                                   TEX_W, // int width,
                                   TEX_H, // int height,
                                   0,     // int border,
                                  GL3.GL_RED, // int pixelFormat,
                                   GL.GL_SHORT, // int pixelType,
                                   mipmap, // boolean mipmap,
                                   false, // boolean dataIsCompressed,
                                   false, // boolean mustFlipVertically,  // TODO: correct?
                                   createTextureDataSigned16bitLuminance(), // Buffer buffer,
                                   null // Flusher flusher);
                                   );
         // interprets all negative texel values as 0 (black).
         // AFAICS, only internalFormat=LUMINANCE16_SNORM (from GL 3.2) would remedy this,
         // but that isn't available in JOGL. :-( See IRC discussion in doc/textureCreationSpeed/results.txt. (TODO)
     }
 
     //private static final int TEX_W = 512, TEX_H = 512;
     private static final int TEX_W = 819, TEX_H = 999;
     //private static final int TEX_W = 1024, TEX_H = 1024;
     //private static final int TEX_W = 832, TEX_H = 1024;
 
     private static ByteBuffer createTextureDataUnsigned8bitLuminance() {
         int bumpW = TEX_W / 2, bumpH = TEX_H / 4;
         ByteBuffer result = ByteBuffer.allocateDirect(TEX_W * TEX_H);
         byte[] row = new byte[TEX_W];
         for (int y = 0; y < TEX_H; y++) {
             double yfac = Math.sin((double)y/bumpH*Math.PI);
             yfac *= yfac;
             for (int x = 0; x < TEX_W; x++) {
                 double xfac = Math.sin((double)x/bumpW*Math.PI);
                 xfac *= xfac;
                 double grayvalue = yfac * xfac;
                 row[x] = (byte)(grayvalue * 255);  // byte is signed, but is used to hold unsigned 8-bit values here
             }
             result.put(row);
         }
         result.rewind();
         return result;
     }
 
     private static ShortBuffer createTextureDataSigned16bitLuminance() {
         int bumpW = TEX_W / 2, bumpH = TEX_H / 4;
         ShortBuffer result = ShortBuffer.allocate(TEX_W * TEX_H);
         short[] row = new short[TEX_W];
         for (int y = 0; y < TEX_H; y++) {
             double yfac = Math.sin((double)y/bumpH*Math.PI);
             yfac *= yfac;
             for (int x = 0; x < TEX_W; x++) {
                 double xfac = Math.sin((double)x/bumpW*Math.PI);
                 xfac *= xfac;
                 double grayvalue = yfac * xfac;
                 row[x] = (short)(grayvalue * 65535 - 32768);
                 if (row[x] < -32000) {
                     if (row[x] < -32600) {
                         row[x] = -16384;
                     } else {
                         row[x] = 0;
                     }
                 }
             }
             result.put(row);
         }
         result.rewind();
         return result;
     }
 
 }
