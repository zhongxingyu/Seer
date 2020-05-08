 package com.evervoid.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.state.geometry.Dimension;
 import com.jme3.math.Vector2f;
 
 public class UIControl extends EverNode
 {
 	public enum BoxDirection
 	{
 		HORIZONTAL, VERTICAL;
 	}
 
 	private static final float sDisabledAlpha = 0.5f;
 	private static final float sEnableDuration = 0.3f;
 	private Bounds aComputedBounds = null;
 	private final List<UIControl> aControls = new ArrayList<UIControl>();
 	private final BoxDirection aDirection;
 	private AnimatedAlpha aEnableAlpha = null;
 	private UIInputListener aFocusedElement = null;
 	private boolean aIsEnabled = true;
 	private Dimension aMinimumDimension = null;
 	private final Transform aOffset;
 	protected UIControl aParent = null;
 	private final Map<UIControl, Integer> aSprings = new HashMap<UIControl, Integer>();
 
 	public UIControl()
 	{
 		this(BoxDirection.HORIZONTAL);
 	}
 
 	public UIControl(final BoxDirection direction)
 	{
 		aDirection = direction;
 		aOffset = getNewTransform();
 		aOffset.translate(0, 0, 1);
 	}
 
 	void addChildUI(final UIControl control)
 	{
 		addChildUI(control, 0);
 	}
 
 	void addChildUI(final UIControl control, final int spring)
 	{
 		if (control == null) {
 			return;
 		}
 		if (aControls.contains(control)) {
 			System.err.println("Warning: Trying to add the same UIControl twice.");
 		}
 		aControls.add(control);
 		aSprings.put(control, spring);
 		addNode(control);
 		if (control instanceof UIControl) {
 			// Update parent
 			(control).aParent = this;
 		}
 		recomputeAllBounds();
 	}
 
 	/**
 	 * Adds a flexible spacer with spring
 	 * 
 	 * @param spring
 	 *            The spring of the spacer
 	 */
 	public void addFlexSpacer(final int spring)
 	{
 		addUI(new UIControl(), spring);
 	}
 
 	/**
 	 * Add a spacer to the inner UIControl, with no spring.
 	 * 
 	 * @param width
 	 *            The width of the spacer
 	 * @param height
 	 *            The height of the spacer
 	 */
 	public void addSpacer(final int width, final int height)
 	{
 		addUI(new SpacerControl(width, height));
 	}
 
 	/**
 	 * Add a control to the inner UIControl with no spring
 	 * 
 	 * @param control
 	 *            The control to add
 	 */
 	public void addUI(final UIControl control)
 	{
 		addUI(control, 0);
 	}
 
 	/**
 	 * Add a control to the inner UIControl. Overridden by container subclasses
 	 * 
 	 * @param control
 	 *            The control to add
 	 * @param spring
 	 *            The spring value
 	 */
 	public void addUI(final UIControl control, final int spring)
 	{
 		addChildUI(control, spring);
 	}
 
 	public boolean click(final Vector2f point)
 	{
 		if (!inBounds(point)) {
 			return false; // Out of bounds
 		}
 		final UIControl root = getRootUI();
 		final UIInputListener focusedNode = root.aFocusedElement;
 		if (this instanceof UIInputListener && !equals(focusedNode)) {
 			// Got new focused element
 			if (focusedNode != null) {
 				focusedNode.onDefocus();
 			}
 			((UIInputListener) this).onClick();
 		}
 		final Vector2f newPoint = new Vector2f(point.x - aComputedBounds.x, point.y - aComputedBounds.y);
 		for (final UIControl c : aControls) {
 			if (c.click(newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void delAllChildUIs()
 	{
 		delAllNodes();
 		aControls.clear();
 		recomputeAllBounds();
 	}
 
 	public void deleteChildUI(final UIControl control)
 	{
 		if (aControls.remove(control)) {
 			delNode(control);
 			// If removal was successful, recompute bounds
 			recomputeAllBounds();
 		}
 	}
 
 	public void deleteUI()
 	{
 		if (aParent != null) {
 			aParent.deleteChildUI(this);
 		}
 	}
 
 	public void disable()
 	{
 		if (!aIsEnabled) {
 			return;
 		}
 		if (aEnableAlpha == null) {
 			aEnableAlpha = getNewAlphaAnimation();
 			aEnableAlpha.setDuration(sEnableDuration).setAlpha(1);
 		}
 		aEnableAlpha.setTargetAlpha(sDisabledAlpha).start();
 		if (equals(getRootUI().aFocusedElement) && this instanceof UIInputListener) {
 			((UIInputListener) this).onDefocus();
 		}
 	}
 
 	public void enable()
 	{
 		if (aIsEnabled) {
 			return;
 		}
 		if (aEnableAlpha == null) {
 			aEnableAlpha = getNewAlphaAnimation();
 			aEnableAlpha.setDuration(sEnableDuration).setAlpha(sDisabledAlpha);
 		}
 		aIsEnabled = true;
 		aEnableAlpha.setTargetAlpha(1).start();
 	}
 
 	/**
 	 * @return The last-computed absolute bounds that this control has
 	 */
 	public Bounds getAbsoluteComputedBounds()
 	{
 		if (aComputedBounds == null) {
 			return null;
 		}
 		if (aParent == null) {
 			return aComputedBounds;
 		}
 		final Bounds parentBounds = aParent.getAbsoluteComputedBounds();
 		return new Bounds(parentBounds.x + aComputedBounds.x, parentBounds.y + aComputedBounds.y, aComputedBounds.width,
 				aComputedBounds.height);
 	}
 
 	public List<UIControl> getChildrenUIs()
 	{
 		return aControls;
 	}
 
 	/**
 	 * @return The last-computed bounds that this control has
 	 */
 	public Bounds getComputedBounds()
 	{
 		return aComputedBounds;
 	}
 
 	public Integer getComputedHeight()
 	{
 		if (aComputedBounds == null) {
 			return null;
 		}
 		return aComputedBounds.height;
 	}
 
 	public Integer getComputedWidth()
 	{
 		if (aComputedBounds == null) {
 			return null;
 		}
 		return aComputedBounds.width;
 	}
 
 	/**
 	 * @return The minimum size that this control wishes to have (should not be overridden)
 	 */
 	public Dimension getDesiredSize()
 	{
 		final Dimension minimum = getMinimumSize();
 		int totalWidth = minimum.width;
 		int totalHeight = minimum.height;
 		if (aMinimumDimension != null) {
 			totalWidth = Math.max(aMinimumDimension.width, totalWidth);
 			totalHeight = Math.max(aMinimumDimension.height, totalHeight);
 		}
 		return new Dimension(totalWidth, totalHeight);
 	}
 
 	/**
 	 * @return The minimum size that this control can handle (overridable by subclasses)
 	 */
 	public Dimension getMinimumSize()
 	{
 		int totalWidth = 0;
 		int totalHeight = 0;
 		for (final UIControl c : aControls) {
 			final Dimension d = c.getDesiredSize();
 			if (aDirection.equals(BoxDirection.HORIZONTAL)) {
 				totalWidth += d.width;
 				totalHeight = Math.max(totalHeight, d.height);
 			}
 			else {
 				totalWidth = Math.max(totalWidth, d.width);
 				totalHeight += d.height;
 			}
 		}
 		return new Dimension(totalWidth, totalHeight);
 	}
 
 	public int getNumChildrenUIs()
 	{
 		return aControls.size();
 	}
 
 	protected UIControl getRootUI()
 	{
 		if (aParent == null) {
 			return this;
 		}
 		UIControl parent = aParent;
 		while (parent.aParent != null) {
 			parent = parent.aParent;
 		}
 		return parent;
 	}
 
 	protected boolean inBounds(final Vector2f point)
 	{
 		return point != null
 				&& aComputedBounds != null
 				&& (aComputedBounds.x <= point.x && aComputedBounds.y <= point.y
 						&& aComputedBounds.x + aComputedBounds.width > point.x && aComputedBounds.y + aComputedBounds.height > point.y);
 	}
 
 	public boolean isEnabled()
 	{
 		return aIsEnabled;
 	}
 
 	public void onKeyPress(final KeyboardKey key)
 	{
 		final UIInputListener focused = getRootUI().aFocusedElement;
 		if (focused != null && !equals(focused)) {
 			focused.onKeyPress(key);
 		}
 	}
 
 	public void onKeyRelease(final KeyboardKey key)
 	{
 		final UIInputListener focused = getRootUI().aFocusedElement;
 		if (focused != null && !equals(focused)) {
 			focused.onKeyRelease(key);
 		}
 	}
 
 	public boolean onMouseMove(final Vector2f point)
 	{
 		if (!inBounds(point)) {
 			return false; // Out of bounds
 		}
 		final Vector2f newPoint = new Vector2f(point.x - aComputedBounds.x, point.y - aComputedBounds.y);
 		for (final UIControl c : aControls) {
 			if (c.onMouseMove(newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Recomputes all dimension of everything in the UI
 	 */
 	protected void recomputeAllBounds()
 	{
 		final UIControl root = getRootUI();
 		if (root.aComputedBounds != null) {
 			root.setBounds(root.aComputedBounds);
 		}
 	}
 
 	/**
 	 * Set the bounds that this control must fit into
 	 * 
 	 * @param bounds
 	 *            The bounds to fit into
 	 */
 	public void setBounds(final Bounds bounds)
 	{
 		aComputedBounds = bounds;
 		setControlOffset(bounds.x, bounds.y);
 		int availWidth = bounds.width;
 		int availHeight = bounds.height;
 		int totalSprings = 0;
 		final Map<UIControl, Dimension> minimumSizes = new HashMap<UIControl, Dimension>();
 		for (final UIControl c : aControls) {
 			final Dimension d = c.getDesiredSize();
 			minimumSizes.put(c, d);
 			availWidth -= d.width;
 			availHeight -= d.height;
 			totalSprings += aSprings.get(c);
 		}
 		float springSize = availWidth / Math.max(1, totalSprings);
 		final List<UIControl> controls = new ArrayList<UIControl>(aControls);
 		if (aDirection.equals(BoxDirection.VERTICAL)) {
 			springSize = availHeight / Math.max(1, totalSprings);
 			// If this is vertical, we want the first control to be at the top, so reverse the
 			Collections.reverse(controls);
 		}
 		int currentX = 0;
 		int currentY = 0;
 		for (final UIControl c : controls) {
 			final Dimension d = minimumSizes.get(c);
 			if (aDirection.equals(BoxDirection.HORIZONTAL)) {
 				final int cWidth = (int) (d.width + aSprings.get(c) * springSize);
 				c.setBounds(new Bounds(currentX, currentY, cWidth, bounds.height));
 				currentX += cWidth;
 			}
 			else {
 				final int cHeight = (int) (d.height + aSprings.get(c) * springSize);
 				c.setBounds(new Bounds(currentX, currentY, bounds.width, cHeight));
 				currentY += cHeight;
 			}
 		}
 	}
 
 	/**
 	 * Translates this UIControl by a certain amount. Subclasses requiring special positioning may override this
 	 * 
 	 * @param x
 	 *            The x offset
 	 * @param y
 	 *            The y offset
 	 */
 	protected void setControlOffset(final float x, final float y)
 	{
 		aOffset.translate(x, y);
 	}
 
 	public void setDesiredDimension(final Dimension minimum)
 	{
 		aMinimumDimension = minimum;
 	}
 
 	public void setEnabled(final boolean enabled)
 	{
 		if (enabled) {
 			enable();
 		}
 		else {
 			disable();
 		}
 	}
 
 	protected void setFocusedNode(final UIInputListener focused)
 	{
 		getRootUI().aFocusedElement = focused;
 	}
 
 	@Override
 	public String toString()
 	{
 		return toString("");
 	}
 
 	/**
 	 * Returns a fancy string representing this control
 	 * 
 	 * @param prefix
 	 *            The prefix to use before new lines
 	 * @return The fancy string
 	 */
 	public String toString(final String prefix)
 	{
 		String str = getClass().getSimpleName() + " - " + aComputedBounds + " with desired " + getDesiredSize() + " ("
 				+ aDirection.toString().toLowerCase() + ")";
 		if (aControls.isEmpty()) {
 			return str;
 		}
 		str += " {\n";
 		for (final UIControl c : aControls) {
 			str += prefix + "\tSpring " + aSprings.get(c) + ": " + c.toString(prefix + "\t") + "\n";
 		}
 		return str + prefix + "}";
 	}
 }
