 /* 
  * This code isn't copyrighted. Do what you want with it. :) 
  */
 package panoramakit.engine.task.tasks;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.minecraft.client.Minecraft;
 import panoramakit.engine.render.CompositeImageRenderer;
 import panoramakit.engine.task.Task;
 import panoramakit.mod.PanoramaKit;
 import panoramakitcore.CoreStates;
 
 /**
  * RenderTask
  * 
  * @author dayanto
  */
 public class RenderTask extends Task
 {
 	private final Minecraft mc = Minecraft.getMinecraft();
 	private final Logger L = PanoramaKit.instance.L;
 	
 	// the active image renderer
 	private CompositeImageRenderer imageRenderer;
 	boolean hasRendered = false;
 	
 	public RenderTask(CompositeImageRenderer imageRenderer)
 	{
 		this.imageRenderer = imageRenderer;
 	}
 	
 	@Override
 	public void perform()
 	{
 		if (mc.currentScreen != null) {
 			return;
 		}
 		
 		try {
 			L.info("Rendering screenshot");
 			imageRenderer.render();
 		} catch (Exception ex) {
 			L.log(Level.SEVERE, "Render failed: " + ex.getMessage(), ex);
 			printChat("panoramakit.renderfail", ex);
 		}
 		// Render a clean image to hide what was just rendered.
 		mc.entityRenderer.updateCameraAndRender(0);
 		setCompleted();
 	}
 	
 	@Override
 	public void init()
 	{
 		CoreStates.setRenderState(true);
 	}
 	
 	@Override
 	public void finish()
 	{
 		CoreStates.setRenderState(false);
 	}
 	
 	@Override
 	public double getProgress()
 	{
 		return 0;
 	}
 	
 }
