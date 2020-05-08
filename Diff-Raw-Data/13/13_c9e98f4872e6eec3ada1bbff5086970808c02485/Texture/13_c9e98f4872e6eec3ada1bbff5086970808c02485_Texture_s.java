 package tsa2035.game.engine.texture;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.PNGDecoder;
 import org.newdawn.slick.opengl.PNGDecoder.Format;
 
 public class Texture {
 	int textureID;
 	PNGDecoder image;
 	
 	public Texture(String imgPath) throws FileNotFoundException, IOException
 	{
 		image = new PNGDecoder(new FileInputStream(imgPath));
 		textureID = GL11.glGenTextures();
 		
 		ByteBuffer buf = ByteBuffer.allocateDirect(image.getHeight()*image.getWidth()*4);
 		image.decode(buf, image.getWidth()*4, Format.RGBA);
 		buf.flip();
 		
 		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
 		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
 	}
 	
 	public Texture(InputStream img) throws IOException
 	{
 		image = new PNGDecoder(img);
 		textureID = GL11.glGenTextures();
 
 		ByteBuffer buf = ByteBuffer.allocateDirect(image.getHeight()*image.getWidth()*4);
 		image.decode(buf, image.getWidth()*4, Format.RGBA);
 		buf.flip();
 
 		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
 		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
 	}
 	
 	public void setRepeat()
 	{
 		bind();
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
 	}
 	
 	public void setStretch()
 	{
 		bind();
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
 		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
 	}
 	
 	public void bind()
 	{
 		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
 	}
 	
 	public int getWidth()
 	{
 		return image.getWidth();
 	}
 	
 	public int getHeight()
 	{
		return image.getWidth();
 	}
 }
