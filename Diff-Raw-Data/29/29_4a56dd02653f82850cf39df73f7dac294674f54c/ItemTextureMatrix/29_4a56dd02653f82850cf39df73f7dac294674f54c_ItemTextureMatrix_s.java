 package sammko.quantumCraft.resources;
 
 public class ItemTextureMatrix {
 
 	//same as block texture matrix
 	
 	public static int PlutoniumIngot = 0x00;
 	
 	public static int PositroniumCrystal = 0x10;
 	public static int RadiumCrystal = 0x20;
 	public static int GammatroniumCrystal = 0x30;
 	public static int NeutriniumCrystal = 0x40;
 	public static int DepletedCrystal = 0x50;
 	
 	public static int EmptyEPacket = 0x01;
 	public static int PositroniumEPacket = 0x11;
 	public static int RadiumEPacket = 0x21;
 	public static int GammatroniumEPacket = 0x31;
 	public static int NeutriniumEPacket = 0x41;
 	
	public static int CrystalPickaxe = 0x02;
 	
 	public static int getIndex(int v)
 	{
 		return ((v >> 4) & 15) | ((v << 4) & 240);
 	
 	}
 }
