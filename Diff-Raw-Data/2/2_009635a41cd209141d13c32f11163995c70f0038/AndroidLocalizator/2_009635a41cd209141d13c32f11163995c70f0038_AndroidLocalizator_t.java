 package fr.hartok.util;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 public class AndroidLocalizator {
 	
 	public static void main(String[] args)
 	{
 		if(args.length > 1 && args[0] instanceof String && args[1] instanceof String)
 		{
 			AndroidLocalizator obj = new AndroidLocalizator();
 			obj.run( (String) args[0], (String) args[1] );
 		}
 		else
 		{
 			System.err.println( "Missing argument." );
 		}
 	}
 	
 	public void run(final String path, final String outpuDir)
 	{	
 		BufferedReader br = null;
 		String line = "";
 		
 		try
 		{
 			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
 			
 			// reading tags
 			if ((line = br.readLine()) != null)
 			{
 				// use comma as separator
 				Object[] tags = splitLine(line).toArray();
 				
 				if(tags.length > 1)
 				{
 					final int languageCount = (tags.length - 1);
 					LanguageExporter[] languages = new LanguageExporter[languageCount];
 					for(int i = 0; i < languageCount; ++i)
 						languages[i] = new LanguageExporter( (String) tags[i+1] );
 					
 					while ((line = br.readLine()) != null)
 					{
 						List<String> data = splitLine(line);
 						
 						if(data.size() > 0 && !data.get(0).isEmpty())
 						{
 							String stringId = data.get(0); 
 							
 							for(int i = 1; (i < languageCount+1) && (i < data.size()); ++i)
 							{
 								if(!data.get(i).isEmpty())
 									languages[i-1].appendLocalizedString(stringId, data.get(i));
 							}
 						}
 					}
 					
 					// create output dir
 					new File(outpuDir).mkdirs();
 					
 					for(int i = 0; i < languageCount; ++i)
 						languages[i].export(outpuDir);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		System.out.println("Done");
 	}
 	
 	protected static List<String> splitLine(String line)
 	{
 		List<String> parts = new LinkedList<String>( Arrays.asList(line.split(",")) ) ;
 		
 		int i = 0;
 		while(i < parts.size())
 		{
 			String part = parts.get(i); 
 			if(part.startsWith("\""))
 			{
 				if(!part.endsWith("\""))
 				{
 					int j = i+1;
 					boolean success = false;
 					
 					if(j < parts.size())
 					{
 						do
 						{
 							part += "," + parts.remove(j);
 							success = part.endsWith("\"") && !part.endsWith("\\\"");
 						} 
 						while (!success && j < parts.size());						
 					}
 					
 					if(!success)
 						throw new RuntimeException("CSV format issue with double quotes");
 				}
 				
				String cleanedString = part.substring( 1, part.length() - 1 );
 				parts.set(i, cleanedString);
 			}
 			
 			++i;
 		}
 		
 		return parts;
 	}
 }
