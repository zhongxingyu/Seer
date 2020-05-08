 package com.detourgames.raw;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Tile extends Sprite{
 	
	private static final float HEIGHT = 32f;
	private static final float WIDTH = 32f;
 	
 	public Tile(){
 		super(new PhysicsStatic(), new AnimationComponent());
 		
 	}
 	
 	public void create(World world, float x, float y, Animation animation, Vector2[] vertices){
 		
 		mDrawWidth = WIDTH;
 		mDrawHeight = HEIGHT;
 		mDrawOffsetX = 0;
 		mDrawOffsetY = 0;
 		//mPhysics.create(world, x, y, 0.5f, 0.5f, false);
 		mPhysics.create(world, x, y, vertices, false);
 		
 		
 		mAnimation.setAnimation(animation);
 		pauseAnimation();
 		
 	}
 	
 	private int convertTileFrame(int frame){
 		//TODO this is kind of an ugly way to do this.
 		int f = 0;
 		if(frame>0&&frame<65){
 			f = Animation.FRAME_TEST_TILES[frame-1];
 		}else{
 			f = frame;
 		}
 		return f;
 	}
 	
 	/*private Vector2[] getTileVertices(int frame){
 		//TODO finish this. Plug in vertices for all different types of frames.
 		Vector2[] verts;
 		verts = new Vector2[]{new Vector2(0f,0f),new Vector2(0.5f,0f),new Vector2(0.5f,0.5f),new Vector2(0f,0.5f)};
 		return verts;
 	}*/
 	
 }
