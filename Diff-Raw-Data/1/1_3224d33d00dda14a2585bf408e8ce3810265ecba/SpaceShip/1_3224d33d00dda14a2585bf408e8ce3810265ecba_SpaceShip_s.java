 package com.game.rania.model;
 
 import java.util.HashMap;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.controller.Controllers;
 import com.game.rania.controller.command.AddObjectCommand;
 import com.game.rania.model.animator.AnimatorColor;
 import com.game.rania.model.animator.AnimatorVector2;
 import com.game.rania.model.element.Font;
 import com.game.rania.model.element.FrameSequence;
 import com.game.rania.model.element.Object;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.model.items.Consumable;
 import com.game.rania.model.items.RepairKit;
 import com.game.rania.model.items.Engine;
 import com.game.rania.model.items.Equip;
 import com.game.rania.model.items.Fuelbag;
 import com.game.rania.model.items.Item;
 import com.game.rania.model.items.Radar;
 import com.game.rania.model.items.Hyper;
 import com.game.rania.model.items.Shield;
 import com.game.rania.model.items.Body;
 import com.game.rania.model.items.Weapon;
 
 public class SpaceShip extends Object
 {
   public String shipName;
 
   public SpaceShip(float posX, float posY, String ShipName)
   {
     super(RegionID.SHIP, posX, posY);
     shipName = ShipName;
   }
 
   public SpaceShip(float posX, float posY, float rotAngle, String ShipName)
   {
     super(RegionID.SHIP, posX, posY, rotAngle);
     shipName = ShipName;
   }
 
   public SpaceShip(float posX, float posY, float rotAngle, float scaleX, float scaleY, String ShipName)
   {
     super(RegionID.SHIP, posX, posY, rotAngle, scaleX, scaleY);
     shipName = ShipName;
   }
 
   private Vector2 targetPosition = new Vector2(0, 0);
   private Vector2 moveVec        = new Vector2(0, 0);
   private Vector2 addVec         = new Vector2(0, 0);
   private boolean move           = false;
 
   public void setPositionTarget(Vector2 target, float time)
   {
     setPositionTarget(target.x, target.y, time);
   }
 
   public void stop()
   {
     move = false;
     moveVec.set(0, 0);
     targetPosition.set(position);
   }
 
   public void setPositionTarget(float x, float y, float time)
   {
     targetPosition.set(x, y);
 
     moveVec.set(targetPosition);
     moveVec.sub(position);
     moveVec.div(time);
 
     move = true;
   }
 
   @Override
   public boolean update(float deltaTime)
   {
     if (!super.update(deltaTime))
       return false;
     if (!move || fuel == null)
       return true;
 
     // unFuel((int)((deltaTime*1000)/engine.item.economic));
 
     if (fuel.num <= 0)
     {
       stop();
       return true;
     }
 
     addVec.set(moveVec);
     addVec.scl(deltaTime);
 
     if (!targetPosition.epsilonEquals(position, addVec.len()))
       position.add(addVec);
     else
       stop();
     angle.value = (float) Math.toDegrees(Math.atan2(-addVec.x, addVec.y));
     return true;
   }
 
   @Override
   public boolean draw(SpriteBatch sprite, ShapeRenderer shape)
   {
     if (!super.draw(sprite, shape) || region == null)
       return false;
     sprite.end();
     shape.begin(ShapeType.Filled);
     float maxSize = Math.max(region.getRegionWidth(), region.getRegionHeight());
     if (body != null)
     {
       shape.setColor(new Color(1, 0, 0, 0.75f));
       shape.rect(position.x - maxSize * 0.5f, position.y + maxSize * 0.55f + 5, maxSize * ((float) body.wear / body.item.durability), 5);
     }
     if (shield != null)
     {
       shape.setColor(new Color(0, 0, 1, 0.75f));
       shape.rect(position.x - maxSize * 0.5f, position.y + maxSize * 0.55f, maxSize * ((float) shield.wear / shield.item.durability), 5);
     }
     if (fuelbag != null)
     {
       shape.setColor(new Color(0, 1, 0, 0.75f));
       shape.rect(position.x - maxSize * 0.5f, position.y - maxSize * 0.55f, maxSize * ((float) fuel.num / maxFuel), 5);
     }
     shape.end();
     sprite.begin();
     return true;
   }
 
   // equips
   public Equip<Engine>                      engine    = null;
   public Equip<Fuelbag>                     fuelbag   = null;
   public Equip<Radar>                       radar     = null;
   public Equip<Hyper>                       hyper     = null;
   public Equip<Shield>                      shield    = null;
   public Equip<Body>                        body      = null;
   public HashMap<Integer, Equip<Weapon>>    weapon    = new HashMap<Integer, Equip<Weapon>>();
   public HashMap<Integer, Equip<RepairKit>> repairKit = new HashMap<Integer, Equip<RepairKit>>();
 
   public HashMap<Integer, Equip<Item>>      inventory = new HashMap<Integer, Equip<Item>>();
 
   // characteristics
   public Equip<Consumable>                  fuel      = null;
   public int                                maxFuel;
   public float                              maxSpeed;
 
   public void setEquips(List<Equip<Item>> equips)
   {
     for (Equip<Item> equip : equips)
     {
       if (!equip.in_use)
       {
         if (equip.item.getClass() == Consumable.class)
         {
           if (equip.item.id == Consumable.Type.fuel)
           {
             this.fuel = new Equip<Consumable>(equip, Consumable.class);
             continue;
           }
         }
         else
         {
           inventory.put(equip.id, equip);
         }
       }
       else
       {
         if (equip.item.getClass() == Body.class)
         {
           this.body = new Equip<Body>(equip, Body.class);
           continue;
         }
 
         if (equip.item.getClass() == Engine.class)
         {
           this.engine = new Equip<Engine>(equip, Engine.class);
           continue;
         }
 
         if (equip.item.getClass() == Fuelbag.class)
         {
           this.fuelbag = new Equip<Fuelbag>(equip, Fuelbag.class);
           continue;
         }
 
         if (equip.item.getClass() == Hyper.class)
         {
           this.hyper = new Equip<Hyper>(equip, Hyper.class);
           continue;
         }
 
         if (equip.item.getClass() == Shield.class)
         {
           this.shield = new Equip<Shield>(equip, Shield.class);
           continue;
         }
 
         if (equip.item.getClass() == Radar.class)
         {
           this.radar = new Equip<Radar>(equip, Radar.class);
           continue;
         }
 
         if (equip.item.getClass() == Weapon.class)
         {
           this.weapon.put(equip.id, new Equip<Weapon>(equip, Weapon.class));
           continue;
         }
 
         if (equip.item.getClass() == RepairKit.class)
         {
           this.repairKit.put(equip.id, new Equip<RepairKit>(equip, RepairKit.class));
           continue;
         }
       }
     }
 
     maxFuel = 0;
     maxSpeed = 0;
 
     if (fuelbag != null)
     {
       maxFuel = (int) fuelbag.item.volume * fuelbag.item.compress;
       fuel.num = fuel.num > maxFuel ? maxFuel : fuel.num;
     }
 
     if (engine != null)
     {
       maxSpeed = (float) engine.item.power;
     }
   }
 
   public void damage(Equip<?> equip, int value)
   {
     equip.wear = Math.max(0, equip.wear - value);
 
     if (equip == body && equip.wear <= 0)
     {
       crashSpaceShip(10);
       FrameSequence boom = new FrameSequence("data/location/boom.png", 10, 1.0f);
       boom.scale.set(2, 2);
       boom.position = position;
 
       Controllers.commandController.addCommand(new AddObjectCommand(boom));
     }
     String text = String.valueOf(value);
     if (value == 0)
       text = "miss";
     Text infoText = new Text(text, Font.getFont("data/fonts/Arial.ttf", 35), new Color(1.0f, 0.2f, 0.1f, 1.0f), position.x, position.y);
     infoText.addAnimator(new AnimatorVector2(infoText.position, infoText.position.x, infoText.position.y + 70, 0, 1.0f));
     infoText.addAnimator(new AnimatorColor(infoText.color, 0, 0, 1.0f));
     infoText.lifeTime = 1.0f;
     infoText.zIndex = Indexes.infoText;
     infoText.setAlign(Align.LEFT, Align.BOTTOM);
     Controllers.commandController.addCommand(new AddObjectCommand(infoText));
   }
 
   public void repair(Equip<?> equip, int value)
   {
     equip.wear = Math.min(body.item.durability, equip.wear + value);
    equip.wear = equip.wear + value;
 
     String text = String.valueOf(value);
     if (value == 0)
       text = "miss";
     Text infoText = new Text(text, Font.getFont("data/fonts/Arial.ttf", 35), new Color(0.2f, 1.0f, 0.1f, 1.0f), position.x, position.y);
     infoText.addAnimator(new AnimatorVector2(infoText.position, infoText.position.x, infoText.position.y + 70, 0, 1.0f));
     infoText.addAnimator(new AnimatorColor(infoText.color, 0, 0, 1.0f));
     infoText.lifeTime = 1.0f;
     infoText.zIndex = Indexes.infoText;
     infoText.setAlign(Align.RIGHT, Align.BOTTOM);
     Controllers.commandController.addCommand(new AddObjectCommand(infoText));
   }
 
   public void unFuel(int f)
   {
     if (fuel == null)
       return;
 
     fuel.num -= f;
     if (fuel.num < 0)
     {
       fuel.num = 0;
       maxSpeed = 0;
     }
 
     if (fuel.num > maxFuel)
     {
       fuel.num = maxFuel;
     }
   }
 
   public void reFuel(int f)
   {
     if (fuel == null)
       return;
 
     fuel.num += f;
     if (fuel.num > maxFuel)
     {
       fuel.num = maxFuel;
     }
     maxSpeed = (float) engine.item.power / 10;
   }
 
   public void crashSpaceShip(int percent)
   {
     if (this.engine != null)
     {
       this.engine.wear = (int) (this.engine.wear - ((double) this.engine.item.durability * percent / 100));
       if (this.engine.wear < 0)
         this.engine.wear = 0;
     }
 
     if (this.fuelbag != null)
     {
       this.fuelbag.wear = (int) (this.fuelbag.wear - ((double) this.fuelbag.item.durability * percent / 100));
       if (this.fuelbag.wear < 0)
         this.fuelbag.wear = 0;
     }
 
     if (this.hyper != null)
     {
       this.hyper.wear = (int) (this.hyper.wear - ((double) this.hyper.item.durability * percent / 100));
       if (this.hyper.wear < 0)
         this.hyper.wear = 0;
     }
 
     if (this.radar != null)
     {
       this.radar.wear = (int) (this.radar.wear - ((double) this.radar.item.durability * percent / 100));
       if (this.radar.wear < 0)
         this.radar.wear = 0;
     }
 
     if (this.shield != null)
     {
       this.shield.wear = (int) (this.shield.wear - ((double) this.shield.item.durability * percent / 100));
       if (this.shield.wear < 0)
         this.shield.wear = 0;
     }
 
     for (Equip<RepairKit> dr : this.repairKit.values())
     {
       dr.wear = dr.wear - (int) ((double) dr.item.durability * percent / 100);
       if (dr.wear < 0)
         dr.wear = 0;
     }
 
     for (Equip<Weapon> weap : this.weapon.values())
     {
       weap.wear = weap.wear - (int) ((double) weap.item.durability * percent / 100);
       if (weap.wear < 0)
         weap.wear = 0;
     }
 
     lifeTime = 0.0f;
   }
 }
