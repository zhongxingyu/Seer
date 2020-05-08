 package asciiWorld;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Camera implements IHasPosition, IHasBounds {
 
 	private IHasBounds _viewport;
 	private IHasPosition _focus;
 	private float _scale;
 	private Vector2f _scaledCenter;
 	
 	public Camera(IHasBounds viewport, IHasPosition focus, float scale) {
 		_viewport = viewport;
 		_focus = focus;
 		_scale = scale;
 		setScaledCenter();
 	}
 	
 	public Camera(IHasBounds viewport, IHasPosition focus) {
 		this(viewport, focus, 1.0f);
 	}
 	
 	public IHasBounds getViewport() {
 		return _viewport;
 	}
 	
 	public void setViewport(IHasBounds value) {
 		_viewport = value;
 		setScaledCenter();
 	}
 	
 	public IHasPosition getFocus() {
 		return _focus;
 	}
 	
 	public void setFocus(IHasPosition value) {
 		_focus = value;
 	}
 	
 	public float getScale() {
 		return _scale;
 	}
 	
 	public void setScale(float scale) {
 		_scale = scale;
 		setScaledCenter();
 	}
 	
 	public Rectangle getBounds() {
 		return getViewport().getBounds();
 	}
 	
 	private void setScaledCenter() {
 		float scale = getScale();
 		Rectangle bounds = getBounds();
 		_scaledCenter = new Vector2f(bounds.getCenterX() / scale, bounds.getCenterY() / scale);
 	}
 	
 	public Vector3f getPosition() {
 		Vector3f focusPosition = getFocus().getPosition();
 		return new Vector3f(focusPosition.x, focusPosition.y, focusPosition.z);
 	}
 	
 	public void apply(Graphics g) {
 		float scale = getScale();
 		Vector3f position = getPosition();
 		
 		g.scale(scale, scale);
		g.translate((int)(_scaledCenter.x - position.x), (int)(_scaledCenter.y - position.y));
 	}
 	
 	public void reset(Graphics g) {
 		g.resetTransform();
 	}
 }
