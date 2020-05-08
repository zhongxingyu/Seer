 package com.vloxlands.util;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Font;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.lwjgl.opengl.ARBShaderObjects;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import com.vloxlands.render.util.ShaderLoader;
 import com.vloxlands.settings.CFG;
 
 //import render.Model;
 //import util.math.MathHelper;
 
 public class RenderAssistant
 {
 	private static HashMap<String, Texture> textures = new HashMap<String, Texture>();
 	
	private static HashMap<Integer, HashMap<String, Integer>> uniformPosition = new HashMap<>();
 	
 	// public static HashMap<String, Model> models = new HashMap<>();
 	
 	public static boolean bindTexture(String path)
 	{
 		Texture t = textures.get(path);
 		if (t != null)
 		{
 			t.bind();
 			return true;
 		}
 		else
 		{
 			t = loadTexture(path);
 			t.bind();
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
 			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 			textures.put(path, t);
 			return true;
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
 			return TextureLoader.getTexture(".png", new FileInputStream(new File(path)));
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
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
 				GL11.glVertex2f(posX, posY);
 				
 				GL11.glTexCoord2d(texPosX + texSizeX, texPosY + texSizeY);
 				GL11.glVertex2f(posX + sizeX, posY);
 				
 				GL11.glTexCoord2d(texPosX + texSizeX, texPosY);
 				GL11.glVertex2f(posX + sizeX, posY + sizeY);
 				
 				GL11.glTexCoord2d(texPosX, texPosY);
 				GL11.glVertex2f(posX, posY + sizeY);
 			}
 			GL11.glEnd();
 		}
 		glPopMatrix();
 		
 	}
 	
 	public static void renderText(float x, float y, String text, Color color, Font f)
 	{
 		glEnable(GL_BLEND);
 		glEnable(GL_TEXTURE_2D);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		FontAssistant.getFont(f).drawString(x, y, text, color);
 		glDisable(GL_BLEND);
 		glDisable(GL_TEXTURE_2D);
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
 		HashMap<String, Integer> h = uniformPosition
 				.get((Integer)ShaderLoader
 						.getCurrentProgram());
 		if(h == null) h = new HashMap<>();
 		if(h.get(name) == null) h.put(name, GL20.glGetUniformLocation(ShaderLoader.getCurrentProgram(), name));
 		return (int) h.get(name);
 		
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
 }
