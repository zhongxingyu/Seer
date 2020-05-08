 package org.anddev.andengine.pvb.singleton;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.extra.Resource;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.pvb.card.Card;
 
 public class GameData {
 	
     private static GameData mInstance = null;
     
     // cards
     public TextureRegion mSelect;
     public TextureRegion mFlower1;
 	public TextureRegion mFlower2;
 
 	// objects
 	public TextureRegion mObjectFlower1;
 	public TextureRegion mObjectFlower2;
 	
 	private LinkedList<Card> mCards;
 	
 	private GameData() {
 		
 	}
 	
 	public static synchronized GameData getInstance() {
 		if (mInstance == null) 
 			mInstance = new GameData();
 		return mInstance;
 	}
 	
 	public void initData() {
 		this.mSelect = Resource.getTexture(64, 128, "select");
 		
 		this.mFlower1 = Resource.getTexture(64, 128, "card");
 		this.mFlower2 = Resource.getTexture(64, 128, "card1");
 		
		this.mObjectFlower1 = Resource.getTexture(64, 64, "default");
		this.mObjectFlower2 = Resource.getTexture(64, 64, "default2");
 		
 		this.mCards = new LinkedList<Card>();
 	}
 	
 	public LinkedList<Card> getCards() {
 		return this.mCards;
 	}
 }
