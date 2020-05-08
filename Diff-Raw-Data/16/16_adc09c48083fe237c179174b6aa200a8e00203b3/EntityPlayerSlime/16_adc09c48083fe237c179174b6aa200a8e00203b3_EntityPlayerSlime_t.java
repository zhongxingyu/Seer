 package com.nekokittygames.modjam.UnDeath;
 
 import org.apache.commons.lang3.ArrayUtils;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
 
 import net.minecraft.command.IAdminCommand;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.monster.EntitySlime;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTBase;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.world.World;
 
 public class EntityPlayerSlime extends EntitySlime implements IEntityAdditionalSpawnData {
 	public static int EntityId;
 	public ItemStack[] items=new ItemStack[41];
 	private boolean dropItems;
 	public EntityPlayerSlime(World par1World) {
 		super(par1World);
 	}
 	
 	@Override
 	public void setDead()
     {
         this.isDead = true;
     }
 	
 	@Override
 	protected void setSlimeSize(int par1)
     {
         this.dataWatcher.updateObject(16, new Byte((byte)par1));
         this.setSize(0.6F * (float)par1, 0.6F * (float)par1);
         this.setPosition(this.posX, this.posY, this.posZ);
         this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a((double)(par1 * par1));
         //this.setEntityHealth(this.func_110138_aP());
         this.experienceValue = par1;
     }
 	
 	
 	@Override
 	protected void dropEquipment(boolean par1, int par2) {
 		for(ItemStack item:items)
 		{
 			if(item!=null)
 				this.entityDropItem(item, 0.0f);
 		}
 	}
 
 	public void InitFromPlayer(EntityPlayer player,EntitySlime slime)
 	{
 		if(slime!=null)
 		{
 			this.setSlimeSize(slime.getSlimeSize());
 			slime.isDead=true;
 		}
 		
		copyInventory(player.inventory);
 		ItemStack head=new ItemStack(Item.skull, 1, 3);
 		NBTTagCompound compound= head.getTagCompound();
 		if(compound==null)
 			compound=new NBTTagCompound();
 		compound.setString("SkullOwner", player.username);
 		//compound.setString("SkullOwner", "nekosune");
 		head.setTagCompound(compound);
 		items[40]=head;
 		
 	}
 	public void copyInventory(InventoryPlayer playerInv)
 	{
		int i;
		for (i = 0; i < playerInv.mainInventory.length; ++i)
        {
            items[i] = ItemStack.copyItemStack(playerInv.mainInventory[i]);
        }

        for (i = 0; i < playerInv.armorInventory.length; ++i)
        {
            items[36+i] = ItemStack.copyItemStack(playerInv.armorInventory[i]);
        }

 	}
 	@Override
 	public void writeSpawnData(ByteArrayDataOutput data) {
 		NBTTagCompound compound=new NBTTagCompound();
 		compound.setName("Skellington");
 		compound.setBoolean("dropItems", dropItems);
 		NBTTagList nbtTagList=new NBTTagList();
 		for(int i=0;i<items.length;i++)
 		{
 			if(items[i]!=null)
 			{
 				NBTTagCompound comp=new NBTTagCompound();
 				items[i].writeToNBT(comp);
 				nbtTagList.appendTag(comp);
 			}
 		}
 		compound.setTag("Inventory", nbtTagList);
 		
 		try {
 
 			NBTBase.writeNamedTag(compound, data);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void readSpawnData(ByteArrayDataInput data) {
 		NBTTagCompound compound;
 		try {
 			compound = (NBTTagCompound) NBTBase.readNamedTag(data);
 			NBTTagList nbttaglist = compound.getTagList("Inventory");
 			items=new ItemStack[41];
 			for (int i = 0; i < nbttaglist.tagCount(); ++i)
 	        {
 				NBTTagCompound nbttagcompound = (NBTTagCompound)nbttaglist.tagAt(i);
 	            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
 	            items[i]=itemstack;
 	        }
 			this.dropItems=compound.getBoolean("dropItems");
 			
 		}catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 }
