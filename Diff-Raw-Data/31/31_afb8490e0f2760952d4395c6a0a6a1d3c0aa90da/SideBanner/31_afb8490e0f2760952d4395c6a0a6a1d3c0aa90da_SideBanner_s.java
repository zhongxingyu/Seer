 package com.example.seamerchant.scene;
 
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.vbo.DrawType;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import com.example.seamerchant.andengine.AEUtils;
 import com.example.seamerchant.game.Game;
 
 public class SideBanner extends Base {
 
 	private static final int MAX_CHARS = 7;
 	private static final float OFFSET_LEFT = 610.0f;
 	private TextureRegion mBgTextureRegion;
 	private BitmapTextureAtlas mItemTexture;
 	private ITiledTextureRegion mDaysTextureRegion;
 	private Font mFont;
 	private Game mGame;
 	private Text mMoney;
 	private Text mWheat;
 	private Text mOlives;
 	private Text mBronze;
 	private Text mTime;
 	private TiledSprite mCurrentDay;
 	
 	public SideBanner(SimpleBaseGameActivity baseActivity, Game game) {
 		super(baseActivity);
 		mGame = game;
 	}
 
 	@Override
 	protected Scene initScene() {
 	    final Scene scene = new Scene();
 	    final Sprite backgroundSprite = new Sprite(OFFSET_LEFT, 0, mBgTextureRegion, getVertexBufferObjectManager());
 	    scene.attachChild(backgroundSprite);
 	    
 	    mMoney = new Text(OFFSET_LEFT+50, 300f, this.mFont, String.format("%d", mGame.getPlayer().getMoney()), MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    scene.attachChild(mMoney);
 	    mWheat = new Text(OFFSET_LEFT+30, 222f, this.mFont, String.format("%d", mGame.getPlayer().getWheat().getCount()), MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    scene.attachChild(mWheat);
 	    mOlives = new Text(OFFSET_LEFT+30, 189f, this.mFont, String.format("%d", mGame.getPlayer().getOlives().getCount()), MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    scene.attachChild(mOlives);
 	    mBronze = new Text(OFFSET_LEFT+30, 152f, this.mFont, String.format("%d", mGame.getPlayer().getBronze().getCount()), MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    scene.attachChild(mBronze);
 	    mCurrentDay = new TiledSprite(OFFSET_LEFT+30, 40, mDaysTextureRegion, getVertexBufferObjectManager()); 
 	    int currentDayIndex = 7 - mGame.getCurrentDay();
 		mCurrentDay.setCurrentTileIndex(currentDayIndex);
 	    scene.attachChild(mCurrentDay);
 	    mTime = new Text(OFFSET_LEFT+30, 68, this.mFont, setTime(), MAX_CHARS, getVertexBufferObjectManager(), DrawType.DYNAMIC);
 	    scene.attachChild(mTime);
 	    return scene;
 	}
 
 	private String setTime() {
 		switch (mGame.travelTime()) {
 		case 0:
 			return "08:00";
 		case 1:
 			return "12:00";		
 		case 2:
 			return "16:00";
 		case 3:
 			return "20:00";
 		case 4:
 			return "24:00";
 		default:
 			break;
 		}
 		return null;
 	}
 
 	@Override
 	protected void unloadResources() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void loadResources() {
 		if (mBgTextureRegion != null) {
 			return;//Do not load twice
 		}
 		mBgTextureRegion = AEUtils.createTextureRegionFromAssets("gfx/sidebg.png", mBaseActivity);
 		mFont = AEUtils.createGameFont(mBaseActivity);
 	    mFont.load();
 	    mItemTexture = new BitmapTextureAtlas(this.getTextureManager(), 512, 512, TextureOptions.BILINEAR);
 		mDaysTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mItemTexture, mBaseActivity, "gfx/days_text.png", 0, 0,1,7);
 		mItemTexture.load();
 	}
 
 	@Override
 	public int getSelectedItem() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void refresh() {
 		mMoney.setText(mGame.getPlayer().getMoney() + "");
 		mWheat.setText(mGame.getPlayer().getWheat().getCount() + "");
 		mOlives.setText(mGame.getPlayer().getOlives().getCount() + "");
 		mBronze.setText(mGame.getPlayer().getBronze().getCount() + "");
 		mTime.setText(setTime());
 	}
 
 }
