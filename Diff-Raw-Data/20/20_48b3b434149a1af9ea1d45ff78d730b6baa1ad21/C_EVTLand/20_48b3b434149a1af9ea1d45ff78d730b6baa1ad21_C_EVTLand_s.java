 package info.pharos.ly;
 
 
 public class C_EVTLand extends oms.GameEngine.GameEvent  {
 
 // static fields
 public static final int CTRL_ATTACKTRACK = 0x1;
 public static final int CTRL_INTRACK = 0x0;
 public static final int CTRL_KILL = 0x2;
 public static final int LAND_BLOCK = 0x3;
 public static final int LAND_BLOCK_HIGHT = 0x4;
 public static final int LAND_CENCTER = 0x2;
 public static final int LAND_HIGHT = 0x0;
 public static final int LAND_MAX = 0x13;
 public static final int LAND_WIDTH = 0x1;
 public static final int LandBasePoint = 0x10;
 public static final int[][][] LandParaPosTBL = new int[20][][];
 public static final int MOVESPEED_L = 0x30000;
 public static final int MOVESPEED_S = 0x3;
 public static final int[][] NPCLANDEVT = new int[23][];
 private static final int[] NPCLand00ACT;
 private static final int[] NPCLand01ACT;
 private static final int[] NPCLand02ACT = {0x7f02004d,0x7f02004e,0x7f02004f,0xffffffff,0x000000c0,0x00000240,0x000000a0,0x00000001,0x000000b4};
 private static final int[] NPCLand03ACT;
 private static final int[] NPCLand04ACT = {0x7f020058,0x7f020059,0x7f02005a,0xffffffff,0x00000168,0x00000258,0x0000014a,0x00000001,0x0000015e};
 private static final int[] NPCLand05ACT = {0x7f02005b,0x7f02005c,0x7f02005d,0x7f02005e,0xffffffff,0x00000130,0x00000468,0x00000100,0x00000001,0x0000012c};
 private static final int[] NPCLand06ACT;
 private static final int[] NPCLand07ACT;
 private static final int[] NPCLand08ACT = {0x7f020062,0x7f020063,0x7f020064,0x7f020065,0xffffffff,0x00000178,0x00000488,0x0000012c,0x00000001,0x0000017c};
 private static final int[] NPCLand09ACT = {0x7f020066,0x7f020067,0xffffffff,0x00000090,0x00000160,0x00000082,0x00000001,0x0000008c};
 private static final int[] NPCLand10ACT = {0xffffffff,0x00000130,0x000003e8,0x000000fa,0x00000001,0x0000012c};
 private static final int[] NPCLand11ACT;
 private static final int[] NPCLand12ACT;
 private static final int[] NPCLand13ACT;
 private static final int[] NPCLand14ACT = {0x7f020072,0x7f020073,0x7f020074,0x7f020075,0xffffffff,0x00000110,0x000003e8,0x000000eb,0x00000001,0x0000010c};
 private static final int[] NPCLand15ACT;
 private static final int[] NPCLand16ACT;
 private static final int[] NPCLand17ACT;
 private static final int[] NPCLand18ACT;
 private static final int[] NPCLand19ACT;
 private static final int[] NPCLand20ACT;
 private static final int[] NPCLand21ACT;
 private static final int[][] NPCLandACT;
 public static final int TOPBLOCKCENTER = 0x130;
 public static final int TOPBLOCKIDX = 0x6;
 public static final int TOPBLOCK_FALSE = 0x0;
 public static final int TOPBLOCK_TRUE = 0x1;
 public static final int[][] UNUseLandTBL = new int[2][];
 // instance fields
 private info.pharos.ly.C_EVTLand[] c_EVTLand;
 public int m_EVTIdx;
 public int m_LandLen;
 // direct methods
 
 static{
 	int[] v0v156 = new int[10];
 	v0v156[0] = 2130837572;
 	v0v156[1] = 2130837573;
 	v0v156[2] = 2130837574;
 	v0v156[3] = 2130837575;
 	v0v156[4] = -1;
 	v0v156[5] = 168;
 	v0v156[6] = 1008;
 	v0v156[7] = 160;
 	NPCLand00ACT = v0v156;
 	int[] v0v170 = new int[11];
 	v0v170[0] = 2130837576;
 	v0v170[1] = 2130837577;
 	v0v170[2] = 2130837578;
 	v0v170[3] = 2130837579;
 	v0v170[4] = 2130837580;
 	v0v170[5] = -1;
 	v0v170[6] = 328;
 	v0v170[7] = 1048;
 	v0v170[8] = 168;
 	NPCLand01ACT = v0v170;
 	;
 	int[] v0v163 = new int[14];
 	v0v163[0] = 2130837584;
 	v0v163[1] = 2130837585;
 	v0v163[2] = 2130837586;
 	v0v163[3] = 2130837587;
 	v0v163[4] = 2130837588;
 	v0v163[5] = 2130837589;
 	v0v163[6] = 2130837590;
 	v0v163[7] = 2130837591;
 	v0v163[8] = -1;
 	v0v163[9] = 688;
 	v0v163[10] = 960;
 	v0v163[11] = 250;
 	NPCLand03ACT = v0v163;
 	;
 	;
 	int[] v0v152 = new int[9];
 	v0v152[0] = 2130837599;
 	v0v152[1] = 2130837600;
 	v0v152[2] = 2130837601;
 	v0v152[3] = -1;
 	v0v152[4] = 616;
 	v0v152[5] = 456;
 	v0v152[6] = 610;
 	NPCLand06ACT = v0v152;
 	int[] v0v173 = new int[6];
 	v0v173[0] = -1;
 	v0v173[1] = 632;
 	v0v173[2] = 1216;
 	v0v173[3] = 240;
 	NPCLand07ACT = v0v173;
 	;
 	;
 	;
 	int[] v0v144 = new int[12];
 	v0v144[0] = 2130837608;
 	v0v144[1] = 2130837609;
 	v0v144[2] = 2130837610;
 	v0v144[3] = 2130837611;
 	v0v144[4] = 2130837612;
 	v0v144[5] = 2130837613;
 	v0v144[6] = -1;
 	v0v144[7] = 624;
 	v0v144[8] = 952;
 	v0v144[9] = 270;
 	NPCLand11ACT = v0v144;
 	int[] v0v174 = new int[10];
 	v0v174[0] = 2130837614;
 	v0v174[1] = 2130837615;
 	v0v174[2] = 2130837616;
 	v0v174[3] = 2130837617;
 	v0v174[4] = -1;
 	v0v174[5] = 312;
 	v0v174[6] = 992;
 	v0v174[7] = 230;
 	NPCLand12ACT = v0v174;
 	int[] v0v171 = new int[6];
 	v0v171[0] = -1;
 	v0v171[1] = 328;
 	v0v171[2] = 944;
 	v0v171[3] = 312;
 	NPCLand13ACT = v0v171;
 	;
 	int[] v0v135 = new int[14];
 	v0v135[0] = 2130837622;
 	v0v135[1] = 2130837623;
 	v0v135[2] = 2130837624;
 	v0v135[3] = 2130837625;
 	v0v135[4] = 2130837626;
 	v0v135[5] = 2130837627;
 	v0v135[6] = 2130837628;
 	v0v135[7] = 2130837629;
 	v0v135[8] = -1;
 	v0v135[9] = 736;
 	v0v135[10] = 1200;
 	v0v135[11] = 250;
 	NPCLand15ACT = v0v135;
 	int[] v0v127 = new int[10];
 	v0v127[0] = 2130837630;
 	v0v127[1] = 2130837631;
 	v0v127[2] = 2130837632;
 	v0v127[3] = 2130837633;
 	v0v127[4] = -1;
 	v0v127[5] = 848;
 	v0v127[6] = 577;
 	v0v127[7] = 415;
 	NPCLand16ACT = v0v127;
 	int[] v0v189 = new int[11];
 	v0v189[0] = 2130837634;
 	v0v189[1] = 2130837635;
 	v0v189[2] = 2130837636;
 	v0v189[3] = 2130837637;
 	v0v189[4] = 2130837638;
 	v0v189[5] = -1;
 	v0v189[6] = 336;
 	v0v189[7] = 1080;
 	v0v189[8] = 200;
 	NPCLand17ACT = v0v189;
 	int[] v0v19 = new int[10];
 	v0v19[0] = 2130837639;
 	v0v19[1] = 2130837640;
 	v0v19[2] = 2130837641;
 	v0v19[3] = 2130837642;
 	v0v19[4] = -1;
 	v0v19[5] = 376;
 	v0v19[6] = 2064;
 	v0v19[7] = 180;
 	NPCLand18ACT = v0v19;
 	int[] v0v120 = new int[10];
 	v0v120[0] = 2130837643;
 	v0v120[1] = 2130837644;
 	v0v120[2] = 2130837645;
 	v0v120[3] = 2130837646;
 	v0v120[4] = -1;
 	v0v120[5] = 240;
 	v0v120[6] = 984;
 	v0v120[7] = 150;
 	NPCLand19ACT = v0v120;
 	int[] v0v171a = new int[6];
 	v0v171a[0] = -1;
 	v0v171a[1] = 472;
 	v0v171a[2] = 2032;
 	v0v171a[3] = 230;
 	NPCLand20ACT = v0v171a;
 	int[] v0v192 = new int[6];
 	v0v192[0] = -1;
 	v0v192[1] = 392;
 	v0v192[2] = 288;
 	v0v192[3] = 260;
 	NPCLand21ACT = v0v192;
 	int[][] v0v120a = new int[22][];
 	v0v120a[0] = info.pharos.ly.C_EVTLand.NPCLand00ACT;
 	v0v120a[1] = info.pharos.ly.C_EVTLand.NPCLand01ACT;
 	v0v120a[2] = info.pharos.ly.C_EVTLand.NPCLand02ACT;
 	v0v120a[3] = info.pharos.ly.C_EVTLand.NPCLand03ACT;
 	v0v120a[4] = info.pharos.ly.C_EVTLand.NPCLand04ACT;
 	v0v120a[5] = info.pharos.ly.C_EVTLand.NPCLand05ACT;
 	v0v120a[6] = info.pharos.ly.C_EVTLand.NPCLand06ACT;
 	v0v120a[7] = info.pharos.ly.C_EVTLand.NPCLand07ACT;
 	v0v120a[8] = info.pharos.ly.C_EVTLand.NPCLand08ACT;
 	v0v120a[9] = info.pharos.ly.C_EVTLand.NPCLand09ACT;
 	v0v120a[10] = info.pharos.ly.C_EVTLand.NPCLand10ACT;
 	v0v120a[11] = info.pharos.ly.C_EVTLand.NPCLand11ACT;
 	v0v120a[12] = info.pharos.ly.C_EVTLand.NPCLand12ACT;
 	v0v120a[13] = info.pharos.ly.C_EVTLand.NPCLand13ACT;
 	v0v120a[14] = info.pharos.ly.C_EVTLand.NPCLand14ACT;
 	v0v120a[15] = info.pharos.ly.C_EVTLand.NPCLand15ACT;
 	v0v120a[16] = info.pharos.ly.C_EVTLand.NPCLand16ACT;
 	v0v120a[17] = info.pharos.ly.C_EVTLand.NPCLand17ACT;
 	v0v120a[18] = info.pharos.ly.C_EVTLand.NPCLand18ACT;
 	v0v120a[19] = info.pharos.ly.C_EVTLand.NPCLand19ACT;
 	v0v120a[20] = info.pharos.ly.C_EVTLand.NPCLand20ACT;
 	v0v120a[21] = info.pharos.ly.C_EVTLand.NPCLand21ACT;
 	NPCLandACT = v0v120a;
 	;
 	int[] v2v128 = new int[8];
 	v2v128[6] = 4;
 	v2v128[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[0] = v2v128;
 	int[] v1v696 = new int[8];
 	v1v696[6] = 4;
 	v1v696[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[1] = v1v696;
 	int[] v1v582 = new int[8];
 	v1v582[6] = 4;
 	v1v582[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[2] = v1v582;
 	int[] v1v993 = new int[8];
 	v1v993[6] = 4;
 	v1v993[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[3] = v1v993;
 	int[] v1v791 = new int[8];
 	v1v791[6] = 4;
 	v1v791[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[4] = v1v791;
 	int[] v2v19 = new int[8];
 	v2v19[6] = 4;
 	v2v19[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[5] = v2v19;
 	int[] v1v895 = new int[8];
 	v1v895[6] = 4;
 	v1v895[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[6] = v1v895;
 	int[] v2v117 = new int[8];
 	v2v117[6] = 4;
 	v2v117[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[7] = v2v117;
 	int[] v2v153 = new int[8];
 	v2v153[6] = 4;
 	v2v153[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[8] = v2v153;
 	int[] v2v157 = new int[8];
 	v2v157[6] = 4;
 	v2v157[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[9] = v2v157;
 	int[] v2v172 = new int[8];
 	v2v172[6] = 4;
 	v2v172[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[10] = v2v172;
 	int[] v2v189 = new int[8];
 	v2v189[6] = 4;
 	v2v189[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[11] = v2v189;
 	int[] v2v134 = new int[8];
 	v2v134[6] = 4;
 	v2v134[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[12] = v2v134;
 	int[] v2v157a = new int[8];
 	v2v157a[6] = 4;
 	v2v157a[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[13] = v2v157a;
 	int[] v2v177 = new int[8];
 	v2v177[6] = 4;
 	v2v177[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[14] = v2v177;
 	int[] v2v132 = new int[8];
 	v2v132[6] = 4;
 	v2v132[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[15] = v2v132;
 	int[] v2v131 = new int[8];
 	v2v131[6] = 4;
 	v2v131[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[16] = v2v131;
 	int[] v2v174 = new int[8];
 	v2v174[6] = 4;
 	v2v174[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[17] = v2v174;
 	int[] v2v18 = new int[8];
 	v2v18[6] = 4;
 	v2v18[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[18] = v2v18;
 	int[] v2v126 = new int[8];
 	v2v126[6] = 4;
 	v2v126[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[19] = v2v126;
 	int[] v2v127 = new int[8];
 	v2v127[6] = 4;
 	v2v127[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[20] = v2v127;
 	int[] v2v131a = new int[8];
 	v2v131a[6] = 4;
 	v2v131a[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[21] = v2v131a;
 	int[] v2v160 = new int[8];
 	v2v160[6] = 4;
 	v2v160[7] = 1;
 	info.pharos.ly.C_EVTLand.NPCLANDEVT[22] = v2v160;
 	//com.game.UnicornDash.C_EVTLand.NPCLANDEVT = new int[23][];
 	;
 	int[] v2v178 = new int[21];
 	v2v178[1] = 1;
 	v2v178[2] = 4;
 	v2v178[3] = 5;
 	v2v178[4] = 6;
 	v2v178[5] = 8;
 	v2v178[6] = 9;
 	v2v178[7] = 12;
 	v2v178[8] = 8;
 	v2v178[9] = 14;
 	v2v178[10] = 16;
 	v2v178[12] = 1;
 	v2v178[13] = 4;
 	v2v178[14] = 5;
 	v2v178[15] = 6;
 	v2v178[16] = 8;
 	v2v178[17] = 9;
 	v2v178[18] = 12;
 	v2v178[19] = 14;
 	v2v178[20] = 16;
 	info.pharos.ly.C_EVTLand.UNUseLandTBL[0] = v2v178;
 	int[] v1v627 = new int[20];
 	v1v627[1] = 1;
 	v1v627[2] = 2;
 	v1v627[3] = 3;
 	v1v627[4] = 4;
 	v1v627[5] = 5;
 	v1v627[6] = 6;
 	v1v627[7] = 3;
 	v1v627[8] = 8;
 	v1v627[9] = 9;
 	v1v627[10] = 8;
 	v1v627[11] = 11;
 	v1v627[12] = 12;
 	v1v627[13] = 15;
 	v1v627[14] = 14;
 	v1v627[15] = 15;
 	v1v627[16] = 16;
 	v1v627[17] = 17;
 	v1v627[18] = 18;
 	v1v627[19] = 19;
 	info.pharos.ly.C_EVTLand.UNUseLandTBL[1] = v1v627;
 	//com.game.UnicornDash.C_EVTLand.UNUseLandTBL = new int[2][];
 	;
 	int[][] v2v126a = new int[4][];
 	int[] v4v317 = new int[2];
 	v2v126a[0] = v4v317;
 	int[] v3v611 = new int[2];
 	v2v126a[1] = v3v611;
 	int[] v3v580 = new int[2];
 	v2v126a[2] = v3v580;
 	int[] v3v988 = new int[2];
 	v2v126a[3] = v3v988;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[0] = v2v126a;
 	int[][] v1v640 = new int[4][];
 	;
 	v1v640[0] = new int[]{0x000000a4,0x00000354};
 	;
 	v1v640[1] = new int[]{0x000000a4,0x00000354};
 	;
 	v1v640[2] = new int[]{0x00000144,0x000002fe};
 	int[] v2v918 = new int[2];
 	v1v640[3] = v2v918;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[1] = v1v640;
 	int[][] v1v541 = new int[4][];
 	int[] v3v234 = new int[2];
 	v1v541[0] = v3v234;
 	int[] v2v674 = new int[2];
 	v1v541[1] = v2v674;
 	int[] v2v528 = new int[2];
 	v1v541[2] = v2v528;
 	int[] v2v956 = new int[2];
 	v1v541[3] = v2v956;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[2] = v1v541;
 	int[][] v1v93 = new int[4][];
 	;
 	v1v93[0] = new int[]{0x00000131,0x00000090};
 	;
 	v1v93[1] = new int[]{0x000000bd,0x0000036b};
 	int[] v2v512 = new int[2];
 	v1v93[2] = v2v512;
 	int[] v2v992 = new int[2];
 	v1v93[3] = v2v992;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[3] = v1v93;
 	int[][] v1v78 = new int[4][];
 	int[] v3v263 = new int[2];
 	v1v78[0] = v3v263;
 	int[] v2v649 = new int[2];
 	v1v78[1] = v2v649;
 	;
 	v1v78[2] = new int[]{0x00000161,0x00000200};
 	int[] v2v993 = new int[2];
 	v1v78[3] = v2v993;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[4] = v1v78;
 	int[][] v2v152 = new int[4][];
 	int[] v4v31 = new int[2];
 	v2v152[0] = v4v31;
 	;
 	v2v152[1] = new int[]{0x0000012c,0x000002b0};
 	;
 	v2v152[2] = new int[]{0x000000f5,0x00000415};
 	int[] v3v93 = new int[2];
 	v2v152[3] = v3v93;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[5] = v2v152;
 	int[][] v1v862 = new int[4][];
 	;
 	v1v862[0] = new int[]{0x00000267,0x000000ef};
 	int[] v2v625 = new int[2];
 	v1v862[1] = v2v625;
 	int[] v2v529 = new int[2];
 	v1v862[2] = v2v529;
 	int[] v2v958 = new int[2];
 	v1v862[3] = v2v958;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[6] = v1v862;
 	int[][] v2v184 = new int[4][];
 	int[] v4v321 = new int[2];
 	v2v184[0] = v4v321;
 	;
 	v2v184[1] = new int[]{0x00000114,0x0000034a};
 	;
 	v2v184[2] = new int[]{0x00000110,0x000003e7};
 	int[] v3v951 = new int[2];
 	v2v184[3] = v3v951;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[7] = v2v184;
 	int[][] v2v182 = new int[4][];
 	;
 	v2v182[0] = new int[]{0x000000ba,0x0000022e};
 	;
 	v2v182[1] = new int[]{0x0000014c,0x0000040d};
 	;
 	v2v182[2] = new int[]{0x00000176,0x00000243};
 	int[] v3v935 = new int[2];
 	v2v182[3] = v3v935;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[8] = v2v182;
 	int[][] v2v172a = new int[4][];
 	;
 	v2v172a[0] = new int[]{0x0000008e,0x000000b4};
 	int[] v3v642 = new int[2];
 	v2v172a[1] = v3v642;
 	int[] v3v582 = new int[2];
 	v2v172a[2] = v3v582;
 	int[] v3v982 = new int[2];
 	v2v172a[3] = v3v982;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[9] = v2v172a;
 	int[][] v2v123 = new int[4][];
 	;
 	v2v123[0] = new int[]{0x00000121,0x00000123};
 	;
 	v2v123[1] = new int[]{0x0000012d,0x000002f4};
 	int[] v3v534 = new int[2];
 	v2v123[2] = v3v534;
 	int[] v3v998 = new int[2];
 	v2v123[3] = v3v998;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[10] = v2v123;
 	int[][] v2v197 = new int[4][];
 	int[] v4v333 = new int[2];
 	v2v197[0] = v4v333;
 	;
 	v2v197[1] = new int[]{0x00000113,0x00000214};
 	;
 	v2v197[2] = new int[]{0x00000126,0x0000034d};
 	int[] v3v932 = new int[2];
 	v2v197[3] = v3v932;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[11] = v2v197;
 	int[][] v2v121 = new int[4][];
 	;
 	v2v121[0] = new int[]{0x00000135,0x00000327};
 	int[] v3v68 = new int[2];
 	v2v121[1] = v3v68;
 	int[] v3v57 = new int[2];
 	v2v121[2] = v3v57;
 	int[] v3v990 = new int[2];
 	v2v121[3] = v3v990;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[12] = v2v121;
 	int[][] v2v179 = new int[4][];
 	int[] v4v318 = new int[2];
 	v2v179[0] = v4v318;
 	;
 	v2v179[1] = new int[]{0x00000144,0x000001e7};
 	;
 	v2v179[2] = new int[]{0x00000130,0x00000359};
 	int[] v3v947 = new int[2];
 	v2v179[3] = v3v947;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[13] = v2v179;
 	int[][] v2v187 = new int[4][];
 	;
 	v2v187[0] = new int[]{0x000000d9,0x0000021c};
 	int[] v3v695 = new int[2];
 	v2v187[1] = v3v695;
 	int[] v3v550 = new int[2];
 	v2v187[2] = v3v550;
 	int[] v3v951a = new int[2];
 	v2v187[3] = v3v951a;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[14] = v2v187;
 	int[][] v2v115 = new int[4][];
 	;
 	v2v115[0] = new int[]{0x0000015d,0x000002b7};
 	;
 	v2v115[1] = new int[]{0x00000153,0x00000218};
 	;
 	v2v115[2] = new int[]{0x0000015c,0x0000034b};
 	int[] v3v962 = new int[2];
 	v2v115[3] = v3v962;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[15] = v2v115;
 	int[][] v2v146 = new int[4][];
 	;
 	v2v146[0] = new int[]{0x000001b8,0x000000b3};
 	int[] v3v661 = new int[2];
 	v2v146[1] = v3v661;
 	int[] v3v511 = new int[2];
 	v2v146[2] = v3v511;
 	int[] v3v948 = new int[2];
 	v2v146[3] = v3v948;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[16] = v2v146;
 	int[][] v2v131aa = new int[4][];
 	;
 	v2v131aa[0] = new int[]{0x000000b1,0x00000318};
 	;
 	v2v131aa[1] = new int[]{0x000000b6,0x000003e7};
 	;
 	v2v131aa[2] = new int[]{0x0000014d,0x0000030f};
 	int[] v3v924 = new int[2];
 	v2v131aa[3] = v3v924;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[17] = v2v131aa;
 	int[][] v2v111 = new int[4][];
 	;
 	v2v111[0] = new int[]{0x00000176,0x00000305};
 	;
 	v2v111[1] = new int[]{0x00000176,0x0000051a};
 	int[] v3v522 = new int[2];
 	v2v111[2] = v3v522;
 	int[] v3v932a = new int[2];
 	v2v111[3] = v3v932a;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[18] = v2v111;
 	int[][] v2v167 = new int[4][];
 	;
 	v2v167[0] = new int[]{0x000000e8,0x0000022a};
 	;
 	v2v167[1] = new int[]{0x000000e9,0x00000395};
 	int[] v3v58 = new int[2];
 	v2v167[2] = v3v58;
 	int[] v3v961 = new int[2];
 	v2v167[3] = v3v961;
 	info.pharos.ly.C_EVTLand.LandParaPosTBL[19] = v2v167;
 	//com.game.UnicornDash.C_EVTLand.LandParaPosTBL = new int[20][][];
 	// op;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 	;
 }
 
 public C_EVTLand(){
 	super();
 	this.c_EVTLand = null /* 0 */;
 	this.EVT.ACTPtr = info.pharos.ly.C_EVTLand.NPCLandACT;
 	this.EVT.EVTPtr = info.pharos.ly.C_EVTLand.NPCLANDEVT;
 }
 
 private int GetLandPara(int LandIdx,int Type){
 	int Result = 0;
 	if (info.pharos.ly.C_Global.g_LandACTNum == -1) {
 	do {
 	info.pharos.ly.C_Global.g_LandACTNum = (info.pharos.ly.C_Global.g_LandACTNum + 0x1);
	} while (info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x1)] != -1);
 	// :cond_1
 	}
 	switch(Type){
 	case 0: 
 	Result = (info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x1)] - 16);
 	return Result;
 	//break;
 	case 1: 
 	Result = (info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x2)] - 16);
 	return Result;
 	//break;
 	case 2: 
 	Result = (info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x3)] - 16);
 	return Result;
 	//break;
 	case 3: 
 	;
	return ((info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x5)] - 16) + 0x96);
 	//break;
 	case 4: 
 	;
 	return ((info.pharos.ly.C_EVTLand.NPCLandACT[LandIdx][(info.pharos.ly.C_Global.g_LandACTNum + 0x5)] - 16) + 0x96);
 	//break;
 	} //end of switch
 	return Result;
 	
 }
 
 private void MakeLandEidolon(int EVTIdx,int XVal,int YVal){
 	XVal = ((this.GetLandPara(info.pharos.ly.C_Global.g_LandIdx,2) + XVal) + 0x96);
 	info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(40,43,EVTIdx,0,(XVal - 16),(YVal - 16));
 	return;
 }
 
 private void NPCLand00(){
 	return;
 }
 
 private void getLandMakeArea(){
 	int LandMakeArea = 0;
 	int CurSpeed = ((info.pharos.ly.C_Global.g_LandMoveSpeed - 0xa0000) >> 0x10);
 	int MaxSpeed = 10;
 	LandMakeArea = ((CurSpeed * 0x13) / MaxSpeed);
 	if (LandMakeArea < 4) {
 	LandMakeArea = 4;
 	// :cond_0
 	}
 	if (LandMakeArea > 19) {
 	LandMakeArea = 19;
 	// :cond_1
 	}
 	info.pharos.ly.C_Global.g_LandMakeArea = LandMakeArea;
 	int LandIdx;
 	do {
 	LandIdx = info.pharos.ly.C_EVTLand.UNUseLandTBL[info.pharos.ly.C_Global.g_chkVersion][(info.pharos.ly.C_PUB.Random(LandMakeArea) + 0x1)];
 	} while (LandIdx == info.pharos.ly.C_Global.g_LandIdx);
 	info.pharos.ly.C_Global.g_LandIdx = LandIdx;
 	return;
 }
 
 private void getLandSpaceArea(int CurLand_Y){
 	info.pharos.ly.C_Global.g_LandSpace = (CurLand_Y - 480);
 	info.pharos.ly.C_Global.g_LandSpace = (info.pharos.ly.C_Global.g_LandSpace + this.GetLandPara(info.pharos.ly.C_Global.g_LandIdx,1));
 	int CurSpeed = (info.pharos.ly.C_Global.g_LandMoveSpeed >> 0x10);
 	info.pharos.ly.C_Global.g_LandSpace = (info.pharos.ly.C_Global.g_LandSpace + ((CurSpeed * CurSpeed) + 0x12c));
 	info.pharos.ly.C_Global.g_LandSpace = (info.pharos.ly.C_Global.g_LandSpace << 0x10);
 	return;
 }
 
 private void getLandTopBlock(int X,int Y){
 	if (this.GetLandPara(info.pharos.ly.C_Global.g_LandIdx,3) == 1 && info.pharos.ly.C_PUB.Random(2) == 1) {
 	X = (X + this.GetLandPara(info.pharos.ly.C_Global.g_LandIdx,4));
 	X = (X + info.pharos.ly.C_PUB.Random(100));
 	Y = (Y + -0x130);
 	Y = (Y + info.pharos.ly.C_PUB.Random(this.GetLandPara(info.pharos.ly.C_Global.g_LandIdx,1)));
 	this.CreateTopBlock(X,Y);
 	// :cond_0
 	}
 	return;
 }
 
 private boolean setLandProp(int EVTIdx,int XVal,int YVal){
 	int Ran = 0;
 	int X = 0;
 	int Y = 0;
 	if (info.pharos.ly.C_PUB.Random(2) == 1) {
 	Ran = info.pharos.ly.C_PUB.Random(3);
 	int Setoff_X = info.pharos.ly.C_EVTLand.LandParaPosTBL[info.pharos.ly.C_Global.g_LandIdx][Ran][0];
 	int Setoff_Y = info.pharos.ly.C_EVTLand.LandParaPosTBL[info.pharos.ly.C_Global.g_LandIdx][Ran][1];
 	if (Setoff_X != 0 && Setoff_X != 0) {
 	X = ((XVal + Setoff_X) - 16);
 	Y = ((YVal + Setoff_Y) - 16);
 	info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(40,41,EVTIdx,0,X,Y);
 	//[OTHER] end local v8           #Setoff_X:I
 	//[OTHER] end local v9           #Setoff_Y:I
 	// :cond_0
 	}
 	}
 	if (info.pharos.ly.C_Global.g_LandIdx == 1 && Ran != 2) {
 	;
 	return true /* 1 */;
 	} else {
 	if (info.pharos.ly.C_Global.g_LandIdx == 8 && Ran != 2) {
 	;
 	return true /* 1 */;
 	// goto :goto_0
 	} else {
 	if (info.pharos.ly.C_Global.g_LandIdx == 17 && Ran != 2) {
 	;
 	return false /* 0 */;
 	// goto :goto_0
 	} else {
 	;
 	return false /* 0 */;
 		}
 		}
 		}
 	// goto :goto_0
 	// :goto_0
 	//return 1;
 }
 // virtual methods
 
 public void CreateLand(int X,int Y,boolean Flag){
 		info.pharos.ly.C_Global.g_LandACTNum = -1;
 		int i = 0;
 		while (i < 5) {
 			if (!this.c_EVTLand[i].EVT.Valid) {
 				int XVal = (X - this.GetLandPara(
 						info.pharos.ly.C_Global.g_LandIdx, 2));
 				XVal = (XVal + (info.pharos.ly.C_PUB.Random(20) - 10));
 				int YVal = Y;
 				this.c_EVTLand[i].MakeEVENT(XVal, YVal, 0);
 				this.c_EVTLand[i].EVT.Attrib = 2;
 				this.c_EVTLand[i].EVT.Status = (this.c_EVTLand[i].EVT.Status | 0x2400);
 				this.c_EVTLand[i].m_LandLen = this.GetLandPara(
 						info.pharos.ly.C_Global.g_LandIdx, 1);
 				this.c_EVTLand[i].SetEVTCtrl(info.pharos.ly.C_Global.g_LandIdx,
 						0);
 				if (this.setLandProp(i, XVal, YVal)) {
 					this.setLandPowerBall(i, XVal, YVal);
 					// :cond_1
 				}
 				this.getLandTopBlock(XVal, YVal);
 				if (Flag) {
 					this.MakeLandEidolon(i, XVal, YVal);
 					// :cond_2
 				}
 				this.getLandSpaceArea(YVal);
 				this.getLandMakeArea();
 				// goto :goto_1
 				break; //
 			} else {
 				i = (i + 0x1);
 			}
 			// goto :goto_0
 			// [OTHER] end local v0 #XVal:I
 			// [OTHER] end local v1 #YVal:I
 			// :cond_0
 		}
 	return;
 }
 
 public void CreateTopBlock(int X,int Y){
 	int i = 0;
 	while (i < 5) {
 	if (!this.c_EVTLand[i].EVT.Valid) {
 	this.c_EVTLand[i].MakeEVENT(X,Y,0);
 	this.c_EVTLand[i].EVT.Attrib = 2;
 	this.c_EVTLand[i].EVT.Status = (this.c_EVTLand[i].EVT.Status | 0x2400);
 	this.c_EVTLand[i].SetEVTCtrl(6,0);
 	this.c_EVTLand[i].m_LandLen = this.GetTopBlockParaWidth();
 	// goto :goto_1
 break ; //
 	} else {
 	i = (i + 0x1);
 		}
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 public void EVTRUN(){
 	this.NPCLand00();
 	return;
 }
 
 public int GetTopBlockParaWidth(){
 	int Idx = 0;
 	int Result = 0;
 	do {
 	Idx = (Idx + 0x1);
 	} while (info.pharos.ly.C_EVTLand.NPCLandACT[6][Idx] != -1);
 	Result = (info.pharos.ly.C_EVTLand.NPCLandACT[6][(Idx + 0x2)] - 16);
 	return Result;
 	//return Result;
 }
 
 public void InitNPCLandOBJ(info.pharos.ly.C_EVTLand[] cEVTLand){
 	this.c_EVTLand = cEVTLand;
 	int i = 0;
 	while (i < 5) {
 	this.c_EVTLand[i].c_EVTLand = cEVTLand;
 	i = (i + 0x1);
 	// goto :goto_0
 	// :cond_0
 	}
 	return;
 }
 
 void setLandPowerBall(int EVTIdx,int XVal,int YVal){
 	if (info.pharos.ly.C_PUB.Random(2) != 1) {
 	} else {
 	int Setoff_X = info.pharos.ly.C_EVTLand.LandParaPosTBL[info.pharos.ly.C_Global.g_LandIdx][2][0];
 	int Setoff_Y = info.pharos.ly.C_EVTLand.LandParaPosTBL[info.pharos.ly.C_Global.g_LandIdx][2][1];
 	int X = (((XVal + Setoff_X) - 16) + 0x28);
 	int Y = ((YVal + Setoff_Y) - 16);
 	info.pharos.ly.C_OPhoneApp.cLib.getMessageMgr().SendMessage(40,48,EVTIdx,0,X,Y);
 		}
 	// goto :goto_0
 	// :goto_0
 	return;
 }
 
 }
