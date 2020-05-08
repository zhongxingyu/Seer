 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package game.components.holdables;
 
 import game.entities.Entity;
 import math.Vector2;
 
 public class OffsetCalculatorLeft implements IOffsetCalculator {
   private final Entity entity;
   private final Vector2 handOffset;
 
   public OffsetCalculatorLeft(Entity entity, Vector2 handOffset) {
     this.entity     = entity;
     this.handOffset = handOffset;
   }
 
   @Override
   public Vector2 getMuzzlePosition(int weaponWidth, Vector2 muzzleOffset) {
    float x = entity.body.getX1() - handOffset.x - weaponWidth + muzzleOffset.x;
     float y = entity.body.getY1() + handOffset.y + muzzleOffset.y;
     return new Vector2(x, y);
   }
 
   @Override
   public Vector2 getWeaponOffset(int weaponWidth) {
     return new Vector2(entity.body.getWidth() - handOffset.x - weaponWidth, handOffset.y);
   }
 }
