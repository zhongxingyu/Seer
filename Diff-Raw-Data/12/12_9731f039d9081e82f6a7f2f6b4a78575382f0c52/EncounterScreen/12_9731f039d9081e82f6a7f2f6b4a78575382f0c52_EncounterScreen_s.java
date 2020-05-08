 /**
  * EncounterScreen.java
  * @version 1.0
  */
 
 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Rocket;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 
 /**
  * Encounter Screen
  * 
  * @author Team Rocket
  * @version 1.0
  * 
  */
 public class EncounterScreen extends AbstractScreen {
 
 	/**
 	 * toString
 	 * 
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "EncounterScreen";
 	}
 
 	/**
 	 * Field backgroundImage.
 	 */
 	private Image backgroundImage;
 
 	/**
 	 * Field game.
 	 */
 	private final GTPoke myGame;
 
 	/**
 	 * Field trade.
 	 */
 	private Button trade;
 
 	/**
 	 * Field fight.
 	 */
 	private Button fight;
 
 	/**
 	 * Field flee.
 	 */
 	private Button flee;
 
 	/**
 	 * Field myPerson.
 	 */
 	private final Person myPerson;
 
 	/**
 	 * Field table.
 	 */
 	private Table table;
 
 	/**
 	 * @param game
 	 *            the game being played
 	 * @param encounteredPerson
 	 *            the person who is encountered
 	 */
 	public EncounterScreen(GTPoke game, Person encounteredPerson) {
 		super(game);
 		this.myGame = game;
 		myPerson = encounteredPerson;
 		if (myPerson instanceof Trader) {
 			((Trader) myPerson).initializeMarket();
 		}
 	}
 
 	/**
 	 * Method show.
 	 * 
 	 * @see com.badlogic.gdx.Screen#show()
 	 */
 	@Override
 	public void show() {
 		super.show();
 
 		table = new Table(GTPoke.getSkin());
 		table.setFillParent(true);
 
 		AtlasRegion buttonSprite = GTPoke.getTextures().findRegion("encounter");
 		stage.clear();
 
 		if (myPerson instanceof Trader) {
 			backgroundImage = new Image(GTPoke.getTextures().findRegion(
 					"encounter"));
 			backgroundImage.getColor().a = 0f;
 			backgroundImage.setPosition(0, 0);
 			backgroundImage.addAction(fadeIn(0.75f));
 		}
 
 		if (myPerson instanceof Rocket) {
 			backgroundImage = new Image(GTPoke.getTextures().findRegion(
 					"RocketEncounter"));
 			backgroundImage.getColor().a = 0f;
 			backgroundImage.setPosition(0, 0);
 			backgroundImage.addAction(fadeIn(0.75f));
 		}
 
 		trade = new Button(new TextureRegionDrawable(new TextureRegion(
 				buttonSprite, 75, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(buttonSprite, 75, 96, 283, 555)));
 		if (myPerson instanceof Trader) {
 			trade.addListener(new ClickListener() {
 				@Override
 				public void clicked(InputEvent event, float x, float y) {
 					myGame.setScreen(myGame.getMarketScreen(
 							((Trader) myPerson).getMarket(), (Trader) myPerson));
 				}
 			});
 		} else {
 			trade.setDisabled(true);
 			trade.setTouchable(Touchable.disabled);
 		}
 
 		fight = new Button(new TextureRegionDrawable(new TextureRegion(
 				buttonSprite, 358, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(buttonSprite, 358, 96, 283, 555)));
 		fight.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGame.setScreen(myGame.getBattleScreen(myPerson));
 			}
 		});
 
 		flee = new Button(new TextureRegionDrawable(new TextureRegion(
 				buttonSprite, 641, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(buttonSprite, 641, 96, 283, 555)));
 		flee.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if (myPerson instanceof Trader) {
 					myGame.setScreen(myGame.getCurrentTownScreenFromEncounter());
 				}
 				if (myGame.getPlayer().flee(myPerson)) {
					System.out.println("true");
 					myGame.setScreen(myGame.getCurrentTownScreenFromEncounter());
 				}
 			}
 		});
 		stage.addActor(backgroundImage);
 
 		if (myPerson instanceof Trader) {
 			trade.setPosition(75, 116);
 			stage.addActor(trade);
 
 			fight.setPosition(358, 116);
 			stage.addActor(fight);
 
 			flee.setPosition(641, 116);
 			stage.addActor(flee);
 
 			stage.addActor(myGame.getStatusBar());
 		} else {
 			
 			buttonSprite = GTPoke.getTextures().findRegion(
 					"RocketEncounter");
 			
 			fight = new Button(new TextureRegionDrawable(new TextureRegion(
 					buttonSprite, 75, 97, 480, 555)), new TextureRegionDrawable(
 					new TextureRegion(buttonSprite, 75, 96, 480, 555)));
 			fight.addListener(new ClickListener() {
 				@Override
 				public void clicked(InputEvent event, float x, float y) {
 					myGame.setScreen(myGame.getBattleScreen(myPerson));
 				}
 			});
 
 			flee = new Button(new TextureRegionDrawable(new TextureRegion(
 					buttonSprite, 506, 97, 480, 555)), new TextureRegionDrawable(
 					new TextureRegion(buttonSprite, 506, 96, 480, 555)));
 			flee.addListener(new ClickListener() {
 				@Override
 				public void clicked(InputEvent event, float x, float y) {
 					if (myPerson instanceof Trader) {
 						myGame.setScreen(myGame.getCurrentTownScreenFromEncounter());
 					}
 					if (myGame.getPlayer().flee(myPerson)) {
 						System.out.println("true");
 						myGame.setScreen(myGame.getCurrentTownScreenFromEncounter());
 					}
 				}
 			});
 
 			fight.setPosition(75, 116);
 			stage.addActor(fight);
 
 			flee.setPosition(506, 116);
 			stage.addActor(flee);
 		}
 		/*
 		 * trade.setPosition(75, 116); stage.addActor(trade);
 		 * 
 		 * fight.setPosition(358, 116); fight.addAction(fadeIn(0.75f));
 		 * stage.addActor(fight);
 		 * 
 		 * flee.setPosition(641, 116); flee.addAction(fadeIn(0.75f));
 		 * stage.addActor(flee);
 		 * 
 		 * stage.addActor(myGame.getStatusBar());
 		 */
 
 	}
 
 	/**
 	 * Method render.
 	 * 
 	 * @param delta
 	 *            float
 	 * @see com.badlogic.gdx.Screen#render(float)
 	 */
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		myGame.update();
 	}
 }
