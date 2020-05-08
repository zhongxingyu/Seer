 package multimil;
 import java.io.*;
 import java.util.*;
 
 public class opHandler
 {
 	private opCode codes[];
 	public opHandler(String filename)
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
 
 				return hex;
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
 					System.out.print(addr(addrs)+ "   "+ hex + "  "
 									   +codes[data].name + "  ");

					String arg = "";

 					for(int i = 1; i < codes[data].size && 
 							reader.available() > 0; i++)
 					{
						arg = hex(reader.read()) + arg;
 					}

					System.out.print(arg + "\t," +codes[data].size + 
 								"  (" + codes[data].addr + ")\n");
 				}						
 				else
 				{
 					System.out.println(addr(addrs)+ "   "+hex  + "  -");
 				}
 				addrs++;
 			}
 		}
 		
 		catch(IOException e)
 		{
 			
 		}
 	}
 }
