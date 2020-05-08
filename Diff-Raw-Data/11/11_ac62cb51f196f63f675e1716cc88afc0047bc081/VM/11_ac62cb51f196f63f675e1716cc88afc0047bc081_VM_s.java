 package vm;
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Random;
 
 
 
 
 
 
 
 
 public class VM {
 	
 	public boolean isHex (char in) {
         boolean ret;
         try {
             // try to parse the string to an integer, using 16 as radix
             int t = Integer.parseInt(Character.toString(in), 16);
             // parsing succeeded, string is valid hex number
             ret = true;
         } catch (NumberFormatException e) {
             // parsing failed, string is not a valid hex number
             ret = false;
         }
         return (ret);
     }
 	
 	public static int realAdress(char x1, char x2) {
 		return RM.BLOCK_SIZE * (Integer.parseInt(Character.toString(RM.memory[RM.BLOCK_SIZE * (RM.BLOCK_SIZE * RM.PLR[2] + RM.PLR[3]) + Integer.parseInt(Character.toString(x1), 16) ][3]) , 16))
 				+ RM.BLOCK_SIZE * 10 * (Integer.parseInt(Character.toString(RM.memory[RM.BLOCK_SIZE * (RM.BLOCK_SIZE * RM.PLR[2] + RM.PLR[3]) + Integer.parseInt(Character.toString(x1), 16)][2]),16) )
 				+ Integer.parseInt(Character.toString(x2), 16) ;
 	}
 	
 	public boolean newPLR() {
 		RM.PLR[0] = 0;
 		RM.PLR[1] = 0;
 		Random random = new Random();
 		boolean free = false;
 		int blockNr = 0;
 		while (!free) {
 			blockNr = random.nextInt(RM.WORD_COUNT / RM.BLOCK_SIZE);
 			if (!RM.takenBlock[blockNr])
 				free = true;
 		}
 		if (blockNr == RM.WORD_COUNT / RM.BLOCK_SIZE) {
 			System.out.println("Nera vietos atmintyje naujai puslapiu lentelei.");
 			return false;
 		} else {
 			RM.PLR[2] = (short) (blockNr / RM.BLOCK_SIZE);
 			RM.PLR[3] = (short) (blockNr % RM.BLOCK_SIZE);
 			RM.takenBlock[blockNr] = true;
 		}
 		return true;
 	}
 	
 	public void print(){
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter(new FileWriter("outputfile.txt"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		for (int i = 0; i < RM.WORD_COUNT; i++) {
 			for (int j = 0; j < RM.BYTE_COUNT; j++) {
 				out.print(RM.memory[i][j]+ " ");
 			}
 			out.println();
 		}
 		out.close();
 	}
 	
 	public static int freeBlock() {
 		for (int i = 0; i < (RM.WORD_COUNT / RM.BLOCK_SIZE); i++) {
 			if (!RM.takenBlock[i]) {
 				return i;
 			}
 		}
 		return (RM.WORD_COUNT / RM.BLOCK_SIZE);
 	}
 	
 	public static boolean checkAndWriteProgram(String file, String file2){
 		int linesCount = 0;
 		BufferedReader reader = null;
 		PrintWriter out = null;
 		String temp;
 		try {
 			reader = new BufferedReader(new FileReader(file));
 			RM.CHST1 = 1 ;
 			temp = reader.readLine();
 			if (temp.compareTo("$BEG") != 0) {
 				reader.close();
 				RM.CHST1 = 0 ;
 				RM.IOI++;
 				return false;
 			}
 			try {
 				RM.CHST3 = 1 ;
 				out = new PrintWriter(new FileWriter(file2));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			while ((temp.compareTo("$END") != 0) && (temp != null)) {
 				String temp2 = temp;
 				temp = reader.readLine();
 				if ((temp2.compareTo("$BEG") != 0) && (temp.compareTo("$END") != 0)) out.println();
 				if (temp.compareTo("$END") != 0) {
 					out.print(temp);
 				}
 				linesCount++;
 			}
 			out.close();
 			RM.CHST3 = 0 ;
 			RM.IOI += 3;
 			if (temp.compareTo("$END") != 0) {
 				reader.close();
 				RM.CHST1 = 0 ;
 				RM.IOI++;
 				return false;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 					RM.CHST1 = 0 ;
 					RM.IOI++;
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		if ((linesCount < 2) || (linesCount > 98)) return false;
 		return true;
 	}
 	
 	
 	public void executeProgram() {
 		char[] cmd = new char[RM.BYTE_COUNT];
 		boolean end = false;
 
 		while (!end) {
 			for (int i = 0; i < RM.BYTE_COUNT; i++) {
 				cmd[i] = RM.memory[realAdress(Integer.toHexString(RM.PC / RM.BLOCK_SIZE).toUpperCase().charAt(0), Integer.toHexString(RM.PC % RM.BLOCK_SIZE).toUpperCase().charAt(0))][i];
 			}
 			
 			String Op = new String();
 			Op = Op + cmd[0] + cmd[1];
 			RM.SI = 0;
 			RM.IOI = 0;
 			if (RM.PI != 0){
 				RM.PI = 0;
 				System.out.println("Programinis pertraukimas.");
 				break;
 			}
 			if (RM.TI == 0) RM.TI = 10;
 			
 			switch (Op) {
 			case "AD":
 				if (cmd[2] == 'D') 
 					ADD();
 				break;
 			case "SU":
 				if (cmd[2] == 'B')
 					SUB();
 				break;
 			case "CM":
 				if (cmd[2] == 'P')
 					CMP();
 				break;
 			case "MU":
 				if (cmd[2] == 'L')
 					MUL();
 				break;
 			case "DI":
 				if (cmd[2] == 'V')
 					DIV();
 				break;
 			case "SW":
 				SW(cmd[2], cmd[3]);
 				break;
 			case "LB":
 				LB(cmd[2], cmd[3]);
 				break;
 			case "LW":
 				LW(cmd[2], cmd[3]);
 				break;
 			case "GD":
 				GD(cmd[2], cmd[3]);
 				break;
 			case "PD":
 				System.out.print(PD(cmd[2], cmd[3]));
 				break;
 			case "JM":
 				JM(cmd[2], cmd[3]);
 				break;
 			case "JL":
 				JL(cmd[2], cmd[3]);
 				break;
 			case "JE":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "JT":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "JV":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "PU":
 				if ((cmd[2] == 'S') & (cmd[3] == 'H')) 
 					PUSH();
 				break;
 			case "PO":
 				if (cmd[2] == 'P') 
 					POP();
 				break;
 			case "HA":
 				if ((cmd[2] == 'L') & (cmd[3] == 'T')) 
 					end = true;
 
 				break;
 			default:
 				System.out.println("Neteisinga komanda.");
 				RM.PI = 2 ;
 				end = true;
 			}
 
 			RM.PC++;
 			GUI1.updateValues();
 		}
 
 	}
 	
 	
 	public void executeProgramSteps() {
 		final char[] cmd = new char[RM.BYTE_COUNT];
 		final boolean end = false;
 		
 		GUI1.startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			for (int i = 0; i < RM.BYTE_COUNT; i++) {
 				cmd[i] = RM.memory[realAdress(Integer.toHexString(RM.PC / RM.BLOCK_SIZE).toUpperCase().charAt(0), Integer.toHexString(RM.PC % RM.BLOCK_SIZE).toUpperCase().charAt(0))][i];
 			}
 			
 			String Op = new String();
 			Op = Op + cmd[0] + cmd[1];
 			RM.SI = 0;
 			RM.IOI = 0;
 			if (RM.PI != 0){
 				RM.PI = 0;
 				System.out.println("Programinis pertraukimas.");
 				return;
 			}
 			if (RM.TI == 0) RM.TI = 10;
 			
 			switch (Op) {
 			case "AD":
 				if (cmd[2] == 'D') 
 					ADD();
 				break;
 			case "SU":
 				if (cmd[2] == 'B')
 					SUB();
 				break;
 			case "CM":
 				if (cmd[2] == 'P')
 					CMP();
 				break;
 			case "MU":
 				if (cmd[2] == 'L')
 					MUL();
 				break;
 			case "DI":
 				if (cmd[2] == 'V')
 					DIV();
 				break;
 			case "SW":
 				SW(cmd[2], cmd[3]);
 				break;
 			case "LB":
 				LB(cmd[2], cmd[3]);
 				break;
 			case "LW":
 				LW(cmd[2], cmd[3]);
 				break;
 			case "GD":
 				GD(cmd[2], cmd[3]);
 				break;
 			case "PD":
 				System.out.print(PD(cmd[2], cmd[3]));
 				break;
 			case "JM":
 				JM(cmd[2], cmd[3]);
 				break;
 			case "JL":
 				JL(cmd[2], cmd[3]);
 				break;
 			case "JE":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "JT":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "JV":
 				JE(cmd[2], cmd[3]);
 				break;
 			case "PU":
 				if ((cmd[2] == 'S') & (cmd[3] == 'H')) 
 					PUSH();
 				break;
 			case "PO":
 				if (cmd[2] == 'P') 
 					POP();
 				break;
 			case "HA":
 				if ((cmd[2] == 'L') & (cmd[3] == 'T')) 
 				break;
 			default:
 				System.out.println("Neteisinga komanda.");
 				RM.PI = 2 ;
 			}
 			RM.PC++;
 			GUI1.updateValues();
 			}
 		});
 	}
 	
 	public void ADD(){
 		int suma = (RM.memory[RM.SP][0] - '0') * 1000 + (RM.memory[RM.SP][1] - '0') * 100
 				+ (RM.memory[RM.SP][2] - '0') * 10 + (RM.memory[RM.SP][3] - '0') + (RM.memory[RM.SP-1][0] - '0')
 				* 1000 + (RM.memory[RM.SP-1][1] - '0') * 100 + (RM.memory[RM.SP-1][2] - '0') * 10
 				+ (RM.memory[RM.SP-1][3] - '0');
 		RM.TI--;
 		RM.SP--;
 		RM.memory[RM.SP][3] = (char) (suma % 10 + '0');
 		RM.memory[RM.SP][2] = (char) (suma % 100 / 10 + '0');
 		RM.memory[RM.SP][1] = (char) (suma % 1000 / 100 + '0');
 		RM.memory[RM.SP][0] = (char) (suma % 10000 / 1000 + '0');
 	}
 	
 	public void SUB(){
 		int skirtumas = (RM.memory[RM.SP-1][0] - '0') * 1000 + (RM.memory[RM.SP-1][1] - '0') * 100
 				+ (RM.memory[RM.SP-1][2] - '0') * 10 + (RM.memory[RM.SP-1][3] - '0') - (RM.memory[RM.SP][0] - '0')
 				* 1000 - (RM.memory[RM.SP][1] - '0') * 100 - (RM.memory[RM.SP][2] - '0') * 10
 				- (RM.memory[RM.SP][3] - '0');
 		RM.TI--;
 		RM.SP--;
 		RM.memory[RM.SP][3] = (char) (skirtumas % 10 + '0');
 		RM.memory[RM.SP][2] = (char) (skirtumas % 100 / 10 + '0');
 		RM.memory[RM.SP][1] = (char) (skirtumas % 1000 / 100 + '0');
 		RM.memory[RM.SP][0] = (char) (skirtumas % 10000 / 1000 + '0');
 	}
 	
 	public void MUL(){
 		int sandauga = ((RM.memory[RM.SP][0] - '0') * 1000 + (RM.memory[RM.SP][1] - '0') * 100
 				+ (RM.memory[RM.SP][2] - '0') * 10 + (RM.memory[RM.SP][3] - '0')) * ((RM.memory[RM.SP-1][0] - '0')
 				* 1000 + (RM.memory[RM.SP-1][1] - '0') * 100 + (RM.memory[RM.SP-1][2] - '0') * 10
 				+ (RM.memory[RM.SP-1][3] - '0'));
 		RM.TI--;
 		RM.SP--;
 		RM.memory[RM.SP][3] = (char) (sandauga % 10 + '0');
 		RM.memory[RM.SP][2] = (char) (sandauga % 100 / 10 + '0');
 		RM.memory[RM.SP][1] = (char) (sandauga % 1000 / 100 + '0');
 		RM.memory[RM.SP][0] = (char) (sandauga % 10000 / 1000 + '0');
 	}
 	
 	public void DIV(){
		int skirtumas = ((RM.memory[RM.SP-1][0] - '0') * 1000 + (RM.memory[RM.SP-1][1] - '0') * 100
 				+ (RM.memory[RM.SP-1][2] - '0') * 10 + (RM.memory[RM.SP-1][3] - '0')) / ((RM.memory[RM.SP][0] - '0')
 				* 1000 + (RM.memory[RM.SP][1] - '0') * 100 + (RM.memory[RM.SP][2] - '0') * 10
 				+ (RM.memory[RM.SP][3] - '0'));
 		RM.TI--;
 		RM.SP--;
		RM.memory[RM.SP][3] = (char) (skirtumas % 10 + '0');
		RM.memory[RM.SP][2] = (char) (skirtumas % 100 / 10 + '0');
		RM.memory[RM.SP][1] = (char) (skirtumas % 1000 / 100 + '0');
		RM.memory[RM.SP][0] = (char) (skirtumas % 10000 / 1000 + '0');
 	}
 	
 	public void PUSH(){
 		RM.TI--;
 		RM.SP++;
 		RM.memory[RM.SP][0] = RM.DR[0];
 		RM.memory[RM.SP][1] = RM.DR[1];
 		RM.memory[RM.SP][2] = RM.DR[2];
 		RM.memory[RM.SP][3] = RM.DR[3];
 	}
 	
 	public void POP(){
 		RM.TI--;
 		RM.DR[0] = RM.memory[RM.SP][0];
 		RM.DR[1] = RM.memory[RM.SP][1];
 		RM.DR[2] = RM.memory[RM.SP][2];
 		RM.DR[3] = RM.memory[RM.SP][3];
 		RM.SP--;
 	}
 	
 	public void JM(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 			RM.PI = 3;
 			return;
 		}
 		RM.TI--;
 		RM.PC = realAdress(x,y);
 		RM.PC--;
 	}
 
 	public void JL(char x, char y) {
 		RM.TI--;
 		if (RM.SF == 2) JM(x,y);
 	}
 	
 	public void JE(char x, char y) {
 		RM.TI--;
 		if (RM.SF == 1) JM(x,y);
 	}
 	
 	public void JT(char x, char y) {
 		RM.TI--;
 		if (RM.BF == 0) JM(x,y);
 	}
 	
 	public void JV(char x, char y) {
 		RM.TI--;
 		if (RM.BF == 1) JM(x,y);
 	}
 	
 	public void FORK(){     
 		RM.TI--;
 		VM copy_VM = new VM() ;
 		copy_VM.newPLR();
 	}
 	
 	
 	
 	
 	public void GD(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 			RM.PI = 3;
 			return;
 		}
 		RM.TI = (byte) (RM.TI - 3);
 		if (RM.TI < 0) RM.TI = 0;
 		RM.SI = 1 ;
 		// pakeisti, kad veiktu skaitant daugiau nei viena kart
 		BufferedReader reader = null;
 		try {
 			RM.CHST1 = 1 ;
 			reader = new BufferedReader(new FileReader("duom.txt"));
 			String line = reader.readLine();
 			for (int i = 0; i < RM.BLOCK_SIZE; i++) {
 				for (int j = 0; j < RM.BYTE_COUNT; j++) {
 					if (line != null){
 						RM.memory[realAdress(x,y)+i][j] =  line.charAt(j);
 					} else {
 						break;
 					}
 				}
 				line = reader.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					RM.CHST1 = 0 ;
 					RM.IOI++;
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public String PD(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 			RM.PI = 3;
 			return "";
 		}
 		RM.CHST2 = 1 ;
 		RM.TI = (byte) (RM.TI - 3);
 		if (RM.TI < 0) RM.TI = 0;
 		RM.SI = 2 ;
 		String text = "";
 		int adr = realAdress(x,y);
 		for (int i = 0; i < RM.BLOCK_SIZE; i++) {
 			for (int j = 0; j < RM.BYTE_COUNT; j++)
 				if (RM.memory[adr+i][j] != '?') {
 					text = text + RM.memory[adr+i][j];
 				}
 		}
 		RM.CHST2 = 0 ;
 		RM.IOI += 2;
 		return(text);
 		
 	}
 	
 	public void HALT(){
 		RM.TI--;
 		RM.SI = 3 ;
 		
 	}
 	
 	public void LB(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 			RM.PI = 3;
 			return;
 		}
 		RM.TI--;
 		RM.DF = 0;
 		RM.DR[0] = RM.memory[realAdress(x,y)][0];
 		RM.DR[1] = RM.memory[realAdress(x,y)][1];
 		RM.DR[2] = RM.memory[realAdress(x,y)][2];
 		RM.DR[3] = RM.memory[realAdress(x,y)][3];
 	}
 	
 	public void LW(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 			RM.PI = 3;
 			return;
 		}
 		RM.TI--;
 		RM.DF = 1;
 		RM.DR[0] = RM.memory[realAdress(x,y)][0];
 		RM.DR[1] = RM.memory[realAdress(x,y)][1];
 		RM.DR[2] = RM.memory[realAdress(x,y)][2];
 		RM.DR[3] = RM.memory[realAdress(x,y)][3];
 	}
 
 	public void SW(char x, char y) {
 		if (!isHex(x) || !isHex(x) ) {
 				RM.PI = 3;
 				return;
 			}
 		RM.TI--;
 		RM.memory[realAdress(x,y)][0] = RM.DR[0];
 		RM.memory[realAdress(x,y)][1] = RM.DR[1];
 		RM.memory[realAdress(x,y)][2] = RM.DR[2];
 		RM.memory[realAdress(x,y)][3] = RM.DR[3];
 	}
 	public void CMP() {
 		RM.TI--;
 		int sk_dr = (RM.DR[0] - '0') * 1000 
 				+ (RM.DR[1] - '0') * 100 + (RM.DR[2] - '0') * 10
 				+ (RM.DR[3] - '0');
 		int sk_sp = (RM.memory[RM.SP][0] - '0') * 1000 
 			+ (RM.memory[RM.SP][1] - '0') * 100 + (RM.memory[RM.SP][2] - '0') * 10
 			+ (RM.memory[RM.SP][3] - '0');
 		if (sk_dr > sk_sp) RM.SF = 0;
 		else if (sk_dr < sk_sp) RM.SF = 2;
 			else RM.SF = 1;
 	}
 }
 
