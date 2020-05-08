 package cobweb;
 
 import java.awt.Color;
 import java.awt.color.ColorSpace;
 
 @Deprecated
 public class RbgInterpol implements ColorLookup {
 
	public static int rbgRange = (1 << 24) - 1;
 
 	private static ColorSpace space = ColorSpace.getInstance(ColorSpace.CS_sRGB);
 
 	public java.awt.Color getColor(int index, int num) {
 		return new Color((int) (rbgRange * ((double) index / (double) (num))));
 	}
 
 	public ColorSpace getSpace() {
 		return space;
 	}
 
 }
