 
 package com.elusivehawk.engine.render;
 
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.lwjgl.BufferUtils;
 import com.elusivehawk.engine.math.Vector3f;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class ParticleScene implements ILogicalRender
 {
 	public static final int PARTICLE_FLOAT_COUNT = 7;
 	
 	protected final FloatBuffer buf;
 	protected final List<IParticle> particles = new ArrayList<IParticle>();
 	protected final VertexBufferObject vbo;
 	protected final int particleCount;
 	protected final GLProgram p;
 	
 	public ParticleScene(int maxParticles)
 	{
 		buf = BufferUtils.createFloatBuffer(maxParticles * PARTICLE_FLOAT_COUNT);
 		particleCount = maxParticles;
 		
 		p = new GLProgram(); //TODO Create default particle shaders.
 		vbo = new VertexBufferObject(GL.GL_ARRAY_BUFFER, buf, GL.GL_STREAM_DRAW);
 		
 		if (p.bind())
 		{
 			p.attachVertexAttribs(new String[]{"in_pos", "in_col"}, new int[]{0, 1}, false);
 			
 			p.attachVBO(vbo, Arrays.asList(0, 1));
 			
 			GL.glVertexAttribPointer(0, 3, false, 0, buf);
 			GL.glVertexAttribPointer(1, 4, false, 3, buf);
 			
 			p.unbind();
 			
 		}
 		else
 		{
 			throw new RuntimeException("Could not create particle scene, due to a program binding failure.");
 			
 		}
 		
 	}
 	
 	public void spawnParticle(IParticle p)
 	{
 		if (this.particles.size() >= this.particleCount)
 		{
 			return;
 		}
 		
 		this.buf.position(this.particles.size() * PARTICLE_FLOAT_COUNT);
 		new Vector3f(p.getPosition()).store(this.buf);
 		EnumColorFormat.RGBA.convert(p.getColor()).store(this.buf);
 		
 		this.particles.add(p);
 		
 	}
 	
 	public int getParticleCount()
 	{
 		return this.particles.size();
 	}
 	
 	@Override
 	public boolean updateBeforeUse(IRenderHUB hub)
 	{
 		for (int c = 0; c < this.particles.size(); c++)
 		{
 			IParticle p = this.particles.get(c);
 			
 			p.updateParticle();
 			
 			if (p.flaggedForRemoval())
 			{
 				this.particles.remove(c);
 				
 				//TODO Fix
 				
 				continue;
 			}
 			
 			if (p.updatePositionOrColor())
 			{
 				Vector3f vec = new Vector3f(p.getPosition());
 				Color col = EnumColorFormat.RGBA.convert(p.getColor());
 				
 				this.buf.position(c * PARTICLE_FLOAT_COUNT);
 				
 				vec.store(this.buf);
 				col.store(this.buf);
 				
 			}
 			
 		}
 		
 		this.buf.position(0);
 		
 		return this.getParticleCount() != 0;
 	}
 	
 	@Override
 	public GLProgram getProgram()
 	{
 		return this.p;
 	}
 	
 }
