 package com.vloxlands.util;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBShaderObjects;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureImpl;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import com.vloxlands.render.util.ShaderLoader;
 import com.vloxlands.settings.CFG;
 import com.vloxlands.ui.IGuiElement;
 
 //import render.Model;
 //import util.math.MathHelper;
 
 public class RenderAssistant
 {
 	public static HashMap<String, Texture> textures = new HashMap<>();
 	private static HashMap<TextureRegion, Texture> textureRegions = new HashMap<>();
 	private static HashMap<String, TextureAtlas> textureAtlases = new HashMap<>();
 	private static HashMap<Integer, HashMap<String, Integer>> uniformPosition = new HashMap<>();
 	
 	// public static HashMap<String, Model> models = new HashMap<>();
 	
 	public static void storeTexture(String path)
 	{
 		Texture t = loadTexture(path);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 		textures.put(path, t);
 	}
 	
 	public static void bindTextureRegion(String path, int x, int y, int width, int height)
 	{
 		TextureRegion tr = new TextureRegion(path, x, y, width, height);
 		Texture t = textureRegions.get(tr);
 		
 		if (t != null)
 		{
 			t.bind();
 		}
 		else
 		{
 			Texture tex = tr.loadTexture();
 			textureRegions.put(tr, tex);
 			tex.bind();
 		}
 	}
 	
 	public static void storeTextureAtlas(String path, int cw, int ch)
 	{
 		if (!textureAtlases.containsKey(path)) textureAtlases.put(path, new TextureAtlas(path, cw, ch));
 		
 	}
 	
 	public static void bindTextureAtlasTile(String path, int x, int y)
 	{
 		if (!textureAtlases.containsKey(path)) throw new NoSuchElementException(" texture atlas " + path);
 		else textureAtlases.get(path).getTile(x, y).bind();
 	}
 	
 	public static void bindTexture(String path)
 	{
 		if (path == null) return;
 		
 		Texture t = textures.get(path);
 		if (t != null) t.bind();// glBindTexture(GL_TEXTURE_2D, t.getTextureID());
 		else
 		{
 			t = loadTexture(path);
 			t.bind();// glBindTexture(GL_TEXTURE_2D, t.getTextureID());
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 			textures.put(path, t);
 		}
 	}
 	
 	// /**
 	// * Returns a model. If it doesn't exist the method will try to load it
 	// * @param path The path to the model file
 	// * @return The model / null if the model could not be loaded
 	// */
 	// public static Model getModel(String path)
 	// {
 	// Model m = models.get(path);
 	// if(m == null)
 	// {
 	// models.put(path, ModelLoader.loadModel(path));
 	// m = models.get(path);
 	// }
 	// return m;
 	// }
 	
 	// /**
 	// * Renders a model at the GL-coordinate-origin. If it doesn't exist the
 	// method will try to load the model
 	// * @param path The path to the model file
 	// */
 	// public static void renderModel(String path)
 	// {
 	// getModel(path).renderModel();
 	// }
 	
 	private static Texture loadTexture(String path)
 	{
 		try
 		{
 			return TextureLoader.getTexture(".png", RenderAssistant.class.getResourceAsStream(path));
 		}
 		catch (Exception e)
 		{
 			CFG.p(path);
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static void renderRect(float posX, float posY, float sizeX, float sizeY)
 	{
 		renderRect(posX, posY, sizeX, sizeY, 1, 1);
 	}
 	
 	public static void renderRect(float posX, float posY, float sizeX, float sizeY, float texSizeX, float texSizeY)
 	{
 		renderRect(posX, posY, sizeX, sizeY, 0, 0, texSizeX, texSizeY);
 	}
 	
 	public static void renderRect(float posX, float posY, float sizeX, float sizeY, float texPosX, float texPosY, float texSizeX, float texSizeY)
 	{
 		glPushMatrix();
 		{
 			glDisable(GL_CULL_FACE);
 			
 			GL11.glBegin(GL11.GL_QUADS);
 			{
 				GL11.glTexCoord2d(texPosX, texPosY + texSizeY);
 				GL11.glVertex2f(posX, posY + sizeY);
 				
 				GL11.glTexCoord2d(texPosX + texSizeX, texPosY + texSizeY);
 				GL11.glVertex2f(posX + sizeX, posY + sizeY);
 				
 				GL11.glTexCoord2d(texPosX + texSizeX, texPosY);
 				GL11.glVertex2f(posX + sizeX, posY);
 				
 				GL11.glTexCoord2d(texPosX, texPosY);
 				GL11.glVertex2f(posX, posY);
 			}
 			GL11.glEnd();
 		}
 		glPopMatrix();
 	}
 	
 	public static void drawRect(float posX, float posY, float sizeX, float sizeY)
 	{
 		glPushMatrix();
 		{
 			glDisable(GL_CULL_FACE);
 			GL11.glBegin(GL11.GL_QUADS);
 			{
 				GL11.glVertex2f(posX, posY + sizeY);
 				
 				GL11.glVertex2f(posX + sizeX, posY + sizeY);
 				
 				GL11.glVertex2f(posX + sizeX, posY);
 				
 				GL11.glVertex2f(posX, posY);
 			}
 			GL11.glEnd();
 		}
 		glPopMatrix();
 	}
 	
 	public static void renderText(float x, float y, String text, Font f)
 	{
 		glEnable(GL_BLEND);
 		FloatBuffer fb = BufferUtils.createFloatBuffer(16);
 		glGetFloat(GL_CURRENT_COLOR, fb);
 		FontAssistant.getFont(f).drawString(x, y, text, new Color(fb));
 		glDisable(GL_BLEND);
 	}
 	
 	public static void set2DRenderMode(boolean t)
 	{
 		if (t)
 		{
 			glMatrixMode(GL_PROJECTION);
 			glPushMatrix();
 			glLoadIdentity();
 			glOrtho(0.0, Display.getWidth(), Display.getHeight(), 0.0, -1.0, 10.0);
 			glMatrixMode(GL_MODELVIEW);
 			glLoadIdentity();
 			
 			glClear(GL_DEPTH_BUFFER_BIT);
 			glDisable(GL_TEXTURE_2D);
 			glShadeModel(GL_SMOOTH);
 			glDisable(GL_DEPTH_TEST);
 		}
 		else
 		{
 			glMatrixMode(GL_PROJECTION);
 			glPopMatrix();
 			glMatrixMode(GL_MODELVIEW);
 		}
 	}
 	
 	public static void glColorHex(String hex, float alpha)
 	{
 		glColor4f(Integer.parseInt(hex.substring(0, 2), 16) / 255f, Integer.parseInt(hex.substring(2, 4), 16) / 255f, Integer.parseInt(hex.substring(4, 6), 16) / 255f, alpha);
 	}
 	
 	public static void enable(int key)
 	{
 		if (key == GL_LIGHTING)
 		{
 			int location = GL20.glGetUniformLocation(ShaderLoader.getCurrentProgram(), "lighting");
 			ARBShaderObjects.glUniform1fARB(location, 1);
 		}
 	}
 	
 	public static void disable(int key)
 	{
 		if (key == GL_LIGHTING)
 		{
 			int location = GL20.glGetUniformLocation(ShaderLoader.getCurrentProgram(), "lighting");
 			ARBShaderObjects.glUniform1fARB(location, 0);
 		}
 	}
 	
 	public static int getUniformLocation(String name)
 	{
 		CFG.p(ShaderLoader.getCurrentProgram());
 		HashMap<String, Integer> h = uniformPosition.get(ShaderLoader.getCurrentProgram());
 		if (h == null) h = new HashMap<>();
 		if (h.get(name) == null) h.put(name, GL20.glGetUniformLocation(ShaderLoader.getCurrentProgram(), name));
 		return h.get(name);
 		
 	}
 	
 	public static void setUniform1f(String name, float value)
 	{
 		ARBShaderObjects.glUniform1fARB(getUniformLocation(name), value);
 	}
 	
 	public static void setUniform2f(String name, Vector2f v)
 	{
 		setUniform2f(name, v.x, v.y);
 	}
 	
 	public static void setUniform2f(String name, float a, float b)
 	{
 		ARBShaderObjects.glUniform2fARB(getUniformLocation(name), a, b);
 	}
 	
 	public static void setUniform3f(String name, Vector3f v)
 	{
 		setUniform3f(name, v.x, v.y, v.z);
 	}
 	
 	public static void setUniform3f(String name, float a, float b, float c)
 	{
 		ARBShaderObjects.glUniform3fARB(getUniformLocation(name), a, b, c);
 	}
 	
 	public static BufferedImage toBufferedImage(Image img)
 	{
 		BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
 		image.getGraphics().drawImage(img, 0, 0, null);
 		
 		return image;
 	}
 	
 	// -- 2D GUI Helper functions -- //
 	
 	public static void renderOutline(int x, int y, int width, int height, boolean doubled)
 	{
 		glEnable(GL_BLEND);
 		bindTexture("/graphics/textures/ui/gui.png");
 		
 		int cornerSize = (doubled) ? 24 : 19;
 		int lineThickness = (doubled) ? 17 : 12;
 		int lineHeight = (doubled) ? 55 : 59;
 		int lineWidth = (doubled) ? 73 : 74;
 		
 		// x1 y1 y2 x2
 		int[] c = (doubled) ? new int[] { 856, 189, 294, 978 } : new int[] { 865, 398, 498, 982 };
 		int[] m = (doubled) ? new int[] { 893, 227, 301, 985 } : new int[] { 899, 428, 505, 989 };
 		
 		renderRect(x, y, cornerSize, cornerSize, c[0] / 1024f, c[1] / 1024f, cornerSize / 1024f, cornerSize / 1024f); // lt
 		
 		for (int i = 0; i < (width - cornerSize * 2) / lineWidth; i++)
 			renderRect(x + cornerSize + i * lineWidth, y, lineWidth, lineThickness, m[0] / 1024f, c[1] / 1024f, lineWidth / 1024f, lineThickness / 1024f); // mt
 		renderRect(x + cornerSize + (width - cornerSize * 2) / lineWidth * lineWidth, y, (width - cornerSize * 2) % lineWidth, lineThickness, m[0] / 1024.0f, c[1] / 1024.0f, ((width - cornerSize * 2) % lineWidth) / 1024.0f, lineThickness / 1024.0f);
 		
 		renderRect(x + width - cornerSize, y, cornerSize, cornerSize, c[3] / 1024f, c[1] / 1024f, cornerSize / 1024f, cornerSize / 1024f); // rt
 		
 		for (int i = 0; i < (height - cornerSize * 2) / lineHeight; i++)
 			renderRect(x, y + cornerSize + i * lineHeight, lineThickness, lineHeight, c[0] / 1024f, m[1] / 1024f, lineThickness / 1024f, lineHeight / 1024f); // ml
 		renderRect(x, y + cornerSize + (height - cornerSize * 2) / lineHeight * lineHeight, lineThickness, (height - cornerSize * 2) % lineHeight, c[0] / 1024.0f, m[1] / 1024.0f, lineThickness / 1024.0f, ((height - cornerSize * 2) % lineHeight) / 1024.0f);
 		
 		for (int i = 0; i < (height - cornerSize * 2) / lineHeight; i++)
 			renderRect(x + width - lineThickness, y + cornerSize + i * lineHeight, lineThickness, lineHeight, m[3] / 1024f, m[1] / 1024f, lineThickness / 1024f, lineHeight / 1024f); // mr
 		renderRect(x + width - lineThickness, y + cornerSize + (height - cornerSize * 2) / lineHeight * lineHeight, lineThickness, (height - cornerSize * 2) % lineHeight, m[3] / 1024.0f, m[1] / 1024.0f, lineThickness / 1024.0f, ((height - cornerSize * 2) % lineHeight) / 1024.0f);
 		
 		renderRect(x, y + height - cornerSize, cornerSize, cornerSize, c[0] / 1024f, c[2] / 1024f, cornerSize / 1024f, cornerSize / 1024f); // lb
 		
 		for (int i = 0; i < (width - cornerSize * 2) / lineWidth; i++)
 			renderRect(x + cornerSize + i * lineWidth, y + height - lineThickness, lineWidth, lineThickness, m[0] / 1024f, m[2] / 1024f, lineWidth / 1024f, lineThickness / 1024f); // mb
 		renderRect(x + cornerSize + (width - cornerSize * 2) / lineWidth * lineWidth, y + height - lineThickness, (width - cornerSize * 2) % lineWidth, lineThickness, m[0] / 1024.0f, m[2] / 1024.0f, ((width - cornerSize * 2) % lineWidth) / 1024.0f, lineThickness / 1024.0f);
 		
 		renderRect(x + width - cornerSize, y + height - cornerSize, cornerSize, cornerSize, c[3] / 1024f, c[2] / 1024f, cornerSize / 1024f, cornerSize / 1024f); // rb
 		
 		glDisable(GL_BLEND);
 	}
 	
 	public static void renderContainer(int x, int y, int width, int height, boolean doubled)
 	{
 		glPushMatrix();
 		{
 			glEnable(GL_BLEND);
 			glColor4f(IGuiElement.gray.x, IGuiElement.gray.y, IGuiElement.gray.z, IGuiElement.gray.w);
 			TextureImpl.bindNone();
 			RenderAssistant.renderRect(x + 5, y + 5, width - 10, height - 10);
 			glColor4f(1, 1, 1, 1);
 			
 			bindTexture("/graphics/textures/ui/gui.png");
 			RenderAssistant.renderOutline(x, y, width, height, doubled);
 			glDisable(GL_BLEND);
 		}
 		glPopMatrix();
 	}
 	
 	public static void renderLine(int x, int y, int length, boolean horizontal, boolean doubled)
 	{
 		glEnable(GL_BLEND);
 		bindTexture("/graphics/textures/ui/gui.png");
 		
 		int height = (doubled) ? 17 : 12;
 		int width = length;
 		int lineLength = (doubled) ? 73 : 74;
 		// if (!horizontal)
 		// {
 		// width = height;
 		// height = length;
 		// }
 		
 		// x1 y1 x2
 		// x3 y2 x4
 		int[] c = (doubled) ? new int[] { 849, 155, 897 } : new int[] { 845, 363, 887 };
 		
 		// x1 y1
 		// x2 y2
 		int[] m = (doubled) ? new int[] { 893, 189 } : new int[] { 899, 398 };
 		
 		if (!horizontal)
 		{
 			glTranslatef(x, y, 0);
 			glRotatef(-90, 0, 0, 1);
 			glTranslatef(-x, -y, 0);
 			glTranslatef(-width, -height, 0);
 		}
 		renderRect(x, y, 15, height, c[0] / 1024f, c[1] / 1024f, 15 / 1024f, height / 1024f);
 		
 		for (int i = 0; i < (width - 15) / lineLength; i++)
 			renderRect(x + i * lineLength + 15, y, lineLength, height, m[0] / 1024f, m[1] / 1024f, lineLength / 1024f, height / 1024f);
 		renderRect(x + 15 + ((width - 30) / lineLength * lineLength), y, (width - 30) % lineLength, height, m[0] / 1024f, m[1] / 1024f, ((width - 30) % lineLength) / 1024f, height / 1024f);
 		
 		renderRect(x + width - 15, y, 15, height, c[2] / 1024f, c[1] / 1024f, 15 / 1024f, height / 1024f);
 		if (!horizontal)
 		{
 			glTranslatef(width, height, 0);
 			glTranslatef(x, y, 0);
 			glRotatef(90, 0, 0, 1);
 			glTranslatef(-x, -y, 0);
 		}
 		glDisable(GL_BLEND);
 	}
 	
 	public static void renderShadow(int x, int y, int width, int height, float rotInDegrees)
 	{
 		glEnable(GL_BLEND);
 		bindTexture("/graphics/textures/ui/gui.png");
 		glDisable(GL_BLEND);
 	}
 	
 	public static String[] wrap(String raw, Font f, int width)
 	{
 		String[] words = raw.split(" ");
 		ArrayList<String> lines = new ArrayList<>();
 		int wordIndex = 0;
 		String line = "";
 		TrueTypeFont ttf = FontAssistant.getFont(f);
 		
 		while (wordIndex < words.length)
 		{
 			if (ttf.getWidth(line + " " + words[wordIndex]) <= width)
 			{
 				line += " " + words[wordIndex];
 				wordIndex++;
 			}
 			else
 			{
 				
 				lines.add(line);
 				line = "";
 			}
 		}
 		lines.add(line);
 		
 		ArrayList<String> realLines = new ArrayList<>();
 		for (String s : lines)
 		{
 			String[] nls = s.split("\n");
 			for (String nl : nls)
 				realLines.add(nl);
 		}
 		
 		return realLines.toArray(new String[] {});
 	}
 }
