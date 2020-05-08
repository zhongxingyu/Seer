 package obscuron.mkin.item;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import obscuron.mkin.ModularKinetics;
 import obscuron.mkin.lib.ItemInfo;
 import obscuron.mkin.lib.Reference;
 import obscuron.mkin.util.KeyUtil;
 import obscuron.mkin.util.NBTWrapper;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemCard extends Item {
 
     private Icon itemIconEncoded;
 
     public static final String TAG_NAME = ItemInfo.CARD_NAME;
     public static final String[] ID_LIST = {
         "Empty",
         "Fuzzy",
         "Normal",
         "Exact"
     };
     
     public static final String[] SIDE_LIST = {
         "Top",
         "Bottom",
        "North",
         "South",
        "East",
        "West"
     };
     
     public static final String[] COUNT_LIST = {
         "= ",
         ">= ",
         "> ",
         "< ",
         "<= ",
     };
 
     public ItemCard(int id) {
         super(id);
         this.setMaxStackSize(1);
         this.setNoRepair();
         this.setUnlocalizedName(ItemInfo.CARD_NAME);
         this.setCreativeTab(ModularKinetics.tabKinetics);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister iconRegister) {
         itemIcon = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName2());
         itemIconEncoded = iconRegister.registerIcon(Reference.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName2() + "Encoded");
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public boolean requiresMultipleRenderPasses() {
         return true;
     }
 
     @Override
     public Icon getIcon(ItemStack itemStack, int pass) {
         if (pass == 1) {
             NBTWrapper tag = new NBTWrapper(itemStack, TAG_NAME);
             if (tag.getByte("id") > 0) {
                 return itemIconEncoded;
             }
         }
         return itemIcon;
     }
 
     public String getUnlocalizedName2() {
         return this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1);
     }
 
     @Override
     public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
         if (player.isSneaking() && !world.isRemote) {
             NBTWrapper tag = new NBTWrapper(itemStack, TAG_NAME);
             tag.setByte("id", (byte) 0);
         }
         return itemStack;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
         NBTWrapper tag = new NBTWrapper(itemStack, TAG_NAME);
         Byte id = tag.getByte("id");
         if (id >= ID_LIST.length) {
             id = 0;
         }
         if (id == 0) {
             list.add(ID_LIST[id]);
         }
         else {
             if (KeyUtil.isShiftHeld()) {
                 list.add(ID_LIST[id]);
                 ItemStack itemInfo = tag.getItem("itemInfo");
                 if (itemInfo != null) {
                     if (id < 3) {
                         itemInfo.stackTagCompound = null;
                     }
                     if (id < 2) {
                         itemInfo.setItemDamage(0);
                     }
                     String name = itemInfo.getItem().getItemDisplayName(itemInfo);
                     list.add(name);
                 }
                 list.add(SIDE_LIST[tag.getByte("side")]);
                 list.add(COUNT_LIST[tag.getByte("countState")] + tag.getInt("count"));
             }
             else {
                 list.add("Hold Shift for more Info");
             }
         }
     }
 
 }
