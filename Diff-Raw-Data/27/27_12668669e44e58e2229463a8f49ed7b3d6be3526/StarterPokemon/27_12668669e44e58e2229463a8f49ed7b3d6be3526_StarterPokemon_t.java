 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 //import com.badlogic.gdx.scenes.scene2d.Actor;
 //import com.badlogic.gdx.scenes.scene2d.ui.Label;
 //import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 //import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 //import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
 
 public class StarterPokemon extends AbstractScreen {
 	private Texture background;
 	private Image backgroundImage;
 
 	private Button bulbasaur;
 	private Button charmander;
 	private Button squirtle;
 	private Button ConfirmButton;
 	private int confirmX = 356;
 	private int confirmY = 10;
 	//private int strength = 0, agility = 0, trade = 0, stamina = 0;
 
 	private Texture title;
 
 	private Table table;
 
 	public StarterPokemon(GTPoke game) {
 		super(game);
 	}
 
 	@Override
 	public void show() {
 		super.show();
 
 		table = new Table(getSkin());
 		table.setFillParent(true);
 
 		background = new Texture("images//icons//chooseStarterTitle.png");
 
 		Texture ButtonSprite = new Texture("images//icons//starterbuttons.png");
 		stage.clear();
 
 		backgroundImage = new Image(background);
 		// backgroundImage.setFillParent(true);
 		backgroundImage.getColor().a = 0f;
 		backgroundImage.setPosition(60, 600);
 		backgroundImage.addAction(fadeIn(0.75f));
 
 		bulbasaur = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 0, 0, 335, 768)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 0, 769, 335, 768)));
 		bulbasaur.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// game.setScreen(game.getNameScreen());
 				confirmX = 25;
 				ConfirmButton.setDisabled(false);
 				ConfirmButton.setTouchable(Touchable.enabled);
 			}
 		});
 
 		charmander = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 335, 0, 338, 768)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 335, 768, 338, 768)));
 		charmander.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// game.setScreen(game.getNameScreen());
 				confirmX = 356;
 				ConfirmButton.setDisabled(false);
 				ConfirmButton.setTouchable(Touchable.enabled);
 			}
 		});
 
 		squirtle = new Button(new TextureRegionDrawable(new TextureRegion(
 				ButtonSprite, 674, 0, 349, 768)), new TextureRegionDrawable(
 				new TextureRegion(ButtonSprite, 674, 768, 349, 768)));
 		squirtle.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				// game.setScreen(game.getNameScreen());
 				confirmX = 675;
 				ConfirmButton.setDisabled(false);
 				ConfirmButton.setTouchable(Touchable.enabled);
 			}
 		});
 
 		Texture confirmButtonSprite = new Texture("images//button-sprite.png");
 		ButtonStyle style = new ButtonStyle();
 		style.up = new TextureRegionDrawable(new TextureRegion(confirmButtonSprite, 0,
 				0, 320, 70));
 		style.down = new TextureRegionDrawable(new TextureRegion(confirmButtonSprite,
 				0, 69, 320, 70));
 		style.disabled = new TextureRegionDrawable(new TextureRegion(
 				confirmButtonSprite, 0, 69, 320, 70));
 
 		ConfirmButton = new Button(style);
 		ConfirmButton.setSkin(getSkin());
 		ConfirmButton.add("Confirm");
 		ConfirmButton.setPosition(confirmX, confirmY);
 		ConfirmButton.setDisabled(true);
 		ConfirmButton.setTouchable(Touchable.disabled);
 
 		ConfirmButton.setStyle(style);
 
 		ConfirmButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
				game.setScreen(game.getCurrentTownScreenFromEncounter());
 			}
 		});
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 
 		// remVal.setText(remainder.toString());
 
 		table.clear();
 
 		table.add(bulbasaur);
 		table.add(charmander);
 		table.add(squirtle);
 
 		stage.addActor(table);
 		stage.addActor(backgroundImage);
 		ConfirmButton.setPosition(confirmX, confirmY);
 		stage.addActor(ConfirmButton);
 
 		// table.debug();
 		// table.drawDebug(stage);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		background.dispose();
 	}
 }
