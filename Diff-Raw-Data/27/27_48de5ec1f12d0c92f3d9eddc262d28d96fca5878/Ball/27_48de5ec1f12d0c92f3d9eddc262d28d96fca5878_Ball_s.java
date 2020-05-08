 package com.pkelly.gameobjects;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * Created with IntelliJ IDEA.
  * User: PeKay
  * Date: 23/06/13
  * Time: 00:00
  * To change this template use File | Settings | File Templates.
  */
 public class Ball extends Sprite
 {
     private final Vector2 g = new Vector2(0, -.1f);
     private final float TERMINAL_VELOCITY = 16;
     private final float mu = .999f;
 
     public Vector2 velocity = new Vector2(0, 0);
 
     boolean cooldown = false;
     float cooldownPeriod = .1f;
     float timeRemaning = 0;
 
    private static Texture ballTexture = new Texture("assets/img/ball.png");
 
     public Ball()
     {
         super(ballTexture);
 
        ballTexture = new Texture("assets/img/ball.png");
 
         setPosition(Gdx.graphics.getWidth() / 2, 120);
     }
 
     @Override
     public void draw(SpriteBatch spriteBatch, float v)
     {
         if (cooldown)
         {
             if (timeRemaning > 0)
             {
                 timeRemaning = timeRemaning - v;
             } else
             {
                 cooldown = false;
             }
         }
         addGravity();
 
         updatePosition();
 
         addResistance();
 
         super.draw(spriteBatch);
     }
 
     private void addGravity()
     {
         velocity.add(g);
     }
 
     private void addResistance()
     {
         velocity.scl(mu);
 
         velocity.limit(TERMINAL_VELOCITY);
     }
 
     private void updatePosition()
     {
         setX(getX() + velocity.x);
         setY(getY() + velocity.y);
 
         if (getX() < 0)
         {
             setX(0);
             bounceX();
         } else if (getX() > Gdx.graphics.getWidth())
         {
             setX(Gdx.graphics.getWidth());
             bounceX();
         }
 
         if (getY() < 0)
         {
             setY(0);
             bounceY();
         } else if (getY() > Gdx.graphics.getHeight())
         {
             setY(Gdx.graphics.getHeight());
             bounceY();
         }
     }
 
     public void paddleBounce(Vector2 paddleVelocity)
     {
         if (cooldown) return;
 
         bounceY();
         velocity.add(paddleVelocity.x / 2, 0);
         velocity.scl(1, 1.3f);
 
         cooldown = true;
         timeRemaning = cooldownPeriod;
     }
 
     public void brickBounce()
     {
         bounceX();
         bounceY();
     }
 
     private void bounceX()
     {
         velocity.x = -velocity.x;
     }
 
     private void bounceY()
     {
         velocity.y = -velocity.y;
     }
 }
