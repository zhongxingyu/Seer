 package GameComponents;
 
 import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ComponentColorModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 
 import GameObjects.GameChar;
 import Maths.*;
 
 import javax.imageio.ImageIO;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 
 import com.jogamp.common.nio.Buffers;
 
 public class ObjectRenderer 
 {
 	public Vector2[] objectUVs;
 	public Shape shape;
 
 	private GameChar parent;
 	private boolean textureSet = false;
 	private IntBuffer textureID = Buffers.newDirectIntBuffer(1);
 
 	public ObjectRenderer(GameChar parent)
 	{
 		objectUVs = new Vector2[0];
 		this.parent = parent;
 	}
 
 	public boolean Draw(GL2 gl)
 	{
 		if(textureSet)
 		{
 			gl.glEnable( GL.GL_TEXTURE_2D );
 			gl.glBindTexture( GL.GL_TEXTURE_2D, textureID.get(0) );
 		}
 		gl.glBegin(GL2.GL_QUADS);
 		for(int i = 0; i<shape.vertices.length; i++)
 		{
 			Vector2 vertexToDraw = Matrix3.Multiply(parent.transform.transformMatrix,new Vector3(shape.vertices[i],1f)).GetVec2();
 			Vector2 color = vertexToDraw.Normalized();
 			Vector2 UVs = shape.UVs[i];
 			if(textureSet)
 			{
 				gl.glTexCoord2f(UVs.x, UVs.y);
 			}
 			else
 			{
 				gl.glColor3f(color.x,color.y,1);
 			}
 			gl.glVertex2f(vertexToDraw.x,vertexToDraw.y);
 		}
 		gl.glEnd();
 		if(textureSet)
 		{
 			gl.glDisable(GL.GL_TEXTURE_2D);
 		}
 		return true;
 	}
 
 	public void SetTexture(GL2 gl, String filePath)
 	{
 		//first we create a BufferedImage object reference
 		BufferedImage bufferedImage = null;
 		//initalizing width and height of image
 		int w = 0;
 		int h = 0;
 		try
 		{
 			//Creating BufferedImage by reading it from file
 			bufferedImage = ImageIO.read(new File(filePath));
 			//Setting width and height of image equal to BufferedImage width and height
 			w = bufferedImage.getWidth();
 			h = bufferedImage.getHeight();
 		} 
 		catch (IOException e) 
 		{
 
 			System.out.println("FUCK I CANT READ YOUR TEXTURE FILE: " + filePath);
 			return;
 
 		}
 		//Creating a writable raster on which we write BufferedImage
 		WritableRaster raster = 
 				Raster.createInterleavedRaster (DataBuffer.TYPE_BYTE,
 						w,
 						h,
 						4,
 						null);
 		//Making ComponenetColorModel for the BufferedImage
 		ComponentColorModel colorModel=
 				new ComponentColorModel (ColorSpace.getInstance(ColorSpace.CS_sRGB),
 						new int[] {8,8,8,8},
 						true,
 						false,
 						ComponentColorModel.TRANSLUCENT,
 						DataBuffer.TYPE_BYTE);
 		//Creating BufferedImage from the given WritableRaster and ComponentColorModel			
 		BufferedImage dukeImg = 
 				new BufferedImage (colorModel,
 						raster,
 						false,
 						null);
 
 		//Creating graphics object on which to draw image
 		Graphics2D g = dukeImg.createGraphics();
 		//Draw original image on new image graphics object 
 		g.drawImage(bufferedImage, null, null);
 		//Retriving DataBufferByte which is backing this WritableRaster Object
 		DataBufferByte dukeBuf =
 				(DataBufferByte)raster.getDataBuffer();
 		//Storing data of DataBufferByte in byte array
 		byte[] dukeRGBA = dukeBuf.getData();
 		//Wrapping this byte array in ByteBuffer object
 		ByteBuffer bb = ByteBuffer.wrap(dukeRGBA);
 		//Marking position to start for reading this ByteBuffer
 		bb.position(0);
 		bb.mark();
 		
 		gl.glGenTextures(1, textureID);
 		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID.get(0));
 		gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
 		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
 		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
 		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
 		gl.glTexImage2D (GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA, w, h, 0, GL2.GL_RGBA, 
 				GL.GL_UNSIGNED_BYTE, bb);
 		
 		textureSet = true;
 		
 	}
 
 	public enum Shape
 	{
 		Square
 		( 
 				new Vector2[]{ new Vector2(-10f,-10f), new Vector2(10f,-10f),
 				new Vector2(10f,10f), new Vector2(-10f,10f) },
 				
 				new Vector2[]{ new Vector2(0,1), new Vector2(1,1),
 				new Vector2(1,0), new Vector2(0,0) } 
 		);
 
 		Vector2[] vertices;
 		Vector2[] UVs;
 
 		Shape(Vector2[] vertices, Vector2[] UVs)
 		{
 			this.vertices = vertices;
 			this.UVs = UVs;
 		}
 
 	}
 
 }
