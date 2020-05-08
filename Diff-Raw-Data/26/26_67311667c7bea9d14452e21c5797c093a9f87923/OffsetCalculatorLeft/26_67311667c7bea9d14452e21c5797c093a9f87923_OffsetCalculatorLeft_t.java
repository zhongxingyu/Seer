 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package game.components.holdables;
 
 import game.entities.Entity;
 import math.Vector2;
 
 public class OffsetCalculatorLeft implements IOffsetCalculator {
   private final Entity entity;
  private final Vector2 newOffset;
 
   public OffsetCalculatorLeft(Entity entity, Vector2 handOffset) {
    this.entity = entity;

    // Since we're flipped, offset come from UPPER RIGHT corner instead of
    // UPPER LEFT.
    newOffset = new Vector2(entity.body.getWidth() - handOffset.x, handOffset.y);
   }
 
   @Override
   public Vector2 getMuzzlePosition(int weaponWidth, Vector2 muzzleOffset) {
    return Vector2.add(entity.body.getMin(),
        newOffset.x - muzzleOffset.x,
        newOffset.y + muzzleOffset.y);
   }
 
   @Override
   public Vector2 getWeaponOffset(int weaponWidth) {
    return new Vector2(newOffset.x - weaponWidth, newOffset.y);
   }
 }
