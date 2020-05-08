 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 
 import com.badlogic.gdx.graphics.Texture;
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
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 
 public class EncounterScreen extends AbstractScreen {
 
 	private Texture background;
 	private Image backgroundImage;
 	private GTPoke game;
 
 	private Button trade;
 	private Button fight;
 	private Button flee;
 
 	private Texture title;
 	private Person myPerson;
 
 	private Table table;
 	public EncounterScreen(GTPoke game, Person encounteredPerson) {
 		super(game);
 		this.game = game;
 		myPerson = encounteredPerson;
 		if(myPerson instanceof Trader) {
 			((Trader) myPerson).initializeMarket();
 		}
 	}
 	
 	public void show() {
 		super.show();
 
 		table = new Table(getSkin());
 		table.setFillParent(true);
 
 		background = new Texture("images//icons//encounter.png");
 
 		Texture ButtonSprite = new Texture("images//icons//encounter.png");
 		stage.clear();
 
 		backgroundImage = new Image(background);
 		backgroundImage.getColor().a = 0f;
 		backgroundImage.setPosition(0, 0);
 		backgroundImage.addAction(fadeIn(0.75f));
 
 		trade = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 75, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 75, 96, 283, 555)));
 		if(myPerson instanceof Trader) {
 			trade.addListener(new ClickListener() {
 				@Override
 				public void clicked(InputEvent event, float x, float y) {
 					game.setScreen(game.getMarketScreen(((Trader)myPerson).getMarket()));
 				}
 			});
 		} else {
 			trade.setDisabled(false);
 			trade.setTouchable(Touchable.enabled);
 		}
 		
 
 		fight = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 358, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 358, 96, 283, 555)));
 		fight.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
				game.setScreen(game.getBattleScreen(myPerson));
 			}
 		});
 
 		flee = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 641, 97, 283, 555)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 641, 96, 283, 555)));
 		flee.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				if(myPerson instanceof Trader) {
 					game.setScreen(game.getCurrentTownScreenFromEncounter());
 				}
 				if(game.getPlayer().flee()) {
 					System.out.println("true");
 					game.setScreen(game.getCurrentTownScreenFromEncounter());
 				}
 			}
 		});
 		stage.addActor(backgroundImage);
 		
 		trade.setPosition(75, 116);
 		stage.addActor(trade);
 		
 		fight.setPosition(358, 116);
 		stage.addActor(fight);
 		
 		flee.setPosition(641, 116);
 		stage.addActor(flee);
 	}
 }
