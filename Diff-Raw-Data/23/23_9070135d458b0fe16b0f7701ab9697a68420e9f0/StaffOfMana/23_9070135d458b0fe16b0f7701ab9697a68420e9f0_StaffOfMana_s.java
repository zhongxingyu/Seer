 package gameObjects.items.Weapons;
 
 import gameObjects.StatusEffects.MPRegeneration;
 import gameObjects.items.IWeapon;
 
 public class StaffOfMana extends MagicWeapon implements IWeapon
 {
 
   public StaffOfMana()
   {
    this.baseEffect = 1;
     this.maxEffect = 1;
     this.name = "Staff of Mana";
     this.description = "A Magic Staff that boosts a characters MP Regeneration";
     this.status = new MPRegeneration(2, -1);
   }
 }
