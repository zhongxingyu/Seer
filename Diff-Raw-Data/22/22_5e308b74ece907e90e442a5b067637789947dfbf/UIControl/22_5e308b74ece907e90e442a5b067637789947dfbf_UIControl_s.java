 package com.evervoid.client.ui;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.EverVoidClient;
 import com.evervoid.client.KeyboardKey;
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.graphics.geometry.FrameTimer;
 import com.evervoid.client.graphics.geometry.Transform;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.state.geometry.Dimension;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class UIControl extends EverNode
 {
 	public enum BoxDirection
 	{
 		HORIZONTAL, VERTICAL;
 	}
 
 	static final float sChildZOffset = 0.000001f;
 	private static final float sDisabledAlpha = 0.5f;
 	private static final float sEnableDuration = 0.3f;
 	private static final float sTooltipTimer = 0.6f;
 	private final Set<ClickObserver> aClickObservers = new HashSet<ClickObserver>(0);
 	protected Bounds aComputedBounds = null;
 	private final List<UIControl> aControls = new ArrayList<UIControl>(0);
 	private ColorRGBA aDefaultColor = ColorRGBA.White;
 	private final BoxDirection aDirection;
 	private AnimatedAlpha aEnableAlpha = null;
 	private UIInputListener aFocusedElement = null;
 	private boolean aHasTooltip;
 	private boolean aHoverSelectable = false;
 	private FrameTimer aHoverSelectTimer;
 	private boolean aIsEnabled = true;
 	private Dimension aMinimumDimension = null;
 	private final Transform aOffset;
 	protected UIControl aParent = null;
 	private boolean aSelected = false;
 	private UIHoverSelect aSelectNode = null;
 	private final Map<UIControl, Integer> aSprings = new HashMap<UIControl, Integer>(0);
 	private UITooltip aTooltip = null;
 	private FrameTimer aTooltipTimer = null;
 
 	public UIControl()
 	{
 		this(BoxDirection.HORIZONTAL);
 	}
 
 	public UIControl(final BoxDirection direction)
 	{
 		aDirection = direction;
 		aOffset = getNewTransform();
 		aOffset.translate(0, 0, sChildZOffset);
 		aHoverSelectTimer = null;
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
 		// Update parent
 		control.aParent = this;
 		recomputeAllBounds();
 	}
 
 	/**
 	 * Adds a flexible spacer with spring
 	 * 
 	 * @param spring
 	 *            The spring of the spacer
 	 */
 	public UIControl addFlexSpacer(final int spring)
 	{
 		addUI(new UIControl(), spring);
 		return this;
 	}
 
 	/**
 	 * Add a spacer to the inner UIControl, with no spring.
 	 * 
 	 * @param width
 	 *            The width of the spacer
 	 * @param height
 	 *            The height of the spacer
 	 */
 	public UIControl addSpacer(final int width, final int height)
 	{
 		addUI(new SpacerControl(width, height));
 		return this;
 	}
 
 	public void addString(final String contents)
 	{
 		addString(contents, aDefaultColor);
 	}
 
 	public void addString(final String text, final BoxDirection vertical)
 	{
 		addString(text, aDefaultColor, StaticTextControl.sDefaultFont, StaticTextControl.sDefaultSize, null);
 	}
 
 	public void addString(final String text, final ColorRGBA color)
 	{
 		addString(text, color, StaticTextControl.sDefaultFont, StaticTextControl.sDefaultSize, null);
 	}
 
 	public void addString(final String text, final ColorRGBA color, final BoxDirection direction)
 	{
 		addString(text, color, StaticTextControl.sDefaultFont, StaticTextControl.sDefaultSize, direction);
 	}
 
 	public void addString(final String text, final ColorRGBA color, final String font, final int size)
 	{
		addString(text, color, StaticTextControl.sDefaultFont, StaticTextControl.sDefaultSize, null);
 	}
 
 	public void addString(final String text, final ColorRGBA color, final String font, final int size,
 			final BoxDirection direction)
 	{
 		if (direction == null) {
 			// catch the null case
 			addUI(new StaticTextControl(text, color, font, size));
 			return;
 		}
 		switch (direction) {
 			case HORIZONTAL:
 				addUI(new HorizontalCenteredControl(new StaticTextControl(text, color, font, size)));
 				break;
 			case VERTICAL:
 				addUI(new VerticalCenteredControl(new StaticTextControl(text, color, font, size)));
 				break;
 			default:
 				// what the?
 				addUI(new StaticTextControl(text, color, font, size));
 		}
 	}
 
 	/**
 	 * Add a control to the inner UIControl with no spring
 	 * 
 	 * @param control
 	 *            The control to add
 	 */
 	public UIControl addUI(final UIControl control)
 	{
 		addUI(control, 0);
 		return this;
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
 
 	@Override
 	protected void cleanUp()
 	{
 		super.cleanUp();
 		if (aTooltip != null) {
 			aTooltip.close();
 		}
 	}
 
 	public boolean click(final Vector2f point)
 	{
 		if (!inBounds(point)) {
 			return false; // Out of bounds
 		}
 		for (final ClickObserver observer : aClickObservers) {
 			observer.uiClicked(this);
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
 		for (final UIControl c : new ArrayList<UIControl>(getChildrenUIs())) {
 			if (c.click(newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	void closeTooltip()
 	{
 		if (aTooltipTimer != null) {
 			aTooltipTimer.stop();
 			aTooltipTimer = null;
 		}
 		if (aTooltip != null) {
 			aTooltip.close();
 		}
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
 			control.aParent = null;
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
 		aIsEnabled = false;
 		aEnableAlpha.setTargetAlpha(sDisabledAlpha).start();
 		if (equals(getRootUI().aFocusedElement) && this instanceof UIInputListener) {
 			((UIInputListener) this).onDefocus();
 		}
 	}
 
 	public void disableTooltop()
 	{
 		aHasTooltip = false;
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
 
 	public void enableTooltip()
 	{
 		aHasTooltip = true;
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
 
 	@Override
 	public Collection<EverNode> getEffectiveChildren()
 	{
 		final Collection<EverNode> normalChildren = super.getEffectiveChildren();
 		if (aTooltip == null) {
 			return normalChildren;
 		}
 		final List<EverNode> withExtras = new ArrayList<EverNode>(normalChildren.size() + 1);
 		withExtras.addAll(normalChildren);
 		if (aTooltip != null) {
 			withExtras.add(aTooltip);
 		}
 		return withExtras;
 	}
 
 	public float getMaxZOffset()
 	{
 		float maxZ = 0;
 		for (final UIControl control : getChildrenUIs()) {
 			maxZ = Math.max(maxZ, control.getMaxZOffset());
 		}
 		return sChildZOffset + maxZ + (aParent == null ? sChildZOffset : 0);
 	}
 
 	public int getMinimumHeight()
 	{
 		return getMinimumSize().height;
 	}
 
 	/**
 	 * @return The minimum size that this control can handle (overridable by subclasses)
 	 */
 	public Dimension getMinimumSize()
 	{
 		int totalWidth = 0;
 		int totalHeight = 0;
 		for (final UIControl c : getChildrenUIs()) {
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
 
 	public int getMinimumWidth()
 	{
 		return getMinimumSize().width;
 	}
 
 	public int getNumChildrenUIs()
 	{
 		return getChildrenUIs().size();
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
 
 	protected boolean inAbsoluteBounds(final Vector2f point)
 	{
 		return aComputedBounds != null && point != null && getAbsoluteComputedBounds().contains(point.x, point.y);
 	}
 
 	protected boolean inBounds(final Vector2f point)
 	{
 		return point != null && aComputedBounds != null && aComputedBounds.contains(point);
 	}
 
 	public boolean isEnabled()
 	{
 		return aIsEnabled;
 	}
 
 	public boolean onKeyPress(final KeyboardKey key)
 	{
 		final UIInputListener focused = getRootUI().aFocusedElement;
 		if (focused != null && !equals(focused)) {
 			if (focused.onKeyPress(key)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onKeyRelease(final KeyboardKey key)
 	{
 		final UIInputListener focused = getRootUI().aFocusedElement;
 		if (focused != null && !equals(focused)) {
 			if (focused.onKeyRelease(key)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onMouseMove(final Vector2f point)
 	{
 		if (!inBounds(point)) {
 			closeTooltip();
 			if (aHoverSelectable) {
 				setSelected(false);
 			}
 			return false; // Out of bounds
 		}
 		if (aHoverSelectable) {
 			setSelected(true);
 		}
 		final Vector2f newPoint = new Vector2f(point.x - aComputedBounds.x, point.y - aComputedBounds.y);
 		// see if the control wants to do something about this loading
 		if (aHasTooltip && aTooltipTimer == null) {
 			// warn the control
 			toolTipLoading();
 			if (aTooltip != null) {
 				// tooltip is present and not shown, load it
 				aTooltipTimer = new FrameTimer(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						if (aTooltip != null) {
 							aTooltip.show();
 						}
 					}
 				}, sTooltipTimer, 1).start();
 			}
 		}
 		for (final UIControl c : getChildrenUIs()) {
 			if (c.onMouseMove(newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onMouseWheelDown(final float delta, final Vector2f position)
 	{
 		if (!inBounds(position)) {
 			return false; // Out of bounds
 		}
 		final Vector2f newPoint = new Vector2f(position.x - aComputedBounds.x, position.y - aComputedBounds.y);
 		for (final UIControl control : getChildrenUIs()) {
 			if (control.onMouseWheelDown(delta, newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean onMouseWheelUp(final float delta, final Vector2f position)
 	{
 		if (!inBounds(position)) {
 			return false; // Out of bounds
 		}
 		final Vector2f newPoint = new Vector2f(position.x - aComputedBounds.x, position.y - aComputedBounds.y);
 		for (final UIControl control : getChildrenUIs()) {
 			if (control.onMouseWheelUp(delta, newPoint)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void pollMouse()
 	{
 		if (aHoverSelectable && !getAbsoluteComputedBounds().contains(EverVoidClient.sCursorPosition)) {
 			setSelected(false); // Will stop the timer
 		}
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
 
 	public void registerClickObserver(final ClickObserver observer)
 	{
 		aClickObservers.add(observer);
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
 		for (final UIControl c : getChildrenUIs()) {
 			final Dimension d = c.getDesiredSize();
 			minimumSizes.put(c, d);
 			availWidth -= d.width;
 			availHeight -= d.height;
 			totalSprings += aSprings.get(c);
 		}
 		float springSize = availWidth / Math.max(1, totalSprings);
 		final List<UIControl> controls = new ArrayList<UIControl>(getChildrenUIs());
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
 		if (aTooltip != null) {
 			aTooltip.parentBoundsChanged();
 		}
 		if (aSelectNode != null) {
 			aSelectNode.parentBoundsChanged();
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
 
 	public void setDefaultTextColor(final ColorRGBA color)
 	{
 		if (color != null) {
 			aDefaultColor = color;
 		}
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
 
 	public void setHoverSelectable(final boolean hoverable)
 	{
 		aHoverSelectable = hoverable;
 		if (aHoverSelectable) {
 			aHoverSelectTimer = new FrameTimer(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					pollMouse();
 				}
 			}, 0.2f);
 		}
 		else if (aHoverSelectTimer != null) {
 			aHoverSelectTimer.stop();
 			aHoverSelectTimer = null;
 		}
 		setSelected(getAbsoluteComputedBounds() != null && getAbsoluteComputedBounds().contains(EverVoidClient.sCursorPosition));
 	}
 
 	public void setSelected(final boolean selected)
 	{
 		aSelected = selected;
 		if (aSelected && aSelectNode == null) {
 			aSelectNode = new UIHoverSelect(this);
 			aSelectNode.smoothAppear(0.2f);
 		}
 		else if (!aSelected && aSelectNode != null) {
 			aSelectNode.smoothDisappear(0.2f);
 			aSelectNode = null;
 		}
 		if (aHoverSelectable) {
 			if (aSelected) {
 				aHoverSelectTimer.start();
 			}
 			else {
 				aHoverSelectTimer.stop();
 			}
 		}
 	}
 
 	public void setTooltip(final String content)
 	{
 		setTooltip(new StaticTextControl(content, aDefaultColor));
 	}
 
 	/**
 	 * Sets the tooltip that should be shown when hovering this control; null if no tooltip should be shown.
 	 * 
 	 * @param control
 	 *            The tooltip, or null to disable tooltips
 	 */
 	public void setTooltip(final UIControl control)
 	{
 		aTooltip = new UITooltip(control, this);
 	}
 
 	/**
 	 * Called when the tooltip is about to be shown. Subclasses should override this if they desire to generate the tooltip now.
 	 */
 	protected void toolTipLoading()
 	{
 		// Overridden by subclasses
 	}
 
 	/**
 	 * @return A Shortened string representing this control.
 	 */
 	public String toShortString()
 	{
 		return getClass().getSimpleName() + " - " + aComputedBounds + " with desired " + getDesiredSize() + " ("
 				+ aDirection.toString().toLowerCase() + ")";
 	}
 
 	@Override
 	public String toString()
 	{
 		return toString("");
 	}
 
 	/**
 	 * Returns a fancy string representing this control. Includes all children.
 	 * 
 	 * @param prefix
 	 *            The prefix to use before new lines
 	 * @return The fancy string
 	 */
 	public String toString(final String prefix)
 	{
 		String str = toShortString();
 		if (getChildrenUIs().isEmpty()) {
 			return str;
 		}
 		str += " {\n";
 		for (final UIControl c : getChildrenUIs()) {
 			str += prefix + "\tSpring " + aSprings.get(c) + ": " + c.toString(prefix + "\t") + "\n";
 		}
 		return str + prefix + "}";
 	}
 }
