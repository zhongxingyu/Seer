 package monnef.dawn.item;
 
import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
import net.minecraft.item.EnumToolMaterial;
 
 public class ItemDawnSword extends ItemDawn implements IHitWithCoolDown {
    private int meleeDamage;
 
     public ItemDawnSword(int par1, int damage) {
         super(par1);
     }
 
     @Override
     public int getHitCoolDown() {
         return 40;
     }
 
     @Override
    public int getDamageVsEntity(Entity par1Entity) {
        return this.meleeDamage;
     }
 }
