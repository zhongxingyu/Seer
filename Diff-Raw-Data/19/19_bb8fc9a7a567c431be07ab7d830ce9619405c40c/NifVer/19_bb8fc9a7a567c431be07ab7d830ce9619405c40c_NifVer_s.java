 package nif;
 
 public class NifVer
 {
 	/**
 	 <version num="2.3">Dark Age of Camelot</version>
 	 <version num="3.0">Star Trek: Bridge Commander</version>
 	 <version num="3.03">Dark Age of Camelot</version>
 	 <version num="3.1">Dark Age of Camelot, Star Trek: Bridge Commander</version>
 	 <version num="3.3.0.13">Oblivion</version>
 	 <version num="4.0.0.0">Freedom Force</version>
 	 <version num="4.0.0.2">Morrowind, Freedom Force</version>
 	 <version num="4.1.0.12">Dark Age of Camelot</version>
 	 <version num="4.2.0.2">Civilization IV</version>
 	 <version num="4.2.1.0">Dark Age of Camelot, Civilization IV</version>
 	 <version num="4.2.2.0">
 	 Dark Age of Camelot, Civilization IV, Empire Earth II, Culpa Innata
 	 </version>
 	 <version num="10.0.1.0">Zoo Tycoon 2, Civilization IV</version>
 	 <version num="10.0.1.2">Oblivion</version>
 	 <version num="10.0.1.3">?</version>
 	 <version num="10.1.0.0">
 	 Dark Age of Camelot, Civilization IV, Freedom Force vs. the 3rd Reich, Axis and Allies, Kohan 2, Entropia Universe, Wildlife Park 2, The Guild 2
 	 </version>
 	 <version num="10.1.0.101">Oblivion</version>
 	 <version num="10.1.0.106">Oblivion</version>
 	 <version num="10.2.0.0">
 	 Civilization IV, Oblivion, Loki, Pro Cycling Manager, Prison Tycoon, Red Ocean, Wildlife Park 2
 	 </version>
 	 <version num="20.0.0.4">Civilization IV, Oblivion, Sid Meier's Railroads</version>
 	 <version num="20.0.0.5">Oblivion</version>
 	 <version num="20.1.0.3">Megami Tensei: Imagine</version>
 	 <version num="20.2.0.7">Emerge, Empire Earth III, Fallout 3</version>
 	 <version num="20.2.0.8">Emerge, Empire Earth III</version>
 	 <!--
 	 version 20.3.0.1 is found in Props/GDCLargeContainer.nif
 	 -->
 	 <version num="20.3.0.1">Emerge</version>
 	 <!--
 	 version 20.3.0.2 is found in PFX/fx_muzzle_flash_example.nif 
 	 -->
 	 <version num="20.3.0.2">Emerge</version>
 	 <version num="20.3.0.3">Emerge</version>
 	 <version num="20.3.0.6">Emerge</version>
 	 <version num="20.3.0.9">Warhammer</version>
 	 */
 	//	NIF Version Constants
 	public static final int VER_4_0_0_2 = 0x04000002;
 
 	public static final int VER_4_1_0_1 = 0x04010001;
 
 	public static final int VER_4_1_0_12 = 0x0401000C;
 
 	public static final int VER_4_2_0_2 = 0x04020002;
 
 	public static final int VER_4_2_1_0 = 0x04020100;
 
 	public static final int VER_4_2_2_0 = 0x04020200;
 
 	public static final int VER_5_0_0_1 = 0x05000001;
 
 	public static final int VER_10_0_1_0 = 0x0A000100;
 
 	public static final int VER_10_0_1_2 = 0x0A000102;
 
 	public static final int VER_10_1_0_0 = 0x0A010000;
	 
 	public static final int VER_10_1_0_106 = 0x0A01006A;
 
 	public static final int VER_10_2_0_0 = 0x0A020000;
 
 	public static final int VER_10_4_0_1 = 0x0A040001;
 
 	public static final int VER_20_0_0_4 = 0x14000004;
 
 	public static final int VER_20_0_0_5 = 0x14000005;
 
 	public static final int VER_20_1_0_3 = 0x14010003;
 
 	public static final int VER_20_2_0_6 = 0x14020006;
 
 	public static final int VER_20_2_0_7 = 0x14020007;
 
 	public static final int VER_20_2_0_8 = 0x14020008;
 
 	public static final int VER_20_3_0_1 = 0x14030001;
 
 	public static final int VER_20_3_0_2 = 0x14030002;
 
 	public static final int VER_20_3_0_3 = 0x14030003;
 
 	public static final int VER_20_3_0_6 = 0x14030006;
 
 	public static final int VER_20_3_0_9 = 0x14030009; // special cases as this is the black prohecy number but it disagrees with nif.xml (slightly
 
 	public static final int VER_UNSUPPORTED = 0xFFFFFFFF; /*!< Unsupported Nif Version */
 
 	public static final int VER_INVALID = 0xFFFFFFFE; /*!< Not a Nif file */
 
 	// instanced strings list to be handed around
 	public String[] indexStrings;
 
 	public int LOAD_VER = VER_INVALID;
 
 	public int LOAD_USER_VER = 0;
 
 	public int LOAD_USER_VER2 = 0;
 
	public NifVer(int LOAD_VER, int LOAD_USER_VER, int LOAD_USER_VER2)
 	{
 		this.LOAD_VER = LOAD_VER;
 		this.LOAD_USER_VER = LOAD_USER_VER;
 		this.LOAD_USER_VER2 = LOAD_USER_VER2;
 	}
 
 	public String toString()
 	{
		return "nifVer " + LOAD_VER + " " + LOAD_USER_VER + " " + LOAD_USER_VER2;
 	}
 }
