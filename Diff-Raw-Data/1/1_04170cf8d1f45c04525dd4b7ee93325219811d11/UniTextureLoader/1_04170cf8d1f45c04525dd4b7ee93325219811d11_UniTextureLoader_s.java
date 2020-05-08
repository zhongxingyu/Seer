 package org.universeengine.opengl.texture;
 
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.StringTokenizer;
 
 import javax.imageio.ImageIO;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.BufferedImageUtil;
 import org.newdawn.slick.util.ResourceLoader;
 import org.universeengine.util.UniPrint;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
 import de.matthiasmann.twl.utils.PNGDecoder.Format;
 
 import static org.lwjgl.opengl.GL11.*;
 
 public final class UniTextureLoader {
 	
 	public static class DecodePack {
 		
 		public ByteBuffer data;
 		public int width;
 		public int height;
 		
 		public DecodePack(ByteBuffer data, int width, int height) {
 			this.data = data;
 			this.width = width;
 			this.height = height;
 		}
 	}
 	
 	public static boolean flipImages = true;
 	
 	public static UniTexture loadTexturePNG(String filepath) {
 		return loadTexturePNG(new File(filepath));
 	}
 	
 	public static UniTexture loadTexturePNG(File file) {
 		DecodePack pack = loadImageBufferPNG(file);
 		UniTexture tex = null;
 		
 		if (pack == null) {
 			throw new RuntimeException("Texture from " + file.getAbsolutePath() + " could not be loaded!");
 		}
 		
 		int width = 1;
 		int height = 1;
 		
 		while(width < pack.width) {
 			width *= 2;
 		}
 		while(height < pack.height) {
 			height *= 2;
 		}
 		
 		int id = glGenTextures();
 		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, height, width, 0, GL_RGBA, GL_UNSIGNED_BYTE, pack.data);
 		tex = new UniTexture(id, width, height);
 		
 		return tex;
 	}
 	
 	public static DecodePack loadImageBufferPNG(File file) {
 		DecodePack data = null;
 		try {
 			InputStream input = new FileInputStream(file);
 			try {
 				PNGDecoder decoder = new PNGDecoder(input);
 				
 				ByteBuffer bufData = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
 				decoder.decode(bufData, decoder.getWidth()*4, Format.RGBA);
 				bufData.flip();
 				data = new DecodePack(bufData, decoder.getWidth(), decoder.getHeight());
 			} finally {
 				input.close();
 			}
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		return data;
 	}
 
 	/**
 	 * Load a Texture from the given Filepath.
 	 * 
 	 * @param filepath
 	 *            the Filepath to load the Texture data from.
 	 * @return the UniTexture Instance.
 	 */
 	public static UniTexture loadTexture(String filepath) {
 		String format = "";
 		UniTexture tex = null;
 		StringTokenizer st = new StringTokenizer(filepath, ".");
 		while(st.hasMoreElements()) {
 			format = st.nextToken();
 		}
 		String[] strs = ImageIO.getReaderFormatNames();
 		boolean okey = false;
 		for (int i = 0; i < strs.length; i++) {
 			if (format.equalsIgnoreCase(strs[i])) {
 				okey = true;
 				break;
 			}
 		}
 		if (!okey) {
 			UniPrint.printerrf("The File format .%s for loading Texture is not supported!\n", 
 					format);
 		}
 		try {
 			Texture slickTex = TextureLoader.getTexture(format,
 					ResourceLoader.getResourceAsStream(filepath), flipImages);
 			tex = new UniTexture(slickTex.getTextureID(),
 					slickTex.getTextureWidth(), slickTex.getTextureHeight());
 		} catch (IOException e) {
 		}
 		return tex;
 	}
 
 	/**
 	 * Load a Texture from the given Filepath. Equals
 	 * loadTexture(loadBufferedImage(filepath));
 	 * 
 	 * @param filepath
 	 *            the Filepath to load the Texture data from.
 	 * @return the UniTexture Instance.
 	 */
 	public static UniTexture loadTexture(File file) {
 		String format = "";
 		UniTexture tex = null;
 		StringTokenizer st = new StringTokenizer(file.getName(), ".");
 		while(st.hasMoreElements()) {
 			format = st.nextToken();
 		}
 		String[] strs = ImageIO.getReaderFormatNames();
 		boolean okey = false;
 		for (int i = 0; i < strs.length; i++) {
 			if (format.equalsIgnoreCase(strs[i])) {
 				okey = true;
 				break;
 			}
 		}
 		if (!okey) {
 			UniPrint.printerrf("The File format .%s for loading Texture is not supported!\n", 
 					format);
 		}
 		try {
 			Texture slickTex = TextureLoader.getTexture(format, new FileInputStream(file), flipImages);
 			tex = new UniTexture(slickTex.getTextureID(),
 					slickTex.getTextureWidth(), slickTex.getTextureHeight());
 		} catch (IOException e) {
 		}
 		return tex;
 	}
 
 	public static UniTexture loadTexture(BufferedImage img) {
 		UniTexture tex = null;
 		try {
 			Texture slickTex = BufferedImageUtil.getTexture("", img);
 			tex = new UniTexture(slickTex.getTextureID(),
 					slickTex.getTextureWidth(), 
 					slickTex.getTextureHeight());
 		} catch (IOException e) {
 		}
 		return tex;
 	}
 
 	public static int uploadTexture(BufferedImage img) {
 		try {
 			return BufferedImageUtil.getTexture("", img).getTextureID();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	public static BufferedImage loadBufferedImage(String path) {
 		BufferedImage copy = null;
 		try {
 			GraphicsConfiguration gc = GraphicsEnvironment
 					.getLocalGraphicsEnvironment().getDefaultScreenDevice()
 					.getDefaultConfiguration();
 			BufferedImage buffImg = ImageIO.read(new File(path));
 			copy = gc.createCompatibleImage(buffImg.getWidth(),
 					buffImg.getHeight(), buffImg.getTransparency());
 			Graphics2D g2d = copy.createGraphics();
 			g2d.drawImage(buffImg, 0, 0, null);
 			g2d.dispose();
 		} catch (IOException e) {
 			System.err.printf("UniTextureLoader:\n"
 					+ " >> Failed to load BufferedImage from %s\n", path);
 		}
 		return copy;
 	}
 	
 }
