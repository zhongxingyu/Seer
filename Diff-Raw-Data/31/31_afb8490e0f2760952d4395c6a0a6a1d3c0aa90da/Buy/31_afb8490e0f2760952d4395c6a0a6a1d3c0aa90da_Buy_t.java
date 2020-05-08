 package com.example.seamerchant.scene;
 
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.vbo.DrawType;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import com.example.seamerchant.andengine.AEUtils;
 import com.example.seamerchant.game.Game;
 import com.example.seamerchant.game.Item;
 import com.example.seamerchant.game.PricedItem;
 import com.example.seamerchant.scene.NumKeyboard.OnNumKeyboardUpdateListener;
 
 public class Buy extends Main implements OnClickListener, OnNumKeyboardUpdateListener {
 	private static final int MAX_CHARS = 7;
 	private TextureRegion mBgTextureRegion;
 	private BitmapTextureAtlas mItemTexture;
 	private ITiledTextureRegion mItemWheatTextureRegion;
 	private ITiledTextureRegion mItemBronzeTextureRegion;
 	private ITiledTextureRegion mItemOlivesTextureRegion;
 	private ITextureRegion mBuyInputTextureRegion;
 	private Scene mItemsScene;
 	private NumKeyboard mNumKeyboard;
 	private Sprite mBuyInput;
 	private Font mFont;
 	private Text mAvailableText;
 	private Text mQuantityText;
 	private Game mGame;
 	private OnBuyItemListener mListener;
 	private PricedItem mSelectedItem;
 	private int mPurchasableCount;
 	
 	public interface OnBuyItemListener {
 		void onBuyItem(PricedItem item, int count, Buy buy);
 	}
 	
 	public Buy(SimpleBaseGameActivity baseActivity, SideBanner sideBanner, LowerBanner lowerBanner, Game game, OnBuyItemListener listener) {
 		super(baseActivity, sideBanner, lowerBanner);
 		mNumKeyboard = new NumKeyboard(400, 25,baseActivity, this);
 		mGame = game;
 		mListener = listener;
 	}
 
 	@Override
 	protected Scene initSceneImpl() {
 		mItemsScene = new Scene();
 		final Sprite backgroundSprite = new Sprite(0, 0, mBgTextureRegion, getVertexBufferObjectManager());
 		mItemsScene.attachChild(backgroundSprite);
 
 		final ButtonSprite wheatTextItem = new ButtonSprite(427, 205, mItemWheatTextureRegion, getVertexBufferObjectManager(), this);
 		wheatTextItem.setTag(Item.WHEAT);
 		mItemsScene.registerTouchArea(wheatTextItem);
 		mItemsScene.attachChild(wheatTextItem);
 
 		final ButtonSprite bronzeTextItem = new ButtonSprite(424, 4, mItemBronzeTextureRegion, getVertexBufferObjectManager(), this);
 		bronzeTextItem.setTag(Item.BRONZE);
 		mItemsScene.registerTouchArea(bronzeTextItem);
 		mItemsScene.attachChild(bronzeTextItem);
 		
 		final ButtonSprite oliveTextItem = new ButtonSprite(18, 4, mItemOlivesTextureRegion, getVertexBufferObjectManager(), this);
 		oliveTextItem.setTag(Item.OLIVES);
 		mItemsScene.registerTouchArea(oliveTextItem);
 		mItemsScene.attachChild(oliveTextItem);
 		
 		mItemsScene.setTouchAreaBindingOnActionDownEnabled(true);
 		
 		mBuyInput = new Sprite(75, 255, mBuyInputTextureRegion, getVertexBufferObjectManager());
 	    mAvailableText = new Text(110, 258, mFont, "0", MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    mQuantityText = new Text(110, 295, mFont, "0", MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 
 	    
 		mNumKeyboard.loadScene();
 		
 		return mItemsScene;
 	}
 
 	@Override
 	protected void loadResourcesImpl() {
 		mBgTextureRegion = AEUtils.createTextureRegionFromAssets("gfx/buy_bg.png", mBaseActivity);
 		
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		mItemTexture = new BitmapTextureAtlas(this.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
 		mItemWheatTextureRegion  = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mItemTexture, mBaseActivity, "buy_item_wheat.png",  0,   0, 1, 2);
 		mItemBronzeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mItemTexture, mBaseActivity, "buy_item_bronze.png", 0,  84, 1, 2);
 		mItemOlivesTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mItemTexture, mBaseActivity, "buy_item_olives.png", 0, 168, 1, 2);
 
 		mBuyInputTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mItemTexture, mBaseActivity, "buy_input.png", 0, 276);
 		
 		mBgTextureRegion.getTexture().load();
 		mItemTexture.load();
 		mNumKeyboard.loadResources();
 		mFont = AEUtils.createGameFont(mBaseActivity);
 	    mFont.load();
 	}
 
 	@Override
 	protected void unloadResources() {
 		mBgTextureRegion.getTexture().unload();
 		mItemTexture.unload();
 		mNumKeyboard.unloadResources();
 	}
 
 	@Override
 	public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 		//int tag = pButtonSprite.getTag();
 		pButtonSprite.setCurrentTileIndex(1);
 		int itemId = pButtonSprite.getTag();
 		mSelectedItem = mGame.getCurrentLocation().getItem(itemId);
		//int purchasable = (int)(mGame.getPlayer().getMoney() / mSelectedItem.getPrice());
		//mPurchasableCount = Math.min(mSelectedItem.getCount(), purchasable);
		mPurchasableCount = (int)(mGame.getPlayer().getMoney() / mSelectedItem.getPrice());
 		Debug.d("Money: "+mGame.getPlayer().getMoney()+", Price: "+mSelectedItem.getPrice()+", Available:" + mPurchasableCount);
 		//mAvailableText.setText(mSelectedItem.getCount() + "");
 		mAvailableText.setText(mPurchasableCount + "");
 		mItemsScene.attachChild(mBuyInput);
 		mItemsScene.attachChild(mAvailableText);
 		mItemsScene.attachChild(mQuantityText);
 		mItemsScene.setChildScene(mNumKeyboard.getScene());
 
 	}
 
 	@Override
 	public boolean onNumberUpdate(Integer num, NumKeyboard kb) {
 		if (num == null) {
 			mQuantityText.setText("0");
 			return false;
 		}
 		if (num <= mPurchasableCount) {
 			mQuantityText.setText(num + "");
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onNumberEnter(Integer num, NumKeyboard kb) {
 		mItemsScene.clearChildScene();
 		mItemsScene.detachChild(mBuyInput);
 		mItemsScene.detachChild(mAvailableText);
 		mItemsScene.detachChild(mQuantityText);
 		if (num != null) {
 			mListener.onBuyItem(mSelectedItem, num, this);
 		} else {
 			//Unpress button
 			getSpriteByItemId(mSelectedItem.getType()).setCurrentTileIndex(0);
 		}
 		mSelectedItem = null;
 	}
 	
 	private ButtonSprite getSpriteByItemId(int itemId) {
 		return (ButtonSprite)mItemsScene.getChildByTag(itemId);
 	}
 
 }
