 package program.mapping;
 
 public class Map {
 	static Road start = new Road("start", 1);	
 	static Road R0 = new Road("R0", 280);
 	static Road R1 = new Road("R1", 145);
 	static Road R2 = new Road("R2", 1);
 	static Road R3 = new Road("R3", 120);
 	static Road R4 = new Road("R4", 1);
 	static Road R5 = new Road("R5", 304);
 	static Road R6 = new Road("R6", 180);
 	static Road R7 = new Road("R7", 50);
 	static Road R8 = new Road("R8", 160);
 	static Road R9 = new Road("R9", 50);
 	static Road R10 = new Road("R10", 180);
 	static Road R11 = new Road("R11", 50);
 	static Road R12 = new Road("R12", 90);
 	static Road R14 = new Road("R14", 70);
 	static Road R15 = new Road("R15", 186);
 	static Road R16 = new Road("R16", 202);
 	static Road R17 = new Road("R17", 159);
 	static Road R18 = new Road("R18", 150);
 	static Road R19 = new Road("R19", 212);
 	static Road R20 = new Road("R20", 24);
 	static Road R21 = new Road("R21", 208);
 	static Road R22 = new Road("R22", 25);
 	static Road R23 = new Road("R23", 200);
 	static Road R24 = new Road("R24", 50);
 	static Road R25 = new Road("R25", 90);
 	static Road R26 = new Road("R26", 24);
 	static Road R27 = new Road("R27", 104);
 	static Road R28 = new Road("R28", 150);
 	static Road R29 = new Road("R29", 30);
 	static Road R30 = new Road("R30", 176);
 	static Road R31 = new Road("R31", 59);
 	static Road R32 = new Road("R32", 50);
 	static Road R33 = new Road("R33", 104);
 	static Road R34 = new Road("R34", 106);
 	static Road R35 = new Road("R35", 70);
 	static Road R36 = new Road("R36", 244);
 	static Road R37 = new Road("R37", 104);
 	static Road R38 = new Road("R38", 104);
 	static Road R39 = new Road("R39", 24);
 	static Road R40 = new Road("R40", 150);
 	static Road R41 = new Road("R41", 90);
 	static Road R42 = new Road("R42", 68);
 	static Road R43 = new Road("R43", 68);
 	static Road R44 = new Road("R44", 90);
 	static Road R45 = new Road("R45", 90);
 	static Road R46 = new Road("R46", 50);
 	static Road R47 = new Road("R47", 50);
 	static Road R48 = new Road("R48", 10);
 	static Road R49 = new Road("R49", 50);
 	static Road R50 = new Road("R50", 24);
 	static Road R51 = new Road("R51", 90);
 	static Road R52 = new Road("R52", 50);
 	static Road R53 = new Road("R53", 50);
 	static Road R54 = new Road("R54", 159);
 	static Road R55 = new Road("R55", 112);
 	static Road R56 = new Road("R56", 50);
 	static Road R57 = new Road("R57", 140);
 	static Road R58 = new Road("R58", 46);
 	static Road R59 = new Road("R59", 140);
 	static Road R60 = new Road("R60", 50);
 	static Road R61 = new Road("R61", 46);
 	static Road R62 = new Road("R62", 88);
 	static Road R63 = new Road("R63", 108);
 	static Road R64 = new Road("R64", 80);
 	static Road R65 = new Road("R65", 75);
 	static Road R66 = new Road("R66", 56);
 	static Road R67 = new Road("R67", 129);
 	static Road R68 = new Road("R68", 98);
 	static Road R69 = new Road("R69", 150);
 	static Road R70 = new Road("R70", 104);
 	static Road R71 = new Road("R71", 50);
 	
 	public static void resetMap() {
 		start.setG_value(-1);
 		R0.setG_value(-1);
 		R1.setG_value(-1);
 		R2.setG_value(-1);
 		R3.setG_value(-1);
 		R4.setG_value(-1);
 		R5.setG_value(-1);
 		R6.setG_value(-1);
 		R7.setG_value(-1);
 		R8.setG_value(-1);
 		R9.setG_value(-1);
 		R10.setG_value(-1);
 		R11.setG_value(-1);
 		R12.setG_value(-1);
		R13.setG_value(-1);
 		R14.setG_value(-1);
 		R15.setG_value(-1);
 		R16.setG_value(-1);
 		R17.setG_value(-1);
 		R18.setG_value(-1);
 		R19.setG_value(-1);
 		R20.setG_value(-1);
 		R21.setG_value(-1);
 		R22.setG_value(-1);
 		R23.setG_value(-1);
 		R24.setG_value(-1);
 		R25.setG_value(-1);
 		R26.setG_value(-1);
 		R27.setG_value(-1);
 		R28.setG_value(-1);
 		R29.setG_value(-1);
 		R30.setG_value(-1);
 		R31.setG_value(-1);
 		R32.setG_value(-1);
 		R33.setG_value(-1);
 		R34.setG_value(-1);
 		R35.setG_value(-1);
 		R36.setG_value(-1);
 		R37.setG_value(-1);
 		R38.setG_value(-1);
 		R39.setG_value(-1);
 		R40.setG_value(-1);
 		R41.setG_value(-1);
 		R42.setG_value(-1);
 		R43.setG_value(-1);
 		R44.setG_value(-1);
 		R45.setG_value(-1);
 		R46.setG_value(-1);
 		R47.setG_value(-1);
 		R48.setG_value(-1);
 		R49.setG_value(-1);
 		R50.setG_value(-1);
 		R51.setG_value(-1);
 		R52.setG_value(-1);
 		R53.setG_value(-1);
 		R54.setG_value(-1);
 		R55.setG_value(-1);
 		R56.setG_value(-1);
 		R57.setG_value(-1);
 		R58.setG_value(-1);
 		R59.setG_value(-1);
 		R60.setG_value(-1);
 		R61.setG_value(-1);
 		R62.setG_value(-1);
 		R63.setG_value(-1);
 		R64.setG_value(-1);
 		R65.setG_value(-1);
 		R66.setG_value(-1);
 		R67.setG_value(-1);
 		R68.setG_value(-1);
 		R69.setG_value(-1);
 		R70.setG_value(-1);
 		R71.setG_value(-1);
 	}
 	
 	public static Road getMap() {
         start.setLeftChild(R23);
 
         R0.setLeftChild(R1);
 
         R1.setStraightChild(R3);
         R1.setLeftChild(R2);
 
         R2.setStraightChild(R4);
 
         R3.setStraightChild(R5);
 
         R4.setLeftChild(R5);
 
         R5.setStraightChild(R6);
         R5.setLeftChild(R7);
         
         
 
         R6.setStraightChild(R8);
         R6.setLeftChild(R9);
 
         R7.setStraightChild(R24);
         R7.setRightChild(R38);
 
         R8.setStraightChild(R10);
         R8.setLeftChild(R11);
 
         R9.setRightChild(R40);
 
         R10.setStraightChild(R12);
         R10.setLeftChild(R56);
 
         R11.setStraightChild(R71);
         R11.setRightChild(R42);
 
         R12.setLeftChild(R14);
 
         //no R13
 
         R14.setStraightChild(R15);
 
         R15.setLeftChild(R16);
 
         R16.setLeftChild(R39);
         R16.setStraightChild(R17);
 
         R17.setLeftChild(R50);
         R17.setStraightChild(R18);
 
         R18.setStraightChild(R19);
         R18.setLeftChild(R20);
 
         R19.setLeftChild(R22);
         R19.setStraightChild(R21);
 
         R20.setStraightChild(R29);
         R20.setLeftChild(R28);
         
         R21.setLeftChild(R64);
         R21.setStraightChild(R23);
          
         R22.setLeftChild(R70);
         R22.setStraightChild(R45);
         
         R23.setLeftChild(R0);        
         
         R24.setLeftChild(R33);
         R24.setStraightChild(R25);
         
         R25.setLeftChild(R70);
         R25.setStraightChild(R26);
         
         R26.setLeftChild(R19);
         
         R27.setLeftChild(R25);
         R27.setStraightChild(R70);
         
         R28.setLeftChild(R29);
         R28.setStraightChild(R27);
         
         R28.setRightChild(R30);
         
         R30.setRightChild(R32);
         R30.setStraightChild(R31);
         
         R31.setRightChild(R48);
         R31.setStraightChild(R30);
         
         R33.setLeftChild(R46);
         R33.setStraightChild(R34);
         
         R34.setLeftChild(R35);
         
         R35.setLeftChild(R36);
         
         R36.setRightChild(R47);
         R36.setStraightChild(R37);
         
         R37.setLeftChild(R24);
         R37.setStraightChild(R38);
         
         R38.setRightChild(R49);
         R38.setStraightChild(R40);
         
         R39.setLeftChild(R55);
         R39.setStraightChild(R59);
         
         R40.setRightChild(R53);
         R40.setStraightChild(R41);
         
         R41.setLeftChild(R71);
         R41.setStraightChild(R42);
         
         R42.setRightChild(R60);
         R42.setStraightChild(R43);
         
         R43.setLeftChild(R57);
         R43.setStraightChild(R44);
         
         R44.setLeftChild(R15);
         
         R45.setRightChild(R34);
         R45.setStraightChild(R46);
         
         R46.setLeftChild(R37);
         R46.setStraightChild(R47);
         
         R47.setLeftChild(R5);
         
         R48.setLeftChild(R40);
         R48.setStraightChild(R49);
         
         R49.setLeftChild(R6);
         
         R50.setLeftChild(R54);
         R50.setStraightChild(R51);
         
         R51.setRightChild(R63);
         R51.setStraightChild(R52);
         
         R52.setLeftChild(R41);
         R52.setStraightChild(R53);
         
         R53.setLeftChild(R8);
         
         R54.setRightChild(R59);
         R54.setStraightChild(R55);
         
         R55.setLeftChild(R58);
         
         R56.setRightChild(R44);
         R56.setStraightChild(R57);
         
         R57.setStraightChild(R58);
         
         R58.setLeftChild(R16);
         
         R59.setRightChild(R55);
         R59.setStraightChild(R39);
         
         R60.setRightChild(R10);        
         
         R61.setStraightChild(R62);
         
         R62.setLeftChild(R52);
         R62.setStraightChild(R63);
         
         R63.setRightChild(R30);
         
         R64.setStraightChild(R65);
         R64.setLeftChild(R67);
         
         R65.setLeftChild(R36);
         R65.setStraightChild(R66);
         
         R66.setLeftChild(R4);
         
         R67.setRightChild(R69);
         R67.setStraightChild(R68);
         
         R68.setStraightChild(R64);
         
         R69.setStraightChild(R70);
         R69.setRightChild(R45);
         
         R70.setLeftChild(R26);
         R70.setStraightChild(R27);
         
         R71.setLeftChild(R62);
         
         
         return start;
 
     }
 
 }
