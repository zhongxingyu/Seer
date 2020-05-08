 package org.anddev.amatidev.pvb.singleton;
 
 import java.util.LinkedList;
 
 import org.amatidev.text.AdTextScoring;
 import org.amatidev.util.AdResourceLoader;
 import org.amatidev.util.AdSound;
 import org.anddev.amatidev.pvb.card.Card;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.util.HorizontalAlign;
 
 import android.graphics.Color;
 
 public class GameData {
 	
     private static GameData mInstance = null;
     
     // generici
     public LinkedList<Card> mCards;
     
     public AdTextScoring mMySeed;
     public AdTextScoring mMyScore;
     public AdTextScoring mMyLevel;
     
     public TextureRegion mSplash;
     public TextureRegion mDialog;
     public TextureRegion mMainTitle;
     public TextureRegion mMainBackground;
     public TextureRegion mBackground;
     
     public TextureRegion mArrow;
     public TextureRegion mTable;
     public TextureRegion mSeed;
     public TextureRegion mSeed2;
     public TextureRegion mShot;
     public TextureRegion mShotShadow;
     public TextureRegion mPlantShadow;
     public TextureRegion mCardSelected;
     
     // cards
     public TextureRegion mCard;
     public TextureRegion mCardTomato;
 	public TextureRegion mCardPotato;
 	public TextureRegion mCardOrange;
 	public TextureRegion mCardMelon;
 	public TextureRegion mCardBag;
 	
 	// plants
 	public TextureRegion mPlantTomato;
 	public TextureRegion mPlantPotato;
 	public TextureRegion mPlantOrange;
 	public TextureRegion mPlantMelon;
 	public TextureRegion mPlantBag;
 	
 	public TextureRegion mBugRip;
 	
 	// bugs
 	public TextureRegion mBugBeetle;
 	public TextureRegion mBugLadybug;
 	public TextureRegion mBugCaterpillar;
 	public TextureRegion mBugCaterpillar2;
 	public TextureRegion mBugSnail;
 
 	public TextureRegion mHelm1;
 	
 	// fonts
 	public Font mFontSeed;
 	public Font mFontCard;
 	public Font mFontScore;
 	public Font mFontEvent;
 	public Font mFontTutorial;
 	public Font mFontMainMenu;
 	public Font mFontGameMenu;
 
 	public TiledTextureRegion mBugLeg;
 	
 	public AdSound mSoundPlanting;
 	public AdSound mSoundPop;
 	public AdSound mSoundCard;
 	public AdSound mSoundSeed;
 	public AdSound mSoundMenu;
 	
 	private GameData() {
 		
 	}
 	
 	public static synchronized GameData getInstance() {
 		if (mInstance == null) 
 			mInstance = new GameData();
 		return mInstance;
 	}
 	
 	public void initData() {
 		this.mSoundPlanting = AdResourceLoader.getSound("planting");
 		this.mSoundPop = AdResourceLoader.getSound("pop");
 		this.mSoundCard = AdResourceLoader.getSound("card");
 		this.mSoundSeed = AdResourceLoader.getSound("seed");
 		this.mSoundMenu = AdResourceLoader.getSound("ok");
 		
 		this.mFontSeed = AdResourceLoader.getFont("akaDylan Plain", 20, 2, Color.WHITE, Color.BLACK);
 		this.mFontCard = AdResourceLoader.getFont("akaDylan Plain", 14, 1, Color.WHITE, Color.BLACK);
 		this.mFontScore = AdResourceLoader.getFont("akaDylan Plain", 22, 2, Color.WHITE, Color.BLACK);
 		
 		this.mDialog = AdResourceLoader.getTexture(512, 256, "dialog");
 		this.mFontEvent = AdResourceLoader.getFont(512, 512, "akaDylan Plain", 38, 3, Color.WHITE, Color.BLACK);
 		this.mFontTutorial = AdResourceLoader.getFont(512, 512, "akaDylan Plain", 56, 3, Color.WHITE, Color.BLACK);
 		this.mFontMainMenu = AdResourceLoader.getFont(512, 512, "akaDylan Plain", 40	, 2, Color.WHITE, Color.BLACK);
 		this.mFontGameMenu = AdResourceLoader.getFont(512, 512, "akaDylan Plain", 48, 3, Color.WHITE, Color.BLACK);
 		
 		this.mSplash = AdResourceLoader.getTexture(1024, 512, "splash");
 		this.mBackground = AdResourceLoader.getTexture(1024, 512, "back");
 		this.mMainBackground = AdResourceLoader.getTexture(1024, 512, "main2");
 		this.mMainTitle = AdResourceLoader.getTexture(1024, 256, "title");
 		
 		this.mTable  = AdResourceLoader.getTexture(1024, 128, "table");
 		this.mSeed = AdResourceLoader.getTexture(64, 64, "seed");
 		this.mSeed2 = AdResourceLoader.getTexture(64, 64, "seed2");
 		this.mShot = AdResourceLoader.getTexture(64, 64, "shot");
 		this.mShotShadow = AdResourceLoader.getTexture(64, 64, "shadow2");
 		
 		this.mCardSelected = AdResourceLoader.getTexture(64, 128, "select");
 		
 		this.mCard = AdResourceLoader.getTexture(64, 128, "card");
 		this.mArrow = AdResourceLoader.getTexture(64, 128, "arrow");
 		
 		this.mCardTomato = AdResourceLoader.getTexture(64, 64, "card_tomato");
 		this.mCardPotato = AdResourceLoader.getTexture(64, 64, "card_potato");
 		this.mCardOrange = AdResourceLoader.getTexture(64, 64, "card_orange");
 		this.mCardMelon = AdResourceLoader.getTexture(64, 64, "card_melon");
 		this.mCardBag = AdResourceLoader.getTexture(64, 64, "card_bag");
 		
 		this.mPlantShadow = AdResourceLoader.getTexture(64, 16, "shadow");
 		
 		this.mPlantTomato = AdResourceLoader.getTexture(64, 128, "tomato");
 		this.mPlantPotato = AdResourceLoader.getTexture(64, 128, "potato");
 		this.mPlantOrange = AdResourceLoader.getTexture(64, 128, "orange");
 		this.mPlantMelon = AdResourceLoader.getTexture(64, 128, "melon");
 		this.mPlantBag = AdResourceLoader.getTexture(64, 128, "bag");
 		
 		this.mBugRip = AdResourceLoader.getTexture(64, 128, "rip");
 		
 		this.mBugBeetle = AdResourceLoader.getTexture(64, 128, "beetle");
 		this.mBugLadybug = AdResourceLoader.getTexture(64, 128, "ladybug");
 		this.mBugCaterpillar = AdResourceLoader.getTexture(64, 128, "caterpillar");
 		this.mBugSnail = AdResourceLoader.getTexture(64, 128, "snail");
 		
 		this.mHelm1 = AdResourceLoader.getTexture(64, 128, "helm1");
 		
		this.mMySeed = new AdTextScoring(48, 67, GameData.getInstance().mFontSeed, HorizontalAlign.CENTER, 60);
 		this.mMyScore = new AdTextScoring(703, 30, GameData.getInstance().mFontScore, HorizontalAlign.RIGHT, 0, "Pt.");
		this.mMyLevel = new AdTextScoring(703, 70, GameData.getInstance().mFontScore, HorizontalAlign.RIGHT, 20, "Lv.");
 		
 		//this.mBugLeg = AdResourceLoader.getTexture(64, 64, "leg", 1, 2);
 		
 		this.mCards = new LinkedList<Card>();
 	}
 	
 }
