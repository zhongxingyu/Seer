 package client.graphics.geometry;
 
 import client.EverNode;
 
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 
 public class AnimatedRotation extends AnimatedTransform
 {
 	private float aOriginRotation = 0f;
 	private float aTargetRotation = 0f;
 
 	public AnimatedRotation(final EverNode node)
 	{
 		super(node);
 	}
 
 	@Override
 	protected void getReady()
 	{
 		aOriginRotation = aRotation;
 	}
 
 	public AnimatedTransform setTargetPoint(final Vector2f point)
 	{
 		final Float angle = Geometry.getAngleTowards(point);
 		if (angle != null)
 		{
 			setTargetRotation(angle);
 		}
 		return this;
 	}
 
 	public AnimatedTransform setTargetPoint(final Vector3f point)
 	{
 		return setTargetPoint(new Vector2f(point.x, point.y));
 	}
 
 	public AnimatedTransform setTargetRotation(final float angle)
 	{
		aTargetRotation = angle % FastMath.TWO_PI;
		if (Math.abs(aTargetRotation - aRotation) > Math.abs(aTargetRotation - FastMath.TWO_PI - aRotation))
 		{
			aTargetRotation -= FastMath.TWO_PI;
 		}
		setBackContinuous(Math.abs(aTargetRotation - aRotation) / FastMath.PI);
 		return this;
 	}
 
 	@Override
 	protected void step(final float progress, final float antiProgress)
 	{
 		rotateTo(aOriginRotation * antiProgress + aTargetRotation * progress);
 	}
 }
