 package dkilian.andy;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import dkilian.andy.jni.agl;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.opengl.GLUtils;
 
 /**
  * Wraps an OpenGL textured quad (using the AGL native wrapper library)
  * as a standard Andy sprite
  * @author dkilian
  */
 public class TexturedQuad implements Sprite
 {
 	/** A handle to this quad's texture in graphics memory. Used for rendering */
 	private int _texture;
 	/** The width of this sprite's texture in texels */
 	private int _width;
 	/** The height of this sprite's texture in texels */
 	private int _height;
 	/** A matrix describing the scales, rotations, and translations performed on this sprite */
 	private Matrix _transform;
 	
 	/**
 	 * Creates a new textured quad from an existing texture
 	 * @param tex A handle to this quad's texture in graphics memory
 	 * @param w The width of this sprite's texture in texels
 	 * @param h The height of this sprite's texture in texels
 	 */
 	public TexturedQuad(int tex, int w, int h)
 	{
 		_texture = tex;
 		_width = w;
 		_height = h;
 	}
 	
 	/**
 	 * Creates a new textured quad containg a copy of the data in the specified bitmap
 	 * @param b The bitmap containing the color data to copy to the texture
 	 */
 	public TexturedQuad(Bitmap b)
 	{
 		_width = b.getWidth();
 		_height = b.getHeight();
 		
 		_texture = agl.CreateTexture(_width, _height);
 		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, b, 0);
 	}
 
 	/** Gets this sprite's transformation matrix */
 	@Override
 	public Matrix getTransform() 
 	{
 		return _transform;
 	}
 
 	/** Sets this sprite's transformation matrix */
 	@Override
 	public void setTransform(Matrix m) 
 	{
 		_transform = m;
 	}
 
 	/** Gets the width of this sprite, in texels */
 	@Override
 	public int getWidth() 
 	{
 		return _width;
 	}
 
 	/** Gets the height of this sprite, in texels */
 	@Override
 	public int getHeight() 
 	{
 		return _height;
 	}
 
 	/** Not currently supported. It's possible but I'm lazy */
 	@Override
 	public boolean getClipRectEnabled() { return false; }
 
 	/** Not currently supported. It's possible but I'm lazy */
 	@Override
 	public void setClipRectEnabled(boolean b) {}
 
 	/** Not currently supported. It's possible but I'm lazy */
 	@Override
 	public Rect getClipRect() { return null; }
 
 	/** Not currently supported. It's possible but I'm lazy */
 	@Override
 	public void setClipRect(Rect r) {}
 
 	/** Renders this sprite */
 	@Override
 	public void draw(Kernel kernel) 
 	{
 		// TODO: overload of draw that takes the 3x3 transformation matrix
 		//       and how properly 'upgrade' it to a 4x4 matrix? (is such an
 		//		 operation even possible)
 		//		 is the 3x3 matrix homogenous for 2D coords? So I could sorta
 		//		 separate it out like:
 		//		 A  B  C
 		//       D  E  F
 		//       G  H  I
 		//			=>
 		//		 A  B  0  C
 		//		 D  E  0  F
 		//       0  0  1  0 
 		//       G  H  0  1
 		//		It's symmetric ... I have no iea if it's right. I assume the
 		//		rightmost row (or column, idk the orientation, the one that
 		//		doesn't contain translation) is always zero.
 		//		Augh wait the agl implementation is expecting 4x4 matrices.
 		//		I guess hack up the AGL impl?
		//
		//      Actually fuck it, just make sprites translate/rotate/scale and do the transform
		//      in C before sending to GPU
 	}
 }
