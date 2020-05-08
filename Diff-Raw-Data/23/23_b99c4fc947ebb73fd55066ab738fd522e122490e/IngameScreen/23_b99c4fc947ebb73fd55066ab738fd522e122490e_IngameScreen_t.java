 package de.bitowl.space;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Peripheral;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 
 import de.bitowl.space.objects.Chunk;
 import de.bitowl.space.objects.ConfiguredEnemy;
 import de.bitowl.space.objects.ConfiguredItem;
 import de.bitowl.space.objects.GameObjects;
 import de.bitowl.space.objects.Item;
 import de.bitowl.space.objects.Marker;
 import de.bitowl.space.objects.Planet;
 import de.bitowl.space.objects.Player;
 import de.bitowl.space.objects.Ship;
 import de.bitowl.space.objects.Shot;
 
 /**
  * the screen where all the good stuff is happening
  * 
  * @author bitowl
  *
  */
 public class IngameScreen extends AbstractScreen{
 
 	TextureRegion background;
 	
 	/**
 	 * our main hero
 	 */
 	Player player;
 	
 
 	
 	/**
 	 * textures for the points for control-visuals
 	 */
 	TextureRegion point_start,point_end;
 	/**
 	 * coordinates for these points
 	 */
 	int psX=-1,psY,peX,peY;
 	
 	AtlasRegion switchWeapons;
 	
 	
 	
 	private static final float SENSITIVITY = 3f;
 	
 	/**
 	 * keeps track of all the chunks
 	 */
 	ArrayList<Chunk> chunks;
 	
 	public ArrayList<Marker> marker;
 	
 	
 	/**
 	 * the health bar
 	 */
 	TextureRegion lifeborder,lifefill;
 	
 	
 	enum ControlScheme{
 		TOUCH_JOYSTICK,
 		TOUCH_AIM,
 		ACCELERATION
 	};
 	/**
 	 * which control scheme to use
 	 */
 	ControlScheme controls=ControlScheme.TOUCH_JOYSTICK;
 	
 	
 	Random random;
 	
 	Rectangle cameraRect;
 	
 	
 	float gameZoom=1;
 	float guiZoom=1;
 	
 	
 	public Array<ConfiguredEnemy> enemyTypes;
 	public float totalEnemyFrequency;
 	public Array<ConfiguredItem> itemTypes;
 	public float totalItemFrequency;
 	
 	public IngameScreen(SpaceGame pGame) {
 		super(pGame);
 
 		Resources.ingame=this;
 		
 		chunks=new ArrayList<Chunk>();
 		marker=new ArrayList<Marker>();
 		
 		Chunk chunk=new Chunk(0,0);
 		chunks.add(chunk);
 		
 		player=new Player();
 		GameObjects.player=player;
 		// set player to the center of the world
 		player.setX(-player.getWidth()/2);player.setY(-player.getHeight()/2);
 		player.setRotation(-90);
 		
 		
 		GameInputProcessor inputProcessor=new GameInputProcessor();
 		Gdx.input.setInputProcessor(inputProcessor);
 		
 		// get preferences
 		gameZoom=Preferences.getFloat("gameZoom",1f);
 		
 		cameraRect=new Rectangle();
 		
 		// get textures
 		point_start=Resources.atlas.findRegion("point_start");
 		point_end=Resources.atlas.findRegion("point_end");
 		background=Resources.atlas.findRegion("background");
 		lifeborder=Resources.atlas.findRegion("life_border");
 		lifefill=Resources.atlas.findRegion("life_fill");
 		switchWeapons=Resources.atlas.findRegion("switchWeapons");
 		
 		
 		// read configuration for enemies and weapons
 		ConfigReader.initGameValues();
 		
 		//marker.add(new Marker(400,400));
 		
 	}
 	
 	
 	/**
 	 * we could land on this planet in this frame
 	 */
 	public Planet avaiableToLandOnThisFrame;
 	
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		camera.zoom=gameZoom;
 
 		// TODO only calculate one? do not reset position of the cam when drawing hud? use different cam?
 		camera.position.x=player.getX()+player.getOriginX();
 		camera.position.y=player.getY()+player.getOriginY();
 		
 		avaiableToLandOnThisFrame=null;
 		if(player.life<=0){
 			game.setScreen(new GameOverScreen(game));
 			//Gdx.app.exit(); // GAME OVER
 		}
 
 
 		
 		
 		// control the player via acceleration
 		if(controls==ControlScheme.ACCELERATION){
 			if( Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer) ){
 				// TODO add option to switch axis and invert them 
 				float accelX = Gdx.input.getAccelerometerX();
 				float accelY = Gdx.input.getAccelerometerY();
 				Gdx.app.log("ACCELERATION",accelX+","+accelY);
 				player.speed=200;//(float) Math.sqrt(accelX*accelX+accelY*accelY);
 				player.angle=MathUtils.atan2(-accelX, accelY);
 				// rotate the player acording to his angle
 				player.setRotation(MathUtils.radDeg*player.angle-90);
 			}
 		}
 		
 		player.act(getChunk(player.getX(), player.getY()),delta);
 		
 		// check if the player gets outside all loaded chunks
 		checkChunks();
 		
 		// keep player inside the game field
 		//if(player.getX()<0){player.setX(0);}else if(player.getX()>chunk.width){player.setX(chunk.width);}
 		//if(player.getY()<0){player.setY(0);}else if(player.getY()>chunk.height){player.setY(chunk.height);}
 		
 		for(int i=0;i<chunks.size();i++){
 			chunks.get(i).checkRemove();// check wheter ships, shots or whatsoever have to be moved to another chunk
 		}
 		for(int i=0;i<chunks.size();i++){
 			chunks.get(i).act(delta);	
 		}
 		
 		// System.out.println("chunks: "+chunks.size());
 		
 
 
 		
 		// scrolling
 		camera.position.x=player.getX()+player.getOriginX();
 		camera.position.y=player.getY()+player.getOriginY();
 		
 		camera.update();
 		camera.activate(batch);
 		cameraRect.set(camera.position.x-camera.getWidth()/2, camera.position.y-camera.getHeight()/2, camera.getWidth(), camera.getHeight());
 		
 		
 		batch.begin();
 		batch.draw(background, camera.position.x-camera.getWidth()/2,camera.position.y-camera.getHeight()/2,camera.getWidth(),camera.getHeight());
 		
 		
 		// draw only visible chunks
 		for(int i=0;i<chunks.size();i++){
 			if(chunks.get(i).rect.overlaps(cameraRect)){
 				// this chunk is in the visible drawarea
 				chunks.get(i).draw(cameraRect,batch);
 				
 				
 				//// DEBUG
				//Resources.font.draw(batch, "chunk "+i, chunks.get(i).x,chunks.get(i).y+40);
 			}
 		}
 
 		
 		player.draw(batch, 1);
 		
 
 	
 		// draw control-visuals
 		if(psX!=-1){
 			batch.draw(point_start, psX*camera.zoom+camera.position.x-32,psY*camera.zoom+camera.position.y-32);
 			batch.draw(point_end, peX*camera.zoom+camera.position.x-16,peY*camera.zoom+camera.position.y-16);
 		}
 		
 		
 		for(int i=0;i<marker.size();i++){
 			marker.get(i).draw(cameraRect, batch);
 		}
 		batch.end();
 		
 		
 
 		
 		///// DEBUG RENDERER /////
	/*	ShapeRenderer renderer=new ShapeRenderer();
 		renderer.begin(ShapeType.Rectangle);
 		renderer.setProjectionMatrix(camera.combined);
 		renderer.setColor(255,0,0,10);
 		for(int i=0;i<chunks.size();i++){
 			if(chunks.get(i).rect.overlaps(cameraRect)){
 				renderer.rect(chunks.get(i).x, chunks.get(i).y, Chunk.width, Chunk.height);		
 			}
 		}
 		
 		renderer.setColor(0,255,0,10);
 		renderer.rect(camera.position.x-camera.getWidth()/2  +1 , camera.position.y-camera.getHeight()/2 +1, camera.getWidth()-2, camera.getHeight()-2);
 		//for(float y=player.getY()-camera.getHeight()/2;y<player.getY()+camera.getHeight()/2;y+=Chunk.height){
 		//	for(float x=player.getX()-camera.getWidth()/2;x<player.getX()+camera.getWidth()/2;x+=Chunk.width){
 
 		
 		renderer.setColor(0,0,255,10);
 		for(float y=camera.position.y-camera.getHeight()/2 - Chunk.height ;y<camera.position.y+camera.getHeight()/2 +Chunk.height;y+=Chunk.height){
 			for(float x=camera.position.x-camera.getWidth()/2 - Chunk.width ;x<camera.position.x+camera.getWidth()/2 +Chunk.width;x+=Chunk.width){
 				if(getChunk(x, y)==null){
 					renderer.setColor(0,0,255,10);			
 				}else{
 					renderer.setColor(0,0,0,10);
 				}
 				renderer.rect(x,y,Chunk.width,Chunk.height);
 			}
 		}
		renderer.end();*/
 		
 		
 		
 		///// HUD //////
 		
 		camera.position.x=0;
 		camera.position.y=0;
 		camera.zoom=guiZoom;
 		camera.update();
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		int padding=10;
 		if(player.weapon.maxAmmo==-1){
 			Resources.font.draw(batch, "ammo inf", -camera.getWidth()/2+padding,camera.getHeight()/2);
 		}else{
 			Resources.font.draw(batch, "ammo "+player.weapon.ammo, -camera.getWidth()/2+padding,camera.getHeight()/2);
 		}
 		
 		Resources.font.drawWrapped(batch, "money: "+(int)player.money, -camera.getWidth()/2,camera.getHeight()/2,camera.getWidth()-padding,HAlignment.CENTER);
 		
 //		Resources.font.drawWrapped(batch, "life "+(int)player.life, -camera.getWidth()/2,camera.getHeight()/2,camera.getWidth()-padding,HAlignment.RIGHT);
 		// a really cool life bar
 		lifefill.setRegionWidth((int) (player.life*2));
 		batch.draw(lifefill, camera.getWidth()/2-200-padding, camera.getHeight()/2-40,player.life*2,32);//, originX, originY, width, height, scaleX, scaleY, rotation)
 		
 		batch.draw(lifeborder, camera.getWidth()/2-200-padding, camera.getHeight()/2-40);//, originX, originY, width, height, scaleX, scaleY, rotation)
 		
 		
 		if(avaiableToLandOnThisFrame!=null){
 			Resources.font.drawWrapped(batch, "land on this planet", -camera.getWidth()/2,-camera.getHeight()/2+40,camera.getWidth()-padding,HAlignment.RIGHT);
 		}else{
 			batch.draw(switchWeapons, camera.getWidth()/2-62,-camera.getHeight()/2);
 		}
 		
 		
 		
 		
 		Resources.font.drawWrapped(batch, "FPS "+Gdx.graphics.getFramesPerSecond(), padding-camera.getWidth()/2,-camera.getHeight()/2+40,camera.getWidth(),HAlignment.LEFT);
 		
 		
 		batch.end();
 		
 		
 		
 	}
 	
 	
 	
 	
 	
 
 	/**
 	 * returns the chunk this point is in
 	 * @param pX
 	 * @param pY
 	 * @return
 	 */
 	public Chunk getChunk(float pX, float pY) {
 		for(int i=0;i<chunks.size();i++){
 			if(chunks.get(i).isInChunk(pX,pY)){
 				return chunks.get(i);
 			}
 		}
 		return null;
 	}
 
 	
 	private void checkChunks(){
 		// do we need to add a new chunk?
 		// TODO do not always go through all chunks to see if this particular one is there
 		//      but link them (maybe in all four directions)
 		/*if(getChunk(player.getX()-camera.getWidth()/2,player.getY()-camera.getHeight()/2)==null){
 			addChunkFor(player.getX()-camera.getWidth()/2,player.getY()-camera.getHeight()/2);
 		}
 		if(getChunk(player.getX()-camera.getWidth()/2,player.getY()+camera.getHeight()/2)==null){
 			addChunkFor(player.getX()-camera.getWidth()/2,player.getY()+camera.getHeight()/2);
 		}
 		if(getChunk(player.getX()+camera.getWidth()/2,player.getY()-camera.getHeight()/2)==null){
 			addChunkFor(player.getX()+camera.getWidth()/2,player.getY()-camera.getHeight()/2);
 		}
 		if(getChunk(player.getX()+camera.getWidth()/2,player.getY()+camera.getHeight()/2)==null){
 			addChunkFor(player.getX()+camera.getWidth()/2,player.getY()+camera.getHeight()/2);
 		}*/
 		
 		// check all chunks
 		//System.out.println( (player.getY()-camera.getHeight()/2) +" -> "+ (player.getY()+camera.getHeight()/2 ));
 		
 		int i=0;
 		for(float y=camera.position.y-camera.getHeight()/2 - Chunk.height ;y<camera.position.y+camera.getHeight()/2 +Chunk.height;y+=Chunk.height){
 			for(float x=camera.position.x-camera.getWidth()/2 - Chunk.width ;x<camera.position.x+camera.getWidth()/2 +Chunk.width;x+=Chunk.width){
 				//System.out.println(x+"..."+y);
 				i++;
 				if(getChunk(x,y)==null){
 					System.err.println("------------------");
 					addChunkFor(x,y);
 				}
 			}
 		}
 		System.out.println(i+" Chunks checked.");
 		
 		
 		// can we remove an old chunk?
 		/*for(int i=0;i<chunks.size();i++){
 			chunks.get(i);
 			chunks.get(i);
 			if(Utils.distance(chunks.get(i).x+Chunk.width/2, chunks.get(i).y+Chunk.height/2, player.getCenterX(),player.getCenterY())>10000){
 				chunks.remove(i);
 				i--;
 				System.err.println("remove Chunk");
 			}
 		}*/
 	}
 	/**
 	 * adds chunk that covers the given point
 	 */
 	private void addChunkFor(float pX, float pY) {
 		if(pX<0){pX-=Chunk.width;}int x=(int)(pX/Chunk.width)*Chunk.width;
 		if(pY<0){pY-=Chunk.height;}int y=(int)(pY/Chunk.height)*Chunk.height;
 		System.err.println("add Chunk "+x+","+y);
 		chunks.add(new Chunk(x,y));
 	}
 	
 	
 
 	
 	
 	public void addShot(Shot shot){
 		System.out.println(shot.getX()+"-"+shot.getY());
 		Chunk chunk=getChunk(shot.getX(),shot.getY());
 		if(chunk!=null){
 			chunk.shots.add(shot);
 		}else{
 			System.out.println("lost one shot in the universe");
 		}
 	}
 	
 	public void addItem(Item item){
 		Chunk chunk=getChunk(item.getX(),item.getY());
 		if(chunk!=null){
 			chunk.items.add(item);
 		}else{
 			System.out.println("One item got lost in the universe!");
 		}
 	}
 
 	public void addShip(Ship ship){
 		Chunk chunk=getChunk(ship.getX(),ship.getY());
 		if(chunk!=null){
 			chunk.ships.add(ship);
 		}else{
 			System.out.println("Sir, we lost one ship in the deep of the universe :(");
 		}
 	}
 	
 	class GameInputProcessor implements InputProcessor{
 		/**
 		 * where the player initially touched the screen
 		 */
 		float originX,originY;
 		
 		@Override
 		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 			Gdx.app.log("TOUCH", "pointer: "+pointer);
 			Vector3 touchPos = new Vector3();
 			touchPos.set(screenX, screenY, 0);
 			camera.unproject(touchPos);
 			if(pointer==0){ // the first finger is for movement
 				if(controls==ControlScheme.TOUCH_JOYSTICK){
 
 						
 					originX=touchPos.x-camera.position.x;
 					originY=touchPos.y-camera.position.y;
 				
 					// update control-visuals
 					psX=(int) (touchPos.x-camera.position.x);peX=psX;
 					psY=(int) (touchPos.y-camera.position.y);peY=psY;
 				}else if(controls==ControlScheme.TOUCH_AIM){
 			//		player.aimX=touchPos.x;
 				//	player.aimY=touchPos.y;
 				}
 				
 
 				
 			}else{ // the second finger makes our ship shoot
 				//player.shoot();
 				player.isShooting=true;
 				//return false;
 			}
 			
 			// we want to be able to shoot while moving
 			if(touchPos.y-camera.position.y<-camera.getHeight()/2+64&&touchPos.x-camera.position.x>camera.getWidth()/2-64){
 				if(avaiableToLandOnThisFrame!=null){
 					//game.setScreen(new GameOverScreen(game));
 				}else{
 					player.switchWeapon();
 				}
 			}
 			
 			if(controls==ControlScheme.ACCELERATION){
 				player.isShooting=true;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 			if(pointer == 0){
 				player.accSpeed=-600;
 			
 				psX=-1;// do not show the visuals anymore
 			}else{
 				player.isShooting=false;
 			}
 			if(controls==ControlScheme.ACCELERATION){
 				player.isShooting=false;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean touchDragged(int screenX, int screenY, int pointer) {
 			if(pointer==0){
 				Vector3 touchPos = new Vector3();
 				touchPos.set(screenX, screenY, 0);
 				camera.unproject(touchPos);
 				if(controls==ControlScheme.TOUCH_JOYSTICK){
 					//
 					player.accSpeed=80;//;(float)Math.sqrt((touchPos.x-camera.position.x-originX)*(touchPos.x-camera.position.x-originX)+(touchPos.y-camera.position.y-originY)*(touchPos.y-camera.position.y-originY))*SENSITIVITY;//-150;
 					player.MAX_SPEED=player.accSpeed=(float)Math.sqrt((touchPos.x-camera.position.x-originX)*(touchPos.x-camera.position.x-originX)+(touchPos.y-camera.position.y-originY)*(touchPos.y-camera.position.y-originY))*SENSITIVITY;
 					if(player.MAX_SPEED>500){player.MAX_SPEED=500;}
 					//player.speed=(float)Math.sqrt((touchPos.x-camera.position.x-originX)*(touchPos.x-camera.position.x-originX)+(touchPos.y-camera.position.y-originY)*(touchPos.y-camera.position.y-originY))*SENSITIVITY;
 					player.angle=MathUtils.atan2((touchPos.y-camera.position.y-originY),(touchPos.x-camera.position.x-originX));
 				
 					// rotate the player acording to his angle
 					player.setRotation(MathUtils.radDeg*player.angle-90);
 				
 					// update control-visuals
 					peX=(int) (touchPos.x-camera.position.x);
 					peY=(int) (touchPos.y-camera.position.y);
 				
 					Gdx.app.log("ROT",""+MathUtils.radDeg*player.angle);
 				}else if(controls==ControlScheme.TOUCH_AIM){
 					//player.speed=(float)Math.sqrt((touchPos.x-player.getX())*(touchPos.x-player.getX())+(touchPos.y-player.getY())*(touchPos.y-player.getY()))*SENSITIVITY;
 			//		player.aimX=touchPos.x;
 				//	player.aimY=touchPos.y;
 				}
 				
 				
 			}
 			return false;
 		}
 
 		
 		@Override
 		public boolean keyDown(int keycode) {
 			if(keycode==Keys.RIGHT){
 				
 			}else if(keycode==Keys.SPACE){
 				player.isShooting=true;
 			}else if(keycode==Keys.X||keycode==Keys.MENU){
 				player.switchWeapon();
 			}
 			
 			
 			else if(keycode==Keys.PLUS || keycode==Keys.VOLUME_UP){
 				gameZoom-=0.5f;
 				Preferences.putFloat("gameZoom", gameZoom);
 				Preferences.flush(); // TODO only flush once, when ingame/options screen is finished?
 			}else if(keycode==Keys.MINUS || keycode==Keys.VOLUME_DOWN){
 				gameZoom+=0.5f;
 				Preferences.putFloat("gameZoom", gameZoom);
 				Preferences.flush(); // TODO only flush once, when ingame/options screen is finished?
 			}
 			return false;
 		}
 
 		@Override
 		public boolean keyUp(int keycode) {
 			if(keycode==Keys.SPACE){
 				player.isShooting=false;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean keyTyped(char character) {
 			return false;
 		}
 
 
 		@Override
 		public boolean mouseMoved(int screenX, int screenY) {
 			return false;
 		}
 		@Override
 		public boolean scrolled(int amount) {
 			return false;
 		}
 	}
 }
