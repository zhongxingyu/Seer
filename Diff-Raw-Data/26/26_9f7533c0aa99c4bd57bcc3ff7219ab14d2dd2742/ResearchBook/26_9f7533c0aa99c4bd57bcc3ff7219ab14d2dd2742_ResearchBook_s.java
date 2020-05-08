 package mod.industrialscience.modules.research;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import mod.industrialscience.IndustrialScience;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.world.World;
 
 public class ResearchBook extends Item {
 	public static final String NBTNAME= "Researches";
 	public ResearchBook(int id) {
 		super(id);
 		setMaxStackSize(1);
 		setUnlocalizedName("Research Book");
 		LanguageRegistry.addName(this, "Research Book");
 
 	}
 	@Override
	public void func_94581_a(IconRegister iconRegister)
 	{
	         iconIndex = iconRegister.func_94245_a("industrialscience:research-book");
 	}
 	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
     if( par1ItemStack.stackTagCompound == null )
     par1ItemStack.setTagCompound( new NBTTagCompound( ) );
     int[] researchesid={};
     par1ItemStack.stackTagCompound.setIntArray(NBTNAME, researchesid);
     
    }
 	@Override
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World,
 			EntityPlayer par3EntityPlayer) {
 		Minecraft.getMinecraft().displayGuiScreen(new ResearchBookGUI(par3EntityPlayer, par1ItemStack));
 		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
 	}
 	public int[] getResearchID() {
 		return null;
 	}
 	@SuppressWarnings("unchecked")
 	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, @SuppressWarnings("rawtypes") List par3List, boolean par4) {
 	    if( par1ItemStack.stackTagCompound == null )
 	        par1ItemStack.setTagCompound( new NBTTagCompound( ) );
 		int[] researchesid= par1ItemStack.stackTagCompound.getIntArray(NBTNAME);
 		if(researchesid!=null){
 			par3List.add("Researches: "+researchesid.length);
 		}
 	}
 	@Override
 	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
 		if( stack.stackTagCompound == null )
 	        stack.setTagCompound( new NBTTagCompound( ) );
 		ArrayList<Integer>  ids = new ArrayList<Integer>();
 		int[] researchesid= stack.stackTagCompound.getIntArray(NBTNAME);
 		for (int i : researchesid){
 			ids.add(new Integer(i));
 		}
 		ids.add(new Integer(27));
 		researchesid = new int[ids.size()];
 		for (int i = 0; i < ids.size(); i++) {
 			researchesid[i]=ids.get(i).intValue();
 		}
 		stack.stackTagCompound.setIntArray(NBTNAME, researchesid);
 		return false;
 	}
 	
 
 }
