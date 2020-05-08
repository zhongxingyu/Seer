 package sammko.quantumCraft.resources;
 
 public class BlockTextureMatrix {
 	
 
 	//texture layout in the file. 0x = Hex; 1st digit = Columns, 2nd digit = Rows. 0 - F (0 - 15)
 	public static int OrePositronium = 0xE0;
 	public static int OrePlutonium = 0xF0;
 	public static int OreRadium = 0xE1;
 	public static int OreGammatronium = 0xF1;
 	public static int OreNeutrinium = 0xF2;
	public static int OreDepleted = 0xE2;
 	
 	public static int Err = 0x14;
 	
 	public static int EBlockBackSingle = 0x00;
 	public static int EBlockBackTop = 0x01;
 	public static int EBlockBackMid = 0x02;
 	public static int EBlockBackBot = 0x03;
 	
 	public static int EBlockSideSingle = 0x04;
 
 	public static int PositronGenerator = 0x10;
 	public static int NeutrinoGenerator = 0x11;
 	public static int RadonGenerator = 0x12;
 	public static int GammaGenerator = 0x13;
 	
 	public static int PositronInput = 0x40;
 	public static int NeutrinoInput = 0x41;
 	public static int RadonInput = 0x42;
 	public static int GammaInput = 0x43;
 	
 	public static int RadonFieldESingle = 0x20;
 	public static int RadonFieldETop = 0x21;
 	public static int RadonFieldEMid = 0x22;
 	public static int RadonFieldEBot = 0x23;
 	
 	public static int RadonFieldESingleI = 0x30;
 	public static int RadonFieldETopI = 0x31;
 	public static int RadonFieldEMidI = 0x32;
 	public static int RadonFieldEBotI = 0x33;
 	
 	public static int NeutrinoBeamEmmiter = 0x24;
 	public static int NeutrinoBeamEmmiterI = 0x34;
 	
 	public static int NeutrinoBeamReceiver = 0x44;
 	public static int NeutrinoBeamReceiverI = 0x54;
 	
 	public static int PositronBeamEmmiter = 0x25;
 	public static int PositronBeamEmmiterI = 0x35;
 	
 	public static int getIndex(int v)
 	{
 		return ((v >> 4) & 15) | ((v << 4) & 240); //equation to convert this to MC texture index.
 	
 	}
 }
