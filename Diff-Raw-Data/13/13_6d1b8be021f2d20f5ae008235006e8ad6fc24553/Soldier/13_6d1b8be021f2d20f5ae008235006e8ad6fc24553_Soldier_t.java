 package org.beavers.ingame;
 
 import org.anddev.andengine.entity.modifier.MoveModifier;
 import org.anddev.andengine.entity.modifier.RotationByModifier;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.beavers.AppActivity;
 
 public class Soldier extends GameObject {
 	
 	private BitmapTextureAtlas mBitmapTextureAtlas;
 	private TiledTextureRegion playerTextureRegion;
 	private AnimatedSprite sprite;
 	private int speed=80;
 	
 	public Soldier(final AppActivity pApp,int team, int x, int y){
 		
 		if(team==0){
 			mBitmapTextureAtlas = new BitmapTextureAtlas(128,128,TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 			playerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pApp, "96x96anim.png", 0, 0, 3, 2);
 			pApp.getEngine().getTextureManager().loadTexture(mBitmapTextureAtlas);
 		}
 		else{}
 	}
 	
 	public void createSoldier(Scene s,int x, int y){
 		sprite=new AnimatedSprite(x,y,playerTextureRegion);
 		sprite.stopAnimation();
 		sprite.setRotationCenter(sprite.getWidth()/2, sprite.getHeight()/2);
 		s.attachChild(sprite);
 	}
 	
 	public AnimatedSprite getSprite(){
 		return sprite;
 	}
 	
 	public float[] getSceneCoordinates(){
 		return sprite.convertLocalToSceneCoordinates(10, 10); //10x10 TMX Map
 	}
 	
 	
 	public void move(int x, int y){
 		//Bewegung nach x,y
 		float distx=Math.abs(x-(sprite.getX()+sprite.getWidth()/2));
 		float disty=Math.abs(y-(sprite.getY()+sprite.getHeight()/2));
 		MoveModifier mod = new MoveModifier((float) (Math.sqrt(distx*distx+disty*disty)/speed),sprite.getX(),x-sprite.getWidth()/2, sprite.getY(),y-sprite.getHeight()/2);
 		sprite.registerEntityModifier(mod);
 		
 		//Rotation
 		float angleX=x-(sprite.getX()+sprite.getWidth()/2);
 		float angleY=y-(sprite.getY()+sprite.getHeight()/2);
 		float angle=(float)Math.toDegrees(Math.atan2(angleY,angleX))+90;
 		RotationByModifier rotate;
	    //if(angle>180)angle=angle-360;
	    //if(angle<-180)angle=360+angle;
		if((angle-sprite.getRotation())>180)angle=(angle-sprite.getRotation())-360;
		else if((angle-sprite.getRotation())<-180)angle=360+(angle-sprite.getRotation());
		else angle=angle-sprite.getRotation();
			rotate= new RotationByModifier(0.2f, angle);
		
 		sprite.registerEntityModifier(rotate);
 		
 		sprite.animate(new long[]{200, 200}, 1, 2, true);
 		
 		
 	
 	}
 	
 	public void stop()
 	{
 		sprite.stopAnimation();
 	}
 	
 	public int getHealthPercentage()
 	{
 		return -1;
 	}
 	
 	public Weapon getWeapon()
 	{
 		return null;
 	}
 	
 	public ViewDirection getViewDirection()
 	{
 		return null;
 	}
 	
 	public Action getAction(int index)
 	{
 		return null;
 	}
 
 	@Override
 	public String getType() {
 		return this.getClass().getSimpleName();
 	}
 	
 	
 }
