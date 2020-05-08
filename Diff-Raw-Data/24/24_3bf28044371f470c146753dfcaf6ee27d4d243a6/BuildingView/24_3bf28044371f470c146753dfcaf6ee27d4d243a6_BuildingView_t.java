 package com.evervoid.client.views.planet;
 
 import java.util.List;
 
 import com.evervoid.client.graphics.geometry.AnimatedTranslation;
 import com.evervoid.client.ui.ButtonControl;
 import com.evervoid.client.ui.ButtonListener;
 import com.evervoid.client.ui.HorizontalCenteredControl;
 import com.evervoid.client.ui.ImageControl;
 import com.evervoid.client.ui.PanelControl;
 import com.evervoid.client.ui.ProgressBarControl;
 import com.evervoid.client.ui.RescalableControl;
 import com.evervoid.client.ui.ScrollingControl;
 import com.evervoid.client.ui.StaticTextControl;
 import com.evervoid.client.ui.UIControl;
 import com.evervoid.client.ui.UIControl.BoxDirection;
 import com.evervoid.client.ui.VerticalCenteredControl;
 import com.evervoid.client.views.Bounds;
 import com.evervoid.client.views.EverUIView;
 import com.evervoid.client.views.solar.UIPlanet;
 import com.evervoid.state.action.IllegalEVActionException;
 import com.evervoid.state.action.building.CancelShipConstruction;
 import com.evervoid.state.action.building.DestroyBuilding;
 import com.evervoid.state.building.Building;
 import com.evervoid.state.data.BuildingData;
 import com.evervoid.state.data.RaceData;
 import com.evervoid.state.data.ShipData;
 import com.evervoid.state.player.ResourceAmount;
 import com.evervoid.state.prop.Planet;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 
 public class BuildingView extends EverUIView implements ButtonListener
 {
 	private final Building aBuilding;
 	private ButtonControl aCancelBuildingButton = null;
 	private ButtonControl aCancelShipButton = null;
 	private final PanelControl aPanel;
 	private final ScrollingControl aPanelContents;
 	private final PlanetView aParent;
 	private final UIPlanet aPlanet;
 	private final AnimatedTranslation aSlideIn;
 	private Vector2f aSlideOutOffset;
 	private final int aSlot;
 
 	public BuildingView(final PlanetView parent, final UIPlanet uiplanet, final int slot)
 	{
 		super(new UIControl());
 		aParent = parent;
 		aSlot = slot;
 		aPlanet = uiplanet;
 		aSlideIn = getNewTranslationAnimation();
 		final Planet planet = uiplanet.getPlanet();
 		BuildingData builddata = uiplanet.getConstructingBuildingDataOnSlot(aSlot);
 		aBuilding = planet.getBuildingAt(aSlot);
 		aPanel = new PanelControl(aBuilding == null ? "(Empty slot #" + (slot + 1) + ")" : aBuilding.getData().getTitle());
 		final UIControl rightMargin = new UIControl(BoxDirection.HORIZONTAL);
 		aPanelContents = new ScrollingControl();
 		aPanelContents.setAutomaticSpacer(4);
 		aPanelContents.setDefaultTextColor(ColorRGBA.LightGray);
 		if (aPlanet.isCancellingBuildingOnSlot(aSlot) || aBuilding == null && builddata == null) {
 			aPanelContents.addString("This building slot is empty.\nBuilding to build:");
 			final RaceData race = planet.getPlayer().getRaceData();
 			for (final String type : race.getBuildings()) {
 				final BuildingData data = race.getBuildingData(type);
 				aPanelContents.addUI(new ConstructibleBuildingControl(aParent, uiplanet, slot, data));
 			}
 			// TODO: Show more building stats somewhere (probably panel) so that the player knows what the building is for
 		}
 		else if (builddata != null || aBuilding == null || !aBuilding.isBuildingComplete()) {
 			// Currently building (if aBuilding == null, then the building process hasn't started state-side yet)
 			String percentage = "0%";
 			float floatpercentage = 0;
 			ImageControl icon = null;
 			if (builddata != null) {
 				icon = new ImageControl(builddata.getIcon());
 				if (aBuilding != null && builddata.equals(planet.getBuildingAt(aSlot).getData())) {
 					percentage = aBuilding.getBuildingProgressPercentage();
 					floatpercentage = aBuilding.getBuildingProgressFloat();
 				}
 			}
 			else if (aBuilding != null) {
 				icon = new ImageControl(BuildingData.getBlankBuildingIcon());
 			}
 			if (icon != null) {
 				aPanel.getTitleBox().addUI(icon);
 			}
 			aPanelContents.addString("This building is under construction.");
 			aPanelContents.addSpacer(1, 16);
 			aPanelContents.addUI(new ProgressBarControl(floatpercentage));
 			aPanelContents.addSpacer(1, 16);
 			aPanelContents.addString("Progress: " + percentage);
 			aPanelContents.addSpacer(1, 16);
 			aCancelBuildingButton = new ButtonControl("Cancel");
 			aCancelBuildingButton.addButtonListener(this);
 			aPanelContents.addUI(new HorizontalCenteredControl(aCancelBuildingButton));
 		}
 		else {
 			// Building is non-null and fully built
 			aCancelBuildingButton = new ButtonControl("Destroy");
 			aCancelBuildingButton.addButtonListener(this);
 			aPanel.getTitleBox().addUI(new VerticalCenteredControl(aCancelBuildingButton));
 			// Display building income/shields
 			final ResourceAmount income = aBuilding.getIncome();
 			if (income != null && !income.isZero()) {
 				aPanelContents.addSpacer(1, 4);
 				aPanelContents.addUI(new ResourceRow(aBuilding.getState(), "Income:", income));
 				aPanelContents.addSpacer(1, 4);
 			}
 			if (aBuilding.getExtraShields() != 0 || aBuilding.getShieldRegen() != 0) {
 				final UIControl shieldrow = new UIControl(BoxDirection.HORIZONTAL);
 				shieldrow.addString("Provided shields: " + aBuilding.getExtraShields(), new ColorRGBA(0.8f, 0.8f, 1f, 1f));
 				if (aBuilding.getShieldRegen() != 0) {
 					shieldrow.addString(" (+" + aBuilding.getShieldRegen() + ")", new ColorRGBA(0.8f, 1f, 0.8f, 1f));
 				}
 				aPanelContents.addSpacer(1, 4);
 				aPanelContents.addUI(shieldrow);
 				aPanelContents.addSpacer(1, 4);
 			}
 			// End display building income/shields
 			ShipData beingBuilt = uiplanet.getConstructingShipDataOnSlot(aSlot);
 			if (!aPlanet.isCancellingShipOnSlot(aSlot) && (aBuilding.isBuildingShip() || beingBuilt != null)) {
 				// Building is currently building a ship (or planning to build one)
 				if (beingBuilt == null) {
 					beingBuilt = aBuilding.getShipCurrentlyBuilding();
 				}
 				aPanelContents.addString("This ship is under construction.");
 				aPanelContents.addSpacer(1, 16);
				String percentage = "0%";
				float percentageF = 0;
				if (aBuilding.getShipCurrentlyBuilding() != null && aBuilding.getShipCurrentlyBuilding().equals(beingBuilt)) {
					percentage = aBuilding.getShipConstructionPercentage();
					percentageF = aBuilding.getShipConstructionFloat();
				}
				aPanelContents.addUI(new ProgressBarControl(percentageF));
 				aPanelContents.addSpacer(1, 16);
				aPanelContents.addString("Progress: " + percentage);
 				aPanelContents.addSpacer(1, 16);
 				aCancelShipButton = new ButtonControl("Cancel");
 				aCancelShipButton.addButtonListener(this);
 				aPanelContents.addUI(new HorizontalCenteredControl(aCancelShipButton));
 				aPanelContents.addSpacer(1, 16);
 				aPanelContents.addUI(new RescalableControl(beingBuilt.getBaseSprite()).setAllowScale(false, false));
 			}
 			else {
 				// Building is idle, display buildable ships
 				builddata = aBuilding.getData();
 				final List<String> shipTypes = builddata.getAvailableShipTypes();
 				if (shipTypes.isEmpty()) {
 					aPanelContents.addUI(new StaticTextControl("This building cannot create ships.", ColorRGBA.Gray));
 				}
 				else {
 					aPanelContents.addUI(new StaticTextControl("Ship to build:", ColorRGBA.LightGray));
 					final RaceData race = planet.getPlayer().getRaceData();
 					for (final String type : shipTypes) {
 						aPanelContents
 								.addUI(new ConstructibleShipControl(aParent, uiplanet, aBuilding, race.getShipData(type)));
 					}
 					// TODO: Show more ship stats somewhere (probably panel) so that the player knows what the ship is for
 				}
 			}
 		}
 		rightMargin.addUI(aPanelContents, 1);
 		rightMargin.addSpacer(4, 0);
 		aPanel.addUI(rightMargin, 1);
 		addUI(aPanel, 1);
 	}
 
 	@Override
 	public void buttonClicked(final UIControl button)
 	{
 		try {
 			if (button.equals(aCancelBuildingButton)) {
 				// aBuilding might be null here if the user has just started building construction this turn.
 				aPlanet.setAction(aSlot, aBuilding == null ? null : new DestroyBuilding(aBuilding));
 				aParent.refreshSlots(aSlot);
 			}
 			else if (button.equals(aCancelShipButton)) {
 				aPlanet.setAction(aSlot, new CancelShipConstruction(aBuilding));
 				aParent.refreshSlots(aSlot);
 			}
 		}
 		catch (final IllegalEVActionException e) {
 			// Shouldn't happen
 		}
 	}
 
 	public int getSlot()
 	{
 		return aSlot;
 	}
 
 	@Override
 	public void setBounds(final Bounds bounds)
 	{
 		if (aSlideIn != null) {
 			aSlideOutOffset = new Vector2f(bounds.width, 0);
 			aSlideIn.setTranslationNow(aSlideOutOffset);
 			super.setBounds(new Bounds(bounds.x + aPanel.getRightMargin(), bounds.y, bounds.width, bounds.height));
 		}
 	}
 
 	public void slideIn(final float duration)
 	{
 		aSlideIn.smoothMoveTo(new Vector2f(0, 0)).setDuration(duration).start();
 	}
 
 	public void slideOut(final float duration, final Runnable callback)
 	{
 		aSlideIn.smoothMoveTo(aSlideOutOffset).setDuration(duration).start(callback);
 	}
 }
