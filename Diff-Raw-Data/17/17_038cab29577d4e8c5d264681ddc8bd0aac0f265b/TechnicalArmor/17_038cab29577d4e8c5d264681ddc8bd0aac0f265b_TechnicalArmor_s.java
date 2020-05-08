 package tech.technicaltools;
 
 import javax.swing.text.html.parser.Entity;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemStack;
 
 public class TechnicalArmor extends ItemArmor {
 	
	private String texturePath = "technicaltools:";
 	private String iconPath = "technicaltools:";
 
 	public TechnicalArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial,
 			int par3, int par4, String type) {
 		super(par1, par2EnumArmorMaterial, par3, par4);
 		
 		this.setMaxStackSize(1);
 		this.setCreativeTab(CreativeTabs.tabCombat);
		this.SetArmorType(type .toLowerCase(), par4);
 	}
 	
 	// 0 = helmet
 	// 1 = chestplate
 	// 2 = leggings
 	// 3 = boots
 	
 	private void SetArmorType (String type, int par4) {
 		switch(par4)
 		{
 		case 0:
 			this.setUnlocalizedName(type + "Helmet");
			this.texturePath += "textures/models/armor/technical_layer_1.png";
 			this.iconPath += type + "_helmet";
 			break;
 		case 1:
 			this.setUnlocalizedName(type + "Chestplate");
			this.texturePath += "textures/models/armor/technical_layer_1.png";
 			this.iconPath += type + "_chestplate";
 			break;
 		case 2:
 			this.setUnlocalizedName(type + "Leggings");
			this.texturePath += "textures/models/armor/technical_layer_2.png";
 			this.iconPath += type + "_leggings";
 			break;
 		case 3:
 			this.setUnlocalizedName(type + "Boots");
			this.texturePath += "textures/models/armor/technical_layer_1.png";
 			this.iconPath += type + "_boots";
 			break;
 		}
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg)
 	{
 		this.itemIcon = reg.registerIcon(this.iconPath);
 	}
 	
 	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) {
 		return this.texturePath;
 	}
 
 }
