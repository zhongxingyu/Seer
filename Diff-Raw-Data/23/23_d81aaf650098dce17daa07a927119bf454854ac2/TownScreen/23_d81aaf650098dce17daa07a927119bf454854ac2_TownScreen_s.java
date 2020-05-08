 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.GymLeader;
 
 /**
  * Town/Navigation screen
  * 
  * @author Team Rocket
  * @version 1.0
  * 
  */
 public class TownScreen extends AbstractScreen {
 	
 	/**
 	 * toString
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "TownScreen";
 	}
 
 	/**
 	 * Field table.
 	 */
 	private Table table;
 
 	/**
 	 * Field healthTable.
 	 */
 	private Table healthTable;
 
 	/**
 	 * Field bp.
 	 */
 	private Table bp;
 
 	/**
 	 * Field townImage.
 	 */
 	private Image townImage;
 
 	/**
 	 * Field mapButton.
 	 */
 	private Button mapButton;
 
 	/**
 	 * Field pokeCenterButton.
 	 */
 	private Button pokeCenterButton;
 
 	/**
 	 * Field marketPlaceButton.
 	 */
 	private Button marketPlaceButton;
 
 	/**
 	 * Field gymLeaderButton.
 	 */
 	private Button gymLeaderButton;
 
 	/**
 	 * Field backPackButton.
 	 */
 	private Button backPackButton;
 
 	/**
 	 * Field town.
 	 */
 	private final String town;
 
 	/**
 	 * @param game
 	 *            the game being played
 	 * @param town
 	 *            the town one is in
 	 */
 	public TownScreen(GTPoke game, String town) {
 		super(game);
 		this.town = town;
 	}
 
 	/**
 	 * Method show.
 	 * @see com.badlogic.gdx.Screen#show()
 	 */
 	@Override
 	public void show() {
 		super.show();
 
 		table = new Table(game.getSkin());
 		healthTable = new Table(game.getSkin());
 		bp = new Table(game.getSkin());
 
 		table.setFillParent(true);
 		healthTable.setFillParent(true);
 		bp.setFillParent(true);
 
 		townImage = new Image(game.getTextures().findRegion("town/" + town));
 
 //		final Texture buttonSprite = new Texture("images//icons//PalletTown.png");
 		stage.clear();
 
 //		healthImage = new Image(new TextureRegionDrawable(new TextureRegion(
 //				buttonSprite, 183, 707, 667, 63)));
 
 		mapButton = game.getButton("town/map");
 
 		mapButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				game.setScreen(game.getMapScreen());
 			}
 		});
 		pokeCenterButton = game.getButton("town/pokecenter");
 		pokeCenterButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (!(game.getPlayer().getHealth().equals(game.getPlayer()
 						.getMaxHealth()))
 						&& game.getPlayer().getWallet().getMoney() >= 300) {
 					game.getPlayer().setHealth(
 							game.getPlayer().getMaxHealth()
 									- game.getPlayer().getHealth());
 					game.getPlayer().getWallet().updateMoney(-300);
 				}
 			}
 		});
 		marketPlaceButton = game.getButton("town/marketplace");
 		marketPlaceButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				game.setScreen(game.getMarketScreen());
 			}
 		});
 		backPackButton = game.getButton("town/backpack");
 //		backPackButton.addListener(new ClickListener() {
 //			@Override
 //			public void clicked(InputEvent event, float x, float y) {
 //				//
 //			}
 //		});
 		gymLeaderButton = game.getButton("town/gymleader");
 		gymLeaderButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				game.setScreen(game.getBattleScreen(new GymLeader("Brock", 8,
 						8, 0, 8, 200, 0, 0, 1000f, game.getMap())));
 			}
 		});
 
 	}
 
 	/**
 	 * Method render.
 	 * @param delta float
 	 * @see com.badlogic.gdx.Screen#render(float)
 	 */
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		game.update();
 		table.clear();
 		bp.clear();
 		healthTable.clear();
 		table.add(townImage).colspan(4);
 		table.row();
 		table.add(mapButton);
 		table.add(pokeCenterButton);
 		table.add(marketPlaceButton);
 		table.add(gymLeaderButton);
 		stage.addActor(table);
 
 		bp.setPosition(-450, 60);
 		bp.add(backPackButton);
 
 		stage.addActor(bp);
 		stage.addActor(game.getStatusBar());
 	}
 
 
 }
