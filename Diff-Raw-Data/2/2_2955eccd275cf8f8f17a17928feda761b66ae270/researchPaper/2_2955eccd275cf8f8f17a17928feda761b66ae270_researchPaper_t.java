 package assets.electrolysm.electro.research;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import assets.electrolysm.electro.electrolysmCore;
 import assets.electrolysm.electro.common.CommonProxy;
 import assets.electrolysm.electro.handlers.ResearchHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class researchPaper extends Item{
 
 	private String message = "Research Complete!";
 	private boolean finished;
 	private int numberOfSub = 1; 
 	@SideOnly(Side.CLIENT)
 	private Icon[] icons;
 	
 	
 	public researchPaper(int id) {
 		super(id);
 		this.setCreativeTab(electrolysmCore.TabElectrolysm);
 		this.setUnlocalizedName("researchPaper");
 	}
 	
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
     {
 		if(!player.isSneaking())
 		{
 			finished = true;
 		}
 		if(player.isSneaking())
 		{
 			finished = false;
 		}
         return stack;
     }
 	
 	public String getUnlocalizedName(ItemStack stack)
 	{
 		return "researchNote" + stack.getItemDamage();
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister red)
 	{
 		icons = new Icon[CommonProxy.RESEARCH_NOTES.length];
 		for(int i = 0; i < icons.length; i++)
 		{
 			icons[i] = red.registerIcon("electrolysm:" + CommonProxy.RESEARCH_NOTES[i]);
 		}
 	}
    
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIconFromDamage(int dmg)
 	{
 		return icons[1];
 	}
 	
 	
     public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) 
     {
     	for(int i = 0; i < ResearchHandler.getAmountOfStoredNames(); i++)
     	{
     		if(stack.getItemDamage() == i)
     		{
     			if(ResearchHandler.getStoredNames(i) != null)
     			{
     				list.add("Research: " + ResearchHandler.getStoredNames(i));
     			}
     			else
     			{
     				list.add("ERROR");
    				list.add("This is a bug! Please report it to the MOD author!");
     			}
     		}
     	}
     	if((ResearchHandler.getAmountOfStoredNames() - 1) < stack.getItemDamage())
     	{
     		list.add("Unknown Methodology");
     	}
   
     }
 
 }
