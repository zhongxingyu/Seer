 package multimil;
 import java.io.*;
 import java.util.*;
 
 public class opHandler
 {
     private opCode codes[];
 	private int mode;
 	
     public opHandler(String filename, int mode)
     {
 		codes = new opCode[256];
 		FileReader file;
 		try
 		{
 			file = new FileReader(filename);
 			BufferedReader breader = new BufferedReader(file);
 		
 			try
 			{
 				while(breader.ready())
 				{
 					processLine(breader.readLine());
 				}
 			}
 			catch(IOException e)
 			{
 				System.out.println("trololo");
 		    }
 		
 		}
 		catch(FileNotFoundException e)
 		{
 			System.out.println("OP code file path is errorous.");
 		}
 		this.mode = mode;
 	
     }
     
     private void processLine(String line)
     {
 		StringTokenizer token = new StringTokenizer(line,"\t");
 		int num = Integer.valueOf(token.nextToken(),16);
 		opCode tmp = new opCode(Byte.valueOf(token.nextToken()),
 								token.nextToken(),
 								token.nextToken(),
 								token.nextToken(),
 								token.nextToken());
 		codes[num] = tmp;
 		
 
     }
     private String addr(int i)
     {
 		String out = Integer.toHexString(i);
 		while(out.length() <4)
 		{
 			out = '0'+ out;
 		}
 		return out;
     }
 
     private String hex(int i)
     {
 		String hex =Integer.toHexString(i); 
 		if (hex.length() == 1)
 			hex = '0' + hex;
 
 		return hex.toUpperCase();
     }
 
     public void generateInstructions( String filename)
     { 
 		int addrs = 0;
 		try
 		{
 			FileInputStream reader = new FileInputStream(filename);
 			while (reader.available() > 0)
 			{
 			
 				int data = reader.read();
 			
 				String hex = hex(data);
 				if (codes[data] != null)
 				{
 					
					switch (mode)
					{
					case 1:
						System.out.print(addr(addrs)+ "   "+ hex + "  "
										 +codes[data].name + "  ");
						break;
					case 2:
						System.out.print(addr(addrs)+ "\t" +codes[data].name);
						break;
					}

 					String arg = "";
 				
 					if (codes[data].addr.equals("A"))
 					{
 						arg = "A";
 					}
 					else if (codes[data].addr.equals("IM"))
 					{
 						//nothing
 					}
 					else if (codes[data].addr.equals("IMM"))
 					{
 						arg = "#$" + hex(reader.read());
 						addrs++;
 					}
 					else if (codes[data].addr.equals("IN"))
 					{
 						arg = hex(reader.read()) + ')';
 						arg = "($" + hex(reader.read()) + arg;
 						addrs++;
 						addrs++;
 					}
 					else if (codes[data].addr.equals("RE"))
 					{
 						arg = "$" + hex(reader.read());
 						addrs++;
 					}
 					else if (codes[data].addr.equals("ZP"))
 					{
 						arg = "$" + hex(reader.read());
 						addrs++;
 						if (!codes[data].arg.equals( "-"))
 						{
 							arg += ','+ codes[data].arg;
 						}
 					}
 					else if (codes[data].addr.equals("ABS"))
 					{
 						arg = hex(reader.read());
 						arg = "$" + hex(reader.read()) +arg;
 						addrs++;
 						addrs++;
 						if (!codes[data].arg.equals( "-"))
 						{
 							arg += ','+ codes[data].arg;
 						}
 					}
 					else if (codes[data].addr.equals("AB"))
 					{
 						arg = hex(reader.read());
 						arg = "$" + hex(reader.read()) +arg;
 						addrs++;
 						addrs++;
 
 					}
 					else if (codes[data].addr.equals("INIX"))
 					{
 						arg ="($" +  hex(reader.read()) +"),"+
 							codes[data].arg;
 						addrs++;
 					}
 					else if (codes[data].addr.equals("IXIN"))
 					{
 						arg ="($" +  hex(reader.read()) +","+ 
 							codes[data].arg + ")";
 						addrs++;
 					}
 					switch (mode)
 					{
 					case 1:
					
 						System.out.print(arg + "\t," +codes[data].size + 
 										 "  (" + codes[data].addr + ")\n");
 						break;
 					case 2:
						System.out.print( "\t" + arg + "\n");
 						break;
 					}
 						
 						
 			    }						
 				else
 				{
 					switch (mode)
 					{
 					case 1:
 						System.out.println(addr(addrs)+ "   "+hex  + "  -");
 						break;
 					case 2:
 						System.out.println(addr(addrs)+ "\t#"+ hex) ;
 						break;
 					}
 				}
 				addrs++;
 			}
 		}
 	
 		catch(IOException e)
 		{		
 		}
 
     }
 }
