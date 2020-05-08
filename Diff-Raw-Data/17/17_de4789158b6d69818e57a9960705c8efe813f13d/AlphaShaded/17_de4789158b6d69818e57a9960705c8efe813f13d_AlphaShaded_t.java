 package com.evervoid.client.graphics.materials;
 
 import com.evervoid.client.graphics.GraphicManager;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 
 public class AlphaShaded extends BaseMaterial
 {
 	private BaseTexture aTexture;
 
 	public AlphaShaded(final String texture) throws TextureException
 	{
 		super("AlphaShaded");
 		setTransparent(true);
 		setColor("ShadeColor", ColorRGBA.Black);
 		// Try to see if there is a shadow map
 		try {
 			aTexture = GraphicManager.getTexture(texture.replace(".png", ".shadow.png"));
 			setBoolean("IsShadowMap", true);
 		}
 		catch (final TextureException e) {
 			aTexture = GraphicManager.getTexture(texture);
 			setBoolean("IsShadowMap", false);
 		}
 		setTexture("AlphaMap", aTexture.getTexture());
 		setFloat("ShadowMapMultiplier", 1.0f);
		setFloat("TexturePortionX", aTexture.getHorizontalPortion());
		setFloat("TexturePortionY", aTexture.getVerticalPortion());
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
 
 	public void setGradientPortion(final float gradientPortion)
 	{
 		setFloat("ShadeGradientPortion", gradientPortion);
 	}
 
 	public void setShadeAngle(final float shadeAngle)
 	{
 		// Negate for convenience
 		// Fragment shader requires angle where the shade actually is
 		// In the engine, it's easier to get the angle where the light is instead
 		setFloat("ShadeAngle", (-shadeAngle) % FastMath.TWO_PI);
 	}
 
 	public void setShadeColor(final ColorRGBA glowColor)
 	{
 		setColor("ShadeColor", glowColor);
 	}
 
 	public void setShadePortion(final float shadePortion)
 	{
 		setFloat("ShadePortion", shadePortion);
 	}
 
 	public void setShadowMapMultiplier(final float multiplier)
 	{
 		setFloat("ShadowMapMultiplier", multiplier);
 	}
 }
