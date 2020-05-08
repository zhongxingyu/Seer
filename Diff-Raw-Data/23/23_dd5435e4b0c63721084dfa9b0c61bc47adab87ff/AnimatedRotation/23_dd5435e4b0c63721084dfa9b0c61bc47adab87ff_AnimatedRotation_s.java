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
		float target = angle % FastMath.TWO_PI;
		if (Math.abs(target - aRotation) > Math.abs(target - FastMath.TWO_PI - aRotation))
 		{
			target -= FastMath.TWO_PI;
 		}
		if (Math.abs(target - aRotation) / FastMath.PI > 0)
		{
			System.out.println("Current rotation: " + aRotation + "; Target: " + target + "; Measure: "
					+ Math.abs(target - aRotation) / FastMath.PI);
		}
		setBackContinuous(Math.abs(target - aRotation) / FastMath.PI);
		aTargetRotation = target;
 		return this;
 	}
 
 	@Override
 	protected void step(final float progress, final float antiProgress)
 	{
 		rotateTo(aOriginRotation * antiProgress + aTargetRotation * progress);
 	}
 }
