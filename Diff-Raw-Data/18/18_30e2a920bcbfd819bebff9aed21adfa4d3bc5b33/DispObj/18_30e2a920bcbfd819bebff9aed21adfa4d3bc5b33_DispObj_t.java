 package com.thorgaming.throwme.displayobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Rect;
 
 import com.thorgaming.throwme.ThrowMe;
 import com.thorgaming.throwme.animation.Anim;
 import com.thorgaming.throwme.callback.MouseCallback;
 import com.thorgaming.throwme.drawing.Camera;
 import com.thorgaming.throwme.drawing.RenderPriority;
 
 public abstract class DispObj {
 
 	private int width = 0;
 	private int height = 0;
 	private int x = 0;
 	private int y = 0;
 	private int alpha = 255;
 	private boolean ignorePause = false;
 	protected Rect hitBox = new Rect();
 	private Rect hitRegion = new Rect();
 
 	public DispObj addToScreen(RenderPriority priority) {
 		ThrowMe.getInstance().stage.registerForRender(priority, this);
 		return this;
 	}
 
 	public DispObj addToScreen() {
 		return addToScreen(RenderPriority.Normal);
 	}
 
 	private MouseCallback onMouseUpEvent, onMouseDownEvent, onMouseMoveEvent;
 
 	public abstract void draw(Canvas canvas, Camera camera);
 
 	public DispObj setMouseUpEvent(MouseCallback event) {
 		onMouseUpEvent = event;
 		return this;
 	}
 
 	public MouseCallback getMouseUpEvent() {
 		return !ThrowMe.getInstance().stage.drawThread.getPaused() || ignorePause ? onMouseUpEvent : null;
 	}
 
 	public DispObj setMouseMoveEvent(MouseCallback event) {
 		onMouseMoveEvent = event;
 		return this;
 	}
 
 	public MouseCallback getMouseMoveEvent() {
 		return !ThrowMe.getInstance().stage.drawThread.getPaused() || ignorePause ? onMouseMoveEvent : null;
 	}
 
 	public DispObj setMouseDownEvent(MouseCallback event) {
 		onMouseDownEvent = event;
 		return this;
 	}
 
 	public MouseCallback getMouseDownEvent() {
 		return !ThrowMe.getInstance().stage.drawThread.getPaused() || ignorePause ? onMouseDownEvent : null;
 	}
 
 	public DispObj setAlpha(int alpha) {
 		this.alpha = alpha;
 		return this;
 	}
 
 	public DispObj setIgnorePause() {
 		ignorePause = true;
 		return this;
 	}
 
 	public int getAlpha() {
 		return alpha;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public void move(int x, int y) {
 		setX(x);
 		setY(y);
 	}
 
 	public DispObj setX(int x) {
 		this.x = x;
 		return this;
 	}
 
 	public DispObj setY(int y) {
 		this.y = y;
 		return this;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public Rect getHitPadding() {
 		return hitRegion;
 	}
 
 	public Rect getHitBox() {
 		return hitBox;
 	}
 
 	public DispObj setHitPadding(int hitPadding) {
 		hitRegion.set(0, 0, hitPadding, hitPadding);
 		return this;
 	}
 
 	public DispObj setHitPadding(Rect hitRegion) {
 		this.hitRegion.set(hitRegion);
 		return this;
 	}
 
 	public DispObj setWidth(int width) {
 		this.width = width;
 		return this;
 	}
 
 	public DispObj setHeight(int height) {
 		this.height = height;
 		return this;
 	}
 
 	public void destroy() {
 		List<Anim> over = new ArrayList<Anim>();
 		for (Anim i : ThrowMe.getInstance().stage.animations) {
 			if (i.getObject() == this) {
 				over.add(i);
 			}
 		}
 		ThrowMe.getInstance().stage.animations.removeAll(over);
 
 		ThrowMe.getInstance().stage.unregisterForRender(this);
 	}
 
 	public boolean checkPress(int x, int y) {
 		hitRegion.offsetTo(x - hitRegion.width() / 2, y - hitRegion.height() / 2);
		return Rect.intersects(hitRegion, hitBox);
 	}
 
 }
