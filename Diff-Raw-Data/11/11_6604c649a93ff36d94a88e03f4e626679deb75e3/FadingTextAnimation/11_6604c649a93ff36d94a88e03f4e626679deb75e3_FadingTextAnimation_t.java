 package asciiWorld.animations;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.geom.Vector2f;
 
 import asciiWorld.FontFactory;
 import asciiWorld.entities.Entity;
 
 public class FadingTextAnimation implements IAnimation {
 
 	private static final double MAX_LIVE_TIME = 3000.0;
 	private static final float SCALE = 0.25f; // this is based on the game world zoom level 
 	
 	private static UnicodeFont _font = null;
 	
 	private Entity _owner;
 	private String _text;
 	private Color _color;
 	private double _totalLiveTime;
 
 	public static FadingTextAnimation createDamageNotification(Entity owner, int amount) {
		return new FadingTextAnimation(owner, Integer.toString(amount), new Color(1.0f, 0.0f, 0.0f, 1.0f));
 	}
 	
 	public static FadingTextAnimation createRestoreNotification(Entity owner, int amount) {
		return new FadingTextAnimation(owner, Integer.toString(amount), new Color(0.0f, 1.0f, 0.0f, 1.0f));
 	}
 
 	public FadingTextAnimation(Entity owner, String text, Color color) {
 		_owner = owner;
 		_text = text;
 		_color = color;
 		_totalLiveTime = 0;
 
 		if (_font == null) {
 			try {
 				_font = FontFactory.get().getResource(12);
 			} catch (SlickException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public boolean isAlive() {
 		return _totalLiveTime < MAX_LIVE_TIME;
 	}
 
 	@Override
 	public void update(double deltaTime) {
 		_totalLiveTime += deltaTime;
 		_color.a = 1.0f - (float)(_totalLiveTime / MAX_LIVE_TIME);
 		return;
 	}
 	
 	private Vector2f getPosition() {
 		Vector2f position = _owner.getCenterPosition();
 		float width = _font.getWidth(_text) * SCALE;
 		float height = _font.getHeight(_text) * SCALE;
 		return new Vector2f(
 				position.x - width / 2,
 				position.y - height * (4 + 2 * (1 - _color.a)));
 	}
 
 	@Override
 	public void render(Graphics g) {
 		g.pushTransform();
 		g.translate(getPosition());
 		g.scale(SCALE, SCALE);
 		_font.drawString(0, 0, _text, _color);
 		g.popTransform();
 	}
 }
