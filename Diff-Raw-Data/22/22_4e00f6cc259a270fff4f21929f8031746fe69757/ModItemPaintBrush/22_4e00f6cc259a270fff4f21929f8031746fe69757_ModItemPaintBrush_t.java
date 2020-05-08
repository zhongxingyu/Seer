 package Mod.Items;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.Icon;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 import Mod.Lib.Refrence;
 import Mod.Main.Main;
 import Mod.TileEntity.TileEntityComputer;
 import Mod.TileEntity.TileEntityPaintBlock;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ModItemPaintBrush extends Item{
 
 	Icon[] icons = new Icon[6];
 
 			
 	
 	int Change = 1;
 	
 	public ModItemPaintBrush(int par1) {
 		super(par1);
 
 		this.setHasSubtypes(true);
 		this.setMaxStackSize(1);
 	}
 	
 	   @SideOnly(Side.CLIENT)
 	   public void registerIcons(IconRegister reg)
 	   {
 		   this.icons[0] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrush");
 		   this.icons[1] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrushRed");
 		   this.icons[2] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrushGreen");
 		   this.icons[3] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrushBlue");
 		   this.icons[4] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrushCopy");
 		   this.icons[5] = reg.registerIcon(Refrence.Mod_Id + ":" + "PaintBrushEdit");
 		   
 	   }
 	   
 	   public String getItemDisplayName(ItemStack stack)
 	    {
 	    	int meta = stack.getItemDamage();
 
 	    	if(meta == 0)return EnumChatFormatting.WHITE + StatCollector.translateToLocal("items.name.paintbrush.1");
 	    	if(meta == 1)return EnumChatFormatting.RED + StatCollector.translateToLocal("items.name.paintbrush.2");
 	    	if(meta == 2)return EnumChatFormatting.GREEN + StatCollector.translateToLocal("items.name.paintbrush.3");
 	    	if(meta == 3)return EnumChatFormatting.BLUE + StatCollector.translateToLocal("items.name.paintbrush.4");
 	    	if(meta == 4)return EnumChatFormatting.GOLD + StatCollector.translateToLocal("items.name.paintbrush.5");
 	    	if(meta == 5)return EnumChatFormatting.GOLD + StatCollector.translateToLocal("items.name.paintbrush.6");
 	    	
 	    	
 	    	
 	    	return null;
 	    }
 	    
 	    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List list)
 	    {
 	        super.getSubItems(par1, par2CreativeTabs, list);
 	        
 	        list.add(new ItemStack(par1, 1, 1));
 	        list.add(new ItemStack(par1, 1, 2));
 	        list.add(new ItemStack(par1, 1, 3));
 	        list.add(new ItemStack(par1, 1, 4));
 	        list.add(new ItemStack(par1, 1, 5));
 	        
 	    }
 	    
 	    public Icon getIconFromDamage(int meta)
 	    {
 
 	    	return icons[meta];
 
 	    }
 	    
 	    int Max = TileEntityPaintBlock.Max;
 	    
 	    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
 	    {
 	    	
 	    	
	    
 	    	
 	    	
 	    	
 	    		
 	    		TileEntity tile_e = world.getBlockTileEntity(x, y, z);
 	    		
 	    		if(tile_e instanceof TileEntityPaintBlock){
 	    			TileEntityPaintBlock tile = (TileEntityPaintBlock)tile_e;
 
 	    	    	UpdateBlock(world, x, y, z);
 	    			
 	    			int Meta = stack.getItemDamage();
 	    			
     				
 	    			
 	    			if(player.isSneaking()){
 
 
 	    				
 	    				
 	    			if(Meta == 1){
 	    				tile.SetRed(tile.GetRed() - Change);
 		    	    	UpdateBlock(world, x, y, z);
 	    				return true;
 	    			}else if (Meta == 2){
 	    				tile.SetGreen(tile.GetGreen() - Change);
 		    	    	UpdateBlock(world, x, y, z);
 	    				return true;
 	    			}else if (Meta == 3){
 	    				tile.SetBlue(tile.GetBlue() - Change);
 		    	    	UpdateBlock(world, x, y, z);
 	    				return true;
 	    			}else if (Meta == 0){
 	    				tile.SetRed(tile.GetRed() - Change);
 	    				tile.SetGreen(tile.GetGreen() - Change);
 	    				tile.SetBlue(tile.GetBlue() - Change);
 		    	    	UpdateBlock(world, x, y, z);
 	    				return true;
 	    			}else if (Meta == 4){
 	    				
 	    				if(stack.stackTagCompound == null){
 	    					stack.setTagCompound(new NBTTagCompound());
 	    					
 	    					stack.stackTagCompound.setInteger("Red", tile.GetRed());
 	    					stack.stackTagCompound.setInteger("Green", tile.GetGreen());
 	    					stack.stackTagCompound.setInteger("Blue", tile.GetBlue());
 
 	    				}else{
 	    					stack.stackTagCompound.setInteger("Red", tile.GetRed());
 	    					stack.stackTagCompound.setInteger("Green", tile.GetGreen());
 	    					stack.stackTagCompound.setInteger("Blue", tile.GetBlue());
 	    				}
     					return true;
 
 	    			}
 	    			
 	    			
 	    			
 	    			}else{
 	    				
 	    				if(Meta == 1 && tile.GetRed() >= Max)
 	    					return false;
 	    				
 	    				if(Meta == 2 && tile.GetGreen() >= Max)
 	    					return false;
 	    				
 	    				if(Meta == 3 && tile.GetBlue() >= Max)
 	    					return false;
 	    				
 		    			if(Meta == 1){
 			    			tile.SetRed(tile.GetRed() + Change);
 			    	    	UpdateBlock(world, x, y, z);
 		    				return true;
 		    				
 		    			}else if (Meta == 2){
 			    			tile.SetGreen(tile.GetGreen() + Change);
 			    	    	UpdateBlock(world, x, y, z);
 		    				return true;
 		    				
 		    			}else if (Meta == 3){
 		    				tile.SetBlue(tile.GetBlue() + Change);
 			    	    	UpdateBlock(world, x, y, z);
 		    				return true;
 		    			}else if (Meta == 4 || Meta == 5){
 		    				
 			    	    	UpdateBlock(world, x, y, z);
 		    				
 
 		    				if(stack.stackTagCompound == null){
 		    					stack.setTagCompound(new NBTTagCompound());
 		    					
 		    					
 		    					
 		    					
 		    					tile.SetRed(stack.stackTagCompound.getInteger("Red"));
 		    					tile.SetGreen(stack.stackTagCompound.getInteger("Green"));
 		    					tile.SetBlue(stack.stackTagCompound.getInteger("Blue"));
 		    					
 		    				}else{
 		    					
 		    					tile.SetRed(stack.stackTagCompound.getInteger("Red"));
 		    					tile.SetGreen(stack.stackTagCompound.getInteger("Green"));
 		    					tile.SetBlue(stack.stackTagCompound.getInteger("Blue"));
 		    					
 		    				}
 			    	    	UpdateBlock(world, x, y, z);
 	    					return true;
 	    			}
 	    			
 	    			
 	    			
 	    			
 	    		
 	    			}
 	    		
 	    	}
 	    	
 	    	
 	    	
 	    	
 	    	
 	        return false;
 	    }
 	    
 	    public void UpdateBlock(World world, int x, int y, int z){
 	    	world.markBlockForUpdate(x, y, z);
 	    	world.markBlockForRenderUpdate(x, y, z);
 	    	world.setBlockMetadataWithNotify(x, y, z, 0, 2);
 
 	    	
 
 	    	
 	    }
 	    
 	    @Override
 	    public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
 	    {
 	    	if(itemstack.getItemDamage() == 4 || itemstack.getItemDamage() == 5){
 	    		
 	    		
 	    		if(itemstack.stackTagCompound == null){
 	    			itemstack.setTagCompound(new NBTTagCompound());
 	    			  list.add(StatCollector.translateToLocal("words.red") + ": " + itemstack.stackTagCompound.getInteger("Red"));
 	    			  list.add(StatCollector.translateToLocal("words.green") + ": " + itemstack.stackTagCompound.getInteger("Green"));
 	    			  list.add(StatCollector.translateToLocal("words.blue") + ": " + itemstack.stackTagCompound.getInteger("Blue"));
 	    			
 	    			
 	    		}else{
 	    			  list.add(StatCollector.translateToLocal("words.red") + ": " + itemstack.stackTagCompound.getInteger("Red"));
 	    			  list.add(StatCollector.translateToLocal("words.green") + ": " + itemstack.stackTagCompound.getInteger("Green"));
 	    			  list.add(StatCollector.translateToLocal("words.blue") + ": " + itemstack.stackTagCompound.getInteger("Blue"));
 	    		}
 
 	    			  
 
 	    			  
 	    		
 	    		
 	    		
 	    	}
 	    	
 	    	
 	    }
 	    
 	    
 	    public void ReciveColors(int Red, int Green, int Blue, ItemStack stack){
 	    	
 	    	
 	    	if(Red < 0)
 	    		Red = 0;
 	    	
 	    	if(Green < 0)
 	    		Green = 0;
 	    	
 	    	if(Blue < 0)
 	    		Blue = 0;
 	    	
 			if(stack.stackTagCompound == null){
 				stack.setTagCompound(new NBTTagCompound());
 				
 				stack.stackTagCompound.setInteger("Red", Red);
 				stack.stackTagCompound.setInteger("Green", Green);
 				stack.stackTagCompound.setInteger("Blue", Blue);
 
 			}else{
 				stack.stackTagCompound.setInteger("Red", Red);
 				stack.stackTagCompound.setInteger("Green", Green);
 				stack.stackTagCompound.setInteger("Blue", Blue);
 			}
 
 		}
 	    
 
	    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player){
	    	
	    	if(player.isSneaking())
	    	if(item.getItemDamage() == 5){
				player.openGui(Main.instance, 1, world, 0, 0, 0);
	    	}
	    	
	    	return item;
	    }
 	    
 
 }
