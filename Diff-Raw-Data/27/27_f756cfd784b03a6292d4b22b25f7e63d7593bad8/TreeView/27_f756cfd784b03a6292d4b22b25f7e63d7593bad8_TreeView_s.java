 package ca.cmput301f13t03.adventure_datetime.view.treeView;
 
 import java.util.Map;
 import java.util.UUID;
 
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IAllFragmentsListener;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class TreeView extends SurfaceView implements IAllFragmentsListener, SurfaceHolder.Callback, Runnable
 {
 	private static final float FPS = 30.0f;
 	private static final String TAG = "TreeView";
 	
	Thread m_drawingThread = null;
	boolean m_isDrawing = false;
	SurfaceHolder m_surface = null;
	NodeGrid m_grid = null;
	Map<UUID, StoryFragment> m_fragments = null;
	Camera m_camera = null;
 	
 	// must have all constructors or it doesn't work
 	public TreeView(Context context) 
 	{
 		super(context);
 		Setup();
 	}
 	
 	public TreeView(Context context, AttributeSet attrs)
 	{
 		super(context, attrs);
 		Setup();
 	}
 	
 	public TreeView(Context context,AttributeSet attrs, int defStyle)
 	{
 		super(context, attrs, defStyle);
 		Setup();
 	}
 	
 	private void Setup()
 	{
 		this.getHolder().addCallback(this);
 		Locator.getPresenter().Subscribe(this);
 	}
 	
 	@Override
 	public void OnAllFragmentsChange(Map<UUID, StoryFragment> newFragments) 
 	{
 		m_fragments = newFragments;
 		if(m_grid != null)
 		{
 			m_grid.SetFragments(newFragments);
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
 	{
 		// Don't care (yet)
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder surface) 
 	{
 		m_camera = new Camera();
 		m_grid = new NodeGrid(this.getResources());
 		m_grid.SetFragments(m_fragments);
 		m_isDrawing = true;
 		m_surface = surface;
 		m_drawingThread = new Thread(this);
 		m_drawingThread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder surface) 
 	{
 		m_isDrawing = false;
 		if(m_drawingThread != null)
 		{
 			try 
 			{
 				// allow it to finish drawing the current frame
 				m_drawingThread.join();
 			} 
 			catch (InterruptedException e) 
 			{
 				Log.w(TAG, "Interrupted while Joining! " + e.getMessage());
 			}
 			m_drawingThread = null;
 			m_grid = null;
 		}
 	}
 	
 	/**
 	 * Main thread for drawing
 	 */
 	public void run() 
 	{
 		while(m_isDrawing)
 		{
 			Canvas canvas = m_surface.lockCanvas();
 			
 			try
 			{
 				// clear canvas
 				canvas.drawColor(Color.WHITE);
 				
 				// draw stuffs
 				m_grid.Draw(canvas, m_camera);
 			}
 			finally
 			{
 				m_surface.unlockCanvasAndPost(canvas);
 			}
 			
 			// then sleep for a bit
 			try 
 			{
 				Thread.sleep((long) (1000.0f / FPS));
 			} 
 			catch (InterruptedException e) 
 			{
 				// Don't care, just log it and move on
 				Log.w(TAG, "Interrupted while sleeping!");
 			}
 		}
 	}
 }
