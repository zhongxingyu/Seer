 //Simply call
 //java Debugger sample.txt NameExtractor
 
 
 
 import java.io.*;
 import java.util.*;
 import java.lang.reflect.*;
 public class Debugger{
 
 	
 
 
 	public static void main(String args[]) throws IOException {
 	
 		if(args.length != 3)
 		{
 			System.out.println("Too few arguments");
 			System.out.println("java Debugger [Input file] [Test Data Set] [Extractor]");
 			System.out.println("java Debugger sample.txt GoldenData.csv NameExtractor");
 			return;
 		}
 	
 	
 		String path = args[0];
 		
 		
 		FileReader f = new FileReader(path);
 		
 		String input = new String("");
 		
 		
 		
 		try {
 		input = readWithStringBuffer(f);
 		
 		DataSet ds = new DataSet(args[1]);
 			
 		
 		
 		
 		} finally {
 		f.close();
 		}
 
 		//Call Extractor
 
 		
 		 try {
             Class c = Class.forName(args[2]);
             
      		Extractor ne = (Extractor)c.newInstance();
             
             Method m[] = c.getDeclaredMethods();
             boolean initilized = false;
             boolean ran = false;
             
             // Object arglist[] = new Object[0];
 			for(int j = 0; j < 2; j++)
 			{
 				for (int i = 0; i < m.length; i++)
 				{
 					
 					
 					if(m[i].getName().equals(new String("init")))
 					{
 						m[i].invoke(ne,null);
 						initilized = true;
 					}
 					
 					if(!ran && initilized && m[i].getName().equals(new String("run")))
 					{
 						 Object arglist[] = new Object[1];
 							arglist[0] = new String(input);
 	
 						System.out.println(m[i].invoke(ne,arglist));
 						
 						ran = true;
 					}
 					
 					if(ran && initilized && m[i].getName().equals(new String("getNames")))
 					{
 						
 	
						HashMap map = (HashMap)m[i].invoke(ne,null);
 						
 						
 					}
 					
 				}
 			}
             
             
             
             
          }
          catch (Throwable e) {
             System.err.println(e);
          }
 
 		
 	
 
 	
 	}
 	
 	
 	static String readWithStringBuffer(Reader fr)
 		throws IOException {
 		
 		BufferedReader br = new BufferedReader(fr);
 		String line;
 		StringBuffer result = new StringBuffer();
 		while ((line = br.readLine()) != null) 
 		{
 			result.append(line);
 		}
 		
 		return result.toString();
 	}
 }
