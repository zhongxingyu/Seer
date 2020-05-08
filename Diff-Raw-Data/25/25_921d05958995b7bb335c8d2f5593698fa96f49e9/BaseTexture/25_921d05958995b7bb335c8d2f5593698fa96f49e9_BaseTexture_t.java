 package com.evervoid.client.graphics.materials;
 
 import com.jme3.math.Vector2f;
 import com.jme3.texture.Image;
 import com.jme3.texture.Texture.MagFilter;
 import com.jme3.texture.Texture.MinFilter;
 import com.jme3.texture.Texture2D;
 
 public class BaseTexture
 {
 	private Vector2f aDimension;
 	private float aHorizontalPortion = 1f;
 	private final Vector2f aImageSize;
 	private final Texture2D aTexture;
 	private float aVerticalPortion = 1f;
 
 	public BaseTexture(final Texture2D texture) throws TextureException
 	{
 		if (texture == null) {
 			throw new TextureException();
 		}
 		aTexture = texture;
 		final Image img = aTexture.getImage();
 		aImageSize = new Vector2f(img.getWidth(), img.getHeight());
 		computeDimension();
 	}
 
 	private void computeDimension()
 	{
 		aDimension = new Vector2f(aImageSize.x * aHorizontalPortion, aImageSize.y * aVerticalPortion);
 	}
 
 	public Vector2f getDimension()
 	{
 		return aDimension;
 	}
 
 	public float getHeight()
 	{
 		return aDimension.y;
 	}
 
 	public float getHorizontalPortion()
 	{
 		return aHorizontalPortion;
 	}
 
 	public Texture2D getTexture()
 	{
 		return aTexture;
 	}
 
 	public float getVerticalPortion()
 	{
 		return aVerticalPortion;
 	}
 
 	public float getWidth()
 	{
		return aDimension.x;
 	}
 
 	public BaseTexture setPortion(final float horizontal, final float vertical)
 	{
 		aHorizontalPortion = horizontal;
 		aVerticalPortion = vertical;
 		computeDimension();
 		return this;
 	}
 
 	public void setSpriteFilters()
 	{
 		aTexture.setMagFilter(MagFilter.Nearest);
 		aTexture.setMinFilter(MinFilter.Trilinear);
 	}
 }
