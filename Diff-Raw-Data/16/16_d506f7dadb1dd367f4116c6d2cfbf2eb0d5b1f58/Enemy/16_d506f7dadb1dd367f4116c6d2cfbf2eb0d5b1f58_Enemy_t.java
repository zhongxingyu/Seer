 package com.me.mygdxgame;
 
 import java.util.HashMap;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.actions.Actions;
 import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
 
 public class Enemy extends ExtendedActor{
 	
 	HashMap<Integer, Sprite> sprites;
 	EnemyStats enemyStats;
 	Rectangle healthBarYellowRectangle, healthBarRedRectangle;
 	boolean slowed, dotted;
 	HashMap<String, Float> floatingStats = new HashMap<String, Float>();
 	
     public Enemy(EnemyStats enemyStats, Vector2 startPosition, List<Direction> directions, Sprite enemySprite, Sprite redHealthBarSprite, Sprite yellowHealthBarSprite)
     {
     	
     	super(enemySprite);
     	this.enemyStats = enemyStats;
     	floatingStats.put("currentHealth", (float)enemyStats.health);
     	floatingStats.put("dotTicksLeft", 0f);
     	floatingStats.put("distanceTravelled", 0f);
     	floatingStats.put("currentMoveSpeedMultiplier", 1.0f);
     	floatingStats.put("currentSlowDuration", 0f);
     	floatingStats.put("currentDotDamage", 0f);
     	floatingStats.put("dotDurationBetweenTicks", 0f);
     	floatingStats.put("currentDotDurationBetweenTicks", 0f);
     	
     	setSize(enemySprite.getWidth(), enemySprite.getHeight());
     	
         Vector2 targetPosition = new Vector2(startPosition.x * GameConstants.tileSize, startPosition.y * GameConstants.tileSize);
         this.sprites = new HashMap<Integer, Sprite>();
         sprites.put(0, enemySprite);
         sprites.put(1, redHealthBarSprite);
         sprites.put(2, yellowHealthBarSprite);
         this.setPosition(targetPosition.x, targetPosition.y);
 
         SequenceAction seqAct = new SequenceAction();
         for(int i = 0; i<directions.size(); i++)
         {
         	switch (directions.get(i))
             {
                 case Up:
                     targetPosition.y -= GameConstants.tileSize;
                     break;
                 case Down:
                     targetPosition.y += GameConstants.tileSize;
                     break;
                 case Left:
                     targetPosition.x -= GameConstants.tileSize;
                     break;
                 case Right:
                     targetPosition.x += GameConstants.tileSize;
                     break;
                 default:
                 	break;
             }
         	seqAct.addAction(Actions.moveTo(targetPosition.x, targetPosition.y, 1.0f/enemyStats.speed));
         	//this.queueAction(Actions.moveTo(targetPosition.x, targetPosition.y, 0.4f));
         }
         addAction(seqAct);
     }
     
     // Basically update(gameTime)
     @Override
     public void act(float gameTime)
     {
     	super.act(gameTime * floatingStats.get("currentMoveSpeedMultiplier"));
     	
		if (floatingStats.get("currentMoveSpeedMultiplier") != 1.0f) {
 			editStat("currentSlowDuration", -gameTime);
 			if (getStat("currentSlowDuration") <= 0)
 			{
 				setStat("currentMoveSpeedMultiplier", 1.0f);
 			}
 		}
		if (getStat("dotTicksLeft") > 0) {
 			editStat("currentDotDurationBetweenTicks", -gameTime);
 			if (getStat("currentDotDurationBetweenTicks") <= 0) {
 				editStat("currentHealth", -getStat("currentDotDamage"));
 				setStat("currentDotDurationBetweenTicks", getStat("dotDurationBetweenTicks"));
 				editStat("dotTicksLeft", -1f);
 			}
 			if (getStat("dotTicksLeft") <= 0) {
 				dotted = false;
 			}
 		}
     	healthBarRedRectangle = new Rectangle((int)getX(), GameConstants.screenHeight - GameConstants.tileSize - (int)getY() - 10, 64, 5);
         healthBarYellowRectangle = new Rectangle((int)getX(), GameConstants.screenHeight - GameConstants.tileSize - (int)getY() - 10, (int)((float)64 * (float)getStat("currentHealth") / (float)enemyStats.getHealth()), 5);
    
     }
     
     @Override
 	public void draw (SpriteBatch batch, float parentAlpha) {
     	super.draw(batch, parentAlpha);
     	batch.draw(sprites.get(1), healthBarRedRectangle.getX(), GameConstants.morphY(healthBarRedRectangle.getY() - GameConstants.tileSize), healthBarRedRectangle.getWidth(), healthBarRedRectangle.getHeight());
         batch.draw(sprites.get(2), healthBarYellowRectangle.getX(), GameConstants.morphY(healthBarYellowRectangle.getY() - GameConstants.tileSize), healthBarYellowRectangle.getWidth(), healthBarYellowRectangle.getHeight());
         
 	}
     
 	public HashMap<Integer, Sprite> getSprites() {
 		return sprites;
 	}
 	public void setSprites(HashMap<Integer, Sprite> sprites) {
 		this.sprites = sprites;
 	}
 	
 	public void editStat(String key, float value)
 	{
 		floatingStats.put(key, (float)(floatingStats.get(key) + value));
 	}
 	
 	public void setStat(String key, float value)
 	{
 		if(floatingStats.containsKey(key))
 			floatingStats.remove(key);
 		floatingStats.put(key, value);
 	}
 	
 	public float getStat(String key)
 	{
 		return floatingStats.get(key);
 	}
 }
