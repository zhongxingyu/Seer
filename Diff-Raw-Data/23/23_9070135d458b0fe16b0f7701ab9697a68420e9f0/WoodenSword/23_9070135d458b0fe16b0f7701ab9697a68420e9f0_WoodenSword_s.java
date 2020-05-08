 package gameObjects.items.Weapons;
 
 import gameObjects.WeaponType;
 import gameObjects.items.IItem;
 import gameObjects.items.IWeapon;
 
 public class WoodenSword extends Weapon implements IWeapon, IItem 
 {
 
   public WoodenSword()
   {
    this.baseEffect = 1;
     this.maxEffect = 1;
     this.name = "A Simple Wooden Sword";
     this.description = "It's not very effective";
   }
   
 	@Override
 	public WeaponType getWeaponType()
 	{
 		return WeaponType.BASIC;
 	}
 }
