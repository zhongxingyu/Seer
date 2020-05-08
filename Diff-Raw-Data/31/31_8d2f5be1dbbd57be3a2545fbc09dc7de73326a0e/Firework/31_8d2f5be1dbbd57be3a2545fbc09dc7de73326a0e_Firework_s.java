 package com.slsw.fiarworks.firework;
 import java.util.ArrayList;
 
 
 public class Firework
 {
 	private ArrayList<Spark> sparks;
 	public Firework()
 	{
 		sparks = new ArrayList<Spark>();
 		Vec3 start_pos = new Vec3(0.0f, 0.0f, 0.0f);
 		Vec3 start_vel = new Vec3(0.0f, 0.0f, 0.01f);
 		Spark start_spark = new Spark(start_pos, start_vel, 0);
 		sparks.add(start_spark);
 	}
 	public void update()
 	{
 		ArrayList<Spark> new_sparks = new ArrayList<Spark>();
 		for(int i = 0; i < sparks.size(); i++)
 		{
 			new_sparks.addAll(sparks.get(i).update());
 		}
 		sparks = new_sparks;
 
 		return;
 	}
 	//returns false if animation is over
 	public boolean draw(MatrixStack stackMV, float[] projection)
 	{
 		float[] draw_buffer = new float[3*3*2*8*sparks.size()];
 
 		if(sparks.size() == 0)
 		{
 			return false;
 		}
 
 		for(int i = 0; i < sparks.size(); i++)
 		{
 			Quad q = new Quad(sparks.get(i));
 			q.set_quad(draw_buffer, i*3*3*2*8);
 		}
 
 		Model m = new Model(draw_buffer);
 		GLModel glm = new GLModel(m);
 
 		glm.draw(stackMV, projection);
 
 		return true;
 	}
 }
 
