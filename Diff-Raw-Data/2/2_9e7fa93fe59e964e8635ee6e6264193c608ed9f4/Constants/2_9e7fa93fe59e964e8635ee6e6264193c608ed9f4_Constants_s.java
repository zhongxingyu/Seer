 package jagex.runescape;
 
 /**
  * Constants.java
  * @author Ryley M. Kimmel <ryley.kimmel@live.com>
  * @version 1.0
  * Aug 14, 2012
  */
 public final class Constants {
 
     /** The programs ASCII tag */
     public static final String TAG = 
 	    "    :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::    \n" +
 	    "    :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::    \n" +
 	    "    .oPYo. .o oooooo   .oPYo.                8          o   o                  \n" +
 	    "        `8  8    .o'   8.                    8          8                      \n" +
 	    "      .oP'  8   .o'    `boo   ooYoYo. o    o 8 .oPYo.  o8P o8 .oPYo. odYo.     \n" +
 	    "       `b.  8  .o'     .P     8' 8  8 8    8 8 .oooo8   8   8 8    8 8' `8     \n" +
 	    "        :8  8 .o'      8      8  8  8 8    8 8 8    8   8   8 8    8 8   8     \n" +
 	    "    `YooP'  8 o'       `YooP' 8  8  8 `YooP' 8 `YooP8   8   8 `YooP' 8   8     \n" +
 	    "    :.....::....::::::::.....:..:..:..:.....:..:.....:::..::..:.....:..::..    \n" +
 	    "    :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::    \n" +
 	    "    :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::    \n" +
 	    "                                                                               \n" +
 	    "-------------------------------------------------------------------------------\n" +
 	    "                         Created by: Ryley M. Kimmel                             ";
 
     /** The maximum amount of players allowed in one game world. */
     public static final int MAXIMUM_WORLD_PLAYERS = 2048;
 
     /** The maximum amount of npcs allowed in one game world */
     public static final int MAXIMUM_WORLD_NPCS = 32767;
 
     /** The maximum amount of entities shown in one region */
     public static final int ENTITIES_IN_VIEW = 256;
 
     /** The cycle length in milliseconds */
     public static final int CYCLE_PERIOD = 600;
 
     /** a byte array of all the packet lengths */
     public static final byte[] PACKET_SIZES = new byte[256];
 
     /**
     * initializes the values for the packet lengths.
      */
     static {
 	for (int id = 0; id < 256; ++id) {
 	    PACKET_SIZES[id] = 0;
 	}
 	PACKET_SIZES[3] = 1;
 	PACKET_SIZES[4] = -1;
 	PACKET_SIZES[14] = 8;
 	PACKET_SIZES[16] = 6;
 	PACKET_SIZES[17] = 2;
 	PACKET_SIZES[18] = 2;
 	PACKET_SIZES[21] = 2;
 	PACKET_SIZES[23] = 6;
 	PACKET_SIZES[25] = 12;
 	PACKET_SIZES[35] = 8;
 	PACKET_SIZES[36] = 4;
 	PACKET_SIZES[39] = 2;
 	PACKET_SIZES[40] = 2;
 	PACKET_SIZES[41] = 6;
 	PACKET_SIZES[43] = 6;
 	PACKET_SIZES[45] = -1;
 	PACKET_SIZES[53] = 12;
 	PACKET_SIZES[57] = 8;
 	PACKET_SIZES[58] = 8;
 	PACKET_SIZES[59] = 12;
 	PACKET_SIZES[60] = 8;
 	PACKET_SIZES[61] = 8;
 	PACKET_SIZES[70] = 6;
 	PACKET_SIZES[72] = 2;
 	PACKET_SIZES[73] = 2;
 	PACKET_SIZES[74] = 8;
 	PACKET_SIZES[75] = 6;
 	PACKET_SIZES[77] = -1;
 	PACKET_SIZES[79] = 6;
 	PACKET_SIZES[85] = 1;
 	PACKET_SIZES[86] = 4;
 	PACKET_SIZES[87] = 6;
 	PACKET_SIZES[95] = 3;
 	PACKET_SIZES[98] = -1;
 	PACKET_SIZES[101] = 13;
 	PACKET_SIZES[103] = -1;
 	PACKET_SIZES[117] = 6;
 	PACKET_SIZES[120] = 1;
 	PACKET_SIZES[122] = 6;
 	PACKET_SIZES[126] = -1;
 	PACKET_SIZES[128] = 2;
 	PACKET_SIZES[129] = 6;
 	PACKET_SIZES[131] = 4;
 	PACKET_SIZES[132] = 6;
 	PACKET_SIZES[133] = 8;
 	PACKET_SIZES[135] = 6;
 	PACKET_SIZES[139] = 2;
 	PACKET_SIZES[145] = 6;
 	PACKET_SIZES[152] = 1;
 	PACKET_SIZES[153] = 2;
 	PACKET_SIZES[155] = 2;
 	PACKET_SIZES[156] = 6;
 	PACKET_SIZES[164] = -1;
 	PACKET_SIZES[165] = -1;
 	PACKET_SIZES[181] = 8;
 	PACKET_SIZES[183] = 3;
 	PACKET_SIZES[185] = 2;
 	PACKET_SIZES[188] = 8;
 	PACKET_SIZES[189] = 1;
 	PACKET_SIZES[192] = 12;
 	PACKET_SIZES[200] = 2;
 	PACKET_SIZES[208] = 4;
 	PACKET_SIZES[210] = 4;
 	PACKET_SIZES[214] = 7;
 	PACKET_SIZES[215] = 8;
 	PACKET_SIZES[218] = 10;
 	PACKET_SIZES[226] = -1;
 	PACKET_SIZES[228] = 6;
 	PACKET_SIZES[230] = 1;
 	PACKET_SIZES[234] = 6;
 	PACKET_SIZES[236] = 6;
 	PACKET_SIZES[237] = 8;
 	PACKET_SIZES[238] = 1;
 	PACKET_SIZES[241] = 4;
 	PACKET_SIZES[246] = -1;
 	PACKET_SIZES[248] = -1;
 	PACKET_SIZES[249] = 4;
 	PACKET_SIZES[252] = 6;
 	PACKET_SIZES[253] = 6;
     }
 
     /**
      * Blank constructor to prevent this class from being created.
      */
     private Constants() { }
 
 }
