 package monnef.dawn.item;
 
 import net.minecraft.entity.Entity;
 
 public class ItemDawnSword extends ItemDawn implements IHitWithCoolDown {
 
     public ItemDawnSword(int par1, int damage) {
         super(par1);
        setMeleeDamage(damage);
     }
 
     @Override
     public int getHitCoolDown() {
         return 40;
     }
 
     @Override
    public boolean isFull3D() {
        return true;
     }
 }
