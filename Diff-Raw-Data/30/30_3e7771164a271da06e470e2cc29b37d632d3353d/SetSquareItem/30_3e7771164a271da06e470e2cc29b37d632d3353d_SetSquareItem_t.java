 package com.tehbeard.forge.schematic.shell.items;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tehbeard.forge.schematic.SchVector;
 import com.tehbeard.forge.schematic.shell.Reference;
 import com.tehbeard.pluginChannel.Message;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.world.World;
 
 public class SetSquareItem extends Item {
 
     public SetSquareItem(int id) {
         super(id);
         setMaxStackSize(1);
         setCreativeTab(CreativeTabs.tabRedstone);
         setUnlocalizedName("protractor");
         func_111206_d("tehbeard.schematic.sh:setsquare");
     }
     
     @Override
     public void onCreated(ItemStack itemStack, World world,
             EntityPlayer player) {
         initTag(itemStack);
         super.onCreated(itemStack, world, player);
     }
     
     
     @Override
     public boolean onItemUseFirst(ItemStack stack, EntityPlayer player,
             World world, int x, int y, int z, int side, float hitX, float hitY,
             float hitZ) {
         
         initTag(stack);
         
         setCoords(player.isSneaking(),new SchVector(x,y,z));
         List<String> l = new ArrayList<String>();
         genList(stack,l);
         
         player.addChatMessage(l.get(2));
         player.addChatMessage(l.get(3));
 
         return true;// super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
     }
    
     
     private void setCoords(boolean pos1,SchVector vector){
         try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bos);
         
         dos.writeBoolean(pos1);
         dos.writeInt(vector.getX());
         dos.writeInt(vector.getY());
         dos.writeInt(vector.getZ());
         dos.flush();
         Message m = new Message(bos.toByteArray(), (char)0, Reference.SUB_CHANNEL, 0);
         
             PacketDispatcher.sendPacketToServer(new Packet250CustomPayload(Reference.BASE_CHANNEL, m.getParts()[0]));
             
             
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
 
     
 
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     @Override
     @SideOnly(Side.CLIENT)
     public void addInformation(ItemStack itemStack,
             EntityPlayer player, List infoList, boolean unknown) {
         super.addInformation(itemStack, player, infoList, unknown);
         
         genList(itemStack,infoList);
         
     }
     
     private void genList(ItemStack itemStack, List<String> infoList) {
         SchVector pos1 = getPos1(itemStack);
         SchVector pos2 = getPos2(itemStack);
         SchVector size = new SchVector(SchVector.max(pos1, pos2)).sub(SchVector.min(pos1, pos2).sub(new SchVector(1,1,1)));
         
         infoList.add(EnumChatFormatting.RED          + "Pos 1 : " + pos1);
         infoList.add(EnumChatFormatting.BLUE         + "Pos 2 : " + pos2);
         infoList.add(EnumChatFormatting.LIGHT_PURPLE + "Size : " + size);
         infoList.add(EnumChatFormatting.LIGHT_PURPLE + "Area : " + size.area());
         
     }
 
     private void initTag(ItemStack stack){
         if(stack.stackTagCompound==null){
         stack.stackTagCompound = new NBTTagCompound();
         stack.stackTagCompound.setCompoundTag("pos1", new SchVector().asTag());
         stack.stackTagCompound.setCompoundTag("pos2", new SchVector().asTag());
         }
     }
     
     public SchVector getPos1(ItemStack stack){
         initTag(stack);
         return new SchVector(stack.stackTagCompound.getCompoundTag("pos1"));
     }
     
     public SchVector getPos2(ItemStack stack){
         initTag(stack);
         return new SchVector(stack.stackTagCompound.getCompoundTag("pos2"));
     }
     
     public void setPos1(ItemStack stack,SchVector sch){
         initTag(stack);
         stack.stackTagCompound.setCompoundTag("pos1", sch.asTag());
     }
     
     public void setPos2(ItemStack stack,SchVector sch){
         initTag(stack);
         stack.stackTagCompound.setCompoundTag("pos2", sch.asTag());
     }
     
     
     
     @Override
     public boolean canItemEditBlocks() {
        return false;
     }
 
     
 }
