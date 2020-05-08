 package osu.controls;
 
 import java.util.ArrayList;
 
 import osu.game.HOSlider;
 import osu.math.Bezier;
 import android.graphics.Point;
 import android.graphics.PointF;
 import android.graphics.Rect;
 import dkilian.andy.Kernel;
 import dkilian.andy.PrerenderCache;
 import dkilian.andy.TexturedQuad;
 import dkilian.andy.jni.agl;
 
 /**
  * Handles interaction with a slider, using a slider hit-object
  * 
  * @author dkilian
  */
 public class Slider implements Control
 {
 	/** The spacing of the points along this slider, in pixels */
 	public static final float LENGTH_PER_STEP = 10.f;
 	/** The amount of time it takes for a button to fade in, in partial seconds */
 	public static final float FADE_IN_TIME  = .3f;
 	/** The amount of time it takes for a button to fade out, in partial seconds */
 	public static final float FADE_OUT_TIME = .3f;
 	/** The amount of time, after fading in, the button appears before its event timing, in partial seconds */
 	public static final float WAIT_TIME = 1.f;
 	/** The scale ratio between the bounding box of the graphic and the interaction bounding box. Also controls the scale factor of the nub when pressed. */
 	public static final float INPUT_FUDGE_FACTOR = 1.35f;
 	/** The scale factor to apply to this control. Hitboxes will be unaffected. */
 	public static final float SCALE_FACTOR = .75f;
 	
 	/** The X coordinate of the initial cap in virtual space */
 	private float _x;
 	/** The Y coordinate of the initial cap in virtual space */
 	private float _y;
 	/** The time this entire slider begins to fade in */
 	private float _tbeg;
 	/** The time this entire slider finishes fading out */
 	private float _tend;
 	/** The bounds of this slider's nub */
 	private Rect _bounds;
 	/** The images the slider itself consists of */
 	private TexturedQuad _cap, _fill, _nubUp, _nubDown, _repeat;
 	/** The event this slider corresponds to */
 	private HOSlider _event;
 	/** A list of objects this slider sends notifications to when its nub is interacted with */
 	private ArrayList<SliderCallback> _callbacks;
 	/** The number of times the nub has traveled across this slider */
 	private int _repeatIteration;
 	/** The bezier points describing this slider, in the format [x1 y1 x2 y2 ... xn yn] */
 	private float[] _bezier;
 	/** The upper bound of this slider's bezier curve, in [0,1] */
 	private float _bezierUpper;
 	/** The movement speed in Bezier t values per second (i.e. 1.f = the entire path in one second) */
 	private float _velocity;
 	/** The current position along this slider, between 0 and 1 */
 	private float _t;
 	/** True iff the nub is being pressed by the user */
 	private boolean _pressed;
 	/** The current location of the nub */
 	private PointF _nubPoint;
 	/** This slider's approach ring */
 	private Ring _approach;
 	/** The cache used to lookup / render text on-the-fly */
 	private PrerenderCache _textCache;
 	/** The text to render centered in this slider's initial cap; null if none desired */
 	private String _text;
 	
 	/** Preallocated for GC performance */
 	private PointF _point = new PointF();
 	
 	/**
 	 * Creates a new slider with no graphics
 	 * @param event The event this slider corresponds to
 	 * @param beatLength The length of a beat at the point in the song this slider occurs, in milliseconds per beat
 	 * @param sliderMultiplier The slider speed multiplier; multiplies the slider speed from the base speed of 100 pixels per beatLength milliseconds
 	 * @param length The desired length of this slider; this slider will be truncated to match this value based on its true length (see Bezier.length())
 	 * @param approach This slider's (optional) approach ring
 	 * @param textCache Caches pre-rendered text sprites
 	 * @param text The text to render centered in this slider's initial cap. May be null if none desired.
 	 */
 	public Slider(HOSlider event, float beatLength, float sliderMultiplier, float length, Ring approach, PrerenderCache textCache, String text)
 	{
 		_event = event;
 		_x = event.getX();
 		_y = event.getY();
 		_bounds = new Rect();
 		_callbacks = new ArrayList<SliderCallback>();
 		_repeatIteration = 0;
 		_t = 0;
 		_pressed = false;
 		_nubPoint = new PointF();
 		_approach = approach;
 		_textCache = textCache;
 		_text = text;
 
 		_bezier = new float[_event.getPathPoints().size() * 2 + 2];
 		int i = 0;
 		_bezier[i++] = _x;
 		_bezier[i++] = _y;
 		for (Point p : _event.getPathPoints())
 		{
 			_bezier[i++] = p.x;
 			_bezier[i++] = p.y;
 		}
 		
 		_velocity = 100.f / (beatLength / 1000.f) * sliderMultiplier / length;
 		_bezierUpper = length / Bezier.length(_bezier);
		if (_bezierUpper > 1.f) _bezierUpper = 1.f;
 		_tbeg = _event.getTiming() / 1000.f - (FADE_IN_TIME + WAIT_TIME);
 		_tend = _event.getTiming() / 1000.f + _event.getRepeats() * _bezierUpper / _velocity + FADE_OUT_TIME;
 	}
 	
 	/**
 	 * Creates a new slider 
 	 * @param event The event this slider corresponds to
 	 * @param beatLength The length of a beat at the point in the song this slider occurs, in milliseconds per beat
 	 * @param sliderMultiplier The slider speed multiplier; multiplies the slider speed from the base speed of 100 pixels per beatLength milliseconds
 	 * @param length The desired length of this slider; this slider will be truncated to match this value based on its true length (see Bezier.length())
 	 * @param cap The graphic drawn at each end of the slider
 	 * @param fill The graphic drawn across the entire slider
 	 * @param nubUp The graphic drawn when the nub is not being pressed
 	 * @param nubDown The graphic drawn when the nub is being pressed
 	 * @param repeat The graphic drawn on the cap when a repeat is required
 	 * @param approach This slider's (optional) approach ring
 	 * @param textCache Caches pre-rendered text sprites
 	 * @param text The text to render centered in this slider's initial cap. May be null if none desired.
 	 */
 	public Slider(HOSlider event, float beatLength, float sliderMultiplier, float length, 
 			      TexturedQuad cap, TexturedQuad fill, TexturedQuad nubUp, TexturedQuad nubDown, TexturedQuad repeat, Ring approach, PrerenderCache textCache, String text)
 	{
 		_event = event;
 		_x = event.getX();
 		_y = event.getY();
 		_bounds = new Rect();
 		_callbacks = new ArrayList<SliderCallback>();
 		_cap = cap;
 		_fill = fill;
 		_nubUp = nubUp;
 		_nubDown = nubDown;
 		_repeat = repeat;
 		_repeatIteration = 0;
 		_t = 0;
 		_pressed = false;
 		_nubPoint = new PointF();
 		_approach = approach;
 		_textCache = textCache;
 		_text = text;
 		
 		_bezier = new float[_event.getPathPoints().size() * 2 + 2];
 		int i = 0;
 		_bezier[i++] = _x;
 		_bezier[i++] = _y;
 		for (Point p : _event.getPathPoints())
 		{
 			_bezier[i++] = p.x;
 			_bezier[i++] = p.y;
 		}
 		
 		_velocity = 100.f / (beatLength / 1000.f) * sliderMultiplier / length;
 		_bezierUpper = length / Bezier.length(_bezier);
		if (_bezierUpper > 1.f) _bezierUpper = 1.f;
 		_tbeg = _event.getTiming() / 1000.f - (FADE_IN_TIME + WAIT_TIME);
 		_tend = _event.getTiming() / 1000.f + _event.getRepeats() * _bezierUpper / _velocity + FADE_OUT_TIME;
 	}
 	
 	/** Gets the text centered in this slider's initial cap. May be null if none desired. */
 	public String getText()
 	{
 		return _text;
 	}
 	
 	/** Sets the text centered in this slider's initial cap. May be null if none desired */
 	public void setText(String text)
 	{
 		_text = text;
 	}
 	
 	/** Gets this slider's approach ring. May be null. */
 	public Ring getApproachRing()
 	{
 		return _approach;
 	}
 	
 	/** Sets this slider's approach ring. May be null. */
 	public void setApproachRing(Ring approach)
 	{
 		_approach = approach;
 		if (_approach != null)
 		{
 			_approach.setAnimated(true);
 			_approach.setX(_bezier[0]);
 			_approach.setY(_bezier[1]);
 			_approach.setStartAlpha(0.f);
 			_approach.setEndAlpha(1.f);
 			_approach.setStartScale(3.f);
 			_approach.setEndScale(SCALE_FACTOR);
 			_approach.setStartTime(_tbeg);
 			_approach.setEndTime(_event.getTiming() / 1000.f);
 		}
 	}
 	
 	/** Gets the position of the nub along this slider, in the range [0,1] */
 	public float getNubPosition()
 	{
 		return _t;
 	}
 
 	/** Sets the position of the nub along this slider, in the range [0,1] */
 	public void setNubPosition(float t)
 	{
 		_t = t;
 	}
 	
 	/**
 	 * Gets the number of times the nub has traveled across the slider, in either direction
 	 * (i.e. so the nub traveling from one end of the slider to another and back counts as
 	 * two repeats). A slider dies when the repeat iteration = the number of repeats the
 	 * slider uses.
 	 */
 	public int getRepeatIteration()
 	{
 		return _repeatIteration;
 	}
 	
 	/** Gets the event this slider corresponds to */
 	public HOSlider getEvent()
 	{
 		return _event;
 	}
 	
 	/** Sets the event this slider corresponds to */
 	public void setEvent(HOSlider event)
 	{
 		_event = event;
 	}
 	
 	/** Gets the graphic drawn at each cap of this slider */
 	public TexturedQuad getCap()
 	{
 		return _cap;
 	}
 	
 	/** Sets the graphic drawn at each cap of this slider */
 	public void setCap(TexturedQuad cap)
 	{
 		_cap = cap;
 	}
 	
 	/** Gets the graphic drawn along this slider's path */
 	public TexturedQuad getFill()
 	{
 		return _fill;
 	}
 	
 	/** Sets the grphic drawn along this slider's path */
 	public void setFill(TexturedQuad fill)
 	{
 		_fill = fill;
 	}
 	
 	/** Gets the graphic drawn when the slider nub is not being pressed */
 	public TexturedQuad getNubUp()
 	{
 		return _nubUp;
 	}
 
 	/** Sets the graphic drawn when the slider nub is not being pressed */
 	public void setNubUp(TexturedQuad up)
 	{
 		_nubUp = up;
 	}
 
 	/** Gets the graphic drawn when the slider nub is being pressed */
 	public TexturedQuad getNubDown()
 	{
 		return _nubDown;
 	}
 
 	/** Sets the graphic drawn when the slider nub is being pressed */
 	public void setNubDown(TexturedQuad nubDown)
 	{
 		_nubDown = nubDown;
 	}
 	
 	/** Registers a callback to receive notifications of user interaction with this slider's nub */
 	public void register(SliderCallback callback)
 	{
 		_callbacks.add(callback);
 	}
 	
 	/** Unregisters a callback from receiving notifications of user interaction with this slider's nub */
 	public void unregister(SliderCallback callback)
 	{
 		_callbacks.remove(callback);
 	}
 
 	/** Gets the X coordinate of this slider's initial cap */
 	@Override
 	public float getX() 
 	{
 		return _x;
 	}
 
 	/** Has no effect */
 	@Override
 	public void setX(float x) 
 	{		
 		_x = x;
 	}
 
 	/** Gets the Y coordinate of this slider's initial cap */
 	@Override
 	public float getY() 
 	{
 		return _y;
 	}
 
 	/** Has no effect */
 	@Override
 	public void setY(float y) 
 	{
 		_y = y;
 	}
 
 	/** Gets the time this slider begins fading in */
 	@Override
 	public float getStartTime() 
 	{
 		return _tbeg;
 	}
 
 	/** Sets the time this slider begins fading in */
 	@Override
 	public void setStartTime(float t) 
 	{
 		_tbeg = t;
 	}
 
 	/** Gets the time this slider finishes fading out */
 	@Override
 	public float getEndTime() 
 	{
 		return _tend;
 	}
 
 	/** Sets the time this slider finished fading out */
 	@Override
 	public void setEndTime(float t) 
 	{
 		_tend = t;
 	}
 
 	/** Gets a value indicating whether this slider is visible */
 	@Override
 	public boolean isVisible(float t) 
 	{
 		return t >= _tbeg && t <= _tend;
 	}
 
 	/** Gets the hitbox describing this slider's nub */
 	@Override
 	public Rect getHitbox() 
 	{
 		return _bounds;
 	}
 
 	/** Registers a touch event with this slider */
 	@Override
 	public void interact(float x, float y, float t) 
 	{
 		if (_pressed)
 			for (int i = 0; i < _callbacks.size(); ++i)
 				_callbacks.get(i).sliderEvent(this, _event);
 	}
 
 	/** Does per-frame updating required by this slider */
 	@Override
 	public void update(Kernel kernel, float t, float dt) 
 	{
 		if (t >= _tbeg + FADE_IN_TIME + WAIT_TIME && t <= _tend)
 		{
 			float t0 = _tbeg + FADE_IN_TIME + WAIT_TIME;
 			_t = (t - t0) * _velocity;
 			_repeatIteration = (int)_t;
 			_t -= _repeatIteration;
 			if ((_repeatIteration & 1) != 0)
 				_t = 1.f - _t;
 			
 			Bezier.evaluate2d(_bezier, _t, _nubPoint);
 			
 			if (_approach != null)
 			{
 				_approach.setAnimated(false);
 				_approach.setScale(2.f);
 				_approach.setX(_nubPoint.x);
 				_approach.setY(_nubPoint.y);
 			}
 		}
 		
 		int dw = (int)(.5f * _nubUp.getWidth() * INPUT_FUDGE_FACTOR);
 		int dh = (int)(.5f * _nubUp.getHeight() * INPUT_FUDGE_FACTOR);
 		
 		_bounds.left   = (int)(_nubPoint.x - dw);
 		_bounds.top    = (int)(_nubPoint.y - dh);
 		_bounds.right  = (int)(_nubPoint.x + dw);
 		_bounds.bottom = (int)(_nubPoint.y + dh);
 		
 		_pressed = kernel.getTouch().isDown() && 
 		           Math.abs(kernel.getTouch().getX() - _nubPoint.x) < dw &&
 		           Math.abs(kernel.getTouch().getY() - _nubPoint.y) < dh;
 	}
 
 	/** Renders this slider */
 	@Override
 	public void draw(Kernel kernel, float t, float dt) 
 	{		
 		if (isVisible(t))
 		{	
 			float scale = _pressed ? INPUT_FUDGE_FACTOR : 1.f;
 			scale *= SCALE_FACTOR;
 			float alpha = 1.f;
 			if (t >= _tbeg && t <= _tbeg + FADE_IN_TIME)
 				alpha = (t - _tbeg) / FADE_IN_TIME;
 			else if (t <= _tend && t >= _tend - FADE_OUT_TIME)
 				alpha = (_tend - t) / FADE_OUT_TIME;			
 			
 			Bezier.evaluate2d(_bezier, _bezierUpper, _point);
 			float endx = _point.x;
 			float endy = _point.y;
 			
 			agl.InstanceBitmapBezier(_fill.getTexture(), _fill.getWidth(), _fill.getHeight(), _bezier, _bezier.length / 2, (int)(_event.getPathLength() / LENGTH_PER_STEP), 
 					                 0.f, _bezierUpper, 0.f, SCALE_FACTOR, SCALE_FACTOR, alpha);
 			_cap.setAlpha(alpha);
 			_cap.getScale().x = SCALE_FACTOR;
 			_cap.getScale().y = SCALE_FACTOR;
 			_cap.getTranslation().x = _bezier[0];
 			_cap.getTranslation().y = _bezier[1];
 			_cap.draw(kernel);
 			_cap.getTranslation().x = endx;
 			_cap.getTranslation().y = endy;
 			_cap.draw(kernel);
 			
 			if (_repeatIteration < _event.getRepeats())
 			{
 				int remainingRepeats = _event.getRepeats() - (_repeatIteration + 1);
 				int direction = _repeatIteration & 1;
 				if (remainingRepeats > 1 || (remainingRepeats == 1 && direction != 0))	// Arrow at initial cap
 				{
 					if (_text == null || t * 1000.f >= _event.getTiming())	// Don't draw if the text is visible
 					{
 						Bezier.evaluate2d(_bezier, .025f, _point);
 						float tmpx = _point.x, tmpy = _point.y;
 						_repeat.getTranslation().x = _bezier[0];
 						_repeat.getTranslation().y = _bezier[1];
 						_repeat.setRotation((float)(-90.f + 180.0 / Math.PI * Math.atan2(tmpx - _bezier[1], tmpy - _bezier[0])));
 						_repeat.setAlpha(alpha);
 						_repeat.getScale().x = SCALE_FACTOR;
 						_repeat.getScale().y = SCALE_FACTOR;
 						_repeat.draw(kernel);
 					}
 				}
 				if (remainingRepeats > 1 || (remainingRepeats == 1 && direction == 0))	// Arrow at end cap
 				{
 					Bezier.evaluate2d(_bezier, _bezierUpper - .025f, _point);
 					float tmpx = _point.x, tmpy = _point.y;
 					_repeat.getTranslation().x = endx;
 					_repeat.getTranslation().y = endy;
 					_repeat.setRotation((float)(-90.f + 180.0 / Math.PI * Math.atan2(tmpx - endx, tmpy - endy)));
 					_repeat.setAlpha(alpha);
 					_repeat.getScale().x = SCALE_FACTOR;
 					_repeat.getScale().y = SCALE_FACTOR;
 					_repeat.draw(kernel);
 				}
 
 				TexturedQuad nub = _pressed ? _nubDown : _nubUp;	
 				agl.DrawAlongBezierPath(nub.getTexture(), nub.getWidth(), nub.getHeight(), _bezier, _bezier.length / 2, _t, 0.f, scale, scale, alpha);
 			}
 			
 			if (_text != null && t * 1000.f < _event.getTiming())
 			{
 				TexturedQuad s = _textCache.string(_text);
 				s.getTranslation().x = _bezier[0];
 				s.getTranslation().y = _bezier[1];
 				s.setAlpha(alpha);
 				s.getScale().x = SCALE_FACTOR;
 				s.getScale().y = SCALE_FACTOR;
 				s.draw(kernel);
 			}
 		}
 
 		if (_approach != null && t <= _tend - FADE_OUT_TIME)
 			_approach.draw(kernel, t, dt);
 	}
 }
 
