 package com.example.homokaasuthegame;
 
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class Ant extends Enemy{
     private static BitmapTextureAtlas textureAtlas;
 	private static ITextureRegion textureRegion;
 
 	private static int SPR_COLUMN  = 2;
 	private static int SPR_ROWS    = 1;
 
 	public static void init(MainActivity activity) {
 	    textureAtlas = new BitmapTextureAtlas(activity.getTextureManager(),
	            200, 40, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	    textureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(textureAtlas, activity.getAssets(),
 	            "ant_sprsheet.png", 0, 0, SPR_COLUMN, SPR_ROWS);
 	    textureAtlas.load();
 	}
 
 	public Ant(float pX, float pY, boolean flip,
 	        VertexBufferObjectManager vertexBufferObjectManager) {
 		super(pX, pY, 0, 100f, 48f, textureRegion, vertexBufferObjectManager);
 		this.animate(100);
 		this.setFlippedHorizontal(flip);
 	}
 
 	public void setWalkSpeed(Vector2 velocity) {
 	    body.setLinearVelocity(velocity);
 	}
 }
