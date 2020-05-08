 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 
 import java.util.Iterator;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 import edu.gatech.CS2340.GrandTheftPoke.GUI.TownTile;
 import edu.gatech.CS2340.GrandTheftPoke.backend.GameMap;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 
 public class MapScreen extends AbstractScreen {
 
 	private Texture background;
 	private Image backgroundImage;
 	private GameMap theMap;
 
 	private TownTile palletTownButton;
 	private TownTile viridianCityButton;
 	private TownTile pewterCityButton;
 	private TownTile ceruleanCityButton;
 	private TownTile vermillionCityButton;
 	private TownTile lavenderTownButton;
 	private TownTile fuchsiaCityButton;
 	private TownTile saffronCityButton;
 	private TownTile cinnabarIslandButton;
 	private TownTile powerPlantButton;
 	private TownTile celadonCityButton;
 	private Button saveButton;
 	private Button backButton;
 
 	public MapScreen(GTPoke game) {
 		super(game);
 	}
 
 	@Override
 	public void show() {
 		super.show();
 
 		background = new Texture("images//map.png");
 
 		stage.clear();
 
 		theMap = game.getMap();
 		theMap.Dijkstras(game.getPlayer().getCurrent());
 		// System.out.println(theMap.getCurrent());
 
 		backgroundImage = new Image(background);
 		backgroundImage.getColor().a = 0f;
 		backgroundImage.addAction(fadeIn(0.75f));
 
 		Texture townTileSprite = new Texture("images//icons//newball.png");
 		ButtonStyle townTileStyle = new ButtonStyle();
 		townTileStyle.up = new TextureRegionDrawable(new TextureRegion(
 				townTileSprite, 0, 0, 36, 45));
 		townTileStyle.down = new TextureRegionDrawable(new TextureRegion(
 				townTileSprite, 0, 46, 36, 45));
 		townTileStyle.disabled = new TextureRegionDrawable(new TextureRegion(
 				townTileSprite, 0, 91, 36, 45));
 
 		Iterator<Town> i = game.getMap().getTownSet().iterator();
 		while (i.hasNext()) {
 			Town theTown = i.next();
 
 			if (theTown.toString().equals("Pallet Town")) {
 				palletTownButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Viridian City")) {
 				viridianCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Power Plant")) {
 				powerPlantButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Pewter City")) {
 				pewterCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Cerulean City")) {
 				ceruleanCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Fuchsia City")) {
 				fuchsiaCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Cinnabar Island")) {
 				cinnabarIslandButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Saffron City")) {
 				saffronCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Vermillion City")) {
 				vermillionCityButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Lavender Town")) {
 				lavenderTownButton = new TownTile(theTown, townTileStyle);
 			} else if (theTown.toString().equals("Celadon City")) {
 				celadonCityButton = new TownTile(theTown, townTileStyle);
 			} else {
 
 			}
 
 			int distance = theMap.Dijkstras(game.getPlayer().getCurrent(),
 					theTown.toString());
 			int range = game.getPlayer().getBackpack().getMaxRange();
 			int playerHealth = game.getPlayer().getHealth();
 
 			if (distance > range || playerHealth < distance / 5) {
 				if (theTown.toString().equals("Pallet Town")) {
 					palletTownButton.setDisabled(true);
 					palletTownButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Viridian City")) {
 					viridianCityButton.setDisabled(true);
 					viridianCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Power Plant")) {
 					powerPlantButton.setDisabled(true);
 					powerPlantButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Pewter City")) {
 					pewterCityButton.setDisabled(true);
 					pewterCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Cerulean City")) {
 					ceruleanCityButton.setDisabled(true);
 					ceruleanCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Fuchsia City")) {
 					fuchsiaCityButton.setDisabled(true);
 					fuchsiaCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Cinnabar Island")) {
 					cinnabarIslandButton.setDisabled(true);
 					cinnabarIslandButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Saffron City")) {
 					saffronCityButton.setDisabled(true);
 					saffronCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Vermillion City")) {
 					vermillionCityButton.setDisabled(true);
 					vermillionCityButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Lavender Town")) {
 					lavenderTownButton.setDisabled(true);
 					lavenderTownButton.setTouchable(Touchable.disabled);
 				} else if (theTown.toString().equals("Celadon City")) {
 					celadonCityButton.setDisabled(true);
 					celadonCityButton.setTouchable(Touchable.disabled);
 				} else {
 
 				}
 			}
 		}
 
 		Texture ButtonSprite = new Texture("images//button-sprite.png");
 		ButtonStyle style = new ButtonStyle();
 		style.up = new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0,
 				0, 320, 70));
 		style.down = new TextureRegionDrawable(new TextureRegion(ButtonSprite,
 				0, 69, 320, 70));
 		style.disabled = new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 0, 69, 320, 70));
 
 		saveButton = new Button(style);
 
 		saveButton.setSkin(getSkin());
 		saveButton.add("Save");
 		saveButton.setPosition(10, 10);
 
 		saveButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				game.save();
 				game.clear();
 				game.setScreen(game.getMainMenuScreen());
 			}
 		});
 
 		backButton = new Button(style);
 
 		backButton.setSkin(getSkin());
 		backButton.add("Back");
 		backButton.setPosition(10, 70);
 
 		backButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
				game.setScreen(game.getCurrentTownScreen());
 			}
 		});
 		// return true;
 
 		viridianCityButton.setStyle(style);
 		palletTownButton.setStyle(style);
 		powerPlantButton.setStyle(style);
 		pewterCityButton.setStyle(style);
 		ceruleanCityButton.setStyle(style);
 		fuchsiaCityButton.setStyle(style);
 		cinnabarIslandButton.setStyle(style);
 		saffronCityButton.setStyle(style);
 		vermillionCityButton.setStyle(style);
 		lavenderTownButton.setStyle(style);
 		celadonCityButton.setStyle(style);
 
 		viridianCityButton.setSkin(getSkin());
 		palletTownButton.setSkin(getSkin());
 		powerPlantButton.setSkin(getSkin());
 		pewterCityButton.setSkin(getSkin());
 		ceruleanCityButton.setSkin(getSkin());
 		fuchsiaCityButton.setSkin(getSkin());
 		cinnabarIslandButton.setSkin(getSkin());
 		saffronCityButton.setSkin(getSkin());
 		vermillionCityButton.setSkin(getSkin());
 		lavenderTownButton.setSkin(getSkin());
 		celadonCityButton.setSkin(getSkin());
 
 		viridianCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 				// viridianCityButton.setStyle(STYLE WITH PLAYER ON IT);
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		celadonCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 				// viridianCityButton.setStyle(STYLE WITH PLAYER ON IT);
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		palletTownButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		powerPlantButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				System.out.println(destination);
 				game.getPlayer().move(destination);
 			}
 
 		});
 
 		pewterCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		ceruleanCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		fuchsiaCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		cinnabarIslandButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		saffronCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		vermillionCityButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		lavenderTownButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				move(event.getListenerActor());
 			}
 
 			private void move(Actor listenerActor) {
 				Town destination = ((TownTile) (listenerActor)).getTown();
 				game.getPlayer().move(destination);
 				game.setScreen(game.getCurrentTownScreen());
 			}
 
 		});
 
 		stage.addActor(backgroundImage);
 
 		viridianCityButton.add("Viridian City");
 		stage.addActor(viridianCityButton);
 		viridianCityButton.setPosition(355, 391);
 
 		palletTownButton.add("Pallet Town");
 		stage.addActor(palletTownButton);
 		palletTownButton.setPosition(355, 210);
 
 		powerPlantButton.add("Power Plant");
 		stage.addActor(powerPlantButton);
 		powerPlantButton.setPosition(849, 693);
 
 		pewterCityButton.add("Pewter City");
 		stage.addActor(pewterCityButton);
 		pewterCityButton.setPosition(375, 607);
 
 		ceruleanCityButton.add("Cerulean City");
 		stage.addActor(ceruleanCityButton);
 		ceruleanCityButton.setPosition(742, 562);
 
 		saffronCityButton.add("Saffron City");
 		stage.addActor(saffronCityButton);
 		saffronCityButton.setPosition(730, 418);
 
 		celadonCityButton.add("Celadon City");
 		stage.addActor(celadonCityButton);
 		celadonCityButton.setPosition(610, 393);
 
 		cinnabarIslandButton.add("Cinnabar Island");
 		stage.addActor(cinnabarIslandButton);
 		cinnabarIslandButton.setPosition(365, 25);
 
 		vermillionCityButton.add("Vermillion City");
 		stage.addActor(vermillionCityButton);
 		vermillionCityButton.setPosition(730, 299);
 
 		lavenderTownButton.add("Lavender Town");
 		stage.addActor(lavenderTownButton);
 		lavenderTownButton.setPosition(898, 408);
 
 		fuchsiaCityButton.add("Fuchsia City");
 		stage.addActor(fuchsiaCityButton);
 		fuchsiaCityButton.setPosition(602, 122);
 
 		stage.addActor(saveButton);
 		stage.addActor(backButton);
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 }
