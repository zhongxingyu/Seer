 package com.senselessweb.android.universeofpictures.scene;
 
 import java.util.concurrent.Executors;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView.Renderer;
 
 import com.senselessweb.android.universeofpictures.R;
 import com.threed.jpct.Camera;
 import com.threed.jpct.FrameBuffer;
 import com.threed.jpct.Light;
 import com.threed.jpct.Object3D;
 import com.threed.jpct.Primitives;
 import com.threed.jpct.RGBColor;
 import com.threed.jpct.SimpleVector;
 import com.threed.jpct.Texture;
 import com.threed.jpct.TextureManager;
 import com.threed.jpct.World;
 import com.threed.jpct.util.BitmapHelper;
 import com.threed.jpct.util.MemoryHelper;
 
 public class RenderableScene extends World implements Renderer
 {
 
 	private static final long serialVersionUID = -9075219920811753949L;
 
 	private FrameBuffer frameBuffer = null;
 
 	private final Camera camera;
 
 	private final Light sun;
 
 	private final Object3D planet;
 
 	public RenderableScene(final Context context)
 	{
 		this.sun = new Light(this);
 		this.sun.setPosition(new SimpleVector(10000, 0, -2000));
 		this.sun.setIntensity(255f, 255f, 255f);
 
 		final Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.texture_planet)), 512, 256));
 		TextureManager.getInstance().addTexture("texture", texture);
 
 		final Texture testfoto = new Texture(BitmapHelper.rescale(BitmapHelper.convert(context.getResources().getDrawable(R.drawable.testfoto)), 256, 256));
 		TextureManager.getInstance().addTexture("testfoto", testfoto);
 
 		this.planet = Primitives.getSphere(60, 100);
 		this.planet.calcTextureWrapSpherical();
 		this.planet.setTexture("texture");
 		this.addObject(this.planet);
		
		RenderableScene.this.planet.rotateY((float) (Math.PI * 0.75));
 
 		this.camera = this.getCamera();
 		//this.camera.setPosition(-100, -100, -700); // far
 		//this.camera.lookAt(this.planet.getCenter());
 		this.camera.setPosition(190, -5, -40); // near
 
 		for (float angle = 0f; angle < Math.PI * 2.0; angle += 0.2f)
 			for (int n = 0; n <= 6; n++)
 				this.planet.addChild(new Picture(this, n, angle));
 
 		this.planet.strip();
 		this.planet.build();
 
 		MemoryHelper.compact();
 
 		this.animate();
 	}
 
 	public void animate()
 	{
 		Executors.newSingleThreadExecutor().execute(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				while (true)
 				{
 					RenderableScene.this.planet.rotateY(0.005f);
 					try
 					{
 						Thread.sleep(100);
 					} catch (final InterruptedException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onSurfaceCreated(final GL10 gl, final EGLConfig config)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void onSurfaceChanged(final GL10 gl, final int width, final int height)
 	{
 		if (this.frameBuffer != null)
 			this.frameBuffer.dispose();
 		this.frameBuffer = new FrameBuffer(gl, width, height);
 
 	}
 
 	@Override
 	public void onDrawFrame(final GL10 gl)
 	{
 		this.frameBuffer.clear(RGBColor.BLACK);
 		this.renderScene(this.frameBuffer);
 		this.draw(this.frameBuffer);
 		this.frameBuffer.display();
 	}
 }
