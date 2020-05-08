 package com.evervoid.client.showroom;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.evervoid.client.ui.ImageControl;
 import com.evervoid.client.ui.ListControl;
 import com.evervoid.client.ui.RescalableControl;
 import com.evervoid.client.ui.UIControl;
 import com.evervoid.state.data.GameData;
 import com.evervoid.state.data.PlanetData;
 import com.evervoid.state.data.RaceData;
 import com.evervoid.state.data.ShipData;
 import com.evervoid.utils.Pair;
 import com.evervoid.utils.namedtree.NamedNode;
 
 /**
  * Panel containing all clickable ships in the showroom menu
  */
 class ShowRoomMenu extends ListControl
 {
 	/**
 	 * The data used to load up sprites and rows.
 	 */
 	private final GameData aData;
 	/**
 	 * Maps clickable controls to the ({@link RaceData}, {@link ShipData}) pair to load when they are clicked
 	 */
 	private final Map<UIControl, Pair<RaceData, ShipData>> aShipControls = new HashMap<UIControl, Pair<RaceData, ShipData>>();
 	/**
 	 * Reference to the parent {@link ShowRoomView}
 	 */
 	private final ShowRoomView aView;
 
 	/**
 	 * Constructor; just needs the {@link GameData} and the parent view
 	 * 
 	 * @param view
 	 *            The parent {@link ShowRoomView}
 	 * @param data
 	 *            The {@link GameData} to use
 	 */
 	public ShowRoomMenu(final ShowRoomView view, final GameData data)
 	{
 		super(600, 400);
 		aData = data;
 		aView = view;
 		buildTree();
 		reset();
 	}
 
 	/**
 	 * Builds the planet rows.
 	 */
 	private void buildPlanets()
 	{
 		// Title planet row
 		final UIControl planets = new UIControl(BoxDirection.VERTICAL);
 		planets.addSpacer(4, 1);
 		planets.addString("Planets");
 		planets.registerClickObserver(this);
 		final NamedNode<UIControl> planetNode = new NamedNode<UIControl>("planets", planets);
 		// for every planet
 		for (final String planetType : aData.getPlanetTypes()) {
 			final PlanetData planet = aData.getPlanetData(planetType);
 			final UIControl planetRow = new UIControl(BoxDirection.HORIZONTAL);
 			planetRow.addSpacer(10, 1);
 			planetRow.addString(planet.getTitle());
 			planetRow.registerClickObserver(this);
 			planetNode.addChild(planet.getTitle(), planetRow);
 		}
 	}
 
 	/**
 	 * Builds the ship rows
 	 */
 	private void buildShips()
 	{
 		// Title row for "Ships"
 		addTopTitleRow("ships", "Ships");
 		// for every race
 		for (final String raceType : aData.getRaceTypes()) {
 			final RaceData race = aData.getRaceData(raceType);
 			final UIControl raceRow = new UIControl(BoxDirection.VERTICAL);
 			final UIControl title = new UIControl(BoxDirection.HORIZONTAL);
 			title.addSpacer(8, 1);
 			title.addUI(new ImageControl(race.getRaceIcon("small")));
 			title.addSpacer(4, 1);
 			title.addString(race.getTitle(), BoxDirection.VERTICAL);
 			raceRow.addUI(title);
 			addChildRow(race.getTitle(), raceRow, Arrays.asList("ships"));
 			// for every ship in the race
 			for (final String shipType : race.getShipTypes()) {
 				final ShipData ship = race.getShipData(shipType);
 				final UIControl shipRow = new UIControl(BoxDirection.HORIZONTAL);
 				shipRow.addSpacer(32, 1); // Indent slightly
 				shipRow.addUI(new RescalableControl(ship.getBaseSprite()).setEnforcedDimension(32, 32));
 				shipRow.addSpacer(8, 1);
 				shipRow.addString(ship.getTitle(), BoxDirection.VERTICAL);
 				aShipControls.put(shipRow, new Pair<RaceData, ShipData>(race, ship));
 				addChildRow(ship.getTitle(), shipRow, Arrays.asList("ships", race.getTitle()));
 			}
 		}
 	}
 
 	/**
 	 * Creates the tree of possible UI controls.
 	 */
 	private void buildTree()
 	{
 		buildShips();
 		buildPlanets();
 	}
 
 	@Override
 	protected boolean controlClicked(final UIControl control, final List<String> hierarchy)
 	{
		if ("ships".equals(hierarchy.get(0))) {
 			final Pair<RaceData, ShipData> clickedShip = aShipControls.get(control);
 			aView.openPlayground(clickedShip.getKey(), clickedShip.getValue());
 		}
 		else {
 			return false;
 		}
 		return true;
 	}
 }
