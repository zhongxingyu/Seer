 package com.evervoid.client.graphics.materials;
 
 import com.evervoid.client.graphics.GraphicManager;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class AlphaTextured extends BaseMaterial
 {
 	private final BaseTexture aTexture;
 	private final String aTextureFile;
 
 	public AlphaTextured(final String texture) throws TextureException
 	{
 		super("AlphaTextured");
 		aTextureFile = texture;
 		setTransparent(true);
 		setFloat("HueMultiplier", 1.7f);
 		setFloat("AlphaMultiplier", 1f);
 		aTexture = GraphicManager.getTexture(texture);
 		setTexture("ColorMap", aTexture.getTexture());
 		getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
 	}
 
 	public Vector2f getDimensions()
 	{
 		return aTexture.getDimension();
 	}
 
 	public float getHeight()
 	{
 		return aTexture.getHeight();
 	}
 
 	public float getWidth()
 	{
 		return aTexture.getWidth();
 	}
 
 	public void setAlpha(final float alpha)
 	{
 		setBoolean("UseAlphaMultiplier", true);
 		setFloat("AlphaMultiplier", alpha);
 	}
 
 	public void setHue(final ColorRGBA hue)
 	{
 		if (hue == null) {
 			setBoolean("UseHueColor", false);
 		}
 		else {
 			setBoolean("UseHueColor", true);
 			setColor("HueColor", hue);
 		}
 	}
 
 	public void setHue(final ColorRGBA hue, final float multiplier)
 	{
 		setHue(hue);
 		setHueMultiplier(multiplier);
 	}
 
 	public void setHueMultiplier(final float multiplier)
 	{
 		setFloat("HueMultiplier", multiplier);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "AlphaTextured(" + aTextureFile + ")";
 	}
 }
