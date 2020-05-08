 package com.evervoid.client.views.planet;
 
 import com.evervoid.client.ui.ClickObserver;
 import com.evervoid.client.ui.ImageControl;
 import com.evervoid.client.ui.StaticTextControl;
 import com.evervoid.client.ui.UIControl;
 import com.evervoid.client.views.solar.UIPlanet;
 import com.evervoid.state.building.Building;
 import com.evervoid.state.data.BuildingData;
 import com.evervoid.state.data.ShipData;
 import com.jme3.math.ColorRGBA;
 
 public class SelectableBuildingControl extends UIControl implements ClickObserver
 {
 	private final PlanetView aParent;
 	private final int aSlot;
 
 	public SelectableBuildingControl(final PlanetView parent, final UIPlanet uiplanet, final int slot, final Building building)
 	{
 		super(BoxDirection.HORIZONTAL);
 		aParent = parent;
 		aSlot = slot;
 		BuildingData builddata = uiplanet.getConstructingBuildingDataOnSlot(aSlot);
 		if (builddata == null && building != null) {
 			builddata = building.getData();
 		}
 		final UIControl rest = new UIControl(BoxDirection.VERTICAL);
 		if (uiplanet.isCancellingBuildingOnSlot(aSlot) || builddata == null) {
 			// Slot is empty
 			addUI(new ImageControl(BuildingData.getBlankBuildingIcon()));
 			addSpacer(16, 1);
 			rest.addString("(Empty slot #" + (slot + 1) + ")", ColorRGBA.Gray);
 			rest.addString("Click to build a building.", ColorRGBA.Gray);
 		}
 		else {
 			addUI(new ImageControl(builddata.getIcon()));
 			addSpacer(16, 1);
 			rest.addUI(new StaticTextControl(builddata.getTitle(), ColorRGBA.White));
 			if (uiplanet.getConstructingBuildingDataOnSlot(aSlot) == null && building != null && building.isBuildingComplete()) {
 				// Building is up; might be creating a ship or not.
 				ShipData currentship = uiplanet.getConstructingShipDataOnSlot(aSlot);
 				if (currentship == null) {
 					currentship = building.getShipCurrentlyBuilding();
 				}
 				if (uiplanet.isCancellingShipOnSlot(aSlot) || currentship == null) {
 					rest.addUI(new StaticTextControl("Status: Idle.", ColorRGBA.LightGray));
 				}
 				else {
					rest.addUI(new StaticTextControl("Building " + currentship.getTitle() + " ("
							+ building.getShipConstructionPercentage() + ")", ColorRGBA.LightGray));
 				}
 			}
 			else {
 				// Erectin' a buildin'
 				String percentage = "0%";
 				if (building != null && builddata.equals(building.getData())) {
 					percentage = building.getBuildingProgressPercentage();
 				}
 				rest.addUI(new StaticTextControl("Under construction... (" + percentage + ")", ColorRGBA.LightGray));
 			}
 		}
 		addUI(rest, 1);
 		setHoverSelectable(true);
 		registerClickObserver(this);
 	}
 
 	@Override
 	public void uiClicked(final UIControl clicked)
 	{
 		aParent.openSlot(aSlot, false);
 	}
 }
