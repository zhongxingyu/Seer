 package platformer.core.renderer.viewport;
 
 import java.util.Comparator;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.systems.Positionable;
 import platformer.core.renderer.Renderable;
 import platformer.core.renderer.Renderer;
 import platformer.core.renderer.RendererFactory;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Graphics;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.utils.Array;
 
 public class DefaultViewportRender implements ViewportRenderer {
 	private final Graphics graphics;
 	private final Camera camera;
 	private final RendererFactory rendererFactory;
 
 	public DefaultViewportRender(RendererFactory rendererFactory, Graphics graphics, Camera camera) {
 		this.rendererFactory = rendererFactory;
 		this.graphics = graphics;
 		this.camera = camera;
 	}
 
 	@Override
 	public void render(Array<Renderable> renderSet) {
 		camera.update();
 		
 		renderSet.sort(new Comparator<Renderable>() {
 			@Override
 			public int compare(Renderable a, Renderable b) {
 				float aZ = a.getPosition().z;
 				float bZ = b.getPosition().z;
 				  
 				return aZ > bZ ? -1 : aZ == bZ ? 0 : 1;
 			}
 		});
 
 		Renderer lastRenderer = null;
 		SpriteBatch batch = new SpriteBatch();
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		for (Renderable renderable : renderSet) {
 			final Renderer renderer = rendererFactory.findRenderer(renderable.getRendererInstructions());
 			final boolean newRenderer = lastRenderer != null && lastRenderer != renderer;
 			final boolean firstRenderer = lastRenderer == null;
 
 			if (firstRenderer || newRenderer) {
 				renderer.initialize(camera, batch);
 			}
 
 			renderer.render(renderable);
 
 			if (newRenderer) {
 				// Finish with the old renderer:
 				lastRenderer.finish();
 			}
 
 			lastRenderer = renderer;
 		}
 		batch.end();
 
 		if (lastRenderer != null)
 			lastRenderer.finish();
 
 	}
 
 	public void renderBackground(Positionable positionable) {
 
 		float y = positionable.getPosition().y;
 
 		float brightnessFactor = 1F - (y / 4096);
 		if (brightnessFactor < 0.2F)
 			brightnessFactor = 0.2F;
 
 		Gdx.gl.glClearColor(0.23F * brightnessFactor, 0.43F * brightnessFactor, 0.61F * brightnessFactor, 0);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	}
 }
