 package com.evervoid.client.views.solar;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.evervoid.client.graphics.GraphicsUtils;
 import com.evervoid.client.graphics.Grid;
 import com.evervoid.client.graphics.GridNode;
 import com.evervoid.client.graphics.geometry.AnimatedAlpha;
 import com.evervoid.client.views.solar.UIProp.PropState;
 import com.evervoid.state.SolarSystem;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.geometry.Point;
 import com.evervoid.state.prop.Prop;
 import com.evervoid.state.prop.Ship;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 /**
  * This class represents the grid displayed when in the solar system view.
  */
 public class SolarGrid extends Grid
 {
 	static final int sCellSize = 64;
 	private final SolarGridSelection aGridHover;
 	private SolarGridHighlightLocations aHighlightedLocations = null;
 	private final Map<Prop, UIProp> aProps = new HashMap<Prop, UIProp>();
 	private Prop aSelectedProp = null;
 	private Dimension aSelectionSize = new Dimension(1, 1);
 	private final SolarSystem aSolarSystem;
 	private final SolarView aSolarSystemView;
 	private final ColorRGBA aStarGlowColor;
 	private final ShipTrailManager aTrailManager = new ShipTrailManager(this);
 
 	/**
 	 * Default constructor generating
 	 * 
 	 * @param view
 	 *            The solar system view to generate the grid in.
 	 * @param ss
 	 *            The solar system represented by this grid.
 	 */
 	public SolarGrid(final SolarView view, final SolarSystem ss)
 	{
 		super(ss.getDimension(), sCellSize, sCellSize, 1, new ColorRGBA(1f, 1f, 1f, 0.2f));
 		aSolarSystemView = view;
 		aSolarSystem = ss;
 		aStarGlowColor = GraphicsUtils.getColorRGBA(ss.getSunShadowColor());
 		aGridHover = new SolarGridSelection();
 		addNode(aGridHover);
 	}
 
 	/**
 	 * Adds a GridNode to the Grid. Called by UIProp itself. It is (always) a UIProp that gets added, so add the corresponding
 	 * mapping too.
 	 */
 	@Override
 	protected void addGridNode(final GridNode node)
 	{
 		super.addGridNode(node);
 		if (node instanceof UIProp) {
 			final UIProp prop = (UIProp) node;
 			aProps.put(prop.getProp(), prop);
 		}
 	}
 
 	@Override
 	public void computeTransforms()
 	{
 		super.computeTransforms();
 		aSolarSystemView.computeGridDimensions();
 	}
 
 	/**
 	 * Deletes a GridNode from the Grid. Called by UIProp itself. It is (always) a UIProp that gets deleted, so delete the
 	 * corresponding mapping too.
 	 */
 	@Override
 	protected void delGridNode(final GridNode node)
 	{
 		super.delGridNode(node);
 		if (node instanceof UIProp) {
 			final UIProp prop = (UIProp) node;
 			aProps.remove(prop.getProp());
 		}
 	}
 
 	/**
 	 * Out of a series of Props, returns the one which is the closest to the given Grid-based position. Used for selection
 	 * "spanning" to the closest prop
 	 * 
 	 * @param position
 	 *            The Grid-based position to look at
 	 * @param props
 	 *            The series of props to consider
	 * @param ignoreSelected
	 *            Whether to ignore the currently-selected prop or not
 	 * @return The closest prop, or null if the series was empty
 	 */
	private Prop getClosestPropTo(final Vector2f position, final Iterable<Prop> props, final boolean ignoreSelected)
 	{
 		float minDistance = Float.MAX_VALUE;
 		Prop closest = null;
 		for (final Prop p : props) {
			if (ignoreSelected && p.equals(aSelectedProp)) {
				continue;
			}
 			final float propDistance = getCellBounds(p.getLocation()).getClosestTo(position).length();
 			if (propDistance < minDistance) {
 				closest = p;
 				minDistance = propDistance;
 			}
 		}
 		return closest;
 	}
 
 	/**
 	 * @return AnimatedAlpha pointer to the nodes hosting the white lines of the grid
 	 */
 	AnimatedAlpha getLineAlphaAnimation()
 	{
 		return aLines.getNewAlphaAnimation();
 	}
 
 	/**
 	 * Finds if there is a UIProp at the given point
 	 * 
 	 * @param position
 	 *            The point to look at
 	 * @return The UIProp at the given point, or null if there is no prop there
 	 */
 	UIProp getPropAt(final Point point)
 	{
 		final Prop found = aSolarSystem.getPropAt(point);
 		if (found == null) {
 			return null;
 		}
 		return aProps.get(found);
 	}
 
 	/**
 	 * @return A GridLocation where the sun is located.
 	 */
 	public GridLocation getSunLocation()
 	{
 		return aSolarSystem.getSunLocation();
 	}
 
 	/**
 	 * @return The glow color of the sun
 	 */
 	public ColorRGBA getSunShadowColor()
 	{
 		return aStarGlowColor;
 	}
 
 	/**
 	 * @return A Trail Manager for this solar system grid.
 	 */
 	public ShipTrailManager getTrailManager()
 	{
 		return aTrailManager;
 	}
 
 	/**
 	 * Handle hover events on the grid
 	 * 
 	 * @param position
 	 *            Grid-based position that was hovered
 	 * @return Whether the cursor was on the grid or not
 	 */
 	boolean hover(final Vector2f position)
 	{
 		final GridLocation pointed = getCellAt(position, aSelectionSize);
 		if (pointed == null) {
 			// Mouse is out of the grid
 			aGridHover.fadeOut();
 			return false;
 		}
 		else {
 			// Mouse is in the grid
 			aGridHover.fadeIn();
 		}
 		// Take care of selection square
		final Prop prop = getClosestPropTo(position, aSolarSystem.getPropsAt(pointed), true);
 		if (prop == null) {
 			aGridHover.goTo(pointed);
 		}
 		else {
 			aGridHover.goTo(prop.getLocation());
 		}
 		// tmpShip.faceTowards(hoveredPoint);
 		return true;
 	}
 
 	/**
 	 * Handle left click events on the grid
 	 * 
 	 * @param position
 	 *            The Grid-based position that was clicked
 	 */
 	void leftclick(final Vector2f position)
 	{
 		// FIXME: This is hax for testing actions; should be moved to Solar Grid when done
 		final GridLocation pointed = getCellAt(position, aSelectionSize);
 		if (pointed == null) {
 			return; // User clicked outside of grid, don't go further
 		}
		final Prop prop = getClosestPropTo(position, aSolarSystem.getPropsAt(pointed), true);
 		leftClickProp(prop);
 		/*
 		 * if (gridPoint != null && prop != null) { if (prop.equals(aGrid.aSelectedProp)) { // prop is selected, make it carry
 		 * out an action if (prop instanceof Ship) { final ArrayList<GridLocation> rand = new ArrayList<GridLocation>(); // add
 		 * two random points for now rand.add(new GridLocation(MathUtils.getRandomIntBetween(0, aSolarSystem.getWidth() - 1),
 		 * MathUtils .getRandomIntBetween(0, aSolarSystem.getHeight() - 1))); rand.add(new
 		 * GridLocation(MathUtils.getRandomIntBetween(0, aSolarSystem.getWidth() - 1), MathUtils .getRandomIntBetween(0,
 		 * aSolarSystem.getHeight() - 1))); final MoveShip action = new MoveShip(prop.getPlayer(), (Ship) prop, rand);
 		 * GameView.commitAction(action); } else if (prop instanceof Planet) { final ConstructShip action = new
 		 * ConstructShip(prop.getPlayer(), (Planet) prop, "scout", GameView.getState()); GameView.commitAction(action); } } else
 		 * { aGrid.selectProp(prop); } } else { }
 		 */
 	}
 
 	public void leftClickProp(final Prop prop)
 	{
 		// The format of the following comments is:
 		// {List of conditions} -> {Effect on UI}
 		if (aSelectedProp == null && prop == null) {
 			// Nothing selected, clicking on nothing -> Do nothing
 			return;
 		}
 		if (aSelectedProp != null) {
 			// Something selected
 			if (aSelectedProp.equals(prop)) {
 				// Something selected, clicking on same thing -> Do nothing
 				return;
 			}
 			// Something selected, clicking on something else -> Deselect current
 			aProps.get(aSelectedProp).setState(PropState.INACTIVE);
 			if (aHighlightedLocations != null) {
 				// Had previously highlighted locations -> Wipe them out
 				aHighlightedLocations.fadeOut(); // This also deletes the aHighlightedLocations node
 				aHighlightedLocations = null; // Don't keep a reference to it anymore
 			}
 		}
 		// Update selection (prop might still be null at this point)
 		aSelectedProp = prop;
 		if (prop != null) {
 			// Clicking on other prop -> Select it
 			aProps.get(prop).setState(PropState.SELECTED);
 			aSelectionSize = prop.getLocation().dimension;
 			if (prop.getPropType().equals("ship")) {
 				// Clicking on ship -> Show available locations
 				final Ship ship = (Ship) prop;
 				aHighlightedLocations = new SolarGridHighlightLocations(ship.getValidDestinations());
 				addNode(aHighlightedLocations);
 			}
 		}
 		else {
 			// Clicking on empty space -> Reset selection dimension to 1x1
 			aSelectionSize = new Dimension(1, 1);
 		}
 	}
 }
