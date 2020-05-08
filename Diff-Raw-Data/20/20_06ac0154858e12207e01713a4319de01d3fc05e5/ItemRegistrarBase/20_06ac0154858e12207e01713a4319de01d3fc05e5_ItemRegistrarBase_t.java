 package airbreather.mods.airbreathercore.item;
 
 import net.minecraft.item.Item;
import cpw.mods.fml.common.FMLLog;
 
 public class ItemRegistrarBase implements ItemRegistrar
 {
    public final void RegisterNewItems(ItemConfiguration configuration, ItemRegistry registry)
     {
         for (int tag : configuration.GetNewItemTags())
         {
             ItemDefinition itemDefinition = configuration.GetItemDefinition(tag);
             Item item = this.CreateItem(itemDefinition);
             registry.RegisterNewItem(itemDefinition, item);
         }
     }
 
     protected Item CreateItemCore(ItemDefinition definition)
     {
        FMLLog.severe("%d is not a recognized item tag (claims to be for %s:%s, id=%d).  THIS IS A PROGRAMMING ERROR.",
                      definition.GetTag(),
                      definition.GetModID(),
                      definition.GetItemName(),
                      definition.GetItemID());
         return null;
     }
 
     private Item CreateItem(ItemDefinition itemDefinition)
     {
         Item item = this.CreateItemCore(itemDefinition);
 
         String itemName = itemDefinition.GetItemName();
         String modID = itemDefinition.GetModID();
 
         // This is what I always set them to anyway,
         // so I refuse to sprinkle them all over the code.
         item.setUnlocalizedName(itemName);
         String textureName = String.format("%s:%s", modID, itemName);
         item.setTextureName(textureName);
 
         return item;
     }
 }
