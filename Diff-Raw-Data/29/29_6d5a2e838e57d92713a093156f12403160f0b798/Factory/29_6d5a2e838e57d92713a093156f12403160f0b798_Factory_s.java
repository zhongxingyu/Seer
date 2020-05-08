 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.factories;
 
 import game.CacheTool;
 import game.actions.RemoveEntity;
 import game.components.ComponentType;
 import game.components.graphics.RSheet;
 import game.components.holdables.Hand;
 import game.components.holdables.weapons.Weapon;
 import game.components.interfaces.ICompAnim;
 import game.components.misc.ActionOnDeath;
 import game.components.misc.Inventory;
 import game.components.misc.Life;
 import game.components.misc.MovementConstraint;
 import game.components.misc.SimpleControl;
 import game.components.misc.SpawnOnDeath;
 import game.components.physics.Movement;
 import game.entities.Entity;
 import game.entities.IEntity;
 import game.entities.InvisibleRectangle;
 import game.entities.Players;
 import game.entities.groups.EntityType;
 import game.events.impl.DamagePlayerEvent;
 import game.events.impl.KillEvent;
 import game.world.World;
 
 import java.io.IOException;
 
 import loader.data.DataException;
 import loader.data.json.CreepsData.CreepData;
 import loader.data.json.PlayersData.PlayerData;
 import loader.parser.ParserException;
 import main.Locator;
 import math.Rectangle;
 
 import org.newdawn.slick.Color;
 
 import ui.hud.infobar.Bar;
 import ui.hud.infobar.InfoBar;
 
 public class Factory {
   public static World makeWorld(Rectangle rectWorld, Players players) {
     /**
      * The layout of rectangles:
      * |---------------------------------|
      * | rectBig                         |
      * |                                 |
      * |                                 |
      * |-----------|-----------|         |
      * | rectCreep | rectWorld |         |
      * |-----------|-----------|         |
      * |                                 |
      * |                                 |
      * |                                 |
      * |---------------------------------|
      */
 
     World w = new World();
 
     InvisibleRectangle rectBig =
       new InvisibleRectangle(
         -rectWorld.getWidth(), -rectWorld.getHeight(),
         3 * rectWorld.getWidth(), 3 * rectWorld.getHeight());
     rectBig.onNotContainsEvent.add(new KillEvent());
     w.add(rectBig);
 
     InvisibleRectangle rectCreepKiller =
       new InvisibleRectangle(-rectWorld.getWidth(), 0,
                              rectWorld.getWidth(), rectWorld.getHeight());
     rectCreepKiller.onContainsEvent.add(new KillEvent());
     rectCreepKiller.onContainsEvent.add(new DamagePlayerEvent());
     w.add(rectCreepKiller);
 
     for (IEntity p : players) {
       p.addLogicComponent(new MovementConstraint(rectWorld));
       w.add(p);
     }
 
     return w;
   }
 
   public static IEntity makeCreep(float x, float y, float ang, CreepData data)
       throws ParserException, DataException, IOException {
     RSheet walk  = CacheTool.getRSheet(Locator.getCache(), data.getSheet("walk"));
     RSheet death = CacheTool.getRSheet(Locator.getCache(), data.getSheet("death"));
 
     return Factory.makeUnit(x, y, data.hitbox.width, data.hitbox.height,
                             (float) Math.cos(ang) * data.speed,
                             (float) Math.sin(ang) * data.speed,
                             EntityType.CREEP, data.hitpoints,
                             walk, death);
   }
 
   private static Color TRANSPARENT = new Color(0, 0, 0, 0);
 
   public static IEntity makePlayer(float x, float y, PlayerData data)
       throws ParserException, DataException, IOException {
     RSheet walk  = CacheTool.getRSheet(Locator.getCache(), data.getSheet("walk"));
     RSheet death = CacheTool.getRSheet(Locator.getCache(), data.getSheet("death"));
 
     Inventory inv         = new Inventory(data.startMoney);
     Hand hand             = new Hand(data.handOffset.x, data.handOffset.y);
     SimpleControl control = new SimpleControl(data.speed);
     Weapon weapon         = CacheTool.getWeapon(Locator.getCache(), data.startWeapon);
 
     IEntity e = Factory.makeUnit(x, y, data.hitbox.width, data.hitbox.height,
                                       0, 0, EntityType.PLAYER, data.hitpoints,
                                       walk, death);
 
     Bar weaponBar   = new Bar(hand, Color.blue, TRANSPARENT);
     InfoBar infoBar = (InfoBar) e.getComponent(ComponentType.INFO_BAR);
     infoBar.add(weaponBar);
 
     inv.addWeapon(weapon);
     hand.grab(weapon);
 
     e.addLogicComponent(inv);
     e.addRenderComponent(hand);
     e.addLogicComponent(control);
 
     return e;
   }
 
   private static IEntity makeUnit(float x, float y, float width, float height,
                                   float dx, float dy, EntityType type,
                                   float maxHP, ICompAnim walkAnim,
                                   ICompAnim deathAnim) {
     Entity e             = new Entity(x, y, width, height, type);
     Movement movement    = new Movement(dx, dy);
     Life life            = new Life(maxHP);
     SpawnOnDeath death   = new SpawnOnDeath(deathAnim);
     InfoBar infoBar      = new InfoBar(width, 2, 0, -6); // FIXME: Magic Numbers
     Bar hpBar            = new Bar(life, Color.green, Color.red);
     ActionOnDeath remove = new ActionOnDeath(new RemoveEntity(e));
 
     infoBar.add(hpBar);
 
     e.addLogicComponent(movement);
     e.addLogicComponent(life);
     e.addLogicComponent(death);
     e.addLogicComponent(remove);
     e.addRenderComponent(walkAnim);
     e.addRenderComponent(infoBar);
 
     return e;
   }
 }
