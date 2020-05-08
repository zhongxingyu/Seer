 package net.w00tc0d3.AdvancedLiquidStorage;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.inventory.SlotFurnace;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidStack;
 import net.minecraftforge.fluids.FluidTankInfo;
 
 public class ContainerAdvancedLiquidStorage extends Container {
 	
 	private TileAdvancedLiquidStorage storage;
 	private int fluidID;
 	private int amount;
	FluidStack fluid;
 	
 	public ContainerAdvancedLiquidStorage(TileAdvancedLiquidStorage tileEntityStorage) {
         this.storage = tileEntityStorage;
        this.fluid = storage.tank.getFluid();
        if(fluid == null || fluid.amount <= 0)
         	return;
        
        this.amount = fluid.amount;
        this.fluidID = fluid.fluidID;
        
        System.out.println(amount);
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return true;
 	}
 	
     public void detectAndSendChanges()
     {
         super.detectAndSendChanges();
 
         for (int i = 0; i < this.crafters.size(); ++i)
         {
             ICrafting icrafting = (ICrafting)this.crafters.get(i);
 
             if (this.fluidID != this.storage.fluidID) {
                 icrafting.sendProgressBarUpdate(this, 0, this.storage.fluidID);
             }
 
             if (this.amount != this.storage.amount) {
                 icrafting.sendProgressBarUpdate(this, 1, this.storage.amount);
             }
         }
 
         this.fluidID = this.storage.fluidID;
         this.amount = this.storage.amount;
     }
 	
     @SideOnly(Side.CLIENT)
     public void updateProgressBar(int par1, int par2) {
     }
 }
