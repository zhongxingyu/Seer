 package dk.aau.cs.giraf.train.opengl;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.opengl.GLUtils;
 import android.util.Log;
 
 /**
  * A texture class that extends the {@link Square} class.
  * The texture will always stretch to the specified size.
  * Use {@link #loadTexture} to load the texture into OpenGL.
  * Use {@link #draw} to draw the texture at the current position.
  * 
  * @author Jesper Riemer Andersen
  * @see Square
  * @see Shape
  */
 public class Texture extends Square {
     
     /** If true: Loading texture will generate an equivalent power-of-two sized bitmap texture. */
    protected boolean GENERATE_POWER_OF_TWO_EQUIVALENT = true;
     
     /** 
      * The texture pointer to memory.
      * It must be an array since the parameter of {@link GL10#glGenTextures} takes an array.
      */
     private int[] texture = new int[1];
     
     /** The buffer holding the texture coordinates */
     private FloatBuffer textureBuffer;
     
     /** The original bitmap width */
     protected int bitmapWidth;
     /** The original bitmap height */
     protected int bitmapHeight;
     
     /** 
      * The texture coordinates.
      * The order of the coordinates determines the orientation of the texture.
      */   
     private float textureCoordinates[] = {
         // Mapping coordinates for the vertices
         0.0f, 0.0f,    // bottom left  (V1)
         1.0f, 0.0f,    // bottom right (V3)
         0.0f, 1.0f,    // top left     (V2)
         1.0f, 1.0f     // top right    (V4)
     };
     
     /** 
      * Sets the size of the texture and initialises a {@link FloatBuffer} for the vertices.
      * @see Square#Square(float, float)
      * @param width
      * @param height
      */
     public Texture(float width, float height) {
         super(width, height);
         this.initialiseTextureBuffer();
     }
     
     /** Initialises the texture buffer based on the texture coordinates. */
     private void initialiseTextureBuffer() {
         ByteBuffer byteBuf = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
         byteBuf.order(ByteOrder.nativeOrder());
         textureBuffer = byteBuf.asFloatBuffer();
         textureBuffer.put(textureCoordinates);
         textureBuffer.position(0);
     }
     
     /**
      * Set new texture coordinates and reinitialise the texture buffer.
      * @param textureCoordinates the new texture coordinates.
      */
     protected void setTextureCoordinates(float[] textureCoordinates) {
         this.textureCoordinates = textureCoordinates;
         this.initialiseTextureBuffer();
     }
     
     /**
      * The aspect ratio setting used to rescale the shape according to the texture.<br><br>
      * {@code KeepWidth} resizes the height.<br>
      * {@code KeepHeight} resizes the width.<br>
      * {@code KeepBoth} does not resize the shape, keeps the original width and height.<br>
      * {@code BitmapOneToOne} resizes both the height and the width to equal the original size of the bitmap texture.
      */
     public enum AspectRatio {
         KeepWidth, KeepHeight, KeepBoth, BitmapOneToOne
     }
     
     /**
      * Resizes shape to fit the texture's aspect ratio.
      * @param option specify to resize the width, the height, both, or none.
      * @see AspectRatio
      */
     @SuppressWarnings("incomplete-switch") // KeepBoth should not have a case
     protected void setAspectRatio(AspectRatio option) {
         switch (option) {
         case KeepHeight:
             super.setWidth(super.getHeight() * ((float) this.bitmapWidth / this.bitmapHeight));
             break;
         case KeepWidth:
             super.setHeight(super.getWidth() * ((float) this.bitmapHeight / this.bitmapWidth));
             break;
         case BitmapOneToOne:
             super.setWidth(this.bitmapWidth);
             super.setHeight(this.bitmapHeight);
             break;
         }
     }
     
     /** 
      * Draw the texture.
      * @param gl         the {@link GL10} instance.
      * @param coordinate where the {@link Renderable} is being drawn.
      */
     @Override
     public void draw(GL10 gl, Coordinate coordinate) {
         this.draw(gl, coordinate, Color.White);
     }
     
     /** 
      * Draw the texture with the specified RGBA color
      * 
      * @param gl    the {@link GL10} instance.
      * @param coordinate where the {@link Renderable} is being drawn.
      * @param color a color overlay.
      */
     @Override
     public void draw(GL10 gl, Coordinate coordinate, Color color) {
         gl.glColor4f(color.red, color.green, color.blue, color.alpha);
         
         //Enable texture
         gl.glEnable(GL10.GL_TEXTURE_2D);
         
         //Bind our only previously generated texture in this case
         gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
         
         //Point to our buffers
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
         gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
         
         //Set the face rotation
         gl.glFrontFace(GL10.GL_CW);
         
         //Enable the vertex and texture state
         gl.glVertexPointer(3, GL10.GL_FLOAT, 0, super.getVertexBuffer());
         gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
         
         //Draw the vertices as triangles, based on the Index Buffer information
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, super.getVertices().length / 3);
         
         //Disable the client state before leaving
         gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
         gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
         
         //Remember to disable texture. Shapes seem to fail if texture is left enabled
         gl.glDisable(GL10.GL_TEXTURE_2D);
     }
     
     /**
      * Generate the texture and the pointer.
      * @param gl                 the {@link GL10} instance.
      * @param context            the current activity context.
      * @param bitmap             the bitmap to use as texture.
      * @param option             specify which length to keep when resizing the shape to match the texture's aspect ratio.
      * @param glTextureParameter the wrap texture parameter.
      */
     protected void generateTexturePointer(GL10 gl, Context context, Bitmap bitmap, AspectRatio option, int glTextureParameter) {
         this.bitmapWidth = bitmap.getWidth();
         this.bitmapHeight = bitmap.getHeight();
         
         //Resizes the shape to fit the aspect ratio
         this.setAspectRatio(option);
         
         //Resizes the bitmap to a power-of-two and crops the texture accordingly
         if(this.GENERATE_POWER_OF_TWO_EQUIVALENT) bitmap = this.generatePowerOfTwoBitmap(bitmap);
         
         //Resizes the bitmap if its size is not supported on this device
         bitmap = this.maintainMaxTextureSize(gl, bitmap);
         
         //Generate one texture pointer...
         gl.glGenTextures(1, texture, 0);
         //...and bind it to our array
         gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
         
         //Specify parameters for texture
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR); // use LINEAR when upscaling
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // use LINEAR when downscaling
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, glTextureParameter);
         gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, glTextureParameter);
         
         //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
         GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
         
         //Clean up. Sends the bitmap to the garbage collector
         bitmap.recycle();
     }
     
     /**
      * Generate the texture and the pointer.
      * @param gl                 the {@link GL10} instance.
      * @param context            the current activity context.
      * @param resourcePointer    a pointer to the resource to load.
      * @param option             specify which length to keep when resizing the shape to match the texture's aspect ratio.
      * @param glTextureParameter the wrap texture parameter.
      */
     protected void generateTexturePointer(GL10 gl, Context context, int resourcePointer, AspectRatio option, int glTextureParameter) {
       //Get the texture from the Android resource directory
         Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourcePointer);
         this.generateTexturePointer(gl, context, bitmap, option, glTextureParameter);
     }
     
     /** 
      * Loads the specified texture.
      * @param gl              the {@link GL10} instance.
      * @param context         the current activity context.
      * @param resourcePointer a pointer to the resource to load.
      */
     public void loadTexture(GL10 gl, Context context, int resourcePointer) {
         this.loadTexture(gl, context, resourcePointer, AspectRatio.KeepBoth);
     }
     
     /** 
      * Loads the specified texture.
      * @param gl              the {@link GL10} instance.
      * @param context         the current activity context.
      * @param resourcePointer a pointer to the resource to load.
      * @param option          specify which length to keep when resizing the shape to match the texture's aspect ratio.
      */
     public void loadTexture(GL10 gl, Context context, int resourcePointer, AspectRatio option) {
         this.generateTexturePointer(gl, context, resourcePointer, option, GL10.GL_CLAMP_TO_EDGE); // make 'clamp to edge' default
     }
     
     /** 
      * Loads the specified texture.
      * @param gl      the {@link GL10} instance.
      * @param context the current activity context.
      * @param bitmap  a bitmap to use as texture.
      */
     public void loadTexture(GL10 gl, Context context, Bitmap bitmap) {
         this.loadTexture(gl, context, bitmap, AspectRatio.KeepBoth);
     }
     
     /** 
      * Loads the specified texture.
      * @param gl      the {@link GL10} instance.
      * @param context the current activity context.
      * @param bitmap  a bitmap to use as texture.
      * @param option  specify which length to keep when resizing the shape to match the texture's aspect ratio.
      */
     public void loadTexture(GL10 gl, Context context, Bitmap bitmap, AspectRatio option) {
         this.generateTexturePointer(gl, context, bitmap, option, GL10.GL_CLAMP_TO_EDGE); // make 'clamp to edge' default
     }
     
     /**
      * Calculate the next power-of-two that is greater than or equal to x.
      * @param x the number to find the next power-of-two of.
      * @return A power-of-two-number greater than or equal to x
      */
     private int getNextPowerOfTwo(int x) {
         return (int) Math.pow(2.0, 32 - Integer.numberOfLeadingZeros(x - 1));  
     }
     
     /**
      * If the size of the bitmap is not power-of-two,
      * then create alpha channels below and to the right of the original bitmap.
      * @param bitmap
      * @return A new bitmap with power-of-two width/height 
      */
     protected Bitmap generatePowerOfTwoBitmap(Bitmap bitmap) {
         int newWidth = this.getNextPowerOfTwo(bitmap.getWidth());
         int newHeight = this.getNextPowerOfTwo(bitmap.getHeight());
         
         // Check whether the bitmap was already the correct size
         if(newWidth != bitmap.getWidth() || newHeight != bitmap.getHeight()) {
             // Create a new bitmap that is a power-of-two
             Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
             Canvas canvas = new Canvas(newBitmap);
             canvas.drawBitmap(bitmap, 0.0f, 0.0f, null);
             
             // Crop the texture to fit the original size
             this.cropTexture(bitmap.getWidth(), bitmap.getHeight(), newWidth, newHeight);
             
             // Clean up. Send to garbage collector
             bitmap.recycle();
             
             return newBitmap;
         }
         return bitmap;
     }
     
     /**
      * Crop the texture to remove the unneccesary alpha channels around the bitmap.
      * @param oldWidth the bitmaps original width.
      * @param oldHeight the bitmaps original height.
      * @param newWidth the new power-of-two width.
      * @param newHeight the new power-of-two height.
      * @see #generatePowerOfTwoBitmap(Bitmap)
      */
     protected void cropTexture(int oldWidth, int oldHeight, int newWidth, int newHeight) {
         this.setTextureCoordinates(new float[] {
                 // Mapping coordinates for the vertices
                 0.0f, 0.0f,                                            // bottom left  (V1)
                 (float) oldWidth/newWidth, 0.0f,                       // bottom right (V3)
                 0.0f, (float) oldHeight/newHeight,                     // top left     (V2)
                 (float) oldWidth/newWidth, (float) oldHeight/newHeight // top right    (V4)
             });
     }
     
     /**
      * The max texture size is different on different devices.
      * To make sure the texture still works on different devices the texture needs to be scaled down to an appropiate size.
      * @param gl     the {@link GL10} instance.
      * @param bitmap the bitmap to change if its size is to big.
      * @return       Scaled bitmap, if neccesary.
      */
     protected Bitmap maintainMaxTextureSize(GL10 gl, Bitmap bitmap) {
         int[] maxSize = new int[1];
         gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSize, 0); //Get max supported texture size on this device
         
         //If the width is to big and width is greater than the height
         if(bitmap.getWidth() > maxSize[0] && bitmap.getWidth() >= bitmap.getHeight()) {
             Log.w(Texture.class.getSimpleName(), "One of the loaded textures is too big. It is downscaled to respect the max texture size for this device.");
             return Bitmap.createScaledBitmap(bitmap, maxSize[0], (int) (bitmap.getHeight() * ((double) bitmap.getHeight() / maxSize[0])), true);
         }
         //If the height is to big and height is greater than the width
         else if(bitmap.getHeight() > maxSize[0] && bitmap.getHeight() >= bitmap.getWidth()) {
             Log.w(Texture.class.getSimpleName(), "One of the loaded textures is too big. It is downscaled to respect the max texture size for this device.");
             return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * ((double) bitmap.getWidth() / maxSize[0])), maxSize[0], true);
         }
         else {
             return bitmap;
         }
     }
 }
