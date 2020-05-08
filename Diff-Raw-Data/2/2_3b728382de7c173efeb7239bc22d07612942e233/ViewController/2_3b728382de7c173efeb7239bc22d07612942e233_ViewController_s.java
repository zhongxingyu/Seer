 package au.edu.unimelb.cis.dragons.core.controller;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import au.edu.unimelb.cis.dragons.core.screen.DragonGameScreen;
 import tripleplay.ui.Group;
import tripleplay.ui.SizableGroup;
import tripleplay.ui.layout.AxisLayout;
 import tripleplay.ui.layout.FlowLayout;
 
 /**
  * A controller for a given view.
  * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
  */
 public class ViewController {
 	
 	// The screen that this ViewController will be presented on.
 	private DragonGameScreen _parentScreen;
 	
 	// The view that this ViewController will be presented as a part of.
 	private ViewController _parentViewController;
 	
 	// The view that this ViewController is managing.
 	private Group _view;
 	
 	// Those ViewControllers that have their views on screen as components of
 	// this view.
 	private Set<ViewController> _subViewControllers =
 		new HashSet<ViewController>();
 	
 	// Is the ViewController's view currently being displayed?
 	private boolean _currentlyOnScreen = false;
 	
 	// Is the ViewController on the view stack?
 	private boolean _currentlyOnStack = false;
 
 	/** Return the view that this controller is managing. */
 	public final Group view() {
 		if (_view == null) {
 			_view = createInterface();
 		}
 		return _view;
 	}
 	
 	/** 
 	 * Set the screen that this view is going to be displayed on.
 	 * @param parentScreen The screen that this view will be displayed on.
 	 */
 	public void setParentScreen(DragonGameScreen parentScreen) {
 		_parentScreen = parentScreen;
 		for (final ViewController controller : _subViewControllers) {
 			controller.setParentScreen(parentScreen);
 		}
 	}
 	
 	/**
 	 * Add a sub ViewController to this ViewController.
 	 * @param child The ViewController to add as a child.
 	 */
 	protected final void addSubViewController(ViewController child) {
 		_subViewControllers.add(child);
 		child.setParentViewController(this);
 	}
 	
 	/**
 	 * Remove a sub ViewController from this ViewController.
 	 * @param child The ViewController to remove.
 	 */
 	protected final void removeSubViewController(ViewController child) {
 		if (child != null) {
 			_subViewControllers.remove(child);
 			child.setParentViewController(null);
 		}
 	}
 	
 	/**
 	 * Returns true if the ViewController's view is currently on screen.
 	 * @return Whether the ViewController's view is currently on screen.
 	 */
 	protected final boolean currentlyOnScreen() {
 		return _currentlyOnScreen;
 	}
 	
 	/**
 	 * Returns true if the ViewController's view is currently in the stack.
 	 * @return Whether the ViewController's view is currently in the stack.
 	 */
 	protected final boolean currentlyOnStack() {
 		return _currentlyOnStack;
 	}
 	
 	/**
 	 * Returns the screen that this view will be displayed on.
 	 * @return The screen that this view will be displayed on.
 	 */
 	protected final DragonGameScreen parentScreen() {
 		return _parentScreen;
 	}
 	
 	// The following methods can be overridden to customize the ViewController.
 
 	/** 
 	 * Called when the parent screen is added to the stack for the first time.
 	 */
 	public void wasAdded() {
 		_currentlyOnStack = true;
 		for (final ViewController child : _subViewControllers) {
 			child.wasAdded();
 		}
 	}
 
 	/** 
 	 * Called when the parent screen becomes the top screen and is made
 	 * visible
 	 */
 	public void wasShown() {
 		_currentlyOnScreen = true;
 		for (final ViewController child : _subViewControllers) {
 			child.wasShown();
 		}
 	}
 
 	/** 
 	 * Called when the parent screen is no longer the top screen. 
 	 */
 	public void wasHidden() {
 		_currentlyOnScreen = false;
 		for (final ViewController child : _subViewControllers) {
 			child.wasHidden();
 		}
 	}
 
 	/** 
 	 * Called when the parent screen is removed from the stack. 
 	 */
 	public void wasRemoved() {
 		_currentlyOnStack = false;
 		for (final ViewController child : _subViewControllers) {
 			child.wasRemoved();
 		}
 		_view.destroyAll();
 		_view = null;
 	}
 	
 	/**
 	 * Return the title for this ViewController's view.
 	 * Override this method to return a human readable name for this
 	 * ViewController's view that will be used in the UI.
 	 * @return The title for this ViewController's view.
 	 */
 	public String title() {
 		return "";
 	}
 	
 	/** 
 	 * Create the view for this ViewController and return it, ready to
 	 * be added to another view, or a screen.
 	 * Override this method to return the interface for your view.
 	 * @return Group The view for this ViewController.
 	 */
 	protected Group createInterface() {
 		return new Group(new FlowLayout());
 	}
 	
 	/**
 	 * Set the parent ViewController for this view.
 	 * @param parent The parent of this ViewController.
 	 */
 	public void setParentViewController(ViewController parent) {
 		_parentViewController = parent;
 	}
 	
 	/**
 	 * Get the parent ViewController for this view.
 	 * @return The parent ViewController.
 	 */
 	protected ViewController parentViewController() {
 		return _parentViewController;
 	}
 
 }
