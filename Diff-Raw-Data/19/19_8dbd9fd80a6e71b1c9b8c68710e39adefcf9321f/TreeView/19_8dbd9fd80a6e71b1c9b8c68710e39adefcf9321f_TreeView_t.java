 package ca.cmput301f13t03.adventure_datetime.view.treeView;
 
 import java.util.Map;
 import java.util.UUID;
 
 import ca.cmput301f13t03.adventure_datetime.model.Choice;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IAllFragmentsListener;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentFragmentListener;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentStoryListener;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 import ca.cmput301f13t03.adventure_datetime.view.IFragmentSelected;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class TreeView extends SurfaceView 
 	implements 	IAllFragmentsListener, 
 				ICurrentFragmentListener,
 				ICurrentStoryListener,
 				SurfaceHolder.Callback, 
 				Runnable
 {
 	private static final float FPS = 120.0f; // strictly speaking this isn't being calculated quite right...
 	private static final String TAG = "TreeView";
 	
 	private Thread m_drawingThread = null;
 	private volatile boolean m_isDrawing = false;
 	private SurfaceHolder m_surface = null;
 	private Camera m_camera = null;
 	
 	private NodeGrid m_grid = null;
 	private Map<UUID, StoryFragment> m_fragments = null;
 	private Story m_currentStory = null;
 	
 	private InputHandler m_touchHandler = null;
 	
 	
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
 		
 		m_grid = new NodeGrid(this.getResources());
 		m_camera = new Camera();
 		m_touchHandler = new InputHandler(m_camera, m_grid);
 	}
 	
 	public void OnAllFragmentsChange(Map<UUID, StoryFragment> newFragments) 
 	{
 		m_fragments = newFragments;
 		
 		// due to the async nature of the callbacks we may not have all the data we need
 		// at this point, we have most, but not all
 		if(m_currentStory != null)
 		{
 			AfterDataAvailable();
 		}
 	}
 	
 	public void OnCurrentStoryChange(Story newStory) 
 	{
 		m_currentStory = newStory;
 		
 		if(m_fragments != null)
 		{
 			AfterDataAvailable();
 		}
 	}
 	
 	public void OnCurrentFragmentChange(StoryFragment newFragment) 
 	{
 		m_grid.SelectFragment(newFragment);
 	}
 	
 	public void SetFragmentCallback(IFragmentSelected selectionCallback)
 	{
 		m_touchHandler.SetSelectionCallback(selectionCallback);
 	}
 	
 	public IFragmentSelected GetFragmentCallback()
 	{
 		return m_touchHandler.GetSelectionCallback();
 	}
 	
 	public void AddChoice(StoryFragment origin, Choice choice)
 	{
 		m_grid.AddChoice(origin, choice);
 	}
 	
 	public void RemoveChoice(StoryFragment origin, Choice choice)
 	{
 		m_grid.RemoveChoice(origin, choice);
 	}
 	
 	public void RefreshView()
 	{
 		m_grid.RefreshView();
 	}
 	
 	private void AfterDataAvailable()
 	{
 		if(m_grid != null)
 		{
 			m_grid.SetFragments(m_fragments, m_currentStory.getHeadFragmentId());
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
 	{
 		m_camera.ResizeView(width, height);
 		m_camera.LookAt(0, 0);
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder surface) 
 	{
 		Locator.getPresenter().Subscribe((ICurrentStoryListener)(this));
 		Locator.getPresenter().Subscribe((IAllFragmentsListener)(this));
 		Locator.getPresenter().Subscribe((ICurrentFragmentListener)(this));
 		
 		m_isDrawing = true;
 		m_surface = surface;
 		m_drawingThread = new Thread(this);
 		m_drawingThread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder surface) 
 	{
 		Locator.getPresenter().Unsubscribe((ICurrentStoryListener)(this));
 		Locator.getPresenter().Unsubscribe((IAllFragmentsListener)(this));
 		Locator.getPresenter().Unsubscribe((ICurrentFragmentListener)(this));
 		
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
 			
 			if(canvas != null)
 			{
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
 
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		if(m_touchHandler != null)
 		{
 			m_touchHandler.OnTouchAction(event);
 		}
 		
 		//Must return true or we stop receiving input events!
 		return true;
 	}
 	
 }
