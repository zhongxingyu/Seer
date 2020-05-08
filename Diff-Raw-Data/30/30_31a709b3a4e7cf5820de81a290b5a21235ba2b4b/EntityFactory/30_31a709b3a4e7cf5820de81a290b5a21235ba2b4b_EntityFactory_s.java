 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package game.factories;
 
 import game.CacheTool;
 import game.components.ai.BossAI;
 import game.components.graphics.AnimatedSheet;
 import game.components.graphics.debug.DebugBoss;
 import game.components.graphics.debug.Outliner;
 import game.components.holdables.Hand;
 import game.components.holdables.weapons.Weapon;
 import game.components.misc.EffectsOnDeath;
 import game.components.misc.Inventory;
 import game.components.misc.Life;
 import game.components.misc.PlayerControl;
 import game.components.physics.Movement;
 import game.components.physics.MovementConstraint;
 import game.entities.Entity;
 import game.misc.Shop;
 import game.triggers.effects.RemoveEntityEffect;
 import game.triggers.effects.spawn.SpawnAnimationEffect;
 import game.types.Orientation;
 import game.types.Player;
 import game.types.Unit;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import loader.data.json.PlayersData;
 import loader.data.json.ShopData;
 import loader.data.json.types.BossData;
 import loader.data.json.types.CreepData;
 import loader.data.json.types.PlayerData;
 import loader.data.json.types.UnitData;
 import loader.parser.ParserException;
 import main.Locator;
 import math.Rectangle;
 import math.Vector2;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 
 import ui.hud.infobar.Bar;
 import ui.hud.infobar.InfoBar;
 
 public class EntityFactory {
   private static final Orientation PLAYER_ORIENTATION = Orientation.LEFT;
   private static final Orientation BOSS_ORIENTATION = Orientation.LEFT;
   private static final Orientation CREEP_ORIENTATION = Orientation.LEFT;
 
   private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
   private static final int BAR_HEIGHT    = 2;
   private static final int BAR_OFFSET_X  = 0;
   private static final int BAR_OFFSET_Y  = -6;
 
   private final Rectangle worldRect;
   private final Graphics statics;
 
   private final WeaponFactory weaponFactory;
   private final ShopData shopData;
   private final PlayersData playersData;
 
   public EntityFactory(Rectangle worldRect, Graphics statics)
       throws ParserException, IOException {
     this.worldRect = worldRect;
     this.statics = statics;
 
     Rectangle bigRect = new Rectangle(
       -worldRect.getWidth(), -worldRect.getHeight(),
       3 * worldRect.getWidth(), 3 * worldRect.getHeight());
 
     weaponFactory = new WeaponFactory(bigRect);
     shopData      = CacheTool.getShop(Locator.getCache());
     playersData   = CacheTool.getPlayers(Locator.getCache());
   }
 
   private Unit makeUnit(float x, float y, float dx, float dy,
       Orientation orientation, UnitData data)
       throws ParserException, IOException {
     AnimatedSheet walk = CacheTool.getAnimatedSheet(
         Locator.getCache(), orientation, 0, data.sprites.get("walk"));
     AnimatedSheet death = CacheTool.getAnimatedSheet(
         Locator.getCache(), orientation, 0, data.sprites.get("death"));
 
     Entity entity = new Entity(x, y, data.hitbox.width, data.hitbox.height);
     Movement mov  = new Movement(entity, dx, dy);
     Life life     = new Life(entity, data.hitpoints);
 
     entity.addLogicComponent(mov);
     entity.addLogicComponent(life);
     entity.addRenderComponent(walk);
 
     entity.addLogicComponent(new EffectsOnDeath(entity, Arrays.asList(
       new SpawnAnimationEffect(entity, death, statics),
       new RemoveEntityEffect(entity)
     )));
 
     InfoBar infoBar = new InfoBar(entity,
       data.hitbox.width, BAR_HEIGHT, BAR_OFFSET_X, BAR_OFFSET_Y);
     infoBar.add(new Bar(life, Color.green, Color.red));
 
     return new Unit(entity, mov, life, infoBar);
   }
 
   public Unit makeCreep(float x, float y, CreepData data)
       throws ParserException, IOException {
     return makeUnit(x, y, -data.unit.speed, 0, CREEP_ORIENTATION, data.unit);
   }
 
   public Player makePlayer(String playerName)
       throws ParserException, IOException {
     PlayerData data = playersData.getPlayer(playerName);
 
     Unit unit = makeUnit(0, 0, 0, 0, PLAYER_ORIENTATION, data.unit);
 
     Hand hand = new Hand(unit.entity, PLAYER_ORIENTATION,
         new Vector2(data.handOffset.x, data.handOffset.y));
    Shop shop = new Shop(shopData, weaponFactory);
 
     Inventory inv = new Inventory(data.startMoney);
     Weapon weapon = weaponFactory.makeWeapon(data.startWeapon, PLAYER_ORIENTATION);
     inv.addWeapon(weapon);
     hand.grab(weapon);
 
     // Add components
     unit.entity.addLogicComponent(new MovementConstraint(unit.entity, worldRect));
     unit.entity.addLogicComponent(inv);
     unit.entity.addRenderComponent(hand);
     unit.entity.addLogicComponent(new PlayerControl(unit.entity, unit.movement,
         inv, shop, hand, data.unit.speed));
 
     unit.entity.addRenderComponent(new Outliner(unit.entity, unit.movement, true, true));
 
     // UI stuff
     unit.infoBar.add(new Bar(hand, Color.blue, TRANSPARENT));
 
     return new Player(unit.entity, shop, inv, unit.infoBar);
   }
 
   public Unit makeBoss(BossData data)
       throws ParserException, IOException {
     int middleY = (int) (worldRect.getCenter().y - data.unit.hitbox.height / 2);
 
     Vector2 initialTarget = new Vector2(
       worldRect.getX2() + data.unit.hitbox.width - data.locationX,
       middleY
     );
 
     Unit unit = makeUnit(worldRect.getX2(), middleY, 0, 0, BOSS_ORIENTATION, data.unit);
 
     Hand hand = new Hand(unit.entity, BOSS_ORIENTATION,
         new Vector2(data.handOffset.x, data.handOffset.y));
     Weapon weapon = weaponFactory.makeWeapon(data.weapon, BOSS_ORIENTATION);
    BossAI ai     = new BossAI(unit.entity, unit.movement, hand, worldRect,
                               data.locationX, data.unit.speed, initialTarget);
 
     unit.entity.addLogicComponent(ai);
     unit.entity.addRenderComponent(hand);
 
     unit.entity.addRenderComponent(new Outliner(unit.entity, unit.movement,
                                                 true, true));
     unit.entity.addRenderComponent(new DebugBoss(unit.entity.body, ai));
 
     hand.grab(weapon);
 
     return unit;
   }
 }
