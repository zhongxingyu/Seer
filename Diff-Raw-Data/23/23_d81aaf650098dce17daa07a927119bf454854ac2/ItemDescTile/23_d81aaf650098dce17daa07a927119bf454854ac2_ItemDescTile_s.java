 package edu.gatech.CS2340.GrandTheftPoke.GUI;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
import edu.gatech.CS2340.GrandTheftPoke.backend.Backpack;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlace;
 import edu.gatech.CS2340.GrandTheftPoke.backend.MarketPlaceItem;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Person;
 import edu.gatech.CS2340.GrandTheftPoke.backend.persons.Trader;
 import edu.gatech.CS2340.GrandTheftPoke.screens.Market;
 
 /**
  * Represents item tile
  * 
  * @author Team Rocket
  * @version 1.0
  * 
  */
 public class ItemDescTile extends Table {
 	/**
 	 * Method toString.
 	 * @return String
 	 */
 	@Override
 	public String toString() {
 		return "ItemDescTile [item=" + item + ", stockInfo=" + stockInfo
 				+ ", backpackStock=" + backpackStock
 				+ ", icon=" + icon + ", name=" + name + ", description="
 				+ description + ", stock=" + stock + ", price=" + price
 				+ ", buy=" + buy + ", sell=" + sell + ", buyListener="
 				+ buyListener + ", sellListener=" + sellListener + ", buysell="
 				+ buysell + ", customer=" + customer + ", otherTrader="
 				+ otherTrader + ", market=" + market + ", screen=" + screen
 				+ ", tile=" + tile + "]";
 	}
 
 	/**
 	 * Field item.
 	 */
 	private Item item;
 
 	/**
 	 * Field stockInfo.
 	 */
 	private MarketPlaceItem stockInfo;
 
 //	/**
 //	 * Field pack.
 //	 */
 //	private Backpack pack;
 
 	/**
 	 * Field backpackStock.
 	 */
 	private final Integer backpackStock;
 
 	/**
 	 * Field icon.
 	 */
 	private final Image icon;
 
 	/**
 	 * Field name.
 	 */
 	private final Label name, description, stock, price;
 
 	/**
 	 * Field buy.
 	 */
 	private final Button buy, sell;
 
 	/**
 	 * Field buyListener.
 	 */
 	private final ClickListener buyListener, sellListener;
 
 	/**
 	 * Field buysell.
 	 */
 	private final Table buysell;
 
 	/**
 	 * Field customer.
 	 */
 	private Person customer;
 
 	/**
 	 * Field otherTrader.
 	 */
 	private final Trader otherTrader;
 
 	/**
 	 * Field market.
 	 */
 	private final MarketPlace market;
 
 	/**
 	 * Field screen.
 	 */
 	private final Market screen;
 
 	/**
 	 * Field tile.
 	 */
 	private ItemTile tile;
 
 	/**
 	
 	 * @return item */
 	public Item getItem() {
 		return item;
 	}
 
 	// private Table tile;
 	/**
 	 * @param item
 	 *            item to represent
 	 * @param stockInfo
 	 *            information about stock
 	 * @param customer
 	 *            person buying item
 	 * @param market
 	 *            market in which item exists
 	 * @param screen
 	 *            screen to show to
 	 * @param tile
 	 *            tile containing item
 	 * @param other
 	 *            other trader
 	 */
 	public ItemDescTile(Item item, MarketPlaceItem stockInfo, Person customer,
 			MarketPlace market, Market screen, ItemTile tile, Trader other) {
 		this.item = item;
 		this.stockInfo = stockInfo;
 		this.customer = customer;
 		this.market = market;
 		this.screen = screen;
 		this.otherTrader = other;
 
 		final Texture buttonSprite = new Texture("images//button-sprite.png");
 		buy = new Button(new TextureRegionDrawable(new TextureRegion(
 				buttonSprite, 0, 0, 320, 70)), new TextureRegionDrawable(
 				new TextureRegion(buttonSprite, 0, 69, 320, 70)));
 		buy.setSkin(new Skin(Gdx.files.internal("skins//uiskin.json")));
 		buy.add("Buy");
 		buyListener = new BuyListener();
 		buy.addListener(buyListener);
 
 		sell = new Button(new TextureRegionDrawable(new TextureRegion(
 				buttonSprite, 0, 0, 320, 70)), new TextureRegionDrawable(
 				new TextureRegion(buttonSprite, 0, 69, 320, 70)));
 		sell.setSkin(new Skin(Gdx.files.internal("skins//uiskin.json")));
 		sell.add("Sell");
 		sellListener = new SellListener();
 		sell.addListener(sellListener);
 
 		buysell = new Table();
 
 		final Pixmap map = new Pixmap(200, 200, Pixmap.Format.RGB565);
 		map.setColor(Color.BLUE);
 		map.fillRectangle(0, 0, 200, 200);
 
 		final Pixmap map2 = new Pixmap(200, 200, Pixmap.Format.RGB565);
 		map2.setColor(Color.GRAY);
 		map2.fillRectangle(0, 0, 200, 200);
 
 		backpackStock = -1;
 		name = new Label("", new Skin(Gdx.files.internal("skins//uiskin.json")));
 		description = new Label("", new Skin(
 				Gdx.files.internal("skins//uiskin.json")));
 		stock = new Label("",
 				new Skin(Gdx.files.internal("skins//uiskin.json")));
 		price = new Label("",
 				new Skin(Gdx.files.internal("skins//uiskin.json")));
 		icon = new Image(new Texture(map));
 		setSkin(new Skin(Gdx.files.internal("skins//uiskin.json")));
 
 		add(stock).right().pad(10);
 		row();
 		add(icon).expandX();
 		row();
 
 		name.setWrap(true);
 		name.setAlignment(Align.center);
 		add(name).width(250).padTop(5);
 		row();
 
 		description.setWrap(true);
 		// description.setAlignment(Align.center);
 		add(description).width(250).padTop(5).expand().top();
 		row();
 		add(price).bottom().pad(10);
 		row();
 		add(buysell).width(150);
 
 		top();
 		setSize(300, 525);
 
 		setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(
 				map2), 0, 0, 300, 525)));
 
 		update(item, stockInfo, customer, tile);
 	}
 
 	/**
 	 * Listener for things being bought
 	 * 
 	 * @author Team Rocket
 	 * 
 	 */
 	private class BuyListener extends ClickListener {
 		/**
 		 * Method clicked.
 		 * @param event InputEvent
 		 * @param x float
 		 * @param y float
 		 */
 		@Override
 		public void clicked(InputEvent event, float x, float y) {
 			if (otherTrader != null) {
 				if (otherTrader.getBackpack().checkContents(item, 1)) {
 					customer.buy(otherTrader.getMarket(), item, 1);
 					otherTrader.getBackpack().remove(item, 1);
 					System.out.println(otherTrader.getBackpack());
 				}
 			} else {
 				customer.buy(market, item, 1);
 			}
 		}
 		
 		/**
 		 * toString
 		 * @return String
 		 */
 		@Override
 		public String toString() {
 			return "listener1";
 		}
 	}
 
 	/**
 	 * Listener for sale
 	 * 
 	 * @author Team Rocket
 	 * 
 	 */
 	private class SellListener extends ClickListener {
 		/**
 		 * Method clicked.
 		 * @param event InputEvent
 		 * @param x float
 		 * @param y float
 		 */
 		@Override
 		public void clicked(InputEvent event, float x, float y) {
 			if (otherTrader != null) {
 				if (otherTrader.getWallet().checkAmount(
 						((MarketPlaceItem) (otherTrader.getMarket().getStock()
 								.get(item))).getBuyingPrice(1))) {
 					customer.sell(otherTrader.getMarket(), item, 1);
 					otherTrader.getBackpack().place(item, 1);
 					System.out.println(otherTrader.getBackpack());
 				}
 
 			} else {
 				customer.sell(market, item, 1);
 			}
 		}
 		
 		/**
 		 * toString
 		 * @return String
 		 */
 		@Override
 		public String toString() {
 			return "listener2";
 		}
 	}
 
 	/**
 	 * @param tile
 	 *            the tile that represents the item
 	 * @param customer
 	 *            the person buying the item
 	 */
 	public void update(ItemTile tile, Person customer) {
 		this.item = tile.getItem();
 		this.stockInfo = tile.getStockInfo();
 		this.customer = customer;
 		this.tile = tile;
 		name.setText(item.toString());
 		description.setText(item.getDescription());
 		price.setText("$" + ((Float) (1.1f * stockInfo.getPrice())).toString());
 		stock.setText(tile.getStock().toString());
 		buysell.clear();
 		if (tile.isMarketItem()) {
 			buysell.add(buy);
 		} else {
 			buysell.add(sell);
 		}
 	}
 
 	/**
 	 * @param item
 	 *            the item to be updated
 	 * @param stockInfo
 	 *            the item in the marketplace
 	
 	 * @param tile
 	 *            the tile that represents the item
 	 * @param customer Person
 	 */
 	public void update(Item item, MarketPlaceItem stockInfo, Person customer,
 			ItemTile tile) {
 		this.item = item;
 		this.stockInfo = stockInfo;
 		this.customer = customer;
 		this.tile = tile;
 		name.setText(item.toString());
 		description.setText(item.getDescription());
 		stock.setText(stockInfo.getStock().toString());
 		price.setText("$" + ((Float) (1.1f * stockInfo.getPrice())).toString());
 		// ((BuyListener) buyListener).update(item, stockInfo, customer);
 		buysell.clear();
 		buysell.add(buy);
 		// sell.remove();
 		// addActorAfter(description, buy);
 
 	}
 
 	/**
 	 * @param item
 	 *            to be put in marketplace
 	 * @param stockInfo
 	 *            MarketPlaceItem representing the stock
 	 * @param backpackStock
 	 *            the stock of the backpack
 	 * @param customer
 	 *            the person with whom to trade
 	 */
 	public void update(Item item, MarketPlaceItem stockInfo,
 			Integer backpackStock, Person customer) {
 		this.item = item;
 		this.stockInfo = stockInfo;
 		this.customer = customer;
 		name.setText(item.toString());
 		description.setText(item.getDescription());
 		stock.setText(backpackStock.toString());
 		price.setText("$" + ((Float) (1.1f * stockInfo.getPrice())).toString());
 		buysell.clear();
 		buysell.add(sell);
 	}
 	
 	
 }
