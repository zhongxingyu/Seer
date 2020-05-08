 package krum.jtx;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JComponent;
 import javax.swing.JViewport;
 import javax.swing.ViewportLayout;
 
 /**
  * A scroll pane viewport layout manager that keeps its viewport scrolled to
  * the bottom and right edge of its client component if it was positioned at
  * those edges before the component's size increased.
  * <p>
  * The common way of achieving this is by queueing a <tt>Runnable</tt> task to
  * call {@link JComponent#scrollRectToVisible(Rectangle)} in the Swing event
  * thread.  But this approach often causes the scroll bars to be painted at
  * the original viewport position before the visible rectangle is adjusted,
  * then painted again at the new position, resulting in "bouncing" scroll bar
  * knobs.  <tt>StickyViewportLayout</tt> adjusts the viewport position while
  * laying out the scroll pane, so the scroll bars are painted only once and
  * the knobs do not bounce.
  * <p>
  * Additionally, if the client component implements {@link StickyScrollable},
  * the layout will honor the client's preferred scroll offset when it is not
  * stuck to the bottom or right edge.
  * 
  * @author Kevin Krumwiede (kjkrum@gmail.com)
  */
 public class StickyViewportLayout extends ViewportLayout {
 	private static final long serialVersionUID = 1L;
 	
 	protected final Dimension oldSize = new Dimension();
 	protected final Dimension newSize = new Dimension();
 	protected Rectangle visible = new Rectangle();
 	protected Point offset = new Point();
 
 	@Override
 	public void layoutContainer(Container owner) {
 		super.layoutContainer(owner);
 		JViewport viewport = (JViewport) owner;
 		Component view = viewport.getView();
 		if(view instanceof JComponent) {
 			((JComponent)view).computeVisibleRect(visible);
 		}
 		else visible = viewport.getViewRect();
 		view.getSize(newSize);
 		boolean doScroll = false;
 		boolean stickyClient = false;
 		
 		if(view instanceof StickyScrollable) {
 			((StickyScrollable) view).getPreferredScrollOffset(offset);
 			stickyClient = true;
 		}

 		// y axis
		// scroll to the bottom if the viewport was previously positioned at
		// the bottom, or if the client just became larger than the viewport
		// TODO handle case where viewport is resized - use oldVisible/newVisible?
		if (visible.y + visible.height == oldSize.height
				|| (newSize.height > visible.height && oldSize.height <= visible.height)) {
			visible.y = newSize.height - visible.height;
			doScroll = true;
		}		
 		else if(stickyClient && offset.y != 0) {
 			visible.y += offset.y;
 			if (visible.y < 0) visible.y = 0;
 			doScroll = true;
 		}
 		
 		// x axis
		if(visible.x + visible.width == oldSize.width
				|| (newSize.width > visible.width && oldSize.width <= visible.width)) {
			visible.x = newSize.width - visible.width;
			doScroll = true;
 		}
 		else if(stickyClient && offset.x != 0) {
 			visible.x += offset.x;
 			if (visible.x < 0) visible.x = 0;
 			doScroll = true;
 		}
 		
 		if(doScroll) viewport.setViewPosition(visible.getLocation());
 
 		oldSize.setSize(newSize);
 	}
 }
