 /*
  * Copyright © 2012-2013 Jason Ekstrand.
  *  
  * Permission to use, copy, modify, distribute, and sell this software and its
  * documentation for any purpose is hereby granted without fee, provided that
  * the above copyright notice appear in all copies and that both that copyright
  * notice and this permission notice appear in supporting documentation, and
  * that the name of the copyright holders not be used in advertising or
  * publicity pertaining to distribution of the software without specific,
  * written prior permission.  The copyright holders make no representations
  * about the suitability of this software for any purpose.  It is provided "as
  * is" without express or implied warranty.
  * 
  * THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
  * INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
  * EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
  * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
  * DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
  * TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
  * OF THIS SOFTWARE.
  */
 package net.jlekstrand.wheatley.jogl;
 
 import java.nio.ByteOrder;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.media.opengl.GLContext;
 import javax.media.opengl.GLException;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLUniformData;
 import javax.media.opengl.GLArrayData;
 import javax.media.opengl.GL2ES2;
 
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 import com.jogamp.opengl.util.glsl.ShaderState;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureData;
 import com.jogamp.opengl.util.GLArrayDataClient;
 
 import org.freedesktop.wayland.server.DestroyListener;
 import org.freedesktop.wayland.protocol.wl_shm;
 
 import net.jlekstrand.wheatley.*;
 
 public class GLRenderer implements Renderer, GLEventListener
 {
     private enum BufferFormat {
         SHM_ARGB8888("surface", "surface_argb8888"),
         SHM_XRGB8888("surface", "surface_xrgb8888");
 
         public String vertexShader;
         public String fragmentShader;
 
         BufferFormat(String vertexShader, String fragmentShader)
         {
             this.vertexShader = vertexShader;
             this.fragmentShader = fragmentShader;
         }
     }
 
     private class SurfaceData
     {
         private Surface surface;
 
         Texture texture;
         public int tex_width;
         public int tex_height;
         public BufferFormat format;
 
         public SurfaceData(final GL2ES2 gl, Surface surface)
         {
             this.surface = surface;
 
             this.texture = new Texture(GL2ES2.GL_TEXTURE_2D);
             texture.setTexParameterf(gl, GL2ES2.GL_TEXTURE_WRAP_S,
                     GL2ES2.GL_CLAMP_TO_EDGE);
             texture.setTexParameterf(gl, GL2ES2.GL_TEXTURE_WRAP_T,
                     GL2ES2.GL_CLAMP_TO_EDGE);
             texture.setTexParameterf(gl, GL2ES2.GL_TEXTURE_MAG_FILTER,
                     GL2ES2.GL_NEAREST);
             texture.setTexParameterf(gl, GL2ES2.GL_TEXTURE_MIN_FILTER,
                     GL2ES2.GL_NEAREST);
 
             surface.resource.addDestroyListener(new DestroyListener() {
                 @Override
                 public void onDestroy()
                 {
                     destroy(gl);
                     surfaceDataCache.remove(this);
                 }
             });
         }
 
         public void refresh(GL2ES2 gl)
         {
             texture.bind(gl);
             Buffer buffer = surface.getBuffer();
 
             if (buffer instanceof ShmBuffer) {
                 ShmBuffer shmBuffer = (ShmBuffer)buffer;
                 ByteBuffer bufferData = shmBuffer.getBuffer();
 
                 switch(shmBuffer.getFormat()) {
                 case wl_shm.FORMAT_ARGB8888:
                     format = BufferFormat.SHM_ARGB8888;
                     break;
                 default:
                 case wl_shm.FORMAT_XRGB8888:
                     format = BufferFormat.SHM_XRGB8888;
                     break;
                 }
 
                 tex_width = shmBuffer.getStride() / 4;
                 tex_height = shmBuffer.getHeight();
 
                 TextureData tdata = new TextureData(drawable.getGLProfile(),
                         GL2ES2.GL_RGBA, tex_width, tex_height, 0,
                         GL2ES2.GL_RGBA, GL2ES2.GL_UNSIGNED_BYTE, false, false,
                         false, bufferData, null);
                 texture.updateImage(gl, tdata);
             }
         }
 
         public void destroy(GL2ES2 gl)
         {
             texture.destroy(gl);
         }
     }
 
     private final HashMap<BufferFormat, ShaderState> shaders;
     private final HashMap<Surface, SurfaceData> surfaceDataCache;
 
     private final GLAutoDrawable drawable;
 
     private GL2ES2 cachedGL;
     private boolean initialized;
 
     private float projectionMatrix[];
 
     public GLRenderer(GLAutoDrawable drawable)
     {
         shaders = new HashMap<BufferFormat, ShaderState>();
         surfaceDataCache = new HashMap<Surface, SurfaceData>();
 
         this.drawable = drawable;
         cachedGL = null;
         initialized = false;
     }
 
     public boolean isInitialized()
     {
         return initialized;
     }
 
     @Override
     public void beginRender(boolean clear)
     {
         int status = drawable.getContext().makeCurrent();
         if (status != GLContext.CONTEXT_CURRENT)
             throw new IllegalStateException();
         cachedGL = drawable.getContext().getGL().getGL2ES2();
 
         if (clear) {
             cachedGL.glClearColor(0, 0, 1, 1);
             cachedGL.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
         }
     }
 
     @Override
     public int endRender()
     {
         drawable.swapBuffers();
         cachedGL = null;
         return (int)System.currentTimeMillis();
     }
 
     @Override
     public void drawSurface(Surface surface)
     {
         GL2ES2 gl = cachedGL;
 
         if (gl == null)
             throw new NullPointerException("cachedGL should not be null");
 
         SurfaceData surfaceData = surfaceDataCache.get(surface);
         if (surfaceData == null) {
             surfaceData = new SurfaceData(gl, surface);
             surfaceDataCache.put(surface, surfaceData);
         }
 
         surfaceData.refresh(gl);
 
         ShaderState sstate = shaders.get(surfaceData.format);
         sstate.useProgram(gl, true);
 
         sstate.uniform(gl, new GLUniformData("vu_texture_size", 2,
             FloatBuffer.wrap(new float[] {
                 surfaceData.tex_width,
                 surfaceData.tex_height
             })));
 
         sstate.uniform(gl, new GLUniformData("vu_projection", 4, 4,
             FloatBuffer.wrap(projectionMatrix)));
         
         sstate.uniform(gl, new GLUniformData("vu_transformation", 4, 4,
             FloatBuffer.wrap(new float[] {
                 1, 0, 0, 0,
                 0, 1, 0, 0,
                 0, 0, 1, 0,
                 0, 0, 0, 1
             })));
 
         sstate.uniform(gl, new GLUniformData("fu_texture", 0));
         
         gl.glActiveTexture(GL2ES2.GL_TEXTURE0);
         surfaceData.texture.bind(gl);
 
         int width = surface.getBuffer().getWidth();
         int height = surface.getBuffer().getHeight();
 
         final float vertices[] = {
             0, 0,
             0, height,
             width, 0,
             0, height,
             width, 0,
             width, height
         };
 
         GLArrayDataClient vertexData = GLArrayDataClient.createGLSL("va_vertex",
                 2, GL2ES2.GL_FLOAT, false, 6);
         vertexData.setName("va_vertex");
         ((FloatBuffer)vertexData.getBuffer()).put(vertices);
         vertexData.seal(true);
         vertexData.enableBuffer(gl, true);
 
         gl.glDrawArrays(GL2ES2.GL_TRIANGLES, 0, 6);
 
         sstate.useProgram(gl, false);
     }
 
     @Override
     public void display(GLAutoDrawable drawable)
     { }
 
     @Override
     public void init(GLAutoDrawable drawable) {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
         gl.setSwapInterval(1);
 
         for (BufferFormat type : BufferFormat.values()) {
             ShaderCode vcode = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER,
                     this.getClass(), "glsl", null, type.vertexShader, false);
             ShaderCode fcode = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER,
                     this.getClass(), "glsl", null, type.fragmentShader, false);
 
             ShaderProgram prog = new ShaderProgram();
             prog.add(vcode);
             prog.add(fcode);
             if (! prog.link(gl, System.err))
                 throw new GLException("Couldn't link program: " + prog);
 
             ShaderState state = new ShaderState();
             state.attachShaderProgram(gl, prog, false);
 
             shaders.put(type, state);
         }
 
         gl.glClearColor(0, 1, 0, 1);
         gl.glClear(GL2ES2.GL_COLOR_BUFFER_BIT);
        gl.glEnable(gl.GL_BLEND);
        gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
         initialized = true;
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
     {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
         gl.glViewport(0, 0, w, h);
 
         projectionMatrix = new float[] {
             2.0f/(float)w, 0, 0, 0,
             0, 2.0f/-(float)h, 0, 0,
             0, 0, -1, 0,
             -1, 1, 0, 1
         };
     }
 
     @Override
     public void dispose(GLAutoDrawable drawable)
     {
         GL2ES2 gl = drawable.getGL().getGL2ES2();
 
         for (SurfaceData data : surfaceDataCache.values())
             data.destroy(gl);
         surfaceDataCache.clear();
 
         for (ShaderState state : shaders.values())
             state.destroy(gl);
         shaders.clear();
 
         return;
     }
 }
 
