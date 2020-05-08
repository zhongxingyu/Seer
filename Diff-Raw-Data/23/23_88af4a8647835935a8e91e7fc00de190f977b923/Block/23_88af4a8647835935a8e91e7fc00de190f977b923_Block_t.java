 package com.gamegear.firstwing.actors;
 
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Filter;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.gamegear.firstwing.TextureMgr;
 import com.gamegear.firstwing.actors.Actor;
 import com.gamegear.firstwing.actors.json.StaticActor;
 import com.gamegear.firstwing.levels.json.LevelProperties.ColorEnum;
 import com.gamegear.firstwing.levels.json.Tile;
 
 public class Block extends Actor {
 
 	public float SIZE = 1f;
 	private TextureRegion blockTexture;
 	private Animation deathAnimation;
 	private Tile tile;
 	private float health;
 	private ColorEnum blockColor;
 	
 	public Block(Vector2 position, World world, StaticActor actor, Tile tile, Filter filter, ColorEnum color) {
 		super(actor, BodyType.StaticBody, position, world, filter);
 		this.tile = tile;
		if(tile.Health == 0){
			tile.Health = 5;
		}
		
 		this.health = tile.Health;
 		this.blockColor = color;
 		this.loadTextures();
 		this.setShape(this.createShape(actor, true), 0f);
 	}
 	
 	public float getHealth(){
 		return this.health;
 	}
 	
 	public void setHealth(float health, ColorEnum color){
 		if(this.tile.AssignedColor == color){
 			this.health = health;
 		}
 	}
 	
 	public boolean isBreakable(){
 		return ((StaticActor)this.getProperties()).Breakable;
 	}
 
 	@Override
 	protected void loadTextures() {
 		switch(((StaticActor)this.getProperties()).Type){
 			case Breakable:
 				this.blockTexture = TextureMgr.getTexture(tile.Name + "-" + tile.AssignedColor.toString(), true);
 				TextureRegion[] deathFrames = new TextureRegion[3];
 				for(int i = 0; i < 3; i++){
 					deathFrames[i] = TextureMgr.getTexture(tile.Name + "-" + tile.AssignedColor.toString() + "-death" + i, true);
 				}
 				
				this.deathAnimation = new Animation(0.33f, deathFrames);
 				break;
 			case Tile:
 				this.blockTexture = TextureMgr.getTexture(tile.Name + "-" + this.blockColor.toString(), true);
 				break;
 			case Collectable:
 			case Finish:
 				this.blockTexture = TextureMgr.getTexture(tile.Name, true);
 				break;
 			default:
 				break;
 		}
 	}
 
 	@Override
 	protected void draw() {
 		if(this.getState() == ActorState.DYING){
 			switch(((StaticActor)this.getProperties()).Type){
 			case Breakable:
 				this.setTexture(this.deathAnimation.getKeyFrame(this.stateTime, false));
 				if(this.deathAnimation.isAnimationFinished(this.stateTime)){
 					this.setState(ActorState.DEAD, false);
 				}
 				
 				break;
 			case Collectable:
 			case Finish:
 			case Tile:
 			default:
 				this.setState(ActorState.DEAD, false);
 				break;
 			}
 		} else {
 			this.setTexture(blockTexture);
 		}
 	}
 }
