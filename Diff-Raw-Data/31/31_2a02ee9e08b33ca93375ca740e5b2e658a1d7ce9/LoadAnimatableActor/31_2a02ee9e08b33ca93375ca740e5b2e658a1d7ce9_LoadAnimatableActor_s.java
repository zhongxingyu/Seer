 package uk.co.jwlawson.aniguffins;
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class LoadAnimatableActor extends AnimatableActor {
 
 	private String id;
 	private int tilesize = 16;
 
 	public LoadAnimatableActor(TextureRegion tex) {
 		super(tex);
 	}
 
 	protected void setId(String id) {
 		this.id = id;
 	}
 
 	protected void load(TextureAtlas atlas, String... strings) {
 		for (String animation : strings) {
 			loadAnimation(animation, atlas);
 		}
 	}
 
 	private void loadAnimation(String animation, TextureAtlas atlas) {
 		TextureRegion reg = atlas.findRegion(id + "." + animation);
		Animation ani = getAnimation(reg, 0.3f, tilesize);
 		ani.setPlayMode(Animation.LOOP);
 		addAnimation(animation, ani);
 	}
 }
