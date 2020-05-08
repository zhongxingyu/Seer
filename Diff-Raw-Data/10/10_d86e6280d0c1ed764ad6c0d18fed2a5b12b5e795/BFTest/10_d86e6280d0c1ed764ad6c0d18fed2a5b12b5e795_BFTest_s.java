 package Brainfuck;
 import Tape.*;
 import java.io.*;
 
 public class BFTest {
 	public static void main(String[] args) throws IOException {
 		final int TAPE_LENGTH = 9;
 		ConsoleTape ct = new ConsoleTape();
 		ct.init();
		// zaehle binaer 255 nullen
 		String inputString = "";
 		for(int i = 0; i < Math.pow(2,TAPE_LENGTH-1)-1; i++)
 			inputString += "0";
 		File file = new File("binary_count.bf");
 		
		// addiere 2+2 unaer 
		// String inputString = "00#00";
		// File file = new File("../unary_add.bf");
 		
 		byte[] buffer = new byte[(int) file.length()];
 		BufferedInputStream f = new BufferedInputStream(new FileInputStream(file.getPath()));
 		f.read(buffer);
 		String code = new String(buffer);
 
 		BFSimulation sim = new BFSimulation(ct, inputString, null);
 		try {
 			sim.runSimulation(code);
 		} 
 		catch(TapeException te){
 			System.out.println("something went wrong");
 			te.printStackTrace();
 		}
 		catch(IllegalArgumentException iae) {
 			System.out.println("syntaxcheck failed");
 		}
 	}
 }
