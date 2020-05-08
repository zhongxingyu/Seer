 
 package com.elusivehawk.engine.render;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.glu.GLU;
 import com.elusivehawk.engine.core.GameLog;
 import com.elusivehawk.engine.core.TextParser;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public final class RenderHelper
 {
 	public static final int VERTEX_SHADER_3D = loadShader(TextParser.createFile("/vertex.glsl"), GL.GL_VERTEX_SHADER);
 	public static final int FRAGMENT_SHADER_3D = loadShader(TextParser.createFile("/fragment.glsl"), GL.GL_FRAGMENT_SHADER);
 	
 	private RenderHelper(){}
 	
 	public static IntBuffer processGifFile(File gif, EnumRenderMode mode)
 	{
 		if (!isContextCurrent() || !mode.isValidImageMode())
 		{
 			return null;
 		}
 		
 		if (gif.getName().endsWith(".gif"))
 		{
 			try
 			{
 				ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
 				ImageInputStream in = ImageIO.createImageInputStream(gif);
 				reader.setInput(in, false);
 				
 				int max = reader.getNumImages(true);
 				
 				IntBuffer ret = BufferUtils.createIntBuffer(max);
 				
 				for (int c = 0; c < max; c++)
 				{
 					BufferedImage img = reader.read(c);
 					
 					ret.put(processImage(img, mode));
 					
 				}
 				
 				in.close();
 				
 				return ret;
 			}
 			catch (Exception e)
 			{
 				GameLog.error(e);
 				
 			}
 			
 		}
 		
 		return null;
 	}
 	
 	public static int processImage(BufferedImage img, int x, int y, int w, int h, EnumRenderMode mode)
 	{
 		return processImage(img.getSubimage(x, y, w, h), mode);
 	}
 	
 	public static int processImage(BufferedImage img, EnumRenderMode mode)
 	{
 		return processImage(readImage(img), img.getWidth(), img.getHeight(), mode);
 	}
 	
 	public static int processImage(ByteBuffer buf, int w, int h, EnumRenderMode mode)
 	{
 		if (!isContextCurrent() || !mode.isValidImageMode())
 		{
 			return 0;
 		}
 		
 		int glId = GL.glGenTextures();
 		
 		GL.glActiveTexture(glId);
 		GL.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
 		
 		GL.glTexParameteri(mode.getOpenGLMode(), GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
 		GL.glTexParameteri(mode.getOpenGLMode(), GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
 		
 		if (mode.is3D())
 		{
 			GL.glTexImage3D(GL.GL_TEXTURE_3D, 0, GL.GL_RGBA8, w, h, 0, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
 			
 		}
 		else
 		{
 			GL.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, w, h, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
 			
 		}
 		
 		GL.glActiveTexture(0);
 		
 		return glId;
 	}
 	
 	public static ByteBuffer readImage(BufferedImage img)
 	{
 		ByteBuffer buf = BufferUtils.createByteBuffer(img.getHeight() * img.getWidth() * 4);
 		
 		for (int x = 0; x < img.getWidth(); ++x)
 		{
 			for (int y = 0; y < img.getHeight(); ++y)
 			{
 				Color col = EnumColorFormat.RGBA.convert(new Color(EnumColorFormat.ARGB, img.getRGB(x, y)));
 				
 				col.store(buf);
 				
 			}
 			
 		}
 		
 		buf.flip();
 		
 		return buf;
 	}
 	
 	public static int loadShader(File shader, int type)
 	{
 		if (!isContextCurrent() || !shader.exists() || !shader.getName().endsWith(".glsl"))
 		{
 			return 0;
 		}
 		
 		String program = TextParser.concat(TextParser.read(shader), "\n", "", null);
 		
 		int id = GL.glCreateShader(type);
 		GL.glShaderSource(id, program);
 		GL.glCompileShader(type);
 		
 		try
 		{
 			checkForGLError();
 			
 			return id;
 		}
 		catch (Exception e)
 		{
 			GameLog.error(e);
 			
 		}
 		
 		return 0;
 	}
 	
 	@Deprecated
 	public static synchronized BufferedImage captureScreen()
 	{
 		ByteBuffer buf = BufferUtils.createByteBuffer(Display.getHeight() * Display.getWidth() * 4);
 		
 		GL.glReadPixels(0, 0, Display.getWidth(), Display.getHeight(), GL.GL_RGBA, GL.GL_BYTE, buf);
 		
 		BufferedImage ret = new BufferedImage(Display.getWidth(), Display.getHeight(), BufferedImage.TYPE_INT_ARGB);
 		
 		for (int x = 0; x < ret.getWidth(); x++)
 		{
 			for (int y = 0; y < ret.getHeight(); y++)
 			{
 				ret.setRGB(x, y, EnumColorFormat.ARGB.convert(new Color(EnumColorFormat.RGBA, buf)).getColor());
 				
 			}
 			
 		}
 		
 		return ret;
 	}
 	
 	public static int getPointCount(int gl)
 	{
 		switch (gl)
 		{
 			case GL.GL_POINTS: return 1;
 			case GL.GL_LINES: return 2;
 			case GL.GL_TRIANGLES: return 3;
 			case GL.GL_QUADS: return 4;
 			case GL.GL_TRIANGLE_FAN: return 5;
 			default: return 0;
 		}
 		
 	}
 	
 	public static void checkForGLError()
 	{
 		if (!isContextCurrent())
 		{
 			return;
 		}
 		
 		int err = GL.glGetError();
 		
 		if (err != GL.GL_NO_ERROR)
 		{
 			throw new RuntimeException("Caught OpenGL error: " + GLU.gluErrorString(err));
 			
 		}
 		
 	}
 	
 	public static FloatBuffer mixColors(Color a, Color b)
 	{
 		FloatBuffer ret = BufferUtils.createFloatBuffer(4);
 		
 		for (EnumColorFilter col : EnumColorFilter.values())
 		{
 			ret.put((a.getColorFloat(col) + b.getColorFloat(col)) % 1f);
 			
 		}
 		
 		ret.flip();
 		
 		return ret;
 	}
 	
 	public static IntBuffer createVBOs(int count)
 	{
 		if (!isContextCurrent())
 		{
 			return BufferUtils.createIntBuffer(1);
 		}
 		
 		IntBuffer ret = BufferUtils.createIntBuffer(count);
 		GL.glGenBuffers(ret);
 		ret.rewind(); //Just a safety precaution.
 		
 		return ret;
 	}
 	
 	public static boolean isContextCurrent()
 	{
 		boolean ret = false;
 		
 		try
 		{
 			ret = Display.isCurrent();
 			
 		}
 		catch (Exception e)
 		{
 			GameLog.error(e);
 			
 		}
 		
 		return ret;
 	}
 	
 	public static void makeContextCurrent()
 	{
 		if (isContextCurrent())
 		{
 			return;
 		}
 		
 		try
 		{
 			Display.makeCurrent();
 			
 		}
 		catch (LWJGLException e)
 		{
 			GameLog.error(e);
 			
 		}
 		
 	}
 	
 }
