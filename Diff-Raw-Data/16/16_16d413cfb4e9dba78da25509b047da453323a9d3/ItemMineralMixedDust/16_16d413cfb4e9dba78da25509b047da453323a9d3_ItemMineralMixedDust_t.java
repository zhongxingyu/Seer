 package slimevoid.tmf.items;
 
 import slimevoid.tmf.core.TMFCore;
 import net.minecraft.item.ItemStack;
 
 public class ItemMineralMixedDust extends ItemMineralDust {
 	/**
 	 * Color multiplier.
 	 * This makes sure max level (9) equals max color (15)
 	 */
 	private static final float COLOR_MULTI = 5f/3f;
 
 	public ItemMineralMixedDust(int id) {
 		super(id);
 	}
 	
 	@Override
 	public int getColorFromItemStack(ItemStack item, int a) {
 		return metaToColor(item.getItemDamage());
 	}
 
 	@Override
 	public int getBurnTime(ItemStack stack) {
 		int timeLevel = (getDustMeta(stack) >> 8) & 15;
 		if ( timeLevel == 0 )
 			return 1600;
 
 		double out = (Math.log(timeLevel+1)*2300)+1600;
 		if ( out >  6400)
 			out = 6400;
 		
 		return (int) out;
 	}
 	@Override
 	public int getBurnSpeed(ItemStack stack) {
 		int speedLevel = (getDustMeta(stack) >> 4) & 15;
 		if ( speedLevel == 0 )
 			return 200;
 		
		double out = ( (Math.log(speedLevel+1)-200) * -(1 / (speedLevel+1) ) );
		if ( out < 30)
			out = 30;
 		
 		return (int) out;
 	}
 	@Override
 	public int getBurnWidth(ItemStack stack) {
 		int widthLevel = getDustMeta(stack) & 15;
 		if ( widthLevel > 9)
 			widthLevel = 9;
 		return widthLevel;
 	}
 	
 	/**
 	 * Convert a 12-bit meta to 24-bit color.
 	 * 
 	 * @param meta 12-bit
 	 * @return 24-bit
 	 */
 	private static int metaToColor(int meta) {
 		// Fetch individual colors
 		int iR = ((meta >> 8) & 15);
 		int iG = ((meta >> 4) & 15);
 		int iB = (meta & 15);
 		
 		// Multiply the colors
 		iR = (int) Math.ceil((float)iR*COLOR_MULTI);
 		iG = (int) Math.ceil((float)iG*COLOR_MULTI);
 		iB = (int) Math.ceil((float)iB*COLOR_MULTI);
 		
 		// Check which one is the largest
 		int max = Math.max(Math.max(iR, iG),iB);
 		
 		// This flips the colors
 		// The highest level color will be x = x+15-x = 15, which gives it max color.
 		// The lowest level color will be x = x+15+max, which gives it the least color.
 		// The higher the max is compared to the min, the stronger the color will be
 		iR += 15-max;
 		iG += 15-max;
 		iB += 15-max;
 		
 		// Reassemble the meta
 		meta = (iR << 8) | (iG << 4) |  iB;
 		
 		// Iterate each bit in the meta and double it.
 		// 12-bit to 24-bit.
 		int out = 0;
 		for ( int b = 0; b <= 12; b++ ) {
 			int shift = 1 << b;
 			int bit = (meta & shift) >> b;
 			int doubleBit = doubleBit(bit==1);
 			out |= ((doubleBit) << (b*2));
 		}
 		return out;
 	}
 	
 	/**
 	 * Doubles the bit
 	 * 1 = 11
 	 * 0 = 00
 	 * 
 	 * @param bit
 	 * @return
 	 */
 	private static int doubleBit(boolean bit) {
 		if ( bit )
 			return 3;
 		else
 			return 0;
 	}
 	
 	/**
 	 * Fetches the dust meta based on item stack with dust.
 	 * 
 	 * @param dust
 	 * @return meta
 	 */
 	public static int getDustMeta(ItemStack dust) {
 		if ( dust.getItem() instanceof ItemMineralMixedDust)
 			return dust.getItemDamage();
 		
 		if ( dust.getItem() == TMFCore.dustAcxium )
 			return 256; // 0001 0000 0000
 		
 		if ( dust.getItem() == TMFCore.dustBisogen )
 			return 16; // 0000 0001 0000
 		
 		if ( dust.getItem() == TMFCore.dustCydrine )
 			return 1; // 0000 0000 0001
 		
 		return 0;
 	}
 	
 	/**
 	 * Mixes two dust metas.
 	 * 
 	 * @param mA meta for dust A
 	 * @param mB meta for dust B
 	 * @return mixed meta
 	 */
 	public static int mixDustMeta(int mA, int mB) {
 		// Fetch the individual levels
 		int rA = (mA >> 8) & 15;
 		int rB = (mB >> 8) & 15;
 
 		int gA = (mA >> 4) & 15;
 		int gB = (mB >> 4) & 15;
 		
 		int bA = mA & 15;
 		int bB = mB & 15;
 
 		// Add the levels up
 		int r = rA+rB;
 		int g = gA+gB;
 		int b = bA+bB;
 		
 		// Neither are clean dust (both are mixed => both is mixed.
 		if ( !(isMetaCleanDust(mA) || isMetaCleanDust(mB)) ) {
 			r /= 2;
 			g /= 2;
 			b /= 2;
 		}
 		
 		// Reassemble meta
 		return (r << 8) | (g << 4) |  b;
 	}
 	
 	/**
 	 * Fetches the total meta level
 	 * 
 	 * @param meta
 	 * @return level
 	 */
 	public static int getTotalLevel(int meta) {
 		return ((meta >> 8) & 15)+((meta >> 4) & 15)+(meta & 15);
 	}
 	
 	/**
 	 * Checks if meta is from a clean dust (not mixed)
 	 * 
 	 * @param meta
 	 * @return
 	 */
 	public static boolean isMetaCleanDust(int meta) {
 		int r = (meta >> 8) & 15;
 		int g = (meta >> 4) & 15;
 		int b = meta & 15;
 		
 		// Meta is clean if all the levels summed together is 1
 		// If it is mixed; it will be greater (obviously)
 		return r+g+b == 1;
 	}
 }
