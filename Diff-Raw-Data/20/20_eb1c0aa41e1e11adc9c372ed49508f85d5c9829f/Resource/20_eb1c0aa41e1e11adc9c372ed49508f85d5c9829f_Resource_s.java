 package se.chalmers.dat255.risk.view.resource;
 
import java.io.File;

 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 
 public class Resource {
 
 	private static Resource instance;
 	
 	public Texture backGround = new Texture(Gdx.files.internal("Gfx/RiskMap1.png"));
 	public Texture cardStageBg2 = new Texture(Gdx.files.internal("Gfx/RiskMap.png")); 
 	public Texture card1 = new Texture(Gdx.files.internal("Gfx/card1.png"));
 	public Texture card2 = new Texture(Gdx.files.internal("Gfx/card1.png")); //Will be card2 later.
 	public Texture card3 = new Texture(Gdx.files.internal("Gfx/card3.jpg"));
 	public Texture bucket = new Texture(Gdx.files.internal("Gfx/bucket.png"));
 	public Texture cardHolder = new Texture(Gdx.files.internal("Gfx/RiskCard.jpg"));
 	
 	public static Resource getInstance(){
 		if(instance == null){
 			instance = new Resource();
 		}
 		return instance;
 	}
 	
 	public Resource(){
 		loadResources();
 	}
 	
 	public void loadResources(){
 		backGround = new Texture(Gdx.files.internal("Gfx/RiskMap1.png"));
 		card1 = new Texture(Gdx.files.internal("Gfx/card1.png"));
 		card2 = new Texture(Gdx.files.internal("Gfx/card1.png")); //Will be card2 later.
 		card3 = new Texture(Gdx.files.internal("Gfx/card3.jpg"));
 		bucket = new Texture(Gdx.files.internal("Gfx/bucket.png"));
 		cardStageBg2 = new Texture(Gdx.files.internal("Gfx/RiskMap.png")); 
 		cardHolder = new Texture(Gdx.files.internal("Gfx/RiskCard.jpg"));
 	}
 	
 	public void dispose(){
 		backGround.dispose();
 	}
 }
