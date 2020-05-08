 package com.aquasheep.GTFTSTG.Items;
 
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
 import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
 
 import com.aquasheep.GTFTSTG.GTFTSTG;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Action;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 /** Items are anything on the screen that can be clicked to produce a certain effect. */
 public abstract class Item extends Actor {
 
 	protected final GTFTSTG game;
 	protected final String name;
 	protected Rectangle pos;
 	protected Action event;
 	protected float sleepChance;
 	protected static final TextureAtlas ATLAS = new TextureAtlas(Gdx.files.internal("image-atlases/items.pack"));
 	protected TextureRegion texture;
 	protected Sound sound;
 	protected float soundLength;
 	
 	/**
 	 * Creates a new Item.
 	 * @param game - the game the item is being used in
 	 * @param name - the name of the item (used to find its texture in the "items.pack" image atlas)
 	 * @param pos  - position to initially draw the item
 	 * @param event - scene2d.Action to fulfill when item is clicked 
 	 */
 	public Item(GTFTSTG theGame, String theName, float x, float y, float soundLength) {
 		this.game = theGame;
 		this.name = theName;
 		texture = ATLAS.findRegion(name);
 		this.setSize(texture.getRegionWidth(), texture.getRegionHeight());
 		this.pos = new Rectangle(x,y,texture.getRegionWidth(),texture.getRegionHeight());
 		this.setPosition(pos.x, pos.y);
 		sound = Gdx.audio.newSound(Gdx.files.internal("sounds/"+name+".ogg"));
 		this.soundLength = soundLength;
		sleepChance = (float)Math.random()/10+.01f;
 		//TODO make collision rectangle (pos) based on image size	
 	}
 	
 	/** Activates particular event associated with the Item. Called when an item is clicked*/
 	public void activate() {	
 		event = sequence(new Action() {
 			@Override
 			public boolean act(float delta) {
 				game.getBaby().cry.stop();
 				game.getBaby().sleep.stop();
 				sound.play();
 				return true;
 			}
 		}, delay(this.soundLength, new Action() {
 
 			@Override
 			public boolean act(float delta) {
 				if (game.DEBUG) {
 					System.out.println(name+" has been activated.");
 					System.out.println("Baby sleeping: "+game.getBaby().attemptSleep((int)(sleepChance*1000)));
 				} else
 					game.getBaby().attemptSleep((int)(sleepChance*1000));
 				return true;
 			}
 			
 		}));
 		this.addAction(event);	
 	}
 
 	public boolean selected(float mouseX, float mouseY) {
 		if (pos.contains(mouseX,game.getHeight()-mouseY)) {
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float alpha) {
 		batch.draw(texture,pos.x,pos.y);
 	}
 
 	public Rectangle getPos() {
 		return pos;
 	}
 	
 	public void setPos(float x, float y) {
 		pos.x = x;
 		pos.y = y;
 		this.setPosition(x,y);
 	}
 }
