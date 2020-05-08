 package com.game.rania.model.element;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.game.rania.RaniaGame;
 import com.game.rania.model.Font;
 
 public class Player extends User
 {
 
   public Target target = new Target(0, Target.none, null);
 
   public Player(int id, float posX, float posY, String ShipName, String PilotName, int domain)
   {
     super(id, posX, posY, ShipName, PilotName, domain);
     zIndex = Indexes.player;
     isPlayer = true;
   }
 
   public Player(int id, float posX, float posY, String ShipName, String PilotName, int domain, int inPlanet)
   {
     super(id, posX, posY, ShipName, PilotName, domain, inPlanet);
     zIndex = Indexes.player;
     isPlayer = true;
   }
   
   public void clearTarget()
   {
     target.id     = 0;
     target.type   = Target.none;
     target.object = null;
   }
 
   @Override
   public boolean update(float deltaTime)
   {
     if (!super.update(deltaTime))
       return false;
     target.update(deltaTime);
     return true;
   }
   
   protected Text deadText = new Text(" .  ...", Font.getFont("data/fonts/Arial.ttf", 50), new Color(1, 0, 0, 1), 0, 0);
 
   @Override
   public boolean draw(SpriteBatch sprite, ShapeRenderer shape)
   {
     if (planet > 0)
     {
       angle.value = 0.0f;
       position.set(target.object.position);
       return true;
     }
     
     if (body.wear <= 0)
     {
       deadText.draw(sprite, position.x, position.y);
       return true;
     }
     
     target.draw(sprite, shape);
     
     sprite.end();
     shape.begin(ShapeType.Filled);
     float dx = RaniaGame.mView.getCamera().getWidth() * 0.25f;
     float dy = RaniaGame.mView.getCamera().getHeight() * 0.5f;
     if (body != null)
     {
       shape.setColor(new Color(1, 0, 0, 0.75f));
       shape.rect(position.x - dx, position.y + dy - 20, dx * ((float) Math.max(0, body.wear) / body.item.durability), 15);
     }
     if (shield != null)
     {
       shape.setColor(new Color(0, 0, 1, 0.75f));
       shape.rect(position.x - dx, position.y + dy - 35, dx * ((float) Math.max(0, shield.wear) / shield.item.durability), 15);
     }
     if (battery != null)
     {
       shape.setColor(new Color(0, 1, 0, 0.75f));
       shape.rect(position.x - dx, position.y + dy - 50, dx * ((float) Math.max(0, energy) / maxEnergy), 15);
     }
 
     if (target.type == Target.user)
     {
       User user = target.getObject(User.class);
      if (user.body.wear <= 0)
        clearTarget();
       if (user.body != null)
       {
         shape.setColor(new Color(1, 0, 0, 0.75f));
         shape.rect(position.x + 15, position.y + dy - 20, dx * ((float) Math.max(0, user.body.wear) / user.body.item.durability), 15);
       }
       if (shield != null)
       {
         shape.setColor(new Color(0, 0, 1, 0.75f));
         shape.rect(position.x + 15, position.y + dy - 35, dx * ((float) Math.max(0, user.shield.wear) / user.shield.item.durability), 15);
       }
     }
     shape.end();
     sprite.begin();
     
     return super.draw(sprite, shape);
   }
 
   public float getTargetDistance()
   {
     return (float) Math.sqrt(Math.pow(position.x - target.object.position.x, 2) + Math.pow(position.y - target.object.position.y, 2));
   }
 }
