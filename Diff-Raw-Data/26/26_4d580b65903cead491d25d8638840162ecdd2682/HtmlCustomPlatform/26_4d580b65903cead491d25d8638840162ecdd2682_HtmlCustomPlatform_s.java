 package au.edu.unimelb.cis.dragons.html;
 
 import au.edu.unimelb.cis.dragons.core.CustomPlatform;
 import playn.core.Analytics;
 import playn.core.Assets;
 import playn.core.Audio;
 import playn.core.Game;
 import playn.core.Json;
 import playn.core.Keyboard;
 import playn.core.Log;
 import playn.core.Mouse;
 import playn.core.Net;
 import playn.core.Platform;
 import playn.core.PlayN.LifecycleListener;
 import playn.core.Pointer;
 import playn.core.RegularExpression;
 import playn.core.Storage;
 import playn.core.Touch;
import playn.core.UIOverlay;
 import playn.html.HtmlPlatform;
 
 public class HtmlCustomPlatform implements CustomPlatform {
 	
 	// The custom graphics object that we drop-in.
 	private HtmlCustomGraphics _customGraphics;
 	
 	// The underlying Platform object that we forward calls to.
 	private Platform _underlyingPlatform;
 	
 	/**
 	 * Create a new custom Platform that wraps a regular platform, allowing the
 	 * drop-in of a custom Graphics implementation.
 	 * 
 	 * @param p The platform that we're wrapping.
 	 */
 	public HtmlCustomPlatform(HtmlPlatform p) {
 		_underlyingPlatform = p;
 		_customGraphics = new HtmlCustomGraphics(p.graphics());
 	}
 
 	@Override
 	public HtmlCustomGraphics graphics() {
 		return _customGraphics;
 	}
 
 	@Override
 	public void run(Game game) {
 		_underlyingPlatform.run(game);
 	}
 
 	@Override
 	public Type type() {
 		return _underlyingPlatform.type();
 	}
 
 	@Override
 	public double time() {
 		return _underlyingPlatform.time();
 	}
 
 	@Override
 	public int tick() {
 		return _underlyingPlatform.tick();
 	}
 
 	@Override
 	public float random() {
 		return _underlyingPlatform.random();
 	}
 
 	@Override
 	public void openURL(String url) {
 		_underlyingPlatform.openURL(url);
 	}
 
 	@Override
 	public void invokeLater(Runnable runnable) {
 		_underlyingPlatform.invokeLater(runnable);
 	}
 
 	@Override
 	public void setLifecycleListener(LifecycleListener listener) {
 		_underlyingPlatform.setLifecycleListener(listener);
 	}
 
 	@Override
 	public void setPropagateEvents(boolean propagate) {
 		_underlyingPlatform.setPropagateEvents(propagate);
 	}
 
 	@Override
 	public Audio audio() {
 		return _underlyingPlatform.audio();
 	}
 
 	@Override
 	public Assets assets() {
 		return _underlyingPlatform.assets();
 	}
 
 	@Override
 	public Json json() {
 		return _underlyingPlatform.json();
 	}
 
 	@Override
 	public Keyboard keyboard() {
 		return _underlyingPlatform.keyboard();
 	}
 
 	@Override
 	public Log log() {
 		return _underlyingPlatform.log();
 	}
 
 	@Override
 	public Net net() {
 		return _underlyingPlatform.net();
 	}
 
 	@Override
 	public Pointer pointer() {
 		return _underlyingPlatform.pointer();
 	}
 
 	@Override
 	public Mouse mouse() {
 		return _underlyingPlatform.mouse();
 	}
 
 	@Override
 	public Touch touch() {
 		return _underlyingPlatform.touch();
 	}
 
 	@Override
 	public Storage storage() {
 		return _underlyingPlatform.storage();
 	}
 
 	@Override
 	public Analytics analytics() {
 		return _underlyingPlatform.analytics();
 	}
 
 	@Override
 	public RegularExpression regularExpression() {
 		return _underlyingPlatform.regularExpression();
 	}
 
	@Override
	public UIOverlay uiOverlay() {
		return _underlyingPlatform.uiOverlay();
	}

 }
