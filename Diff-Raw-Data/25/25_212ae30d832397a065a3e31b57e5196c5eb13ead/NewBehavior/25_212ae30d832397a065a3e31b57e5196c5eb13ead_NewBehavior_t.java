 package homework3.android;
 
 import android.util.Log;
 
 public abstract class NewBehavior extends BehaviorBase {
 	static final String LOG_TAG = "Homework3.NewBehavior";
 	
 	/*
 	 When a NewBehavior starts, it should call its own make() method to create 
 	 a new instance of a graphical object. The NewBehavior should add the object 
 	 returned by make() to the group, so it will appear on screen immediately. 
 	 While the Behavior is running, it should resize the new object to follow the 
 	 mouse (assuming onePoint is false). When it stops, it should leave the object 
 	 where it is.
 	 */
 	
 	// TODO: can we declare abstract methods in a non-abstract class?
 	// TODO: figure out how to create an abstract constructor
 
 	protected boolean m_onePoint;
 	protected GraphicalObject m_graphicalObject;
 	
 	/**
 	 * If the onePoint parameter to the constructor is true, then the new Behavior needs 
 	 * only one point to create the object. It calls make(x, y, x, y) to create the object, 
 	 * then stops immediately after starting, never calling resize(). This is useful for fixed size objects, like icons.
 	 * @param onePoint
 	 */
 	public NewBehavior (boolean onePoint){
 		this(onePoint, null);
 	}
 	
 	public NewBehavior (boolean onePoint, Group g)
 	{
 		super(g);
 		m_onePoint = onePoint;
 	}
 	
 	@Override
 	protected void behaviorStarted(BehaviorEvent event) {
 		m_graphicalObject = make(event.getX(), event.getY(), event.getX(), event.getY());
//		m_graphicalObject.setGroup(m_group);
		m_group.addChild(m_graphicalObject);
 		if(m_onePoint)
 		{
 			stop(event);
 		} 	
		
 		Log.v(LOG_TAG, "behavior started");
 	}
 	
 	@Override
 	protected boolean startConditionSatisfied(BehaviorEvent event) {
 		return true;
 	}
 	
 	@Override
 	protected void doRunningInside(BehaviorEvent event) {
 		resize(m_graphicalObject, m_touchStartPoint.x, m_touchStartPoint.y, event.getX(), event.getY());
 	}
 	
 	@Override
 	protected void doRunningOutside(BehaviorEvent event) {
 		// do nothing
 		Log.v(LOG_TAG, "running outside");
 	}
 	
 	@Override
 	protected void onCancelled(BehaviorEvent event) {
 		// TODO add interim feedback via selection handles
 		Log.v(LOG_TAG, "cancelled");
 		m_group.removeChild(m_graphicalObject);
 		
 	}
 	
 	@Override
 	protected void onStopped(BehaviorEvent event) {
 		// TODO  get rid eof interim feedback and add the real item.
 	}
 	
     
 	/**
      * make() creates a graphical object from point (x1, y1) to point (x2, y2). 
      * This method is declared abstract. It should be overridden in a subclass of NewBehavior, 
      * which decides which graphical object to create and how to interpret the (x1,y1) and (x2, y2) coordinates.
      * @param x1
      * @param y1
      * @param x2
      * @param y2
      * @return
      */
     public abstract GraphicalObject make (int x1, int y1, int x2, int y2);
     
     /**
      * resize() adjusts a graphical object created by make() with new points. 
      * (x1,y1) is the anchor point, which should be the same as was passed to make(). 
      * The point (x2,y2) follows the mouse cursor, so it will be different.
      * @param gobj
      * @param x1
      * @param y1
      * @param x2
      * @param y2
      */
     public abstract void resize (GraphicalObject gobj, int x1, int y1, int x2, int y2);
     
 }
