 package ecologylab.generic;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 
 public class ConfParser
 {
 	private File confFile;
 	private HashMap<String, String> confMap;
 	
 	public ConfParser(File confFile)
 	{
 		this.confFile = confFile;
 	}
 	
 	public void setConfFile(File confFile)
 	{
 		this.confFile = confFile;
 	}
 	
 	public HashMap<String, String> parse() throws FileNotFoundException
 	{
 		confMap = new HashMap<String, String>();
 		
 		if (!confFile.exists() || !confFile.canRead())
 		{
			System.err.println("Failed to parse conf file: " + confFile);
 			throw new FileNotFoundException();
 		}
 		
 		//go line by line and add to the hash map if it's not a comment;
 		Scanner scanner = new Scanner(confFile);
 		scanner.useDelimiter("=|\\n");
 		
 		while (scanner.hasNextLine())
 		{
 			//ignore comments
 			if (scanner.findInLine("#") != null)
 			{
 				scanner.nextLine();
 				continue;
 			}
 			
 			try
 			{
 			
 				String key 		= scanner.next().trim();
 				
 				String value 	= scanner.next().trim();
 				
 				System.out.println(key + ": " + value);
 				
 				confMap.put(key, value);
 			} catch (NoSuchElementException e)
 			{
 				break;
 			}
 		}
 		
 		return confMap;
 	}
 }
