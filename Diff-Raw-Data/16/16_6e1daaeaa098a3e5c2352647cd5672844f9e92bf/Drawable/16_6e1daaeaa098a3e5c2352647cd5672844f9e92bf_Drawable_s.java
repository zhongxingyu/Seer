 package initial3d.engine;
 
 import java.awt.AWTEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 
 import initial3d.Initial3D;
 
 public abstract class Drawable {
 
 	// external requests for state change
 	private volatile boolean request_focus = false;
 	private volatile boolean request_removal = false;
 	private volatile boolean request_input_enabled = false;
	private volatile boolean request_visible = false;
 	private volatile int requested_id_count = 1;
 
 	// the actual state
 	private volatile boolean focused = false;
 	private volatile boolean input_enabled = false;
	private volatile boolean visible = false;
 	private volatile int draw_id_start = 0;
 	private volatile int draw_id_count = 0;
 
 	public final void requestFocus() {
 		request_focus = true;
 	}
 
 	public final void requestRemoval() {
 		request_removal = true;
 	}
 
 	public final void requestInputEnabled(boolean b) {
 		request_input_enabled = b;
 	}
 
 	public final void requestVisible(boolean b) {
 		request_visible = b;
 	}
 
 	/* package-private */
 	final boolean pollFocusRequested() {
 		try {
 			return request_focus;
 		} finally {
 			request_focus = false;
 		}
 	}
 
 	/* package-private */
 	final boolean pollRemovalRequested() {
 		try {
 			return request_removal;
 		} finally {
 			request_removal = false;
 		}
 	}
 
 	/* package-private */
 	final int pollRequestedIDCount() {
 		return requested_id_count;
 	}
 
 	/* package-private */
 	/**
 	 * Called once per frame (immediately-ish) prior to when a call to draw() would happen, if it happens or not.
 	 */
 	final void update() {
 		input_enabled = request_input_enabled;
 		visible = request_visible;
 	}
 
 	/* package-private */
 	final void setFocused(boolean b) {
 		focused = b;
 	}
 
 	/* package-private */
 	final void setDrawIDs(int id, int count) {
 		draw_id_start = id;
 		draw_id_count = count;
 	}
 
 	/**
 	 * Returns whether this drawable is visible. This is only updated at the same time as input enabled. Invisible
 	 * drawables behave as if isInputEnabled returned false.
 	 */
 	public final boolean isVisible() {
 		return visible;
 	}
 
 	/**
 	 * Returns whether this Drawable has input focus. This is only updated once per frame, prior to the processing of
 	 * input events.
 	 */
 	public final boolean isFocused() {
 		return focused;
 	}
 
 	/**
 	 * Returns whether input is enabled for this frame. This is only updated once per frame (immediately-ish) prior to
 	 * when a call to draw() would happen, if it happens or not.
 	 */
 	public final boolean isInputEnabled() {
 		return input_enabled;
 	}
 
 	/**
 	 * Returns the start of the contiguous set of ids that this Drawable was allocated to use when drawing itself the
 	 * last time <code>isInputEnabled()</code> returned true from a call by the SceneManager.
 	 */
 	public final int getDrawIDStart() {
 		return draw_id_start;
 	}
 
 	/**
 	 * Returns the length of the contiguous set of ids that this Drawable was allocated to use when drawing itself the
 	 * last time <code>isInputEnabled()</code> returned true from a call by the SceneManager.
 	 */
 	public final int getDrawIDCount() {
 		return draw_id_count;
 	}
 
 	/**
 	 * Set the number of draw ids that will be allocated to this drawable iff <code>isInputEnabled()</code> returns true
 	 * from a call by the SceneManager.
 	 */
 	protected final void setRequestedIDCount(int count) {
 		requested_id_count = count;
 	}
 
 	/**
 	 * Assuming that this Drawable currently has focus, determines if it will allow the focus to be released to another
 	 * Drawable, including <code>null</code>.
 	 */
 	public boolean releaseFocusTo(Drawable d) {
 		return true;
 	}
 
 	/**
 	 * Override this to draw stuff. Enabling and disabling WRITE_ID is handled by the SceneManager.
 	 */
 	protected abstract void draw(Initial3D i3d);
 
 	protected void dispatchEvent(AWTEvent e, int drawid) {
 
 	}
 
 }
