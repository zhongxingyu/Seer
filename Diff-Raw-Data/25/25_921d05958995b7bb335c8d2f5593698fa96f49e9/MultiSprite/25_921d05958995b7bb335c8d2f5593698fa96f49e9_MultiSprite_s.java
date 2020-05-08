 package com.evervoid.client.graphics;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.evervoid.client.EverNode;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.gamedata.SpriteInfo;
 import com.jme3.math.Vector2f;
 
 public class MultiSprite extends EverNode implements Sizeable
 {
 	private int aNumberOfSprites = 0;
 	protected final Map<EverNode, Transform> aSpriteTransforms = new HashMap<EverNode, Transform>();
 	protected Vector2f aTotalSize = new Vector2f(0, 0);
 
 	public MultiSprite()
 	{
 		// Nothing
 	}
 
 	public MultiSprite(final String image)
 	{
 		this();
 		addSprite(image);
 	}
 
 	public EverNode addSprite(final EverNode image)
 	{
 		return addSprite(image, 0, 0);
 	}
 
 	public EverNode addSprite(final EverNode sprite, final float x, final float y)
 	{
 		addNode(sprite);
 		final Transform spriteTransform = sprite.getNewTransform().move(x, y, Sprite.getNewZDepth());
 		aSpriteTransforms.put(sprite, spriteTransform);
 		recomputeTotalSize();
 		return sprite;
 	}
 
 	public EverNode addSprite(final SpriteInfo sprite)
 	{
		return addSprite(sprite.sprite, sprite.x, sprite.y);
 	}
 
 	public EverNode addSprite(final String image)
 	{
		return addSprite(image, 0, 0);
 	}
 
	public EverNode addSprite(final String sprite, final float x, final float y)
 	{
		return addSprite(new Sprite(sprite), x, y);
 	}
 
 	@Override
 	public Vector2f getDimensions()
 	{
 		return aTotalSize;
 	}
 
 	@Override
 	public float getHeight()
 	{
 		return aTotalSize.y;
 	}
 
 	public int getNumberOfFrames()
 	{
 		return aNumberOfSprites;
 	}
 
 	@Override
 	public float getWidth()
 	{
 		return aTotalSize.x;
 	}
 
 	protected void recomputeTotalSize()
 	{
 		final Vector2f min = new Vector2f();
 		final Vector2f max = new Vector2f();
 		aNumberOfSprites = 0;
 		for (final EverNode n : aSubnodes) {
 			if (n instanceof Sizeable) {
 				final Sizeable size = (Sizeable) n;
 				final Vector2f offset = aSpriteTransforms.get(n).getTranslation2f();
 				min.set(Math.min(min.x, offset.x), Math.min(min.y, offset.y));
 				max.set(Math.min(max.x, size.getWidth() + offset.x), Math.min(max.y, size.getHeight() + offset.y));
 				aNumberOfSprites++;
 			}
 		}
 		aTotalSize.set(max.x - min.x, max.y - min.y);
 		if (aParent != null) {
 			if (aParent instanceof MultiSprite) {
 				// Notify parent that size changed, make it recompute its size
 				((MultiSprite) aParent).recomputeTotalSize();
 			}
 		}
 	}
 }
