 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 public class Simulator {
 
 	/**
 	 * 
 	 * @author Everett
 	 * 
 	 * @param objectFileName = the name of the object file to load
 	 * @throws IOException 
 	 */
 	public static void main(File objectFileName) throws IOException {
 		String[] MEM = new String[65536];
 		String[] REG = new String[16];
		boolean isHalt = false;
		int LC = 0;
		int PC = 0;
		
 		
 		
 		
 		//class object imports
 		ObjectInInterface objectFile = new ObjectIn();
 		ConverterInterface converter = new Converter();
 		
 		//Read object file
 		objectFile.readObjectFile(objectFileName);
 		
 		//get object array
 		ArrayList<ArrayList<String>> objectArray = objectFile.outputObjectArray();
 
 		
 		//load MEM
 		for(int inc = 0; inc < objectArray.size(); inc++)
 		{
 			
 			//create ArrayList<String> that is the line
 			ArrayList<String> line = objectArray.get(inc);
 			
 			//check line has length greater than 1	
 			if (line.size() > 1)
 			{
 				
 				if(line.get(0).equalsIgnoreCase("LT"))
 				{
 					
 					//create address variable
 					int address = Integer.parseInt(converter.hexToDec(line.get(1)));
 					
 					//load MEM
 					MEM[address] = line.get(2);
 					
 				}
 				
 			}
 			
 			//else there is an improper object file conversion
 			else
 			{				
 				//print error message
 				System.out.println("There is an improper object file.");
 			}
 			
 		}
 		
 		//get execution start address
 		String startHex = objectArray.get(0).get(1);
 		
 		//convert start address to decimal
 		int execStart = Integer.parseInt(converter.hexToDec(startHex));
 		
 		
 		
 		//start exec. case select by opcode
		while( !isHalt )
		{
			
			
			
			
			
			
			
		}
		
 		//get opcode and convert to integer
 		
 		//start case select
 		
 		
 
 	}
 
 }
