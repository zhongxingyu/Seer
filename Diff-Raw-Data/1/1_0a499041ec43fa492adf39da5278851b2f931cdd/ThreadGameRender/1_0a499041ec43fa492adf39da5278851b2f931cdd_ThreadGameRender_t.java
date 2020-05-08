 
 package com.elusivehawk.engine.render;
 
 import java.util.Collection;
 import com.elusivehawk.engine.core.GameLog;
 import com.elusivehawk.engine.core.ThreadTimed;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class ThreadGameRender extends ThreadTimed
 {
 	protected final IRenderHUB hub;
 	protected int fps;
 	protected float delta;
 	
 	public ThreadGameRender(IRenderHUB renderHub, int framerate)
 	{
 		hub = renderHub;
 		fps = framerate;
 		
 		delta = (1000000000.0f / fps);
 		
 	}
 	
 	@Override
 	public void update()
 	{
 		RenderHelper.makeContextCurrent();
 		
 		this.hub.getCamera().updateCamera(this.hub);
 		
 		Collection<IRenderEngine> engines = this.hub.getRenderEngines();
 		
 		if (engines == null || engines.isEmpty())
 		{
 			return;
 		}
 		
 		GL.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 		
 		for (IRenderEngine engine : engines)
 		{
 			//TODO Unbind all textures.
 			
 			engine.render(this.hub);
 			
 			try
 			{
 				RenderHelper.checkForGLError();
 				
 			}
 			catch (Exception e)
 			{
 				GameLog.error(e);
 				
 			}
 			
 		}
 		
 	}
 	
 	@Override
 	public int getDelta() //TODO Convert to float
 	{
 		return 0;
 	}
 	
 	@Override
 	public int getTargetUpdateCount()
 	{
 		return this.fps;
 	}
 	
 	public synchronized void setTargetFPS(int framerate)
 	{
 		this.fps = framerate;
		this.delta = (1000000000.0f / this.fps);
 		
 	}
 	
 }
