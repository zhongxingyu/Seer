 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package main;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.newdawn.slick.Graphics;
 
 import other.GameTime;
 import physics.CollisionHelper;
 import basics.Rectangle;
 
 import components.interfaces.IActionProducer;
 import components.interfaces.IDamagable;
 import components.interfaces.IEntity;
 import components.interfaces.IUnit;
 import components.physics.AABB;
 import components.triggers.actions.IAction;
 
 import entities.Animated;
 import entities.Creep;
 import entities.Entity;
 import entities.Player;
 import entities.projectiles.Projectile;
 
 public class World {
   private final float                 width, height;
   private final Rectangle             smallBox, bigBox, creepKiller;
 
   // TODO: The multiple-lists solution isn't really nice. Make something better.
   private final ArrayList<Entity>     entities;
   private final ArrayList<IUnit>      units;
   private final ArrayList<Creep>      creeps;
   private final ArrayList<Projectile> projectiles;
   private final ArrayList<Player>     players;
 
   public World(final float width, final float height) {
     this.width = width;
     this.height = height;
 
     smallBox = new Rectangle(0, 0, width, height);
    bigBox = new Rectangle(-width, -height, width * 2, height * 2);
     creepKiller = new Rectangle(-width, 0, width, height);
 
     entities = new ArrayList<Entity>();
     units = new ArrayList<IUnit>();
     creeps = new ArrayList<Creep>();
     projectiles = new ArrayList<Projectile>();
     players = new ArrayList<Player>();
   }
 
   public void update(final GameTime time) {
     // Use this for delaying action execution to the end of the frame
     final ArrayList<IAction> actions = new ArrayList<IAction>();
 
     // Do projectile collisions first, I think it leads to more accurate collisions
     for (final Projectile p : projectiles) {
       if (p.canCollide()) {
         // Check for collisions with units
         final AABB a = p.getBody();
         for (final IUnit u : units) {
           if (CollisionHelper.SweepCollisionTest(a, u.getBody(), time.getFrameLength())) {
             if (!projCollisionWithUnit(p, u)) {
               break;
             }
           }
         }
       }
     }
 
     // Update
     updateEntities(projectiles, time);
     updateEntities(entities, time);
     updateEntities(units, time);
 
     // Update players
     for (final Player p : players) {
       p.update(time);
       CollisionHelper.BlockFromExiting(p.getBody(), smallBox);
     }
 
     // Kill creeps when they've reach their goal
     for (final Creep creep : creeps) {
       if (creepKiller.isContaining(creep.getBody())) {
         creep.killSilently();
         for (Player p : players) {
           p.damage(creep.getDamage());
         }
       }
     }
 
     // Get actions
     getActions(projectiles, actions);
     getActions(players, actions);
     getActions(units, actions);
 
     // Execute all actions accumulated during the frame
     for (final IAction a : actions) {
       a.execute(this);
     }
 
     // Remove dead objects, do this last so we make sure any actions
     // are carried out properly
     removeNoMores(units);
     removeNoMores(creeps);
     removeNoMores(projectiles);
   }
 
   public void render(final Graphics g) {
     for (final IEntity e : entities) {
       e.render(g);
     }
 
     for (final IUnit u : units) {
       u.render(g);
     }
 
     for (final Player p : players) {
       p.render(g);
     }
 
     for (final Projectile p : projectiles) {
       p.render(g);
     }
   }
 
   public void add(final Player p) {
     assert (p != null);
 
     players.add(p);
   }
 
   public void add(final Projectile p) {
     assert (p != null);
 
     projectiles.add(p);
   }
 
   public void add(final Creep c) {
     assert (c != null);
 
     units.add(c);
     creeps.add(c);
   }
 
   public void add(final Animated a) {
     assert (a != null);
 
     entities.add(a);
   }
 
   public int getX2() {
     return (int) width;
   }
 
   public int getY2() {
     return (int) height;
   }
 
   private <T extends IActionProducer> void getActions(final Iterable<T> list, final AbstractList<IAction> result) {
     for (final IActionProducer actions : list) {
       result.addAll(actions.getActions());
       actions.clearActions();
     }
   }
 
   private <T extends IDamagable> void removeNoMores(final Iterable<T> list) {
     final Iterator<T> it = list.iterator();
     while (it.hasNext()) {
       if (!it.next().isAlive()) {
         it.remove();
       }
     }
   }
 
   private <T extends IEntity> void updateEntities(final Iterable<T> list, final GameTime time) {
     final Iterator<T> it = list.iterator();
     while (it.hasNext()) {
       final IEntity e = it.next();
       e.update(time);
 
       if (!e.getBody().isIntersecting(bigBox)) {
         // Do not kill entity here since it's unnecessary. This is a design
         // decision.
         it.remove();
       }
     }
   }
 
   /**
    * Handles collisons between a projectile and a unit.
    * 
    * @return Returns true if the projecile still exists, false otherwise.
    */
   private boolean projCollisionWithUnit(final Projectile p, final IDamagable u) {
     if (p.isAlive()) {
       p.damage(1);
       u.damage(p.getDamage());
     } else {
       p.kill();
 
       return false;
     }
 
     return true;
   }
 
   public Iterable<IUnit> getUnits() {
     return units;
   }
 }
