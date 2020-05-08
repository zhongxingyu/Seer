 package ghost606.networkcraft.resources;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 
 public class IconProvider {
 	private static IconProvider instance;
 	
 	public static final int Button = 0;
 	public static final int Button_Highlight = 1;
 	public static final int Button_Inactive = 2;
 	public static final int Access_Friends = 3;
 	public static final int Access_Private = 4;
 	public static final int Access_Public = 5;
 	public static final int Arrow_Up = 6;
 	public static final int Arrow_Down = 7;
 	public static final int Cancel = 8;
 	public static final int Cancel_Inactive = 9;
 	
 	public static final int Max = 10;
 	
 	@SideOnly(Side.CLIENT)
 	private Icon[] icons;
 	
 	@SideOnly(Side.CLIENT)
 	public static IconProvider getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new IconProvider();
 		}
 		return instance;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int iconIndex)
 	{
 		return icons[iconIndex];
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register)
 	{
 		icons = new Icon[IconProvider.Max];
 		icons[IconProvider.Button] = register.registerIcon("networkcraft:icons/Icon_Button");
 		icons[IconProvider.Button_Highlight] = register.registerIcon("networkcraft:icons/Icon_Button_Highlight");
 		icons[IconProvider.Button_Inactive] = register.registerIcon("networkcraft:icons/Icon_Button_Inactive");
 		icons[IconProvider.Access_Friends] = register.registerIcon("networkcraft:icons/Icon_Access_Friends");
 		icons[IconProvider.Access_Private] = register.registerIcon("networkcraft:icons/Icon_Access_Private");
 		icons[IconProvider.Access_Public] = register.registerIcon("networkcraft:icons/Icon_Access_Public");
 		icons[IconProvider.Arrow_Up] = register.registerIcon("networkcraft:icons/Icon_Arrow_Up");
 		icons[IconProvider.Arrow_Down] = register.registerIcon("networkcraft:icons/Icon_Arrow_Down");
 		icons[IconProvider.Cancel] = register.registerIcon("networkcraft:icons/Icon_Cancel");
 		icons[IconProvider.Cancel_Inactive] = register.registerIcon("networkcraft:icons/Icon_Cancel_Inactive");
 	}
 }
