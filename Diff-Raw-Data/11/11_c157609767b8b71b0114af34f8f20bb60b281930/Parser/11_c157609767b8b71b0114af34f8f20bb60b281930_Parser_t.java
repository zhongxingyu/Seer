 /*
  * Parser/Assembler for 429 in 2011
  * Regan Gunther
  */
 
 package authenticator;
 
 import gnu.io.*;
 import java.io.*;
 import java.util.*;
 import java.lang.*;
 
 /**
  *
  * @author regan89
  */
 public class Parser {
     
     Enumeration         portList;
     CommPortIdentifier  portId;
     String              messageString = "Hello, world!";
     SerialPort	    serialPort;
     OutputStream        outputStream;
     InputStream         inputStream;
     boolean             outputBufferEmptyFlag = false;
     String              defaultPort = "COM1"; 
     
     boolean portFound = false;
     
     //----------------
     // MEMORY
     //----------------
     char[][] imem = new char[4096][2]; //Instr mem. 12bit bus = 4k entries x 16bit
     char[] datamem = new char[65536];  //Data memory. 16bit bus = 65k entries x 8bit
     int pc = 0; //Local program counter
     static char OPCODE_MASK = 0xFC;
 
 
 
 
     //----------------------
     // Functions
     //---------------------
 	public String byteToString(byte b) {
 		String s = "";
 		
 		for (int i = 0; i < 8; i++) {
 			s = ('0' + (b >> i & 1)) + s;
 		}
 		
 		return s;
 	}
 
 	public String charToString(char c) {
 		String s = "";
 		
 		for (int i = 0; i < 8; i++) {
 			s = ('0' + (c >> i & 1)) + s;
 		}
 		
 		return s;
 	}
 	
 	public byte stringToBinaryByte(String s) {
 		int len = s.length();
 	
 		if (len > 8) {
 			len = 8;
 		}
 		
 		byte b = 0;
 		
 		for (int i = 0; i < 8; i++, b <<= 1) {
 			if (len - i >= 0) {
 				b |= s.charAt(len - i) - '0';
 			} else {
 				break;
 			}
 		}
 		
 		return b;
 	}
 	
     public void readFile()
     {
         try {
                 InputStream fstream = new FileInputStream("instr.txt");
                 //Get the object of DataInputStream
                 DataInputStream in = new DataInputStream(fstream);
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String strLine;
                 //Read File Line By Line
           
                 while ((strLine = br.readLine()) != null)   {
                     System.out.println (strLine);
                     
                     //For all lines in the file, decode to bytecode and add to mem[]
                     parseLine(strLine); //Decode from string to bytecode
                     //if ()
                 }
 
                 //Close the input stream
                 in.close();
 
         }catch (Exception e){
             //Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
 
     }
     
     public void parseLine(String line)
     {
         //Parse each line of the input file here.    
         if (line.startsWith("LDI"))
         {
             stringToLDIByteCode(line);
         } else if (line.startsWith("STI"))
         {
             stringToSTIByteCode(line);
         } else if (line.startsWith("ADD"))
         {
             stringToADDByteCode(line);
         } else if (line.startsWith("SUB"))
         {
             stringToSUBByteCode(line);
         } else if (line.startsWith("AND"))
         {
             stringToANDByteCode(line);
         } else if (line.startsWith("OR"))
         {
             stringToORByteCode(line);
         } else
         {
             System.out.println("Unsupported instruction encoutered.\n");
         }
 
     }
     
     
     //--------------------
     // Convert a LDI string to bytecode
     // LDI rx, v
     // 100001|v7|v6|v5|v4|v3|v2|v1|v0|x1|x0
     //
     //--------------------
     public char[] stringToLDIByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerToUse = 0;
         String register = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
         String value = string.substring(string.indexOf(",")+1).trim();
         int valueInt = Integer.parseInt(value);
 
 
         System.out.println("LDI: register = "+register+", value = "+valueInt+"\n");
         
         if (register.equals("r0"))
         {
             registerToUse = 0;
         } else if (register.equals("r1"))
 		{
             registerToUse = 1;
         } else if (register.equals("r2"))
         {
             registerToUse = 2;
         } else if (register.equals("r3"))
         {
             registerToUse = 3;
         }
 
         byteCode[0] = (char)(0x84 & OPCODE_MASK | (valueInt >> 6 & ~OPCODE_MASK));
         byteCode[1] = (char)((valueInt << 2 & OPCODE_MASK) | registerToUse);
 
         return byteCode;
     }
 
     //--------------------
     // Convert a STI string to bytecode
     // STI ay, v
     // 100101|v7|v6|v5|v4|v3|v2|v1|v0|v1|v0
     //
     //--------------------
     public char[] stringToSTIByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerToUse = 0;
         String register = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
         String value = string.substring(string.indexOf(",")+1).trim();
         int valueInt = Integer.parseInt(value);
 
 
         System.out.println("STI: register = "+register+", value = "+valueInt+"\n");
         
         if (register.equals("r0"))
         {
             registerToUse = 0;
         } else if (register.equals("r1"))
         {
             registerToUse = 1;
         } else if (register.equals("r2"))
         {
             registerToUse = 2;
         } else if (register.equals("r3"))
         {
             registerToUse = 3;
         }
 
         byteCode[0] = (char)(0x94 & OPCODE_MASK | (valueInt >> 6 & ~OPCODE_MASK));
         byteCode[1] = (char)((valueInt << 2 & OPCODE_MASK) | registerToUse); 
 
         return byteCode;
     
     }
 	
 	//--------------------
     // Convert a SIO string to bytecode - Store to IO bus
     // STI ax, a - (address immediate)
     // 100101|a7|a6|a5|a4|a3|a2|a1|a0|x1|x0
     //
     //--------------------
     public char[] stringToSIOByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerToUse = 0;
         String register = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
         String value = string.substring(string.indexOf(",")+1).trim();
         int valueInt = Integer.parseInt(value);
 
 
         System.out.println("SIO: register = "+register+", value = "+valueInt+"\n");
         
         if (register.equals("r0"))
         {
             registerToUse = 0;
         } else if (register.equals("r1"))
         {
             registerToUse = 1;
         } else if (register.equals("r2"))
         {
             registerToUse = 2;
         } else if (register.equals("r3"))
         {
             registerToUse = 3;
         }
 
         byteCode[0] = (char)(0x94 & OPCODE_MASK | (valueInt >> 6 & ~OPCODE_MASK));
         byteCode[1] = (char)((valueInt << 2 & OPCODE_MASK) | registerToUse); 
 
         return byteCode;
     
     }
 
 
     //--------------------
     // Convert a ADD string to bytecode
     // ADD ry, rx
     // 010010|0|0|y2|y1|y0|0|0|x2|x1|x0|
     //
     //--------------------
     public char[] stringToADDByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerXToUse = 0, registerYToUse = 0;
         String registerX = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
        String registerY = string.substring(string.indexOf(",")+1).trim();
         
         System.out.println("ADD: register y = "+registerY+", register x = "+registerX+"\n");
         
         if (registerX.equals("r0"))
         {
             registerXToUse = 0;
         } else if (registerX.equals("r1"))
         {
             registerXToUse = 1;
         } else if (registerX.equals("r2"))
         {
             registerXToUse = 2;
         } else if (registerX.equals("r3"))
         {
             registerXToUse = 3;
         }
 		
 		if (registerY.equals("r0"))
         {
             registerYToUse = 0;
         } else if (registerY.equals("r1"))
         {
             registerYToUse = 1;
         } else if (registerY.equals("r2"))
         {
             registerYToUse = 2;
         } else if (registerY.equals("r3"))
         {
             registerYToUse = 3;
         }
 
         byteCode[0] = 0x48;
         byteCode[1] = (char)((registerYToUse << 5 & 0xE0) | (registerXToUse & 0x07));
 
         return byteCode;
     
     }
 
     //--------------------
     // Convert a SUB string to bytecode
     // SUB ry, rx
     // 01101000|y2|y1|y0|0|0|x2|x1|x0|
     //
     //--------------------
     public char[] stringToSUBByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerXToUse = 0, registerYToUse = 0;
         String registerX = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
        String registerY = string.substring(string.indexOf(",")+1).trim();
         
         System.out.println("SUB: register y = "+registerY+", register x = "+registerX+"\n");
         
         if (registerX.equals("r0"))
         {
             registerXToUse = 0;
         } else if (registerX.equals("r1"))
         {
             registerXToUse = 1;
         } else if (registerX.equals("r2"))
         {
             registerXToUse = 2;
         } else if (registerX.equals("r3"))
         {
             registerXToUse = 3;
         }
 		
 		if (registerY.equals("r0"))
         {
             registerYToUse = 0;
         } else if (registerY.equals("r1"))
         {
             registerYToUse = 1;
         } else if (registerY.equals("r2"))
         {
             registerYToUse = 2;
         } else if (registerY.equals("r3"))
         {
             registerYToUse = 3;
         }
 
         byteCode[0] = 0x68;
         byteCode[1] = (char)((registerYToUse << 5 & 0xE0) | (registerXToUse & 0x07));
 
         return byteCode;
     }
 
     //--------------------
     // Convert a AND string to bytecode
     // AND ry, rx
     // 00001000|y2|y1|y0|0|0|x2|x1|x0|
     //
     //--------------------
     public char[] stringToANDByteCode(String string)
     {
 		char[] byteCode = new char[2]; //16 bit bytecode
         int registerXToUse = 0, registerYToUse = 0;
         String registerX = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
        String registerY = string.substring(string.indexOf(",")+1).trim();
         
         System.out.println("AND: register y = "+registerY+", register x = "+registerX+"\n");
         
         if (registerX.equals("r0"))
         {
             registerXToUse = 0;
         } else if (registerX.equals("r1"))
         {
             registerXToUse = 1;
         } else if (registerX.equals("r2"))
         {
             registerXToUse = 2;
         } else if (registerX.equals("r3"))
         {
             registerXToUse = 3;
         }
 		
 		if (registerY.equals("r0"))
         {
             registerYToUse = 0;
         } else if (registerY.equals("r1"))
         {
             registerYToUse = 1;
         } else if (registerY.equals("r2"))
         {
             registerYToUse = 2;
         } else if (registerY.equals("r3"))
         {
             registerYToUse = 3;
         }
 
         byteCode[0] = 0x8;
         byteCode[1] = (char)((registerYToUse << 5 & 0xE0) | (registerXToUse & 0x07));
 
         return byteCode;
     }
 
     //--------------------
     // Convert a OR string to bytecode
     // OR ry, rx
     // 00011000|y2|y1|y0|0|0|x2|x1|x0|
     //
     //--------------------
     public char[] stringToORByteCode(String string)
     {
         char[] byteCode = new char[2]; //16 bit bytecode
         int registerXToUse = 0, registerYToUse = 0;
         String registerX = string.substring(string.indexOf(" ")+1, string.indexOf(",")).trim();
        String registerY = string.substring(string.indexOf(",")+1).trim();
         
         System.out.println("OR: register y = "+registerY+", register x = "+registerX+"\n");
         
         if (registerX.equals("r0"))
         {
             registerXToUse = 0;
         } else if (registerX.equals("r1"))
         {
             registerXToUse = 1;
         } else if (registerX.equals("r2"))
         {
             registerXToUse = 2;
         } else if (registerX.equals("r3"))
         {
             registerXToUse = 3;
         }
 		
 		if (registerY.equals("r0"))
         {
             registerYToUse = 0;
         } else if (registerY.equals("r1"))
         {
             registerYToUse = 1;
         } else if (registerY.equals("r2"))
         {
             registerYToUse = 2;
         } else if (registerY.equals("r3"))
         {
             registerYToUse = 3;
         }
 
         byteCode[0] = 0x18;
         byteCode[1] = (char)((registerYToUse << 5 & 0xE0) | (registerXToUse & 0x07));
 
         return byteCode;
     }
 	
 	public char[] encodeRegisterImmediateInstruction(byte opCode, String string) {
 		char[] byteCode = new char[2]; //16 bit bytecode
         int registerToUse = 0;
         String register = string.substring(0, string.indexOf(",")).trim();
         String value = string.substring(string.indexOf(",")+1).trim();
         int valueInt = Integer.parseInt(value);
 
 
         System.out.println("Opcode " + byteToString(opCode) + ": register = "+register+", value = "+valueInt+"\n");
         
         if (register.equals("r0"))
         {
             registerToUse = 0;
         } else if (register.equals("r1"))
 		{
             registerToUse = 1;
         } else if (register.equals("r2"))
         {
             registerToUse = 2;
         } else if (register.equals("r3"))
         {
             registerToUse = 3;
         }
 
         byteCode[0] = (char)(0x84 & OPCODE_MASK | (valueInt >> 6 & ~OPCODE_MASK));
         byteCode[1] = (char)((valueInt << 2 & ~OPCODE_MASK) | registerToUse);
 
         return byteCode;
 	}
 
 	public char[] encodeRegisterRegisterInstruction(byte opCode, String string) {
 		char[] byteCode = new char[2]; //16 bit bytecode
         int registerXToUse = 0, registerYToUse = 0;
         String registerX = string.substring(0, string.indexOf(",")).trim();
         String registerY = string.substring(string.indexOf(",")+1).trim();
         
         System.out.println("Opcode "+opCode+": register y = "+registerY+", register x = "+registerX+"\n");
         
         if (registerX.equals("r0"))
         {
             registerXToUse = 0;
         } else if (registerX.equals("r1"))
         {
             registerXToUse = 1;
         } else if (registerX.equals("r2"))
         {
             registerXToUse = 2;
         } else if (registerX.equals("r3"))
         {
             registerXToUse = 3;
         }
 		
 		if (registerY.equals("r0"))
         {
             registerYToUse = 0;
         } else if (registerY.equals("r1"))
         {
             registerYToUse = 1;
         } else if (registerY.equals("r2"))
         {
             registerYToUse = 2;
         } else if (registerY.equals("r3"))
         {
             registerYToUse = 3;
         }
 
         byteCode[0] = (char)(opCode << 2);
         byteCode[1] = (char)((registerYToUse << 5 & 0xE0) | (registerXToUse & 0x07));
 
         return byteCode;
 	}
     
     //---------------------
     // Initialise the UART etc
     //---------------------
     public void runSerial()
     {
         portList = CommPortIdentifier.getPortIdentifiers();
             
 
         while (portList.hasMoreElements()) {
             portId = (CommPortIdentifier) portList.nextElement();
 
             if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
 
             if (portId.getName().equals(defaultPort)) {
                 System.out.println("Found port " + defaultPort);
 
                 portFound = true;
 
                 try {
                     serialPort = (SerialPort) portId.open("SimpleWrite", 2000);
                 } catch (PortInUseException e) {
                     System.out.println("Port in use.");
                     continue;
                 } 
 
                 try {
                     outputStream = serialPort.getOutputStream();
                     inputStream = serialPort.getInputStream();
                 } catch (IOException e) {}
 
                 try {
                     serialPort.setSerialPortParams(19200, 
                                    SerialPort.DATABITS_8, 
                                    SerialPort.STOPBITS_1, 
                                    SerialPort.PARITY_NONE);
                 } catch (UnsupportedCommOperationException e) {}
         
 
                 try {
                     serialPort.notifyOnOutputEmpty(true);
                 } catch (Exception e) {
                     System.out.println("Error setting event notification");
                     System.out.println(e.toString());
                     System.exit(-1);
                 }
                 
                 
                 System.out.println(
                     "Writing \""+messageString+"\" to " +serialPort.getName());
 
                 //try {
                 //outputStream.write(messageString.getBytes());
                 //} catch (IOException e) {}
 
                 //try {
                //    Thread.sleep(2000);  // Be sure data is xferred before closing
                // } catch (Exception e) {}
                 serialPort.close();
                 //System.exit(1);
             } 
             } 
         } 
 
         if (!portFound) {
             System.out.println("port " + defaultPort + " not found.");
         } 
     }
     
     //-----------------------
     // Write data over the UART
     //-----------------------
     public boolean writeData(String messageString)
     {
         try {
             outputStream.write(messageString.getBytes());
         } catch (Exception e) 
         {
             System.out.println("Error writing to stream.");
         }
         return true;
     }
     
     //-----------------------
     // Read data from UART
     //----------------------
     public byte[] readData() throws IOException
     {
         int bytesAvailable = 0;
         byte[] arrToReturn = {};
         
         try {
              bytesAvailable = inputStream.available();
         } catch (Exception e){System.out.println("Error reading from stream.");}
         
         inputStream.read(arrToReturn, 0, 9);
         
         return arrToReturn;
     }
 
 
     public static void main(String[] args)
     {
         Parser parser = new Parser();
         parser.readFile();
 
     }
 }
