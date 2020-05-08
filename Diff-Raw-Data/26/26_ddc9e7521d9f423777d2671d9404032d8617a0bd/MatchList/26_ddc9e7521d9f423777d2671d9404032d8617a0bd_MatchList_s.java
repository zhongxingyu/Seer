 package cleaner;
 
 import java.io.File;
 import java.util.ArrayList;
 
 //MatchList - contains all the files of a certain type (ie blacklist, whitelist, etc)
 public class MatchList
 {
 	public boolean useMatchList = true;
 	public ArrayList<ListFile> files = new ArrayList<ListFile>();
 	
 	public MatchList()
 	{
 		
 	}
 	
 	public void addFile(File path)
 	{
 		files.add(new ListFile(path));
 	}
 	
 	//returns -1 if not found
 	public int getListFile(String path) throws Exception
 	{
 		for(int i = 0; i < files.size(); i++)
 			if(files.get(i).listPath.equals(path))
 				return i;
 		throw new Exception("getListFile("+path+") file not found");
 	}
 	
 	public ArrayList<String> getUnrestictedContents()
 	{
 		ArrayList<String> allUnrestictedContents = new ArrayList<String>();
 		for(int i = 0; i < files.size(); i++)
 			for(String s : files.get(i).getUnrestrictedContents())
 				allUnrestictedContents.add(s);
 		return allUnrestictedContents;
 	}
 	
 	//return true if any of the lines in all the enabled files are contained in String str
 	public boolean hasMatch(String str)
 	{
 		for(int i = 0; i < files.size(); i++)
 			for(String match : files.get(i).getUnrestrictedContents())
 				if(str.contains(match))
 					return true;
 		return false;
 	}
 }
