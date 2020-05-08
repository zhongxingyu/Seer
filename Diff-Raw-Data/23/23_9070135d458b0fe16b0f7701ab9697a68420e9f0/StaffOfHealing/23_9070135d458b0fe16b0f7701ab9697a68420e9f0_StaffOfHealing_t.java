 package gameObjects.items.Weapons;
 
 import gameObjects.StatusEffects.HealOverTime;
 import gameObjects.items.IWeapon;
 
 public class StaffOfHealing extends MagicWeapon implements IWeapon
 {
 
   public StaffOfHealing()
   {
    this.baseEffect = 0;
     this.maxEffect = 1;
     this.name = "Staff of Healing";
     this.description = "A Magic Staff that boosts a characters HP Regeneration";
     this.status = new HealOverTime(2, -1);
   }
 
 
 }
