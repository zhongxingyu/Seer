 package au.edu.unimelb.cis.dragons.java;
 
 import au.edu.unimelb.cis.dragons.core.CustomGraphics;
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
 import playn.core.PlayN.LifecycleListener;
 import playn.core.Pointer;
 import playn.core.RegularExpression;
 import playn.core.Storage;
 import playn.core.Touch;
import playn.core.UIOverlay;
 import playn.java.JavaPlatform;
 
 public class JavaCustomPlatform implements CustomPlatform {
 
 	private JavaPlatform _underlyingPlatform;
 	private CustomGraphics _customGraphics;
 	
 	public JavaCustomPlatform(JavaPlatform platform) {
 		_underlyingPlatform = platform;
 		_customGraphics = new JavaCustomGraphics(platform.graphics());
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

	@Override
 	public CustomGraphics graphics() {
 		return _customGraphics;
 	}
 
 }
