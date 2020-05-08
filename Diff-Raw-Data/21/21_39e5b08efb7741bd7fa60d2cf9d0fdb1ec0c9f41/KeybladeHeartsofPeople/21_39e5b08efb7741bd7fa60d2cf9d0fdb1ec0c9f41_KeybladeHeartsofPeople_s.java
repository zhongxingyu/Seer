 package keyblades.common.Keyblades;
 
 import net.minecraft.src.EntityArrow;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntitySmallFireball;
 import net.minecraft.src.PotionEffect;
 import net.minecraft.src.EnumToolMaterial;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.ItemSword;
 import net.minecraft.src.Vec3;
 import net.minecraft.src.World;
 
 public class KeybladeHeartsofPeople extends ItemSword{
 	public KeybladeHeartsofPeople(int i, EnumToolMaterial enumtoolmaterial)
 	{
 		super(i, enumtoolmaterial);
 	}
 	public String getTextureFile()
 	{
 		return "/Keyblademod/keyblades.png";
 	}
 	public boolean hitEntity(ItemStack items, EntityLiving Living1, EntityLiving Living2)
 	{
		Living1.addPotionEffect(new PotionEffect(19, 25, 1));
 		return true;
 	}
 
 }
