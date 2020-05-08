 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Animation {
 
 	// public static final int HERO_TEXTURE = R.drawable.sprite_tiles;
 	
	public static final int[] ANIMATION_RUNNING = {1,2,3,4,5,6,7,8};
 
 	public static final int FRAME_JUMP_BUTTON = 17;
 	public static final int FRAME_JUMP_BUTTON_DOWN = 18;
 	public static final int FRAME_DOUBLE_JUMP_BUTTON = 19;
 	public static final int FRAME_DOUBLE_JUMP_BUTTON_DOWN = 20;
 	public static final int FRAME_PROJECTILE_RAW_BULLET = 21;
 
 	public static final int[] FRAME_TEST_TILES = { 28, 29, 30, 31, 32, 33, 34,
 			35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
 			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68,
 			69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85,
 			86, 87, 88, 89, 90, 91 };
 
 	public static final int FPS = 12;
 	
 	private TextureRegion[] mFrames;
 
 	public Animation() {
 		
 	}
 	
 	public void setAnimation(TextureRegion[] animation){
 		mFrames = animation;
 	}
 	
 	public void createAnimation(SpriteSheet spriteSheet, int[] frameNumbers){
 		mFrames = new TextureRegion[frameNumbers.length];
 		for(int i=0;i<mFrames.length;i++){
 			mFrames[i] = new TextureRegion(spriteSheet.getTexture(), spriteSheet.getFrames()[frameNumbers[i]][0], spriteSheet.getFrames()[frameNumbers[i]][1], spriteSheet.getFrames()[frameNumbers[i]][2], spriteSheet.getFrames()[frameNumbers[i]][3]);
 		}
 	}
 	
 	public void createFrame(SpriteSheet spriteSheet, int frameNumber){
 		mFrames = new TextureRegion[1];
 		mFrames[0] = new TextureRegion(spriteSheet.getTexture(), spriteSheet.getFrames()[frameNumber][0], spriteSheet.getFrames()[frameNumber][1], spriteSheet.getFrames()[frameNumber][2], spriteSheet.getFrames()[frameNumber][3]);
 	}
 	
 	public TextureRegion[] getFrames(){
 		return mFrames;
 	}
 	
 	public static int convertTileFrame(int frame){
 		//TODO this is kind of an ugly way to do this.
 		int f = 0;
 		if(frame>0&&frame<65){
 			f = Animation.FRAME_TEST_TILES[frame-1];
 		}else{
 			f = frame;
 		}
 		return f;
 	}
 
 }
