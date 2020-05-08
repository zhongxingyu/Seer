 package com.killoctal.andenginecontrols.buttons;
 
 
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import com.killoctal.andenginecontrols.detectors.PointerDetector;
 import com.killoctal.andenginecontrols.detectors.PointerDetector.IClickListener;
 import com.killoctal.andenginecontrols.detectors.PointerDetector.IMoveListener;
 
 /**
  * @brief A simple push button
  * @author Gabriel Schlozer
  * 
  * The events are based on listeners. The button itself is a listener so you don't have to manually add listeners.
  * You can add listeners for extend the button possibilities.
  * 
  * @note The "leaving" event requires the scene has setTouchAreaBindingOnActionDownEnabled(true) 
  */
 public class PushButton extends Rectangle
 {
 	private PointerDetector mDetector;
 	
 	
 	public PushButton(VertexBufferObjectManager pVertexBufferObjectManager)
 	{
 		this(0, 0, 0, 0, pVertexBufferObjectManager);
 	}
 	
 	
 	public PushButton(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager)
 	{
		this(pX, pY, pWidth, pHeight, pVertexBufferObjectManager, null);
 	}
 	
 	
 	public PushButton(float pX, float pY, float pWidth, float pHeight, VertexBufferObjectManager pVertexBufferObjectManager, PointerDetector pPointerDetector)
 	{
 		super(pX, pY, pWidth, pHeight, pVertexBufferObjectManager);
 		
 		// Creates the default detector
 		setClickDetector(pPointerDetector);
 		
 		// Auto add listeners
 		if (this instanceof PointerDetector.IClickListener)
 		{
 			mDetector.mClickListener = (IClickListener) this;
 		}
 		
 		if (this instanceof PointerDetector.IMoveListener)
 		{
 			mDetector.mMoveListener = (IMoveListener) this;
 		}
 	}
 	
 	
 	
 	final public PointerDetector getDetector()
 	{
 		return mDetector;
 	}
 	
 	public void setClickDetector(PointerDetector pDetector)
 	{
		mDetector = (pDetector != null) ? pDetector : new PointerDetector(this);
 		mDetector.mTouchArea = this;
 	}
 	
 	public void setEnabled(boolean pEnabled)
 	{
 		mDetector.setEnabled(pEnabled);
 	}
 	
 	public boolean isEnabled()
 	{
 		return mDetector.isEnabled();
 	}
 	
 	
 	
 	
 	/**
 	 * @note Don't override this method, it is best to override the virtual methods onClick, onMove, etc
 	 */
 	@Override
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY)
 	{
 		return mDetector.handleTouchEvent(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
 	}
 	
	
	
 	@Override
 	public boolean contains(float pX, float pY)
 	{
 		if (! isEnabled() || ! isVisible())
 		{
 			return false;
 		}
 		
 		return super.contains(pX, pY);
 	}
 	
 
 
 }
