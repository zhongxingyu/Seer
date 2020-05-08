 package dkilian.andy;
 
 import android.app.Activity;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 /**
  * Entry point for an Andy game. The main Activity of your game must 
  * extend this class. For any Activity methods you override, you must
  * call the super implementation as well (this applied to Android
  * activities (Activity's?) in general anyways)
  * 
  * @author dkilian
  */
 public abstract class KernelActivity extends Activity 
 {
 	/** The game's kernel */
 	private Kernel _kernel;
 	/** Whether or not to quit when the game is paused */
 	private boolean _quitOnPause = true;
 	
 	/** Gets a value indicating whether this activity quits when paused by the system */
 	public boolean getQuitOnPause()
 	{
 		return _quitOnPause;
 	}
 
 	/** Sets a value indicating whether this activity quits when paused by the system */
 	public void setQuitOnPause(boolean quit)
 	{
 		_quitOnPause = quit;
 	}
 	
 	/** Gets a value from the activity indicating whether the game should execute in Canvas/Paint mode or OpenGL surface mode */
 	protected abstract boolean enableOpenGL();
 	
 	/**
 	 * Gets the minimum version of OpenGL ES required by this game (only called if this object returns true for enableOpenGL() 
 	 * during startup). The upper 16 bits indicate the major version and the lower 16 bits indicate the minor version (e.g.
 	 * 0x10001 for OpenGL ES 1.1 and 0x20000 for OpenGL ES 2.0). If the device does not support the version of OpenGL ES
 	 * returned by this method, the game will exit, showing the supplied error message (in getMissingGLError())
 	 */
 	protected abstract int getMinimumGLVersion();
 	
 	/** Gets the error shown when the required version of OpenGL is not supported on the current device */
 	protected abstract String getMissingGLError();
 	
 	/** Called before the Kernel is created. Allows the game to required initialization tasks */
 	protected abstract void initialize();
 	
 	/** Called after the Kernel is created. Allows the game to required initialization tasks */
 	protected abstract void onKernelInitialized();
 	
 	/** Gets the Screen to execute. Called after onKernelInitialized() */
 	protected abstract Screen execFirst();
 	
 	/** The application's entry point */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         
     	requestWindowFeature(Window.FEATURE_NO_TITLE);
     	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
     						 WindowManager.LayoutParams.FLAG_FULLSCREEN);
     	
 		initialize();
 		
 		_kernel = new Kernel(this);
 		
 		if (enableOpenGL())
 		{
 			if (_kernel.getGLView().getGLVersion() < getMinimumGLVersion())
 			{
 				// TODO: display an error message and quit
 			}
 			
 			setContentView(_kernel.getGLView());
 			_kernel.getVirtualScreen().setPhysicalDimensions(_kernel.getGLView());
 			_kernel.getGLView().queueResize();
 		}
 		else
 		{
 			setContentView(_kernel.getView());
 			_kernel.getVirtualScreen().setPhysicalDimensions(_kernel.getView());
 			_kernel.getView().queueResize();
 			_kernel.getView().getPaint().setFilterBitmap(true);
 		}
 		
 		onKernelInitialized();
 		
 		_kernel.setScreen(execFirst());
 		_kernel.start();
 	}
 	
 	/** Called when a device configuration change occurs */
 	@Override
 	public void onConfigurationChanged(Configuration newConfig)
 	{
 		super.onConfigurationChanged(newConfig);
 		if (_kernel.isOpenGLEnabled())
 			_kernel.getGLView().queueResize();
 		else
 			_kernel.getView().queueResize();
 	}
 	
 	/** Called by the OS during touch events */
     @Override
     public boolean onTouchEvent(MotionEvent e)
     {
     	if (e.getAction() == MotionEvent.ACTION_DOWN)
     	{
     		_kernel.getTouch().onPress(e.getX(), e.getY());
     	}
     	else if (e.getAction() == MotionEvent.ACTION_MOVE)
     	{
     		_kernel.getTouch().onMove(e.getX(), e.getY());
     	}
     	else if (e.getAction() == MotionEvent.ACTION_CANCEL || e.getAction() == MotionEvent.ACTION_UP)
     	{
     		_kernel.getTouch().onRelease();
     	}
     	
     	super.onTouchEvent(e);
     	return true;
     }
     
     /** Pause/resume currently isn't supported. To save users' battery life, the kernel exits whenever the application is stopped,
      *  as long as quit-on-pause functionality hasn't been disabled (see KernelActivity.[get|set]QuitOnPause) */
     @Override
     public void onPause()
     {
     	if (_quitOnPause)
     	{
     		System.exit(0);
     	}
     	else
     	{
     		super.onPause();
     		if (_kernel.isOpenGLEnabled())
     			_kernel.getGLView().onPause();
     	}
     }
     
     /** Resumes the game after it has been paused (e.g. via tombstoning) */
     @Override
     public void onResume()
     {
     	super.onResume();
     	if (_kernel.isOpenGLEnabled())
     		_kernel.getGLView().onResume();
     }
 	
 	/** Gets a reference to the game's Kernel. This value is undefined before onKernelInitialized() has been called */
 	public final Kernel getKernel()
 	{
 		return _kernel;
 	}
 }
