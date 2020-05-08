 package entity;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.util.vector.Vector3f;
 
 import render.RenderHelper;
 
 public class TrollBall extends Entity
 {
 	Vector3f velocity;
 
 	private float v;
 
 	public TrollBall()
 	{
 		super();
 	}
 
 	@Override
 	public void initEntity()
 	{
 		super.initEntity();
 		v = (float) (double) this.customValues.get("speed");
 		this.randomizeVelocity();
 	}
 
 	@Override
 	public void onTick()
 	{
 		super.onTick();
 		this.position.translate(this.velocity.x, this.velocity.y, this.velocity.z);
 
 		if (this.position.length() > (double) this.customValues.get("maxRad"))
 		{
 			this.position = new Vector3f(0, 0, 0);
 			this.randomizeVelocity();
 			this.triggerEvent("onReachMaxRad");
 		}
 	}
 	
 	public void randomizeVelocity()
 	{
 		this.velocity = new Vector3f(randomSpeed(), randomSpeed(), randomSpeed());
 		this.velocity.normalise();
 		this.velocity.scale(v);
 	}
 	
 	private float randomSpeed()
 	{
 		return (float) (Math.random() * 2) - 1;
 	}
 
 	protected void doRendering()
 	{
 		glTranslated(this.position.x, this.position.y, this.position.z);
 		glRotated(this.rotation.x, 1, 0, 0);
 		glRotated(this.rotation.y, 0, 1, 0);
 		glRotated(this.rotation.z, 0, 0, 1);
 		glColor3d(getColorAsDecimal("red"), getColorAsDecimal("green"), getColorAsDecimal("blue"));
 		RenderHelper.renderModel(this.model);
 	}
 	
 	private double getColorAsDecimal(String color)
 	{
 		return (double)((int)this.customValues.get(color)) / 256D;
 	}
 
 	public void setColor3i(int red, int blue, int green)
 	{
 		this.customValues.put("red", red);
 		this.customValues.put("green", green);
 		this.customValues.put("blue", blue);
 	}
 }
