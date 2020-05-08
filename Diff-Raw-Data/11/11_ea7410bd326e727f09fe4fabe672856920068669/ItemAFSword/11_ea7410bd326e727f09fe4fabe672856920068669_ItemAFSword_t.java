 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package arcaneFantasy.common.item;
 
 import net.minecraft.src.Entity;
 import net.minecraft.src.EnumToolMaterial;
 import net.minecraft.src.ItemSword;
 
import arcaneFantasy.common.lib.Reference;

 /**
  *
  * @author HMPerson1
  */
 public class ItemAFSword extends ItemSword {
 
     /**
      * The material this item is made of.
      */
     public final EnumAFToolMaterial toolMaterial;
     /**
      * How much damage this does to an entity.
      */
     private final int weaponDamage;
 
     /**
      *
      * @param id this item's id
      * @param material this sword's material
      */
     public ItemAFSword(int id, EnumAFToolMaterial material) {
         super(id, EnumToolMaterial.IRON); // MUST be called
         // Overwrite what super() just did:
         setMaxDamage(material.maxUses);
         this.toolMaterial = material;
         weaponDamage = 4 + material.damageVsEntity;
        setTextureFile(Reference.SPRITE_SHEET_LOCATION + Reference.ITEM_SPRITE_SHEET);
     }
 
     @Override
     public int getDamageVsEntity(Entity par1Entity) {
         return weaponDamage;
     }
 
     @Override
     public int getItemEnchantability() {
         return toolMaterial.enchantability;
     }
 
     /**
      * {@inheritDoc}
      *
      * @return {@inheritDoc}
      */
     @Override
     public String func_77825_f() {
         return toolMaterial.toString();
     }
 }
