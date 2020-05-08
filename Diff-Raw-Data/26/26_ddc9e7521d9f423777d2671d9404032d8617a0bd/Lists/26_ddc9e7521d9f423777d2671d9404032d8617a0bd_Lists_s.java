 package cleaner;
 import java.io.File;
 import java.util.ArrayList;
 
 public class Lists
 {
 	public static File listsDir = new File("lists/");
 	public static MatchList blacklist = new MatchList();
 	public static MatchList whitelist = new MatchList();
 	public static FolderList folders = new FolderList();
 	
 	public static void main(String[] args)
 	{
 		loadLists();
 //		for(String str : blackList)
 //			System.out.println("~"+str);
 		
 //		String testString = "-junk1-/filename@$#here.txt";
 //		System.out.println(testString);
 //		System.out.println(cleanString(testString));
 		
 //		for(String str : blacklist.getUnrestictedContents())
 //			System.out.println(str);
 		
 //		System.out.println("!~OBFUSCATED~!");
 //		for(String str : obfuscate(blackList))
 //		System.out.println(str);
 //		System.out.println("!~UNOBFUSCATED~!");
 //		for(String str : Cypher.unobfuscate(blackList))
 //			System.out.println(str);
 	}
 	
 	public static void loadLists()
 	{
 		//check all the files in the lists directory, and add them to their corresponding MatchLists
 		
 		ArrayList<File> files = SaveNLoad.getFilesRecur(listsDir);
 		for(File file : files)
 		{
 			if(file.getName().endsWith("Blacklist.txt"))
 			{
 				blacklist.addFile(file);
 			}
 			else if(file.getName().endsWith("Whitelist.txt"))
 			{
 				whitelist.addFile(file);
 			}
 			else if(file.getName().endsWith("Folders.txt"))
 			{
 				folders.addFile(file);
 			}
 			else
 			{
 				System.out.println("X "+file.getName());
 			}
 		}
 	}
 	
 	//change to lowercase & replace all special characters with spaces (except last period)
 	public static String cleanString(String str)
 	{
 //		replace special characters with spaces to avoid bad matches
 		boolean foundLastPeriod = false, foundLastSlash = false;
 		char[] newString = new char[str.length()];
 		for(int i = str.length()-1; i > 0; i--)
 		{
 			char c = str.charAt(i);
 			if(foundLastSlash)
 				newString[i] = ' ';
 			else if(Character.isLetterOrDigit(c))
 				newString[i] = c;
 			else
 			{
 				if(c == '.' && !foundLastPeriod && !foundLastSlash)
 				{
 					newString[i] = c;
 					foundLastPeriod = true;
 				}
 				else if(c == '/' && !foundLastSlash)
 				{
 					newString[i] = c;
 					foundLastSlash = true;
 				}
 				else
 					newString[i] = ' ';
 			}
 		}
 		String newStr = new String(newString).trim();
 		
 		newStr = newStr.toLowerCase();
 		
 		return newStr;
 	}
 	
 	public static boolean shouldMarkFile(String str)
 	{
		boolean hasMAtch = false;
		
		if(blacklist.useMatchList)
		{
			
		}
		
		if(blacklist.useMatchList)
		{
			
		}
		
		if(blacklist.useMatchList)
		{
			
		}
		
		return false;
 	}
 }
