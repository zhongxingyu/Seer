 package com.soupcan.the_love_of_rice.core.actor.people;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.soupcan.the_love_of_rice.core.TheLoveOfRice;
 import com.soupcan.the_love_of_rice.core.image.SpriteAnimation;
 
 /**
 * Samurai kill ninjas.
  *
  * @author Zachary Latta
  */
 public class Samurai extends Actor
 {
     private SpriteAnimation currentAnimation = null;
     private SpriteAnimation idleAnimation = null;
     private SpriteAnimation blockAnimation = null;
 
     private float stateTime = 0;
 
     public Samurai()
     {
         idleAnimation = new SpriteAnimation(1f, "samurai_", 1, 2);
         blockAnimation = new SpriteAnimation(.5f, "samurai_block_", 1, 2);
 
         idleAnimation.setPlayMode(SpriteAnimation.LOOP);
         blockAnimation.setPlayMode(SpriteAnimation.LOOP);
 
         currentAnimation = idleAnimation;
     }
 
     public void draw(SpriteBatch batch, float parentAlpha)
     {
         stateTime += Gdx.graphics.getDeltaTime();
 
         Sprite toRender = currentAnimation.getKeyFrame(stateTime);
 
         // -1 to account for the sword below his feet
         toRender.setPosition(getX(), getY() - 1);
         toRender.setOrigin(toRender.getX() + toRender.getWidth(), toRender.getY() + toRender.getHeight());
         toRender.setRotation(getRotation());
 
         setSize(toRender.getWidth(), toRender.getHeight());
         setOrigin(toRender.getOriginX(), toRender.getOriginY());
 
         toRender.draw(batch);
 
         if(TheLoveOfRice.DRAW_DEBUG)
         {
             drawDebug(batch);
         }
     }
 
     public void drawDebug(SpriteBatch batch)
     {
         batch.end();
 
         ShapeRenderer renderer = new ShapeRenderer();
 
         renderer.setProjectionMatrix(batch.getProjectionMatrix());
         renderer.setTransformMatrix(batch.getTransformMatrix());
         renderer.translate(getX(), getY(), 0);
 
         renderer.begin(ShapeRenderer.ShapeType.Line);
         renderer.setColor(new Color(Color.RED));
         renderer.rect(0, 0, getWidth(), getHeight());
         renderer.end();
 
         batch.begin();
     }
 }
