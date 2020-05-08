 package engine.render;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL13.*;
 import static org.lwjgl.opengl.GL15.*;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
 import de.matthiasmann.twl.utils.PNGDecoder.Format;
 
 public class TextRender {
 	
 	/**
 	 * Singleton instance to render text on VBO Level
 	 */
 	private static TextRender textRender = new TextRender();
 	
 	/**
 	 * Array to keep the vertexStuff in
 	 */
 	private int[] glTextureCoordId;
 	
 	/**
 	 * Texture id for the font map
 	 */
 	private int glTextureId;
 	
 	/**
 	 * Vertex id for the box
 	 */
 	private int glVertexId;
 	
 	private TextRender() {
 		glTextureCoordId = new int[128];
 		init();
 	}
 	
 	/**
 	 * Loads the font.png file and prepares it for useage and registers a basic box for the images
 	 */
 	private void init() {
 		try {
 			//Load Texture
 			InputStream in = TextRender.class.getResourceAsStream("/res/font.png");
 			PNGDecoder decoder = new PNGDecoder(in);
 	        ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
 	        decoder.decode(buffer, decoder.getWidth() * 4, Format.RGBA);
 	        buffer.flip();
 	        in.close();
 	        
 	        //Register Texture
 	        glActiveTexture(GL_TEXTURE0);
 	        glTextureId = glGenTextures();
 	        glBindTexture(GL_TEXTURE_2D, glTextureId);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
 			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
 			glBindTexture(GL_TEXTURE_2D, 0);
 			
 			//Register Basic Box
 			glVertexId = glGenBuffers();
 			
 			FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(12);
 			
 			final float width = 9F;
 			final float height = 13F;
 			
 			vertexBuffer.put(new float[] {0, height, 0, width, height, 0, width, 0, 0, 0, 0, 0});
 			vertexBuffer.flip();
 			
 			glBindBuffer(GL_ARRAY_BUFFER, glVertexId);
 			glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
 			glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Calculates the texture coords and saves those.
 	 * @param charStart first character to load
 	 * @param charEnd last character to load
 	 */
 	public void load(int charStart, int charEnd) {
 		for(int i = charStart; i <= charEnd; i++) {
 			if(glTextureCoordId[i] != GL_NONE) {
 				System.out.println("Skipping: " + i + ", Already Loaded");
 			}
 			glTextureCoordId[i] = glGenBuffers();
 			
			int xOffset = i % 13;
			int yOffset = i / 13;
 			
			float xSpacing = 0.078F;
 			float ySpacing = 1F / 16F;
 			
 			float x = xOffset * xSpacing;
 			float y = yOffset * ySpacing;
 			
 			float xMax = x + xSpacing;
 			if(xMax > 1F)
 				xMax = 1F;
 			float yMax = y + ySpacing;
 			if(yMax > 1F)
 				yMax = 1F;
 			
 			System.out.println(i + " " + (char)i + " " + xOffset + " " + yOffset + " (" + x + ", " + y + ") (" + xMax + ", " + yMax + ")");
 			
 			FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(8); //2 (x, y) * 4
 			textureBuffer.put(new float[] { x, yMax, xMax, yMax, xMax, y, x, y });
 			textureBuffer.flip();
 			
 			glBindBuffer(GL_ARRAY_BUFFER, glTextureCoordId[i]);
 			glBufferData(GL_ARRAY_BUFFER, textureBuffer, GL_STATIC_DRAW);
 			glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
 		}
 	}
 	
 	/**
 	 * Draws the text and will recalculate the x to make it centered
 	 * if glColorId param is 0 (GL_NONE) it wont use any color. Therefore it will be white
 	 * @param x
 	 * @param y
 	 * @param text
 	 * @param glColorId
 	 */
 	public void drawCentered(float x, float y, String text, int glColorId) {
 		x -= (text.length() * 4.5F);
 		draw(x, y, text, glColorId);
 	}
 	
 	/**
 	 * Draws the text
 	 * if glColorId param is 0 (GL_NONE) it wont use any color. therefore it will be white
 	 * @param x
 	 * @param y
 	 * @param text
 	 * @param glColorId
 	 */
 	public void draw(float x, float y, String text, int glColorId) {
 		glEnable(GL_TEXTURE_2D);
 		if(glColorId != GL_NONE)
 			glEnableClientState(GL_COLOR_ARRAY);
 		glEnableClientState(GL_VERTEX_ARRAY);
 		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
 		glActiveTexture(GL_TEXTURE0);
 		glBindTexture(GL_TEXTURE_2D, glTextureId);
 		//Draw
 		for(int i = 0; i < text.length(); i++) {
 			glPushMatrix();
 			glTranslatef(x + (i * 9F), y, 0F);
 			char c = text.charAt(i);
 			
 			if(glTextureCoordId[c] == GL_NONE) {
 				System.out.println(c + " is unloaded!");
 				System.exit(1);
 			}
 			
 			glBindBuffer(GL_ARRAY_BUFFER, glVertexId);
 			glVertexPointer(3, GL_FLOAT, 0, 0L);
 			if(glColorId != GL_NONE) {
 				glBindBuffer(GL_ARRAY_BUFFER, glColorId);
 				glColorPointer(3, GL_FLOAT, 0, 0L);
 			}
 			glBindBuffer(GL_ARRAY_BUFFER, glTextureCoordId[c]);
 			glTexCoordPointer(2, GL_FLOAT, 0, 0L);
 			
 			glDrawArrays(GL_QUADS, 0, 4);
 			glPopMatrix();
 		}
 		
 		glBindTexture(GL_TEXTURE_2D, GL_NONE);
 		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
 		glDisableClientState(GL_VERTEX_ARRAY);
 		if(glColorId != GL_NONE)
 			glDisableClientState(GL_COLOR_ARRAY);
 		
 		glDisable(GL_TEXTURE_2D);
 	}
 	
 	/**
 	 * Gets the singleton instance of TextRender
 	 * @return
 	 */
 	public static TextRender getTextRender() {
 		return textRender;
 	}
 
 }
