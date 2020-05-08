 package de.sofd.viskit.controllers.cellpaint;
 
 import static javax.media.opengl.GL.GL_LINEAR;
 import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
 import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;
 import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
 import static javax.media.opengl.GL2.GL_CLAMP;
 import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;
 
 import java.nio.FloatBuffer;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import org.apache.log4j.Logger;
 
 import de.sofd.viskit.model.LookupTable;
 
 /**
  *
  * @author olaf
  */
 public class LookupTableTextureManager {
 
     protected static final Logger logger = Logger.getLogger(LookupTableTextureManager.class);
 
     public static class TextureRef {
         private final int texId;
 
         public TextureRef(int texId) {
             this.texId = texId;
         }
 
         public int getTexId() {
             return texId;
         }
     }
 
     private static class TextureRefStore {
         private final LinkedHashMap<LookupTable, TextureRef> texRefsByImageKey = new LinkedHashMap<LookupTable, TextureRef>(256, 0.75F, true);
 
         public boolean containsTextureFor(LookupTable lut) {
             return texRefsByImageKey.containsKey(lut);
         }
 
         public TextureRef getTexRef(LookupTable lut) {
             return texRefsByImageKey.get(lut);
         }
 
         public void putTexRef(LookupTable lut, TextureRef texRef, GL gl) {
             texRefsByImageKey.put(lut, texRef);
         }
 
     }
 
     private static final String TEX_STORE = "lutTexturesStore";
 
     public static TextureRef bindLutTexture(GL2 gl, Map<String, Object> sharedContextData, LookupTable lut) {
         if (lut == null) {
             return null;
         }
         
         TextureRefStore texRefStore = (TextureRefStore) sharedContextData.get(TEX_STORE);
         if (null == texRefStore) {
             System.out.println("CREATING NEW TextureRefStore");
             texRefStore = new TextureRefStore();
             sharedContextData.put(TEX_STORE, texRefStore);
         }
         TextureRef texRef = texRefStore.getTexRef(lut);
         
         if (null == texRef) {
             logger.info("need to create LUT texture for: " + lut);
             int[] texId = new int[1];
             gl.glGenTextures(1, texId, 0);
             gl.glEnable(GL2.GL_TEXTURE_1D);
             gl.glActiveTexture(GL2.GL_TEXTURE2);
             gl.glBindTexture(gl.GL_TEXTURE_1D, texId[0]);
             FloatBuffer lutToUse = lut.getRGBAValues();
             gl.glTexImage1D(
                     gl.GL_TEXTURE_1D,   // target
                     0,                  // level
                     gl.GL_RGBA32F,      // internalFormat
                     lutToUse.capacity() / 4,  //width
                     0,                  // border
                     gl.GL_RGBA,         // format
                     gl.GL_FLOAT,        // type
                     lutToUse            // data
                     );
             gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP);
             gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
             gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
             texRef = new TextureRef(texId[0]);
             texRefStore.putTexRef(lut, texRef, gl);
         }
         gl.glEnable(GL2.GL_TEXTURE_1D);
         gl.glActiveTexture(GL2.GL_TEXTURE2);
         gl.glBindTexture(GL2.GL_TEXTURE_1D, texRef.getTexId());
         return texRef;
     }
 
     public static void unbindCurrentLutTexture(GL2 gl) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
     }
 }
