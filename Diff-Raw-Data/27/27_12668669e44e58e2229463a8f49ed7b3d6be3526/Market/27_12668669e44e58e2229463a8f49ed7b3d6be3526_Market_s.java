 package edu.gatech.CS2340.GrandTheftPoke.screens;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import edu.gatech.CS2340.GrandTheftPoke.GTPoke;
 import edu.gatech.CS2340.GrandTheftPoke.GUI.ItemTile;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Backpack;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlace;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlaceItem;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 //import java.util.Map.Entry;
 //import com.badlogic.gdx.scenes.scene2d.Touchable;
 
 public class Market extends AbstractScreen {
 	private Texture background;
 	private Image backgroundImage;
 	private	Table markettable,
 					backpacktable,
 					table;
 	private MarketPlace market;
 	private Backpack playerPack;
 	private ButtonGroup marketItemGroup,
 						backPackItemGroup;
 	private ScrollPane 	marketStock,
 						backpackStock;
 	private Button buy, sell;
 	private Button backButton;
 	public Market(GTPoke game){
 		super(game);
 		market = game.getCurrentTown().getMarket();
 	}
 	
 	public Market(GTPoke game, MarketPlace theMarket) {
 		super(game);
 		market = theMarket;
 	}
 	
 	@Override
 	public void show(){
 		super.show();
 		
 		background = new Texture("images//icons//chooseStarterTitle.png");
 		backgroundImage = new Image(background);
 		backgroundImage.setFillParent(true);
 		backgroundImage.getColor().a = 0f;
 		backgroundImage.addAction(fadeIn(0.75f));
 		
 		
 		//market = game.getCurrentTown().getMarket();
 
 		
 		Texture ButtonSprite = new Texture("images//button-sprite.png");
 		
 		playerPack = game.getPlayer().getBackpack();
 		
 		markettable = new Table();
 		backpacktable = new Table();
 		
 		marketItemGroup = new ButtonGroup();
 		backPackItemGroup = new ButtonGroup();
 		marketItemGroup.setMaxCheckCount(1);
 		backPackItemGroup.setMaxCheckCount(1);
 		
 		//marketStock = new ScrollPane(markettable);
 		//ItemTile tile = new ItemTile(game.getItems().getAgilityRoid(), new MarketPlaceItem(500, 500));
 		//marketStock.setPosition(0, 150);
 		//marketStock.setSize(150	, 150);
 		//marketStock.setTouchable(Touchable.childrenOnly);
 		//marketStock.setScrollingDisabled(true, true);
 		
 		
 		
 		update();
 		
 		buy = new Button(
 				new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 0, 320, 70)),
 				new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 69, 320, 70)));
 		buy.setSkin(getSkin());
 		buy.add("Buy");
 		buy.setPosition(1024 - 400, 50);
 		
 		buy.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				System.out.print(((ItemTile) (marketItemGroup.getChecked())).getItem());
 				System.out.print(market);
 				game.getPlayer().buy(market, ((ItemTile) (marketItemGroup.getChecked())).getItem(), 1);
 				update();
 			}
 		});
 		
 		sell = new Button(
 				new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 0, 320, 70)),
 				new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 69, 320, 70)));
 		sell.setSkin(getSkin());
 		sell.add("Sell");
 		sell.setPosition(1024 - 400, 150);
 		
 		sell.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				game.getPlayer().sell(market, ((ItemTile) (backPackItemGroup.getChecked())).getItem(), 1);
 				
 				update();
 			}
 		});
 		
 		table = new Table(); 
 		table.add(markettable);
 		table.add(backpacktable);
 	}
 	
 	public boolean update(){
 		int col = 1;
 		markettable.clear();
 		Button checked = marketItemGroup.getChecked();
 		for(Iterator<Map.Entry<Item, MarketPlaceItem>> i =  market.getStock().entrySet().iterator(); i.hasNext(); ){
 			Map.Entry<Item, MarketPlaceItem> item = i.next();
 			if(item.getValue().getStock() != 0){
 				ItemTile tile = new ItemTile(item.getKey(), item.getValue());
 				markettable.add(tile);
 				marketItemGroup.add(tile);
 				if(col++ % 2 == 0)
 					markettable.row();
 			}
 		}
 		col = 1;
 		backpacktable.clear();
 		checked = marketItemGroup.getChecked();
 		//for(Entries entries =  playerPack.getContents().entries(); entries.hasNext(); ){
 			//Entry item = entries.next();
 			//ItemTile tile = new ItemTile(market, (Item)item.key, (Integer)item.value, playerPack);
 			//backpacktable.add(tile);
 			//backPackItemGroup.add(tile);
 			//if(col++ % 2 == 0)
 				//backpacktable.row();
 		//}
 		for(Iterator<java.util.Map.Entry<Item, Integer>> i =  playerPack.getContents().entrySet().iterator(); i.hasNext(); ){
 			java.util.Map.Entry<Item, Integer> item = i.next();
 			ItemTile tile = new ItemTile(market, item.getKey(), item.getValue(), playerPack);
 			backpacktable.add(tile);
 			backPackItemGroup.add(tile);
 			if(col++ % 2 == 0)
 				backpacktable.row();
 		}
 		marketItemGroup.setMaxCheckCount(1);
 		backPackItemGroup.setMaxCheckCount(1);
 		
 		Texture ButtonSprite = new Texture("images//button-sprite.png");
 		ButtonStyle style = new ButtonStyle();
 		style.up = new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 0, 320, 70));
 		style.down = new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 69, 320, 70));
 		style.disabled = new TextureRegionDrawable(new TextureRegion(ButtonSprite, 0, 69, 320, 70));
 		backButton = new Button(style);
 		
 		backButton.setSkin(getSkin());
 		backButton.add("Back");
 		backButton.setPosition(10, 10);
 		
 		backButton.addListener(new ClickListener() {
 			@Override
 			public void clicked (InputEvent event, float x, float y) {
				game.setScreen(game.getCurrentTownScreen());
 			}
 		});
 		return true;
 	}
 
 	@Override
 	public void render(float delta){
 		super.render(delta);
 		//markettable.debug();
 		//table.drawDebug(stage);
 		//markettable.setPosition(500, 100);
 		//backpacktable.setPosition(800, 100);
 		//stage.addActor(marketStock);
 		for(Actor tile: markettable.getChildren())
 			((ItemTile) tile).update();
 		for(Actor tile: backpacktable.getChildren())
 			((ItemTile) tile).update();
 		table.setPosition(100, -400);
 		table.setBounds(0, 0, 768, 1024);
 		stage.addActor(table);
 		stage.addActor(buy);
 		stage.addActor(sell);
 		stage.addActor(backButton);
 		//table.debug();
 		//table.drawDebug(stage);
 	}
 
 	@Override
 	public void resize(int width, int height){
 		super.resize(width, height);
 	}
 
 	@Override
 	public void dispose(){
 		super.dispose();
 		background.dispose();
 	}
 }
